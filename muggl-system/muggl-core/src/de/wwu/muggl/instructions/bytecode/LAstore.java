package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.general.Astore;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.typed.LongInstruction;
import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * Implementation of the instruction <code>lastore</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class LAstore extends Astore implements Instruction {

	/**
	 * Constructor to initialize the TypedInstruction.
	 */
	public LAstore() {
		 this.typedInstruction = new LongInstruction();
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "l" + super.getName();
	}

	/**
	 * Get the types of elements this instruction will pop from the stack.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The types this instruction pops. The length of the arrays reflects the number of
	 *         elements pushed in the order they are pushed. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the popped type cannot be determined statically.
	 */
	public byte[] getTypesPopped(ClassFile methodClassFile) {
		byte[] types = {ClassFile.T_LONG, ClassFile.T_INT, 0};
		return types;
	}

}
