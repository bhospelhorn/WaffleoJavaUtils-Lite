package waffleoRai_soundbank;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public abstract class Region {
	
	private int iVolume; //In units of 1/(signed int max) of max volume
	private short iPan; //Positive values to right - % of short max
	
	private byte iUnityKey; //0-127
	private byte iFineTune; //In cents
	
	private byte iMinKey; //0-127
	private byte iMaxKey; //0-127
	private byte iMinVelocity; //0-127
	private byte iMaxVelocity; //0-127
	
	private int iPitchBendMin; //Semitones
	private int iPitchBendMax; //Semitones
	
	private Attack iAttack;
	private Decay iDecay;
	private Sustain iSustain;
	private Release iRelease;
	private int iHold; //Milliseconds
	
	private List<Modulator> iMods;
	private List<Generator> iGens;
	
	public void resetDefaults()
	{
		iVolume = Integer.MAX_VALUE;
		iPan = 0;
		iUnityKey = -1; //This is an override to the sound unity key. If -1, then no override entered.
		iFineTune = 0;
		iMinKey = 0;
		iMaxKey = 127;
		iMinVelocity = 0;
		iMaxVelocity = 127;
		iPitchBendMin = 2;
		iPitchBendMax = 2;
		iAttack = Attack.getDefault();
		iDecay = Decay.getDefault();
		iSustain = Sustain.getDefault();
		iRelease = Release.getDefault();
		iMods = new LinkedList<Modulator>();
		iGens = new LinkedList<Generator>();
	}

	public int getVolume() 
	{
		return iVolume;
	}

	public void setVolume(int volume) 
	{
		this.iVolume = volume;
	}

	public short getPan() 
	{
		return iPan;
	}

	public void setPan(short pan) 
	{
		this.iPan = pan;
	}

	public byte getUnityKey()
	{
		return iUnityKey;
	}
	
	public void setUnityKey(byte pitch)
	{
		if (pitch < 0) pitch = 0;
		iUnityKey = pitch;
	}
	
	public byte getFineTuneCents()
	{
		return iFineTune;
	}
	
	public void setFineTune(byte cents)
	{
		iFineTune = cents;
	}
	
	public int getPitchBendMin()
	{
		return iPitchBendMin;
	}
	
	public int getPitchBendMax()
	{
		return iPitchBendMax;
	}
	
	public void setPitchBend(int min, int max)
	{
		iPitchBendMin = min;
		iPitchBendMax = max;
	}
	
	public byte getMinKey() 
	{
		return iMinKey;
	}

	public void setMinKey(byte pitch) 
	{
		if (pitch < 0) pitch = 0;
		this.iMinKey = pitch;
	}
	
	public byte getMaxKey() 
	{
		return iMaxKey;
	}

	public void setMaxKey(byte pitch) 
	{
		if (pitch < 0) pitch = 0;
		this.iMaxKey = pitch;
	}
	
	public byte getMinVelocity() 
	{
		return iMinVelocity;
	}

	public void setMinVelocity(byte v) 
	{
		if (v < 0) v = 0;
		this.iMinVelocity = v;
	}
	
	public byte getMaxVelocity() 
	{
		return iMaxVelocity;
	}

	public void setMaxVelocity(byte v) 
	{
		if (v < 0) v = 0;
		this.iMaxVelocity = v;
	}
	
	public Attack getAttack()
	{
		return iAttack;
	}
	
	public Decay getDecay()
	{
		return iDecay;
	}
	
	public Sustain getSustain()
	{
		return iSustain;
	}
	
	public Release getRelease()
	{
		return iRelease;
	}
	
	public int getHoldInMillis()
	{
		return iHold;
	}
	
	public List<Modulator> getMods()
	{
		int count = iMods.size();
		count++;
		List<Modulator> copy = new ArrayList<Modulator>(count);
		copy.addAll(iMods);
		return copy;
	}
	
	public void addMod(Modulator m)
	{
		iMods.add(m);
	}
	
	public void clearMods()
	{
		iMods.clear();
	}
	
	public List<Generator> getGenerators()
	{
		int count = iGens.size();
		count++;
		List<Generator> copy = new ArrayList<Generator>(count);
		copy.addAll(iGens);
		return copy;
	}
	
	public void addGenerator(Generator g)
	{
		iGens.add(g);
	}
	
	public void clearGenerators()
	{
		iGens.clear();
	}
	
	public void setAttack(Attack a)
	{
		if (a == null) return;
		iAttack = a;
	}
	
	public void setDecay(Decay d)
	{
		if (d == null) return;
		iDecay = d;
	}
	
	public void setSustain(Sustain s)
	{
		if (s == null) return;
		iSustain = s;
	}
	
	public void setRelease(Release r)
	{
		if (r == null) return;
		iRelease = r;
	}

	public void setHold(int millis)
	{
		iHold = millis;
	}
	
	public void printInfo(int tabs)
	{
		String leading = "";
		for(int i = 0; i < tabs; i++) leading += "\t";
		
		System.out.println(leading + "Volume: 0x" + Integer.toHexString(this.iVolume));
		System.out.println(leading + "Pan: 0x" + String.format("%04x", this.iPan));
		System.out.println(leading + "Unity Key: 0x" + String.format("%02x", this.iUnityKey) + " (" + this.iUnityKey + ")");
		System.out.println(leading + "Fine Tune: " + this.iFineTune);
		System.out.println(leading + "Key Range: " + this.iMinKey + " - " + this.iMaxKey);
		System.out.println(leading + "Velocity Range: " + this.iMinVelocity + " - " + this.iMaxVelocity);
		System.out.println(leading + "Pitch Bend Range: " + this.iPitchBendMin + " - " + this.iPitchBendMax);
		System.out.println(leading + "Attack: " + this.iAttack.getTime() + " ms");
		System.out.println(leading + "Hold: " + this.iHold + " ms");
		System.out.println(leading + "Decay: " + this.iDecay.getTime() + " ms");
		System.out.println(leading + "Release: " + this.iRelease.getTime() + " ms");
		System.out.println(leading + "Sustain: 0x" + Integer.toHexString(this.iSustain.getLevel()));
		System.out.println(leading + "Generators: " + this.iGens.size());
		if(!iGens.isEmpty())
		{
			for(Generator g : iGens) System.out.println(leading + "->" + g.getType() + " | " + g.getAmount());
		}
		System.out.println(leading + "Modulators: " + this.iMods.size());
		if(!iMods.isEmpty())
		{
			for(Modulator m : iMods) System.out.println(leading + "->" + m.getSource() + 
					" | " + m.getDestination() + 
					" | " + m.getAmount() + 
					" | " + m.getSourceAmount() + 
					" | " + m.getTransform());
		}
	}
	
}
