package waffleoRai_Utils;

import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.tree.TreeNode;

/**
 * Wrapper for a FileBuffer - represents a file in memory as a component of a
 * VirDirectory (virtual directory).
 * @author Blythe Hospelhorn
 * @version 1.3.0
 * @since February 17, 2019
 *
 */
public class VirFile implements FDBuffer{

	private FileBuffer file;
	private String name;
		
	private ParserType parser;
		
	private VirDirectory parent;
		
	public VirFile(FileBuffer f, String fileName)
	{
		this.file = f;
		this.name = fileName;
		parser = null;
	}
		
	public FileBuffer getFile()
	{
		return this.file;
	}
		
	public FDBufferType getType()
	{
		return FDBufferType.FILE;
	}
		
	public String getName()
	{
		return name;
	}
	
	public String getFullName()
	{
		if (parser != null) return this.name + "." + parser.getExtension();
		return this.name;
	}
		
	public long getSizeOnDisk()
	{
		return this.file.getFileSize();
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
		else if (o instanceof VirFile)
		{
			VirFile f = (VirFile)o;
			if (!f.getName().equals(this.name)) return false;
			if (!f.getFile().equals(this.file)) return false;
			return true;
		}
		return false;
	}

	public int compareTo(FDBuffer o) 
	{
		if (o == null) return -1;
		if (o instanceof VirDirectory) return 1;
		return this.name.compareTo(o.getFullName());
	}

	public TreeNode getChildAt(int childIndex) 
	{
		//A file has no children
		return null;
	}

	public int getChildCount() 
	{
		//A file has no children
		return 0;
	}

	public void setParentDirectory(VirDirectory p)
	{
		this.parent = p;
	}
		
	public TreeNode getParent() 
	{
		return this.parent;
	}

	public int getIndex(TreeNode node) 
	{
		return -1;
	}

	public boolean getAllowsChildren() 
	{
		return false;
	}

	public boolean isLeaf() 
	{
		return true;
	}

	public Enumeration<TreeNode> children() 
	{
		TreeNode[] n = null;
		return new Treenumeration(n);
	}
		
	public String toString()
	{
		return this.getFullName();
	}

	public void printTree(int tabs)
	{
		for (int i = 0; i < tabs; i++) System.out.print("\t");
		System.out.print(this.name);
		System.out.println();
	}

	public ParserType getParserType()
	{
		return parser;
	}
		
	public void setParserType(ParserType pt)
	{
		//Strip any extension
		int dot = name.lastIndexOf('.');
		if (dot >= 0) setName(name.substring(0, dot));
			
		//if (pt == null) name += ".bin";
			
		parser = pt;
	}

	public void setName(String s)
	{
		String oldname = this.getName();
		name = s;
		if (parent != null) parent.moveItem(oldname, name);
	}
		
	public VirDirectory getParentDirectory()
	{
		return parent;
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
	
}
