package waffleoRai_soundbank.modulator;

import waffleoRai_soundbank.ModSource;
import waffleoRai_soundbank.ModType;

public class DefaultModSource extends ModSource {
	
	public DefaultModSource()
	{
		super.setController(new EmptyModController());
		super.setContinuous(false);
		super.setDirection(true);
		super.setPolarity(false);
		super.setType(ModType.LINEAR);
	}

}
