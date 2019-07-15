package waffleoRai_soundbank.sf2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.Attack;
import waffleoRai_soundbank.Decay;
import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.Modulator;
import waffleoRai_soundbank.Region;
import waffleoRai_soundbank.Release;
import waffleoRai_soundbank.Sustain;
import waffleoRai_soundbank.sf2.ADSR.SF2ADSRConverter;
import waffleoRai_soundbank.sf2.generator.SF2GeneratorConverter;
import waffleoRai_soundbank.sf2.modulator.SF2ModConverter;

public class SF2Zone {
	
	private List<SF2Gen> generators;
	private List<SF2Mod> modulators;
	
	public SF2Zone()
	{
		generators = new LinkedList<SF2Gen>();
		modulators = new LinkedList<SF2Mod>();
	}
	
	public List<SF2Gen> getGenerators()
	{
		int gcount = 1;
		gcount += generators.size();
		List<SF2Gen> copy = new ArrayList<SF2Gen>(gcount);
		copy.addAll(generators);
		return copy;
	}
	
	public List<SF2Mod> getModulators()
	{
		int mcount = 1;
		mcount += modulators.size();
		List<SF2Mod> copy = new ArrayList<SF2Mod>(mcount);
		copy.addAll(modulators);
		return copy;
	}
	
	public int countGenerators()
	{
		return generators.size();
	}
	
	public int countModulators()
	{
		return modulators.size();
	}
	
	public void addGenerator(SF2Gen g)
	{
		generators.add(g);
	}
	
	public void addModulator(SF2Mod m)
	{
		modulators.add(m);
	}
	
	public void clearGenerators()
	{
		generators.clear();
	}
	
	public void clearModulators()
	{
		modulators.clear();
	}

	public FileBuffer getSerializedModList()
	{
		int mcount = modulators.size();
		if (mcount == 0) return null;
		FileBuffer mlist = new CompositeBuffer(mcount);
		for (SF2Mod m : modulators)
		{
			mlist.addToFile(m.serializeMe());
		}
		return mlist;
	}
	
	public void sortGenerators()
	{
		Collections.sort(generators);
	}
	
	public FileBuffer getSerializedGenList()
	{
		//Some generators demand to be higher in list. Make sure they are.
		sortGenerators();
		int gcount = generators.size();
		if (gcount == 0) return null;
		FileBuffer glist = new FileBuffer(gcount * 4, false);
		for (SF2Gen g : generators)
		{
			glist.addToFile((short)g.getType().getID());
			glist.addToFile(g.getRawAmount());
		}
		return glist;
	}
	
	public boolean hasInstrumentGenerator()
	{
		for (SF2Gen g : generators)
		{
			if (g.getType() == SF2GeneratorType.instrument) return true;
		}
		return false;
	}
	
	public boolean hasSampleGenerator()
	{
		for (SF2Gen g : generators)
		{
			if (g.getType() == SF2GeneratorType.sampleID) return true;
		}
		return false;
	}
	
	public static void convertZone(SF2Zone z, Region r, SF2ADSRConverter adsrcon)
	{
		if (z == null) return;
		if (r == null) return;

		//Dump region instance variables
		int val = r.getVolume();
		if (val != 0x7FFFFFFF)
		{
			int cval = SF2.envelopeDiffToCentibels(0x7FFFFFFF, val);
			SF2Gen g = new SF2Gen(SF2GeneratorType.initialAttenuation, (short)(Math.abs(cval)));
			z.addGenerator(g);
		}
		
		val = r.getPan();
		if (val != 0)
		{
			short cval = SF2.convertPan(val);
			SF2Gen g = new SF2Gen(SF2GeneratorType.pan, cval);
			z.addGenerator(g);
		}
		
		byte bval = r.getUnityKey();
		if (bval >= 0)
		{
			SF2Gen g = new SF2Gen(SF2GeneratorType.overridingRootKey, (short)bval);
			z.addGenerator(g);
		}
		
		bval = r.getFineTuneCents();
		if (bval != 0)
		{
			int cents = (int)bval;
			int coarse = cents / 100;
			if (coarse != 0)
			{
				SF2Gen g = new SF2Gen(SF2GeneratorType.coarseTune, (short)coarse);
				z.addGenerator(g);
			}
			
			cents = cents % 100;
			if (cents != 0)
			{
				SF2Gen g = new SF2Gen(SF2GeneratorType.fineTune, (short)cents);
				z.addGenerator(g);				
			}
		}
		
		byte min = r.getMinKey();
		byte max = r.getMaxKey();
		
		if (min != 0 || max != 127)
		{
			int cval = Byte.toUnsignedInt(max) << 8;
			cval |= Byte.toUnsignedInt(min);
			SF2Gen g = new SF2Gen(SF2GeneratorType.keyRange, (short)cval);
			z.addGenerator(g);	
		}
		
		min = r.getMinVelocity();
		max = r.getMaxVelocity();
		
		if (min != 0 || max != 127)
		{
			int cval = Byte.toUnsignedInt(min) << 8;
			cval |= Byte.toUnsignedInt(max);
			SF2Gen g = new SF2Gen(SF2GeneratorType.velRange, (short)cval);
			z.addGenerator(g);	
		}
		
		//ADSR
		if (adsrcon != null)
		{
			adsrcon.calibrate(r);
			
			Attack a = r.getAttack();
			if (a != null)
			{
				int cval = adsrcon.getAttack(a);
				SF2Gen g = new SF2Gen(SF2GeneratorType.attackVolEnv, (short)cval);
				z.addGenerator(g);	
			}
			
			Decay d = r.getDecay();
			if (d != null)
			{
				int cval = adsrcon.getDecay(d);
				SF2Gen g = new SF2Gen(SF2GeneratorType.decayVolEnv, (short)cval);
				z.addGenerator(g);	
			}
			
			Sustain s = r.getSustain();
			if (s != null)
			{
				int cval = adsrcon.getSustain(s);
				//Rescale to centibels
				if (cval == 0x7FFFFFFF) cval = 0;
				else
				{
					cval = SF2.envelopeDiffToCentibels(cval, 0x7FFFFFFF);
				}
				SF2Gen g = new SF2Gen(SF2GeneratorType.sustainVolEnv, (short)cval);
				z.addGenerator(g);	
			}
			
			Release l = r.getRelease();
			if (l != null)
			{
				int cval = adsrcon.getRelease(l);
				SF2Gen g = new SF2Gen(SF2GeneratorType.releaseVolEnv, (short)cval);
				z.addGenerator(g);	
			}
			
			if(r.getHoldInMillis() > 0)
			{
				int cval = adsrcon.getHold(r.getHoldInMillis());
				SF2Gen g = new SF2Gen(SF2GeneratorType.holdVolEnv, (short)cval);
				z.addGenerator(g);
			}
			
		}
		
		//Auto convert the remaining generators
		List<Generator> glist = r.getGenerators();
		if (glist != null && !glist.isEmpty())
		{
			for (Generator g : glist)
			{
				SF2Gen sfg = SF2GeneratorConverter.convertToSF2Gen(g);
				z.addGenerator(sfg);
			}
		}
		
		//Auto convert the modulators
		List<Modulator> mlist = r.getMods();
		if (mlist != null && !mlist.isEmpty())
		{
			for (Modulator m : mlist)
			{
				SF2Mod sfm = SF2ModConverter.convertMod(m);
				z.addModulator(sfm);
			}
		}
	}
	
	public static SF2Zone convertZone(Region r, SF2ADSRConverter adsrcon)
	{
		if (r == null) return null;
		SF2Zone z = new SF2Zone();
		convertZone(z, r, adsrcon);
		
		return z;
	}
	
}
