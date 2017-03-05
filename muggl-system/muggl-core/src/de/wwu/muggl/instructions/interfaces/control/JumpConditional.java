package de.wwu.muggl.instructions.interfaces.control;

/**
 * Interface that is to be implemented by any instruction that will lead to a jump
 * in the control flow under a specified condition (if). If this interface is
 * implemented, the interfaces JumpAlways, JumpInvocation, JumpNever and
 * JumpSwitching must not be implemented. (JumpException may be implemented.)
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface JumpConditional {

	/**
	 * Return the target pc of the possible jump.
	 * @return The target pc of the possible jump.
	 */
	int getJumpTarget();

}
