package waffleoRai_Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MappedBlockBuffer extends ROSubFileBuffer{
	
	private FileBuffer parent; //Needed? Should be put in ROSubFileBuffer area?
	
	private int blockSize_shift;
	private long blockSize;
	private ConcurrentMap<Integer, Integer> blockMap;
	
	public MappedBlockBuffer(FileBuffer parentBuffer, int block_size_shift_factor)
	{
		super(parentBuffer, 0, parentBuffer.getFileSize());
		super.setReadOnly();
		parent = parentBuffer;
		blockSize_shift = block_size_shift_factor;
		blockSize = 1L << blockSize_shift;
		
		blockMap = new ConcurrentHashMap<Integer, Integer>();
	}
	
	public void addBlockMapping(int external, int internal)
	{
		blockMap.put(external, internal);
	}
	
	public long getInternalOffset(long offset)
	{
		if (offset < 0L) return -1L;
		//Get block index
		long block = offset >>> blockSize_shift;
		//System.err.println("MappedBlockBuffer.getInternalOffset || -DEBUG- Input Block: " + block);
		
		Integer internalBlock = blockMap.get((int)block);
		if(internalBlock == null) return -1L;
		//System.err.println("MappedBlockBuffer.getInternalOffset || -DEBUG- Output Block: " + internalBlock);
		
		long blockoff = offset - (block << blockSize_shift);
		long iblock = Integer.toUnsignedLong(internalBlock);
		
		long newoff = (iblock << blockSize_shift) + blockoff;
		
		//System.err.println("MappedBlockBuffer.getInternalOffset || -DEBUG- Input: 0x" + Long.toHexString(offset));
		//System.err.println("MappedBlockBuffer.getInternalOffset || -DEBUG- Output: 0x" + Long.toHexString(newoff));
		return newoff;
	}

	public int getNumberBlocks()
	{
		return (int)(getFileSize() >>> blockSize_shift);
	}
	
	/* --- GETTER OVERRIDE --- */
	
	public long getFileSize()
	{
		if(blockMap.isEmpty()) return parent.getFileSize();
		
		//Find the highest block value in the keyset
		List<Integer> keys = new ArrayList<Integer>(blockMap.size());
		keys.addAll(blockMap.keySet());
		Collections.sort(keys);
		int biggest = keys.get(keys.size() - 1);
		long sz = Integer.toUnsignedLong(biggest+1) << blockSize_shift;
		
		return sz;
	}
	
	public byte getByte(int position)
	{
		return getByte(Integer.toUnsignedLong(position));
	}
	  
	public byte getByte(long position)
	{
		long mypos = getInternalOffset(position);
		if (mypos == -1) return 0;
		return parent.getByte(mypos);
	}
	  
	public byte[] getBytes()
	{
		long fsz = this.getFileSize();
		if (fsz > 0x7FFFFFFFL) throw new IndexOutOfBoundsException();
		return getBytes(0, fsz);
	}
	
	public byte[] getBytes(long stpos, long edpos)
	{
		//Do sector by sector...
		//System.err.println("MappedBlockBuffer.getBytes || -DEBUG- Called: 0x" + Long.toHexString(stpos) + " to 0x" + Long.toHexString(edpos) + " || " + Thread.currentThread().getName());
		long lsz = edpos - stpos;
		if (lsz > 0x7FFFFFFFL) throw new IndexOutOfBoundsException();
		
		//First, see if edpos and stpos are in the same sector...
		int ex_sec = (int)(stpos >>> blockSize_shift);
		if(ex_sec == (int)((edpos-1L) >>> blockSize_shift))
		{
			//Just getBytes() from the parent...
			long s_off = getInternalOffset(stpos);
			if(s_off == -1L) return new byte[(int)lsz]; //Empty sector
			long e_off = getInternalOffset(edpos);
			if(e_off == -1L)
			{
				e_off = getInternalOffset(edpos-1L) + 1L;
			}
			//System.err.println("MappedBlockBuffer.getBytes || -DEBUG- Calling parent: 0x" + Long.toHexString(s_off) + " to 0x" + Long.toHexString(e_off) + " || " + Thread.currentThread().getName());
			return parent.getBytes(s_off, e_off);
		}
		
		//If not, just fill sector by sector
		int sz = (int)lsz;
		long secpos = stpos - (Integer.toUnsignedLong(ex_sec) << blockSize_shift);
		
		byte[] out = new byte[sz];
		int sector = ex_sec;
		int written = 0;
		
		while(written < sz)
		{
			//System.exit(2);
			Integer in_sec = blockMap.get(sector);
			if(in_sec == null)
			{
				for(int j = 0; j < blockSize; j++)
				{
					if(written >= sz) break;
					out[written] = 0;
					written++;
				}
			}
			else
			{
				long s_start = ((long)in_sec << blockSize_shift) + secpos;
				long s_end = ((long)(in_sec+1) << blockSize_shift);
				secpos = 0;
				byte[] pbytes = parent.getBytes(s_start, s_end);
				for(int j = 0; j < pbytes.length; j++)
				{
					if(written >= sz) break;
					out[written] = pbytes[j];
					written++;
				}
			}
			ex_sec++;
		}
		
		return out;
	}

	/* --- STATUS OVERRIDE --- */
	
  	public boolean offsetValid(int off)
  	{
  		return offsetValid((long)off);
  	}
  
  	public boolean offsetValid(long off)
  	{
  		if (off < 0) return false;
  		if (off >= this.getFileSize()) return false;
  		return true;
  	}
  
	/* --- OTHER OVERRIDE --- */
  	
  	public ByteBuffer toByteBuffer()
  	{
  		long fSize = this.getFileSize();
  		if (fSize > 0x7FFFFFFFL)throw new IndexOutOfBoundsException();
  		
  		ByteBuffer bb = ByteBuffer.allocate((int)fSize);
  		
  		//Do by block
  		int bcount = this.getNumberBlocks();
  		for(int b = 0; b < bcount; b++)
  		{
  			Integer iblock = this.blockMap.get(b);
  			if(iblock == null)
  			{
  				//Zero fill
  				bb.put(new byte[(int)blockSize]);
  			}
  			else
  			{
  				//Grab from file
  				long istart = Integer.toUnsignedLong(iblock) << blockSize_shift;
  				bb.put(parent.toByteBuffer(istart, istart+blockSize));
  			}
  		}
  		
  		return bb;
  	}
	  
  	public ByteBuffer toByteBuffer(long stPos, long edPos)
  	{
  		if (stPos < 0) throw new IndexOutOfBoundsException();
  		if (stPos >= edPos) throw new IndexOutOfBoundsException();	
  		long fSize = this.getFileSize();
  		if (edPos > fSize) throw new IndexOutOfBoundsException();
  		
  		if (stPos == 0 && edPos == fSize) return this.toByteBuffer();
  		long sz = edPos - stPos;
  		if (sz > 0x7FFFFFFFL)throw new IndexOutOfBoundsException();
  		
  		ByteBuffer bb = ByteBuffer.allocate((int)sz);
  		
  		//Get start and end block info
  		long stioff = this.getInternalOffset(stPos);
  		int stBlock = (int)(stPos >>> blockSize_shift);
  		
  		long edioff = this.getInternalOffset(edPos);
  		int edBlock = (int)(edPos >>> blockSize_shift);
  		
  		//First block
  		long blockoff = stPos - (stBlock << blockSize_shift);
  		long blockleft = blockSize - blockoff;
  		if(stioff == -1)
  		{
  			//0 fill until end of block
  			bb.put(new byte[(int)blockleft]);
  		}
  		else
  		{
  			//Just copy block
  			long blockend = blockleft + stioff;
  			bb.put(parent.toByteBuffer(stioff, blockend));
  		}
  		
  		//Middle blocks
  		for(int b = stBlock+1; b < edBlock; b++)
  		{
  			//See if blocks are real
  			Integer iblock = this.blockMap.get(b);
  			if(iblock == null)
  			{
  				//Zero fill
  				bb.put(new byte[(int)blockSize]);
  			}
  			else
  			{
  				//Grab from file
  				long istart = Integer.toUnsignedLong(iblock) << blockSize_shift;
  				bb.put(parent.toByteBuffer(istart, istart+blockSize));
  			}
  		}
  		
  		//Last block
  		blockoff = edPos - (edBlock << blockSize_shift);
  		if(edioff == -1){bb.put(new byte[(int)blockoff]);}
  		else
  		{
  			bb.put(parent.toByteBuffer(edioff-blockoff, edioff));
  		}
  		
  		return bb;
  	}
	  
  	public String typeString()
  	{
  		return "Mapped Block FileBuffer";
  	}

  	public FileBuffer createCopy(long stPos, long edPos) throws IOException
  	{
  		MappedBlockBuffer copy = new MappedBlockBuffer(parent, blockSize_shift);
  		for(Integer k : blockMap.keySet())
  		{
  			copy.blockMap.put(k, blockMap.get(k));
  		}
  		return copy;
  	}
  	
}
