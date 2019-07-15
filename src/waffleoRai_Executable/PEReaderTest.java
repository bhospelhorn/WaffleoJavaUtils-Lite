package waffleoRai_Executable;

import java.io.IOException;

import waffleoRai_Executable.winpe.ExportTable;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class PEReaderTest {

	public static void main(String[] args) {
		
		String testdll = "C:\\Users\\Blythe\\Documents\\Sims Modding Workspace\\bin\\CAW\\Sims3Common.dll";
		
		try 
		{
			Win32PE exe = new Win32PE(testdll, true);
			System.out.println("Now attempting to virtualize...");
			Winexe vexe = exe.readdress();
			vexe.printExportTableSTDOUT();
			System.out.println("Reprinting table ordered by RVA...");
			ExportTable.setSortByRVA(true);
			vexe.printExportTableSTDOUT();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (UnsupportedFileTypeException e) 
		{
			e.printStackTrace();
		}
		
		System.out.println("All done :)");

	}

}
