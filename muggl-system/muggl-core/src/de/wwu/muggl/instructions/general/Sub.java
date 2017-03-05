package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.solvers.expressions.Difference;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with some concrete methods for the substraction of two values.
 * Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-02-01
 */
public abstract class Sub extends Arithmetic  implements JumpNever {

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
		return Difference.newInstance(element1, element2);
	}

}
