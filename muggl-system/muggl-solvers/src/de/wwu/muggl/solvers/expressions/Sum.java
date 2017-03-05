package de.wwu.muggl.solvers.expressions;

import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the addition of two numeric expressions.
 * @author Christoph Lembeck
 */
public class Sum extends BinaryOperation{

    /**
     * Creates a new Sum object representing the addition of the two passed
     * addends.
     * @param addend1 the first addend of the sum.
     * @param addend2 the second addend of the sum.
     * @return the new Sum instance.
     */
    public static Term newInstance(Term addend1, Term addend2){
	if (addend1 instanceof NumericConstant && addend2 instanceof NumericConstant)
	    return ((NumericConstant)addend1).add((NumericConstant)addend2);
	else
	    return new Sum(addend1, addend2);
    }

    /**
     * The first addend of the sum.
     */
    protected Term addend1;

    /**
     * The second addend of the sum.
     */
    protected Term addend2;

    /**
     * Creates a new Sum object representing the addition of the two passed
     * addends.
     * @param addend1 the first addend of the sum.
     * @param addend2 the second addend of the sum.
     * @see #newInstance(Term, Term)
     */
    private Sum(Term addend1, Term addend2) {
	this.addend1 = addend1;
	this.addend2 = addend2;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	addend1.checkTypes();
	addend2.checkTypes();
	if (!isNumericType(addend1.getType()))
	    throw new TypeCheckException(addend1.toString() + " is not of a numeric type");
	if (!isNumericType(addend2.getType()))
	    throw new TypeCheckException(addend2.toString() + " is not of a numeric type");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return addend1.containsAsDenominator(t) || addend2.containsAsDenominator(t);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Sum){
	    Sum otherSum = (Sum)other;
	    return (addend1.equals(otherSum.addend1) && addend2.equals(otherSum.addend2)) || (addend1.equals(otherSum.addend2) && addend2.equals(otherSum.addend1));
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = addend1.findSubstitution(subTable);
	if (result == null)
	    result = addend2.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	Set<Term> leftSet = addend1.getDenominators();
	Set<Term> rightSet = addend2.getDenominators();
	if (leftSet == null)
	    return rightSet;
	if (rightSet == null)
	    return leftSet;
	leftSet.addAll(rightSet);
	return leftSet;
    }

    @Override
    public Term getLeft(){
	return addend1;
    }

    @Override
    public Term getRight(){
	return addend2;
    }

    @Override
    public byte getType(){
	byte leftType = addend1.getType();
	byte rightType = addend2.getType();
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
	return addend1.hashCode() + addend2.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term addend1New = addend1.insertAssignment(assignment);
	Term addend2New = addend2.insertAssignment(assignment);
	if (addend1New instanceof NumericConstant && addend2New instanceof NumericConstant)
	    return ((NumericConstant)addend1New).add((NumericConstant)addend2New);
	else
	    return newInstance(addend1New, addend2New);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term addend1New = addend1.insert(solution, produceNumericSolution);
	Term addend2New = addend2.insert(solution, produceNumericSolution);
	if (addend1New instanceof NumericConstant && addend2New instanceof NumericConstant)
	    return ((NumericConstant)addend1New).add((NumericConstant)addend2New);
	else
	    return newInstance(addend1New, addend2New);
    }

    @Override
    protected Term multiply(Term factor) {
	return new Sum(addend1.multiply(factor), addend2.multiply(factor));
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return new Sum(addend1.substitute(a, b), addend2.substitute(a, b));
    }

    @Override
    public String toHaskellString(){
	return "(Sum " + addend1.toHaskellString() + " " + addend2.toHaskellString() + ")";
    }

    @Override
    public Polynomial toPolynomial() {
	return addend1.toPolynomial().addPolynomial(addend2.toPolynomial());
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + addend1.toString(useInternalVariableNames) + "+" + addend2.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + addend1.toTexString(useInternalVariableNames) + "+" + addend2.toTexString(useInternalVariableNames) + ")";
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newAddend1 = addend1.clearMultiFractions(denominators);
	Term newAddend2 = addend2.clearMultiFractions(denominators);
	if (newAddend1 == addend1 && newAddend2 == addend2)
	    return this;
	else
	    return Sum.newInstance(newAddend1, newAddend2);
    }
}
