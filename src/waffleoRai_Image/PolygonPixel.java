package waffleoRai_Image;

public class PolygonPixel {
	
	public MappedPixel pixel;

	private int xoff;
	private int yoff;
	
	public PolygonPixel(MappedPixel p, Line l)
	{
		pixel = p;
		xoff = l.getOffset_X();
		yoff = l.getOffset_Y();
	}
	
	public int getX()
	{
		return pixel.getX() + xoff;
	}
	
	public int getY()
	{
		return pixel.getY() + yoff;
	}

}
