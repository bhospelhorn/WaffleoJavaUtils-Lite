package waffleoRai_soundbank.sf2;

import java.util.HashMap;
import java.util.Map;

public enum SF2TransformType 
{

	LINEAR(0),
	ABS_VAL(2);
	
	private int enumVal;
	
	private SF2TransformType(int eval)
	{
		enumVal = eval;
	}
	
	public int getValue()
	{
		return enumVal;
	}
	
	private static Map<Integer, SF2TransformType> vmap;
	
	private static void populateMap()
	{
		vmap = new HashMap<Integer, SF2TransformType>();
		SF2TransformType[] val = SF2TransformType.values();
		for (SF2TransformType t : val)
		{
			vmap.put(t.getValue(), t);
		}
	}
	
	public static SF2TransformType getTransform(int val)
	{
		if (vmap == null) populateMap();
		return vmap.get(val);
	}
	
}
