package de.wwu.muggl.vm.execution;

import de.wwu.muggl.configuration.MugglException;

/**
 * Exception that is to be thrown on problems while Executing java bytecode in the normal execution mode.
 * It indicates unexpected circumstances that lead to abnormal execution conditions.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class ExecutionException extends MugglException {

	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see Exception#Exception()
	 */
	public ExecutionException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param arg0 the detail message.
	 * @see Exception#Exception(String)
	 */
	public ExecutionException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param arg0 the detail message.
	 * @param arg1 the cause.
	 * @see Exception#Exception(String, Throwable)
	 */
	public ExecutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of (cause==null ?
	 * null : cause.toString()).
	 *
	 * @param arg0 the cause.
	 * @see Exception#Exception(Throwable)
	 */
	public ExecutionException(Throwable arg0) {
		super(arg0);
	}

}
