package waffleoRai_Containers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer;


/*
 * UPDATES
 * 
 * 2017.10.31 | 1.0.0 -> 1.1.0
 * 	Updated all file offset values to be long instead of int
 * 	Added javadoc annotation
 * 	Updated to use Gregorian Calendar timestamping instead of arbitrary "Date" class.
 */

/**
 * Interface to represent a top directory table of a CD image.
 * <br> Can be used to correlate file names and offsets with locations and sectors on CD image.
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since October 31, 2017
 */
public interface CDTable {
	
	/**
	 * Class to represent an entry in a CDTable. An entry should generally
	 * refer to a file on the CD and contain information such as the file's name, size,
	 * and the sector it begins at.
	 * @author Blythe Hospelhorn
	 * @version 1.1.0
	 * @since October 31, 2017
	 */
	public static class CDTEntry implements Comparable<CDTEntry>
	{
		private String fileName;
		private int location;
		private long size;
		private boolean isDir;
		
		private GregorianCalendar dateCreated;
		
		/* -- Constructors -- */
		
		/**
		 * Construct an empty CD table entry.
		 * All instance variables are set to default values.
		 */
		public CDTEntry()
		{
			this.fileName = null;
			this.location = -1;
			this.size = 0;
			this.isDir = false;
			this.dateCreated = null;
		}
		
		/**
		 * Construct a CD table entry with a name and starting sector index (relative).
		 * @param name File or directory name.
		 * @param startBlock Relative index of the first sector containing data for the file.
		 */
		public CDTEntry(String name, int startBlock)
		{
			this.fileName = name;
			this.location = startBlock;
			this.size = 0;
			this.isDir = false;
			this.dateCreated = null;
		}
		
		/* -- Getters -- */
		
		/**
		 * Get the name of the file or directory referenced by this CD table entry.
		 * @return String containing the file name.
		 */
		public String getName()
		{
			return this.fileName;
		}
		
		/**
		 * Get the relative index of the first sector containing data for the file
		 * or directory referenced by this table entry.
		 * @return An integer representing the relative index of the first sector(block)
		 * of the file.
		 */
		public int getStartBlock()
		{
			return this.location;
		}
		
		/**
		 * Get the size of the file.
		 * @return A long integer representing the size in bytes of the file or directory
		 * referenced by this CD entry.
		 */
		public long getFileSize()
		{
			return this.size;
		}
		
		/**
		 * Get whether this CD table entry refers to a directory.
		 * @return True - If this entry refers to a directory.
		 * <br>False - If this entry refers to a file or something else.
		 */
		public boolean isDirectory()
		{
			return this.isDir;
		}
		
		/**
		 * Get the timestamp for the file or directory this entry refers to.
		 * @return A GregorianCalendar containing the date this file was created.
		 */
		public Calendar getDate()
		{
			return this.dateCreated;
		}

		/**
		 * Get the year the file referred to by this entry was created.
		 * @return Integer representing the time stamped year according to the Gregorian calendar.
		 */
		public int getTimestampYear()
		{
			return this.dateCreated.get(Calendar.YEAR);
		}
		
		/**
		 * Get the month the file referred to by this entry was created.
		 * @return Integer representing the time stamped month (with January starting at 0) 
		 * according to the Gregorian calendar.
		 */
		public int getTimestampMonth()
		{
			return this.dateCreated.get(Calendar.MONTH);
		}
		
		/**
		 * Get the day of the month the file referred to by this entry was created.
		 * @return Integer representing the time stamped day (1 - 31) 
		 * according to the Gregorian calendar.
		 */
		public int getTimestampDay()
		{
			return this.dateCreated.get(Calendar.DAY_OF_MONTH);
		}
		
		/**
		 * Get the hour the file referred to by this entry was created.
		 * @return Integer representing the time stamped hour according to the 24 hour clock.
		 * (Hour returned refers to military time).
		 */
		public int getTimestampHour()
		{
			return this.dateCreated.get(Calendar.HOUR_OF_DAY);
		}
		
		/**
		 * Get the minute the file referred to by this entry was created.
		 * @return Integer representing the time stamped minute according to the 24 hour clock.
		 */
		public int getTimestampMinute()
		{
			return this.dateCreated.get(Calendar.MINUTE);
		}
		
		/**
		 * Get the second the file referred to by this entry was created.
		 * @return Integer representing the time stamped second according to the 24 hour clock.
		 */
		public int getTimestampSecond()
		{
			return this.dateCreated.get(Calendar.SECOND);
		}
		
		/**
		 * Get the hour offset from GMT of the timezone the file referred to by
		 * this entry was created and time stamped in.
		 * @return Integer representing an offset from GMT.
		 */
		public int getTimestampZoneOffset()
		{
			return this.dateCreated.get(Calendar.ZONE_OFFSET);
		}
		
		/**
		 * Get the timezone the file referenced by this entry was time stamped in.
		 * @return TimeZone object containing timezone information for this file.
		 */
		public TimeZone getTimestampTimezone()
		{
			return this.dateCreated.getTimeZone();
		}
		
		/**
		 * Get the number of sectors it would take to contain the file referenced by
		 * this entry.
		 * @return An integer representing the file size in sectors.
		 */
		public int getSizeInSectors()
		{
			int sectorCount = (int)(this.size / 0x930L);
			if (this.size % 0x930L != 0) sectorCount++;
			return sectorCount;
		}
		
		/* -- Setters -- */
		
		/**
		 * Set the name of the file or directory referred to by this entry.
		 * @param name New name of file or directory.
		 */
		public void setName(String name)
		{
			this.fileName = name;
		}

		/**
		 * Set the index of the sector/block (relative to image start) that this file begins at - 
		 * ie. the first sector/block containing data for the file referenced by this table entry.
		 * @param start Relative index of the start sector/block of file.
		 */
		public void setStartBlock(int start)
		{
			this.location = start;
		}
		
		/**
		 * Set the recorded file/directory size (in bytes). This is size of the file itself - it 
		 * need not align to sector/block boundaries.
		 * @param size Size, in bytes, of the file/directory.
		 */
		public void setFileSize(long size)
		{
			this.size = size;
		}
		
		/**
		 * Set directory flag for item referenced by this entry.
		 * @param isDir 
		 * <br>True - If item is to be treated as a directory.
		 * <br>False - If item is to be treated as a regular file.
		 */
		public void setIsDirectory(boolean isDir)
		{
			this.isDir = isDir;
		}
		
		/**
		 * Set the timestamp (date of file/directory creation) by providing a Gregorian Calendar
		 * containing information on the date, time, and timezone.
		 * @param stamp GregorianCalendar specifying the timestamp to set.
		 */
		public void setDate(GregorianCalendar stamp)
		{
			this.dateCreated = stamp;
		}
		
		/* -- Sorting -- */
		
		public int compareTo(CDTEntry o)
		{
			//Compares by start sector
			if (o.location < this.location) return 1;
			else if (o.location > this.location) return -1;
			
			if (this.fileName.compareTo(o.fileName) > 0) return 1;
			if (this.fileName.compareTo(o.fileName) < 0) return -1;
			return 0;
		}

		/* -- Information -- */
		
		public String toString()
		{
			return this.fileName;
		}
		
		/**
		 * Get an extended string providing information on the entry beyond the file/directory name.
		 * @return A multi-line string in English containing information about the entry.
		 */
		public String getInformation()
		{
			String s = "";
			s += this.fileName + "\n";
			s += "\t" + "Starting Sector: " + this.location + "\n";
			s += "\t" + "File Size: 0x" + Long.toHexString(size) + "\n";
			s += "\t" + "Is Directory: " + this.isDir + "\n";
			s += "\t" + "Timestamp: " + FileBuffer.formatTimeAmerican(this.dateCreated) + "\n";
			//s += "\t" + "Number of Sectors: " +  + "\n"; PUT ON OUTSIDE
			
			return s;
		}
		
		//Maybe need to set up to interpret A/V streaming as well?
	
	}
	
	/**
	 * Framework specialized directory entry class for optional recursive functionality in a CDTable.
	 * Holds all the standard record information for any item in a table, but contains a table of its
	 * own as well outlining its own contents.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since November 1, 2017
	 *
	 */
	public static class CDTDirEntry extends CDTEntry
	{
		protected CDTable localTable;
		
		/**
		 * Construct an empty CDTable directory entry.
		 */
		public CDTDirEntry()
		{
			super();
			localTable = null;
			super.setIsDirectory(true);
		}
		
		/**
		 * Construct a new CDTable directory entry from the directory name and the relative
		 * index of the sector its table lies in.
		 * @param name Name of directory.
		 * @param startBlock Relative sector/block index of table.
		 */
		public CDTDirEntry(String name, int startBlock)
		{
			super(name, startBlock);
			localTable = null;
			super.setIsDirectory(true);
		}
		
		/**
		 * Get this inner directory's local table. Can manipulate this object to view or
		 * change this directory's contents.
		 * @return Directory local table.
		 */
		public CDTable getTable()
		{
			return localTable;
		}

		public boolean isDirectory()
		{
			return true;
		}
		
	}
	
	/**
	 * Exception to be thrown in the event that an apparent entry in a CD image directory table
	 * cannot be parsed correctly.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since August 1, 2017
	 */
	public static class CDInvalidRecordException extends Exception
	{
		private static final long serialVersionUID = -3034167210242007132L;
		
		private String errorDetails;
		
		/**
		 * Construct an exception instance with a String describing the conditions under
		 * which it was constructed or thrown.
		 * @param details String containing an error message for debugging purposes.
		 */
		public CDInvalidRecordException(String details)
		{
			this.errorDetails = details;
		}
		
		/**
		 * Get information in the form of a String detailing why this exception instance
		 * was thrown.
		 * <br>May be used for user information or debugging.
		 * @return String containing error message.
		 */
		public String getErrorDetails()
		{
			return this.errorDetails;
		}
		
	}

	/**
	 * Get the absolute index of the first sector in the image. Index 0 is considered the first
	 * sector on the physical disk the image originated from; the first sector on the image
	 * may not be 0. In the case of standard ISO9660 (.iso) images, this value is usually 150.
	 * @return Integer representing the absolute index of the first sector in the image.
	 */
	public int getFirstSectorIndex();
	
	/**
	 * Get whether this image has a file or directory at the given path. Although
	 * the table does not contain the file itself, it does list the files found in a given
	 * directory.
	 * <br>This function is not guaranteed to check recursively. Inner directory tables are not required
	 * to be components of the same table as the root directory table, and usually begin in other sectors.
	 * Because the table contains no image data, unless the inner directories are parsed with the root
	 * directory, there is no way of knowing what information lies in the inner directory tables.
	 * <br>Some implementations of this method may throw an exception if an inner directory cannot be 
	 * opened.
	 * @param path The path (relative to the image file system) of the desired file or directory.
	 * @return True - If file or directory exists and could be found.
	 * <br>False - If the file or directory at that path does not exist, could not be found, or the path
	 * could not be followed.
	 */
	public boolean hasFile(String path);
	
	/**
	 * If the file or directory at the specified image-relative path can be found, return
	 * the table entry for it.
	 * @param path Path in image file system to desired file.
	 * @return CDTable entry containing information about desired file and its position on 
	 * the image.
	 * <br> null If file or directory could not be found.
	 */
	public CDTEntry getEntry(String path);
	
	/**
	 * Get the entry referencing the file with data in the given (relative) sector.
	 * @param sector Relative index of sector of interest.
	 * @return The entry for the file containing the given sector.
	 * <br> null If sector is invalid or file was not found.
	 */
	public CDTEntry getEntry(int sector);
	
	/**
	 * Get the relative index of the first sector/block of the file or directory
	 * at the given path, if file or directory exists
	 * and can be accessed.
	 * @param filePath Path, relative to the image file system, of desired file or directory.
	 * @return Relative index of the first sector of the image containing data for the specified
	 * file or directory.
	 * <br>null if specified file or directory could not be found.
	 */
	public int getFirstSector(String filePath);
	
	/**
	 * Get the size (in bytes) of the file or directory with the given path, if file or directory exists
	 * and can be accessed.
	 * @param filePath Path, relative to the image file system, of desired file or directory.
	 * @return Size in bytes of the specified file or directory, if found. This is the size of the file
	 * or directory itself, and will likely not line up evenly to sector boundary.
	 * <br>-1 if specified file or directory entry could not be found.
	 */
	public long getItemSize(String filePath);
	
	/**
	 * Get the total number of sectors/blocks in the image.
	 * Can be used to calculate total size of image in bytes as well.
	 * @return Number of used sectors in disk image. This does not count any sectors before
	 * the start of the image.
	 */
	public int getNumberSectors();
	
	
}
