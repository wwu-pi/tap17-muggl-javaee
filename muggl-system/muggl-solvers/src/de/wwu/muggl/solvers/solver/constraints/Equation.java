package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.muggl.solvers.solver.tools.MonomialFactorizer;
import de.wwu.muggl.solvers.solver.tools.StringFormater;


/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class Equation implements NumericConstraint{

    public static SingleConstraint newInstance(Polynomial poly){
	if (poly.isConstant()){
	    NumberWrapper constant = poly.getConstant();
	    if (constant == null || constant.isZero())
		return BooleanConstant.TRUE;
	    else
		return BooleanConstant.FALSE;
	}
	if (poly.isUnivariate() && poly.isLinear()){
	    TreeSet<NumericVariable> vars = new TreeSet<NumericVariable>();
	    poly.collectNumericVariables(vars);
	    NumericVariable variable = vars.first();
	    Monomial monomial = poly.getMonomials().iterator().next();
	    NumberWrapper coeff = poly.getCoefficient(monomial);
	    NumberWrapper constant = poly.getConstant();
	    switch (variable.getType()){
	    case Expression.INT:
		if (constant == null)
		    return new Assignment(variable, IntConstant.ZERO);
		constant = constant.negate();
		NumberWrapper value = constant.div(coeff);
		if (value.isInteger())
		    return new Assignment(variable, IntConstant.getInstance(value.intValue()));
		else
		    return BooleanConstant.FALSE;
	    case Expression.LONG:
		if (constant == null)
		    return new Assignment(variable, LongConstant.ZERO);
		constant = constant.negate();
		value = constant.div(coeff);
		if (value.isInteger())
		    return new Assignment(variable, LongConstant.getInstance(value.longValue()));
		else
		    return BooleanConstant.FALSE;
	    case Expression.FLOAT:
		if (constant == null)
		    return new Assignment(variable, FloatConstant.ZERO);
		constant = constant.negate();
		return new Assignment(variable, FloatConstant.getInstance((float)(constant.doubleValue()/coeff.doubleValue())));
	    case Expression.DOUBLE:
		if (constant == null)
		    return new Assignment(variable, DoubleConstant.ZERO);
		constant = constant.negate();
		return new Assignment(variable, DoubleConstant.getInstance(constant.doubleValue()/coeff.doubleValue()));
	    }
	}
	return new Equation(poly);
    }

    /**
     * Stores the contained polynomial of the equation.
     */
     protected Polynomial poly;

    /**
     * Creates a new equation with the passed polynomial as left hand side of the
     * equation and zero as the right hand side of the equation.
     * @param poly the polynomial that should be equal to zero.
     */
     private Equation(Polynomial poly) {
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
	 if (other instanceof Equation){
	     Equation eq = (Equation)other;
	     return (eq.poly.equals(poly));
	 } else
	     return false;
     }
          
     /**
      * @return the contained polynomial which should be equal to zero.
      */
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
      * the number will be 1, because we only have one equation.
      * @return <i>1</i>.
      */
     @Override
     public int getSystemCount(){
	 return 1;
     }

     @Override
     public int hashCode(){
	 return poly.hashCode() + 1;
     }

     @Override
     public SingleConstraint insert(Assignment assignment) {
	 return newInstance(poly.insert(assignment));
     }

     /**
      * Returns false, because numeric equations do not contain boolean variables.
      * @return <i>false</i>.
      */
     @Override
     public boolean isBoolean(){
	 return false;
     }

     /**
      * Returns true, because this object is an equation.
      * @return <i>true</i>.
      */
     @Override
     public boolean isEquation() {
	 return true;
     }

     @Override
     public boolean isLinear(){
	 return poly.isLinear();
     }

     /**
      * Returns false, because this object is no strict inequation.
      * @return <i>false</i>.
      */
     @Override
     public boolean isStrictInequation() {
	 return false;
     }

     /**
      * Returns false, because this object is no weak inequation.
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
	 return OrConstraint.newInstance(StrictInequation.newInstance(poly), StrictInequation.newInstance(poly.negate()));
     }

     @Override
     public ComposedConstraint toDNF() {
	 return this;
     }

     /**
      * Returns a String representation of this equation.
      * @return a String representation of this equation.
      */
     @Override
     public String toString(){
	 return poly.toString() + " = 0";
     }

     @Override
     public String toTexString(){
	 return toTexString(false, false);
     }

     @Override
     public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames){
	 if (inArrayEnvironment)
	     return poly.toTexString(useInternalVariableNames) + "&=&0";
	 else
	     return poly.toTexString(useInternalVariableNames) + "=0";
     }

     @Override
     public boolean validateSolution(Solution solution) throws IncompleteSolutionException{
	 NumberWrapper lhs = poly.computeValue(solution);
	 return lhs.isZero();
     }

     @Override
     public void writeCCToLog(PrintStream logStream) {
	 logStream.print("<composedconstraint>");
	 logStream.print(StringFormater.xmlEncode(toString()));
	 logStream.println("</composedconstraint>");
     }

}
