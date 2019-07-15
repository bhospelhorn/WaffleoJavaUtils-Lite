package waffleoRai_soundbank.modulator;

import waffleoRai_soundbank.ModController;
import waffleoRai_soundbank.sf2.SF2ModController;

public class EmptyModController implements ModController{

	@Override
	public boolean controllerSet() 
	{
		return false;
	}

	@Override
	public int getCurrentValue() 
	{
		return 0;
	}

	@Override
	public SF2ModController getSF2Enum() 
	{
		return SF2ModController.NO_CONTROLLER;
	}
	
	

}
