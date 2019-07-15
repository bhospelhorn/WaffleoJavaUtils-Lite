package waffleoRai_Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/*
 * UPDATES
 * 2017.09.24
 * 	1.1.0 -> 1.1.1 | Minor update for slash character detection.
 * 
 * 2017.11.01
 * 	1.1.1 -> 1.1.2 | Added console printout representation
 * 
 * 2017.11.12
 * 	1.1.2 -> 1.1.3 | Added TreePath conversion (not yet debugged)
 * 
 * 2019.01.26
 * 	1.1.3 -> 1.2.0 | Added file extensions and file name changing
 * 
 * 2019.02.17
 * 	1.2.0 -> 1.3.0 | Added better path tracking
 * 	1.3.0 -> 1.3.1 | Added method for getting files recursively
 */

/**
 * Object to mimic a nested file system, holding all files in memory.
 * @author Blythe Hospelhorn
 * @version 1.3.1
 * @since February 17, 2019
 */
public class VirDirectory implements FDBuffer{

	private Map<String, FDBuffer> contents;
	private String name;
	private char divider;
	
	private VirDirectory parent;
	private FDBuffer[] indexedChildren;
	
	/**
	 * Constructor to create empty virtual directory.
	 * Sets default path delimiter to backslash '\\'
	 * @param dName : Name of directory
	 */
	public VirDirectory(String dName)
	{
		this.contents = new HashMap<String, FDBuffer>();
		this.name = dName;
		this.divider = File.separatorChar;
		this.parent = null;
		this.indexedChildren = null;
	}
	
	/**
	 * Constructor to create virtual directory mimicking a directory on disc.
	 * Sets default path delimiter to backslash '\\'
	 * @param path : Path of directory to copy into memory.
	 * @param dName : Desired name of directory
	 * @throws IOException : If it cannot find/read directory at specified path.
	 */
	public VirDirectory(String path, String dName) throws IOException
	{
		this.name = dName;
		this.contents = new HashMap<String, FDBuffer>();
		this.divider = File.separatorChar;
		this.readDirectory(path);
		this.parent = null;
		this.indexedChildren = null;
	}
	
	/**
	 * Constructor to create empty virtual directory with custom delimiter character.
	 * @param dName : Desired name of directory
	 * @param divider : Delimiting character
	 */
	public VirDirectory(String dName, char divider)
	{
		this.name = dName;
		this.contents = new HashMap<String, FDBuffer>();
		this.divider = divider;
		this.parent = null;
		this.indexedChildren = null;
	}
	
	/**
	 * Constructor that takes a path object to create
	 * a virtual directory that mimics a directory on disc.
	 * @param path : Path to directory
	 * @throws IOException : If it cannot find/read directory at specified path.
	 */
	public VirDirectory(Path path) throws IOException
	{
		this.name = path.getFileName().toString();
		this.contents = new HashMap<String, FDBuffer>();
		this.divider = File.separatorChar;
		this.readDirectory(path);
		this.parent = null;
		this.indexedChildren = null;
	}
	
	/**
	 * Reads an existing directory into virtual directory.
	 * @param dirPath : Path to directory to read.
	 * @throws IOException : If it cannot find/read directory at specified path.
	 */
	public void readDirectory(String dirPath) throws IOException
	{
		if (!FileBuffer.directoryExists(dirPath)) 
		{
			//System.out.println("Path [" + dirPath + "] does not appear to be a directory.");
			//System.out.println("Now returning...");
			throw new IOException();
		}
		Path myPath = Paths.get(dirPath, "");
		this.readDirectory(myPath);
	}
	
	/**
	 * Reads an existing directory into virtual directory, takes a Path object.
	 * @param dirPath : Path to directory to read.
	 * @throws IOException : If it cannot find/read directory at specified path.
	 */
	public void readDirectory(Path dirPath) throws IOException
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath))
		{
			for (Path f : stream)
			{
				if (FileBuffer.fileExists(f.toString()))
				{
					FileBuffer myFile = new FileBuffer(f.toString());
					String fname = f.getFileName().toString();
					this.addItem(new VirFile(myFile, fname));
				}
				else if (FileBuffer.directoryExists(f.toString()))
				{
					VirDirectory subDir = new VirDirectory(f.getFileName().toString());
					subDir.readDirectory(f.toString());
				}
				//System.out.println(f.getFileName());
			}
			stream.close();
		}
		catch (IOException | DirectoryIteratorException e)
		{
			System.out.println("Unknown Read Error...");
			e.printStackTrace();
		}
	}
	
	/**
	 * Get whether the FDBuffer is a file or directory.
	 * @return FDBufferType enum specifying file or directory.
	 */
	public FDBufferType getType()
	{
		return FDBufferType.DIR;
	}
	
	/**
	 * Gets the name of this directory.
	 * @return Directory name
	 */
	public String getName()
	{
		return this.name;
	}
	
	public String getFullName()
	{
		return name;
	}
	
	/**
	 * Add a virtual file (VirFile) or inner virtual directory (VirDirectory)
	 * to this directory at the root level.
	 * @param myItem : File or directory to add.
	 */
	public void addItem(FDBuffer myItem)
	{
		String key = myItem.getName();
		this.contents.put(key, myItem);
	}
	
	/**
	 * Add a file to this directory reflecting the path string specified.
	 * Will create all intermediate nesting directories if they do not exist.
	 * @param myFile : Raw file to wrap in VirFile and put in directory.
	 * @param pathname : String specifying target path of file in this directory.
	 */
	public void addItem(FileBuffer myFile, String pathname)
	{
		//System.out.println("addItem path overload called for VirDir " + this.name + " Path = " + pathname);
		//String slsh = "" + divider;
		int slash = pathname.lastIndexOf(divider);
		String filename = pathname;
		String dir = null;
		//System.out.println("Checkpoint 1");
		if (slash >= 0)
		{
			//System.out.println("Checkpoint 2");
			filename = pathname.substring(slash+1, pathname.length());
			dir = pathname.substring(0, slash);
		}
		//System.out.println("Checkpoint 3 | dir = " + dir + " filename = " + filename);
		VirFile vfile = new VirFile(myFile, filename);
		//System.out.println("Checkpoint 4");
		if (dir == null) 
		{
			this.addItem(vfile);
			//System.out.println("addItem returning VirDir " + this.name + " Path = " + pathname + " Point 1");
			return;
		}
		
		String sDir = dir;
		FDBuffer deepestExisting = this.getItem(sDir);
		//System.out.println("Checkpoint 5");
		while (deepestExisting == null || !(deepestExisting instanceof VirDirectory))
		{
			slash = sDir.lastIndexOf(divider);
			//System.out.println("slash = " + slash);
			if (slash < 0)
			{
				sDir = null;
				break;
			}
			sDir = sDir.substring(0, slash);
			if (sDir.isEmpty())
			{
				sDir = null;
				break;
			}
			deepestExisting = this.getItem(sDir);
			//System.out.println("sDir = " + sDir);
		}
		//System.out.println("Checkpoint 6");
		if (deepestExisting == null || !(deepestExisting instanceof VirDirectory)) deepestExisting = this;
		//System.out.println("Checkpoint 6.5");
		String[] dfPath = this.splitStringAlongDivider(dir);
		//for (String str : dfPath) System.out.println("dfPath | "+ str);
		String[] foundPath = null;
		if (sDir != null) foundPath = this.splitStringAlongDivider(sDir);
		else foundPath = new String[0];
		//for (String str : foundPath) System.out.println("foundPath | "+ str);
		int dfi = foundPath.length;
		//System.out.println("Checkpoint 7 | dfi = " + dfi);
		while (dfi < dfPath.length)
		{
			VirDirectory nDir = new VirDirectory(dfPath[dfi]);
			VirDirectory deep = (VirDirectory)deepestExisting;
			deep.addItem(nDir);
			deepestExisting = nDir;
			dfi++;
		}
		//System.out.println("Checkpoint 8");
		VirDirectory deep = (VirDirectory)deepestExisting;
		deep.addItem(vfile);
		//System.out.println("addItem returning VirDir " + this.name + " Path = " + pathname + " Point 2");
	}
	
	/**
	 * Checks whether an item with the specified path exists within this
	 * virtual directory.
	 * @param path : The path of the item to check
	 * @return Whether or not the item exists
	 */
	public boolean itemExists(String path)
	{
		if (this.contents.containsKey(path)) return true;
		
		int firstSlash = name.indexOf(this.divider);
		if (firstSlash < 0) return false;
		
		String dName = name.substring(0, firstSlash);
		String fName = name.substring(firstSlash + 1);
		
		//See if the first part refers to a subdirectory
		FDBuffer subItm = this.contents.get(dName);
		if (subItm == null) return false;
		if (!(subItm instanceof VirDirectory)) return false;
		VirDirectory subDir = (VirDirectory)subItm;
		
		return subDir.itemExists(fName);
	}
	
	/**
	 * Does that same thing as itemExists, except does not return true
	 * if the item exists, but is a file rather than a directory.
	 * @param path : The path of the item to check
	 * @return Whether or not the item exists
	 */
	public boolean directoryExists(String path)
	{
		FDBuffer item = this.getItem(path);
		if (item == null) return false;
		if (!(item instanceof VirDirectory)) return false;
		
		return true;
	}
	
	/**
	 * Retrieve the VirFile or VirDirectory from this directory at
	 * the specified path. If it doesn't exist, return null.
	 * @param name : Name/ Path of file or directory to retrieve
	 * @return File or directory if it exists. If not, null.
	 */
	public FDBuffer getItem(String name)
	{
		FDBuffer target = this.contents.get(name);
		if (target != null) return target;
		
		int firstSlash = name.indexOf(this.divider);
		if (firstSlash < 0) return null;
		
		String dName = name.substring(0, firstSlash);
		String fName = name.substring(firstSlash + 1);
		
		//See if the first part refers to a subdirectory
		target = this.contents.get(dName);
		
		//If not, return null or file
		if (target == null) return null;
		if (target.getType() == FDBufferType.FILE) return target;
		
		//If so, return the result of a getItem call to this subdir
		VirDirectory subDir = (VirDirectory)target;
		return subDir.getItem(fName);
	}
	
	/**
	 * Counts the total number of subdirectories and files in this directory.
	 * @return Total number of items
	 */
	public int countItems()
	{
		return this.contents.size();
	}

	/**
	 * Calculates the size of this directory including all of its internal files
	 * and subdirectories.
	 * In reality, it will likely be bigger if extracted raw on disk, but this is mostly
	 * for the purpose of serialized archive generation.
	 * @return Size of directory data
	 */
	public long getSizeOnDisk()
	{
		long sz = 0;
		Collection<FDBuffer> vals = this.contents.values();
		for (FDBuffer b : vals)
		{
			sz += b.getSizeOnDisk();
		}
		
		return sz;
	}
	
	/**
	 * Counts the number of subdirectories in this directory
	 * @return Number of next level subdirectories
	 */
	public int countDirectories()
	{
		int c = 0;
		Collection<FDBuffer> vals = this.contents.values();
		for (FDBuffer b : vals)
		{
			if (b.getType() == FDBufferType.DIR) c++;
		}
		
		return c;
	}
	
	/**
	 * Counts the number of files (not directories) in the top level of this
	 * directory.
	 * @return Number of top level files in directory.
	 */
	public int countFiles()
	{
		int c = 0;
		Collection<FDBuffer> vals = this.contents.values();
		for (FDBuffer b : vals)
		{
			if (b.getType() == FDBufferType.FILE) c++;
		}
		
		return c;
	}
	
	/**
	 * Generates a list view of all top level items in this directory, including
	 * subdirectories and files for sorting and iteration.
	 * @return List of all top level items.
	 */
	public List<FDBuffer> getAllItems()
	{
		Collection<FDBuffer> vals = this.contents.values();
		List<FDBuffer> dList = new ArrayList<FDBuffer>(this.contents.size());
		dList.addAll(vals);
		
		Collections.sort(dList);
		return dList;
	}
	
	/**
	 * Generates a list view of all top level subdirectories for sorting and iteration.
	 * @return List of all top level directories.
	 */
	public List<VirDirectory> getDirectories()
	{
		Collection<FDBuffer> vals = this.contents.values();
		List<VirDirectory> dList = new ArrayList<VirDirectory>(this.countDirectories());
		for (FDBuffer b : vals)
		{
			if (b instanceof VirDirectory)
			{
				dList.add((VirDirectory)b);
			}
		}
		
		Collections.sort(dList);
		return dList;
	}
	
	/**
	 * Generates a list view of all top level files for sorting and iteration.
	 * @return List of all top level files.
	 */
	public List<VirFile> getFiles()
	{
		Collection<FDBuffer> vals = this.contents.values();
		List<VirFile> dList = new ArrayList<VirFile>(this.countFiles());
		for (FDBuffer b : vals)
		{
			if (b instanceof VirFile)
			{
				dList.add((VirFile)b);
			}
		}
		
		Collections.sort(dList);
		return dList;
	}
	
	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (o == this) return true;
		if (o instanceof String)
		{
			String s = (String)o;
			if (s.equals(this.name)) return true;
			else return false;
		}
		else if (o instanceof VirDirectory)
		{
			VirDirectory d = (VirDirectory)o;
			if (!d.getName().equals(this.name)) return false;
			if (!d.contents.equals(this.contents)) return false;
			return true;
		}
		return false;
	}
	
	public int compareTo(FDBuffer o) 
	{
		if (o == null) return -1;
		if (o instanceof VirFile) return -1;
		return this.name.compareTo(o.getName());
	}
	
	/**
	 * Writes all contents of this directory to disc using the native file system.
	 * @param DirPath : Path of directory to dump to.
	 * @throws IOException : If something cannot be written or found for any reason.
	 */
	public void extractTo(String DirPath) throws IOException
	{
		List<FDBuffer> items = this.getAllItems();
		char sysDelimiter = File.separatorChar;
		
		File myPath = new File(DirPath);
		if (!myPath.exists())
		{
			if (!myPath.mkdirs()) throw new IOException();
		}
		
		for (FDBuffer i : items)
		{
			if (i instanceof VirDirectory)
			{
				VirDirectory d = (VirDirectory)i;
				String dPath = DirPath + sysDelimiter + d.getName();
				d.extractTo(dPath);
			}
			else if (i instanceof VirFile)
			{
				VirFile f = (VirFile)i;
				String fPath = DirPath + sysDelimiter + f.getName();
				f.getFile().writeFile(fPath);
			}
		}
	}

	public void setParentDirectory(VirDirectory p)
	{
		this.parent = p;
	}

	private void indexChildren()
	{
		this.indexedChildren = new FDBuffer[this.contents.size()];
		List<FDBuffer> l = this.getAllItems();
		
		int i = 0;
		for (FDBuffer o : l)
		{
			o.setParentDirectory(this);
			this.indexedChildren[i] = o;
			i++;
		}
	}

	public boolean treeNeedsRefresh()
	{
		if (this.indexedChildren == null) return true;
		if (this.indexedChildren.length != this.contents.size()) return true;
		return false;
	}
	
	public void refreshTreeView()
	{
		if (this.treeNeedsRefresh()) this.indexChildren();
	}
	
	public TreeNode getChildAt(int childIndex) 
	{
		this.refreshTreeView();
		if (childIndex < 0 || childIndex >= this.indexedChildren.length) return null;
		return this.indexedChildren[childIndex];
	}

	@Override
	public int getChildCount() 
	{
		this.refreshTreeView();
		return this.indexedChildren.length;
	}

	@Override
	public TreeNode getParent() 
	{
		return this.parent;
	}

	@Override
	public int getIndex(TreeNode node) 
	{
		int nChildren = this.getChildCount();
		for (int i = 0; i < nChildren; i++)
		{
			if (indexedChildren[i].equals(node)) return i;
		}
		return -1;
	}

	@Override
	public boolean getAllowsChildren() 
	{
		return true;
	}

	@Override
	public boolean isLeaf() {
		int nChildren = this.getChildCount();
		return (nChildren == 0);
	}

	@Override
	public Enumeration<TreeNode> children() 
	{
		refreshTreeView();
		return new Treenumeration(this.indexedChildren);
	}
	
	public TreeModel toTreeModel()
	{
		refreshTreeView();
		return new DefaultTreeModel(this);
	}
	
	public String toString()
	{
		return this.name;
	}
	
	public int countCharInString(String s, char c)
	{
		int tot = 0;
		for (int i = 0; i < s.length(); i++)
		{
			if (s.charAt(i) == c) tot++;
		}
		
		return tot;
	}
	
	public String[] splitStringAlongDivider(String s)
	{
		//System.out.println("Split called | input string  = " + s);
		if (s == null) return null;
		String t = s;
		String[] sarr = new String[countCharInString(s, divider) + 1];
		//System.out.println("Split | output array length  = " + sarr.length);
		int i = 0;
		int slash = t.indexOf(divider);
		while (slash >= 0 && i < sarr.length)
		{
			sarr[i] = t.substring(0, slash);
			t = t.substring(slash + 1);
			slash = t.indexOf(divider);
			i++;
		}
		sarr[i] = t;
		return sarr;
	}
	
	public void printTree(int tabs)
	{
		for (int i = 0; i < tabs; i++) System.out.print("\t");
		System.out.print(this.name);
		System.out.println();
		List<FDBuffer> l = this.getAllItems();
		for (FDBuffer f : l)
		{
			f.printTree(tabs + 1);
		}
	}
	
	public static String TreePath_to_String(TreePath path, char separator)
	{
		String s = "";
		Object[] oPath = path.getPath();
		for (int i = 0; i < oPath.length; i++) 
		{
			s += oPath[i].toString();
			if (i < oPath.length - 1) s += separator;
		}
		return s;
	}
	
	protected void moveItem(String oldname, String newname)
	{
		FDBuffer item = contents.remove(oldname);
		if (item != null) contents.put(newname, item);
	}

	public void moveAnItem(String oldpath, String newpath)
	{
		FDBuffer item = removeItem(oldpath);
		if (item != null) addItem(newpath, item);
	}
	
	public FDBuffer removeItem(String path)
	{
		String[] paths = splitStringAlongDivider(path);
		if (paths.length > 1)
		{
			VirDirectory currentDir = this;
			for (int i = 0; i < paths.length-1; i++)
			{
				FDBuffer nextItem = currentDir.contents.get(paths[i]);
				if (nextItem == null) return null;
				if (nextItem instanceof VirDirectory)
				{
					currentDir = (VirDirectory)nextItem;
				}
				else return null;
			}	
			return currentDir.contents.remove(paths[paths.length - 1]);
		}
		else
		{
			return contents.remove(path);
		}
	}
	
	public void addItem(String path, FDBuffer item)
	{
		String[] paths = splitStringAlongDivider(path);
		if (paths.length > 1)
		{
			VirDirectory currentDir = this;
			for (int i = 0; i < paths.length-1; i++)
			{
				FDBuffer nextItem = currentDir.contents.get(paths[i]);
				if (nextItem == null)
				{
					//Make new directory
					currentDir.contents.put(paths[i], new VirDirectory(paths[i]));
					nextItem = currentDir.contents.get(paths[i]);
				}
				
				if (nextItem instanceof VirDirectory)
				{
					currentDir = (VirDirectory)nextItem;
				}
				else
				{
					//There's an item there, and it's a file!
					//It'll just replace the file...
					currentDir.contents.put(paths[i], new VirDirectory(paths[i]));
					currentDir = (VirDirectory)currentDir.contents.get(paths[i]);
				}
			}	
			//Add the new object
			currentDir.contents.put(paths[paths.length - 1], item);
		}
		else
		{
			contents.put(path, item);
		}
	}
	
	public VirDirectory getParentDirectory()
	{
		return parent;
	}
	
	public char getDividerCharacter()
	{
		return this.divider;
	}
	
	public FDBuffer[] getPath()
	{
		LinkedList<FDBuffer> ancestors = new LinkedList<FDBuffer>();
		
		//Traverse ancestor tree
		ancestors.add(this);
		FDBuffer p = this.parent;
		while (p != null)
		{
			ancestors.add(p);
			p = p.getParentDirectory();
		}
		
		int len = ancestors.size();
		FDBuffer[] arr = new FDBuffer[len];
		
		for(int i = 0; i < len; i++)
		{
			arr[i] = ancestors.pollLast();
		}
		
		return arr;
	}
	
	public String getPathString()
	{
		//Get ancestors
		FDBuffer[] ancestors = getPath();
		//Get separator char
		char sep = '/';
		if (ancestors == null || ancestors.length < 1) return this.getFullName();
		FDBuffer rawroot = ancestors[0];
		VirDirectory root = null;
		if (rawroot instanceof VirDirectory) root = (VirDirectory)rawroot;
		else return this.getFullName();
		sep = root.getDividerCharacter();
		
		//String the string
		StringBuilder sb = new StringBuilder(512);
		for(int i = 1; i < ancestors.length; i++)
		{
			FDBuffer a = ancestors[i];
			sb.append(sep);
			sb.append(a.getFullName());
		}
		
		return sb.toString();
	}

	public List<VirFile> getAllFilesRecursive()
	{
		List<VirFile> all = new LinkedList<VirFile>();
		all.addAll(getFiles());
		List<VirDirectory> dirlist = getDirectories();
		for(VirDirectory vd : dirlist) all.addAll(vd.getAllFilesRecursive());
		return all;
	}
	
	public List<VirDirectory> getAllDirectoriesRecursive()
	{
		List<VirDirectory> all = new LinkedList<VirDirectory>();
		List<VirDirectory> dirlist = getDirectories();
		for(VirDirectory vd : dirlist) all.addAll(vd.getAllDirectoriesRecursive());
		return all;
	}
	
	public List<FDBuffer> getAllItemsRecursive()
	{
		List<FDBuffer> all = new LinkedList<FDBuffer>();
		all.addAll(getAllItems());
		List<VirDirectory> dirlist = getDirectories();
		for(VirDirectory vd : dirlist) all.addAll(vd.getAllItems());
		return all;
	}
}
