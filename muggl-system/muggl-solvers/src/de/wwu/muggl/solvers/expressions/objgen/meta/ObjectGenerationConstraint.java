package de.wwu.muggl.solvers.expressions.objgen.meta;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

public abstract class ObjectGenerationConstraint extends ConstraintExpression {

	@Override
	public String toString() {
		return toString(false);
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public byte getType() {
		return 0;
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
	public ConstraintExpression insert(Solution solution, boolean produceNumericSolution) {
		return null;
	}

}
