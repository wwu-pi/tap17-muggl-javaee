package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.If_ref;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.initialization.ObjectNullRef;

/**
 * Implementation of the instruction  <code>ifnull</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-09-17
 */
public class Ifnull extends If_ref implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument. Also the line number is given as an argument, since
	 * it is needed in case of jumping.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param lineNumber The line number of this instruction (including other bytes!).
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Ifnull(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
		super(code, lineNumber);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "ifnull";
	}

	/**
	 * Compare if the value is null. Return true if the expected
	 * condition is met, false otherwise.
	 * @param value The value to check.
	 * @return true if the expected condition is met, false otherwise.
	 */
	@Override
	protected boolean compare(Object value) {
		return value == null || value instanceof ObjectNullRef;
	}

	@Override
	protected boolean isJumpToIsNull() {
		return true;
	}

}
