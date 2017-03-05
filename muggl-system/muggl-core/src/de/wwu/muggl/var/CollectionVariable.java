//package de.wwu.muggl.var;
//
//import java.io.PrintStream;
//import java.util.Collection;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.expressions.CollectionSize;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
//import de.wwu.muggl.solvers.expressions.IntConstant;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.TypeCheckException;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//import de.wwu.muggl.vm.VirtualMachine;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class CollectionVariable implements Variable {
//	
//	protected String name;
//	protected String type;
//	protected NumericVariable length;
//	protected IteratorVariable iterator;
//	protected Collection<Objectref> elements;
//	SymbolicVirtualMachine vm;
//	
//	public CollectionVariable(String name, String type, SymbolicVirtualMachine vm) {
//		this.name = name;
//		this.type = type;
//		this.vm = vm;
//		this.length = new NumericVariable(name + ".length", Expression.INT);
//		
//		// size must be at least zero
//		this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(this.length, IntConstant.ZERO));
////		this.vm.getSolverManager().addConstraint(CollectionSize.newInstance(this, this.length));
//	}
//	
//	
//	public IteratorVariable iterator() {
//		if(this.iterator == null)  {
//			this.iterator = new IteratorVariable(this);
//		}
//		return this.iterator;
//	}
//	
//	public NumericVariable size() {
//		return this.length;
//	}
//	
//	@Override
//	public boolean isConstant() {
//		return this.length != null && this.length.isConstant() && this.elements != null;
//	}
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
//		
//	}
//
//	@Override
//	public Expression insert(Solution solution,	boolean produceNumericSolution) {
//		System.out.println("************* WHAT TO DO HERE?");
//		return null;
//	}
//
//	@Override
//	public Expression insertAssignment(Assignment assignment) {
//		System.out.println("************* WHAT TO DO HERE?");
//		return null;
//	}
//
//	@Override
//	public boolean isBoolean() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//
//
//	@Override
//	public String toString(boolean useInternalVariableNames) {
//		return this.name;
//	}
//	
//	@Override
//	public String toString() {
//		return this.name;
//	}
//
//	@Override
//	public byte getType() {
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
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getInternalName() {
//		return null;
//	}
//
//	@Override
//	public String getName() {
//		return this.name;
//	}
//
//	@Override
//	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
//		return null;
//	}
//
//	@Override
//	public void writeToLog(PrintStream logStream) {
//		
//	}
//
//	@Override
//	public boolean isInternalVariable() {
//		return false;
//	}
//}
