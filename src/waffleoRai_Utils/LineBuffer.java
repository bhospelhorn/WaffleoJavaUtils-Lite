package waffleoRai_Utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Deprecated
public class LineBuffer {

	private FileBuffer source;
	private long filesize;
	private Deque<String> buffer; 
	
	private int buffersize; //Default: 1024
	private boolean open;
	private boolean busy;
	
	private long cPos;
	private int fLine;
	//private long eLine;
	private Map<Integer, Long> lineCoordinates;
	
	private boolean nonASCII;
	private String encoding;
	
	public LineBuffer(FileBuffer myFile)
	{
		if (myFile == null) throw new IllegalArgumentException();
		constructorCore(myFile, 1024);
	}
	
	public LineBuffer(FileBuffer myFile, int bufferedLines)
	{
		if (bufferedLines <= 0) throw new IllegalArgumentException();
		if (myFile == null) throw new IllegalArgumentException();
		constructorCore(myFile, bufferedLines);
	}
	
	private void constructorCore(FileBuffer myFile, int bufferedLines)
	{
		source = myFile;
		filesize = myFile.getFileSize();
		buffer = new LinkedList<String>();
		buffersize = bufferedLines;
		open = false;
		busy = false;
		cPos = 0;
		//cLine = 1;
		//pPos = 0;
		fLine = 0;
		//eLine = 0;
		nonASCII = false;
		encoding = null;
		lineCoordinates = new HashMap<Integer, Long>();
	}
	
	private void fillBuffer()
	{
		busy = true;
		while (buffer.size() < buffersize && cPos < filesize)
		{
			int eline = fLine + buffer.size();
			lineCoordinates.put(eline, cPos);
			String line = getLineAtCPOS();
			if (line == null) break;
		}
		busy = false;
	}
	
	private void pushBuffer(int lines)
	{
		busy = true;
		if (lines < 0) return;
		int newFront = fLine - lines;
		if (newFront < 0) newFront = 0;
		for (int i = fLine - 1; i >= newFront; i--)
		{
			buffer.removeLast();
			jumpReaderToLine(i);
			buffer.push(getLineAtCPOS());
		}
		fLine = newFront;
		int eLine = fLine + buffer.size();
		jumpReaderToLine(eLine);
		busy = false;
	}
	
	private String getLineAtCPOS()
	{
		String line = null;
		if (!nonASCII) line = source.getASCII_string(cPos, '\n');
		else line = source.readEncoded_string(encoding, cPos, "\n");
		if (line == null) return null;
		buffer.addLast(line);
		cPos += line.length() + 1;
		return line;
	}
	
	public String nextLine()
	{
		//Does NOT refill the buffer! This should be handled in the background.
		if (open)
		{
			//Waits for buffer filler if buffer is empty and isn't at end of file.
			while (buffer.isEmpty())
			{
				if (cPos >= filesize) return null;
			}
		}
		String line = buffer.pop();
		fLine++;
		return line;
	}
	
	private void jumpReaderToLine(int line)
	{
		if (line < 0) return;
		Long l = lineCoordinates.get(line);
		if (l == null) return;
		cPos = l;
	}
	
	public void rewind()
	{
		pushBuffer(fLine);
	}
	
	public void rewind(int lines)
	{
		pushBuffer(lines);
	}
	
	public void skip(int lines)
	{
		for (int i = 0; i < lines; i++) nextLine();
	}
	
	public void open()
	{
		open = true;
		Thread refiller = new Thread(){
			public void run()
			{
				while(open)
				{
					if (!busy)
					{
						if (buffer.size() < buffersize) fillBuffer();	
					}
				}
			}
		};
		refiller.start();
	}
	
	public void close()
	{
		open = false;
		cPos = 0;
		fLine = 0;
		buffer.clear();
	}
	
	
}
