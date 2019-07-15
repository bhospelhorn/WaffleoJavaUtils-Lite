package waffleoRai_soundbank.sf2;

import java.util.HashMap;
import java.util.Map;

public enum SF2ModSourceType {
	
	LINEAR(0),
	CONCAVE(1),
	CONVEX(2),
	SWITCH(3);
	
	private int enumVal;
	
	private SF2ModSourceType(int eval)
	{
		enumVal = eval;
	}
	
	public int getValue()
	{
		return enumVal;
	}
	
	private static Map<Integer, SF2ModSourceType> vmap;
	
	private static void populateMap()
	{
		vmap = new HashMap<Integer, SF2ModSourceType>();
		SF2ModSourceType[] val = SF2ModSourceType.values();
		for (SF2ModSourceType t : val)
		{
			vmap.put(t.getValue(), t);
		}
	}
	
	public static SF2ModSourceType getSourceType(int val)
	{
		if (vmap == null) populateMap();
		return vmap.get(val);
	}
	

}
