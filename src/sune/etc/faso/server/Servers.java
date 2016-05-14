package sune.etc.faso.server;

import java.util.List;

import sune.etc.faso.util.ArrayItems;

public class Servers extends ArrayItems<Server> {
	
	public Servers(Server[] array) {
		super(array);
	}
	
	public Servers(List<Server> list) {
		super(list.toArray(new Server[list.size()]));
	}
	
	public Server forName(String name) {
		for(Server server : array) {
			if(server.getName().equals(name))
				return server;
		}
		return null;
	}
}