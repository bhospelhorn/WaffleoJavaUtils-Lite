package waffleoRai_Containers;

import java.io.IOException;

import waffleoRai_Containers.WArcTable.WSDir;
import waffleoRai_Containers.WArcTable.WSRecord;
import waffleoRai_Utils.FDBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.VirDirectory;
import waffleoRai_Utils.VirFile;

public class WSArchive {

	public static final int TYPE_UNKNOWN = 0; //All I know is that it has the ptr table - .winkyarcx
	public static final int TYPE_1 = 1; //Like BGM - Has the size fields before file start - .winkyarcs
	public static final int TYPE_2 = 2; //Like SE - Recursive - .winkyarcr
	
	public static VirDirectory parseArchive(String filepath, WArcTable table) throws IOException
	{
		if (table == null) return null;
		//Open
		FileBuffer file = FileBuffer.createBuffer(filepath, false);
		return parseArchive(file, table);
	}
	
	public static VirDirectory parseArchive(FileBuffer file, WArcTable table) throws IOException
	{
		if (table == null) return null;
		
		String arcName = table.getArchiveName();
		VirDirectory root = new VirDirectory(arcName);
		
		WSDir rawdir = table.getRootDirectory();
		int fcount = rawdir.getRecordCount();
		
		for (int i = 0; i < fcount; i++)
		{
			FDBuffer element = parseElement(file, rawdir.getRecord(i));
			root.addItem(element);
		}
		
		return root;
	}
	
	private static FDBuffer parseElement(FileBuffer rawDir, WSRecord rec) throws IOException
	{
		if (rec.isDirectory())
		{
			//Make sub-buffer
			FileBuffer dir = rawDir.createReadOnlyCopy(rec.getStartOffset(), rec.getStartOffset() + rec.getSize());
			VirDirectory vd = new VirDirectory(rec.getName());
			int filecount = rec.getRecordCount();
			for (int i = 0; i < filecount; i++)
			{
				FDBuffer element = parseElement(dir, rec.getRecord(i));
				vd.addItem(element);
			}
			return vd;
		}
		else
		{
			//Grab file
			int stpos = rec.getStartOffset();
			int edpos = stpos + rec.getSize();
			FileBuffer file = rawDir.createReadOnlyCopy(stpos, edpos);
			VirFile vf = new VirFile(file, rec.getName());
			vf.setParserType(rec.getParserType());
			return vf;
		}
	}
	
}
