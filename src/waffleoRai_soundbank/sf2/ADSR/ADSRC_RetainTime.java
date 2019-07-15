package waffleoRai_soundbank.sf2.ADSR;

import waffleoRai_soundbank.Attack;
import waffleoRai_soundbank.Decay;
import waffleoRai_soundbank.Region;
import waffleoRai_soundbank.Release;
import waffleoRai_soundbank.Sustain;
import waffleoRai_soundbank.sf2.SF2;

public class ADSRC_RetainTime implements SF2ADSRConverter{
	
	public int getAttack(Attack a)
	{
		//int ms = a.getTime() * 1000;
		return SF2.millisecondsToTimecents(a.getTime());
	}
	
	public int getDecay(Decay d)
	{
		//int ms = d.getTime() * 1000;
		return SF2.millisecondsToTimecents(d.getTime());
	}
	
	public int getRelease(Release r)
	{
		//int ms = r.getTime() * 1000;
		return SF2.millisecondsToTimecents(r.getTime());
	}
	
	public int getSustain(Sustain s)
	{
		return s.getLevel();
	}
	
	public int getDelay(int ms_d)
	{
		return SF2.millisecondsToTimecents(ms_d);
	}
	
	public int getHold(int ms_h)
	{
		return SF2.millisecondsToTimecents(ms_h);
	}

	@Override
	public void calibrate(Region r) {
		//Does nothing
		
	}

}
