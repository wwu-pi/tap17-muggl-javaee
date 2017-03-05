package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Implementation of the instruction  <code>dup_x1</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-23
 */
public class Dup_x1 extends de.wwu.muggl.instructions.general.Duplication implements Instruction {

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
			if (!(checkCategory2(value1) || checkCategory2(value2))) {
				stack.push(value1);
				stack.push(value2);
				stack.push(value1);
			} else {
				// Recovery.
				stack.push(value2);
				stack.push(value1);
				throw new ExecutionException("The two topmost values of the operand stack must not be category 2 types when using " + getName() + ".");
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
			if (!(checkCategory2(value1) || checkCategory2(value2))) {
				stack.push(value1);
				stack.push(value2);
				stack.push(value1);
			} else {
				// recovery
				stack.push(value2);
				stack.push(value1);
				throw new ExecutionException("The two topmost values of the operand stack must not be category 2 types when using " + getName() + ".");
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
		return "dup_x1";
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

}
