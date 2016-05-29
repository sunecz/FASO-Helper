package sune.etc.faso.video;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import sune.etc.faso.server.Server;
import sune.etc.faso.subtitles.Subtitles;

public class VideoSource {
	
	private final Server 			  server;
	private final URL 				  url;
	private final VideoFormat 		  format;
	private final Map<String, String> data;
	private final long				  size;
	private final String			  userAgent;
	private final VideoQuality		  quality;
	private final Subtitles[]		  subtitles;
	
	public VideoSource(Server server, URL url, VideoFormat format,
			Map<String, String> data, long size, String userAgent,
			VideoQuality quality, Subtitles[] subtitles) {
		this.server    = server;
		this.url 	   = url;
		this.format    = format;
		this.data	   = data;
		this.size	   = size;
		this.userAgent = userAgent;
		this.quality   = quality;
		this.subtitles = subtitles;
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
	
	public VideoQuality getQuality() {
		return quality;
	}
	
	public Subtitles[] getSubtitles() {
		return subtitles;
	}
	
	@Override
	public String toString() {
		return String.format("{server=%s, url=%s, format=%s, size=%d, quality=%s, subtitles=%s}",
			server.toString(), url.toExternalForm(), format.name(), size, quality,
			Arrays.toString(subtitles));
	}
}