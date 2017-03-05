package de.wwu.muggl.solvers.expressions;

import java.io.PrintStream;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSystem;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;


/**
 * @author Christoph Lembeck
 */
public class BooleanConstant extends SingleConstraintExpression implements SingleConstraint, Constant{

    /**
     * Represents the boolean constant false.
     */
    public final static BooleanConstant FALSE = new BooleanConstant(false);

    /**
     * Represents the boolean constant true.
     */
    public final static BooleanConstant TRUE = new BooleanConstant(true);

    /**
     * Returns an instance of BooleanConstant that represents the passed boolean
     * value.
     * @param value the value the constant should represent.
     * @return the BooleanConstant object representing the passed value.
     */
    public static BooleanConstant getInstance(boolean value){
	if (value)
	    return TRUE;
	else
	    return FALSE;
    }

    /**
     * Stores whether this constant is true of false.
     */
    protected boolean value;

    /**
     * Creates a new BooleanConstant object to represent either the constant
     * true or false.
     * @param value specifies the internal state of this boolean constant.
     */
    private BooleanConstant(boolean value){
	this.value = value;
    }

    /**
     * Does nothing here, because the type of a boolean constant is always valid
     * and the constant does not contain any subnodes.
     */
    @Override
    public void checkTypes(){/* nothng to do here */}

    /**
     * Does nothing thus a constant does not contain any variables.
     * @param set unused.
     */
    @Override
    public void collectNumericVariables(Set<NumericVariable> set) {
	// no variable contained
    }

    /**
     * Does not really do anything hence constants do not contain any variables.
     * @param set the set, the contained variables should be added to.
     */
    @Override
    public void collectVariables(Set<Variable> set) {/* no variable inside */}

    /**
     * Returns <i>false</i>, because Constants have nothing to do with variables.
     * @param var the variable that should be searched for.
     * @return <i>false</i>, because Constants have nothing to do with variables.
     */
    @Override
    public boolean containsVariable(Variable var) {
	return false;
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
	return this;
    }

    @Override
    public boolean equals(Object obj){
	return (obj == this) || ((obj instanceof BooleanConstant) && (((BooleanConstant)obj).value == value));
    }
    
    public ConstraintExpression getOriginalExpression(){
	return this;
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
	    throw new IllegalArgumentException("Not so many systems");
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
     * Returns the boolean value of this constant.
     * @return the boolean value of this constant.
     */
    public boolean getValue(){
	return value;
    }

    /**
     * Calculates a hash code value for the constant.
     * @return the hash code value of this object.
     */
    @Override
    public int hashCode(){
	if (value)
	    return 1231;
	else
	    return 1237;
    }

    /**
     * Returns this as constants do not contain variables.
     * @param assignment unused.
     * @return this.
     */
    @Override
    public BooleanConstant insertAssignment(Assignment assignment){
	return this;
    }

    @Override
    public BooleanConstant insert(Assignment assignment){
	return insertAssignment(assignment);
    }

    /**
     * Returns this constant.
     * @param solution unused.
     * @param produceNumericSolution unused.
     * @return this object.
     */
    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	return this;
    }

    /**
     * Returns <code>true</code> because this is definitely a constant.
     * @return <code>true</code>.
     */
    @Override
    public boolean isConstant(){
	return true;
    }

    /**
     * Returns <i>false</i>, because this is just a constant.
     * @return <i>false</i>
     */
    @Override
    public boolean isEquation() {
	return false;
    }

    /**
     * Returns <i>false</i>, because this is just a boolean constant.
     * @return <i>false</i>
     */
    @Override
    public boolean isLinear(){
	return false;
    }

    /**
     * Returns <i>false</i>, because this is just a constant.
     * @return <i>false</i>
     */
    @Override
    public boolean isStrictInequation() {
	return false;
    }

    /**
     * Returns <i>false</i>, because this is just a constant.
     * @return <i>false</i>
     */
    @Override
    public boolean isWeakInequation() {
	return false;
    }

    /**
     * Returns the opposite of this boolean constant.
     * @return <i>BooleanConstant.TRUE</i> if this constant represents the boolean
     * constant false, <i>BooleanConstant.FALSE</i> otherwise.
     */
    @Override
    public BooleanConstant negate() {
	if (value)
	    return FALSE;
	else
	    return TRUE;
    }

    @Override
    public ComposedConstraint toDNF() {
	return this;
    }

    @Override
    public String toHaskellString(){
	return "(BoolConst " + (value?"CTrue":"CFalse") + ")";
    }

    /**
     * Returns a string representation of the constant.
     * @param useInternalVariableNames if set to true the string representation
     * will be build using the internal names for each variable. Otherwise the
     * originally given names of the variables will be used.
     * @return the string representation of the constant.
     */
    @Override
    public String toString(boolean useInternalVariableNames){
	if (value)
	    return "true";
	else
	    return "false";
    }

    /**
     * Returns the representation of this constant as latex expression.
     * @return the representation of this constant as latex expression.
     */
    @Override
    public String toTexString(){
	return toTexString(false, false);
    }

    /**
     * Returns the representation of this constant as latex expression.
     * @param inArrayEnvironment unused.
     * @return the representation of this constant as latex expression.
     */
    @Override
    public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames){
	return toString();
    }

    /**
     * Returns the inner value of this constant because it is totally independent
     * from any assignements of variables.
     * @param solution <i>unused</i>.
     * @return the boolean value of this constant.
     */
    @Override
    public boolean validateSolution(Solution solution){
	return value;
    }

    /**
     * Writes this constant as a ConposedConstraint to the log stream.
     * @param logStream the stream the logging information should be written into.
     */
    @Override
    public void writeCCToLog(PrintStream logStream){
	logStream.print("<composedconstraint>");
	logStream.print(toString());
	logStream.println("</composedconstraint>");
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return toTexString(false, useInternalVariableNames);
    }
}
