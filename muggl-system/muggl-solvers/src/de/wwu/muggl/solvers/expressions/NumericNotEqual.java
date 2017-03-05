package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.OrConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>&#033;=</code> operation on numeric expressions (terms).
 * @author Christoph Lembeck
 */
public class NumericNotEqual extends ConstraintExpression implements HasLeftAndRightTerms{

    /**
     * Creates a new NumericNotEqual object representing the operation
     * <code>&#033;=</code> on numeric expressions (terms).
     * @param left the first argument of the <code>&#033;=</code> operation.
     * @param right the second argument of the <code>&#033;=</code> operation.
     * @return the new not equal expression.
     */
    public static ConstraintExpression newInstance(Term left, Term right){
	if (left instanceof NumericConstant && right instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)left).isNotEqualTo((NumericConstant)right));
	else
	    return new NumericNotEqual(left, right);
    }

    /**
     * The first argument of the &#033;= operation.
     */
    protected Term left;

    /**
     * The second argument of the &#033;= operation.
     */
    protected Term right;

    /**
     * Creates a new NumericNotEqual object representing the operation
     * <code>&#033;=</code> on numeric expressions (terms).
     * @param left the first argument of the <code>&#033;=</code> operation.
     * @param right the second argument of the <code>&#033;=</code> operation.
     * @see #newInstance(Term, Term)
     */
    protected NumericNotEqual(Term left, Term right) {
	this.left = left;
	this.right = right;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	left.checkTypes();
	right.checkTypes();
	if (!Term.isNumericType(left.getType()))
	    throw new TypeCheckException(left.toString() + " is not of a numeric type");
	if (!Term.isNumericType(right.getType()))
	    throw new TypeCheckException(right.toString() + " is not of a numeric type");
	if (left.getType() != right.getType())
	    throw new TypeCheckException(left.toString() + " and " + right.toString() + " have different types");
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
	return OrConstraint.newInstance(LessThan.newInstance(left, right).convertToComposedConstraint(subTable), LessThan.newInstance(right, left).convertToComposedConstraint(subTable));
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
     * Returns <i>Expression.Boolean</i> as type of this inequation.
     * @return <i>Expression.Boolean</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    public byte getType(){
	return Expression.BOOLEAN;
    }

    @Override
    public int hashCode(){
	return right.hashCode() - left.hashCode();
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	Term leftNew = left.insertAssignment(assignment);
	Term rightNew = right.insertAssignment(assignment);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isNotEqualTo((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	Term leftNew = left.insert(solution, produceNumericSolution);
	Term rightNew = right.insert(solution, produceNumericSolution);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isNotEqualTo((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public boolean isConstant(){
	return left.isConstant() && right.isConstant();
    }

    @Override
    public ConstraintExpression negate(){
	return NumericEqual.newInstance(left, right);
    }

    @Override
    public String toHaskellString(){
	return "(NumericNotEqual " + left.toHaskellString() + " " + right.toHaskellString() + ")";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + left.toString(useInternalVariableNames) + "!=" + right.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + left.toTexString(useInternalVariableNames) + " \\neq " + right.toTexString(useInternalVariableNames) + ")";
    }

    @Override
	public Term getLeft() {
		return left;
	}

    @Override
	public Term getRight() {
		return right;
	}
}
