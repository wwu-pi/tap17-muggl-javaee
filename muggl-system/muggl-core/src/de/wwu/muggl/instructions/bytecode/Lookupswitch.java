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
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>lookupswitch</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Lookupswitch extends Switch implements Instruction {
	// Fields.
	private int otherBytesLength = 0;
	private int lineNumber;
	private int defaultValue;
	private int npairs;
	private int[] matches;
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
	public Lookupswitch(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
		this.lineNumber = lineNumber;
		// Calculate the padding.
		int padding = (lineNumber + 1) % 4;
		if (padding != 0) padding = 4 - padding;
		try {
			for (int a = 0; a < padding; a++) {
				// Just drop the padding bytes.
				code.getNextCodeByte();
			}
			// Construct the default and the npairs values.
			this.defaultValue = code.getNextCodeByte() << THREE_BYTES | code.getNextCodeByte() << TWO_BYTES | code.getNextCodeByte() << ONE_BYTE | code.getNextCodeByte();
			this.npairs = code.getNextCodeByte() << THREE_BYTES | code.getNextCodeByte() << TWO_BYTES | code.getNextCodeByte() << ONE_BYTE | code.getNextCodeByte();

			// Unexpected exception: npairs is less than 0.
			if (this.npairs < 0)
				throw new InvalidInstructionInitialisationException("Npair is less than 0 - this is impossible.");

			// Fetch the pairs of matches and offsets.
			this.matches = new int[this.npairs];
			this.offsets = new int[this.npairs];
			for (int a = 0; a < this.npairs; a++) {
				short[] values = code.getNextCodeBytes(8);
				this.matches[a] = values[0] << THREE_BYTES | values[1] << TWO_BYTES | values[2] << ONE_BYTE | values[3];
				this.offsets[a] = values[4] << THREE_BYTES | values[5] << TWO_BYTES | values[6] << ONE_BYTE | values[7];
			}
			// Set the number of additional bytes.
			this.otherBytesLength = padding + 8 + (this.npairs * 8);
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
		Integer key = (Integer) frame.getOperandStack().pop();
		findTargetAndSwitch(frame, key);
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		Term key = (Term) frame.getOperandStack().pop();
		// Check if the key is constant.
		if (key.isConstant()) {
			// Proceed with the normal execution.
			findTargetAndSwitch(frame, ((IntConstant) key).getIntValue());
		} else {
			// Gather data.
			int length = this.matches.length + 1;
			IntConstant[] keys = new IntConstant[length];
			int[] pcs = new int[length];
			keys[0] = null;
			pcs[0] = this.lineNumber + this.defaultValue;
			for (int a = 1; a < length; a++) {
				keys[a] = IntConstant.getInstance(this.matches[a - 1]);
				pcs[a] = this.lineNumber + this.offsets[a - 1];
			}

			// Create a choice point.
			((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, key, keys, pcs, null, null);
		}
	}

	/**
	 * Find the appropriate target and switch to it.
	 *
	 * @param frame The currently executed frame.
	 * @param key The key to use for switching.
	 */
	private void findTargetAndSwitch(Frame frame, int key) {
		// Find the match for the key.
		for (int a = 0; a < this.npairs; a++) {
			if (key == this.matches[a]) {
				frame.getVm().setPC(this.lineNumber + this.offsets[a]);
				return;
			}
		}

		// No key found, use the default value for jumping.
		frame.getVm().setPC(this.lineNumber + this.defaultValue);
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
		return "lookupswitch";
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
