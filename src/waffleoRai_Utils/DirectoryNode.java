package waffleoRai_Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreeNode;

public class DirectoryNode extends FileNode{

	/* --- Instance Variables --- */
	
	private Set<FileNode> children;
	private int endIndex;
	
	/* --- Construction --- */
	
	public DirectoryNode(DirectoryNode parent, String name)
	{
		super(parent, name);
		children = new HashSet<FileNode>();
		endIndex = -1;
	}
	
	/* --- Getters --- */
	
	public int getEndIndex(){return endIndex;}
	
	public List<FileNode> getChildren()
	{
		List<FileNode> list = new ArrayList<FileNode>(children.size() + 1);
		list.addAll(children);
		Collections.sort(list);
		return list;
	}
	
	public boolean isDirectory()
	{
		return true;
	}
	
	/* --- Setters --- */
	
	protected void addChild(FileNode node){children.add(node);}
	public void clearChildren(){children.clear();}
	public void setEndIndex(int i){endIndex = i;}
	
	/* --- TreeNode --- */
	
	@Override
	public TreeNode getChildAt(int childIndex) 
	{
		List<FileNode> childlist = this.getChildren();
		return childlist.get(childIndex);
	}

	@Override
	public int getChildCount() {return children.size();}

	@Override
	public int getIndex(TreeNode node) 
	{
		if(children.contains(node))
		{
			List<FileNode> clist = this.getChildren();
			int ccount = clist.size();
			for(int i = 0; i < ccount; i++)
			{
				if(clist.get(i).equals(node)) return i;
			}
		}
		return -1;
	}

	@Override
	public boolean getAllowsChildren() {return true;}

	@Override
	public boolean isLeaf() 
	{
		return (children.isEmpty());
	}

	@Override
	public Enumeration<TreeNode> children() 
	{
		List<TreeNode> list = new ArrayList<TreeNode>(children.size()+1);
		list.addAll(getChildren());
		return new Treenumeration(list);
	}

}
