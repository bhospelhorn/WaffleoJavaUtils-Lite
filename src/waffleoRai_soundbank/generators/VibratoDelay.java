package waffleoRai_soundbank.generators;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;

public class VibratoDelay implements Generator{

	private int iTime;
	
	public VibratoDelay(int millis)
	{
		iTime = millis;
	}
	
	@Override
	public GeneratorType getType() 
	{
		return GeneratorType.VIBRATO_DELAY;
	}

	@Override
	public int getAmount() 
	{
		return iTime;
	}

}
