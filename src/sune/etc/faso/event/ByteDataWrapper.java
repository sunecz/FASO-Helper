package sune.etc.faso.event;

public class ByteDataWrapper {
	
	public final byte[] data;
	public final int	length;
	
	public ByteDataWrapper(byte[] data, int length) {
		this.data 	= data;
		this.length = length;
	}
}