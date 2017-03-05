package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.NotConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>&#33;</code> operation on boolean expressions.
 * @author Christoph Lembeck
 */
public class Not extends ConstraintExpression{

    /**
     * Creates a new Not expression representing the sematics of
     * <code>&#33;expr</code>
     * @param expr the argument of the new Not expression
     * @return the new not expression.
     */
    public static ConstraintExpression newInstance(ConstraintExpression expr){
	if (expr.equals(BooleanConstant.TRUE))
	    return BooleanConstant.FALSE;
	if (expr.equals(BooleanConstant.FALSE))
	    return BooleanConstant.TRUE;
	if (expr instanceof BooleanVariable)
	    return new Not((BooleanVariable)expr);
	if (expr instanceof Not)
	    return ((Not)expr).var;
	else
	    return expr.negate();
    }

    /**
     * The argument of the <code>&#33;</code> operation.
     */
    protected BooleanVariable var;

    /**
     * Creates a new Not expression representing the sematics of
     * <code>&#33;expr</code>
     * @param var the argument of the new Not expression
     * @see #newInstance(ConstraintExpression)
     */
    private Not(BooleanVariable var) {
	this.var = var;
    }

    @Override
    public void checkTypes(){
	var.checkTypes();
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable){
	return NotConstraint.newInstance(var.convertToComposedConstraint(subTable));
    }

    /**
     * Checks whether the passed object is equal to this Not object.
     * @param obj the other object this one should be compared to.
     * @return <i>true</i> if the other object is a Not objetc too and its
     * argument is equal to the argument of this object, <i>false</i> otherwise.
     */
    @Override
    public boolean equals(Object obj){
	return ((obj instanceof Not) && (((Not)obj).var.equals(var)));
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
	return -var.hashCode();
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	ConstraintExpression exprNew = var.insertAssignment(assignment);
	if (exprNew.equals(BooleanConstant.TRUE))
	    return BooleanConstant.FALSE;
	if (exprNew.equals(BooleanConstant.FALSE))
	    return BooleanConstant.TRUE;
	else
	    return newInstance(exprNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	ConstraintExpression exprNew = var.insert(solution, produceNumericSolution);
	if (exprNew.equals(BooleanConstant.TRUE))
	    return BooleanConstant.FALSE;
	if (exprNew.equals(BooleanConstant.FALSE))
	    return BooleanConstant.TRUE;
	else
	    return newInstance(exprNew);
    }

    @Override
    public boolean isConstant(){
	return var.isConstant();
    }

    @Override
    public ConstraintExpression negate(){
	return var;
    }

    @Override
    public String toHaskellString(){
	return "(BooleanNegation " + var.toHaskellString() + ")";
    }
    
    @Override
    public String toString(boolean useInternalVariableNames){
	return "!(" + var.toString(useInternalVariableNames) + ")";
    }
    
    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "\\neg (" + var.toTexString(useInternalVariableNames) + ")";
    }
}
