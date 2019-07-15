package waffleoRai_Containers;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import waffleoRai_Containers.XATable.XASubmode;
import waffleoRai_Utils.BitStreamer;
import waffleoRai_Utils.FileBuffer;

/*UPDATE: 2017.08.31
 * 	Tweaked for compatibility with StreamBuffer class and FileBuffer alterations
 * 
 * 2017.10.30 | 2.0.0 -> 2.0.1
 * 	Javadoc annotation
 * 	Added requirement for data-only write to not have any M2F2 sectors.
 * 2017.11.18 | 2.0.1 -> 2.1.0
 * 	For Java 9 compatibility, removed all Observer/Observable pieces
 * */

/**
 * Class to hold basic ISO image information - split by sector.
 * This does not parse the directory tree or file information at all.
 * @author Blythe Hospelhorn
 * @version 2.1.0
 * @since November 18, 2017
 */
public class ISO {
	
	public static final byte ZERO = 0x00;
	public static final byte MAXS = 0x7F;
	public static final byte MAXU = (byte)0xFF;
	public static final byte[] SYNC = {ZERO, MAXU, MAXU, MAXU,
									   MAXU, MAXU, MAXU, MAXU,
									   MAXU, MAXU, MAXU, ZERO};
	public static final int F1SIZE = 0x800;
	public static final int F2SIZE = 0x914;
	public static final int SECSIZE = 0x930;
	public static final int DEFO_FIRSTSEC = 150;
	
	private static FileBuffer zeroSector = null;
	
	private Sector[] sectors;
	private int firstSector;
	
	private boolean loosedata;
	
	/* ----- Helper Objects ----- */
	
	/**
	 * Inner Interface for a sector object - holds raw information pertaining to a 
	 * CD/ ISO sector (2352 bytes standard)
	 * @author Blythe Hospelhorn
	 * @since August 31, 2017
	 */
	public static interface Sector
	{
		/**
		 * Generate a serialized version of the sector including an ISO9660 formatted header.
		 * At the moment, fills error checking fields with zero.
		 * @param sectornum : Absolute sector number (for ISO sector header)
		 * @return FileBuffer size 2352 bytes (0x930) containing full sector
		 */
		public FileBuffer serializeSector(int sectornum);
		
		/**
		 * Gets a FileBuffer containing only the data from this sector. Typically, this will
		 * be 2048 bytes, though because this is also meant to handle Mode 2, it may be slightly larger.
		 * @return FileBuffer containing sector data
		 */
		public FileBuffer getData();
		
		/**
		 * Returns the ISO standard mode of the sector as a simple int.
		 * @return 
		 * 0 = Mode 0 <br> 
		 * 1 = Mode 1 <br>
		 * 2 = Mode 2 <br>
		 * -1 = Raw/Unknown
		 */
		public int getMode();
		
		/**
		 * Sets the new sector data to a reference to the specified FileBuffer.
		 * WILL adjust the input FileBuffer (including making a new copy) to be
		 * the sector type's correct data size if the input FileBuffer does not match it.
		 * @param newData : FileBuffer to reference
		 * @throws IOException : If, in an attempt to generate a new stream, there is an error reading
		 * an input file from disk.
		 * @throws UnsupportedOperationException : If the sector data is not settable
		 */
		public void setData(FileBuffer newData) throws IOException;
		
		/**
		 * Set data by copying into a fresh FileBuffer rather than referencing an existing one.
		 * Good for enabling writing.
		 * Will always copy the same amount of bytes as Sector data length.
		 * Specifying len, if shorter than Sector data length, will force it to fill in anything after
		 * that with zero bytes.
		 * @param newData : FileBuffer to copy from
		 * @param stPos : Offset to start copying from
		 * @param len : Number of bytes in newData to copy
		 */
		public void copyData(FileBuffer newData, long stPos, long len);

	}
	
	/**
	 * Mode 0 Sector - Contains a reference to a 2048-byte 0 filled sector
	 * @author Blythe Hospelhorn
	 * @since August 31, 2017
	 */
	public static class SectorM0 implements Sector
	{
		
		public FileBuffer serializeSector(int sectornum)
		{
			FileBuffer mySector = new FileBuffer(SECSIZE);
			for (int i = 0; i < SYNC.length; i++) mySector.addToFile(SYNC[i]);
			mySector.addToFile(getBCDminute(sectornum));
			mySector.addToFile(getBCDsecond(sectornum));
			mySector.addToFile(getBCDsector(sectornum));
			mySector.addToFile(ZERO);
			mySector.addToFile(zeroSector);
			return mySector;
		}
		
		public FileBuffer getData()
		{
			return zeroSector;
		}
		
		public int getMode()
		{
			return 0;
		}
		
		public void setData(FileBuffer newData)
		{
			throw new UnsupportedOperationException();
		}
		
		public void copyData(FileBuffer newData, long stPos, long len)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Mode 1 Sector - Contains a reference to a 2048-byte FileBuffer as its data.
	 * @author Blythe Hospelhorn
	 * @since August 31, 2017
	 */
	public static class SectorM1 implements Sector
	{
		private FileBuffer data;
		
		/**
		 * Standard Mode 1 Sector constructor.
		 * Sets data reference to null.
		 */
		public SectorM1()
		{
			this.data = null;
		}
		
		public FileBuffer serializeSector(int sectornum)
		{
			FileBuffer mySector = new FileBuffer(SECSIZE, true);
			//12 bytes sync
			for (int i = 0; i < SYNC.length; i++) mySector.addToFile(SYNC[i]);
			
			//4 bytes header
			mySector.addToFile(getBCDminute(sectornum));
			mySector.addToFile(getBCDsecond(sectornum));
			mySector.addToFile(getBCDsector(sectornum));
			mySector.addToFile((byte)0x01);
			
			//2048 bytes data
			mySector.addToFile(this.data);
			
			//4 bytes EDC
			//(Right now just adds 4 ZERO bytes)
			mySector.addToFile(this.generateEDC());
			
			//8 bytes ZERO
			for (int i = 0; i < 8; i++) mySector.addToFile(ZERO);
			
			//276 bytes ECC
			//(Right now just adds ZERO bytes)
			mySector.addToFile(this.generateECC());
			
			return mySector;
		}
		
		public FileBuffer getData()
		{
			return this.data;
		}
		
		public int getMode()
		{
			return 1;
		}
		
		public void setData(FileBuffer newData) throws IOException
		{
			if (newData.getFileSize() > F1SIZE)
			{
				this.data = newData.createReadOnlyCopy(0, F1SIZE);
			}
			else if (newData.getFileSize() < F1SIZE)
			{
				this.copyData(newData, 0, newData.getFileSize());
			}
			else
			{
				this.data = newData;
			}
		}
		
		public void copyData(FileBuffer newData, long stPos, long len)
		{
			//Will always copy exactly 0x800 bytes
			if (this.data == null) this.data = new FileBuffer(F1SIZE);
			if (stPos < 0) return;
			if (stPos + len > newData.getFileSize()) return;
			
			for (int i = 0; i < F1SIZE; i++)
			{
				if (stPos + i < len)
				{
					this.data.addToFile(newData.getByte(stPos + i));
				}
				else this.data.addToFile(ZERO);
			}
			
		}
		
		public int generateEDC()
		{
			int EDC = 0;
			return EDC;
		}
		
		public FileBuffer generateECC()
		{
			FileBuffer ECC = new FileBuffer(0x114, true);
			for (int i = 0; i < 0x114; i++) ECC.addToFile(ZERO);
			return ECC;
		}
	}
	
	/**
	 * Mode 2 Sector - Contains a reference to a FileBuffer as its data. Data will either
	 * be 2048 [0x800] bytes in size (Form 1) or 2324 [0x914] bytes in size (Form 2).
	 * @author Blythe Hospelhorn
	 * @since August 31, 2017
	 * @version 1.0.0
	 */
	public static class SectorM2 implements Sector
	{
		private boolean isForm2;
		
		private int fileNumber;
		private int channelNumber;
		
		private boolean isEOR;
		private XASubmode submode;
		private boolean trigger;
		private boolean isRealTime;
		private boolean isEOF;
		private byte CI; //Just the raw byte. Downstream parsers can interpret.
		
		private FileBuffer data;
		
		/* --- Construction --- */
		
		/**
		 * Construct a vanilla Mode 2 Form 1 data sector.
		 */
 		public SectorM2()
		{
			this.isForm2 = false;
			this.fileNumber = -1;
			this.channelNumber = -1;
			this.isEOR = false;
			this.submode = XASubmode.DATA;
			this.trigger = false;
			this.isRealTime = false;
			this.isEOF = false;
			this.CI = 0;
			this.data = null;
		}
 		
 		/* --- Getters --- */
 		
		public FileBuffer getData()
		{
			return this.data;
		}
		
		public int getMode()
		{
			return 2;
		}
		
		/**
		 * Get the size of the sector *data* in bytes. This is determined by the Form.
		 * @return
		 * 0x800 (2048) if Form 1 <br>
		 * 0x914 (2324) if Form 2
		 */
		public int dataSize()
		{
			if (this.isForm2) return F2SIZE;
			else return F1SIZE;
		}
		
		/**
		 * Get whether this Mode 2 sector is Form 2.
		 * @return
		 * True if Form 2 <br>
		 * False if Form 1
		 */
		public boolean isForm2()
		{
			return this.isForm2;
		}
 		
		/**
		 * Get the file number embedded in Mode 2 sector sub-header.
		 * @return
		 * A number 0x00 - 0xFF (1 byte).
		 */
		public int getFileNumber()
		{
			return this.fileNumber;
		}
		
		/**
		 * Get the channel number embedded in Mode 2 sector sub-header.
		 * @return
		 * A number 0x00 - 0xFF (1 byte).
		 */
		public int getChannelNumber()
		{
			return this.channelNumber;
		}
		
		/**
		 * Get End-of-Record flag embedded in Mode 2 sector sub-header.
		 * @return
		 * Submode EOR flag (boolean). <br>
		 * True - If sector is end of record. <br>
		 * False - If sector is not end of record.
		 */
		public boolean isEOR()
		{
			return this.isEOR;
		}
		
		/**
		 * Get sector submode (DATA, AUDIO, or VIDEO)
		 * @return
		 * A XASubmode enumeration detailing which submode this sector is set to in the sub-header.
		 */
		public XASubmode getSubmode()
		{
			return this.submode;
		}
		
		/**
		 * Get Trigger flag embedded in Mode 2 sector sub-header.
		 * @return
		 * Submode Trigger flag (boolean).
		 */
		public boolean hasTrigger()
		{
			return this.trigger;
		}
		
		/**
		 * Get Realtime flag embedded in Mode 2 sector sub-header.
		 * @return
		 * Submode Realtime flag (boolean).
		 */
		public boolean isRealTime()
		{
			return this.isRealTime;
		}
		
		/**
		 * Get End-of-File flag embedded in Mode 2 sector sub-header.
		 * @return
		 * Submode EOF flag (boolean). <br>
		 * True - If sector is end of file. <br>
		 * False - If sector is not end of file.
		 */
		public boolean isEOF()
		{
			return this.isEOF;
		}
		
		/**
		 * Get raw CodingInfo byte from Mode 2 sector sub-header.
		 * @return
		 * A byte for audio and video sector streaming information. <br><br>
		 * For PSX XA-ADPCM, the following flags are encoded in the CI byte: <br>
		 * 0 : [On] Stereo | [Off] Mono <br>
		 * 2 : [On] 18900Hz SR | [Off] 37800Hz SR <br>
		 * 4 : [On] 8-bit | [Off] 4-bit <br>
		 * 6 : Emphasis <br>
		 */
		public byte getCI()
		{
			return this.CI;
		}
		
 		/* --- Setters --- */
		
		public void setData(FileBuffer newData) throws IOException
		{
			long newSize = newData.getFileSize();
			if (newSize == dataSize()) this.data = newData;
			else if (newSize < dataSize()) this.copyData(newData, 0, newSize);
			else this.data = newData.createReadOnlyCopy(0, dataSize());
		}
		
		public void copyData(FileBuffer newData, long stPos, long len)
		{
			this.data = new FileBuffer(dataSize());
			for (int i = 0; i < dataSize(); i++)
			{
				if (stPos + i < newData.getFileSize())
				{
					this.data.addToFile(newData.getByte(stPos + i));
				}
			}
		}
		
		/**
		 * Set submode flags with XA Mode 2 formatted submode byte. This function parses
		 * the byte into the component boolean flags.
		 * @param SM The submode byte from a Mode 2 sub-header.
		 */
		public void setSubModeInfo(byte SM)
		{
			this.isEOR = BitStreamer.readABit(SM, 0);
			this.trigger = BitStreamer.readABit(SM, 4);
			this.isForm2 = BitStreamer.readABit(SM, 5);
			this.isRealTime = BitStreamer.readABit(SM, 6);
			this.isEOF = BitStreamer.readABit(SM, 7);
			
			boolean V = BitStreamer.readABit(SM, 1);
			boolean A = BitStreamer.readABit(SM, 2);
			boolean D = BitStreamer.readABit(SM, 3);
			
			if (V && !A && !D) this.submode = XASubmode.VIDEO;
			else if (!V && A && !D) this.submode = XASubmode.AUDIO;
			else if (!V && !A && D) this.submode = XASubmode.DATA;
			else this.submode = null;
		}
 		
		/**
		 * Set the sector's file number by inputting a File Number byte as would be found
		 * in a Mode 2 sub-header.
		 * @param FN File number byte from a Mode 2 sub-header.
		 */
		public void setFileNumber(byte FN)
		{
			this.fileNumber = (int)FN;
		}
		
		/**
		 * Set the sector's channel number by inputting a Channel Number byte as would be found
		 * in a Mode 2 sub-header.
		 * @param CN Channel number byte from a Mode 2 sub-header.
		 */
		public void setChannelNumber(byte CN)
		{
			this.channelNumber = (int)CN;
		}
		
		/**
		 * Set the coding info byte as found in Mode 2 subheader.
		 * This method does not parse the coding info.
		 * @param CI Coding info byte from a Mode 2 sub-header.
		 */
		public void setCI(byte CI)
		{
			this.CI = CI;
		}
		
 		/* --- Serialization --- */
		
		public FileBuffer serializeSector(int sectornum)
		{
			FileBuffer mySector = new FileBuffer(SECSIZE);
			
			//Sync pattern
			for (int i = 0; i < SYNC.length; i++) mySector.addToFile(SYNC[i]);
			
			//Header
			mySector.addToFile(getBCDminute(sectornum));
			mySector.addToFile(getBCDsecond(sectornum));
			mySector.addToFile(getBCDsector(sectornum));
			mySector.addToFile((byte)0x02);
			
			byte SM = generateSMbyte();
			
			//Subheader (x2)
			for (int i = 0; i < 2; i++)
			{
				mySector.addToFile((byte)fileNumber);
				mySector.addToFile((byte)channelNumber);
				mySector.addToFile(SM);
				mySector.addToFile(this.CI);
			}

			//Data
			mySector.addToFile(this.data);
			
			//EDC
			for (int i = 0; i < 4; i++) mySector.addToFile(ZERO);
			
			//ECC (if form 1)
			if (!this.isForm2)
			{
				for (int i = 0; i < 0x114; i++) mySector.addToFile(ZERO);
			}
			
			return mySector;
		}
		
		/**
		 * Serialize this sector's submode flags into a single byte for encoding into
		 * a Mode 2 sub-header.
		 * @return A Mode 2 submode byte derived from sector's submode flags.
		 */
		private byte generateSMbyte()
		{
			byte SM = 0;
			
			if (isEOR) BitStreamer.writeABit(SM, true, 0);
			if (trigger) BitStreamer.writeABit(SM, true, 4);
			if (isForm2) BitStreamer.writeABit(SM, true, 5);
			if (isRealTime) BitStreamer.writeABit(SM, true, 6);
			if (isEOF) BitStreamer.writeABit(SM, true, 7);
			
			switch(this.submode)
			{
			case VIDEO:
				BitStreamer.writeABit(SM, true, 1);
				break;
			case AUDIO:
				BitStreamer.writeABit(SM, true, 2);
				break;
			case DATA:
				BitStreamer.writeABit(SM, true, 3);
				break;
			default:
				break;
			}
			
			return SM;
		}

	}

	/**
	 * Raw Sector - Contains a reference to a 2352 byte FileBuffer containing a CD or ISO image's
	 * sector's raw data.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since August 31, 2017
	 */
	public static class RawSector implements Sector
	{
		private FileBuffer data;
		
		/**
		 * Construct an empty RawSector. Initial data reference will be null.
		 */
		public RawSector()
		{
			this.data = null;
		}
		
		public FileBuffer serializeSector(int sectornum)
		{
			return this.data;
		}
		
		public FileBuffer getData()
		{
			return this.data;
		}
		
		public int getMode()
		{
			return -1;
		}
		
		public void setData(FileBuffer newData) throws IOException
		{
			long nSz = newData.getFileSize();
			if (nSz < SECSIZE)
			{
				this.copyData(newData, 0, nSz);
			}
			else if (nSz > SECSIZE)
			{
				this.data = newData.createReadOnlyCopy(0, SECSIZE);
			}
			else
			{
				this.data = newData;
			}
		}
		
		public void copyData(FileBuffer newData, long stPos, long len)
		{
			this.data = new FileBuffer(SECSIZE, true);
			for (int i = 0; i < SECSIZE; i++)
			{
				if (stPos + i < newData.getFileSize() && i < len)
				{
					this.data.addToFile(newData.getByte(stPos + i));
				}
				else
				{
					this.data.addToFile(ZERO);
				}
			}
		}
	}
	
	/* ----- Constructors ----- */
	
	/**
	 * Construct ISO object from full loaded file.
	 * @param myISO The image to parse into sectors.
	 * @param rawMode Whether to look at only data in sectors or full sectors.
	 * @throws IOException If there is an error creating the read-only references for sector data.
	 */
	public ISO(FileBuffer myISO, boolean rawMode) throws IOException
	{
		this(myISO, 0, rawMode);
	}
	
	/**
	 * Construct ISO object from loaded file starting at the specified offset.
	 * @param myISO The image to parse into sectors.
	 * @param stPos Position in FileBuffer to start reading.
	 * @param rawMode Whether to look at only data in sectors or full sectors.
	 * @throws IOException If there is an error creating the read-only references for sector data.
	 */
	public ISO(FileBuffer myISO, long stPos, boolean rawMode) throws IOException
	{
		this.constructorCore();
		if (rawMode) this.parseRAW(myISO, stPos);
		else this.parseISO(myISO, stPos);
	}
	
	private void constructorCore()
	{
		this.loosedata = false;	
		if (zeroSector == null)
		{
			zeroSector = new FileBuffer(0x920);
			for (int i = 0; i < 0x920; i++) zeroSector.addToFile(ZERO);
		}
	}

	/* ----- Getters ----- */
	
	/**
	 * Get whether the disk image has data that falls outside of a clean sector boundary.
	 * This is usually determined by obtaining the modulus of the total size by the sector size.
	 * If the modulus is nonzero, there is "loose data."
	 * <br> If this occurs, the image was improperly formatted. This may cause serious errors.
	 * @return 
	 * True: If parsed image size is not an even multiple of the sector size.
	 * <br>False: If image is formatted correctly. 
	 */
	public boolean hasLooseData()
	{
		return this.loosedata;
	}

	/**
	 * Get the absolute index of the first sector (usually 150 for an ISO format file). 
	 * <br> "Relative" index coordinates are relative to this value.
	 * @return An integer representing the index value of the first sector in the image in absolute
	 * coodinates.
	 */
	public int firstSector()
	{
		return this.firstSector;
	}

	/**
	 * Get the sector correlated with the given absolute index.
	 * @param absNum The index of the desired sector in absolute coordinates.
	 * @return A Sector at the specified absolute index.
	 * <br> null if the index is invalid.
	 */
	public Sector getSectorAbsolute(int absNum)
	{
		if (absNum < 0) return null;
		if (absNum >= this.sectors.length) return null;
		return this.sectors[absNum];
	}
	
	/**
	 * Get the sector correlated with the given relative index. 
	 * @param relNum The index of the desired sector relative to the start of the image.
	 * @return A Sector at the specified relative index.
	 * <br> null if the index is invalid.
	 */
	public Sector getSectorRelative(int relNum)
	{
		int absNum = this.firstSector + relNum;
		return this.getSectorAbsolute(absNum);
	}

	/**
	 * The absolute number of sectors on the disk the image originated from.
	 * <br>This accounts for images such as in ISO files that do not start at 
	 * disk sector 0.
	 * @return Absolute number of disk sectors.
	 */
	public int getNumberSectorsAbsolute()
	{
		return this.sectors.length;
	}
	
	/**
	 * The number of sectors on the parsed image.
	 * <br>"Relative" coordinates are relative to the beginning of the image rather
	 * than the beginning of the disk the image was derived from.
	 * @return The number of sectors on this image.
	 */
	public int getNumberSectorsRelative()
	{
		int abs = this.sectors.length;
		return abs - this.firstSector;
	}
	
	/* --- Memory Handling --- */
	
	/**
	 * Calculate the minimum memory taken up by data in this object.
	 * @return Minimum memory (in bytes) used for holding CD image data.
	 */
	public long getMemoryTax()
	{
		long CDmem = 0;
		Collection<FileBuffer> counted = new LinkedList<FileBuffer>();
		for (Sector s : this.sectors)
		{
			if (s != null)
			{
				CDmem += s.getData().getMemoryBurden(counted);
			}
		}
		return CDmem;
	}
	
	/* --- BCD Conversion --- */
	
	/**
	 * Calculate the absolute sector index from the sector header "time" encoded in BCD.
	 * <br>(ie. "Minute-second-sector to sector")
	 * @param BCDminute The first time byte representing the "minute" in BCD.
	 * @param BCDsecond The second time byte representing the "second" in BCD.
	 * @param BCDsector The third time byte representing the sector offset in BCD. There are 75
	 * sectors in a "second."
	 * @return The sector index in absolute coordinates calculated from the sector time.
	 */
 	public static int MSS_to_sector(byte BCDminute, byte BCDsecond, byte BCDsector)
	{
		int min = fromBCD(BCDminute);
		int second = fromBCD(BCDsecond);
		int sector = fromBCD(BCDsector);
		
		int absSec = min * 60 * 75;
		absSec += second * 75;
		absSec += sector;
		
		return absSec;
	}
	
 	/**
 	 * Calculate a sector's minute coordinate from its absolute index.
 	 * @param sector Absolute index of a sector.
 	 * @return BCD encoded byte representing the minute coordinate for encoding into an ISO
 	 * sector header.
 	 */
	public static byte getBCDminute(int sector)
	{
		int min = (sector / 75) / 60;
		return toBCD(min);
	}
	
 	/**
 	 * Calculate a sector's second coordinate from its absolute index.
 	 * @param sector Absolute index of a sector.
 	 * @return BCD encoded byte representing the second coordinate for encoding into an ISO
 	 * sector header.
 	 */
	public static byte getBCDsecond(int sector)
	{
		int sec = (sector / 75) % 60;
		return toBCD(sec);
	}
	
 	/**
 	 * Calculate a sector's finest time coordinate from its absolute index.
 	 * <br>There are 75 sectors in a second.
 	 * @param sector Absolute index of a sector.
 	 * @return BCD encoded byte representing the sector coordinate as a subunit of the second coordinate
 	 * for encoding into an ISO sector header.
 	 */
	public static byte getBCDsector(int sector)
	{
		int sec = sector % 75;
		return toBCD(sec);
	}
	
	/**
	 * Encode an integer as a single BCD byte.
	 * @param val Value to encode. Must be between 0 and 99.
	 * @return BCD encoded byte.
	 */
	public static byte toBCD(int val)
	{
		int valR = val % 10;
		int valL = val / 10;
		
		byte b = (byte)(valL + valR);
		
		return b;
	}
	
	/**
	 * Decode a BCD byte.
	 * @param BCDnum Byte to decode.
	 * @return The encoded value (0 - 99)
	 */
	public static int fromBCD(byte BCDnum)
	{
		int valI = Byte.toUnsignedInt(BCDnum);
		int valR = valI & 0x0F;
		int valL = (valI >> 4) & 0x0F;
		
		int val = valL * 10;
		val += valR;
		
		return val;
	}
	
	/* --- Parsers --- */
	
	/**
	 * Parse image into raw sectors.
	 * @param myISO Image file in memory to parse.
	 * @param stPos Position at which to start parsing.
	 * @throws IOException If there is an error creating read-only reference buffers for sectors.
	 */
	private void parseRAW(FileBuffer myISO, long stPos) throws IOException
	{
		int numSecs = 0;
		int secSize = 0;
		
		if ((myISO.getFileSize() - stPos) % F1SIZE == 0) secSize = F1SIZE;
		else secSize = SECSIZE;
		
		numSecs = (int)((myISO.getFileSize() - stPos)/ Integer.toUnsignedLong(secSize));
		if ((myISO.getFileSize() - stPos) % secSize != 0) this.loosedata = true;
		
		this.firstSector = 0;
		this.sectors = new Sector[numSecs];
		//if (this.eventContainer != null) this.eventContainer.fireNewEvent(EventType.ISO_NUMSECSCALCULATED, this.getNumberSectorsRelative());
		
		for (int s = 0; s < numSecs; s++)
		{
			long sSt = stPos + (s * secSize);
			this.sectors[s] = new RawSector();
			this.sectors[s].setData(myISO.createReadOnlyCopy(sSt, sSt + secSize));
			//if (this.eventContainer != null) eventContainer.fireNewEvent(EventType.ISO_SECTORREAD, s);
		}
	}
	
	/**
	 * Parse image and data out of sectors.
	 * @param myISO FileBuffer of disk image to parse.
	 * @param stPos Offset from buffer start to begin parsing.
	 * @throws IOException If there is an error creating read-only references to buffer.
	 */
	private void parseISO(FileBuffer myISO, long stPos) throws IOException
	{
		boolean datOnly = false;
		int numSecs = 0;
		
		/*First, check for sync pattern*/
		datOnly = (!checkSyncPattern(myISO, stPos));
		//System.out.println("ISO.parseISO || datOnly = " + datOnly);
		
		/*Determine number of sectors, whether there is overflow,
		 * and the first sector number*/
		int secSize = 0;
		if (!datOnly)
		{
			secSize = SECSIZE;
			this.firstSector = readSecNumber(myISO, stPos);
		}
		else
		{
			secSize = F1SIZE;
			this.firstSector = DEFO_FIRSTSEC;
		}
		
		numSecs = (int)((myISO.getFileSize() - stPos) / secSize);
		this.loosedata = (((myISO.getFileSize() - stPos) % secSize) != 0);
		//System.out.println("ISO.parseISO || numSecs = " + numSecs);
		
		numSecs += this.firstSector;
		this.sectors = new Sector[numSecs];
		//if (this.eventContainer != null) this.eventContainer.fireNewEvent(EventType.ISO_NUMSECSCALCULATED, this.getNumberSectorsRelative());
		
		long cPos = stPos;
		
		for (int s = this.firstSector; s < numSecs; s++)
		{
			//System.out.println("ISO.parseISO || Parsing Sector " + s + " at 0x" + Long.toHexString(cPos));
			this.sectors[s] = parseSector(myISO, cPos);
			//if (this.eventContainer != null) eventContainer.fireNewEvent(EventType.ISO_SECTORREAD, s);
			cPos += secSize;
		}
		
	}
	
	/**
	 * Check for the 12 byte sync pattern at the given offset in an image file.
	 * @param myISO FileBuffer of the image to check.
	 * @param secStPos Position to check for sync pattern at.
	 * @return True - If the next 12 bytes are identical to the ISO sync pattern.
	 * <br> False - Otherwise
	 */
	private static boolean checkSyncPattern(FileBuffer myISO, long secStPos)
	{
		if (myISO.findString(secStPos, secStPos + 0x10, SYNC) != secStPos) return false;
		return true;
	}
	
	/**
	 * Read the header of an unparsed sector to determine its index.
	 * <br> Taken in good faith that the sector offset is correct and that the sector is raw.
	 * If this is not the case, the number returned will be meaningless.
	 * @param myISO FileBuffer containing disk image.
	 * @param secStPos Position of the raw sector start relative to the start of the image FileBuffer
	 * (first byte of sync pattern).
	 * @return Sector index number (absolute) of sector - derived from time coordinate.
	 */
	private static int readSecNumber(FileBuffer myISO, long secStPos)
	{
		//Takes your word for it that the sector offset is legit.
		long cPos = secStPos + 0x0C;
		byte min = myISO.getByte(cPos);
		cPos++;
		byte second = myISO.getByte(cPos);
		cPos++;
		byte secOff = myISO.getByte(cPos);
		int secNum = MSS_to_sector(min, second, secOff);
		
		/*Returns information from the header*/
		return secNum;
	}
	
	/**
	 * Read the header of an unparsed raw sector to determine its mode.
	 * <br> Taken in good faith that the sector offset is correct and that the sector is raw.
	 * If this is not the case, the number returned will be meaningless.
	 * @param myISO FileBuffer containing disk image.
	 * @param secStPos Position of the raw sector start relative to the start of the image FileBuffer
	 * (first byte of sync pattern).
	 * @return ISO mode of the sector as an integer.
	 * <br> 0 - If the sector is Mode 0
	 * <br> 1 - If the sector is Mode 1 (standard)
	 * <br> 2 - If the sector is Mode 2 (XA)
	 */
	private static int readSecMode(FileBuffer myISO, long secStPos)
	{
		//Takes your word for it that the sector offset is legit.
		long cPos = secStPos + 0x0F;
		byte mode = myISO.getByte(cPos);
		
		/*Returns information from the header*/
		return Byte.toUnsignedInt(mode);
	}
	
	/**
	 * Parse the rudimentary sector information of a raw sector.
	 * @param myISO FileBuffer containing disk image to parse sector from.
	 * @param secStPos Position of the sector start relative to the start of the image FileBuffer.
	 * @return A Sector containing the sector data separate from the header and/or subheader information
	 * (if present).
	 * @throws IOException If there is an error creating a readonly reference buffer (for streamed
	 * FileBuffers).
	 */
	private static Sector parseSector(FileBuffer myISO, long secStPos) throws IOException
	{
		//int cPos = secStPos;
		Sector s = null;
		
		boolean dataOnly = !checkSyncPattern(myISO, secStPos);
		
		int mode = 0;
		if (!dataOnly) mode = readSecMode(myISO, secStPos);
		else mode = 1;
		long datSt = 0;
		switch (mode)
		{
		case 0:
			s = new SectorM0();
			break;
		case 1:
			s = new SectorM1();
			if (dataOnly) datSt = secStPos;
			else datSt = secStPos + 0x10;
			s.setData(myISO.createReadOnlyCopy(datSt, datSt + F1SIZE));
			break;
		case 2:
			SectorM2 s2 = new SectorM2();
			byte FN = myISO.getByte(secStPos + 0x10);
			byte CN = myISO.getByte(secStPos + 0x11);
			byte SM = myISO.getByte(secStPos + 0x12);
			byte CI = myISO.getByte(secStPos + 0x13);
			s2.setFileNumber(FN);
			s2.setChannelNumber(CN);
			s2.setSubModeInfo(SM);
			s2.setCI(CI);
			datSt = secStPos + 0x18;
			s2.setData(myISO.createReadOnlyCopy(datSt, datSt + s2.dataSize()));
			s = s2;
			break;
		default:
			s = new RawSector();
			s.setData(myISO.createReadOnlyCopy(secStPos, secStPos + SECSIZE));
			break;
		}
		
		
		//returns the sector it creates
		return s; 
	}
	
	/* --- Writers --- */
	
	/**
	 * Calculate the size in bytes that this image would take up when serialized.
	 * @param datOnly Whether only the data or both the data and sector information will be written.
	 * <br> True if data only. 
	 * <br> False if raw sectors are desired.
	 * @return The size in bytes this image would take up if written to disk.
	 */
	public int calculateImageSize(boolean datOnly)
	{
		int s = this.sectors.length - this.firstSector;
		if (datOnly) return s*F1SIZE;
		
		return s*SECSIZE;
	}
	
	/**
	 * Serialize and write image to disk. Sectors will be full raw sectors.
	 * <br> Error coding regions will be all zero.
	 * @param path Path on disk to write ISO image to.
	 * @throws IOException If there is an error writing to disk.
	 */
	public void writeFullImage(String path) throws IOException
	{
		FileBuffer isoOut = new FileBuffer(this.calculateImageSize(false));
		for(int s = this.firstSector; s < this.sectors.length; s++)
		{
			isoOut.addToFile(this.sectors[s].serializeSector(s));
		}
		isoOut.writeFile(path);
	}
	
	/**
	 * Serialize and write data-only image (without ISO headers, subheaders, or error correction)
	 * to disk, assuming all sectors in this image are standard 2048 byte data sectors.
	 * @param path Path on disk to write ISO image to.
	 * @throws IOException If there is an error writing to disk.
	 * @throws UnsupportedOperationException If image contains any sectors that have more or less than 2048
	 * bytes in data (eg. Mode 2 Form 2)
	 */
	public void writeDataImage(String path) throws IOException
	{
		int[] sectorCounts = countSectorTypes();
		if (sectorCounts[3] > 0) throw new UnsupportedOperationException(); //There are M2F2 sectors
		FileBuffer isoOut = new FileBuffer(this.calculateImageSize(true));
		for(int s = this.firstSector; s < this.sectors.length; s++)
		{
			isoOut.addToFile(this.sectors[s].getData());
		}
		isoOut.writeFile(path);
	}
	
	/* ----- Event ----- */
	
	//Observer/ Observable were deprecated
	
	/* ----- Information ----- */
	
	/**
	 * Return an array of each sector type counted. <br>
	 * Index - Mode <br>
	 * 0 - Mode 0 <br>
	 * 1 - Mode 1 <br>
	 * 2 - Mode 2/ Form 1 <br>
	 * 3 - Mode 2/ Form 2 <br>
	 * 4 - Raw
	 * @return Integer array length 5 with counts of each sector type in this image.
	 */
	public int[] countSectorTypes()
	{
		int[] count = new int[5];
		for (Sector s : this.sectors)
		{
			if (s != null)
			{
				if (s instanceof SectorM0) count[0]++;
				else if (s instanceof SectorM1) count[1]++;
				else if (s instanceof RawSector) count[4]++;
				else if (s instanceof SectorM2)
				{
					SectorM2 s2 = (SectorM2)s;
					if (s2.isForm2()) count[3]++;
					else count[2]++;
				}
			}
		}
		return count;
	}
	
	
}
