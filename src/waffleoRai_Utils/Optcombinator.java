package waffleoRai_Utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used for calculating optimum combinations of values
 * to obtain a target value.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since November 11, 2017
 *
 */
public class Optcombinator {

	public static final int MODE_SUM = 0;
	public static final int MODE_PRODUCT = 1;
	
	public static final int SORT_PREFER_MINIMIZATION = 2;
	public static final int SORT_PREFER_MAXIMIZATION = 3;
	
	private int[] inputNumbers;
	private int mode;
	
	/**
	 * Construct a new Optcombinator from an array of seed input values and a mode
	 * to generate an output of input combinations.
	 * @param numbers Values to combine and evaluate.
	 * @param mode Mode of evaluation. For example, if MODE_SUM is set as the mode, then evaluations
	 * on the quality of the combinations will be based on the sums of different combinations of
	 * input values.
	 */
	public Optcombinator(int[] numbers, int mode)
	{
		if (numbers == null) throw new IllegalArgumentException();
		if (numbers.length == 0) throw new IllegalArgumentException();
		inputNumbers = numbers;
		switch(mode)
		{
		case MODE_SUM:
			this.mode = mode;
			break;
		case MODE_PRODUCT:
			this.mode = mode;
			break;
		default:
			this.mode = MODE_SUM;
			break;
		}
	}
	
	private class Combination implements Comparable<Combination>
	{
		private int[] indices;
		private int output;
		
		private int sortMode;
		
		public Combination(int[] inputIndices)
		{
			if (inputIndices == null) throw new IllegalArgumentException();
			if (inputIndices.length == 0) throw new IllegalArgumentException();
			indices = inputIndices;
			sortMode = SORT_PREFER_MINIMIZATION;
			calcOutput();
		}
		
		private void calcOutput()
		{
			switch(mode)
			{
			case MODE_SUM:
				output = 0;
				for (int i : indices)
				{
					int val = inputNumbers[i];
					output += val;
				}
				break;
			case MODE_PRODUCT:
				output = 1;
				for (int i : indices)
				{
					int val = inputNumbers[i];
					output *= val;
				}
				break;
			}
		}

		public int getOutput()
		{
			return output;
		}

		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null) return false;
			if (!(o instanceof Combination)) return false;
			Combination c = (Combination)o;
			if (c.getOutput() != this.getOutput()) return false;
			if (c.indices.length != this.indices.length) return false;
			if (c.sortMode != this.sortMode) return false;
			for (int i = 0; i < this.indices.length; i++)
			{
				if (c.indices[i] != this.indices[i]) return false;
			}
			return true;
		}
		
		public int compareTo(Combination o) 
		{
			if (o == this) return 0;
			if (o == null) return 1;
			if (this.equals(o)) return 0;
			//Compare output (most important)
			if (this.output > o.output) return 1;
			if (this.output < o.output) return -1;
			//Compare indices array lengths (by sort mode)
			if (this.indices.length > o.indices.length)
			{
				if (sortMode == SORT_PREFER_MINIMIZATION) return 1;
				if (sortMode == SORT_PREFER_MAXIMIZATION) return -1;
			}
			if (this.indices.length < o.indices.length)
			{
				if (sortMode == SORT_PREFER_MINIMIZATION) return -1;
				if (sortMode == SORT_PREFER_MAXIMIZATION) return 1;
			}
			//Compare indices
			for (int i = 0; i < this.indices.length; i++)
			{
				if (this.indices[i] > o.indices[i]) return 1;
				if (this.indices[i] < o.indices[i]) return 1;
			}
			return 0;
		}
		
	}
	
	/**
	 * Add an integer to the beginning of an array. This will generate a new array of length
	 * one larger than the provided array.
	 * @param array Array to add an element to the beginning (index 0) of.
	 * @param newNumber Number to add to the beginning of the array.
	 * @return An array one element larger than the provided array with the specified number
	 * set to index 0, and everything previously in the array set to one index higher than before.
	 */
	private static int[] addToArrayStart(int[] array, int newNumber)
	{
		if (array != null && array.length > 0)
		{
			int[] newArr = new int[array.length + 1];
			newArr[0] = newNumber;
			for (int i = 0; i < array.length; i++) newArr[i+1] = array[i];
			return newArr;
		}
		else
		{
			int[] newArr = new int[1];
			newArr[0] = newNumber;
			return newArr;
		}
	}
	
	/**
	 * Generate a list of all possible unique order irrelevant
	 * combinations of nMembers indices for the internal array of numbers starting at element start.
	 * @param nMembers The number of elements in each combination.
	 * @param start The index of the Optcombinator's array to start consideration of potential
	 * elements from. All indices before this will be ignored.
	 * @return A List of all possible combinations, given the parameters, of indices - each combination
	 * represented as an array of integers.
	 */
	private List<int[]> generateCombinations(int nMembers, int start)
	{
		//Base cases
		if (nMembers < 1) return null;
		if (start >= inputNumbers.length) return null;
		//Generate list one smaller (recursive)
		List<int[]> combos = new LinkedList<int[]>();
		List<int[]> seedCombos = generateCombinations((nMembers - 1), start + 1);
		if (seedCombos == null) seedCombos = new LinkedList<int[]>();
		for (int i = start; i < inputNumbers.length - (nMembers - 1); i++)
		{
			if (seedCombos.isEmpty())
			{
				combos.add(addToArrayStart(null, i));
			}
			else
			{
				for (int[] c : seedCombos)
				{
					if (c[0] > i)
					{
						combos.add(addToArrayStart(c, i));
					}
				}	
			}
		}
		return combos;
	}

	/**
	 * Get the combination of elements, encoded as a list(array) of indices
	 * in the array provided at construction, whose output as defined by the Optcombinator's mode is as close
	 * to the threshold as possible, without going under.
	 * <br>For example, if the mode is set to MODE_SUM, and an array of 7 values was provided at construction,
	 * this function will attempt to find which combination of those 7 values adds up to a sum that is closest
	 * to the threshold value without being less than the threshold value. The return value will be
	 * an array of the indices within the original array of the elements used to comprise that
	 * choice combination.
	 * @param threshold Value that combination of values must be at or above - preference given to values
	 * as little above the threshold as possible.
	 * @param minimizeComponents Whether combinations made of few elements are preferred over combinations
	 * made of many elements.
	 * @return An array of the indices of the elements whose combination appears to be closest to
	 * the threshold value without going under.
	 * <br>null: If no such combination is found.
	 */
	public int[] getClosestOrOver(int threshold, boolean minimizeComponents)
	{
		int sortmode = SORT_PREFER_MINIMIZATION;
		if (!minimizeComponents) sortmode = SORT_PREFER_MAXIMIZATION;
		List<Combination> combos = new LinkedList<Combination>();
		for (int i = 1; i <= inputNumbers.length; i++)
		{
			List<int[]> rawCombos = generateCombinations(i, 0);
			for (int[] a : rawCombos)
			{
				//Generate Combination and see if it passes basic test.
				Combination c = new Combination(a);
				c.sortMode = sortmode;
				//If it does, then put in list
				if (c.getOutput() >= threshold) combos.add(c);
			}
		}
		if(combos.isEmpty()) return null;
		Collections.sort(combos);
		Combination best = combos.get(0);
		return best.indices;
	}
	
	/**
	 * Get the combination of elements, encoded as a list(array) of indices
	 * in the array provided at construction, whose output as defined by the Optcombinator's mode is as close
	 * to the threshold as possible, without going over.
	 * <br>For example, if the mode is set to MODE_SUM, and an array of 7 values was provided at construction,
	 * this function will attempt to find which combination of those 7 values adds up to a sum that is closest
	 * to the threshold value without being greater than the threshold value. The return value will be
	 * an array of the indices within the original array of the elements used to comprise that
	 * choice combination.
	 * @param threshold Value that combination of values must be at or above - preference given to values
	 * as little below the threshold as possible.
	 * @param minimizeComponents Whether combinations made of few elements are preferred over combinations
	 * made of many elements.
	 * @return An array of the indices of the elements whose combination appears to be closest to
	 * the threshold value without going over.
	 * <br>null: If no such combination is found.
	 */
	public int[] getClosestOrUnder(int threshold, boolean minimizeComponents)
	{
		//Sort mode is superficially swapped so can grab end of list!
		int sortmode = SORT_PREFER_MINIMIZATION;
		if (minimizeComponents) sortmode = SORT_PREFER_MAXIMIZATION;
		List<Combination> combos = new LinkedList<Combination>();
		for (int i = 1; i <= inputNumbers.length; i++)
		{
			List<int[]> rawCombos = generateCombinations(i, 0);
			for (int[] a : rawCombos)
			{
				//Generate Combination and see if it passes basic test.
				Combination c = new Combination(a);
				c.sortMode = sortmode;
				//If it does, then put in list
				if (c.getOutput() <= threshold) combos.add(c);
			}
		}
		if(combos.isEmpty()) return null;
		Collections.sort(combos);
		Combination best = combos.get(combos.size() - 1);
		return best.indices;
	}
	
	/**
	 * Get the combination of elements, encoded as a list(array) of indices
	 * in the array provided at construction, whose output as defined by the Optcombinator's mode is
	 * precisely equal to the provided threshold value.
	 * <br>For example, if the mode is set to MODE_SUM, and an array of 7 values was provided at construction,
	 * this function will attempt to find which combination of those 7 values adds up to a sum that is
	 * exactly the same as the threshold value. The return value will be
	 * an array of the indices within the original array of the elements used to comprise that
	 * choice combination.
	 * @param threshold Value that combination of values must be equal to.
	 * @param minimizeComponents Whether combinations made of few elements are preferred over combinations
	 * made of many elements.
	 * @return An array of the indices of the elements whose combination is equal to the threshold value.
	 * <br>null: If no such combination is found.
	 */
	public int[] getExact(int threshold, boolean minimizeComponents)
	{
		int sortmode = SORT_PREFER_MINIMIZATION;
		if (!minimizeComponents) sortmode = SORT_PREFER_MAXIMIZATION;
		List<Combination> combos = new LinkedList<Combination>();
		for (int i = 1; i <= inputNumbers.length; i++)
		{
			List<int[]> rawCombos = generateCombinations(i, 0);
			for (int[] a : rawCombos)
			{
				//Generate Combination and see if it passes basic test.
				Combination c = new Combination(a);
				c.sortMode = sortmode;
				//If it does, then put in list
				if (c.getOutput() == threshold) combos.add(c);
			}
		}
		if(combos.isEmpty()) return null;
		Collections.sort(combos);
		Combination best = combos.get(0);
		return best.indices;
	}
	
	/**
	 * Get the combination of elements, encoded as a list(array) of indices
	 * in the array provided at construction, whose output as defined by the Optcombinator's mode
	 * is the smallest of any combination.
	 * <br>For example, if the mode is set to MODE_SUM, and an array of 7 values was provided at construction,
	 * this function will attempt to find which combination of those 7 values adds up to a sum that is
	 * smaller than the sum of any other combination. The return value will be
	 * an array of the indices within the original array of the elements used to comprise that
	 * choice combination.
	 * @param minimizeComponents Whether combinations made of few elements are preferred over combinations
	 * made of many elements.
	 * @return An array of the indices of the elements whose combination appears to yield the smallest value.
	 * <br>null: If no such combination is found.
	 */
	public int[] getSmallest(boolean minimizeComponents)
	{
		int sortmode = SORT_PREFER_MINIMIZATION;
		if (!minimizeComponents) sortmode = SORT_PREFER_MAXIMIZATION;
		List<Combination> combos = new LinkedList<Combination>();
		for (int i = 1; i <= inputNumbers.length; i++)
		{
			List<int[]> rawCombos = generateCombinations(i, 0);
			for (int[] a : rawCombos)
			{
				//Generate Combination
				Combination c = new Combination(a);
				c.sortMode = sortmode;
			}
		}
		if(combos.isEmpty()) return null;
		Collections.sort(combos);
		Combination best = combos.get(0);
		return best.indices;
	}
	
	/**
	 * Get the combination of elements, encoded as a list(array) of indices
	 * in the array provided at construction, whose output as defined by the Optcombinator's mode
	 * is the largest of any combination.
	 * <br>For example, if the mode is set to MODE_SUM, and an array of 7 values was provided at construction,
	 * this function will attempt to find which combination of those 7 values adds up to a sum that is
	 * larger than the sum of any other combination. The return value will be
	 * an array of the indices within the original array of the elements used to comprise that
	 * choice combination.
	 * @param minimizeComponents Whether combinations made of few elements are preferred over combinations
	 * made of many elements.
	 * @return An array of the indices of the elements whose combination appears to yield the largest value.
	 * <br>null: If no such combination is found.
	 */
	public int[] getLargest(boolean minimizeComponents)
	{
		//Sort mode is superficially swapped so can grab end of list!
		int sortmode = SORT_PREFER_MINIMIZATION;
		if (minimizeComponents) sortmode = SORT_PREFER_MAXIMIZATION;
		List<Combination> combos = new LinkedList<Combination>();
		for (int i = 1; i <= inputNumbers.length; i++)
		{
			List<int[]> rawCombos = generateCombinations(i, 0);
			for (int[] a : rawCombos)
			{
				Combination c = new Combination(a);
				c.sortMode = sortmode;
			}
		}
		if(combos.isEmpty()) return null;
		Collections.sort(combos);
		Combination best = combos.get(combos.size() - 1);
		return best.indices;
	}
	
}
