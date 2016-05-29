package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.subtitles.Subtitles;
import sune.etc.faso.util.JavaScript;
import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;
import sune.ssdf.SSDArray;
import sune.ssdf.SSDFCore;

public class ServerLetWatch implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	
	static {
		REGEX_IFRAME_URL 	= "^http://(?:www\\.)?letwatch\\.us/embed-(.*?)(?:-(?:.*?))?\\.html$";
		JWPLAYER_SETUP_TEXT = "jwplayer(\"vplayer\").setup(";
	}
	
	static String videoPlayLink(String videoID) {
		return "http://letwatch.us/embed-" + videoID + ".html";
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
	
	ServerLetWatch() {
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
					String playURL 	  = videoPlayLink(videoID);
					Document doc 	  = Utils.getDocument(playURL);
					String jsVideo 	  = videoJS(doc);
					String result 	  = (String) JavaScript.execute(jsVideo);
					String playerData = playerSetupData(result);
					SSDArray array 	  = new SSDFCore(playerData).getArray();
					
					List<Subtitles> listSubs = new ArrayList<>();
					if(array.hasArray("tracks")) {
						SSDArray arrTracks = array.getArray("tracks");
						Map<String, Map<String, String>> tracks = Utils.convert(arrTracks);
						for(Map<String, String> map : tracks.values()) {
							if(map.get("kind").equals("captions")) {
								String surl = map.get("file");
								String lang = map.get("label");
								listSubs.add(new Subtitles(surl, lang));
							}
						}
					}
					
					if(array.hasArray("sources")) {
						SSDArray arrSources = array.getArray("sources");
						Subtitles[] subs = listSubs.toArray(new Subtitles[listSubs.size()]);
						if(subs.length == 0) subs = null;
						Map<String, Map<String, String>> arrsrcs = Utils.convert(arrSources);
						for(Map<String, String> map : arrsrcs.values()) {
							String furl    = map.get("file");
							String quality = map.get("label");
							long fileSize  = Utils.getFileSizeURL(furl);
							VideoSource vs = new VideoSource(this, new URL(furl),
								VideoFormat.FLV, null, fileSize, null, VideoQuality.get(quality), subs);
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
		return "http://letwatch.us/";
	}
	
	@Override
	public String getName() {
		return "LetWatch";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}