package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.tools.StringFormater;


/**
 * @author Christoph
 */
@SuppressWarnings("all")
public class Assignment implements SingleConstraint{

    protected Constant constant;

    protected Variable variable;

    public Assignment(Variable variable, Constant constant){
	this.variable = variable;
	this.constant = constant;
    }

    @Override
    public void collectNumericVariables(Set<NumericVariable> set) {
	if (variable instanceof NumericVariable)
	    set.add((NumericVariable)variable);
	else
	    throw new RuntimeException("A boolean variable should be added to a set of numeric variables!");
    }

    @Override
    public void collectVariables(Set<Variable> set) {
	set.add(variable);
    }

    public boolean containsVariable(Variable var) {
	return var.equals(variable);
    }

    @Override
    public boolean equals(Object obj){
	if (this == obj)
	    return true;
	if (obj instanceof Assignment){
	    Assignment ass = (Assignment)obj;
	    return (ass.variable.equals(variable) && ass.constant.equals(constant));
	} else
	    return false;
    }
    
    @Override
    public ConstraintSystem getSystem(int idx) {
	if (idx != 0)
	    throw new IllegalArgumentException();
	return ConstraintSystem.getConstraintSystem(this);
    }

    @Override
    public int getSystemCount() {
	return 1;
    }

    /**
     * 
     * @return the value of this assignment
     */
    public Constant getValue(){
	return constant;
    }

    /**
     * 
     * @return the variable of this assignment
     */
    public Variable getVariable(){
	return variable;
    }

    @Override
    public int hashCode(){
	return variable.hashCode() + constant.hashCode() - 674;
    }

    @Override
    public SingleConstraint insert(Assignment assignment) {
	if (assignment.getVariable().equals(variable) && !assignment.getValue().equals(constant))
	    return BooleanConstant.FALSE;
	else
	    return this;
    }

    @Override
    public boolean isBoolean() {
	return variable.isBoolean();
    }

    @Override
    public boolean isEquation() {
	return isLinear();
    }

    @Override
    public boolean isLinear() {
	return !variable.isBoolean();
    }

    @Override
    public boolean isStrictInequation() {
	return false;
    }

    @Override
    public boolean isWeakInequation() {
	return false;
    }

    public ComposedConstraint negate() {
	throw new InternalError();
    }

    @Override
    public ComposedConstraint toDNF() {
	return this;
    }

    @Override
    public String toString(){
	return "Assignment: " + variable + " = " + constant;
    }

    @Override
    public String toTexString(){
	return toTexString(false, false);
    }

    @Override
    public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames){
	if (inArrayEnvironment)
	    return variable.toTexString(inArrayEnvironment, useInternalVariableNames) + "&=&" + constant.toTexString();
	else
	    return variable.toTexString(inArrayEnvironment, useInternalVariableNames) + "=" + constant.toTexString();
    }

    @Override
    public boolean validateSolution(Solution solution) throws IncompleteSolutionException {
	Constant value = solution.getValue(variable);
	if (value == null)
	    throw new IncompleteSolutionException("no binding for variable " + variable);
	return (value.equals(this.constant));
    }

    @Override
    public void writeCCToLog(PrintStream logStream) {
	logStream.print("<composedconstraint>");
	logStream.print(StringFormater.xmlEncode(toString()));
	logStream.println("</composedconstraint>");
    }
}
