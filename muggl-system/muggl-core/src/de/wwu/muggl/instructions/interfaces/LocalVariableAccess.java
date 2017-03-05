package de.wwu.muggl.instructions.interfaces;

/**
 * Interface that is to be implemented by any instruction that accessed exactly one local variable,
 * reading or writing it. It only forces the implementation of the method getLocalVariableIndex().
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface LocalVariableAccess {

	/**
	 * Get the index into the local variables that will be read and/or written to.
	 * @return The index into the local variables to be used by the implementing instruction.
	 */
	int getLocalVariableIndex();

}
