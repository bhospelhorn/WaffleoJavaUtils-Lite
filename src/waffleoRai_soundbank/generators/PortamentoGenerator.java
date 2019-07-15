package waffleoRai_soundbank.generators;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;

public class PortamentoGenerator implements Generator{

	private int iTime;
	
	public PortamentoGenerator(int millis)
	{
		iTime = millis;
	}
	
	@Override
	public GeneratorType getType() 
	{
		return GeneratorType.PORTAMENTO;
	}

	@Override
	public int getAmount() 
	{
		return iTime;
	}

}
