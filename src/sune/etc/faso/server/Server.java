package sune.etc.faso.server;

import org.jsoup.nodes.Document;

import sune.etc.faso.video.VideoSource;

public interface Server {
	
	public VideoSource getVideoSource(Document document);
	public String getAddress();
	public String getName();
}