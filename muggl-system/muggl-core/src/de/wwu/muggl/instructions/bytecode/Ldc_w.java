package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;

/**
 * Implementation of the instruction <code>ldc_w</code>.
 *
 * Last modified: 2010-03-11
 *
 * @author Tim Majchrzak
 * @version 1.0.0
 */
public class Ldc_w extends de.wwu.muggl.instructions.general.PushFromConstantPool implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Ldc_w(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "ldc_w";
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 2;
	}

	/**
	 * Fetch the index into the constant_pool from the additional bytes of the instruction.
	 * @param otherBytes The array of additional bytes.
	 * @return The index.
	 */
	@Override
	protected int fetchIndex(short[] otherBytes) {
		return otherBytes[0] << ONE_BYTE | otherBytes[1];
	}

}
