package sune.etc.faso.util;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayItems<T> implements Iterable<T> {
	
	protected final T[] array;
	
	public ArrayItems(T[] array) {
		this.array = array;
	}
	
	public T get(int index) {
		if(index < 0 || index >= array.length) {
			throw new ArrayIndexOutOfBoundsException(
				"Index is out of the items array!");
		}
		return array[index];
	}
	
	public T first() {
		// Without the bound checking and exception
		return array.length > 0 ? array[0] : null;
	}
	
	public T last() {
		// Without the bound checking and exception
		return array.length > 0 ? array[array.length-1] : null;
	}
	
	public T[] all() {
		return Arrays.copyOf(array, array.length);
	}
	
	public int length() {
		return array.length;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator<>(array);
	}
	
	private static final class ArrayIterator<T> implements Iterator<T> {
		
		private final T[] array;
		private int index;
		
		public ArrayIterator(T[] array) {
			this.array = array;
			this.index = 0;
		}
		@Override
		public boolean hasNext() {
			return index < array.length;
		}
		
		@Override
		public T next() {
			return array[index++];
		}
	}
	
	@Override
	public String toString() {
		return Arrays.toString(array);
	}
}