package sune.etc.faso.video;

import java.util.LinkedList;
import java.util.List;

import sune.etc.faso.Episode;
import sune.etc.faso.FASO;
import sune.etc.faso.util.ArrayItems;

public class VideoSources extends ArrayItems<VideoSource> {
	
	public VideoSources(VideoSource... array) {
		super(array);
	}
	
	public VideoSources(List<VideoSource> list) {
		super(list.toArray(new VideoSource[list.size()]));
	}
	
	public static VideoSources forURL(String url) {
		return FASO.getVideoSources(url);
	}
	
	public static VideoSources forEpisode(Episode episode) {
		return FASO.getVideoSources(episode.getURL());
	}
	
	public VideoSources forFormat(VideoFormat format) {
		List<VideoSource> list = new LinkedList<>();
		for(VideoSource source : array) {
			if(source.getFormat() == format) {
				list.add(source);
			}
		}
		return new VideoSources(list);
	}
	
	public VideoSources forServerName(String name) {
		List<VideoSource> list = new LinkedList<>();
		for(VideoSource source : array) {
			if(source.getServer()
					 .getName()
					 .equalsIgnoreCase(name)) {
				list.add(source);
			}
		}
		return new VideoSources(list);
	}
}