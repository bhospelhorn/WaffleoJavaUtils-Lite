package waffleoRai_SeqSound;

import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class MidiMessageGenerator {
	
	//Note - a Velocity 0 note-on is treated as a note-off
	//	This can be used for running status optimization
	
	public static final int UNSIGNED_14_BITS_MAX = 0x3FFF;
	
	public static final int RPN_PITCHBEND_RANGE = 0x0000;
	public static final int RPN_FINETUNE = 0x0001;
	public static final int RPN_COARSETUNE = 0x0002;
	public static final int RPN_TUNING_PROG_CHANGE = 0x0003;
	public static final int RPN_TUNING_BANK_CHANGE = 0x0004;
	public static final int RPN_MOD_DEPTH_RANGE = 0x0005;
	
	/*--- Utility ---*/
	
	public static int clampTo14U(int val)
	{
		if(val > UNSIGNED_14_BITS_MAX) val = UNSIGNED_14_BITS_MAX;
		if(val < 0) val = 0;
		return val;
	}
	
	public static int clampTo7U(int val)
	{
		if(val > 0x7F) val = 0x7F;
		if(val < 0) val = 0;
		return val;
	}
	
	/*--- State ---*/
	
	private int lastBank_msb;
	private int lastBank_lsb;
	private int lastModLevel_msb;
	private int lastModLevel_lsb;
	private int lastPortaTime_msb;
	private int lastPortaTime_lsb;
	private int lastVolume_msb;
	private int lastVolume_lsb;
	private int lastPan_msb;
	private int lastPan_lsb;
	private int lastExp_msb;
	private int lastExp_lsb;
	
	private boolean lastDamperSetting;
	private boolean lastPortaSetting;
	private int lastReleaseSetting = -1;
	private int lastAttackSetting = -1;
	private int lastPortaCtrlSetting = -1;

	/*--- Primary Commands ---*/
	
	public MidiMessage genNoteOn(int channel, int note, int velocity) throws InvalidMidiDataException
	{
		int status = 0x90 | (channel & 0xF);
		return new ShortMessage(status, note, velocity);
	}
	
	public MidiMessage genNoteOff(int channel, int note) throws InvalidMidiDataException
	{
		int status = 0x80 | (channel & 0xF);
		return new ShortMessage(status, note, 0);
	}
	
	public MidiMessage genNoteOff(int channel, int note, int velocity) throws InvalidMidiDataException
	{
		int status = 0x80 | (channel & 0xF);
		return new ShortMessage(status, note, velocity);
	}
	
	public MidiMessage genProgramChange(int channel, int program) throws InvalidMidiDataException
	{
		int status = 0xC0 | (channel & 0xF);
		return new ShortMessage(status, program, 0);
	}
	
	public MidiMessage genPitchBend(int channel, int cents, int bendRangeSemitones) throws InvalidMidiDataException
	{
		return genPitchBend(channel, cents, 100, bendRangeSemitones);
	}
	
	public MidiMessage genPitchBend(int channel, int units, int unitsPerSemitone, int bendRangeSemitones) throws InvalidMidiDataException
	{
		int maxunits = bendRangeSemitones * unitsPerSemitone;
		int minunits = -1 * maxunits;
		if(units > maxunits) units = maxunits;
		if(units < minunits) units = minunits;
		
		//Goes up to 0x1FFF
		double midMax = (double)0x1FFF;
		double ratio = (double)units/(double)maxunits;
		int scale = (int)Math.round(midMax * ratio);
		scale += 0x1FFF;
		
		return genPitchBend(channel, scale);
	}
	
	public MidiMessage genPitchBend(int channel, int value) throws InvalidMidiDataException
	{
		int status = 0xE0 | (channel & 0xF);
		//Clamp to 14 bits unsigned
		if(value > 0x3FFF) value = 0x3FFF;
		if(value < 0) value = 0;
		int lsb = value & 0x7F;
		int msb = (value >>> 7) & 0x7F;
		return new ShortMessage(status, lsb, msb);
	}
	
	/*--- Controller Commands ---*/
	
	public List<MidiMessage> genBankSelect(int channel, int bank) throws InvalidMidiDataException
	{
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		bank = clampTo14U(bank);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (bank >>> 7)&0x7F;
		int lsb = bank &0x7F;
		if(msb != lastBank_msb){list.add(new ShortMessage(status, 0x00, msb)); lastBank_msb = msb;}
		if(lsb != lastBank_lsb){list.add(new ShortMessage(status, 0x20, lsb)); lastBank_lsb = lsb;}
		return list;
	}
	
	public List<MidiMessage> genModWheelLevel(int channel, int level) throws InvalidMidiDataException
	{
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		level = clampTo14U(level);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (level >>> 7)&0x7F;
		int lsb = level &0x7F;
		if(msb != lastModLevel_msb){list.add(new ShortMessage(status, 0x01, msb)); lastModLevel_msb = msb;}
		if(lsb != lastModLevel_lsb){list.add(new ShortMessage(status, 0x21, lsb)); lastModLevel_lsb = lsb;}
		return list;
	}
	
	public List<MidiMessage> genModWheelLevel(int channel, byte level) throws InvalidMidiDataException
	{
		int bi = Byte.toUnsignedInt(level);
		bi = bi << 7;
		
		return genModWheelLevel(channel, bi);
	}
	
	public List<MidiMessage> genPortamentoTime(int channel, int value) throws InvalidMidiDataException
	{
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		value = clampTo14U(value);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (value >>> 7)&0x7F;
		int lsb = value &0x7F;
		if(msb != lastPortaTime_msb){list.add(new ShortMessage(status, 0x05, msb)); lastPortaTime_msb = msb;}
		if(lsb != lastPortaTime_lsb){list.add(new ShortMessage(status, 0x25, lsb)); lastPortaTime_lsb = lsb;}
		return list;
	}
	
	public List<MidiMessage> genPortamentoTime(int channel, byte value) throws InvalidMidiDataException
	{
		int bi = Byte.toUnsignedInt(value);
		bi = bi << 7;
		
		return genPortamentoTime(channel, bi);
	}
	
	public List<MidiMessage> genVolumeChange(int channel, int level) throws InvalidMidiDataException
	{
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		level = clampTo14U(level);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (level >>> 7)&0x7F;
		int lsb = level &0x7F;
		if(msb != lastVolume_msb){list.add(new ShortMessage(status, 0x07, msb)); lastVolume_msb = msb;}
		if(lsb != lastVolume_lsb){list.add(new ShortMessage(status, 0x27, lsb)); lastVolume_lsb = lsb;}
		return list;
	}
	
	public List<MidiMessage> genVolumeChange(int channel, byte level) throws InvalidMidiDataException
	{
		int bi = Byte.toUnsignedInt(level);
		bi = bi << 7;
		
		return genVolumeChange(channel, bi);
	}
	
	public List<MidiMessage> genPanChange(int channel, int value) throws InvalidMidiDataException
	{
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		value = clampTo14U(value);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (value >>> 7)&0x7F;
		int lsb = value &0x7F;
		if(msb != lastPan_msb){list.add(new ShortMessage(status, 0x0A, msb)); lastPan_msb = msb;}
		if(lsb != lastPan_lsb){list.add(new ShortMessage(status, 0x2A, lsb)); lastPan_lsb = lsb;}
		return list;
	}
	
	public List<MidiMessage> genPanChange(int channel, byte value) throws InvalidMidiDataException
	{
		int bi = Byte.toUnsignedInt(value);
		bi = bi << 7;
		return genPanChange(channel, bi);
	}
	
	public List<MidiMessage> genExpressionChange(int channel, int level) throws InvalidMidiDataException
	{
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		level = clampTo14U(level);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (level >>> 7)&0x7F;
		int lsb = level &0x7F;
		if(msb != lastExp_msb){list.add(new ShortMessage(status, 0x0B, msb)); lastExp_msb = msb;}
		if(lsb != lastExp_lsb){list.add(new ShortMessage(status, 0x2B, lsb)); lastExp_lsb = lsb;}
		return list;
	}
	
	public List<MidiMessage> genExpressionChange(int channel, byte level) throws InvalidMidiDataException
	{
		int bi = Byte.toUnsignedInt(level);
		bi = bi << 7;
		return genExpressionChange(channel, bi);
	}
	
	public MidiMessage genDamperChange(int channel, boolean on) throws InvalidMidiDataException
	{
		if(on == lastDamperSetting) return null;
		lastDamperSetting = on;
		int status = 0xB0 | (channel & 0xF);
		int v = 0;
		if(on) v = 0x7F;
		return new ShortMessage(status, 0x40, v);
	}
	
	public MidiMessage genPortamentoSet(int channel, boolean on) throws InvalidMidiDataException
	{
		if(on == lastPortaSetting) return null;
		lastPortaSetting = on;
		int status = 0xB0 | (channel & 0xF);
		int v = 0;
		if(on) v = 0x7F;
		return new ShortMessage(status, 0x41, v);
	}
	
	public MidiMessage genReleaseTimeChange(int channel, int value) throws InvalidMidiDataException
	{
		value = clampTo7U(value);
		if(value == lastReleaseSetting) return null;
		lastReleaseSetting = value;
		int status = 0xB0 | (channel & 0xF);
		return new ShortMessage(status, 0x48, value);
	}
	
	public MidiMessage genAttackTimeChange(int channel, int value) throws InvalidMidiDataException
	{
		value = clampTo7U(value);
		if(value == lastAttackSetting) return null;
		lastAttackSetting = value;
		int status = 0xB0 | (channel & 0xF);
		return new ShortMessage(status, 0x49, value);
	}
	
	public MidiMessage genPortamentoControl(int channel, int sourceNote) throws InvalidMidiDataException
	{
		sourceNote = clampTo7U(sourceNote);
		if(sourceNote == lastPortaCtrlSetting) return null;
		lastPortaCtrlSetting = sourceNote;
		int status = 0xB0 | (channel & 0xF);
		return new ShortMessage(status, 0x54, sourceNote);
	}
	
	public List<MidiMessage> genNRPN(int channel, int index, int value, boolean omitFine) throws InvalidMidiDataException
	{
		if(index < 0 || index > 0x3FFF) return null;
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		value = clampTo14U(value);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (value >>> 7) & 0x7F;
		int lsb = value &0x7F;
		int msbi = (index >>> 7) & 0x7F;
		int lsbi = index &0x7F;

		list.add(new ShortMessage(status, 0x62, lsbi)); 
		list.add(new ShortMessage(status, 0x63, msbi)); 
		list.add(new ShortMessage(status, 0x06, msb)); 
		if(!omitFine) list.add(new ShortMessage(status, 0x26, lsb));
		
		//Do I really need these?
		//list.add(new ShortMessage(status, 0x62, 0x7F)); 
		//list.add(new ShortMessage(status, 0x63, 0x7F)); 
		return list;
	}
	
	private List<MidiMessage> genRPN(int channel, int index, int value, boolean omitFine) throws InvalidMidiDataException
	{
		if(index < 0 || index > 0x3FFF) return null;
		int status = 0xB0 | (channel & 0xF);
		//Clamp to 14 bits
		value = clampTo14U(value);
		List<MidiMessage> list = new LinkedList<MidiMessage>();
		int msb = (value >>> 7) & 0x7F;
		int lsb = value &0x7F;
		int msbi = (index >>> 7) & 0x7F;
		int lsbi = index &0x7F;

		list.add(new ShortMessage(status, 0x64, lsbi)); 
		list.add(new ShortMessage(status, 0x65, msbi)); 
		list.add(new ShortMessage(status, 0x06, msb)); 
		if(!omitFine) list.add(new ShortMessage(status, 0x26, lsb));
		
		//Do I really need these?
		//list.add(new ShortMessage(status, 0x64, 0x7F)); 
		//list.add(new ShortMessage(status, 0x65, 0x7F)); 
		return list;
	}

	public List<MidiMessage> genPitchBendRangeSet(int channel, int semitones) throws InvalidMidiDataException
	{
		int val = (semitones & 0x7F) << 7;
		return genRPN(channel, RPN_PITCHBEND_RANGE, val, true);
	}
	
	public List<MidiMessage> genPitchBendRangeSet(int channel, int semitones, int cents) throws InvalidMidiDataException
	{
		int val = (semitones & 0x7F) << 7;
		val |= (cents & 0x7F);
		return genRPN(channel, RPN_PITCHBEND_RANGE, val, false);
	}
	
	public List<MidiMessage> genModDepthRangeSet(int channel, int coarse, int fine) throws InvalidMidiDataException
	{
		int val = (coarse & 0x7F) << 7;
		val |= (fine & 0x7F);
		return genRPN(channel, RPN_MOD_DEPTH_RANGE, val, false);
	}
	
	/*--- Meta Commands ---*/
	
	public MidiMessage genTextEvent(String text) throws InvalidMidiDataException
	{
		if(text == null || text.isEmpty()) return null;
		byte[] bytes = text.getBytes();
		int len = text.length();
		return new MetaMessage(0x01, bytes, len);
	}
	
	public MidiMessage genMarker(String text) throws InvalidMidiDataException
	{
		if(text == null || text.isEmpty()) text = "MARKER";
		byte[] bytes = text.getBytes();
		int len = text.length();
		return new MetaMessage(0x06, bytes, len);
	}
	
	public MidiMessage genTrackName(String text) throws InvalidMidiDataException
	{
		if(text == null || text.isEmpty()) return null;
		byte[] bytes = text.getBytes();
		int len = text.length();
		return new MetaMessage(0x03, bytes, len);
	}
	
	public MidiMessage genTrackEnd() throws InvalidMidiDataException
	{
		return new MetaMessage(0x2F, null, 0);
	}
	
	public MidiMessage genTempoSet(int bpm, double beatsPerQuarterNote) throws InvalidMidiDataException
	{
		//System.err.println("Tempo: Input - " + bpm + " bpm | Beats Per QN: " + beatsPerQuarterNote);
		int uspqn = MIDI.bpm2uspqn(bpm, beatsPerQuarterNote);
		return genTempoSet(uspqn);
	}
	
	public MidiMessage genTempoSet(int microseconds_per_qn) throws InvalidMidiDataException
	{
		//Clamp to 24 bits
		if(microseconds_per_qn < 0) microseconds_per_qn = 0;
		if(microseconds_per_qn > 0xFFFFFF) microseconds_per_qn = 0xFFFFFF;
		//Looks like it's stored BE
		byte[] tempo = new byte[3];
		tempo[0] = (byte)((microseconds_per_qn >>> 16) & 0xFF);
		tempo[1] = (byte)((microseconds_per_qn >>> 8) & 0xFF);
		tempo[2] = (byte)(microseconds_per_qn & 0xFF);
		return new MetaMessage(0x51, tempo, 3);
	}
	
	/*--- System Commands ---*/

}
