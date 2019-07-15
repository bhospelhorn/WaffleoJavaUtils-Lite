package waffleoRai_Image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * UPDATES
 * 
 * 1.0.1 -> 1.0.2 | May 17, 2018
 * 	Added functions for hard trimming of polygon edges
 * 
 */

/**
 * A container for a Polygon. Internally implemented as a series of lines.
 * @author Blythe Hospelhorn
 * @version 1.0.2
 * @since May 17, 2018
 *
 */
public class Polygon implements Iterable<PolygonPixel> {
	
	/* ----- Instance Variables ----- */
		
	private Line baseline;
	
	private Point v1;
	private Point v2;
	private Point v3;
	
	private List<Line> data;
	
	private PolygonIterator iterator;
	
	/* ----- Inner Classes ----- */
	
	/**
	 * Pixel iterator to make it easier to cycle through polygon pixels
	 * (for rendering or applying textures) without having to pull the lines.
	 * <br>PolygonPixel is a wrapper for a MappedPixel additionally having the line
	 * offset information (since the raw mapped coordinates are relative to the line
	 * which the pixel is a part of rather than to the polygon or anything external.)
	 * The MappedPixel is referenced, so modification of it will reflect in the polygon.
	 * @author Blythe Hospelhorn
	 * @version 2.0.0
	 * @since May 13, 2018
	 *
	 */
	public class PolygonIterator implements Iterator<PolygonPixel>
	{
		private int line_i;
		private int pix_i;
		
		/**
		 * Construct an iterator for this Polygon, setting the current
		 * positions to zero.
		 */
		public PolygonIterator()
		{
			line_i = 0;
			pix_i = 0;
		}

		public boolean hasNext() 
		{
			if (line_i >= data.size())
			{
				line_i = 0;
				pix_i = 0;
				return false;
			}
			return true;
		}

		public PolygonPixel next() 
		{
			Line l = null;
			try
			{
				l = data.get(line_i);
			}
			catch (Exception e)
			{
				//If line is invalid, we have a serious problem...
				//This method was called when it should not have been -_-
				//It will rewind and recall itself to instead return the first in the series.
				line_i = 0;
				pix_i = 0;
				next();
				//Because this will loop forever, make sure to call hasNext() when loop is done!!!
				
				/*System.err.println("INTERNAL ERROR: Polygon iterator error.");
				e.printStackTrace();
				throw e;*/
			}
			MappedPixel mp = null;
			PolygonPixel pp = null;
			try
			{
				//mp = (MappedPixel)l.getPixel(pix_i).copy();
				//mp.setX(mp.getX() + l.getOffset_X());
				//mp.setY(mp.getY() + l.getOffset_Y());
				mp = l.getPixel(pix_i);
				pp = new PolygonPixel(mp, l);
				pix_i++;
			}
			catch (Exception e)
			{
				line_i++;
				pix_i = 0;
			}
			return pp;
		}
		
	}
	
	/* ----- Construction ----- */
	
	/**
	 * Protected method to construct an empty Polygon. Except for the iterator,
	 * all instance variables are set to null. Should only be called by package or
	 * descendant methods that explicitly set the instance variables, otherwise
	 * many of the Polygon methods will throw exceptions when called.
	 */
	protected Polygon()
	{
		v1 = null;
		v2 = null;
		v3 = null;
		data = null;
		baseline = null;
		iterator = new PolygonIterator();
	}
	
	/**
	 * Construct a three-point Polygon by specifying three vertices. Polygon will be drawn from the line
	 * connected vertex 2 and vertex 3 towards vertex 1. This directionality will affect the iteration
	 * order as well as any potential texture mapping.
	 * @param vert1 Vertex 1 coordinates
	 * @param vert2 Vertex 2 coordinates
	 * @param vert3 Vertex 3 coordinates
	 */
	public Polygon(Point vert1, Point vert2, Point vert3)
	{
		iterator = new PolygonIterator();
		v1 = vert1;
		v2 = vert2;
		v3 = vert3;
		
		baseline = new Line(v2.x, v2.y, v3.x, v3.y);
		
		switch(baseline.getMajorDirection())
		{
		case Line.NO_MAJOR: 
			if (v2.y <= v1.y) renderDown();
			else renderUp();
			break; //Defaults to up-down since that's the standard way of drawing
		case Line.X_MAJOR: 
			if (v2.y <= v1.y) renderDown();
			else renderUp();
			break;
		case Line.Y_MAJOR:
			if (v2.x <= v1.x) renderRight();
			else renderLeft();
			break;
		}
		
	}

	/**
	 * Draw the polygon by drawing parallel lines upward (negative Y direction) from the baseline towards vertex 1,
	 * clipping/extending the lines to fill and not exceed the area bounded by the lines connected vertices 1 & 2
	 * and 1 & 3.
	 * <br>Unexpected results may occur if vertex 1 does not have a smaller or equal Y coordinate relative to vertices 2 and 3.
	 * Because of this, the method is private and called only by the constructor during initial rendering.
	 */
	private void renderUp()
	{
		Line l12 = new Line(v1.x, v1.y, v2.x, v2.y);
		Line l31 = new Line(v3.x, v3.y, v1.x, v1.y);
		boolean clockwise = false;
		if (v3.x < v2.x) clockwise = true;
		//It should not be possible for v3.x to equal v2.x
			// This method is only called if l23 is major x
		
		//Boundaries
		Map<Integer,Integer> top1 = l12.map_to_X(true, true);
		Map<Integer,Integer> top2 = l31.map_to_X(true, true);
		Map<Integer,Integer> leftBound;
		Map<Integer,Integer> rightBound;
		
		if (clockwise)
		{
			leftBound = l31.map_to_Y(true, true);
			rightBound = l12.map_to_Y(false, true);
		}
		else
		{
			leftBound = l12.map_to_Y(true, true);
			rightBound = l31.map_to_Y(false, true);
		}
		
		//Initialize data array
		int sz = 1;
		int y = v2.y;
		if (v3.y < v2.y)
		{
			sz = (v3.y - v1.y) + 1;
			y = v3.y;
		}
		else sz += (v2.y - v1.y);
		data = new ArrayList<Line>(sz);
		data.add(baseline);	
		int off = -1;
		y--;
		
		while (y >= v1.y)
		{
			Line l = baseline.createParallelLine(new Point(0, off));
			//Trim & Extend
			l.clipAbove(top1, false);
			l.clipAbove(top2, false);
			l.extendLeft(leftBound, false);
			l.extendRight(rightBound, false);
			off--;
			y--;
		}
		
	}
	
	/**
	 * Draw the polygon by drawing parallel lines downward (positive Y direction) from the baseline towards vertex 1,
	 * clipping/extending the lines to fill and not exceed the area bounded by the lines connected vertices 1 & 2
	 * and 1 & 3.
	 * <br>Unexpected results may occur if vertex 1 does not have a larger or equal Y coordinate relative to vertices 2 and 3.
	 * Because of this, the method is private and called only by the constructor during initial rendering.
	 */
	private void renderDown()
	{
		Line l12 = new Line(v1.x, v1.y, v2.x, v2.y);
		Line l31 = new Line(v3.x, v3.y, v1.x, v1.y);
		boolean clockwise = false;
		if (v3.x > v2.x) clockwise = true;
		//It should not be possible for v3.x to equal v2.x
			// This method is only called if l23 is major x
		
		//Boundaries
		Map<Integer,Integer> bot1 = l12.map_to_X(false, true);
		Map<Integer,Integer> bot2 = l31.map_to_X(false, true);
		Map<Integer,Integer> leftBound;
		Map<Integer,Integer> rightBound;
		
		if (clockwise)
		{
			leftBound = l12.map_to_Y(true, true);
			rightBound = l31.map_to_Y(false, true);
		}
		else
		{
			leftBound = l31.map_to_Y(true, true);
			rightBound = l12.map_to_Y(false, true);
		}
		
		//Initialize data array
		int sz = 1;
		int y = v2.y;
		if (v3.y > v2.y)
		{
			sz += (v1.y - v3.y);
			y = v3.y;
		}
		else sz += (v1.y - v2.y);
		data = new ArrayList<Line>(sz);
		data.add(baseline);	
		int off = 1;
		y++;
		
		while (y <= v1.y)
		{
			Line l = baseline.createParallelLine(new Point(0, off));
			//Trim & Extend
			l.clipBelow(bot1, false);
			l.clipBelow(bot2, false);
			l.extendLeft(leftBound, false);
			l.extendRight(rightBound, false);
			off++;
			y++;
		}
	}
	
	/**
	 * Draw the polygon by drawing parallel lines right-ward (positive X direction) from the baseline towards vertex 1,
	 * clipping/extending the lines to fill and not exceed the area bounded by the lines connected vertices 1 & 2
	 * and 1 & 3.
	 * <br>Unexpected results may occur if vertex 1 does not have a larger or equal X coordinate relative to vertices 2 and 3.
	 * Because of this, the method is private and called only by the constructor during initial rendering.
	 */
	private void renderRight()
	{
		Line l12 = new Line(v1.x, v1.y, v2.x, v2.y);
		Line l31 = new Line(v3.x, v3.y, v1.x, v1.y);
		boolean clockwise = false;
		if (v2.y > v3.y) clockwise = true;
		//It should not be possible for v3.x to equal v2.x
			// This method is only called if l23 is major x
		
		//Boundaries
		Map<Integer,Integer> right1 = l12.map_to_Y(false, true);
		Map<Integer,Integer> right2 = l31.map_to_Y(false, true);
		Map<Integer,Integer> topBound;
		Map<Integer,Integer> botBound;
		
		if (clockwise)
		{
			topBound = l12.map_to_X(true, true);
			botBound = l31.map_to_X(false, true);
		}
		else
		{
			topBound = l31.map_to_X(true, true);
			botBound = l12.map_to_X(false, true);
		}
		
		//Initialize data array
		int sz = 1;
		int x = v2.x;
		if (v3.x > v2.x)
		{
			sz += (v1.x - v3.x);
			x = v3.x;
		}
		else sz += (v1.x - v2.x);
		data = new ArrayList<Line>(sz);
		data.add(baseline);	
		int off = 1;
		x++;
		
		while (x <= v1.x)
		{
			Line l = baseline.createParallelLine(new Point(off, 0));
			//Trim & Extend
			l.clipRight(right1, false);
			l.clipRight(right2, false);
			l.extendUp(topBound, false);
			l.extendDown(botBound, false);
			off++;
			x++;
		}
	}
	
	/**
	 * Draw the polygon by drawing parallel lines left-ward (negative X direction) from the baseline towards vertex 1,
	 * clipping/extending the lines to fill and not exceed the area bounded by the lines connected vertices 1 & 2
	 * and 1 & 3.
	 * <br>Unexpected results may occur if vertex 1 does not have a smaller or equal X coordinate relative to vertices 2 and 3.
	 * Because of this, the method is private and called only by the constructor during initial rendering.
	 */
	private void renderLeft()
	{
		Line l12 = new Line(v1.x, v1.y, v2.x, v2.y);
		Line l31 = new Line(v3.x, v3.y, v1.x, v1.y);
		boolean clockwise = false;
		if (v2.y < v3.y) clockwise = true;
		//It should not be possible for v3.y to equal v2.y
			// This method is only called if l23 is major y
		
		//Boundaries
		Map<Integer,Integer> left1 = l12.map_to_Y(true, true);
		Map<Integer,Integer> left2 = l31.map_to_Y(true, true);
		Map<Integer,Integer> topBound;
		Map<Integer,Integer> botBound;
		
		if (clockwise)
		{
			topBound = l31.map_to_X(true, true);
			botBound = l12.map_to_X(false, true);
		}
		else
		{
			topBound = l12.map_to_X(true, true);
			botBound = l31.map_to_X(false, true);
		}
		
		//Initialize data array
		int sz = 1;
		int x = v2.x;
		if (v3.x < v2.x)
		{
			sz += (v3.x - v1.x);
			x = v3.x;
		}
		else sz += (v2.x - v1.x);
		data = new ArrayList<Line>(sz);
		data.add(baseline);	
		int off = -1;
		x--;
		
		while (x >= v1.x)
		{
			Line l = baseline.createParallelLine(new Point(off, 0));
			//Trim & Extend
			l.clipLeft(left1, false);
			l.clipLeft(left2, false);
			l.extendUp(topBound, false);
			l.extendDown(botBound, false);
			off--;
			x--;
		}
	}
	
	/* ----- Data Access ----- */
	
	/**
	 * Get a MappedPixel from the polygon by specifying the "row"/line index and the index of the
	 * pixel within that row.
	 * <br><br><b>CAUTION!</b> The coordinates mapped internally in the pixel are relative only to the
	 * line containing the pixel. Attempting to draw the pixel without knowing the containing line's 
	 * global offsets will yield undesired results.
	 * <br><br><b>CAUTION!</b> This method has no check for the validity of the indices. It will
	 * merely throw an exception that must be handled by the caller.
	 * @param row Index of row to retrieve pixel from.
	 * @param index Index of pixel within line to retrieve.
	 * @return Raw MappedPixel reference of indexed pixel.
	 */
	public MappedPixel getPixel(int row, int index)
	{
		return data.get(row).getPixel(index);
	}
	
	/**
	 * Get a full "row" from this Polygon as a Line object by specifying the desired row index.
	 * <br><br><b>CAUTION!</b> This method has no check for the validity of the row index. It will
	 * merely throw an exception that must be handled by the caller.
	 * @param row Index of row in polygon to retrieve.
	 * @return A reference to the Line containing the Row of Polygon pixels.
	 */
	public Line getRow(int row)
	{
		return data.get(row);
	}
	
	/**
	 * Get the number of rows that compose this polygon.
	 * @return Number of rows (Line objects) in polygon.
	 */
	public int countRows()
	{
		return data.size();
	}
	
	/* ----- Distances ----- */
	
	/**
	 * Get the direct distance, as a double, from the provided point to vertex 1 of this polygon.
	 * @param x X-coordinate of point to check distance of.
	 * @param y Y-coordinate of point to check distance of.
	 * @return The distance between the provided point and vertex 1 with pixels being the unit of
	 * measurement. Note that this is the raw distance calculated using the Pythagorean theorem,
	 * not the number of pixels that would be required to draw a line between the points!
	 */
	public double distance_v1(int x, int y)
	{
		int dx = Math.abs(v1.x - x);
		int dy = Math.abs(v1.y - y);
		double dist = Math.sqrt(Math.pow((double)dx, 2.0) + Math.pow((double)dy, 2.0));
		return dist;
	}
	
	/**
	 * Get the direct distance, as a double, from the provided point to vertex 2 of this polygon.
	 * @param x X-coordinate of point to check distance of.
	 * @param y Y-coordinate of point to check distance of.
	 * @return The distance between the provided point and vertex 2 with pixels being the unit of
	 * measurement. Note that this is the raw distance calculated using the Pythagorean theorem,
	 * not the number of pixels that would be required to draw a line between the points!
	 */
	public double distance_v2(int x, int y)
	{
		int dx = Math.abs(v2.x - x);
		int dy = Math.abs(v2.y - y);
		double dist = Math.sqrt(Math.pow((double)dx, 2.0) + Math.pow((double)dy, 2.0));
		return dist;
	}
	
	/**
	 * Get the direct distance, as a double, from the provided point to vertex 3 of this polygon.
	 * @param x X-coordinate of point to check distance of.
	 * @param y Y-coordinate of point to check distance of.
	 * @return The distance between the provided point and vertex 3 with pixels being the unit of
	 * measurement. Note that this is the raw distance calculated using the Pythagorean theorem,
	 * not the number of pixels that would be required to draw a line between the points!
	 */
	public double distance_v3(int x, int y)
	{
		int dx = Math.abs(v3.x - x);
		int dy = Math.abs(v3.y - y);
		double dist = Math.sqrt(Math.pow((double)dx, 2.0) + Math.pow((double)dy, 2.0));
		return dist;
	}
	
	/* ----- Modify ----- */
	
	/**
	 * Apply a solid color to the entire polygon.
	 * @param r Red value of solid color to apply.
	 * @param g Green value of solid color to apply.
	 * @param b Blue value of solid color to apply.
	 * @param a Alpha (transparency) value of solid color to apply. Set to 255 for full opacity.
	 */
	public void color(int r, int g, int b, int a)
	{
		for (Line l : data) l.setColor(r, g, b, a);
	}
	
	/**
	 * Apply a texture pattern to the polygon, specified by another polygon.
	 * @param texture Texture to scale and apply to this polygon.
	 * @param blur Whether to apply blurring to the texture when rescaling it to fit
	 * this polygon.
	 */
	public void applyTexture(Polygon texture, boolean blur)
	{
		int rows = data.size();
		
		//Condense rows/lines
		List<Line> scaledRows = Scale.scale_2D(texture.data, rows, blur);
		
		//Apply line by line
		for (int i = 0; i < rows; i++) data.get(i).applyTexture(scaledRows.get(i), blur);
		
	}
	
	/**
	 * Apply a linear gradient to the polygon by specifying color values for each
	 * of the three vertices and interpolating all values in between.
	 * @param c1 Color to set for vertex 1.
	 * @param c2 Color to set for vertex 2.
	 * @param c3 Color to set for vertex 3.
	 */
	public void gouraudShade(Pixel c1, Pixel c2, Pixel c3)
	{
		for (Line l : data)
		{
			int len = l.getPixelCount();
			for(int i = 0; i < len; i++)
			{
				MappedPixel mp = l.getPixel(i);
				double d1 = this.distance_v1(mp.getX(), mp.getY());
				double d2 = this.distance_v2(mp.getX(), mp.getY());
				double d3 = this.distance_v3(mp.getX(), mp.getY());
				double tot = d1 + d2 + d3;
				double p1 = d1/tot;
				double p2 = d2/tot;
				double p3 = d3/tot;
				int red = (int)(((double)c1.getRed() * p1) + ((double)c2.getRed() * p2) + ((double)c3.getRed() * p3));
				int green = (int)(((double)c1.getGreen() * p1) + ((double)c2.getGreen() * p2) + ((double)c3.getGreen() * p3));
				int blue = (int)(((double)c1.getBlue() * p1) + ((double)c2.getBlue() * p2) + ((double)c3.getBlue() * p3));
				int alpha = (int)(((double)c1.getAlpha() * p1) + ((double)c2.getAlpha() * p2) + ((double)c3.getAlpha() * p3));
				mp.setRed(red);
				mp.setGreen(green);
				mp.setBlue(blue);
				mp.setAlpha(alpha);
				mp.saturate8();
			}
		}
	}

	/**
	 * Blend a second polygon into this polygon by the amount specified in factor.
	 * @param top Polygon to blend into this polygon.
	 * @param factor Proportion of top polygon to use in blend. Must be between 0.0 and 1.0.
	 * This function will ceiling or floor if the factor value is out of range.
	 * @param scaleblur Whether to apply blurring in the event of rescaling.
	 */
	public void blend(Polygon top, double factor, boolean scaleblur)
	{
		int mrows = data.size();
		int orows = top.data.size();
		
		if (mrows == orows)
		{
			for (int r = 0; r < mrows; r++)
			{
				Line ml = data.get(r);
				Line ol = top.data.get(r);
				ml.blend(ol, factor, scaleblur);
			}
		}
		else
		{
			List<Line> scaledRows = Scale.scale_2D(top.data, mrows, scaleblur);	
			for (int r = 0; r < mrows; r++)
			{
				Line ml = data.get(r);
				Line ol = scaledRows.get(r);
				ml.blend(ol, factor, scaleblur);
			}
		}
	
	}
	
	/**
	 * Delete (clip) all pixels that fall left of the x coordinate provided. The pixels falling
	 * on the vertical line defined by x are not clipped out.
	 * @param x New hard left boundary (farthest left x coordinate remaining in polygon).
	 */
	public void clipLeftOf(int x)
	{
		for (Line l : data) l.clipLeftOf(x);
	}
	
	/**
	 * Delete (clip) all pixels that fall right of the x coordinate provided. The pixels falling
	 * on the vertical line defined by x are not clipped out.
	 * @param x New hard right boundary (farthest right x coordinate remaining in polygon).
	 */
	public void clipRightOf(int x)
	{
		for (Line l : data) l.clipRightOf(x);
	}
	
	/**
	 * Delete (clip) all pixels that fall above of the y coordinate provided. The pixels falling
	 * on the horizontal line defined by y are not clipped out.
	 * @param y New hard top boundary.
	 */
	public void clipAbove(int y)
	{
		for (Line l : data) l.clipAbove(y);
	}
	
	/**
	 * Delete (clip) all pixels that fall below of the y coordinate provided. The pixels falling
	 * on the horizontal line defined by y are not clipped out.
	 * @param y New hard bottom boundary.
	 */
	public void clipBelow(int y)
	{
		for (Line l : data) l.clipBelow(y);
	}
	
	/* ----- Copy ----- */
	
	/**
	 * Create a copy of this polygon independent of any references used by this polygon.
	 * Modification of the copy should not affect the original polygon.
	 * @return Copy of this polygon.
	 */
	public Polygon copy()
	{
		Polygon cpy = new Polygon();
		cpy.v1 = new Point(v1.x, v1.y);
		cpy.v2 = new Point(v2.x, v2.y);
		cpy.v3 = new Point(v3.x, v3.y);
		cpy.data = new ArrayList<Line>(this.data.size());
		for (Line l : data) data.add(l.copy());
		cpy.baseline = cpy.data.get(0);
		return cpy;
	}

	/* ----- Pixel Iteration ----- */
	
	public Iterator<PolygonPixel> iterator() 
	{
		return iterator;
	}
	
}
