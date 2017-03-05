package de.wwu.muggl.symbolic.searchAlgorithms.choice.ref;

import java.util.Stack;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.objgen.ObjectReferenceEqual;
import de.wwu.muggl.solvers.expressions.objgen.ObjectReferenceNotEqual;
import de.wwu.muggl.solvers.type.IObjectreference;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.EquationViolationException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class ReferenceVariableComparisonChoicePoint implements ChoicePoint {

	protected boolean alreadyVisitedNonJumpingBranch = false;
	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected int pcWithJump;
	protected ChoicePoint parent;
	protected long number;
	
	protected Object reference1;
	protected Object reference2;
	protected boolean compareEquality;
	
	protected int constraintLevel;
	
	protected ConstraintExpression constraintExpression;
	
	public ReferenceVariableComparisonChoicePoint(
			Frame frame, int pc, int pcNext, int pcWithJump, ChoicePoint parent,
			Object reference1, Object reference2, boolean compareEquality) 
	throws EquationViolationException, SolvingException {
		if( !(reference1 instanceof ReferenceVariable) && (reference2 instanceof ReferenceVariable) ) {
			throw new IllegalArgumentException("At least one of the reference values must be of type ReferenceVariable, otherwise no choice point can be generated here");
		}
		
		this.number = 0;
		this.parent = parent;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.pcWithJump = pcWithJump;
		
		this.reference1 = reference1;
		this.reference2 = reference2;
		this.compareEquality = compareEquality;
		
		SolverManager solverManager = ((SymbolicVirtualMachine)frame.getVm()).getSolverManager();
		
		this.constraintExpression = null;
		
		System.out.println("Create a choice point in which reference1=["+reference1+"], reference2=["+reference2+"], checkForEquality="+compareEquality);
		System.out.println("Change to the true branch: " + this.pcWithJump);
		if(compareEquality) {
			if(this.reference1 instanceof ReferenceVariable) {
//				changeObjectReferenceInLocalVariables(frame, (ReferenceVariable)reference1, reference2);
//				this.reference1 = reference2;
				
//				ObjectReferenceEqual objRefEqual = new ObjectReferenceEqual((ReferenceVariable)reference1, reference2);
				ObjectReferenceEqual objRefEqual = new ObjectReferenceEqual((ReferenceVariable)this.reference1, reference2);
				constraintExpression = objRefEqual;
			} else {
//				changeObjectReferenceInLocalVariables(frame, (ReferenceVariable)reference2, reference1);
//				this.reference2 = reference1;
				
				ObjectReferenceEqual objRefEqual = new ObjectReferenceEqual((ReferenceVariable)this.reference2, reference1);
				constraintExpression = objRefEqual;
			}
		} else {
			if(this.reference1 instanceof ReferenceVariable) {
				ObjectReferenceNotEqual objRefNotEqual = new ObjectReferenceNotEqual((ReferenceVariable)this.reference1, reference2);
				constraintExpression = objRefNotEqual;
			} else {
				ObjectReferenceNotEqual objRefNotEqual = new ObjectReferenceNotEqual((ReferenceVariable)this.reference2, reference1);
				constraintExpression = objRefNotEqual;
			}
		}
		
		solverManager.addConstraint(constraintExpression);
		
		// check if the constraint does not violate any other constraints:
		try {
			if(!solverManager.hasSolution()) {
				tryTheNegatedConstraint(solverManager, constraintExpression);
			} else {
				frame.getVm().setPC(pcWithJump);
			}
		} catch (SolverUnableToDecideException e) {
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.alreadyVisitedNonJumpingBranch = true;
		} catch (TimeoutException e) {
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.alreadyVisitedNonJumpingBranch = true;
		}
		
		
	}
	
	private void tryTheNegatedConstraint(SolverManager solverManager, ConstraintExpression constraintExpression) throws EquationViolationException, SolvingException {
		try {
			solverManager.removeConstraint();
			solverManager.addConstraint(constraintExpression.negate());
			if (solverManager.hasSolution()) {
				this.frame.getVm().setPC(this.pcNext);
				this.alreadyVisitedNonJumpingBranch = true;
			} else {
				throw new EquationViolationException("Cannot continue with this choice point, since equations are violated.");
			}
		} catch (SolverUnableToDecideException e) {
			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
		} catch (TimeoutException e) {
			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
		}
			
	}

	// change reference1 to reference2 in local variables, if it exists there...
	private void changeObjectReferenceInLocalVariables(Frame frame, ReferenceVariable reference1, Object reference2) {
		for(int i=0; i<frame.getLocalVariables().length; i++) {
			Object localVar = frame.getLocalVariables()[i];
			if(localVar instanceof Objectref) {
				Objectref objectLocalVar = (Objectref)localVar;
				if(objectLocalVar.getObjectId().equals(reference1.getObjectId())) {
					frame.setLocalVariable(i, reference2);
				}
			} else if(localVar instanceof SymbolicArrayref) { 
				SymbolicArrayref symbolicArray = (SymbolicArrayref)localVar;
				for(Integer symElementIndex : symbolicArray.getElements().keySet()) {
					Object symbolicElement = symbolicArray.getElements().get(symElementIndex);
					if(symbolicElement instanceof ReferenceVariable) {
						symbolicArray.setElementAt(symElementIndex, reference2);
						changeObjectReferenceInLocalVariables(frame, (ReferenceVariable)symbolicElement, reference2);
					}
				}
			}
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
		return "reference value comparison choice point";
	}

	@Override
	public boolean hasAnotherChoice() {
		return !this.alreadyVisitedNonJumpingBranch;
	}

	@Override
	public void changeToNextChoice() throws MugglException {
		if (!this.alreadyVisitedNonJumpingBranch) {
			this.alreadyVisitedNonJumpingBranch = true;
			compareEquality = !compareEquality;
			// Negate the constraint expression.
			System.out.println("Change to next choice: create a choice point in which reference1=["+reference1+"], reference2=["+reference2+"], checkForEquality="+compareEquality);
		}
	}

	@Override
	public ConstraintExpression getConstraintExpression() {
		return this.constraintExpression.negate();
	}

	@Override
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
		
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
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
