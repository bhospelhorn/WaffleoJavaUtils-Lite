package waffleoRai_soundbank.sf2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class SF2PDTA {
	
	/* ----- Constants ----- */
	
	public static final String MAGIC_PDTA = "pdta";
	
	public static final int BAGSIZE = 4;
	public static final int GENSIZE = 4;
	public static final int MODSIZE = 10;
	
	/* ----- Instance Variables ----- */
	
	private List<SF2Preset> presets;
	private List<SF2Inst> instruments;
	
	/* ----- Construction ----- */
	
	public SF2PDTA()
	{
		presets = new LinkedList<SF2Preset>();
		instruments = new LinkedList<SF2Inst>();
	}
	
	public SF2PDTA(FileBuffer pdta, long stpos) throws UnsupportedFileTypeException
	{
		presets = new LinkedList<SF2Preset>();
		instruments = new LinkedList<SF2Inst>();
		parsePDTA(pdta, stpos);
	}
	
	/* ----- Parsing ----- */
	
	private class BagRecord
	{
		public int gen;
		public int mod;
	}
	
	private void parsePDTA(FileBuffer pdta, long stpos) throws UnsupportedFileTypeException
	{
		if (pdta == null) throw new FileBuffer.UnsupportedFileTypeException();
		
		//Does NOT parse SHDR!!!
		long sz = pdta.getFileSize();
		
		//phdr
		long cpos = pdta.findString(stpos, sz, SF2Preset.MAGIC_PHDR);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		int csz = pdta.intFromFile(cpos); cpos += 4;
		int nrec = (csz/SF2Preset.PHDR_SIZE) - 1; //Ignore EOP
		for (int i = 0; i < nrec; i++)
		{
			SF2Preset p = new SF2Preset(pdta, cpos);
			cpos += SF2Preset.PHDR_SIZE;
			presets.add(p);
		}
		//Get terminal record
		SF2Preset EOP = new SF2Preset(pdta, cpos);
		
		//pbag
		cpos = pdta.findString(stpos, sz, SF2Preset.MAGIC_PBAG);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		
		csz = pdta.intFromFile(cpos); cpos += 4;
		nrec = (csz/BAGSIZE); //No terminal record?
		List<BagRecord> baglist = new ArrayList<BagRecord>(nrec);
		for (int i = 0; i < nrec; i++)
		{
			BagRecord br = new BagRecord();
			br.gen = (int)pdta.shortFromFile(cpos); cpos += 2;
			br.mod = (int)pdta.shortFromFile(cpos); cpos += 2;
			baglist.add(br);
			cpos += BAGSIZE;
		}
		
		//pgen
		cpos = pdta.findString(stpos, sz, SF2Preset.MAGIC_PGEN);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		
		csz = pdta.intFromFile(cpos); cpos += 4;
		nrec = (csz/GENSIZE); //No terminal record?
		List<SF2Gen> glist = new ArrayList<SF2Gen>(nrec);
		for (int i = 0; i < nrec; i++)
		{
			int gtype = (int)pdta.shortFromFile(cpos); cpos += 2;
			short amt = pdta.shortFromFile(cpos); cpos += 2;
			SF2GeneratorType t = SF2GeneratorType.getGeneratorType(gtype);
			if (t == null) t = SF2GeneratorType.keynum;
			SF2Gen g = new SF2Gen(t, amt);
			glist.add(g);
			cpos += GENSIZE;
		}
		
		//pmod
		cpos = pdta.findString(stpos, sz, SF2Preset.MAGIC_PMOD);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		
		csz = pdta.intFromFile(cpos); cpos += 4;
		nrec = (csz/MODSIZE); //No terminal record?
		List<SF2Mod> mlist = new ArrayList<SF2Mod>(nrec);
		for (int i = 0; i < nrec; i++)
		{
			SF2Mod m = new SF2Mod(pdta, cpos);
			mlist.add(m);
			cpos += MODSIZE;
		}
		
		//Put presets together
		int np = presets.size();
		if (np < 1) throw new FileBuffer.UnsupportedFileTypeException();
		SF2Preset[] parr = new SF2Preset[np + 1];
		int l = 0;
		for (SF2Preset p : presets)
		{
			parr[l] = p;
			l++;
		}
		parr[np] = EOP;
		int gcount = glist.size();
		int mcount = mlist.size();
		for (int j = 0; j < np; j++)
		{
			//Get zone range
			SF2Preset p = parr[j];
			int stbag = p.getRead_PBAG_index();
			int edbag = parr[j+1].getRead_PBAG_index();
			
			for (int b = stbag; b < edbag; b++)
			{
				//Assemble a zone and determine if global
				BagRecord now = baglist.get(b);
				BagRecord next = null;
				try
				{
					next = baglist.get(b+1);
				}
				catch(Exception e)
				{
					next = null;
				}
				
				int stgen = now.gen;
				int stmod = now.mod;
				int edgen = gcount;
				int edmod = mcount;
				if (next != null)
				{
					edgen = next.gen;
					edmod = next.mod;
				}
				
				SF2Zone z = new SF2Zone();
				for (int k = stgen; k < edgen; k++) z.addGenerator(glist.get(k));
				for (int k = stmod; k < edmod; k++) z.addModulator(mlist.get(k));
				z.sortGenerators();
				if (b == stbag && !z.hasInstrumentGenerator())
				{
					p.instantiateGlobalZone();
					z = p.getGlobalZone();
					for (int k = stgen; k < edgen; k++) z.addGenerator(glist.get(k));
					for (int k = stmod; k < edmod; k++) z.addModulator(mlist.get(k));
				}
				else p.addZone(z);	
			}
		}
		
		//inst
		cpos = pdta.findString(stpos, sz, SF2Inst.MAGIC_INST);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		csz = pdta.intFromFile(cpos); cpos += 4;
		nrec = (csz/SF2Inst.INST_SIZE) - 1; //Ignore EOI
		for (int i = 0; i < nrec; i++)
		{
			SF2Inst inst = new SF2Inst(pdta, cpos);
			cpos += SF2Inst.INST_SIZE;
			instruments.add(inst);
		}
		//Get terminal record
		SF2Inst EOI = new SF2Inst(pdta, cpos);
		
		//ibag
		cpos = pdta.findString(stpos, sz, SF2Inst.MAGIC_IBAG);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		
		csz = pdta.intFromFile(cpos); cpos += 4;
		nrec = (csz/BAGSIZE); //No terminal record?
		baglist = new ArrayList<BagRecord>(nrec);
		for (int i = 0; i < nrec; i++)
		{
			BagRecord br = new BagRecord();
			br.gen = (int)pdta.shortFromFile(cpos); cpos += 2;
			br.mod = (int)pdta.shortFromFile(cpos); cpos += 2;
			baglist.add(br);
			cpos += BAGSIZE;
		}
		
		//igen
		cpos = pdta.findString(stpos, sz, SF2Inst.MAGIC_IGEN);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		
		csz = pdta.intFromFile(cpos); cpos += 4;
		nrec = (csz/GENSIZE); //No terminal record?
		glist = new ArrayList<SF2Gen>(nrec);
		for (int i = 0; i < nrec; i++)
		{
			int gtype = (int)pdta.shortFromFile(cpos); cpos += 2;
			short amt = pdta.shortFromFile(cpos); cpos += 2;
			SF2GeneratorType t = SF2GeneratorType.getGeneratorType(gtype);
			if (t == null) t = SF2GeneratorType.keynum;
			SF2Gen g = new SF2Gen(t, amt);
			glist.add(g);
			cpos += GENSIZE;
		}
		
		//imod
		cpos = pdta.findString(stpos, sz, SF2Inst.MAGIC_IMOD);
		if (cpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		
		csz = pdta.intFromFile(cpos); cpos += 4;
		nrec = (csz/MODSIZE); //No terminal record?
		mlist = new ArrayList<SF2Mod>(nrec);
		for (int i = 0; i < nrec; i++)
		{
			SF2Mod m = new SF2Mod(pdta, cpos);
			mlist.add(m);
			cpos += MODSIZE;
		}
		
		//Put instruments together
		int ni = instruments.size();
		if (ni < 1) throw new FileBuffer.UnsupportedFileTypeException();
		SF2Inst[] iarr = new SF2Inst[ni + 1];
		l = 0;
		for (SF2Inst inst : instruments)
		{
			iarr[l] = inst;
			l++;
		}
		iarr[ni] = EOI;
		gcount = glist.size();
		mcount = mlist.size();
		for (int j = 0; j < ni; j++)
		{
			//Get zone range
			SF2Inst inst = iarr[j];
			int stbag = inst.getRead_IBAG_index();
			int edbag = iarr[j].getRead_IBAG_index();
			
			for (int b = stbag; b < edbag; b++)
			{
				//Assemble a zone and determine if global
				BagRecord now = baglist.get(b);
				BagRecord next = null;
				try
				{
					next = baglist.get(b+1);
				}
				catch(Exception e)
				{
					next = null;
				}
				
				int stgen = now.gen;
				int stmod = now.mod;
				int edgen = gcount;
				int edmod = mcount;
				if (next != null)
				{
					edgen = next.gen;
					edmod = next.mod;
				}
				
				SF2Zone z = new SF2Zone();
				for (int k = stgen; k < edgen; k++) z.addGenerator(glist.get(k));
				for (int k = stmod; k < edmod; k++) z.addModulator(mlist.get(k));
				z.sortGenerators();
				if (b == stbag && !z.hasSampleGenerator())
				{
					inst.instantiateGlobalZone();
					z = inst.getGlobalZone();
					for (int k = stgen; k < edgen; k++) z.addGenerator(glist.get(k));
					for (int k = stmod; k < edmod; k++) z.addModulator(mlist.get(k));
				}
				else inst.addZone(z);	
			}
		}
	}
	
	/* ----- Serialization ----- */
	
	private FileBuffer serializePHDR()
	{
		int pcount = presets.size() + 1;
		FileBuffer phdr = new CompositeBuffer(pcount + 1);
		int csz = pcount * SF2Preset.PHDR_SIZE;
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Preset.MAGIC_PHDR);
		header.addToFile(csz);
		phdr.addToFile(header);
		
		int bag = 0;
		for (SF2Preset p : presets)
		{
			phdr.addToFile(p.serializePHDR_record(bag));
			bag += p.countZones();
		}
		
		//EOP
		SF2Preset EOP = new SF2Preset(0x8C,0);
		EOP.setName("EOP");
		phdr.addToFile(EOP.serializePHDR_record(bag));
		
		return phdr;
	}
	
	private FileBuffer serializePBAG() throws UnsupportedFileTypeException
	{
		int pcount = presets.size();
		FileBuffer pbag = new CompositeBuffer(pcount);
		List<FileBuffer> baglist  = new LinkedList<FileBuffer>();
		
		int gen = 0;
		int mod = 0;
		int zones = 0;
		//SF2Preset lastp = null;
		for (SF2Preset p : presets)
		{
			//lastp = p;
			baglist.add(p.serializePBAG_set(gen, mod));
			gen += p.countGenerators();
			mod += p.countModulators();
			zones += p.countZones();
		}
		//Terminal record
		baglist.add(SF2Preset.serializeEmptyPBAG(gen, mod));
		zones++;
		
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Preset.MAGIC_PBAG);
		int csz = zones * BAGSIZE;
		header.addToFile(csz);
		
		pbag.addToFile(header);
		for(FileBuffer p : baglist) pbag.addToFile(p);
		
		return pbag;
	}
	
	private FileBuffer serializePGEN()
	{
		int pcount = presets.size();
		FileBuffer pgen = new CompositeBuffer(pcount);
		List<FileBuffer> genlist  = new LinkedList<FileBuffer>();
		
		int gcount = 0;
		for (SF2Preset p : presets)
		{
			FileBuffer zgen = p.serializePGEN_set();
			if (zgen != null) genlist.add(zgen);
			gcount += p.countGenerators();
		}
		//Terminal record
		genlist.add(SF2Gen.serializeEmptyGenerator());
		gcount++;
		
		int csz = gcount * GENSIZE;
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Preset.MAGIC_PGEN);
		header.addToFile(csz);
		
		pgen.addToFile(header);
		for(FileBuffer p : genlist) pgen.addToFile(p);
		
		return pgen;
	}
	
	private FileBuffer serializePMOD()
	{
		int pcount = presets.size();
		FileBuffer pmod = new CompositeBuffer(pcount);
		List<FileBuffer> modlist  = new LinkedList<FileBuffer>();
		
		int mcount = 0;
		for (SF2Preset p : presets)
		{
			FileBuffer zmod = p.serializePMOD_set();
			if(zmod != null) modlist.add(zmod);
			mcount += p.countModulators();
		}
		//Terminal Record
		modlist.add(SF2Mod.serializeEmptyModulator());
		mcount++;
		
		int csz = mcount * MODSIZE;
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Preset.MAGIC_PMOD);
		header.addToFile(csz);
		
		pmod.addToFile(header);
		for(FileBuffer p : modlist) pmod.addToFile(p);
		
		return pmod;
	}
	
	private FileBuffer serializeINST()
	{
		int icount = instruments.size() + 1;
		FileBuffer inst = new CompositeBuffer(icount);
		int csz = icount * SF2Inst.INST_SIZE;
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Inst.MAGIC_INST);
		header.addToFile(csz);
		inst.addToFile(header);
		
		int bag = 0;
		for (SF2Inst i : instruments)
		{
			inst.addToFile(i.serializeINST_record(bag));
			bag += i.countZones();
		}
		
		//EOI
		SF2Inst EOI = new SF2Inst(0);
		EOI.setName("EOI");
		inst.addToFile(EOI.serializeINST_record(bag));
		
		return inst;
	}
	
	private FileBuffer serializeIBAG() throws UnsupportedFileTypeException
	{
		int icount = instruments.size();
		FileBuffer ibag = new CompositeBuffer(icount);
		List<FileBuffer> baglist  = new LinkedList<FileBuffer>();
		
		int gen = 0;
		int mod = 0;
		int zones = 0;
		//SF2Inst lasti = null;
		for (SF2Inst i : instruments)
		{
			//lasti = i;
			baglist.add(i.serializeIBAG_set(gen, mod));
			gen += i.countGenerators();
			mod += i.countModulators();
			zones += i.countZones();
		}
		//Terminal record
		baglist.add(SF2Inst.serializeEmptyIBAG(gen, mod));
		zones++;
		//System.err.println("Zone Count: " + zones);
		
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Inst.MAGIC_IBAG);
		int csz = zones * BAGSIZE;
		header.addToFile(csz);
		
		ibag.addToFile(header);
		for(FileBuffer i : baglist) ibag.addToFile(i);
		
		return ibag;
	}
	
	private FileBuffer serializeIMOD()
	{
		int icount = instruments.size();
		FileBuffer imod = new CompositeBuffer(icount);
		List<FileBuffer> modlist  = new LinkedList<FileBuffer>();
		
		int mcount = 0;
		for (SF2Inst i : instruments)
		{
			FileBuffer zmod = i.serializeIMOD_set();
			if(zmod != null) modlist.add(zmod);
			mcount += i.countModulators();
		}
		//Terminal Record
		modlist.add(SF2Mod.serializeEmptyModulator());
		mcount++;
		
		int csz = mcount * MODSIZE;
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Inst.MAGIC_IMOD);
		header.addToFile(csz);
		
		imod.addToFile(header);
		for(FileBuffer i : modlist) imod.addToFile(i);
		
		return imod;
	}
	
	private FileBuffer serializeIGEN()
	{
		int icount = instruments.size();
		FileBuffer igen = new CompositeBuffer(icount);
		List<FileBuffer> genlist  = new LinkedList<FileBuffer>();
		
		int gcount = 0;
		for (SF2Inst i : instruments)
		{
			FileBuffer zgen = i.serializeIGEN_set();
			if(zgen != null) genlist.add(zgen);
			gcount += i.countGenerators();
		}
		//Terminal Record
		genlist.add(SF2Gen.serializeEmptyGenerator());
		gcount++;
		
		int csz = gcount * GENSIZE;
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2Inst.MAGIC_IGEN);
		header.addToFile(csz);
		
		igen.addToFile(header);
		for(FileBuffer i : genlist) igen.addToFile(i);
		
		return igen;
	}
	
	public FileBuffer serializePDTA(SF2SDTA sdta) throws UnsupportedFileTypeException
	{
		//9 Sections + header
		FileBuffer pdta = new CompositeBuffer(10);
		
		//Sort
		sortPresets();
		
		FileBuffer phdr = this.serializePHDR();
		FileBuffer pbag = this.serializePBAG();
		FileBuffer pmod = this.serializePMOD();
		FileBuffer pgen = this.serializePGEN();
		FileBuffer inst = this.serializeINST();
		FileBuffer ibag = this.serializeIBAG();
		FileBuffer imod = this.serializeIMOD();
		FileBuffer igen = this.serializeIGEN();
		FileBuffer shdr = sdta.serializeSHDR();
		
		//Calculate size
		int size = 4;
		size += (int)phdr.getFileSize();
		size += (int)pbag.getFileSize();
		size += (int)pmod.getFileSize();
		size += (int)pgen.getFileSize();
		size += (int)inst.getFileSize();
		size += (int)ibag.getFileSize();
		size += (int)imod.getFileSize();
		size += (int)igen.getFileSize();
		size += (int)shdr.getFileSize();
		
		//Header
		FileBuffer header = new FileBuffer(12, false);
		header.printASCIIToFile(SF2.LIST_MAGIC);
		header.addToFile(size);
		header.printASCIIToFile(MAGIC_PDTA);
		
		//Add
		pdta.addToFile(header);
		pdta.addToFile(phdr);
		pdta.addToFile(pbag);
		pdta.addToFile(pmod);
		pdta.addToFile(pgen);
		pdta.addToFile(inst);
		pdta.addToFile(ibag);
		pdta.addToFile(imod);
		pdta.addToFile(igen);
		pdta.addToFile(shdr);
		
		return pdta;
	}
	
	/* ----- Getters ----- */
	
	public List<SF2Preset> getPresets()
	{
		int pcount = presets.size() + 1;
		List<SF2Preset> copy = new ArrayList<SF2Preset>(pcount);
		copy.addAll(presets);
		return copy;
	}
	
	public List<SF2Inst> getInstruments()
	{
		int icount = instruments.size() + 1;
		List<SF2Inst> copy = new ArrayList<SF2Inst>(icount);
		copy.addAll(instruments);
		return copy;
	}
	
	/* ----- Setters ----- */
	
	public void sortPresets()
	{
		Collections.sort(presets);
	}
	
	public void addPreset(SF2Preset p)
	{
		presets.add(p);
	}
	
	public void addInstrument(SF2Inst i)
	{
		instruments.add(i);
	}
	
	/* ----- Conversion ----- */
	
	/* ----- Debug ----- */
	
	public void printInfo()
	{
		System.out.println("--- SF2 PDATA ---");
		System.out.println("Presets: " + this.presets.size());
		System.out.println("Instruments: " + this.instruments.size());
		
		System.out.println("\n-- Presets --");
		int i = 0;
		for(SF2Preset p : presets)
		{
			System.out.println("-> PRESET " + i);
			System.out.println("\tName: " + p.getName());
			System.out.println("\tPatch Index: Bank " + p.getBankIndex() + ", Program " + p.getPresetIndex());
			int zcount = p.countZones();
			System.out.println("\tZones: " + zcount);
			System.out.println("\tGlobal Zone --");
			SF2Zone z = p.getGlobalZone();
			if(z != null)
			{
				System.out.println("\t\tGenerators: " + z.countGenerators());
				z.sortGenerators();
				List<SF2Gen> glist = z.getGenerators();
				for(SF2Gen g : glist) System.out.println("\t\t-> " + g.getType() + "\t0x" + String.format("%04x", g.getRawAmount())+ " (" + g.getRawAmount() + ")");
				System.out.println("\t\tModulators: " + z.countModulators());
				List<SF2Mod> mlist = z.getModulators();
				for(SF2Mod m : mlist) System.out.println("\t\t-> " + m.getSource() + "\t" + m.getDestination() + "\t" + m.getSourceAmount() + "\t" + m.getTransform() + "\t" + String.format("%04x", m.getAmount()));	
			}
			else System.out.println("\t\t(None)");
			
			//Repeat for all zones
			for(int j = 0; j < zcount; j++)
			{
				System.out.println("\tZONE " + j);
				z = p.getZone(j);
				if(z == null)
				{
					System.out.println("\t\t(Null)");
					continue;
				}
				System.out.println("\t\tGenerators: " + z.countGenerators());
				z.sortGenerators();
				List<SF2Gen> glist = z.getGenerators();
				for(SF2Gen g : glist) System.out.println("\t\t-> " + g.getType() + "\t0x" + String.format("%04x", g.getRawAmount())+ " (" + g.getRawAmount() + ")");
				System.out.println("\t\tModulators: " + z.countModulators());
				List<SF2Mod> mlist = z.getModulators();
				for(SF2Mod m : mlist) System.out.println("\t\t-> " + m.getSource() + "\t" + m.getDestination() + "\t" + m.getSourceAmount() + "\t" + m.getTransform() + "\t" + String.format("%04x", m.getAmount()));
			}
			
			i++;
		}
		
		System.out.println("\n-- Instruments --");
		i = 0;
		for(SF2Inst inst : instruments)
		{
			System.out.println("-> INSTRUMENT " + i);
			System.out.println("\tName: " + inst.getName());
			int zcount = inst.countZones();
			System.out.println("\tZones: " + zcount);
			System.out.println("\tGlobal Zone --");
			SF2Zone z = inst.getGlobalZone();
			if(z != null)
			{
				System.out.println("\t\tGenerators: " + z.countGenerators());
				z.sortGenerators();
				List<SF2Gen> glist = z.getGenerators();
				for(SF2Gen g : glist) System.out.println("\t\t-> " + g.getType() + "\t0x" + String.format("%04x", g.getRawAmount())+ " (" + g.getRawAmount() + ")");
				System.out.println("\t\tModulators: " + z.countModulators());
				List<SF2Mod> mlist = z.getModulators();
				for(SF2Mod m : mlist) System.out.println("\t\t-> " + m.getSource() + "\t" + m.getDestination() + "\t" + m.getSourceAmount() + "\t" + m.getTransform() + "\t" + String.format("%04x", m.getAmount()));	
			}
			else System.out.println("\t\t(None)");
			
			//Repeat for all zones
			for(int j = 0; j < zcount; j++)
			{
				System.out.println("\tZONE " + j);
				z = inst.getZone(j);
				if(z == null)
				{
					System.out.println("\t\t(Null)");
					continue;
				}
				System.out.println("\t\tGenerators: " + z.countGenerators());
				z.sortGenerators();
				List<SF2Gen> glist = z.getGenerators();
				for(SF2Gen g : glist) System.out.println("\t\t-> " + g.getType() + "\t0x" + String.format("%04x", g.getRawAmount()) + " (" + g.getRawAmount() + ")");
				System.out.println("\t\tModulators: " + z.countModulators());
				List<SF2Mod> mlist = z.getModulators();
				for(SF2Mod m : mlist) System.out.println("\t\t-> " + m.getSource() + "\t" + m.getDestination() + "\t" + m.getSourceAmount() + "\t" + m.getTransform() + "\t" + String.format("%04x", m.getAmount()));
			}
			
			i++;
		}
	}

}
