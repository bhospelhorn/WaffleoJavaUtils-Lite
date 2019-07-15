package waffleoRai_Image;

import java.awt.Image;

public class ImageAnimation implements Animation{

	private Image[] frames;
	private int numFrames;
	
	public ImageAnimation(int numFrames)
	{
		this.numFrames = numFrames;
		this.frames = new Image[numFrames];
	}
	
	public int getNumberFrames() 
	{
		return this.numFrames;
	}

	public Picture getFramePicture(int index) 
	{
		if (index < 0 || index >= this.numFrames) return null;
		Image i = this.frames[index];
		if (i == null) return null;
		return new UncompressedRaster(i);
	}

	public Image getFrameImage(int index)
	{
		if (index < 0 || index >= this.numFrames) return null;
		return frames[index];
	}
	
	public void setNumberFrames(int newNFrames) 
	{
		Image[] newFrames = new Image[newNFrames];
		for (int i = 0; i < this.numFrames; i++)
		{
			if (i >= newNFrames) break;
			newFrames[i] = this.frames[i];
		}
		this.numFrames = newNFrames;
		this.frames = newFrames;
	}
	
	public void setFrame(Picture frame, int index) 
	{
		if (index < 0 || index >= this.numFrames) return;
		if (frame != null) this.frames[index] = frame.toImage();
	}
	
	public void setFrame(Image frame, int index)
	{
		if (index < 0 || index >= this.numFrames) return;
		this.frames[index] = frame;
	}
	
	public Animation scale(double factor)
	{
		ImageAnimation a = new ImageAnimation(frames.length);
		for (int i = 0; i < frames.length; i++)
		{
			if (frames[i] != null)
			{
				Image img = frames[i];
				int w = img.getWidth(null);
				int h = img.getHeight(null);
				int scW = (int)Math.round(factor * (double)w);
				int scH = (int)Math.round(factor * (double)h);
				a.frames[i] = img.getScaledInstance(scW, scH, Image.SCALE_SMOOTH);
			}
		}
		return a;
	}

}
