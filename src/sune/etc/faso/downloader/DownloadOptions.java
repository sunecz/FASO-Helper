package sune.etc.faso.downloader;

import java.io.File;

public class DownloadOptions {
	
	private final File 	 output;
	private final String userAgent;
	
	public DownloadOptions(File output) {
		this(output, null);
	}
	
	public DownloadOptions(File output, String userAgent) {
		this.output    = output;
		this.userAgent = userAgent;
	}
	
	public File getOutput() {
		return output;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
}