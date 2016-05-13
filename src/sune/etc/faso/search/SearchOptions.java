package sune.etc.faso.search;

public class SearchOptions {
	
	public static enum Genre {
		
		ALL,
		ACTION,
		SCIFI,
		HORROR,
		COMEDY,
		FAIRYTALE,
		ROMANTIC,
		DRAMA,
		FANTASY,
		THRILLER,
		MUSICAL,
		CRIMI,
		FAMILY,
		MYSTERY,
		DOCUMENTARY;
		
		public int getID() {
			return ordinal();
		}
	}
	
	private final String text;
	private final Genre genre;
	private final int year;
	
	public SearchOptions(String text) {
		this(text, Genre.ALL, 0);
	}
	
	public SearchOptions(String text, Genre genre, int year) {
		this.text  = text;
		this.genre = genre;
		this.year  = year;
	}
	
	public String getText() {
		return text;
	}
	
	public Genre getGenre() {
		return genre;
	}
	
	public int getYear() {
		return year;
	}
}