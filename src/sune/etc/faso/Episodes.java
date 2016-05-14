package sune.etc.faso;

import java.util.List;

import sune.etc.faso.util.ArrayItems;

public class Episodes extends ArrayItems<Episode> {
	
	public Episodes(Episode... episodes) {
		super(episodes);
	}
	
	public Episodes(List<Episode> episodes) {
		super(episodes.toArray(new Episode[episodes.size()]));
	}
}