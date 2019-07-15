package waffleoRai_Executable.winpe;

public class SectionCharacteristics {
	
	public boolean IMAGE_SCN_TYPE_NO_PAD; //0x00000008
	
	public boolean IMAGE_SCN_CNT_CODE; //0x00000020
	public boolean IMAGE_SCN_CNT_INITIALIZED_DATA; //0x00000040
	public boolean IMAGE_SCN_CNT_UNINITIALIZED_DATA; //0x00000080
	
	public boolean IMAGE_SCN_LNK_INFO; //0x00000200
	public boolean IMAGE_SCN_LNK_REMOVE; //0x00000800
	
	public boolean IMAGE_SCN_LNK_COMDAT; //0x00001000
	public boolean IMAGE_SCN_GPREL; //0x00008000
	
	public int align;
	
	public boolean IMAGE_SCN_LNK_NRELOC_OVFL; //0x01000000
	public boolean IMAGE_SCN_MEM_DISCARDABLE; //0x02000000
	public boolean IMAGE_SCN_MEM_NOT_CACHED; //0x04000000
	public boolean IMAGE_SCN_MEM_NOT_PAGED; //0x08000000
	
	public boolean IMAGE_SCN_MEM_SHARED; //0x10000000
	public boolean IMAGE_SCN_MEM_EXECUTE; //0x20000000
	public boolean IMAGE_SCN_MEM_READ; //0x40000000
	public boolean IMAGE_SCN_MEM_WRITE; //0x80000000
	
	public SectionCharacteristics(int rawflags)
	{
		IMAGE_SCN_TYPE_NO_PAD = (rawflags & 0x00000008) != 0;
		
		IMAGE_SCN_CNT_CODE = (rawflags & 0x00000020) != 0;
		IMAGE_SCN_CNT_INITIALIZED_DATA = (rawflags & 0x00000040) != 0;
		IMAGE_SCN_CNT_UNINITIALIZED_DATA = (rawflags & 0x00000080) != 0;
		
		IMAGE_SCN_LNK_INFO = (rawflags & 0x00000200) != 0;
		IMAGE_SCN_LNK_REMOVE = (rawflags & 0x00000800) != 0;
		
		IMAGE_SCN_LNK_COMDAT = (rawflags & 0x00001000) != 0;
		IMAGE_SCN_GPREL = (rawflags & 0x00008000) != 0;
		
		int a = (rawflags >>> 20) & 0x0F;
		align = 0x00000001 << (a-1);
		
		IMAGE_SCN_LNK_NRELOC_OVFL = (rawflags & 0x01000000) != 0;
		IMAGE_SCN_MEM_DISCARDABLE = (rawflags & 0x02000000) != 0;
		IMAGE_SCN_MEM_NOT_CACHED = (rawflags & 0x04000000) != 0;
		IMAGE_SCN_MEM_NOT_PAGED = (rawflags & 0x08000000) != 0;
		
		IMAGE_SCN_MEM_SHARED = (rawflags & 0x10000000) != 0;
		IMAGE_SCN_MEM_EXECUTE = (rawflags & 0x20000000) != 0;
		IMAGE_SCN_MEM_READ = (rawflags & 0x40000000) != 0;
		IMAGE_SCN_MEM_WRITE = (rawflags & 0x80000000) != 0;
	}
	
	public void printInfo()
	{
		System.err.println("Section Padded: " + !IMAGE_SCN_TYPE_NO_PAD);
		System.err.println("Contains Code: " + IMAGE_SCN_CNT_CODE);
		System.err.println("Contains Initialized Data: " + IMAGE_SCN_CNT_INITIALIZED_DATA);
		System.err.println("Contains Uninitialized Data: " + IMAGE_SCN_CNT_UNINITIALIZED_DATA);
		System.err.println("Contains Comments: " + IMAGE_SCN_LNK_INFO);
		System.err.println("Remove From Image: " + IMAGE_SCN_LNK_REMOVE);
		System.err.println("Contains COMDAT Data: " + IMAGE_SCN_LNK_COMDAT);
		System.err.println("GP Relative: " + IMAGE_SCN_GPREL);
		System.err.println("Data Align: " + align + " byte boundary");
		System.err.println("Extended Relocations: " + IMAGE_SCN_LNK_NRELOC_OVFL);
		System.err.println("Discardable: " + IMAGE_SCN_MEM_DISCARDABLE);
		System.err.println("Cacheable: " + !IMAGE_SCN_MEM_NOT_CACHED);
		System.err.println("Pageable: " + !IMAGE_SCN_MEM_NOT_PAGED);
		System.err.println("Memory Share: " + IMAGE_SCN_MEM_SHARED);
		System.err.println("Executable: " + IMAGE_SCN_MEM_EXECUTE);
		System.err.println("Readable: " + IMAGE_SCN_MEM_READ);
		System.err.println("Writable: " + IMAGE_SCN_MEM_WRITE);
	}


}
