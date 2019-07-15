package waffleoRai_Compression;

import java.io.IOException;

import waffleoRai_Utils.BitStreamer;
import waffleoRai_Utils.FileBuffer;

public class RLEncoder {

	private boolean hasMarker;
	private int markerBits;
	private short marker;
	
	private int runLengthBits;
	private int dataPieceBits;
	
	private boolean toggleOff;
	private short toggleRLsub;
	//private short toggleDataSub;
	
	private boolean rlSigned; /*Unsigned by default*/
	
	public RLEncoder(int rLBits, int dBits)
	{
		this.constructorCore(false, 0, (short)-1, rLBits, dBits, false);
	}
	
	public RLEncoder(int mBits, short marker, int rLBits, int dBits)
	{
		this.constructorCore(true, mBits, marker, rLBits, dBits, false);
	}
	
	public RLEncoder(int mBits, short marker, int rLBits, int dBits, boolean toggle)
	{
		this.constructorCore(true, mBits, marker, rLBits, dBits, toggle);
	}
	
	private void constructorCore(boolean hasM, int mBits, short mrk, int rlBits, int dBits, boolean toggle)
	{
		this.hasMarker = hasM;
		if (hasM)
		{
			if (mBits > 16) mBits = 16;
			if (mBits < 1) mBits = 1;
			this.markerBits = mBits;
			this.marker = mrk;
		}
		else
		{
			this.markerBits = 0;
			this.marker = -1;
		}
		
		if (rlBits > 16) rlBits = 16;
		else if (rlBits < 1) rlBits = 1;
		if (dBits > 16) dBits = 16;
		else if (dBits < 1) dBits = 1;
		
		this.runLengthBits = rlBits;
		this.dataPieceBits = dBits;
		
		this.toggleRLsub = 0;
		this.toggleOff = toggle;
		this.rlSigned = false;
	}
	
	/*** Getters ***/
	
	public boolean hasMarker()
	{
		return this.hasMarker;
	}
	
	public int numMarkerBits()
	{
		return this.markerBits;
	}
	
	public short getMarker()
	{
		return this.marker;
	}
	
	public int numRunLengthBits()
	{
		return this.runLengthBits;
	}
	
	public int numDataBits()
	{
		return this.dataPieceBits;
	}
	
	public boolean toggleSet()
	{
		return this.toggleOff;
	}
	
	public short getToggleRLSub()
	{
		return this.toggleRLsub;
	}
	
	public boolean runLengthSigned()
	{
		return this.rlSigned;
	}
	
	/*** Setters ***/
	
	public void setMarker(int mBits, short marker)
	{
		this.hasMarker = true;
		this.markerBits = mBits;
		this.marker = marker;
	}
	
	public void unsetMarker()
	{
		this.hasMarker = false;
		this.markerBits = 0;
		this.marker = -1;
	}
	
	public void setRunLengthBits(int rlBits)
	{
		this.runLengthBits = rlBits;
	}
	
	public void setDataBits(int dBits)
	{
		this.dataPieceBits = dBits;
	}
	
	public void setToggleMode(boolean toggle)
	{
		this.toggleOff = toggle;
	}
	
	public void setToggleRLSub(short symbol)
	{
		this.toggleRLsub = symbol;
	}
	
	public void setRunLengthSigned(boolean signed)
	{
		this.rlSigned = signed;
	}
	
	/*** Common ***/
	
	public int minimumRL()
	{
		if (toggleOff) return 3;
		return 2;
	}
	
	public int maximumRL()
	{
		int ex = 1 << this.runLengthBits;
		if (this.rlSigned) return (ex/2) - 1;
		return ex - 1;
	}
	
	/*** Encoding ***/
	
	/**
	 * 
	 * @param in
	 * @param stPos
	 * @param edPos
	 * @return
	 * @throws IOException
	 */
	public FileBuffer RLE_Encode(FileBuffer in, long stPos, long edPos) throws IOException
	{
		long reqSize = edPos - stPos;
		FileBuffer out = FileBuffer.createWritableBuffer("RLE_enc", reqSize, true);
		BitStreamer inStream = new BitStreamer(in, stPos, true);
		BitStreamer outStream = new BitStreamer(out, false);
		
		int counter = 0;
		boolean toggleOn = false;
		short rVal = 0;
		
		while (inStream.canMoveForward(this.dataPieceBits))
		{
			short cVal = inStream.readToShort(this.dataPieceBits);
			if (cVal == rVal && counter < this.maximumRL()) counter++;
			else
			{
				/*Write rVal however needed, to output file*/
				if (counter > 0)
				{
					if (counter < this.minimumRL())
					{
						/*Not long enough for RL shortening*/
						if (this.toggleOff && toggleOn)
						{
							/*RL Toggle off signal if applicable*/
							outStream.writeBits(this.marker, this.markerBits);
						}
						for (int i = 0; i < counter; i++)
						{
							/*Write data piece as many times as it occurred.*/
							outStream.writeBits(rVal, this.dataPieceBits);
						}
						toggleOn = false;
					}
					else
					{
						/*Long enough for RL shortening*/
						if (!(this.toggleOff && toggleOn))
						{
							/*Write marker first*/
							outStream.writeBits(this.marker, this.markerBits);
						}
						if (this.toggleOff && counter == this.marker) outStream.writeBits(this.toggleRLsub, this.runLengthBits);
						else outStream.writeBits(counter, this.runLengthBits);
						outStream.writeBits(rVal, this.dataPieceBits);
					}
					counter = 0;
				}
				/*Set cVal as new counter reference*/
				rVal = cVal;
				counter = 1;
			}
		}
		outStream.writeIncompleteTemp();
		return out;
	}
	
	/*** Decoding ***/
	
	/**
	 * 
	 * @param in
	 * @param stPos
	 * @param edPos
	 * @param initCap
	 * @return
	 * @throws IOException
	 */
	public FileBuffer RLE_Decode(FileBuffer in, long stPos, long edPos, long initCap) throws IOException
	{
		FileBuffer out = FileBuffer.createWritableBuffer("RLE_dec", initCap, true);
		
		BitStreamer inStream = new BitStreamer(in, stPos, true);
		BitStreamer outStream = new BitStreamer(out, false);
		
		boolean toggleOn = false;
		
		while(inStream.canMoveForward(this.dataPieceBits))
		{
			/*Check for marker*/
			short mCheck = inStream.readToShort(this.markerBits);
			boolean markerFound = mCheck == this.marker;
			boolean readAsRL = false;
			
			if (markerFound)
			{
				if (!this.toggleOff || (this.toggleOff && toggleOn == false))
				{
					/*Marker was on-switch*/
					readAsRL = true;
				}
				else if (this.toggleOff && toggleOn == true)
				{
					/*Marker was off-switch*/
					readAsRL = false;
				}
			}
			else
			{
				inStream.rewind(this.markerBits);
				if (!this.toggleOff || (this.toggleOff && toggleOn == false))
				{
					/*Next read is pure data*/
					readAsRL = false;
				}
				else if (this.toggleOff && toggleOn == true)
				{
					/*Next read is a unmarked run data*/
					readAsRL = true;
				}
			}
			
			short count = 1;
			if (readAsRL)
			{
				count = inStream.readToShort(this.runLengthBits);
				if (this.toggleOff && count == this.toggleRLsub) count = this.marker;
			}
			short data = inStream.readToShort(this.dataPieceBits);
			for (int i = 0; i < count; i++) outStream.writeBits(data, this.dataPieceBits);

		}
		
		
		return out;
	}
	
}
