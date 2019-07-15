package waffleoRai_Executable.winpe;

import java.io.IOException;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class Section implements Comparable<Section>{
	
	public SectionHeader header;
	
	public FileBuffer data;
	
	public Section(FileBuffer myfile, SectionHeader sh) throws UnsupportedFileTypeException, IOException
	{
		if (sh == null) throw new FileBuffer.UnsupportedFileTypeException();
		if (myfile == null) throw new FileBuffer.UnsupportedFileTypeException();
		header = sh;
		long stpos = header.rawDataPtr;
		long rawsz = header.rawDataSize;
		data = FileBuffer.createWritableBuffer("Win32PESectionParser", rawsz, false);
		
		data.addToFile(myfile, stpos, stpos + rawsz);
	}
	
	public void sizeVirtual()
	{
		long rawsz = header.rawDataSize;
		long vsz = header.virtualSize;
		try 
		{
			FileBuffer newdata = FileBuffer.createWritableBuffer("Win32PESectionVirtual", vsz, false);
			if (rawsz <= vsz)
			{
				newdata.addToFile(data);
				long diff = vsz - rawsz;
				for (long i = 0; i < diff; i++)
				{
					newdata.addToFile((byte)0x00);
				}
			}
			else
			{
				//Virtual size is smaller than raw - usually the case
				newdata.addToFile(data, 0, vsz);
			}
			data = newdata;
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Section resize failed!");
		}
	}

	public int compareTo(Section o) 
	{
		if (o == null) return 1;
		if (o == this) return 0;
		if (this.header == null && o.header == null) return 0;
		else if (this.header != null && o.header == null) return 1;
		else if (this.header == null && o.header != null) return -1;
		
		long diff = this.header.virtualAddr - o.header.virtualAddr;
		if (diff > 0) return 1;
		else if (diff < 0) return -1;
		
		return 0;
	}


}
