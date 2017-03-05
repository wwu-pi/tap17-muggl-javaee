//package de.wwu.muggl.vm.var.gen.types;
//
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
//import de.wwu.muggl.solvers.expressions.NumericConstant;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.TypeCheckException;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
//import de.wwu.muggl.vm.classfile.ClassFile;
//import de.wwu.muggl.vm.classfile.ClassFileException;
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//import de.wwu.muggl.vm.initialization.InitializedClass;
//import de.wwu.muggl.vm.initialization.Objectref;
//import de.wwu.muggl.vm.initialization.ReferenceValue;
//import de.wwu.muggl.vm.var.EntityObjectref;
//import de.wwu.muggl.vm.var.sym.ISymbolicList;
//
//public class EntityListObjectref extends Objectref implements ISymbolicList, Variable, ReferenceValue, Comparable {
//
//	protected List<Objectref> list; // the list
//	protected NumericVariable symbolicLength; // the symbolic length
//	protected String collectionType; // the collection type
//	
//	protected String name; // the name of this variable
//	protected EntityObjectref owner; // the entity object reference that owns this list
//	protected JPAVirtualMachine vm; // the current virtual machine
//	
//	public EntityListObjectref(EntityObjectref owner, String name, String collectionType, JPAVirtualMachine vm) throws SymbolicObjectGenerationException {
//		super(getArrayListInitClass(vm), false);
//		this.name = name;
//		this.collectionType = collectionType;
//		this.owner = owner;
//		this.vm = vm;
//		this.list = new ArrayList<>();
//		this.symbolicLength = new NumericVariable(name+".length", Expression.INT);
//		vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(symbolicLength, NumericConstant.getZero(Expression.INT)));
//		Field lengthField = this.getInitializedClass().getClassFile().getFieldByName("size");
//		this.putField(lengthField, symbolicLength);	
//	}
//	
//	private static InitializedClass getArrayListInitClass(JPAVirtualMachine vm) throws SymbolicObjectGenerationException {
//		ClassFile classFile = null;
//		try {
//			classFile = vm.getClassLoader().getClassAsClassFile("java.util.ArrayList");
//		} catch (ClassFileException e) {
//			throw new SymbolicObjectGenerationException("Could not get reference value for field with type: java.util.ArrayList");
//		}
//		return classFile.getInitializedClass();
//	}
//	
//	@Override
//	public List<?> getResultList() {
//		return this.getResultList();
//	}
//
//	@Override
//	public NumericVariable getSymbolicLength() {
//		
//	}
//
//	@Override
//	public JPAVirtualMachine getVM() {
//		
//	}
//
//	@Override
//	public void addElement(Objectref element) {
//		
//	}
//
//	@Override
//	public boolean removeElement(Objectref element) {
//
//	}
//
//	@Override
//	public String getCollectionType() {
//	
//	}
//
//	@Override
//	public Objectref generateNewElement() {
//		System.out.println("**auch auf required dann  erhoehen die length der liste..");
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
//	
//	
//	
//	
//	@Override
//	public void checkTypes() throws TypeCheckException {	
//	}
//
//	@Override
//	public Expression insert(Solution solution,	boolean produceNumericSolution) {
//		return null;
//	}
//
//	@Override
//	public Expression insertAssignment(Assignment assignment) {
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
//		return null;
//	}
//
//	@Override
//	public byte getType() {
//		return 0;
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
//	public int compareTo(Object o) {
//		return 0;
//	}
//
//	@Override
//	public String getInternalName() {
//		return null;
//	}
//
//	@Override
//	public String toTexString(boolean inArrayEnvironment,boolean useInternalVariableNames) {
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
//}
