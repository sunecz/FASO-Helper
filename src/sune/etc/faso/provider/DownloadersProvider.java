package sune.etc.faso.provider;

import sune.etc.faso.downloader.Downloader;

public class DownloadersProvider extends DefaultProvider<String, Downloader<?>> {
	
	DownloadersProvider() { super(String.class); }
}