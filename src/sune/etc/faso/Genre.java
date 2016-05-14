package sune.etc.faso;

public enum Genre {
	
	ALL			(null),
	ACTION		("akcne"),
	SCIFI		("scifi"),
	HORROR		("horory"),
	COMEDY		("komedie"),
	FAIRYTALE	("rozpravky"),
	ROMANTIC	("romanticke"),
	DRAMA		("drama"),
	FANTASY		("fantasy"),
	THRILLER	("thriller"),
	MUSICAL		("hudobne"),
	CRIMI		("krimi"),
	FAMILY		("rodinne"),
	MYSTERY		("mysteriozne"),
	DOCUMENTARY	("dokumentarne");
	
	private final String name;
	private Genre(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ordinal();
	}
}