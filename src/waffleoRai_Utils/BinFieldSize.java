package waffleoRai_Utils;

public enum BinFieldSize {
	
	BYTE(1),
	WORD(2),
	DWORD(4),
	QWORD(8);
	
	private int nbytes;
	
	private BinFieldSize(int bytes)
	{
		nbytes = bytes;
	}
	
	public int getByteCount()
	{
		return nbytes;
	}

}
