package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.general.Const;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * Implementation of the instruction  <code>aconst_null</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class AConst_Null extends Const implements Instruction {

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		frame.getOperandStack().push(null);
	}

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		frame.getOperandStack().push(null);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "aconst_null";
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
