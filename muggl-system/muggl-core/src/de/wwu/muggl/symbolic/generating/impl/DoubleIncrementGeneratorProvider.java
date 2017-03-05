package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;

/**
 * This is the GeneratorProvider for an DoubleIncrementGenerator.
 *
 * @see de.wwu.muggl.symbolic.generating.impl.DoubleIncrementGenerator
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-08
 */
public class DoubleIncrementGeneratorProvider implements GeneratorProvider {

	/**
	 * Provide the instance of a DoubleIncrementGenerator with custom settings. The first Double
	 * will have a value of 0.0 and each successor will be increased by 1.0. The maximum value is
	 * 1000.0. It will also return Double.NaN, Double.POSITIVE_INFINITY and
	 * DOUBLE.NEGATIVE_INFINITY.
	 *
	 * @param parameterName The name of the parameter. Will be ignored.
	 * @return The instance of a DoubleIncrementGenerator.
	 */
	public Generator provideInstance(String parameterName) {
		return new DoubleIncrementGenerator(0.0, false, 1.0, 1000.0, true);
	}

	/**
	 * Get the full path of the generator, including the packages it is in
	 * and ".class".
	 *
	 * @return The full path of the generator
	 */
	public String getFullGeneratorPath() {
		return "de.wwu.muggl.symbolic.generating.impl.DoubleIncremenetGenerator.class";
	}

	/**
	 * Get a description of what the generator provider does and how it works.
	 * @return A description of the generator provider.
	 */
	public String getDescription() {
		return "Provides an DoubleIncrementGenerator with default settings "
			+ "(starts with zero, value is incremented by one with each request), "
			+ "beside the fact that it will return special values.";
	}
}
