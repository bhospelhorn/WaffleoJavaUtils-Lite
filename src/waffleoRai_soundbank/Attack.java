package waffleoRai_soundbank;

public class Attack {
	
	private int iTime;
	private ADSRMode eMode;
	
	protected Attack()
	{
		iTime = 0;
		eMode = ADSRMode.LINEAR_ENVELOPE;
	}
	
	public Attack(int millis, ADSRMode mode)
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
	
	public static Attack getDefault()
	{
		return new Attack(0, ADSRMode.LINEAR_ENVELOPE);
	}
	
}
