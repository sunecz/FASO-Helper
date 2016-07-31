package sune.etc.faso.downloader;

import java.io.File;

import sune.etc.faso.event.DownloadEventM3U8;
import sune.etc.faso.event.DownloadEventM3U8.ByteDataWrapperM3U8;
import sune.etc.faso.event.DownloadEventM3U8.MessageHolder;
import sune.etc.faso.event.EventRegistry;
import sune.etc.faso.util.UserAgent;
import sune.etc.faso.video.VideoFormat;
import sune.etc.faso.video.VideoSource;
import sune.util.jd.m3u8.M3U8Downloader;

// Wrapper for M3U8Downloader class
public class DownloaderM3U8 implements Downloader<DownloadEventM3U8> {
	
	private static final VideoFormat[] SUPPORTED_FORMATS
		= new VideoFormat[] { VideoFormat.M3U8 };
	
	// Event registry for downloader-specific events
	private final EventRegistry<DownloadEventM3U8> eventRegistry
		= new EventRegistry<>();
	
	// Downloader instance
	private M3U8Downloader downloader;
		
	@Override
	public void download(VideoSource source, DownloadOptions options) {
		// If the current file is not yet downloaded, forbid continuing
		if(downloader != null) return;
		// Create a new instance of downloader
		downloader = new M3U8Downloader(
			source.getURL(), options.getOutput(),
			options.getUserAgent() == null ?
				source.getUserAgent() == null ?
					UserAgent.MOZILLA :
					source.getUserAgent() :
				options.getUserAgent(),
			null, new File(options.getOutput().getAbsolutePath() + ".ts"),
			source.getData()) {
			
			@Override
			public void onBegin() {
				eventRegistry.call(DownloadEventM3U8.BEGIN);
			}
			
			@Override
			public void onEnd() {
				eventRegistry.call(DownloadEventM3U8.END);
				cancel();
			}
			
			@Override
			public void onDownloadBegin(String path) {
				eventRegistry.call(DownloadEventM3U8.DOWNLOAD_BEGIN, path);
			}
			
			@Override
			public void onDownloadEnd(String path) {
				eventRegistry.call(DownloadEventM3U8.DOWNLOAD_END, path);
			}
			
			@Override
			public void onDownloadData(String path, byte[] buffer, int length) {
				eventRegistry.call(DownloadEventM3U8.DOWNLOAD_DATA,
					new ByteDataWrapperM3U8(path, buffer, length));
			}
			
			@Override
			public void onDownloadError(String path, String message) {
				eventRegistry.call(DownloadEventM3U8.DOWNLOAD_ERROR,
					new MessageHolder(path, message));
			}
			
			@Override
			public void onConversionBegin() {
				eventRegistry.call(DownloadEventM3U8.CONVERSION_BEGIN);
			}
			
			@Override
			public void onConversionChange(String text) {
				eventRegistry.call(DownloadEventM3U8.CONVERSION_UPDATE, text);
			}
			
			@Override
			public void onConversionEnd() {
				eventRegistry.call(DownloadEventM3U8.CONVERSION_END);
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
	public EventRegistry<DownloadEventM3U8> getEventRegistry() {
		return eventRegistry;
	}
}