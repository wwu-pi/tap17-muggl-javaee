package de.wwu.muggl.symbolic.searchAlgorithms.choice.fpComparison;

import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * A FloatComparisonChoicePoint is generated whenever the instructions fcmpg or fcmpl are reached.
 * The instructions are used to compare float types and leaves four possibilites. This choice point
 * will make sure all four will be checked.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-03
 */
public class FloatComparisonChoicePoint extends FpComparisonChoicePoint {

	/**
	 * Create the float comparison choice point.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param less If set to true, the choice point will have the behaviour of fcmpl;
	 *        otherwise, it will behave like fcmpg.
	 * @param leftTerm The term of float variables and constants of the left hand side of
	 *        the comparison.
	 * @param rightTerm The term of float variables and constants of the right hand side
	 *        of the comparison.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public FloatComparisonChoicePoint(Frame frame, int pc, int pcNext, boolean less, Term leftTerm, Term rightTerm)
			throws SymbolicExecutionException {
		super(frame, pc, pcNext, less, leftTerm, rightTerm);
	}

	/**
	 * Create the float comparison choice point that has a parent ChoicePoint.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param parent The parent ChoicePoint
	 * @param less If set to true, the choice point will have the behaviour of fcmpl;
	 *        otherwise, it will behave like fcmpg.
	 * @param leftTerm The term of float variables and constants of the left hand side of
	 *        the comparison.
	 * @param rightTerm The term of float variables and constants of the right hand side
	 *        of the comparison.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public FloatComparisonChoicePoint(Frame frame, int pc, int pcNext, boolean less, Term leftTerm,
			Term rightTerm, ChoicePoint parent) throws SymbolicExecutionException {
		super(frame, pc, pcNext, less, leftTerm, rightTerm, parent);
	}

	/**
	 * Get a NumericConstant representing the double value NaN.
	 *
	 * @return A NumericConstant representing the double value NaN.
	 */
	@Override
	protected NumericConstant getNaNConstant() {
		return FloatConstant.getInstance(Float.NaN);
	}

	/**
	 * Check if a term is constant and its value is NaN.
	 *
	 * @param term The term to check.
	 * @return true if it is constant and its value is NaN; false otherwise.
	 */
	@Override
	protected boolean isNaN(Term term) {
		if (term.isConstant()) {
			if (Float.valueOf(((FloatConstant) term).getFloatValue()).isNaN()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get a string representation of the type of choice point.
	 *
	 * @return A string representation of the type of choice point
	 */
	@Override
	public String getChoicePointType() {
		return "float comparison choice point";
	}

}
