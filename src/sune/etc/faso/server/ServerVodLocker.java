package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;
import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;

public class ServerVodLocker implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	
	static {
		REGEX_IFRAME_URL 	= "^https?://(?:www\\.)?vodlocker\\.com/embed-(.*?)(-(.*?))?\\.html$";
		JWPLAYER_SETUP_TEXT = "jwplayer(\"flvplayer\").setup(";
	}
	
	static String videoJS(Document doc) {
		for(Element el : doc.select("script[type]")) {
			String content = el.html();
			if(content.indexOf(JWPLAYER_SETUP_TEXT) > -1) {
				return content;
			}
		}
		return "";
	}
	
	static String playerSetupData(String js) {
		try {
			int index = js.indexOf(JWPLAYER_SETUP_TEXT);
			if((index >= 0)) {
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
				return text.substring(0, i+1);
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	static String formatPlayerData(String data) {
		try {
			boolean name  = true;
			boolean dq 	  = false;
			boolean sq 	  = false;
			boolean found = false;
			boolean comma = false;
			int bracket   = 0;
			StringBuilder sb = new StringBuilder();
			StringBuilder sn = new StringBuilder();
			char[] chars = data.toCharArray();
			for(int i = data.indexOf('{')+1, l = chars.length, c; i < l; ++i) {
				c = chars[i];
				// Skip the value if the correct item is found
				if(found) {
					if(comma) {
						if(c == ',') {
							// Put there null instead of the value
							sb.append("null");
							sb.append(',');
							found = false;
						}
					} else {
						if(c == '{') {
							++bracket;
						} else if(c == '}') {
							if(--bracket == 0) {
								// Comma should be next
								comma = true;
							}
						}
					}
				} else {
					if(name) {
						if(c == ':') {
							name = false;
							String sname = sn.toString()
											 .trim()
											 .toLowerCase();
							// Check current name with the names of items
							// that should be skipped
							if(sname.equals("events") ||
							   sname.equals("plugins")) {
								found = true;
							}
							sn.setLength(0);
						} else {
							sn.append((char) c);
						}
					} else {
						// Quotes logic
						if(c == '\"' && !sq) dq = !dq; else
						if(c == '\'' && !dq) sq = !sq; else
						if(!dq && !sq) {
							if(c == ',') {
								name = true;
							}
						}
					}
					sb.append((char) c);
				}
			}
			return '{' + sb.toString();
		} catch(Exception ex) {
		}
		return null;
	}
	
	ServerVodLocker() {
	}
	
	@Override
	public VideoSource[] getVideoSources(Document document) {
		List<VideoSource> sources = new ArrayList<>();
		List<String> list 		  = new ArrayList<>();
		// Test iframes with the correct source url
		Elements iframes = document.select("iframe[src]");
		if(iframes.size() > 0) {
			Pattern pattern = Pattern.compile(REGEX_IFRAME_URL);
			for(Element iframe : iframes) {
				String src 		= iframe.attr("src");
				Matcher matcher = pattern.matcher(src);
				if(matcher.matches()) {
					list.add(src);
				}
			}
		}
		if(!list.isEmpty()) {
			for(String url : list) {
				try {
					Document doc 	  = Utils.getDocument(url);
					String jsVideo 	  = videoJS(doc);
					String playerData = playerSetupData(jsVideo);
					// Format the player data first
					playerData 		  = formatPlayerData(playerData);
					SSDCollection arr = SSDF.read(playerData);
					if(arr.hasObject("file")) {
						String furl	   = arr.getObject("file").stringValue();
						long fileSize  = Utils.getFileSize_Type(furl);
						VideoSource vs = new VideoSource(
							this, new URL(furl), VideoFormat.get(furl),
							null, fileSize, null, VideoQuality.QUALITY_UNKNOWN,
							null);
						sources.add(vs);
					}
				} catch(Exception ex) {
				}
			}
		}
		return sources.toArray(new VideoSource[sources.size()]);
	}
	
	@Override
	public String getAddress() {
		return "http://vodlocker.com/";
	}
	
	@Override
	public String getName() {
		return "VodLocker";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}