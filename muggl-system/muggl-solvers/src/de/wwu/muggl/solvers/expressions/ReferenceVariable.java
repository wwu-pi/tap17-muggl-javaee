//package de.wwu.muggl.solvers.expressions;
//
//import java.io.PrintStream;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//
//public class ReferenceVariable implements Variable {
//	
//	protected ReferenceValue referenceValue;
//	
//	public ReferenceVariable(ReferenceValue referenceValue) {
//		this.referenceValue = referenceValue;
//	}
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
//	public Expression insert(Solution solution,
//			boolean produceNumericSolution) {
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
//	public String toTexString(boolean inArrayEnvironment,
//			boolean useInternalVariableNames) {
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
////	@Override
////	public Variable getClone() {
////		// TODO Auto-generated method stub
////		return null;
////	}
//
//}
