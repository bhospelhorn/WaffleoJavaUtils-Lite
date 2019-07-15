package waffleoRai_soundbank;

public class Release {
	
	private int iTime;
	private ADSRMode eMode;
	
	protected Release()
	{
		iTime = 0;
		eMode = ADSRMode.LINEAR_DB;
	}
	
	public Release(int millis, ADSRMode mode)
	{
		iTime = millis;
		eMode = mode;
	}
	
	public int getTime()
	{
		return iTime;
	}
	
	public ADSRMode getMode()
	{
		return eMode;
	}
	
	public void setTime(int millis)
	{
		iTime = millis;
	}
	
	public void setMode(ADSRMode mode)
	{
		eMode = mode;
	}
	
	public static Release getDefault()
	{
		return new Release(0, ADSRMode.LINEAR_DB);
	}

}
