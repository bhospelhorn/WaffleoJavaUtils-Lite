package waffleoRai_Sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

public class WAVInputStream extends InputStream{
	
	/* ----- Instance Variables ----- */
	
	private WAV iSource;
	
	private int iChannels;
	private int iBytesPerSample;
	private int iBytesPerFrame;
	
	private int iCurrentByte;
	private int iCurrentFrame;
	private int iLoopFrame;
	private boolean bEndReached;
	
	private int iBufferSize;
	private int iBufferFrame;
	private boolean bBufferReachedEnd;
	
	private ByteQueue iBuffer;
	private BufferThread iBufferThread;
	
	private int iNumberFrames;
	private boolean bLoops;
	
	private boolean bIsOpen;
	
	private boolean bMarkSet;
	private int iMarkedByte;
	private int iMarkedFrame;
	private ByteQueue iSavedBuffer;
	private int iMarkedBufferFrame;
	private boolean bMarkedBufferReachedEnd;
	private int iMarkedBytesLeft;
	
	/* ----- Construction ----- */
	
	public WAVInputStream(WAV source, int bufferedFrames)
	{
		if (source == null) throw new IllegalArgumentException();
		iSource = source;
		
		iChannels = iSource.numberChannels();
		iBytesPerSample = iSource.getRawBitDepth()/8;
		iBytesPerFrame = iBytesPerSample * iChannels;
		iLoopFrame = iSource.getLoopFrame();
		if (iLoopFrame >= 0) bLoops = true;
		else bLoops = false;
		iNumberFrames = iSource.totalFrames();
		
		iCurrentByte = 0;
		iCurrentFrame = 0;
		bEndReached = false;
		
		if (bufferedFrames < 64) bufferedFrames = 64;
		iBufferSize = bufferedFrames * iBytesPerFrame;
		iBufferFrame = 0;
		bBufferReachedEnd = false;
		
		iBuffer = new ByteQueue();
		iBufferThread = new BufferThread();
		
		bMarkSet = false;
		iMarkedByte = -1;
		iMarkedFrame = -1;
		iSavedBuffer = null;
		iMarkedBytesLeft = 0;
		
		//Start
		iBufferThread.start();
		bIsOpen = true;
	}
	
	/* ----- Buffer ----- */
	
	private static class ByteQueue
	{
		private static final int rowsize = 64;
		
		private byte[] readrow;
		private byte[] writerow;
		
		private Deque<byte[]> bytes;
		
		private int readpos;
		private int writepos;
		
		public ByteQueue()
		{
			bytes = new LinkedList<byte[]>();
			readrow = null;
			writerow = new byte[rowsize];
			readpos = 0;
			writepos = 0;
		}
		
		public synchronized void writeByte(byte b)
		{
			if (writepos < rowsize)
			{
				writerow[writepos] = b;
				writepos++;
			}
			else
			{
				addRow();
				writepos = 0;
				writerow[writepos] = b;
				writepos++;
			}
		}
		
		public synchronized byte popByte() throws EmptyBufferException
		{
			if (readrow == null) popRow();
			if (readrow == null) throw new EmptyBufferException();
			if (readpos < rowsize)
			{
				byte b = readrow[readpos];
				readpos++;
				return b;
			}
			else
			{
				readpos = 0;
				popRow();
				byte b = readrow[readpos];
				readpos++;
				return b;
			}
		}
		
		private void popRow() throws EmptyBufferException
		{
			try
			{
				readrow = bytes.pop();
			}
			catch (Exception e)
			{
				throw new EmptyBufferException();
			}
		}
		
		private void addRow()
		{
			bytes.addLast(writerow);
			writerow = new byte[rowsize];
		}
		
		public synchronized int size()
		{
			int nrows = bytes.size();
			int sz = nrows * rowsize;
			sz += readpos;
			sz += rowsize - writepos;
			return sz;
		}
		
		public synchronized ByteQueue copy()
		{
			return null;
		}
		
	}

	private class BufferThread extends Thread
	{
		private boolean bKill;
		
		private boolean bPaused;
		private boolean bPauseRequest;
		private boolean bUnpauseRequest;
		
		public BufferThread()
		{
			Random r = new Random();
			super.setName("WAVInputStream_BufferDaemon_" + Long.toHexString(r.nextLong()));
			super.setDaemon(true);
			bKill = false;
			bPaused = false;
			bPauseRequest = false;
			bUnpauseRequest = false;
		}
		
		public void run()
		{
			while (!killSet())
			{
				//Process pause requests
				if (bPauseRequest) pause();
				if (bUnpauseRequest) unpause();
				if(!isPaused())
				{
					//Fill buffer to capacity or until end reached
					while(iBuffer.size() < iBufferSize && !bBufferReachedEnd)
					{
						if (iBufferFrame >= iNumberFrames)
						{
							if (bLoops) iBufferFrame = iLoopFrame;
							else {
								bBufferReachedEnd = true;
								break;
							}
						}
						byte[] fullframe = getSerializedFrame(iBufferFrame);
						for (int i = 0; i < fullframe.length; i++)
						{
							iBuffer.writeByte(fullframe[i]);
						}
						iBufferFrame++;
					}
				}
				//Sleep
				try 
				{
					Thread.sleep(100);
				} 
				catch (InterruptedException e) {
					Thread.interrupted();
					//e.printStackTrace();
				}
			}
		}
		
		public synchronized boolean killSet()
		{
			return bKill;
		}
		
		public synchronized boolean isPaused()
		{
			return bPaused;
		}
		
		private synchronized void pause()
		{
			bPauseRequest = false;
			bPaused = true;
		}
		
		private synchronized void unpause()
		{
			bUnpauseRequest = false;
			bPaused = false;
		}
		
		public synchronized void requestPause()
		{
			bPauseRequest = true;
			this.interrupt();
		}
		
		public synchronized void requestUnpause()
		{
			bUnpauseRequest = true;
			this.interrupt();
		}
		
		public synchronized void kill()
		{
			bKill = true;
			this.interrupt();
		}
		
		public synchronized void interruptMe()
		{
			this.interrupt();
		}

	}
	
	public byte[] getSerializedFrame(int frameindex)
	{
		int[] samples = new int[iChannels];
		for (int c = 0; c < iChannels; c++) samples[c] = iSource.getSample(c, frameindex);
		byte[] bytes = new byte[samples.length * iBytesPerSample];
		int i = 0;
		for (int c = 0; c < iChannels; c++)
		{
			int s = samples[c];
			int b0 = s & 0xFF;
			int b1 = (s >>> 8);
			int b2 = (s >>> 16);
			int b3 = (s >>> 24);
			bytes[i] = (byte)b0; i++;
			if (iBytesPerSample >= 2) {
				bytes[i] = (byte)b1; i++; 
			}
			if (iBytesPerSample >= 3) {
				bytes[i] = (byte)b2; i++; 
			}
			if (iBytesPerSample == 4) {
				bytes[i] = (byte)b3; i++; 
			}
		}
		return bytes;
	}
	
	public static class EmptyBufferException extends Exception
	{
		private static final long serialVersionUID = -7406741361835372063L;
	}
	
	/* ----- Mark ----- */
	
	public void disposeMark()
	{
		bMarkSet = false;
		iSavedBuffer = null;
		iMarkedByte = -1;
		iMarkedFrame = -1;
		iMarkedBytesLeft = 0;
		iMarkedBufferFrame = -1;
		bMarkedBufferReachedEnd = false;
	}
	
	/* ----- Advancing Marker ----- */
	
	public void advanceMarker()
	{
		iCurrentByte++;
		if (iCurrentByte >= iBytesPerFrame)
		{
			iCurrentByte = 0;
			iCurrentFrame++;
		}
		
		if (bMarkSet)
		{
			iMarkedBytesLeft--;
			if (iMarkedBytesLeft <= 0)
			{
				disposeMark();
			}
		}
	}
	
	public void advanceMarker(int bytes)
	{
		int mod = bytes % iBytesPerFrame;
		int frames = bytes/iBytesPerFrame;
		iCurrentFrame += frames;
		
		iCurrentByte += mod;
		if (iCurrentByte >= iBytesPerFrame)
		{
			iCurrentByte = iCurrentByte - iBytesPerFrame;
			iCurrentFrame++;
		}
		
		if (bMarkSet)
		{
			iMarkedBytesLeft -= bytes;
			if (iMarkedBytesLeft <= 0)
			{
				disposeMark();
			}
		}
	}
	
	/* ----- InputStream Methods ----- */
	
	public int available()
	{
		if (!bIsOpen) return 0;
		if (bEndReached) return 0;
		return iBuffer.size();
	}
	
	public void close()
	{
		if (!bIsOpen) return;
		iBufferThread.kill();
		bIsOpen = false;
	}
	
	public void mark(int readlimit)
	{
		if (!bIsOpen) return;
		iBufferThread.requestPause();
		bMarkSet = true;
		iMarkedByte = iCurrentByte;
		iMarkedFrame = iCurrentFrame;
		iMarkedBytesLeft = readlimit;
		while (!iBufferThread.isPaused())
		{
			//Wait
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		iSavedBuffer = iBuffer.copy();
		iMarkedBufferFrame = iBufferFrame;
		bMarkedBufferReachedEnd = bBufferReachedEnd;
		
		iBufferThread.requestUnpause();
	}
	
	public boolean markSupported()
	{
		return (bIsOpen && !bEndReached);
	}
	
	private byte getNextByte()
	{
		boolean got = false;
		byte b = -1;
		while (!got)
		{
			try
			{
				b = iBuffer.popByte();
				got = true;
			}
			catch (EmptyBufferException e)
			{
				//Wait and try again
				iBufferThread.interruptMe();
				try 
				{
					Thread.sleep(10);
				} 
				catch (InterruptedException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		return b;
	}
	
	@Override
	public int read() throws IOException 
	{
		if (!bIsOpen) return -1;
		if (bEndReached) return -1;
		if (iCurrentFrame >= iNumberFrames)
		{
			if (bLoops) {
				iCurrentFrame = iLoopFrame;
				iCurrentByte = 0;
			}
			else
			{
				bEndReached = true;
				return -1;
			}
		}
		byte b = getNextByte();
		advanceMarker();
		iBufferThread.interruptMe();
		return Byte.toUnsignedInt(b);
	}
	
	public int read(byte[] b)
	{
		if (!bIsOpen) return 0;
		if (b == null) return 0;
		if (b.length == 0) return 0;
		int len = b.length;
		int remaining = len;
		int read = 0;
		
		int i = 0;
		while (remaining > 0 && !bEndReached)
		{
			if (iCurrentFrame >= iNumberFrames)
			{
				if (bLoops) {
					iCurrentFrame = iLoopFrame;
					iCurrentByte = 0;
				}
				else
				{
					bEndReached = true;
					continue;
				}
			}
			
			b[i] = getNextByte();
			advanceMarker();
			remaining--;
			read++;
			i++;
		}
		
		iBufferThread.interruptMe();
		return read;
	}
	
	public int read(byte[] b, int off, int len)
	{
		if (!bIsOpen) return 0;
		if (b == null) return 0;
		if (b.length == 0) return 0;

		int remaining = len;
		int read = 0;
		
		int bi = off;
		while (remaining > 0 && !bEndReached)
		{
			if (iCurrentFrame >= iNumberFrames)
			{
				if (bLoops) {
					iCurrentFrame = iLoopFrame;
					iCurrentByte = 0;
				}
				else
				{
					bEndReached = true;
					continue;
				}
			}
			
			b[bi] = getNextByte();
			advanceMarker();
			remaining--;
			read++;
			bi++;
		}
		
		iBufferThread.interruptMe();
		return read;
	}
	
	public void reset()
	{
		if (!bIsOpen) return;
		if (!bMarkSet) return;
		iBufferThread.requestPause();
		
		iCurrentByte = iMarkedByte;
		iCurrentFrame = iMarkedFrame;
		
		while (!iBufferThread.isPaused())
		{
			//Wait
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		iBuffer = iSavedBuffer;
		iBufferFrame = iMarkedBufferFrame;
		bBufferReachedEnd = bMarkedBufferReachedEnd;
		
		iBufferThread.requestUnpause();
	}
	
	public long skip(long n)
	{
		if (!bIsOpen) return 0;
		if (n <= 0) return 0;
		long skipped = 0;
		
		try 
		{
			int i = read();
			while ((i != -1) && (skipped < n))
			{
				i = read();
				skipped++;
			}
		} 
		catch (IOException e) 
		{
			return skipped;
		}
		
		return skipped;
	}

	
}
