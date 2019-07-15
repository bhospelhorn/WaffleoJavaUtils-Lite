package waffleoRai_soundbank.sf2;

import java.util.HashMap;
import java.util.Map;

public enum SF2GeneratorType {
	
	startAddrsOffset(0),
	endAddrsOffset(1),
	startloopAddrsOffset(2),
	endloopAddrsOffset(3),
	startAddrsCoarseOffset(4),
	modLfoToPitch(5),
	vibLfoToPitch(6),
	modEnvToPitch(7),
	initialFilterFc(8),
	initialFilterQ(9),
	modLfoToFilterFc(10),
	modEnvToFilterFc(11),
	endAddrsCoarseOffset(12),
	modLfoToVolume(13),
	chorusEffectsSend(15),
	reverbEffectsSend(16),
	pan(17),
	
	delayModLFO(21),
	freqModLFO(22),
	delayVibLFO(23),
	freqVibLFO(24),
	delayModEnv(25),
	attackModEnv(26),
	holdModEnv(27),
	decayModEnv(28),
	sustainModEnv(29),
	releaseModEnv(30),
	keynumToModEnvHold(31),
	keynumToModEnvDecay(32),
	
	delayVolEnv(33),
	attackVolEnv(34),
	holdVolEnv(35),
	decayVolEnv(36),
	sustainVolEnv(37),
	releaseVolEnv(38),
	keynumToVolEnvHold(39),
	keynumToVolEnvDecay(40),
	
	instrument(41),
	
	keyRange(43),
	velRange(44),
	
	startloopAddrsCoarseOffset(45),
	
	keynum(46),
	velocity(47),
	initialAttenuation(48),
	
	endloopAddrsCoarseOffset(50),
	
	coarseTune(51),
	fineTune(52),
	
	sampleID(53),
	sampleModes(54),
	
	scaleTuning(56),
	exclusiveClass(57),
	overridingRootKey(58),
	;
	
	private int ID;
	
	private SF2GeneratorType(int sfid)
	{
		ID = sfid;
	}
	
	public int getID()
	{
		return ID;
	}
	
	private static Map<Integer, SF2GeneratorType> vmap;
	
	private static void populateMap()
	{
		vmap = new HashMap<Integer, SF2GeneratorType>();
		SF2GeneratorType[] val = SF2GeneratorType.values();
		for (SF2GeneratorType t : val)
		{
			vmap.put(t.getID(), t);
		}
	}
	
	public static SF2GeneratorType getGeneratorType(int val)
	{
		if (vmap == null) populateMap();
		return vmap.get(val);
	}

}
