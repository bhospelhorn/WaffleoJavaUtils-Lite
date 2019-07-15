package waffleoRai_Image;

public class Pixel_RGBA implements Pixel{
	
	private int red;
	private int blue;
	private int green;
	private int alpha;
	
	public Pixel_RGBA(int r, int b, int g, int a)
	{
		this.red = r;
		this.blue = b;
		this.green = g;
		this.alpha = a;
	}
	
	public Pixel_RGBA(int RGBA)
	{
		int r = ((RGBA & 0xFF000000) >> 24) & 0xFF;
		int g = ((RGBA & 0x00FF0000) >> 16) & 0xFF;
		int b = ((RGBA & 0x0000FF00) >> 8) & 0xFF;
		int a = RGBA & 0xFF;
		
		this.red = r;
		this.blue = b;
		this.green = g;
		this.alpha = a;
	}
	
	public int getRed()
	{
		return this.red;
	}
	
	public int getGreen()
	{
		return this.green;
	}
	
	public int getBlue()
	{
		return this.blue;
	}
	
	public int getAlpha()
	{
		return this.alpha;
	}
	
	public int getRGBA()
	{
		int RGBA = 0;
		int r = red << 24;
		int g = green << 16;
		int b = blue << 8;
		int a = alpha;
		RGBA |= r | g | b | a;
		
		return RGBA;
	}
	
	public void setRed(int r)
	{
		this.red = r;
	}
	
	public void setGreen(int g)
	{
		this.green = g;
	}
	
	public void setBlue(int b)
	{
		this.blue = b;
	}
	
	public void setAlpha(int a)
	{
		this.alpha = a;
	}
	
	public int getColorDistance(Pixel p)
	{
		return getColorDistance(p.getRGBA());
	}
	
	public int getColorDistance(int RGBA)
	{
		int oRed = (RGBA >> 24) & 0x7F;
		int oGreen = (RGBA >> 16) & 0x7F;
		int oBlue = (RGBA >> 8) & 0x7F;
		int oAlpha = RGBA & 0x7F;
		
		int rDist = Math.abs(red - oRed);
		int gDist = Math.abs(green - oGreen);
		int bDist = Math.abs(blue - oBlue);
		int aDist = Math.abs(alpha - oAlpha);
		
		int totalDist = rDist + gDist + bDist + aDist;
		
		return totalDist;
	}

	public Pixel add(Pixel p)
	{
		int r = p.getRed() + red;
		int g = p.getGreen() + green;
		int b = p.getBlue() + blue;
		int a = p.getAlpha() + alpha;
		return new Pixel_RGBA(r, b, g, a);
	}
	
	public Pixel subtract(Pixel p)
	{
		int r = red - p.getRed();
		int g = green - p.getGreen();
		int b = blue - p.getBlue();
		int a = alpha - p.getAlpha();
		return new Pixel_RGBA(r, b, g, a);
	}
	
	public Pixel multiply(int i)
	{
		int r = red * i;
		int g = green * i;
		int b = blue * i;
		int a = alpha * i;
		return new Pixel_RGBA(r, b, g, a);
	}
	
	public Pixel multiply(double f)
	{
		int r = (int)Math.round((double)red * f);
		int g = (int)Math.round((double)green * f);
		int b = (int)Math.round((double)blue * f);
		int a = (int)Math.round((double)alpha * f);
		return new Pixel_RGBA(r, b, g, a);
	}
	
	public Pixel divide(int i)
	{
		int r = red / i;
		int g = green / i;
		int b = blue / i;
		int a = alpha / i;
		return new Pixel_RGBA(r, b, g, a);
	}
	
	public Pixel divide(double f)
	{
		int r = (int)Math.round((double)red / f);
		int g = (int)Math.round((double)green / f);
		int b = (int)Math.round((double)blue / f);
		int a = (int)Math.round((double)alpha / f);
		return new Pixel_RGBA(r, b, g, a);
	}
	
	public void saturate8()
	{
		if (red < 0) red = 0;
		if (red > 255) red = 255;
		if (green < 0) green = 0;
		if (green > 255) green = 255;
		if (blue < 0) blue = 0;
		if (blue > 255) blue = 255;
		if (alpha < 0) alpha = 0;
		if (alpha > 255) alpha = 255;
	}
	
	public Pixel copy()
	{
		return new Pixel_RGBA(red, blue, green, alpha);
	}
	
}
