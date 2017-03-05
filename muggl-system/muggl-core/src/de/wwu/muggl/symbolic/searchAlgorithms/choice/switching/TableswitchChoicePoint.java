package de.wwu.muggl.symbolic.searchAlgorithms.choice.switching;

import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LessThan;
import de.wwu.muggl.solvers.expressions.Or;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * A TableswitchChoicePoint is generated whenever one the instruction tableswitch is reached and the
 * element on top of the operand stack of the executed frame is not constant. The choice point will
 * make sure that all possible jumping targets are evaluated.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class TableswitchChoicePoint extends SwitchingChoicePoint {
	// Fields regarding the choice point.
	private IntConstant low;
	private IntConstant high;

	/**
	 * Create the tableswitch choice point.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param termFromStack The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 * @param keys The possible keys.
	 * @param pcs The possible jump targets.
	 * @param low The "low" boundary of the tableswitch instruction.
	 * @param high  The "high" boundary of the tableswitch instruction.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets or if there are no choices at all.
	 * @throws NullPointerException If either of the specified arrays or the boundaries is null.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public TableswitchChoicePoint(Frame frame, int pc, int pcNext, Term termFromStack,
			IntConstant[] keys, int[] pcs, IntConstant low, IntConstant high)
			throws SymbolicExecutionException {
		super(frame, pc, pcNext, termFromStack, keys, pcs);
		if (low == null)
			throw new NullPointerException("The \"low\" boundary must not be null.");
		if (high == null)
			throw new NullPointerException("The \"high\" boundary must not be null.");
		this.low = low;
		this.high = high;
	}

	/**
	 * Create a ChoicePoint that has a parent ChoicePoint.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param termFromStack The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 * @param keys The possible keys.
	 * @param pcs The possible jump targets.
	 * @param low The "low" boundary of the tableswitch instruction.
	 * @param high  The "high" boundary of the tableswitch instruction.
	 * @param parent The parent ChoicePoint.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets.
	 * @throws NullPointerException If either of the specified arrays or the boundaries is null.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public TableswitchChoicePoint(Frame frame, int pc, int pcNext, Term termFromStack,
			IntConstant[] keys, int[] pcs, IntConstant low, IntConstant high, ChoicePoint parent)
			throws SymbolicExecutionException {
		super(frame, pc, pcNext, termFromStack, keys, pcs, parent);
		if (low == null)
			throw new NullPointerException("The \"low\" boundary must not be null.");
		if (high == null)
			throw new NullPointerException("The \"high\" boundary must not be null.");
		this.low = low;
		this.high = high;
	}

	/**
	 * Change to the next choice.
	 *
	 * @throws IllegalStateException If there are no more choices.
	 */
	@Override
	public void changeToNextChoice() {
		// Is it the default case? It can be found at position 0.
		if (this.step == 0) {
			/*
			 * This is the default case for tableswitch. Using a constraint expression that uses the
			 * lowest and the highest value.
			 */
			ConstraintExpression expression1 = LessThan.newInstance(this.termFromStack, this.low);
			ConstraintExpression expression2 = GreaterThan.newInstance(this.termFromStack, this.high);
			this.constraintExpression = Or.newInstance(expression1, expression2);
			this.jumpTo = this.pcs[0];
			this.step++;
			this.appliedState = false;
		} else {
			// Invoke the super implementation.
			super.changeToNextChoice();
		}

	}

	/**
	 * Get a string representation of the type of choice point.
	 *
	 * @return A string representation of the type of choice point
	 */
	@Override
	public String getChoicePointType() {
		return "tableswitch choice point";
	}

}
