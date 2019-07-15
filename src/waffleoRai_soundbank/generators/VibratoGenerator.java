package waffleoRai_soundbank.generators;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;

public class VibratoGenerator implements Generator {

	private int iFreq;
	private int iSemis;
	
	public VibratoGenerator(int frequency, int semitones)
	{
		iFreq = frequency;
	}
	
	@Override
	public GeneratorType getType() {
		return GeneratorType.VIBRATO;
	}

	@Override
	public int getAmount() {
		return iFreq;
	}
	
	public int getSemitones()
	{
		return iSemis;
	}
	
	

}
