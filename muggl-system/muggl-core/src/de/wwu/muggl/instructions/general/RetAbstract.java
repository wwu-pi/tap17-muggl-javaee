package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;

/**
 * Abstract instruction with some concrete methods for returning from a subroutine. This usually
 * is used to implement "finally". Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-10-29
 */
public abstract class RetAbstract extends GeneralInstructionWithOtherBytes implements JumpNever {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public RetAbstract(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
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
