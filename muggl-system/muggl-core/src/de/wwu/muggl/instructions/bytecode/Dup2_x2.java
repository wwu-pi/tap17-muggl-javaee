package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.data.VariablyStackPop;
import de.wwu.muggl.instructions.interfaces.data.VariablyStackPush;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Implementation of the instruction  <code>dup2_x2</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Dup2_x2 extends de.wwu.muggl.instructions.general.Duplication implements Instruction,
		VariablyStackPop, VariablyStackPush {

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			Object value1 = stack.pop();
			Object value2 = stack.pop();
			if (checkCategory2(value1) && checkCategory2(value2)) {
				// Form 4: Both value1 and value2 are types of category 2.
				stack.push(value1);
				stack.push(value2);
				stack.push(value1);
			} else {
				Object value3 = stack.pop();
				if (!checkCategory2(value1) && !checkCategory2(value2) && checkCategory2(value3))
				{
					// Form 3: Both value1 and value2 are types of category 1, value3 is a type of category 2.
					stack.push(value2);
					stack.push(value1);
					stack.push(value3);
					stack.push(value2);
					stack.push(value1);
				} else if (checkCategory2(value1) && !(checkCategory2(value2)) || checkCategory2(value3)) {
					// Form 2: Value1 is a type of category 2, both value2 and value3 are types of category 1.
					stack.push(value1);
					stack.push(value3);
					stack.push(value2);
					stack.push(value1);
				} else {
					Object value4 = stack.pop();
					if (!(checkCategory2(value1) || checkCategory2(value2) || checkCategory2(value3) || checkCategory2(value4))) {
						// Form 1: All four values are types of category 1.
						stack.push(value2);
						stack.push(value1);
						stack.push(value4);
						stack.push(value3);
						stack.push(value2);
						stack.push(value1);
					} else {
						// Recovery
						stack.push(value4);
						stack.push(value3);
						stack.push(value2);
						stack.push(value1);
						throw new ExecutionException("The instruction " + getName() + " cannot be used with the current operand stack due to unexpected types of the topmost elements.");
					}
				}
			}
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			Object value1 = stack.pop();
			Object value2 = stack.pop();
			if (checkCategory2(value1) && checkCategory2(value2)) {
				// Form 4: Both value1 and value2 are types of category 2.
				stack.push(value1);
				stack.push(value2);
				stack.push(value1);
			} else {
				Object value3 = stack.pop();
				if (!checkCategory2(value1) && !checkCategory2(value2) && checkCategory2(value3))
				{
					// Form 3: Both value1 and value2 are types of category 1, value3 is a type of category 2.
					stack.push(value2);
					stack.push(value1);
					stack.push(value3);
					stack.push(value2);
					stack.push(value1);
				} else if (checkCategory2(value1) && !(checkCategory2(value2)) || checkCategory2(value3)) {
					// Form 2: Value1 is a type of category 2, both value2 and value3 are types of category 1.
					stack.push(value1);
					stack.push(value3);
					stack.push(value2);
					stack.push(value1);
				} else {
					Object value4 = stack.pop();
					if (!(checkCategory2(value1) || checkCategory2(value2) || checkCategory2(value3) || checkCategory2(value4))) {
						// Form 1: All four values are types of category 1.
						stack.push(value2);
						stack.push(value1);
						stack.push(value4);
						stack.push(value3);
						stack.push(value2);
						stack.push(value1);
					} else {
						// Recovery
						stack.push(value4);
						stack.push(value3);
						stack.push(value2);
						stack.push(value1);
						throw new ExecutionException("The instruction " + getName() + " cannot be used with the current operand stack due to unexpected types of the topmost elements.");
					}
				}
			}
		} catch (ExecutionException e) {
			symbolicExecutionFailedWithAnExecutionException(e);
		}
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "dup2_x2";
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be popped from the stack.
	 */
	public int getNumberOfPoppedElements() {
		return 2;
	}

	/**
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be pushed onto the stack.
	 */
	public int getNumberOfPushedElements() {
		return 3;
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @param types The types of the elements on the stack in reversed order. Non-primitive types
	 *        should be represented as -1. Types are {@link ClassFile#T_BOOLEAN},
	 *        {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE}, {@link ClassFile#T_FLOAT},
	 *        {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and {@link ClassFile#T_SHORT}.
	 * @return The number of elements that will be popped from the stack.
	 * @throws IllegalArgumentException If the argument array is too short or the types have an
	 *         unexpected order.
	 */
	public int getNumberOfPoppedElements(byte[] types) {
		if (types.length < 2) throw new IllegalArgumentException("Not enough elements supplied.");
		if (checkCategory2(types[0]) && checkCategory2(types[1]))
			return 2; // 3

		if (types.length < 3) throw new IllegalArgumentException("Not enough elements supplied.");
		if (!checkCategory2(types[0]) && !checkCategory2(types[1]) && checkCategory2(types[1]))
			return 3; // 5

		 if (checkCategory2(types[0]) && !(checkCategory2(types[1])) || checkCategory2(types[2]))
			 return 3; // 4

		 if (types.length < 4) throw new IllegalArgumentException("Not enough elements supplied.");
		 if (!(checkCategory2(types[0]) || checkCategory2(types[1]) || checkCategory2(types[2]) || checkCategory2(types[3])))
			return 4; // 6

		throw new IllegalArgumentException("Illegal arguments on specified array.");
	}

	/**
	 * Get the maximum number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The maximum number of elements that will be popped from the stack.
	 */
	public int getMaximumNumberOfPoppedElements() {
		return 4;
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @param types The types of the elements on the stack in reversed order. Non-primitive types
	 *        should be represented as -1. Types are {@link ClassFile#T_BOOLEAN},
	 *        {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE}, {@link ClassFile#T_FLOAT},
	 *        {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and {@link ClassFile#T_SHORT}.
	 * @return The number of elements that will be popped from the stack.
	 * @throws IllegalArgumentException If the argument array is too short or the types have an
	 *         unexpected order.
	 */
	public int getNumberOfPushedElements(byte[] types) {
		if (types.length < 2) throw new IllegalArgumentException("Not enough elements supplied.");
		if (checkCategory2(types[0]) && checkCategory2(types[1]))
			return 3;

		if (types.length < 3) throw new IllegalArgumentException("Not enough elements supplied.");
		if (!checkCategory2(types[0]) && !checkCategory2(types[1]) && checkCategory2(types[1]))
			return 5;

		 if (checkCategory2(types[0]) && !(checkCategory2(types[1])) || checkCategory2(types[2]))
			 return 4;

		 if (types.length < 4) throw new IllegalArgumentException("Not enough elements supplied.");
		 if (!(checkCategory2(types[0]) || checkCategory2(types[1]) || checkCategory2(types[2]) || checkCategory2(types[3])))
			return 6;

		throw new IllegalArgumentException("Illegal arguments on specified array.");
	}

	/**
	 * Get the maximum number of elements that will be pushed from the stack when this instruction is
	 * executed.
	 *
	 * @return The maximum number of elements that will be pushed from the stack.
	 */
	public int getMaximumNumberOfPushedElements() {
		return 6;
	}

}
