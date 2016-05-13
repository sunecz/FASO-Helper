package sune.etc.faso.event;

@FunctionalInterface
public interface Listener<T> {
	
	public void call(T value);
}