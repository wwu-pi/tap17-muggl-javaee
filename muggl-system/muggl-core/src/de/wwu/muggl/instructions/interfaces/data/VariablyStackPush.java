package de.wwu.muggl.instructions.interfaces.data;

import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * The implementing instruction is known to push an element onto the operand stack of the currently
 * executed frame. The actual number of pushed elements cannot be determined without
 * analyzing the preceding code.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface VariablyStackPush extends StackPush {

	/**
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed.
	 *
	 * @param types The types of the elements on the stack in reversed order. Non-primitive types
	 *        should be represented as -1. Types are {@link ClassFile#T_BOOLEAN},
	 *        {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE}, {@link ClassFile#T_FLOAT},
	 *        {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and {@link ClassFile#T_SHORT}.
	 * @return The number of elements that will be pushed onto the stack.
	 */
	int getNumberOfPushedElements(byte[] types);

	/**
	 * Get the maximum number of elements that will be pushed from the stack when this instruction is
	 * executed.
	 *
	 * @return The maximum number of elements that will be pushed from the stack.
	 */
	int getMaximumNumberOfPushedElements();

}
