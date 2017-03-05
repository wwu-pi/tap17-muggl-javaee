package de.wwu.muggl.instructions.general;

import java.util.Stack;

import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with some concrete methods for comparison instructions of the types
 * double. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public abstract class CompareDouble extends CompareFp {

	/**
	 * Execute the inheriting instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		Stack<Object> stack = frame.getOperandStack();
		Double value2 = (Double) stack.pop();
		Double value1 = (Double) stack.pop();
		if (value1.isNaN() || value2.isNaN()) {
			stack.push(getPushValueForNaN());
		} else if (value1 > value2) {
			stack.push(Integer.valueOf(1));
		} else if (value1 < value2) {
			stack.push(Integer.valueOf(-1));
		} else {
			stack.push(Integer.valueOf(0));
		}
	}

	/**
	 * Get the result that will be pushed onto the operand stack if either value1 or value 2 is NaN.
	 *
	 * @return A new Integer with either the value 1 or -1, according to the implementing
	 *         instruction.
	 */
	protected abstract Integer getPushValueForNaN();

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic
	 *         execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			Term term2 = (Term) stack.pop();
			Term term1 = (Term) stack.pop();

			// Check if both values are constant.
			if (term1.isConstant() && term2.isConstant()) {
				// Execute without generating a choice point.
				Double value1 = ((DoubleConstant) term1).getDoubleValue();
				Double value2 = ((DoubleConstant) term2).getDoubleValue();
				if (value1.isNaN() || value2.isNaN()) {
					stack.push(IntConstant.getInstance(getPushValueForNaN()));
				} else if (value1 > value2) {
					stack.push(IntConstant.getInstance(1));
				} else if (value1 < value2) {
					stack.push(IntConstant.getInstance(-1));
				} else {
					stack.push(IntConstant.getInstance(0));
				}
			} else {
				/*
				 * Create the ConstraintExpression and generate a new ChoicePoint. It will set the
				 * pc.
				 */
				((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this,
						pushMinusOneForNaN(), term1, term2);
			}
		} catch (SymbolicExecutionException e) {
			symbolicExecutionFailedWithAnExecutionException(e);
		}
	}

	/**
	 * Determine if this is dcmpg or dcmpl. Dcmpg will push 1 if a NaN value is encountered, while
	 * dcmpl will push -1 in such a case.
	 *
	 * @return true, if the inheriting instruction is dcmpl; false if it is dcmpg.
	 */
	protected abstract boolean pushMinusOneForNaN();

	/**
	 * Get the number of other bytes for this instruction.
	 *
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
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
		byte[] types = {ClassFile.T_DOUBLE, ClassFile.T_DOUBLE};
		return types;
	}

}
