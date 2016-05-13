package sune.etc.faso.event;

public class DownloadEventM3U8 implements IEventType {
	
	public static final class ByteDataWrapperM3U8
			extends ByteDataWrapper {
		
		public final String path;
		public ByteDataWrapperM3U8(String path, byte[] data, int length) {
			super(data, length);
			this.path = path;
		}
	}
	
	public static final class MessageHolder {
		
		public final String file;
		public final String message;
		
		public MessageHolder(String file, String message) {
			this.file 	 = file;
			this.message = message;
		}
	}
	
	public static final EventType<DownloadEventM3U8, Object> 			  BEGIN;
	public static final EventType<DownloadEventM3U8, Object> 			  END;
	public static final EventType<DownloadEventM3U8, String> 			  DOWNLOAD_BEGIN;
	public static final EventType<DownloadEventM3U8, String> 			  DOWNLOAD_END;
	public static final EventType<DownloadEventM3U8, ByteDataWrapperM3U8> DOWNLOAD_DATA;
	public static final EventType<DownloadEventM3U8, MessageHolder> 	  DOWNLOAD_ERROR;
	public static final EventType<DownloadEventM3U8, Object> 			  CONVERSION_BEGIN;
	public static final EventType<DownloadEventM3U8, Object> 			  CONVERSION_END;
	public static final EventType<DownloadEventM3U8, String> 			  CONVERSION_UPDATE;
	
	static {
		BEGIN 			  = new EventType<>();
		END				  = new EventType<>();
		DOWNLOAD_BEGIN 	  = new EventType<>();
		DOWNLOAD_END 	  = new EventType<>();
		DOWNLOAD_DATA 	  = new EventType<>();
		DOWNLOAD_ERROR 	  = new EventType<>();
		CONVERSION_BEGIN  = new EventType<>();
		CONVERSION_END 	  = new EventType<>();
		CONVERSION_UPDATE = new EventType<>();
	}
}