package sune.etc.faso.downloader;

import sune.etc.faso.event.ByteDataWrapper;
import sune.etc.faso.event.DownloadEventFLV;
import sune.etc.faso.event.EventRegistry;
import sune.etc.faso.util.UserAgent;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;
import sune.util.jd.mp4.MP4Downloader;

// Wrapper for MP4Downloader class since FLVDownloader does not exist
public class DownloaderFLV implements Downloader<DownloadEventFLV> {
	
	private static final VideoFormat[] SUPPORTED_FORMATS
		= new VideoFormat[] { VideoFormat.FLV };
	
	// Event registry for downloader-specific events
	private final EventRegistry<DownloadEventFLV> eventRegistry
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
				eventRegistry.call(DownloadEventFLV.BEGIN);
			}
			
			@Override
			public void onEnd() {
				eventRegistry.call(DownloadEventFLV.END);
				cancel();
			}
			
			@Override
			public void onDownloaded(byte[] buffer, int length) {
				eventRegistry.call(DownloadEventFLV.DOWNLOADED,
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
	public EventRegistry<DownloadEventFLV> getEventRegistry() {
		return eventRegistry;
	}
}