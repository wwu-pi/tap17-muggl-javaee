package de.wwu.muggl.instructions.interfaces.control;

/**
 * Interface that is to be implemented by any instruction that will never lead to
 * a jump in the control flow. If this interface is implemented, the interfaces
 * JumpAlways, JumpConditional, JumpInvocation, JumpException and JumpSwitching
 * must not be implemented.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public interface JumpNever { }
