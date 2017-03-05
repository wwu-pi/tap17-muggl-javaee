package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.attributes.NoMoreCodeBytesException;

/**
 * This abstract class extends the GenerelInstruction, augmenting it with methods that can be invoked
 * by instructions that have other bytes. Any instruction that has additional bytes has to extend this
 * class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class GeneralInstructionWithOtherBytes extends GeneralInstruction {
	/**
	 *  A field for the additional bytes.
	 */
	protected short[] otherBytes;

	/**
	 * Empty constructor.
	 */
	public GeneralInstructionWithOtherBytes() { }

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public GeneralInstructionWithOtherBytes(AttributeCode code) throws InvalidInstructionInitialisationException {
		if (code != null) {
			try {
				// Fetch the other bytes.
				this.otherBytes = code.getNextCodeBytes(getNumberOfOtherBytes());
			} catch (NoMoreCodeBytesException e) {
				throw new InvalidInstructionInitialisationException("Instruction initialisation failed: " + e.getMessage());
			}
		}
	}

	/**
	 * Constructor only used by Load and Store, since the other bytes number might be one, depending on the initialization.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param index The index of Load or Store.
	 * @param upperBoundary The maximum possible index.
	 * @param numberOfOtherBytes The number of other bytes to be fetched.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public GeneralInstructionWithOtherBytes(AttributeCode code, int index, int upperBoundary, int numberOfOtherBytes) throws InvalidInstructionInitialisationException {
		if (code != null) {
			if (index > upperBoundary) {
				try {
					this.otherBytes = code.getNextCodeBytes(numberOfOtherBytes);
				} catch (NoMoreCodeBytesException e) {
					throw new InvalidInstructionInitialisationException("Instruction initialisation failed: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Fetch the other byte of the given index.
	 * @param index The other byte to fetch.
	 * @return The other byte as a short.
	 * @throws IndexOutOfBoundsException If the given index is below zero or beyond the total number of other bytes.
	 */
	public short getOtherByte(int index) {
		if (index < 0) throw new IndexOutOfBoundsException("The index must be greater than or equal to zero.");
		if (index > this.otherBytes.length) throw new IndexOutOfBoundsException("Cannot fetch other byte #" + index + " since there only are " + this.otherBytes.length + " other bytes in total.");
		return this.otherBytes[index];
	}

	/**
	 * Calculate an index of the other bytes between a given range. The bytes will be shifted to do
	 * so. The last other bytes to be fetched will not be shifted, the one to be fetched before that
	 * will be shifted to the left by 8, the one before that by 16 to the left, then one before that
	 * by 24 to the left and so on.
	 * 
	 * @param start The index to start at.
	 * @param end The index to end at.
	 * @return The calculated index.
	 * @throws IndexOutOfBoundsException If one of the given indexes is below zero or beyond the
	 *         total number of other bytes, or if the ending index is less than the starting index.
	 */
	public int constructValueFromOtherBytes(int start, int end) {
		// Instead of checking for end inclusively, make end exclusively.
		end++;

		// Check for potential problems.
		if (start < 0) throw new IndexOutOfBoundsException("The starting index must be greater than or equal to zero.");
		if (start > this.otherBytes.length) throw new IndexOutOfBoundsException("Cannot fetch other byte #" + start + " since there only are " + this.otherBytes.length + " other bytes in total.");
		if (end < 0) throw new IndexOutOfBoundsException("The ending index must be greater than or equal to zero.");
		if (end > this.otherBytes.length) throw new IndexOutOfBoundsException("Cannot fetch other byte #" + end + " since there only are " + this.otherBytes.length + " other bytes in total.");
		if (start > end) throw new IndexOutOfBoundsException("The starting index must not be greater than the ending index.");

		int calculatedValue = 0;
		int difference = end - start - 1;
		for (int a = start; a < end; a++) {
			if (a == end - 1) {
				calculatedValue |= this.otherBytes[a];
			} else {
				calculatedValue |= this.otherBytes[a] << ((difference - a) * ONE_BYTE);
			}
		}
		return calculatedValue;
	}

	/**
	 * Resolve the instructions name including the additional bytes (if there are any).
	 *
	 * @return The instructions name including the additional bytes as a String.
	 */
	@Override
	public String getNameWithOtherBytes() {
		if (this.otherBytes == null || this.otherBytes.length == 0) {
			// This should not happen normally, but behave gracefully.
			return getName();
		}
		String name = "";
		for (int a = 0; a < this.otherBytes.length; a++) {
			name += " " + this.otherBytes[a];
		}
		return getName() + " " + name;
	}

}
