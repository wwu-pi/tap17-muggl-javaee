package de.wwu.muggl.instructions.interfaces.control;

/**
 * Interface that is to be implemented by any instruction that will always lead to
 * a jump in the control flow. If this interface is implemented, the interfaces
 * JumpConditional, JumpInvocation, JumpNever and JumpSwitching must not be
 * implemented. (JumpException may be implemented.)
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface JumpAlways {

	/**
	 * Return the value to increase the current pc by in order to perform the jump.
	 * @return The value to increase the current pc by.
	 */
	int getJumpIncrement();

}
