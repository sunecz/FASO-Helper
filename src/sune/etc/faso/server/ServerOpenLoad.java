package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.subtitles.Subtitles;
import sune.etc.faso.util.JavaScript;
import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;

public class ServerOpenLoad implements Server {
	
	private static final String REGEX_EMBED_URL;
	private static final String AAENCODE_PREFIX;
	
	static {
		REGEX_EMBED_URL = "^https?://(?:www\\.)?openload\\.(?:co|io)/embed/(.*?)/$";
		AAENCODE_PREFIX	= "\uff9f\u03c9\uff9f\uff89";
	}
	
	ServerOpenLoad() {
	}
	
	@Override
	public VideoSource[] getVideoSource(Document document) {
		List<VideoSource> sources = new ArrayList<>();
		List<String> list 		  = new ArrayList<>();
		// Test all iframes with src attribute
		Elements iframes = document.select("iframe[src]");
		if(iframes.size() > 0) {
			Pattern pattern = Pattern.compile(REGEX_EMBED_URL);
			for(Element iframe : iframes) {
				String src 		= iframe.attr("src");
				Matcher matcher = pattern.matcher(src);
				if(matcher.matches()) {
					list.add(src);
				}
			}
		}
		// Test all anchors with href attribute
		Elements anchors = document.select("a[href]");
		if(anchors.size() > 0) {
			Pattern pattern = Pattern.compile(REGEX_EMBED_URL);
			for(Element anchor : anchors) {
				String href 	= anchor.attr("href");
				Matcher matcher = pattern.matcher(href);
				if(matcher.matches()) {
					list.add(href);
				}
			}
		}
		if(!list.isEmpty()) {
			for(String url : list) {
				Document diframe = Utils.getDocument(url);
				Elements scripts = diframe.select("script[type]");
				if(scripts.size() > 0) {
					for(Element script : scripts) {
						String con = script.html();
						if(con.startsWith(AAENCODE_PREFIX)) {
							String value 	= Utils.aadecode(con).replace("window.", "var ");
							String videoURL = (String) JavaScript.execute(value + "vs;");
							// The vs variable does not have to be visible for the first time,
							// it can be wrapped in another variable or it can be a variable
							// with completely different name.
							if(videoURL == null && value.startsWith("var ")) {
								String string  = value.substring(4);
								int eqsign	   = string.indexOf('=');
								String varname = string.substring(0, eqsign).trim();
								// The variable could also be an array, check the variable's name
								boolean varArray = false;
								for(int i = 0, l = varname.length(); i < l; ++i) {
									if((varname.charAt(i) == '[')) {
										varname  = varname.substring(0, i);
										varArray = true;
										break;
									}
								}
								String execute = value + varname;
								// If the variable is an array, save the result into
								// a variable not an array, so that the future value
								// can be casted into a string
								if(varArray) {
									execute = "var " + varname + "=" +
											  execute.substring(
													  execute.indexOf('=') + 1);
								}
								Object vobject;
								if((vobject = JavaScript.execute(execute)) != null) {
									// Test whether the variable can be casted to a string
									if(vobject.getClass().isAssignableFrom(String.class)) {
										// Cast the video URL to a string, so it can be read
										videoURL = (String) vobject;
									} else continue;
									// Get an exact file URL from the gotten stream URL
									try {
										// Openload.co uses HTTPS protocol
										HttpsURLConnection hcon =
											(HttpsURLConnection) new URL(videoURL).openConnection();
										hcon.setInstanceFollowRedirects(false);
										hcon.connect();
										
										// Catch the redirection
										if(hcon.getResponseCode() == 302) {
											// Get the redirection URL
											videoURL = hcon.getHeaderField("Location");
											// Check if the video URL is not an URL for the video
											// with pigeons (aka video that informs people that the
											// hotlinking is not allowed)
											if(videoURL.contains("/Komp+1.mp4"))
												continue;
										}
										
										// Get all subtitles
										List<Subtitles> listSubs = new ArrayList<>();
										for(Element track : diframe.select("track[src]")) {
											if(track.attr("kind").equals("captions")) {
												String surl = track.attr("src");
												String lang = track.attr("label");
												listSubs.add(new Subtitles(surl, lang));
											}
										}
										
										Subtitles[] subs = listSubs.toArray(new Subtitles[listSubs.size()]);
										if(subs.length == 0) subs = null;
										long fileSize  	 = Utils.getFileSizeURL(videoURL);
										VideoSource vs 	 = new VideoSource(this, new URL(videoURL),
											VideoFormat.MP4, null, fileSize, null,
											VideoQuality.QUALITY_UNKNOWN, subs);
										sources.add(vs);
									} catch(Exception ex) {
									}
								}
							}
						}
					}
				}
			}
		}
		return sources.toArray(new VideoSource[sources.size()]);
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