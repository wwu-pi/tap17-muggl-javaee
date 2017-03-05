package de.wwu.muggl.vm.exceptions;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.vm.initialization.Objectref;

/**
 * The Exception that can be thrown during execution, indicating a runtime error. It wraps a
 * Throwable. It should be tried to be catch with the means of the executed class. This means, it
 * should be handed to the ExceptionHandler and processed by it, leading either to a jump or to the
 * abortion of the program.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public class VmRuntimeException extends MugglException {
	// The wrapped object reference of the Throwable.
	private final Objectref wrappedThrowable;

	/**
	 * Basic constructor that sets the wrapped object reference of the Throwable.
	 * 
	 * @param wrappedException The object reference of the Throwable to be wrapped.
	 */
	public VmRuntimeException(Objectref wrappedException) {
		super();
		this.wrappedThrowable = wrappedException;
	}

	/**
	 * Getter for the wrapped object reference of the Throwable.
	 * 
	 * @return The wrapped object reference of the Throwable.
	 */
	public Objectref getWrappedException() {
		return this.wrappedThrowable;
	}

}
