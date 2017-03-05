package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.Quotient;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with some concrete methods for the division of two values. Concrete
 * instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public abstract class Div extends Arithmetic implements JumpException {

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}

	/**
	 * Calculate the two terms with the applicable arithmetic operation.
	 * @param element1 The first Term.
	 * @param element2 The second Term.
	 * @return The resulting Term.
	 * @throws ArithmeticException On an attempted division by zero.
	 */
	@Override
	protected Term calculate(Term element1, Term element2) {
		if (element2.isConstant()) {
			boolean isZero = false;
			switch (element2.getType()) {
				case Expression.BYTE:
					if (((IntConstant) element2).getValue() == 0) isZero = true;
					break;
				case Expression.DOUBLE:
					if (((DoubleConstant) element2).getValue() == 0.0) isZero = true;
					break;
				case Expression.FLOAT:
					if (((FloatConstant) element2).getValue() == 0.0f) isZero = true;
					break;
				case Expression.INT:
					if (((IntConstant) element2).getValue() == 0) isZero = true;
					break;
				case Expression.LONG:
					if (((LongConstant) element2).getValue() == 0L) isZero = true;
					break;
				case Expression.SHORT:
					if (((IntConstant) element2).getValue() == 0) isZero = true;
					break;
			}
			if (isZero) throw new ArithmeticException("Divison by zero.");
		}

		return Quotient.newInstance(element1, element2);
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = {"java.lang.ArithmeticException"};
		return exceptionTypes;
	}

}
