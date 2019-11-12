package waffleoRai_Utils;

public interface StreamWrapper {
	
	public byte get();
	public int getFull();
	public void push(byte b);
	public void put(byte b);
	public boolean isEmpty();
	public void close();
	
}
