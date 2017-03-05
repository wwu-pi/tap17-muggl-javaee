package de.wwu.muggl.symbolic.generating.impl;

import java.util.Random;

import de.wwu.muggl.symbolic.generating.ArrayElementsGenerator;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * This ArrayElementsGenerator provides random constant Integer array elements.<br>
 *<br>
 * Example: If four elements are requested the following elements could be provided:
 * <ul>
 * <li>124</li>
 * <li>235</li>
 * <li>0</li>
 * <li>23</li>
 * </ul>
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-01-05
 */
public class RandomConstantIntegerArrayElementsGenerator implements ArrayElementsGenerator {
	// Fields.
	private int lowerBound;
	private int upperBound;
	private int elementsRequested;
	private Random rand;
	
	/**
	 * Construct the array elements generator by specifying the order.
	 * @param lowerBound The lowest number provided.
	 * @param upperBound The highest number provided.
	 */
	public RandomConstantIntegerArrayElementsGenerator(int lowerBound, int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound + 1;
		this.elementsRequested = 0;
		this.rand = new Random();
	}

	/**
	 * Check if this generator can provide another element.
	 * @return true.
	 */
	public boolean hasAnotherElement() {
		return true;
	}

	/**
	 * Provide a generated element of type Term.
	 *
	 * @param index The index the element requested will take in the array. Will be ignored.
	 * @return A generated element of type Term.
	 * @throws IllegalStateException
	 *             If no more elements are available. This cannot happen if checking the availability with
	 *             hasAnotherElement() first and not having more than one thread access this generator.
	 */
	public Term provideElement(int index) {
		int randomValue = this.rand.nextInt(this.upperBound - this.lowerBound) - this.lowerBound;
		Term term = IntConstant.getInstance(randomValue);
		this.elementsRequested++;
		return term;
	}

	/**
	 * Reset the internal element count. The next element provided will be an unbound variable.
	 */
	public void reset() {
		this.elementsRequested = 0;
	}

	/**
	 * Get the name of the ArrayElementsGenerator.
	 * @return "Ordered array elements generator".
	 */
	public String getName() {
		return "Random constant Integer array elements generator";
	}

	/**
	 * Get a description of what the array elements generator does and how it works.
	 * @return A description of the arary elements generator.
	 */
	public String getDescription() {
		return "Provides random constant Integer array elements.\n\n"
			+ "Example: If four elements are requested the following elements are provided:\n"
			+ "124\n"
			+ "235\n"
			+ "0\n"
			+ "23\n\n"
			+ "Upper and lower bounds can be configured.";
	}

}
