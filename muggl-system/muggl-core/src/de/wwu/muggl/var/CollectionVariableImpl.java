//package de.wwu.muggl.var;
//
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Set;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.expressions.CollectionSize;
//import de.wwu.muggl.solvers.expressions.CollectionVariable;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
//import de.wwu.muggl.solvers.expressions.IntConstant;
//import de.wwu.muggl.solvers.expressions.Modulo;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.Quotient;
//import de.wwu.muggl.solvers.expressions.Term;
//import de.wwu.muggl.solvers.expressions.TypeCast;
//import de.wwu.muggl.solvers.expressions.TypeCheckException;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//import de.wwu.muggl.solvers.solver.constraints.Polynomial;
//import de.wwu.muggl.solvers.solver.tools.Substitution;
//import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;
//import de.wwu.muggl.vm.VirtualMachine;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class CollectionVariableImpl extends CollectionVariable {
//	
////	SymbolicVirtualMachine vm;
//	
//	protected Collection<Objectref> elements;
//	
//	protected IteratorVariable iterator; 
//	
////	public CollectionVariableImpl(String name, String collectionType, SymbolicVirtualMachine vm) {
//	public CollectionVariableImpl(String name, String collectionType) {
//		super(name, collectionType);
////		this.vm = vm;
//		this.elements = new ArrayList<Objectref>();
//		
//		// create iterator variable of _this_ collection
//		this.iterator = new IteratorVariable(this);
//		
//		// size must be at least zero
////		this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(this.length, IntConstant.ZERO));
////		this.vm.getSolverManager().addConstraint(CollectionSize.newInstance(this, this.length));
//	}
//
//	public IntConstant add(Objectref object) {
//		this.elements.add(object);
//		return IntConstant.ONE;
//	}
//	
//	public NumericVariable size() {
//		return this.length;
//	}
//	
//	public IteratorVariable iterator() {
//		return this.iterator;
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
//	@Override
//	public void checkTypes() throws TypeCheckException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String toString() {
//		return this.name;
//	}
//	
//	@Override
//	public String toString(boolean useInternalVariableNames) {
//		return this.name;
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
//	public Term clearMultiFractions(Set<Term> denominators) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Term insertAssignment(Assignment assignment) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Term insert(Solution solution, boolean produceNumericSolution) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Term substitute(Term a, Term b) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Polynomial toPolynomial() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected boolean containsAsDenominator(Term t) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	protected Substitution findSubstitution(SubstitutionTable subTable) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Set<Term> getDenominators() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Modulo getFirstModulo() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Quotient getFirstNonintegerQuotient() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Quotient getFirstQuotient() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected TypeCast getFirstTypeCast(boolean onlyNarrowing) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Modulo getInmostModulo() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Quotient getInmostQuotient() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Term multiply(Term factor) {
//		// TODO Auto-generated method stub
//		return null;
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
////	
////	
////	public NumericVariable size() {
////		return this.length;
////	}
////	
////	@Override
////	public boolean isConstant() {
////		return this.length != null && this.length.isConstant() && this.elements != null;
////	}
////	
////	
////	
////	
////	
////	
////	
////
////	@Override
////	public void checkTypes() throws TypeCheckException {
////		
////	}
////
////
////
////	@Override
////	public boolean isBoolean() {
////		// TODO Auto-generated method stub
////		return false;
////	}
////
////
////
////	@Override
////	public String toString(boolean useInternalVariableNames) {
////		return this.name;
////	}
////	
////	@Override
////	public String toString() {
////		return this.name;
////	}
////
////	@Override
////	public byte getType() {
////		return -1;
////	}
////
////	@Override
////	public String toTexString(boolean useInternalVariableNames) {
////		return null;
////	}
////
////	@Override
////	public String toHaskellString() {
////		// TODO Auto-generated method stub
////		return null;
////	}
////
////	@Override
////	public String getInternalName() {
////		return null;
////	}
////
////	@Override
////	public String getName() {
////		return this.name;
////	}
////
////	@Override
////	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
////		return null;
////	}
////
////	@Override
////	public void writeToLog(PrintStream logStream) {
////		
////	}
////
////	@Override
////	public boolean isInternalVariable() {
////		return false;
////	}
//}
