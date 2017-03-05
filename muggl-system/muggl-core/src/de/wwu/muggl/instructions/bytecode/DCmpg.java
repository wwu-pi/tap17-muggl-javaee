package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.general.CompareDouble;
import de.wwu.muggl.instructions.interfaces.Instruction;

/**
 * Implementation of the instruction  <code>dcmpg</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-04
 */
public class DCmpg extends CompareDouble implements Instruction {

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
	 * Determine if this is dcmpg or dcmpl. It is dcmpg. Dcmpg will push 1 if a NaN value is
	 * encountered, while dcmpl will push -1 in such a case.
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
		return "dcmpg";
	}

}
