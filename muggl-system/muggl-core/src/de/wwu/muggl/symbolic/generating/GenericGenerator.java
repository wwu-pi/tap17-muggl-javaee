package de.wwu.muggl.symbolic.generating;

/**
 * Super interface for any kind of generators that the symbolic virtual machine may use.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface GenericGenerator {

	/**
	 * Get the name of the Generator.
	 * @return The name of the Generator.
	 */
	String getName();

	/**
	 * Get a description of what the generator does and how it works.
	 * @return A description of the generator.
	 */
	String getDescription();
}
