package de.wwu.muggl.instructions.interfaces.control;

/**
 * Interface that is to be implemented by any instruction that can throw an exception
 * which will disrupt the control flow. If this interface is implemented, the interfaces
 * JumpNever must not be implemented. Any other jumping interfaces may also be implemented.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface JumpException {

	/**
	 * Get the thrown exception types as fully qualified java names (like
	 * java.lang.NullPointerException).
	 * @return The thrown exception types.
	 */
	String[] getThrownExceptionTypes();

}
