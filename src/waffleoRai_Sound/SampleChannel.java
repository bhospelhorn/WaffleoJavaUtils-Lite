package waffleoRai_Sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SampleChannel implements Iterable<Integer>{
	
	private List<Integer> samples;
	
	private SampleChannel(){}
	
	public SampleChannel(SampleChannel src)
	{
		samples = new ArrayList<Integer>(src.samples.size() + 2);
		samples.addAll(src.samples);
	}
	
	public static SampleChannel createArrayChannel(int initSize)
	{
		SampleChannel c = new SampleChannel();
		c.samples = new ArrayList<Integer>(initSize+1);
		return c;
	}
	
	public static SampleChannel createLinkedChannel()
	{
		SampleChannel c = new SampleChannel();
		c.samples = new LinkedList<Integer>();
		return c;
	}
	
	@Override
	public Iterator<Integer> iterator() 
	{
		return samples.iterator();
	}

	public void addSample(int sample){samples.add(sample);}
	public void clearSamples(){samples.clear();}
	public int countSamples(){return samples.size();}
	public int getSample(int index){return samples.get(index);}
	
	public int[] toArray()
	{
		int[] arr = new int[samples.size()];
		int i = 0;
		for(Integer s : samples) {arr[i] = s; i++;}
		return arr;
	}
	
}
