package sune.etc.faso.provider;

import sune.etc.faso.server.ServerAllMyVideos;
import sune.etc.faso.server.ServerAllVid;
import sune.etc.faso.server.ServerAnyFiles;
import sune.etc.faso.server.ServerExashare;
import sune.etc.faso.server.ServerFlashXTV;
import sune.etc.faso.server.ServerHqqTV;
import sune.etc.faso.server.ServerLetWatch;
import sune.etc.faso.server.ServerOpenLoad;
import sune.etc.faso.server.ServerTheVideo;
import sune.etc.faso.server.ServerVShare;
import sune.etc.faso.server.ServerVeoh;
import sune.etc.faso.server.ServerVidziTV;
import sune.etc.faso.server.ServerVodLocker;
import sune.etc.faso.server.ServerYouWatch;

public class DefaultServersProvider extends ServersProvider {
	
	public DefaultServersProvider() {
		register("hqqtv", 	 	ServerHqqTV.class);
		register("flashxtv", 	ServerFlashXTV.class);
		register("youwatch", 	ServerYouWatch.class);
		register("exashare", 	ServerExashare.class);
		register("anyfiles", 	ServerAnyFiles.class);
		register("openload", 	ServerOpenLoad.class);
		register("letwatch", 	ServerLetWatch.class);
		register("allvid", 	 	ServerAllVid.class);
		register("allmyvideos", ServerAllMyVideos.class);
		register("vodlocker", 	ServerVodLocker.class);
		register("veoh", 		ServerVeoh.class);
		register("vshare",		ServerVShare.class);
		register("vidzitv",		ServerVidziTV.class);
		register("thevideo",	ServerTheVideo.class);
	}
}