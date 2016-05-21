package sune.etc.faso.subtitles;

public class Subtitles {
	
	private final String url;
	private final String language;
	
	public Subtitles(String url, String language) {
		this.url 	  = url;
		this.language = language;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getLanguage() {
		return language;
	}
	
	@Override
	public String toString() {
		return String.format("{url=%s, language=%s}", url, language);
	}
}