package waffleoRai_Containers;

import java.io.IOException;

import javax.swing.tree.TreeModel;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.VirDirectory;

/*
 * UPDATES
 * 
 * 2017.11.05 | 1.0.0 -> 1.1.0
 * 	Added a "get raw sector" method.
 */

/**
 * Interface to represent a parsed CD image. Implementing classes should contain a virtual directory tree 
 * of extracted files and a table containing information on how the files were stored on the image,
 * at minimum.
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since November 5, 2017
 */
public interface CDImage {

	/**
	 * Retrieve the file at the given path, if it exists.
	 * @param path Path within the image file system to file of interest.
	 * @return FileBuffer object containing the file data.
	 * <br> null if sector could not be found or data could not be retrieved.
	 */
	public FileBuffer getFile(String path);
	
	/**
	 * Get the data from only the specified sector.
	 * @param sector Relative index of desired sector.
	 * @return FileBuffer containing raw data from specified sector.
	 * <br> null if sector could not be found or data could not be retrieved.
	 * @throws IOException If there is an error reading or writing to disk in the process
	 * of data isolation.
	 */
	public FileBuffer getSectorData(int sector) throws IOException;

	/**
	 * Get a regenerated version of the requested sector, including any header,
	 * data, and error checking.
	 * There is a possibility that the error checking region is set to all zero.
	 * @param relativeSector Relative index of desired full sector.
	 * @return FileBuffer containing data mimicking the original full disk sector.
	 */
	public FileBuffer getRawSector(int relativeSector) throws IOException;
	
	/**
	 * Get the raw directory tree.
	 * @return VirDirectory containing all of the files from the image and mirroring the
	 * file structure of the original image.
	 */
	public VirDirectory getRootDirectory();
	
	/**
	 * Get whether a file at the given path exists.
	 * @param path Path in the image file system to the file of interest.
	 * @return True - If the file exists and could be found.
	 * <br>False - If the file could not be found.
	 */
	public boolean fileExists(String path);
	
	/**
	 * Generate a TreeModel of the internal image directory tree.
	 * <br>For use with swing - designed for quick GUI viewing of image file structure.
	 * @return TreeModel mimicking image file structure and internal virtual directory - ready to
	 * set in a JTree.
	 */
	public TreeModel getDirectoryTree();
	
}
