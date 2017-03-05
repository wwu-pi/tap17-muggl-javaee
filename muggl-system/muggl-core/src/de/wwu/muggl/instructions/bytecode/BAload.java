package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.general.Aload;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.typed.ByteInstruction;
import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * Implementation of the instruction <code>baload</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class BAload extends Aload implements Instruction {

	/**
	 * Constructor to initialize the TypedInstruction.
	 */
	public BAload() {
		 this.typedInstruction = new ByteInstruction();
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "b" + super.getName();
	}

	/**
	 * Get the type of elements this instruction will push onto the stack.<br />
	 * <br />
	 * Please note that this method returns {@link ClassFile#T_BYTE}, but also values of type
	 * {@link ClassFile#T_BOOLEAN} can be pushed.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The type this instruction pushes. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the pushed type cannot be determined statically.
	 */
	public byte getTypePushed(ClassFile methodClassFile) {
		return ClassFile.T_BYTE;
	}

}
