package sune.etc.faso.registry;

import java.lang.reflect.Constructor;

import sune.etc.faso.util.Utils;

public class ClassRegistry<T> extends Registry<Class<? extends T>> {
	
	@SuppressWarnings("unchecked")
	T instance0(String name, Object... args) {
		if(name == null) {
			throw new IllegalArgumentException(
				"Name cannot be null!");
		}
		Class<T> clazz;
		if((clazz = (Class<T>) get(name)) != null) {
			try {
				Class<?>[] classes = Utils.recognizeClasses(args);
				Constructor<T> con = clazz.getDeclaredConstructor(classes);
				con.setAccessible(true);
				return con.newInstance(args);
			} catch(Exception ex) {
			}
		}
		return null;
	}
	
	// Wrapper for instance0 method
	public T instance(String name, Object... args) {
		return instance0(name, args);
	}
}