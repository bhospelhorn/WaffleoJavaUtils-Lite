package waffleoRai_soundbank;

import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

import waffleoRai_Sound.Sound;

public class SoundSample extends SoundbankResource{

	private Sound iData;
	
	public SoundSample(Soundbank parentBank, String name, Sound data)
	{
		super(parentBank, name, Sound.class);
		iData = data;
	}
	
	@Override
	public Object getData() {
		
		return iData;
	}
	
	public Sound getSound()
	{
		return iData;
	}

}
