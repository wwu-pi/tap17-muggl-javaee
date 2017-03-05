package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>&#94;</code> operation on boolean expressions.
 * @author Christoph Lembeck
 */
public class Xor extends ConstraintExpression{

    /**
     * Creates a new Xor expression representing the sematics of
     * <code>e1 &#94; e2</code>
     * @param left the first argument of the new Xor expression
     * @param right the second argument of the new Xor expression
     * @return the new Xor expression.
     */
    public static ConstraintExpression newInstance(ConstraintExpression left, ConstraintExpression right){
	if (left instanceof BooleanConstant){
	    BooleanConstant bcLeft = (BooleanConstant)left;
	    if (bcLeft.equals(BooleanConstant.TRUE))
		return Not.newInstance(right);
	    else
		return right;
	}
	if (right instanceof BooleanConstant){
	    BooleanConstant bcRight = (BooleanConstant)right;
	    if (bcRight.equals(BooleanConstant.TRUE))
		return Not.newInstance(left);
	    else
		return left;
	}
	return new Xor(left, right);
    }

    /**
     * The first argument of the <code>&#94;</code> operation.
     */
    protected ConstraintExpression e1;

    /**
     * The first argument of the <code>&#94;</code> operation.
     */
    protected ConstraintExpression e2;

    /**
     * Creates a new Xor expression representing the sematics of
     * <code>e1 &#94; e2</code>
     * @param e1 the first argument of the new Xor expression
     * @param e2 the second argument of the new Xor expression
     * @see #newInstance(ConstraintExpression, ConstraintExpression)
     */
    private Xor(ConstraintExpression e1, ConstraintExpression e2) {
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
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
	return Or.newInstance(And.newInstance(e1, e2.negate()), And.newInstance(e1.negate(), e2)).convertToComposedConstraint(subTable);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof Xor){
	    Xor otherXor = (Xor)other;
	    return (e1.equals(otherXor.e1) && e2.equals(otherXor.e2)) || (e1.equals(otherXor.e2) && e2.equals(otherXor.e1));
	}
	return false;
    }

    /**
     * Returns <i>Expression.Boolean</i> as the type of this expression.
     * @return <i>Expression.Boolean</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
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
		return Not.newInstance(rightNew);
	    else
		return rightNew;
	}
	if (rightNew instanceof BooleanConstant){
	    BooleanConstant bcRight = (BooleanConstant)rightNew;
	    if (bcRight.equals(BooleanConstant.TRUE))
		return Not.newInstance(leftNew);
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
		return Not.newInstance(rightNew);
	    else
		return rightNew;
	}
	if (rightNew instanceof BooleanConstant){
	    BooleanConstant bcRight = (BooleanConstant)rightNew;
	    if (bcRight.equals(BooleanConstant.TRUE))
		return Not.newInstance(leftNew);
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
	return Or.newInstance(And.newInstance(e1, e2), And.newInstance(e1.negate(), e2.negate()));
    }

    @Override
    public String toHaskellString(){
	return "(BooleanXor " + e1.toHaskellString() + " " + e2.toHaskellString() + ")";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + e1.toString(useInternalVariableNames) + " ^ " + e2.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + e1.toTexString(useInternalVariableNames) + " \\ \\textup{xor}\\ " + e2.toTexString(useInternalVariableNames) + ")";
    }
}
