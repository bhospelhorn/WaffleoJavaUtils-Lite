package waffleoRai_Executable.winpe;

import waffleoRai_Utils.FileBuffer;

public class SectionHeader {
	
	public static final int SERIAL_SIZE = 40;
	
	public String name;
	
	public long virtualSize;
	public long virtualAddr;
	
	public long rawDataSize;
	public long rawDataPtr;
	
	public long relocPtr;
	public int relocCount;
	
	public SectionCharacteristics characteristics;
	
	public SectionHeader(FileBuffer myfile, long stpos)
	{
		if (myfile == null) return;
		long cpos = stpos;
		
		name = myfile.readEncoded_string("UTF8", cpos, cpos + 8);
		int i = name.indexOf('\0');
		if (i >= 0) name = name.substring(0, i);
		cpos += 8;
		
		virtualSize = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
		virtualAddr = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
		rawDataSize = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
		rawDataPtr = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
		relocPtr = Integer.toUnsignedLong(myfile.intFromFile(cpos)); cpos += 4;
		cpos += 4; //Deprecated field
		relocCount = Short.toUnsignedInt(myfile.shortFromFile(cpos)); cpos += 2;
		cpos += 2; //Deprecated field
		int rawflags = myfile.intFromFile(cpos); cpos += 4;
		characteristics = new SectionCharacteristics(rawflags);
	}
	
	public void printInfo()
	{
		System.err.println("--- Section Header ---");
		System.err.println("\tName: " + name);
		System.err.println("\tVirtual Size: " + String.format("0x%08X", virtualSize) + " (" + virtualSize + " bytes)");
		System.err.println("\tVirtual Address: " + String.format("0x%08X", virtualAddr));
		System.err.println("\tRaw Data Size: " + String.format("0x%08X", rawDataSize) + " (" + rawDataSize + " bytes)");
		System.err.println("\tRaw Data Pointer: " + String.format("0x%08X", rawDataPtr));
		System.err.println("\tRelocation Pointer: " + String.format("0x%08X", relocPtr));
		System.err.println("\tRelocation Number: " + relocCount);
		System.err.println("-- Section Characteristics");
		characteristics.printInfo();
	}


}
