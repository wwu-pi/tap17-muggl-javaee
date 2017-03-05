package de.wwu.muggl.solvers.expressions;

import java.util.HashSet;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the division of two numeric expressions.
 * <br>
 * Interesting overridden functions are {@link #toPolynomial()} and {@link #clearMultiFractions}
 * @author Christoph Lembeck
 */
public class Quotient extends BinaryOperation{

    /**
     * Creates a new Quotient object representing the division of the passed
     * numerator by the passed denominator.
     * If the parameters are of type @{link NumericConstant} they are divided and a
     * NumericConstant is returned.
     * @param numerator the numerator of the new quotient.
     * @param denominator the denominator of the new quotient.
     * @return the new Quotient expression.
     */
    public static Term newInstance(Term numerator, Term denominator){
	if (numerator instanceof NumericConstant && denominator instanceof NumericConstant)
	    return ( (NumericConstant) numerator).divide( (NumericConstant) denominator);
	return new Quotient(numerator, denominator);
    }

    /**
     * The denominator of the division.
     */
    protected Term denominator;

    /**
     * The numerator of the division.
     */
    protected Term numerator;

    /**
     * Creates a new Quotient object representing the division of the passed
     * numerator by the passed denominator.
     * @param numerator the numerator of the new quotient.
     * @param denominator the denominator of the new quotient.
     * @see #newInstance(Term, Term)
     */
    private Quotient(Term numerator, Term denominator) {
	this.numerator = numerator;
	this.denominator = denominator;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	numerator.checkTypes();
	denominator.checkTypes();
	if (!isNumericType(numerator.getType()))
	    throw new TypeCheckException(numerator.toString() + " is not of a numeric type");
	if (!isNumericType(denominator.getType()))
	    throw new TypeCheckException(denominator.toString() + " is not of a numeric type");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return (denominator.equals(t) || numerator.containsAsDenominator(t));
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Quotient){
	    Quotient otherQuotient = (Quotient)other;
	    return numerator.equals(otherQuotient.numerator) && denominator.equals(otherQuotient.denominator);
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = numerator.findSubstitution(subTable);
	if (result == null)
	    result = denominator.findSubstitution(subTable);
	return result;
    }

    /**
     * Returns the denominator of the quotient.
     * @return the denominator of the quotient.
     */
    public Term getDenominator(){
	return denominator;
    }

    @Override
    protected Set<Term> getDenominators() {
	Set<Term> leftSet = numerator.getDenominators();
	Set<Term> rightSet = denominator.getDenominators();
	if (leftSet == null){
	    if (rightSet == null){
		Set<Term> s = new HashSet<Term>();
		s.add(denominator);
		return s;
	    } else {
		rightSet.add(denominator);
		return rightSet;
	    }
	} else {
	    if (rightSet == null){
		leftSet.add(denominator);
		return leftSet;
	    } else {
		leftSet.addAll(rightSet);
		leftSet.add(denominator);
		return leftSet;
	    }
	}
    }

    @Override
    protected Quotient getFirstQuotient() {
	return this;
    }

    @Override
    protected Quotient getInmostQuotient() {
	Quotient result = numerator.getInmostQuotient();
	if (result != null)
	    return result;
	result = denominator.getInmostQuotient();
	if (result != null)
	    return result;
	return this;
    }

    @Override
    public Term getLeft(){
	return numerator;
    }

    /**
     * Returns the numerator of the quotient.
     * @return the numerator of the quotient.
     */
    public Term getNumerator(){
	return numerator;
    }

    @Override
    public Term getRight(){
	return denominator;
    }

    @Override
    public byte getType(){
	byte leftType = numerator.getType();
	byte rightType = denominator.getType();
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
	return numerator.hashCode() / denominator.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term numeratorNew = numerator.insertAssignment(assignment);
	Term denominatorNew = denominator.insertAssignment(assignment);
	if (denominatorNew instanceof NumericConstant && numeratorNew instanceof NumericConstant)
	    return ((NumericConstant)numeratorNew).divide((NumericConstant)denominatorNew);
	else
	    return newInstance(numeratorNew, denominatorNew);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term numeratorNew = numerator.insert(solution, produceNumericSolution);
	Term denominatorNew = denominator.insert(solution, produceNumericSolution);
	if (denominatorNew instanceof NumericConstant && numeratorNew instanceof NumericConstant)
	    return ((NumericConstant)numeratorNew).divide((NumericConstant)denominatorNew);
	else
	    return newInstance(numeratorNew, denominatorNew);
    }

    @Override
    protected Term multiply(Term factor) {
	if (denominator.equals(factor))
	    return numerator;
	else
	    return new Quotient(numerator.multiply(factor), denominator);
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return new Quotient(numerator.substitute(a, b), denominator.substitute(a, b));
    }

    @Override
    public String toHaskellString(){
	return "(Quotient " + numerator.toHaskellString() + " " + denominator.toHaskellString() + ")";
    }

    /**
     * Throws an error because fractions can not be transformed to
     * polynomials without transforming the whole constraint this modulo operation
     * is member of. Please transform the constraint into an arithmetic equivalent
     * constraint without containing any fractions operations in it before
     * generating polynomials out of it.
     * @return <i>nothing</i>.
     * @throws InternalError
     */
    @Override
    public Polynomial toPolynomial() {
	throw new InternalError();
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + numerator.toString(useInternalVariableNames) + "/" + denominator.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "\\frac{" + numerator.toTexString(useInternalVariableNames) + "}{" + denominator.toTexString(useInternalVariableNames) + "}";
    }

    @Override
    protected Quotient getFirstNonintegerQuotient(){
	if ( Term.isIntegerType(getType()) )
	    return null; // integer quotients do not contain non-integer members
	else
	    return this;
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {

	// recursively clearMultiFractions first, start from bottom up
	Term newNumerator = numerator.clearMultiFractions(denominators);
	Term newDenominator = denominator.clearMultiFractions(denominators);

	// for integer types we already have fractions
	if ( Term.isIntegerType( getType() ) ){
	    if (numerator == newNumerator && denominator == newDenominator)
		return this;
	    else
		return Quotient.newInstance(newNumerator, newDenominator);
	}

	// non-integer types
	// 1. we get a possibly remaining noninteger quotient in
	// the current denominator and extend our current quotient with its denominator
	// (ie multiply denominator and numerator with the nonint quotient's denominator)
	// to get rid of this quotient
	// the denominators used to multiply are gathered in denominators set
	Quotient nonIntQuotient = newDenominator.getFirstNonintegerQuotient();
	if (nonIntQuotient != null){
	    // extend this quotient
	    newNumerator = newNumerator.multiply( nonIntQuotient.getDenominator() );
	    newDenominator = newDenominator.multiply( nonIntQuotient.getDenominator() );
	    // add denominator
	    denominators.add( nonIntQuotient.getDenominator() );
	}

	// 2. the numerator is handled analogously
	nonIntQuotient = newNumerator.getFirstNonintegerQuotient();
	if (nonIntQuotient != null){
	    newNumerator = newNumerator.multiply( nonIntQuotient.getDenominator() );
	    newDenominator = newDenominator.multiply( nonIntQuotient.getDenominator() );
	    denominators.add(nonIntQuotient.getDenominator());
	}

	// with the now cleared new denominator and numerator we construct and return
	// a new quotient without multifractions
	if (numerator == newNumerator && denominator == newDenominator)
	    return this;
	else
	    return Quotient.newInstance(newNumerator, newDenominator);
    }
}
