package de.wwu.muggl.instructions;

import de.wwu.muggl.configuration.MugglException;

/**
 * Exception that is to be thrown on problems initializing an instruction. The initialisation can
 * fail due to a wrong count of additional bytes.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class InvalidInstructionInitialisationException extends MugglException {

	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see Exception#Exception()
	 */
	public InvalidInstructionInitialisationException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param arg0 the detail message.
	 * @see Exception#Exception(String)
	 */
	public InvalidInstructionInitialisationException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param arg0 the detail message.
	 * @param arg1 the cause.
	 * @see Exception#Exception(String, Throwable)
	 */
	public InvalidInstructionInitialisationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of (cause==null ?
	 * null : cause.toString()).
	 *
	 * @param arg0 the cause.
	 * @see Exception#Exception(Throwable)
	 */
	public InvalidInstructionInitialisationException(Throwable arg0) {
		super(arg0);
	}

}
