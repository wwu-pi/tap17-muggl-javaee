package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

public class EntityFieldValueEqualConstraint extends ConstraintExpression {

	protected Object object1;
	protected Object object2;
	
	public EntityFieldValueEqualConstraint(Object object1, Object object2) {
		this.object1 = object1;
		this.object2 = object2;
	}

	public Object getObject1() {
		return object1;
	}

	public Object getObject2() {
		return object2;
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
		return "object equal constraint: ["+this.object1+"] and ["+this.object2+"]";
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
