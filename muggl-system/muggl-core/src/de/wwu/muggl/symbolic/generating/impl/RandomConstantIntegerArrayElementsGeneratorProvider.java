package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.ArrayElementsGenerator;
import de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider;

/**
 * This GeneratorProvider for an RandomConstantIntegerArrayElementsGenerator.
 *
 * @see de.wwu.muggl.symbolic.generating.impl.RandomConstantIntegerArrayElementsGenerator
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-01-05
 */
public class RandomConstantIntegerArrayElementsGeneratorProvider implements ArrayElementsGeneratorProvider {

	/**
	 * Provide the instance of a RandomConstantIntegerArrayElementsGenerator.
	 *
	 * @param arrayName The name of the array element (not used).
	 * @return The instance of a RandomConstantIntegerArrayElementsGenerator.
	 */
	public ArrayElementsGenerator provideInstance(String arrayName) {
		return new RandomConstantIntegerArrayElementsGenerator(0, 255);
	}

	/**
	 * Get the full path of the array elements generator, including the packages it is in
	 * and ".class".
	 * @return The full path of the array elements generator
	 */
	public String getFullGeneratorPath() {
		return "de.wwu.muggl.symbolic.generating.impl.RandomConstantIntegerArrayElementsGenerator.class";
	}

	/**
	 * Get a description of what the array elements generator provider does and how it works.
	 * @return A description of the array elements generator provider.
	 */
	public String getDescription() {
		return "Provides an RandomConstantIntegerArrayElementsGenerator that generates elements between 0 and 255.";
	}

}
