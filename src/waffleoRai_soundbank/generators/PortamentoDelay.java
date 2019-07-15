package waffleoRai_soundbank.generators;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;

public class PortamentoDelay implements Generator{
	
	private int iTime;
	
	public PortamentoDelay(int millis)
	{
		iTime = millis;
	}
	
	@Override
	public GeneratorType getType() 
	{
		return GeneratorType.PORTAMENTO_DELAY;
	}

	@Override
	public int getAmount() 
	{
		return iTime;
	}

}
