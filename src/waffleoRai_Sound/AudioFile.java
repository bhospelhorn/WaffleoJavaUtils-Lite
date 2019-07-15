package waffleoRai_Sound;

import waffleoRai_Utils.*;

public interface AudioFile {
	
	public AudioFile open(String inPath);
	public AudioFile parse(FileBuffer myFile, int stPos);
	public Sound readFile(FileBuffer myFile, int stPos); /*Uses default settings*/
	
	public Sound toSound();
	public Sound toSound(int bDepth, boolean memCompress, int channels);
	
	public AudioFile toFormat(Sound mySnd);
	public void writetoFormat(Sound mySnd, String outPath);
	
	public FileBuffer serialize();
	public void writeFile(String outPath);
	
	public boolean isCompressed();
	public String getFileType();
	public boolean isType(FileBuffer myFile, int stPos);

}

