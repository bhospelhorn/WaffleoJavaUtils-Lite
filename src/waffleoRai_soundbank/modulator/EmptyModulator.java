package waffleoRai_soundbank.modulator;

import waffleoRai_soundbank.GeneratorType;
import waffleoRai_soundbank.ModSource;
import waffleoRai_soundbank.Modulator;
import waffleoRai_soundbank.TransformType;

public class EmptyModulator implements Modulator{

	private ModSource emptySource;
	
	public EmptyModulator()
	{
		emptySource = new EmptyModSource();
	}
	
	@Override
	public ModSource getSource() 
	{
		return emptySource;
	}

	@Override
	public ModSource getSourceAmount() 
	{
		return emptySource;
	}

	@Override
	public int getAmount() 
	{
		return 0;
	}

	@Override
	public GeneratorType getDestination() 
	{
		return null;
	}

	@Override
	public TransformType getTransform() 
	{
		return TransformType.LINEAR;
	}

}
