package de.wwu.muggl.symbolic.generating;

/**
 * Interface for generator providers.<br />
 * <br />
 * Generators are unlikely to have empty constructors. They will have a constructor
 * that accepts arguments and or multiple constructors to offer customizability.
 * In order to be able to utilize a generator with customizable settings and not
 * having the need to put the setting into the generator itself, generators cannot
 * be directly used but need an appropriate GeneratorProvider that instantiates
 * them. These providers have to implement this interface and to have an empty
 * constructor in order to be accepted as feasible providers.
 *
 * @see de.wwu.muggl.symbolic.generating.Generator
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface GeneratorProvider extends GenericGeneratorProvider {

	/**
	 * Provide the instance of a Generator.
	 *
	 * @param parameterName The name of the parameter.
	 * @return The instance of a Generator.
	 */
	Generator provideInstance(String parameterName);

}
