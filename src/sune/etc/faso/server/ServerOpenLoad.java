package sune.etc.faso.server;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.util.JavaScript;
import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;

public class ServerOpenLoad implements Server {
	
	private static final String REGEX_IFRAME_URL = "^https:\\/\\/openload\\.co\\/embed\\/(.*?)\\/$";
	private static final String AAENCODE_PREFIX  = "\uff9f\u03c9\uff9f\uff89";
	
	ServerOpenLoad() {
	}
	
	@Override
	public VideoSource getVideoSource(Document document) {
		String videoURL  = null;
		// Test iframes with the correct source url
		Elements iframes = document.select("iframe[src]");
		if(iframes.size() > 0) {
			Pattern pattern = Pattern.compile(REGEX_IFRAME_URL);
			for(Element iframe : iframes) {
				String src 		= iframe.attr("src");
				Matcher matcher = pattern.matcher(src);
				if(matcher.matches()) {
					Document diframe = Utils.getDocument(src);
					Elements scripts = diframe.select("script[type]");
					if(scripts.size() > 0) {
						for(Element script : scripts) {
							String con = script.html();
							if(con.startsWith(AAENCODE_PREFIX)) {
								String value = Utils.aadecode(con).replace("window.", "var ");
								videoURL 	 = (String) JavaScript.execute(value + "vs;");
								break;
							}
						}
					}
					break;
				}
			}
		}
		if(videoURL != null) {
			// Get an exact file URL from the gotten stream URL
			try {
				// Openload.co uses HTTPS protocol
				HttpsURLConnection con =
					(HttpsURLConnection) new URL(videoURL).openConnection();
				con.setInstanceFollowRedirects(false);
				con.connect();
				
				// Catch the redirection
				if(con.getResponseCode() == 302) {
					// Get the redirection URL
					videoURL = con.getHeaderField("Location");
				}
				long fileSize = Utils.getFileSizeURL(videoURL);
				return new VideoSource(this, new URL(videoURL), VideoFormat.MP4, fileSize);
			} catch(Exception ex) {
			}
		}
		return null;
	}
	
	@Override
	public String getAddress() {
		return "https://openload.co/";
	}
	
	@Override
	public String getName() {
		return "OpenLoad";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}