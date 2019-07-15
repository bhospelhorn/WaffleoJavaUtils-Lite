package waffleoRai_Image;

public interface Palette {

	public int getRGBA(int index);
	public int getRed(int index);
	public int getGreen(int index);
	public int getBlue(int index);
	
	public Pixel getPixel(int index);
	public void setPixel(Pixel p, int index);
	public int getBitDepth();
	
	public int getClosestValue(int RGBA);
	
	public void printMe();
	
}
