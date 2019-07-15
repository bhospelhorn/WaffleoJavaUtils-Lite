package waffleoRai_soundbank;

import waffleoRai_soundbank.sf2.SF2ModController;

public interface ModController {
	
	public boolean controllerSet(); //False if no controller
	public int getCurrentValue();
	public SF2ModController getSF2Enum();

}
