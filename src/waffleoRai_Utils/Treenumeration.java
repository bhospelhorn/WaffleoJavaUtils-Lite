package waffleoRai_Utils;

import java.util.Collection;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

/**
 * An Enumeration of the TreeNode type. For use with displaying a JTree when 
 * creating a GUI with swing. This particular class is utilized for VirDirectory
 * viewing.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 2017
 */
public class Treenumeration implements Enumeration<TreeNode>{

	private TreeNode[] myNodes;
	private int position;
	
	public Treenumeration(Collection<TreeNode> nodes)
	{
		if (nodes == null)
		{
			 this.myNodes = null;
			 return;
		}
		myNodes = new TreeNode[nodes.size()];
		int i = 0;
		for (TreeNode n : nodes)
		{
			myNodes[i] = n;
			i++;
		}
		this.position = 0;
	}
	
	public Treenumeration(TreeNode[] nodes)
	{
		myNodes = nodes;
		this.position = 0;
	}
	
	@Override
	public boolean hasMoreElements() 
	{
		if (myNodes == null) return false;
		if (this.position < myNodes.length) return true;
		return false;
	}

	@Override
	public TreeNode nextElement() 
	{
		if (myNodes == null) return null;
		if (position >= myNodes.length) return null;
		TreeNode n = myNodes[position];
		position++;
		// TODO Auto-generated method stub
		return n;
	}

}
