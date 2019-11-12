package waffleoRai_Utils;

import java.util.LinkedList;

public class LinkedBytesStreamer implements StreamWrapper{
	
	private LinkedList<Byte> list;
	
	public LinkedBytesStreamer()
	{
		list = new LinkedList<Byte>();
	}
	
	public byte get()
	{
		if(list.isEmpty()) return -1;
		return list.pop();
	}
	
	public int getFull()
	{
		if(list.isEmpty()) return -1;
		return Byte.toUnsignedInt(list.pop());
	}
	
	public void push(byte b)
	{
		list.push(b);
	}
	
	public void put(byte b)
	{
		list.add(b);
	}
	
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
	
	public void close()
	{
		list.clear();
	}
	
	public int size()
	{
		return list.size();
	}

}
