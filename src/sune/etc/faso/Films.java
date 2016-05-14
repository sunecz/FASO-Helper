package sune.etc.faso;

import java.util.List;

import sune.etc.faso.util.ArrayItems;

public class Films extends ArrayItems<Film> {
	
	public Films(Film... films) {
		super(films);
	}
	
	public Films(List<Film> films) {
		super(films.toArray(new Film[films.size()]));
	}
	
	public static Films get() {
		return FASO.getFilms();
	}
	
	public static Films get(Genre genre) {
		return FASO.getFilms(genre);
	}
}