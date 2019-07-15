package waffleoRai_Executable.winpe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionSet {
	
	private long baseAddress;
	private List<Section> sections;
	
	public SectionSet(int secCount, long baseAddr)
	{
		baseAddress = baseAddr;
		if (secCount < 1) secCount = 1;
		sections = new ArrayList<Section>(secCount);
	}
	
	public Section getSection(int i)
	{
		return sections.get(i);
	}
	
	public void addSection(Section s)
	{
		sections.add(s);
		Collections.sort(sections);
	}
	
	public Section getSectionWithRVA(long RVA)
	{
		//long addr = RVA + baseAddress;
		long addr = RVA;
		for (Section s : sections)
		{
			if (s == null) continue;
			long vaddr = s.header.virtualAddr;
			if (addr < vaddr) continue;
			long vsize = s.header.virtualSize;
			if (addr < (vaddr + vsize)) return s;
		}
		return null;
	}
	
	public Section getSectionWithVA(long VA)
	{
		long addr = VA + baseAddress;
		for (Section s : sections)
		{
			if (s == null) continue;
			long vaddr = s.header.virtualAddr;
			if (addr < vaddr) continue;
			long vsize = s.header.virtualSize;
			if (addr < (vaddr + vsize)) return s;
		}
		return null;
	}

	public long getFileOffset(long RVA)
	{
		Section s = getSectionWithRVA(RVA);
		if (s == null) return -1;
		
		long soff = RVA - s.header.virtualAddr;
		return s.header.rawDataPtr + soff;
	}
	
}
