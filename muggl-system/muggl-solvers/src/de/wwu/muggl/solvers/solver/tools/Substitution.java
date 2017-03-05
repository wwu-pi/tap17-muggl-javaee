package de.wwu.muggl.solvers.solver.tools;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;

/**
 * TODOME: doc!
 * @author Marko Ernsting
 */
public class Substitution{
    protected Expression dest;
    protected Expression src;
    ConstraintExpression nb;
    public Substitution(Expression src, Expression dest, ConstraintExpression nb){
	this.src = src;
	this.dest = dest;
	this.nb = nb;
    }

    public Expression getDestination(){
	return dest;
    }

    public ConstraintExpression getSideCondition(){
	return nb;
    }

    public Expression getSource(){
	return src;
    }

    @Override
    public String toString(){
	return src.toString() + " --> " + dest.toString() + ", " + nb;
    }
}