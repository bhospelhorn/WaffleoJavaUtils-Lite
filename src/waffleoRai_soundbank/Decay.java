package waffleoRai_soundbank;

public class Decay {
	
	private int iTime;
	private ADSRMode eMode;
	
	protected Decay()
	{
		iTime = 0;
		eMode = ADSRMode.LINEAR_DB;
	}
	
	public Decay(int millis, ADSRMode mode)
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
	
	public static Decay getDefault()
	{
		return new Decay(0, ADSRMode.LINEAR_DB);
	}

}
