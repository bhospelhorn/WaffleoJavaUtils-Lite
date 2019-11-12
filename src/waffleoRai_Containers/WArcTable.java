package waffleoRai_Containers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import waffleoRai_Compression.huffman.Huffman;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.ParserType;
import waffleoRai_Utils.SerializedString;
import waffleoRai_Utils.Treenumeration;

public class WArcTable {
	
	/* ----- Constants ----- */
	
	public static final String MAGIC = "WARCTBLH";
	public static final String INNER_MAGIC = "WARCTBL_";
	public static final String EXT = "warctbl";
	
	public static final int CURRENT_VERSION = 1;
	
	/* ----- Instance Variables ----- */
	
	private int winkyarc_type;
	private String arcName;
	
	private WSDir rootRecord;
	
	/* ----- Constructors ----- */
	
	public WArcTable(FileBuffer wsarchive, int arc_type, String name) throws IOException
	{
		arcName = name;
		winkyarc_type = arc_type;
		//System.err.println("WArcTable.<init> || -DEBUG- Params read. arcName = " + arcName);
		
		//Try to parse out from the raw archive
		wsarchive.setEndian(false);
		long cpos = 0;
		
		//Read pointer table
		//System.err.println("WArcTable.<init> || -DEBUG- Reading pointer table!");
		int ptr = wsarchive.intFromFile(cpos); cpos+=4;
		int filecount = ptr/4;
		int[] pointers = new int[filecount];
		pointers[0] = ptr;
		for (int i = 1; i < filecount; i++)
		{
			//Offset from start to file - so cpos is already added
			int p = wsarchive.intFromFile(cpos);
			pointers[i] = p + (int)cpos;
			cpos += 4;
		}
		
		//System.err.println("WArcTable.<init> || -DEBUG- Generating directory tree...");
		rootRecord = new WSDir(filecount, null);
		for (int i = 0; i < filecount; i++)
		{
			//System.err.println("WArcTable.<init> || -DEBUG- RECORD " + i);
			int fsize = (int)wsarchive.getFileSize();
			int p1 = pointers[i];
			int p2 = -1;
			if (p1 >= fsize) continue;
			if (i+1 >= filecount) p2 = fsize;
			else p2 = pointers[i+1];
			//System.err.println("WArcTable.<init> || -DEBUG- \tPointer 1: 0x" + Integer.toHexString(p1));
			//System.err.println("WArcTable.<init> || -DEBUG- \tPointer 2: 0x" + Integer.toHexString(p2));
			if (p1 >= p2)
			{
				//System.err.println("WArcTable.<init> || -DEBUG- File " + i + " is empty. Continuing...");
				continue;
			}
			
			//Check if it's a type 1.
			//If so, there's some padding at the end. The size is noted before the file.
			if (arc_type == WSArchive.TYPE_1)
			{
				//System.err.println("WArcTable.<init> || -DEBUG- \t(Parse as winkyarcS)...");
				int sz = wsarchive.intFromFile(p1);
				//System.err.println("WArcTable.<init> || -DEBUG- \tFile Size Read: 0x" + Integer.toHexString(sz));
				p2 = p1 + sz;
				p1 += 0x14;
				//System.err.println("WArcTable.<init> || -DEBUG- \tNew Pointer 1: 0x" + Integer.toHexString(p1));
				//System.err.println("WArcTable.<init> || -DEBUG- \tNew Pointer 2: 0x" + Integer.toHexString(p2));
			}
			
			//See if could be a directory...
			if (arc_type == WSArchive.TYPE_2)
			{
				FileBuffer subdata = wsarchive.createReadOnlyCopy(p1, p2);
				WSRecord r = parseAsPotentialDirectory(rootRecord, arcName, i, p1, p2, subdata);
				rootRecord.addRecord(r);
			}
			else
			{
				//Assume it's a file
				WSRecord r = getNewFileRecord(rootRecord, arcName, i, p1, p2);
				rootRecord.addRecord(r);
			}
		}
		//System.err.println("WArcTable.<init> || -DEBUG- Done!");
		
	}
	
	public WArcTable(String tableFilePath) throws IOException, UnsupportedFileTypeException
	{
		//Read in a table previously saved
		
		//warctbl format
		
		//Magic [8]
		//Version [4]
		//Archive type [4]
		//File count (top level) [4]
		
		//Archive name [VLS]
		
		//Records...
		//	File:
		//		Start pos (relative to dir start) [4]
		//		Size [4]
		//		Flags [4]
		//			(0) - IsDir
		//		Parser type [4]
		//		Name [VLS]
		
		//	Directory:
		//		File Part [16 + VLS]
		//		Number files/inner dirs [4]
		//			Files and inner dirs...
		
		//** VLS = Variable length string
		//	[2] Strlen
		//	[var] String
		//	[0-1] Padding to 2 bytes
		
		FileBuffer file = FileBuffer.createBuffer(tableFilePath, true);
		long cpos = 0;
		
		//Scan for Magic ID
		cpos = file.findString(0, 0x10, MAGIC);
		if (cpos != 0) throw new FileBuffer.UnsupportedFileTypeException();
		
		//De-Huff
		FileBuffer dec = Huffman.HuffDecodeFile(file, MAGIC.length());
		cpos = dec.findString(0, 0x10, INNER_MAGIC);
		if (cpos != 0) throw new FileBuffer.UnsupportedFileTypeException();
		file = dec;
		
		//String debugpath = "C:\\Users\\Blythe\\Documents\\Desktop\\WARCTEST." + EXT;
		//dec.writeFile(debugpath);
		
		//Read main stuff
		cpos = 12; //Skip inner magic and version
		winkyarc_type = file.intFromFile(cpos); cpos += 4;
		int filecount = file.intFromFile(cpos); cpos += 4;
		rootRecord = new WSDir(null);
		//System.err.println("WArcTable.<init> || -DEBUG- WinkyArc Type: " + winkyarc_type);
		//System.err.println("WArcTable.<init> || -DEBUG- File Count: " + filecount);
		
		//Archive name
		SerializedString ss = file.readVariableLengthString(cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		arcName = ss.getString();
		rootRecord.setName(arcName);
		
		for (int i = 0; i < filecount; i++)
		{
			//System.err.println("WArcTable.<init> || -DEBUG- Parsing Record " + i);
			//System.err.println("WArcTable.<init> || -DEBUG- Position: 0x" + Long.toHexString(cpos));
			WSRecord r = parseFromWARCTBL(file, cpos, rootRecord);
			cpos += r.getSerializedSize();
		}
	}
	
	/* ----- Helper Classes ----- */
	
	public static class WSDir extends WSRecord
	{
		private ArrayList<WSRecord> records;
		
		public WSDir(WSDir parentDir)
		{
			super(parentDir);
			records = new ArrayList<WSRecord>(256);
		}
		
		public WSDir(int initSize, WSDir parentDir)
		{
			super(parentDir);
			records = new ArrayList<WSRecord>(initSize);
		}
		
		public boolean isDirectory()
		{
			return true;
		}
		
		public WSRecord getRecord(int index)
		{
			return records.get(index);
		}
		
		public int getRecordCount()
		{
			return records.size();
		}
		
		public void addRecord(WSRecord r)
		{
			records.add(r);
		}
		
		public void setRecord(int index, WSRecord r)
		{
			records.set(index, r);
		}
		
		public int getSerializedSize()
		{
			int sz = super.getSerializedSize();
			sz += 4;
			for (WSRecord r : records) sz += r.getSerializedSize();
			return sz;
		}
	
		public FileBuffer serializeMe(boolean root)
		{
			int nrec = records.size();
			FileBuffer fb = new CompositeBuffer(1 + nrec);
			if(!root)
			{
				FileBuffer top = super.serializeMe(false);
				top.addToFile(nrec);
				fb.addToFile(top);
			}
			
			for (WSRecord r : records)
			{
				fb.addToFile(r.serializeMe(false));
			}
			
			return fb;
		}
	
		public boolean hasInnerDir()
		{
			for (WSRecord r : records)
			{
				if (r.isDirectory()) return true;
			}
			return false;
		}
	
		public TreeNode getChildAt(int childIndex) 
		{
			return getRecord(childIndex);
		}
		
		@Override
		public int getChildCount() 
		{
			return getRecordCount();
		}
		
		@Override
		public int getIndex(TreeNode node) 
		{
			if (records == null) return -1;
			int rcount = records.size();
			for (int i = 0; i < rcount; i++)
			{
				WSRecord r = records.get(i);
				if (r == node) return i;
			}
			return -1;
		}
		
		@Override
		public boolean getAllowsChildren() 
		{
			return true;
		}
		
		@Override
		public boolean isLeaf() 
		{
			if (records == null || records.isEmpty()) return true;
			return false;
		}
		
		@Override
		public Enumeration<TreeNode> children() 
		{
			List<TreeNode> list = new ArrayList<TreeNode>(records.size());
			list.addAll(records);
			return new Treenumeration(list);
		}

		public TreeModel toTreeModel()
		{
			return new DefaultTreeModel(this);
		}
			
		public List<WSRecord> getAllFileRecords()
		{
			List<WSRecord> rlist = new LinkedList<WSRecord>();
			for(WSRecord r : records)
			{
				if (!r.isDirectory()) rlist.add(r);
			}
			return rlist;
		}
		
		public List<WSRecord> getAllFileRecordsRecursive()
		{
			List<WSRecord> rlist = new LinkedList<WSRecord>();
			for(WSRecord r : records)
			{
				if (!r.isDirectory()) rlist.add(r);
				else
				{
					WSDir dir = (WSDir)r;
					rlist.addAll(dir.getAllFileRecordsRecursive());
				}
			}
			return rlist;
		}
		
		public List<WSDir> getAllDirectoryRecords()
		{
			List<WSDir> rlist = new LinkedList<WSDir>();
			for(WSRecord r : records)
			{
				if (r.isDirectory()) rlist.add((WSDir)r);
			}
			return rlist;
		}
		
		public List<WSDir> getAllDirectoryRecordsRecursive()
		{
			List<WSDir> rlist = new LinkedList<WSDir>();
			for(WSRecord r : records)
			{
				if (r.isDirectory())
				{
					WSDir dir = (WSDir)r;
					rlist.add(dir);
					rlist.addAll(dir.getAllDirectoryRecordsRecursive());
				}
			}
			return rlist;
		}
		
		public List<WSRecord> getAllRecords()
		{
			List<WSRecord> rlist = new ArrayList<WSRecord>(records.size() + 1);
			rlist.addAll(records);
			return rlist;
		}
		
		public List<WSRecord> getAllRecordsRecursive()
		{
			List<WSRecord> rlist = new LinkedList<WSRecord>();
			for(WSRecord r : records)
			{
				rlist.add(r);
				if (r.isDirectory())
				{
					WSDir dir = (WSDir)r;
					rlist.addAll(dir.getAllRecordsRecursive());
				}
			}
			return rlist;
		}
		
	}
	
	public static class WSRecord implements TreeNode
	{
		private int startPos;
		private int size;
		
		private String name;
		private ParserType parser_type;
		
		private WSDir parent;
		
		public WSRecord(WSDir parentDir)
		{
			name = null;
			startPos = -1;
			size = -1;
			parser_type = null;
			parent = parentDir;
			if(parent != null) parent.addRecord(this);
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getFullName()
		{
			if (parser_type == null) return name;
			return name + "." + parser_type.getExtension();
		}
		
		public ParserType getParserType()
		{
			return parser_type;
		}
		
		public int getStartOffset()
		{
			return this.startPos;
		}
		
		public int getSize()
		{
			return this.size;
		}
		
		public boolean isDirectory()
		{
			return false;
		}
		
		public void setName(String s)
		{
			name = s;
		}
		
		public void setParserType(ParserType type)
		{
			parser_type = type;
		}
		
		public void setStartOffset(int stpos)
		{
			this.startPos = stpos;
		}
		
		public void setSize(int sz)
		{
			this.size = sz;
		}
		
		public WSRecord getRecord(int index)
		{
			return null;
		}
		
		public int getRecordCount()
		{
			return 1;
		}
		
		public void addRecord(WSRecord r)
		{
			//Do nothing
		}
		
		public void setRecord(int index, WSRecord r)
		{
			//Do nothing
		}
			
		public int getSerializedSize()
		{
			int sz = 16 + name.length() + 2;
			if (sz % 2 != 0) sz++;
			return sz;
		}
	
		public FileBuffer serializeMe(boolean root)
		{
			FileBuffer fb = new FileBuffer(getSerializedSize() + 4, true);
			fb.addToFile(startPos);
			fb.addToFile(size);
			fb.addToFile(0);
			if (this.parser_type == null) fb.addToFile(ParserType.BINARY_UNKNOWN.getNumber());
			else fb.addToFile(parser_type.getNumber());
			fb.addVariableLengthString(name, BinFieldSize.WORD, 2);
			return fb;
		}

		public TreeNode getChildAt(int childIndex) 
		{
			return null;
		}

		@Override
		public int getChildCount() 
		{
			return 0;
		}
		
		@Override
		public TreeNode getParent() 
		{
			return parent;
		}
		
		@Override
		public int getIndex(TreeNode node) 
		{
			return -1;
		}
		
		@Override
		public boolean getAllowsChildren() 
		{
			return false;
		}
		
		@Override
		public boolean isLeaf() 
		{
			return true;
		}
		
		@Override
		public Enumeration<TreeNode> children() 
		{
			TreeNode[] n = null;
			return new Treenumeration(n);
		}
		
		public WSDir getParentDir()
		{
			return this.parent;
		}
	
		public WSRecord[] getPath()
		{
			LinkedList<WSRecord> ancestors = new LinkedList<WSRecord>();
			ancestors.add(this);
			WSDir p = this.getParentDir();
			while (p != null)
			{
				ancestors.add(p);
				p = p.getParentDir();
			}
			int acount = ancestors.size();
			WSRecord[] arr = new WSRecord[acount];
			for (int i = 0; i < acount; i++)
			{
				arr[i] = ancestors.pollLast();
			}
			
			return arr;
		}
		
		public String getPathString(char separator)
		{
			WSRecord[] path = getPath();
			StringBuilder sb = new StringBuilder(256);
			for (int i = 1; i < path.length; i++) //Skip root
			{
				sb.append(separator);
				sb.append(path[i].getFullName());
			}
			return sb.toString();
		}

		public String toString()
		{
			return getFullName();
		}
		
	}
	
	/* ----- Parsing ----- */
	
	private WSRecord getNewFileRecord(WSDir parent, String dirname, int index, int startPtr, int endPtr)
	{
		WSRecord r = new WSRecord(parent);
		r.setName(dirname + "_" + String.format("%04d", index));
		r.setParserType(ParserType.BINARY_UNKNOWN);
		r.setStartOffset(startPtr);
		r.setSize(endPtr-startPtr);
		return r;
	}
	
	private int innerDirFileCount(FileBuffer data)
	{
		//Assumed data is already trimmed.
		int count = 0;
		long cpos = 0;
		int word = data.intFromFile(cpos);
		while (word != 0)
		{
			//Check pointer
			int ptr = (int)cpos + word;
			if (ptr >= data.getFileSize()) return -1;
			
			count++;
			cpos += 4;
			word = data.intFromFile(cpos);
		}
		
		return count;
	}
	
	private WSRecord parseAsPotentialDirectory(WSDir parent, String upperdir_name, int upperdir_index, int startPtr, int endPtr, FileBuffer data) throws IOException
	{
		//What would qualify as a directory?
		//Must start with pointer table, but this time it seems the pointer table is out of order
		//Instead, it ends with a 0x00000000 word? Is that just padding?
		
		//So, start scanning the data word by word.
		//	If every word value plus its offset from the start of the potential dir
		//  is within the provided dir/file up to the terminating word, this is read
		//	as a pointer table for an inner dir.
		//Otherwise, data treated as a file.
		
		int filecount = innerDirFileCount(data);
		if (filecount <= 0)
		{
			return getNewFileRecord(parent, upperdir_name, upperdir_index, startPtr, endPtr);
		}
		
		//Make dir record...
		WSDir mydir = new WSDir(filecount, parent);
		String myname = upperdir_name + "_" + String.format("%04d", upperdir_index);
		mydir.setName(myname);
		mydir.setParserType(ParserType.BINARY_UNKNOWN);
		mydir.setStartOffset(startPtr);
		mydir.setSize(endPtr-startPtr);
		
		//Scan directory table and sort
		List<Integer> ptrtbl = new ArrayList<Integer>(filecount);
		long cpos = 0;
		for (int i = 0; i < filecount; i++)
		{
			int word = data.intFromFile(cpos);
			ptrtbl.add(word + (int)cpos);
			cpos += 4;
		}
		Collections.sort(ptrtbl);
		
		//Populate directory (recursively)
		for (int i = 0; i < filecount; i++)
		{
			int stpos = ptrtbl.get(i);
			int edpos = -1;
			if (i + 1 >= filecount) edpos = (int)data.getFileSize();
			else edpos = ptrtbl.get(i+1);
			
			FileBuffer subdata = data.createReadOnlyCopy(stpos, edpos);
			WSRecord sub = parseAsPotentialDirectory(mydir, myname, i, stpos, edpos, subdata);
			mydir.addRecord(sub);
		}
		
		return mydir;
	}
	
	private WSRecord parseFromWARCTBL(FileBuffer data, long pos, WSDir parentDir)
	{
		long cpos = pos;
		int stpos = data.intFromFile(cpos); cpos += 4;
		int size = data.intFromFile(cpos); cpos += 4;
		int flags = data.intFromFile(cpos); cpos += 4;
		int ptype = data.intFromFile(cpos); cpos += 4;
		//System.err.println("WArcTable.parseFromWARCTBL || -DEBUG- \tstpos: 0x" + Integer.toHexString(stpos));
		//System.err.println("WArcTable.parseFromWARCTBL || -DEBUG- \tsize: 0x" + Integer.toHexString(size));
		//System.err.println("WArcTable.parseFromWARCTBL || -DEBUG- \tflags: 0x" + Integer.toHexString(flags));
		//System.err.println("WArcTable.parseFromWARCTBL || -DEBUG- \tptype: " + ptype);
		
		SerializedString ss = data.readVariableLengthString(cpos, BinFieldSize.WORD, 2);
		cpos += ss.getSizeOnDisk();
		String name = ss.getString();
		//System.err.println("WArcTable.parseFromWARCTBL || -DEBUG- \tname: " + name);
		
		//See if dir...
		boolean isdir = (flags & 0x1) != 0;
		
		if (isdir)
		{
			WSDir d = new WSDir(parentDir);
			d.setName(name);
			d.setParserType(ParserType.getType(ptype));
			d.setSize(size);
			d.setStartOffset(stpos);
			
			int filecount = data.intFromFile(cpos); cpos += 4;
			for (int i = 0; i < filecount; i++)
			{
				WSRecord r = parseFromWARCTBL(data, cpos, d);
				cpos += r.getSerializedSize();
				d.addRecord(r);
			}
			
			return d;
		}
		else
		{
			WSRecord r = new WSRecord(parentDir);
			r.setName(name);
			r.setParserType(ParserType.getType(ptype));
			r.setSize(size);
			r.setStartOffset(stpos);
			return r;
		}
	}
	
	/* ----- Getters ----- */
	
	public String getArchiveName()
	{
		return arcName;
	}
	
	public int getType()
	{
		return winkyarc_type;
	}
	
	public boolean isRecursive()
	{
		return rootRecord.hasInnerDir();
	}
	
	public WSDir getRootDirectory()
	{
		return this.rootRecord;
	}
	
	/* ----- Setters ----- */
	
	public void setName(String s)
	{
		arcName = s;
	}
	
	public void setType(int t)
	{
		winkyarc_type = t;
	}
	
	/* ----- Serialization ----- */
	
	public void writeToFile(String outpath) throws IOException
	{
		int filecount = rootRecord.getRecordCount();
		
		int headersize = 20 + this.arcName.length() + 2;
		if (headersize % 2 != 0) headersize++;
		FileBuffer header = new FileBuffer(headersize, true);
		header.printASCIIToFile(INNER_MAGIC);
		header.addToFile(CURRENT_VERSION);
		header.addToFile(winkyarc_type);
		header.addToFile(filecount);
		header.addVariableLengthString(arcName, BinFieldSize.WORD, 2);
		
		FileBuffer rawfile = new CompositeBuffer(2);
		rawfile.addToFile(header);
		rawfile.addToFile(rootRecord.serializeMe(true));
		
		//Compress
		FileBuffer enc = Huffman.HuffEncodeFile(rawfile, 8, MAGIC);
		
		//Write
		enc.writeFile(outpath);
	}

}
