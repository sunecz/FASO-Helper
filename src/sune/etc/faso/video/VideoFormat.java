package sune.etc.faso.video;

public enum VideoFormat {
	
	MP4("mp4"), M3U8("m3u8"), UNKNOWN(null);
	
	private final String extension;	
	private VideoFormat(String extension) {
		this.extension = extension;
	}
	
	public String getExtension() {
		return extension;
	}
}