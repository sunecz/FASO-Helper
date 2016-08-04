package sune.etc.faso.video;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;

import sune.etc.faso.Episode;
import sune.etc.faso.FASO;
import sune.etc.faso.provider.ServersProvider;
import sune.etc.faso.server.Server;
import sune.etc.faso.server.Servers;
import sune.etc.faso.util.ArrayItems;
import sune.etc.faso.util.Utils;

public class VideoSources extends ArrayItems<VideoSource> {
	
	public VideoSources(VideoSource... array) {
		super(array);
	}
	
	public VideoSources(List<VideoSource> list) {
		super(list.toArray(new VideoSource[list.size()]));
	}
	
	public static final VideoSources forDocument(Document document) {
		return forDocument(document, FASO.getServersProvider());
	}
	
	public static final VideoSources forDocument(Document document, ServersProvider provider) {
		List<VideoSource> list = new LinkedList<>();
		for(String name : provider.names()) {
			Server server 		  = provider.instance(name);
			VideoSource[] sources = server.getVideoSources(document);
			if(sources != null && sources.length > 0) {
				for(VideoSource source : sources)
					list.add(source);
			}
		}
		return new VideoSources(list);
	}
	
	public static VideoSources forURL(String url) {
		return forURL(url, FASO.getServersProvider());
	}
	
	public static VideoSources forURL(String url, ServersProvider provider) {
		return forDocument(Utils.getDocument(url), provider);
	}
	
	public static VideoSources forEpisode(Episode episode) {
		return forURL(episode.getURL());
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
	
	public Servers getServers(Document document) {
		List<Server> list = new LinkedList<>();
		for(VideoSource source : array)
			list.add(source.getServer());
		return new Servers(list);
	}
}