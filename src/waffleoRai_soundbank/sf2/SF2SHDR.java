package waffleoRai_soundbank.sf2;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class SF2SHDR {
	
	/* ----- Constants ----- */
	
	public static final String SHDR_MAGIC = "shdr";
	
	public static final String TERMINAL_NAME = "EOS";
	
	public static final int SAMPLENAME_MAX_SIZE = 20;
	public static final int RECORD_SIZE = 46;
	
	/* ----- Instance Variables ----- */
	
	private String sampleName;
	
	private long start;
	private long end;
	private long startLoop;
	private long endLoop;
	
	private int sampleRate;
	
	private byte unityNote;
	private int fineTune;
	
	private int sampleLink;
	private boolean inROM;
	private LinkType link;
	
	/* ----- Inner Classes ----- */
	
	public static enum LinkType
	{
		MONO(0),
		RIGHT(1),
		LEFT(2),
		LINKED(3);
		
		private int shift;
		
		private LinkType(int s)
		{
			shift = s;
		}
		
		public int getShift()
		{
			return shift;
		}
		
	}

	/* ----- Construction ----- */
	
	public SF2SHDR(String name)
	{
		sampleName = name;
		start = 0;
		end = 0;
		startLoop = 0;
		endLoop = 0;
		unityNote = 60;
		fineTune = 0;
		sampleLink = 0;
		inROM = false;
		link = LinkType.MONO;
	}
	
	public SF2SHDR(FileBuffer record, long stpos) throws UnsupportedFileTypeException
	{
		parseSHDRRecord(record, stpos);
	}
	
	/* ----- Parsing ----- */
	
	private void parseSHDRRecord(FileBuffer record, long stpos) throws UnsupportedFileTypeException
	{
		if (record == null) throw new FileBuffer.UnsupportedFileTypeException();
		record.setEndian(false);
		
		long cpos = stpos;
		sampleName = record.getASCII_string(cpos, SAMPLENAME_MAX_SIZE); cpos += SAMPLENAME_MAX_SIZE;
		start = Integer.toUnsignedLong(record.intFromFile(cpos)); cpos += 4;
		end = Integer.toUnsignedLong(record.intFromFile(cpos)); cpos += 4;
		startLoop = Integer.toUnsignedLong(record.intFromFile(cpos)); cpos += 4;
		endLoop = Integer.toUnsignedLong(record.intFromFile(cpos)); cpos += 4;
		sampleRate = record.intFromFile(cpos); cpos += 4;
		unityNote = record.getByte(cpos); cpos++;
		fineTune = (int)record.getByte(cpos); cpos++;
		sampleLink = Short.toUnsignedInt(record.shortFromFile(cpos)); cpos += 2;
		int rawflags = Short.toUnsignedInt(record.shortFromFile(cpos)); cpos += 2;
		inROM = ((rawflags & 0x8000) != 0);
		rawflags = rawflags & 0xF;
		if (rawflags == 1) link = LinkType.MONO;
		else if (rawflags == 2) link = LinkType.RIGHT;
		else if (rawflags == 4) link = LinkType.LEFT;
		else if (rawflags == 8) link = LinkType.LINKED;
		else link = LinkType.MONO;
	}
	
	/* ----- Serialization ----- */
	
	public FileBuffer serializeRecord()
	{
		FileBuffer rec = new FileBuffer(40, false);
		if (sampleName.length() > SAMPLENAME_MAX_SIZE) sampleName = sampleName.substring(0, SAMPLENAME_MAX_SIZE);
		rec.printASCIIToFile(sampleName);
		while (rec.getFileSize() < 20) rec.addToFile((byte)0x00);
		rec.addToFile((int)start);
		rec.addToFile((int)end);
		rec.addToFile((int)startLoop);
		rec.addToFile((int)endLoop);
		rec.addToFile(sampleRate);
		
		rec.addToFile(unityNote);
		rec.addToFile((byte)fineTune);
		
		rec.addToFile((short)sampleLink);
		int rawflags = 1;
		int shift = link.getShift();
		rawflags = rawflags << shift;
		if (inROM) rawflags |= 0x8000;
		rec.addToFile((short)rawflags);
		
		return rec;
	}
	
	public static FileBuffer getSerializedTerminalRecord()
	{
		FileBuffer rec = new FileBuffer(40, false);
		rec.printASCIIToFile(TERMINAL_NAME);
		while (rec.getFileSize() < 20) rec.addToFile((byte)0x00);
		rec.addToFile(0);
		rec.addToFile(0);
		rec.addToFile(0);
		rec.addToFile(0);
		rec.addToFile(0);
		
		rec.addToFile((byte)0);
		rec.addToFile((byte)0);
		
		rec.addToFile((short)0);
		rec.addToFile((short)0);

		return rec;
	}
	
	/* ----- Getters ----- */
	
	public String getSampleName()
	{
		return sampleName;
	}
	
	public long getStartSamplePoint()
	{
		return start;
	}
	
	public long getEndSamplePoint()
	{
		return end;
	}
	
	public long getStartLoopPoint()
	{
		return startLoop;
	}
	
	public long getEndLoopPoint()
	{
		return endLoop;
	}
	
	public int getSampleRate()
	{
		return sampleRate;
	}
	
	public byte getUnityNote()
	{
		return unityNote;
	}
	
	public int getFineTuneCents()
	{
		return fineTune;
	}
	
	public int getSampleLink()
	{
		return sampleLink;
	}
	
	public boolean readFromROM()
	{
		return inROM;
	}
	
	public LinkType getLinkType()
	{
		return link;
	}
	
	/* ----- Setters ----- */
	
	public void setName(String newname)
	{
		if (newname.length() > SAMPLENAME_MAX_SIZE) newname = newname.substring(0, SAMPLENAME_MAX_SIZE);
		sampleName = newname;
	}
	
	public void setSampleCoordinates(long st, long ed)
	{
		start = st;
		end = ed;
	}
	
	public void setLoopCoordinates(long st, long ed)
	{
		startLoop = st;
		endLoop = ed;
	}
	
	public void setSampleRate(int sr)
	{
		sampleRate = sr;
	}
	
	public void setUnityNote(byte pitch)
	{
		if (pitch < 0) pitch = 0;
		unityNote = pitch;
	}
	
	public void setFineTune(int cents)
	{
		fineTune = cents;
	}
	
	public void setSampleLink(int link)
	{
		sampleLink = link;
	}
	
	public void setReadFromROM(boolean b)
	{
		inROM = b;
	}
	
	public void setLinkType(LinkType t)
	{
		if (t == null) return;
		link = t;
	}
	
}
