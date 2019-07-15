package waffleoRai_Executable;

import java.util.List;

import waffleoRai_Executable.Win32PE.Machine;
import waffleoRai_Executable.winpe.ExportTable;
import waffleoRai_Executable.winpe.Section;
import waffleoRai_Executable.winpe.SectionSet;
import waffleoRai_Executable.winpe.ExportTable.ExportEntry;

public class Winexe {
	
	/* --- Constants --- */
	
	/* --- Instance Variables --- */
	
	private Machine system;
	private int[] rva_tbl;
	private int[] sz_tbl;
	
	private SectionSet sections;
	
	private ExportTable exportTable;
	
	/* --- Construction --- */
	
	protected Winexe(Machine arch, int secCount, long baseAddress)
	{
		system = arch;
		//rva_tbl = new int[ddEntries];
		//sz_tbl = new int[ddEntries];
		sections = new SectionSet(secCount, baseAddress);
	}
	
	/* --- Inner Classes --- */
	

	/* --- Getters --- */
	
	public long getDataTableRVA(int table)
	{
		return Integer.toUnsignedLong(rva_tbl[table]);
	}
	
	public long getDataTableSize(int table)
	{
		return Integer.toUnsignedLong(sz_tbl[table]);
	}
	
	public long getFileOffset(long RVA)
	{
		return sections.getFileOffset(RVA);
	}
	
	public byte getByte(long RVA)
	{
		Section s = sections.getSectionWithRVA(RVA);
		if (s == null) throw new IndexOutOfBoundsException();
		long soff = RVA - s.header.virtualAddr;
		return s.data.getByte(soff);
	}
	
	public short getShort(long RVA, boolean bigendian)
	{
		Section s = sections.getSectionWithRVA(RVA);
		if (s == null) throw new IndexOutOfBoundsException();
		long soff = RVA - s.header.virtualAddr;
		return s.data.shortFromFile(soff);
	}
	
	public int getInt(long RVA, boolean bigendian)
	{
		Section s = sections.getSectionWithRVA(RVA);
		if (s == null) throw new IndexOutOfBoundsException();
		long soff = RVA - s.header.virtualAddr;
		return s.data.intFromFile(soff);
	}
	
	public long getLong(long RVA, boolean bigendian)
	{
		Section s = sections.getSectionWithRVA(RVA);
		if (s == null) throw new IndexOutOfBoundsException();
		long soff = RVA - s.header.virtualAddr;
		return s.data.longFromFile(soff);
	}
	
	public String getString(long RVA)
	{
		Section s = sections.getSectionWithRVA(RVA);
		if (s == null) throw new IndexOutOfBoundsException();
		long soff = RVA - s.header.virtualAddr;
		return s.data.getASCII_string(soff, '\0');
	}
	
	public String getString(String encoding, long RVA)
	{
		Section s = sections.getSectionWithRVA(RVA);
		if (s == null) throw new IndexOutOfBoundsException();
		long soff = RVA - s.header.virtualAddr;
		return s.data.readEncoded_string(encoding, soff, '\0');
	}
	
	public Machine getIntendedArchitecture()
	{
		return system;
	}
	
	public ExportTable getExportTable()
	{
		if (exportTable == null)
		{
			exportTable = Win32PE.readExportTable(this, false);
		}
		return exportTable;
	}

	/* --- Setters --- */
	
	protected void loadDataDirTable(int[] addrs, int[] szs)
	{
		rva_tbl = new int[addrs.length];
		for (int i = 0; i < addrs.length; i++) rva_tbl[i] = addrs[i];
		sz_tbl = new int[szs.length];
		for (int i = 0; i < szs.length; i++) sz_tbl[i] = szs[i];
	}
	
	protected void addSection(Section s)
	{
		s.sizeVirtual();
		sections.addSection(s);
	}
	
	/* --- Info --- */
	
	public void printExportTableSTDOUT()
	{
		ExportTable et = this.getExportTable();
		if (et == null)
		{
			System.out.println("No export table!");
			return;
		}
		List<ExportEntry> entries = et.getSortedEntries();
		System.out.println("--------- Export Table ---------");
		System.out.println("ORD\tNAME\tRVA\tFILEOFFSET");
		for (ExportEntry e : entries)
		{
			if (e == null) continue;
			System.out.print(e.getOrdinal() + "\t");
			System.out.print(e.getExportName() + "\t");
			long rva = e.getRVA();
			System.out.print(String.format("0x%08X", rva) + "\t");
			long off = this.getFileOffset(rva);
			System.out.print(String.format("0x%08X", off) + "\n");
			//System.out.print(e.getForwarderName() + "\n");
		}
	}
}
