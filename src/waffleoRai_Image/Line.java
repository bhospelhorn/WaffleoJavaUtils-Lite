package waffleoRai_Image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*
 * UPDATES
 * 
 * 1.0.2 -> 1.0.3 | May 16, 2018
 * 	Added functions for hard trimming 
 * 
 */

/**
 * A container for a line (one-dimensional array of pixels) mapped in two dimensions.
 * This object may be used for rendering lines on a two-dimensional image.
 * <br>It's likely more efficient to use the Java awt library, but this class also exists
 * in the event customization is needed.
 * @author Blythe Hospelhorn
 * @version 1.0.3
 * @since May 16, 2018
 *
 */
public class Line {
	
	/* ----- Constants ----- */
	
	/**
	 * Line orientation enum - Line neither is oriented predominately in the X or Y direction.
	 */
	public static final int NO_MAJOR = 0;
	
	/**
	 * Line orientation enum - Line is oriented predominately in the X direction (primarily horizontal).
	 */
	public static final int X_MAJOR = 1;
	
	/**
	 * Line orientation enum - Line is oriented predominately in the Y direction (primarily vertical).
	 */
	public static final int Y_MAJOR = 2;
	
	/* ----- Instance Variables ----- */
	
	private ArrayList<MappedPixel> pixels;
	private int dir;
	
	private int xOffset;
	private int yOffset;
	
	/* ----- Construction ----- */
	
	/**
	 * Package/family locked constructor for an empty line. Sets the pixel array to null.
	 * <br>Downstream methods with no null check will throw exceptions if the pixel array is never set!
	 */
	protected Line()
	{
		pixels = null;
		dir = -1;
		xOffset = 0;
		yOffset = 0;
	}
	
	/**
	 * Package/family locked constructor for creating a line linked to an existing pixel array.
	 * The reason this is locked is to prevent generation of a line that is linked internally to
	 * another line without being able to keep track of that link. Modification of this
	 * arraylist will affect any other lines using the same list reference!
	 * @param parr ArrayList of pixels to link to this line during construction.
	 */
	protected Line(ArrayList<MappedPixel> parr)
	{
		pixels = parr;
		dir = -1;
		xOffset = 0;
		yOffset = 0;
	}
	
	/**
	 * Construct a line from two known vertices.
	 * <br>Line will be automatically generated and mapped during construction.
	 * <br>Initial offset of the line is determined by the lower x and y values of the input vertices.
	 * <br>The line's internal coordinates are relative to the line only - 0,0 being the top-left
	 * corner of the rectangle containing this line.
	 * @param x1 X coordinate of the first(start) vertex.
	 * @param y1 Y coordinate of the first(start) vertex.
	 * @param x2 X coordinate of the second(end) vertex.
	 * @param y2 Y coordinate of the second(end) vertex.
	 */
	public Line(int x1, int y1, int x2, int y2)
	{
		dir = -1;
		int xdiff = x2-x1;
		int ydiff = y2-y1;
		boolean xflip = false;
		boolean yflip = false;
		if (xdiff < 0) xflip = true;
		if (ydiff < 0) yflip = true;
		int xlen = Math.abs(xdiff) + 1; //Don't chop off end vertex!
		int ylen = Math.abs(ydiff) + 1;
		if (xflip) xOffset = x2;
		else xOffset = x1;
		if (yflip) yOffset = y2;
		else yOffset = y1;
		if (xlen == ylen) drawLine_eqXY(xlen, xflip, yflip);
		else drawLine(xlen, ylen, xflip, yflip);
	}
	
	/**
	 * Internal construction rendering function. This function is called if the line is
	 * equally long in the X and Y directions. The line is rendered by generating an ArrayList of
	 * pixels mapped to coordinates relative to the line's top left "corner" (the lower of the x and y
	 * coordinates of the vertices, separately).
	 * <br>The length (in pixels) of the line and the orientation must be determined by the main constructor
	 * before calling this method.
	 * @param len Length (in pixels) of the line to be generated.
	 * @param xflip Whether the direction of this line is flipped in the X direction, that is, whether
	 * it is to be drawn right-to-left (true) instead of the default left-to-right (false).
	 * @param yflip Whether the direction of this line is flipped in the Y direction, that is, whether
	 * it is to be drawn upward (true) instead of the downward (false).
	 */
	private void drawLine_eqXY(int len, boolean xflip, boolean yflip)
	{
		int x = 0;
		int y = 0;
		if (xflip) x = len-1;
		if (yflip) y = len-1;

		pixels = new ArrayList<MappedPixel>(len);
		
		while (x >= 0 && x < len)
		{
			MappedPixel p = new MappedPixel(0, 0, 0, 255); //Black by default
			p.setX(x);
			p.setY(y);
			if (xflip) x--;
			else x++;
			if (yflip) y--;
			else y++;
			pixels.add(p);
		}
		
		dir = NO_MAJOR;

	}
	
	/**
	 * Internal construction rendering function. This function is called if the line is
	 * longer in one direction than the other. The line is rendered by generating an ArrayList of
	 * pixels mapped to coordinates relative to the line's top left "corner" (the lower of the x and y
	 * coordinates of the vertices, separately).
	 * <br>The x and y distances as well as the orientation must be determined by the main constructor
	 * before calling this method.
	 * @param xSz The distance in pixels that must be covered by this line in the X direction.
	 * @param ySz The distance in pixels that must be covered by this line in the Y direction.
	 * @param xflip Whether the direction of this line is flipped in the X direction, that is, whether
	 * it is to be drawn right-to-left (true) instead of the default left-to-right (false).
	 * @param yflip Whether the direction of this line is flipped in the Y direction, that is, whether
	 * it is to be drawn upward (true) instead of the downward (false).
	 */
	private void drawLine(int xSz, int ySz, boolean xflip, boolean yflip)
	{
		boolean xmajor = true;
		if (ySz > xSz) xmajor = false;
		int x = 0;
		int y = 0;
		if (xflip) x = xSz - 1;
		if (yflip) y = ySz - 1;
		
		int majorSz = xSz;
		if (!xmajor) majorSz = ySz;
		int minorSz = ySz;
		if (!xmajor) minorSz = xSz;
		
		int segsz = majorSz/ minorSz;
		int nseg = majorSz/segsz;
		
		pixels = new ArrayList<MappedPixel>(majorSz + nseg);
		
		if (xmajor)
		{
			dir = X_MAJOR;
			if (xflip)
			{
				while (x >= 0)
				{
					//If falling on a seg boundary, draw on next row
					if (x % segsz == 0)
					{
						if (yflip) y--;
						else y++;
					}
					MappedPixel p = new MappedPixel(0,0,0,255);
					p.setX(x);
					p.setY(y);
					pixels.add(p);
					x--;
				}
			}
			else
			{
				while (x < majorSz)
				{
					//If falling on a seg boundary, draw on next row
					if (x % segsz == 0)
					{
						if (yflip) y--;
						else y++;
					}
					MappedPixel p = new MappedPixel(0,0,0,255);
					p.setX(x);
					p.setY(y);
					pixels.add(p);
					x++;
				}
			}
		}
		else
		{
			dir = Y_MAJOR;
			if (yflip)
			{
				while (y >= 0)
				{
					//If falling on a seg boundary, draw on next col
					if (y % segsz == 0)
					{
						if (xflip) x--;
						else x++;
					}
					MappedPixel p = new MappedPixel(0,0,0,255);
					p.setX(x);
					p.setY(y);
					pixels.add(p);
					y--;
				}
			}
			else
			{
				while (y < majorSz)
				{
					//If falling on a seg boundary, draw on next col
					if (y % segsz == 0)
					{
						if (xflip) x--;
						else x++;
					}
					MappedPixel p = new MappedPixel(0,0,0,255);
					p.setX(x);
					p.setY(y);
					pixels.add(p);
					y++;
				}
			}
		}
		
	}
	
	/* ----- Getters ----- */
	
	/**
	 * Get the pixel at the provided index.
	 * <br><br><b>CAUTION!</b> This function does not check the index for validity.
	 * You must handle the exception yourself.
	 * <br><br><b>CAUTION!</b> The coordinates mapped to the pixel are relative to the
	 * line ONLY. To get the coordinate taking the location of the line as a whole into
	 * account, you must add it to the line's offset coordinates.
	 * <br><br><b>CAUTION!</b> This method returns the pixel object linked to the line, not
	 * a copy. Modify at your own risk.
	 * @param index One-dimensional index relative to line of pixel to retrieve.
	 * @return MappedPixel (containing color, transparency, and location data) at position in Line.
	 */
	public MappedPixel getPixel(int index)
	{
		//NO DUMMY CHECK!!
		return pixels.get(index);
	}
	
	/**
	 * Get the pixel at the start vertex in the line.
	 * <br><br><b>CAUTION!</b> The coordinates mapped to the pixel are relative to the
	 * line ONLY. To get the coordinate taking the location of the line as a whole into
	 * account, you must add it to the line's offset coordinates.
	 * <br><br><b>CAUTION!</b> This method returns the pixel object linked to the line, not
	 * a copy. Modify at your own risk.
	 * @return MappedPixel (containing color, transparency, and location data) at Line start.
	 */
	public MappedPixel getFirstPixel()
	{
		return pixels.get(0);
	}
	
	/**
	 * Get the pixel at the end vertex in the line.
	 * <br><br><b>CAUTION!</b> The coordinates mapped to the pixel are relative to the
	 * line ONLY. To get the coordinate taking the location of the line as a whole into
	 * account, you must add it to the line's offset coordinates.
	 * <br><br><b>CAUTION!</b> This method returns the pixel object linked to the line, not
	 * a copy. Modify at your own risk.
	 * @return MappedPixel (containing color, transparency, and location data) at Line end.
	 */
	public MappedPixel getLastPixel()
	{
		return pixels.get(pixels.size() - 1);
	}
	
	/**
	 * Count the number of pixels that make up this line.
	 * @return Number of pixels in line.
	 */
	public int getPixelCount()
	{
		return pixels.size();
	}
	
	/**
	 * Retrieve the net X offset of the line. This might be thought of as the
	 * coordinate of the top left corner of the rectangle drawn by setting two
	 * of its opposing corners as the two line vertices.
	 * <br>This value is 0 by default. It may be negative if set to a negative number.
	 * @return X offset (with values increasing right)
	 */
	public int getOffset_X()
	{
		return xOffset;
	}
	
	/**
	 * Retrieve the net Y offset of the line. This might be thought of as the
	 * coordinate of the top left corner of the rectangle drawn by setting two
	 * of its opposing corners as the two line vertices.
	 * <br>This value is 0 by default. It may be negative if set to a negative number.
	 * @return Y offset (with values increasing downward)
	 */
	public int getOffset_Y()
	{
		return yOffset;
	}
	
	/* ----- Modification ----- */
	
	/**
	 * Scale, but do not remap, the line to fit in a given number of pixels.
	 * This method is protected as it must be used with caution. Its purpose is for texture
	 * mapping, not resizing. Though it can be used as a step in resizing, it alone cannot
	 * resize a line. If the output list is forcibly set as the internal pixel list without remapping
	 * the pixels, attempts to render may yield undesired results.
	 * @param newPixCount Target resolution of the line, in pixels.
	 * @param blur Whether to apply the blurring algorithm (combine pixels into new ones) or simply
	 * delete/duplicate pixels to get it to size.
	 * @return A new List instance that may or may not contain references to existing Pixels or entirely
	 * new Pixels. This can be cycled through to set color values or set directly as the line pixel array
	 * (if remapped).
	 */
	protected List<Pixel> scale_1D(int newPixCount, boolean blur)
	{
		//This only generates a new list of pixels. It doesn't MAP them!!
		List<Pixel> list = new ArrayList<Pixel>(pixels.size());
		list.addAll(pixels);
		return Scale.scale_1D(list, newPixCount, blur);
	}
	
	/**
	 * Set the line to be rendered a solid color, specified by the RGBA values provided
	 * in the parameters.
	 * <br>This will overwrite any existing color values the pixels in the line may currently
	 * have.
	 * @param r Red value of desired solid color.
	 * @param g Green value of desired solid color.
	 * @param b Blue value of desired solid color.
	 * @param a Alpha value of desired solid color. (Set to 255 for full opacity).
	 */
	public void setColor(int r, int g, int b, int a)
	{
		for (Pixel p : pixels)
		{
			p.setAlpha(a);
			p.setRed(r);
			p.setBlue(b);
			p.setGreen(g);
		}
	}
		
	/**
	 * Set the color/transparency values of the line pixels according to the pattern
	 * specified by another line object.
	 * <br>The texture line is scaled to fit the target line (with or without blurring),
	 * and the RGBA values of the pixels are copied 1 to 1 to the target.
	 * @param texture Line containing texture information to apply to this line.
	 * @param blur Whether to interpolate values of new pixels by combining existing ones (true), or
	 * whether to simply delete or duplicate pixels to fit the size of the target (false).
	 */
	public void applyTexture(Line texture, boolean blur)
	{
		int len = getPixelCount();
		List<Pixel> tex = texture.scale_1D(len, blur);
		for (int i = 0; i < len; i++)
		{
			MappedPixel p = pixels.get(i);
			Pixel t = tex.get(i);
			p.setRed(t.getRed());
			p.setGreen(t.getGreen());
			p.setBlue(t.getBlue());
			p.setAlpha(t.getAlpha());
		}
	}
	
	/**
	 * Apply a linearly interpolated gradient across the line by specifying the
	 * target pixel values at each vertex (including alpha).
	 * <br>This will overwrite any existing color values the pixels in the line may currently
	 * have.
	 * @param RGBA1 Brightness/Transparency value at start vertex.
	 * @param RGBA2 Brightness/Transparency value at end vertex.
	 */
	public void gouraudShade(Pixel RGBA1, Pixel RGBA2)
	{
		int len = this.getPixelCount();
		for (int i = 0; i < len; i++)
		{
			//Just linear
			double p1 = (double)i/(double)len;
			double p2 = 1.0 - p1;
			int r = (int)((p1 * (double)RGBA1.getRed()) + (p2 * (double)RGBA2.getRed()));
			int g = (int)((p1 * (double)RGBA1.getGreen()) + (p2 * (double)RGBA2.getGreen()));
			int b = (int)((p1 * (double)RGBA1.getBlue()) + (p2 * (double)RGBA2.getBlue()));
			int a = (int)((p1 * (double)RGBA1.getAlpha()) + (p2 * (double)RGBA2.getAlpha()));
			Pixel p = pixels.get(i);
			p.setRed(r);
			p.setGreen(g);
			p.setBlue(b);
			p.setAlpha(a);
		}
	}

	/**
	 * Overlay another Line on top of this one and combine the pixel values (eg. blending the
	 * two images together) by using the desired proportion of overlay and base.
	 * @param overlay Line to blend to this Line.
	 * @param amount Proportion (<1) of overlay to integrate. Ceiling is 1, floor is 0.
	 * @param scaleblur If rescaling the overlay line is required, whether to use blur.
	 */
	public void blend(Line overlay, double amount, boolean scaleblur)
	{
		int osz = overlay.getPixelCount();
		int msz = this.getPixelCount();
		
		double oamt = amount;
		if (oamt <= 0.0) return;
		double mamt = 1.0 - oamt;
		
		if (osz == msz)
		{
			//No point in wasting time sending it through the scaling method...
			for (int i = 0; i < msz; i++)
			{
				MappedPixel mp = this.pixels.get(i);
				MappedPixel op = overlay.pixels.get(i);
				
				int r = (int)Math.round(((double)mp.getRed() * mamt) + ((double)op.getRed() * oamt));
				int g = (int)Math.round(((double)mp.getGreen() * mamt) + ((double)op.getGreen() * oamt));
				int b = (int)Math.round(((double)mp.getBlue() * mamt) + ((double)op.getBlue() * oamt));
				int a = (int)Math.round(((double)mp.getAlpha() * mamt) + ((double)op.getAlpha() * oamt));
				mp.setRed(r);
				mp.setGreen(g);
				mp.setBlue(b);
				mp.setAlpha(a);
				mp.saturate8();
			}
		}
		else
		{
			List<Pixel> scaledOverlay = overlay.scale_1D(msz, scaleblur);
			for (int i = 0; i < msz; i++)
			{
				MappedPixel mp = this.pixels.get(i);
				Pixel op = scaledOverlay.get(i);
				
				int r = (int)Math.round(((double)mp.getRed() * mamt) + ((double)op.getRed() * oamt));
				int g = (int)Math.round(((double)mp.getGreen() * mamt) + ((double)op.getGreen() * oamt));
				int b = (int)Math.round(((double)mp.getBlue() * mamt) + ((double)op.getBlue() * oamt));
				int a = (int)Math.round(((double)mp.getAlpha() * mamt) + ((double)op.getAlpha() * oamt));
				mp.setRed(r);
				mp.setGreen(g);
				mp.setBlue(b);
				mp.setAlpha(a);
				mp.saturate8();
			}
		}
		
	}
	
	/* ----- Orientation & Position ----- */
	
	/**
	 * Get the major direction (as an int-enum, described by the Line class) of this line.
	 * <br>Value should be X, Y, or no direction.
	 * @return Integer enumeration describing the direction this Line predominately goes (ie. whether
	 * it might be considered more horizontal, vertical, or diagonal).
	 * @see <br>Line.NO_MAJOR
	 * <br>Line.X_MAJOR
	 * <br>Line.Y_MAJOR
	 */
	public int getMajorDirection()
	{
		if (dir >= 0) return dir;
		MappedPixel p1 = this.getFirstPixel();
		MappedPixel p2 = this.getLastPixel();
		int xdiff = Math.abs(p1.getX() - p2.getX());
		int ydiff = Math.abs(p1.getY() - p2.getY());
		if (xdiff > ydiff) dir = X_MAJOR;
		else if (ydiff > xdiff) dir = Y_MAJOR;
		else if (xdiff == ydiff) dir = NO_MAJOR;
		return dir;
	}
	
	/**
	 * Get the relative directionality of the line. If flipped relative to X, the line is drawn
	 * right-to-left (otherwise left-to-right). If flipped relative to Y, the line is drawn bottom-to-top
	 * (otherwise top-to-bottom).
	 * @return A 2-element boolean array. The first element is the X flip, the second element is the Y flip.
	 * <br>The value of each flip boolean is true if the line is flipped relative to that direction, false if not.
	 */
	public boolean[] getFlip()
	{
		//flip[0] = xflip
		//flip[1] = yflip
		//Essentially tells you which of the two vertices is further up and left
		int stX = pixels.get(0).getX();
		int stY = pixels.get(0).getY();
		int edX = this.getLastPixel().getX();
		int edY = this.getLastPixel().getY();
		boolean[] flip = new boolean[2];
		if (stX > edX) flip[0] = true;
		else flip[0] = false;
		if (stY > edY) flip[1] = true;
		else flip[1] = false;
		return flip;
	}

	/**
	 * Move the line left or right relative to the external coordinate system by adding to the
	 * line's x offset. Negative numbers move the entire line left, positive numbers move the entire
	 * line right.
	 * @param xOff Relative number of pixels to move the line as a whole.
	 */
	public void translocate_X(int xOff)
	{
		//for (MappedPixel p : pixels) p.setX(p.getX() + xOff);
		xOffset += xOff;
	}
	
	/**
	 * Move the line up or down relative to the external coordinate system by adding to the
	 * line's y offset. Negative numbers move the entire line up, positive numbers move the entire
	 * line down.
	 * @param yOff Relative number of pixels to move the line as a whole.
	 */
	public void translocate_Y(int yOff)
	{
		//for (MappedPixel p : pixels) p.setY(p.getY() + yOff);
		yOffset += yOff;
	}
	
	/**
	 * Move the line relative to the external coordinate system by adding to the
	 * line's x and y offsets. 
	 * <br>Negative x values will move the line left, while positive x values will move the line right.
	 * <br>Negative y values will move the line up, while positive y values will move the line down.
	 * @param xOff Relative number of pixels to move the line as a whole in the x direction.
	 * @param yOff Relative number of pixels to move the line as a whole in the y direction.
	 */
	public void translocate(int xOff, int yOff)
	{
		/*for (MappedPixel p : pixels)
		{
			p.setX(p.getX() + xOff);
			p.setY(p.getY() + yOff);
		}*/
		xOffset += xOff;
		yOffset += yOff;
	}
	
	/* ----- Duplication ----- */
	
	/**
	 * Generate a copy of this Line independent of any references within it.
	 * It should be possible to modify the copy in any way without affecting the original.
	 * @return A copy of this Line as a new Line object.
	 */
	public Line copy()
	{
		Line cp = new Line();
		cp.pixels = new ArrayList<MappedPixel>(this.getPixelCount());
		for (MappedPixel p : pixels) cp.pixels.add((MappedPixel)p.copy());
		cp.dir = this.dir;
		cp.xOffset = xOffset;
		cp.yOffset = yOffset;
		return cp;
	}
	
	/**
	 * Generate a copy of this Line independent of any references within it,
	 * that is also offset from the original Line by a specified amount.
	 * It should be possible to modify the copy in any way without affecting the original.
	 * @param offset Number of pixels (specified by Point.x and Point.y) to offset the new line
	 * from the original, relative to the external coordinate system.
	 * @return A copy of this Line as a new Line object with modified offsets.
	 */
	public Line createParallelLine(Point offset)
	{
		Line nline = this.copy();
		//int dirMaj = this.getMajorDirection();
		
		nline.translocate(offset.x, offset.y);
		
		return nline;
	}
	
	/* ----- Trimming & Extension ----- */
	
		//---- Map Generation
	
	/**
	 * Map the Y coordinates of this line relative to the X coordinates. Because only one Y coordinate
	 * can be mapped to each X coordinate, whether to prioritize the Y with the lowest or the highest
	 * value must be specified.
	 * @param prioritizeMax If set, method will map the Y coordinate with the highest value to each X coordinate
	 * in the event that an X coordinate has multiple Y mappings. Otherwise, it will select the Y coordinates with
	 * the lowest value.
	 * @param includeOffsets If set, the output map coordinates will be relative to the external coordinate system
	 * (by adding the Line's global x and y offsets to each coordinate) rather than the Line's internal coordinate system.
	 * @return A mapping of Y coordinates to X coordinates.
	 */
	public Map<Integer, Integer> map_to_X(boolean prioritizeMax, boolean includeOffsets)
	{
		Map<Integer, Integer> xmap = new HashMap<Integer, Integer>();
		for (MappedPixel p : pixels)
		{
			int x = p.getX();
			if (includeOffsets) x += xOffset;
			int y = p.getY();
			if (includeOffsets) y += yOffset;
			Integer i = xmap.get(x);
			if (i == null) xmap.put(x, y);
			else
			{
				if (prioritizeMax)
				{
					if (y > i) xmap.put(x, y);
				}
				else
				{
					if (y < i) xmap.put(x, y);
				}
			}
		}
		
		return xmap;
	}
	
	/**
	 * Map the X coordinates of this line relative to the Y coordinates. Because only one X coordinate
	 * can be mapped to each Y coordinate, whether to prioritize the X with the lowest or the highest
	 * value must be specified.
	 * @param prioritizeMax If set, method will map the X coordinate with the highest value to each Y coordinate
	 * in the event that an Y coordinate has multiple X mappings. Otherwise, it will select the X coordinates with
	 * the lowest value.
	 * @param includeOffsets If set, the output map coordinates will be relative to the external coordinate system
	 * (by adding the Line's global x and y offsets to each coordinate) rather than the Line's internal coordinate system.
	 * @return A mapping of X coordinates to Y coordinates.
	 */
	public Map<Integer, Integer> map_to_Y(boolean prioritizeMax, boolean includeOffsets)
	{
		Map<Integer, Integer> ymap = new HashMap<Integer, Integer>();
		for (MappedPixel p : pixels)
		{
			int x = p.getX();
			if (includeOffsets) x += xOffset;
			int y = p.getY();
			if (includeOffsets) y += yOffset;
			Integer i = ymap.get(y);
			if (i == null) ymap.put(y, x);
			else
			{
				if (prioritizeMax)
				{
					if (x > i) ymap.put(y, x);
				}
				else
				{
					if (x < i) ymap.put(y, x);
				}
			}
		}
		
		return ymap;
	}
	
		//---- Trimming
	
	/**
	 * Delete any pixels that fall above (have a lower Y value than) the provided boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the minimum allowed Y values for this Line. The lowest (highest Y) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the Y value must be larger than, and not equal to the boundary value
	 * at that X for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	protected void clipAbove(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//No check for top being null. You'll just get the NullPointerException. Like you deserve.
		ArrayList<MappedPixel> line = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel p : pixels)
		{
			int x = p.getX() + xOffset;
			int y = p.getY() + yOffset;
			Integer min = bound.get(x);
			if (min != null)
			{
				if (y > min) line.add(p);
				else if (!excludeBound && (y == min)) line.add(p);
			}
			else line.add(p);
		}
		pixels = line;
	}
	
	/**
	 * Delete any pixels that fall below (have a higher Y value than) the provided boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the maximum allowed Y values for this Line. The highest (lowest Y) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the Y value must be smaller than, and not equal to the boundary value
	 * at that X for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	protected void clipBelow(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//No check for bottom being null.
		ArrayList<MappedPixel> line = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel p : pixels)
		{
			int x = p.getX() + xOffset;
			int y = p.getY() + yOffset;
			Integer max = bound.get(x);
			if (max != null)
			{
				if (y < max) line.add(p);
				else if (!excludeBound && (y == max)) line.add(p);
			}
			else line.add(p);
		}
		pixels = line;
	}
	
	/**
	 * Delete any pixels that fall right of (have a higher X value than) the provided boundary line.
	 * <br>Any pixels that have an Y value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the maximum allowed X values for this Line. The farthest left (lowest X) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the X value must be smaller than, and not equal to the boundary value
	 * at that Y for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	protected void clipRight(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//No check for bound line being null.
		ArrayList<MappedPixel> line = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel p : pixels)
		{
			int x = p.getX() + xOffset;
			int y = p.getY() + yOffset;
			Integer max = bound.get(y);
			if (max != null)
			{
				if (x < max) line.add(p);
				else if (!excludeBound && (x == max)) line.add(p);
			}
			else line.add(p);
		}
		pixels = line;
	}
	
	/**
	 * Delete any pixels that fall left of (have a lower X value than) the provided boundary line.
	 * <br>Any pixels that have an Y value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the maximum allowed X values for this Line. The farthest right (highest X) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the X value must be larger than, and not equal to the boundary value
	 * at that Y for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	protected void clipLeft(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//No check for bound line being null.
		//Map<Integer, Integer> bound = leftBound.map_to_Y(true);
		ArrayList<MappedPixel> line = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel p : pixels)
		{
			int x = p.getX() + xOffset;
			int y = p.getY() + yOffset;
			Integer min = bound.get(y);
			if (min != null)
			{
				if (x > min) line.add(p);
				else if (!excludeBound && (x == min)) line.add(p);
			}
			else line.add(p);
		}
		pixels = line;
	}

	/**
	 * Delete any pixels that fall above (have a lower Y value than) the provided boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * @param top Line specifying the minimum allowed Y values for this Line. The lowest (highest Y) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the Y value must be larger than, and not equal to the boundary value
	 * at that X for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	public void clipAbove(Line top, boolean excludeBound)
	{
		if (top == null) return;
		Map<Integer, Integer> bound = top.map_to_X(true, true);
		clipAbove(bound, excludeBound);
	}
	
	/**
	 * Delete any pixels that fall below (have a higher Y value than) the provided boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * @param bottom Line specifying the maximum allowed Y values for this Line. The highest (lowest Y) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the Y value must be smaller than, and not equal to the boundary value
	 * at that X for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	public void clipBelow(Line bottom, boolean excludeBound)
	{
		if (bottom == null) return;
		Map<Integer, Integer> bound = bottom.map_to_X(false, true);
		clipBelow(bound, excludeBound);
	}
	
	/**
	 * Delete any pixels that fall right of (have a higher X value than) the provided boundary line.
	 * <br>Any pixels that have an Y value not covered by the boundary line are ignored.
	 * @param rightBound Line specifying the maximum allowed X values for this Line. The farthest left (lowest X) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the X value must be smaller than, and not equal to the boundary value
	 * at that Y for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	public void clipRight(Line rightBound, boolean excludeBound)
	{
		if (rightBound == null) return;
		Map<Integer, Integer> bound = rightBound.map_to_Y(false, true);
		clipRight(bound, excludeBound);
	}
	
	/**
	 * Delete any pixels that fall left of (have a lower X value than) the provided boundary line.
	 * <br>Any pixels that have an Y value not covered by the boundary line are ignored.
	 * @param leftBound Line specifying the maximum allowed X values for this Line. The farthest right (highest X) pixels
	 * in the boundary line define the boundary.
	 * @param excludeBound If set, the X value must be larger than, and not equal to the boundary value
	 * at that Y for the pixel to pass. If unset, the pixel may fall on the boundary line and pass.
	 */
	public void clipLeft(Line leftBound, boolean excludeBound)
	{
		if (leftBound == null) return;
		Map<Integer, Integer> bound = leftBound.map_to_Y(true, true);
		clipLeft(bound, excludeBound);
	}

	/**
	 * Delete (clip) all pixels that fall left of the x coordinate provided. The pixels falling
	 * on the vertical line defined by x are not clipped out.
	 * @param x New hard left boundary (farthest left x coordinate remaining in line).
	 */
	public void clipLeftOf(int x)
	{
		ArrayList<MappedPixel> keepers = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel mp : pixels)
		{
			if (mp.getX() >= x) keepers.add(mp);
		}
		pixels = keepers;
	}
	
	/**
	 * Delete (clip) all pixels that fall right of the x coordinate provided. The pixels falling
	 * on the vertical line defined by x are not clipped out.
	 * @param x New hard right boundary (farthest right x coordinate remaining in line).
	 */
	public void clipRightOf(int x)
	{
		ArrayList<MappedPixel> keepers = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel mp : pixels)
		{
			if (mp.getX() <= x) keepers.add(mp);
		}
		pixels = keepers;
	}
	
	/**
	 * Delete (clip) all pixels that fall above of the y coordinate provided. The pixels falling
	 * on the horizontal line defined by y are not clipped out.
	 * @param y New hard top boundary.
	 */
	public void clipAbove(int y)
	{
		ArrayList<MappedPixel> keepers = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel mp : pixels)
		{
			if (mp.getY() >= y) keepers.add(mp);
		}
		pixels = keepers;
	}
	
	/**
	 * Delete (clip) all pixels that fall below of the y coordinate provided. The pixels falling
	 * on the horizontal line defined by y are not clipped out.
	 * @param y New hard bottom boundary.
	 */
	public void clipBelow(int y)
	{
		ArrayList<MappedPixel> keepers = new ArrayList<MappedPixel>(pixels.size());
		for (MappedPixel mp : pixels)
		{
			if (mp.getY() <= y) keepers.add(mp);
		}
		pixels = keepers;
	}
	
		//---- Extension
	
	/**
	 * Extend this Line in the negative (upward) Y direction from its higher vertex to meet the
	 * top boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's X position. If not, it will stop one pixel short of the boundary.
	 */
	protected void extendUp(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//Map<Integer, Integer> bound = top.map_to_X(true);
		//Add [black] pixels to front or back of list until it reaches the line
		//Determine which vertex has a lower y coord
		int yf = pixels.get(0).getY() + yOffset;
		int yl = this.getLastPixel().getY() + yOffset;
		if (yf <= yl)
		{
			int x = pixels.get(0).getX() + xOffset;
			Integer ty = bound.get(x);
			if ((ty == null) || yf < ty || (excludeBound && yf == ty)) return;
			int tailSize = yf-ty;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + tailSize + 1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			
			int y = ty;
			if (excludeBound) y++;
			while (y < yf)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				y++;
			}
			nlist.addAll(tail);
			nlist.addAll(pixels);
			pixels = nlist;
			
			//Remap relative to new offset
			//Just to get rid of any negative coordinates
			//More of a cleanup than anything else.
			if(!excludeBound) tailSize++;
			yOffset -= tailSize;
			for (MappedPixel mp : pixels) mp.setY(mp.getY() + tailSize); 
		}
		else
		{
			int x = this.getLastPixel().getX() + xOffset;
			Integer ty = bound.get(x);
			if ((ty==null) || yl < ty || (excludeBound && yl == ty)) return;
			int tailSize = yl-ty;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + tailSize + 1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			if (excludeBound) ty++;
			int y = yl-1;
			while (y >= ty)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				y--;
			}
			nlist.addAll(pixels);
			nlist.addAll(tail);
			pixels = nlist;
			
			//Coord cleanup
			if(!excludeBound) tailSize++;
			yOffset -= tailSize;
			for (MappedPixel mp : pixels) mp.setY(mp.getY() + tailSize); 
		}
	}
	
	/**
	 * Extend this Line in the positive (downward) Y direction from its lower vertex to meet the
	 * bottom boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's X position. If not, it will stop one pixel short of the boundary.
	 */
	protected void extendDown(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//Add [black] pixels to front or back of list until it reaches the line
		//Determine which vertex has a higher y coord
		int yf = pixels.get(0).getY() + yOffset;
		int yl = this.getLastPixel().getY() + yOffset;
		if (yf >= yl)
		{
			int x = pixels.get(0).getX() + xOffset;
			Integer ty = bound.get(x);
			if (ty == null || yf > ty || (excludeBound && yf == ty)) return;
			int tailSize = ty-yf;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + tailSize+1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			int y = ty;
			if (excludeBound) y--;
			while (y > yf)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				y--;
			}
			nlist.addAll(tail);
			nlist.addAll(pixels);
			pixels = nlist;
			//Don't need coord cleanup for extending in positive direction
		}
		else
		{
			int x = this.getLastPixel().getX() + xOffset;
			Integer ty = bound.get(x);
			if (ty == null || yl > ty || (excludeBound && yl == ty)) return;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + ty-yl+1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			if (excludeBound) ty--;
			int y = yl+1;
			while (y <= ty)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				y++;
			}
			nlist.addAll(pixels);
			nlist.addAll(tail);
			pixels = nlist;
		}
	}
	
	/**
	 * Extend this Line in the negative (leftward) X direction from its left-most vertex to meet the
	 * left boundary line.
	 * <br>Any pixels that have a Y value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's Y position. If not, it will stop one pixel short of the boundary.
	 */
	protected void extendLeft(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//Add [black] pixels to front or back of list until it reaches the line
		//Determine which vertex has a lower x coord
		int xf = pixels.get(0).getX() + xOffset;
		int xl = this.getLastPixel().getX() + xOffset;
		if (xf <= xl)
		{
			int y = pixels.get(0).getY() + yOffset;
			Integer tx = bound.get(y);
			if (tx == null || xf < tx || (excludeBound && xf == tx)) return;
			int tailSize = xf-tx;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + tailSize+1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			int x = tx;
			if (excludeBound) x++;
			while (x < xf)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				x++;
			}
			nlist.addAll(tail);
			nlist.addAll(pixels);
			pixels = nlist;
			
			//Coord cleanup
			if(!excludeBound) tailSize++;
			xOffset -= tailSize;
			for (MappedPixel mp : pixels) mp.setX(mp.getX() + tailSize); 
		}
		else
		{
			int y = this.getLastPixel().getY() + yOffset;
			Integer tx = bound.get(y);
			if (tx == null || xl < tx || (excludeBound && xl == tx)) return;
			int tailSize = xl-tx;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + tailSize+1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			if (excludeBound) tx++;
			int x = xl-1;
			while (x >= tx)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				x--;
			}
			nlist.addAll(pixels);
			nlist.addAll(tail);
			pixels = nlist;
			
			//Coord cleanup
			if(!excludeBound) tailSize++;
			xOffset -= tailSize;
			for (MappedPixel mp : pixels) mp.setX(mp.getX() + tailSize); 
		}
	}
	
	/**
	 * Extend this Line in the positive (rightward) X direction from its right-most vertex to meet the
	 * right boundary line.
	 * <br>Any pixels that have a Y value not covered by the boundary line are ignored.
	 * <br>**This method is protected as it accepts a pre-generated map (in the case of one Line being
	 * used repeatedly, the map does not have to be regenerated).
	 * @param bound Map derived from Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's Y position. If not, it will stop one pixel short of the boundary.
	 */
	protected void extendRight(Map<Integer, Integer> bound, boolean excludeBound)
	{
		//Map<Integer, Integer> bound = rightBound.map_to_Y(false);
		//Add [black] pixels to front or back of list until it reaches the line
		//Determine which vertex has a higher x coord
		int xf = pixels.get(0).getX() + xOffset;
		int xl = this.getLastPixel().getX() + xOffset;
		if (xf >= xl)
		{
			int y = pixels.get(0).getY() + yOffset;
			Integer tx = bound.get(y);
			if (tx == null || xf > tx || (excludeBound && xf == tx)) return;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + tx-xf+1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			int x = tx;
			if (excludeBound) x--;
			while (x > xf)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				x--;
			}
			nlist.addAll(tail);
			nlist.addAll(pixels);
			pixels = nlist;
		}
		else
		{
			int y = this.getLastPixel().getY() + yOffset;
			Integer tx = bound.get(y);
			if (tx == null || xl > tx || (excludeBound && xl == tx)) return;
			ArrayList<MappedPixel> nlist = new ArrayList<MappedPixel>(pixels.size() + tx-xl+1);
			LinkedList<MappedPixel> tail = new LinkedList<MappedPixel>();
			if (excludeBound) tx--;
			int x = xl+1;
			while (x <= tx)
			{
				MappedPixel mp = new MappedPixel(0,0,0,255);
				mp.setX(x - xOffset);
				mp.setY(y - yOffset);
				tail.add(mp);
				x++;
			}
			nlist.addAll(pixels);
			nlist.addAll(tail);
			pixels = nlist;
		}
	}
	
	/**
	 * Extend this Line in the negative (upward) Y direction from its higher vertex to meet the
	 * top boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * @param top Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's X position. If not, it will stop one pixel short of the boundary.
	 */
	public void extendUp(Line top, boolean excludeBound)
	{
		if (top == null) return;
		Map<Integer, Integer> bound = top.map_to_X(true, true);
		extendUp(bound, excludeBound);
	}
	
	/**
	 * Extend this Line in the positive (downward) Y direction from its lower vertex to meet the
	 * bottom boundary line.
	 * <br>Any pixels that have an X value not covered by the boundary line are ignored.
	 * @param bottom Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's X position. If not, it will stop one pixel short of the boundary.
	 */
	public void extendDown(Line bottom, boolean excludeBound)
	{
		if (bottom == null) return;
		Map<Integer, Integer> bound = bottom.map_to_X(false, true);
		extendDown(bound, excludeBound);
	}
	
	/**
	 * Extend this Line in the negative (leftward) X direction from its left-most vertex to meet the
	 * left boundary line.
	 * <br>Any pixels that have a Y value not covered by the boundary line are ignored.
	 * @param leftBound Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's Y position. If not, it will stop one pixel short of the boundary.
	 */
	public void extendLeft(Line leftBound, boolean excludeBound)
	{
		if (leftBound == null) return;
		Map<Integer, Integer> bound = leftBound.map_to_Y(true, true);
		extendLeft(bound, excludeBound);
	}
	
	/**
	 * Extend this Line in the positive (rightward) X direction from its right-most vertex to meet the
	 * right boundary line.
	 * <br>Any pixels that have a Y value not covered by the boundary line are ignored.
	 * @param rightBound Line specifying the line to reach to.
	 * @param excludeBound If set, the line will be extended to cover the pixel on the boundary line
	 * matching the extended vertex's Y position. If not, it will stop one pixel short of the boundary.
	 */
	public void extendRight(Line rightBound, boolean excludeBound)
	{
		if (rightBound == null) return;
		Map<Integer, Integer> bound = rightBound.map_to_Y(false, true);
		extendRight(bound, excludeBound);
	}
	
	
}
