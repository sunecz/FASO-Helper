package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import sune.etc.faso.subtitles.Subtitles;
import sune.etc.faso.util.UserAgent;
import sune.etc.faso.util.Utils;
import sune.etc.faso.util.Utils.Selector;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;
import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;
import sune.util.ssdf2.SSDFilter;
import sune.util.ssdf2.SSDNode;
import sune.util.ssdf2.SSDObject;

public class ServerTheVideo implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	
	static {
		REGEX_IFRAME_URL 	= "^https?://(?:www\\.)?thevideo\\.me/(.*?)(?:\\?.*?)?(?:#.*?)?$";
		JWPLAYER_SETUP_TEXT = "var jwConfig_vars =";
	}
	
	static String videoURL(String videoID) {
		return "http://thevideo.me/" + videoID;
	}
	
	static String videoJS(Document doc) {
		for(Element el : doc.select("script")) {
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
	
	ServerTheVideo() {
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
					String   url   = videoURL(videoID);
					// Get information before accessing the page
					String 	 pcon  = Utils.quickGETRequest(url);
					Document doc0  = Utils.createDocument(pcon);
					String   hash  = doc0.select("input[name='hash']").first().val();
					String	 fname = doc0.select("input[name='fname']").first().val();
					// _vhash and gfk are loaded by JavaScript
					String _vhash = "";
					String gfk    = "";
					String find   = "$('<input/>').attr(";
					for(int i = 0, k = 0; i < 2; ++i) {
						int index = pcon.indexOf(find, k);
						if((index > -1)) {
							index  += find.length();
							int end = pcon.indexOf(')', index);
							if((end > -1)) {
								String 		  data = pcon.substring(index, end);
								SSDCollection coll = SSDF.read(data);
								String 		  sval = coll.getString("value");
								if(i == 0) gfk 	  = sval;
								else	   _vhash = sval;
								k = end;
							}
						}
					}
					// Sent POST data to unlock access to the video page
					Document doc = Utils.createDocument(
					    	Utils.request("POST", url, UserAgent.MOZILLA2,
								new String[] {
									"_vhash", "gfk", "op", "usr_login", "id", "fname",
									"referer", "hash", "inhu", "imhuman"
								},
								new String[] {
									_vhash, gfk, "download1", "", videoID, fname,
									"", hash, "foff", ""
								},
								// Referer is important
								null, Utils.headers("Referer=\"" + url + "\""))
					    		 .result);
					String jsVideo 	  = videoJS(doc);
					String playerData = playerSetupData(jsVideo);
					SSDCollection arr = SSDF.read(playerData);
					// Get the playlist array and loop through each item
					arr = arr.getCollection("playlist");
					for(SSDNode item : arr.filter(SSDFilter.ONLY_COLLECTIONS)) {
						// Cast the item to a collection
						SSDCollection data = (SSDCollection) item;
						
						List<Subtitles> listSubs = new ArrayList<>();
						if(data.hasCollection("tracks")) {
							for(SSDNode node : data.getCollection("tracks")) {
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
						
						if(data.hasCollection("sources")) {
							Subtitles[] subs = listSubs.isEmpty() ? null :
								listSubs.toArray(new Subtitles[listSubs.size()]);
							for(SSDNode node : data.getCollection("sources")) {
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
					}
				} catch(Exception ex) {
				}
			}
		}
		return sources.toArray(new VideoSource[sources.size()]);
	}
	
	@Override
	public String getAddress() {
		return "http://thevideo.me/";
	}
	
	@Override
	public String getName() {
		return "TheVideo";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}