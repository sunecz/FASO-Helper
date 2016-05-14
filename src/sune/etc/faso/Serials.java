package sune.etc.faso;

import java.util.List;

import sune.etc.faso.util.ArrayItems;

public class Serials extends ArrayItems<Serial> {
	
	public Serials(Serial... serials) {
		super(serials);
	}
	
	public Serials(List<Serial> serials) {
		super(serials.toArray(new Serial[serials.size()]));
	}
	
	public static Serials get() {
		return get(false);
	}
	
	public static Serials get(boolean loadFully) {
		return FASO.getSerials(loadFully);
	}
}