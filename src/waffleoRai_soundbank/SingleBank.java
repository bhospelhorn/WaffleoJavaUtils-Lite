package waffleoRai_soundbank;

import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;


public class SingleBank {
	
	private Soundbank iParent;
	private int iIndex;
	
	private SimplePreset[] presets;
	//private Map<String, SoundSample> samples;
	
	public SingleBank(Soundbank parentbank, int index)
	{
		iParent = parentbank;
		iIndex = index;
		
		presets = new SimplePreset[128];
		//samples = new HashMap<String, SoundSample>();
	}
	
	public int getIndex()
	{
		return iIndex;
	}
	
	public SimplePreset getPreset(int index)
	{
		if (index < 0) return null;
		if (index > 127) return null;
		return presets[index];
	}
	
	public List<SimplePreset> getAllPresets()
	{
		List<SimplePreset> plist = new LinkedList<SimplePreset>();
		for (SimplePreset p : presets)
		{
			if (p != null) plist.add(p);
		}
		return plist;
	}
	
	public Soundbank getParentBank()
	{
		return iParent;
	}

	public int newPreset(int index, String presetName, int maxRegions)
	{
		if (index < 0 || index > 127) return -1;
		SimplePreset p = presets[index];
		if (p != null)
		{
			//Find empty slot
			index = -1;
			for (int i = 0; i < 127; i++)
			{
				p = presets[index];
				if (p == null) {
					index = i;
					break;
				}
			}
		}
		if (index < 0) return -1;
		p = new SimplePreset(iParent, new Patch(iIndex, index), presetName, maxRegions);
		presets[index] = p;
		return index;
	}

	public int countValidPresets()
	{
		int pcount = 0;
		for(int i = 0; i < presets.length; i++)
		{
			if (presets[i] != null) pcount++;
		}
		return pcount;
	}
	
	public void printInfo()
	{
		System.out.println("-- BANK " + iIndex + " --");
		System.out.println("Presets: " + countValidPresets());
		
		for(int i = 0; i < presets.length; i++)
		{
			//System.out.println();
			//System.out.println("PRESET " + i + " --");
			if(presets[i] != null)
			{
				System.out.println();
				System.out.println("PRESET " + i + " --");
				presets[i].printInfo();
			}
			//else System.out.println("(Empty)");
		}
		
	}
	
}
