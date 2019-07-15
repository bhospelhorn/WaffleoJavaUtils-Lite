package waffleoRai_Sound;

public enum BitDepth {
	
	EIGHT_BIT_UNSIGNED(8, false, false),
	SIXTEEN_BIT_SIGNED(16, true, false),
	TWENTYFOUR_BIT_SIGNED(24, true, false),
	THIRTYTWO_BIT_SIGNED(32, true, false),
	SIXTEEN_BIT_UNSIGNED(16, false, false);
	
	private int bits;
	private boolean signed;
	private boolean floated;
	
	private BitDepth(int b, boolean s, boolean f)
	{
		bits = b;
		signed = s;
		floated = f;
	}
	
	public int getBitCount()
	{
		return bits;
	}
	
	public boolean isSigned()
	{
		return signed;
	}
	
	public boolean isFloat()
	{
		return floated;
	}

}
