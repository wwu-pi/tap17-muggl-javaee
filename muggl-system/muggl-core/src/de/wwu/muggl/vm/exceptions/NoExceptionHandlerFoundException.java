package de.wwu.muggl.vm.exceptions;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * Wrapper exception for uncaught Throwable object references.<br />
 * <br />
 * Whenever an application that is executed throws an Throwable or causes a Throwable
 * to be thrown by an instruction, it is wrapped into a VMRuntimeException.
 * VMRuntimeExceptions are processed by the ExceptionHandler. If it is not caught at
 * all, the ExceptionHandler will throw this exception and wrap the Throwable into it.
 * It should be caught by the virtual machine. For a normal execution, aborting the
 * execution will be the common reaction. However, further handling might be chosen
 * by vm implementations.<br />
 * <br />
 * This exception should not be thrown by any other classes but the ExceptionHandler.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public class NoExceptionHandlerFoundException extends ExecutionException {
	// The object reference of the Throwable that could not be caught.
	private final Objectref uncaughtThrowable;
	// The Method the Throwable was thrown from.
	private final Method method;
	// The pc the Throwable was thrown at.
	private final int thrownAtPC;

	/**
	 * Construct the exception and pass the uncaught Throwable to it.
	 * 
	 * @param uncaughtThrowable The object reference of the Throwable that could not be caught.
	 * @param method The method the Throwable was thrown by.
	 * @param thrownAtPC The pc the Throwable was thrown at.
	 */
	public NoExceptionHandlerFoundException(Objectref uncaughtThrowable, Method method, int thrownAtPC) {
		this.uncaughtThrowable = uncaughtThrowable;
		this.method = method;
		this.thrownAtPC = thrownAtPC;
	}

	/**
	 * Getter for the object reference of the uncaught Throwable.
	 *
	 * @return The object reference of the Throwable that could not be caught.
	 */
	public Objectref getUncaughtThrowable() {
		return this.uncaughtThrowable;
	}

	/**
	 * Getter for the Method the Throwable was thrown from.
	 *
	 * @return The Method the Throwable was thrown from.
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * Getter for the pc the Throwable was thrown at.
	 *
	 * @return The pc the Throwable was thrown at.
	 */
	public int getThrownAtPC() {
		return this.thrownAtPC;
	}
	
	/**
	 * Get class name and message of the uncaught throwable.
	 * 
	 * @return class name and message of the uncaught throwable as an String array of two
	 *         dimensions.
	 */
	public String[] getUncaughtThrowableNameAndMessage() {
		String[] nameAndMessage = new String[2];
		ClassFile uncaughtThrowableClassFile = this.uncaughtThrowable.getInitializedClass()
				.getClassFile();
		nameAndMessage[0] = uncaughtThrowableClassFile.getCanonicalName();
		nameAndMessage[1] = "";
		try {
			ClassFile throwableClassFile = this.uncaughtThrowable.getInitializedClass()
					.getClassFile();
			Field detailMessageField = throwableClassFile.getFieldByName("detailMessage", true);
			ClassFile stringClassFile = uncaughtThrowableClassFile.getClassLoader()
					.getClassAsClassFile("java.lang.String");
			Field stringValueField = stringClassFile.getFieldByNameAndDescriptor("value", "[C");
			Objectref stringObjectref = (Objectref) this.uncaughtThrowable
					.getField(detailMessageField);
			if (stringObjectref != null) {
				Arrayref arrayref = (Arrayref) stringObjectref.getField(stringValueField);

				// Convert it.
				boolean symbolicalMode = Options.getInst().symbolicMode;
				char[] characters = new char[arrayref.length];
				for (int a = 0; a < arrayref.length; a++) {
					if (symbolicalMode) {
						characters[a] = (char) ((IntConstant) arrayref.getElement(a)).getIntValue();
					} else {
						characters[a] = (Character) arrayref.getElement(a);
					}
				}
				nameAndMessage[1] = new String(characters);
			}
		} catch (ClassFileException e) {
			// This cannot happen under any expected circumstances.
		} catch (FieldResolutionError e) {
			// This may happen, but it is no problem.
		}
		return nameAndMessage;
	}

}
