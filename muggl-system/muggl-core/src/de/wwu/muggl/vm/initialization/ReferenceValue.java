package de.wwu.muggl.vm.initialization;

/**
 * This interface is implemented by all reference values. These are the classes
 * Arrayref and Objectref.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface ReferenceValue {

	/**
	 * Return true, if this is a reference to an array, or false otherwise.
	 * @return true, if this is a reference to an array, false otherwise.
	 */
	boolean isArray();

	/**
	 * Get the name of this reference value. This is the full class name
	 * including information whether this is an array or not. If it is an
	 * array, the dimensions are also mentioned.
	 *
	 * Examples:
	 * java.lang.String		(reference of java.lang.String)
	 * [Ljava.lang.String	(reference of an one-dimensional array of java.lang.String)
	 * [[[Ljava.lang.String	(reference of a three-dimensional array of java.lang.String)
	 *
	 * @return The name of this reference value.
	 */
	String getName();

	/**
	 * Getter for the corresponding InitializedClass.
	 * @return The corresponding InitializedClass.
	 */
	InitializedClass getInitializedClass();

	/**
	 * Returns true, if the ReferenceValue is wrapping a primitive type.
	 * @return true, if the ReferenceValue is wrapping a primitive type, false otherwise.
	 */
	boolean isPrimitive();

	/**
	 * Getter for the instantiation number. Instantiation numbers can be used
	 * to determine which of two ReferenceValues has been generated earlier.
	 * @return The instantiation number.
	 */
	long getInstantiationNumber();
}
