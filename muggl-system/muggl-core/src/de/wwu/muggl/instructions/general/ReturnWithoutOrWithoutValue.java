package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpException;

/**
 * Abstract instruction with some concrete methods for returning from a method.
 * Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-04-01
 */
public abstract class ReturnWithoutOrWithoutValue extends GeneralInstruction implements JumpException {

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}
	
	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = {"java.lang.IllegalMonitorStateException"};
		return exceptionTypes;
	}

}
