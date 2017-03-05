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
public class Modulo extends BinaryOperation{

    /**
     * Creates a new Modulo object representing the modulo operation on the two
     * passed arguments.
     * @param dividend the dividend of the modulo operation.
     * @param divisor the divisor of the modulo operation.
     * @return the new Modulo expression.
     */
    public static Term newInstance(Term dividend, Term divisor){
	if (dividend instanceof NumericConstant && divisor instanceof NumericConstant)
	    return ((NumericConstant)dividend).modulo((NumericConstant)divisor);
	else
	    return new Modulo(dividend, divisor);
    }

    /**
     * The dividend of the modulo operation.
     */
    protected Term dividend;

    /**
     * The divisor of the modulo operation.
     */
    protected Term divisor;

    /**
     * Creates a new Modulo object representing the modulo operation on the two
     * passed arguments.
     * @param dividend the dividend of the modulo operation.
     * @param divisor the divisor of the modulo operation.
     * @see #newInstance(Term, Term)
     */
    private Modulo(Term dividend, Term divisor) {
	this.dividend = dividend;
	this.divisor = divisor;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	dividend.checkTypes();
	divisor.checkTypes();
	if (!isNumericType(dividend.getType()))
	    throw new TypeCheckException(dividend.toString() + " is not of a numeric type");
	if (!isNumericType(divisor.getType()))
	    throw new TypeCheckException(divisor.toString() + " is not of a numeric type");
	if (dividend.getType() != divisor.getType())
	    throw new TypeCheckException(dividend.toString() + " and " + divisor.toString() + " have different types");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return dividend.containsAsDenominator(t);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Modulo){
	    Modulo otherModulo = (Modulo)other;
	    return dividend.equals(otherModulo.dividend) && divisor.equals(otherModulo.divisor);
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = dividend.findSubstitution(subTable);
	if (result == null)
	    result = divisor.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	Set<Term> leftSet = dividend.getDenominators();
	Set<Term> rightSet = divisor.getDenominators();
	if (leftSet == null)
	    return rightSet;
	if (rightSet == null)
	    return leftSet;
	leftSet.addAll(rightSet);
	return leftSet;
    }

    /**
     * Returns this hence this is the first modulo operation that can be found.
     * @return <i>this</i>.
     */
    @Override
    protected Modulo getFirstModulo() {
	return this;
    }

    @Override
    protected Modulo getInmostModulo() {
	Modulo result = dividend.getInmostModulo();
	if (result != null)
	    return result;
	result = divisor.getInmostModulo();
	if (result != null)
	    return result;
	return this;
    }

    /**
     * Returns the dividend of the modulo operation.
     * @return the dividend of the modulo operation.
     */
    @Override
    public Term getLeft(){
	return dividend;
    }

    /**
     * Returns the divisor of the modulo operation.
     * @return the divisor of the modulo operation.
     */
    @Override
    public Term getRight(){
	return divisor;
    }

    @Override
    public byte getType(){
	byte leftType = dividend.getType();
	byte rightType = divisor.getType();
	if ((leftType == Expression.DOUBLE) ||(rightType == Expression.DOUBLE))
	    return Expression.DOUBLE;
	if ((leftType == Expression.FLOAT) ||(rightType == Expression.FLOAT))
	    return Expression.FLOAT;
	if ((leftType == Expression.LONG) ||(rightType == Expression.LONG))
	    return Expression.LONG;
	return Expression.INT;
    }

    @Override
    public int hashCode(){
	return dividend.hashCode() % divisor.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term leftNew = dividend.insertAssignment(assignment);
	Term rightNew = divisor.insertAssignment(assignment);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return ((NumericConstant)leftNew).modulo((NumericConstant)rightNew);
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term leftNew = dividend.insert(solution, produceNumericSolution);
	Term rightNew = divisor.insert(solution, produceNumericSolution);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return ((NumericConstant)leftNew).modulo((NumericConstant)rightNew);
	else
	    return newInstance(leftNew, rightNew);
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
	    return new Modulo(dividend.substitute(a, b), divisor.substitute(a, b));
    }

    @Override
    public String toHaskellString(){
	return "(Remainder " + dividend.toHaskellString() + " " + divisor.toHaskellString() + ")";
    }

    /**
     * Throws an error because modulo expressions can not be transformed to
     * polynomials without trnasforming the whole constraint this modulo operation
     * is member of. Please transform the constraint into an arithmetic equivalent
     * constraint without having any modulo operations in it before generating
     * polynomials out of it.
     * @return <i>nothing</i>.
     * @throws InternalError
     */
    @Override
    public Polynomial toPolynomial() {
	throw new InternalError();
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + dividend.toString(useInternalVariableNames) + "%" + divisor.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + dividend.toTexString(useInternalVariableNames) + " \\bmod " + divisor.toTexString(useInternalVariableNames) + ")";
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newDividend = dividend.clearMultiFractions(denominators);
	Term newDivisor = divisor.clearMultiFractions(denominators);
	if (divisor == newDivisor && dividend == newDividend)
	    return this;
	else
	    return Modulo.newInstance(newDividend, newDivisor);
    }
}
