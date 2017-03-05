package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.ArrayElementsGenerator;
import de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;

/**
 * This GeneratorProvider for an OrderedArrayElementsGenerator.
 *
 * @see de.wwu.muggl.symbolic.generating.impl.OrderedArrayElementsGenerator
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-08
 */
public class OrderedArrayElementsGeneratorProvider implements ArrayElementsGeneratorProvider {

	/**
	 * Provide the instance of a OrderedArrayElementsGenerator.
	 *
	 * @param arrayName The name of the array element.
	 * @return The instance of a OrderedArrayElementsGenerator.
	 */
	public ArrayElementsGenerator provideInstance(String arrayName) {
		NumericVariable variable = new NumericVariable(arrayName + "[]", Expression.INT, false);
		return new OrderedArrayElementsGenerator(variable, false);
	}

	/**
	 * Get the full path of the array elements generator, including the packages it is in
	 * and ".class".
	 * @return The full path of the array elements generator
	 */
	public String getFullGeneratorPath() {
		return "de.wwu.muggl.symbolic.generating.impl.OrderedArrayElementsGenerator.class";
	}

	/**
	 * Get a description of what the array elements generator provider does and how it works.
	 * @return A description of the array elements generator provider.
	 */
	public String getDescription() {
		return "Provides an OrderedArrayElementsGenerator that generates elements in ascending order";
	}

}
