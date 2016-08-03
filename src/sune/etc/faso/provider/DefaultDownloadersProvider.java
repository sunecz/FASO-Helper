package sune.etc.faso.provider;

import sune.etc.faso.downloader.DownloaderFLV;
import sune.etc.faso.downloader.DownloaderM3U8;
import sune.etc.faso.downloader.DownloaderMP4;

public class DefaultDownloadersProvider extends DownloadersProvider {
	
	public DefaultDownloadersProvider() {
		register("mp4",  DownloaderMP4.class);
		register("m3u8", DownloaderM3U8.class);
		register("flv",  DownloaderFLV.class);
	}
}