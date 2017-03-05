package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.attributes.NoMoreCodeBytesException;
import de.wwu.muggl.vm.classfile.support.BytecodeParser;

/**
 * Abstract instruction with some concrete methods for the widening instruction.
 * The concrete instruction can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class WideAbstract extends GeneralInstructionWithOtherBytes implements JumpNever {
	/**
	 * The instruction this instruction offers wide access to.
	 */
	protected Instruction nextInstruction;
	/**
	 * The number of other bytes of this instruction.
	 */
	protected int numberOfOtherBytes;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument. In this case, the number of other bytes is determind
	 * by parsing the first additional byte. Then the calculated number of bytes is fetched.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public WideAbstract(AttributeCode code) throws InvalidInstructionInitialisationException {
		try {
			// Get the first additional byte, since it determines the widened instruction.
			short opcode = code.getNextCodeByte();
			this.nextInstruction = BytecodeParser.parse(opcode);
			if (this.nextInstruction instanceof IntegerIncremenet) {
				this.otherBytes = new short[5];
				this.otherBytes[0] = opcode;
				this.otherBytes[1] = code.getNextCodeByte();
				this.otherBytes[2] = code.getNextCodeByte();
				this.otherBytes[3] = code.getNextCodeByte();
				this.otherBytes[4] = code.getNextCodeByte();
				this.numberOfOtherBytes = 5;
			} else {
				this.otherBytes = new short[3];
				this.otherBytes[0] = opcode;
				this.otherBytes[1] = code.getNextCodeByte();
				this.otherBytes[2] = code.getNextCodeByte();
				this.numberOfOtherBytes = 3;
			}
		} catch (NoMoreCodeBytesException e) {
			throw new InvalidInstructionInitialisationException("Initialiation of Instruction failed with an " + e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return this.numberOfOtherBytes;
	}

	/**
	 * Getter for the next instruction. (The instruction that can be accessed widendly.)
	 * @return The widened instruction.
	 */
	public Instruction getNextInstruction() {
		return this.nextInstruction;
	}
}
