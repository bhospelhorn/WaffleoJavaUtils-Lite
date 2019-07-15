package waffleoRai_soundbank.sf2;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.Modulator;
import waffleoRai_soundbank.SimpleBank;
import waffleoRai_soundbank.SimpleInstrument;
import waffleoRai_soundbank.SimpleInstrument.InstRegion;
import waffleoRai_soundbank.SimplePreset;
import waffleoRai_soundbank.SimplePreset.PresetRegion;
import waffleoRai_soundbank.SoundSample;
import waffleoRai_soundbank.sf2.SF2SHDR.LinkType;
import waffleoRai_soundbank.sf2.ADSR.ADSRC_RetainTime;
import waffleoRai_soundbank.sf2.ADSR.SF2ADSRConverter;
import waffleoRai_soundbank.sf2.generator.SF2GeneratorConverter;
import waffleoRai_soundbank.sf2.modulator.SF2ModConverter;

public class SF2 {
	
	/* ----- Constants ----- */
	
	public static final String RIFF_MAGIC = "RIFF";
	public static final String SF2_MAGIC = "sfbk";
	
	public static final String LIST_MAGIC = "LIST";
	
	public static final int MAX_BANKS = 0x7FFF;
	
	public static final String s_name_root = "S_";
	
	/* ----- Instance Variables ----- */
	
	private static SF2ADSRConverter adsrcon = new ADSRC_RetainTime();
	
	private SF2INFO infochunk;
	private SF2SDTA sdata;
	private SF2PDTA pdata;
	
	/* ----- Construction ----- */
	
	protected SF2()
	{
		infochunk = new SF2INFO("");
		sdata = new SF2SDTA();
		pdata = new SF2PDTA();
	}
	
	public SF2(String filepath) throws IOException, UnsupportedFileTypeException
	{
		FileBuffer sf2 = FileBuffer.createBuffer(filepath, false);
		parseSF2(sf2);
	}
	
	/* ----- Parsing ----- */
	
	private void parseSF2(FileBuffer file) throws UnsupportedFileTypeException, IOException
	{
		if (file == null) throw new FileBuffer.UnsupportedFileTypeException();
		file.setEndian(false);
		
		//Check for SF2 magic numbers
		long cpos = 0;
		cpos = file.findString(0, 0x10, RIFF_MAGIC);
		if (cpos != 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos = file.findString(0x08, 0x20, SF2_MAGIC);
		if (cpos != 0x08) throw new FileBuffer.UnsupportedFileTypeException();
		long fsize = file.getFileSize();
		
		//Look for the three big chunks
		//INFO
		long info_pos = -1;
		long info_size = -1;
		while (info_pos < 0)
		{
			cpos = file.findString(cpos, fsize, SF2INFO.INFO_MAGIC);
			if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException(); //If not there, cannot read
			info_pos = cpos;
			cpos = file.findString(info_pos - 8L, info_pos, LIST_MAGIC); //Look for LIST 8 bytes back
			if (cpos != (info_pos - 8L))
			{
				cpos = info_pos + 4;
				info_pos = -1;
			}
			else
			{
				info_pos = cpos;
				info_size = Integer.toUnsignedLong(file.intFromFile(info_pos + 4L));
			}
		}
		
		//SDTA
		long sdta_pos = -1;
		long sdta_size = -1;
		cpos = 0;
		while (sdta_pos < 0)
		{
			cpos = file.findString(cpos, fsize, SF2SDTA.MAGIC_SDTA);
			if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException(); //If not there, cannot read
			sdta_pos = cpos;
			cpos = file.findString(sdta_pos - 8L, sdta_pos, LIST_MAGIC); //Look for LIST 8 bytes back
			if (cpos != (sdta_pos - 8L))
			{
				cpos = sdta_pos + 4;
				sdta_pos = -1;
			}
			else
			{
				sdta_pos = cpos;
				sdta_size = Integer.toUnsignedLong(file.intFromFile(sdta_pos + 4L));
			}
		}
		
		//PDTA
		long pdta_pos = -1;
		long pdta_size = -1;
		cpos = 0;
		while (pdta_pos < 0)
		{
			cpos = file.findString(cpos, fsize, SF2PDTA.MAGIC_PDTA);
			if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException(); //If not there, cannot read
			pdta_pos = cpos;
			cpos = file.findString(pdta_pos - 8L, pdta_pos, LIST_MAGIC); //Look for LIST 8 bytes back
			if (cpos != (pdta_pos - 8L))
			{
				cpos = pdta_pos + 4;
				pdta_pos = -1;
			}
			else
			{
				pdta_pos = cpos;
				pdta_size = Integer.toUnsignedLong(file.intFromFile(pdta_pos + 4L));
			}
		}
		
		//Parse INFO
		FileBuffer info_chunk = file.createReadOnlyCopy(info_pos, info_pos + info_size);
		infochunk = new SF2INFO(info_chunk);
		
		//Parse PDTA
		FileBuffer pdta_chunk = file.createReadOnlyCopy(pdta_pos, pdta_pos + pdta_size);
		pdata = new SF2PDTA(pdta_chunk, 0);
		
		//Find the shdr sub-chunk
		long shdr_pos = -1;
		shdr_pos = pdta_chunk.findString(0, pdta_size, SF2SHDR.SHDR_MAGIC);
		if (shdr_pos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		long shdr_size = Integer.toUnsignedLong(pdta_chunk.intFromFile(shdr_pos + 8L));
		FileBuffer shdr_chunk = pdta_chunk.createReadOnlyCopy(shdr_pos, shdr_pos + shdr_size);
		
		//Parse shdr chunk & SDTA
		FileBuffer sdta_chunk = file.createReadOnlyCopy(sdta_pos, sdta_pos + sdta_size);
		sdata = new SF2SDTA(shdr_chunk, sdta_chunk);
		
	}
	
	/* ----- Serialization ----- */
	
	public void writeSF2(String filepath) throws UnsupportedFileTypeException, IOException
	{
		FileBuffer info_chunk = infochunk.serializeMe();
		FileBuffer sdta_chunk = sdata.serializeSDTA();
		FileBuffer pdta_chunk = pdata.serializePDTA(sdata);
		
		long totalsz = info_chunk.getFileSize() + sdta_chunk.getFileSize() + pdta_chunk.getFileSize();
		FileBuffer header = new FileBuffer(12, false);
		header.printASCIIToFile(RIFF_MAGIC);
		header.addToFile((int)totalsz + 4);
		header.printASCIIToFile(SF2_MAGIC);
		
		FileBuffer myfile = new CompositeBuffer(4);
		myfile.addToFile(header);
		myfile.addToFile(info_chunk);
		myfile.addToFile(sdta_chunk);
		myfile.addToFile(pdta_chunk);
		
		myfile.writeFile(filepath);
	}
	
	/* ----- Getters ----- */
	
	public SF2INFO getINFOSection()
	{
		return infochunk;
	}
	
	/* ----- Setters ----- */
	
	/* ----- Conversion ----- */
	
	public SimpleBank toSimpleBank()
	{
		
		//Name, version, vendor, max banks
		String name = infochunk.getFontName();
		String version = infochunk.getMajorVersion() + "." + infochunk.getMinorVersion();
		String vendor = infochunk.getAuthor();
		
		SimpleBank bank = new SimpleBank(name, version, vendor, MAX_BANKS);
		
		//Copy samples
		Map<String, Collection<SF2Sample>> linkedsamples = new HashMap<String, Collection<SF2Sample>>();
		Set<Integer> skiplist = new HashSet<Integer>();
		List<SF2Sample> slist = sdata.getSampleList();
		int sz = slist.size();
		//int i = 0; //Zero-based
		for (int i = 0; i < sz; i++)
		{
			SF2Sample s = slist.get(i);
			if (skiplist.contains(i)) continue;
			String key = s_name_root + i;
			//See if linked
			boolean linked = !(s.getHeader().getLinkType() == LinkType.MONO);
			if (!linked)
			{
				SoundSample ss = s.toBankSample(bank);
				bank.addSample(key, ss);
			}
			else
			{
				//If it IS linked, then we need to get all linked samples
				// and combine them with this one
				List<SF2Sample> list = new LinkedList<SF2Sample>();
				list.add(s);
				int j = s.getHeader().getSampleLink();
				while (j != i && j >= 0)
				{
					SF2Sample partner = slist.get(j);
					if (partner == null) break;
					skiplist.add(j);
					list.add(partner);
					j = partner.getHeader().getSampleLink();
				}
				linkedsamples.put(key, list);
			}
		}
		
		//Handle linked samples...
		Set<String> lvals = linkedsamples.keySet();
		if (lvals != null && !lvals.isEmpty())
		{
			for (String key : lvals)
			{
				Collection<SF2Sample> scoll = linkedsamples.get(key);
				SoundSample ss = SF2Sample.toBankSample(bank, scoll);
				bank.addSample(key, ss);
			}
		}
		
		
		//Dump instruments, but don't add to bank yet
		List<SF2Inst> raw_ilist = pdata.getInstruments();
		int icount = raw_ilist.size() + 1;
		List<SimpleInstrument> ilist = new ArrayList<SimpleInstrument>(icount);
		for (SF2Inst inst : raw_ilist)
		{
			SimpleInstrument sinst = inst.toInstrument();
			ilist.add(sinst);
		}
		
		//Dump presets to bank, adding instruments to presets
		List<SF2Preset> raw_plist = pdata.getPresets();
		for (SF2Preset p : raw_plist)
		{
			p.addSimplePreset(bank, ilist);
		}
		
		return bank;
	}
	
	public static SimpleBank readSF2(String filepath) throws IOException, UnsupportedFileTypeException
	{
		SF2 sf = new SF2(filepath);
		return sf.toSimpleBank();
	}
	
	public static SF2 createSF2(SimpleBank soundbank, String softwareTool, boolean bit24)
	{
		if (soundbank == null) return null;
		SF2 sf = new SF2();
		
		//Load metadata
		sf.infochunk.setName(soundbank.getName());
		sf.infochunk.setAuthor(soundbank.getVendor());
		OffsetDateTime tstamp = OffsetDateTime.now();
		String time = tstamp.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		sf.infochunk.setDateString(time);
		if (softwareTool != null && !softwareTool.isEmpty()) sf.infochunk.setToolName(softwareTool);
		
		//Load sound samples
		//Need to make a key -> index map
		sf.sdata.set24bit(bit24);
		List<String> keylist = soundbank.getAllSampleKeys();
		Map<String, Integer> indexMap = new HashMap<String, Integer>();
		int i = 0;
		for (String k : keylist)
		{
			indexMap.put(k, i);
			List<SF2Sample> s_chs = SF2Sample.convertSample(soundbank.getSample(k));
			int j = 0;
			int base = i;
			int ccount = s_chs.size();
			for (SF2Sample samp : s_chs)
			{
				if (ccount > 1)
				{
					int partner = j + 1;
					if (partner >= ccount) partner = 0;	
					samp.getHeader().setSampleLink(base + partner);
				}
				sf.sdata.addSample(samp);
				i++;	
				j++;
			}
		}
		
		//Load instruments/presets
		//Iterate through presets. When ripping instrument from preset zones, check against existing instruments to see if need to add to list.
		Map<Integer, Integer> imap = new HashMap<Integer, Integer>();
		List<SimplePreset> plist = soundbank.getAllPresets();
		List<SF2Inst> ilist = new LinkedList<SF2Inst>();
		i = 0;
		
		for (SimplePreset p : plist)
		{
			Collection<SimpleInstrument> pilist = p.getAllInstruments();
			SF2Preset preset = new SF2Preset(p.getBankIndex(), p.getPresetIndex());
			preset.setName(p.getName());
			
			//Dump and convert instruments
			for (SimpleInstrument sinst : pilist)
			{
				SF2Inst sfinst = new SF2Inst(i);
				//int sfihash = sfinst.hashCode();
				int ihash = sinst.hashCode();
				if (imap.containsKey(ihash)) continue;
				imap.put(ihash, i);
				sfinst.setName(sinst.getName());
				//Do global zone
				List<Generator> ggen = sinst.getGlobalGenerators();
				if (ggen != null && !ggen.isEmpty())
				{
					sfinst.instantiateGlobalZone();
					for (Generator g : ggen)
					{
						SF2Gen sfg = SF2GeneratorConverter.convertToSF2Gen(g);
						sfinst.getGlobalZone().addGenerator(sfg);
					}
				}
				List<Modulator> gmod = sinst.getGlobalMods();
				if (gmod != null && !gmod.isEmpty())
				{
					if (!sfinst.hasGlobalZone()) sfinst.instantiateGlobalZone();
					for (Modulator m : gmod)
					{
						SF2Mod sfm = SF2ModConverter.convertMod(m);
						sfinst.getGlobalZone().addModulator(sfm);
					}
				}
				//Do other zones - don't forget to port over sample ID and loop type!
				int rcount = sinst.countRegions();
				for (int k = 0; k < rcount; k++)
				{
					InstRegion r = sinst.getRegion(k);
					if (r == null) continue;
					SF2Zone z = SF2Zone.convertZone(r, adsrcon);
					//Determine sample ID
					String sampKey = r.getSampleKey();
					int sind = indexMap.get(sampKey);
					z.addGenerator(new SF2Gen(SF2GeneratorType.sampleID, (short)sind));
					//Determine loop type
					SoundSample sample = soundbank.getSample(sampKey);
					boolean sloop = sample.getSound().loops();
					if (!sloop) z.addGenerator(new SF2Gen(SF2GeneratorType.sampleModes, (short)0));
					else z.addGenerator(new SF2Gen(SF2GeneratorType.sampleModes, (short)1));
					
					sfinst.addZone(z);
				}
				
				//Add new instrument to list
				ilist.add(sfinst);
				i++;
			}
			
			//Convert the preset
			//Global zone...
			List<Generator> ggen = p.getGlobalGenerators();
			if (ggen != null && !ggen.isEmpty())
			{
				preset.instantiateGlobalZone();
				for (Generator g : ggen)
				{
					SF2Gen sfg = SF2GeneratorConverter.convertToSF2Gen(g);
					preset.getGlobalZone().addGenerator(sfg);
				}
			}
			List<Modulator> gmod = p.getGlobalMods();
			if (gmod != null && !gmod.isEmpty())
			{
				if (!preset.hasGlobalZone()) preset.instantiateGlobalZone();
				for (Modulator m : gmod)
				{
					SF2Mod sfm = SF2ModConverter.convertMod(m);
					preset.getGlobalZone().addModulator(sfm);
				}
			}
			//Other zones (linking instruments)...
			int rcount = p.countRegions();
			for (int k = 0; k < rcount; k++)
			{
				PresetRegion r = p.getRegion(k);
				if (r == null) continue;
				SF2Zone z = SF2Zone.convertZone(r, adsrcon);
				//Determine instrument
				SimpleInstrument si = r.getInstrument();
				int ihash = si.hashCode();
				int iind = imap.get(ihash);
				z.addGenerator(new SF2Gen(SF2GeneratorType.instrument, (short)iind));
				
				preset.addZone(z);
			}
			
			//Add preset to sf
			sf.pdata.addPreset(preset);
			
		}
		
		//Add instruments to sf
		for (SF2Inst sfi : ilist)
		{
			sf.pdata.addInstrument(sfi);
		}
		
		return sf;
	}
	
	public static void writeSF2(SimpleBank soundbank, String softwareTool, boolean bit24, String outpath) throws UnsupportedFileTypeException, IOException
	{
		SF2 sf = createSF2(soundbank, softwareTool, bit24);
		sf.writeSF2(outpath);
	}
	
	/* ----- Static ADSR Conversion ----- */
	
	public SF2ADSRConverter getADSRConverter()
	{
		if (adsrcon == null) adsrcon = new ADSRC_RetainTime();
		return adsrcon;
	}
	
	public static void setADSRConverter(SF2ADSRConverter c)
	{
		adsrcon = c;
	}
	
	/* ----- Static Conversion ----- */
	
	public static int freqToCents(int frequency)
	{
		final double refFreq = 440.0;
		final double refNote = 69.0;
		double logfac = Math.log10(2.0);
		double dfreq = (double)frequency;
		
		double temp = dfreq/refFreq;
		if (temp == 0.0) temp = Double.MIN_VALUE;
		temp = Math.log10(temp)/logfac;
		temp *= 12.0;
		temp += refNote;
		temp *= 100.0;
		
		return (int)Math.round(temp);
	}
	
	public static int millisecondsToTimecents(int ms)
	{
		double seconds = (double)ms/1000.0;
		double tc = log2(seconds);
		tc *= 1200.0;
		return (int)Math.round(tc);
	}
	
	public static int timecentsToMilliseconds(int tc)
	{
		double n = (double)tc/1200.0;
		n = Math.pow(2.0, n);
		n *= 1000.0;
		return (int)Math.round(n);
	}
	
	public static double log2(double value)
	{
		if (value == 0.0) return Double.NaN;
		if (value < 0.0) return Double.NaN;
		final double factor = Math.log10(2.0);
		return Math.log10(value)/factor;
	}

	public static int envelopeDiffToCentibels(int initialEnv, int finalEnv)
	{
		double initd = (double)initialEnv;
		if (initd == 0.0) initd = Double.MIN_NORMAL;
		double out = (double)finalEnv/initd;
		out = Math.log10(out);
		out *= 100.0;
		return (int)Math.round(out);
	}
	
	public static int envelopeDiffToCentibels(double envRatio)
	{
		double out = envRatio;
		if (out == 0.0) out = Double.MIN_NORMAL;
		out = Math.log10(out);
		out *= 100.0;
		return (int)Math.round(out);
	}
	
	public static double centibelsToEnvelopeRatio(int cB)
	{
		double bels = (double)cB/100.0;
		double ratio = Math.pow(10.0, bels);
		return ratio;
	}
	
	public static int getPan(int SF2Pan)
	{
		double n = (double)SF2Pan/500.0;
		double rawpan = n * (double)0x7FFFFFFF;
		return (int)Math.round(rawpan);
	}
	
	public static short getShortPan(int SF2Pan)
	{
		double n = (double)SF2Pan/500.0;
		double rawpan = n * (double)0x7FFF;
		return (short)Math.round(rawpan);
	}
	
	public static short convertPan(int pan)
	{
		double n = (double)pan/(double)0x7FFF;
		double rawpan = n * 500.0;
		return (short)Math.round(rawpan);
	}
	
	/* ----- Debug ----- */
	
	public void printInfo()
	{
		infochunk.printInfo();
		System.out.println();
		sdata.printInfo();
		System.out.println();
		pdata.printInfo();
		System.out.println();
	}
	
}
