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
import sune.util.ssdf2.SSDFilter;
import sune.util.ssdf2.SSDNode;

public class ServerAllMyVideos implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	private static final String JWPLAYER_SETUP_END;
	
	static {
		REGEX_IFRAME_URL 	= "^https?://(?:www\\.)?allmyvideos\\.net/embed-(.*?)\\.html(?:\\?.*?)?(?:#.*?)?$";
		JWPLAYER_SETUP_TEXT = "jwplayer('flvplayer').setup(jwConfig(";
		JWPLAYER_SETUP_END	= "));";
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
				int index0 = js.lastIndexOf(JWPLAYER_SETUP_END);
				if((index0) >= 0) {
					index += JWPLAYER_SETUP_TEXT.length();
					return js.substring(index, index0).trim();
				}
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	ServerAllMyVideos() {
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
					SSDCollection arr = SSDF.readJSON(playerData);
					// Sources are contained in playlist array
					if(arr.hasCollection("playlist")) {
						for(SSDNode item : arr.getCollection("playlist")
											  .filter(SSDFilter.ONLY_COLLECTIONS)) {
							SSDCollection icol = (SSDCollection) item;
							if(icol.hasCollection("sources")) {
								for(SSDNode node : icol.getCollection("sources")
													   .filter(SSDFilter.ONLY_COLLECTIONS)) {
									SSDCollection coll = (SSDCollection) node;
									// Add the video source to the list
									String furl    = coll.getObject("file").stringValue();
									String quality = null;
									if(coll.hasObject("label"))
										quality = coll.getObject("label").stringValue();
									long fileSize  = Utils.getFileSize_Type(furl);
									VideoSource vs = new VideoSource(
										this, new URL(furl), VideoFormat.get(furl),
										null, fileSize, null, VideoQuality.get(quality),
										null);
									sources.add(vs);
								}
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
		return "http://allmyvideos.net/";
	}
	
	@Override
	public String getName() {
		return "AllMyVideos";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}