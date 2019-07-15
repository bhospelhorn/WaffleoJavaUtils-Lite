package waffleoRai_Image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class PaletteRaster implements Picture{
	
	private int width;
	private int height;
	private Palette palette;
	
	private int[][] pixelArray;
	
	public PaletteRaster(int width, int height, Palette plt)
	{
		this.width = width;
		this.height = height;
		this.pixelArray = new int[this.width][this.height];
		this.palette = plt;
	}
	
	public PaletteRaster(BufferedImage source)
	{
		readFromBufferedImage(source);
	}
	
	public PaletteRaster(Image source)
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
		pixelArray = new int[height][width];
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int RGBA = source.getRGB(x, y);
				this.setPixelAt(y, x, palette.getClosestValue(RGBA));
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
		return this.palette.getPixel(this.pixelArray[r][l]);
	}
	
	public void setPixelAt(int r, int l, int color)
	{
		if (r < 0 || r >= this.height) return;
		if (l < 0 || l >= this.width) return;
		if (color < 0 || color >= 1 << this.palette.getBitDepth()) return;
		this.pixelArray[r][l] = color;
	}
	
	public int getColorAt(int r, int l)
	{
		if (r < 0 || r >= this.height) return -1;
		if (l < 0 || l >= this.width) return -1;
		return this.pixelArray[r][l];
	}
	
	public Picture scale(double factor)
	{
		int scaledWidth = (int)Math.round((double)this.width * factor);
		int scaledHeight = (int)Math.round((double)this.height * factor);
		if (scaledWidth < 1) return null;
		if (scaledHeight < 1) return null;
		PaletteRaster pic = null;
		BufferedImage temp = this.toImage();
		Image scaled = temp.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
		pic = new PaletteRaster(scaled);
		return pic;
	}

	public BufferedImage toImage()
	{
		BufferedImage myImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int color = 0;
				int red = this.getPixelAt(y, x).getRed() << 16;
				int green = this.getPixelAt(y, x).getGreen() << 0;
				int blue = this.getPixelAt(y, x).getBlue() << 8;
				int alpha = this.getPixelAt(y, x).getAlpha() << 24;
				color |= red | green | blue | alpha;
				myImage.setRGB(x, y, color);
			}
		}
		return myImage;
	}
	
	public void printMe()
	{
		System.out.println("Picture - PaletteRaster");
		System.out.println("Size: " + this.getWidth() + "x" + this.getHeight());
		System.out.println("\nPalette:");
		if (palette == null) System.out.println("null");
		else palette.printMe();
		System.out.println("\nPalette Representation:");
		for (int i = 0; i < getWidth(); i++)
		{
			for (int j = 0; j < getHeight(); j++)
			{
				int c = this.getColorAt(j, i);
				System.out.print(String.format("%02x ", c));
			}
			System.out.println();
		}
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
