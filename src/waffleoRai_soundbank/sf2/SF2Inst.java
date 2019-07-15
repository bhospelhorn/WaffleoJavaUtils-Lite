package waffleoRai_soundbank.sf2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.ADSRMode;
import waffleoRai_soundbank.Attack;
import waffleoRai_soundbank.Decay;
import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.Modulator;
import waffleoRai_soundbank.Release;
import waffleoRai_soundbank.SimpleInstrument;
import waffleoRai_soundbank.SimpleInstrument.InstRegion;
import waffleoRai_soundbank.Sustain;
import waffleoRai_soundbank.sf2.generator.SF2GeneratorConverter;
import waffleoRai_soundbank.sf2.modulator.SF2ModConverter;

public class SF2Inst {
	
/* ----- Constants ----- */
	
	public static final String MAGIC_INST = "inst";
	public static final String MAGIC_IBAG = "ibag";
	public static final String MAGIC_IMOD = "imod";
	public static final String MAGIC_IGEN = "igen";
	
	public static final int NAME_MAX_SIZE = 20;
	public static final int INST_SIZE = 22;
	
	/* ----- Instance Variables ----- */
	
	private String instName;
	
	private SF2Zone globalZone; //No instrument generator. First if present.
	private List<SF2Zone> zones; //Must have instrument generator.
	
	private int bag_idx; //For parsing, ignored in serialization and conversion
	
	private int uid;
	
	/* ----- Construction ----- */
	
	public SF2Inst(int i)
	{
		instName = "I" + i;
		globalZone = null;
		zones = new LinkedList<SF2Zone>();
		bag_idx = -1;
		Random r = new Random();
		uid = r.nextInt();
	}
	
	public SF2Inst(FileBuffer inst_record, long stpos) throws UnsupportedFileTypeException
	{
		globalZone = null;
		zones = new LinkedList<SF2Zone>();
		bag_idx = -1;
		parse_inst_record(inst_record, stpos);
		Random r = new Random();
		uid = r.nextInt();
	}
	
	/* ----- Parsing ----- */
	
	private void parse_inst_record(FileBuffer inst_record, long stpos) throws UnsupportedFileTypeException
	{
		if (inst_record == null) throw new FileBuffer.UnsupportedFileTypeException();
		if (stpos < 0)  throw new FileBuffer.UnsupportedFileTypeException();
		
		long cpos = stpos;
		instName = inst_record.getASCII_string(cpos, NAME_MAX_SIZE); cpos += NAME_MAX_SIZE;
		bag_idx = Short.toUnsignedInt(inst_record.shortFromFile(cpos)); cpos += 2;
	}
	
	/* ----- Serialization ----- */
	
	public FileBuffer serializeINST_record(int stbag)
	{
		FileBuffer inst = new FileBuffer(INST_SIZE, false);
		if (instName.length() > NAME_MAX_SIZE) instName = instName.substring(0, NAME_MAX_SIZE);
		inst.printASCIIToFile(instName);
		while (inst.getFileSize() < NAME_MAX_SIZE) inst.addToFile((byte)0x00);
		inst.addToFile((short)stbag);
		return inst;
	}
	
	public FileBuffer serializeIBAG_set(int stgen, int stmod) throws UnsupportedFileTypeException
	{
		//Gen (WORD)
		//Mod (WORD)
		int zc = countZones();
		//System.err.println("Zones: " + zc);
		FileBuffer ibags = new FileBuffer((zc * 4), false);
		int gen = stgen;
		int mod = stmod;
		if (globalZone != null)
		{
			//System.err.println("Writing Global Zone...");
			ibags.addToFile((short)gen);
			ibags.addToFile((short)mod);
			int gc = globalZone.countGenerators();
			int mc = globalZone.countModulators();
			gen += gc;
			mod += mc;
		}
		int i = 0;
		for (SF2Zone z : zones)
		{
			//System.err.println("Writing Zone " + i);
			if (!z.hasSampleGenerator())
			{
				System.err.println("SF2Inst.serializeIBAG_set || Zone " + i + " of instrument " + instName + " lacks sampleID generator!");
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			ibags.addToFile((short)gen);
			ibags.addToFile((short)mod);
			int gc = z.countGenerators();
			int mc = z.countModulators();
			gen += gc;
			mod += mc;
			i++;
		}
		return ibags;
	}
	
	public FileBuffer serializeIGEN_set()
	{
		if (this.countGenerators() < 1) return null;
		int zc = countZones();
		FileBuffer igens = new CompositeBuffer(zc);
		if (globalZone != null) {
			FileBuffer zgens = globalZone.getSerializedGenList();
			if (zgens != null) igens.addToFile(zgens);
		}
		for (SF2Zone z : zones) {
			FileBuffer zgens = z.getSerializedGenList();
			if (zgens != null) igens.addToFile(zgens);
		}
		
		return igens;
	}
	
	public FileBuffer serializeIMOD_set()
	{
		if (countModulators() < 1) return null;
		int zc = countZones();
		FileBuffer imods = new CompositeBuffer(zc);
		if (globalZone != null) {
			FileBuffer zmods = globalZone.getSerializedModList();
			if(zmods != null) imods.addToFile(zmods);
		}
		for (SF2Zone z : zones) {
			FileBuffer zmods = z.getSerializedModList();
			if(zmods != null) imods.addToFile(zmods);
		}
		
		return imods;
	}
	
	public static FileBuffer serializeEmptyIBAG(int gen, int mod)
	{
		FileBuffer rec = new FileBuffer(4, false);
		rec.addToFile((short)gen);
		rec.addToFile((short)mod);
		return rec;
	}
	
	/* ----- Getters ----- */
	
	public int countZones()
	{
		int zcount = zones.size();
		if (globalZone != null) zcount++;
		return zcount;
	}
	
	public int countGenerators()
	{
		int gcount = 0;
		if (globalZone != null) gcount += globalZone.countGenerators();
		for (SF2Zone z : zones) gcount += z.countGenerators();
		return gcount;
	}
	
	public int countModulators()
	{
		int mcount = 0;
		if (globalZone != null) mcount += globalZone.countModulators();
		for (SF2Zone z : zones) mcount += z.countModulators();
		return mcount;
	}
	
	public String getName()
	{
		return instName;
	}
	
	public int getRead_IBAG_index()
	{
		return bag_idx;
	}
	
	public SF2Zone getGlobalZone()
	{
		return globalZone;
	}
	
	public SF2Zone getZone(int index)
	{
		if (zones == null) return null;
		if (zones.isEmpty()) return null;
		if (index < 0) return null;
		if (index >= zones.size()) return null;
		return zones.get(index);
	}
	
	public List<SF2Zone> getNonGlobalZones()
	{
		int zc = 1;
		zc += zones.size();
		List<SF2Zone> copy = new ArrayList<SF2Zone>(zc);
		copy.addAll(zones);
		return copy;
	}
	
	public boolean hasGlobalZone()
	{
		return (globalZone != null);
	}
	
	/* ----- Setters ----- */
	
	public void setName(String n)
	{
		instName = n;
	}
	
	public void instantiateGlobalZone()
	{
		globalZone = new SF2Zone();
	}
	
	public void deleteGlobalZone()
	{
		globalZone = null;
	}
	
	public boolean addZone(SF2Zone z)
	{
		if (!z.hasSampleGenerator()) return false;
		zones.add(z);
		return true;
	}
	
	public void clearNonGlobalZones()
	{
		zones.clear();
	}

	/* ----- Conversion ----- */

	public SimpleInstrument toInstrument()
	{
		SimpleInstrument inst = new SimpleInstrument(getName(), countZones());
		
		//Analyze global zone, if present
		if (globalZone != null)
		{
			List<SF2Gen> g_global = globalZone.getGenerators();
			List<SF2Mod> m_global = globalZone.getModulators();
			if (g_global != null && !g_global.isEmpty())
			{
				//Look for master volume & master pan
				boolean mvol = false;
				boolean mpan = false;
				for (SF2Gen g : g_global)
				{
					if (!mvol && g.getType() == SF2GeneratorType.initialAttenuation)
					{
						//Grab amount and scale to master volume
						// cB -> 1/(intmax)
						int cb = (int)g.getRawAmount() * -1;
						double eratio = SF2.centibelsToEnvelopeRatio(cb);
						int level = (int)Math.round((double)0x7FFFFFFF * eratio);
						inst.setMasterVolume(level);
						mvol = true;
					}
					else if (!mpan && g.getType() == SF2GeneratorType.pan)
					{
						int pan = SF2.getPan((int)g.getRawAmount());
						inst.setMasterPan(pan);
						mpan = true;
					}
					else
					{
						//Add as is
						Generator gen = SF2GeneratorConverter.convertGenerator(g);
						inst.addGlobalGenerator(gen);
					}
				}
				
			}
			if (m_global != null && !m_global.isEmpty())
			{
				for (SF2Mod m : m_global)
				{
					Modulator mod = SF2ModConverter.getMod(m);
					inst.addGlobalMod(mod);
				}
			}
		}
		
		//Go through each zone
		//	1. Get sample ID (remove from list)
		// 	2. Search for standard region params set as generators here (and remove from list)
			// - Master volume
			// - Master pan
			// - Unity Key (Override)
			// - Fine Tune (Coarse tune is additional) (Override)
			// - Key Range
			// - Velocity Range
			// - [Pitch bend isn't stored in SF2]
			// - ADSR (Vol Envelope)
		//  3. Copy/convert all remaining generators
		//	4. Copy/convert all modulators
		if (zones != null && !zones.isEmpty())
		{
			for (SF2Zone z : zones)
			{
				boolean samp_found = false; //SampleID
				boolean v_found = false; //Volume
				boolean p_found = false; //Pan
				boolean uk_found = false; //Unity Key
				boolean ft_found = false; //Fine tune
				boolean kr_found = false; //Key range
				boolean vr_found = false; //Velocity range
				boolean a_found = false; //Attack
				boolean d_found = false; //Decay
				boolean s_found = false; //Sustain
				boolean r_found = false; //Release
				
				int rind = inst.newRegion(SF2.s_name_root + "0");
				InstRegion r = inst.getRegion(rind);
				
				//Get gens & mods
				List<SF2Gen> g_list = z.getGenerators();
				List<SF2Mod> m_list = z.getModulators();
				
				if (g_list != null && !g_list.isEmpty())
				{
					for (SF2Gen g : g_list)
					{
						if (!samp_found && g.getType() == SF2GeneratorType.sampleID)
						{
							r.setSampleKey(SF2.s_name_root + g.getRawAmount());
							samp_found = true;
						}
						else if (!v_found && g.getType() == SF2GeneratorType.initialAttenuation)
						{
							//cB to 1/maxint units
							int cb = (int)g.getRawAmount() * -1;
							double eratio = SF2.centibelsToEnvelopeRatio(cb);
							int level = (int)Math.round((double)0x7FFFFFFF * eratio);
							r.setVolume(level);
							v_found = true;
						}
						else if (!p_found && g.getType() == SF2GeneratorType.pan)
						{
							short pan = SF2.getShortPan(g.getRawAmount());
							r.setPan(pan);
							p_found = true;
						}
						else if (!uk_found && g.getType() == SF2GeneratorType.overridingRootKey)
						{
							r.setUnityKey((byte)g.getRawAmount());
							uk_found = true;
						}
						else if (!ft_found && g.getType() == SF2GeneratorType.fineTune)
						{
							r.setFineTune((byte)g.getRawAmount());
							ft_found = true;
						}
						else if (!kr_found && g.getType() == SF2GeneratorType.keyRange)
						{
							int top = Short.toUnsignedInt(g.getRawAmount()) & 0xFF;
							int bot = (Short.toUnsignedInt(g.getRawAmount()) >>> 8) & 0xFF;
							r.setMaxKey((byte)top);
							r.setMinKey((byte)bot);
							kr_found = true;
						}
						else if (!vr_found && g.getType() == SF2GeneratorType.velRange)
						{
							int top = Short.toUnsignedInt(g.getRawAmount()) & 0xFF;
							int bot = (Short.toUnsignedInt(g.getRawAmount()) >>> 8) & 0xFF;
							r.setMaxVelocity((byte)top);
							r.setMinVelocity((byte)bot);
							vr_found = true;
						}
						else if (!a_found && g.getType() == SF2GeneratorType.attackVolEnv)
						{
							int tc = (int)g.getRawAmount();
							int ms = SF2.timecentsToMilliseconds(tc);
							Attack a = new Attack(ms, ADSRMode.LINEAR_ENVELOPE);
							r.setAttack(a);
							a_found = true;
						}
						else if (!d_found && g.getType() == SF2GeneratorType.decayVolEnv)
						{
							int tc = (int)g.getRawAmount();
							int ms = SF2.timecentsToMilliseconds(tc);
							Decay d = new Decay(ms, ADSRMode.LINEAR_DB);
							r.setDecay(d);
							d_found = true;
						}
						else if (!s_found && g.getType() == SF2GeneratorType.sustainVolEnv)
						{
							int rawl = Short.toUnsignedInt(g.getRawAmount());
							//cB to 1/maxint units
							int cb = rawl * -1;
							double eratio = SF2.centibelsToEnvelopeRatio(cb);
							int level = (int)Math.round((double)0x7FFFFFFF * eratio);
							Sustain s = new Sustain(level);
							r.setSustain(s);
							s_found = true;
						}
						else if (!r_found && g.getType() == SF2GeneratorType.releaseVolEnv)
						{
							int tc = (int)g.getRawAmount();
							int ms = SF2.timecentsToMilliseconds(tc);
							Release d = new Release(ms, ADSRMode.LINEAR_DB);
							r.setRelease(d);
							r_found = true;
						}
						else
						{
							//Add as is
							Generator gen = SF2GeneratorConverter.convertGenerator(g);
							r.addGenerator(gen);
						}
					}
				}
				
				if (m_list != null && !m_list.isEmpty())
				{
					for (SF2Mod m : m_list)
					{
						Modulator mod = SF2ModConverter.getMod(m);
						r.addMod(mod);
					}
				}
				
			}
			
		}
		
		return inst;
	}
	
	public int hashCode()
	{
		return uid;
	}

}
