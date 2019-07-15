package waffleoRai_soundbank.sf2;

import waffleoRai_Sound.WAV;
import waffleoRai_soundbank.SimpleBank;
import waffleoRai_soundbank.SimpleInstrument;
import waffleoRai_soundbank.SimplePreset;
import waffleoRai_soundbank.SingleBank;

public class SF2Test {

	public static void main(String[] args) 
	{
		String soundPath = "C:\\Users\\Blythe\\Documents\\Desktop\\Notes\\brsar\\brwar_test\\001.wav";
		String outPath = "C:\\Users\\Blythe\\Documents\\Desktop\\Notes\\brsar\\test.sf2";
		
		String title = "sf2test";
		String tool = "sf2test";
		String soundKey = "sound0";
		
		//Load Sound
		try
		{
			WAV wav = new WAV(soundPath);
		
			//Try creating a sound bank
			SimpleBank bank = new SimpleBank(title, "1.0", "vendor", 1);
			bank.addSample(soundKey, wav);
			
			int bi = bank.newBank(0, "Bank1");
			SingleBank mybank = bank.getBank(bi);
			int pi = mybank.newPreset(0, "Preset", 1);
			SimplePreset p = mybank.getPreset(pi);
			int ii = p.newInstrument("Instrument", 1);
			SimpleInstrument i = p.getRegion(ii).getInstrument();
			i.newRegion(soundKey);
			//InstRegion r = i.getRegion(ri);
			
			SF2 font = SF2.createSF2(bank, tool, false);
			font.printInfo();
			font.writeSF2(outPath);
			
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

	}

}
