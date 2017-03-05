package de.wwu.muggl.db.sym.var;

import java.io.PrintStream;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.constraints.Assignment;

public class EntityReferenceVariable implements Variable {

	protected String name;
	protected Class<?> type;
	
	public EntityReferenceVariable(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public void checkTypes() throws TypeCheckException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Expression insert(Solution solution, boolean produceNumericSolution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression insertAssignment(Assignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConstant() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString(boolean useInternalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toTexString(boolean useInternalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toHaskellString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInternalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeToLog(PrintStream logStream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInternalVariable() {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public Variable getClone() {
//		return new EntityReferenceVariable(name, type);
//	}
}
