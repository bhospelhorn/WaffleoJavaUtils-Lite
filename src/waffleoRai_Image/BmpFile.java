package waffleoRai_Image;


public class BmpFile implements ImageFile {
	
	private static final ImageType type = ImageType.BITMAP;
	
	/** Main Header **/
	
	private static final String windowsMagic = "BM";
	private static final String OS2bmparray = "BA";
	private long fileSize;
	private int offsetToPixelArray;
	
	/** DIB Header **/
	
	/** Palette **/
	
	/** Pixel Array **/
	
	/** ICC Color Profile **/

	/*** Overrides (Move later) ***/
	
	public ImageType getType() {
		return type;
	}

	@Override
	public int getBitsPerPixel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberChannels() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Picture convertToPicture() {
		// TODO Auto-generated method stub
		return null;
	}

}
