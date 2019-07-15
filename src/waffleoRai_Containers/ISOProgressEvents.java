package waffleoRai_Containers;

import java.util.Observable;

public class ISOProgressEvents {

	public static enum EventType
	{
		ISO_ZEROSECTORCONSTRUCTION,
		ISO_NUMSECSCALCULATED,
		ISO_SECTORREAD,
		TBL9660_BASECONSTRUCTION,
		TBL9660_ROOTDIRSECFOUND,
		TBL9660_PARSEDIRENTERED,
		TBL9660_ENTRYPARSESTART,
		TBL9660_ENTRYPARSEEND,
		TBL9660_PARSEDIREND,
		TBL9660_ENTRYIGNORED,
		TBL9660_UCSECFOUND,
		TBL9660_ROOTDIRPARSED,
		TBL9660_UCSECSHANDLED,
		IMG9660_TABLEGENERATED,
		IMG9660_VIRDIRPOPULATED,
		IMG9660_INFOREAD,
		IMG9660_TBLLISTED,
		IMG9660_FILEADDED;
	}
	
	public static class ISOReadEvent extends Observable
	{
		private int val;
		private EventType type;
		private String details;
		
		public ISOReadEvent()
		{
			this.val = -1;
			this.type = null;
			this.details = null;
		}
		
		public int getValue()
		{
			return this.val;
		}
		
		public EventType getType()
		{
			return this.type;
		}
	
		public String getDetails()
		{
			return this.details;
		}
		
		public void fireNewEvent(EventType t, int value)
		{
			this.fireNewEvent(t, value, null);
		}
		
		public void fireNewEvent(EventType t, int value, String details)
		{
			this.val = value;
			this.type = t;
			this.details = details;
			this.setChanged();
			this.notifyObservers();
		}
		
	}
	
	public static class TableReadEvent extends Observable
	{
		
	}
	
	public static class ImageReadEvent extends Observable
	{
		
	}
	
}
