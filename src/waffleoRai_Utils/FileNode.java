package waffleoRai_Utils;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

public class FileNode implements TreeNode, Comparable<FileNode>{

	/* --- Instance Variables --- */
	
	protected DirectoryNode parent;
	
	private String sourcePath;
	
	private String fileName;
	private long offset;
	private long length;
	
	/* --- Construction --- */
	
	public FileNode(DirectoryNode parent, String name)
	{
		this.parent = parent;
		fileName = name;
		offset = -1;
		length = 0;
		if(parent != null) parent.addChild(this);
	}
	
	/* --- Getters --- */
	
	public String getFileName(){return fileName;}
	public long getOffset(){return offset;}
	public long getLength(){return length;}
	public DirectoryNode getParent(){return parent;}
	public String getSourcePath(){return sourcePath;}
	
	/* --- Setters --- */
	
	public void setFileName(String name){fileName = name;}
	public void setOffset(long off){offset = off;}
	public void setLength(long len){length = len;}
	public void setParent(DirectoryNode p){parent = p; if(p != null) p.addChild(this);}
	public void setSourcePath(String path){sourcePath = path;}
	
	/* --- Comparable --- */
	
	public boolean isDirectory()
	{
		return false;
	}
	
	public boolean equals(Object o)
	{
		if(o == this) return true;
		if(o == null) return false;
		if(!(o instanceof FileNode)) return false;
		FileNode fn = (FileNode)o;
		if(this.isDirectory() != fn.isDirectory()) return false;
		return fileName.equals(fn.fileName);
	}
	
	public int hashCode()
	{
		return fileName.hashCode() ^ (int)offset;
	}
	
	public int compareTo(FileNode other)
	{
		if(other == this) return 0;
		if(other == null) return 1;
		
		if(this.isDirectory() && !other.isDirectory()) return -1;
		if(!this.isDirectory() && other.isDirectory()) return 1;
		
		return this.fileName.compareTo(other.fileName);
	}
	
	/* --- TreeNode --- */
	
	@Override
	public TreeNode getChildAt(int childIndex) {return null;}

	@Override
	public int getChildCount() {return 0;}

	@Override
	public int getIndex(TreeNode node) {return -1;}

	@Override
	public boolean getAllowsChildren() {return false;}

	@Override
	public boolean isLeaf() {return true;}

	@Override
	public Enumeration<TreeNode> children() 
	{
		TreeNode[] n = null;
		return new Treenumeration(n);
	}
	
}
