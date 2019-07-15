package waffleoRai_SeqSound;

import javax.sound.midi.MidiEvent;

public class SortableMidiEvent implements Comparable<SortableMidiEvent>{

	private MidiEvent event;
	
	public SortableMidiEvent(MidiEvent e)
	{
		if (e == null) throw new IllegalArgumentException();
		event = e;
	}
	
	public MidiEvent getEvent()
	{
		return event;
	}
	
	public int hashCode()
	{
		if (event == null) return 0;
		return event.hashCode();
	}
	
	public boolean equals(Object o)
	{
		if (event == null) return false;
		return event.equals(o);
	}
	
	@Override
	public int compareTo(SortableMidiEvent o) 
	{
		if (o == null) return 1;
		//Compare time coordinates
		if (o.getEvent().getTick() > this.getEvent().getTick()) return -1;
		else if (o.getEvent().getTick() < this.getEvent().getTick()) return 1;
		
		//Sort by status
		int ostat = o.getEvent().getMessage().getStatus();
		int tstat = this.getEvent().getMessage().getStatus();
		return tstat - ostat;

	}

}
