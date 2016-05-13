package sune.etc.faso.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Registry<T> {
	
	private final Map<String, T> map;
	
	public Registry() {
		map = new LinkedHashMap<>();
	}
	
	public void register(String name, T value) {
		synchronized(map) {
			map.put(name, value);
		}
	}
	
	public void unregister(String name) {
		synchronized(map) {
			map.remove(name);
		}
	}
	
	public boolean contains(String name) {
		synchronized(map) {
			return map.containsKey(name);
		}
	}
	
	public T get(String name) {
		synchronized(map) {
			return map.get(name);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Entry<String, T>[] all() {
		Entry<?, ?>[] array;
		synchronized(map) {
			array = map.entrySet().toArray(
						new Entry<?, ?>[map.size()]);
		}
		return (Entry<String, T>[]) array;
	}
	
	@SuppressWarnings("unchecked")
	public T[] values() {
		Object[] array;
		synchronized(map) {
			array = map.values().toArray(
						new Object[map.size()]);
		}
		return (T[]) array;
	}
	
	public String[] names() {
		String[] array;
		synchronized(map) {
			array = map.keySet().toArray(
						new String[map.size()]);
		}
		return array;
	}
}