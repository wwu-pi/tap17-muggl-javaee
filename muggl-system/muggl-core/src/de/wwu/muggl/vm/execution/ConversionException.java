package de.wwu.muggl.vm.execution;

import de.wwu.muggl.configuration.MugglException;

/**
 * Exception that is to be If the conversion of objects from the java virtual machine
 * implementation of SUN to the jvm of Muggl or the other way around failed.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class ConversionException extends MugglException {

	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see Exception#Exception()
	 */
	public ConversionException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param arg0 the detail message.
	 * @see Exception#Exception(String)
	 */
	public ConversionException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param arg0 the detail message.
	 * @param arg1 the cause.
	 * @see Exception#Exception(String, Throwable)
	 */
	public ConversionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of (cause==null ?
	 * null : cause.toString()).
	 *
	 * @param arg0 the cause.
	 * @see Exception#Exception(Throwable)
	 */
	public ConversionException(Throwable arg0) {
		super(arg0);
	}

}
