package waffleoRai_Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PathManager {
	
	private String memoPath;

	private HashMap<String,String> pathMap;
	private List<String> keyOrder;
	
	public PathManager(String listPath)
	{
		memoPath = listPath;
		pathMap = new HashMap<String, String>();
		keyOrder = new LinkedList<String>();
	}
	
	public void addPath(String key)
	{
		pathMap.put(key, null);
		keyOrder.add(key);
	}
	
	public void setPath(String key, String path)
	{
		if (!pathMap.containsKey(key)) return;
		pathMap.put(key, path);
	}
	
	public String getPath(String key)
	{
		return pathMap.get(key);
	}
	
	public void readFile() throws IOException
	{
		FileReader freader = new FileReader(memoPath);
		BufferedReader breader = new BufferedReader(freader);
		
		for (String k : keyOrder)
		{
			String line = breader.readLine();
			if (line == null) break;
			if (line.equals("null")) pathMap.put(k, null);
			else pathMap.put(k, line);
		}
		
		breader.close();
		freader.close();
	}
	
	public void writeFile() throws IOException
	{
		FileWriter fwriter = new FileWriter(memoPath);
		BufferedWriter bwriter = new BufferedWriter(fwriter);
		
		for (String k : keyOrder)
		{
			String line = pathMap.get(k);
			if (line == null) line = "null";
			bwriter.write(line);
		}
		
		bwriter.close();
		fwriter.close();
	}

}
