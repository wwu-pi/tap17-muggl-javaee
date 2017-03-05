package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.general.CompareLong;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>lcmp</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class LCmp extends CompareLong implements Instruction {

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		Stack<Object> stack = frame.getOperandStack();
		Long value2 = (Long) stack.pop();
		Long value1 = (Long) stack.pop();
		if (value1 > value2) {
			stack.push(Integer.valueOf(1));
		} else if (value1 < value2) {
			stack.push(Integer.valueOf(-1));
		} else {
			stack.push(Integer.valueOf(0));
		}
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic
	 *         execution.
	 */
	@Override
	public void executeSymbolically(Frame frame)
			throws SymbolicExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			Term term2 = (Term) stack.pop();
			Term term1 = (Term) stack.pop();

			// Check if both values are constant.
			if (term1.isConstant() && term2.isConstant()) {
				// Execute without generating a choice point.
				Long value1 = ((LongConstant) term1).getLongValue();
				Long value2 = ((LongConstant) term2).getLongValue();
				if (value1 > value2) {
					stack.push(IntConstant.getInstance(1));
				} else if (value1 < value2) {
					stack.push(IntConstant.getInstance(-1));
				} else {
					stack.push(IntConstant.getInstance(0));
				}
			} else {
			/*
			 * Create the ConstraintExpression and generate a new ChoicePoint. It will set the pc.
			 */
			((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, term1, term2);
			}
		} catch (SymbolicExecutionException e) {
			symbolicExecutionFailedWithAnExecutionException(e);
		}
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "lcmp";
	}

}
