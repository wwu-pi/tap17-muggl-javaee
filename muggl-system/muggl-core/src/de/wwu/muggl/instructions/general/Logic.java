package de.wwu.muggl.instructions.general;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with a concrete method for logic operations. Abstract instructions
 * can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-23
 */
public abstract class Logic extends GeneralInstruction implements StackPop, StackPush {

	/**
	 * Execute the inheriting instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		Stack<Object> stack = frame.getOperandStack();
		Term term2 = (Term) stack.pop();
		Term term1 = (Term) stack.pop();
		stack.push(calculate(term1, term2));
	}

	/**
	 * Calculate the two terms with the applicable logic operation.
	 *
	 * @param term1 The first Term.
	 * @param term2 The second Term.
	 * @return The resulting Term.
	 */
	protected abstract Term calculate(Term term1, Term term2);

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
