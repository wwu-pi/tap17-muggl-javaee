package de.wwu.muggl.instructions;

import de.wwu.muggl.configuration.MugglError;

/**
 * Exception that is to be thrown on problems resolving a field.
 * For example, it could not be found or might have the wrong visibility.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class FieldResolutionError extends MugglError  {

	/**
	 * Constructs a new error with null as its detail message.
	 *
	 * @see Error#Error()
	 */
	public FieldResolutionError() {
		super();
	}

	/**
	 * Constructs a new error with the specified detail message.
	 *
	 * @param message the detail message.
	 * @see Error#Error(String)
	 */
	public FieldResolutionError(String message) {
		super(message);
	}

	/**
	 * Constructs a new error with the specified detail message and cause.
	 *
	 * @param  message the detail message.
     * @param  cause the cause.
	 * @see Error#Error(String, Throwable)
	 */
	public FieldResolutionError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new error with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt>.
     *
     * @param  cause the cause.
     * @see Error#Error(Throwable)
	 */
	public FieldResolutionError(Throwable cause) {
		super(cause);
	}
}
