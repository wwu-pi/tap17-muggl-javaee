package de.wwu.muggl.vm.initialization;

import de.wwu.muggl.vm.execution.ExecutionException;

/**
 * Exception that is to be thrown when a class file cannot be instantiated as
 * a wrapper for a primitive type.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class PrimitiveWrappingImpossibleException extends ExecutionException {

	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see Exception#Exception()
	 */
	public PrimitiveWrappingImpossibleException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param arg0 the detail message.
	 * @see Exception#Exception(String)
	 */
	public PrimitiveWrappingImpossibleException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param arg0 the detail message.
	 * @param arg1 the cause.
	 * @see Exception#Exception(String, Throwable)
	 */
	public PrimitiveWrappingImpossibleException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of (cause==null ?
	 * null : cause.toString()).
	 *
	 * @param arg0 the cause.
	 * @see Exception#Exception(Throwable)
	 */
	public PrimitiveWrappingImpossibleException(Throwable arg0) {
		super(arg0);
	}

}
