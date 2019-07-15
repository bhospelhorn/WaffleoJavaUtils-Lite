package waffleoRai_soundbank.sf2.ADSR;

import waffleoRai_soundbank.Attack;
import waffleoRai_soundbank.Decay;
import waffleoRai_soundbank.Region;
import waffleoRai_soundbank.Release;
import waffleoRai_soundbank.Sustain;

public interface SF2ADSRConverter {
	
	public void calibrate(Region r);
	
	public int getAttack(Attack a);
	public int getDecay(Decay d);
	public int getRelease(Release r);
	public int getSustain(Sustain s);
	
	public int getDelay(int ms_d);
	public int getHold(int ms_h);

}
