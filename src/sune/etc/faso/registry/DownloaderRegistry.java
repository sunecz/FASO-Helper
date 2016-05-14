package sune.etc.faso.registry;

import java.util.LinkedList;
import java.util.List;

import sune.etc.faso.downloader.Downloader;
import sune.etc.faso.downloader.Downloaders;

public class DownloaderRegistry extends ClassRegistry<Downloader<?>> {
	
	public Downloaders getDownloaders() {
		List<Downloader<?>> list = new LinkedList<>();
		for(String name : names()) {
			list.add(instance(name));
		}
		return new Downloaders(list);
	}
}