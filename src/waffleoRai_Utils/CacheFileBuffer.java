package waffleoRai_Utils;

/*
 * UPDATES
 * 
 * 1.0.0 | August 5, 2019
 */

/**
 * A better version of StreamBuffer (which was a cache, not a stream)
 * that facilitates random access in large files without loading the
 * full file into memory.
 * <br>The size of each page and the number of pages can be modified.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since August 5, 2019
 */
public class CacheFileBuffer extends FileBuffer{
	
	/* ----- Constants ----- */
	
	public static final int DEFO_PAGE_SIZE = 0x1000; //4096
	public static final int DEFO_PAGE_NUM = 0x10000; //65536
	
	/* ----- Instance Variables ----- */
	
	private int page_size;
	private int page_count;
	
	/* ----- Construction ----- */
	
	private CacheFileBuffer()
	{
		//This is just an override to prevent use of defo constructor
		this(DEFO_PAGE_SIZE, DEFO_PAGE_NUM, false);
	}
	
	private CacheFileBuffer(int pageSize, int pageCount, boolean allowWrite)
	{
		//TODO
	}

	/* ----- Static Object Generators ----- */
	
	public static CacheFileBuffer getReadOnlyCacheBuffer(String filepath)
	{
		//TODO
		return null;
	}
	
	public static CacheFileBuffer getReadOnlyCacheBuffer(String filepath, int pageSize)
	{
		//TODO
		return null;
	}
	
	public static CacheFileBuffer getReadOnlyCacheBuffer(String filepath, int pageSize, int pageCount)
	{
		//TODO
		return null;
	}
	
	public static CacheFileBuffer getWritableCacheBuffer()
	{
		//TODO
		return null;
	}
	
	public static CacheFileBuffer getWritableCacheBuffer(int pageSize)
	{
		//TODO
		return null;
	}
	
	public static CacheFileBuffer getWritableCacheBuffer(int pageSize, int pageCount)
	{
		//TODO
		return null;
	}
	
}
