package sune.etc.faso;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sune.etc.faso.downloader.Downloader;
import sune.etc.faso.downloader.DownloaderFLV;
import sune.etc.faso.downloader.DownloaderM3U8;
import sune.etc.faso.downloader.DownloaderMP4;
import sune.etc.faso.event.IEventType;
import sune.etc.faso.registry.DownloaderRegistry;
import sune.etc.faso.registry.ServerRegistry;
import sune.etc.faso.search.DefaultSearchEngine;
import sune.etc.faso.search.SearchEngine;
import sune.etc.faso.search.SearchOptions;
import sune.etc.faso.search.SearchResults;
import sune.etc.faso.server.Server;
import sune.etc.faso.server.ServerAllMyVideos;
import sune.etc.faso.server.ServerAllVid;
import sune.etc.faso.server.ServerAnyFiles;
import sune.etc.faso.server.ServerExashare;
import sune.etc.faso.server.ServerFlashXTV;
import sune.etc.faso.server.ServerHqqTV;
import sune.etc.faso.server.ServerLetWatch;
import sune.etc.faso.server.ServerOpenLoad;
import sune.etc.faso.server.ServerVeoh;
import sune.etc.faso.server.ServerVodLocker;
import sune.etc.faso.server.ServerYouWatch;
import sune.etc.faso.util.Utils;
import sune.etc.faso.video.VideoSources;

public final class FASO {
	
	public static final String 			   SERVER_URL;
	public static final ServerRegistry 	   SERVERS;
	public static final DownloaderRegistry DOWNLOADERS;
	
	private static final String REGEX_VIDEO_PAGE_LINK;
	private static final String REGEX_LINK_EPISODE;
	
	private static final String REGEX_LINK_HREF_PAGE;
	private static final String REGEX_LINK_HREF_ITEM;
	
	private static final String REGEX_URL_SERIAL;
	private static final String REGEX_URL_FILM;
	private static final String REGEX_URL_EPISODE;
	private static final String REGEX_URL_PLAY_VIDEO;
	private static final String REGEX_URL_VIDEO_FRAME;
	
	static {
		SERVER_URL 	= "http://filmy-serialy-online.tv";
		SERVERS 	= new ServerRegistry();
		DOWNLOADERS = new DownloaderRegistry();
		// Register all the default servers
		SERVERS.register("hqqtv", 	 	ServerHqqTV.class);
		SERVERS.register("flashxtv", 	ServerFlashXTV.class);
		SERVERS.register("youwatch", 	ServerYouWatch.class);
		SERVERS.register("exashare", 	ServerExashare.class);
		SERVERS.register("anyfiles", 	ServerAnyFiles.class);
		SERVERS.register("openload", 	ServerOpenLoad.class);
		SERVERS.register("letwatch", 	ServerLetWatch.class);
		SERVERS.register("allvid", 	 	ServerAllVid.class);
		SERVERS.register("allmyvideos", ServerAllMyVideos.class);
		SERVERS.register("vodlocker", 	ServerVodLocker.class);
		SERVERS.register("veoh", 		ServerVeoh.class);
		// Register all the default downloaders
		DOWNLOADERS.register("mp4",  DownloaderMP4.class);
		DOWNLOADERS.register("m3u8", DownloaderM3U8.class);
		DOWNLOADERS.register("flv",  DownloaderFLV.class);
		// Set the important Regular expressions
		REGEX_VIDEO_PAGE_LINK = "^/video/(.*?)/watchv=(.*?)/([0-9]+)/(?:\\#c1)?$";
		REGEX_LINK_EPISODE	  = "^/serialy/(.*?)/(.*?)/(\\d+)/$";
		REGEX_URL_SERIAL	  = "^https?://(?:www\\.)?filmy-serialy-online\\.tv/serialy/(.*?)/$";
		REGEX_URL_FILM		  = "^https?://(?:www\\.)?filmy-serialy-online\\.tv/filmy/(.*?)/(.*?)/(\\d+)/$";
		REGEX_URL_EPISODE 	  = "^https?://(?:www\\.)?filmy-serialy-online\\.tv/serialy/(.*?)/(.*?)/(\\d+)/$";
		REGEX_URL_PLAY_VIDEO  = "^https?://(?:www\\.)?filmy-serialy-online\\.tv/video/(.*?)/watchv=(.*?)/(\\d+)/(?:#c1)?$";
		REGEX_URL_VIDEO_FRAME = "^https?://(?:www\\.)?filmy-serialy-online\\.tv/video_frame\\.php\\?id=(\\d+)$";
		REGEX_LINK_HREF_PAGE  = "^&strana=(\\d+)$";
		REGEX_LINK_HREF_ITEM  = "^/(.*?)/(.*?)/(.*?)/(\\d+)/$";
	}
	
	static String[] urlPages(String url) {
		String cuturl 	  = url.substring(SERVER_URL.length());
		List<String> urls = new LinkedList<>();
		Document doc 	  = Utils.getDocument(url);
		Pattern pattern	  = Pattern.compile(REGEX_LINK_HREF_PAGE);
		int minPage = 1;
		int maxPage = 1;
		for(Element link : doc.select("a[href]")) {
			String href = link.attr("href");
			if(href.startsWith(cuturl)) {
				String cuthref	= href.substring(cuturl.length());
				Matcher matcher = pattern.matcher(cuthref);
				if(matcher.matches()) {
					try {
						int val = Integer.parseInt(matcher.group(1));
						if(val > maxPage) maxPage = val;
					} catch(Exception ex) {
					}
				}
			}
		}
		for(int i = minPage; i <= maxPage; ++i) {
			urls.add(url + "&strana=" + i);
		}
		return urls.toArray(new String[urls.size()]);
	}
	
	static String[] urlItems(String pageURL, String type, String name) {
		List<String> eps = new LinkedList<>();
		Document doc 	 = Utils.getDocument(pageURL);
		Pattern pattern  = Pattern.compile(REGEX_LINK_HREF_ITEM);
		for(Element link : doc.select("a[href]")) {
			String href 	= link.attr("href");
			Matcher matcher = pattern.matcher(href);
			if(matcher.matches() &&
			   matcher.group(1).equals(type) &&
			   matcher.group(2).equals(name)) {
				String url = SERVER_URL + href;
				if(!eps.contains(url))
					eps.add(url);
			}
		}
		return eps.toArray(new String[eps.size()]);
	}
	
	static String serialURL(String name) {
		return SERVER_URL + "/serialy/" + name + "/";
	}
	
	static String[] serialPages(String name) {
		return urlPages(serialURL(name));
	}
	
	static String[] serialPageEpisodes(String pageURL, String serial) {
		return urlItems(pageURL, "serialy", serial);
	}
	
	static String[] serialEpisodesURL(String serial) {
		List<String> eps = new LinkedList<>();
		String[] pages 	 = serialPages(serial);
		int length 		 = pages.length;
		// Supporting variables that helps to maintain synchronization
		AtomicBoolean[] bools = new AtomicBoolean[length];
		for(int i = 0; i < length; ++i)
			bools[i] = new AtomicBoolean();
		AtomicInteger ai = new AtomicInteger();
		for(int i = 0; i < length; ++i) {
			String page = pages[i];
			final int k = i;
			// Increase the counter before a thread is started
			ai.incrementAndGet();
			// Create and start a new thread
			new Thread(() -> {
				List<String> list = new LinkedList<>();
				for(String episode :
						serialPageEpisodes(page, serial)) {
					if(!list.contains(episode))
						list.add(episode);
				}
				// Continue only when the previous items was added
				if(k > 0) {
					AtomicBoolean bool = bools[k-1];
					while(!bool.get()) Utils.sleep(1);
				}
				// Synchronizely add the items to the final collection
				synchronized(eps) {
					eps.addAll(list);
				}
				bools[k].lazySet(true);
				ai.decrementAndGet();
			}).start();
		}
		while(ai.get() > 0) Utils.sleep(1);
		return eps.toArray(new String[eps.size()]);
	}
	
	static String episodePlayURL(Document doc) {
		for(Element link : doc.select("a[href]")) {
			String href = link.attr("href");
			if(href.matches(REGEX_VIDEO_PAGE_LINK))
				return SERVER_URL + href;
		}
		return null;
	}
	
	static final Episodes serialEpisodesSingleThread(String name) {
		List<Episode> episodes = new LinkedList<>();
		String[] pages 		   = serialEpisodesURL(name);
		// Invert the array so that the epsiode at index 0 is the first one
		for(int i = pages.length-1; i > -1; --i) {
			String page    = pages[i];
			Document edoc  = Utils.getDocument(page);
			Elements estrs = edoc.select("strong");
			// The episode's name is in the second strong tag
			String etitle  = estrs.size() > 1 ?
								estrs.get(1).text() :
								null;
			String playURL = episodePlayURL(edoc);
			episodes.add(new Episode(page, playURL, etitle));
		}
		return new Episodes(episodes.toArray(new Episode[episodes.size()]));
	}
	
	static final Episodes serialEpisodesMultiThread(String name) {
		List<Episode> episodes = new LinkedList<>();
		String[] pages 		   = serialEpisodesURL(name);
		int length			   = pages.length;
		// Supporting variables that helps to maintain synchronization
		AtomicBoolean[] bools = new AtomicBoolean[length];
		for(int i = 0; i < length; ++i)
			bools[i] = new AtomicBoolean();
		AtomicInteger ai = new AtomicInteger();
		// Invert the array so that the epsiode at index 0 is the first one
		for(int i = length-1; i > -1; --i) {
			String page = pages[i];
			final int k = length-i-1;
			// Increase the counter before a thread is started
			ai.incrementAndGet();
			// Create and start a new thread
			new Thread(() -> {
				String content = null;
				do {
					content = Utils.quickGETRequest(page);
					if((content == null || content.isEmpty() ||
						content.contains("503 Service Unavailable"))) {
						// Wait some time since the server can be overloaded
						// because of the sent requests. This is also the cause
						// why 503 http status code shows up.
						Utils.sleep(200);
					} else break;
				} while(true);
				Document edoc  = Utils.createDocument(content);
				Elements estrs = edoc.select("strong");
				// Check if the content was gotten correctly
				// The episode's name is in the second strong tag
				String etitle  = estrs.get(1).text();
				String playURL = episodePlayURL(edoc);
				// Continue only when the previous items was added
				if(k > 0) {
					AtomicBoolean bool = bools[k-1];
					while(!bool.get()) Utils.sleep(1);
				}
				// Synchronizely add the items to the final collection
				synchronized(episodes) {
					episodes.add(new Episode(page, playURL, etitle));
				}
				bools[k].lazySet(true);
				ai.decrementAndGet();
			}).start();
		}
		while(ai.get() > 0) Utils.sleep(1);
		return new Episodes(episodes.toArray(new Episode[episodes.size()]));
	}
	
	static String videoFrameURL(int videoID) {
		return SERVER_URL + "/video_frame.php?id=" + videoID;
	}
	
	static String videoFrameURL(String url) {
		Pattern p0 = Pattern.compile(REGEX_URL_FILM);
		Pattern p1 = Pattern.compile(REGEX_URL_EPISODE);
		Pattern p2 = Pattern.compile(REGEX_URL_PLAY_VIDEO);
		Pattern p3 = Pattern.compile(REGEX_URL_VIDEO_FRAME);
		Matcher m; int videoID = -1;
		if((m = p0.matcher(url)) != null &&
		   (m.matches())) {
			videoID = Integer.parseInt(m.group(3));
		} else if((m = p1.matcher(url)) != null &&
				  (m.matches())) {
			videoID = Integer.parseInt(m.group(3));
		} else if((m = p2.matcher(url)) != null &&
				  (m.matches())) {
			videoID = Integer.parseInt(m.group(3));
		} else if((m = p3.matcher(url)) != null &&
				  (m.matches())) {
			videoID = Integer.parseInt(m.group(1));
		}
		return videoFrameURL(videoID);
	}
	
	static String serialName(String url) {
		Pattern p0 = Pattern.compile(REGEX_URL_SERIAL);
		Pattern p1 = Pattern.compile(REGEX_URL_EPISODE);
		Matcher m;
		if((m = p0.matcher(url)) != null &&
		   (m.matches())) {
			return m.group(1);
		} else if((m = p1.matcher(url)) != null &&
				  (m.matches())) {
			return m.group(1);
		}
		return null;
	}
	
	static final String serialThumbnailURL(int firstEpID) {
		return "http://filmy-serialy-online.eu/data/filmy/" + firstEpID + ".jpg";
	}
	
	static final Episodes serialEpisodes(String name, boolean multiThreaded) {
		return multiThreaded ? serialEpisodesMultiThread(name) :
			serialEpisodesSingleThread(name);
	}
	
	static final AdditionalData serialData(String name) {
		String[] pages   = serialPages(name);
		String lastPage  = pages[pages.length-1];
		int minID 		 = Integer.MAX_VALUE;
		Document doc 	 = Utils.getDocument(lastPage);
		Pattern pattern  = Pattern.compile(REGEX_LINK_EPISODE);
		for(Element link : doc.select("a[href]")) {
			String href 	= link.attr("href");
			Matcher matcher = pattern.matcher(href);
			if(matcher.matches() &&
			   matcher.group(1).equals(name)) {
				int id = Integer.parseInt(matcher.group(3));
				if(id < minID) minID = id;
			}
		}
		String thumbURL = minID != Integer.MAX_VALUE ?
			serialThumbnailURL(minID-1) : null;
		Elements sstrs = doc.select("strong");
		// The serial's description is in the fourth strong tag
		String desc  = sstrs.get(3).text();
		return new AdditionalData(desc, thumbURL);
	}
	
	static final String filmGenreURL(Genre genre) {
		return SERVER_URL + "/filmy/" + genre.getName() + "/";
	}
	
	static final class AdditionalData {
		
		public final String description;
		public final String thumbnailURL;
		
		public AdditionalData(String description, String thumbnailURL) {
			this.description  = description;
			this.thumbnailURL = thumbnailURL;
		}
	}
	
	public static final Serial getSerial(String name) {
		return getSerial(name, false, false);
	}
	
	public static final Serial getSerial(String name, boolean loadEpisodes,
			boolean multiThreaded) {
		String url 	   = serialURL(name);
		Document sdoc  = Utils.getDocument(url);
		Elements sstrs = sdoc.select("strong");
		// The serial's name is in the third strong tag
		String stitle = sstrs.get(2).text();
		// The serial's description is in the fourth strong tag
		String sdesc  = sstrs.get(3).text();
		// Get the serial thumbnail's url
		String thumbnail = null;
		Pattern p_thumb  = Pattern.compile("url\\('?(.*?)'?\\)");
		for(Element table : sdoc.select("table.rounded")) {
			String style 	= table.attr("style");
			Matcher matcher = p_thumb.matcher(style);
			if(matcher.find()) {
				thumbnail = matcher.group(1);
				break;
			}
		}
		return new Serial(url, name, stitle, sdesc, thumbnail,
			loadEpisodes ?
				serialEpisodes(name, multiThreaded) :
				null);
	}
	
	public static final Serials getSerials() {
		return getSerials(false);
	}
	
	public static final Serials getSerials(boolean loadFully) {
		Document doc = Utils.getDocument(SERVER_URL);
		Element menu = doc.getElementById("menu_serialy");
		Elements trs = menu.select("table.rounded_menu tbody tr td table tr");
		List<Serial> serials = new LinkedList<>();
		for(int i = 0, l = trs.size(); i < l; i+=2) {
			Element anchor = trs.get(i).select("a").first();
			String name    = anchor.attr("href");
			// Get correct name from the link
			name = name.substring(0, name.length()-1);
			int index = name.lastIndexOf('/');
			name = name.substring(index+1);
			// Create the serial object
			if(loadFully) {
				// Add the serial object
				serials.add(getSerial(name));
			} else {
				// Get some more information
				String url 	 = serialURL(name);
				String title = anchor.text();
				// Add the serial object
				serials.add(new Serial(url, name, title, null, null, null));
			}
		}
		return new Serials(serials.toArray(new Serial[serials.size()]));
	}
	
	public static final AdditionalData getSerialData(Serial serial) {
		return serialData(serial.getName());
	}
	
	public static final Serial getSerialFromURL(String url) {
		return getSerialFromURL(url, false, false);
	}
	
	public static final Serial getSerialFromURL(String url, boolean loadFully,
			boolean multiThreaded) {
		return getSerial(serialName(url), loadFully, multiThreaded);
	}
	
	public static final Film getFilm(String url) {
		Document sdoc = Utils.getDocument(url);
		// The film's name is in the third strong tag
		String stitle = sdoc.select("strong").first().text();
		// The film's description is in an anchor with style attribute
		String sdesc 	 = "";
		Elements anchors = sdoc.select("strong + br + a[style]");
		// The description does not have to exist
		if(!anchors.isEmpty()) {
			sdesc = anchors.first().text();
		}
		// Get the film thumbnail's url
		String thumbnail = null;
		Pattern p_thumb  = Pattern.compile("url\\('?(.*?)'?\\)");
		for(Element table : sdoc.select("table.rounded")) {
			String style 	= table.attr("style");
			Matcher matcher = p_thumb.matcher(style);
			if(matcher.find()) {
				thumbnail = matcher.group(1);
				break;
			}
		}
		return new Film(url, stitle, sdesc, thumbnail);
	}
	
	public static final Films getFilms() {
		List<Film> films = new LinkedList<>();
		for(Genre genre : Genre.values()) {
			for(Film film : getFilms(genre))
				films.add(film);
		}
		return new Films(films);
	}
	
	public static final Films getFilms(Genre genre) {
		if(genre == Genre.ALL) return getFilms();
		List<Film> films = new LinkedList<>();
		for(String page : urlPages(filmGenreURL(genre))) {
			for(String item : urlItems(page, "filmy", genre.getName())) {
				films.add(getFilm(item));
			}
		}
		return new Films(films);
	}
	
	public static final VideoSources getVideoSources(String url) {
		return SERVERS.getVideoSources(Utils.getDocument(videoFrameURL(url)));
	}
	
	@SuppressWarnings("unchecked")
	public static final <T extends Server> T getServer(String name, Object... args) {
		return (T) SERVERS.instance(name, args);
	}
	
	@SuppressWarnings("unchecked")
	public static final <E extends IEventType, T extends Downloader<E>> T getDownloader(
			String name, Object... args) {
		return (T) DOWNLOADERS.instance(name, args);
	}
	
	static SearchEngine SEARCH_ENGINE = new DefaultSearchEngine();
	public static void setSearchEngine(SearchEngine engine) {
		if(engine == null) {
			throw new IllegalArgumentException(
				"Search engine cannot be null!");
		}
		SEARCH_ENGINE = engine;
	}
	public static SearchEngine getSearchEngine() { return SEARCH_ENGINE; }
	
	public static SearchResults search(SearchOptions options) {
		return SEARCH_ENGINE.search(options);
	}
}