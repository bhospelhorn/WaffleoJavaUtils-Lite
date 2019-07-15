package waffleoRai_Image.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import waffleoRai_Image.EightBitPalette;
import waffleoRai_Image.FourBitPalette;
import waffleoRai_Image.ImageFile;
import waffleoRai_Image.ImageType;
import waffleoRai_Image.Palette;
import waffleoRai_Image.Picture;
import waffleoRai_Image.Pixel;
import waffleoRai_Image.Pixel_RGBA;
import waffleoRai_Utils.BitStreamer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.StreamBuffer;

public class PcxFile implements ImageFile{

	private static final ImageType type = ImageType.PCX;
	private static final byte magic = 10;
	
	private int bitsPerPx;
	private int planes;
	private int height;
	private int width;
	
	private Palette plt;
	private static PCX_RLE decompressor = new PCX_RLE();
	private List<Plane> data;
	
	/*Information*/
	private byte version;
	private byte encoding;
	private int HRes;
	private int VRes;
	private int byPerScanline;
	private int paletteInfo;
	private int HScreenSz;
	private int VScreenSz;
	
	
	private class Plane
	{
		private int rows;
		private int cols;
		
		private byte[][] values;
		
		public Plane(int height, int width)
		{
			this.rows = height;
			this.cols = width;
			this.values = new byte[this.rows][this.cols];
		}
		
		public byte getValue(int r, int l)
		{
			if (r < 0 || r >= this.rows) throw new ArrayIndexOutOfBoundsException();
			if (l < 0 || l >= this.cols) throw new ArrayIndexOutOfBoundsException();
			return this.values[r][l];
		}
	
		public void setValue(int r, int l, byte b)
		{
			if (r < 0 || r >= this.rows) throw new ArrayIndexOutOfBoundsException();
			if (l < 0 || l >= this.cols) throw new ArrayIndexOutOfBoundsException();
			this.values[r][l] = b;
		}
	}
	
	/*** Constructors  
	 * @throws IOException ***/
	
	public PcxFile(String fPath) throws UnsupportedFileTypeException, IOException
	{
		FileBuffer in = new FileBuffer(fPath, false);
		if (in.isEmpty()) throw new FileBuffer.UnsupportedFileTypeException();
		this.parse(in);
	}
	
	public PcxFile(String fPath, int stPos) throws UnsupportedFileTypeException, IOException
	{
		FileBuffer in = new FileBuffer(fPath, stPos, false);
		if (in.isEmpty()) throw new FileBuffer.UnsupportedFileTypeException();
		this.parse(in);
	}
	
	public PcxFile(String fPath, int stPos, int edPos) throws UnsupportedFileTypeException, IOException
	{
		FileBuffer in = new FileBuffer(fPath, stPos, edPos, false);
		if (in.isEmpty()) throw new FileBuffer.UnsupportedFileTypeException();
		this.parse(in);
	}
	
	public PcxFile(int width, int height, int channels)
	{
		this.width = width;
		this.height = height;
		this.planes = channels;
		this.constructPlanes();
		this.plt = null;
		
		this.bitsPerPx = 8;
		this.version = 5;
		this.encoding = 1;
		this.HRes = 300;
		this.VRes = 300;
		this.byPerScanline = this.width;
		if (this.width % 2 == 1) this.byPerScanline++;
		this.paletteInfo = 1;
		this.HScreenSz = 640;
		this.VScreenSz = 480;
		
	}
	
	private void constructPlanes()
	{
		this.data = new ArrayList<Plane>(this.planes);
		
		for(int i = 0; i < this.planes; i++)
		{
			this.data.add(new Plane(this.height, this.width));
		}
	}
	
	/*** Readers ***/
	
	public void parse(FileBuffer myFile)throws UnsupportedFileTypeException, IOException
	{
		this.parse(myFile, 0, myFile.getFileSize());
	}
	
	public void parse(FileBuffer myFile, long stPos) throws UnsupportedFileTypeException, IOException
	{
		this.parse(myFile, 0, myFile.getFileSize());
	}
	
	public void parse(FileBuffer myFile, long stPos, long edPos) throws UnsupportedFileTypeException, IOException
	{
		long cPos = stPos;
		
		/*Header*/
		byte m = myFile.getByte(cPos);
		cPos++;
		if (m != magic) throw new UnsupportedFileTypeException();
		
		this.version = myFile.getByte(cPos);
		cPos++;
		this.encoding = myFile.getByte(cPos);
		cPos++;
		this.bitsPerPx = Byte.toUnsignedInt(myFile.getByte(cPos));
		cPos++;
		
		short xmin = 0;
		short xmax = 0;
		short ymin = 0;
		short ymax = 0;
		xmin = myFile.shortFromFile(cPos);
		cPos += 2;
		ymin = myFile.shortFromFile(cPos);
		cPos += 2;
		xmax = myFile.shortFromFile(cPos);
		cPos += 2;
		ymax = myFile.shortFromFile(cPos);
		cPos += 2;
		
		this.height = ymax - ymin;
		this.width = xmax - xmin;
		
		this.HRes = Short.toUnsignedInt(myFile.shortFromFile(cPos));
		cPos += 2;
		this.VRes = Short.toUnsignedInt(myFile.shortFromFile(cPos));
		cPos += 2;
		
		cPos += 48;
		cPos++;
		this.planes = myFile.getByte(cPos);
		cPos++;
		
		if (this.bitsPerPx == 4 && this.planes == 1)
		{
			this.plt = new FourBitPalette();
			cPos -= 50;
			for (int i = 0; i < 16; i++)
			{
				int r = Byte.toUnsignedInt(myFile.getByte(cPos));
				cPos++;
				int g = Byte.toUnsignedInt(myFile.getByte(cPos));
				cPos++;
				int b = Byte.toUnsignedInt(myFile.getByte(cPos));
				cPos++;
				Pixel p = new Pixel_RGBA(r, g, b, 255);
				this.plt.setPixel(p, i);
			}
			cPos += 2;
		}
		
		this.byPerScanline = Short.toUnsignedInt(myFile.shortFromFile(cPos));
		cPos += 2;
		this.paletteInfo = Short.toUnsignedInt(myFile.shortFromFile(cPos));
		cPos += 2;
		this.HScreenSz = Short.toUnsignedInt(myFile.shortFromFile(cPos));
		cPos += 2;
		this.VScreenSz = Short.toUnsignedInt(myFile.shortFromFile(cPos));
		cPos += 2;
		
		cPos += 54;
		
		/*Data*/
		int totalBytes = this.planes * this.byPerScanline * this.height;
		boolean hasEndPal = false;
		if (edPos - cPos >= totalBytes + 769)
		{
			byte pMark = myFile.getByte(edPos - 769);
			if (pMark == 12) hasEndPal = true;
		}
		
		long datEnd = edPos;
		if (hasEndPal) datEnd -= 769;
		FileBuffer decData = decompressor.RLE_Decode(myFile, cPos, datEnd, totalBytes);
		BitStreamer datStr = new BitStreamer(decData, true);
		this.constructPlanes();
		
		int line = 0;
		int plane = 0;
		int slPos = 0;
		while (datStr.canMoveForward(this.bitsPerPx))
		{
			byte b = datStr.readToByte(this.bitsPerPx);
			/*Note: not sure if scanline includes all planes or is per plane*/
			if (slPos >= this.byPerScanline)
			{
				plane++;
				slPos = 0;
				if (plane >= this.planes)
				{
					line++;
					plane = 0;
				}
			}
			if (line >= 0 && line < this.height)
			{
				if (slPos >= 0 && slPos < this.width)
				{
					this.data.get(plane).setValue(line, slPos, b);
				}
			}
			
		}
		
		/*End Palette*/
		if (hasEndPal)
		{
			cPos = edPos - 768;
			this.plt = new EightBitPalette();
			for (int i = 0; i < 256; i++)
			{
				int d = Byte.toUnsignedInt(myFile.getByte(cPos));
				cPos++;
				int g = Byte.toUnsignedInt(myFile.getByte(cPos));
				cPos++;
				int b = Byte.toUnsignedInt(myFile.getByte(cPos));
				cPos++;
				Pixel p = new Pixel_RGBA(d, g, b, 255);
				this.plt.setPixel(p, i);
			}
		}
		
		
	}
	
	/*** Writers ***/
	
	private FileBuffer serializeHeader()
	{
		FileBuffer header = new FileBuffer(128, false);
		
		header.addToFile(magic);
		header.addToFile((byte)this.version);
		header.addToFile((byte)this.encoding);
		header.addToFile((byte)this.bitsPerPx);
		
		header.addToFile((short)0);
		header.addToFile((short)0);
		header.addToFile((short)this.width);
		header.addToFile((short)this.height);
		header.addToFile((short)this.HRes);
		header.addToFile((short)this.VRes);
		
		if (this.plt != null)
		{
			if (this.plt.getBitDepth() == 4)
			{
				for (int i = 0; i < 16; i++)
				{
					header.addToFile((byte)this.plt.getRed(i));
					header.addToFile((byte)this.plt.getGreen(i));
					header.addToFile((byte)this.plt.getBlue(i));
				}
			}
			else for (int i = 0; i < 48; i++) header.addToFile((byte)0);
				
		}
		else for (int i = 0; i < 48; i++) header.addToFile((byte)0);
		
		header.addToFile((byte)0);
		header.addToFile((byte)this.planes);
		header.addToFile((short)this.byPerScanline);
		header.addToFile((short)this.paletteInfo);
		header.addToFile((short)this.HScreenSz);
		header.addToFile((short)this.VScreenSz);
		
		for (int i = 0; i < 54; i++) header.addToFile((byte)0);
		
		return header;
	}
	
	private FileBuffer serializeData() throws IOException
	{
		FileBuffer raw = new FileBuffer(this.planes * this.byPerScanline * this.height, false);
		
		for (int r = 0; r < this.height; r++)
		{
			for (int p = 0; p < this.planes; p++)
			{
				for (int l = 0; l < this.byPerScanline; l++)
				{
					if (l < this.width)
					{
						byte b = this.data.get(p).getValue(r, l);
						raw.addToFile(b);
					}
					else raw.addToFile((byte)0);
				}
			}
		}
		
		if (this.encoding == 1)
		{
			FileBuffer comp = decompressor.RLE_Encode(raw, 0, raw.getFileSize());
			return comp;
		}
		
		return raw;
	}
	
	private FileBuffer serializeEndPalette()
	{
		if (this.plt == null) return null;
		if (this.plt.getBitDepth() != 8) return null;
		FileBuffer ep = new FileBuffer(769, false);
		
		ep.addToFile((byte)12);
		for (int i = 0; i < 256; i++)
		{
			ep.addToFile((byte)this.plt.getRed(i));
			ep.addToFile((byte)this.plt.getGreen(i));
			ep.addToFile((byte)this.plt.getBlue(i));
		}
		
		return ep;
	}
	
	public FileBuffer serializeMe() throws IOException
	{
		FileBuffer header = this.serializeHeader();
		FileBuffer data = this.serializeData();
		FileBuffer pal = this.serializeEndPalette();
		long pSize = 128 + data.getFileSize();
		if (pal != null) pSize += 769;
		FileBuffer pcx = null;
		if (pSize < FileBuffer.getCurrentMemoryThreshold()) pcx = new FileBuffer((int)pSize, false);
		else pcx = new StreamBuffer(FileBuffer.generateTemporaryPath("pcxout"), false);
		
		pcx.addToFile(header);
		pcx.addToFile(data);
		if (pal != null) pcx.addToFile(pal);
		
		return pcx;
	}
	
	/*** Getters ***/
	
	public ImageType getType() 
	{
		return type;
	}

	public int getBitsPerPixel() 
	{
		return this.bitsPerPx;
	}

	public int getNumberChannels() 
	{
		return this.planes;
	}

	public int getHeight() 
	{
		return this.height;
	}

	public int getWidth() 
	{
		return this.width;
	}

	public int getVersion()
	{
		return this.version;
	}
	
	public int getHResolution()
	{
		return this.HRes;
	}
	
	public int getVResolution()
	{
		return this.VRes;
	}

	public boolean isColor()
	{
		if (this.paletteInfo == 1) return true;
		return false;
	}
	
	 /*** Setters ***/
	
	
	 /*** Conversion ***/
	
	public Picture convertToPicture() 
	{
		return null;
	}

	
}
