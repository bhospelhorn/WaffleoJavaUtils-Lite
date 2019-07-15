package waffleoRai_soundbank;

public class Sustain {
	
	//private int iMax;
	
	private int iLevel;
	
	private int iTime;
	private boolean bDirection;
	private ADSRMode eMode;
	
	public Sustain(int level)
	{
		iLevel = level;
		bDirection = false;
		iTime = 0;
		eMode = ADSRMode.STATIC;
		//iMax = maxLevel;
	}
	
	public Sustain(int level, boolean up, int millis, ADSRMode mode)
	{
		bDirection = up;
		iLevel = level;
		iTime = millis;
		eMode = mode;
		//iMax = maxLevel;
	}
	
	public int getTime()
	{
		return iTime;
	}
	
	public ADSRMode getMode()
	{
		return eMode;
	}
	
	public int getLevel()
	{
		return iLevel;
	}
	
	public boolean rampUp()
	{
		return bDirection;
	}
	
	public void setTime(int millis)
	{
		iTime = millis;
	}
	
	public void setMode(ADSRMode mode)
	{
		eMode = mode;
	}

	public void setLevel(int level)
	{
		iLevel = level;
	}
	
	public void setDirection(boolean up)
	{
		bDirection = up;
	}
	
	public static Sustain getDefault()
	{
		return new Sustain(Integer.MAX_VALUE);
	}
	
}
