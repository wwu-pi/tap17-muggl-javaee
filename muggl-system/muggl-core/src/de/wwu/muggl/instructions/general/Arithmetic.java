package de.wwu.muggl.instructions.general;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with a concrete method for arithmetic operations. Abstract instructions
 * can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public abstract class Arithmetic extends GeneralInstruction implements StackPop, StackPush {

	/**
	 * Execute the inheriting instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		Stack<Object> stack = frame.getOperandStack();
		Term element2 = (Term) stack.pop();
		Term element1 = (Term) stack.pop();
		try {
			stack.push(calculate(element1, element2));
		} catch (ArithmeticException e) {
			Objectref objectref = frame.getVm().generateExc("java.lang.ArithmeticException", e.getMessage());
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, objectref);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		}
	}

	/**
	 * Calculate the two terms with the applicable arithmetic operation.
	 *
	 * @param element1 The first Term.
	 * @param element2 The second Term.
	 * @return The resulting Term.
	 * @throws ArithmeticException On an attempted division by zero.
	 */
	protected abstract Term calculate(Term element1, Term element2);

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
		return 1;
	}

}
