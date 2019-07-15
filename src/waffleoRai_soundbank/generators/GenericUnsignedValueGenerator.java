package waffleoRai_soundbank.generators;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;

public class GenericUnsignedValueGenerator implements Generator{

	private GeneratorType type;
	private int amount;
	
	public GenericUnsignedValueGenerator()
	{
		type = null;
		amount = 0;
	}
	
	public GenericUnsignedValueGenerator(GeneratorType t, int amt)
	{
		type = t;
		amount = amt;
	}
	
	@Override
	public GeneratorType getType() 
	{
		return type;
	}

	@Override
	public int getAmount() 
	{
		return amount;
	}

	public void setType(GeneratorType t)
	{
		type = t;
	}
	
	public void setAmount(int amt)
	{
		if (amt < 0) amt = 0;
		amount = amt;
	}
	
}
