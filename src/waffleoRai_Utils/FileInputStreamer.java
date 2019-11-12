package waffleoRai_Utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileInputStreamer implements StreamWrapper{
	
	private InputStream stream;
	private boolean eof;
	
	//private LinkedList<Byte> pushStack;
	
	public FileInputStreamer(String path) throws FileNotFoundException
	{
		stream = new BufferedInputStream(new FileInputStream(path));
		eof = false;
		//pushStack = new LinkedList<Byte>();
	}
	
	public FileInputStreamer(InputStream str)
	{
		stream = str;
		eof = false;
		//pushStack = new LinkedList<Byte>();
	}
	
	public InputStream getStream(){return stream;}
	
	public byte get()
	{
		//if(!pushStack.isEmpty()) return pushStack.pop();
		
		try
		{
			int b = stream.read();
			if(b == -1) 
			{
				eof = true;
				return 0;
			}
			return (byte)b;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			eof = true;
			return 0;
		}
	}
	
	public int getFull()
	{
		try
		{
			int b = stream.read();
			if(b == -1) 
			{
				eof = true;
				return 0;
			}
			return b;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			eof = true;
			return 0;
		}
	}

	public void put(byte b){throw new UnsupportedOperationException();}
	public boolean isEmpty(){return eof;}
	
	public void close()
	{
		try{stream.close();}
		catch(IOException e){e.printStackTrace();}
	}

	public void push(byte b)
	{
		throw new UnsupportedOperationException();
	}
	
}
