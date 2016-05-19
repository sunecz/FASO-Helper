package sune.etc.faso.event;

public class DownloadEventFLV implements IEventType {
	
	public static final EventType<DownloadEventFLV, Object> 		 BEGIN;
	public static final EventType<DownloadEventFLV, ByteDataWrapper> DOWNLOADED;
	public static final EventType<DownloadEventFLV, Object> 		 END;
	
	static {
		BEGIN 	   = new EventType<>();
		DOWNLOADED = new EventType<>();
		END 	   = new EventType<>();
	}
}