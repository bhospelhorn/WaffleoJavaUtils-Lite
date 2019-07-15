package waffleoRai_soundbank;

import java.util.HashMap;
import java.util.Map;

import waffleoRai_soundbank.SimpleInstrument.InstRegion;
import waffleoRai_soundbank.SimplePreset.PresetRegion;

public class InstrumentData {
	
	private DataMap map;
	
	public InstrumentData(SimplePreset preset)
	{
		map = new DataMap();
		int preg = preset.countRegions();
		for (int i = 0; i < preg; i++)
		{
			PresetRegion r = preset.getRegion(i);
			SimpleInstrument inst = r.getInstrument();
			if (inst != null)
			{
				int ireg = inst.countRegions();
				for (int j = 0; j < ireg; j++)
				{
					InstRegion ir = inst.getRegion(j);
					map.put(i, j, ir.getSampleKey());
				}
			}
		}
	}
	
	private static class DataMap
	{
		private Map<Integer, InnerMap> map;
		
		public DataMap()
		{
			map = new HashMap<Integer, InnerMap>();
		}
		
		public synchronized void put(int p, int i, String s)
		{
			InnerMap im = map.get(p);
			if (im == null)
			{
				im = new InnerMap();
				im.put(i, s);
				map.put(p, im);
			}
			else
			{
				im.put(i, s);
			}
		}
		
		public synchronized String get(int p, int i)
		{
			InnerMap im = map.get(p);
			if (im == null) return null;
			return im.get(i);
		}
		
	}
	
	private static class InnerMap
	{
		private Map<Integer, String> map;
		
		public InnerMap()
		{
			map = new HashMap<Integer, String>();
		}
		
		public synchronized void put(int i, String s)
		{
			map.put(i, s);
		}
		
		public synchronized String get(int i)
		{
			return  map.get(i);
		}
	}
	
	public String getSoundKey(int pReg, int iReg)
	{
		return map.get(pReg, iReg);
	}

}
