package de.wwu.muggl.instructions.bytecode;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.general.DebugInstruction;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;

/**
 * Implementation of the instruction  <code>breakpoint</code>.<br />
 * <br />
 * This instruction is not supposed to be encountered, but will gracefully be skipped.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-24
 */
public class Breakpoint extends DebugInstruction implements Instruction {

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		if (Globals.getInst().execLogger.isEnabledFor(Level.WARN))
			Globals.getInst().execLogger.warn(
					"Gracefully skipping instruction breakpoint. It is not supposed to appear in any class.");
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
			Globals.getInst().symbolicExecLogger.warn(
					"Gracefully skipping instruction breakpoint. It is not supposed to appear in any class.");
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "breakpoint";
	}

}
