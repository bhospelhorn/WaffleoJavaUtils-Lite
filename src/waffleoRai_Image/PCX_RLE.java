package waffleoRai_Image;

import waffleoRai_Compression.RLEncoder;
import waffleoRai_Utils.BitStreamer;
import waffleoRai_Utils.FileBuffer;

public class PCX_RLE extends RLEncoder{

	public PCX_RLE()
	{
		super(2, (short)0x11, 6, 8);
	}
	
	public int minimumRL()
	{
		return 1;
	}
	
	public FileBuffer RLE_Encode(FileBuffer in, int stPos, int edPos)
	{
		int initCap = edPos - stPos;
		FileBuffer out = new FileBuffer(initCap);
		BitStreamer inStream = new BitStreamer(in, stPos, true);
		BitStreamer outStream = new BitStreamer(out, false);
		
		int counter = 0;
		byte rVal = 0;
		
		while (inStream.canMoveForward(8))
		{
			byte cVal = inStream.readToByte(8);
			if (cVal == rVal && counter < this.maximumRL()) counter++;
			else
			{
				/*Write rVal however needed, to output file*/
				if (counter > 0)
				{
					if (counter >= 2 || Byte.toUnsignedInt(rVal) >= 192)
					{
						/*RL shortening - write marker/ run len byte*/
						outStream.writeBits(0x11, 2);
						outStream.writeBits(counter, 6);
					}
					/*Write byte*/
					outStream.writeBits(rVal, 8);
					counter = 0;
				}
				/*Set cVal as new counter reference*/
				rVal = cVal;
				counter = 1;
			}
		}
		return out;
	}
}
