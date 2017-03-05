package de.wwu.muggl.symbolic.flow.controlflow;

/**
 * An internal class to store information for the handling of <code>finally</code>.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-02-14
 */
final class FinallyHandler {
	int pc;
	String exceptionType;
	
	/**
	 * Construct a handler for a <code>finally</code> construct.
	 * 
	 * @param pc the pc the exception is thrown at.
	 * @param exceptionType The exception type thrown.
	 */
	FinallyHandler(int pc, String exceptionType) {
		this.pc = pc;
		this.exceptionType = exceptionType;
	}
}