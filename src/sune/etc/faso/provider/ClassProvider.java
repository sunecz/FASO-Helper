package sune.etc.faso.provider;

import java.lang.reflect.Constructor;

import sune.etc.faso.util.Utils;

public abstract class ClassProvider<E, T> implements Provider<E, Class<? extends T>> {
	
	@SuppressWarnings("unchecked")
	T instance0(E name, Object... args) {
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
	
	public T instance(E name, Object... args) {
		// Call the internal method
		return instance0(name, args);
	}
}