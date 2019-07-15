package waffleoRai_Image;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of methods for scaling drawing objects in the waffleoRai_Image package.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since May 13, 2018
 *
 */
public class Scale {
	
	/**
	 * Scale a list of n Pixels to a target number of Pixels (adjusting the resolution), either
	 * by blurring pixels or by removing or duplicating existing pixels.
	 * <br><br><b>CAUTION:</b> The Pixels this function outputs ARE NOT REMAPPED. This function was designed for
	 * texture mapping, and as a result the output lists are just lists of Pixels.
	 * If you try to draw these as lines, you may get unexpected results. 
	 * @param in Input array/list of Pixels.
	 * @param targetPixelCount Desired resolution/pixel count of output.
	 * @param blur Whether to use the blur algorithm (true) or simply remove or duplicate pixels (false) to get
	 * the desired resolution.
	 * @return A List of Pixels rescaled as requested.
	 */
	public static List<Pixel> scale_1D(List<Pixel> in, int targetPixelCount, boolean blur)
	{
		if (blur) return scale_1D_blur(in, targetPixelCount);
		else return scale_1D_noblur(in, targetPixelCount);
	}
	
	/**
	 * The function that generates a blurred rescale of a list of Pixels. Instead
	 * of treated Pixels as discrete units that can only be removed or duplicated, it attempts
	 * to create an array of entirely new Pixels by combining the existing Pixels in certain proportions.
	 * I have no idea if it's any good.
	 * @param in Input Pixel List
	 * @param targetPixelCount Target resolution (number of pixels desired in output)
	 * @return List of target size containing newly generated Pixels derived from input list.
	 */
	private static List<Pixel> scale_1D_blur(List<Pixel> in, int targetPixelCount)
	{
		if (in == null || in.isEmpty()) return null;
		int cSz = in.size();
		int tSz = targetPixelCount;
		
		List<Pixel> newpix = new ArrayList<Pixel>(tSz);
		
		if (cSz == tSz) newpix.addAll(in);
		else
		{
			int[][] pixmakeup = new int[tSz][cSz];
			int counter = 0;
			//Determine which pixels will make up new pixels
			for (int i = 0; i < tSz; i++)
			{
				for (int j = 0; j < cSz; j++)
				{
					//Count cSz partial pix units of size 1/tSz
					pixmakeup[i][counter/tSz]++;
					counter++;
				}
			}
			//Generate new pixels
			for (int i = 0; i < tSz; i++)
			{
				Pixel np = null;
				for (int j = 0; j < cSz; j++)
				{
					//There are cSz ppix units per new pix
					if (pixmakeup[i][j] > 0)
					{
						Pixel sp = in.get(j).multiply((double)pixmakeup[i][j]/(double)cSz);
						if (np == null) np = sp;
						else np = np.add(sp);
					}
				}
				newpix.add(np);
			}
		}
		
		return newpix;
	}
	
	private static List<Pixel> scale_1D_noblur(List<Pixel> in, int targetPixelCount)
	{
		if (in == null || in.isEmpty()) return null;
		int cSz = in.size();
		int tSz = targetPixelCount;
		List<Pixel> newpix = new ArrayList<Pixel>(tSz);
		
		if (cSz == tSz) newpix.addAll(in);
		else if(cSz > tSz)
		{
			//Delete some pixels
			int q = cSz/tSz; //Unit size - one pix per unit is kept
			//In odd size units, it is the middle one
			//In even size units, it is the middle left
			int r = cSz%tSz; //# of additional random pixels to delete
			//Delete some random pixels to get down to even division
			List<Pixel> plist = new ArrayList<Pixel>(cSz);
			if (r != 0)
			{
				int skipped = 0;
				int div = cSz/(r+1);
				for (int i = 0; i < cSz; i++)
				{
					if (skipped < r)
					{
						if ((i % div == 0) && (i != 0)) skipped++;
						else plist.add(in.get(i));
					}
					else plist.add(in.get(i));
				}
			}
			else plist.addAll(in);
		
			int counter = 0;
			int uind = q/2;
			if (q%2 == 0) uind--;
			
			//Pull one pixel from each unit
			while (counter < cSz)
			{
				newpix.add(plist.get(counter + uind));
				counter += q;
			}
		}
		else if (cSz < tSz)
		{
			//Duplicate some pixels
			int q = tSz/cSz; //Unit size - a single pixel is duplicated to create this many pixels
			int r = tSz%cSz; //# of additional random pixels to duplicate
			//Expand pixels to unit sizes
			List<Pixel> plist = new ArrayList<Pixel>(tSz);
			for (Pixel p : in)
			{
				for (int i = 0; i < q; i++) plist.add(p);
			}
			
			//Duplicate some pixels to fill out
			if (r != 0)
			{
				int duped = 0;
				int lSz = tSz - r;
				int div = lSz/(r+1);
				for (int i = 0; i < lSz; i++)
				{
					newpix.add(plist.get(i));
					if (duped < r)
					{
						if ((i%div == 0) && (i!=0)) {
							newpix.add(plist.get(i));
							duped++;
						}
					}
				}
			}
			else newpix.addAll(plist);
		}
		
		return newpix;
	}

	/**
	 * A static internal class for use by static Scale methods working with Line objects.
	 * Functions as a bookkeeping structure more than anything.
	 * @author Blythe Hospelhorn
	 * @version 1.0.0
	 * @since May 13, 2018
	 */
	private static class LineRec
	{
		//public int count;
		
		/**
		 * Line linked to the record.
		 */
		public Line line;
		
		/**
		 * Proportion of Line to contribute to a new Line.
		 */
		public double prop;
		
		/**
		 * The Pixel List of this Line rescaled to a new resolution.
		 */
		public List<Pixel> scaled;
		
		/**
		 * Create a LineRec (Line Record) linked to an existing line.
		 * @param l Line to link this record to.
		 */
		public LineRec(Line l)
		{
			//count = 0;
			line = l;
			prop = 0.0;
			scaled = null;
		}
	}
	
	/**
	 * Scale a list of n Lines to a target number of lines, either by blurring lines together into new lines
	 * or by removing or duplicating existing lines.
	 * <br><br><b>CAUTION:</b> The Lines this function outputs ARE NOT REMAPPED. This function was designed for
	 * texture mapping, and as a result the Line objects serve more as Pixels lists with fancy methods
	 * attached. If you try to draw these lines, you may get unexpected results. 
	 * <br>To use this method as part of a resizer, you must remap the the pixels in this output.
	 * @param in Input array/list of Lines.
	 * @param targetRowCount Number of Lines desired in resized set.
	 * @param blur Whether to use the blur algorithm (true) or simply remove or duplicate lines (false) to get
	 * the desired Line count.
	 * @return An ArrayList of length targetRowCount containing recalculated Lines. The pixels within
	 * these Line objects are not mapped, and as a result, attempting to render the Lines may not
	 * yield true lines.
	 */
	public static List<Line> scale_2D(List<Line> in, int targetRowCount, boolean blur)
	{
		if (blur) return scale_2D_blur(in, targetRowCount);
		else return scale_2D_noblur(in, targetRowCount);
	}
	
	private static List<Line> scale_2D_blur(List<Line> in, int targetRowCount)
	{
		int osz = in.size();
		int nsz = targetRowCount;
		ArrayList<Line> list = new ArrayList<Line>(nsz);
		
		if (osz > nsz)
		{
			//Scale down
			//Calculate component rows and ratios
			int[][] compCount = new int[nsz][osz];
			int c = 0;
			for (int i = 0; i < nsz; i++)
			{
				for (int j = 0; j < osz; j++)
				{
					compCount[i][c/nsz]++;
					c++;
				}
			}
			
			//Calculate row lengths and combine pixels
			for (int i = 0; i < nsz; i++)
			{
				//Row length
				int len = 0;
				List<LineRec> complines = new LinkedList<LineRec>();
				for (int j = 0; j < osz; j++)
				{
					int parts = compCount[i][j];
					if (parts > 0)
					{
						LineRec rec = new LineRec(list.get(j));
						//rec.count = parts;
						double prop = (double)parts/(double)osz;
						rec.prop = prop;
						len += (int)(Math.round(prop * (double)rec.line.getPixelCount()));
						complines.add(rec);
					}
				}
				
				//Scale rows of interest
				for (LineRec l : complines) l.scaled = l.line.scale_1D(len, true);
				
				//Calculate pixels
				//***IT SAYS it's a MappedPixel, BUT THESE PIXELS ARE NOT MAPPED!!!
				ArrayList<MappedPixel> lpix = new ArrayList<MappedPixel>(len);
				for (int k = 0; k < len; k++)
				{
					MappedPixel mp = new MappedPixel(0,0,0,0);
					for (LineRec l : complines)
					{
						int r = (int)Math.round(l.prop * (double)l.scaled.get(k).getRed()) + mp.getRed();
						int g = (int)Math.round(l.prop * (double)l.scaled.get(k).getGreen()) + mp.getGreen();
						int b = (int)Math.round(l.prop * (double)l.scaled.get(k).getBlue()) + mp.getBlue();
						int a = (int)Math.round(l.prop * (double)l.scaled.get(k).getAlpha()) + mp.getAlpha();
						mp.setRed(r);
						mp.setGreen(g);
						mp.setBlue(b);
						mp.setAlpha(a);
					}					
					lpix.add(mp);
				}

				//Generate output row object & add to list
				list.add(new Line(lpix));
		
			}
			
			
		}
		else if (nsz > osz)
		{
			//Scale up
			//Calculate component rows and ratios
			int[][] compCount = new int[nsz][osz];
			int c = 0;
			for (int i = 0; i < nsz; i++)
			{
				for (int j = 0; j < nsz; j++)
				{
					compCount[i][c/osz]++;
					c++;
				}
			}
			
			//Calculate row lengths and combine pixels
			for (int i = 0; i < nsz; i++)
			{
				//Row length
				int len = 0;
				List<LineRec> complines = new LinkedList<LineRec>();
				for (int j = 0; j < osz; j++)
				{
					int parts = compCount[i][j];
					if (parts > 0)
					{
						LineRec rec = new LineRec(list.get(j));
						//rec.count = parts;
						double prop = (double)parts/(double)nsz;
						rec.prop = prop;
						len += (int)(Math.round(prop * (double)rec.line.getPixelCount()));
						complines.add(rec);
					}
				}
				
				//Scale rows of interest
				//for (LineRec l : complines) l.line = l.line.scale(len, true);
				for (LineRec l : complines) l.scaled = l.line.scale_1D(len, true);
				
				//Calculate pixels
				//***IT SAYS it's a MappedPixel, BUT THESE PIXELS ARE NOT MAPPED!!!
				ArrayList<MappedPixel> lpix = new ArrayList<MappedPixel>(len);
				for (int k = 0; k < len; k++)
				{
					MappedPixel mp = new MappedPixel(0,0,0,0);
					for (LineRec l : complines)
					{
						int r = (int)Math.round(l.prop * (double)l.scaled.get(k).getRed()) + mp.getRed();
						int g = (int)Math.round(l.prop * (double)l.scaled.get(k).getGreen()) + mp.getGreen();
						int b = (int)Math.round(l.prop * (double)l.scaled.get(k).getBlue()) + mp.getBlue();
						int a = (int)Math.round(l.prop * (double)l.scaled.get(k).getAlpha()) + mp.getAlpha();
						mp.setRed(r);
						mp.setGreen(g);
						mp.setBlue(b);
						mp.setAlpha(a);
					}					
					lpix.add(mp);
				}

				//Generate output row object & add to list
				list.add(new Line(lpix));
		
			}
			
		}
		else list.addAll(in);
		
		return list;
	}
	
	private static List<Line> scale_2D_noblur(List<Line> in, int targetRowCount)
	{
		//Merely remove or duplicate rows at random
		int osz = in.size();
		int nsz = targetRowCount;
		ArrayList<Line> list = new ArrayList<Line>(nsz);
		
		if (osz > nsz)
		{
			//Scale down (delete rows)
			int q = osz/nsz; //Unit size - one row per unit is kept
			//In odd size units, it is the middle one
			//In even size units, it is the middle "top"
			int r = osz%nsz; //# of additional random rows to delete
			
			//Delete some random rows to get down to even division
			List<Line> init = new ArrayList<Line>(osz);
			if (r != 0)
			{
				int skipped = 0;
				int div = osz/(r+1);
				for (int i = 0; i < osz; i++)
				{
					if (skipped < r)
					{
						if ((i % div == 0) && (i != 0)) skipped++;
						else init.add(in.get(i));
					}
					else init.add(in.get(i));
				}
			}
			else init.addAll(in);
			
			//Pull one row from each unit
			int counter = 0;
			int uind = q/2;
			if (q%2 == 0) uind--;
			while (counter < osz)
			{
				list.add(init.get(counter + uind));
				counter += q;
			}
			
		}
		else if (nsz > osz)
		{
			//Scale up (duplicate rows)
			int q = nsz/osz; //Unit size - a single row is duplicated to create this many rows
			int r = nsz%osz; //# of additional random rows to duplicate
			
			//Expand rows to unit sizes
			List<Line> init = new ArrayList<Line>(nsz);
			for (Line l : in)
			{
				for (int i = 0; i < q; i++) init.add(l);
			}
			
			//Duplicate some pixels to fill out
			if (r != 0)
			{
				int duped = 0;
				int lSz = nsz - r;
				int div = lSz/(r+1);
				for (int i = 0; i < lSz; i++)
				{
					list.add(init.get(i));
					if (duped < r)
					{
						if ((i%div == 0) && (i!=0)) {
							list.add(init.get(i));
							duped++;
						}
					}
				}
			}
			else list.addAll(init);
		}
		else list.addAll(in);
		
		return list;
	}
	
}
