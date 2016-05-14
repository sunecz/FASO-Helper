package sune.etc.faso.server;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.util.JavaScript;
import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;
import sune.ssdf.SSDArray;
import sune.ssdf.SSDFCore;

public class ServerFlashXTV implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	
	static {
		REGEX_IFRAME_URL 	= "^http://(?:www\\.)?flashx\\.tv/embed-(.*?)(?:-(?:.*?))?\\.html$";
		JWPLAYER_SETUP_TEXT = "jwplayer(\"vplayer\").setup(";
	}
	
	static String videoPlayLink(String videoID) {
		return "http://www.flash-x.tv/playthis-" + videoID + ".html";
	}
	
	static String videoJS(Document doc) {
		try {
			for(Element script : doc.select("script[type]")) {
				String html = script.html();
				if(html.startsWith("eval"))
					// Additionally remove the eval text but only
					// from the beggining of the gotten html
					return html.substring(4);
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	static String playerSetupData(String js) {
		try {
			int index = js.indexOf(JWPLAYER_SETUP_TEXT);
			if(index > -1) {
				index += JWPLAYER_SETUP_TEXT.length();
				String text  = js.substring(index);
				char[] chars = text.toCharArray();
				int i = 0, b = 0;
				for(int l = chars.length; i < l; ++i) {
					char c = chars[i];
					if(c == '{') ++b; else
					if(c == '}') {
						if(--b == 0) break;
					}
				}
				return text.substring(0, i);
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	ServerFlashXTV() {
	}
	
	@Override
	public VideoSource getVideoSource(Document document) {
		String videoID = null;
		// Test iframes with the correct source url
		Elements iframes = document.select("iframe[src]");
		if(iframes.size() > 0) {
			Pattern pattern = Pattern.compile(REGEX_IFRAME_URL);
			for(Element iframe : iframes) {
				String src = iframe.attr("src");
				Matcher matcher = pattern.matcher(src);
				if(matcher.matches()) {
					videoID = matcher.group(1);
					break;
				}
			}
		}
		if(videoID != null) {
			try {
				String playURL 	  = videoPlayLink(videoID);
				Document doc 	  = Utils.getDocument(playURL);
				String jsVideo 	  = videoJS(doc);
				String result 	  = (String) JavaScript.execute(jsVideo);
				String playerData = playerSetupData(result);
				SSDArray array 	  = new SSDFCore(playerData).getArray();
				String videoURL   = array.getObject("sources.0.file").stringValue();
				long fileSize 	  = Utils.getFileSizeURL(videoURL);
				return new VideoSource(this, new URL(videoURL), VideoFormat.MP4, fileSize);
			} catch(Exception ex) {
			}
		}
		return null;
	}
	
	@Override
	public String getAddress() {
		return "http://flashx.tv/";
	}
	
	@Override
	public String getName() {
		return "FlashXTV";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}