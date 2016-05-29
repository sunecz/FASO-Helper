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
import sune.etc.faso.util.UserAgent;
import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;
import sune.ssdf.SSDArray;
import sune.ssdf.SSDFCore;

public class ServerYouWatch implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	
	static {
		REGEX_IFRAME_URL 	= "^http://(?:www\\.)?youwatch\\.org/embed-(.*?)(?:-(?:.*?))?\\.html$";
		JWPLAYER_SETUP_TEXT = "jwplayer(\"vplayer\").setup(";
	}
	
	static String playerSetupData(String html) {
		try {
			int index = html.indexOf(JWPLAYER_SETUP_TEXT);
			if(index > -1) {
				index += JWPLAYER_SETUP_TEXT.length();
				String text  = html.substring(index);
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
	
	ServerYouWatch() {
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
					Document iframe = Utils.getDocument(url);
					Elements frames = iframe.select("iframe[src]");
					if(frames.size() > 0) {
						String src 	   	  = frames.first().attr("src");
						String result  	  = Utils.request("GET", src, UserAgent.MOZILLA).result;
						String headers 	  = String.format("Referer=\"%s\"", src);
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
								long fileSize  = Utils.getFileSizeURL(furl, headers);
								VideoSource vs = new VideoSource(this, new URL(furl),
									VideoFormat.MP4, Utils.headers(headers), fileSize,
									null, VideoQuality.get(quality), subs);
								sources.add(vs);
							}
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
		return "http://youwatch.org/";
	}
	
	@Override
	public String getName() {
		return "YouWatch";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}