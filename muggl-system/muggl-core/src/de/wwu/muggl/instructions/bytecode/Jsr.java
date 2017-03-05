package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * Implementation of the instruction <code>jsr</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class Jsr extends de.wwu.muggl.instructions.general.UnconditionalJump implements Instruction, StackPush {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Jsr(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		frame.getOperandStack().push(Integer.valueOf(frame.getVm().getPc() + 1 + getNumberOfOtherBytes()));
		frame.getVm().addToPC(getJumpIncrement());
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		frame.getOperandStack().push(IntConstant.getInstance(frame.getVm().getPc() + 1 + getNumberOfOtherBytes()));
		frame.getVm().addToPC(getJumpIncrement());
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "jsr";
	}

	/**
	 * Return the value to increase the current pc by in order to perform the jump.
	 * @return The value to increase the current pc by.
	 */
	public int getJumpIncrement() {
		return this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
	}

	/**
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be pushed onto the stack.
	 */
	public int getNumberOfPushedElements() {
		return 1;
	}

	/**
	 * Get the type of elements this instruction will push onto the stack.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The type this instruction pushes. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the pushed type cannot be determined statically.
	 */
	public byte getTypePushed(ClassFile methodClassFile) {
		return 0;
	}


}
