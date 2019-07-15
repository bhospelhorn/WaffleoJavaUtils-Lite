package waffleoRai_Utils;

import java.util.HashMap;
import java.util.Map;

public enum ParserType {
	
	BINARY_UNKNOWN(0, "bin"),
	
	PNG_IMAGE(1, "png"),
	JPEG_IMAGE(2, "jpg"),
	BITMAP_IMAGE(3, "bmp"),
	
	WAV_SOUND(4, "wav"),
	OGG_SOUND(5, "ogg"),
	
	ISO_CD_IMAGE(6, "iso"),
	
	MIDI_SEQUENCE(7, "mid"),
	SOUNDFONT2(8, "sf2"),
	
	MS_EXECUTABLE(9, "exe"),
	MS_DLL(10, "dll"),
	
	TEXT_GENERIC(11, "txt"),
	XML_FILE(12, "xml"),
	
	RAW_DIRECTORY(13, ""),
	
	CHUNSOFT_SMD_SEQ(-2, "smd"),
	SONYPS_SEQP_SEQ(-3, "seq"),
	SONYPS_VAGP_SOUND(-4, "vag"),
	SONYPS_VABP_SOUNDBANK(-5, "vab"),
	SONYPS_VABP_HEADER(-6, "vh"),
	SONYPS_VABP_BODY(-7, "vb"),
	SONYPS_EXECUTABLE(-8, "psxexe"),
	SONYPS_CONFIG(-9, "psxcfg"),
	SONYPS_STREAM(-10, "psxstr"),
	SONYPS_CDAUDIO(-11, "psxda"),
	
	WINKYSOFTPS_ARCHIVE_UNK(-12, "winkyarcx"),
	WINKYSOFTPS_ARCHIVE_T1(-13, "winkyarcs"),
	WINKYSOFTPS_ARCHIVE_T2(-14, "winkyarcr"),
	
	;
	
	
	private int n;
	private String extension;
	
	private ParserType(int number, String ext)
	{
		n = number;
		extension = ext;
	}
	
	public int getNumber()
	{
		return n;
	}
	
	public String getExtension()
	{
		return this.extension;
	}

	
	private static Map<Integer, ParserType> intMap;
	
	public static ParserType getType(int n)
	{
		if (intMap == null)
		{
			intMap = new HashMap<Integer, ParserType>();
			ParserType[] all = ParserType.values();
			for (ParserType t : all) intMap.put(t.getNumber(), t);
		}
		return intMap.get(n);
	}

	public static void clearStaticMap()
	{
		intMap = null;
	}
	
}
