package waffleoRai_soundbank.sf2.generator;

import java.util.HashMap;
import java.util.Map;

import waffleoRai_soundbank.Generator;
import waffleoRai_soundbank.GeneratorType;
import waffleoRai_soundbank.sf2.SF2;
import waffleoRai_soundbank.sf2.SF2Gen;
import waffleoRai_soundbank.sf2.SF2GeneratorType;

public class SF2GeneratorConverter {
	
	//From
	
	private static Map<SF2GeneratorType, GeneratorFetcher> fetcher_map;
	
	public static Generator convertGenerator(SF2Gen g)
	{
		if (g == null) return null;
		if (g.getType() == null) return null;
		if (fetcher_map == null) populateConverterMap();
		
		GeneratorFetcher fetcher = fetcher_map.get(g.getType());
		if (fetcher == null) return null;

		return fetcher.createGenerator(g);
	}
	
	private static void populateConverterMap()
	{
		fetcher_map = new HashMap<SF2GeneratorType, GeneratorFetcher>();
		
		GenericSignedFetcher gf_signed = new GenericSignedFetcher();
		GenericUnsignedFetcher gf_unsigned = new GenericUnsignedFetcher();
		
		fetcher_map.put(SF2GeneratorType.startAddrsOffset, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.endAddrsOffset, gf_signed);
		fetcher_map.put(SF2GeneratorType.startloopAddrsOffset, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.endloopAddrsOffset, gf_signed);
		fetcher_map.put(SF2GeneratorType.startAddrsCoarseOffset, gf_unsigned);
		
		fetcher_map.put(SF2GeneratorType.modLfoToPitch, gf_signed);
		fetcher_map.put(SF2GeneratorType.vibLfoToPitch, gf_signed);
		fetcher_map.put(SF2GeneratorType.modEnvToPitch, gf_signed);
		fetcher_map.put(SF2GeneratorType.initialFilterFc, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.initialFilterQ, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.modLfoToFilterFc, gf_signed);
		fetcher_map.put(SF2GeneratorType.modEnvToFilterFc, gf_signed);
		
		fetcher_map.put(SF2GeneratorType.endAddrsCoarseOffset, gf_signed);
		fetcher_map.put(SF2GeneratorType.modLfoToVolume, gf_signed);
		fetcher_map.put(SF2GeneratorType.chorusEffectsSend, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.reverbEffectsSend, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.pan, gf_signed);
		
		fetcher_map.put(SF2GeneratorType.delayModLFO, gf_signed);
		fetcher_map.put(SF2GeneratorType.freqModLFO, gf_signed);
		fetcher_map.put(SF2GeneratorType.delayVibLFO, gf_signed);
		fetcher_map.put(SF2GeneratorType.freqVibLFO, gf_signed);
		fetcher_map.put(SF2GeneratorType.delayModEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.attackModEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.holdModEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.decayModEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.sustainModEnv, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.releaseModEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.keynumToModEnvHold, gf_signed);
		fetcher_map.put(SF2GeneratorType.keynumToModEnvDecay, gf_signed);
		
		fetcher_map.put(SF2GeneratorType.delayVolEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.attackVolEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.holdVolEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.decayVolEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.sustainVolEnv, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.releaseVolEnv, gf_signed);
		fetcher_map.put(SF2GeneratorType.keynumToVolEnvHold, gf_signed);
		fetcher_map.put(SF2GeneratorType.keynumToVolEnvDecay, gf_signed);
		
		fetcher_map.put(SF2GeneratorType.keyRange, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.velRange, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.startloopAddrsCoarseOffset, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.keynum, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.velocity, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.initialAttenuation, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.endloopAddrsCoarseOffset, gf_signed);
		
		fetcher_map.put(SF2GeneratorType.coarseTune, gf_signed);
		fetcher_map.put(SF2GeneratorType.fineTune, gf_signed);
		fetcher_map.put(SF2GeneratorType.sampleModes, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.scaleTuning, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.exclusiveClass, gf_unsigned);
		fetcher_map.put(SF2GeneratorType.overridingRootKey, gf_unsigned);
		
	}
	
	public static void freeMaps()
	{
		fetcher_map = null;
		type_map = null;
	}

	private static Map<SF2GeneratorType, GeneratorType> type_map;
	
	public static GeneratorType convertType(SF2GeneratorType type)
	{
		if (type == null) return null;
		if (type_map == null) populateTypeMap();
		
		return type_map.get(type);
	}
	
	private static void populateTypeMap()
	{
		type_map = new HashMap<SF2GeneratorType, GeneratorType>();
		
		type_map.put(SF2GeneratorType.startAddrsOffset, GeneratorType.START_SAMPLE_OVERRIDE);
		type_map.put(SF2GeneratorType.endAddrsOffset, GeneratorType.END_SAMPLE_OVERRIDE);
		type_map.put(SF2GeneratorType.startloopAddrsOffset, GeneratorType.LOOP_START_OVERRIDE);
		type_map.put(SF2GeneratorType.endloopAddrsOffset, GeneratorType.LOOP_END_OVERRIDE);
		type_map.put(SF2GeneratorType.startAddrsCoarseOffset, GeneratorType.START_SAMPLE_COARSE_OVERRIDE);
		
		type_map.put(SF2GeneratorType.modLfoToPitch, GeneratorType.MOD_LFO_PITCH);
		type_map.put(SF2GeneratorType.vibLfoToPitch, GeneratorType.VIBRATO);
		type_map.put(SF2GeneratorType.modEnvToPitch, GeneratorType.MOD_ENV_PITCH);
		type_map.put(SF2GeneratorType.initialFilterFc, GeneratorType.HIGH_PASS_FILTER_FREQ);
		type_map.put(SF2GeneratorType.initialFilterQ, GeneratorType.HIGH_PASS_FILTER_Q);
		type_map.put(SF2GeneratorType.modLfoToFilterFc, GeneratorType.MOD_LFO_HPF_FREQ);
		type_map.put(SF2GeneratorType.modEnvToFilterFc, GeneratorType.MOD_ENV_HPF_FREQ);
		
		type_map.put(SF2GeneratorType.endAddrsCoarseOffset, GeneratorType.END_SAMPLE_COARSE_OVERRIDE);
		type_map.put(SF2GeneratorType.modLfoToVolume, GeneratorType.MOD_LFO_VOL);
		type_map.put(SF2GeneratorType.chorusEffectsSend, GeneratorType.CHORUS);
		type_map.put(SF2GeneratorType.reverbEffectsSend, GeneratorType.REVERB);
		type_map.put(SF2GeneratorType.pan, GeneratorType.PAN_OVERRIDE);
		
		type_map.put(SF2GeneratorType.delayModLFO, GeneratorType.MOD_LFO_DELAY);
		type_map.put(SF2GeneratorType.freqModLFO, GeneratorType.MOD_LFO_FREQ);
		type_map.put(SF2GeneratorType.delayVibLFO, GeneratorType.VIB_LFO_DELAY);
		type_map.put(SF2GeneratorType.freqVibLFO, GeneratorType.VIB_LFO_FREQ);
		type_map.put(SF2GeneratorType.delayModEnv, GeneratorType.MOD_ENV_DELAY);
		type_map.put(SF2GeneratorType.attackModEnv, GeneratorType.MOD_ENV_ATTACK);
		type_map.put(SF2GeneratorType.holdModEnv, GeneratorType.MOD_ENV_HOLD);
		type_map.put(SF2GeneratorType.decayModEnv, GeneratorType.MOD_ENV_DECAY);
		type_map.put(SF2GeneratorType.sustainModEnv, GeneratorType.MOD_ENV_SUSTAIN);
		type_map.put(SF2GeneratorType.releaseModEnv, GeneratorType.MOD_ENV_RELEASE);
		type_map.put(SF2GeneratorType.keynumToModEnvHold, GeneratorType.KEYNUM_TO_MOD_ENV_HOLD);
		type_map.put(SF2GeneratorType.keynumToModEnvDecay, GeneratorType.KEYNUM_TO_MOD_ENV_DECAY);
		
		type_map.put(SF2GeneratorType.delayVolEnv, GeneratorType.VOL_ENV_DELAY);
		type_map.put(SF2GeneratorType.attackVolEnv, GeneratorType.VOL_ENV_ATTACK_OVERRIDE);
		type_map.put(SF2GeneratorType.holdVolEnv, GeneratorType.VOL_ENV_HOLD);
		type_map.put(SF2GeneratorType.decayVolEnv, GeneratorType.VOL_ENV_DECAY_OVERRIDE);
		type_map.put(SF2GeneratorType.sustainVolEnv, GeneratorType.VOL_ENV_SUSTAIN_OVERRIDE);
		type_map.put(SF2GeneratorType.releaseVolEnv, GeneratorType.VOL_ENV_RELEASE_OVERRIDE);
		type_map.put(SF2GeneratorType.keynumToVolEnvHold, GeneratorType.KEYNUM_TO_VOL_ENV_HOLD);
		type_map.put(SF2GeneratorType.keynumToVolEnvDecay, GeneratorType.KEYNUM_TO_VOL_ENV_DECAY);
		
		type_map.put(SF2GeneratorType.keyRange, GeneratorType.KEY_RANGE_OVERRIDE);
		type_map.put(SF2GeneratorType.velRange, GeneratorType.VEL_RANGE_OVERRIDE);
		type_map.put(SF2GeneratorType.startloopAddrsCoarseOffset, GeneratorType.LOOP_START_COARSE_OVERRIDE);
		type_map.put(SF2GeneratorType.keynum, GeneratorType.CONSTANT_KEY);
		type_map.put(SF2GeneratorType.velocity, GeneratorType.CONSTANT_VEL);
		type_map.put(SF2GeneratorType.initialAttenuation, GeneratorType.VOLUME_OVERRIDE);
		type_map.put(SF2GeneratorType.endloopAddrsCoarseOffset, GeneratorType.LOOP_END_COARSE_OVERRIDE);
		
		type_map.put(SF2GeneratorType.coarseTune, GeneratorType.TUNING_OVERRIDE_COARSE);
		type_map.put(SF2GeneratorType.fineTune, GeneratorType.TUNING_OVERRIDE_FINE);
		type_map.put(SF2GeneratorType.sampleModes, GeneratorType.LOOP_TYPE_OVERRIDE);
		type_map.put(SF2GeneratorType.scaleTuning, GeneratorType.SCALE_TUNE);
		type_map.put(SF2GeneratorType.exclusiveClass, GeneratorType.GREEDY);
		type_map.put(SF2GeneratorType.overridingRootKey, GeneratorType.UNITY_KEY_OVERRIDE);
		
	}
	
	//To 

	public static SF2Gen convertToSF2Gen(Generator g)
	{
		if (g == null) return null;
		GeneratorType type = g.getType();
		if (type == null) return null;
		int amt = g.getAmount();
		int intmax = 0x7FFFFFFF;
		
		switch(type)
		{
		case CHORUS:
			//0.01% units -> 0.1% units
			amt = amt/10;
			return new SF2Gen(SF2GeneratorType.chorusEffectsSend, (short)amt);
		case CONSTANT_KEY:
			//0-127 - same
			if (amt < 0) amt = 0;
			if (amt > 127) amt = 127;
			return new SF2Gen(SF2GeneratorType.keynum, (short)amt);
		case CONSTANT_VEL:
			//0-127 - same
			if (amt < 0) amt = 0;
			if (amt > 127) amt = 127;
			return new SF2Gen(SF2GeneratorType.velocity, (short)amt);
		case END_SAMPLE_COARSE_OVERRIDE:
			//In units of 32768 samples. < 0
			if (amt > 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.endAddrsCoarseOffset, (short)amt);
		case END_SAMPLE_OVERRIDE:
			//In units of samples. < 0
			if (amt > 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.endAddrsOffset, (short)amt);
		case GREEDY:
			//Arbitrary number 0-127 (or 1-127) depending on set to lock
			if (amt < 0) amt = 0;
			if (amt > 127) amt = 127;
			return new SF2Gen(SF2GeneratorType.exclusiveClass, (short)amt);
		case HIGH_PASS_FILTER_FREQ:
			//Absolute cents. Convert from Hz.
			int cents = SF2.freqToCents(amt);
			if (cents < 1500) cents = 1500;
			if (cents > 13500) cents = 13500;
			return new SF2Gen(SF2GeneratorType.initialFilterFc, (short)cents);
		case HIGH_PASS_FILTER_Q:
			//Centibels
			if (amt < 0) amt = 0;
			if (amt > 960) amt = 960;
			return new SF2Gen(SF2GeneratorType.initialFilterQ, (short)amt);
		case KEYNUM_TO_MOD_ENV_DECAY:
			//Timecents/key from Milliseconds/key
			int tc1 = SF2.millisecondsToTimecents(amt);
			if (tc1 < -1200) tc1 = 1200;
			if (tc1 > 1200) tc1 = 1200;
			return new SF2Gen(SF2GeneratorType.keynumToModEnvDecay, (short)tc1);
		case KEYNUM_TO_MOD_ENV_HOLD:
			//Timecents/key from Milliseconds/key
			int tc2 = SF2.millisecondsToTimecents(amt);
			if (tc2 < -1200) tc2 = 1200;
			if (tc2 > 1200) tc2 = 1200;
			return new SF2Gen(SF2GeneratorType.keynumToModEnvHold, (short)tc2);
		case KEYNUM_TO_VOL_ENV_DECAY:
			//Timecents/key from Milliseconds/key
			int tc3 = SF2.millisecondsToTimecents(amt);
			if (tc3 < -1200) tc3 = 1200;
			if (tc3 > 1200) tc3 = 1200;
			return new SF2Gen(SF2GeneratorType.keynumToVolEnvDecay, (short)tc3);
		case KEYNUM_TO_VOL_ENV_HOLD:
			//Timecents/key from Milliseconds/key
			int tc4 = SF2.millisecondsToTimecents(amt);
			if (tc4 < -1200) tc4 = 1200;
			if (tc4 > 1200) tc4 = 1200;
			return new SF2Gen(SF2GeneratorType.keynumToVolEnvHold, (short)tc4);
		case KEY_RANGE_OVERRIDE:
			//Top in lo byte, bottom in hi byte - same
			return new SF2Gen(SF2GeneratorType.keyRange, (short)amt);
		case LOOP_END_COARSE_OVERRIDE:
			//In units of 32768 samples. <= 0
			if (amt > 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.endloopAddrsCoarseOffset, (short)amt);
		case LOOP_END_OVERRIDE:
			//In units of samples. <= 0
			if (amt > 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.endloopAddrsOffset, (short)amt);
		case LOOP_START_COARSE_OVERRIDE:
			//In units of 32768 samples. >= 0
			if (amt < 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.startloopAddrsCoarseOffset, (short)amt);
		case LOOP_START_OVERRIDE:
			//In units of 0 samples. >= 0
			if (amt < 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.startloopAddrsOffset, (short)amt);
		case LOOP_TYPE_OVERRIDE:
			if (amt > 3) amt = 3;
			if (amt < 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.sampleModes, (short)amt);
		case MOD_ENV_ATTACK:
			//Milliseconds -> abs timecents
			//If these modes are being used, the assumption is that they are already SF2 scaled.
			//These generators should only be made by reading SF2 files!
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.attackModEnv, (short)amt);
		case MOD_ENV_DECAY:
			//Milliseconds -> abs timecents
			//If these modes are being used, the assumption is that they are already SF2 scaled.
			//These generators should only be made by reading SF2 files!
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.decayModEnv, (short)amt);
		case MOD_ENV_DELAY:
			//Milliseconds -> abs timecents
			//If these modes are being used, the assumption is that they are already SF2 scaled.
			//These generators should only be made by reading SF2 files!
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.delayModEnv, (short)amt);
		case MOD_ENV_HOLD:
			//Milliseconds -> abs timecents
			//If these modes are being used, the assumption is that they are already SF2 scaled.
			//These generators should only be made by reading SF2 files!
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.holdModEnv, (short)amt);
		case MOD_ENV_HPF_FREQ:
			//Freq -> cents
			int cents1 = SF2.freqToCents(amt);
			return new SF2Gen(SF2GeneratorType.modEnvToFilterFc, (short)cents1);
		case MOD_ENV_PITCH:
			//Cents
			return new SF2Gen(SF2GeneratorType.modEnvToPitch, (short)amt);
		case MOD_ENV_RELEASE:
			//Milliseconds -> abs timecents
			//If these modes are being used, the assumption is that they are already SF2 scaled.
			//These generators should only be made by reading SF2 files!
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.releaseModEnv, (short)amt);
		case MOD_ENV_SUSTAIN:
			//(1/intmax units) -> 0.1% units
			double damt = (double)amt;
			//if (damt == 0.0) damt = 0.0000001;
			double d = (1000.0 * damt)/((double)intmax);
			amt = (int)Math.round(d);
			return new SF2Gen(SF2GeneratorType.sustainModEnv, (short)amt);
		case MOD_LFO_DELAY:
			//Milliseconds -> abs timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.delayModLFO, (short)amt);
		case MOD_LFO_FREQ:
			//Freq -> cents
			int cents2 = SF2.freqToCents(amt);
			return new SF2Gen(SF2GeneratorType.freqModLFO, (short)cents2);
		case MOD_LFO_HPF_FREQ:
			//Freq -> cents
			int cents3 = SF2.freqToCents(amt);
			return new SF2Gen(SF2GeneratorType.modLfoToFilterFc, (short)cents3);
		case MOD_LFO_PITCH:
			//Cents
			return new SF2Gen(SF2GeneratorType.modLfoToPitch, (short)amt);
		case MOD_LFO_VOL:
			//Centibels
			return new SF2Gen(SF2GeneratorType.modLfoToVolume, (short)amt);
		case PAN_OVERRIDE:
			//(1/intmax) units -> 0.1% units
			double d1 = (1000.0 * (double)amt)/((double)intmax);
			amt = (int)Math.round(d1);
			return new SF2Gen(SF2GeneratorType.pan, (short)amt);
		case PORTAMENTO:
			//No dice
			return null;
		case PORTAMENTO_DELAY:
			//No dice
			return null;
		case REVERB:
			//0.01% units -> 0.1% units
			amt = amt/10;
			return new SF2Gen(SF2GeneratorType.reverbEffectsSend, (short)amt);
		case SCALE_TUNE:
			//Percent (0-100)
			return new SF2Gen(SF2GeneratorType.scaleTuning, (short)amt);
		case START_SAMPLE_COARSE_OVERRIDE:
			//In units of 32768 samples. >= 0
			if (amt < 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.startAddrsCoarseOffset, (short)amt);
		case START_SAMPLE_OVERRIDE:
			//In units of samples. >= 0
			if (amt < 0) amt = 0;
			return new SF2Gen(SF2GeneratorType.startAddrsOffset, (short)amt);
		case TUNING_OVERRIDE_COARSE:
			//Semitones
			return new SF2Gen(SF2GeneratorType.coarseTune, (short)amt);
		case TUNING_OVERRIDE_FINE:
			//Cents
			return new SF2Gen(SF2GeneratorType.fineTune, (short)amt);
		case UNITY_KEY_OVERRIDE:
			//0-127
			if (amt < 0) amt = 0;
			if (amt > 127) amt = 127;
			return new SF2Gen(SF2GeneratorType.overridingRootKey, (short)amt);
		case VEL_RANGE_OVERRIDE:
			//Top in lo byte, bottom in hi byte - same
			return new SF2Gen(SF2GeneratorType.velRange, (short)amt);
		case VIBRATO:
			//Cents
			return new SF2Gen(SF2GeneratorType.vibLfoToPitch, (short)amt);
		case VIBRATO_DELAY:
			//Milliseconds -> timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.delayVibLFO, (short)amt);
		case VIB_LFO_DELAY:
			//Milliseconds -> timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.delayVibLFO, (short)amt);
		case VIB_LFO_FREQ:
			//Freq -> cents
			int cents4 = SF2.freqToCents(amt);
			return new SF2Gen(SF2GeneratorType.freqVibLFO, (short)cents4);
		case VOLUME_OVERRIDE:
			//1/(int max) units -> Centibels (Attenuation)
			amt = SF2.envelopeDiffToCentibels(0x7FFFFFFF, 0x7FFFFFFF-amt);
			return new SF2Gen(SF2GeneratorType.initialAttenuation, (short)amt);
		case VOL_ENV_ATTACK_OVERRIDE:
			//Milliseconds -> timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.attackVolEnv, (short)amt);
		case VOL_ENV_DECAY_OVERRIDE:
			//Milliseconds -> timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.decayVolEnv, (short)amt);
		case VOL_ENV_DELAY:
			//Milliseconds -> timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.delayVolEnv, (short)amt);
		case VOL_ENV_HOLD:
			//Milliseconds -> timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.holdVolEnv, (short)amt);
		case VOL_ENV_RELEASE_OVERRIDE:
			//Milliseconds -> timecents
			amt = SF2.millisecondsToTimecents(amt);
			return new SF2Gen(SF2GeneratorType.releaseVolEnv, (short)amt);
		case VOL_ENV_SUSTAIN_OVERRIDE:
			//(1/(max int)) max envelope level -> Centibels decrease
			amt = SF2.envelopeDiffToCentibels(intmax, (intmax-amt));
			return new SF2Gen(SF2GeneratorType.sustainVolEnv, (short)amt);
		}
		
		return null;
	}
	
	public static SF2GeneratorType convertGenType(GeneratorType t)
	{
		if (t == null) return null;
		switch (t)
		{
		case CHORUS: return SF2GeneratorType.chorusEffectsSend;
		case CONSTANT_KEY: return SF2GeneratorType.keynum;
		case CONSTANT_VEL: return SF2GeneratorType.velocity;
		case END_SAMPLE_COARSE_OVERRIDE: return SF2GeneratorType.endAddrsCoarseOffset;
		case END_SAMPLE_OVERRIDE: return SF2GeneratorType.endAddrsOffset;
		case GREEDY: return SF2GeneratorType.exclusiveClass;
		case HIGH_PASS_FILTER_FREQ: return SF2GeneratorType.initialFilterFc;
		case HIGH_PASS_FILTER_Q: return SF2GeneratorType.initialFilterQ;
		case KEYNUM_TO_MOD_ENV_DECAY: return SF2GeneratorType.keynumToModEnvDecay;
		case KEYNUM_TO_MOD_ENV_HOLD: return SF2GeneratorType.keynumToModEnvHold;
		case KEYNUM_TO_VOL_ENV_DECAY: return SF2GeneratorType.keynumToVolEnvDecay;
		case KEYNUM_TO_VOL_ENV_HOLD: return SF2GeneratorType.keynumToVolEnvHold;
		case KEY_RANGE_OVERRIDE: return SF2GeneratorType.keyRange;
		case LOOP_END_COARSE_OVERRIDE: return SF2GeneratorType.endloopAddrsCoarseOffset;
		case LOOP_END_OVERRIDE: return SF2GeneratorType.endloopAddrsOffset;
		case LOOP_START_COARSE_OVERRIDE: return SF2GeneratorType.startloopAddrsCoarseOffset;
		case LOOP_START_OVERRIDE: return SF2GeneratorType.startloopAddrsOffset;
		case LOOP_TYPE_OVERRIDE: return SF2GeneratorType.sampleModes;
		case MOD_ENV_ATTACK: return SF2GeneratorType.attackModEnv;
		case MOD_ENV_DECAY: return SF2GeneratorType.decayModEnv;
		case MOD_ENV_DELAY: return SF2GeneratorType.delayModEnv;
		case MOD_ENV_HOLD: return SF2GeneratorType.holdModEnv;
		case MOD_ENV_HPF_FREQ: return SF2GeneratorType.modEnvToFilterFc;
		case MOD_ENV_PITCH: return SF2GeneratorType.modEnvToPitch;
		case MOD_ENV_RELEASE: return SF2GeneratorType.releaseModEnv;
		case MOD_ENV_SUSTAIN: return SF2GeneratorType.sustainModEnv;
		case MOD_LFO_DELAY: return SF2GeneratorType.delayModLFO;
		case MOD_LFO_FREQ: return SF2GeneratorType.freqModLFO;
		case MOD_LFO_HPF_FREQ: return SF2GeneratorType.modLfoToFilterFc;
		case MOD_LFO_PITCH: return SF2GeneratorType.modLfoToPitch;
		case MOD_LFO_VOL: return SF2GeneratorType.modLfoToVolume;
		case PAN_OVERRIDE: return SF2GeneratorType.pan;
		case PORTAMENTO: return null;
		case PORTAMENTO_DELAY: return null;
		case REVERB: return SF2GeneratorType.reverbEffectsSend;
		case SCALE_TUNE: return SF2GeneratorType.scaleTuning;
		case START_SAMPLE_COARSE_OVERRIDE: return SF2GeneratorType.startAddrsCoarseOffset;
		case START_SAMPLE_OVERRIDE: return SF2GeneratorType.startAddrsOffset;
		case TUNING_OVERRIDE_COARSE: return SF2GeneratorType.coarseTune;
		case TUNING_OVERRIDE_FINE: return SF2GeneratorType.fineTune;
		case UNITY_KEY_OVERRIDE: return SF2GeneratorType.overridingRootKey;
		case VEL_RANGE_OVERRIDE: return SF2GeneratorType.velRange;
		case VIBRATO: return SF2GeneratorType.vibLfoToPitch;
		case VIBRATO_DELAY: return SF2GeneratorType.delayVibLFO;
		case VIB_LFO_DELAY: return SF2GeneratorType.delayVibLFO;
		case VIB_LFO_FREQ: return SF2GeneratorType.freqVibLFO;
		case VOLUME_OVERRIDE: return SF2GeneratorType.initialAttenuation;
		case VOL_ENV_ATTACK_OVERRIDE: return SF2GeneratorType.attackVolEnv;
		case VOL_ENV_DECAY_OVERRIDE: return SF2GeneratorType.decayVolEnv;
		case VOL_ENV_DELAY: return SF2GeneratorType.delayVolEnv;
		case VOL_ENV_HOLD: return SF2GeneratorType.holdVolEnv;
		case VOL_ENV_RELEASE_OVERRIDE: return SF2GeneratorType.releaseVolEnv;
		case VOL_ENV_SUSTAIN_OVERRIDE: return SF2GeneratorType.sustainVolEnv;
		}
		return null;
	}
	
}
