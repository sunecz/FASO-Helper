package sune.etc.faso.event;

public class DownloadEventMP4 implements IEventType {
	
	public static final EventType<DownloadEventMP4, Object> 		 BEGIN;
	public static final EventType<DownloadEventMP4, ByteDataWrapper> DOWNLOADED;
	public static final EventType<DownloadEventMP4, Object> 		 END;
	
	static {
		BEGIN 	   = new EventType<>();
		DOWNLOADED = new EventType<>();
		END 	   = new EventType<>();
	}
}