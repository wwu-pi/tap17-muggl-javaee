package de.wwu.muggl.instructions.interfaces.data;

import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * The implementing instruction is known to push an element onto the operand stack of the currently
 * executed frame.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface StackPush {

	/**
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed. If the number of elements is variable, this method will return the minimum number
	 * of elements popped. In this case, {@link VariablyStackPush} should be implemented by the
	 * instruction.
	 *
	 * @return The number of elements that will be pushed onto the stack.
	 */
	int getNumberOfPushedElements();

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
	byte getTypePushed(ClassFile methodClassFile);

}
