package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.util.UserAgent;
import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;

public class ServerAnyFiles implements Server {
	
	private static final String REGEX_IFRAME_URL;
	
	static {
		REGEX_IFRAME_URL = "^http://(?:www\\.)?video\\.anyfiles\\.pl/w.jsp\\?id=(\\d+)(?:&(?:.*?))?$";
	}
	
	static String videoPlayLink(String videoID) {
		return "http://video.anyfiles.pl/w.jsp?id=" + videoID +
			   "&width=640&height=360&start=0&skin=0&autostart=false&browser=html";
	}
	
	ServerAnyFiles() {
	}
	
	@Override
	public VideoSource[] getVideoSource(Document document) {
		List<VideoSource> sources = new ArrayList<>();
		List<String> list 		  = new ArrayList<>();
		// Test iframes with the correct source url
		Elements iframes = document.select("iframe[src]");
		if(iframes.size() > 0) {
			Pattern pattern = Pattern.compile(REGEX_IFRAME_URL);
			for(Element iframe : iframes) {
				String src = iframe.attr("src");
				Matcher matcher = pattern.matcher(src);
				if(matcher.matches()) {
					list.add(matcher.group(1));
				}
			}
		}
		if(!list.isEmpty()) {
			for(String videoID : list) {
				try {
					String url  = videoPlayLink(videoID);
					String html = Utils.request("GET", url, UserAgent.MOZILLA2,
						null, null, null, Utils.headers("Referer=\""+url+"\"")).result;
					Document doc   = Utils.createDocument(html);
					String dataURL = null;
					for(Element script : doc.select("script[src]")) {
						String src = script.attr("src");
						if(src.startsWith("/pcs?code=")) {
							dataURL = "http://video.anyfiles.pl" + src;
							break;
						}
					}
					if(dataURL != null) {
						// Get the video data (JavaScript)
						String data = Utils.request("GET", dataURL, UserAgent.MOZILLA2,
							null, null, null, Utils.headers("Referer=\""+url+"\"")).result;
						// Get the source URL in source variable
						StringBuilder sb = new StringBuilder();
						char[] chars = data.toCharArray();
						boolean value = false;
						boolean quotes = false;
						String svalue = null;
						for(int i = 0, l = chars.length; i < l; ++i) {
							char c = chars[i];
							if(value) {
								if(c == '"') {
									quotes = !quotes;
								} else if(c == ';' && !quotes) {
									svalue = sb.toString().trim();
									sb.setLength(0);
									break;
								} else {
									sb.append(c);
								}
							} else {
								if(c == '=') {
									if(sb.toString().trim()
										 .equals("var source")) {
										value = true;
									}
									sb.setLength(0);
								} else {
									sb.append(c);
								}
							}
						}
						// Get src attribute's value
						int index0 = svalue.indexOf("src=\\");
						int index1 = svalue.indexOf("\\", index0+5);
						if(index0 > -1 && index1 > -1) {
							String source  = svalue.substring(index0+5, index1);
							long fileSize  = Utils.getFileSizeURL(source);
							VideoSource vs = new VideoSource(this, new URL(source),
								VideoFormat.MP4, null, fileSize, null, null, null);
							sources.add(vs);
						}
					}
				} catch(Exception ex) {
				}
			}
		}
		return sources.toArray(new VideoSource[sources.size()]);
	}
	
	@Override
	public String getAddress() {
		return "https://anyfiles.pl/";
	}
	
	@Override
	public String getName() {
		return "AnyFiles";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}