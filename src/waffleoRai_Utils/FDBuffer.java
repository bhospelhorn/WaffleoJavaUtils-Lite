package waffleoRai_Utils;

import javax.swing.tree.TreeNode;

/**
 * Interface to denote either a virtual directory (VirDirectory) or virtual file (VirFile)
 * @author Blythe Hospelhorn
 * @version 1.0.2
 * @since November 1, 2017
 *
 */
public interface FDBuffer extends Comparable<FDBuffer>, TreeNode{

	public static enum FDBufferType
	{
		FILE,
		DIR;
	}
	
	public FDBufferType getType();
	public String getName();
	public String getFullName();
	public long getSizeOnDisk();
	public void setParentDirectory(VirDirectory p);
	public void printTree(int tabs);
	public VirDirectory getParentDirectory();
	public FDBuffer[] getPath();
	public String getPathString();
}
