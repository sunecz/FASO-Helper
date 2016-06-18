package sune.etc.faso.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import sune.ssdf.SSDArray;
import sune.ssdf.SSDObject;

public class Utils {
	
	public static final Charset CHARSET;
	static {
		CHARSET = Charset.forName("UTF-8");
	}
	
	public static final Document getDocument(String url) {
		return getDocument(url, UserAgent.MOZILLA);
	}
	
	public static final Document getDocument(String url, String userAgent) {
		return createDocument(quickGETRequest(url, userAgent));
	}
	
	public static final Document createDocument(String content) {
		return content == null ? null : Jsoup.parse(content);
	}
	
	public static final String getFileContent(String url) {
		try(InputStream input = new BufferedInputStream(
				new URL(url).openStream())) {
			StringBuilder sb = new StringBuilder();
			int read 		 = 0;
			byte[] buffer 	 = new byte[8192];
			while((read = input.read(buffer)) != -1) {
				sb.append(
					new String(buffer, 0, read, CHARSET));
			}
			return sb.toString();
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String deobfuscateJS(String js) {
		return deobfuscateJS(js, false);
	}
	
	public static final String deobfuscateJS(String js, boolean varcontent) {
		try {
			String deobfuscated = (String) JavaScript.execute(
				"deobfuscate.js",
				"var c='" + js.replace('\'', '"') + "';c=deobfuscate(c);");
			String unescaped = (String) JavaScript.execute(
				"function f(s){" + deobfuscated.replace("eval", "return ") + "}" +
				"unescape(f(c));");
			String value = unescaped;
			// Get content of first variable in
			// the unescaped and deobfuscated content
			if(varcontent) {
				StringBuilder sb = new StringBuilder();
				char[] chars 	 = value.toCharArray();
				boolean quotes   = false;
				for(int i = 0, l = chars.length; i < l; ++i) {
					char c = chars[i];
					if(c == '\'') {
						quotes = !quotes;
						if(!quotes && i+1 < l && chars[i+1] == ';')
							break;
					} else if(quotes) sb.append(c);
				}
				value = sb.toString();
			}
			return value;
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String jjdecode(String string) {
		return (String) JavaScript.execute(
			"jjdecode.js",
			"jjdecode('" + escapeJS(string) + "');");
	}
	
	public static final String aadecode(String string) {
		return AADecoder.decode(string);
	}
	
	// Same as StringEscapeUtils for ECMAScript
	public static final String escapeJS(String string) {
		StringBuilder sb = new StringBuilder();
		char[] chars 	 = string.toCharArray();
		for(int i = 0, l = chars.length; i < l; ++i) {
			char c = chars[i];
			if(c == '\'' || c == '"'  || c == '\\' ||
			   c == '/'  || c == '\b' || c == '\n' ||
			   c == '\t' || c == '\f' || c == '\r') {
				sb.append('\\');
			} else if(c < 32 || c > 127) {
				sb.append("\\u");
				sb.append(intToHex(c));
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static final String intToHex(int v) {
		String s = Integer.toHexString(v);
		return ("0000".substring(0, 4-s.length()) + s).toUpperCase();
	}
	
	public static final String encodeX(String string) {
		String[] array = string.replace("\\", "")
							   .split("x");
		StringBuilder sb = new StringBuilder();
		for(int i = 1, l = array.length; i < l; ++i) {
		    int hex = Integer.parseInt(array[i], 16);
		    sb.append((char) hex);
		}
		return sb.toString();
	}
	
	public static final String base64Decode(String string) {
		return new String(
			Base64.getDecoder()
				  .decode(string.getBytes(CHARSET)),
			CHARSET);
	}
	
	public static final String base64URLDecode(String string) {
		return new String(
			Base64.getUrlDecoder()
				  .decode(string.getBytes(CHARSET)),
			CHARSET);
	}
	
	public static final byte[] rawBase64Decode(String string) {
		return Base64.getDecoder()
					 .decode(string.getBytes());
	}
	
	public static final byte[] rawBase64URLDecode(String string) {
		return Base64.getUrlDecoder()
					 .decode(string.getBytes());
	}
	
	public static final String base64URLEncode(String string) {
		return new String(
			Base64.getUrlEncoder()
				  .encode(string.getBytes(CHARSET)),
			CHARSET);
	}
	
	public static final String base64Data(Document doc, String tag, String attr) {
		String dataJS = null;
		for(Element script : doc.select(tag+"["+attr+"]")) {
			String src = script.attr(attr);
			if(src.startsWith("data:")) {
				dataJS = src;
				break;
			}
		}
		if(dataJS != null) {
			String dataB64 = null;
			for(String s : dataJS.split(";")) {
				if(s.startsWith("base64")) {
					dataB64 = s.substring(7);
					break;
				}
			}
			if(dataB64 != null) {
				return base64Decode(dataB64);
			}
		}
		return null;
	}
	
	
	public static final CookieManager COOKIE_MANAGER;
	static {
		COOKIE_MANAGER = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
	}
	public static final void initCookieManager() {
		if(CookieManager.getDefault() == null) {
			CookieManager.setDefault(COOKIE_MANAGER);
		}
	}
	
	public static final class RequestResult {
		
		public final String result;
		public final String cookies;
		
		public RequestResult(String result, String cookies) {
			this.result  = result;
			this.cookies = cookies;
		}
	}
	
	public static final RequestResult request(String method, String url, String userAgent) {
		return request(method, url, userAgent, null, null, null, null);
	}
	
	public static final RequestResult requestGET(String url, String userAgent,
			String[] names, String[] values, String cookie) {
		return request("GET", url, userAgent, names, values, cookie, null);
	}
	
	public static final RequestResult requestPOST(String url, String userAgent,
			String[] names, String[] values, String cookie) {
		return request("POST", url, userAgent, names, values, cookie, null);
	}
	
	public static final RequestResult request(String method, String url, String userAgent,
			String[] names, String[] values, String cookie, Map<String, String> headers) {
		try {
			initCookieManager();
			HttpURLConnection con = (HttpURLConnection)
				new URL(url).openConnection();
			con.setRequestMethod(method);
			if(cookie != null)
			con.setRequestProperty("Cookie", cookie);
			con.setRequestProperty("User-Agent", userAgent);
			if(headers != null && headers.size() > 0) {
				for(Entry<String, String> h : headers.entrySet()) {
					con.setRequestProperty(h.getKey(), h.getValue());
				}
			}
			con.setDoOutput(true);
			con.connect();
			
			StringBuilder sb = new StringBuilder();
			if(names != null && values != null) {
				for(int i = 0, l = names.length; i < l; ++i) {
					if(i != 0) sb.append('&');
					sb.append(names[i]);
					sb.append("=");
					sb.append(values[i]);
				}
				
				try(DataOutputStream wr
						= new DataOutputStream(con.getOutputStream())) {
					wr.writeBytes(sb.toString());
					wr.flush();
				}
			}
			
			InputStream stream;
			if(con.getResponseCode() == 200) stream = con.getInputStream();
			else							 stream = con.getErrorStream();
			
			sb.setLength(0);
			try(InputStream is = new BufferedInputStream(stream)) {
				int read	  = 0;
				byte[] buffer = new byte[8192];
				while((read = is.read(buffer)) != -1) {
					sb.append(new String(buffer, 0, read, CHARSET));
				}
			} catch(IOException ex) {
			}
			StringBuilder cookies = new StringBuilder();
			for(String string : COOKIE_MANAGER.get(new URI(url),
												   new HashMap<>())
											  .get("Cookie")) {
				cookies.append(string);
				cookies.append(';');
			}
			return new RequestResult(sb.toString(), cookies.toString());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static final InputStream requestStream(String method, String url, String userAgent) {
		return requestStream(method, url, userAgent, null, null, null, null);
	}
	
	public static final InputStream requestStream(String method, String url, String userAgent,
			String[] names, String[] values, String cookie, Map<String, String> headers) {
		try {
			initCookieManager();
			HttpURLConnection con = (HttpURLConnection)
				new URL(url).openConnection();
			con.setRequestMethod(method);
			if(cookie != null)
			con.setRequestProperty("Cookie", cookie);
			con.setRequestProperty("User-Agent", userAgent);
			if(headers != null && headers.size() > 0) {
				for(Entry<String, String> h : headers.entrySet()) {
					con.setRequestProperty(h.getKey(), h.getValue());
				}
			}
			con.setDoOutput(true);
			con.connect();
			
			StringBuilder sb = new StringBuilder();
			if(names != null && values != null) {
				for(int i = 0, l = names.length; i < l; ++i) {
					if(i != 0) sb.append('&');
					sb.append(names[i]);
					sb.append("=");
					sb.append(values[i]);
				}
				
				try(DataOutputStream wr
						= new DataOutputStream(con.getOutputStream())) {
					wr.writeBytes(sb.toString());
					wr.flush();
				}
			}
			return new BufferedInputStream(
					con.getResponseCode() == 200 ?
						con.getInputStream() :
						con.getErrorStream());
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String quickGETRequest(String url) {
		return quickGETRequest(url, UserAgent.MOZILLA);
	}
	
	public static final String quickGETRequest(String url, String userAgent) {
		try {
			HttpURLConnection con =
				(HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", userAgent);
			con.setDoOutput(true);
			con.connect();
			
			InputStream stream;
			if(con.getResponseCode() == 200) stream = con.getInputStream();
			else							 stream = con.getErrorStream();
			
			try(ByteArrayOutputStream ba = new ByteArrayOutputStream()) {
				try(InputStream is = new BufferedInputStream(stream)) {
					int read	  = 0;
					byte[] buffer = new byte[8192];
					while((read = is.read(buffer)) != -1)
						ba.write(buffer, 0, read);
				}
				return new String(ba.toByteArray(), CHARSET);
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final long getFileSizeURL(String url) {
		return getFileSizeURL(url, UserAgent.MOZILLA, (Map<String, String>) null);
	}
	
	public static final long getFileSizeURL(String url, String headers) {
		return getFileSizeURL(url, UserAgent.MOZILLA, headers(headers));
	}
	
	public static final long getFileSizeURL(String url, String userAgent,
			String headers) {
		return getFileSizeURL(url, userAgent, headers(headers));
	}
	
	public static final long getFileSizeURL(String url, String userAgent,
			Map<String, String> headers) {
		try {
			URLConnection con = new URL(url).openConnection();
			con.setRequestProperty("User-Agent", userAgent);
			if(headers != null && headers.size() > 0) {
				for(Entry<String, String> h : headers.entrySet()) {
					con.setRequestProperty(h.getKey(), h.getValue());
				}
			}
			String length 	  = con.getHeaderField("Content-Length");
			if(length != null && !length.isEmpty()) {
				return Long.parseLong(length);
			}
		} catch(Exception ex) {
		}
		return -1;
	}
	
	public static final Map<String, String> headers(String data) {
		if(data == null) return null;
		Map<String, String> map = new LinkedHashMap<>();
		boolean quotes 			= false;
		String tempName 		= null;
		StringBuilder sb 		= new StringBuilder();
		char[] chars 			= data.toCharArray();
		for(int i = 0, l = chars.length; i < l; ++i) {
			char c = chars[i];
			if(c == '"') {
				quotes = !quotes;
			} else {
				if(!quotes) {
					if(c == '=') {
						tempName = sb.toString();
						sb.setLength(0);
					} else if(c == ';') {
						if(tempName != null) {
							map.put(tempName, sb.toString());
							sb.setLength(0);
							tempName = null;
						}
					} else {
						sb.append(c);
					}
				} else {
					sb.append(c);
				}
			}
			if(i == l-1 && tempName != null) {
				map.put(tempName, sb.toString());
				sb.setLength(0);
			}
		}
		return map;
	}
	
	public static final byte[] hexStringToByteArray(String string) {
		char[] chars = string.toCharArray();
		int length 	 = chars.length;
		byte[] data  = new byte[length / 2];
	    for(int i = 0, k = 0; i < length; i+=2, ++k) {
	        data[k] = (byte) ((Character.digit(chars[i],   16) << 4)
	        				 + Character.digit(chars[i+1], 16));
	    }
	    return data;
	}
	
	public static final String hexString(String string) {
		char[] chars = string.toCharArray();
		char[] array = new char[chars.length];
	    for(int i = 0, k = 0, l = chars.length; i < l; i+=2, ++k) {
	    	array[k] = (char) (
	    		(Character.digit(chars[i],   16) << 4)
	    	   + Character.digit(chars[i+1], 16) + 2);
	    }
	    return new String(array);
	}
	
	public static final String hexString0(String string) {
		char[] chars = string.toCharArray();
		byte[] array = new byte[chars.length];
	    for(int i = 0, k = 0, l = chars.length; i < l; i+=2, ++k) {
	    	array[k] = (byte) (
	    		(Character.digit(chars[i],   16) << 4)
	    	   + Character.digit(chars[i+1], 16));
	    }
	    return new String(array);
	}
	
	public static final String utf8(String string) {
		try {
			return new String(
				CHARSET.newEncoder()
					   .encode(
						    CharBuffer.wrap(
						    	string.toCharArray()))
					   .array(),
				CHARSET);
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String utf8(byte[] bytes) {
		return new String(bytes, CHARSET);
	}
	
	public static final class NVHolder {
		
		public final String[] names;
		public final String[] values;
		
		public NVHolder(String[] names, String[] values) {
			this.names  = names;
			this.values = values;
		}
	}
	
	public static final Map<String, String> url(String url) {
		Map<String, String> map = new LinkedHashMap<>();
		for(String part : url.split("&")) {
			int index;
			if((index = part.indexOf('=')) > -1) {
				map.put(part.substring(0, index),
						decodeURL(part.substring(index+1)));
			}
		}
		return map;
	}
	
	public static final NVHolder urlNV(String url) {
		Map<String, String> map = url(url);
		return new NVHolder(
			map.keySet().toArray(new String[map.size()]),
			map.values().toArray(new String[map.size()]));
	}
	
	public static final String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, CHARSET.name());
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final String decodeURL(String url) {
		try {
			return URLDecoder.decode(url, CHARSET.name());
		} catch(Exception ex) {
		}
		return null;
	}
	
	public static final long getUnixTime(String zone) {
		return Instant.now(Clock.tickSeconds(ZoneId.of(zone))).getEpochSecond();
	}
	
	public static final Class<?>[] recognizeClasses(Object... arguments) {
		int length 		   = arguments.length;
		Class<?>[] classes = new Class<?>[length];
		for(int i = 0; i < length; ++i) {
			Class<?> clazz = arguments[i].getClass();
			classes[i] 	   = toPrimitive(clazz);
		}
		return classes;
	}
	
	public static final Class<?> toPrimitive(Class<?> clazz) {
		if(clazz == Boolean.class) 	 return boolean.class;
        if(clazz == Byte.class) 	 return byte.class;
        if(clazz == Character.class) return char.class;
        if(clazz == Short.class) 	 return short.class;
        if(clazz == Integer.class) 	 return int.class;
        if(clazz == Long.class) 	 return long.class;
        if(clazz == Float.class) 	 return float.class;
        if(clazz == Double.class) 	 return double.class;
        if(clazz == Void.class) 	 return void.class;
		return clazz;
	}
	
	public static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Exception ex) {
		}
	}
	
	public static final int[] toIntArray(Integer[] a) {
		int   l = a.length;
		int[] b = new int[l];
		for(int i = 0; i < l; ++i)
			b[i] = a[i];
		return b;
	}
	
	// Converts a SSDArray to formatted 2-dimensional map of strings
	public static final Map<String, Map<String, String>> convert(SSDArray array) {
		Map<String, Map<String, String>> map = new LinkedHashMap<>();
		for(Entry<String, SSDObject> entry : array.getAllObjects().entrySet()) {
			String name 	= entry.getKey();
			SSDObject value = entry.getValue();
			int indexDot    = name.indexOf('.');
			String mapName  = name.substring(0, indexDot);
			String valName  = name.substring(indexDot+1);
			// This data conversion is allowed for only 2-dimensional data
			if(valName.indexOf('.') == -1) {
				Map<String, String> map2;
				if((map2 = map.get(mapName)) == null) {
					map2 = new LinkedHashMap<>();
					map.put(mapName, map2);
				}
				map2.put(valName, value.stringValue());
			}
		}
		return map;
	}
	
	// Provides realtively fast method for converting a string into an integer
	// not using any built-in function and ignoring all other characters that
	// are not defined as digits.
	public static final int extractInt(String string) {
		if(string == null || string.isEmpty()) {
			throw new IllegalArgumentException(
				"The given string cannot be null nor empty!");
		}
		boolean has	 = false;
		boolean neg  = false;
		int value 	 = 0;
		int temp  	 = 0;
		char[] chars = string.toCharArray();
		for(int i = 0, l = chars.length, c; i < l; ++i) {
			if((c 	 = chars[i]) == '-') neg = true; else
			if((temp = Character.digit(c, 10)) != -1) {
				int val = (value * 10) - temp;
				if((val > value) || (!neg && val == Integer.MIN_VALUE)) {
					throw new IllegalArgumentException(
						"The given string contains number outside of " +
						"the range of a signed integer!");
				}
				value = val;
				has   = true;
			}
		}
		if(!has) {
			throw new IllegalArgumentException(
				"The given string does not contain any digit!");
		}
		return neg ? value : -value;
	}
}