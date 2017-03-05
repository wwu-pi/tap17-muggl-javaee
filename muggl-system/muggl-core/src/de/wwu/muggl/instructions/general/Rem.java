package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.solvers.expressions.Modulo;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with some concrete methods for the calculation of the remainder of
 * the division of two values. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public abstract class Rem extends Arithmetic implements JumpException {

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
	 */
	@Override
	protected Term calculate(Term element1, Term element2) {
		return Modulo.newInstance(element1, element2);
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
