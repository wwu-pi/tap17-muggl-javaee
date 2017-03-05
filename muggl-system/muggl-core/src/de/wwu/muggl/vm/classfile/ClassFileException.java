package de.wwu.muggl.vm.classfile;

import de.wwu.muggl.configuration.MugglException;

/**
 * Exception that is to be thrown on problems parsing a class file.<br />
 * <br />
 * This Exception indicates a state in which a class file contains information which cannot
 * be analyzed by this application as they are not known by it. This might by caused by corrupt
 * class files. Less likely class files of newer java versions might not be covered by the used
 * version of this application, yet.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class ClassFileException extends MugglException {

	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see Exception#Exception()
	 */
	public ClassFileException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param arg0 the detail message.
	 * @see Exception#Exception(String)
	 */
	public ClassFileException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param arg0 the detail message.
	 * @param arg1 the cause.
	 * @see Exception#Exception(String, Throwable)
	 */
	public ClassFileException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructs a new exception with the specified cause and a detail message of (cause==null ?
	 * null : cause.toString()).
	 *
	 * @param arg0 the cause.
	 * @see Exception#Exception(Throwable)
	 */
	public ClassFileException(Throwable arg0) {
		super(arg0);
	}
}
