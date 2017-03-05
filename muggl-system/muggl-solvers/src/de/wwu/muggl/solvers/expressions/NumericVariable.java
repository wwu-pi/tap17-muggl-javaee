package de.wwu.muggl.solvers.expressions;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeMap;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Monomial;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.numbers.Fraction;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents a single numeric variable used inside the virtual machine.
 * @author Christoph Lembeck
 */
public class NumericVariable extends Term implements Variable, Comparable<Variable>{

    /**
     * Internal counter for the numbering of internally generated variables.
     */
    protected static int counter = 0;

    protected static int internalIDcounter;

    protected static String internalVariablesNamePostfix;
    protected static String internalVariablesNamePrefix;
    
    // for boolean values
    protected boolean isBoolean;

    static {
	TesttoolConfig options = TesttoolConfig.getInstance();
	internalVariablesNamePrefix = options.getInternalVariablesNamePrefix();
	internalVariablesNamePostfix = options.getInternalVariablesNamePostfix();
    }

    /**
     * Creates an internal variable having the specified type. Internal variables
     * may only be created by the SolverManager or any underlying solver.
     * @param type the type the new internal variable should have.
     * @return the new internal variable.
     */
    public static NumericVariable createInternalVariable(byte type){
	return new NumericVariable(internalVariablesNamePrefix + (counter++) + internalVariablesNamePostfix, type, true);
    }

    protected int internalID;
    
    public int getInternalID(){
	return internalID;
    }

    protected boolean isInternal;

    /**
     * Stores the name of the variable.
     */
    protected String name;

    /**
     * Stores the type of the variable as it is defined in the interface
     * Expression.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    protected byte type;

    /**
     * Creates a new Variable object with the passed name and type.
     * @param name the new name of the variable.
     * @param type the type of the variable as defined in the Expression
     * interface.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    public NumericVariable(String name, byte type){
	this(name, type, false);
    }

    /**
     * Creates a new Variable object with the passed name and type.
     * @param name the new name of the variable.
     * @param type the type of the variable as defined in the Expression
     * interface.
     * @param isInternal indicates whether the new variable should be treated as
     * an internal variable that was generated during the transformation of some
     * constraints or as a really occuring variable out of the java
     * bytecode.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    public NumericVariable(String name, byte type, boolean isInternal) {
	if (type == Expression.BOOLEAN)
	    throw new IllegalArgumentException("Type boolean is not allowed for NumericVariables");
	this.name = name;
	this.type = type;
	this.internalID = internalIDcounter++;
	this.isInternal = isInternal;
    }

    /**
     * Creates a new Variable object with the passed name and type.
     * @param name the new name of the variable.
     * @param type the type of the variable as string.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    public NumericVariable(String name, String type){
	this(name, type, false);
    }

    /**
     * Creates a new Variable object with the passed name and type.
     * @param name the new name of the variable.
     * @param type the type of the variable as string.
     * @param isInternal indicates whether the new variable should be treated as
     * an internal variable that was generated during the transformation of some
     * constraints or as a really occuring variable out of the java
     * bytecode.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    private NumericVariable(String name, String type, boolean isInternal){
	if (type.equalsIgnoreCase("boolean"))
	    throw new IllegalArgumentException("Type boolean is not allowed for NumericVariables");
	this.name = name;
	this.isInternal = isInternal;
	this.internalID = internalIDcounter++;
	if (type.equalsIgnoreCase("byte"))
	    this.type = Expression.BYTE;
	if (type.equalsIgnoreCase("short"))
	    this.type = Expression.SHORT;
	if (type.equalsIgnoreCase("char"))
	    this.type = Expression.CHAR;
	if (type.equalsIgnoreCase("int"))
	    this.type = Expression.INT;
	if (type.equalsIgnoreCase("long"))
	    this.type = Expression.LONG;
	if (type.equalsIgnoreCase("float"))
	    this.type = Expression.FLOAT;
	if (type.equalsIgnoreCase("double"))
	    this.type = Expression.DOUBLE;
	if (this.type == 0)
	    throw new IllegalArgumentException("unknown type " + type);
    }

    /**
     * Verifies the internal type of the variable and throws an TypeCheckException
     * if the variable has no numeric type.
     * @throws TypeCheckException if the variable has no numeric type.
     */
    @Override
    public void checkTypes() throws TypeCheckException{
	if (!isNumericType(getType()))
	    throw new TypeCheckException(toString() + " has not a numeric type");
    }

    /**
     * Compares this variable lexicographically to the passed variable.
     * @param other the other variable this one should be compared to.
     * @return a negative number if this variable is lexicographically smaller
     * than the passed variable, zero if both variables are lexicographically
     * equal, or a positive value if this variable is lexicographically greater
     * than the other variable
     */
    public int compareTo(Variable other) {
	if (other instanceof NumericVariable){
	    NumericVariable nv = (NumericVariable)other;
	    int nameComp = name.compareTo(nv.name);
	    if (nameComp == 0)
		return nv.internalID - internalID;
	    else
		return nameComp;
	} else {
	    if (other instanceof BooleanVariable || other.getClass().getName().equals("de.wwu.muggl.vm.var.ReferenceVariable"))
		return -1;
	    else
		throw new ClassCastException();
	}
    }

    /**
     * Returns <i>false</i> hence variables do not contain any other expressions.
     * @param t the term that should be searched for.
     * @return <i>false</i>.
     */
    @Override
    protected boolean containsAsDenominator(Term t) {
	return false;
    }

    /**
     * Checks wether this variable is equal to the passed object.
     * @param obj the other object this variable should be compared to.
     * @return <i>true</i> if the other object is also a numeric variable and
     * its name and type are equal to the name and type of this variable,
     * <i>false</i> otherwise.
     */
    @Override
    public boolean equals(Object obj){
	return obj == this;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	return result;
    }

    /**
     * Returns <i>null<i> because variables do not contain any other expressions.
     * @return <i>false</i>.
     */
    @Override
    protected Set<Term> getDenominators() {
	return null;
    }

    /**
     * Returns <i>null</i>, because variables never contain any modulo
     * expressions.
     * @return <i>null</i>.
     */
    @Override
    protected Modulo getFirstModulo() {
	return null;
    }

    /**
     * Returns <i>null</i>, because variables never contain any quotients.
     * @return <i>null</i>.
     */
    @Override
    protected Quotient getFirstQuotient() {
	return null;
    }

    /**
     * Returns null as variables do not contain any other expressions as subnodes.
     * @param onlyNarrowing <i>unused</i>.
     * @return <i>null</i>.
     */
    @Override
    protected TypeCast getFirstTypeCast(boolean onlyNarrowing) {
	return null;
    }

    /**
     * @return null because variables do not contain modulos
     */
    @Override
    protected Modulo getInmostModulo(){
	return null;
    }

    /**
     * @return null because variables do not contain quotients
     */
    @Override
    protected Quotient getInmostQuotient() {
	return null;
    }

    @Override
    public String getInternalName(){
	switch (type){
	case Expression.BYTE: return "b" + internalID;
	case Expression.CHAR: return "c" + internalID;
	case Expression.SHORT: return "s" + internalID;
	case Expression.INT: return "i" + internalID;
	case Expression.LONG: return "l" + internalID;
	case Expression.FLOAT: return "f" + internalID;
	case Expression.DOUBLE: return "d" + internalID;
	default: return "v" + internalID;
	}
    }

    @Override
    public String getName(){
	return name;
    }

    @Override
    public byte getType(){
	return type;
    }

    @Override
    public int hashCode(){
	return name.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	if (assignment.getVariable().equals(this))
	    return (NumericConstant)assignment.getValue();
	else
	    return this;
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Constant c = solution.getValue(this);
	if (c == null){
	    if (produceNumericSolution)
		return NumericConstant.getZero(this.type);
	    else
		return this;
	} else
	    return (NumericConstant)c;
    }

    /**
     * Returns <code>false</code> because is definitely not a constant.
     * @return <code>false</code>.
     */
    @Override
    public boolean isConstant(){
	return false;
    }

    /**
     * Checks whether this variable has an integer type or not.
     * @return <i>true</i> if the variable is integer, <i>false</i> if the
     * variable is noniteger.
     */
    public boolean isInteger(){
	return isIntegerType(getType());
    }

    @Override
    public boolean isInternalVariable(){
	return isInternal;
    }

    @Override
    protected Term multiply(Term factor) {
	return Product.newInstance(factor, this);
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return this;
    }

    @Override
    public String toHaskellString(){
	String haskellType = null;
	switch (type){
	case Expression.BOOLEAN: haskellType = "TypeBoolean"; break;
	case Expression.BYTE: haskellType = "TypeByte"; break;
	case Expression.SHORT: haskellType = "TypeShort"; break;
	case Expression.CHAR: haskellType = "TypeChar"; break;
	case Expression.INT: haskellType = "TypeInt"; break;
	case Expression.FLOAT: haskellType = "TypeFloat"; break;
	case Expression.LONG: haskellType = "TypeLong"; break;
	case Expression.DOUBLE: haskellType = "TypeDouble"; break;
	}
	return "(Variable (NumericVariable " + haskellType + " \"" + name + "\"))";
    }

    @Override
    public Polynomial toPolynomial() {
	// CLTODO NumberWrapper anpassen
	NumberFactory numberFactory = new Fraction();
	
	Monomial monomial = new Monomial(this, 1);
	
	TreeMap<Monomial, NumberWrapper> polynomialMap = new TreeMap<Monomial, NumberWrapper>();
	polynomialMap.put(monomial, numberFactory.getOne());
	
	Polynomial polynomial = new Polynomial(polynomialMap, numberFactory.getZero());
	
	return polynomial;
    }

    /**
     * Returns a string representation of the variable.
     * @return the string representation of the variable.
     */
    @Override
    public String toString(){
	return name;
    }

    /**
     * Returns a string representation of this variable.
     * @param useInternalVariableNames if set to true the string representation
     * will be build using the internal names for each variable. Otherwise the
     * originally given names of the variables will be used.
     * @return the string representation of the variable.
     */
    @Override
    public String toString(boolean useInternalVariableNames){
	if (useInternalVariableNames)
	    return getInternalName();
	else
	    return toString();
    }

    /**
     * Returns a representation of this variable as a latex expression.
     * @return a representation of this variable as a latex expression.
     */
    @Override
    public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames){
	String vName = useInternalVariableNames?getInternalName():getName();
	String index = "";
	while (Character.isDigit(vName.charAt(vName.length() - 1))){
	    index = vName.charAt(vName.length() - 1) + index;
	    vName = vName.substring(0, vName.length() - 1);
	}
	if (index.length() > 0)
	    return vName + "_{" + index + "}";
	else
	    return name;
    }

    /**
     * Writes this variable into the passed log stream.
     * @param logStream the stream logging informations about this variable should
     * be written into.
     */
    @Override
    public void writeToLog(PrintStream logStream) {
	logStream.print("<variable name=\"" +name+  "\" type=\"" + getTypeName(getType()) + "\" />");
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return toTexString(false, useInternalVariableNames);
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	return this;
    }

    @Override
    protected Quotient getFirstNonintegerQuotient() {
	return null;
    }

	public void setIsBoolean(boolean isBoolean) {
		this.isBoolean = isBoolean;
	}
	
	public boolean isBoolean() {
		return this.isBoolean;
	}
    
//    @Override
//    public Variable getClone() {
//    	return new NumericVariable(this.name, this.type, this.isInternal);
//    }
}
