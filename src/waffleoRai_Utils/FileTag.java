package waffleoRai_Utils;

public class FileTag {
	
	private String filepath;
	private long offset;
	private long size;
	
	public FileTag(String path, long off, long sz)
	{
		filepath = path;
		offset = off;
		size = sz;
	}
	
	public String getPath(){return filepath;}
	public long getOffset(){return offset;}
	public long getSize(){return size;}
	
	public void setPath(String path){filepath = path;}
	public void setOffset(long off){offset = off;}
	public void setSize(long sz){size = sz;}

}
