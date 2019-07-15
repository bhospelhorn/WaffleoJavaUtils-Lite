package waffleoRai_Image;

import java.awt.Image;

public class PictureAnimation implements Animation{
	
	private Picture[] frames;
	private int numFrames;
	
	public PictureAnimation(int numFrames)
	{
		this.numFrames = numFrames;
		this.frames = new Picture[numFrames];
	}
	
	public int getNumberFrames() 
	{
		return this.numFrames;
	}

	public Picture getFramePicture(int index) 
	{
		if (index < 0 || index >= this.numFrames) return null;
		return this.frames[index];
	}

	public void setNumberFrames(int newNFrames) 
	{
		Picture[] newFrames = new Picture[newNFrames];
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
		this.frames[index] = frame;
	}
	
	public void setFrame(Image frame, int index)
	{
		Picture p = new UncompressedRaster(frame);
		setFrame(p, index);
	}
	
	public Animation scale(double factor)
	{
		PictureAnimation a = new PictureAnimation(frames.length);
		for (int i = 0; i < frames.length; i++)
		{
			if (frames[i] != null) a.frames[i] = frames[i].scale(factor);
		}
		return a;
	}
	
	public Image getFrameImage(int index)
	{
		Picture f = this.getFramePicture(index);
		if (f == null) return null;
		return f.toImage();
	}

}
