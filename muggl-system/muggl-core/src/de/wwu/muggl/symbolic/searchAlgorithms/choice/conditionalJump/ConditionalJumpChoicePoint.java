package de.wwu.muggl.symbolic.searchAlgorithms.choice.conditionalJump;

import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;

/**
 * A ConditionalJumpChoicePoint is generated whenever a conditional jump statement is reached.
 * Conditional jumps branch the execution tree, leaving two possibilities: Continue at the next
 * instruction in the program flow, or jump to another instruction. To execute a program
 * symbolically, ChoicePoints are set during execution. By using a search algorithm ChoicePoints can
 * be used to track back in the program flow and visit alternative branches in the execution tree.
 * This does not only safe computation time, but enables the effective visiting of different
 * branches at all, since ChoicePoints safe information about what branch has already been visited.<br />
 * <br />
 * Search algorithms usually extend this class in order to have choice points that offer the
 * functionality needed by them.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-14
 */
public abstract class ConditionalJumpChoicePoint implements ChoicePoint {
	// General fields needed for each ChoicePoint, regardless of the search algorithm.
	/**
	 * The identification number of the choice point.
	 */
	protected long number;
	/**
	 * The parent choice point. Can be null to indicate that a choice point has no parent.
	 */
	protected ChoicePoint parent = null;
	/**
	 * The frame of the method that offers a choice.
	 */
	protected Frame frame;
	/**
	 * The pc of the instruction that offers a choice.
	 */
	protected int pc;
	/**
	 * The pc following the instruction that offers a choice.
	 */
	protected int pcNext;
	/**
	 * The pc that is executed after the instruction that offers a choice if the conditional jump is
	 * triggered.
	 */
	protected int pcWithJump;
	/**
	 * The ConstraintExpression describing the current choice of the conditional jump.
	 */
	protected ConstraintExpression constraintExpression;
	
	protected int constraintLevel;

	/**
	 * Create the initial ChoicePoint of an application.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the conditional jump instruction.
	 * @param pcWithJump The pc the conditional jump would jump to.
	 * @param constraintExpression The ConstraintExpression describing the choice of the conditional jump.
	 */
	public ConditionalJumpChoicePoint(Frame frame, int pc, int pcNext, int pcWithJump, ConstraintExpression constraintExpression) {
		// Possible exceptions.
		if (frame == null) throw new NullPointerException("The Frame must not be null.");
		if (constraintExpression == null) throw new NullPointerException("The ConstraintExpression must not be null.");

		// Set the fields.
		this.number = 0;
		this.parent = null;
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.pcWithJump = pcWithJump;
		this.constraintExpression = constraintExpression;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
	}

	/**
	 * Return this ChoicePoint's number.
	 *
	 * @return This ChoicePoint's number.
	 */
	public long getNumber() {
		return this.number;
	}

	/**
	 * Return the parent of this ChoicePoint.
	 * @return The parent of this ChoicePoint.
	 */
	public ChoicePoint getParent() {
		return this.parent;
	}

	/**
	 * Return the Frame of this ChoicePoint.
	 * @return The Frame of this ChoicePoint.
	 */
	public Frame getFrame() {
		return this.frame;
	}

	/**
	 * Return the pc of this ChoicePoint.
	 * @return The pc of this ChoicePoint.
	 */
	public int getPc() {
		return this.pc;
	}

	/**
	 * Return the pc without the jump of this ChoicePoint.
	 * @return The pc without the jump of this ChoicePoint.
	 */
	public int getPcNext() {
		return this.pcNext;
	}

	/**
	 * Return the pc with the jump of this ChoicePoint.
	 * @return The pc with the jump of this ChoicePoint.
	 */
	public int getPcWithJump() {
		return this.pcWithJump;
	}

	/**
	 * Return the ConstraintExpression of this ChoicePoint.
	 * @return The ConstraintExpression of this ChoicePoint.
	 */
	public ConstraintExpression getConstraintExpression() {
		return this.constraintExpression;
	}

	/**
	 * Setter for the ConstraintExpression of this ChoicePoint.
	 * @param constraintExpression The new ConstraintExpression for this ChoicePoint.
	 */
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
		this.constraintExpression = constraintExpression;
	}

	/**
	 * Indicates whether this choice point enforces changes to the constraint system. As this
	 * is a conditional jump choice point, it changes the constraint system with an constraint
	 * that describes the condition.
	 * @return true
	 */
	public boolean changesTheConstraintSystem() {
		return true;
	}

	/**
	 * Indicates whether this choice point enforces changes to the execution state. Conditional jump
	 * choice points do not need any state changes.
	 *
	 * @return false.
	 */
	public boolean enforcesStateChanges() {
		return false;
	}

	/**
	 * This method does nothing.
	 */
	public void applyStateChanges() { }

	/**
	 * Get a string representation of the type of choice point.
	 * @return A string representation of the type of choice point
	 */
	public String getChoicePointType() {
		return "conditional jump choice point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
