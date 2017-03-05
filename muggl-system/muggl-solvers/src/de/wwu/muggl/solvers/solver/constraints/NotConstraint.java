package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.BooleanVariable;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * @author Christoph Lembeck
 */
public class NotConstraint implements SingleConstraint{

    public static ComposedConstraint newInstance(ComposedConstraint arg){
	if (arg instanceof BooleanVariable)
	    return new NotConstraint( (BooleanVariable) arg );
	else
	    throw new InternalError("Only variables should be negated");
	//return arg.negate();
    }

    protected BooleanVariable arg;

    private NotConstraint(BooleanVariable arg) {
	this.arg = arg;
    }

    @Override
    public void collectNumericVariables(Set<NumericVariable> set) {
	arg.collectNumericVariables(set);
    }

    @Override
    public void collectVariables(Set<Variable> set) {
	arg.collectVariables(set);
    }

    @Override
    public boolean containsVariable(Variable var) {
	return arg.containsVariable(var);
    }

    @Override
    public boolean equals(Object o){
	return ((o instanceof NotConstraint) && (((NotConstraint)o).arg.equals(arg)));
    }
    
    @Override
    public ConstraintSystem getSystem(int idx) {
	if (idx != 0)
	    throw new IllegalArgumentException("Not so many systems");
	return ConstraintSystem.getConstraintSystem(this);
    }

    @Override
    public int getSystemCount() {
	return arg.getSystemCount();
    }

    public BooleanVariable getVariable(){
	return arg;
    }

    @Override
    public int hashCode(){
	return -arg.hashCode();
    }

    @Override
    public SingleConstraint insert(Assignment assignment) {
	if (assignment.getVariable().equals(arg))
	    return ((BooleanConstant)assignment.getValue()).negate();
	else
	    return this;
    }

    /**
     * Returns <i>true</i> since the negation of a boolean variable is of type
     * boolean too.
     * @return <i>true</i>.
     */
    @Override
    public boolean isBoolean(){
	return true;
    }

    /**
     * Returns <i>false</i> because negated boolean variables are no equations.
     * @return <i>false</i>.
     */
    @Override
    public boolean isEquation() {
	return false;
    }

    /**
     * Returns <i>false</i> because a boolean variable is not linear.
     * @return <i>false</i>.
     */
    @Override
    public boolean isLinear(){
	return false;
    }

    /**
     * Returns <i>false</i> because negated boolean variables are no inequations.
     * @return <i>false</i>.
     */
    @Override
    public boolean isStrictInequation() {
	return false;
    }

    /**
     * Returns <i>false</i> because negated boolean variables are no inequations.
     * @return <i>false</i>.
     */
    @Override
    public boolean isWeakInequation() {
	return false;
    }

    public ComposedConstraint negate() {
	return arg;
    }

    @Override
    public ComposedConstraint toDNF() {
	return this;
    }

    @Override
    public String toString(){
	return "!(" + arg.toString() + ")";
    }

    @Override
    public String toTexString(){
	return toTexString(false, false);
    }

    @Override
    public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames){
	return "\\neg(" + arg.toTexString(inArrayEnvironment, useInternalVariableNames) + ")";
    }

    @Override
    public boolean validateSolution(Solution solution){
	return !solution.getBooleanValue(arg).getValue();
    }

    @Override
    public void writeCCToLog(PrintStream logStream) {
	logStream.print("<composedconstraint>");
	logStream.print(toString());
	logStream.println("</composedconstraint>");
    }
}
