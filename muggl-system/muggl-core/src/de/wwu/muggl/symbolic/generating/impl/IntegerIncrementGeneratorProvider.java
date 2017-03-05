package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;

/**
 * This is the GeneratorProvider for an IntegerIncrementGenerator.
 *
 * @see de.wwu.muggl.symbolic.generating.impl.IntegerIncrementGenerator
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class IntegerIncrementGeneratorProvider implements GeneratorProvider {

	/**
	 * Provide the instance of a IntegerIncrementGenerator with custom settings.
	 * The first Integer will have a value of 0 and each successor will be
	 * increased by 1. The maximum value is 10000.
	 *
	 * @param parameterName The name of the parameter. Will be ignored.
	 * @return The instance of a IntegerIncrementGenerator.
	 */
	public Generator provideInstance(String parameterName) {
		return new IntegerIncrementGenerator(0, false, 1, 10000);
	}

	/**
	 * Get the full path of the generator, including the packages it is in
	 * and ".class".
	 *
	 * @return The full path of the generator
	 */
	public String getFullGeneratorPath() {
		return "de.wwu.muggl.symbolic.generating.impl.IncrementGenerator.class";
	}

	/**
	 * Get a description of what the generator provider does and how it works.
	 * @return A description of the generator provider.
	 */
	public String getDescription() {
		return "Provides an IntegerIncrementGenerator with default settings "
			+ "(starts with zero, value is in incremented by one with each request).";
	}
}
