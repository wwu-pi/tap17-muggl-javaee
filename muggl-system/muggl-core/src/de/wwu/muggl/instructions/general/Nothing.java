package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Abstract instruction with some concrete methods for instructions that have no
 * function. Those instructions might be found in java files, for example for stuffing
 * reasons. The execution of them simple does not change anything to the current
 * virtual machine state, only the pc is increaed by 1 after the execution has been
 * finished.
 *
 * Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-11-26
 */
public abstract class Nothing extends GeneralInstruction implements JumpNever {

	/**
	 * Execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void execute(Frame frame) throws ExecutionException {
		// do nothing
	}

	/**
	 * Execute the inheriting instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		// do nothing
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
