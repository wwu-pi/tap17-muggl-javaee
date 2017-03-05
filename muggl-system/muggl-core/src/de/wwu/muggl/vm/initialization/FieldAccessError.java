package de.wwu.muggl.vm.initialization;

import de.wwu.muggl.configuration.MugglError;

/**
 * Exception that is to be thrown when trying to fetch or modify the value of a Field that
 * does not belong to the class accessed.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class FieldAccessError extends MugglError {

	/**
	 * Constructs a new error with null as its detail message.
	 *
	 * @see Error#Error()
	 */
	public FieldAccessError() {
		super();
	}

	/**
	 * Constructs a new error with the specified detail message.
	 *
	 * @param message the detail message.
	 * @see Error#Error(String)
	 */
	public FieldAccessError(String message) {
		super(message);
	}

	/**
	 * Constructs a new error with the specified detail message and cause.
	 *
	 * @param  message the detail message.
     * @param  cause the cause.
	 * @see Error#Error(String, Throwable)
	 */
	public FieldAccessError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new error with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt>.
     *
     * @param  cause the cause.
     * @see Error#Error(Throwable)
	 */
	public FieldAccessError(Throwable cause) {
		super(cause);
	}
}
