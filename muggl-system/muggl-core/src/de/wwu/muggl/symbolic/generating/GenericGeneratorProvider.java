package de.wwu.muggl.symbolic.generating;

/**
 * Super interface for any kind of generator providers that the symbolic virtual machine may use.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface GenericGeneratorProvider {

	/**
	 * Get the full path of the generator, including the packages it is in
	 * and ".class". Example:
	 * de.wwu.muggl.symbolic.generating.impl.IncrementGenerator.class
	 * @return The full path of the generator
	 */
	String getFullGeneratorPath();

	/**
	 * Get a description of what the generator provider does and how it works.
	 * @return A description of the generator provider.
	 */
	String getDescription();

}
