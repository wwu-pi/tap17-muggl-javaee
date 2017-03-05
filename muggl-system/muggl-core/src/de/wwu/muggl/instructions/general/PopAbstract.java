package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.instructions.interfaces.data.StackPop;

/**
 * Abstract instruction with some concrete methods for popping elements from the
 * operand stack. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-23
 */
public abstract class PopAbstract extends GeneralInstruction implements JumpNever, StackPop {

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be popped from the stack.
	 */
	public int getNumberOfPoppedElements() {
		return 1;
	}

}
