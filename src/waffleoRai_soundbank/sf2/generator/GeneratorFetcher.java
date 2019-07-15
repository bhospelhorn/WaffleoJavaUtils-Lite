package waffleoRai_soundbank.sf2.generator;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.sf2.SF2Gen;

public interface GeneratorFetcher {
	
	public Generator createGenerator(SF2Gen gen);
	

}
