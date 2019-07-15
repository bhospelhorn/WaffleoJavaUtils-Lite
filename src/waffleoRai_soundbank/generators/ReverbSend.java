package waffleoRai_soundbank.generators;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;

public class ReverbSend implements Generator{
	
	private int iAmount;
	
	public ReverbSend(int hundredthOfPercent)
	{
		iAmount = hundredthOfPercent;
		if (iAmount < 0) iAmount = 0;
		if (iAmount > 10000) iAmount = 10000;
	}
	
	public GeneratorType getType()
	{
		return GeneratorType.REVERB;
	}
	
	public int getAmount()
	{
		return iAmount;
	}
	
	public void setAmount(int hundredthOfPercent)
	{
		iAmount = hundredthOfPercent;
		if (iAmount < 0) iAmount = 0;
		if (iAmount > 10000) iAmount = 10000;
	}

}
