package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.general.CompareFloat;
import de.wwu.muggl.instructions.interfaces.Instruction;

/**
 * Implementation of the instruction <code>fcmpg</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-04
 */
public class FCmpg extends CompareFloat implements Instruction {

	/**
	 * Get the result that will be pushed onto the operand stack if either
	 * value1 or value 2 is NaN.
	 *
	 * @return A new Integer with the value 1.
	 */
	@Override
	protected Integer getPushValueForNaN() {
		return Integer.valueOf(1);
	}

	/**
	 * Determine if this is fcmpg or fcmpl. It is fcmpg. Fcmpg will push 1 if a NaN value is
	 * encountered, while fcmpl will push -1 in such a case.
	 *
	 * @return false.
	 */
	@Override
	protected boolean pushMinusOneForNaN() {
		return false;
	}


	/**
	 * Resolve the instructions name.
	 *
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "fcmpg";
	}

}
