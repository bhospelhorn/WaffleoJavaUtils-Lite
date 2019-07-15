package waffleoRai_soundbank.sf2;

import java.util.HashMap;
import java.util.Map;

public enum SF2ModController {
	
	NO_CONTROLLER(0),
	NOTE_ON_VELOCITY(2),
	NOTE_ON_KEYNUM(3),
	POLY_PRESSURE(10),
	CHANNEL_PRESSURE(13),
	PITCH_WHEEL(14),
	PITCH_WHEEL_SENSITIVITY(16),
	LINK(127),;
	
	private int enumVal;
	
	private SF2ModController(int eval)
	{
		enumVal = eval;
	}
	
	public int getValue()
	{
		return enumVal;
	}
	
	private static Map<Integer, SF2ModController> vmap;
	
	private static void populateMap()
	{
		vmap = new HashMap<Integer, SF2ModController>();
		SF2ModController[] val = SF2ModController.values();
		for (SF2ModController t : val)
		{
			vmap.put(t.getValue(), t);
		}
	}
	
	public static SF2ModController getController(int val)
	{
		if (vmap == null) populateMap();
		return vmap.get(val);
	}
	


}
