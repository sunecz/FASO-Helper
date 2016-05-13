package sune.etc.faso;

import sune.etc.faso.util.ArrayItems;

public class Serials extends ArrayItems<Serial> {
	
	public Serials(Serial... serials) {
		super(serials);
	}
	
	public static Serials get() {
		return get(false);
	}
	
	public static Serials get(boolean loadFully) {
		return FASO.getSerials(loadFully);
	}
}