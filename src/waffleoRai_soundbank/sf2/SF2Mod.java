package waffleoRai_soundbank.sf2;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class SF2Mod {
	
	private SF2ModType source;
	private SF2GeneratorType dest;
	private short amount;
	private SF2ModType sourceAmount;
	private SF2TransformType transform;
	
	public SF2Mod()
	{
		source = new SF2ModType();
		dest = SF2GeneratorType.keynum;
		amount = 0;
		sourceAmount = new SF2ModType();
		transform = SF2TransformType.LINEAR;
	}
	
	public SF2Mod(FileBuffer file, long stpos) throws UnsupportedFileTypeException
	{
		if (file == null) throw new FileBuffer.UnsupportedFileTypeException();
		long cpos = stpos;
		
		short s = file.shortFromFile(cpos); cpos += 2;
		short d = file.shortFromFile(cpos); cpos += 2;
		amount = file.shortFromFile(cpos); cpos += 2;
		short sa = file.shortFromFile(cpos); cpos += 2;
		short t = file.shortFromFile(cpos);
		
		source = new SF2ModType(s);
		sourceAmount = new SF2ModType(sa);
		dest = SF2GeneratorType.getGeneratorType(d);
		if (dest == null) dest = SF2GeneratorType.keynum;
		transform = SF2TransformType.getTransform(t);
		if (transform == null) transform = SF2TransformType.LINEAR;
	}

	public SF2ModType getSource()
	{
		return source;
	}
	
	public SF2GeneratorType getDestination()
	{
		return dest;
	}
	
	public short getAmount()
	{
		return amount;
	}
	
	public SF2ModType getSourceAmount()
	{
		return sourceAmount;
	}
	
	public SF2TransformType getTransform()
	{
		return transform;
	}
	
	public void setSource(SF2ModType s)
	{
		source = s;
	}
	
	public void setDestination(SF2GeneratorType d)
	{
		dest = d;
	}
	
	public void setAmount(short amt)
	{
		amount = amt;
	}
	
	public void setSourceAmount(SF2ModType sa)
	{
		sourceAmount = sa;
	}
	
	public void setTransform(SF2TransformType t)
	{
		transform = t;
	}

	public FileBuffer serializeMe()
	{
		FileBuffer mod = new FileBuffer(10, false);
		mod.addToFile(source.serializeMe());
		mod.addToFile((short)dest.getID());
		mod.addToFile(amount);
		mod.addToFile(sourceAmount.serializeMe());
		mod.addToFile((short)transform.getValue());
		return mod;
	}
	
	public static FileBuffer serializeEmptyModulator()
	{
		FileBuffer mod = new FileBuffer(10, false);
		for(int i = 0; i < 10; i++) mod.addToFile((byte)0x00);
		return mod;
	}
	
}
