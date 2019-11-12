package waffleoRai_Utils;

import java.nio.ByteBuffer;

public class ByteBufferStreamer implements StreamWrapper{
	
	private ByteBuffer buffer;
	
	//private LinkedList<Byte> pushStack;
	
	private ByteBufferStreamer(){};
	
	public ByteBufferStreamer(int alloc)
	{
		buffer = ByteBuffer.allocate(alloc);
		//pushStack = new LinkedList<Byte>();
	}
	
	public static ByteBufferStreamer wrap(ByteBuffer byteBuffer)
	{
		ByteBufferStreamer stream = new ByteBufferStreamer();
		stream.buffer = byteBuffer;
		//stream.pushStack = new LinkedList<Byte>();
		return stream;
	}
	
	public static ByteBufferStreamer wrap(FileBuffer fileBuffer)
	{
		ByteBufferStreamer stream = new ByteBufferStreamer();
		stream.buffer = fileBuffer.toByteBuffer();
		//stream.pushStack = new LinkedList<Byte>();
		return stream;
	}
	
	public ByteBuffer getBuffer(){return buffer;}
	
	public int getFull(){return Byte.toUnsignedInt(get());}
	
	public byte get()
	{
		//if(!pushStack.isEmpty()) return pushStack.pop();
		return buffer.get();
	}
	
	public void put(byte b){buffer.put(b);}
	public boolean isEmpty(){return buffer.position() >= buffer.limit();}
	public void close(){buffer.clear();}
	
	public void push(byte b)
	{
		//pushStack.push(b);
		throw new UnsupportedOperationException();
	}

}
