package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>&gt;=</code> operation on numeric expressions (terms).
 * @author Christoph Lembeck
 */
public class GreaterOrEqual extends ConstraintExpression implements HasLeftAndRightTerms{

    /**
     * Creates a new GreaterOrEqual object representing the <code>&gt;=</code>
     * operation on numeric expressions.
     * @param left the left hand side of the inequality.
     * @param right the right hand side of the inequality.
     * @return the new greater or equal expression.
     */
    public static ConstraintExpression newInstance(Term left, Term right){
	if (left instanceof NumericConstant && right instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)left).isGreaterOrEqual((NumericConstant)right));
	else
	    return new GreaterOrEqual(left, right);
    }

    /**
     * The left hand side of the inequation.
     */
    protected Term left;

    /**
     * The right hand side of the inequation.
     */
    protected Term right;

    /**
     * Creates a new GreaterOrEqual object representing the <code>&gt;=</code>
     * operation on numeric expressions.
     * @param left the left hand side of the inequality.
     * @param right the right hand side of the inequality.
     * @see #newInstance(Term, Term)
     */
    private GreaterOrEqual(Term left, Term right) {
	this.left = left;
	this.right = right;
    }

    /**
     * Verifies the internal types of the inequation and throws
     * an TypeCheckException if the tree contains compositions of objects whose
     * types do not fit together (e.g. an addition of boolean variables).
     * @throws TypeCheckException if the tree contains compositions of objects
     * whose types do not fit together.
     */
    @Override
    public void checkTypes() throws TypeCheckException{
	left.checkTypes();
	right.checkTypes();
	if (!Term.isNumericType(left.getType()))
	    throw new TypeCheckException(left.toString() + " is not of a numeric type");
	if (!Term.isNumericType(right.getType()))
	    throw new TypeCheckException(right.toString() + " is not of a numeric type");
	if (!Term.compatibleTypes(left.getType(), right.getType()))
	    throw new TypeCheckException(left.toString() + " and " + right.toString() + " have different types");
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable){
	return LessOrEqual.newInstance(right, left).convertToComposedConstraint(subTable);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof GreaterOrEqual){
	    GreaterOrEqual otherGeq = (GreaterOrEqual)other;
	    return left.equals(otherGeq.left) && right.equals(otherGeq.right);
	}
	return false;
    }

    /**
     * Returns <i>Expression.Boolean</i> as type of this inequation.
     * @return <i>Expression.Boolean</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    @Override
    public byte getType(){
	return Expression.BOOLEAN;
    }

    @Override
    public int hashCode(){
	return left.hashCode() - right.hashCode() + 5;
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	Term leftNew = left.insertAssignment(assignment);
	Term rightNew = right.insertAssignment(assignment);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isGreaterOrEqual((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	Term leftNew = left.insert(solution, produceNumericSolution);
	Term rightNew = right.insert(solution, produceNumericSolution);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isGreaterOrEqual((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public boolean isConstant(){
	return left.isConstant() && right.isConstant();
    }

    @Override
    public ConstraintExpression negate(){
	return LessThan.newInstance(left, right);
    }

    @Override
    public String toHaskellString(){
	return "(GreaterOrEqual " + left.toHaskellString() + " " + right.toHaskellString() + ")";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + left.toString(useInternalVariableNames) + ">=" + right.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + left.toTexString(useInternalVariableNames) + " \\geq " + right.toTexString(useInternalVariableNames) + ")";
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
