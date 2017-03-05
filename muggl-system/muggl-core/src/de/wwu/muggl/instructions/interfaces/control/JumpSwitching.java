package de.wwu.muggl.instructions.interfaces.control;

/**
 * Interface that is to be implemented by a switching operation (lookupswitch or
 * tableswitch). If this interface is implemented, the interfaces JumpAlways,
 * JumpConditional, JumpInvocation and JumpNever must not be implemented.
 * (JumpException may be implemented.)
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface JumpSwitching {

	/**
	 * Return the target pc values of the possible jumps.
	 * @return The target pc values of the possible jumps.
	 */
	int[] getJumpTargets();

}
