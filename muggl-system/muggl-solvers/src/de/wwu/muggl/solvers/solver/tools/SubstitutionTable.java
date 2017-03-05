package de.wwu.muggl.solvers.solver.tools;

import java.util.ArrayList;
import java.util.Stack;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;

/**
 * Right now this is use to replace certain expressions in Term objects by 
 * other ones.
 * <BR><BR>
 * LessThan, LessOrEqual, NumericEqual
 * <BR><BR>
 * Replacement of: NarrowingTypeCasts, IntegerDenominators, IntegerModulo, Modulo
 * 
 * But replacement of expressions as implemented right now is inserting the same constrains over
 * and over again.
 * 
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class SubstitutionTable {

    protected ArrayList<Substitution> table;

    protected Stack<Integer> constraintStackBounds;

    public SubstitutionTable(){
	table = new ArrayList<Substitution>();
	constraintStackBounds = new Stack<Integer>();
	constraintStackBounds.push(0);
    }

    public void signalStackElementAdded(){
	constraintStackBounds.push(table.size());
    }

    public void signalStackElementRemoved(){
	signalStackElementsRemoved(1);
    }

    public void signalStackElementsRemoved(int count){
	for (int i = 0; i < count; i++)
	    constraintStackBounds.pop();
	int last = constraintStackBounds.peek();
	while (table.size() > last)
	    table.remove(last);
    }

    public void addSubstitution(Expression src, Expression dest, ConstraintExpression nb){
	table.add(new Substitution(src, dest, nb));
    }

    public Expression getSubstitution(Expression expr){
	for (Substitution sub: table)
	    if (sub.src.equals(expr))
		return sub.dest;
	return null;
    }

    public Substitution lookupSubstitution(Expression source) {
	for (Substitution substitution: table)
	    if (substitution.getSource().equals(source))
		return substitution;
	return null;
    }

    @Override
    public String toString(){
	return table.toString();
    }
}
