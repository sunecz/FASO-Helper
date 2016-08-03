package sune.etc.faso.downloader;

import sune.etc.faso.event.EventRegistry;
import sune.etc.faso.event.IEventType;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;

public interface Downloader<T extends IEventType> {
	
	void download(VideoSource source, DownloadOptions options);
	void cancel();
	VideoFormat[] getSupportedFormats();
	// Events and events handling
	EventRegistry<T> getEventRegistry();
}