//package de.wwu.muggl.vm.impl.jpa;
//
//import java.io.PrintStream;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.TypeCheckException;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//import de.wwu.muggl.vm.impl.jpa.list.meta.ObjectrefAdapter;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class ReferenceVariableObjectref extends ObjectrefAdapter implements ReferenceVariable {
//	
//	protected String name;
//	protected String type;
//	
//	public ReferenceVariableObjectref(Objectref original, String name, String type) {
//		super(original);
//		this.name = name;
//		this.type = type;
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	@Override
//	public void checkTypes() throws TypeCheckException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public Expression insert(Solution solution, boolean produceNumericSolution) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Expression insertAssignment(Assignment assignment) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean isBoolean() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean isConstant() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public String toString(boolean useInternalVariableNames) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public String getTypeString() {
//		return this.type;
//	}
//	
//	@Override
//	public byte getType() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public String toTexString(boolean useInternalVariableNames) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String toHaskellString() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getInternalName() {
//		return this.name;
//	}
//
//	@Override
//	public String getName() {
//		return this.name;
//	}
//
//	@Override
//	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void writeToLog(PrintStream logStream) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public boolean isInternalVariable() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//
//
//	@Override
//	public int compareTo(Variable other) {
//		if(other instanceof ReferenceVariableObjectref) {
//			ReferenceVariableObjectref otherRef = (ReferenceVariableObjectref)other;
//			return this.name.compareTo(otherRef.name);
//		}
//		throw new ClassCastException();
//	}
//
//}
