package de.wwu.muggl.solvers.expressions;

import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * TODOME: remove! not supported anyway. See {@link #toPolynomial()}.<BR>
 * Represents the <code>&#124;</code> operation on numeric expressions.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class NumericOr extends BinaryOperation{

    /**
     * Creates a new NumericOr object representing the <code>&#124;</code>
     * operation on numeric expressions.
     * @param e1 the first argument of the or operation.
     * @param e2 the second argument of the or operation.
     * @return the new or expression.
     */
    public static Term newInstance(Term e1, Term e2){
	if (e1 instanceof NumericConstant && e2 instanceof NumericConstant)
	    return ((NumericConstant)e1).or((NumericConstant)e2);
	else
	    return new NumericOr(e1, e2);
    }

    /**
     * The first argument of the or operation.
     */
    protected Term e1;

    /**
     * The second argument of the or operation.
     */
    protected Term e2;

    /**
     * Creates a new NumericOr object representing the <code>&#124;</code>
     * operation on numeric expressions.
     * @param e1 the first argument of the or operation.
     * @param e2 the second argument of the or operation.
     * @see #newInstance(Term, Term)
     */
    protected NumericOr(Term e1, Term e2) {
	this.e1 = e1;
	this.e2 = e2;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	e1.checkTypes();
	e2.checkTypes();
	if (!isIntegerType(e1.getType()))
	    throw new TypeCheckException(e1.toString() + " is not integer");
	if (!isIntegerType(e2.getType()))
	    throw new TypeCheckException(e2.toString() + " is not integer");
	if (e1.getType() != e2.getType())
	    throw new TypeCheckException(e1.toString() + " and " + e2.toString() + "have different types");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return e1.containsAsDenominator(t) || e2.containsAsDenominator(t);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof NumericOr){
	    NumericOr otherOr = (NumericOr)other;
	    return (e1.equals(otherOr.e1) && e2.equals(otherOr.e2)) || (e1.equals(otherOr.e2) && e2.equals(otherOr.e1));
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = e1.findSubstitution(subTable);
	if (result == null)
	    result = e2.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	Set<Term> left = e1.getDenominators();
	Set<Term> right = e2.getDenominators();
	if (left == null)
	    return right;
	if (right == null)
	    return left;
	left.addAll(right);
	return left;
    }

    @Override
    public Term getLeft(){
	return e1;
    }

    @Override
    public Term getRight(){
	return e2;
    }

    @Override
    public byte getType(){
	return e1.getType();
    }

    @Override
    public int hashCode(){
	return e1.hashCode() * e2.hashCode();
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term e1New = e1.insertAssignment(assignment);
	Term e2New = e2.insertAssignment(assignment);
	if (e1New instanceof NumericConstant && e2New instanceof NumericConstant)
	    return ((NumericConstant)e1New).or((NumericConstant)e2New);
	else
	    return newInstance(e1New, e2New);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term e1New = e1.insert(solution, produceNumericSolution);
	Term e2New = e2.insert(solution, produceNumericSolution);
	if (e1New instanceof NumericConstant && e2New instanceof NumericConstant)
	    return ((NumericConstant)e1New).or((NumericConstant)e2New);
	else
	    return newInstance(e1New, e2New);
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
	    return new NumericOr(e1.substitute(a, b), e2.substitute(a, b));
    }

    @Override
    public String toHaskellString(){
	return "(NumericOr " + e1.toHaskellString() + " " + e2.toHaskellString() + ")";
    }

    /**
     * Throws an InternalError because the numeric or operation can not be
     * transformed into a polynomial.
     * @return <i>nothing</i>.
     * @throws InternalError
     */
    @Override
    public Polynomial toPolynomial() {
	return null;
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + e1.toString(useInternalVariableNames) + " | " + e2.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + e1.toTexString(useInternalVariableNames) + " | " + e2.toTexString(useInternalVariableNames) + ")";
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newLeft = e1.clearMultiFractions(denominators);
	Term newRight = e2.clearMultiFractions(denominators);
	if (e1 == newLeft && e2 == newRight)
	    return this;
	else
	    return NumericOr.newInstance(newLeft, newRight);
    }
}
