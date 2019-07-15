package waffleoRai_soundbank;

public abstract class ModSource {
	
	private ModController controller;
	private boolean continuous;
	private boolean direction;
	private boolean polarity;
	private ModType type;
	
	public ModController getController() {return controller;}
	public boolean isContinuous(){return continuous;}
	public boolean getDirection(){return direction;}
	public boolean getPolarity(){return polarity;}
	public ModType getType(){return type;}
	
	public void setController(ModController mc) {controller = mc;}
	public void setContinuous(boolean b) {continuous = b;}
	public void setDirection(boolean b) {direction = b;}
	public void setPolarity(boolean b) {polarity = b;}
	public void setType(ModType mt) {type = mt;}

}
