package de.wwu.muggl.solvers.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

public class AndList extends ConstraintExpression {

	public List<ConstraintExpression> constraintExpressions;

	public AndList(List<ConstraintExpression> constraintExpressions) {
		this.constraintExpressions = constraintExpressions;
	}

	public List<ConstraintExpression> getConstraintExpressions() {
		return constraintExpressions;
	}

	@Override
	public void checkTypes() throws TypeCheckException {
		
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	private String getListString() {
		String s = "{";
		for(ConstraintExpression c : this.constraintExpressions) {
			s+=c.toString()+ " AND ";
		}
		return s+"}";
	}
	
	@Override
	public String toString(boolean useInternalVariableNames) {
		return "And="+getListString();
	}

	@Override
	public byte getType() {
		return -1;
	}

	@Override
	public String toTexString(boolean useInternalVariableNames) {
		return toString();
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
	public ConstraintExpression insert(Solution solution,
			boolean produceNumericSolution) {
		return null;
	}

	@Override
	public ConstraintExpression negate() {
		ConstraintExpression ce = this.constraintExpressions.get(0);
		List<ConstraintExpression> dest = new ArrayList<ConstraintExpression>(this.constraintExpressions);
		dest.set(0, ce.negate());
		return new AndList(dest);
	}

}
