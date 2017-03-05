package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.general.Nothing;
import de.wwu.muggl.instructions.interfaces.Instruction;

/**
 * Implementation of the instruction <code>nop</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-11-14
 */
public class Nop extends Nothing implements Instruction {

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "nop";
	}

}
