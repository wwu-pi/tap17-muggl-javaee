package de.wwu.muggl.solvers.expressions.array.meta;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.array.ISymbolicArrayref;
import de.wwu.muggl.solvers.expressions.post.PendingConstraint;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

public abstract class SymbolicArrayrefEqual extends ConstraintExpression implements PendingConstraint {

	protected ISymbolicArrayref symArray1;
	protected ISymbolicArrayref symArray2;
	
	public SymbolicArrayrefEqual(ISymbolicArrayref symArray1, ISymbolicArrayref symArray2) {
		this.symArray1 = symArray1;
		this.symArray2 = symArray2;
	}
	
	public ISymbolicArrayref getSymbolicArrayref1() {
		return this.symArray1;
	}
	
	public ISymbolicArrayref getSymbolicArrayref2() {
		return this.symArray2;
	}

	@Override
	public boolean isConstant() {
		return false;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": array1=["+symArray1+"], array2=["+symArray2+"]";
	}

	@Override
	public String toString(boolean useInternalVariableNames) {
		return toString();
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
	public ConstraintExpression insert(Solution solution, boolean produceNumericSolution) {
		return null;
	}
}
