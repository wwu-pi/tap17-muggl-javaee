//package de.wwu.muggl.var;
//
//import java.io.PrintStream;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.TypeCheckException;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//import de.wwu.muggl.vm.classfile.ClassFile;
//import de.wwu.muggl.vm.classfile.ClassFileException;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class IteratorVariable implements Variable {
//	
//	protected CollectionVariableImpl collection;
//	
//	protected int currentPosition;
//	
//	public IteratorVariable(CollectionVariableImpl collection) {
//		this.collection = collection;
//		this.currentPosition = 0;
//	}
//	
//	public NumericVariable hasNext() {
//		return new NumericVariable(collection.getName()+".hasNext."+currentPosition, Expression.INT);
//	}
//	
//	public Objectref next() {
////		currentPosition++;
////		try {
////			ClassFile classFile = collection.vm.getClassLoader().getClassAsClassFile(collection.getCollectionType());
////			Objectref objectRef = collection.vm.getAnObjectref(classFile);
////			return objectRef;
////		} catch (ClassFileException e) {
////			e.printStackTrace();
////		}
////		currentPosition--; // something went wront -> back to beginning...
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
