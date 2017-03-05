package de.wwu.muggl.symbolic.searchAlgorithms.choice.switching;

import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * A LookupswitchChoicePoint is generated whenever one the instruction lookupswitch is reached and
 * the element on top of the operand stack of the executed frame is not constant. The choice point
 * will make sure that all possible jumping targets are evaluated.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class LookupswitchChoicePoint extends SwitchingChoicePoint {

	/**
	 * Create the lookupswitch choice point.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param termFromStack The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 * @param keys The possible keys.
	 * @param pcs The possible jump targets.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets or if there are no choices at all.
	 * @throws NullPointerException If either of the specified arrays is null.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public LookupswitchChoicePoint(Frame frame, int pc, int pcNext, Term termFromStack,
			IntConstant[] keys, int[] pcs) throws SymbolicExecutionException {
		super(frame, pc, pcNext, termFromStack, keys, pcs);
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
	 * @param parent The parent ChoicePoint.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets.
	 * @throws NullPointerException If either of the specified arrays is null.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public LookupswitchChoicePoint(Frame frame, int pc, int pcNext, Term termFromStack,
			IntConstant[] keys, int[] pcs, ChoicePoint parent) throws SymbolicExecutionException {
		super(frame, pc, pcNext, termFromStack, keys, pcs, parent);
	}

	/**
	 * Change to the next choice.
	 *
	 * @throws IllegalStateException If there are no more choices.
	 */
	@Override
	public void changeToNextChoice() {
		// Is it the default case? It can be found at position 0, with a key value of null.
		if (this.step == 0 && this.keys[0] == null) {
			/*
			 * This is the default case for lookupswitch. Using a constraint expression that is true
			 * in any case.
			 */
			this.constraintExpression = NumericEqual.newInstance(this.termFromStack, this.termFromStack);
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
		return "lookupswitch choice point";
	}
}
