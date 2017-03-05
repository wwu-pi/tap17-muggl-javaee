package de.wwu.muggl.symbolic.searchAlgorithms.choice.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.And;
import de.wwu.muggl.solvers.expressions.AndList;
import de.wwu.muggl.solvers.expressions.CharVariable;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.array.ISymbolicArrayref;
import de.wwu.muggl.solvers.expressions.array.cstr.SymbolicCharArrayEqual;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class StringEqualityComparisonChoicePoint implements ChoicePoint {

	protected boolean generatedEquality = false;
	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected int pcWithJump;
	protected ChoicePoint parent;
	protected long number;
	
	protected Object reference1;
	protected Object reference2;
	protected int constraintLevel;
	protected ConstraintExpression constraintExpression;
	
	public StringEqualityComparisonChoicePoint(
			Frame frame, int pc, int pcNext, ChoicePoint parent,			
			Object reference1, Object reference2) {
		if( !(reference1 instanceof ReferenceVariable) && !(reference2 instanceof ReferenceVariable) ) {
			throw new IllegalArgumentException("At least one of the reference values must be of type ReferenceVariable, otherwise no choice point can be generated here");
		}
		
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		this.number = 0;
		this.parent = parent;
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		
		this.reference1 = reference1;
		this.reference2 = reference2;
		
		SolverManager solverManager = ((SymbolicVirtualMachine)this.frame.getVm()).getSolverManager();
		try {
			if(reference1 instanceof ReferenceVariable && reference2 instanceof ReferenceVariable) {
				Field valueField = ((Objectref)reference1).getInitializedClass().getClassFile().getFieldByName("value");
				SymbolicArrayref symArrRef1 = (SymbolicArrayref)((ReferenceVariable)reference1).getField(valueField);
				SymbolicArrayref symArrRef2 = (SymbolicArrayref)((ReferenceVariable)reference2).getField(valueField);
				this.constraintExpression = new SymbolicCharArrayEqual(symArrRef1, symArrRef2);
			}
			else if(reference1 instanceof ReferenceVariable && ((Objectref)reference2).getObjectType().equals("java.lang.String")) {
				Field valueField = ((Objectref)reference2).getInitializedClass().getClassFile().getFieldByName("value");
				SymbolicArrayref symArrRef = (SymbolicArrayref)((ReferenceVariable)reference1).getField(valueField);
				Arrayref arrRef = (Arrayref)((Objectref)reference2).getField(valueField);
				this.constraintExpression = imposeEqualityConstraints(symArrRef, arrRef);
			} 
			else if(reference2 instanceof ReferenceVariable && ((Objectref)reference1).getObjectType().equals("java.lang.String")) {
				Field valueField = ((Objectref)reference1).getInitializedClass().getClassFile().getFieldByName("value");
				SymbolicArrayref symArrRef = (SymbolicArrayref)((ReferenceVariable)reference2).getField(valueField);
				Arrayref arrRef = (Arrayref)((Objectref)reference1).getField(valueField);
				this.constraintExpression = imposeEqualityConstraints(symArrRef, arrRef);
			}
			
			solverManager.addConstraint(this.constraintExpression);
			this.frame.getOperandStack().push(NumericConstant.getOne(Expression.BOOLEAN));
		
			if(!solverManager.hasSolution()) {
				tryTheNegatedConstraint(solverManager, constraintExpression);
			}
		} catch (SolverUnableToDecideException e) {
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.generatedEquality = true;
		} catch (TimeoutException e) {
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.generatedEquality = true;
		}
	}
	
	private void tryTheNegatedConstraint(
			SolverManager solverManager,
			ConstraintExpression constraintExpression2) {	
	}
	
	// a symbolic array should have the values of a concrete array
	protected ConstraintExpression imposeEqualityConstraints(SymbolicArrayref symbolicValueArray, Arrayref objectArray) {
		List<ConstraintExpression> list = new ArrayList<>();
		list.add(NumericEqual.newInstance(symbolicValueArray.getSymbolicLength(), NumericConstant.getInstance(objectArray.length, Expression.INT)));
		for(int i=0; i<objectArray.length; i++) {
			CharVariable element = (CharVariable)symbolicValueArray.getElement(i);
			if(element == null) {
				element = new CharVariable(symbolicValueArray.getName()+".element."+i, Expression.INT);
				symbolicValueArray.setElementAt(i, element);
			}
			list.add(NumericEqual.newInstance(element, (IntConstant)objectArray.getElement(i)));
		}
		return new AndList(list);
	}
	
	// two symolic array should be equal
	protected ConstraintExpression imposeEqualityConstraints(SymbolicArrayref symbolicValueArray1, SymbolicArrayref symbolicValueArray2) {
		List<ConstraintExpression> list = new ArrayList<>();
		list.add(NumericEqual.newInstance(symbolicValueArray1.getSymbolicLength(), symbolicValueArray2.getSymbolicLength()));
		
		
		return new AndList(list);
	}

	protected ConstraintExpression imposeEqualityConstraintsabc(ReferenceVariable referenceVariable, Objectref stringObjRef) throws TimeoutException, SolverUnableToDecideException {
		List<ConstraintExpression> list = new ArrayList<>();
		Field valueField = stringObjRef.getInitializedClass().getClassFile().getFieldByName("value");
		Arrayref arrRef = (Arrayref)stringObjRef.getField(valueField);
		SymbolicArrayref symArrRef = (SymbolicArrayref)referenceVariable.getField(valueField);
		SolverManager solverManager = ((SymbolicVirtualMachine)this.frame.getVm()).getSolverManager();
//		solverManager.addConstraint(NumericEqual.newInstance(symArrRef.getSymbolicLength(), NumericConstant.getInstance(arrRef.length, Expression.INT)));
		list.add(NumericEqual.newInstance(symArrRef.getSymbolicLength(), NumericConstant.getInstance(arrRef.length, Expression.INT)));
		for(int i=0; i<arrRef.length; i++) {
			CharVariable element = (CharVariable)symArrRef.getElement(i);
			if(element == null) {
				element = new CharVariable(symArrRef.getName()+".element."+i, Expression.INT);
				symArrRef.setElementAt(i, element);
			}
			// TODO: hier nicht direkt drauf setzen
//			solverManager.addConstraint(NumericEqual.newInstance(element, (IntConstant)arrRef.getElement(i)));
			list.add(NumericEqual.newInstance(element, (IntConstant)arrRef.getElement(i)));
		}
		return new AndList(list);
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
		return true;
	}


	public void applyStateChanges() {
		System.out.println("do: "+reference1 + "!="+reference2);
		this.constraintExpression = constraintExpression.negate();
		this.frame.getOperandStack().push(NumericConstant.getZero(Expression.BOOLEAN));
	}

	/**
	 * Get a string representation of the type of choice point.
	 * @return A string representation of the type of choice point
	 */
	public String getChoicePointType() {
		return "reference value comparison choice point";
	}

	@Override
	public boolean hasAnotherChoice() {
		return !this.generatedEquality;
	}

	@Override
	public void changeToNextChoice() throws MugglException {
		if (!this.generatedEquality) {
			this.generatedEquality = true;
			System.out.println("Change to next choice");
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
