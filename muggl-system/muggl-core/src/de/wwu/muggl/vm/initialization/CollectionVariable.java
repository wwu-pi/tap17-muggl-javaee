//package de.wwu.muggl.vm.initialization;
//
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.TypeCheckException;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//import de.wwu.muggl.vm.VirtualMachine;
//import de.wwu.muggl.vm.classfile.ClassFileException;
//
//public class CollectionVariable extends Objectref implements Variable, Collection {
//
//	/**
//     * Internal counter for the numbering of internally generated variables.
//     */
//	protected static int internalIDcounter;
//	
//	// the internal id of this variable
//	protected int internalID;
//	
//	// name of this variable 
//	protected String name;
//	
//	// the expected length of this collection
//	protected NumericVariable length;
//		
//	protected Collection<Object> collection; 
//	
//	public CollectionVariable(String name, VirtualMachine vm) throws ClassFileException {
//		super(vm.getClassLoader().getClassAsClassFile("java.util.ArrayList").getInitializedClass(), false);
//		this.name = name;
//		this.internalID = internalIDcounter++;
//		this.collection = new ArrayList<Object>();
//		
//	}
//	
//	public String getName() {
//		return this.name;
//	}
//	
//	public NumericVariable getLength() {
//		return this.length;
//	}
//	
//	public void setLength(NumericVariable length) {
//		this.length = length;
//	}
//
//	@Override
//	public void checkTypes() throws TypeCheckException {
//		// TODO check if every element in collection is of the same type
//	}	
//
//	@Override
//	public Expression insert(Solution solution,	boolean produceNumericSolution) {
//		// TODO Auto-generated method stub
//		System.out.println("*** INSERT SOLUTION FOR COLLECTION *** HOW???");
//		return null;
//	}
//	
//	@Override
//	public Expression insertAssignment(Assignment assignment) {
//		// TODO Auto-generated method stub
//		System.out.println("*** INSERT ASSIGNMENT FOR COLLECTION *** HOW???");
//		return null;
//	}
//
//	@Override
//	public boolean isBoolean() {
//		return false;
//	}
//
//	@Override
//	public boolean isConstant() {
//		return false;
//	}
//
//	@Override
//	public String toString(boolean useInternalVariableNames) {
//		return this.name;
//	}
//	
//	@Override
//	public String toString() {
//		return name;
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
//	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
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
//		return "["+internalID;
//	}
//	
//	@Override
//	public void writeToLog(PrintStream logStream) {
//		logStream.print("<variable name=\"" +name+  "\" type=\"collection\" />");
//	}
//
//	@Override
//	public boolean isInternalVariable() {
//		return false;
//	}
//
//
//	@Override
//	public boolean isArray() {
//		return true;
//	}
//
//
//	@Override
//	public long getInstantiationNumber() {
//		return this.internalID;
//	}
//
//	
//	@Override
//	public boolean add(Object e) {
//		return this.collection.add(e);
//	}
//
//	@Override
//	public boolean addAll(Collection c) {
//		return this.collection.addAll(c);
//	}
//
//	@Override
//	public void clear() {
//		this.collection.clear();
//	}
//
//	@Override
//	public boolean contains(Object o) {
//		return this.collection.contains(o);
//	}
//
//	@Override
//	public boolean containsAll(Collection c) {
//		return this.collection.containsAll(c);
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return this.collection.isEmpty();
//	}
//
//	@Override
//	public Iterator iterator() {
//		System.out.println("*** BIG TODO !!! HIER JETZT MAL NEN SYMBOLISCHEN ITERATOR ****");
//		return this.collection.iterator();
//	}
//
//	@Override
//	public boolean remove(Object o) {
//		return this.collection.remove(o);
//	}
//
//	@Override
//	public boolean removeAll(Collection c) {
//		return this.collection.removeAll(c);
//	}
//
//	@Override
//	public boolean retainAll(Collection c) {
//		return this.collection.retainAll(c);
//	}
//
//	@Override
//	public int size() {
//		return this.collection.size();
//	}
//
//	@Override
//	public Object[] toArray() {
//		return this.collection.toArray();
//	}
//
//	@Override
//	public Object[] toArray(Object[] a) {
//		return this.collection.toArray(a);
//	}
//}
