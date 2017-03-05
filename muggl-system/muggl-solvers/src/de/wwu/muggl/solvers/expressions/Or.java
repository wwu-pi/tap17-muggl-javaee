package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.OrConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>&#124;</code> operation on boolean expressions.
 * @author Christoph Lembeck
 */
public class Or extends ConstraintExpression{

    /**
     * Creates a new Or expression representing the sematics of
     * <code>e1 &#124; e2</code>
     * @param left the first argument of the new Or expression
     * @param right the second argument of the new Or expression
     * @return the new Or expression.
     */
    public static ConstraintExpression newInstance(ConstraintExpression left, ConstraintExpression right){
	if (left instanceof BooleanConstant){
	    BooleanConstant bcLeft = (BooleanConstant)left;
	    if (bcLeft.equals(BooleanConstant.TRUE))
		return BooleanConstant.TRUE;
	    else
		return right;
	}
	if (right instanceof BooleanConstant){
	    BooleanConstant bcRight = (BooleanConstant)right;
	    if (bcRight.equals(BooleanConstant.TRUE))
		return BooleanConstant.TRUE;
	    else
		return left;
	}
	return new Or(left, right);
    }

    /**
     * The first argument of the <code>&#124;</code> operation.
     */
    protected ConstraintExpression e1;

    /**
     * The second argument of the <code>&#124;</code> operation.
     */
    protected ConstraintExpression e2;

    /**
     * Creates a new Or expression representing the sematics of
     * <code>e1 &#124; e2</code>
     * @param e1 the first argument of the new Or expression
     * @param e2 the second argument of the new Or expression
     * @see #newInstance(ConstraintExpression, ConstraintExpression)
     */
    private Or(ConstraintExpression e1, ConstraintExpression e2) {
	this.e1 = e1;
	this.e2 = e2;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	e1.checkTypes();
	e2.checkTypes();
	if (!e1.isBoolean())
	    throw new TypeCheckException(e1.toString() + " is not of type boolean");
	if (!e2.isBoolean())
	    throw new TypeCheckException(e2.toString() + " is not of type boolean");
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable){
	return OrConstraint.newInstance(e1.convertToComposedConstraint(subTable), e2.convertToComposedConstraint(subTable));
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Or){
	    Or otherOr = (Or)other;
	    return (e1.equals(otherOr.e1) && e2.equals(otherOr.e2)) || (e1.equals(otherOr.e2) && e2.equals(otherOr.e1));
	}
	return false;
    }

    /**
     * Returns <i>Expression.Boolean</i> as the type of this expression.
     * @return <i>Expression.Boolean</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    @Override
    public byte getType(){
	return Expression.BOOLEAN;
    }

    @Override
    public int hashCode(){
	return e1.hashCode() * e2.hashCode();
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	ConstraintExpression leftNew = e1.insertAssignment(assignment);
	ConstraintExpression rightNew = e2.insertAssignment(assignment);
	if (leftNew instanceof BooleanConstant){
	    BooleanConstant bcLeft = (BooleanConstant)leftNew;
	    if (bcLeft.equals(BooleanConstant.TRUE))
		return BooleanConstant.TRUE;
	    else
		return rightNew;
	}
	if (rightNew instanceof BooleanConstant){
	    BooleanConstant bcRight = (BooleanConstant)rightNew;
	    if (bcRight.equals(BooleanConstant.TRUE))
		return BooleanConstant.TRUE;
	    else
		return leftNew;
	}
	return newInstance(leftNew, rightNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	ConstraintExpression leftNew = e1.insert(solution, produceNumericSolution);
	ConstraintExpression rightNew = e2.insert(solution, produceNumericSolution);
	if (leftNew instanceof BooleanConstant){
	    BooleanConstant bcLeft = (BooleanConstant)leftNew;
	    if (bcLeft.equals(BooleanConstant.TRUE))
		return BooleanConstant.TRUE;
	    else
		return rightNew;
	}
	if (rightNew instanceof BooleanConstant){
	    BooleanConstant bcRight = (BooleanConstant)rightNew;
	    if (bcRight.equals(BooleanConstant.TRUE))
		return BooleanConstant.TRUE;
	    else
		return leftNew;
	}
	return newInstance(leftNew, rightNew);
    }

    @Override
    public boolean isConstant(){
	return e1.isConstant() && e2.isConstant();
    }

    @Override
    public ConstraintExpression negate(){
	return And.newInstance(e1.negate(), e2.negate());
    }

    @Override
    public String toHaskellString(){
	return "(BooleanOr " + e1.toHaskellString() + " " + e2.toHaskellString() + ")";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + e1.toString(useInternalVariableNames) + " | " + e2.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + e1.toTexString(useInternalVariableNames) + " \\vee " + e2.toTexString(useInternalVariableNames) + ")";
    }

	public ConstraintExpression getE1() {
		return e1;
	}

	public ConstraintExpression getE2() {
		return e2;
	}
}
