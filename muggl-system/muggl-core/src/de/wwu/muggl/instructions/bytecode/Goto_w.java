package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Implementation of the instruction <code>goto_w</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class Goto_w extends de.wwu.muggl.instructions.general.UnconditionalJump implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Goto_w(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void execute(Frame frame) throws ExecutionException {
		frame.getVm().addToPC(getJumpIncrement());
	}

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		frame.getVm().addToPC(getJumpIncrement());
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 4;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "goto_w";
	}

	/**
	 * Return the value to increase the current pc by in order to perform the jump.
	 * @return The value to increase the current pc by.
	 */
	public int getJumpIncrement() {
		return this.otherBytes[0] << THREE_BYTES | this.otherBytes[1] << TWO_BYTES | this.otherBytes[2] << ONE_BYTE | this.otherBytes[3];
	}


}
