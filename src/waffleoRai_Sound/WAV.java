package waffleoRai_Sound;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class WAV implements Sound{
	
	/* ----- Constants ----- */
	
	public static final String MAG0 = "RIFF";
	public static final String MAG1 = "WAVE";
	
	public static final String MAG_FMT = "fmt ";
	public static final String MAG_DATA = "data";
	public static final String MAG_SMPL = "smpl";
	
	public static final int DEFAULT_BUFFERED_FRAME = 44100;
	
	/* ----- Instance Variables ----- */
	
		//fmt
	private int sampleRate;
	//private boolean bd_signed;
	private int bitDepth;
	
		//data
	private Channel[] data;
		
		//smpl
	private boolean useSMPL;
	private int smpl_unityNote;
	private int smpl_pitchTune;
	private List<SampleLoop> smpl_loops;
	
	/* ----- Inner Classes ----- */
	
	public static enum LoopType
	{
		Forward(0),
		Pingpong(1),
		Reverse(2);
		
		private int typecode;
		
		private LoopType(int en)
		{
			typecode = en;
		}
	
		public int getWAV_enum()
		{
			return typecode;
		}
	
		public static LoopType getType(int t)
		{
			switch(t)
			{
			case 0: return LoopType.Forward;
			case 1: return LoopType.Pingpong;
			case 2: return LoopType.Reverse;
			}
			return null;
		}
		
	}
	
	private static class Channel
	{
		private int[] samples;
		private int currentFrame;
		
		public Channel(int frameCount)
		{
			samples = new int[frameCount];
			currentFrame = 0;
		}
		
		public int getSample(int framei)
		{
			return samples[framei];
		}
		
		public void setSample(int framei, int sample)
		{
			samples[framei] = sample;
		}
		
		private void scaleSigned(int nBD, int tBD)
		{
			if (nBD < 32 && tBD < 32)
			{
				int nmax = (1 << (nBD - 1));	
				int tmax = (1 << (tBD - 1));
				
				double factor = (double)tmax/(double)nmax;
				for (int i = 0; i < samples.length; i++)
				{
					int s = samples[i];
					double p = (double)s * factor;
					samples[i] = (int)Math.round(p);
				}
			}
			else
			{
				long nmax = (1L << (nBD - 1L));	
				long tmax = (1L << (tBD - 1L));
				
				double factor = (double)tmax/(double)nmax;
				
				for (int i = 0; i < samples.length; i++)
				{
					long s = (long)samples[i];
					double p = (double)s * factor;
					samples[i] = (int)Math.round(p);
				}
			}
		}
		
		private void scaleUnsigned(int nBD, int tBD)
		{
			if (nBD < 32 && tBD < 32)
			{
				int nmax = (1 << nBD) - 1;	
				int tmax = (1 << tBD) - 1;
				
				double factor = (double)tmax/(double)nmax;
				for (int i = 0; i < samples.length; i++)
				{
					int s = samples[i];
					double p = (double)s * factor;
					samples[i] = (int)Math.round(p);
				}
			}
			else
			{
				long nmax = (1L << nBD) - 1L;	
				long tmax = (1L << tBD) - 1L;
				
				double factor = (double)tmax/(double)nmax;
				
				for (int i = 0; i < samples.length; i++)
				{
					long s = Integer.toUnsignedLong(samples[i]);
					double p = (double)s * factor;
					samples[i] = (int)Math.round(p);
				}
			}
		}
		
		private void signedToUnsigned(int BD)
		{
			long centerl = 1L << (BD - 1);
			int centeri = 1 << (BD - 1);
			
			for (int i = 0; i < samples.length; i++)
			{
				int s = samples[i];
				if(BD >= 32)
				{
					long l = (long)s;
					l += centerl;
					s = (int)l;
				}
				else s += centeri;
				samples[i] = s;
			}
		}
		
		private void unsignedToSigned(int BD)
		{
			long centerl = 1L << BD;
			int centeri = 1 << BD;
			
			for (int i = 0; i < samples.length; i++)
			{
				int s = samples[i];
				if(BD >= 32)
				{
					long l = (long)s;
					l -= centerl;
					s = (int)l;
				}
				else s -= centeri;
				samples[i] = s;
			}
		}
		
		public void scaleBitDepth(int initBD, int targetBD, boolean initSigned, boolean targetSigned)
		{
			//Unsigned -> signed | subtract max signed positive
			//long centerl = 1L << targetBD;
			//int centeri = 1 << targetBD;
			
			if (targetBD == initBD)
			{
				if (initSigned == targetSigned) return;
				if (initSigned) signedToUnsigned(targetBD);
				else unsignedToSigned(targetBD);
			}
			else
			{
				//Scale up
				if (initSigned)
				{
					scaleSigned(initBD, targetBD);
					if (!targetSigned) signedToUnsigned(targetBD);
				}
				else
				{
					scaleUnsigned(initBD, targetBD);
					if (targetSigned) unsignedToSigned(targetBD);
				}
			}
		}
		
		public void changeLength(int nlen)
		{
			if (nlen < 1) return;
			if (nlen == samples.length) return;
			int[] oldsamps = samples;
			samples = new int[nlen];
			if (oldsamps.length <= samples.length)
			{
				for (int i = 0; i < oldsamps.length; i++) samples[i] = oldsamps[i];
			}
			else
			{
				for (int i = 0; i < samples.length; i++) samples[i] = oldsamps[i];
			}
		}
		
		public int countSamples()
		{
			return samples.length;
		}
		
		public int getCurrentFrameIndex()
		{
			return currentFrame;
		}
		
		public void setCurrentFrameIndex(int fi)
		{
			if (fi > samples.length) return;
			currentFrame = fi;
		}
		
		public void incrementCurrentFrameIndex()
		{
			currentFrame++;
		}
		
	}
	
	private static class SampleLoop
	{
		private int ID;
		private LoopType type;
		private int start;
		private int end;
		private int fraction;
		private int playCount;
		
		public SampleLoop(int n)
		{
			ID = n;
			type = LoopType.Forward;
			start = 0;
			end = 0;
			fraction = 0;
			playCount = 0;
		}
	
		public SampleLoop copy()
		{
			SampleLoop sl = new SampleLoop(ID);
			sl.type = this.type;
			sl.start = this.start;
			sl.end = this.end;
			sl.fraction = this.fraction;
			sl.playCount = this.playCount;
			return sl;
		}
	}
	
	/* ----- Construction ----- */
	
	public WAV(String file) throws IOException, UnsupportedFileTypeException
	{
		FileBuffer wavfile = FileBuffer.createBuffer(file, false);
		smpl_loops = new LinkedList<SampleLoop>();
		parseWAV(wavfile);
	}
	
	public WAV(int bitdepth, int channels, int frames)
	{
		if (channels < 1 || frames < 1 || bitdepth < 1) throw new IllegalArgumentException();
		bitDepth = bitdepth;
		//bd_signed = false;
		//if (bitDepth <= 8) bd_signed = true;
		data = new Channel[channels];
		for (int i = 0; i < channels; i++) data[i] = new Channel(frames);
		sampleRate = 44100; //Default
		
		useSMPL = false;
		smpl_unityNote = 60;
		smpl_pitchTune = 0;
		smpl_loops = new LinkedList<SampleLoop>();
	}
	
	/* ----- Parsing ----- */
	
	private void parseWAV(FileBuffer mywav) throws UnsupportedFileTypeException
	{
		if (mywav == null) throw new FileBuffer.UnsupportedFileTypeException();
		
		//Check for magics
		long m0 = mywav.findString(0, 0x10, MAG0);
		long m1 = mywav.findString(0, 0x10, MAG1);
		
		if (m0 != 0) throw new FileBuffer.UnsupportedFileTypeException();
		if (m1 != 8) throw new FileBuffer.UnsupportedFileTypeException();
		mywav.setEndian(false);
		
		long cPos = m1 + 4;
		long sz = mywav.getFileSize();
		
		//Find fmt
		cPos = mywav.findString(m1, sz, MAG_FMT);
		if (cPos < 0) throw new FileBuffer.UnsupportedFileTypeException(); //Mandatory chunk
		cPos += 8; //Skip magic and chunk size
		short compCode = mywav.shortFromFile(cPos); cPos += 2;
		//Right now, we'll only deal with uncompressed PCM. Maybe add more later.
		if (compCode != 1) throw new FileBuffer.UnsupportedFileTypeException();
		short chNum = mywav.shortFromFile(cPos); cPos += 2;
		sampleRate = mywav.intFromFile(cPos); cPos += 4;
		cPos += 4; //Skip average bytes per second.
		cPos += 2; //Skip block align
		bitDepth = Short.toUnsignedInt(mywav.shortFromFile(cPos)); cPos += 2;
		
		//Find data
		cPos = mywav.findString(m1, sz, MAG_DATA);
		if (cPos < 0) throw new FileBuffer.UnsupportedFileTypeException(); //Mandatory chunk
		cPos += 4; //Skip magic
		int datSz = mywav.intFromFile(cPos); cPos += 4;
		int bdBytes = bitDepth/8;
		int fCount = (datSz/bdBytes)/chNum;
		data = new Channel[chNum];
		for (int c = 0; c < chNum; c++) data[c] = new Channel(fCount);
		for (int f = 0; f < fCount; f++)
		{
			for (int c = 0; c < chNum; c++)
			{
				Channel ch = data[c];
				switch(bdBytes)
				{
				case 1:
					byte sBY = mywav.getByte(cPos); cPos++;
					ch.setSample(f, Byte.toUnsignedInt(sBY));
					break;
				case 2:
					short sSH = mywav.shortFromFile(cPos); cPos += 2;
					ch.setSample(f, (int)sSH);
					break;
				case 3:
					int sSI = mywav.shortishFromFile(cPos); cPos += 3;
					//Sign extend
					sSI = sSI << 8;
					sSI = sSI >> 8;
					ch.setSample(f, sSI);
					break;
				case 4:
					int sIN = mywav.intFromFile(cPos); cPos += 4;
					ch.setSample(f, sIN);
					break;
				default: throw new FileBuffer.UnsupportedFileTypeException();
				}
			}
		}
		
		//Find smpl
		useSMPL = false;
		cPos = mywav.findString(m1, sz, MAG_SMPL);
		if (cPos < 0) return;
		useSMPL = true;
		smpl_loops = new LinkedList<SampleLoop>();
		cPos += (4+4+4+4+4); //Skip to unity note
		smpl_unityNote = mywav.intFromFile(cPos); cPos+=4;
		smpl_pitchTune = mywav.intFromFile(cPos); cPos+=4;
		cPos += (4+4); //Skip SMPTE stuff
		int loopCount = mywav.intFromFile(cPos); cPos += 4;
		cPos += 4; //Skip sampler data
		
		//Read sample loops
		for (int i = 0; i < loopCount; i++)
		{
			int ID = mywav.intFromFile(cPos); cPos+=4;
			SampleLoop loop = new SampleLoop(ID);
			loop.type = LoopType.getType(mywav.intFromFile(cPos)); cPos+=4;
			loop.start = mywav.intFromFile(cPos); cPos += 4;
			loop.end = mywav.intFromFile(cPos); cPos += 4;
			loop.fraction = mywav.intFromFile(cPos); cPos += 4;
			loop.playCount = mywav.intFromFile(cPos); cPos += 4;
			
			smpl_loops.add(loop);
		}
		
	}
	
	/* ----- Serialization ----- */
	
	private int dataChunkSize()
	{
		int byS = bitDepth/8;
		int chN = data.length;
		if (data == null) return -1;
		Channel c = data[0];
		if (c == null) return -1;
		int fc = c.countSamples();
		
		return (byS * chN * fc);
	}
	
	private FileBuffer serializeFMT()
	{
		FileBuffer fmt = new FileBuffer(20, false);
		
		fmt.printASCIIToFile(MAG_FMT);
		fmt.addToFile(16); //Chunk size. Fixed for now since only handle one format
		fmt.addToFile((short)1); //Compression code. Fixed for now since only handle one format
		fmt.addToFile((short)data.length); //Number of channels
		fmt.addToFile(sampleRate); //Sample rate
		int ba = (bitDepth/8)/data.length;
		int abps = sampleRate * ba;
		fmt.addToFile(abps);
		fmt.addToFile((short)ba);
		fmt.addToFile((short)bitDepth);
		
		return fmt;
	}
	
	private FileBuffer serializeDATA() throws IOException
	{
		int csz = dataChunkSize();
		FileBuffer datChunk = FileBuffer.createWritableBuffer("wavdat", csz + 8, false);
		datChunk.printASCIIToFile(MAG_DATA);
		datChunk.addToFile(csz);
		
		int byS = bitDepth/8;
		int chN = data.length;
		if (data == null) return null;
		Channel ch = data[0];
		if (ch == null) return null;
		int fc = ch.countSamples();
		
		for (int f = 0; f < fc; f++)
		{
			for (int c = 0; c < chN; c++)
			{
				int s = data[c].getSample(f);
				switch(byS)
				{
				case 1:
					datChunk.addToFile((byte)s);
					break;
				case 2:
					datChunk.addToFile((short)s);
					break;
				case 3:
					datChunk.add24ToFile(s);
					break;
				case 4:
					datChunk.addToFile(s);
					break;
				default: return null;
				}
			}
		}
		
		
		return datChunk;
	}
	
	private FileBuffer serializeSMPL()
	{
		if(!useSMPL) return null;
		int smplsize = (9 * 4) + (smpl_loops.size() * 24);
		
		FileBuffer smpl = new FileBuffer(smplsize+8, false);
		smpl.printASCIIToFile(MAG_SMPL);
		smpl.addToFile(smplsize);
		smpl.addToFile(0); //Manufacturer 
		smpl.addToFile(0); //Product
		int sper = 1000000000/sampleRate; //Sample period (time passed for one sample in ns)
		smpl.addToFile(sper);
		smpl.addToFile(smpl_unityNote); //Unity note
		smpl.addToFile(smpl_pitchTune); //Pitch Fraction
		smpl.addToFile(0); //SMPTE Format
		smpl.addToFile(0); //SMPTE Offset
		smpl.addToFile(smpl_loops.size()); //Loop count
		smpl.addToFile(0); //Sampler data
		
		//Loops
		for (SampleLoop l : smpl_loops)
		{
			smpl.addToFile(l.ID);
			smpl.addToFile(l.type.typecode);
			smpl.addToFile(l.start);
			smpl.addToFile(l.end);
			smpl.addToFile(l.fraction);
			smpl.addToFile(l.playCount);
		}
		
		return smpl;
	}
	
	public FileBuffer serializeMe() throws IOException
	{
		int chunks = 3;
		FileBuffer fmt = serializeFMT();
		FileBuffer data = serializeDATA();
		FileBuffer smpl = serializeSMPL();
		if(smpl != null) chunks++;
		long wavSz = fmt.getFileSize() + data.getFileSize();
		if (smpl != null) wavSz += smpl.getFileSize();
		
		//Make header.
		FileBuffer wavhead = new FileBuffer(12, false);
		wavhead.printASCIIToFile(MAG0);
		wavhead.addToFile((int)wavSz);
		wavhead.printASCIIToFile(MAG1);
		
		//Make composite
		FileBuffer comp = new CompositeBuffer(chunks);
		comp.addToFile(wavhead);
		comp.addToFile(fmt);
		comp.addToFile(data);
		if(smpl != null) comp.addToFile(smpl);
		
		return comp;
	}
	
	public void writeFile(String filepath) throws IOException
	{
		FileBuffer wav = serializeMe();
		wav.writeFile(filepath);
	}
	
	/* ----- Getters ----- */
	
	public int numberChannels()
	{
		return data.length;
	}
	
	public int getRawBitDepth()
	{
		return bitDepth;
	}
	
	public int getSample(int channel, int frame)
	{
		if (channel < 0) throw new IndexOutOfBoundsException();
		if (channel >= data.length)  throw new IndexOutOfBoundsException();
		Channel c = data[channel];
		return c.getSample(frame);
	}
	
	public int getSampleRate()
	{
		return this.sampleRate;
	}
	
	/* ----- Setters ----- */
	
	public void setSampleRate(int sr)
	{
		sampleRate = sr;
	}

	public void copyData(int targetChannel, int[] copydata)
	{
		if (targetChannel < 0 || targetChannel > data.length) return;
		if (copydata == null) return;
		
		int dlen = copydata.length;
		Channel c = this.data[targetChannel];
		if (c == null) return;
		c.changeLength(dlen);
		for (int i = 0; i < dlen; i++) c.setSample(i, copydata[i]);
		
	}
	
	public void setLoop(LoopType type, int startSamp, int endSamp)
	{
		if (smpl_loops == null) smpl_loops = new LinkedList<SampleLoop>();
		useSMPL = true;
		SampleLoop loop = new SampleLoop(smpl_loops.size());
		
		loop.end = endSamp;
		loop.start = startSamp;
		loop.type = type;
		smpl_loops.add(loop);
	}
	
	public void adjustBitDepth(int newBitDepth)
	{
		if (data == null) return;
		for (int c = 0; c < data.length; c++)
		{
			Channel ch = data[c];
			if (ch == null) continue;
			boolean nowsigned = bitDepth <= 8;
			boolean tsigned = newBitDepth <= 8;
			ch.scaleBitDepth(bitDepth, newBitDepth, nowsigned, tsigned);
		}
	}

	public void setSMPL_tune(int unityKey, int finetune)
	{
		smpl_unityNote = unityKey;
		smpl_pitchTune = finetune;
	}
	
	/* ----- Sound Interface ----- */
	
	@Override
	public AudioFormat getFormat() {
		int ch = data.length;
		AudioFormat wavformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float)sampleRate, 
				bitDepth, 
				ch, (bitDepth/8) * ch,
				(float)sampleRate, false);
		return wavformat;
	}

	@Override
	public AudioInputStream getStream() 
	{
		WAVInputStream rawis = new WAVInputStream(this, DEFAULT_BUFFERED_FRAME);
		AudioInputStream ais = new AudioInputStream(rawis, getFormat(), totalFrames());
		return ais;
	}

	@Override
	public void jumpToFrame(int frame) 
	{
		if (frame < 0) return;
		for (Channel c : data)
		{
			c.setCurrentFrameIndex(frame);
		}
	}

	@Override
	public void rewind() 
	{
		for (Channel c : data)
		{
			c.setCurrentFrameIndex(0);
		}
	}

	@Override
	public int nextSample(int channel) 
	{
		if (channel < 0) throw new IndexOutOfBoundsException();
		if (channel >= data.length) throw new IndexOutOfBoundsException();
		
		Channel c = data[channel];
		int fi = c.getCurrentFrameIndex();
		if (fi >= c.countSamples())
		{
			//End of channel
			throw new IndexOutOfBoundsException();
		}
		int sample = c.getSample(fi);
		c.incrementCurrentFrameIndex();
		
		fi = c.getCurrentFrameIndex();
		if (loops() && (fi == this.getLoopEndFrame()))
		{
			int lf = this.getLoopFrame();
			if (lf >= 0)
			{
				c.setCurrentFrameIndex(lf);
			}
		}
		
		return sample;
	}

	@Override
	public int samplesLeft(int channel) 
	{
		if (this.loops()) return -1;
		if (channel < 0) throw new IndexOutOfBoundsException();
		if (channel >= data.length) throw new IndexOutOfBoundsException();
		
		Channel c = data[channel];
		int fi = c.getCurrentFrameIndex();
		return c.countSamples() - fi;
	}

	@Override
	public boolean hasSamplesLeft(int channel)
	{
		if (this.loops()) return true;
		if (channel < 0) throw new IndexOutOfBoundsException();
		if (channel >= data.length) throw new IndexOutOfBoundsException();
		
		Channel c = data[channel];
		int fi = c.getCurrentFrameIndex();
		return (fi < c.countSamples());
	}

	@Override
	public int totalFrames() 
	{
		if (data == null) return 0;
		if (data[0] == null) return 0;
		return data[0].countSamples();
	}
	
	public boolean loops()
	{
		return !(smpl_loops.isEmpty());
	}

	@Override
	public int getLoopFrame() 
	{
		if (!loops()) return -1;
		SampleLoop sl = smpl_loops.get(0);
		if (sl == null) return -1;
		return sl.start;
	}
	
	public int getLoopEndFrame()
	{
		if (!loops()) return -1;
		SampleLoop sl = smpl_loops.get(0);
		if (sl == null) return -1;
		return sl.end;
	}

	@Override
	public void flushBuffer() 
	{
		//Does nothing
	}
	
	public int[] getRawSamples(int channel)
	{
		if (channel < 0) throw new IndexOutOfBoundsException();
		if (channel >= data.length) throw new IndexOutOfBoundsException();
		
		Channel c = data[channel];
		int frames = c.countSamples();
		
		int[] rawsamp = new int[frames];
		for (int i = 0; i < frames; i++) rawsamp[i] = c.getSample(i);
		
		return rawsamp;
	}

	public BitDepth getBitDepth()
	{
		switch(bitDepth)
		{
		case 8: return BitDepth.EIGHT_BIT_UNSIGNED;
		case 16: return BitDepth.SIXTEEN_BIT_SIGNED;
		case 24: return BitDepth.TWENTYFOUR_BIT_SIGNED;
		case 32: return BitDepth.THIRTYTWO_BIT_SIGNED;
		}
		return null;
	}
	
	public int[] getSamples_16Signed(int channel)
	{
		if (channel < 0) throw new IndexOutOfBoundsException();
		if (channel >= data.length) throw new IndexOutOfBoundsException();
		
		Channel c = data[channel];
		int frames = c.countSamples();
		
		int[] scaled = new int[frames];
		
		BitDepth bd = getBitDepth();
		
		switch(bd)
		{
		case EIGHT_BIT_UNSIGNED:
			for (int i = 0; i < frames; i++) 
			{
				int samp = c.getSample(i);
				samp = Sound.scaleSampleUp8Bits(samp, BitDepth.EIGHT_BIT_UNSIGNED);
				samp = Sound.scaleSampleToSigned(samp, BitDepth.SIXTEEN_BIT_UNSIGNED);
				scaled[i] = samp;
			}
			break;
		case THIRTYTWO_BIT_SIGNED:
			for (int i = 0; i < frames; i++) 
			{
				int samp = c.getSample(i);
				samp = Sound.scaleSampleDown8Bits(samp, BitDepth.THIRTYTWO_BIT_SIGNED);
				samp = Sound.scaleSampleDown8Bits(samp, BitDepth.TWENTYFOUR_BIT_SIGNED);
				scaled[i] = samp;
			}
			break;
		case TWENTYFOUR_BIT_SIGNED:
			for (int i = 0; i < frames; i++) 
			{
				int samp = c.getSample(i);
				samp = Sound.scaleSampleDown8Bits(samp, BitDepth.TWENTYFOUR_BIT_SIGNED);
				scaled[i] = samp;
			}
			break;
		default:
			for (int i = 0; i < frames; i++) scaled[i] = c.getSample(i);
			break;
		}
		
		return scaled;
	}
	
	public int[] getSamples_24Signed(int channel)
	{
		if (channel < 0) throw new IndexOutOfBoundsException();
		if (channel >= data.length) throw new IndexOutOfBoundsException();
		
		Channel c = data[channel];
		int frames = c.countSamples();
		
		int[] scaled = new int[frames];
		
		BitDepth bd = getBitDepth();
		
		switch(bd)
		{
		case EIGHT_BIT_UNSIGNED:
			for (int i = 0; i < frames; i++) 
			{
				int samp = c.getSample(i);
				samp = Sound.scaleSampleUp8Bits(samp, BitDepth.EIGHT_BIT_UNSIGNED);
				samp = Sound.scaleSampleToSigned(samp, BitDepth.SIXTEEN_BIT_UNSIGNED);
				samp = Sound.scaleSampleUp8Bits(samp, BitDepth.SIXTEEN_BIT_SIGNED);
				scaled[i] = samp;
			}
			break;
		case THIRTYTWO_BIT_SIGNED:
			for (int i = 0; i < frames; i++) 
			{
				int samp = c.getSample(i);
				samp = Sound.scaleSampleDown8Bits(samp, BitDepth.THIRTYTWO_BIT_SIGNED);
				scaled[i] = samp;
			}
			break;
		case SIXTEEN_BIT_SIGNED:
			for (int i = 0; i < frames; i++) 
			{
				int samp = c.getSample(i);
				samp = Sound.scaleSampleUp8Bits(samp, BitDepth.SIXTEEN_BIT_SIGNED);
				scaled[i] = samp;
			}
			break;
		default:
			for (int i = 0; i < frames; i++) scaled[i] = c.getSample(i);
			break;
		}
		
		return scaled;
	}
	
	public int totalChannels()
	{
		return data.length;
	}
	
	public int getUnityNote()
	{
		return smpl_unityNote;
	}
	
	public int getFineTune()
	{
		return smpl_pitchTune;
	}

	public Sound getSingleChannel(int channel)
	{
		if (this.totalChannels() == 1) return this;
		if (channel < 0) return null;
		if (channel >= data.length) return null;
		
		WAV ch = new WAV(bitDepth, 1, totalFrames());
		ch.sampleRate = this.sampleRate;
		ch.copyData(channel, this.data[channel].samples);
		
		ch.useSMPL = this.useSMPL;
		ch.smpl_unityNote = this.smpl_unityNote;
		ch.smpl_pitchTune = this.smpl_pitchTune;
		
		if (this.useSMPL)
		{
			for (SampleLoop sl : smpl_loops)
			{
				ch.smpl_loops.add(sl.copy());
			}
		}
		
		return ch;
	}
	
}
