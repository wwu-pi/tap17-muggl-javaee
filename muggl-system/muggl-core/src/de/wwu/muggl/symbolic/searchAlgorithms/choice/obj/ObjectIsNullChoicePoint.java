package de.wwu.muggl.symbolic.searchAlgorithms.choice.obj;

import java.util.Stack;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;

public class ObjectIsNullChoicePoint implements ChoicePoint {
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

	
	
	protected boolean jumpToIsNull;
	
	protected Objectref objectReference;
	
	protected int constraintLevel;
	
	// Fields.
	private boolean alreadyVisitedNonJumpingBranch = false;
	private Stack<TrailElement> trail = new Stack<TrailElement>();
	/**
	 * Flag to determine if the execution time should be measured.
	 */
	protected boolean measureExecutionTime;
	/**
	 * Temporary field for time measuring.
	 */
	protected long timeSolvingTemp;
	
	public ObjectIsNullChoicePoint(Frame frame, int pc, int pcNext, int pcWithJump, Objectref objectReference, boolean jumpToIsNull, ChoicePoint parent) {
		// Possible exceptions.
		if (frame == null) throw new NullPointerException("The Frame must not be null.");
		if (objectReference == null) throw new NullPointerException("The ConstraintExpression must not be null.");

		// Set the fields.
		this.number = 0;
		this.parent = parent;
		if(parent != null) {
			this.number = parent.getNumber()+1;
		}
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.pcWithJump = pcWithJump;
		this.objectReference = objectReference;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		this.measureExecutionTime = Options.getInst().measureSymbolicExecutionTime;
		this.jumpToIsNull = jumpToIsNull;
		
		if(objectReference instanceof EntityObjectref) {
			EntityObjectref eo = (EntityObjectref)objectReference;
			eo.getRequiredEntity().setIsNull(this.jumpToIsNull);
			eo.setIsNull(this.jumpToIsNull);
			this.frame.getVm().setPC(pcWithJump);
		}
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
		return null;
	}

	/**
	 * Setter for the ConstraintExpression of this ChoicePoint.
	 * @param constraintExpression The new ConstraintExpression for this ChoicePoint.
	 */
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
	}
	
	/**
	 * Indicates whether this choice point enforces changes to the constraint system. As this
	 * is a conditional jump choice point, it changes the constraint system with an constraint
	 * that describes the condition.
	 * @return true
	 */
	public boolean changesTheConstraintSystem() {
		return false;
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
		return "object reference is null choice point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}

	@Override
	public boolean hasAnotherChoice() {
		return !this.alreadyVisitedNonJumpingBranch;
	}

	@Override
	public void changeToNextChoice() throws MugglException {
		this.alreadyVisitedNonJumpingBranch = true;
		if(this.objectReference instanceof EntityObjectref) {
			EntityObjectref eo = (EntityObjectref)this.objectReference;
			eo.setIsNull(!this.jumpToIsNull);
			eo.getRequiredEntity().setIsNull(!this.jumpToIsNull);
			eo.setCanBeNull(false);
		}
		((JPAVirtualMachine)this.frame.getVm()).printVODB();
	}

	@Override
	public boolean hasTrail() {
		return true;
	}

	@Override
	public Stack<TrailElement> getTrail() {
		return this.trail;
	}

	@Override
	public void addToTrail(TrailElement element) {
		this.trail.push(element);		
	}
}
