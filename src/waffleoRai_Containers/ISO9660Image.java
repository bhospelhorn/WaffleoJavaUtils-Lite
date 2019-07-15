package waffleoRai_Containers;

import java.io.IOException;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.tree.TreeModel;

import waffleoRai_Containers.CDTable.CDInvalidRecordException;
import waffleoRai_Containers.ISO.Sector;
import waffleoRai_Containers.ISO9660Table.ISO9660Entry;
import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FDBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.VirDirectory;
import waffleoRai_Utils.VirFile;

/*
 * UPDATES
 * 
 * 2017.11.01 | 1.0.1 -> 1.1.0
 * 	Added more FileBuffer compatibility (long over int offset preference)
 * 	Javadoc
 * 	Fixed timestamp problem, added timestamp parsing capability
 * 
 * 2017.11.02 | 1.1.0 -> 1.2.0
 * 	Fixed some issues with parsing and timestamping
 * 2017.11.04 | 1.2.0 -> 1.2.1
 * 	Fixed some directory parsing issues - turned into a Composite Buffer
 * 2017.11.05 | 1.2.1 -> 1.3.0
 * 	Added accessibility methods, especially for child classes
 * 2017.11.18 | 1.3.0 -> 1.4.0
 * 	For compatibility with Java 9, took out all Observer/Observable usage
 */

/**
 * Contains a parsed image of an ISO9660 formatted CD or ISO image derived from an ISO9660 formatted
 * CD.
 * <br>This class allows for quick access to contents of CD by referencing either the file path or
 * the sector index.
 * @author Blythe Hospelhorn
 * @version 1.4.0
 * @since November 18, 2017
 *
 */
public class ISO9660Image implements CDImage{
	
	private ISO9660Table table;
	protected VirDirectory rootDir;
	
	private String stdIdent;
	private String sysIdent;
	private String volIdent;
	private int volSize;
	
	private int volSetSize;
	private int volSeqNumber;
	private int datBlockSize;
	
	private int pathTblSize;
	private int pathTbl1;
	private int pathTbl2;
	private int pathTbl3;
	private int pathTbl4;
	
	private String volSetIdent;
	private String publisherIdent;
	private String dataPrepIdent;
	private String applicationIdent;
	
	private GregorianCalendar dateCreated;
	private String CDXAtag;

	/* --- Construction --- */
	
	/**
	 * Construct an empty ISO9660 image with null table and default virtual directory.
	 * <br>Primarily for use by child classes.
	 */
	protected ISO9660Image()
	{
		table = null;
		rootDir = new VirDirectory("", '\\');
	}
	
	/**
	 * Construct a parsed ISO9660 image from a raw ISO image object.
	 * @param myISO Raw image to parse.
	 * @throws CDInvalidRecordException If a record in one of the directory tables is unreadable
	 * or invalid. This can occur from bad formatting or from erroneous offset calculations.
	 * @throws IOException If there is an error creating parsing buffers.
	 * @throws UnsupportedFileTypeException If there is an error parsing volume information. 
	 */
	public ISO9660Image(ISO myISO) throws CDInvalidRecordException, IOException, UnsupportedFileTypeException
	{
		this.constructorCore(myISO);
	}
	
	/**
	 * Construct a parsed ISO9660 image from a raw ISO image object.
	 * Include a set of Observers to monitor parsing progress.
	 * @param myISO Raw image to parse.
	 * @param obs Array of Observers (listeners) to include. 
	 * @throws CDInvalidRecordException If a record in one of the directory tables is unreadable
	 * or invalid. This can occur from bad formatting or from erroneous offset calculations.
	 * @throws IOException If there is an error creating parsing buffers.
	 * @throws UnsupportedFileTypeException If there is an error parsing volume information. 
	 */
	/*public ISO9660Image(ISO myISO, Observer[] obs) throws CDInvalidRecordException, IOException, UnsupportedFileTypeException
	{
		this.eventContainer = myISO.getEventContainer();
		if (this.eventContainer == null) this.eventContainer = new ISOReadEvent();
		for (Observer o : obs) this.eventContainer.addObserver(o);
		this.constructorCore(myISO);
	}*/
	
	/**
	 * Set and instantiate instance variable defaults. Defers image to parsing method.
	 * Generate table and use for further parsing.
	 * @param myISO Raw image to parse.
	 * @throws CDInvalidRecordException If a record in one of the directory tables is unreadable
	 * or invalid. This can occur from bad formatting or from erroneous offset calculations.
	 * @throws IOException If an internal file is large enough to require disk-aided streaming and there
	 * is an error creating the buffer necessary.
	 * @throws UnsupportedFileTypeException If there is an error parsing volume information.
	 */
	private void constructorCore(ISO myISO) throws CDInvalidRecordException, IOException, UnsupportedFileTypeException
	{
		table = new ISO9660Table(myISO);
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.IMG9660_TABLEGENERATED, 0);
		System.out.println("ISO9660Image.constructorCore || Image table parsed.");
		rootDir = new VirDirectory("", '\\');
		this.generateRootDirectory(myISO, table);
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.IMG9660_VIRDIRPOPULATED, 0);
		System.out.println("ISO9660Image.constructorCore || Image directory tree parsed.");
		this.readInformation(myISO);
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.IMG9660_INFOREAD, 0, this.volIdent);
		System.out.println("ISO9660Image.constructorCore || Information read.");
	}
	
	/**
	 * Once table has been parsed, extract the files to a virtual directory whose
	 * structure mimics the image's original file structure.
	 * @param myISO Raw image to parse.
	 * @param t Image's table - use to locate and extract files and directories.
	 * @throws IOException If an internal file is large enough to require disk-aided streaming and there
	 * is an error creating the buffer necessary.
	 */
	protected void generateRootDirectory(ISO myISO, ISO9660Table t) throws IOException
	{
		Collection<ISO9660Entry> c = t.getAllEntries();
		//if (eventContainer != null) eventContainer.fireNewEvent(EventType.IMG9660_TBLLISTED, c.size());
		for (ISO9660Entry e : c)
		{
			if (!e.isDirectory() && e.getStartBlock() < myISO.getNumberSectorsRelative()) 
			{
				//System.out.println("ISO9660Image.generateRootDirectory || File " + e.getName() + " is not a directory.");
				//Generate new file
				//FileBuffer compFile = FileBuffer.createWritableBuffer("ISO9660Image.generateRootDirectory", e.getFileSize(), true);
				CompositeBuffer compBuffer = new CompositeBuffer(e.sizeInSectors());
				int s = e.getStartBlock();
				//System.out.println("ISO9660Image.generateRootDirectory || Start block: " + s);
				long left = e.getFileSize();
				Sector sec = myISO.getSectorRelative(s);
				//Nab full sectors
				//System.out.println("ISO9660Image.generateRootDirectory || Entry information... ");
				e.printMe();
				//System.out.println("ISO9660Image.generateRootDirectory || s = " + s + " left = " + left);
				while (left >= sec.getData().getFileSize())
				{
					//System.out.println("ISO9660Image.generateRootDirectory || While loop: s = " + s + " left = " + left);
					//compFile.addToFile(sec.getData());
					compBuffer.addToFile(sec.getData());
					//System.out.println("ISO9660Image.generateRootDirectory || Sector added to composite.");
					s++;
					//left = e.getFileSize() - compFile.getFileSize();
					left = e.getFileSize() - compBuffer.getFileSize();
					
					if (s < myISO.getNumberSectorsRelative()) sec = myISO.getSectorRelative(s);
					else break;
					//System.out.println("ISO9660Image.generateRootDirectory || Next sector retrieved.");
				}
				//System.out.println("ISO9660Image.generateRootDirectory || Full sectors copied...");
				//System.out.println("ISO9660Image.generateRootDirectory || Last sector: " + s);
				/*If the amount left is less than a full sector (breaking above loop,
				 * but the amount currently copied to the buffer is still less than the full file size.
				 * ie. if there is a partial sector at the end*/
				//if (compFile.getFileSize() < e.getFileSize())
				if (compBuffer.getFileSize() < e.getFileSize())
				{
					//System.out.println("Current comp size: " + compFile.getFileSize() + " Target size: " + e.getFileSize());
					FileBuffer lastDat = myISO.getSectorRelative(e.lastSector()).getData();
					long lastPos = e.getFileSize() - compBuffer.getFileSize();
					FileBuffer subSec = lastDat.createReadOnlyCopy(0, lastPos);
					compBuffer.addToFile(subSec);
				}
				//System.out.println("ISO9660Image.generateRootDirectory || Partial sectors copied...");
				//this.rootDir.addItem(compFile, e.getName());
				compBuffer.updateFileSize();
				this.rootDir.addItem(compBuffer, e.getName());
				//if (eventContainer != null) eventContainer.fireNewEvent(EventType.IMG9660_FILEADDED, 0, e.getName());
				//System.out.println("ISO9660Image.generateRootDirectory || File " + e.getName() + " added to virtual directory.");
			}
		}
	}
	
	/**
	 * Parse basic ISO9660 volume information (such as volume name and timestamp).
	 * <br>ASSUMPTION: The Primary Volume Descriptor is located in relative sector 16 (ISO9660 standard!)
	 * @param myISO Raw image to parse.
	 * @throws UnsupportedFileTypeException If there is an error parsing volume information.
	 */
	protected void readInformation(ISO myISO) throws UnsupportedFileTypeException
	{
		int cPos = 1;
		FileBuffer pvd = myISO.getSectorRelative(16).getData();
		pvd.setEndian(true);
	
		this.stdIdent = pvd.getASCII_string(cPos, 5);
		cPos += 7;
		
		this.sysIdent = pvd.getASCII_string(cPos, 32);
		cPos += 32;
		
		this.volIdent = pvd.getASCII_string(cPos, 32);
		cPos += 32 + 8 + 4;
		
		this.volSize = pvd.intFromFile(cPos);
		cPos += 4 + 32 + 2;
		
		this.volSetSize = Short.toUnsignedInt(pvd.shortFromFile(cPos));
		cPos += 2 + 2;
		
		this.volSeqNumber = Short.toUnsignedInt(pvd.shortFromFile(cPos));
		cPos += 2 + 2;
		
		this.datBlockSize = Short.toUnsignedInt(pvd.shortFromFile(cPos));
		cPos += 2 + 4;
		
		this.pathTblSize = pvd.intFromFile(cPos);
		cPos += 4;
		pvd.setEndian(false);
		
		this.pathTbl1 = pvd.intFromFile(cPos);
		cPos += 4;
		
		this.pathTbl2 = pvd.intFromFile(cPos);
		cPos += 4;
		pvd.setEndian(true);
		
		this.pathTbl3 = pvd.intFromFile(cPos);
		cPos += 4;
		
		this.pathTbl4 = pvd.intFromFile(cPos);
		cPos += 4 + 34;
		
		this.volSetIdent = pvd.getASCII_string(cPos, 128);
		cPos += 128;
		
		this.publisherIdent = pvd.getASCII_string(cPos, 128);
		cPos += 128;
		
		this.dataPrepIdent = pvd.getASCII_string(cPos, 128);
		cPos += 128;
		
		this.applicationIdent = pvd.getASCII_string(cPos, 128);
		cPos += 128 + (37 * 3);
		
		//Insert 17 byte timestamp read here
		GregorianCalendar time = FileBuffer.getVanillaTimestamp();
		String y = pvd.getASCII_string(cPos, 4); cPos += 4;
		String m = pvd.getASCII_string(cPos, 2); cPos += 2;
		String d = pvd.getASCII_string(cPos, 2); cPos += 2;
		String h = pvd.getASCII_string(cPos, 2); cPos += 2;
		String i = pvd.getASCII_string(cPos, 2); cPos += 2;
		String n = pvd.getASCII_string(cPos, 2); cPos += 4;
		byte tz = pvd.getByte(cPos); cPos++;
		try
		{
			int year = Integer.parseInt(y);
			int month = Integer.parseInt(m) - 1;
			int date = Integer.parseInt(d);
			int hourOfDay = Integer.parseInt(h);
			int minute = Integer.parseInt(i);
			int second = Integer.parseInt(n);
			time.set(year, month, date, hourOfDay, minute, second);
			int itz = (int)tz;
			itz *= 15 * 60 * 1000;
			String[] possibleZones = TimeZone.getAvailableIDs(itz);
			if (possibleZones != null && possibleZones.length > 0)
			{
				time.setTimeZone(TimeZone.getTimeZone(possibleZones[0]));
			}
		}
		catch(NumberFormatException ex)
		{
			throw new FileBuffer.UnsupportedFileTypeException();
		}
		this.dateCreated = time;
		
		cPos += (17 * 3) + 2 + 141;
		
		this.CDXAtag = pvd.getASCII_string(cPos, 8);	
	}
	
	/* --- Getters --- */
	
	/**
	 * Retrieve image directory table.
	 * Table contains information on what files and directories are present on image
	 * and how they were encoded in the original image.
	 * @return Image table as an ISO9660Table.
	 */
	public ISO9660Table getTable()
	{
		return this.table;
	}
	
	/**
	 * Get a collection containing all image table entries. Collection should contain
	 * an entry for each file and directory in the image.
	 * @return Collection of ISO9660Entry objects, or all table entries in the image.
	 */
	public Collection<ISO9660Entry> getTableEntries()
	{
		return this.getTable().getAllEntries();
	}
	
	public FileBuffer getFile(String path)
	{
		FDBuffer f = this.rootDir.getItem(path);
		if (f == null) return null;
		if (!(f instanceof VirFile)) return null;
		VirFile vf = (VirFile)f;	
		return vf.getFile();
	}
	
	public FileBuffer getSectorData(int sector) throws IOException
	{
		ISO9660Entry e = (ISO9660Entry)(this.getTable().getEntry(sector));
		if (e == null) return null;
		long sOff = this.getTable().calcFileOffsetOfSector(e.getName(), sector);
		FileBuffer mySec = this.getFile(e.getName());
		if (mySec == null) return null;
		if (sOff > mySec.getFileSize() || sOff < 0) return null;
		mySec = mySec.createReadOnlyCopy(sOff, this.getTable().sectorDataSize(sector));
		
		return mySec;
	}

	public FileBuffer getRawSector(int relativeSector) throws IOException
	{
		//This version only deals with Mode 1.
		int absSec = relativeSector + table.getFirstSectorIndex();
		FileBuffer secHeader = new FileBuffer(0x10);
		for (int i = 0; i < ISO.SYNC.length; i++) secHeader.addToFile(ISO.SYNC[i]);
		byte minByte = ISO.getBCDminute(absSec);
		secHeader.addToFile(minByte);
		byte sndByte = ISO.getBCDsecond(absSec);
		secHeader.addToFile(sndByte);
		byte secByte = ISO.getBCDsector(absSec);
		secHeader.addToFile(secByte);
		byte modeByte = 0x01;
		secHeader.addToFile(modeByte);
		int tailSize = 0x04 + 0x08 + 0x114;
		FileBuffer secTail = new FileBuffer(tailSize);
		for (int i = 0; i < tailSize; i++) secTail.addToFile(ISO.ZERO);
		FileBuffer mySector = new CompositeBuffer(3);
		mySector.addToFile(secHeader);
		mySector.addToFile(getSectorData(relativeSector));
		mySector.addToFile(secTail);
		return mySector;
	}
	
	public VirDirectory getRootDirectory()
	{
		return this.rootDir;
	}
	
		//---------------------------------------
	
	/**
	 * Get the standard identification string of the image volume.
	 * @return Standard identification String.
	 */
	public String getStandardIdent()
	{
		return this.stdIdent;
	}
	
	/**
	 * Get the system identifier string embedded into the CD data.
	 * <br>This string may simply be the name of the platform or hardware system the CD was intended
	 * to be run on.
	 * @return System identifier string.
	 */
	public String getSystemIdent()
	{
		return this.sysIdent;
	}
	
	/**
	 * Get the string representing the name of the CD volume the image was made from.
	 * <br>The volume identifier is essentially the internal name of the CD.
	 * @return Volume identifier string.
	 */
	public String getVolumeIdent()
	{
		return this.volIdent;
	}
	
	/**
	 * Get the number of sectors/blocks used by the image of this volume. The total size in bytes
	 * of the data in this image can be calculated from this value.
	 * @return Size of image in sectors.
	 */
	public int getVolumeSize()
	{
		return this.volSize;
	}
	
	/**
	 * Get the number of volumes in the set the volume this image was made from is part of.
	 * @return Number of volumes in set.
	 */
	public int getVolumeSetSize()
	{
		return this.volSetSize;
	}
	
	/**
	 * Get the series number of the volume this image was made from in its set.
	 * @return Volume sequence number
	 */
	public int getVolumeSequenceNumber()
	{
		return this.volSeqNumber;
	}
	
	/**
	 * Get the size in bytes of the data in each sector of this image.
	 * @return The size, in bytes, of the data in each sector.
	 * <br>This will usually be 0x800 (2048 bytes).
	 */
	public int getDataBlockSize()
	{
		return this.datBlockSize;
	}
	
	/**
	 * Size of the image's path table. 
	 * <br>The path table is not the same as a directory table.
	 * Not all images have a useful path table. It is not essential for parsing the directory tree;
	 * it is primarily intended for quicker access of files.
	 * <br>As such, the path table is not parsed by default. However with the pertinent volume information, it
	 * can be parsed by outside classes if so desired.
	 * @return The size (in bytes) of the path table.
	 */
	public int getPathTableSize()
	{
		return this.pathTblSize;
	}
	
	/**
	 * Get the relative index of the first sector of Path Table 1.
	 * <br>The path table is not the same as a directory table.
	 * Not all images have a useful path table. It is not essential for parsing the directory tree;
	 * it is primarily intended for quicker access of files.
	 * <br>As such, the path table is not parsed by default. However with the pertinent volume information, it
	 * can be parsed by outside classes if so desired.
	 * @return The relative index of the first sector of Path Table 1.
	 */
	public int getPathTable_1_start()
	{
		return this.pathTbl1;
	}
	
	/**
	 * Get the relative index of the first sector of Path Table 2.
	 * <br>The path table is not the same as a directory table.
	 * Not all images have a useful path table. It is not essential for parsing the directory tree;
	 * it is primarily intended for quicker access of files.
	 * <br>As such, the path table is not parsed by default. However with the pertinent volume information, it
	 * can be parsed by outside classes if so desired.
	 * @return The relative index of the first sector of Path Table 2.
	 */
	public int getPathTable_2_start()
	{
		return this.pathTbl2;
	}
	
	/**
	 * Get the relative index of the first sector of Path Table 3.
	 * <br>The path table is not the same as a directory table.
	 * Not all images have a useful path table. It is not essential for parsing the directory tree;
	 * it is primarily intended for quicker access of files.
	 * <br>As such, the path table is not parsed by default. However with the pertinent volume information, it
	 * can be parsed by outside classes if so desired.
	 * @return The relative index of the first sector of Path Table 3.
	 */
	public int getPathTable_3_start()
	{
		return this.pathTbl3;
	}
	
	/**
	 * Get the relative index of the first sector of Path Table 4.
	 * <br>The path table is not the same as a directory table.
	 * Not all images have a useful path table. It is not essential for parsing the directory tree;
	 * it is primarily intended for quicker access of files.
	 * <br>As such, the path table is not parsed by default. However with the pertinent volume information, it
	 * can be parsed by outside classes if so desired.
	 * @return The relative index of the first sector of Path Table 4.
	 */
	public int getPathTable_4_start()
	{
		return this.pathTbl4;
	}
	
	/**
	 * Get the name of the set the volume this image was made from.
	 * @return Volume set identifier string. May be null or empty.
	 */
	public String getVolumeSetIdent()
	{
		return this.volSetIdent;
	}
	
	/**
	 * Get the name of the publisher of the CD this image was made from.
	 * @return Publisher identifier string.
	 */
	public String getPublisherIdent()
	{
		return this.publisherIdent;
	}
	
	/**
	 * Get the name of the data preparer of the CD this image was made from.
	 * @return Data preparer string. May be null or empty.
	 */
	public String getDataPrepIdent()
	{
		return this.dataPrepIdent;
	}
	
	/**
	 * Get the name of the application used to create the CD this image was made from.
	 * @return Application identifier string.
	 */
	public String getApplicationIdent()
	{
		return this.applicationIdent;
	}
	
	/**
	 * Get the volume creation timestamp.
	 * @return GregorianCalendar object containing the timestamp. Calendar lacks fraction of second
	 * information.
	 */
	public GregorianCalendar getDateCreated()
	{
		return this.dateCreated;
	}

	/**
	 * The 8-byte ASCII string found at offset 0x400 from primary volume descriptor sector
	 * start. If this volume is XA (eXtended Architecture) encoded, this string should be 
	 * something akin to "CD-XA001".
	 * @return CD-XA tag string, if present.
	 */
	public String getCDXAtag()
	{
		return this.CDXAtag;
	}
	
	/* --- Setters --- */
	
	/**
	 * Add a file and the entry containing information about where it is located on the image
	 * to the image.
	 * <br>This method is suggested for use by custom parsers, not for building new images.
	 * @param myFile File to add.
	 * @param myEntry Entry correlating to file. This may be modified as needed for the file's
	 * location on disk to make sense.
	 * @param path Path of file relative to image file structure.
	 * @return Entry for file if successful. 
	 * <br>null if addition was not successful.
	 */
	public ISO9660Entry addFile(FileBuffer myFile, ISO9660Entry myEntry, String path)
	{
		myEntry.setName(path);
		int startSec = myEntry.getStartBlock();
		int endSec = startSec + myEntry.getSizeInSectors();
		//boolean fits = true;
		for (int i = startSec; i < endSec; i++)
		{
			if (!table.isSectorFree(i)) return null;
		}
		table.putInMainMap(myEntry, path);
		table.putInSectorMap(myEntry);
		rootDir.addItem(myFile, path);
		return myEntry;
	}
	
	/* --- Checks --- */
	
 	public boolean fileExists(String path)
	{
		return this.rootDir.itemExists(path);
	}
	
 	protected void printMyBasicInfo()
 	{
 		System.out.println("Standard Ident: " + this.stdIdent);
 		System.out.println("System Ident: " + this.sysIdent);
 		System.out.println("Volume Ident: " + this.volIdent);
 		System.out.println("Volume Size: " + this.getVolumeSize() + " sectors");
 		System.out.println("Volume Set Size: " + this.getVolumeSetSize() + " volumes");
 		System.out.println("Volume Sequence Number: " + this.getVolumeSequenceNumber());
 		System.out.println("Data Block Size: 0x" + Integer.toHexString(this.datBlockSize) + " (" + this.datBlockSize + " bytes)");
 		System.out.println("Path Table Size: " + this.getPathTableSize() + " bytes");
 		System.out.println("Path Table 1 Start: Sector " + this.getPathTable_1_start());
 		System.out.println("Path Table 2 Start: Sector " + this.getPathTable_2_start());
 		System.out.println("Path Table 3 Start: Sector " + this.getPathTable_3_start());
 		System.out.println("Path Table 4 Start: Sector " + this.getPathTable_4_start());
 		System.out.println("Volume Set Ident: " + this.volSetIdent);
 		System.out.println("Publisher Ident: " + this.publisherIdent);
 		System.out.println("Data Prepper Ident: " + this.dataPrepIdent);
 		System.out.println("Application Ident: " + this.applicationIdent);
 		System.out.println("Timestamp: " + FileBuffer.formatTimeAmerican(this.dateCreated));
 		System.out.println("XA Tag: " + this.CDXAtag);
 		//System.out.println("Has Event Container: " + (this.eventContainer != null));
 		System.out.println();
 	}
 	
 	protected void printMyTable()
 	{
 		System.out.println("----- TABLE ----- ");
 		System.out.println();
 		this.table.printTable();
 		System.out.println();
 	}
 	
 	protected void printMyDirectory()
 	{
 		System.out.println("----- FILE HIERARCHY ----- ");
 		System.out.println();
 		this.rootDir.printTree(0);
 		System.out.println();
 	}
 	
 	public void printMe()
 	{
 		System.out.println("ISO9660 IMAGE ================== ");
 		printMyBasicInfo();
 		printMyTable();
 		printMyDirectory();
 	}
 	
 	/* --- Conversion --- */
 	
 	public TreeModel getDirectoryTree()
 	{
 		if (this.rootDir == null) return null;
 		return this.rootDir.toTreeModel();
 	}
 	
}
