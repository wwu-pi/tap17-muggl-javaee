package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;

/**
 * This is the GeneratorProvider for an DoubleMultiplicationGenerator.
 *
 * @see de.wwu.muggl.symbolic.generating.impl.DoubleMultiplicationGenerator
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class DoubleMultiplicationGeneratorProvider implements GeneratorProvider {

	/**
	 * Provide the instance of a DoubleMultiplicationGenerator with custom settings. The first Double
	 * will have a value of 1.0 and each successor will be Multiplied with 2.0. The maximum value is
	 * 4,294,967,296. It will also return Double.NaN, Double.POSITIVE_INFINITY and
	 * DOUBLE.NEGATIVE_INFINITY.
	 *
	 * @param parameterName The name of the parameter. Will be ignored.
	 * @return The instance of a DoubleIncrementGenerator.
	 */
	public Generator provideInstance(String parameterName) {
		return new DoubleMultiplicationGenerator(1.0, 2.0, 4294967296.0, true);
	}

	/**
	 * Get the full path of the generator, including the packages it is in
	 * and ".class".
	 *
	 * @return The full path of the generator
	 */
	public String getFullGeneratorPath() {
		return "de.wwu.muggl.symbolic.generating.impl.DoubleMultiplicationGenerator.class";
	}

	/**
	 * Get a description of what the generator provider does and how it works.
	 * @return A description of the generator provider.
	 */
	public String getDescription() {
		return "Provides an DoubleMultiplicationGenerator with default settings "
			+ "(starts with zero, value multiplicated with two with each request), "
			+ "beside the fact that it will return special values.";
	}
}
