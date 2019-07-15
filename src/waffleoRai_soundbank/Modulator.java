package waffleoRai_soundbank;

public interface Modulator {
	
	public ModSource getSource();
	public ModSource getSourceAmount();
	public int getAmount();
	public GeneratorType getDestination();
	public TransformType getTransform();
	
}
