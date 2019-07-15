package waffleoRai_Image;

public interface ImageFile {

	public ImageType getType();
	public int getBitsPerPixel();
	public int getNumberChannels();
	public int getHeight();
	public int getWidth();
	public Picture convertToPicture();
	
}
