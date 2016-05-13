package sune.etc.faso;

import sune.etc.faso.FASO.SerialData;

public class Serial {
	
	private String 	 url;
	private String 	 name;
	private String 	 title;
	private String 	 desc;
	private String 	 thumbnail;
	private Episodes episodes;
	
	public Serial(String url, String name, String title, String desc,
			String thumbnail, Episodes episodes) {
		this.url 	   = url;
		this.name 	   = name;
		this.title	   = title;
		this.desc	   = desc;
		this.thumbnail = thumbnail;
		this.episodes  = episodes;
	}
	
	public static Serial forURL(String url) {
		return forURL(url, false, false);
	}
	
	public static Serial forURL(String url, boolean loadFully,
			boolean multiThreaded) {
		return FASO.getSerialFromURL(url, loadFully, multiThreaded);
	}
	
	public static Serial forName(String name) {
		return forName(name, false, false);
	}
	
	public static Serial forName(String name, boolean loadFully,
			boolean multiThreaded) {
		return FASO.getSerial(name, loadFully, multiThreaded);
	}
	
	private void loadAdditionalData() {
		SerialData data = FASO.getSerialData(this);
		desc 			= data.description;
		thumbnail 		= data.thumbnailURL;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		if((desc == null)) {
			loadAdditionalData();
		}
		return desc;
	}
	
	public String getThumbnailURL() {
		if((thumbnail == null)) {
			loadAdditionalData();
		}
		return thumbnail;
	}
	
	public Episodes getEpisodes() {
		return getEpisodes(false);
	}
	
	public Episodes getEpisodes(boolean multiThreaded) {
		if((episodes == null)) {
			episodes = FASO.serialEpisodes(name, multiThreaded);
		}
		return episodes;
	}
}