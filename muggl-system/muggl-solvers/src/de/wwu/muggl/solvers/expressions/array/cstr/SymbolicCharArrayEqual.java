package de.wwu.muggl.solvers.expressions.array.cstr;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.array.ISymbolicArrayref;
import de.wwu.muggl.solvers.expressions.array.meta.SymbolicArrayrefEqual;

public class SymbolicCharArrayEqual extends SymbolicArrayrefEqual {

	public SymbolicCharArrayEqual(ISymbolicArrayref symArray1, ISymbolicArrayref symArray2) {
		super(symArray1, symArray2);
	}

	@Override
	public void checkTypes() throws TypeCheckException {
		
	}

	@Override
	public ConstraintExpression negate() {
		return new SymbolicCharArrayNotEqual(symArray1, symArray2);
	}
	
	
}
