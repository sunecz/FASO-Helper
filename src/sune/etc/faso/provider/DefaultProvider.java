package sune.etc.faso.provider;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DefaultProvider<E, T> extends ClassProvider<E, T> {
	
	// Map for all the objects
	private final Map<E, Class<? extends T>> servers;
	private final Class<E> 					 classE;
	
	DefaultProvider(Class<E> classE) {
		this(classE, new LinkedHashMap<>());
	}
	
	DefaultProvider(Class<E> classE, Map<E, Class<? extends T>> map) {
		this.classE  = classE;
		this.servers = map;
	}
	
	@Override
	public void register(E name, Class<? extends T> value) {
		servers.put(name, value);
	}
	
	@Override
	public void unregister(E name) {
		servers.remove(name);
	}
	
	@Override
	public Class<? extends T> get(E name) {
		return servers.get(name);
	}
	
	@SuppressWarnings("unchecked")
	Class<? extends T>[] values0() {
		Collection<Class<? extends T>> coll = servers.values();
		return coll.toArray(new Class[coll.size()]);
	}
	
	@Override
	public Class<? extends T>[] values() {
		// Call the internal method
		return values0();
	}
	
	@SuppressWarnings("unchecked")
	E[] names0() {
		Set<E> coll = servers.keySet();
		int length  = coll.size();
		E[] array   = (E[]) Array.newInstance(classE, length);
		int i = 0; for(E item : coll) array[i++] = item;
		return array;
	}
	
	@Override
	public E[] names() {
		// Call the internal method
		return names0();
	}
}