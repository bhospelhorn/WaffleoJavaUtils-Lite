package waffleoRai_soundbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;

public class SimplePreset extends Instrument{
	
	/* ----- Instance Variables ----- */
	
	//private Soundbank parent;
	//private Patch coordinates;
	
	private int masterVolume;
	private int masterPan; //Positive values to right - % of int max

	private List<Modulator> globalMods;
	private List<Generator> globalGens;
	
	private PresetRegion[] regions;
	
	/* ----- Construction ----- */
	
	public SimplePreset(Soundbank parentBank, Patch p, String name, int maxRegions)
	{
		super(parentBank, p, name, InstrumentData.class);
		//coordinates = p;
		//parent = parentBank;
		resetGlobalDefaults();
		if (maxRegions < 1) maxRegions = 1;
		regions = new PresetRegion[maxRegions];
	}
	
	public void resetGlobalDefaults()
	{
		masterVolume = Integer.MAX_VALUE;
		masterPan = 0;
		globalMods = new LinkedList<Modulator>();
		globalGens = new LinkedList<Generator>();
	}
	
	/* ----- Internal Classes ----- */
	
	public static class PresetRegion extends Region
	{
		private SimpleInstrument iInst;
		
		public PresetRegion(SimpleInstrument inst)
		{
			iInst = inst;
			resetDefaults();
		}
		
		public SimpleInstrument getInstrument() 
		{
			return iInst;
		}

		public void setInstrument(SimpleInstrument inst) 
		{
			iInst = inst;
		}
		
		public void printInfo()
		{
			super.printInfo(2);
			if(iInst != null){
				System.out.println("\t\t~~~ Linked Instrument ~~~");
				iInst.printInfo();
			}
		}
	}
	
	/* ----- Getters ----- */
	
	public int getMasterVolume()
	{
		return this.masterVolume;
	}
	
	public int getMasterPan()
	{
		return this.masterPan;
	}

	public List<Modulator> getGlobalMods()
	{
		int count = globalMods.size();
		count++;
		List<Modulator> copy = new ArrayList<Modulator>(count);
		copy.addAll(globalMods);
		return copy;
	}
	
	public List<Generator> getGlobalGenerators()
	{
		int count = globalGens.size();
		count++;
		List<Generator> copy = new ArrayList<Generator>(count);
		copy.addAll(globalGens);
		return copy;
	}
	
	public PresetRegion getRegion(int index)
	{
		if (index < 0) return null;
		if (index >= regions.length) return null;
		return regions[index];
	}

	public int countRegions()
	{
		int tot = 0;
		for (int i = 0; i < regions.length; i++)
		{
			if (regions[i] != null) tot++;
		}
		return tot;
	}
	
	public Collection<SimpleInstrument> getAllInstruments()
	{
		Set<SimpleInstrument> ilist = new HashSet<SimpleInstrument>();
		if (regions == null) return ilist;
		for (int i = 0; i < regions.length; i++)
		{
			if (regions[i] != null)
			{
				ilist.add(regions[i].getInstrument());
			}
		}
		
		return ilist;
	}

	public int getBankIndex()
	{
		return super.getPatch().getBank();
	}
	
	public int getPresetIndex()
	{
		return super.getPatch().getProgram();
	}
	
	/* ----- Setters ----- */
	
	public void setMasterVolume(int v)
	{
		masterVolume = v;
	}
	
	public void setMasterPan(int p)
	{
		masterPan = p;
	}
	
	public void addGlobalMod(Modulator m)
	{
		globalMods.add(m);
	}
	
	public void clearGlobalMods()
	{
		globalMods.clear();
	}
	
	public void addGlobalGenerator(Generator g)
	{
		globalGens.add(g);
	}
	
	public void clearGlobalGenerator()
	{
		globalGens.clear();
	}
	
	public void clearRegions()
	{
		for (int i = 0; i < regions.length; i++)
		{
			regions[i] = null;
		}
	}
	
	public int newInstrument(String iName, int iRegions)
	{
		for (int i = 0; i < regions.length; i++)
		{
			if (regions[i] == null)
			{
				regions[i] = new PresetRegion(new SimpleInstrument(iName, iRegions));
				return i;
			}
		}
		return -1;
	}
	
	public int newRegion(SimpleInstrument inst)
	{
		//Allows null for now...
		//if (inst == null) return -1;
		for (int i = 0; i < regions.length; i++)
		{
			if (regions[i] == null)
			{
				regions[i] = new PresetRegion(inst);
				return i;
			}
		}
		return -1;
	}
	
	/* ----- Java API ----- */

	@Override
	public Object getData() 
	{
		return new InstrumentData(this);
	}

	/* ----- Debug ----- */
	
	public void printInfo()
	{
		System.out.println("\tMaster Volume: 0x" + String.format("%08x", this.masterVolume));
		System.out.println("\tMaster Pan: 0x" + String.format("%04x", this.masterPan));
		System.out.println("\tRegions: " + countRegions());
		System.out.println("\tGlobal Generators: " + this.globalGens.size());
		if(!globalGens.isEmpty())
		{
			for(Generator g : globalGens) System.out.println("\t->" + g.getType() + " | " + g.getAmount());
		}
		System.out.println("\tGlobal Modulators: " + this.globalMods.size());
		if(!globalMods.isEmpty())
		{
			for(Modulator m : globalMods) System.out.println("\t->" + m.getSource() + 
					" | " + m.getDestination() + 
					" | " + m.getAmount() + 
					" | " + m.getSourceAmount() + 
					" | " + m.getTransform());
		}
		
		for(int i = 0; i < regions.length; i++)
		{
			if(regions[i] != null)
			{
				System.out.println("\tREGION " + i + " --");
				regions[i].printInfo();
			}
		}
	}
	
}
