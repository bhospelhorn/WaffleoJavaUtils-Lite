package waffleoRai_soundbank.sf2.generator;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;
import waffleoRai_soundbank.generators.GenericSignedValueGenerator;
import waffleoRai_soundbank.sf2.SF2Gen;

public class GenericSignedFetcher implements GeneratorFetcher{

	@Override
	public Generator createGenerator(SF2Gen gen) 
	{
		if (gen == null) return null;
		GeneratorType t = SF2GeneratorConverter.convertType(gen.getType());
		int amt = (int)gen.getRawAmount();
		
		return new GenericSignedValueGenerator(t, amt);
	}

}
