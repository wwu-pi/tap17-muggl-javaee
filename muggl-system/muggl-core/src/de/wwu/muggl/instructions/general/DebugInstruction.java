package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Abstract instruction with some concrete methods for debug instructions. Concrete instructions can
 * be extended from this class.<br />
 * <br />
 * Debug instructions should not be found in class files since they are only meant to be set by
 * debuggers and similar tools during the inspection of an application. They can be parsed, but
 * execution will by default fail with an ExecutionException / SymbolicExecutionException being
 * thrown. Concrete classes might override this behavior and for example only log a warning.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-11-26
 */
public abstract class DebugInstruction extends GeneralInstruction implements JumpNever {

	/**
	 * Execute the inheriting instruction.
	 *
	 * @param frame The currently executed frame.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		throw new ExecutionException("The instruction " + getName() + " is a debug instruction only and not implemented.");
	}

	/**
	 * Execute the inheriting instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException In case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		throw new SymbolicExecutionException("The instruction " + getName() + " is a debug instruction only and not implemented.");
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}

}
