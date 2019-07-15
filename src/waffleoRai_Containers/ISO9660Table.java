package waffleoRai_Containers;

import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import waffleoRai_Containers.ISO.Sector;
import waffleoRai_Utils.BitStreamer;
import waffleoRai_Utils.FileBuffer;

/*
 * UPDATES
 * 
 * 2017.10.30 | 1.2.0 -> 1.2.1
 * 	Javadoc annotation
 * 
 * 2017.11.02 | 1.2.0 -> 1.3.0
 * 	Fixed some parsing issues, especially with timestamp
 * 2017.11.18 | 1.3.0 -> 1.4.0
 * 	Updated for compatibility with Java 9 (removed Observer/Observable usage)
 */

/**
 * CDTable class specific to ISO9660 standard (no extensions).
 * Can parse an ISO object upon construction to deduce a ISO9660 encoded CD image's file structure.
 * @author Blythe Hospelhorn
 * @version 1.4.0
 * @since November 18, 2017
 *
 */
public class ISO9660Table implements CDTable
{
	/**
	 * The (relative) sector where the Primary Volume Descriptor in an ISO9660
	 * encoded image is found.
	 */
	public static final int VOLDESCSEC = 16;
	
	/**
	 * An array of file names that may be found in an ISO9660 directory table that
	 * do not point to a file within the directory and should be ignored.
	 */
	public static final String[] badNames = {"", ".", "..", "/", null, "\\", " "};
	
	private Map<String, ISO9660Entry> itemMap;
	private List<ISO9660Entry> sectorMap;
	
	private int firstSector;
	private int numSectors;
	
	/* --- Objects --- */
	
	/**
	 * Class to represent an entry in a directory table in the ISO9660 file system.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since August 31, 2017
	 */
	public static class ISO9660Entry extends CDTable.CDTEntry 
	{
		private int volumeSeqNumber;
		private boolean rawFile;
		
		private boolean compliantName;
		
		/**
		 * Construct a ISO9660 table entry from the file name and the (relative) sector
		 * the file data begins at on the image.
		 * @param name Name of the file.
		 * @param startBlock The first sector containing the data of the file relative to the
		 * beginning of the image.
		 */
 		public ISO9660Entry(String name, int startBlock)
		{
			super(name, startBlock);
			volumeSeqNumber = 0;
			this.rawFile = false;
		}
		
 		/**
 		 * Construct a ISO9660 table entry given the disk image (in a FileBuffer) and 
 		 * the position of the first byte in the data record relative to the disk image.
 		 * @param isoData The FileBuffer containing the full image or sector data to parse.
 		 * @param dePos The position of the first byte of the record in an image directory.
 		 * @throws CDInvalidRecordException If the record cannot be parsed. This is detected
 		 * by checking for setting in reserved flag bits that should never be set.
 		 */
		public ISO9660Entry(FileBuffer isoData, int dePos) throws CDInvalidRecordException
		{
			this.parseISORecord(isoData, dePos);
		}
		
 		/**
 		 * Construct a ISO9660 table entry given the disk image (in a FileBuffer) and 
 		 * the position of the first byte in the data record relative to the disk image.
 		 * Include an observable so that parsing progress can be observed, namely by a GUI.
 		 * @param isoData The FileBuffer containing the full image or sector data to parse.
 		 * @param dePos The position of the first byte of the record in an image directory.
 		 * @param eCont Observable "event container" for firing events.
 		 * @throws CDInvalidRecordException If the record cannot be parsed. This is detected
 		 * by checking for setting in reserved flag bits that should never be set.
 		 */
		/*public ISO9660Entry(FileBuffer isoData, int dePos, ISOReadEvent eCont) throws CDInvalidRecordException
		{
			this.parseISORecord(isoData, dePos, eCont);
		}*/
		
		/* --- Parsers --- */
		
		/**
		 * Parse an ISO9660 directory record and save data in this object.
		 * @param isoData Data to parse.
		 * @param dePos Offset from beginning of isoData to begin parsing.
		 * @param eCont Event container (Observable) for monitoring parsing progress.
		 * @throws CDInvalidRecordException If the record cannot be parsed. This is detected
 		 * by checking for setting in reserved flag bits that should never be set.
		 */
		private void parseISORecord(FileBuffer isoData, int dePos) throws CDInvalidRecordException
		{
			/*It will assume that what you shove in front of it is a legit
			 * ISO9660 directory entry and behave accordingly.
			 */
			//if (eCont != null) eCont.fireNewEvent(EventType.TBL9660_ENTRYPARSESTART, dePos);
			if (!isoData.isBigEndian()) isoData.setEndian(true);
			
			int cPos = dePos;
			//int delen = Byte.toUnsignedInt(isoData.getByte(cPos));
			
			//skip entry length (1)
			//skip EA length (1)
			//skip LE start sector
			cPos += 6;
			
			super.setStartBlock(isoData.intFromFile(cPos));
			cPos += 8; //skip next LE read
			
			super.setFileSize(isoData.intFromFile(cPos));
			cPos += 4;
			
			GregorianCalendar c = FileBuffer.getVanillaTimestamp();
			int year = 1900 + Byte.toUnsignedInt(isoData.getByte(cPos));
			cPos++;
			
			int month = Byte.toUnsignedInt(isoData.getByte(cPos)) - 1;
			cPos++;
			
			int date = Byte.toUnsignedInt(isoData.getByte(cPos));
			cPos++;
			
			int hourOfDay = Byte.toUnsignedInt(isoData.getByte(cPos));
			cPos++;
			
			int minute = Byte.toUnsignedInt(isoData.getByte(cPos));
			cPos++;
			
			int second = Byte.toUnsignedInt(isoData.getByte(cPos));
			cPos++;
			
			c.set(year, month, date, hourOfDay, minute, second);
			
			int tz = (int)isoData.getByte(cPos);
			tz *= 15 * 60 * 1000;
			cPos++;
			String[] possible = TimeZone.getAvailableIDs(tz);
			if (possible != null && possible.length > 0)
			{
				String tzID = possible[0];
				c.setTimeZone(TimeZone.getTimeZone(tzID));
				super.setDate(c);
			}
			
			byte flags = isoData.getByte(cPos);
			cPos++;
			if (BitStreamer.readABit(flags, 1)) super.setIsDirectory(true);
			else super.setIsDirectory(false);
			if (BitStreamer.readABit(flags, 5) || BitStreamer.readABit(flags, 6)) 
							throw new CDInvalidRecordException("ISO9660 directory entry parser error: \n"
																+ "Illegal flag bits set.\n"
																+ "Parser assumes directory entry is not valid ISO entry.\n");
			
			//skip file unit size (1)
			//skip interleave gap size (1)
			//skip LE volume seq number (2)
			cPos += 4;
			
			this.volumeSeqNumber = Short.toUnsignedInt(isoData.shortFromFile(cPos));
			cPos += 2;
			
			int nLen = Byte.toUnsignedInt(isoData.getByte(cPos));
			cPos++;
			
			String name = isoData.getASCII_string(cPos, nLen);
			this.setName(name);
			
			this.rawFile = false;
			//if (eCont != null) eCont.fireNewEvent(EventType.TBL9660_ENTRYPARSEEND, dePos, this.getName());
		}
		
		/* --- Getters --- */
		
		/**
		 * Get the volume sequence number contained in this entry. The volume sequence number
		 * is used to denote which volume in a set of CDs the file is found on.
		 * @return Integer representing the Volume Sequence Number for this record.
		 */
		public int getVolumeSequenceNumber()
		{
			return this.volumeSeqNumber;
		}
		
		/**
		 * Get whether entry refers to a true file found on the image
		 * or a conglomeration of raw sectors.
		 * @return
		 * True - If entry refers to a string of raw sectors.
		 * <br>False - If entry refers to a file found in the image directory.
		 */
		public boolean isRawFile()
		{
			return this.rawFile;
		}
		
		/**
		 * Get the number of sectors the file occupies. This is NOT the size in bytes, and
		 * the file is more likely than not NOT the exact same size as the total size in bytes
		 * of the sectors it occupies.
		 * @return Integer representing the file size in sectors.
		 */
		public int sizeInSectors()
		{
			if (super.isDirectory()) return 1;
			int sz = (int)(super.getFileSize() / (long)ISO.F1SIZE);
			if (super.getFileSize() % ISO.F1SIZE != 0) return sz + 1;
			return sz;
		}
		
		/**
		 * Get the index (relative) of the last sector containing data for the file
		 * referenced by this entry.
		 * @return The image relative index of the last sector.
		 */
		public int lastSector()
		{
			int ss = this.sizeInSectors();
			return (super.getStartBlock() + ss) - 1;
		}
		
		/**
		 * Calculate the offset relative to the file referenced by this entry indicating
		 * the first byte of the ISO image sector at the (relative) index provided.
		 * <br> This can be used to obtain just the data from a single sector having the extracted
		 * file and table, but not the original image.
		 * @param s Index, relative to image start, of the desired sector.
		 * @return Long integer representing the position relative to the start of the file referenced by
		 * this entry of the first byte originally stored in the specified ISO image sector.
		 * <br>-1 if file referenced by this entry does not lie within specified sector.
		 */
		public long calcFileOffsetOfSector(int s)
		{
			if (s < super.getStartBlock()) return -1;
			if (s > this.lastSector()) return -1;
			long tsec = s - super.getStartBlock();
			return tsec * ISO.F1SIZE;
		}
		
		/**
		 * Whether name of file in entry is ISO9660 compliant
		 * (contains only letters, numbers, and underscores; 
		 * is 12 or fewer characters; may contain a semicolon followed by numbers as well)
		 * @return
		 * True - If file name in entry is compliant.
		 * <br> False - If file name in entry is not ISO9660 compliant.
		 */
		public boolean isNameCompliant()
		{
			return this.compliantName;
		}
		
		public int getSizeInSectors()
		{
			int sectorCount = (int)(super.getFileSize() / (long)ISO.F1SIZE);
			if (super.getFileSize() % (long)ISO.F1SIZE != 0) sectorCount++;
			return sectorCount;
		}
		
		/* --- Setters --- */
		
		/**
		 * Set the volume sequence number - the index of the volume file can be found on.
		 * @param VSN Integer to set the volume sequence number to.
		 */
		public void setVolumeSequenceNumber(int VSN)
		{
			this.volumeSeqNumber = VSN;
		}
		
		/**
		 * Set flag to denote whether this entry refers to a file that was originally
		 * in the image directory or to a conglomeration of raw sectors.
		 * @param isRaw Whether this file is to be marked as a series of raw sectors or not.
		 */
		public void setRawFile(boolean isRaw)
		{
			this.rawFile = isRaw;
		}
	
		public int compareTo(CDTEntry o) 
		{
			if (o.getStartBlock() < super.getStartBlock()) return 1;
			if (o.getStartBlock() > super.getStartBlock()) return -1;
			return 0;
		}
	
		/**
		 * Whether a character is a valid character in an ISO9660 file name.
		 * <br>Capital letters, numbers, and the underscore are valid characters.
		 * @param c Character to check.
		 * @return True - If character is ISO9660 valid.
		 * False - Otherwise.
		 */
		public static boolean isValidCharacter(char c)
		{
			if (c >= 'A' && c <= 'Z') return true;
			if (c == '_') return true;
			if (c >= '0' && c <= '9') return true;
			return false;
		}
		
		public static boolean nameValid(String name)
		{
			int dot = name.indexOf('.');
			int semic = name.indexOf(';');
			String n  = null;
			String e = null;
			//String v = null;
			
			if (dot >= 0 && semic >= 0 && semic > dot)
			{
				n = name.substring(0, dot);
				e = name.substring(dot + 1, semic);
				//v = name.substring(semic + 1);
			}
			else
			{
				if (dot >= 0 && (semic < 0 || semic < dot))
				{
					n = name.substring(0, dot);
					e = name.substring(dot + 1);
				}
				else
				{
					return false;
				}
			}
			
			if (n.length() > 8) return false;
			if (e.length() > 3) return false;
			
			for (int i = 0; i < n.length(); i++)
			{
				if (!isValidCharacter(n.charAt(i))) return false;
			}
			for (int i = 0; i < e.length(); i++)
			{
				if (!isValidCharacter(e.charAt(i))) return false;
			}
			
			return true;
		}
		
		public void setName(String name)
		{
			this.compliantName = nameValid(name);
			int semic = name.indexOf(';');
			String sname = name;
			if (semic >= 0)
			{
				sname = name.substring(0, semic);
			}
			super.setName(sname);
		}
		
		private void printMeTop()
		{
			System.out.println("--- ISO9660 Entry ---");
		}
		
		/**
		 * Print information on this entry to stdout.
		 */
		public void printMe()
		{
			printMeTop();
			System.out.println(super.getInformation());
			System.out.println("\tVolume Sequence Number: " + this.volumeSeqNumber);
			System.out.println("\tIs raw conglomeration file: " + this.rawFile);
			System.out.println("\tIs name compliant: " + this.compliantName);
		}
	}

	/* --- Construction --- */
	
	/**
	 * Construct an empty ISO9660 table with default values for 
	 * instance variables and empty internal collections.
	 */
	public ISO9660Table()
	{
		//System.out.println("ISO9660Table Primary Constructor Called");
		itemMap = new HashMap<String, ISO9660Entry>();
		sectorMap = new LinkedList<ISO9660Entry>();
		firstSector = 150;
		//this.eventContainer = null;
	}
	
	/**
	 * Construct an ISO9660 table and fill it with table information from the provided image.
	 * <br>Assumption: the primary volume descriptor is in relative sector 16. 
	 * It should be if it follows the ISO9660 standard.
	 * @param myISO Image to generate table of.
	 * @throws CDInvalidRecordException If there is an error parsing or finding the table. 
	 * (ie. An apparent record is found that cannot be read).
	 */
	public ISO9660Table(ISO myISO) throws CDInvalidRecordException
	{
		this();
		this.parseFromISO(myISO);
	}
	
	/**
	 * Construct a new ISO9660 table as a separate copy of another table.
	 * @param other Existing table to copy data from.
	 */
	protected ISO9660Table(ISO9660Table other)
	{
		this.itemMap = other.itemMap;
		this.sectorMap = other.sectorMap;
		this.firstSector = other.firstSector;
		this.numSectors = other.numSectors;
		//this.eventContainer = other.eventContainer;
	}

	/* --- Getters --- */
	
	public int getFirstSectorIndex()
	{
		return this.firstSector;
	}
	
	public boolean hasFile(String path)
	{
		return this.itemMap.containsKey(path);
	}
	
	public CDTEntry getEntry(String path)
	{
		if (!this.itemMap.containsKey(path)) return null;
		return this.itemMap.get(path);
	}
	
	public int getFirstSector(String filePath)
	{
		CDTEntry e = this.getEntry(filePath);
		if (e == null) return -1;
		return e.getStartBlock();
	}
	
	public long getItemSize(String filePath)
	{
		CDTEntry e = this.getEntry(filePath);
		if (e == null) return -1;
		return e.getFileSize();
	}
	
	public CDTEntry getEntry(int sector)
	{
		//When checking file sizes, don't forget ignore directory sizes!
		for (ISO9660Entry e : this.sectorMap)
		{
			if (sector < e.getStartBlock()) return null;
			else if (sector >= e.getStartBlock())
			{
				if (sector <= e.lastSector()) return e;
			}
		}
		return null;
	}
	
	public int getNumberSectors()
	{
		return this.numSectors;
	}
	
	/**
	 * Calculate the offset from the file start of the first byte of the provided (relative) sector.
	 * That is to say, if the sector contains data from the file at the given path, retrieve the
	 * offset relative to the start of that file of the beginning of the sector in question.
	 * <br>This can be used to obtain just the data from a single sector having the extracted file and table, 
	 * but not the original image.
	 * @param filePath Path in image file system to desired file.
	 * @param sector Relative index of desired sector.
	 * @return Long integer representing the offset within file to the beginning of the specified sector.
	 * <br>-1 if file cannot be found or if file does not lie within the specified sector.
	 */
	public long calcFileOffsetOfSector(String filePath, int sector)
	{
		ISO9660Entry e = (ISO9660Entry)this.getEntry(filePath);
		if (e == null) return -1;
		
		return e.calcFileOffsetOfSector(sector);
	}

	/**
	 * Get the number of bytes used for data in a given sector.
	 * @param sector Relative index of sector to query data size of.
	 * @return 0x800 (2048) for Form 1 sector.
	 * <br>0x914 (2323) for Form 2 sector.
	 * <br>Other values may be returned as well.
	 */
	public int sectorDataSize(int sector)
	{
		return ISO.F1SIZE;
	}
	
	protected List<ISO9660Entry> getSectorMap()
	{
		return sectorMap;
	}

	
	/* --- Setters --- */
	
	protected void putInSectorMap(ISO9660Entry e)
	{
		this.sectorMap.add(e);
		Collections.sort(this.sectorMap);
	}
	
	protected void putInMainMap(ISO9660Entry e, String path)
	{
		this.itemMap.put(path, e);
	}

	protected void setFirstSecIndex(int first)
	{
		this.firstSector = first;
	}
	
	protected void setNumberSecs(int nSecs)
	{
		this.numSectors = nSecs;
	}
	
	public void addEntry(ISO9660Entry e, String path)
	{
		putInMainMap(e, path);
		putInSectorMap(e);
	}
	
	/* --- Sector Coverage --- */
	
	protected int nextUncoveredSector()
	{
		for (int i = 0; i < this.numSectors; i++)
		{
			if (this.getEntry(i) == null) return i;
		}
		return -1;
	}
	
	protected int calculateSectorsToNextCovered(int sector)
	{
		if (this.getEntry(sector) != null) return 0;
		int s = sector;
		
		while (this.getEntry(s) == null && s < this.numSectors)
		{
			s++;
		}
		
		return s - sector;
	}
	
	/**
	 * Check whether a sector is free or if there is data already already occupying it.
	 * @param sector Relative index of query sector.
	 * @return True - If the sector is open.
	 * <br>False - If the sector is already being used by another file.
	 */
	public boolean isSectorFree(int sector)
	{
		if (sector <= 16) return false;
		return (this.getEntry(sector) == null);
	}
	
	/* --- Parsing/ Conversion --- */
	
 	protected static boolean badName(String name)
 	{
		for (String s : badNames)
		{
			if (name.equals(s)) return true;
		}
		return false;
 	}
	
	private void parseFromISO(ISO myISO) throws CDInvalidRecordException
	{
		this.firstSector = myISO.firstSector();
		this.numSectors = myISO.getNumberSectorsRelative();
		
		Sector vDesc = myISO.getSectorRelative(VOLDESCSEC);
		FileBuffer sDat = vDesc.getData();
		
		sDat.setEndian(true);
		int rdsoff = 0x9C + 0x02 + 0x04;
		int rootDirSec = sDat.intFromFile(rdsoff);
		//if (this.eventContainer != null) eventContainer.fireNewEvent(EventType.TBL9660_ROOTDIRSECFOUND, rootDirSec);
		//System.out.println("ISO9660Table.parseFromISO || rootDirSec found: " + rootDirSec);
		
		this.parseDirectory(myISO, rootDirSec, "");
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.TBL9660_ROOTDIRPARSED, 0);
		//System.out.println("ISO9660Table.parseFromISO || root directory parsed ");
		
		//Create "RAW" entries to cover any sectors not explicitly included in a file.
		
		int us = nextUncoveredSector();
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.TBL9660_UCSECFOUND, us);
		while (us >= 0)
		{
			int l = this.calculateSectorsToNextCovered(us);
			String n = ".RAW" + Integer.toString(us);
			ISO9660Entry rawe = new ISO9660Entry(n, us);
			rawe.setFileSize(l * ISO.F1SIZE);
			rawe.setRawFile(true);
			rawe.setIsDirectory(false);
			this.itemMap.put(n, rawe);
			this.putInSectorMap(rawe);
			
			us = nextUncoveredSector();
			//if (eventContainer != null) eventContainer.fireNewEvent(EventType.TBL9660_UCSECFOUND, us);
		}
		//System.out.println("ISO9660Table.parseFromISO || Uncovered sectors handled ");
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.TBL9660_UCSECSHANDLED, 0);
		//System.out.println("ISO9660Table.parseFromISO || Table parsed");
	}
	
	private void parseDirectory(ISO myISO, int secRel, String dName) throws CDInvalidRecordException
	{
		//System.out.println("ISO9660Table.parseDirectory || Entered -- secRel = " + secRel);
		int cPos = 0;
		boolean ignore = false;
		Sector dirTable = myISO.getSectorRelative(secRel);
		//System.out.println("ISO9660Table.parseDirectory || root directory parsed ");
		//int c = 0;
		
		while (cPos < dirTable.getData().getFileSize())
		{
			//System.out.println("ISO9660Table.parseDirectory || cPos = 0x" + Integer.toHexString(cPos));
			int eLen = Byte.toUnsignedInt(dirTable.getData().getByte(cPos));
			//System.out.println("Entry length read: " + eLen);
			if (eLen <= 0) break;
			ignore = false;
			
			ISO9660Entry e = parseDirectoryEntry(dirTable, cPos);
			//c++;
			cPos += eLen;
			if (badName(e.getName()))
			{
				ignore = true;
				//if (eventContainer != null) eventContainer.fireNewEvent(EventType.TBL9660_ENTRYIGNORED, c, e.getName());	
				//System.out.println("ISO9660Table.parseDirectory || Ignored entry: " + e.getName());
			}
			if (ignore) continue;
			else
			{
				String n = dName + e.getName();
				e.setName(n); //Full path is set as the name
				this.itemMap.put(n, e);
				this.putInSectorMap(e);
				if (e.isDirectory())
				{
					parseDirectory(myISO, e.getStartBlock(), e.getName() + "\\");
				}
			}
			//System.out.println("ISO9660Table.parseDirectory || Entry found: " + e.getName());
		}
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.TBL9660_PARSEDIREND, c, dName);
		//System.out.println("ISO9660Table.parseDirectory || Leaving... ");
	}
	
 	private ISO9660Entry parseDirectoryEntry(Sector s, int offset) throws CDInvalidRecordException
	{
		FileBuffer sDat = s.getData();
		if (offset < 0 || offset >= sDat.getFileSize()) return null;
		
		ISO9660Entry e = new ISO9660Entry(sDat, offset);
		
		return e;
	}
	
 	/**
 	 * Get a collection of all entries in this table.
 	 * Entries are references, the collection is a copy.
 	 * @return A newly generated Collection of entries for all items in this table.
 	 */
 	public Collection<ISO9660Entry> getAllEntries()
 	{
 		return this.itemMap.values();
 	}
	
 	/**
 	 * Print a text representation of the table to stdout.
 	 */
 	public void printTable()
 	{
 		System.out.println("ISO9660Table.printTable()  ----------- ");
 		//System.out.println("Has event container: " + this.eventContainer != null);
 		System.out.println("First sector: " + this.firstSector);
 		System.out.println("Number sectors: " + this.numSectors);
 		System.out.println();
 		System.out.println("ENTRIES (As ordered in sectorMap --- \n");
 		for (ISO9660Entry e : this.sectorMap)
 		{
 			e.printMe();
 			System.out.println();
 		}
 	}
 	
}
