package waffleoRai_soundbank.sf2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sound.midi.Soundbank;

import waffleoRai_Sound.BitDepth;
import waffleoRai_Sound.Sound;
import waffleoRai_Sound.WAV;
import waffleoRai_Sound.WAV.LoopType;
import waffleoRai_soundbank.SoundSample;
import waffleoRai_soundbank.sf2.SF2SHDR.LinkType;

public class SF2Sample {
	
	private SF2SHDR header;
	
	private Sound sounddata;
	
	public SF2Sample(SF2SHDR hdr, Sound snd)
	{
		header = hdr;
		sounddata = snd;
	}
	
	public void updateHeader(long startSample)
	{
		int frames = sounddata.totalFrames();
		int lpst = sounddata.getLoopFrame();
		int lped = sounddata.getLoopEndFrame();
		if (lpst < 0) lpst = 0;
		if (lped < 0) lped = frames;
		header.setSampleCoordinates(startSample, (long)frames + startSample);
		header.setLoopCoordinates(startSample + (long)lpst, startSample + (long)lped);
	}
	
	public SF2SHDR getHeader()
	{
		return header;
	}
	
	public String getName()
	{
		return header.getSampleName();
	}
	
	public Sound getSound()
	{
		return sounddata;
	}
	
	public SoundSample toBankSample(Soundbank parentBank)
	{
		SoundSample s = new SoundSample(parentBank, header.getSampleName(), sounddata);
		return s;
	}

	public static SoundSample toBankSample(Soundbank parentBank, Collection<SF2Sample> sfsamps)
	{
		if (sfsamps == null || sfsamps.isEmpty()) return null;
		//We'll have to merge all of the sounds into a single new sound...
		
		//Get initial data
		int chcount = sfsamps.size();
		SF2Sample[] sarr = new SF2Sample[chcount];
		sarr = sfsamps.toArray(sarr);
		SF2Sample first = sarr[0];
		if (chcount == 1)
		{
			return first.toBankSample(parentBank);
		}
		int bd = 16;
		if (first.getSound().getBitDepth() == BitDepth.TWENTYFOUR_BIT_SIGNED) bd = 24;
		int sr = first.getHeader().getSampleRate();
		//(Don't forget loop points)
		long st = first.getHeader().getStartSamplePoint();
		long lst = first.getHeader().getStartLoopPoint() - st;
		long led = first.getHeader().getEndLoopPoint() - st;
		int frames = first.getSound().totalFrames();
		
		//Make WAV skeleton
		WAV mywav = new WAV(bd, chcount, frames);
		mywav.setSampleRate(sr);
		mywav.setLoop(LoopType.Forward, (int)lst, (int)led);
		
		//Load channel data from previous sounds
		//Figure out if this is a case of left/right or multi-channel
		if (chcount == 2)
		{
			SF2Sample second = sarr[1];
			Sound fsound = first.getSound();
			Sound ssound = second.getSound();
			if(first.getHeader().getLinkType() == LinkType.RIGHT)
			{
				int[] right = fsound.getRawSamples(0);
				int[] left = ssound.getRawSamples(0);
				mywav.copyData(0, left);
				mywav.copyData(1, right);
			}
			else
			{
				int[] right = ssound.getRawSamples(0);
				int[] left = fsound.getRawSamples(0);
				mywav.copyData(0, left);
				mywav.copyData(1, right);
			}
			
		}
		else
		{
			for (int i = 0; i < chcount; i++)
			{
				SF2Sample ch = sarr[i];
				Sound cdat = ch.getSound();
				int[] carr = cdat.getRawSamples(0);
				mywav.copyData(i, carr);
			}
		}
		
		//Wrap up our new Sound in a SoundSample and return
		SoundSample ss = new SoundSample(parentBank, first.getHeader().getSampleName(), mywav);
		
		return ss;
	}
	
	public static List<SF2Sample> convertSample(SoundSample s)
	{
		Sound sound = s.getSound();
		List<SF2Sample> slist = new ArrayList<SF2Sample>(sound.totalChannels()+1);

		if (sound.totalChannels() == 1)
		{
			SF2SHDR header = new SF2SHDR(s.getName());
			//Load header
			header.setFineTune(sound.getFineTune());
			header.setLinkType(LinkType.MONO);
			header.setSampleCoordinates(0, sound.totalFrames());
			header.setLoopCoordinates(sound.getLoopFrame(), sound.getLoopEndFrame());
			header.setSampleRate(sound.getSampleRate());
			header.setUnityNote((byte)sound.getUnityNote());
			SF2Sample sample = new SF2Sample(header, sound);
			slist.add(sample);
		}
		else if (sound.totalChannels() == 2)
		{
			for (int c = 0; c < 2; c++)
			{
				String name = s.getName();
				if (name.length() > 18) name = name.substring(0, 18);
				if (c == 0) name += "_L";
				else if (c == 1) name += "_R";
				SF2SHDR header = new SF2SHDR(name);
				header.setFineTune(sound.getFineTune());
				if (c == 0) header.setLinkType(LinkType.LEFT);
				else if (c == 1) header.setLinkType(LinkType.RIGHT);
				header.setSampleCoordinates(0, sound.totalFrames());
				header.setLoopCoordinates(sound.getLoopFrame(), sound.getLoopEndFrame());
				header.setSampleRate(sound.getSampleRate());
				header.setUnityNote((byte)sound.getUnityNote());
				SF2Sample sample = new SF2Sample(header, sound.getSingleChannel(c));
				slist.add(sample);
			}
		}
		else
		{
			int ccount = sound.totalChannels();
			for (int c = 0; c < ccount; c++)
			{
				String name = s.getName();
				int digits = (c % 10) + 1;
				int nmax = 20 - (digits + 1);
				if (name.length() > nmax) name = name.substring(0, nmax);
				name += "_" + c;
				SF2SHDR header = new SF2SHDR(name);
				header.setFineTune(sound.getFineTune());
				header.setLinkType(LinkType.LINKED);
				header.setSampleCoordinates(0, sound.totalFrames());
				header.setLoopCoordinates(sound.getLoopFrame(), sound.getLoopEndFrame());
				header.setSampleRate(sound.getSampleRate());
				header.setUnityNote((byte)sound.getUnityNote());
				SF2Sample sample = new SF2Sample(header, sound.getSingleChannel(c));
				slist.add(sample);
			}
		}
		
		
		return slist;
	}
	
}
