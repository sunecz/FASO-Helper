package sune.etc.faso.downloader;

import sune.etc.faso.event.EventRegistry;
import sune.etc.faso.event.IEventType;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;

public interface Downloader<T extends IEventType> {
	
	public void download(VideoSource source, DownloadOptions options);
	public VideoFormat[] getSupportedFormats();
	// Events and events handling
	public EventRegistry<T> getEventRegistry();
}