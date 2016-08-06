package sune.etc.faso.server;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.subtitles.Subtitles;
import sune.etc.faso.util.JavaScript;
import sune.etc.faso.util.UserAgent;
import sune.etc.faso.util.Utils;
import sune.etc.faso.util.Utils.Selector;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;

public class ServerOpenLoad implements Server {
	
	private static final String REGEX_EMBED_URL;
	private static final String AAENCODE_PREFIX;
	private static final String URL_SIGNATURE_NUMS;
	
	static {
		REGEX_EMBED_URL    = "^https?://(?:www\\.)?openload\\.(?:co|io)/embed/(.*?)/(?:\\?.*?)?(?:#.*?)?$";
		AAENCODE_PREFIX	   = "\uff9f\u03c9\uff9f\uff89";
		URL_SIGNATURE_NUMS = "https://openload.co/assets/js/obfuscator/n.js";
	}
	
	ServerOpenLoad() {
	}
	
	@Override
	public VideoSource[] getVideoSources(Document document) {
		List<VideoSource> sources = new ArrayList<>();
		List<String> 	  list 	  = Utils.search(document,
			((element, matcher, value) -> value),
			new Selector("iframe[src]", "src",  Pattern.compile(REGEX_EMBED_URL)),
			new Selector("a[href]",		"href", Pattern.compile(REGEX_EMBED_URL)));
		if(!list.isEmpty()) {
			for(String url : list) {
				try {
					Document diframe = Utils.getDocument(url);
					Elements scripts = diframe.select("script[type]");
					if(scripts.size() > 0) {
						for(Element script : scripts) {
							String con = script.html();
							if(con.startsWith(AAENCODE_PREFIX)) {
								String value 	= Utils.aadecode(con).replace("window.", "var ");
								String videoURL = (String) JavaScript.execute(value + "vs;");
								// Try different technique to extract the URL
								if(videoURL == null) {
									// The vs variable does not have to be visible for the first time,
									// it can be wrapped in another variable or it can be a variable
									// with completely different name.
									if(value.startsWith("var ")) {
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
											}
										}
									}
									if(videoURL == null) {
										// There could also be an image obfuscated link. It is a link
										// that is encoded into an image which is put into an img tag
										// with src attribute set to some base64 encoded image data.
										// These data contains some strings that are used with some other
										// strings, called signature numbers, to 'decrypt' the content
										// and serve the correct stream URL link.
										try {
											Elements imgs = diframe.select("img#linkimg");
											Element  img;
											if(!imgs.isEmpty() && (img = imgs.first()) != null) {
												// Get image's base64 encoded image data
												String data = img.attr("src");
												data = data.substring(data.indexOf(',')+1);
												// Decode image src attribute and get image bytes
												byte[] bytes = Utils.rawBase64Decode(data);
												BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(bytes));
												bytes = ((DataBufferByte) bimg.getRaster().getDataBuffer()).getData();
															
												// 1. Convert all the image bytes to characters
												StringBuilder sb = new StringBuilder();
												StringBuilder tb = new StringBuilder();
												for(int i = 0, k = 0, l = bytes.length, c; i < l; ++i) {
													// Bytes are in BGR format, needs to be in RGB format!
													if((c = bytes[i] & 0xff) != '\0')
														tb.append((char) c);
													// Convert from BGR format to RGB format
													if(++k == 3) {
														sb.append(tb.reverse().toString());
														tb.setLength(0);
														k = 0;
													}
												}
												
												// 2. Filter characters with RegExp
												String  rs = sb.toString();
												Pattern p0 = Pattern.compile(".{1,"+(rs.length()/10)+"}");
												Pattern p1 = Pattern.compile(".{1,20}");
												Matcher m0 = p0.matcher(rs);
												List<String[]> str = new ArrayList<>();
												List<String>   tsl = new ArrayList<>();
												while(m0.find()) {
													String  g0 = m0.group();
													Matcher m1 = p1.matcher(g0);
													while(m1.find()) tsl.add(m1.group());
													str.add(tsl.toArray(new String[tsl.size()]));
													tsl.clear();
												}
												
												// 3. Get all the signature numbers
												String nums = Utils.quickGETRequest(URL_SIGNATURE_NUMS);
												nums = nums.substring(nums.indexOf('\'')+1, nums.lastIndexOf('\''));
												
												// 4. Filter signature numbers
												Pattern np0 = Pattern.compile(".{1,"+(nums.length()/10)+"}");
												Pattern np1 = Pattern.compile(".{1,26}");
												Matcher nm0 = np0.matcher(nums);
												List<String[]> nstr = new ArrayList<>();
												List<String>   ntsl = new ArrayList<>();
												while(nm0.find()) {
													String  ng0 = nm0.group();
													Matcher nm1 = np1.matcher(ng0);
													while(nm1.find()) ntsl.add(nm1.group());
													nstr.add(ntsl.toArray(new String[ntsl.size()]));
													ntsl.clear();
												}
												
												String[][] arrStr  = Utils.toArray(str);
												String[][] arrNStr = Utils.toArray(nstr);
												
												@SuppressWarnings("unchecked")
												ArrayList<Character>[] listArr
													= (ArrayList<Character>[]) new ArrayList[arrStr.length];
												
												// 5. Run 'decrypting' loop no. 1
												double charC = 0.0;
												for(int i = 0, l0 = arrStr.length; i < l0; ++i) {
													if((i < 2 || ((i & 1) == 0 && i != 2)) || i > 8)
														continue;
													charC = 'c';
													listArr[i] = new ArrayList<>();
													for(int k = 0, l1 = arrNStr[i].length; k < l1; ++k) {
														for(int j = 0, l2 = arrNStr[i][k].length(); j < l2; ++j) {
															if(charC > 122) charC = 'b';
															if(arrNStr[i][k].charAt(j) == (char) Math.floor(charC)) {
																if(listArr[i].size() >  k &&
																   listArr[i].get(k) != null)
																	continue;
																charC += 2.5;
																if(j < arrStr[i][k].length())
																	 listArr[i].add(arrStr[i][k].charAt(j));
																else listArr[i].add(null);
															}
														}
													}
												}
												
												// 6. Run 'decrypting' loop no. 2
												List<String>  listFinal = new ArrayList<>();
												StringBuilder sb0 		= new StringBuilder();
												for(int i = 0; i < 10; ++i) {
													if(!((i < 2 || ((i & 1) == 0 && i != 2)) || i > 8)
															&& listArr[i] != null) {
														for(Character c : listArr[i])
															if((c != null && c != ','))
																sb0.append(c);
														listFinal.add(sb0.toString());
														sb0.setLength(0);
													}
												}
												
												// 7. Build the final URL
												String urlFinal = listFinal.get(3) + '~' +
																  listFinal.get(1) + '~' +
																  listFinal.get(2) + '~' +
																  listFinal.get(0);
												videoURL  = "https://openload.co/";
												videoURL += "stream/" + urlFinal + "?mime=true";
											}
										} catch(Exception ex) {
										}
									}
								}
								// Check the video URL
								if((videoURL == null)) continue;
								if(!videoURL.startsWith("http") ||
								   !videoURL.startsWith("https")) {
									// Fix the URL if needed
									if((videoURL.startsWith("//")))
										videoURL = "https:" + videoURL;
								}
								// Get the HTTP Connection
								HttpURLConnection hcon
									= (HttpURLConnection) new URL(videoURL).openConnection();
								// Cast the object to the right type if HTTPS is used
								if(videoURL.startsWith("https"))
									hcon = (HttpsURLConnection) hcon;
								// HTTP Connection configuration
								hcon.setInstanceFollowRedirects(false);
								// Also set the correct User-Agent or HTTP 403 is returned
								hcon.addRequestProperty("User-Agent", UserAgent.MOZILLA2);
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
								
								Subtitles[] subs = listSubs.isEmpty() ? null :
									listSubs.toArray(new Subtitles[listSubs.size()]);
								long fileSize  	 = Utils.getFileSize_Type(videoURL);
								VideoSource vs 	 = new VideoSource(
									this, new URL(videoURL), VideoFormat.get(videoURL),
									null, fileSize, null, VideoQuality.QUALITY_UNKNOWN,
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