package waffleoRai_Compression.huffman;

import java.nio.file.Paths;

import waffleoRai_Utils.FileBuffer;

public class HuffTest {

	public static void main(String[] args) {
		
		String in_path = "C:\\Users\\Blythe\\Documents\\Desktop\\Notes\\brsar\\final_sound_data.sdat";
		String comp_path_1 = "C:\\Users\\Blythe\\Documents\\Desktop\\hufftest_comp_stream.bin";
		//String decomp_path_1 = "C:\\Users\\Blythe\\Documents\\Desktop\\hufftest_decomp_stream.bin";
		String comp_path_2 = "C:\\Users\\Blythe\\Documents\\Desktop\\hufftest_comp_mem.bin";
		//String decomp_path_2 = "C:\\Users\\Blythe\\Documents\\Desktop\\hufftest_decomp_mem.bin";
		
		try
		{
			long insz = FileBuffer.fileSize(in_path);
			
			Huffman.HuffEncodeFileStream(Paths.get(in_path), Paths.get(comp_path_1), 8, 0, insz, "test");
			Huffman.HuffEncodeFile(FileBuffer.createBuffer(in_path), 8, "test").writeFile(comp_path_2);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

	}

}
