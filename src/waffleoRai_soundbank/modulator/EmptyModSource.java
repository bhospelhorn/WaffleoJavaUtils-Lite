package waffleoRai_soundbank.modulator;

import waffleoRai_soundbank.ModSource;
import waffleoRai_soundbank.ModType;

public class EmptyModSource extends ModSource {
	
	public EmptyModSource()
	{
		super.setController(new EmptyModController());
		super.setContinuous(false);
		super.setDirection(true);
		super.setPolarity(false);
		super.setType(ModType.LINEAR);
	}

}
