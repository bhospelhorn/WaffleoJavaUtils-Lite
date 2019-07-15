package waffleoRai_soundbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

import waffleoRai_Sound.Sound;

public class SimpleBank implements Soundbank{

	private String sName;
	private String sVersion;
	private String sVendor;
	
	private String sDescription;
	
	private SingleBank[] iBanks;
	private Map<String, SoundSample> samples;
	
	public SimpleBank(String name, String version, String vendor, int maxBanks)
	{
		sName = name;
		sVersion = version;
		sVendor = vendor;
		if (maxBanks < 1) maxBanks = 1;
		if (maxBanks > 16384) maxBanks = 16384;
		iBanks = new SingleBank[maxBanks];
		sDescription = "";
		samples = new HashMap<String, SoundSample>();
	}
	
	@Override
	public String getName() 
	{
		return sName;
	}

	@Override
	public String getVersion() 
	{
		return sVersion;
	}

	@Override
	public String getVendor() 
	{
		return sVendor;
	}

	@Override
	public String getDescription() 
	{
		return sDescription;
	}

	@Override
	public SoundbankResource[] getResources() 
	{
		List<SoundbankResource> rlist = new LinkedList<SoundbankResource>();
		for (SingleBank b : iBanks)
		{
			if (b != null)
			{
				rlist.addAll(b.getAllPresets());
				//rlist.addAll(b.getAllSamples());
			}
		}
		rlist.addAll(getAllSamples());
		int nr = rlist.size();
		if (nr < 1) return null;
		SoundbankResource[] rarr = new SoundbankResource[nr];
		rarr = rlist.toArray(rarr);
		return rarr;
	}

	@Override
	public Instrument[] getInstruments() 
	{
		List<Instrument> rlist = new LinkedList<Instrument>();
		for (SingleBank b : iBanks)
		{
			if (b != null)
			{
				rlist.addAll(b.getAllPresets());
			}
		}
		int nr = rlist.size();
		if (nr < 1) return null;
		Instrument[] rarr = new Instrument[nr];
		rarr = rlist.toArray(rarr);
		return rarr;
	}

	@Override
	public Instrument getInstrument(Patch patch) 
	{
		int b = patch.getBank();
		int p = patch.getProgram();
		SingleBank bank = getBank(b);
		if (bank == null) return null;
		SimplePreset preset = bank.getPreset(p);
		return preset;
	}
	
	public SingleBank getBank(int index)
	{
		if (index < 0 || index >= iBanks.length) return null;
		return iBanks[index];
	}
	
	public int newBank(int index, String name)
	{
		if (index < 0 || index >= iBanks.length) return -1;
		SingleBank bank = iBanks[index];
		if (bank != null)
		{
			index = -1;
			for (int i = 0; i < iBanks.length; i++)
			{
				if (iBanks[index] == null) {
					index = i;
					break;
				}
			}
			if (index < 0) return -1;
		}
		
		iBanks[index] = new SingleBank(this, index);
		
		return index;
	}
	
	public void setDescription(String desc)
	{
		if (desc == null) return;
		sDescription = desc;
	}
	
	public SoundSample getSample(String key)
	{
		return samples.get(key);
	}
	
	public List<SoundSample> getAllSamples()
	{
		int nsamps = samples.size();
		List<SoundSample> sorted = new ArrayList<SoundSample>(nsamps+1); 
		List<String> sortedkeys = new ArrayList<String>(nsamps + 1);
		sortedkeys.addAll(samples.keySet());
		Collections.sort(sortedkeys);
		for (String k : sortedkeys)
		{
			sorted.add(samples.get(k));
		}
		return sorted;
	}
	
	public List<String> getAllSampleKeys()
	{
		List<String> list = new LinkedList<String>();
		Set<String> keyset = samples.keySet();
		list.addAll(keyset);
		Collections.sort(list);
		return list;
	}
	
	public void addSample(String key, Sound sample)
	{
		SoundSample s = new SoundSample(this, key, sample);
		samples.put(key, s);
	}
	
	public void addSample(String key, SoundSample sample)
	{
		samples.put(key, sample);
	}

	public Set<SimpleInstrument> getAllBaseInstruments()
	{
		List<SimplePreset> plist = getAllPresets();
		Set<SimpleInstrument> ilist = new HashSet<SimpleInstrument>();
		if (plist == null) return ilist;
		if (plist.isEmpty()) return ilist;
		for (SimplePreset p : plist)
		{
			if (p != null)
			{
				Collection<SimpleInstrument> pinst = p.getAllInstruments();
				if (pinst != null && !pinst.isEmpty())
				{
					ilist.addAll(pinst);
				}
			}
		}
		
		return ilist;
	}
	
	public List<SimplePreset> getAllPresets()
	{
		List<SimplePreset> plist = new LinkedList<SimplePreset>();
		if (iBanks == null) return plist;
		for (int i = 0; i < iBanks.length; i++)
		{
			if (iBanks[i] == null) continue;
			List<SimplePreset> bank_plist = iBanks[i].getAllPresets();
			if (bank_plist != null && !bank_plist.isEmpty()) plist.addAll(bank_plist);
		}
		return plist;
	}
	
	public List<SingleBank> getAllBanks()
	{
		List<SingleBank> blist = new LinkedList<SingleBank>();
		if (iBanks == null) return blist;
		for (int i = 0; i < iBanks.length; i++)
		{
			if (iBanks[i] != null) blist.add(iBanks[i]);
		}
		return blist;
	}
	
	public void printInfo()
	{
		System.out.println("---== Soundbank ==---");
		System.out.println("Name: " + this.sName);
		System.out.println("Version: " + this.sVersion);
		System.out.println("Vendor: " + this.sVendor);
		System.out.println("Description: " + this.sDescription);
		System.out.println("Sounds: " + this.samples.size());
		System.out.println("Sample List -- ");
		List<String> sampleKeys = new LinkedList<String>();
		sampleKeys.addAll(this.samples.keySet());
		Collections.sort(sampleKeys);
		for(String k : sampleKeys)
		{
			System.out.println("\t->" + k);
			SoundSample samp = samples.get(k);
			if(samp != null)
			{
				Sound s = samp.getSound();
				if(s.totalChannels() == 1) System.out.println("\tMono, " + s.getSampleRate() + " Hz");
				else System.out.println("\t" + s.totalChannels() + " Channels, " + s.getSampleRate() + " Hz");	
			}
			else System.out.println("\t(Empty)");
		}
		
		//Banks
		//int bcount = 0;
		for(int i = 0; i < iBanks.length; i++)
		{
			System.out.println();
			if(iBanks[i] != null) iBanks[i].printInfo();
		}
		
		
	}
	
}
