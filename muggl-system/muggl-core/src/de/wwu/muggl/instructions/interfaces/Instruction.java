package de.wwu.muggl.instructions.interfaces;

import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.execution.ExecutionException;

/**
 * Interface to be implemented by any class representing a java bytecode instruction. It defines the
 * very main methods every instruction class has to provide.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface Instruction {

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	void execute(Frame frame) throws ExecutionException;

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	void executeSymbolically(Frame frame) throws ExecutionException;

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	String getName();

	/**
	 * Resolve the instructions name including the additional bytes (if there are any).
	 * @return The instructions name inlcuding the additional bytes as a String.
	 */
	String getNameWithOtherBytes();

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	int getNumberOfOtherBytes();

}
