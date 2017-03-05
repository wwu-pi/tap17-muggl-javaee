package de.wwu.muggl.solvers.expressions;

import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;


/**
 * TODOME: what to do with this?
 * @author Marko Ernsting
 */
public class ArithmeticShiftLeft extends BinaryOperation{

    private Term left;
    private Term right;

    private ArithmeticShiftLeft(Term leftNew, Term rightNew) {
	this.left = leftNew;
	this.right = rightNew;
    }

    @Override
    public Term getLeft() {
	return left;
    }

    @Override
    public Term getRight() {
	return right;
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return left.containsAsDenominator(t) || right.containsAsDenominator(t);
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = left.findSubstitution(subTable);
	if (result == null)
	    result = right.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	Set<Term> leftSet = left.getDenominators();
	Set<Term> rightSet = right.getDenominators();
	if (leftSet == null)
	    return rightSet;
	if (rightSet == null)
	    return leftSet;
	leftSet.addAll(rightSet);
	return leftSet;
    }

    @Override
    public Term insertAssignment(Assignment assignment) {
	Term leftNew = left.insertAssignment(assignment);
	Term rightNew = right.insertAssignment(assignment);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return ((NumericConstant)leftNew).shiftLeft(((NumericConstant)rightNew).getIntValue());
	else
	    return newInstance(leftNew, rightNew);
    }

    public static Term newInstance(Term leftNew, Term rightNew) {
	if (rightNew instanceof NumericConstant){
	    NumericConstant constant = (NumericConstant)rightNew;
	    if (!constant.isInteger())
		throw new InternalError("right hand side argument of shift operation has to be integer");
	    int range = constant.getIntValue() & 0x1f;
	    if (leftNew instanceof NumericConstant)
		return ((NumericConstant)leftNew).shiftLeft(range);
	    if (range == 0)
		return leftNew;
	    int denominator = 1 << range;
	    if (leftNew.getType() == Expression.LONG)
		return Product.newInstance(leftNew, LongConstant.getInstance(denominator));
	    return Product.newInstance(leftNew, IntConstant.getInstance(denominator));
	} else
	    return new ArithmeticShiftLeft(leftNew, rightNew);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution) {
	Term leftNew = left.insert(solution, produceNumericSolution);
	Term rightNew = right.insert(solution, produceNumericSolution);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return ((NumericConstant)leftNew).shiftLeft(((NumericConstant)rightNew).getIntValue());
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    protected Term multiply(Term factor) {
	return newInstance(left.multiply(factor), right);
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return newInstance(left.substitute(a, b), right.substitute(a, b));
    }

    @Override
    public String toHaskellString() {
	// CLTODO toHaskellTerm implementieren
	throw new InternalError("Not yet implemented");
    }

    @Override
    public Polynomial toPolynomial() {
	// CLTODO shift Operation
	throw new InternalError("Not yet implemented");
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newLeft = left.clearMultiFractions(denominators);
	Term newRight = right.clearMultiFractions(denominators);
	if (newLeft == left && newRight == right)
	    return this;
	else
	    return newInstance(newLeft, newRight);
    }

    public void checkTypes() throws TypeCheckException {
	left.checkTypes();
	right.checkTypes();
	if (!isIntegerType(left.getType()))
	    throw new TypeCheckException(left.toString() + " is not of an integer type");
	if (!isIntegerType(right.getType()))
	    throw new TypeCheckException(right.toString() + " is not of an integer type");
    }

    public String toString(boolean useInternalVariableNames) {
	return "(" + left.toString(useInternalVariableNames) + "<<" + right.toString(useInternalVariableNames) + ")";
    }

    public String toTexString(boolean useInternalVariableNames) {
	return "(" + left.toString(useInternalVariableNames) + "<<" + right.toString(useInternalVariableNames) + ")";
    }

    public byte getType() {
	byte leftType = left.getType();
	byte rightType = right.getType();
	if ((leftType == Expression.LONG) ||(rightType == Expression.LONG))
	    return Expression.LONG;
	return Expression.INT;
    }

}
