package de.wwu.muggl.configuration;

/**
 * Abstract super error for any error thrown within this application. Any error
 * defined in this application should inherit from this one.
 *
 * @see MugglException
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public abstract class MugglError extends Error {

	/**
	 * Constructs a new error with null as its detail message.
	 *
	 * @see Error#Error()
	 */
	public MugglError() {
		super();
	}

	/**
	 * Constructs a new error with the specified detail message.
	 *
	 * @param message the detail message.
	 * @see Error#Error(String)
	 */
	public MugglError(String message) {
		super(message);
	}

	/**
	 * Constructs a new error with the specified detail message and cause.
	 *
	 * @param  message the detail message.
     * @param  cause the cause.
	 * @see Error#Error(String, Throwable)
	 */
	public MugglError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new error with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt>.
     *
     * @param  cause the cause.
     * @see Error#Error(Throwable)
	 */
	public MugglError(Throwable cause) {
		super(cause);
	}
}
