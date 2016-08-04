package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import sune.etc.faso.util.UserAgent;
import sune.etc.faso.util.Utils;
import sune.etc.faso.util.Utils.Selector;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;
import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;

public class ServerVShare implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JWPLAYER_SETUP_TEXT;
	
	static {
		REGEX_IFRAME_URL 	= "^https?://(?:www\\.)?vshare\\.eu/(.*?)\\.html?(?:\\?.*?)?(?:#.*?)?$";
		JWPLAYER_SETUP_TEXT = "jwplayer(\"flvplayer\").setup(";
	}
	
	static String videoURL(String videoID) {
		return "http://vshare.eu/" + videoID + ".htm";
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
	
	ServerVShare() {
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
					String	 fname = doc0.select("input[name='fname']").first().val();
					// Sent POST data to unlock access to the video page
					Document doc = Utils.createDocument(
					    	Utils.request("POST", url, UserAgent.MOZILLA2,
								new String[] { "op", "usr_login", "id", "fname", "referer", "method_free" },
								new String[] { "download1", "", videoID, fname, "", "Proceed+to+video" },
								// Referer is important
								null, Utils.headers("Referer=\"" + url + "\""))
					    		 .result);
					String jsVideo 	  = videoJS(doc);
					String playerData = playerSetupData(jsVideo);
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
		return "http://vshare.eu/";
	}
	
	@Override
	public String getName() {
		return "VShare";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}