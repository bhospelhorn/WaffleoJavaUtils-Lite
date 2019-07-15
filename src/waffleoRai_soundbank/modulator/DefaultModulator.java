package waffleoRai_soundbank.modulator;

import waffleoRai_soundbank.GeneratorType;
import waffleoRai_soundbank.ModSource;
import waffleoRai_soundbank.Modulator;
import waffleoRai_soundbank.TransformType;

public class DefaultModulator implements Modulator{

	private ModSource source;
	private ModSource source_amt;
	private int amount;
	private GeneratorType dest;
	private TransformType transform;
	
	public DefaultModulator()
	{
		source = new DefaultModSource();
		source_amt = new DefaultModSource();
		amount = 0;
		dest = null;
		transform = TransformType.LINEAR;
	}
	
	@Override
	public ModSource getSource() 
	{
		return source;
	}

	@Override
	public ModSource getSourceAmount() 
	{
		return source_amt;
	}

	@Override
	public int getAmount() 
	{
		return amount;
	}

	@Override
	public GeneratorType getDestination() 
	{
		return dest;
	}

	@Override
	public TransformType getTransform() 
	{
		return transform;
	}
	
	public void setAmount(int a)
	{
		amount = a;
	}
	
	public void setDestination(GeneratorType d)
	{
		dest = d;
	}
	
	public void setTransform(TransformType t)
	{
		transform = t;
	}
	
	public void setSource(ModSource s)
	{
		source = s;
	}
	
	public void setSourceAmount(ModSource s)
	{
		source_amt = s;
	}

}
