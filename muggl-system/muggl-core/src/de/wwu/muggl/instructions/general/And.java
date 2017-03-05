package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.solvers.expressions.NumericAnd;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with some concrete methods for the and operarion.
 * Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-10-30
 */
public abstract class And extends Logic implements JumpNever {

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}

	/**
	 * Calculate the two terms with the applicable logic operation.
	 *
	 * @param term1 The first Term.
	 * @param term2 The second Term.
	 * @return The resulting Term.
	 */
	@Override
	protected Term calculate(Term term1, Term term2) {
		return NumericAnd.newInstance(term1, term2);
	}

}
