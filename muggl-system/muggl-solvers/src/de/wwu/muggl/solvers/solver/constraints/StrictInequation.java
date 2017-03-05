package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.muggl.solvers.solver.tools.StringFormater;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class StrictInequation implements NumericConstraint{

    /**
     * Creates a new inequation with the passed polynomial as left hand side of
     * the inequation and zero as the right hand side.
     * @param poly the polynomial that should be lesser than zero.
     * @return the new instance of the constraint.
     */
    public static SingleConstraint newInstance(Polynomial poly){
	if (poly.isConstant()){
	    NumberWrapper c = poly.getConstant();
	    if (c == null)
		return BooleanConstant.FALSE; // 0 < 0 --> FALSE
	    if (c.isLessThanZero())
		return BooleanConstant.TRUE;  // neg < 0 --> TRUE
	    else
		return BooleanConstant.FALSE; // !neg < 0 --> FALSE
	}
	return new StrictInequation(poly);
    }

    /**
     * Stores the contained polynomial of this inequation
     */
    protected Polynomial poly;

    /**
     * Creates a new inequation with the passed polynomial as left hand side of
     * the inequation and zero as the right hand side.
     * @param poly the polynomial that should be lesser than zero.
     */
    protected StrictInequation(Polynomial poly) {
	this.poly = poly;
    }

    @Override
    public void collectNumericVariables(Set<NumericVariable> set) {
	poly.collectNumericVariables(set);
    }

    @Override
    public void collectVariables(Set<Variable> set) {
	poly.collectVariables(set);
    }

    @Override
    public boolean containsVariable(Variable var) {
	return poly.containsVariable(var);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof StrictInequation){
	    StrictInequation eq = (StrictInequation)other;
	    return (eq.poly.equals(poly));
	} else
	    return false;
    }
    
    @Override
    public Polynomial getPolynomial(){
	return poly;
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
     * the number will be 1, because we only have one inequation.
     * @return <i>1</i>.
     */
    @Override
    public int getSystemCount(){
	return 1;
    }

    @Override
    public int hashCode(){
	return poly.hashCode() + 2;
    }

    @Override
    public SingleConstraint insert(Assignment assignment) {
	return newInstance(poly.insert(assignment));
    }

    /**
     * Returns false, because strict inequations do not contain boolean variables.
     * @return <i>false</i>.
     */
    @Override
    public boolean isBoolean(){
	return false;
    }

    /**
     * Returns false, because this is not an equation.
     * @return <i>false</i>.
     */
    @Override
    public boolean isEquation() {
	return false;
    }

    /**
     * Checks whether the included polynomial is linear or not.
     * @return <i>true</i> if the included polynomial is linear, <i>false</i>
     * otherwise.
     */
    @Override
    public boolean isLinear(){
	return poly.isLinear();
    }

    /**
     * Returns true, because this is a strict inequation.
     * @return <i>true</i>.
     */
    @Override
    public boolean isStrictInequation() {
	return true;
    }

    /**
     * Returns false, because this is a strict inequation.
     * @return <i>false</i>.
     */
    @Override
    public boolean isWeakInequation() {
	return false;
    }

    /**
     * Returns the negation of the constraint.
     * @return the negation of the constraint.
     */
    public ComposedConstraint negate() {
	return WeakInequation.newInstance(poly.negate());
    }

    @Override
    public ComposedConstraint toDNF() {
	return this;
    }

    /**
     * Returns a String representation of this inequation.
     * @return a String representation of this inequation.
     */
    @Override
    public String toString(){
	return poly.toString() + " < 0";
    }

    @Override
    public String toTexString(){
	return toTexString(false, false);
    }

    @Override
    public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames){
	if (inArrayEnvironment)
	    return poly.toTexString(useInternalVariableNames) + "&<&0";
	else
	    return poly.toTexString(useInternalVariableNames) + "<0";
    }

    @Override
    public boolean validateSolution(Solution solution) throws IncompleteSolutionException{
	NumberWrapper lhs = poly.computeValue(solution);
	return lhs.isLessThanZero();
    }

    @Override
    public void writeCCToLog(PrintStream logStream) {
	logStream.print("<composedconstraint>");
	logStream.print(StringFormater.xmlEncode(toString()));
	logStream.println("</composedconstraint>");
    }
}
