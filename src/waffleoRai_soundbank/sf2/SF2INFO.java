package waffleoRai_soundbank.sf2;

import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class SF2INFO {
	
	/* ----- Constants ----- */
	
	public static final int ASCII_MAXCHAR = 256;
	public static final int ASCII_MAXCHAR_LONG = 65536;
	//ASCII padding is 1-2 bytes (2 if string is even size, 1 if odd)
	
	public static final short DEFAULT_MAJOR_VERSION = 2;
	public static final short DEFAULT_MINOR_VERSION = 4;
	public static final String DEFAULT_ENGINE = "EMU8000";
	
	public static final String INFO_MAGIC = "INFO";
	public static final String ifil_MAGIC = "ifil";
	public static final String isng_MAGIC = "isng";
	public static final String inam_MAGIC = "INAM";
	public static final String irom_MAGIC = "irom";
	public static final String iver_MAGIC = "iver";
	public static final String icrd_MAGIC = "ICRD";
	public static final String ieng_MAGIC = "IENG";
	public static final String iprd_MAGIC = "IPRD";
	public static final String icop_MAGIC = "ICOP";
	public static final String icmt_MAGIC = "ICMT";
	public static final String isft_MAGIC = "ISFT";

	/* ----- Instance Variables ----- */
	
	//Mandatory
	private short IFIL_major;
	private short IFIL_minor;
	
	//Mandatory
	private String ISNG_engine;
	
	//Mandatory
	private String INAM_fontName;
	
	private String IROM_ROM;
	
	private short IVER_ROM_major;
	private short IVER_ROM_minor;
	
	private String ICRD_datestring;
	
	private String IENG_engineer;
	
	private String IPRD_product;
	
	private String ICOP_copyright;
	
	private String ICMT_comments; //Long string
	
	private String ISFT_sftool;
	
	/* ----- Construction ----- */
	
	public SF2INFO(String bankname)
	{
		IVER_ROM_major = -1;
		
		if (bankname == null || bankname.isEmpty()) bankname = "UNTITLED_SF2BANK";
		if (bankname.length() > (ASCII_MAXCHAR - 1))
		{
			bankname = bankname.substring(0, (ASCII_MAXCHAR - 2));
		}
		INAM_fontName = bankname;
		
		IFIL_major = DEFAULT_MAJOR_VERSION;
		IFIL_minor = DEFAULT_MINOR_VERSION;
		ISNG_engine = DEFAULT_ENGINE;
		
		//Everything else is null by default
	}
	
	public SF2INFO(FileBuffer chunk) throws UnsupportedFileTypeException
	{
		IVER_ROM_major = -1;
		parseINFO(chunk);
	}
	
	/* ----- Parsing ----- */
	
	private void parseINFO(FileBuffer chunk) throws UnsupportedFileTypeException
	{
		//Chunk SHOULD start with the LIST....INFO!
		chunk.setEndian(false);
		
		//Look for INFO tag
		long infoloc = chunk.findString(0, 0x40, INFO_MAGIC);
		if (infoloc < 0) throw new FileBuffer.UnsupportedFileTypeException();
		
		//Look for LIST tag - should be 8 bytes before INFO!
		long listloc = chunk.findString(0, infoloc, SF2.LIST_MAGIC);
		if (infoloc - listloc != 8) throw new FileBuffer.UnsupportedFileTypeException();
		
		//Grab chunk size
		int listsize = chunk.intFromFile(listloc + 4);
		
		//Look for individual chunks
		
		//ifil
		long cst = chunk.findString(infoloc, infoloc + listsize, ifil_MAGIC);
		if (cst < 0) throw new FileBuffer.UnsupportedFileTypeException(); //Mandatory
		long cpos = cst + 4;
		int csz = chunk.intFromFile(cpos); cpos += 4;
		if (csz != 4) throw new FileBuffer.UnsupportedFileTypeException();
		IFIL_major = chunk.shortFromFile(cpos); cpos += 2;
		IFIL_minor = chunk.shortFromFile(cpos); cpos += 2;
		
		//isng
		cst = chunk.findString(infoloc, infoloc + listsize, isng_MAGIC);
		if (cst < 0) throw new FileBuffer.UnsupportedFileTypeException(); //Mandatory
		cpos = cst + 4;
		csz = chunk.intFromFile(cpos); cpos += 4;
		//Read to 0 byte
		ISNG_engine = chunk.getASCII_string(cpos, '\0');
		
		//inam
		cst = chunk.findString(infoloc, infoloc + listsize, inam_MAGIC);
		if (cst < 0) throw new FileBuffer.UnsupportedFileTypeException(); //Mandatory
		cpos = cst + 4;
		csz = chunk.intFromFile(cpos); cpos += 4;
		INAM_fontName = chunk.getASCII_string(cpos, '\0');
		
		//irom
		cst = chunk.findString(infoloc, infoloc + listsize, irom_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			IROM_ROM = chunk.getASCII_string(cpos, '\0');	
		}
		
		//iver
		cst = chunk.findString(infoloc, infoloc + listsize, iver_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			if (csz != 4) throw new FileBuffer.UnsupportedFileTypeException();
			IVER_ROM_major = chunk.shortFromFile(cpos); cpos += 2;
			IVER_ROM_minor = chunk.shortFromFile(cpos); cpos += 2;
		}
		
		
		//icrd
		cst = chunk.findString(infoloc, infoloc + listsize, icrd_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			ICRD_datestring = chunk.getASCII_string(cpos, '\0');	
		}
		
		//ieng
		cst = chunk.findString(infoloc, infoloc + listsize, ieng_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			IENG_engineer = chunk.getASCII_string(cpos, '\0');	
		}
		
		//iprd
		cst = chunk.findString(infoloc, infoloc + listsize, iprd_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			IPRD_product = chunk.getASCII_string(cpos, '\0');	
		}
		
		//icop
		cst = chunk.findString(infoloc, infoloc + listsize, icop_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			ICOP_copyright = chunk.getASCII_string(cpos, '\0');	
		}
		
		//icmt
		cst = chunk.findString(infoloc, infoloc + listsize, icmt_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			ICMT_comments = chunk.getASCII_string(cpos, '\0');	
		}
		
		//isft
		cst = chunk.findString(infoloc, infoloc + listsize, isft_MAGIC);
		if (cst >= 0)
		{
			cpos = cst + 4;
			csz = chunk.intFromFile(cpos); cpos += 4;
			ISFT_sftool = chunk.getASCII_string(cpos, '\0');	
		}
		
	}
	
	/* ----- Serialization ----- */
	
	private FileBuffer serialize_numPair(String magic, short n1, short n2)
	{
		if (n1 < 0) return null;
		int totalsz = 4 + 4 + 4;
		FileBuffer subchunk = new FileBuffer(totalsz, false);
		subchunk.printASCIIToFile(magic);
		subchunk.addToFile(4); 
		subchunk.addToFile(n1);
		subchunk.addToFile(n2);
		return subchunk;
	}
	
	private FileBuffer serialize_string(String magic, String s)
	{
		if (s == null) return null;
		int slen = s.length();
		int tsz = 4 + 4 + slen;
		int pad = 1;
		if (slen % 2 == 0) pad = 2;
		tsz += pad;
		
		FileBuffer subchunk = new FileBuffer(tsz, false);
		subchunk.printASCIIToFile(magic);
		subchunk.addToFile((tsz - 8));
		subchunk.printASCIIToFile(s);
		subchunk.addToFile((byte)0x00);
		if (pad == 2) subchunk.addToFile((byte)0x00);
		return subchunk;
	}
	
	public FileBuffer serializeMe()
	{
		FileBuffer info = new CompositeBuffer(12);
		
		FileBuffer ifil = serialize_numPair(ifil_MAGIC, IFIL_major, IFIL_minor);
		FileBuffer isng = serialize_string(isng_MAGIC, ISNG_engine);
		FileBuffer inam = serialize_string(inam_MAGIC, INAM_fontName);
		
		FileBuffer irom = serialize_string(irom_MAGIC, IROM_ROM);
		FileBuffer iver = serialize_numPair(iver_MAGIC, IVER_ROM_major, IVER_ROM_minor);
		FileBuffer icrd = serialize_string(icrd_MAGIC, ICRD_datestring);
		FileBuffer ieng = serialize_string(ieng_MAGIC, IENG_engineer);
		FileBuffer iprd = serialize_string(iprd_MAGIC, IPRD_product);
		FileBuffer icop = serialize_string(icop_MAGIC, ICOP_copyright);
		FileBuffer icmt = serialize_string(icmt_MAGIC, ICMT_comments);
		FileBuffer isft = serialize_string(isft_MAGIC, ISFT_sftool);
		
		long csz = 0;
		csz += ifil.getFileSize();
		csz += isng.getFileSize();
		csz += inam.getFileSize();
		if (irom != null) csz += irom.getFileSize();
		if (iver != null) csz += iver.getFileSize();
		if (icrd != null) csz += icrd.getFileSize();
		if (ieng != null) csz += ieng.getFileSize();
		if (iprd != null) csz += iprd.getFileSize();
		if (icop != null) csz += icop.getFileSize();
		if (icmt != null) csz += icmt.getFileSize();
		if (isft != null) csz += isft.getFileSize();
		
		FileBuffer cheader = new FileBuffer(4+4+4, false);
		cheader.printASCIIToFile(SF2.LIST_MAGIC);
		cheader.addToFile((int)csz + 4);
		cheader.printASCIIToFile(INFO_MAGIC);
		
		info.addToFile(cheader);
		info.addToFile(ifil);
		info.addToFile(isng);
		info.addToFile(inam);
		if (irom != null) info.addToFile(irom);
		if (iver != null) info.addToFile(iver);
		if (icrd != null) info.addToFile(icrd);
		if (ieng != null) info.addToFile(ieng);
		if (iprd != null) info.addToFile(iprd);
		if (icop != null) info.addToFile(icop);
		if (icmt != null) info.addToFile(icmt);
		if (isft != null) info.addToFile(isft);
		
		return info;
	}
	
	/* ----- Getters ----- */
	
	public String getFontName()
	{
		return this.INAM_fontName;
	}
	
	public int getMajorVersion()
	{
		return Short.toUnsignedInt(IFIL_major);
	}
	
	public int getMinorVersion()
	{
		return Short.toUnsignedInt(IFIL_minor);
	}
	
	public String getEngineName()
	{
		return this.ISNG_engine;
	}
	
	public String getROMName()
	{
		return this.IROM_ROM;
	}
	
	public int getROMMajorVersion()
	{
		return Short.toUnsignedInt(this.IVER_ROM_major);
	}
	
	public int getROMMinorVersion()
	{
		return Short.toUnsignedInt(this.IVER_ROM_minor);
	}
	
	public String getDateString()
	{
		return this.ICRD_datestring;
	}
	
	public String getAuthor()
	{
		return this.IENG_engineer;
	}
	
	public String getProduct()
	{
		return this.IPRD_product;
	}
	
	public String getCopyright()
	{
		return this.ICOP_copyright;
	}
	
	public String getComments()
	{
		return this.ICMT_comments;
	}
	
	public String getToolName()
	{
		return this.ISFT_sftool;
	}
	
	/* ----- Setters ----- */
	
	private String checkString(String in)
	{
		if (in == null) return "";
		if (in.length() > (ASCII_MAXCHAR - 1))
		{
			return in.substring(0, (ASCII_MAXCHAR - 2));
		}
		return in;
	}
	
	private String checkLongString(String in)
	{
		if (in == null) return "";
		if (in.length() > (ASCII_MAXCHAR_LONG - 1))
		{
			return in.substring(0, (ASCII_MAXCHAR_LONG - 2));
		}
		return in;
	}
	
	public void setName(String name)
	{
		this.INAM_fontName = checkString(name);
	}
	
	public void setVersion(int major, int minor)
	{
		this.IFIL_major = (short)major;
		this.IFIL_minor = (short)minor;
	}
	
	public void setEngineName(String name)
	{
		this.ISNG_engine = checkString(name);
	}
	
	public void setROMName(String name)
	{
		this.IROM_ROM = checkString(name);
	}
	
	public void setROMVersion(int major, int minor)
	{
		this.IVER_ROM_major = (short)major;
		this.IVER_ROM_minor = (short)minor;
	}
	
	public void setDateString(String date)
	{
		this.ICRD_datestring = checkString(date);
	}
	
	public void setDateStringToNow_English()
	{
		OffsetDateTime now = OffsetDateTime.now();
		String date = "";
		date += now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " ";
		date += now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " ";
		date += now.getDayOfMonth() + ", ";
		date += now.getYear() + " ";
		date += String.format("%02d:", now.getHour());
		date += String.format("%02d ", now.getMinute());
		date += now.getOffset().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
		this.ICRD_datestring = checkString(date);
	}
	
	public void setAuthor(String name)
	{
		this.IENG_engineer = checkString(name);
	}
	
	public void setProduct(String name)
	{
		this.IPRD_product = checkString(name);
	}
	
	public void setCopyright(String cp)
	{
		this.ICOP_copyright = checkString(cp);
	}
	
	public void setToolName(String name)
	{
		this.ISFT_sftool = checkString(name);
	}
	
	public void setComments(String comments)
	{
		this.ICMT_comments = checkLongString(comments);
	}
	
	/* ----- Debug ----- */
	
	public void printInfo()
	{
		System.out.println("--- SF2 INFO ---");
		System.out.println("Font Name: " + this.INAM_fontName);
		System.out.println("Engine: " + this.ISNG_engine);
		System.out.println("Version: " + this.IFIL_major + "." + this.IFIL_minor);
		if(ICRD_datestring != null) System.out.println("Date: " + this.ICRD_datestring);
		if(ISFT_sftool != null) System.out.println("Authoring Tool: " + this.ISFT_sftool);
		if(IENG_engineer != null) System.out.println("Engineer: " + this.IENG_engineer);
		if(IROM_ROM != null) System.out.println("ROM: " + this.IROM_ROM);
		if(IROM_ROM != null) System.out.println("ROM Version: " + this.IVER_ROM_major + "." + this.IVER_ROM_minor);
		if(IPRD_product != null) System.out.println("Product: " + this.IPRD_product);
		if(ICOP_copyright != null) System.out.println("Copyright: " + this.ICOP_copyright);
		if(ICMT_comments != null) System.out.println("Comments: " + this.ICMT_comments);
	}
	
}
