package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;
import de.wwu.muggl.solvers.type.IObjectreference;

public class ReferenceVariablesEqualConstraint extends ConstraintExpression {

	protected IObjectreference objectReference1;
	protected IObjectreference objectReference2;
	
	public ReferenceVariablesEqualConstraint(IObjectreference objectReference1, IObjectreference objectReference2) {
		this.objectReference1 = objectReference1;
		this.objectReference2 = objectReference2;
	}

	public IObjectreference getObjectReference1() {
		return objectReference1;
	}

	public IObjectreference getObjectReference2() {
		return objectReference2;
	}
	
	@Override
	public void checkTypes() throws TypeCheckException {
		
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public String toString(boolean useInternalVariableNames) {
		return "object references equal constraint: ["+this.objectReference1+"] and ["+this.objectReference2+"]";
	}

	@Override
	public byte getType() {
		return -1;
	}

	@Override
	public String toTexString(boolean useInternalVariableNames) {
		return null;
	}

	@Override
	public String toHaskellString() {
		return null;
	}

	@Override
	public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
		return null;
	}

	@Override
	public ConstraintExpression insertAssignment(Assignment assignment) {
		return null;
	}

	@Override
	public ConstraintExpression insert(Solution solution,boolean produceNumericSolution) {
		return null;
	}

	@Override
	public ConstraintExpression negate() {
		return null;
	}
}
