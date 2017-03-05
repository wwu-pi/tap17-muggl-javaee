package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Implementation of the instruction <code>pop</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class Pop extends de.wwu.muggl.instructions.general.PopAbstract implements Instruction {

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			Object value = stack.peek();
			// Value must be a type of category 1.
			if (checkCategory2(value))
				throw new ExecutionException(
						"The topmost value of the operand stack must not be a category 2 type when using "
								+ getName() + ".");
			stack.pop();
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
			Object value = stack.peek();
			// Value must be a type of category 1.
			if (checkCategory2(value))
				throw new ExecutionException(
						"The topmost value of the operand stack must not be a category 2 type when using "
								+ getName() + ".");
			stack.pop();
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
		return "pop";
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
		byte[] types = {-1};
		return types;
	}

}
