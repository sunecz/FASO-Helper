package sune.etc.faso.server;

import org.jsoup.nodes.Document;

import sune.etc.faso.video.VideoSource;

public interface Server {
	
	// Functional methods
	public VideoSource[] getVideoSources(Document document);
	// Informational methods
	public String getAddress();
	public String getName();
}