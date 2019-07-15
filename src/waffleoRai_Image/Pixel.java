package waffleoRai_Image;

public interface Pixel {

	public int getRed();
	public int getGreen();
	public int getBlue();
	public int getAlpha();
	
	public void setRed(int r);
	public void setGreen(int g);
	public void setBlue(int b);
	public void setAlpha(int a);
	
	public int getRGBA();
	public int getColorDistance(Pixel p);
	public int getColorDistance(int RGBA);
	
	public Pixel add(Pixel p);
	public Pixel subtract(Pixel p);
	public Pixel multiply(int i);
	public Pixel multiply(double f);
	public Pixel divide(int i);
	public Pixel divide(double f);
	
	public Pixel copy();
	
	public void saturate8();
	
}
