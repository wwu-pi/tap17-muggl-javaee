package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>&#033;=</code> operation on boolean expressions.
 * @author Christoph Lembeck
 */
public class BooleanNotEqual extends ConstraintExpression{

    /**
     * Creates a new instance of a NotEqual expression representing the semantics
     * of left &#033;= right while left and right are boolean expressions.
     * @param left the first argument of the unequality operator.
     * @param right the second argument of the unequality operator.
     * @return the new boolean not equal expression.
     */
    public static ConstraintExpression newInstance(ConstraintExpression left, ConstraintExpression right){
	if (left.equals(BooleanConstant.TRUE))
	    return Not.newInstance(right);
	if (right.equals(BooleanConstant.TRUE))
	    return Not.newInstance(left);
	if (left.equals(BooleanConstant.FALSE))
	    return right;
	if (right.equals(BooleanConstant.FALSE))
	    return left;
	return new BooleanNotEqual(left, right);
    }

    /**
     * The first argument of the &#033;= operand.
     */
    protected ConstraintExpression left;

    /**
     * The second arguemtn of the &#033;= operand.
     */
    protected ConstraintExpression right;

    /**
     * Creates a new instance of a NotEqual expression representing the semantics
     * of left &#033;= right while left and right are boolean expressions.
     * @param left the first argument of the unequality operator.
     * @param right the second argument of the unequality operator.
     * @see #newInstance(ConstraintExpression, ConstraintExpression)
     */
    protected BooleanNotEqual(ConstraintExpression left, ConstraintExpression right) {
	this.left = left;
	this.right = right;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	left.checkTypes();
	right.checkTypes();
	if (!left.isBoolean())
	    throw new TypeCheckException(left.toString() + " is not of type boolean");
	if (!right.isBoolean())
	    throw new TypeCheckException(right.toString() + " is not of type boolean");
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
	return Or.newInstance(And.newInstance(left, right.negate()), And.newInstance(left.negate(), right)).convertToComposedConstraint(subTable);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof NumericNotEqual){
	    NumericNotEqual otherNotEqual = (NumericNotEqual)other;
	    return (left.equals(otherNotEqual.left) && right.equals(otherNotEqual.right)) || (left.equals(otherNotEqual.right) && right.equals(otherNotEqual.left));
	}
	return false;
    }

    /**
     * Returns <i>Expression.BOOLEAN</i> as the type of this expression.
     * @return <i>Expression.BOOLEAN</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    @Override
    public byte getType(){
	return Expression.BOOLEAN;
    }

    @Override
    public int hashCode(){
	return right.hashCode() - left.hashCode();
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	ConstraintExpression leftNew = left.insertAssignment(assignment);
	ConstraintExpression rightNew = right.insertAssignment(assignment);
	if (leftNew.equals(BooleanConstant.TRUE))
	    return Not.newInstance(rightNew);
	if (rightNew.equals(BooleanConstant.TRUE))
	    return Not.newInstance(leftNew);
	if (leftNew.equals(BooleanConstant.FALSE))
	    return rightNew;
	if (rightNew.equals(BooleanConstant.FALSE))
	    return leftNew;
	return newInstance(leftNew, rightNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	ConstraintExpression leftNew = left.insert(solution, produceNumericSolution);
	ConstraintExpression rightNew = right.insert(solution, produceNumericSolution);
	if (leftNew.equals(BooleanConstant.TRUE))
	    return Not.newInstance(rightNew);
	if (rightNew.equals(BooleanConstant.TRUE))
	    return Not.newInstance(leftNew);
	if (leftNew.equals(BooleanConstant.FALSE))
	    return rightNew;
	if (rightNew.equals(BooleanConstant.FALSE))
	    return leftNew;
	return newInstance(leftNew, rightNew);
    }

    @Override
    public boolean isConstant(){
	return left.isConstant() && right.isConstant();
    }

    @Override
    public ConstraintExpression negate(){
	return BooleanEqual.newInstance(left, right);
    }

    @Override
    public String toHaskellString(){
	return "(BooleanNotEqual " + left.toHaskellString() + " " + right.toHaskellString() + ")";
    }

    /**
     * Returns a string representation of this inequality.
     * @param useInternalVariableNames if set to true the string representation
     * will be build using the internal names for each variable. Otherwise the
     * originally given names of the variables will be used.
     * @return the string representation of this inequality.
     */
    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + left.toString(useInternalVariableNames) + "!=" + right.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + left.toTexString(useInternalVariableNames) + " \\neq " + right.toTexString(useInternalVariableNames) + ")";
    }
}
