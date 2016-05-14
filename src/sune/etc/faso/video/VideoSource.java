package sune.etc.faso.video;

import java.net.URL;
import java.util.Map;

import sune.etc.faso.server.Server;
import sune.etc.faso.util.Utils;

public class VideoSource {
	
	private final Server 			  server;
	private final URL 				  url;
	private final VideoFormat 		  format;
	private final Map<String, String> data;
	private final long				  size;
	private final String			  userAgent;
	
	public VideoSource(Server server, URL url, VideoFormat format, long size) {
		this(server, url, format, (Map<String, String>) null, size, null);
	}
	
	public VideoSource(Server server, URL url, VideoFormat format,
			String data, long size) {
		this(server, url, format, Utils.headers(data), size, null);
	}
	
	public VideoSource(Server server, URL url, VideoFormat format,
			Map<String, String> data, long size, String userAgent) {
		this.server    = server;
		this.url 	   = url;
		this.format    = format;
		this.data	   = data;
		this.size	   = size;
		this.userAgent = userAgent;
	}
	
	public Server getServer() {
		return server;
	}
	
	public URL getURL() {
		return url;
	}
	
	public String getURLString() {
		return url.toExternalForm();
	}
	
	public VideoFormat getFormat() {
		return format;
	}
	
	public Map<String, String> getData() {
		return data;
	}
	
	public long getSize() {
		return size;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	@Override
	public String toString() {
		return String.format("{server=%s, url=%s, format=%s, size=%d}",
			server.toString(), url.toExternalForm(), format.name(), size);
	}
}