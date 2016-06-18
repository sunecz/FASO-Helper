package sune.etc.faso.video;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sune.etc.faso.util.Utils;

public enum VideoQuality {
	
	QUALITY_UNKNOWN,
	QUALITY_240p,
	QUALITY_360p,
	QUALITY_480p,
	QUALITY_720p,
	QUALITY_1080p,
	QUALITY_2160p;
	
	private static final String PATTERN_PROGRESSIVE_SCAN = "^(\\d+)p?$";
	private final int value;
	private VideoQuality() {
		int val = -1;
		try {
			val = Utils.extractInt(name());
		} catch(Exception ex) {
		}
		this.value = val;
	}
	
	public static final VideoQuality get(String quality) {
		if(quality.equalsIgnoreCase("sd")) return QUALITY_480p;
		if(quality.equalsIgnoreCase("hd")) return QUALITY_720p;
		// Special cases for quality names (e.g. Low, Medium, High, ...)
		if(quality.equalsIgnoreCase("low"))    return QUALITY_240p;
		if(quality.equalsIgnoreCase("middle")) return QUALITY_480p;
		if(quality.equalsIgnoreCase("high"))   return QUALITY_720p;
		Pattern p = Pattern.compile(PATTERN_PROGRESSIVE_SCAN);
		Matcher m = p.matcher(quality);
		if(m.matches()) {
			switch(Integer.parseInt(m.group(1))) {
				case 240:  return QUALITY_240p;
				case 360:  return QUALITY_360p;
				case 480:  return QUALITY_480p;
				case 720:  return QUALITY_720p;
				case 1080: return QUALITY_1080p;
				case 2160: return QUALITY_2160p;
			}
		}
		return QUALITY_UNKNOWN;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value == -1 ? "unknown" : (value + "p");
	}
}