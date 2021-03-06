package sune.etc.faso.downloader;

import sune.etc.faso.event.ByteDataWrapper;
import sune.etc.faso.event.DownloadEventMP4;
import sune.etc.faso.event.EventRegistry;
import sune.etc.faso.util.UserAgent;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;
import sune.util.jd.mp4.MP4Downloader;

// Wrapper for MP4Downloader class
public class DownloaderMP4 implements Downloader<DownloadEventMP4> {
	
	private static final VideoFormat[] SUPPORTED_FORMATS
		= new VideoFormat[] { VideoFormat.MP4 };
	
	// Event registry for downloader-specific events
	private final EventRegistry<DownloadEventMP4> eventRegistry
		= new EventRegistry<>();
	
	// Downloader instance
	private MP4Downloader downloader;
	
	@Override
	public void download(VideoSource source, DownloadOptions options) {
		// If the current file is not yet downloaded, forbid continuing
		if(downloader != null) return;
		// Create a new instance of downloader
		downloader = new MP4Downloader(
			source.getURL(), options.getOutput(),
			options.getUserAgent() == null ?
				source.getUserAgent() == null ?
					UserAgent.MOZILLA :
					source.getUserAgent() :
				options.getUserAgent(),
			source.getData()) {
			
			@Override
			public void onBegin() {
				eventRegistry.call(DownloadEventMP4.BEGIN);
			}
			
			@Override
			public void onEnd() {
				eventRegistry.call(DownloadEventMP4.END);
				cancel();
			}
			
			@Override
			public void onDownloaded(byte[] buffer, int length) {
				eventRegistry.call(DownloadEventMP4.DOWNLOADED,
					new ByteDataWrapper(buffer, length));
			}
		};
		downloader.start();
	}

	@Override
	public void cancel() {
		if((downloader != null))
			downloader.stop();
		downloader = null;
	}
	
	@Override
	public VideoFormat[] getSupportedFormats() {
		return SUPPORTED_FORMATS;
	}
	
	@Override
	public EventRegistry<DownloadEventMP4> getEventRegistry() {
		return eventRegistry;
	}
}