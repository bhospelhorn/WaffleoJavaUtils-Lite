package waffleoRai_Image;

//import java.awt.Image;
import java.awt.image.BufferedImage;

public interface Picture {

	public int getWidth();
	public int getHeight();
	
	public Pixel getPixelAt(int r, int l);
	public void setPixelAt(int r, int l, int color);
	
	public Picture scale(double factor);
	public BufferedImage toImage();
	
	public void printMe();
	
}
