//package de.wwu.muggl.solvers.expressions;
//
//import java.io.PrintStream;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//
//public class IteratorVariable implements Variable {
//	
//	protected CollectionVariable collection;
//	
//	public IteratorVariable(CollectionVariable collection) {
//		this.collection = collection;
//	}
//	
//	public Object hasNext() {
//		return new NumericVariable("hasMoreElement", Expression.INT);
//	}
//	
//	public Objectref next() {
//		return null;
//	}
//	
//	
//	
//	
//	
//	
//
//	@Override
//	public void checkTypes() throws TypeCheckException {
//		System.out.println("*** ?? what to do here ? *** please IMPLEMENT ME");
//	}
//
//	@Override
//	public Expression insert(Solution solution, boolean produceNumericSolution) {
//		System.out.println("*** ?? what to do here ? *** please IMPLEMENT ME");
//		return null;
//	}
//
//	@Override
//	public Expression insertAssignment(Assignment assignment) {
//		System.out.println("*** ?? what to do here ? *** please IMPLEMENT ME");
//		return null;
//	}
//
//	@Override
//	public boolean isBoolean() {
//		System.out.println("*** ?? what to do here ? *** please IMPLEMENT ME");
//		return false;
//	}
//
//	@Override
//	public boolean isConstant() {
//		System.out.println("*** ?? what to do here ? *** please IMPLEMENT ME");
//		return false;
//	}
//
//	@Override
//	public String toString(boolean useInternalVariableNames) {
//		return this.toString();
//	}
//	
//	@Override
//	public String toString() {
//		return this.collection.getName()+".iterator";
//	}
//
//	@Override
//	public byte getType() {
//		System.out.println("*** ?? what to do here ? *** please IMPLEMENT ME");
//		return -1;
//	}
//
//	@Override
//	public String toTexString(boolean useInternalVariableNames) {
//		return null;
//	}
//
//	@Override
//	public String toHaskellString() {
//		return null;
//	}
//
//	@Override
//	public String getInternalName() {
//		return this.collection.getName()+".iterator";
//	}
//
//	@Override
//	public String getName() {
//		return this.collection.getName()+".iterator";
//	}
//
//	@Override
//	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
//		return null;
//	}
//
//	@Override
//	public void writeToLog(PrintStream logStream) {	
//	}
//
//	@Override
//	public boolean isInternalVariable() {
//		return false;
//	}
//
//
//
//
//
//
//
//}
