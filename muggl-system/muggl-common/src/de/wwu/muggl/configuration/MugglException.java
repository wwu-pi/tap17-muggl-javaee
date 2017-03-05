package de.wwu.muggl.configuration;

/**
 * Abstract super exception for any exception thrown within this application. Any exception
 * defined in this application should inherit from this one.
 * @see MugglError
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public abstract class MugglException extends Exception {

	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see Exception#Exception()
	 */
	public MugglException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param arg0 the detail message.
	 * @see Exception#Exception(String)
	 */
	public MugglException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param arg0 the detail message.
	 * @param arg1 the cause.
	 * @see Exception#Exception(String, Throwable)
	 */
	public MugglException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of (cause==null ?
	 * null : cause.toString()).
	 *
	 * @param arg0 the cause.
	 * @see Exception#Exception(Throwable)
	 */
	public MugglException(Throwable arg0) {
		super(arg0);
	}

}
