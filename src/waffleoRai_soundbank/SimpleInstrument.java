package waffleoRai_soundbank;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SimpleInstrument
{
	
	/* ----- Instance Variables ----- */
	
	private String name;
	
	private int masterVolume;
	private int masterPan; //Positive values to right - % of int max

	private List<Modulator> globalMods;
	private List<Generator> globalGens;
	
	private InstRegion[] regions;
	
	private int instanceID;
	
	/* ----- Construction ----- */
	
	public SimpleInstrument(String name, int maxRegions)
	{
		this.name = name;
		resetGlobalDefaults();
		if (maxRegions < 1) maxRegions = 1;
		regions = new InstRegion[maxRegions];
		Random r = new Random();
		instanceID = r.nextInt();
	}
	
	public void resetGlobalDefaults()
	{
		masterVolume = Integer.MAX_VALUE;
		masterPan = 0;
		//masterAttack = Attack.getDefault();
		//masterDecay = Decay.getDefault();
		//masterSustain = Sustain.getDefault();
		//masterRelease = Release.getDefault();
		globalMods = new LinkedList<Modulator>();
		globalGens = new LinkedList<Generator>();
	}
	
	/* ----- Internal Classes ----- */
	
	public static class InstRegion extends Region
	{
		//private Sound iSample;
		private String iSampleKey;
		
		public InstRegion(String samplekey)
		{
			//setSample(samplekey);
			iSampleKey = samplekey;
			super.resetDefaults();
		}
		
		public String getSampleKey() {
			return iSampleKey;
		}

		public void setSampleKey(String key) 
		{
			this.iSampleKey = key;
		}
		
		public void printInfo()
		{
			super.printInfo(3);
			System.out.println("\t\t\tSound Key: " + iSampleKey);
		}
		
	}
	
	/* ----- Getters ----- */
	
	public String getName()
	{
		return name;
	}
	
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
	
	public InstRegion getRegion(int index)
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
	
	public int hashCode()
	{
		return instanceID;
	}
	
	/* ----- Setters ----- */
	
	public void setName(String n)
	{
		name = n;
	}
	
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
	
	public int newRegion(String soundKey)
	{
		for (int i = 0; i < regions.length; i++)
		{
			if (regions[i] == null)
			{
				regions[i] = new InstRegion(soundKey);
				return i;
			}
		}
		return -1;
	}
	
	public void printInfo()
	{
		System.out.println("\t\tMaster Volume: 0x" + String.format("%08x", this.masterVolume));
		System.out.println("\t\tMaster Pan: 0x" + String.format("%04x", this.masterPan));
		System.out.println("\t\tRegions: " + countRegions());
		System.out.println("\t\tGlobal Generators: " + this.globalGens.size());
		if(!globalGens.isEmpty())
		{
			for(Generator g : globalGens) System.out.println("\t\t->" + g.getType() + " | " + g.getAmount());
		}
		System.out.println("\t\tGlobal Modulators: " + this.globalMods.size());
		if(!globalMods.isEmpty())
		{
			for(Modulator m : globalMods) System.out.println("\t\t->" + m.getSource() + 
					" | " + m.getDestination() + 
					" | " + m.getAmount() + 
					" | " + m.getSourceAmount() + 
					" | " + m.getTransform());
		}
		
		for(int i = 0; i < regions.length; i++)
		{
			if(regions[i] != null)
			{
				System.out.println("\t\tREGION " + i + " --");
				regions[i].printInfo();
			}
		}
	}
	
}
