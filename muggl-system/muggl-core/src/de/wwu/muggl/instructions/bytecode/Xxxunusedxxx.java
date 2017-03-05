package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.general.Nothing;
import de.wwu.muggl.instructions.interfaces.Instruction;

/**
 * Implementation of the instruction  <code>xxxunusedxxx</code>.
 *
 * According to the java virtual machine specification, this opcode is not used
 * for historical reasons. If it will be encountered in a ClassFile, treatment
 * will be equal to the execution of  <code>nop</code>. This means, simply nothing will be
 * changed in the currently execution thread.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-11-14
 */
public class Xxxunusedxxx extends Nothing implements Instruction {

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "xxxunusedxxx";
	}

}
