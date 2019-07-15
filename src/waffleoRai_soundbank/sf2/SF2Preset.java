package waffleoRai_soundbank.sf2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.ADSRMode;
import waffleoRai_soundbank.Attack;
import waffleoRai_soundbank.Decay;
import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.Modulator;
import waffleoRai_soundbank.Release;
import waffleoRai_soundbank.SimpleBank;
import waffleoRai_soundbank.SimpleInstrument;
import waffleoRai_soundbank.SimplePreset;
import waffleoRai_soundbank.SimplePreset.PresetRegion;
import waffleoRai_soundbank.SingleBank;
import waffleoRai_soundbank.Sustain;
import waffleoRai_soundbank.sf2.generator.SF2GeneratorConverter;
import waffleoRai_soundbank.sf2.modulator.SF2ModConverter;

public class SF2Preset implements Comparable<SF2Preset>{
	
	/* ----- Constants ----- */
	
	public static final String MAGIC_PHDR = "phdr";
	public static final String MAGIC_PBAG = "pbag";
	public static final String MAGIC_PMOD = "pmod";
	public static final String MAGIC_PGEN = "pgen";
	
	public static final int NAME_MAX_SIZE = 20;
	public static final int PHDR_SIZE = 38;
	
	/* ----- Instance Variables ----- */
	
	private String presetName;
	private int presetIndex;
	private int bankIndex;
	
	private int library;
	private int genre;
	private int morphology;
	
	private SF2Zone globalZone; //No instrument generator. First if present.
	private List<SF2Zone> zones; //Must have instrument generator.
	
	private int bag_idx; //For parsing, ignored in serialization and conversion
	
	/* ----- Construction ----- */
	
	public SF2Preset(int bank, int preset)
	{
		presetIndex = preset;
		bankIndex = bank;
		presetName = "B" + bank + "P" + preset;
		library = 0;
		genre = 0;
		morphology = 0;
		globalZone = null;
		zones = new LinkedList<SF2Zone>();
		bag_idx = -1;
	}
	
	public SF2Preset(FileBuffer phdr_record, long stpos) throws UnsupportedFileTypeException
	{
		globalZone = null;
		zones = new LinkedList<SF2Zone>();
		bag_idx = -1;
		parse_phdr_record(phdr_record, stpos);
	}
	
	/* ----- Parsing ----- */
	
	private void parse_phdr_record(FileBuffer phdr_record, long stpos) throws UnsupportedFileTypeException
	{
		if (phdr_record == null) throw new FileBuffer.UnsupportedFileTypeException();
		if (stpos < 0)  throw new FileBuffer.UnsupportedFileTypeException();
		
		long cpos = stpos;
		presetName = phdr_record.getASCII_string(cpos, NAME_MAX_SIZE); cpos += NAME_MAX_SIZE;
		presetIndex = Short.toUnsignedInt(phdr_record.shortFromFile(cpos)); cpos += 2;
		bankIndex = Short.toUnsignedInt(phdr_record.shortFromFile(cpos)); cpos += 2;
		bag_idx = Short.toUnsignedInt(phdr_record.shortFromFile(cpos)); cpos += 2;
		
		library = phdr_record.intFromFile(cpos); cpos += 4;
		genre = phdr_record.intFromFile(cpos); cpos += 4;
		morphology = phdr_record.intFromFile(cpos); cpos += 4;
	}
	
	/* ----- Serialization ----- */
	
	public FileBuffer serializePHDR_record(int stbag)
	{
		FileBuffer phdr = new FileBuffer(PHDR_SIZE, false);
		if (presetName.length() > NAME_MAX_SIZE) presetName = presetName.substring(0, NAME_MAX_SIZE);
		phdr.printASCIIToFile(presetName);
		while (phdr.getFileSize() < NAME_MAX_SIZE) phdr.addToFile((byte)0x00);
		phdr.addToFile((short)presetIndex);
		phdr.addToFile((short)bankIndex);
		phdr.addToFile((short)stbag);
		phdr.addToFile(library);
		phdr.addToFile(genre);
		phdr.addToFile(morphology);
		return phdr;
	}
	
	public FileBuffer serializePBAG_set(int stgen, int stmod) throws UnsupportedFileTypeException
	{
		//Gen (WORD)
		//Mod (WORD)
		int zc = countZones();
		FileBuffer pbags = new FileBuffer((zc * 4), false);
		int gen = stgen;
		int mod = stmod;
		if (globalZone != null)
		{
			pbags.addToFile((short)gen);
			pbags.addToFile((short)mod);
			int gc = globalZone.countGenerators();
			int mc = globalZone.countModulators();
			gen += gc;
			mod += mc;
		}
		int i = 0;
		for (SF2Zone z : zones)
		{
			if (!z.hasInstrumentGenerator())
			{
				System.err.println("SF2Preset.serializePBAG_set || Zone " + i + " of preset " + bankIndex + ":" + presetIndex + " lacks instrument generator!");
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			pbags.addToFile((short)gen);
			pbags.addToFile((short)mod);
			int gc = z.countGenerators();
			int mc = z.countModulators();
			gen += gc;
			mod += mc;
			i++;
		}
		return pbags;
	}
	
	public FileBuffer serializePGEN_set()
	{
		if (countGenerators() < 1) return null;
		int zc = countZones();
		FileBuffer pgens = new CompositeBuffer(zc);
		if (globalZone != null) {
			FileBuffer zgens = globalZone.getSerializedGenList();
			if (zgens != null) pgens.addToFile(zgens);
		}
		for (SF2Zone z : zones) {
			FileBuffer zgens = z.getSerializedGenList();
			if (zgens != null) pgens.addToFile(zgens);
		}
		
		return pgens;
	}
	
	public FileBuffer serializePMOD_set()
	{
		if (countModulators() < 1) return null;
		int zc = countZones();
		FileBuffer pmods = new CompositeBuffer(zc);
		if (globalZone != null) {
			FileBuffer zmods = globalZone.getSerializedModList();
			if(zmods != null) pmods.addToFile(zmods);
		}
		for (SF2Zone z : zones) {
			FileBuffer zmods = z.getSerializedModList();
			if(zmods != null) pmods.addToFile(zmods);
		}
		
		return pmods;
	}
	
	public static FileBuffer serializeEmptyPBAG(int gen, int mod)
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
		return presetName;
	}
	
	public int getPresetIndex()
	{
		return presetIndex;
	}
	
	public int getBankIndex()
	{
		return bankIndex;
	}
	
	public int getLibrary()
	{
		return library;
	}
	
	public int getGenre()
	{
		return genre;
	}
	
	public int getMorphology()
	{
		return morphology;
	}
	
	public int getRead_PBAG_index()
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
		presetName = n;
	}
	
	public void setPresetIndex(int i)
	{
		if (i < 0 || i > 127) return;
		presetIndex = i;
	}
	
	public void setBankIndex(int i)
	{
		if (i < 0) return;
		bankIndex = i;
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
		if (!z.hasInstrumentGenerator()) return false;
		zones.add(z);
		return true;
	}
	
	public void clearNonGlobalZones()
	{
		zones.clear();
	}

	/* ----- Sort ----- */
	
	@Override
	public int compareTo(SF2Preset o) 
	{
		if (o == null) return 1;
		if (o == this) return 0;
		
		if (o.bankIndex != this.bankIndex) return this.bankIndex - o.bankIndex;
		if (o.presetIndex != this.presetIndex) return this.presetIndex - o.presetIndex;
		return this.presetName.compareTo(o.presetName);
	}
	
	/* ----- Convert ----- */
	
	public void addSimplePreset(SimpleBank parentBank, List<SimpleInstrument> ilist)
	{
		//Patch patch = new Patch(bankIndex, presetIndex);
		SingleBank localBank = parentBank.getBank(bankIndex);
		if (localBank == null)
		{
			parentBank.newBank(bankIndex, "BANK_" + bankIndex);
			localBank = parentBank.getBank(bankIndex);
		}
		int zonecount = this.countZones();
		
		SimplePreset preset = localBank.getPreset(presetIndex);
		if (preset == null)
		{
			localBank.newPreset(presetIndex, presetName, zonecount);
			preset = localBank.getPreset(presetIndex);
		}
			
		int icount = ilist.size();

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
						preset.setMasterVolume(level);
						mvol = true;
					}
					else if (!mpan && g.getType() == SF2GeneratorType.pan)
					{
						int pan = SF2.getPan((int)g.getRawAmount());
						preset.setMasterPan(pan);
						mpan = true;
					}
					else
					{
						//Add as is
						Generator gen = SF2GeneratorConverter.convertGenerator(g);
						preset.addGlobalGenerator(gen);
					}
				}
				
			}
			if (m_global != null && !m_global.isEmpty())
			{
				for (SF2Mod m : m_global)
				{
					Modulator mod = SF2ModConverter.getMod(m);
					preset.addGlobalMod(mod);
				}
			}
		}
		
		//Go through each zone
		//	1. Get inst ID
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
				boolean inst_found = false; //InstID
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
				
				//Initial region will have a null instrument, since instrument
				//	generator is always last in the list...
				
				int rind = preset.newRegion(null);
				PresetRegion r = preset.getRegion(rind);
				
				//Get gens & mods
				List<SF2Gen> g_list = z.getGenerators();
				List<SF2Mod> m_list = z.getModulators();
				
				if (g_list != null && !g_list.isEmpty())
				{
					for (SF2Gen g : g_list)
					{
						if (!inst_found && g.getType() == SF2GeneratorType.instrument)
						{
							int iind = (int)g.getRawAmount();
							SimpleInstrument inst = null;
							if (iind >= 0 && iind < icount) inst = ilist.get(iind);
							r.setInstrument(inst);
							inst_found = true;
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

	}
	
}
