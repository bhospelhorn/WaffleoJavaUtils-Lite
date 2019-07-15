package waffleoRai_Executable;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

import waffleoRai_Executable.winpe.ExportTable;
import waffleoRai_Executable.winpe.Section;
import waffleoRai_Executable.winpe.SectionHeader;

public class Win32PE {
	
	//https://en.wikibooks.org/wiki/X86_Disassembly/Windows_Executable_Files
	//https://blog.kowalczyk.info/articles/pefileformat.html
	//https://docs.microsoft.com/en-us/windows/desktop/debug/pe-format

	/* --- Constants --- */
	
	public static final String DOS_MAGIC = "MZ";
	
	public static final byte[] PE_MAGIC = {0x50, 0x45, 0x00, 0x00};
	public static final byte[] PE_OP_MAGIC_32 = {0x0b, 0x01}; //LE
	public static final byte[] PE_OP_MAGIC_64 = {0x0b, 0x02}; //LE
	
	public static final int ENTRY_SIZE = 8;
	
	public static final int TBL_IDX_EXPORT = 0;
	public static final int TBL_IDX_IMPORT = 1;
	public static final int TBL_IDX_RESOURCE = 2;
	public static final int TBL_IDX_EXCEPTION = 3;
	
	public static final int TBL_IDX_CERTIFICATE = 4;
	public static final int TBL_IDX_BASERELOCATION = 5;
	public static final int TBL_IDX_DEBUG = 6;
	//public static final int TBL_IDX_ARCHITECTURE = 7;
	
	public static final int TBL_IDX_GLOBALPTR = 8;
	public static final int TBL_IDX_TLS = 9;
	public static final int TBL_IDX_LOADCONFIG = 10;
	public static final int TBL_IDX_BOUNDIMPORT = 11;
	
	public static final int TBL_IDX_IAT = 12;
	public static final int TBL_IDX_DELAYIMPORT = 13;
	public static final int TBL_IDX_CLRRUNTIMEHEADER = 14;
	
	/* --- Instance Variables --- */
	
	private MSDOS_Header header_msdos;
	private MSDOS_Stub msdos_stub;
	
	private PE_Header header_signature;
	private OPHeader_COFF header_coff;
	private OPHeader_WIN header_win;
	
	private DD_Table table_datadir;
	private Section[] sections;
	
	/* --- Construction --- */
	
	public Win32PE(String filepath, boolean verbose) throws IOException, UnsupportedFileTypeException
	{
		FileBuffer myfile = FileBuffer.createBuffer(filepath, false);
		parsePE(myfile, verbose);
	}
	
	/* --- Inner Classes --- */
	
	private class MSDOS_Header
	{
		public static final int SERIAL_SIZE = 0x40;
		
		public int lastsize;
		public int nblocks;
		public int nreloc;
		public int hdrsize;
		public int minalloc;
		public int maxalloc;
		public int ss_ptr;
		public int sp_ptr;
		public short checksum;
		public int ip_ptr;
		public int cs_ptr;
		public int relocpos;
		public int noverlay;
		public int oemid;
		public int oeminfo;
		
		public long e_lfanew; //PE offset from file start
		
		public MSDOS_Header(FileBuffer myfile) throws UnsupportedFileTypeException
		{
			boolean oldendian = myfile.isBigEndian();
			myfile.setEndian(false);
			long cpos = myfile.findString(0, 0x10, DOS_MAGIC);
			if (cpos != 0) throw new FileBuffer.UnsupportedFileTypeException();
			cpos += 2;
			lastsize = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			nblocks = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			nreloc = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			hdrsize = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			minalloc = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			maxalloc = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			ss_ptr = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			sp_ptr = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			checksum = myfile.shortFromFile(cpos); cpos += 2;
			ip_ptr = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			cs_ptr = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			relocpos = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			noverlay = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			cpos += 2*4; //Reserved
			oemid = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			oeminfo = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			cpos += 2*10; //Reserved
			e_lfanew = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4; //PE start offset
			myfile.setEndian(oldendian);
		}
		
		public void printInfo()
		{
			System.err.println("Bytes on last page of file: " + lastsize + String.format(" (0x%04X)", lastsize));
			System.err.println("Pages in file: " + nblocks + String.format(" (0x%04X)", nblocks));
			System.err.println("Number of relocations: " + nreloc + String.format(" (0x%04X)", nreloc));
			System.err.println("Size of header in paragraphs: " + hdrsize + String.format(" (0x%04X)", hdrsize));
			System.err.println("Minimum extra paragraphs: " + minalloc + String.format(" (0x%04X)", minalloc));
			System.err.println("Maximum extra paragraphs: " + maxalloc + String.format(" (0x%04X)", maxalloc));
			System.err.println("Initial SS: " + String.format("0x%04X", ss_ptr));
			System.err.println("Initial SP: " + String.format("0x%04X", sp_ptr));
			System.err.println("Checksum: " + String.format("0x%04X", checksum));
			System.err.println("Initial IP: " + String.format("0x%04X", ip_ptr));
			System.err.println("Initial CS: " + String.format("0x%04X", cs_ptr));
			System.err.println("File Address of Relocation Table: " + String.format("0x%04X", relocpos));
			System.err.println("Overlay number: " + noverlay + String.format(" (0x%04X)", noverlay));
			System.err.println("OEM Identifier: " + oemid + String.format(" (0x%04X)", oemid));
			System.err.println("OEM Information: " + oeminfo + String.format(" (0x%04X)", oeminfo));
			System.err.println("PE Header Offset: " + String.format(" (0x%08X)", e_lfanew));
		}
	}

	private class MSDOS_Stub
	{
		public byte[] stub;
		
		public MSDOS_Stub(FileBuffer myfile, long stpos, int len) throws UnsupportedFileTypeException
		{
			if (len < 1) throw new FileBuffer.UnsupportedFileTypeException();
			stub = new byte[len];
			for (int i = 0; i < len; i++)
			{
				stub[i] = myfile.getByte(stpos + i);
			}
		}
		
		public void printInfo()
		{
			System.err.println("DOS Stub");
			System.err.println("Length: " + String.format("0x%04X", stub.length) + " (" + stub.length + " bytes)");
			for (int i = 0; i < stub.length; i++)
			{
				System.err.print(String.format("%02X ", stub[i]));
				if (i % 16 == 15) System.err.print("\n");
			}
			System.err.print("\n");
		}
	}
	
	public static enum Machine
	{
		INTEL_I386(0x14C, "Intel i386"),
		INTEL_I860(0x14D, "Intel i860"),
		INTEL_X64(0x8664, "Intel x64"),
		MIPS_R3000(0x162, "MIPS R3000"),
		MIPS_R10000(0x168, "MIPS R10000"),
		MIPS_LE_WCIV2(0x168, "MIPS little endian WCI v2"),
		ALPHA_AXP_OLD(0x183, "Alpha AXP (Old)"),
		ALPHA_AXP(0x184, "Alpha AXP"),
		HITACHI_SH3(0x1a2, "Hitachi SH3"),
		HITACHI_SH3_DSP(0x1a3, "Hitachi SH3 DSP"),
		HITACHI_SH4(0x1a6, "Hitachi SH4"),
		HITACHI_SH5(0x1a8, "Hitachi SH5"),
		ARM_LE(0x1c0, "ARM (Little Endian)"),
		THUMB(0x1c2, "Thumb"),
		ARM_V7(0x1c4, "ARM v7"),
		MATSUSHITA_AM33(0x1d3, "Matsushita AM33"),
		POWERPC_LE(0x1f0, "PowerPC (Little Endian)"),
		POWERPC_FP(0x1f1, "PowerPC (Floating Point)"),
		INTEL_IA64(0x200, "Intel IA64"),
		MIPS16(0x266, "MIPS 16"),
		MOTOROLA_68000(0x268, "Motorola 68000 Series"),
		ALPHA_AXP_64(0x284, "Alpha AXP 64-bit"),
		MIPS_FPU(0x366, "MIPS w/ FPU"),
		MIPS16_FPU(0x466, "MIPS16 w/ FPU"),
		EFI_BC(0xebc, "EFI Byte Code"),
		//AMD64(0x8664, "AMD64"),
		MITSUBISHI_M32R_LE(0x9041, "Mitsubishi M32R (Little Endian)"),
		ARM64_LE(0xaa64, "ARM64 (Little Endian)"),
		MSIL(0xc0ee, "MSIL"),;
		
		private int n;
		private String name;
		
		private Machine(int number, String str)
		{
			n = number;
			name = str;
		}
		
		public short getNumber()
		{
			return (short)n;
		}
		
		public String toString()
		{
			return name;
		}
	
		private static Map<Integer, Machine> codemap;
		
		private static void populateMap()
		{
			codemap = new HashMap<Integer, Machine>();
			Machine[] all = Machine.values();
			for (Machine m : all)
			{
				codemap.put(m.n, m);
			}
		}
		
		public static Machine getMachine(int code)
		{
			if (codemap == null) populateMap();
			return codemap.get(code);
		}
		
	}
	
	private class PE_Header
	{
		public static final int SERIAL_SIZE = 24;
		
		public Machine machine;
		public int nSections;
		//public int rawtimestamp;
		public OffsetDateTime timestamp;
		public long symbols_ptr;
		public int nsymbols;
		public int opheader_size;
		//public short characteristics;
		
		//Actual characteristics
		public boolean IMAGE_FILE_RELOCS_STRIPPED; //0x0001
		public boolean IMAGE_FILE_EXECUTABLE_IMAGE; //0x0002
		public boolean IMAGE_FILE_LINE_NUMS_STRIPPED; //0x0004
		public boolean IMAGE_FILE_LOCAL_SYMS_STRIPPED; //0x0008
		
		public boolean IMAGE_FILE_AGGRESSIVE_WS_TRIM; //0x0010
		public boolean IMAGE_FILE_LARGE_ADDRESS_AWARE; //0x0020
		public boolean IMAGE_FILE_BYTES_REVERSED_LO; //0x0080
		
		public boolean IMAGE_FILE_32BIT_MACHINE; //0x0100
		public boolean IMAGE_FILE_DEBUG_STRIPPED; //0x0200
		public boolean IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP; //0x0400
		public boolean IMAGE_FILE_NET_RUN_FROM_SWAP; //0x0800
		
		public boolean IMAGE_FILE_SYSTEM; //0x1000
		public boolean IMAGE_FILE_DLL; //0x2000
		public boolean IMAGE_FILE_UP_SYSTEM_ONLY; //0x4000
		public boolean IMAGE_FILE_BYTES_REVERSED_HI; //0x8000
		
		public PE_Header(FileBuffer myfile, long stpos) throws UnsupportedFileTypeException
		{
			myfile.setEndian(false);
			long cpos = myfile.findString(stpos, stpos+0x10, PE_MAGIC);
			if (cpos != stpos) throw new FileBuffer.UnsupportedFileTypeException();
			cpos += 4;
			
			int mcode = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			machine = Machine.getMachine(mcode);
			nSections = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			int rawtimestamp = myfile.intFromFile(cpos); cpos += 4;
			timestamp = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
			timestamp = timestamp.plusSeconds(rawtimestamp);
			symbols_ptr = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			nsymbols = myfile.intFromFile(cpos); cpos += 4;
			opheader_size = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			
			int characteristics = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			IMAGE_FILE_RELOCS_STRIPPED = (characteristics & 0x0001) != 0;
			IMAGE_FILE_EXECUTABLE_IMAGE = (characteristics & 0x0002) != 0;
			IMAGE_FILE_LINE_NUMS_STRIPPED = (characteristics & 0x0004) != 0;
			IMAGE_FILE_LOCAL_SYMS_STRIPPED = (characteristics & 0x0008) != 0;
			
			IMAGE_FILE_AGGRESSIVE_WS_TRIM = (characteristics & 0x0010) != 0;
			IMAGE_FILE_LARGE_ADDRESS_AWARE = (characteristics & 0x0020) != 0;
			IMAGE_FILE_BYTES_REVERSED_LO = (characteristics & 0x0080) != 0;
			
			IMAGE_FILE_32BIT_MACHINE = (characteristics & 0x0100) != 0;
			IMAGE_FILE_DEBUG_STRIPPED = (characteristics & 0x0200) != 0;
			IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP = (characteristics & 0x0400) != 0;
			IMAGE_FILE_NET_RUN_FROM_SWAP = (characteristics & 0x0800) != 0;
			
			IMAGE_FILE_SYSTEM = (characteristics & 0x1000) != 0;
			IMAGE_FILE_DLL = (characteristics & 0x2000) != 0;
			IMAGE_FILE_UP_SYSTEM_ONLY = (characteristics & 0x4000) != 0;
			IMAGE_FILE_BYTES_REVERSED_HI = (characteristics & 0x8000) != 0;
			
		}
		
		public void printInfo()
		{
			System.err.println("Machine: " + machine.toString());
			System.err.println("Number of Sections: " + nSections);
			System.err.println("Timestamp: " + timestamp.format(DateTimeFormatter.RFC_1123_DATE_TIME));
			System.err.println("Symbol Table Pointer: " + String.format("0x%08X", symbols_ptr));
			System.err.println("Number of Symbols: " + nsymbols);
			System.err.println("Optional Header Size: " + String.format("0x%04X", opheader_size) + " (" + opheader_size + " bytes)");
			//System.err.println("Raw Characteristics: " + String.format("0x%04X", characteristics));
			System.err.println("Relocations Stripped: " + IMAGE_FILE_RELOCS_STRIPPED);
			System.err.println("Is Executable: " + IMAGE_FILE_EXECUTABLE_IMAGE);
			System.err.println("Line Numbers Stripped [-Deprecated Flag-]: " + IMAGE_FILE_LINE_NUMS_STRIPPED);
			System.err.println("Symbol Table Stripped [-Deprecated Flag-]: " + IMAGE_FILE_LOCAL_SYMS_STRIPPED);
			System.err.println("Aggressively Trim Working Set [-Deprecated Flag-]: " + IMAGE_FILE_AGGRESSIVE_WS_TRIM);
			System.err.println("Large Address Aware (Can handle address over 2GB): " + IMAGE_FILE_LARGE_ADDRESS_AWARE);
			System.err.println("Little Endian [-Deprecated Flag-]: " + IMAGE_FILE_BYTES_REVERSED_LO);
			System.err.println("Machine Uses 32-bit Word Architecture: " + IMAGE_FILE_32BIT_MACHINE);
			System.err.println("Debugging Info Stripped: " + IMAGE_FILE_DEBUG_STRIPPED);
			System.err.println("Load and Copy to Swap File if Removable: " + IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP);
			System.err.println("Load and Copy to Swap File if on Network: " + IMAGE_FILE_NET_RUN_FROM_SWAP);
			System.err.println("System File: " + IMAGE_FILE_SYSTEM);
			System.err.println("Is DLL: " + IMAGE_FILE_DLL);
			System.err.println("Uniprocessor Only: " + IMAGE_FILE_UP_SYSTEM_ONLY);
			System.err.println("Big Endian [-Deprecated Flag-]: " + IMAGE_FILE_BYTES_REVERSED_HI);	
		}
	}
	
	private class OPHeader_COFF
	{
		public static final int SERIAL_SIZE_32 = 28;
		public static final int SERIAL_SIZE_64 = 24;
		
		public boolean PE32PLUS; //Tick this if 64 bit
		
		public int majorLinkerVer; //1
		public int minorLinkerVer; //1
 		public long sizeOfCode; //4
		public long sizeOfInitializedData; //4
		public long sizeOfUninitializedData; //4
		public long addressOfEntryPoint; //4
		public long baseOfCode; //4
		public long baseOfData; //32-bit only [4]
		
		public OPHeader_COFF(FileBuffer myfile, long stpos) throws UnsupportedFileTypeException
		{
			PE32PLUS = false;
			if (myfile == null) throw new FileBuffer.UnsupportedFileTypeException();
			long cpos = myfile.findString(stpos, stpos + 0x10, PE_OP_MAGIC_32);
			if (cpos != stpos){
				cpos = myfile.findString(stpos, stpos + 0x10, PE_OP_MAGIC_64);
				if (cpos == stpos) PE32PLUS = true;
			}
			if (cpos != stpos){
				System.err.println("Error Parsing COFF Header!");
				System.err.println("stpos = " + String.format("0x%08X", stpos));
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			cpos += 2;
			
			majorLinkerVer = Byte.toUnsignedInt(myfile.getByte(cpos)); cpos++;
			minorLinkerVer = Byte.toUnsignedInt(myfile.getByte(cpos)); cpos++;
			sizeOfCode = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			sizeOfInitializedData = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			sizeOfUninitializedData = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			addressOfEntryPoint = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			baseOfCode = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			if (!PE32PLUS) baseOfData = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
		}
		
		public void printInfo()
		{
			if(PE32PLUS) System.err.println("64-Bit PE");
			else  System.err.println("32-Bit PE");
			System.err.println("Linker Version: " + majorLinkerVer + "." + minorLinkerVer);
			System.err.println("Size of Text (Code): " + String.format("0x%08X", sizeOfCode) + " (" + sizeOfCode + " bytes)");
			System.err.println("Size of Initialized Data: " + String.format("0x%08X", sizeOfInitializedData) + " (" + sizeOfInitializedData + " bytes)");
			System.err.println("Size of Uninitialized Data: " + String.format("0x%08X", sizeOfUninitializedData) + " (" + sizeOfUninitializedData + " bytes)");
			System.err.println("Entry Point Address: " + String.format("0x%08X", addressOfEntryPoint));
			System.err.println("Text Base Address: " + String.format("0x%08X",  baseOfCode));
			if(!PE32PLUS) System.err.println("Data Base Address: " + String.format("0x%08X", baseOfData));
		}
		
	}
	
	public static enum Subsystem
	{
		IMAGE_SUBSYSTEM_UNKNOWN(0,"Subsystem Unknown"),
		IMAGE_SUBSYSTEM_NATIVE(1,"Device Drivers & Native Windows Processes"),
		IMAGE_SUBSYSTEM_WINDOWS_GUI(2,"Windows GUI Subsystem"),
		IMAGE_SUBSYSTEM_WINDOWS_CUI(3,"Windows Character Subsystem"),
		IMAGE_SUBSYSTEM_OS2_CUI(5,"OS/2 Character Subsystem"),
		IMAGE_SUBSYSTEM_POSIX_CUI(7,"Posix Character Subsystem"),
		IMAGE_SUBSYSTEM_NATIVE_WINDOWS(8,"Native Win9x Driver"),
		IMAGE_SUBSYSTEM_WINDOWS_CE_GUI(9,"Windows CE"),
		IMAGE_SUBSYSTEM_EFI_APPLICATION(10,"EFI Application"),
		IMAGE_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER(11,"EFI Driver with Boot Services"),
		IMAGE_SUBSYSTEM_EFI_RUNTIME_DRIVER(12,"EFI Driver with Runtime Services"),
		IMAGE_SUBSYSTEM_EFI_ROM(13,"EFI ROM Image"),
		IMAGE_SUBSYSTEM_XBOX(14,"XBOX"),
		IMAGE_SUBSYSTEM_WINDOWS_BOOT_APPLICATION(16,"Windows Boot Application"),;
		
		private int n;
		private String name;
		
		private Subsystem(int number, String str)
		{
			n = number;
			name = str;
		}
		
		public short getNumber()
		{
			return (short)n;
		}
		
		public String toString()
		{
			return name;
		}
	
		private static Map<Integer, Subsystem> codemap;
		
		private static void populateMap()
		{
			codemap = new HashMap<Integer, Subsystem>();
			Subsystem[] all = Subsystem.values();
			for (Subsystem m : all)
			{
				codemap.put(m.n, m);
			}
		}
		
		public static Subsystem getSubsystem(int code)
		{
			if (codemap == null) populateMap();
			return codemap.get(code);
		}
		
	}
	
	private class DLL_Characteristics
	{
		public boolean IMAGE_DLLCHARACTERISTICS_HIGH_ENTROPY_VA; //0x0020
		public boolean IMAGE_DLLCHARACTERISTICS_DYNAMIC_BASE; //0x0040
		public boolean IMAGE_DLLCHARACTERISTICS_FORCE_INTEGRITY; //0x0080
		
		public boolean IMAGE_DLLCHARACTERISTICS_NX_COMPAT; //0x0100
		public boolean IMAGE_DLLCHARACTERISTICS_NO_ISOLATION; //0x0200
		public boolean IMAGE_DLLCHARACTERISTICS_NO_SEH; //0x0400
		public boolean IMAGE_DLLCHARACTERISTICS_NO_BIND; //0x0800
		
		public boolean IMAGE_DLLCHARACTERISTICS_APPCONTAINER; //0x1000
		public boolean IMAGE_DLLCHARACTERISTICS_WDM_DRIVER; //0x2000
		public boolean IMAGE_DLLCHARACTERISTICS_GUARD_CF; //0x4000
		public boolean IMAGE_DLLCHARACTERISTICS_TERMINAL_SERVER_AWARE; //0x8000
	}
	
	private class OPHeader_WIN
	{
		public static final int SERIAL_SIZE_32 = 68;
		public static final int SERIAL_SIZE_64 = 88;
		
		public boolean PE32PLUS; //Tick this if 64 bit
		
		public long imageBase;
		public long sectionAlignment;
		public long fileAlignment;
		
		public int majorOSVer;
		public int minorOSVer;
		public int majorImageVer;
		public int minorImageVer;
		public int majorSubsystemVer;
		public int minorSubsystemVer;
		
		public long sizeOfImage;
		public long sizeOfHeaders;
		public int checksum;
		
		public Subsystem subsystem;
		public DLL_Characteristics dll_char;
		
		public long sizeOfStackReserve;
		public long sizeOfStackCommit;
		public long sizeOfHeapReserve;
		public long sizeOfHeapCommit;
		
		public int numberOfRvaAndSizes;
		
		public OPHeader_WIN(FileBuffer myfile, long stpos, boolean as64bit) throws UnsupportedFileTypeException
		{
			if (myfile == null) throw new FileBuffer.UnsupportedFileTypeException();
			if (stpos < 0) throw new FileBuffer.UnsupportedFileTypeException();
			long cpos = stpos;
			PE32PLUS = as64bit;
			
			if (as64bit)
			{
				imageBase = myfile.longFromFile(cpos); 
				cpos += 8;
			}
			else
			{
				imageBase = Integer.toUnsignedLong(myfile.intFromFile(cpos));
				cpos += 4;
			}
			
			sectionAlignment = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			fileAlignment = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			
			majorOSVer = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			minorOSVer = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			majorImageVer = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			minorImageVer = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			majorSubsystemVer = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			minorSubsystemVer = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			cpos += 4; //Reserved
			
			sizeOfImage = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			sizeOfHeaders = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			checksum = myfile.intFromFile(cpos); cpos += 4;
			
			short rawss = myfile.shortFromFile(cpos); cpos += 2;
			subsystem = Subsystem.getSubsystem(rawss);
			int rawflags = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
			dll_char = new DLL_Characteristics();
			dll_char.IMAGE_DLLCHARACTERISTICS_HIGH_ENTROPY_VA = (rawflags & 0x0020) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_DYNAMIC_BASE = (rawflags & 0x0040) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_FORCE_INTEGRITY = (rawflags & 0x0080) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_NX_COMPAT = (rawflags & 0x0100) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_NO_ISOLATION = (rawflags & 0x0200) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_NO_SEH = (rawflags & 0x0400) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_NO_BIND = (rawflags & 0x0800) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_APPCONTAINER = (rawflags & 0x1000) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_WDM_DRIVER = (rawflags & 0x2000) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_GUARD_CF = (rawflags & 0x4000) != 0;
			dll_char.IMAGE_DLLCHARACTERISTICS_TERMINAL_SERVER_AWARE = (rawflags & 0x8000) != 0;
			
			if (as64bit)
			{
				sizeOfStackReserve = myfile.longFromFile(cpos); cpos += 8;
				sizeOfStackCommit = myfile.longFromFile(cpos); cpos += 8;
				sizeOfHeapReserve = myfile.longFromFile(cpos); cpos += 8;
				sizeOfHeapCommit = myfile.longFromFile(cpos); cpos += 8;
			}
			else
			{
				sizeOfStackReserve = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
				sizeOfStackCommit = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
				sizeOfHeapReserve = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
				sizeOfHeapCommit = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
			}
			
			cpos += 4; //Reserved
			numberOfRvaAndSizes = myfile.intFromFile(cpos); cpos += 4;
		}
		
		public void printInfo()
		{
			if(PE32PLUS) System.err.println("Target Image Address: " + String.format("0x%016X", imageBase));
			else System.err.println("Target Image Address: " + String.format("0x%08X", imageBase));
			System.err.println("Section Alignment: " + String.format("0x%08X", sectionAlignment) + " (" + sectionAlignment + " bytes)");
			System.err.println("File Alignment: " + String.format("0x%08X", fileAlignment) + " (" + fileAlignment + " bytes)");
			System.err.println("OS Version: " + majorOSVer + "." + minorOSVer);
			System.err.println("Image Version: " + majorImageVer + "." + minorImageVer);
			System.err.println("Subsystem Version: " + majorSubsystemVer + "." + minorSubsystemVer);
			System.err.println("Image Size: " + String.format("0x%08X", sizeOfImage) + " (" + sizeOfImage + " bytes)");
			System.err.println("Header Size: " + String.format("0x%08X", sizeOfHeaders) + " (" + sizeOfHeaders + " bytes)");
			System.err.println("Checksum: " + String.format("0x%08X", checksum));
			System.err.println("Subsystem: " + subsystem.toString());
			//DLL Characteristics
			System.err.println("--- DLL Characteristics");
			System.err.println("High-Entropy 64-bit VA Space: " + dll_char.IMAGE_DLLCHARACTERISTICS_HIGH_ENTROPY_VA);
			System.err.println("DLL Can Be Relocated At Load: " + dll_char.IMAGE_DLLCHARACTERISTICS_DYNAMIC_BASE);
			System.err.println("Enforce Code Integrity: " + dll_char.IMAGE_DLLCHARACTERISTICS_FORCE_INTEGRITY);
			System.err.println("NX Compatible: " + dll_char.IMAGE_DLLCHARACTERISTICS_NX_COMPAT);
			System.err.println("Do Not Isolate: " + dll_char.IMAGE_DLLCHARACTERISTICS_NO_ISOLATION);
			System.err.println("No Structured Exception Handler: " + dll_char.IMAGE_DLLCHARACTERISTICS_NO_SEH);
			System.err.println("Do Not Bind: " + dll_char.IMAGE_DLLCHARACTERISTICS_NO_BIND);
			System.err.println("Execute in AppContainer: " + dll_char.IMAGE_DLLCHARACTERISTICS_APPCONTAINER);
			System.err.println("WDM Driver: " + dll_char.IMAGE_DLLCHARACTERISTICS_WDM_DRIVER);
			System.err.println("Control Flow Guard Supported: " + dll_char.IMAGE_DLLCHARACTERISTICS_GUARD_CF);
			System.err.println("Terminal Server Aware: " + dll_char.IMAGE_DLLCHARACTERISTICS_TERMINAL_SERVER_AWARE);
			System.err.println("-----------------------");
			if(PE32PLUS) System.err.println("Stack Reserve Size: " + String.format("0x%016X", sizeOfStackReserve));
			else System.err.println("Stack Reserve Size: " + String.format("0x%08X", sizeOfStackReserve));
			if(PE32PLUS) System.err.println("Stack Commit Size: " + String.format("0x%016X", sizeOfStackCommit));
			else System.err.println("Stack Commit Size: " + String.format("0x%08X", sizeOfStackCommit));
			if(PE32PLUS) System.err.println("Heap Reserve Size: " + String.format("0x%016X", sizeOfHeapReserve));
			else System.err.println("Heap Reserve Size: " + String.format("0x%08X", sizeOfHeapReserve));
			if(PE32PLUS) System.err.println("Heap Commit Size: " + String.format("0x%016X", sizeOfHeapCommit));
			else System.err.println("Heap Commit Size: " + String.format("0x%08X", sizeOfHeapCommit));
			System.err.println("Number of Data-Directory Entries: " + numberOfRvaAndSizes);
		}
		
	}

	private class DD_Table
	{
		public static final int ENTRY_SIZE = 8;
		
		public static final int TBL_IDX_EXPORT = 0;
		public static final int TBL_IDX_IMPORT = 1;
		public static final int TBL_IDX_RESOURCE = 2;
		public static final int TBL_IDX_EXCEPTION = 3;
		
		public static final int TBL_IDX_CERTIFICATE = 4;
		public static final int TBL_IDX_BASERELOCATION = 5;
		public static final int TBL_IDX_DEBUG = 6;
		//public static final int TBL_IDX_ARCHITECTURE = 7;
		
		public static final int TBL_IDX_GLOBALPTR = 8;
		public static final int TBL_IDX_TLS = 9;
		public static final int TBL_IDX_LOADCONFIG = 10;
		public static final int TBL_IDX_BOUNDIMPORT = 11;
		
		public static final int TBL_IDX_IAT = 12;
		public static final int TBL_IDX_DELAYIMPORT = 13;
		public static final int TBL_IDX_CLRRUNTIMEHEADER = 14;
		
		public int numberEntries;
		
		public int[] rva_tbl;
		public int[] sz_tbl;
		
		public DD_Table(FileBuffer myfile, long stpos, int nEntries)
		{
			numberEntries = -1;
			if (myfile == null) return;
			if (nEntries > 16) nEntries = 16;
			if (nEntries < 1) return;
			numberEntries = nEntries;
			
			rva_tbl = new int[numberEntries];
			sz_tbl = new int[numberEntries];
			
			long cpos = stpos;
			for (int i = 0; i < numberEntries; i++)
			{
				rva_tbl[i] = myfile.intFromFile(cpos); cpos += 4;
				sz_tbl[i] = myfile.intFromFile(cpos); cpos += 4;
			}
		}
		
		public int getSize()
		{
			return numberEntries * ENTRY_SIZE;
		}
	
		public void printInfo()
		{
			System.err.println("Data Directory Table");
			System.err.println("Number of Entries: " + numberEntries);
			if (numberEntries > TBL_IDX_EXPORT)
			{
				System.err.println("Export Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_EXPORT]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_EXPORT]) + " (" + sz_tbl[TBL_IDX_EXPORT] + " bytes)");
			}
			if (numberEntries > TBL_IDX_IMPORT)
			{
				System.err.println("Import Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_IMPORT]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_IMPORT]) + " (" + sz_tbl[TBL_IDX_IMPORT] + " bytes)");
			}
			if (numberEntries > TBL_IDX_RESOURCE)
			{
				System.err.println("Resource Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_RESOURCE]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_RESOURCE]) + " (" + sz_tbl[TBL_IDX_RESOURCE] + " bytes)");
			}
			if (numberEntries > TBL_IDX_EXCEPTION)
			{
				System.err.println("Exception Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_EXCEPTION]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_EXCEPTION]) + " (" + sz_tbl[TBL_IDX_EXCEPTION] + " bytes)");
			}
			
			if (numberEntries > TBL_IDX_CERTIFICATE)
			{
				System.err.println("Certificate Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_CERTIFICATE]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_CERTIFICATE]) + " (" + sz_tbl[TBL_IDX_CERTIFICATE] + " bytes)");
			}
			if (numberEntries > TBL_IDX_BASERELOCATION)
			{
				System.err.println("Base Relocation Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_BASERELOCATION]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_BASERELOCATION]) + " (" + sz_tbl[TBL_IDX_BASERELOCATION] + " bytes)");
			}
			if (numberEntries > TBL_IDX_DEBUG)
			{
				System.err.println("Debug Section");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_DEBUG]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_DEBUG]) + " (" + sz_tbl[TBL_IDX_DEBUG] + " bytes)");
			}
			
			if (numberEntries > TBL_IDX_GLOBALPTR)
			{
				System.err.println("Global Pointer");
				System.err.println("\tInitial Value: " + String.format("0x%08X", rva_tbl[TBL_IDX_GLOBALPTR]));
			}
			if (numberEntries > TBL_IDX_TLS)
			{
				System.err.println("TLS Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_TLS]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_TLS]) + " (" + sz_tbl[TBL_IDX_TLS] + " bytes)");
			}
			if (numberEntries > TBL_IDX_LOADCONFIG)
			{
				System.err.println("Load Config Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_LOADCONFIG]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_LOADCONFIG]) + " (" + sz_tbl[TBL_IDX_LOADCONFIG] + " bytes)");
			}
			if (numberEntries > TBL_IDX_BOUNDIMPORT)
			{
				System.err.println("Bound Import Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_BOUNDIMPORT]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_BOUNDIMPORT]) + " (" + sz_tbl[TBL_IDX_BOUNDIMPORT] + " bytes)");
			}
			
			if (numberEntries > TBL_IDX_IAT)
			{
				System.err.println("Import Address Table");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_IAT]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_IAT]) + " (" + sz_tbl[TBL_IDX_IAT] + " bytes)");
			}
			if (numberEntries > TBL_IDX_DELAYIMPORT)
			{
				System.err.println("Delay Import Descriptor");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_DELAYIMPORT]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_DELAYIMPORT]) + " (" + sz_tbl[TBL_IDX_DELAYIMPORT] + " bytes)");
			}
			if (numberEntries > TBL_IDX_CLRRUNTIMEHEADER)
			{
				System.err.println("CLR Runtime Header");
				System.err.println("\tRVA: " + String.format("0x%08X", rva_tbl[TBL_IDX_CLRRUNTIMEHEADER]));
				System.err.println("\tSize: " + String.format("0x%08X", sz_tbl[TBL_IDX_CLRRUNTIMEHEADER]) + " (" + sz_tbl[TBL_IDX_CLRRUNTIMEHEADER] + " bytes)");
			}
		}
		
	}
		
	private class SectionHeaderTable
	{
		public SectionHeader[] headers;
		
		public SectionHeaderTable(FileBuffer myfile, long stpos, int nSections) throws UnsupportedFileTypeException
		{
			if (nSections == 0) throw new FileBuffer.UnsupportedFileTypeException();
			headers = new SectionHeader[nSections];
			
			long cpos = stpos;
			for (int i = 0; i < nSections; i++)
			{
				headers[i] = new SectionHeader(myfile, cpos);
				cpos += SectionHeader.SERIAL_SIZE;
			}
		}
		
		public void printInfo()
		{
			if (headers == null) System.err.println("<NO SECTIONS>");
			for (int i = 0; i < headers.length; i++)
			{
				headers[i].printInfo();
				System.err.println();
			}
		}
		
	}
	
	/* --- Parsers --- */
	
	private void parsePE(FileBuffer myfile, boolean verbose) throws UnsupportedFileTypeException, IOException
	{
		//DOS Header
		myfile.setEndian(false);
		header_msdos = new MSDOS_Header(myfile);
		
		if (verbose)
		{
			System.err.println("----------------");
			System.err.println("DOS Header Read!");
			System.err.println("----------------");
			header_msdos.printInfo();
		}
		
		//DOS Stub
		int stublen = (int)header_msdos.e_lfanew - MSDOS_Header.SERIAL_SIZE;
		msdos_stub = new MSDOS_Stub(myfile, MSDOS_Header.SERIAL_SIZE, stublen);
		
		if (verbose)
		{
			System.err.println("----------------");
			System.err.println("DOS Stub Read!");
			System.err.println("----------------");
			msdos_stub.printInfo();
		}
		
		//PE Header
		long cpos = header_msdos.e_lfanew;
		header_signature = new PE_Header(myfile, header_msdos.e_lfanew);
		
		if (verbose)
		{
			System.err.println("----------------");
			System.err.println("PE Header Read!");
			System.err.println("----------------");
			header_signature.printInfo();
		}
		
		//COFF OP Header
		cpos += PE_Header.SERIAL_SIZE;
		header_coff = new OPHeader_COFF(myfile, cpos);
		
		if (verbose)
		{
			System.err.println("----------------");
			System.err.println("PE OP COFF Header Read!");
			System.err.println("----------------");
			header_coff.printInfo();
		}
		long cohsz = OPHeader_COFF.SERIAL_SIZE_32;
		if (header_coff.PE32PLUS) cohsz = OPHeader_COFF.SERIAL_SIZE_64;
		
		//WIN OP Header
		cpos += cohsz;
		header_win = new OPHeader_WIN(myfile, cpos, header_coff.PE32PLUS);
		
		if (verbose)
		{
			System.err.println("----------------");
			System.err.println("PE OP WIN Header Read!");
			System.err.println("----------------");
			header_win.printInfo();
		}

		//Data Directory
		long wohsz = OPHeader_WIN.SERIAL_SIZE_32;
		if (header_win.PE32PLUS) wohsz = OPHeader_WIN.SERIAL_SIZE_64;
		cpos += wohsz;
		table_datadir = new DD_Table(myfile, cpos, header_win.numberOfRvaAndSizes);
		
		if (verbose)
		{
			System.err.println("----------------");
			System.err.println("Data Directory Read!");
			System.err.println("----------------");
			table_datadir.printInfo();
		}
		
		//Section Header Table
		long ddsz = table_datadir.getSize();
		cpos += ddsz;
		int nsec = header_signature.nSections;
		SectionHeaderTable sht = new SectionHeaderTable(myfile, cpos, nsec);
		
		if (verbose)
		{
			System.err.println("----------------");
			System.err.println("Section Header Table Read!");
			System.err.println("----------------");
			sht.printInfo();
		}
		
		//Copy Sections
		if (nsec > 0)
		{
			sections = new Section[nsec];
			for (int i = 0; i < nsec; i++)
			{
				SectionHeader sh = sht.headers[i];
				sections[i] = new Section(myfile, sh);
			}
		}
		
	}
	
	public static ExportTable readExportTable(Winexe myfile, boolean verbose)
	{
		if (myfile == null) return null;
		
		long staddr = myfile.getDataTableRVA(Win32PE.TBL_IDX_EXPORT);
		long cpos = staddr + 4;
		int rawtime = myfile.getInt(cpos, false); cpos += 4;
		short vmaj = myfile.getShort(cpos, false); cpos += 2;
		short vmin = myfile.getShort(cpos, false); cpos += 2;
		long nameaddr = Integer.toUnsignedLong(myfile.getInt(cpos, false)); cpos += 4;
		int ordbase = myfile.getInt(cpos, false); cpos += 4;
		int nent = myfile.getInt(cpos, false); cpos += 4;
		int nnptr = myfile.getInt(cpos, false); cpos += 4;
		
		long addrtblptr = Integer.toUnsignedLong(myfile.getInt(cpos, false)); cpos += 4;
		long nametblptr = Integer.toUnsignedLong(myfile.getInt(cpos, false)); cpos += 4;
		long ordltblptr = Integer.toUnsignedLong(myfile.getInt(cpos, false)); cpos += 4;
		
		if (verbose)
		{
			System.err.println("Win32PE.readExportTable DEBUG ----");
			System.err.println("RVA: " + String.format("0x%016X", staddr));
			System.err.println("Offset: " + String.format("0x%016X", myfile.getFileOffset(staddr)));
			System.err.println("Raw Time: " + rawtime);
			System.err.println("Table Version: " + vmaj + "." + vmin);
			System.err.println("Name RVA: " + String.format("0x%016X", nameaddr));
			System.err.println("Ordinal Base: " + ordbase);
			System.err.println("Address Table Entries: " + nent);
			System.err.println("Name & Ordinal Table Entries: " + nnptr);
			System.err.println("Address Table RVA: " + String.format("0x%016X", addrtblptr));
			System.err.println("Name Table RVA: " + String.format("0x%016X", nametblptr));
			System.err.println("Ordinal Table RVA: " + String.format("0x%016X", ordltblptr));
		}
		
		int[] saddr_tbl = new int[nent];
		//int[] faddr_tbl = new int[nent];
		
		int[] nameptr_tbl = new int[nnptr];
		short[] ord_tbl = new short[nnptr];
		
		cpos = addrtblptr;
		for (int i = 0; i < nent; i++)
		{
			saddr_tbl[i] = myfile.getInt(cpos, false); cpos += 4;
			//faddr_tbl[i] = myfile.getInt(cpos, false); cpos += 4;
		}
		
		cpos = nametblptr;
		for (int i = 0; i < nnptr; i++)
		{
			nameptr_tbl[i] = myfile.getInt(cpos, false); cpos += 4;
		}
		
		cpos = ordltblptr;
		for (int i = 0; i < nnptr; i++)
		{
			ord_tbl[i] = myfile.getShort(cpos, false); cpos += 2;
			//System.err.println("Ordinal Table [" + i + "]: " + String.format("0x%04X", ord_tbl[i]));
		}
		
		String image_name = myfile.getString(nameaddr);
		if (verbose) System.err.println("Image Name Found: " + image_name);
		
		ExportTable et = new ExportTable(image_name, nnptr, ordbase);
		
		et.setVersion(vmaj, vmin);
		et.setTimestamp(rawtime);
		
		for (int i = 0; i < nnptr; i++)
		{
			int ord = Short.toUnsignedInt(ord_tbl[i]);
			long nptr = Integer.toUnsignedLong(nameptr_tbl[i]);
			int aind = ord - (ordbase-1);
			long saddr = Integer.toUnsignedLong(saddr_tbl[aind]);
			//long faddr = Integer.toUnsignedLong(faddr_tbl[aind]);
			
			//Get some strings!
			String exname = myfile.getString(nptr);
			//String fname = myfile.getString(faddr);
			
			et.setEntry(ord, exname, saddr);
		}
		
		return et;
	}
	
	/* --- Getters --- */
	
	public Winexe readdress()
	{
		Winexe exe = new Winexe(header_signature.machine, header_signature.nSections, header_win.imageBase);
		
		exe.loadDataDirTable(table_datadir.rva_tbl, table_datadir.sz_tbl);
		for (Section s : sections)
		{
			if (s != null)
			{
				exe.addSection(s);
			}
		}
		
		return exe;
	}
	
	/* --- Setters --- */
	
}
