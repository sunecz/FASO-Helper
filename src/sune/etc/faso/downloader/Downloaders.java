package sune.etc.faso.downloader;

import java.util.LinkedList;
import java.util.List;

import sune.etc.faso.FASO;
import sune.etc.faso.util.ArrayItems;
import sune.etc.faso.video.VideoFormat;

public class Downloaders extends ArrayItems<Downloader<?>> {
	
	public Downloaders(List<Downloader<?>> list) {
		super(list.toArray(new Downloader[list.size()]));
	}
	
	public Downloaders(Downloader<?>... array) {
		super(array);
	}
	
	public static Downloaders forVideoFormat(VideoFormat format) {
		return FASO.DOWNLOADERS.getDownloaders().forFormat(format);
	}
	
	public Downloaders forFormat(VideoFormat format) {
		List<Downloader<?>> list = new LinkedList<>();
		for(Downloader<?> downloader : array) {
			for(VideoFormat vf :
					downloader.getSupportedFormats()) {
				if(vf == format) {
					list.add(downloader);
				}
			}
		}
		return new Downloaders(list);
	}
}