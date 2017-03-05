package de.wwu.muggl.solvers.expressions;

import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * @author Christoph Lembeck
 */
public class Negation extends Term{

    /**
     * Creates a new Negation object representing the arithmetic negation
     * operation on terms.
     * @param expr the term that should get another sign.
     * @return the negation of the passed term.
     */
    public static Term newInstance(Term expr){
	if (expr instanceof NumericConstant){
	    NumericConstant constant = (NumericConstant)expr;
	    return constant.negate();
	}
	return new Negation(expr);
    }

    /**
     * The expression that should be negated by this operation.
     */
    protected Term expr;

    /**
     * Creates a new Negation object representing the arithmetic negation
     * operation on terms. This constructor should not be called directly to
     * allow more flexible optimizations at runtime. Please use the method
     * <code>newInstance</code> instead.
     * @param expr the term that should get another sign.
     * @see #newInstance(Term)
     */
    private Negation(Term expr) {
	this.expr = expr;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	expr.checkTypes();
	if (!isNumericType(expr.getType()))
	    throw new TypeCheckException(expr.toString() + " is not of a numeric type");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return expr.containsAsDenominator(t);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Negation){
	    Negation otherNegation = (Negation)other;
	    return expr.equals(otherNegation.expr);
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = expr.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	return expr.getDenominators();
    }

    @Override
    protected Modulo getFirstModulo() {
	return expr.getFirstModulo();
    }

    @Override
    protected Quotient getFirstQuotient() {
	return expr.getFirstQuotient();
    }

    @Override
    protected TypeCast getFirstTypeCast(boolean onlyNarrowing) {
	return expr.getFirstTypeCast(onlyNarrowing);
    }

    @Override
    protected Modulo getInmostModulo(){
	return expr.getInmostModulo();
    }

    @Override
    protected Quotient getInmostQuotient() {
	return expr.getInmostQuotient();
    }

    @Override
    public byte getType(){
	byte type = expr.getType();
	if ((type == Expression.BYTE) || (type == Expression.CHAR) || (type == Expression.SHORT))
	    return Expression.INT;
	else
	    return type;
    }

    @Override
    public int hashCode(){
	return -expr.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term t = expr.insertAssignment(assignment);
	if (t instanceof NumericConstant)
	    return ((NumericConstant)t).negate();
	else
	    return newInstance(t);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term t = expr.insert(solution, produceNumericSolution);
	if (t instanceof NumericConstant)
	    return ((NumericConstant)t).negate();
	else
	    return newInstance(t);
    }

    @Override
    public boolean isConstant(){
	return expr.isConstant();
    }

    @Override
    protected Term multiply(Term factor) {
	return new Negation(expr.multiply(factor));
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return new Negation(expr.substitute(a, b));
    }

    @Override
    public String toHaskellString(){
	return "(Negation " + expr.toHaskellString() + ")";
    }

    @Override
    public Polynomial toPolynomial() {
	return expr.toPolynomial().negate();
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "-" + expr.toString(useInternalVariableNames);
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "-" + expr.toTexString(useInternalVariableNames);
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newExpr = expr.clearMultiFractions(denominators);
	if (expr == newExpr)
	    return this;
	else
	    return Negation.newInstance(newExpr);
    }

    @Override
    protected Quotient getFirstNonintegerQuotient() {
	return expr.getFirstNonintegerQuotient();
    }


}
