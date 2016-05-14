package sune.etc.faso.search;

public class SearchResult {
	
	private final String titleText;
	private final String linkURL;
	private final String imageURL;
	
	public SearchResult(String titleText, String linkURL, String imageURL) {
		this.titleText = titleText;
		this.linkURL   = linkURL;
		this.imageURL  = imageURL;
	}
	
	public String getTitle() {
		return titleText;
	}
	
	public String getLinkURL() {
		return linkURL;
	}
	
	public String getThumbnailURL() {
		return imageURL;
	}
}