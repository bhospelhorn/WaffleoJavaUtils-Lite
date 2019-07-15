package waffleoRai_Image;

public class MappedPixel extends Pixel_RGBA{

	private int x;
	private int y;
	
	public MappedPixel(int r, int g, int b, int a)
	{
		super(r, b, g, a);
		x = -1;
		y = -1;
	}
	
	public MappedPixel(int RGBA)
	{
		super(RGBA);
		x = -1;
		y = -1;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getRow()
	{
		return y;
	}
	
	public int getColumn()
	{
		return x;
	}
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
	
	public void setRow(int r)
	{
		this.y = r;
	}
	
	public void setColumn(int l)
	{
		this.x = l;
	}

	public Pixel copy()
	{
		MappedPixel cp = new MappedPixel(super.getRed(), super.getBlue(), super.getGreen(), super.getAlpha());
		cp.setX(x);
		cp.setY(y);
		return cp;
	}
	
}
