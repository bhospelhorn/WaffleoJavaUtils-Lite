package waffleoRai_soundbank.sf2.modulator;


import waffleoRai_soundbank.ModController;
import waffleoRai_soundbank.ModSource;
import waffleoRai_soundbank.ModType;
import waffleoRai_soundbank.Modulator;
import waffleoRai_soundbank.TransformType;
import waffleoRai_soundbank.modulator.DefaultModSource;
import waffleoRai_soundbank.modulator.DefaultModulator;
import waffleoRai_soundbank.modulator.EmptyModController;
import waffleoRai_soundbank.sf2.SF2Mod;
import waffleoRai_soundbank.sf2.SF2ModController;
import waffleoRai_soundbank.sf2.SF2ModSourceType;
import waffleoRai_soundbank.sf2.SF2ModType;
import waffleoRai_soundbank.sf2.SF2TransformType;
import waffleoRai_soundbank.sf2.generator.SF2GeneratorConverter;

public class SF2ModConverter {
	
	public static Modulator getMod(SF2Mod mod)
	{
		DefaultModulator m = new DefaultModulator();
		
		//Amount
		m.setAmount(mod.getAmount());
		
		//GeneratorType (dest)
		m.setDestination(SF2GeneratorConverter.convertType(mod.getDestination()));
		
		//Transform
		m.setTransform(getTransform(mod.getTransform()));
		
		//Source
		m.setSource(getModType(mod.getSource()));
		
		//Source Amount
		m.setSourceAmount(getModType(mod.getSourceAmount()));
		
		return m;
	}
	
	public static SF2Mod convertMod(Modulator mod)
	{
		SF2Mod m = new SF2Mod();
		
		//Amount
		m.setAmount((short)mod.getAmount());
		
		//GeneratorType (dest)
		m.setDestination(SF2GeneratorConverter.convertGenType(mod.getDestination()));
		
		//Transform
		m.setTransform(convertTransform(mod.getTransform()));
		
		//Source
		m.setSource(convertModType(mod.getSource()));
		
		//Source Amount
		m.setSourceAmount(convertModType(mod.getSourceAmount()));
		
		return m;
	}
	
	public static SF2ModType convertModType(ModSource ms)
	{
		SF2ModType mst = new SF2ModType();
		
		//Controller
		if (ms.getController() == null) mst.setController(SF2ModController.NO_CONTROLLER);
		else mst.setController(ms.getController().getSF2Enum());
		
		//Source type
		mst.setSourceType(convertModType(ms.getType()));
		
		//Continuous
		mst.setContinuousController(ms.isContinuous());
		
		//Direction
		mst.setDirection(ms.getDirection());
		
		//Polarity
		mst.setPolarity(ms.getPolarity());
		
		return mst;
	}
	
	public static ModSource getModType(SF2ModType mst)
	{
		ModSource ms = new DefaultModSource();
		
		//Controller
		ms.setController(getController(mst.getController()));
		
		//Source type
		ms.setType(getModType(mst.getSourceType()));
		
		//Continuous
		ms.setContinuous(mst.getContinuousController());
		
		//Direction
		ms.setDirection(mst.getDirection());
		
		//Polarity
		ms.setPolarity(mst.getPolarity());
		
		return ms;
	}
	
	public static ModController getController(SF2ModController mc)
	{
		return new EmptyModController();
	}
	
	public static ModType getModType(SF2ModSourceType t)
	{
		if (t == null) return null;
		switch(t)
		{
		case CONCAVE:
			return ModType.CONCAVE;
		case CONVEX:
			return ModType.CONVEX;
		case LINEAR:
			return ModType.LINEAR;
		case SWITCH:
			return ModType.SWITCH;
		}
		return null;
	}
	
	public static SF2ModSourceType convertModType(ModType t)
	{
		if (t == null) return null;
		switch(t)
		{
		case CONCAVE:
			return SF2ModSourceType.CONCAVE;
		case CONVEX:
			return SF2ModSourceType.CONVEX;
		case LINEAR:
			return SF2ModSourceType.LINEAR;
		case SWITCH:
			return SF2ModSourceType.SWITCH;
		}
		return null;
	}
	
	public static TransformType getTransform(SF2TransformType t)
	{
		if (t == null) return null;
		switch(t)
		{
		case ABS_VAL:
			return TransformType.ABS_VAL;
		case LINEAR:
			return TransformType.LINEAR;
		}
		return null;
	}
	
	public static SF2TransformType convertTransform(TransformType t)
	{
		if (t == null) return null;
		switch(t)
		{
		case ABS_VAL:
			return SF2TransformType.ABS_VAL;
		case LINEAR:
			return SF2TransformType.LINEAR;
		}
		return null;
	}

}
