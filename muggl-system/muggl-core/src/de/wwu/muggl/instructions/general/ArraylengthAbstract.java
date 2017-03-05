package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.StackPush;

/**
 * Abstract instruction with some concrete methods for the arraylength instruction. The concrete
 * instruction can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-19
 */
public abstract class ArraylengthAbstract extends GeneralInstruction implements JumpException,
		StackPop, StackPush {

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}

}
