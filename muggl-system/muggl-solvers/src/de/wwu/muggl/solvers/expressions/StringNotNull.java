package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

public class StringNotNull extends ConstraintExpression {
	
	protected StringVariable stringVariable;

	public StringNotNull(StringVariable variable) {
		this.stringVariable = variable;
	}
	
	public StringVariable getStringVariable() {
		return this.stringVariable;
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
		return "String variable NOT null: ["+this.stringVariable+"]";
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
