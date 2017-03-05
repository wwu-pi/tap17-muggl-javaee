package de.wwu.muggl.symbolic.searchAlgorithms.choice.fpComparison;

import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * A DoubleComparisonChoicePoint is generated whenever the instructions dcmpg or dcmpl are reached.
 * The instructions are used to compare double types and leaves four possibilites. This choice point
 * will make sure all four will be checked.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-03
 */
public class DoubleComparisonChoicePoint extends FpComparisonChoicePoint {

	/**
	 * Create the double comparison choice point.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param less If set to true, the choice point will have the behaviour of dcmpl; otherwise, it
	 *        will behave like dcmpg.
	 * @param leftTerm The term of double variables and constants of the left hand side of the
	 *        comparison.
	 * @param rightTerm The term of double variables and constants of the right hand side of the
	 *        comparison.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public DoubleComparisonChoicePoint(Frame frame, int pc, int pcNext, boolean less,
			Term leftTerm, Term rightTerm) throws SymbolicExecutionException {
		super(frame, pc, pcNext, less, leftTerm, rightTerm);
	}

	/**
	 * Create the double comparison choice point that has a parent ChoicePoint.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param parent The parent ChoicePoint
	 * @param less If set to true, the choice point will have the behaviour of dcmpl; otherwise, it
	 *        will behave like dcmpg.
	 * @param leftTerm The term of double variables and constants of the left hand side of the
	 *        comparison.
	 * @param rightTerm The term of double variables and constants of the right hand side of the
	 *        comparison.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public DoubleComparisonChoicePoint(Frame frame, int pc, int pcNext, boolean less,
			Term leftTerm, Term rightTerm, ChoicePoint parent) throws SymbolicExecutionException {
		super(frame, pc, pcNext, less, leftTerm, rightTerm, parent);
	}

	/**
	 * Get a NumericConstant representing the double value NaN.
	 *
	 * @return A NumericConstant representing the double value NaN.
	 */
	@Override
	protected NumericConstant getNaNConstant() {
		return DoubleConstant.getInstance(Double.NaN);
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
			if (Double.valueOf(((DoubleConstant) term).getDoubleValue()).isNaN()) {
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
		return "double comparison choice point";
	}

}
