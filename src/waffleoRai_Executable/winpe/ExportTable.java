package waffleoRai_Executable.winpe;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ExportTable {
	
	/* --- Constants --- */
	
	/* --- Static Variables --- */
	
	private static boolean sortByRVA = false;
	
	/* --- Instance Variables --- */
	
	private String name;
	
	private OffsetDateTime timestamp;
	
	private int ver_major;
	private int ver_minor;
	
	private int ordinal_base;
	
	private ExportEntry[] table;
	
	/* --- Construction --- */
	
	public ExportTable(String n, int nEntries, int obase)
	{
		name = n;
		if (name == null)
		{
			Random r = new Random();
			name = Long.toHexString(r.nextLong()) + ".dll";
		}
		ordinal_base = obase;
		table = new ExportEntry[nEntries];
	}
	
	/* --- Inner Classes --- */
	
	public static class ExportEntry implements Comparable<ExportEntry>
	{
		private String name;
		private long RVA;
		//private String forwarder;
		private int ordinal;
		
		public ExportEntry(String exportName, long rva, int ord)
		{
			name = exportName;
			//forwarder = forwardName;
			RVA = rva;
			ordinal = ord;
		}
		
		public String getExportName()
		{
			return name;
		}
		
		public long getRVA()
		{
			return RVA;
		}
		
		public int getOrdinal()
		{
			return ordinal;
		}

		@Override
		public int compareTo(ExportEntry o) 
		{
			if (o == null) return 1;
			if (this == o) return 0;
			if (ExportTable.sortByRVA){
				if (this.RVA > o.RVA) return 1;
				else if (this.RVA == o.RVA) return 0;
				else return -1;
			}
			return this.ordinal - o.ordinal;
		}
		
		public boolean equals(Object o)
		{
			if (o == null) return false;
			if (this == o) return true;
			if (!(o instanceof ExportEntry)) return false;
			ExportEntry e = (ExportEntry)o;
			if (this.ordinal != e.ordinal) return false;
			if (this.RVA != e.RVA) return false;
			if (!this.name.equals(e.name)) return false;
			//if (!this.forwarder.equals(e.forwarder)) return false;
			return true;
		}
		
		public int hashCode()
		{
			return name.hashCode() ^ (int)RVA;
		}
		
		public String toString()
		{
			return name;
		}
		
	}
	
	/* --- Getters --- */
	
	public String getName()
	{
		return name;
	}
	
	public OffsetDateTime getTimestamp()
	{
		return timestamp;
	}
	
	public int getMajorVersion()
	{
		return this.ver_major;
	}
	
	public int getMinorVersion()
	{
		return this.ver_minor;
	}
	
	public int getOrdinalBase()
	{
		return ordinal_base;
	}
	
	public List<ExportEntry> getSortedEntries()
	{
		int sz = 1;
		if (table != null) sz += table.length;
		List<ExportEntry> list = new ArrayList<ExportEntry>(sz);
		for (int i = 0; i < table.length; i++)
		{
			if (table[i] != null) list.add(table[i]);
		}
		Collections.sort(list);
		return list;
	}
	
	public ExportEntry getEntry(int ordinal)
	{
		int ind = ordinal - (ordinal_base-1);
		if (ind < 0) return null;
		if (ind >= table.length) return null;
		return table[ind];
	}
	
	/* --- Setters --- */
	
	public void setName(String s)
	{
		if (s == null || s.isEmpty()) return;
		name = s;
	}
	
	public void setVersion(int major, int minor)
	{
		ver_major = major;
		ver_minor = minor;
	}
	
	public void setTimestamp(int secondsSinceEpoch)
	{
		timestamp = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		timestamp = timestamp.plusSeconds(secondsSinceEpoch);
	}

	public void setEntry(int ordinal, String exname, long RVA)
	{
		int ind = ordinal - (ordinal_base-1);
		if (ind < 0) throw new IndexOutOfBoundsException();
		if (ind >= table.length) throw new IndexOutOfBoundsException();
		ExportEntry e = new ExportEntry(exname, RVA, ordinal);
		table[ind] = e;
	}

	public static void setSortByRVA(boolean b)
	{
		sortByRVA = b;
	}
	
}
