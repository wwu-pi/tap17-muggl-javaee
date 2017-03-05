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
public class Difference extends BinaryOperation{

    /**
     * Creates a new Difference object representing the operation minuend -
     * subtrahend.
     * @param minuend the minuend of the differnece.
     * @param subtrahend the subtrahend of the difference.
     * @return the new Difference expression.
     */
    public static Term newInstance(Term minuend, Term subtrahend){
	if (minuend instanceof NumericConstant && subtrahend instanceof NumericConstant)
	    return ((NumericConstant)minuend).subtract((NumericConstant)subtrahend);
	else
	    return new Difference(minuend, subtrahend);
    }

    /**
     * The minuend of the difference from that the subtrahend should be
     * subtracted.
     */
    protected Term minuend;

    /**
     * the subtrahend of the difference that sould be subtracted from the minuend.
     */
    protected Term subtrahend;

    /**
     * Creates a new Difference object representing the operation minuend -
     * subtrahend.
     * @param minuend the minuend of the differnece.
     * @param subtrahend the subtrahend of the difference.
     * @see #newInstance(Term, Term)
     */
    private Difference(Term minuend, Term subtrahend) {
	this.minuend = minuend;
	this.subtrahend = subtrahend;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	minuend.checkTypes();
	subtrahend.checkTypes();
	if (!isNumericType(minuend.getType()))
	    throw new TypeCheckException(minuend.toString() + " is not of a numeric type");
	if (!isNumericType(subtrahend.getType()))
	    throw new TypeCheckException(subtrahend.toString() + " is not of a numeric type");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return minuend.containsAsDenominator(t) || subtrahend.containsAsDenominator(t);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Difference){
	    Difference otherDifference = (Difference)other;
	    return minuend.equals(otherDifference.minuend) && subtrahend.equals(otherDifference.subtrahend);
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = minuend.findSubstitution(subTable);
	if (result == null)
	    result = subtrahend.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	Set<Term> leftSet = minuend.getDenominators();
	Set<Term> rightSet = subtrahend.getDenominators();
	if (leftSet == null)
	    return rightSet;
	if (rightSet == null)
	    return leftSet;
	leftSet.addAll(rightSet);
	return leftSet;
    }

    @Override
    public byte getType(){
	byte leftType = minuend.getType();
	byte rightType = subtrahend.getType();
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
	return minuend.hashCode() - subtrahend.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term minuendNew = minuend.insertAssignment(assignment);
	Term subtrahendNew = subtrahend.insertAssignment(assignment);
	if (subtrahendNew instanceof NumericConstant && minuendNew instanceof NumericConstant)
	    return ((NumericConstant)minuendNew).subtract((NumericConstant)subtrahendNew);
	else
	    return newInstance(minuendNew, subtrahendNew);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term minuendNew = minuend.insert(solution, produceNumericSolution);
	Term subtrahendNew = subtrahend.insert(solution, produceNumericSolution);
	if (subtrahendNew instanceof NumericConstant && minuendNew instanceof NumericConstant)
	    return ((NumericConstant)minuendNew).subtract((NumericConstant)subtrahendNew);
	else
	    return newInstance(minuendNew, subtrahendNew);
    }

    @Override
    protected Term multiply(Term factor) {
	return new Difference(minuend.multiply(factor), subtrahend.multiply(factor));
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return new Difference(minuend.substitute(a, b), subtrahend.substitute(a, b));
    }

    @Override
    public String toHaskellString(){
	return "(Difference " + minuend.toHaskellString() + " " + subtrahend.toHaskellString() + ")";
    }

    @Override
    public Polynomial toPolynomial() {
	return minuend.toPolynomial().subtractPolynomial(subtrahend.toPolynomial());
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + minuend.toString(useInternalVariableNames) + "-" + subtrahend.toString(useInternalVariableNames) + ")";
    }

    @Override
    public Term getLeft(){
	return minuend;
    }

    @Override
    public Term getRight(){
	return subtrahend;
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + minuend.toTexString(useInternalVariableNames) + "-" + subtrahend.toTexString(useInternalVariableNames) + ")";
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newMinuend = minuend.clearMultiFractions(denominators);
	Term newSubtrahend = subtrahend.clearMultiFractions(denominators);
	if (newMinuend == minuend && newSubtrahend == subtrahend)
	    return this;
	else
	    return Difference.newInstance(newMinuend, newSubtrahend);
    }
}
