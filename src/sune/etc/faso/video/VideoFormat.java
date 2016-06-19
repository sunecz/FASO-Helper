package sune.etc.faso.video;

import java.net.URL;

public enum VideoFormat {
	
	MP4,
	M3U8,
	FLV,
	UNKNOWN;
	
	private final String extension;
	private VideoFormat() {
		String ext = name().toLowerCase();
		extension  = ext.equals("unknown") ? null : ext;
	}
	
	public static final VideoFormat get(String url) {
		try {
			URL urlObj 	= new URL(url);
			// Get path part of the URL
			String path = urlObj.getPath();
			int index;
			// Get file name
			index = path.lastIndexOf('/');
			if(index > -1)
				path = path.substring(index+1);
			// Get file type
			String type = null;
			index 		= path.lastIndexOf('.');
			if(index > -1)
				type = path.substring(index+1);
			// Return the correct format
			switch(type.toLowerCase()) {
				case "mp4":  return MP4;
				case "m3u8": return M3U8;
				case "flv":  return FLV;
			}
		} catch(Exception ex) {
		}
		return UNKNOWN;
	}
	
	public String getExtension() {
		return extension;
	}
}