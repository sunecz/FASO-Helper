package sune.etc.faso.server;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoQuality;
import sune.etc.faso.video.VideoSource;

public class ServerVeoh implements Server {
	
	private static final String REGEX_IFRAME_URL;
	private static final String JS_VARIABLE_DATA;
	
	static {
		REGEX_IFRAME_URL = "^https?://(?:www\\.)?veoh\\.com/watch/(.*?)$";
		JS_VARIABLE_DATA = "__watch.videoDetailsJSON";
	}
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	// From VeohMediaPlayer.swf (com.hurlant.util.Hex)
	static final class Hex {
		
		public static String fromString(String str, boolean colons) {
			return fromArray(str.getBytes(CHARSET), colons);
		}
		
		public static String toString(String hex) {
			return new String(toArray(hex), CHARSET);
		}
		
		public static byte[] toArray(String hex) {
			hex = Pattern.compile("\\s|:", Pattern.MULTILINE)
						 .matcher(hex)
						 .replaceAll("");
			int length = hex.length();
			if((length & 1) == 1)
				hex = "0" + hex;
			byte[] array = new byte[length/2];
			for(int i = 0; i < length; i+=2)
				array[i / 2] = (byte) Integer.parseInt(
					hex.substring(i, i+2), 16);
			return array;
		}
		
		public static String fromArray(byte[] array, boolean colons) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0, l = array.length; i < l; ++i) {
				String s = Integer.toString(array[i] & 0xff, 16);
				if(s.length() == 1) s = "0" + s;
				sb.append(s);
				if(colons && i < l-1)
					sb.append(':');
			}
			return sb.toString();
		}
	}
	
	// From VeohMediaPlayer.swf (com.hurlant.util.Base64)
	static final class Base64 {
		static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
		
		public static String encode(String data) {
			return encodeByteArray(data.getBytes(CHARSET));
		}
		
		public static String encodeByteArray(byte[] data) {
			int i = 0;
			int j = 0;
			int k = 0;
			int[] outputBuffer = new int[4];
			int[] dataBuffer   = new int[3];
			StringBuilder output = new StringBuilder();
			for(int f = 0, l = data.length; f < l;) {
				Arrays.fill(dataBuffer, 0);
				i = 0;
				while(i < 3 && f < l)
					dataBuffer[i++] = data[f++];
				outputBuffer[0] = (dataBuffer[0] & 252) >> 2;
				outputBuffer[1] = (dataBuffer[0] & 3)  << 4 | dataBuffer[1] >> 4;
				outputBuffer[2] = (dataBuffer[1] & 15) << 2 | dataBuffer[2] >> 6;
				outputBuffer[3] = (dataBuffer[2] & 63);
				for(j = i; j < 4; ++j)
					outputBuffer[j] = 64;
				for(k = 0; k < 4; ++k)
					output.append(BASE64_CHARS.charAt(outputBuffer[k]));
			}
			return output.toString();
		}
		
		public static String decode(String data) {
			return new String(decodeToByteArray(data), CHARSET);
		}
		
		public static byte[] decodeToByteArray(String data) {
			int j = 0;
			int k = 0;
			int[] dataBuffer   = new int[4];
			int[] outputBuffer = new int[3];
			try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				for(int i = 0, l = data.length(); i < l; i+=4) {
					j = 0;
					while(j < 4 && i + j < l) {
						dataBuffer[j] = BASE64_CHARS.indexOf(data.charAt(i + j));
						++j;
					}
					outputBuffer[0] = ((dataBuffer[0]) 		<< 2) + ((dataBuffer[1] & 48) >> 4);
					outputBuffer[1] = ((dataBuffer[1] & 15) << 4) + ((dataBuffer[2] & 60) >> 2);
					outputBuffer[2] = ((dataBuffer[2] & 3)  << 6) + ((dataBuffer[3]));
					for(k = 0; k < 3; ++k) {
						if(dataBuffer[k+1] == 64)
							break;
						baos.write((byte) outputBuffer[k]);
					}
				}
				return baos.toByteArray();
			} catch(Exception ex) {
			}
			return null;
		}
	}
	
	// From VeohMediaPlayer.swf (com.qlipso.core.utils.encryption.EncryptionUtils)
	static final String secretKeyDecrypt(String value, String valueTextFormat, String returnValueTextFormat,
			String encryptionType, String encryptionMode, String encryptionPadding, String key,
			String keyTextFormat, String initialVector) {
		// Format key
		byte[] kdata;
		switch(keyTextFormat) {
			case EncryptionTextFormat.HEX:
				kdata = Hex.toArray(key);
				break;
			case EncryptionTextFormat.BASE_64:
				kdata = Base64.decodeToByteArray(key);
				break;
			default:
				kdata = Hex.toArray(Hex.fromString(key, false));
				break;
		}
		// Format value
		byte[] data;
		switch(valueTextFormat) {
			case EncryptionTextFormat.HEX:
				data = Hex.toArray(value);
				break;
			case EncryptionTextFormat.BASE_64:
				data = Base64.decodeToByteArray(value);
				break;
			default:
				data = Hex.toArray(Hex.fromString(value, false));
				break;
		}
		// Check if data are valid
		int length = data.length;
		if((length % 16) != 0) {
			int nlength  = (length / 16) + 1;
			byte[] ndata = new byte[nlength];
			System.arraycopy(data, 0, ndata, 0, length);
			data = ndata;
		}
		// Decrypt the data
		try {
			String algName = encryptionType + '/' +
							 encryptionMode + '/' +
							 encryptionPadding;
			Cipher cipher = Cipher.getInstance(algName);
			SecretKeySpec 	specKey = new SecretKeySpec(kdata, encryptionType);
			IvParameterSpec specIV  = new IvParameterSpec(Hex.toArray(initialVector));
			cipher.init(Cipher.DECRYPT_MODE, specKey, specIV);
			byte[] result = cipher.doFinal(data);
			// Format result
			switch(returnValueTextFormat) {
				case EncryptionTextFormat.HEX:
					return Hex.fromArray(result, false);
				case EncryptionTextFormat.BASE_64:
					return Base64.encodeByteArray(result);
				default:
					return Hex.toString(Hex.fromArray(result, false));
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	// From VeohMediaPlayer.swf (com.veoh.model.VideoProxy)
	static final String SECRET_KEY 	   = "8694dfcdd864caaac8902d7ebd04edae";
	// From VeohMediaPlayer.swf (com.veoh.model.VideoProxy)
	static final String INITIAL_VECTOR = "ff57cec30aeea89a0f50db4164a1da72";
	
	// From VeohMediaPlayer.swf (com.qlipso.core.utils.encryption.EncryptionTextFormat)
	static final class EncryptionTextFormat {
		public static final String HEX 	   = "hex";
		public static final String BASE_64 = "b64";
		public static final String TEXT    = "text";
	}
	
	// From VeohMediaPlayer.swf (com.qlipso.core.utils.encryption.SecretKeyEncryptionType)
	static final class SecretKeyEncryptionType {
		public static final String BLOW_FISH = "blowfish";
		public static final String DES_3 	 = "des3";
		public static final String RC_4  	 = "rc4";
		public static final String AES  	 = "aes";
		public static final String DES  	 = "des";
		public static final String XTEA  	 = "xtea";
	}
	
	// From VeohMediaPlayer.swf (com.qlipso.core.utils.encryption.SecretKeyEncryptionMode)
	static final class SecretKeyEncryptionMode {
		public static final String CTR 	 = "ctr";
		public static final String ECB 	 = "ecb";
		public static final String CBC 	 = "cbc";
		public static final String OFB 	 = "ofb";
		public static final String CFB_8 = "cfb8";
		public static final String CFB 	 = "cfb";
	}
	
	// From VeohMediaPlayer.swf (com.qlipso.core.utils.encryption.SecretKeyEncryptionPadding)
	static final class SecretKeyEncryptionPadding {
		public static final String NONE   = "NoPadding";
		public static final String PKCS_5 = "PKCS5Padding";
	}
	
	// From VeohMediaPlayer.swf (com.veoh.model.VideoProxy)
	static final String decryptURL(String hashPath, String hashPathToken) {
		if(hashPath 	 == null || hashPath	 .isEmpty() ||
		   hashPathToken == null || hashPathToken.isEmpty())
			return null;
		return hashPath + secretKeyDecrypt(hashPathToken,
										   EncryptionTextFormat.BASE_64,
										   EncryptionTextFormat.TEXT,
										   SecretKeyEncryptionType.AES,
										   SecretKeyEncryptionMode.CBC,
										   SecretKeyEncryptionPadding.PKCS_5,
										   SECRET_KEY,
										   EncryptionTextFormat.HEX,
										   INITIAL_VECTOR);
	}
	
	ServerVeoh() {
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
					Document doc = Utils.getDocument(url);
					for(Element js : doc.select("script[type='text/javascript']")) {
						String content = js.html();
						int	   index   = content.indexOf(JS_VARIABLE_DATA);
						if((index > -1)) {
							// Update the content
							index  += JS_VARIABLE_DATA.length();
							content = content.substring(index);
							// Quotes
							boolean bq = false;
							int 	cq = 0;
							// Escaping
							boolean esc = false;
							// String buffer
							StringBuilder sb = new StringBuilder();
							char[] chars 	 = content.toCharArray();
							for(int i = 0, l = chars.length, c; i < l; ++i) {
								c = chars[i];
								// We are in the variable value string quotes
								if(bq) {
									// Escaping
									if(c == '\\' && !esc) { esc = true;  continue; } else
									if(esc) 			  { esc = false; }
									if(c == cq) {
										// We are in the end of the variable
										// value string quotes
										bq = false;
										// Exit the loop
										break;
									}
									// Add any character we encouter
									else sb.append((char) c);
								} else {
									// We are not in the variable value string quotes
									if(c == '\'' || c == '"') {
										cq = c;
										bq = true;
									}
								}	
							}
							String jsonData = sb.toString();
							JSONObject jobj = new JSONObject(jsonData);
							// Get download URL information
							String hashPath 	 = jobj.getString("downloadUrl");
							String hashPathToken = jobj.getString("downloadUrlToken");
							// Decrypt the URL
							String decrypted = decryptURL(hashPath, hashPathToken);
							// Add the Video Source
							long fileSize  = Utils.getFileSize_Type(decrypted);
							VideoSource vs = new VideoSource(
								this, new URL(decrypted), VideoFormat.get(decrypted),
								null, fileSize, null, VideoQuality.QUALITY_UNKNOWN,
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
		return "http://veoh.com/";
	}
	
	@Override
	public String getName() {
		return "Veoh";
	}
	
	@Override
	public String toString() {
		return getName();
	}
}