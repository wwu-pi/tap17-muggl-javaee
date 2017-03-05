package de.wwu.muggl.solvers.expressions;

import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;


/**
 * Represents the multiplication of two numeric expressions.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class Product extends BinaryOperation{

    /**
     * Creates a new Product object that represents the multiplication of the two
     * passed arguments.
     * @param factor1 the first factor of the product.
     * @param factor2 the second factor of the product.
     * @return the new Product expression.
     */
    public static Term newInstance(Term factor1, Term factor2){
	if (factor1 instanceof NumericConstant && factor2 instanceof NumericConstant)
	    return ((NumericConstant)factor1).multiply((NumericConstant)factor2);
	else
	    return new Product(factor1, factor2);
    }

    /**
     * The first factor of the product.
     */
    protected Term factor1;

    /**
     * The second factor of the product.
     */
    protected Term factor2;

    /**
     * Creates a new Product object that represents the multiplication of the two
     * passed arguments.
     * @param factor1 the first factor of the product.
     * @param factor2 the second factor of the product.
     * @see #newInstance(Term, Term)
     */
    private Product(Term factor1, Term factor2) {
	this.factor1 = factor1;
	this.factor2 = factor2;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	factor1.checkTypes();
	factor2.checkTypes();
	if (!isNumericType(factor1.getType()))
	    throw new TypeCheckException(factor1.toString() + " is not of a numeric type");
	if (!isNumericType(factor2.getType()))
	    throw new TypeCheckException(factor2.toString() + " is not of a numeric type");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return factor1.containsAsDenominator(t) || factor2.containsAsDenominator(t);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Product){
	    Product otherProduct = (Product)other;
	    return (factor1.equals(otherProduct.factor1) && factor2.equals(otherProduct.factor2)) || (factor1.equals(otherProduct.factor2) && factor2.equals(otherProduct.factor1));
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = factor1.findSubstitution(subTable);
	if (result == null)
	    result = factor2.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	Set<Term> leftSet = factor1.getDenominators();
	Set<Term> rightSet = factor2.getDenominators();
	if (leftSet == null)
	    return rightSet;
	if (rightSet == null)
	    return leftSet;
	leftSet.addAll(rightSet);
	return leftSet;
    }

    @Override
    public Term getLeft(){
	return factor1;
    }

    @Override
    public Term getRight(){
	return factor2;
    }

    @Override
    public byte getType(){
	byte leftType = factor1.getType();
	byte rightType = factor2.getType();
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
	return factor1.hashCode() * factor2.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term factor1New = factor1.insertAssignment(assignment);
	Term factor2New = factor2.insertAssignment(assignment);
	if (factor1New instanceof NumericConstant && factor2New instanceof NumericConstant)
	    return ((NumericConstant)factor1New).multiply((NumericConstant)factor2New);
	else
	    return newInstance(factor1New, factor2New);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term factor1New = factor1.insert(solution, produceNumericSolution);
	Term factor2New = factor2.insert(solution, produceNumericSolution);
	if (factor1New instanceof NumericConstant && factor2New instanceof NumericConstant)
	    return ((NumericConstant)factor1New).multiply((NumericConstant)factor2New);
	else
	    return newInstance(factor1New, factor2New);
    }

    @Override
    protected Term multiply(Term factor) {
	if (factor1.containsAsDenominator(factor))
	    return new Product(factor1.multiply(factor), factor2);
	if (factor2.containsAsDenominator(factor))
	    return new Product(factor1, factor2.multiply(factor));
	return new Product(factor, this);
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return new Product(factor1.substitute(a, b), factor2.substitute(a, b));
    }

    @Override
    public String toHaskellString(){
	return "(Product " + factor1.toHaskellString() + " " + factor2.toHaskellString() + ")";
    }

    @Override
    public Polynomial toPolynomial() {
	return factor1.toPolynomial().multiplyPolynomial(factor2.toPolynomial());
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + factor1.toString(useInternalVariableNames) + "*" + factor2.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + factor1.toTexString(useInternalVariableNames) + " \\cdot " + factor2.toTexString(useInternalVariableNames) + ")";
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newFactor1 = factor1.clearMultiFractions(denominators);
	Term newFactor2 = factor2.clearMultiFractions(denominators);
	if (newFactor1 == factor1 && newFactor2 == factor2)
	    return this;
	return Difference.newInstance(newFactor1, newFactor2);
    }
}
