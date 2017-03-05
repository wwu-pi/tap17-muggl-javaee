package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Switch;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.attributes.NoMoreCodeBytesException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction  <code>tableswitch</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Tableswitch extends Switch implements Instruction {
	// Fields.
	private int otherBytesLength = 0;
	private int lineNumber;
	private int defaultValue;
	private int low;
	private int high;
	private int[] offsets;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument. Also the line number is given as an argument, since
	 * it is needed in case of jumping.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param lineNumber The line number of this instruction (including other bytes!).
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Tableswitch(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
		this.lineNumber = lineNumber;
		// Calculate the padding.
		int padding = (lineNumber + 1) % 4;
		if (padding != 0) padding = 4 - padding;
		try {
			for (int a = 0; a < padding; a++) {
				// Just drop the padding bytes.
				code.getNextCodeByte();
			}
			// Construct the default, the low and the high value.
			this.defaultValue = code.getNextCodeByte() << THREE_BYTES | code.getNextCodeByte() << TWO_BYTES | code.getNextCodeByte() << ONE_BYTE | code.getNextCodeByte();
			this.low = code.getNextCodeByte() << THREE_BYTES | code.getNextCodeByte() << TWO_BYTES | code.getNextCodeByte() << ONE_BYTE | code.getNextCodeByte();
			this.high = code.getNextCodeByte() << THREE_BYTES | code.getNextCodeByte() << TWO_BYTES | code.getNextCodeByte() << ONE_BYTE | code.getNextCodeByte();

			// Unexpected exception: low is not <= high.
			if (this.low > this.high)
				throw new InvalidInstructionInitialisationException("Low is greater than high - this is impossible.");

			// Calculate the numbers of offsets.
			int numberOfOffsets = this.high - this.low + 1;

			// Fetch the offsets.
			this.offsets = new int[numberOfOffsets];
			for (int a = 0; a < numberOfOffsets; a++) {
				short[] values = code.getNextCodeBytes(4);
				this.offsets[a] = values[0] << THREE_BYTES | values[1] << TWO_BYTES | values[2] << ONE_BYTE | values[3];
			}
			// Set the number of additional bytes.
			this.otherBytesLength = padding + 12 + (numberOfOffsets * 4);
		} catch (NoMoreCodeBytesException e) {
			throw new InvalidInstructionInitialisationException("Initialiation of Instruction failed: " + e.getMessage());
		}
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		int index = (Integer) frame.getOperandStack().pop();
		findTargetAndSwitch(frame, index);
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		Term index = (Term) frame.getOperandStack().pop();
		// Check if the index is constant.
		if (index.isConstant()) {
			// Proceed with the normal execution.
			findTargetAndSwitch(frame, ((IntConstant) index).getIntValue());
		} else {
			// Gather data.
			int length = this.offsets.length + 1;
			IntConstant[] keys = new IntConstant[length];
			int[] pcs = new int[length];
			keys[0] = null;
			pcs[0] = this.lineNumber + this.defaultValue;
			for(int i = 0; i < this.offsets.length;i++) {
				keys[i+1] = IntConstant.getInstance(this.low+i); 
				pcs[i+1] = this.lineNumber + this.offsets[i];
			}
			IntConstant lowConstant = IntConstant.getInstance(this.low);
			IntConstant highConstant = IntConstant.getInstance(this.high);

			// Create a choice point.
			((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, index, keys,
					pcs, lowConstant, highConstant);
		}
	}

	/**
	 * Find the appropriate target and switch to it.
	 *
	 * @param frame The currently executed frame.
	 * @param index The index to use for switching.
	 */
	private void findTargetAndSwitch(Frame frame, int index) {
		// Determine the target for the jump.
		if (index < this.low || index > this.high) {
			// index is less than low or greater than high, so the default value is used for jumping.
			frame.getVm().setPC(this.lineNumber + this.defaultValue);
		} else {
			// The offset at the position index - low is used for jumping.
			frame.getVm().setPC(this.lineNumber + this.offsets[index - this.low]);
		}
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return this.otherBytesLength;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "tableswitch";
	}

	/**
	 * Return the target pc values of the possible jumps.
	 * @return The target pc values of the possible jumps.
	 */
	public int[] getJumpTargets() {
		int[] jumpTargets = new int[this.offsets.length + 1];
		for (int a = 0; a < jumpTargets.length - 1; a++) {
			jumpTargets[a] = this.lineNumber + this.offsets[a];
		}
		jumpTargets[jumpTargets.length - 1] = this.lineNumber + this.defaultValue;
		return jumpTargets;
	}

}
