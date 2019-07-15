package waffleoRai_Utils;

public class SerializedString {
	
	private int sizeOnDisk;
	private String string;
	private String charset;
	
	protected SerializedString(int size, String s)
	{
		sizeOnDisk = size;
		string = s;
	}
	
	protected SerializedString(int size, String s, String cs)
	{
		sizeOnDisk = size;
		string = s;
		charset = cs;
	}
	
	public int getSizeOnDisk()
	{
		return sizeOnDisk;
	}
	
	public String getString()
	{
		return string;
	}
	
	public String getCharset()
	{
		return charset;
	}

}
