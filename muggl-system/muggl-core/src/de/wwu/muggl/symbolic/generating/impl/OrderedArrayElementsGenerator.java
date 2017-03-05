package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.ArrayElementsGenerator;
import de.wwu.muggl.solvers.expressions.Difference;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * This ArrayElementsGenerator provides ordered array elements.<br>
 *<br>
 * Example: If four elements are requested the following elements are provided:
 * <ul>
 * <li>a</li>
 * <li>a + 1</li>
 * <li>a + 2</li>
 * <li>a + 3</li>
 * </ul>
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-01-05
 */
public class OrderedArrayElementsGenerator implements ArrayElementsGenerator {
	// Fields.
	private NumericVariable baseVariable;
	private boolean descending;
	private int elementsRequested;

	/**
	 * Construct the array elements generator by specifying the order.
	 * @param baseVariable The base variable for all elements provided.
	 * @param descending If set to true, elements will be provided in descending order, otherwise the order will be ascending.
	 */
	public OrderedArrayElementsGenerator(NumericVariable baseVariable, boolean descending) {
		this.baseVariable = baseVariable;
		this.descending = descending;
		this.elementsRequested = 0;
	}

	/**
	 * Check if this generator can provide another element.
	 * @return true, if less than Integer.MAX_VALUE elements have been provided.
	 */
	public boolean hasAnotherElement() {
		if (this.elementsRequested == Integer.MAX_VALUE) return false;
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
		if (this.elementsRequested == Integer.MAX_VALUE)
			throw new IllegalStateException("There are no more elements available.");
		Term term;
		if (this.elementsRequested == 0) {
			term = this.baseVariable;
		} else {
			NumericConstant constant = NumericConstant.getInstance(this.elementsRequested, this.baseVariable.getType());
			if (this.descending) {
				term = Difference.newInstance(this.baseVariable, constant);
			} else {
				term = Sum.newInstance(this.baseVariable, constant);
			}
		}

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
		return "Ordered array elements generator";
	}

	/**
	 * Get a description of what the array elements generator does and how it works.
	 * @return A description of the arary elements generator.
	 */
	public String getDescription() {
		return "Provides ordered array elements.\n\n"
			+ "Example: If four elements are requested the following elements are provided:\n"
			+ "a\n"
			+ "a + 1\n"
			+ "a + 2\n"
			+ "a + 3\n\n"
			+ "It also can be configured to provide the elements in descending order.";
	}

}
