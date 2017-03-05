package de.wwu.muggl.instructions.bytecode;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.general.DebugInstruction;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.execution.ExecutionException;

/**
 * Implementation of the instruction <code>impdep2</code>.
 *
 * This instruction is not supposed to be encountered, but will gracefully be skipped.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-11-14
 */
public class Impdep2 extends DebugInstruction implements Instruction {

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void execute(Frame frame) throws ExecutionException {
		if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Gracefully skipping instruction breakpoint. It is not supposed to appear in any class.");
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "impdep2";
	}

}
