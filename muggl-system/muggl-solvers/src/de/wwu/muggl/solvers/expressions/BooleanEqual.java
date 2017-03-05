package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>==</code> operation on boolean expressions.
 * @author Christoph Lembeck
 */
public class BooleanEqual extends ConstraintExpression{

    /**
     * Creates a new instance of a BooleanEqual expression representing the
     * semantics of left == right while left and right are boolean expressions.
     * @param left the first arguement of the equals operator.
     * @param right the second arguemtn of the equals operator.
     * @return the new boolean equal expression.
     */
    public static ConstraintExpression newInstance(ConstraintExpression left, ConstraintExpression right){
	if (left.equals(BooleanConstant.TRUE))
	    return right;
	if (right.equals(BooleanConstant.TRUE))
	    return left;
	if (left.equals(BooleanConstant.FALSE))
	    return Not.newInstance(right);
	if (right.equals(BooleanConstant.FALSE))
	    return Not.newInstance(left);
	return new BooleanEqual(left, right);
    }

    /**
     * The first argument of the == operation.
     */
    protected ConstraintExpression left;

    /**
     * The second argument of the == operation.
     */
    protected ConstraintExpression right;

    /**
     * Creates a new instance of a BooleanEqual expression representing the
     * semantics of left == right while left and right are boolean expressions.
     * @param left the first arguement of the equals operator.
     * @param right the second arguemtn of the equals operator.
     * @see #newInstance(ConstraintExpression, ConstraintExpression)
     */
    protected BooleanEqual(ConstraintExpression left, ConstraintExpression right) {
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
	return Or.newInstance(And.newInstance(left, right), And.newInstance(left.negate(), right.negate())).convertToComposedConstraint(subTable);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof NumericEqual){
	    NumericEqual otherEqual = (NumericEqual)other;
	    return (left.equals(otherEqual.left) && right.equals(otherEqual.right)) || (left.equals(otherEqual.right) && right.equals(otherEqual.left));
	}
	return false;
    }

    /**
     * Returns <i>Expression.Boolean</i> as type of this equation.
     * @return <i>Expression.Boolean</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    public byte getType(){
	return Expression.BOOLEAN;
    }

    @Override
    public int hashCode(){
	return left.hashCode() - right.hashCode();
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	ConstraintExpression leftNew = left.insertAssignment(assignment);
	ConstraintExpression rightNew = right.insertAssignment(assignment);
	if (leftNew.equals(BooleanConstant.TRUE))
	    return rightNew;
	if (rightNew.equals(BooleanConstant.TRUE))
	    return leftNew;
	if (leftNew.equals(BooleanConstant.FALSE))
	    return Not.newInstance(rightNew);
	if (rightNew.equals(BooleanConstant.FALSE))
	    return Not.newInstance(leftNew);
	return newInstance(leftNew, rightNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	ConstraintExpression leftNew = left.insert(solution, produceNumericSolution);
	ConstraintExpression rightNew = right.insert(solution, produceNumericSolution);
	if (leftNew.equals(BooleanConstant.TRUE))
	    return rightNew;
	if (rightNew.equals(BooleanConstant.TRUE))
	    return leftNew;
	if (leftNew.equals(BooleanConstant.FALSE))
	    return Not.newInstance(rightNew);
	if (rightNew.equals(BooleanConstant.FALSE))
	    return Not.newInstance(leftNew);
	return newInstance(leftNew, rightNew);
    }

    @Override
    public boolean isConstant(){
	return left.isConstant() && right.isConstant();
    }

    @Override
    public ConstraintExpression negate(){
	return BooleanNotEqual.newInstance(left, right);
    }

    @Override
    public String toHaskellString(){
	return "(BooleanEqual " + left.toHaskellString() + " " + right.toHaskellString() + ")";
    }

    /**
     * Returns the string representation of this boolean equation.
     * @param useInternalVariableNames if set to true the string representation
     * will be build using the internal names for each variable. Otherwise the
     * originally given names of the variables will be used.
     * @return a string representation of this boolean equation.
     */
    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + left.toString(useInternalVariableNames) + "==" + right.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + left.toTexString(useInternalVariableNames) + "=" + right.toTexString(useInternalVariableNames) + ")";
    }
}
