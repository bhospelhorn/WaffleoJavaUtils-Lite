package waffleoRai_soundbank.sf2;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.Generator;

public class SF2Gen implements Comparable<SF2Gen> {
	
	private SF2GeneratorType gType;
	private short gAmount;
	
	public SF2Gen(SF2GeneratorType t, short rawAmt)
	{
		gType = t;
		gAmount = rawAmt;
	}
	
	public SF2GeneratorType getType()
	{
		return gType;
	}
	
	public short getRawAmount()
	{
		return gAmount;
	}

	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (this == o) return true;
		if (!(o instanceof SF2Gen)) return false;
		SF2Gen g = (SF2Gen)o;
		if (g.getType() != this.getType()) return false;
		if (g.getRawAmount() != this.getRawAmount()) return false;
		
		return true;
	}
	
	public int hashCode()
	{
		return (gType.getID() << 16) | gAmount; 
	}
	
	@Override
	public int compareTo(SF2Gen o) 
	{
		//Instrument & sampleID must be last in the list
		//keyRange and velRange must be first (keyRange before velRange)
		if (o == null) return 1;
		SF2GeneratorType ttype = this.getType();
		SF2GeneratorType otype = o.getType();
		if (ttype != otype)
		{
			if(ttype == SF2GeneratorType.instrument)
			{
				//It's last unless other is sampleID
				if (otype == SF2GeneratorType.sampleID) return -1;
				else return 1;
			}
			else if (ttype == SF2GeneratorType.sampleID)
			{
				//It's last
				return 1;
			}
			else if (ttype == SF2GeneratorType.keyRange)
			{
				//It's first
				return -1;
			}
			else if (ttype == SF2GeneratorType.velRange)
			{
				//It's first unless other is keyRange
				if (otype == SF2GeneratorType.keyRange) return 1;
				else return -1;
			}
			else if (otype == SF2GeneratorType.sampleID)
			{
				//Other is last
				return -1;
			}
			else if (otype == SF2GeneratorType.keyRange)
			{
				//Other is before this
				return 1;
			}
			else
			{
				//Just look at the enum values
				return ttype.getID() - otype.getID();
			}
		}
		else
		{
			return this.getRawAmount() - o.getRawAmount();
		}

	}

	public Generator toGeneralGenerator()
	{
		return null;
	}
	
	public static FileBuffer serializeEmptyGenerator()
	{
		FileBuffer gen = new FileBuffer(4, false);
		gen.addToFile(0);
		return gen;
	}
	
}
