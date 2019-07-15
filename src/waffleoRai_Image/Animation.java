package waffleoRai_Image;

import java.awt.Image;

public interface Animation {

	public int getNumberFrames();
	public Picture getFramePicture(int index);
	public Image getFrameImage(int index);
	public void setNumberFrames(int newNFrames);
	public void setFrame(Picture frame, int index);
	public void setFrame(Image frame, int index);
	public Animation scale(double factor);
	
}
