package sune.etc.faso.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.util.JavaScript;
import sune.etc.faso.util.UserAgent;
import sune.etc.faso.util.Utils;
import sune.etc.faso.util.Utils.RequestResult;
import sune.etc.faso.util.Utils.Selector;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;
import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;

public class ServerHqqTV implements Server {
	
	public static class HqqTVUtils {
		
		// From uppod-0.5.9.js
		public static final String un(String t) {
			if(t.indexOf('.') == -1) {
				t = t.substring(1); // Remove # character
				StringBuilder sb = new StringBuilder();
				for(int i = 0, l = t.length(); i < l; i+=3) {
					sb.append("%u0")
					  .append(t.substring(i, i + 3));
				}
				return (String) JavaScript.execute(
					"unescape('" + sb.toString() + "')");
			}
			return t;
		}
	}
	
	private static final String URL_EMBED_PLAYER;
	private static final String URL_IP_ADDRESS;
	private static final String URL_GET_MD5_DATA;
	
	private static final String REGEX_IFRAME_URL;
	private static final String REGEX_FORM_URL;
	private static final String REGEX_SCRIPT_HASH;
	
	static {
		URL_EMBED_PLAYER  = "http://hqq.tv/player/embed_player.php";
		URL_IP_ADDRESS 	  = "http://hqq.tv/player/ip.php";
		URL_GET_MD5_DATA  = "http://hqq.tv/player/get_md5.php";
		REGEX_IFRAME_URL  = "^https?://(?:www\\.)?hqq\\.tv/player/embed_player\\.php\\?vid=(.*?)(&(?:.*?))?(?:#.*?)?$";
		REGEX_FORM_URL 	  = "^https?://(?:www\\.)?hqq\\.tv/player/embed_player\\.php(?:\\?.*?)?(?:#.*?)?$";
		REGEX_SCRIPT_HASH = "^https?://(?:www\\.)?hqq\\.tv/player/hash\\.php\\?hash=(.*?)(?:#.*?)?$";
	}
	
	ServerHqqTV() {
	}
	
	@Override
	public final VideoSource[] getVideoSources(Document document) {
		List<VideoSource> sources = new ArrayList<>();
		List<String> 	  list 	  = Utils.search(document,
			((element, matcher, value) -> {
				String name = element.tagName().toLowerCase();
				if(name.equals("iframe") || name.equals("a"))
					return matcher.group(1);
				if(name.equals("form"))
					return element.select("input[name=vid]").first().val();
				return null;
			}),
			new Selector("iframe[src]",  "src",    Pattern.compile(REGEX_IFRAME_URL)),
			new Selector("a[href]",		 "href",   Pattern.compile(REGEX_IFRAME_URL)),
			new Selector("form[action]", "action", Pattern.compile(REGEX_FORM_URL)));
		// Test scripts with decoded (base64) content
		String parsed;
		if((parsed = Utils.deobfuscateJS(Utils.base64Data(document, "script", "src"))) != null) {
			Matcher matcher = Pattern.compile("value\\s*=\\s*\"(.*?)\"").matcher(parsed);
			if(matcher.find()) list.add(matcher.group(1));
		} else {
			// If it fails there still can be an url with hash
			Pattern pattern  = Pattern.compile(REGEX_SCRIPT_HASH);
			Elements scripts = document.select("script[src]");
			for(Element script : scripts) {
				String src = script.attr("src");
				if(pattern.matcher(src).matches()) {
					String con = Utils.quickGETRequest(src);
					con = con.replace("document.write", "");
					String result = (String) JavaScript.execute(con);
					// Get value of variable "vid" from the JS code
					Pattern var = Pattern.compile("var vid\\s*=\\s*['|\"](.*?)['|\"];");
					Matcher mat = var.matcher(result);
					if(mat.find()) list.add(mat.group(1));
				}
			}
		}
		if(!list.isEmpty()) {
			for(String videoID : list) {
				try {
					// The parsed text is made up from form with the post method
					// and some data, so send the post request with the data.
					RequestResult result = Utils.requestPOST(
						URL_EMBED_PLAYER, UserAgent.MOZILLA,
						new String[] { "vid" }, new String[] { videoID }, null);
					String req 		= result.result;
					String jsParsed = Utils.deobfuscateJS(
						Utils.base64Data(Jsoup.parse(req), "script", "src"), true);
					
					// Get IP address of the device
					String ip = "";
					try {
						ip = SSDF.readJSON(
								Utils.requestGET(
									URL_IP_ADDRESS,
									UserAgent.MOZILLA,
									new String[] { "type" },
									new String[] { "json" },
									result.cookies
								).result
							).getString("ip");
					} catch(Exception ex) {
						// Probably a parsing error
					}
					
					// Get the at id value or whatever it is
					String atID    = null;
					Matcher m_atID = Pattern.compile(
						"name\\s*=\\s*\"at\"(?:.*?)value\\s*=\\s*\"(.*?)\"").matcher(jsParsed);
					if(m_atID.find()) atID = m_atID.group(1);
					
					if(atID != null) {
						RequestResult r0 = Utils.requestGET(
							URL_EMBED_PLAYER, UserAgent.MOZILLA,
							new String[] { "iss", "vid", "at", "autoplayed",
								"referer", "http_referer", "pass", "embed_form" },
							new String[] { ip, videoID, atID, "yes",
								"on", "", "", "" }, result.cookies);
						Pattern p_vidlink   = Pattern.compile("vid_link\\s*=\\s*\"(.*?)\"");
						Pattern p_vidserver = Pattern.compile("vid_server\\s*=\\s*\"(.*?)\"");
						String vidlink 	 	= null;
						String vidserver 	= null;
						
						Document d = Jsoup.parse(r0.result);
						for(Element script : d.select("script")) {
							String content = script.html();
							// Important data are only in the script tags with
							// the unescape function call
							int index = content.indexOf("unescape");
							if(index > -1) {
								int bracket 		= content.indexOf(')', index);
								String unescape 	= content.substring(index, bracket+1);
								String executed 	= (String) JavaScript.execute(unescape);
								Matcher m_vidlink 	= p_vidlink.matcher(executed);
								Matcher m_vidserver = p_vidserver.matcher(executed);
								if(m_vidlink.find())   vidlink 	 = m_vidlink.group(1);
								if(m_vidserver.find()) vidserver = m_vidserver.group(1);
								if(vidlink != null && vidserver != null) break;
								// If this finding-by-pattern method fails because of, for example,
								// a variable naming, use another method.
								// This method finds a script tag that contains document.domain info,
								// particulary document.domain="hqq.tv";
								// In this script there are contained three variables and they go
								// in this order: hash, video_link, video_server. Just extract these
								// variables' values and save video_link and video_server data.
								if(executed.toLowerCase().contains("document.domain")) {
									// Split the statements
									String[] stats = executed.split("\\;");
									// Check if the number of statements is correct.
									// The correct number is five, there should be these statements:
									// setting document.domain, three variables, and one additional
									// value for closing the script tag.
									if(stats.length == 5) {
										// Get the video_link and video_server variables
										String var_vidlink   = stats[2];
										String var_vidserver = stats[3];
										int var_index 		 = -1;
										// Get the variables' values and save them
										var_index = var_vidlink.indexOf('"');
										vidlink   = var_vidlink.substring(var_index+1,
														var_vidlink.indexOf('"', var_index+1));
										var_index = var_vidserver.indexOf('"');
										vidserver = var_vidserver.substring(var_index+1,
														var_vidserver.indexOf('"', var_index+1));
										// Check if the variables are not null
										if(vidlink != null && vidserver != null) break;
									}
								}
							}
						}
						
						if(vidlink != null && vidserver != null) {
							SSDCollection data = SSDF.readJSON(
								Utils.requestGET(URL_GET_MD5_DATA, UserAgent.MOZILLA,
									new String[] { "server_1", "link_1", "at", "adb", "b", "vid" },
									new String[] { vidserver, vidlink, atID, "false", "1", videoID },
								result.cookies).result);
							String html5_file = data.getString("html5_file");
							String _file 	  = HqqTVUtils.un(html5_file);
							StringBuilder sb  = new StringBuilder();
							String[] addr 	  = _file.split("/");
							// Format the gotten URL of the video file
							for(int i = 0, l = addr.length; i < l; ++i) {
								String part = addr[i];
								if(i != 0) sb.append('/');
								if(i == 0 || i == l-1) {
									sb.append(part);
								} else if(part.startsWith("false")) {
									sb.append("0/")
									  .append(part.substring(5));
								} else {
									sb.append(part);
								}
							}
							String videoURL = sb.toString();
							long fileSize 	= Utils.getFileSize_Type(videoURL, UserAgent.IPHONE, (String) null);
							VideoSource vs  = new VideoSource(
								this, new URL(videoURL), VideoFormat.get(videoURL),
								null, fileSize, UserAgent.IPHONE, VideoQuality.QUALITY_UNKNOWN,
								null);
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
		return "http://hqq.tv/";
	}
	
	@Override
	public String getName() {
		return "HqqTV";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}