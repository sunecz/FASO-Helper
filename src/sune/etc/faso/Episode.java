package sune.etc.faso;

import sune.etc.faso.video.VideoSources;

public class Episode {
	
	private String url;
	private String playURL;
	private String title;
	
	public Episode(String url, String playURL, String title) {
		this.url  	 = url;
		this.playURL = playURL;
		this.title 	 = title;
	}
	
	public VideoSources getVideoSources() {
		return FASO.getVideoSources(url);
	}
	
	public String getURL() {
		return url;
	}
	
	public String getPlayURL() {
		return playURL;
	}
	
	public String getTitle() {
		return title;
	}
}