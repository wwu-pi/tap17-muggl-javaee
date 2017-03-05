package de.wwu.muggl.symbolic.searchAlgorithms.choice.obj;

import java.util.Stack;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.bytecode.Getfield;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;

public class GetFieldOfNullableObjectChoicePoint implements ChoicePoint {
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
	
	
	protected Objectref objectReference;
	
	protected int constraintLevel;
	
	
	protected Object fieldValue;
	
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
	
	protected Getfield instruction;
	
	protected Stack<Object> invokedByStack = new Stack<>();
		
	public GetFieldOfNullableObjectChoicePoint(Getfield instruction, Frame frame, int pc, int pcNext, Objectref objectReference, ChoicePoint parent) throws VmRuntimeException {
		// Possible exceptions.
		if (frame == null) throw new NullPointerException("The Frame must not be null.");
		if (objectReference == null) throw new NullPointerException("The ConstraintExpression must not be null.");

		// Set the fields.
		this.instruction = instruction;
		this.number = 0;
		this.parent = parent;
		if(parent != null) {
			this.number = parent.getNumber()+1;
		}
		this.frame = frame;
		
		if(this.frame.getInvokedBy() != null) {
			for(Object o : this.frame.getInvokedBy().getOperandStack()) {
				this.invokedByStack.push(o);
			}
		}
		
		this.pc = pc;
		this.pcNext = pcNext;
		this.objectReference = objectReference;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		this.measureExecutionTime = Options.getInst().measureSymbolicExecutionTime;
		
		if(objectReference instanceof EntityObjectref) {
			EntityObjectref eo = (EntityObjectref)objectReference;
			eo.getRequiredEntity().setIsNull(true);
			eo.setIsNull(true);
//			throw new VmRuntimeException(frame.getVm()
//					.generateExc("java.lang.NullPointerException"));
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
		return true;
	}


	public void applyStateChanges() { 
		if(this.objectReference instanceof EntityObjectref) {
			EntityObjectref eo = (EntityObjectref)this.objectReference;
			eo.setIsNull(false);
			eo.getRequiredEntity().setIsNull(false);
			eo.setCanBeNull(false);
		}
		
		this.frame.getOperandStack().push(this.fieldValue);
		this.frame.getVm().setPC(pcNext);
	}

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
			eo.setIsNull(false);
			eo.getRequiredEntity().setIsNull(false);
		}
		if(this.frame.getInvokedBy() != null) {
			for(Object o : this.invokedByStack) {
				this.frame.getInvokedBy().getOperandStack().push(o);
			}
		}
		this.fieldValue = this.instruction.getFieldValue(frame);
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