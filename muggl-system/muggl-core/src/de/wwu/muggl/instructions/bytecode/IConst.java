package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Const;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * Implementation of the instruction <code>iconst</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class IConst extends Const implements Instruction {
	private Integer value;

	/**
	 * Basic constructor to initialize this instruction to either iconst_m1, iconst_0,
	 * iconst_1, iconst_2, iconst_3, iconst_4 or iconst_5.
	 *
	 * @param value The Integer value that this instruction will push onto the operand stack.
	 * @throws InvalidInstructionInitialisationException If the value is invalid for this instruction (i.e. there is no such instruction).
	 */
	public IConst(Integer value) throws InvalidInstructionInitialisationException {
		if (value != -1 && value != 0 && value != 1 && value != 2 && value != 3 && value != 4 && value != 5) {
			throw new InvalidInstructionInitialisationException("Invalid value for iconst: " + value.toString());
		}
		this.value = value;
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		frame.getOperandStack().push(this.value);
	}

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		frame.getOperandStack().push(IntConstant.getInstance(this.value));
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		if (this.value == -1) {
			return "iconst_m1";
		}
		return "iconst_" + this.value.toString().substring(0, 1);
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
		return ClassFile.T_INT;
	}

}
