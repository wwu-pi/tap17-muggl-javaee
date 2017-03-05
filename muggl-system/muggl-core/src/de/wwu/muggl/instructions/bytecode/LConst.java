package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Const;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.solvers.expressions.LongConstant;

/**
 * Implementation of the instruction <code>lconst</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class LConst extends Const implements Instruction {
	private Long value;

	/**
	 * Basic constructor to initialize this instruction to either lconst_0 or lconst_1.
	 *
	 * @param value The Long value that this instruction will push onto the operand stack.
	 * @throws InvalidInstructionInitialisationException If the value is invalid for this instruction (i.e. there is no such instruction).
	 */
	public LConst(Long value) throws InvalidInstructionInitialisationException {
		if (value != 0L && value != 1L) {
			throw new InvalidInstructionInitialisationException("Invalid value for lconst: " + value.toString());
		}
		this.value = value;
	}

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		frame.getOperandStack().push(this.value);
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		frame.getOperandStack().push(LongConstant.getInstance(this.value));
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "lconst_" + this.value.toString().substring(0, 1);
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
		return ClassFile.T_LONG;
	}

}
