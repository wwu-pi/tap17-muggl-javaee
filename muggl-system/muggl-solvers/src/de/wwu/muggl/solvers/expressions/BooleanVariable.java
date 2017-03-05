package de.wwu.muggl.solvers.expressions;

import java.io.PrintStream;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSystem;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;


/**
 * @author Christoph Lembeck
 */
public class BooleanVariable extends SingleConstraintExpression implements Variable, SingleConstraint, Comparable<Variable>{

    protected static int internalIDcounter = 0;

    protected int internalID;

    /**
     * Stores the name of the boolean variable.
     */
    protected String name;

    /**
     * Creates a new BooleanVariable object.
     * @param name the name of the new Variable.
     */
    public BooleanVariable(String name) {
	this.name = name;
	internalID = internalIDcounter++;
    }

    /**
     * Does nothing because the type of boolean variables is immutable and hence
     * always valid.
     */
    @Override
    public void checkTypes(){/* nothing to do here */}

    /**
     * Throws a RuntimeException because a boolean variable may not be
     * added to a set of numeric variables.
     * @param set the set, the numeric variables should be added to.
     * @throws RuntimeException because a boolean variable may not be
     * added to a set of numeric variables.
     */
    @Override
    public void collectNumericVariables(Set<NumericVariable> set){
	throw new RuntimeException("A boolean variable should be added to a set of numeric variables!");
    }

    /**
     * Adds this variables to the passed set.
     * @param set the set, this variable should be added to.
     */
    @Override
    public void collectVariables(Set<Variable> set){
	set.add(this);
    }

    /**
     * Compares this variable lexicographically to the passed variable.
     * @param other the other variable this one should be compared to.
     * @return a negative number if this variable is lexicographically smaller
     * than the passed variable, zero if both variables are lexicographically
     * equal, or a positive value if this variable is lexicographically greater
     * than the other variable
     */
    @Override
    public int compareTo(Variable other){
	if (other instanceof BooleanVariable){
	    BooleanVariable bv = (BooleanVariable)other;
	    int nameComp = name.compareTo(bv.name);
	    if (nameComp == 0)
		return bv.internalID - internalID;
	    else
		return nameComp;
	} else {
	    if (other instanceof NumericVariable)
		return -1;
	    else
		throw new ClassCastException();
	}
    }

    /**
     * Checks whether the passed variable <i>var</i> is equal to this object or
     * not.
     * @param var the variable that this boolean variable should be compared to.
     * @return <i>true</i> if the passed variable is equal to this object,
     * <i>false</i> otherwise.
     */
    @Override
    public boolean containsVariable(Variable var) {
	return equals(var);
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
	return this;
    }

    /**
     * Checks whether this variable is equal to the passed argument.
     * @param obj the other object this variable should be compared with.
     * @return <i>true</i> if the other object is also a boolean variable and
     * has the same name as this variable, <i>false</i> otherwise.
     */
    @Override
    public boolean equals(Object obj){
	return obj == this;
    }

    @Override
    public String getInternalName(){
	return "z" + internalID;
    }

    /**
     * Returns the name of the variable.
     * @return the name of the variable.
     */
    @Override
    public String getName(){
	return name;
    }

    /**
     * Returns the contained system of constraints with the specified index.
     * Here the index should always be 0.
     * @param idx <i>should be 0 here</i>.
     * @return the only contained system.
     */
    @Override
    public ConstraintSystem getSystem(int idx) {
	if (idx != 0)
	    throw new IllegalArgumentException("Not so many systems (" + idx + ")");
	return ConstraintSystem.getConstraintSystem(this);
    }

    /**
     * Returns the number of disjunctive combined constraints in this object. Here
     * the number will be 1, because we only have one single constant.
     * @return <i>1</i>.
     */
    @Override
    public int getSystemCount() {
	return 1;
    }

    /**
     * Returns the type of the actual expression. Here it will be
     * <i>Expression.BOOLEAN</i>.
     * @return <i>Expression.BOOLEAN</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    @Override
    public byte getType(){
	return Expression.BOOLEAN;
    }

    /**
     * Calculates a hash code value for the constant.
     * @return the hash code value of this object.
     */
    @Override
    public int hashCode(){
	return name.hashCode();
    }

    /**
     * If the passed assignment does contain this variable, the assigned value
     * of this variable will be returned. otherwise the variable itself will
     * be returned.
     * @param assignment the assignment that should be used to substitute
     * variables by values.
     * @return the value of this variable or the variable itself, if it is not
     * equal to the variable inside the assignment.
     */
    @Override
    public SingleConstraintExpression insertAssignment(Assignment assignment) {
	if (assignment.getVariable().equals(this))
	    return (BooleanConstant)assignment.getValue();
	else
	    return this;
    }

    @Override
    public SingleConstraintExpression insert(Assignment assignment) {
	return insertAssignment(assignment);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	Constant c = solution.getValue(this);
	if (c != null){
	    if (produceNumericSolution)
		return BooleanConstant.FALSE;
	    else
		return (BooleanConstant)c;
	} else
	    return this;
    }

    /**
     * Checks whether the expression is a constant or a combined term containing
     * unbound variables.
     * @return <code>false</code>, as a variable is not constant.
     */
    @Override
    public boolean isConstant(){
	return false;
    }

    /**
     * Returns <i>false</i>, because this is just a boolean variable.
     * @return <i>false</i>
     */
    @Override
    public boolean isEquation() {
	return false;
    }

    @Override
    public boolean isInternalVariable(){
	return false;
    }

    /**
     * Returns <i>false</i>, because this is just a boolean variable.
     * @return <i>false</i>
     */
    @Override
    public boolean isLinear(){
	return false;
    }

    /**
     * Returns <i>false</i>, because this is just a boolean variable.
     * @return <i>false</i>
     */
    @Override
    public boolean isStrictInequation() {
	return false;
    }

    /**
     * Returns <i>false</i>, because this is just a boolean variable.
     * @return <i>false</i>
     */
    @Override
    public boolean isWeakInequation() {
	return false;
    }

    /**
     * Returns the boolean negation of this variable.
     * @return the boolean negation of this variable.
     */
    @Override
    public ConstraintExpression negate() {
	return Not.newInstance(this);
    }

    @Override
    public String toHaskellString(){
	return "(BoolVar (BooleanVariable \"" + name + "\"))";
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
	    return name;
    }

    /**
     * Returns a representation of this variable as a latex expression.
     * @return a representation of this variable as a latex expression.
     */
    @Override
    public String toTexString(){
	return toTexString(false, false);
    }

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

    @Override
    public boolean validateSolution(Solution solution) throws IncompleteSolutionException{
	BooleanConstant value = solution.getBooleanValue(this);
	if (value == null)
	    throw new IncompleteSolutionException("no binding for variable " + this);
	return value.getValue();
    }

    /**
     * Writes this variable as an ComposedConstraint into the passed log stream.
     * @param logStream the stream the logging informations should be written
     * into.
     * @see #writeToLog(PrintStream)
     */
    @Override
    public void writeCCToLog(PrintStream logStream){
	logStream.print("<composedconstraint>");
	logStream.print(toString());
	logStream.println("</composedconstraint>");
    }

    /**
     * Writes this variable as variable into the passed log stream.
     * @param logStream the stream the logging informations should be written
     * into.
     * @see #writeCCToLog(PrintStream)
     */
    @Override
    public void writeToLog(PrintStream logStream){
	logStream.print("<variable name=\"" +name+  "\" type=\"boolean\" />");
    }

    @Override
    public ComposedConstraint toDNF() {
	return this;
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return toTexString(false, useInternalVariableNames);
    }

//	@Override
//	public Variable getClone() {
//		return new BooleanVariable(this.name);
//	}

}
