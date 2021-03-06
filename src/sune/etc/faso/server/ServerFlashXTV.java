package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import sune.etc.faso.subtitles.Subtitles;
import sune.etc.faso.util.JavaScript;
import sune.etc.faso.util.Utils;
import sune.etc.faso.util.Utils.Selector;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;
import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;
import sune.util.ssdf2.SSDNode;
import sune.util.ssdf2.SSDObject;

public class ServerFlashXTV implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	
	static {
		REGEX_IFRAME_URL 	= "^https?://(?:www\\.)?flashx\\.tv/embed-(.*?)(?:-(?:.*?))?\\.html(?:\\?.*?)?(?:#.*?)?$";
		JWPLAYER_SETUP_TEXT = "jwplayer(\"vplayer\").setup(";
	}
	
	static String videoPlayLink(String videoID) {
		return "http://www.flash-x.tv/playvid-" + videoID + ".html";
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
	public VideoSource[] getVideoSources(Document document) {
		List<VideoSource> sources = new ArrayList<>();
		List<String> 	  list 	  = Utils.search(document,
			((element, matcher, value) -> matcher.group(1)),
			new Selector("iframe[src]", "src",  Pattern.compile(REGEX_IFRAME_URL)),
			new Selector("a[href]",		"href", Pattern.compile(REGEX_IFRAME_URL)));
		if(!list.isEmpty()) {
			for(String videoID : list) {
				try {
					String playURL 	  = videoPlayLink(videoID);
					Document doc 	  = Utils.getDocument(playURL);
					String jsVideo 	  = videoJS(doc);
					String result 	  = (String) JavaScript.execute(jsVideo);
					String playerData = playerSetupData(result);
					SSDCollection arr = SSDF.read(playerData);
					
					List<Subtitles> listSubs = new ArrayList<>();
					if(arr.hasCollection("tracks")) {
						for(SSDNode node : arr.getCollection("tracks")) {
							if(node.isCollection()) {
								SSDCollection coll = (SSDCollection) node;
								SSDObject 	  kind = coll.getObject("kind");
								if(kind != null && kind.stringValue().equals("captions")) {
									String surl = coll.getObject("file").stringValue();
									String lang = coll.getObject("label").stringValue();
									listSubs.add(new Subtitles(surl, lang));
								}
							}
						}
					}
					
					if(arr.hasCollection("sources")) {
						Subtitles[] subs = listSubs.isEmpty() ? null :
							listSubs.toArray(new Subtitles[listSubs.size()]);
						for(SSDNode node : arr.getCollection("sources")) {
							if(node.isCollection()) {
								SSDCollection coll = (SSDCollection) node;
								String furl    = coll.getObject("file").stringValue();
								String quality = null;
								if(coll.hasObject("label"))
									quality = coll.getObject("label").stringValue();
								long fileSize  = Utils.getFileSize_Type(furl);
								VideoSource vs = new VideoSource(
									this, new URL(furl), VideoFormat.get(furl),
									null, fileSize, null, VideoQuality.get(quality),
									subs);
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