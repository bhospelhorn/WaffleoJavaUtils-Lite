package waffleoRai_Containers;

import java.io.IOException;

import waffleoRai_Containers.CDTable.CDInvalidRecordException;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class TestCases {
	
	public static String standardImage = "C:\\Users\\Blythe\\Documents\\Desktop\\Dogz 4.iso";
	public static String sectorDataOnlyImage = "C:\\Users\\Blythe\\Documents\\Desktop\\MathJourneyTrimmed.iso";
	public static String XAImage = "C:\\Users\\Blythe\\Documents\\Game Stuff\\PSX\\Games\\Track 01.iso";
	public static String largeImage = "C:\\Users\\Blythe\\Documents\\Desktop\\Fedora-Workstation-Live-x86_64-26-1.5.iso";

	public static void testStandardImage()
	{
		System.out.println("================== TEST 1 ==================");
		System.out.println("Standard Image Path: " + standardImage);
		try 
		{
			FileBuffer myFile = FileBuffer.createBuffer(standardImage);
			System.out.println("File has been read into memory...");
			System.out.println("\tBuffer type: " + myFile.typeString());
			System.out.println("\tBuffer size: " + myFile.getFileSize());
			ISO myISO = new ISO(myFile, false);
			System.out.println("Initial ISO parse complete!");
			ISO9660Image myImage = new ISO9660Image(myISO);
			myImage.printMe();
		} 
		catch (IOException e) {
			System.out.println("Uh oh! IOException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (CDInvalidRecordException e)
		{
			System.out.println("Uh oh! CDInvalidRecordException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (UnsupportedFileTypeException e)
		{
			System.out.println("Uh oh! UnsupportedFileTypeException! [testStandardImage()]");
			e.printStackTrace();
		}
	}
	
	public static void testHeaderlessImage()
	{
		System.out.println("================== TEST 2 ==================");
		System.out.println("Standard Image Path: " + sectorDataOnlyImage);
		try 
		{
			FileBuffer myFile = FileBuffer.createBuffer(sectorDataOnlyImage);
			System.out.println("File has been read into memory...");
			System.out.println("\tBuffer type: " + myFile.typeString());
			System.out.println("\tBuffer size: " + myFile.getFileSize());
			ISO myISO = new ISO(myFile, false);
			System.out.println("Initial ISO parse complete!");
			ISO9660Image myImage = new ISO9660Image(myISO);
			myImage.printMe();
		} 
		catch (IOException e) {
			System.out.println("Uh oh! IOException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (CDInvalidRecordException e)
		{
			System.out.println("Uh oh! CDInvalidRecordException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (UnsupportedFileTypeException e)
		{
			System.out.println("Uh oh! UnsupportedFileTypeException! [testStandardImage()]");
			e.printStackTrace();
		}
	}
	
	public static void testXAImage()
	{
		System.out.println("================== TEST 3.1 ==================");
		System.out.println("Standard Image Path: " + XAImage);
		try 
		{
			FileBuffer myFile = FileBuffer.createBuffer(XAImage);
			System.out.println("File has been read into memory...");
			System.out.println("\tBuffer type: " + myFile.typeString());
			System.out.println("\tBuffer size: " + myFile.getFileSize());
			ISO myISO = new ISO(myFile, false);
			System.out.println("Initial ISO parse complete!");
			ISO9660Image myImage = new ISO9660Image(myISO);
			myImage.printMe();
			
			System.out.println("================== TEST 3.2 ==================");
			ISOXAImage myXA = new ISOXAImage(myISO);
			myXA.printMe();
		} 
		catch (IOException e) {
			System.out.println("Uh oh! IOException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (CDInvalidRecordException e)
		{
			System.out.println("Uh oh! CDInvalidRecordException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (UnsupportedFileTypeException e)
		{
			System.out.println("Uh oh! UnsupportedFileTypeException! [testStandardImage()]");
			e.printStackTrace();
		}
		
	}
	
	public static void testLargeImage()
	{
		System.out.println("================== TEST 4 ==================");
		System.out.println("Large Image Path: " + largeImage);
		try 
		{
			FileBuffer myFile = FileBuffer.createBuffer(largeImage);
			System.out.println("File has been read into memory...");
			System.out.println("\tBuffer type: " + myFile.typeString());
			System.out.println("\tBuffer size: " + myFile.getFileSize());
			ISO myISO = new ISO(myFile, false);
			System.out.println("Initial ISO parse complete!");
			ISO9660Image myImage = new ISO9660Image(myISO);
			myImage.printMe();
		} 
		catch (IOException e) {
			System.out.println("Uh oh! IOException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (CDInvalidRecordException e)
		{
			System.out.println("Uh oh! CDInvalidRecordException! [testStandardImage()]");
			e.printStackTrace();
		}
		catch (UnsupportedFileTypeException e)
		{
			System.out.println("Uh oh! UnsupportedFileTypeException! [testStandardImage()]");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		//testStandardImage();
		System.out.println("Standard image test complete!");
		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");
		System.out.println();
		
		//testHeaderlessImage();
		System.out.println("Headerless image test complete!");
		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");
		System.out.println();
		
	//	testXAImage();
		System.out.println("XA image test complete!");
		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");
		System.out.println();
		
		testLargeImage();
		System.out.println("Large image test complete!");
		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");
		System.out.println();

	}

}
