package sune.etc.faso;

import sune.etc.faso.video.VideoSources;

public class Film {
	
	private String url;
	private String title;
	private String desc;
	private String thumbnail;
	
	public Film(String url, String title, String desc, String thumbnail) {
		this.url 	   = url;
		this.title	   = title;
		this.desc	   = desc;
		this.thumbnail = thumbnail;
	}
	
	public static Film forURL(String url) {
		return FASO.getFilm(url);
	}
	
	public String getURL() {
		return url;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return desc;
	}
	
	public String getThumbnailURL() {
		return thumbnail;
	}
	
	public VideoSources getVideoSources() {
		return FASO.getVideoSources(url);
	}
}