package waffleoRai_soundbank.sf2.generator;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;
import waffleoRai_soundbank.generators.GenericUnsignedValueGenerator;
import waffleoRai_soundbank.sf2.SF2Gen;

public class GenericUnsignedFetcher implements GeneratorFetcher{

	@Override
	public Generator createGenerator(SF2Gen gen) 
	{
		if (gen == null) return null;
		GeneratorType t = SF2GeneratorConverter.convertType(gen.getType());
		int amt = Short.toUnsignedInt(gen.getRawAmount());
		
		return new GenericUnsignedValueGenerator(t, amt);
	}

}
