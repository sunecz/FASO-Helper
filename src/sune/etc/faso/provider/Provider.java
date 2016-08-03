package sune.etc.faso.provider;

public interface Provider<E, T> {
	
	void register(E key, T value);
	void unregister(E key);
	T get(E key);
	T[] values();
	E[] names();
}