package sune.etc.faso.registry;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.nodes.Document;

import sune.etc.faso.server.Server;
import sune.etc.faso.server.Servers;
import sune.etc.faso.video.VideoSource;
import sune.etc.faso.video.VideoSources;

public class ServerRegistry extends ClassRegistry<Server> {
	
	public Servers getServers(Document document) {
		List<Server> list = new LinkedList<>();
		for(String name : names()) {
			Server server 		  = instance(name);
			VideoSource[] sources = server.getVideoSources(document);
			if(sources != null && sources.length > 0) {
				list.add(server);
			}
		}
		return new Servers(list);
	}
	
	public VideoSources getVideoSources(Document document) {
		List<VideoSource> list = new LinkedList<>();
		for(String name : names()) {
			Server server 		  = instance(name);
			VideoSource[] sources = server.getVideoSources(document);
			if(sources != null && sources.length > 0) {
				for(VideoSource source : sources)
					list.add(source);
			}
		}
		return new VideoSources(list);
	}
}