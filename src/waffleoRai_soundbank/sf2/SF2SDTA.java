package waffleoRai_soundbank.sf2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Sound.Sound;
import waffleoRai_Sound.WAV;
import waffleoRai_Sound.WAV.LoopType;
import waffleoRai_Utils.CompositeBuffer;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class SF2SDTA {
	
	//Note: SHDRs go here!!
	
	public static final String MAGIC_SDTA = "sdta";
	public static final String MAGIC_SMPL = "smpl";
	public static final String MAGIC_SM24 = "sm24";
	
	public static final int SAMPLE_PAD = 46;
	
	/* ----- Constants ----- */
	
	/* ----- Instance Variables ----- */
	
	private boolean use_24bit;
	
	private List<SF2Sample> samples;
	
	/* ----- Construction ----- */
	
	public SF2SDTA()
	{
		use_24bit = false;
		samples = new LinkedList<SF2Sample>();
	}
	
	public SF2SDTA(FileBuffer SHDR, FileBuffer SDTA) throws UnsupportedFileTypeException, IOException
	{
		samples = new LinkedList<SF2Sample>();
		List<SF2SHDR> hlist = parseSHDRList(SHDR);
		parseSDTA(SDTA, hlist);
	}
	
	/* ----- Parsing ----- */
	
	private List<SF2SHDR> parseSHDRList(FileBuffer SHDR) throws UnsupportedFileTypeException
	{
		if (SHDR == null) throw new FileBuffer.UnsupportedFileTypeException();
		List<SF2SHDR> hlist = new LinkedList<SF2SHDR>();
		long cpos = 0;
		cpos = SHDR.findString(0, 0x10, SF2SHDR.SHDR_MAGIC);
		if (cpos != 0) throw new FileBuffer.UnsupportedFileTypeException();
		cpos += 4;
		//Get the chunk size
		int chsz = SHDR.intFromFile(cpos); cpos += 4;
		
		int nrec = (chsz/SF2SHDR.RECORD_SIZE) - 1; //Take out EOS
		for (int i = 0; i < nrec; i++)
		{
			SF2SHDR header = new SF2SHDR(SHDR, cpos);
			hlist.add(header);
			cpos += SF2SHDR.RECORD_SIZE;
		}
		
		return hlist;
	}

	private void parseSDTA(FileBuffer sdta, List<SF2SHDR> headers) throws UnsupportedFileTypeException, IOException
	{
		if (sdta == null) throw new FileBuffer.UnsupportedFileTypeException();
		if (headers == null) throw new FileBuffer.UnsupportedFileTypeException();
		
		long sz = sdta.getFileSize();
		long smpl_pos = sdta.findString(0, sz, MAGIC_SMPL);
		long sm24_pos = sdta.findString(0, sz, MAGIC_SM24);
		
		if (smpl_pos < 0) throw new FileBuffer.UnsupportedFileTypeException();
		if (sm24_pos < 0) use_24bit = false;
		else use_24bit = true;
		
		//Get chunk sizes
		long smpl_sz = Integer.toUnsignedLong(sdta.intFromFile(smpl_pos + 4));
		long sm24_sz = 0;
		if (use_24bit) sm24_sz = Integer.toUnsignedLong(sdta.intFromFile(sm24_pos + 4));
		
		//Make sub-buffers for each chunk
		FileBuffer smpl_buf = sdta.createReadOnlyCopy(smpl_pos, smpl_pos + smpl_sz);
		FileBuffer sm24_buf = null;
		if (use_24bit) sm24_buf = sdta.createReadOnlyCopy(sm24_pos, sm24_pos + sm24_sz);
		
		//Set expected bit depth
		int bd = 16;
		if (use_24bit) bd = 24;
		
		//Start reading
		for (SF2SHDR h : headers)
		{
			//Get number of frames
			int frames = (int)(h.getEndSamplePoint() - h.getStartSamplePoint());
			//Make WAV
			WAV mysound = new WAV(bd, 1, frames);
			//Load in header information
			mysound.setSampleRate(h.getSampleRate());
			mysound.setSMPL_tune(h.getUnityNote(), h.getFineTuneCents());
			long loopstart = h.getStartLoopPoint() - h.getStartSamplePoint();
			long loopend = h.getEndLoopPoint() - h.getStartSamplePoint();
			mysound.setLoop(LoopType.Forward, (int)loopstart, (int)loopend);
			//Copy sound data
			int[] sounddat = new int[frames];
			long spos = h.getStartSamplePoint()*2 + 8;
			long epos = h.getStartSamplePoint() + 8;
			for (int i = 0; i < frames; i++)
			{
				int basesamp = (int)smpl_buf.shortFromFile(spos); //Sign-extend???
				if (use_24bit)
				{
					int addsamp = Byte.toUnsignedInt(sm24_buf.getByte(epos));
					basesamp = (basesamp << 8) | addsamp;
				}
				sounddat[i] = basesamp;
				spos += 2;
				epos++;
			}
			mysound.copyData(0, sounddat);
			
			SF2Sample samp = new SF2Sample(h, mysound);
			samples.add(samp);
		}
		
	}
	
	/* ----- Serialization ----- */
	
	private void updateSampleHeaders()
	{
		//If any sounds are new or altered, the header positions must be changed
		//	to reflect that!!
		long stsamp = 0;
		for (SF2Sample s : samples)
		{
			s.updateHeader(stsamp);
			stsamp = s.getHeader().getEndSamplePoint() + SAMPLE_PAD;
		}
	}
	
	private FileBuffer serializeSMPL()
	{
		//Don't forget that 46 sample padding!
		int scount = samples.size();
		FileBuffer smpl = new CompositeBuffer(scount + 1);
		List<FileBuffer> sslist = new LinkedList<FileBuffer>();
		for (SF2Sample s : samples)
		{
			Sound snd = s.getSound();
			int frames = snd.totalFrames();
			FileBuffer mysound = new FileBuffer((frames + SAMPLE_PAD) * 2, false);
			
			int[] scaled = null;
			if(use_24bit) scaled = snd.getSamples_24Signed(0);
			else scaled = snd.getSamples_16Signed(0);
			
			for (int i = 0; i < frames; i++) {
				if(use_24bit) 
				{
					int samp = scaled[i];
					samp = (samp >>> 8) & 0xFFFF;
					mysound.addToFile((short)samp);
				}
				else mysound.addToFile((short)scaled[i]);
			}
			for (int i = 0; i < SAMPLE_PAD; i++) mysound.addToFile((short)0x0000);
			
			sslist.add(mysound);
		}
		int chsz = 0;
		for(FileBuffer s : sslist) chsz += (int)s.getFileSize();
		
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(MAGIC_SMPL);
		header.addToFile(chsz);
		
		smpl.addToFile(header);
		for(FileBuffer s : sslist) smpl.addToFile(s);
		
		return smpl;
	}
	
	private FileBuffer serializeSM24()
	{
		if(!use_24bit) return null;
		
		int scount = samples.size();
		FileBuffer smpl = new CompositeBuffer(scount + 2);
		List<FileBuffer> sslist = new LinkedList<FileBuffer>();
		for (SF2Sample s : samples)
		{
			Sound snd = s.getSound();
			int frames = snd.totalFrames();
			FileBuffer mysound = new FileBuffer((frames + SAMPLE_PAD), false);
			
			int[] scaled = snd.getSamples_24Signed(0);
			
			for (int i = 0; i < frames; i++) {
				int samp = scaled[i];
				samp = samp & 0xFF;
				mysound.addToFile((byte)samp);
			}
			for (int i = 0; i < SAMPLE_PAD; i++) mysound.addToFile((byte)0x00);
			
			sslist.add(mysound);
		}
		int chsz = 0;
		for(FileBuffer s : sslist) chsz += (int)s.getFileSize();
		boolean pad = (chsz % 2 != 0);
		if (pad) chsz++;
		
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(MAGIC_SM24);
		header.addToFile(chsz);
		
		smpl.addToFile(header);
		for(FileBuffer s : sslist) smpl.addToFile(s);
		
		if (pad)
		{
			FileBuffer padding = new FileBuffer(2, false);
			padding.addToFile((byte)0x00);
			smpl.addToFile(padding);
		}
		
		return smpl;
		
	}
	
	public FileBuffer serializeSDTA()
	{
		//LIST (size) sdta
		//smpl chunk
		//sm24 chunk
		
		FileBuffer smpl_ch = serializeSMPL();
		FileBuffer sm24_ch = serializeSM24();
		
		int list_sz = (int)smpl_ch.getFileSize();
		if (sm24_ch != null) list_sz += (int)sm24_ch.getFileSize();
		list_sz += 4;
		
		FileBuffer header = new FileBuffer(12, false);
		header.printASCIIToFile(SF2.LIST_MAGIC);
		header.addToFile(list_sz);
		header.printASCIIToFile(MAGIC_SDTA);
		
		FileBuffer sdta = new CompositeBuffer(3);
		sdta.addToFile(header);
		sdta.addToFile(smpl_ch);
		if (sm24_ch != null) sdta.addToFile(sm24_ch);
		
		return sdta;
	}
	
	public FileBuffer serializeSHDR()
	{
		updateSampleHeaders();
		FileBuffer header = new FileBuffer(8, false);
		header.printASCIIToFile(SF2SHDR.SHDR_MAGIC);
		int hcount = samples.size() + 1;
		int csz = hcount * SF2SHDR.RECORD_SIZE;
		header.addToFile(csz);
		
		FileBuffer shdr = new CompositeBuffer(hcount + 2);
		shdr.addToFile(header);
		
		for (SF2Sample s : samples)
		{
			SF2SHDR h = s.getHeader();
			shdr.addToFile(h.serializeRecord());
		}
		
		shdr.addToFile(SF2SHDR.getSerializedTerminalRecord());
		
		return shdr;
	}
	
	/* ----- Getters ----- */
	
	public boolean use_16bit()
	{
		return !use_24bit;
	}
	
	public boolean use_24bit()
	{
		return use_24bit;
	}
	
	public List<SF2Sample> getSampleList()
	{
		//List<SF2Sample> copy = new LinkedList<SF2Sample>();
		int sz = samples.size();
		List<SF2Sample> copy = new ArrayList<SF2Sample>(sz);
		copy.addAll(samples);
		return copy;
	}
	
	/* ----- Setters ----- */
	
	public void set24bit(boolean b)
	{
		use_24bit = b;
	}
	
	public void addSample(SF2Sample s)
	{
		samples.add(s);
	}
	
	public void clearSamples()
	{
		samples.clear();
	}

	/* ----- Debug ----- */
	
	public void printInfo()
	{
		System.out.println("--- SF2 SDATA (and SHDR) ---");
		if(this.use_24bit) System.out.println("24-bit Samples");
		else System.out.println("16-bit Samples");
		System.out.println("Sample Count: " + this.samples.size());
		int i = 0;
		for(SF2Sample s : samples)
		{
			System.out.println("-> SAMPLE " + i);
			SF2SHDR header = s.getHeader();
			System.out.println("\tName: " + header.getSampleName());
			System.out.println("\tSample Rate: " + header.getSampleRate());
			System.out.println("\tUnity Note: " + String.format("%02x", header.getUnityNote()));
			System.out.println("\tFine Tune: " + header.getFineTuneCents());
			System.out.println("\tStart: 0x" + Long.toHexString(header.getStartSamplePoint()));
			System.out.println("\tEnd: 0x" + Long.toHexString(header.getEndSamplePoint()));
			System.out.println("\tLoop Start: 0x" + Long.toHexString(header.getStartLoopPoint()));
			System.out.println("\tLoop End: 0x" + Long.toHexString(header.getEndLoopPoint()));
			System.out.println("\tLink Type: " + header.getLinkType());
			System.out.println("\tLink: " + header.getSampleLink());
			
			System.out.println("\tSound is null? " + (s.getSound() == null));
			i++;
		}
	}
	
}
