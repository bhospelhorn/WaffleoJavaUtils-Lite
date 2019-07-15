package waffleoRai_Containers;

import java.util.HashMap;
import java.util.Map;

import waffleoRai_Containers.ISO.Sector;
import waffleoRai_Containers.ISO.SectorM2;
import waffleoRai_Utils.BitStreamer;
import waffleoRai_Utils.FileBuffer;

/*
 * UPDATES
 * 
 * 2017.11.02 | 1.1.0 -> 1.2.0
 * 	Fixed parsing issues
 * 
 * 2017.11.05 | 1.2.0 -> 1.3.0
 * 	Allow checking of mode for sectors.
 */

/**
 * A child class of the ISO9660Table for parsing a subtype of ISO9660 image - eXtended Architecture,
 * which utilizes Mode 2 sectors and real time streaming.
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since November 2, 2017
 */
public class XATable extends ISO9660Table{

	private Map<Integer, XASectorHeader> sectorSH_Map;
	
	/* --- Objects --- */
	
	/**
	 * A small enum to represent the three possible XA submodes (Data, Audio, Video).
	 * @author Blythe Hospelhorn
	 * @version 2.0.0
	 * @since August 1, 2017
	 */
	public static enum XASubmode
	{
		/**
		 * Denotes that the XA sector is a video streaming sector.
		 */
		VIDEO("VIDEO"),
		
		/**
		 * Denotes that the XA sector is a audio streaming sector.
		 */
		AUDIO("AUDIO"),
		
		/**
		 * Denotes that the XA sector is a standard data sector.
		 */
		DATA("DATA");
		
		private String infoStr;
		
		private XASubmode(String s)
		{
			this.infoStr = s;
		}
		
		/**
		 * Get a string to represent the submode value in text when printing information
		 * about it.
		 * @return A string textually representing the enum value.
		 */
		public String getInfoStr()
		{
			return this.infoStr;
		}
	
		public String toString()
		{
			return infoStr;
		}
		
	}
	
	/**
	 * A class to represent the subheader found in XA Mode 2 ISO9660 sectors.
	 * The subheader of a Mode 2 sector contains additional information on how the sector 
	 * is to be read.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since August 1, 2017
	 */
	public static class XASectorHeader
	{
		public static final int BD_LO = 4;
		public static final int BD_HI = 8;
		public static final int SR_LO = 18900;
		public static final int SR_HI = 37800;
		
		//private int SectorNumber;
		
		private int FileNumber;
		private int ChannelNumber;
		
		private boolean EOR;
		private boolean EOF;
		private boolean trigger;
		private boolean form2;
		private boolean realTime;
		
		private XASubmode submode;
		
		private int audioChannels; //(Stereo/ Mono)
		private int bitsPerSample;
		private int sampleRate;
		private boolean emphasis;
		
		/* --- Constructors --- */
		
		/**
		 * Construct an empty sub-header
		 */
		public XASectorHeader()
		{
			//this.SectorNumber = -1;
			this.FileNumber = -1;
			this.ChannelNumber = -1;
			this.EOR = false;
			this.EOF = false;
			this.trigger = false;
			this.form2 = false;
			this.realTime = false;
			this.submode = XASubmode.DATA;
			this.audioChannels = -1;
			this.bitsPerSample = -1;
			this.sampleRate = -1;
			this.emphasis = false;
		}
		
		/**
		 * Construct a sector sub-header by parsing the sub-header from a Mode 2 sector.
		 * @param sector Mode 2 sector to parse.
		 */
		public XASectorHeader(SectorM2 sector)
		{
			//Creates a header from information in a sector
			this.FileNumber = sector.getFileNumber();
			this.ChannelNumber = sector.getChannelNumber();
			this.EOR = sector.isEOR();
			this.EOF = sector.isEOF();
			this.trigger = sector.hasTrigger();
			this.form2 = sector.isForm2();
			this.realTime = sector.isRealTime();
			this.submode = sector.getSubmode();
			this.interpretCI(sector.getCI());
		}
		
		/**
		 * Construct a sector sub-header given the 4-byte sub-header encoded as a JVM int.
		 * @param header Big-Endian 4-byte sub-header of a sector as a single int (4-bytes).
		 */
		public XASectorHeader(int header)
		{
			int FN = (header >> 24) & 0xFF;
			int CN = (header >> 16) & 0xFF;
			byte SM = (byte)((header >> 8) & 0xFF);
			int CI = header & 0xFF;
			
			this.FileNumber = FN;
			this.ChannelNumber = CN;
			this.interpretCI((byte)CI);
			
			this.EOR = BitStreamer.readABit(SM, 0);
			this.trigger = BitStreamer.readABit(SM, 4);
			this.form2 = BitStreamer.readABit(SM, 5);
			this.realTime = BitStreamer.readABit(SM, 6);
			this.EOF = BitStreamer.readABit(SM, 7);
			
			boolean V = BitStreamer.readABit(SM, 1);
			boolean A = BitStreamer.readABit(SM, 2);
			boolean D = BitStreamer.readABit(SM, 3);
			
			if (V && !A && !D) this.submode = XASubmode.VIDEO;
			else if (!V && A && !D) this.submode = XASubmode.AUDIO;
			else if (!V && !A && D) this.submode = XASubmode.DATA;
			else this.submode = null;
			
		}
		
		/* --- Getters --- */
		
		/**
		 * Get the file number, or index of the file this sector is included in.
		 * @return File number as an integer.
		 */
		public int getFileNumber() 
		{
			return FileNumber;
		}
		
		/**
		 * Get the index of the channel this sector should be streaming to.
		 * @return Channel number as an integer.
		 */
		public int getChannelNumber()
		{
			return this.ChannelNumber;
		}
		
		/**
		 * Get whether this sector marks an End-of-Record; it is the last sector
		 * in its record.
		 * @return True - If sector is an EOR marker.
		 * <br>False - Otherwise.
		 */
		public boolean EOR()
		{
			return this.EOR;
		}
		
		/**
		 * Get whether this sector marks an End-of-File; it is the last sector
		 * in its file.
		 * @return True - If sector is an EOF marker.
		 * <br>False - Otherwise.
		 */
		public boolean EOF()
		{
			return this.EOF;
		}
		
		/**
		 * Get whether this sector has the trigger flag set.
		 * @return True - If flag is set to trigger.
		 * <br>False - If flag is not set.
		 */
		public boolean hasTrigger()
		{
			return this.trigger;
		}
		
		/**
		 * Get whether this sector is a Form 2 sector.
		 * <br>Form 2 sectors utilize 0x914(2323) bytes for data rather than the Form 1
		 * standard 0x800(2048). XA Form 2 sectors are more often used for A/V streaming than data.
		 * @return True - If sector is Form 2.
		 * <br>False - If sector is Form 1.
		 */
		public boolean isForm2()
		{
			return this.form2;
		}
		
		/**
		 * Get whether this sector has the real-time flag set.
		 * @return True - If real-time flag is set.
		 * <br>False - If real-time flag is not set.
		 */
		public boolean isRealTime()
		{
			return this.realTime;
		}

		/**
		 * Get the sector submode (Data, Audio, or Video).
		 * @return Sector submode if set.
		 * <br>null if there was a parsing error or submode has not been set.
		 */
		public XASubmode getSubmode()
		{
			return this.submode;
		}
		
		/**
		 * Get the number of audio channels to be used from A/V streaming of this sector.
		 * @return 1 - If sector is part of monophonic stream.
		 * <br>2 - If sector is part of stereophonic stream.
		 * <br>0 - If this value is not set or not applicable.
		 * <br>-1 - If this value was never set.
		 * <br>Other values may be returned. If this occurs, there is an error.
		 */
		public int getAudioChannels()
		{
			return this.audioChannels;
		}
		
		/**
		 * Get the bit depth for audio streaming of this sector.
		 * @return
		 * 4 - If bit depth is flagged as default 4-bit.
		 * <br>8 - If bit depth is flagged as 8-bit.
		 * <br>0 - If not applicable.
		 * <br>-1 - If value is unset.
		 * <br>Other values may be returned in error.
		 */
		public int getBitDepth()
		{
			return this.bitsPerSample;
		}
		
		/**
		 * Get the sample rate for audio streaming of this sector.
		 * @return
		 * 18900 - If flagged for low sample rate (18900 Hz).
		 * <br>37800 - If flagged for high sample rate (37800 Hz).
		 * <br>0 - If not applicable.
		 * <br>-1 - If this value was never set.
		 */
		public int getSampleRate()
		{
			return this.sampleRate;
		}
		
		/**
		 * Get whether this sector has the emphasis flag set for A/V streaming.
		 * @return True - If emphasis is on.
		 * <br>False - If emphasis is off.
		 */
		public boolean getEmphasis()
		{
			return this.emphasis;
		}
		
		/* --- Setters --- */
		// Maybe only set in CDTable where private vars are visible?
		
		/* --- Writers --- */
		
		/**
		 * Serialize the sub-header for potential encoding into a Mode 2 sector.
		 * The result of this function can also be used for easier passage of the information
		 * contained in the sub-header.
		 * @return A 4-byte integer representation of the sub-header as a JVM int.
		 */
		public int serialize()
		{
			int FN = this.FileNumber & 0xFF;
			int CN = this.ChannelNumber & 0x1F;
			int SM = 0;
			int CI = 0;
			
			int me = 0;
			
			switch (this.submode)
			{
			case VIDEO: SM = BitStreamer.writeABit(SM, true, 1);
			case AUDIO: SM = BitStreamer.writeABit(SM, true, 2);
			case DATA: SM = BitStreamer.writeABit(SM, true, 3);
			}
			
			if (this.EOF) SM = BitStreamer.writeABit(SM, true, 7);
			if (this.realTime) SM = BitStreamer.writeABit(SM, true, 6);
			if (this.form2) SM = BitStreamer.writeABit(SM, true, 5);
			if (this.trigger) SM = BitStreamer.writeABit(SM, true, 4);
			if (this.EOR) SM = BitStreamer.writeABit(SM, true, 0);
			
			if (this.audioChannels == 2) CI = BitStreamer.writeABit(CI, true, 0);
			if (this.sampleRate == SR_LO) CI = BitStreamer.writeABit(CI, true, 2);
			if (this.bitsPerSample == BD_HI) CI = BitStreamer.writeABit(CI, true, 4);
			if (this.emphasis) CI = BitStreamer.writeABit(CI, true, 6);
			
			FN = (FN << 24) & 0xFF000000;
			CN = (CN << 16) & 0xFF0000;
			SM = (SM << 8) & 0xFF00;
			
			me = FN | CN | SM | CI;
			
			return me;
		}
	
		/* -- Information -- */
		
		/**
		 * Parse the sub-header CI (coding information) byte.
		 * @param CI The fourth byte in a Mode 2 sub-header.
		 */
		private void interpretCI(byte CI)
		{
			if (BitStreamer.readABit(CI, 0)) this.audioChannels = 2;
			else this.audioChannels = 1;
			
			if (BitStreamer.readABit(CI, 2)) this.sampleRate = SR_LO;
			else this.sampleRate = SR_HI;
			
			if (BitStreamer.readABit(CI, 4)) this.bitsPerSample = BD_HI;
			else this.bitsPerSample = BD_LO;
			
			if (BitStreamer.readABit(CI, 6)) this.emphasis = true;
			else this.emphasis = false;
		}
		
		public String toString()
		{
			String s = "";
			
			s += "\t" + "Submode: " + this.submode.infoStr + "\n";
			if (this.form2) s += "\t" + "Form 2" + "\n";
			else s += "\t" + "Form 1" + "\n";
			s += "\t" + "File Number: " + this.FileNumber + "\n";
			s += "\t" + "Channel Number: " + this.ChannelNumber + "\n";
			s += "\n";
			
			s += "\t" + "End of Record: ";
			if (this.EOR) s += "Yes\n";
			else s += "No\n";
			
			s += "\t" + "End of File: ";
			if (this.EOF) s += "Yes\n";
			else s += "No\n";
			
			s += "\t" + "Real Time: ";
			if (this.realTime) s += "Yes\n";
			else s += "No\n";
			
			s += "\t" + "Trigger: ";
			if (this.trigger) s += "Yes\n";
			else s += "No\n";
			
			if (this.submode == XASubmode.AUDIO)
			{
				s += "\n";
				
				s += "\t" + "Audio Channels: ";
				if (this.audioChannels == 1) s += "Mono\n";
				else if (this.audioChannels == 2) s += "Stereo\n";
				else s += "Unknown\n";
				
				s += "\t" + "Sample Rate: " + this.sampleRate + "\n";
				s += "\t" + "Bits Per Sample: " + this.bitsPerSample + "\n";
				s += "\t" + "Emphasis: ";
				if (this.emphasis) s += "On\n";
				else s += "Off\n";
			}
			
			return s;
		}
	
		public void printMe()
		{
			System.out.println(" -- XA Sub-Header -- ");
			System.out.println("\tSubmode: " + this.submode.getInfoStr());
			System.out.println("\tForm 2: " + this.form2);
			System.out.println("\tFile Number: " + this.FileNumber);
			System.out.println("\tChannel Number: " + this.ChannelNumber);
			System.out.println("\tEnd of Record: " + this.EOR);
			System.out.println("\tEnd of File: " + this.EOF);
			System.out.println("\tTrigger: " + this.trigger);
			System.out.println("\tReal-Time: " + this.realTime);
			System.out.println("\tAudio Channels: " + this.audioChannels);
			System.out.println("\tBit Depth: " + this.bitsPerSample + " bit");
			System.out.println("\tSample Rate: " + this.sampleRate + " Hz");
			System.out.println("\tEmphasis: " + this.emphasis);
		}
	
	}

	/**
	 * A child class of the ISO9660Entry - extended to hold additional XA specific information
	 * found in directory table records.
	 * @author Blythe Hospelhorn
	 * @version 1.1.0
	 * @since November 1, 2017
	 */
	public static class XAEntry extends ISO9660Entry
	{
		
		private int ownIDgroup;
		private int ownIDuser;
		
		private boolean o_r;
		private boolean o_x;
		private boolean g_r;
		private boolean g_x;
		private boolean a_r;
		private boolean a_x;
		
		private boolean isMode2;
		private boolean isForm2;
		private boolean isInterleaved;
		private boolean isCDDA;
		private boolean isDirectory;
		
		private int fileNumber;
		
		/* --- Construction --- */
		
		/**
		 * Construct an entry from the file or directory name and relative index of the first sector
		 * containing the file or directory data.
		 * @param name Name of the file or directory.
		 * @param startBlock Relative index of the first sector of the file.
		 */
		public XAEntry(String name, int startBlock)
		{
			super(name, startBlock);
			constructorCore();
		}
		
		/**
		 * Set the instance variables to default and empty values.
		 */
		private void constructorCore()
		{
			this.ownIDgroup = 0;
			this.ownIDuser = 0;
			
			this.o_r = true;
			this.o_x = true;
			this.g_r = true;
			this.g_x = true;
			this.a_r = true;
			this.a_x = true;
			
			this.isMode2 = true;
			this.isForm2 = false;
			this.isInterleaved = false;
			this.isCDDA = false;
			this.isDirectory = false;
			
			this.fileNumber = 0;
		}
		
		/**
		 * Construct an entry by parsing it from the data of an ISO image sector containing
		 * a directory table.
		 * @param ISOdata Raw data from a table sector of an ISO image.
		 * @param ePos Position relative to the start of the sector data of the first byte of the
		 * record to parse.
		 * @throws CDInvalidRecordException If there is an error parsing the record.
		 */
		public XAEntry(FileBuffer ISOdata, int ePos) throws CDInvalidRecordException
		{
			super(ISOdata, ePos);
			//super.printMe();
			if (ISO9660Table.badName(super.getName()))
			{
				constructorCore();
				return;
			}
			int nLen = Byte.toUnsignedInt(ISOdata.getByte(ePos + 0x20));
			int XAst = ePos + 33 + nLen;
			if (nLen % 2 == 0) XAst++;
			
			ISOdata.setEndian(true);
			int cPos = XAst;
			
			this.ownIDgroup = Short.toUnsignedInt(ISOdata.shortFromFile(cPos));
			cPos += 2;
			this.ownIDuser = Short.toUnsignedInt(ISOdata.shortFromFile(cPos));
			cPos += 2;
			
			int attr = Short.toUnsignedInt(ISOdata.shortFromFile(cPos));
			cPos += 2;
			
			this.o_r = BitStreamer.readABit(attr, 0);
			this.o_x = BitStreamer.readABit(attr, 2);
			this.g_r = BitStreamer.readABit(attr, 4);
			this.g_x = BitStreamer.readABit(attr, 6);
			this.a_r = BitStreamer.readABit(attr, 8);
			this.a_x = BitStreamer.readABit(attr, 10);
			this.isMode2 = BitStreamer.readABit(attr, 11);
			this.isForm2 = BitStreamer.readABit(attr, 12);
			if (isForm2) this.isMode2 = true;
			this.isInterleaved = BitStreamer.readABit(attr, 13);
			this.isCDDA = BitStreamer.readABit(attr, 14);
			this.isDirectory = BitStreamer.readABit(attr, 15);
			
			//this.printMe();
			String XAstr = ISOdata.getASCII_string(cPos, 2);
			
			if (!XAstr.equals("XA")) 
				throw new CDInvalidRecordException("CDXA directory entry parser error: \n"
													+ "No XA marker found.\n"
													+ "Parser assumes directory entry is not valid XA entry.\n");
			cPos += 2;
			
			this.fileNumber = Byte.toUnsignedInt(ISOdata.getByte(cPos));
			
		}
		
		/* --- Getters --- */
		
		/**
		 * Get the owner group ID of the file or directory referenced by this entry.
		 * @return Integer representation of the owner group ID.
		 */
		public int getOwnerGroupID()
		{
			return this.ownIDgroup;
		}
		
		/**
		 * Get the owner user ID of the file or directory referenced by this entry.
		 * @return Integer representation of the owner user ID.
		 */
		public int getOwnerUserID()
		{
			return this.ownIDuser;
		}
		
		/**
		 * Get permission flag: Owner Read
		 * @return True - If file owner has permission to read.
		 * <br>False - If file owner does not have permission to read.
		 */
		public boolean ownerRead()
		{
			return this.o_r;
		}
		
		/**
		 * Get permission flag: Owner Execute
		 * @return True - If file owner has permission to execute.
		 * <br>False - If file owner does not have permission to execute.
		 */
		public boolean ownerExecute()
		{
			return this.o_x;
		}
		
		/**
		 * Get permission flag: Group Read
		 * @return True - If file owner's group has permission to read.
		 * <br>False - If file owner's group does not have permission to read.
		 */
		public boolean groupRead()
		{
			return this.g_r;
		}
		
		/**
		 * Get permission flag: Group Execute
		 * @return True - If file owner's group has permission to execute.
		 * <br>False - If file owner's group does not have permission to execute.
		 */
		public boolean groupExecute()
		{
			return this.g_x;
		}
		
		/**
		 * Get permission flag: All Read
		 * @return True - If everybody has permission to read.
		 * <br>False - If everybody does not have permission to read.
		 */
		public boolean AllRead()
		{
			return this.a_r;
		}
		
		/**
		 * Get permission flag: All Execute
		 * @return True - If everybody has permission to execute.
		 * <br>False - If everybody does not have permission to execute.
		 */
		public boolean AllExecute()
		{
			return this.a_x;
		}
		
		/**
		 * Get whether the file is stored in a series of Mode 2 sectors (rather
		 * than the standard Mode 1).
		 * @return True - If file is stored in Mode 2 sectors.
		 * <br>False - If file is stored in standard Mode 1 sectors.
		 */
		public boolean isMode2()
		{
			return this.isMode2;
		}
		
		/**
		 * Get whether the file is stored in a series of Mode 2 Form 2 sectors.
		 * (Default is Form 1).
		 * @return True - If file is stored in Form 2 (0x914 byte data) sectors.
		 * <br>False - If file is stored in Form 1 (0x800 byte data) sectors.
		 */
		public boolean isForm2()
		{
			return this.isForm2;
		}
		
		/**
		 * Get whether file consists of interleaved streaming data.
		 * @return True - If file is interleaved.
		 * <br>False - If file is not interleaved.
		 */
		public boolean isInterleaved()
		{
			return this.isInterleaved;
		}
		
		/**
		 * Get whether file is flagged as CD-DA (an audio track, as opposed to data).
		 * @return True - If file is a CD-DA audio track.
		 * <br>False - Otherwise.
		 */
		public boolean isCDDA()
		{
			return this.isCDDA;
		}
		
		public boolean isDirectory()
		{
			return this.isDirectory;
		}
		
		/**
		 * Get series file number of file. Must match file number in sub-headers of the sectors
		 * data for this file is found in.
		 * @return Integer representation of the file's file number.
		 */
		public int getFileNumber()
		{
			return this.fileNumber;
		}
		
		public long calcFileOffsetOfSector(int s)
		{
			if (s < super.getStartBlock()) return -1;
			if (s > this.lastSector()) return -1;
			int tsec = s - super.getStartBlock();
			int secSize = ISO.F1SIZE;
			if (this.isForm2) secSize = ISO.F2SIZE;
			return (long)tsec * (long)secSize;
		}
		
		public int getSizeInSectors()
		{
			if (!this.isForm2) return super.getSizeInSectors();
			int sectorCount = (int)(super.getFileSize() / (long)ISO.F2SIZE);
			if (super.getFileSize() % (long)ISO.F2SIZE != 0) sectorCount++;
			return sectorCount;
		}
		
		/* --- Setters --- */
		
		/**
		 * Set the owner group ID of the file or directory referenced by this entry.
		 * @param gID New group ID.
		 */
		public void setOwnerGroupID(int gID)
		{
			this.ownIDgroup = gID;
		}
		
		/**
		 * Set the owner user ID of the file or directory referenced by this entry.
		 * @param uID New user ID.
		 */
		public void setOwnerUserID(int uID)
		{
			this.ownIDuser = uID;
		}
		
		/**
		 * Set permission: Owner Read
		 * @param oR Whether the file owner is permitted to read file.
		 */
		public void setOwnerRead(boolean oR)
		{
			this.o_r = oR;
		}
		
		/**
		 * Set permission: Owner Execute
		 * @param oX Whether the file owner is permitted to execute file.
		 */
		public void setOwnerExecute(boolean oX)
		{
			this.o_x = oX;
		}
		
		/**
		 * Set permission: Group Read
		 * @param gR Whether the file owner's group is permitted to read file.
		 */
		public void setGroupRead(boolean gR)
		{
			this.g_r = gR;
		}
		
		/**
		 * Set permission: Group Execute
		 * @param gX Whether the file owner's group is permitted to execute file.
		 */
		public void setGroupExecute(boolean gX)
		{
			this.g_x = gX;
		}
		
		/**
		 * Set permission: All Read
		 * @param aR Whether everybody is permitted to read file.
		 */
		public void setAllRead(boolean aR)
		{
			this.a_r = aR;
		}
		
		/**
		 * Set permission: All Execute
		 * @param aX Whether everybody is permitted to execute file.
		 */
		public void setAllExecute(boolean aX)
		{
			this.a_x = aX;
		}
		
		/**
		 * Set whether file is stored in a series of Mode 2 sectors.
		 * @param isM2 True if wish to set to Mode 2. False if wish to set to Mode 1.
		 */
		public void setMode2(boolean isM2)
		{
			this.isMode2 = isM2;
		}
		
		/**
		 * Set whether file is stored in a series of Form 2 (0x914 byte data) sectors.
		 * @param isF2 True if wish to set to Form 2. False if wish to set to Form 1.
		 */
		public void setForm2(boolean isF2)
		{
			this.isForm2 = isF2;
		}
		
		/**
		 * Set interleaved flag. Set to true if file contains interleaved streaming data.
		 * @param isI Whether file is interleaved.
		 */
		public void setInterleaved(boolean isI)
		{
			this.isInterleaved = isI;
		}
		
		/**
		 * Set CD-DA flag. Set to true if file is a CD-DA audio track.
		 * @param isDA Whether file is a CD-DA audio track.
		 */
		public void setCDDA(boolean isDA)
		{
			this.isCDDA = isDA;
		}
		
		/**
		 * Set XA directory flag. Somewhat redundant to standard ISO9660 directory flag. Set to
		 * true if the entry refers to a directory instead of a file.
		 * @param isDir Whether the entry refers to a directory.
		 */
		public void setDirectory(boolean isDir)
		{
			this.isDirectory = isDir;
		}
		
		/**
		 * Set the file number of the file. Sector sub-headers must match.
		 * @param FN New file number.
		 */
		public void setFileNumber(int FN)
		{
			this.fileNumber = FN;
		}
		
		/* --- Information --- */
		
		private void printMeTop()
		{
			System.out.println("--- ISO9660 Entry ---");
		}
		
		public void printMe()
		{
			printMeTop();
			super.printMe();
			System.out.println("- XA Info -");
			System.out.print("\tMode: ");
			if (this.isMode2) System.out.print("2\n");
			else System.out.print("1\n");
			System.out.print("\tForm: ");
			if (this.isForm2) System.out.print("2\n");
			else System.out.print("1\n");
			System.out.println("\tOwner User ID: " + this.ownIDuser);
			System.out.println("\tOwner Group ID: " + this.ownIDgroup);
			System.out.print("\tPermissions: ");
			if (this.isDirectory) System.out.print("d");
			else System.out.print("-");
			if (this.o_r) System.out.print("r");
			else System.out.print("-");
			System.out.print("-");
			if (this.o_x) System.out.print("x");
			else System.out.print("-");
			if (this.g_r) System.out.print("r");
			else System.out.print("-");
			System.out.print("-");
			if (this.g_x) System.out.print("x");
			else System.out.print("-");
			if (this.a_r) System.out.print("r");
			else System.out.print("-");
			System.out.print("-");
			if (this.a_x) System.out.print("x");
			else System.out.print("-");
			System.out.println();
			System.out.println("\tInterleaved: " + this.isInterleaved);
			System.out.println("\tCD-DA: " + this.isCDDA);
			System.out.println("\tFile Number: " + this.fileNumber);
		}
		
	}
	
	/* --- Construction --- */
	
	/**
	 * Construct an empty XA table with default and empty values.
	 */
	public XATable()
	{
		super();
		this.sectorSH_Map = new HashMap<Integer, XASectorHeader>();
	}

	/**
	 * Construct an XA table by parsing an ISO disk image.
	 * @param myISO Sector parsed ISO to parse table from.
	 * @throws CDInvalidRecordException If there is an error reading a record in the table.
	 */
	public XATable(ISO myISO) throws CDInvalidRecordException
	{
		this();
		this.parseFromISO(myISO);
	}
	
	/**
	 * Construct an XA table by making a copy referencing collections of an existing XA table.
	 * @param otherTable Table to copy from.
	 */
	protected XATable(XATable otherTable)
	{
		super(otherTable);
		this.sectorSH_Map = otherTable.sectorSH_Map;
	}
	
	/* --- Getters --- */
	
	public long calcFileOffsetOfSector(String filePath, int sector)
	{
		ISO9660Entry e = (ISO9660Entry)this.getEntry(filePath);
		if (e == null) return -1;
		if (sector < e.getStartBlock() || sector > e.lastSector()) return -1;
		
		long tot = 0;
		for (int s = e.getStartBlock(); s < sector; s++) tot += (long)sectorDataSize(s);
		
		return tot;
	}
	
	public int sectorDataSize(int sector)
	{
		XASectorHeader sh = this.sectorSH_Map.get(sector);
		if (sh != null)
		{
			if (sh.isForm2()) return ISO.F2SIZE;
		}
		return ISO.F1SIZE;
	}
	
	/**
	 * Get the number of non-default Mode 2 sub-headers found in this image.
	 * @return Number of non-default (not 0-filled data flagged) sub-headed sectors.
	 */
	public int numSubHeadersRecorded()
	{
		return this.sectorSH_Map.size();
	}

	/**
	 * Get the sub-header of a specified sector.
	 * @param sector Relative index of sector of interest.
	 * @return XASectorHeader with sub-header information for Mode 2 sector with a
	 * non-default sub-header.
	 * <br>null if sector is Mode 1, index is invalid, or sector has default sub-header.
	 */
	public XASectorHeader getSectorHeader(int sector)
	{
		return this.sectorSH_Map.get(sector);
	}
	
	/**
	 * Get a properly cast entry for easier access to XA specific fields.
	 * @param sector Relative index of sector in question.
	 * @return The entry for the file covering the sector in question, if valid.
	 * <br> null if sector is invalid or if entry could not be cast to XAEntry.
	 */
	public XAEntry getXAEntry(int sector)
	{
		CDTEntry e = this.getEntry(sector);
		if (e instanceof XAEntry) return (XAEntry)e;
		else return null;
	}
	
	/**
	 * Get whether the sector at the provided relative index is a Mode 2 sector.
	 * @param sector Relative index of query sector.
	 * @return True - If sector is Mode 2.
	 * <br> False - If sector is Mode 1.
	 */
	public boolean isSectorMode2(int sector)
	{
		XAEntry e = this.getXAEntry(sector);
		if (e == null) return false;
		return e.isMode2();
	}
	
	/**
	 * Get whether the sector at the provided relative index is a Form 2 sector.
	 * @param sector Relative index of query sector.
	 * @return True - If sector is Form 2.
	 * <br> False - If sector is Form 1.
	 */
	public boolean isSectorForm2(int sector)
	{
		XAEntry e = this.getXAEntry(sector);
		if (e == null) return false;
		return e.isForm2();
	}
	
	/* --- Setters --- */
	
	protected void addSubHeader(int sh, int sec)
	{
		XASectorHeader sech = new XASectorHeader(sh);
		this.sectorSH_Map.put(sec, sech);
	}
	
	/* --- Parsing/ Conversion --- */
	
	protected void parseFromISO(ISO myISO) throws CDInvalidRecordException
	{
		super.setFirstSecIndex(myISO.firstSector());
		super.setNumberSecs(myISO.getNumberSectorsRelative());
		
		Sector vDesc = myISO.getSectorRelative(VOLDESCSEC);
		FileBuffer sDat = vDesc.getData();
		
		sDat.setEndian(true);
		//0x9C - offset of root directory record
		//0x02 - Bypass record length fields
		//0x04 = Skip LittleEndian record
		//System.err.println("XATable.parseFromISO || Sector Mode = " + vDesc.getMode());
		int rawoffset = 0;
		if (vDesc.getMode() == -1) rawoffset = 0x18;
		int rootDirSec = sDat.intFromFile(0x9C + 0x02 + 0x04 + rawoffset);
		//System.err.println("XATable.parseFromISO || rootDirSec = " + rootDirSec);
		
		this.parseDirectory(myISO, rootDirSec, "");
		//System.err.println("XATable.parseFromISO || parseDirectory exited");
		//Create "RAW" entries to cover any sectors not explicitly included in a file.
		int us = super.nextUncoveredSector();
		//System.err.println("XATable.parseFromISO || next uncovered sector = " + us);
		while (us >= 0)
		{
			int l = super.calculateSectorsToNextCovered(us);
			String n = ".RAW" + Integer.toString(us);
			XAEntry rawe = new XAEntry(n, us);
			rawe.setFileSize(l * ISO.F1SIZE);
			rawe.setRawFile(true);
			rawe.setIsDirectory(false);
			super.putInMainMap(rawe, n);
			super.putInSectorMap(rawe);
			
			us = nextUncoveredSector();
		}
		//System.out.println("XATable.parseFromISO || us loop exit : us = " + us);
		
		//Scan image for "unusual" XA subheaders
		//System.err.println("XATable.parseFromISO || subheader scan start ");
		for (int s = 0; s < super.getNumberSectors(); s++)
		{
			//System.out.println("XATable.parseFromISO || s = " + s);
			Sector sec = myISO.getSectorRelative(s);
			//System.out.println("XATable.parseFromISO || sec is null?: " + (sec == null));
			if (sec instanceof SectorM2)
			{
				//System.out.println("XATable.parseFromISO || sector is Mode 2 instance ");
				SectorM2 sm2 = (SectorM2)sec;
				//System.out.println("XATable.parseFromISO || sm2 is null?: " + (sm2 == null));
				if (sm2.getFileNumber() != 0 || sm2.getChannelNumber() != 0 
						|| sm2.getSubmode() != XASubmode.DATA || sm2.isForm2())
				{
					//System.out.println("XATable.parseFromISO || sector is nonstandard");
					XASectorHeader sh = new XASectorHeader(sm2);
					//System.out.println("XATable.parseFromISO || sh is null?: " + (sh == null));
					this.sectorSH_Map.put(s, sh);
					//System.out.println("XATable.parseFromISO || sh has been put in map");
				}
			}
		}
		//System.out.println("XATable.parseFromISO || subheader scan end ");
	}
	
	private void parseDirectory(ISO myISO, int secRel, String dName) throws CDInvalidRecordException
	{
		//System.err.println("XATable.parseDirectory || -DEBUG- Called: secRel = " + secRel + ", dName = " + dName);
		int cPos = 0;
		boolean ignore = false;
		Sector dirTable = myISO.getSectorRelative(secRel);
		
		while (cPos < dirTable.getData().getFileSize())
		{
			//System.out.println("XATable.parseDirectory || cPos = " + cPos);
			int eLen = Byte.toUnsignedInt(dirTable.getData().getByte(cPos));
			if (eLen <= 0) break;
			ignore = false;
			
			XAEntry e = parseDirectoryEntry(dirTable, cPos);
			cPos += eLen;
			if (super.badName(e.getName())) ignore = true;
			if (ignore) continue;
			else
			{
				super.putInMainMap(e, dName + e.getName());
				super.putInSectorMap(e);
				if (e.isDirectory())
				{
					parseDirectory(myISO, e.getStartBlock(), dName + e.getName() + "\\");
				}
			}
		}
		//System.out.println("XATable.parseDirectory || While break : cPos = " + cPos);
	}
	
 	private XAEntry parseDirectoryEntry(Sector s, int offset) throws CDInvalidRecordException
	{
		FileBuffer sDat = s.getData();
		if (offset < 0 || offset >= sDat.getFileSize()) return null;
		
		XAEntry e = new XAEntry(sDat, offset);
		
		return e;
	}
 	
	
 	/* --- Information --- */
 	
 	public void printMe()
 	{
 		System.out.println("ISO9660XATable.printTable()  ----------- ");
 		System.out.println("First sector: " + super.getFirstSectorIndex());
 		System.out.println("Number sectors: " + super.getNumberSectors());
 		System.out.println();
 		System.out.println("ENTRIES (As ordered in sectorMap --- \n");
 		for (ISO9660Entry e : super.getSectorMap())
 		{
 			e.printMe();
 			System.out.println();
 		}
 		/*System.out.println("SECTOR SUB-HEADERS --- \n");
 		int nSec = getNumberSectors();
 		for (int i = getFirstSectorIndex(); i < nSec; i++)
 		{
 			XASectorHeader h = sectorSH_Map.get(i);
 			if (h != null)
 			{
 				System.out.println("Non-standard subheader found! Sector " + i);
 				h.printMe();
 				System.out.println();
 			}
 		}*/
 	}
 	
}
