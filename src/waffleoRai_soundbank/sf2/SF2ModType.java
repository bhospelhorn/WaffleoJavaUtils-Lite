package waffleoRai_soundbank.sf2;

public class SF2ModType {
	
	private SF2ModController controller;
	private boolean continuousCtrlr;
	private boolean direction;
	private boolean polarity;
	private SF2ModSourceType sourceType;
	
	public SF2ModType()
	{
		controller = SF2ModController.NO_CONTROLLER;
		continuousCtrlr = false;
		direction = false;
		polarity = false;
		sourceType = SF2ModSourceType.LINEAR;
	}
	
	public SF2ModType(short raw)
	{
		int iraw = Short.toUnsignedInt(raw);
		int idx = iraw & 0x7F;
		int type = (iraw >>> 10) & 0x3F;
		continuousCtrlr = (iraw & 0x0080) != 0;
		direction = (iraw & 0x0100) != 0;
		polarity = (iraw & 0x0200) != 0;
		controller = SF2ModController.getController(idx);
		if (controller == null) controller = SF2ModController.NO_CONTROLLER;
		sourceType = SF2ModSourceType.getSourceType(type);
		if (sourceType == null) sourceType = SF2ModSourceType.LINEAR;
	}
	
	public SF2ModController getController()
	{
		return controller;
	}
	
	public boolean getContinuousController()
	{
		return continuousCtrlr;
	}
	
	public boolean getDirection()
	{
		return direction;
	}
	
	public boolean getPolarity()
	{
		return polarity;
	}
	
	public SF2ModSourceType getSourceType()
	{
		return sourceType;
	}
	
	public void setController(SF2ModController c)
	{
		if (c == null) return;
		controller = c;
	}
	
	public void setContinuousController(boolean b)
	{
		continuousCtrlr = b;
	}
	
	public void setDirection(boolean b)
	{
		direction = b;
	}
	
	public void setPolarity(boolean b)
	{
		polarity = b;
	}
	
	public void setSourceType(SF2ModSourceType t)
	{
		if (t == null) return;
		sourceType = t;
	}
	
	public short serializeMe()
	{
		int iraw = controller.getValue() & 0x7F;
		iraw |= ((sourceType.getValue() & 0x3F) << 10);
		if (continuousCtrlr) iraw |= 0x0080;
		if (direction) iraw |= 0x0100;
		if (polarity) iraw |= 0x0200;
		
		return (short)iraw;
	}

}
