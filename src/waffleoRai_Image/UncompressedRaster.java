package waffleoRai_Image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class UncompressedRaster implements Picture{

	private int width;
	private int height;
	
	private Pixel[][] pixelArray;
	
	public UncompressedRaster(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.pixelArray = new Pixel[height][width];
	}
	
	public UncompressedRaster(BufferedImage source)
	{
		readFromBufferedImage(source);
	}
	
	public UncompressedRaster(Image source)
	{
		//From stackoverflow (https://stackoverflow.com/questions/9417356/bufferedimage-resize)
		BufferedImage ref = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics2D g2 = ref.createGraphics();
		g2.drawImage(source, 0, 0, null);
		g2.dispose();
		
		readFromBufferedImage(ref);
	}
	
	private void readFromBufferedImage(BufferedImage source)
	{
		width = source.getWidth();
		height = source.getHeight();
		pixelArray = new Pixel[height][width];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int RGBA = source.getRGB(x, y);
				this.setPixelAt(y, x, RGBA);
			}
		}
	}
	
	public int getWidth()
	{
		return this.width;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public Pixel getPixelAt(int r, int l)
	{
		if (r < 0 || r >= this.height) return null;
		if (l < 0 || l >= this.width) return null;
		return this.pixelArray[r][l];
	}
	
	public void setPixelAt(int r, int l, Pixel p)
	{
		if (r < 0 || r >= this.height) return;
		if (l < 0 || l >= this.width) return;
		this.pixelArray[r][l] = p;
	}
	
	public void setPixelAt(int r, int l, int color)
	{
		if (r < 0 || r >= this.height) return;
		if (l < 0 || l >= this.width) return;
		this.pixelArray[r][l] = new Pixel_RGBA(color);
	}
	
	public Picture scale(double factor)
	{
		int scaledWidth = (int)Math.round((double)this.width * factor);
		int scaledHeight = (int)Math.round((double)this.height * factor);
		if (scaledWidth < 1) return null;
		if (scaledHeight < 1) return null;
		UncompressedRaster pic = null;
		Image temp = this.toImage();
		Image scaled = temp.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
		pic = new UncompressedRaster(scaled);
		return pic;
	}
	
	public BufferedImage toImage()
	{
		BufferedImage myImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				myImage.setRGB(x, y, this.getPixelAt(y, x).getRGBA());
			}
		}
		return myImage;
	}
	
	public void printMe()
	{
		System.out.println("Picture - UncompressedRaster");
		System.out.println("Size: " + this.getWidth() + "x" + this.getHeight());
		System.out.println("RGBA Representation:");
		for (int i = 0; i < getWidth(); i++)
		{
			for (int j = 0; j < getHeight(); j++)
			{
				Pixel p = this.getPixelAt(j, i);
				int RGBA = p.getRGBA();
				System.out.print(String.format("%08x ", RGBA));
			}
			System.out.println();
		}
	}
	
}
