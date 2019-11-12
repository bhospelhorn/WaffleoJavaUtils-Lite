package waffleoRai_Utils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileOutputStreamer implements StreamWrapper{
	
	private OutputStream stream;
	private int written;
	
	public FileOutputStreamer(String path) throws IOException
	{
		stream = new BufferedOutputStream(new FileOutputStream(path));
		written = 0;
	}
	
	public OutputStream getStream(){return stream;}
	
	public byte get()
	{
		throw new UnsupportedOperationException();
	}
	
	public int getFull()
	{
		throw new UnsupportedOperationException();
	}

	public void put(byte b)
	{
		try
		{
			stream.write(Byte.toUnsignedInt(b));
			written++;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isEmpty(){return (written == 0);}
	
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
