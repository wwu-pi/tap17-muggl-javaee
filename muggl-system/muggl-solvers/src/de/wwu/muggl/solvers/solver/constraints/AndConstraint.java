package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.Set;
import java.util.Vector;

import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * @author Christoph Lembeck
 */
public class AndConstraint implements ComposedConstraint{

    /**
     * Creates a new instance of AndConstraint if the new arising AndConstraint
     * will not be simplifyable by simple conversions. If the new AndConstraint
     * would be easy to simplify, the simplification of the AndConstraint will be
     * returned instead of creating the AndConstraint.
     * @param left the left handed side of the and operator.
     * @param right the right handed side of the and operator.
     * @return the new instance of AndConstraint or an equivalent simplification
     * of it.
     */
    public static ComposedConstraint newInstance(ComposedConstraint left, ComposedConstraint right){
	if (left.equals(BooleanConstant.TRUE))
	    return right;
	if (right.equals(BooleanConstant.TRUE))
	    return left;
	if (left.equals(BooleanConstant.FALSE))
	    return BooleanConstant.FALSE;
	if (right.equals(BooleanConstant.FALSE))
	    return BooleanConstant.FALSE;
	if (left.equals(right))
	    return left;
	return new AndConstraint(left, right);
    }

    /**
     * The left handed side of the and operator
     */
    protected ComposedConstraint left;

    /**
     * The right handed side of the and operator.
     */
    protected ComposedConstraint right;

    /**
     * Stores the number of systems of constraints in the disjunctive normal form
     * of this constraint. Here the total number is the product of the number of
     * systems of each argument.
     */
    protected int systemCount;

    /**
     * Creates a new instance of AndConstraint. This constructor has private
     * access only because no AndConstraint should be created directly using this
     * constructor. Please use the method newInstance instead.
     * @param left the first argument of the and operator.
     * @param right the second argument of the and operator.
     * @see de.wwu.muggl.solvers.solver.constraints.AndConstraint#newInstance(de.wwu.muggl.solvers.solver.constraints.ComposedConstraint,de.wwu.muggl.solvers.solver.constraints.ComposedConstraint)
     */
    private AndConstraint(ComposedConstraint left, ComposedConstraint right) {
	this.left = left;
	this.right = right;
	systemCount = left.getSystemCount() * right.getSystemCount();
    }

    @Override
    public void collectNumericVariables(Set<NumericVariable> set) {
	left.collectNumericVariables(set);
	right.collectNumericVariables(set);
    }

    @Override
    public void collectVariables(Set<Variable> set) {
	left.collectVariables(set);
	right.collectVariables(set);
    }

    @Override
    public boolean containsVariable(Variable var) {
	return left.containsVariable(var) || right.containsVariable(var);
    }

    /**
     * Indicates whether some other object is equal to this one.
     * @param o the object this one should be compared to.
     * @return <i>true</i> if the other object is also of type AndConstraint and
     * each of the arguments is equal to one of the other AndConstraint;
     * <i>false</i> otherwise.
     */
    @Override
    public boolean equals(Object o){
	if (o instanceof AndConstraint){
	    AndConstraint oAnd = (AndConstraint)o;
	    return (oAnd.left.equals(left) && oAnd.right.equals(right)) ||
	    (oAnd.left.equals(right) && oAnd.right.equals(left));
	} else
	    return false;
    }
    
    @Override
    public ConstraintSystem getSystem(int idx){
	if (idx >= systemCount)
	    throw new IllegalArgumentException("Not so many systems");
	return ConstraintSystem.getConstraintSystem(left.getSystem(idx/right.getSystemCount()), right.getSystem(idx % right.getSystemCount()));
    }

    /**
     * {@inheritDoc}
     * </BR></BR>
     * Here only the number of disjunctive systems of
     * constraints contained in the left handed argument is multiplied with the
     * number of systems of the right handed argument. Even this will only be done
     * during construction of the AndConstraint so that this method will answer in
     * O(1).
     */
    @Override
    public int getSystemCount() {
	return systemCount;
    }

    @Override
    public int hashCode(){
	return left.hashCode() + right.hashCode() + 1704;
    }

    @Override
    public String toString(){
	return "(" + left.toString() + " & " + right.toString() +  ")";
    }

    @Override
    public String toTexString(){
	StringBuffer sb = new StringBuffer();
	sb.append("(");
	Vector<ComposedConstraint> alternatives = new Vector<ComposedConstraint>();
	alternatives.add(left);
	alternatives.add(right);
	for (int i = 0; i < alternatives.size(); i++){
	    ComposedConstraint constraint = alternatives.get(i);
	    if (constraint instanceof AndConstraint){
		alternatives.remove(i);
		alternatives.add(i, ((AndConstraint)constraint).right);
		alternatives.add(i, ((AndConstraint)constraint).left);
		i--;
	    } else {
		if (i > 0)
		    sb.append("\\wedge ");
		sb.append(constraint.toTexString());
	    }
	}
	sb.append(")");
	return sb.toString();
    }

    @Override
    public void writeCCToLog(PrintStream logStream) {
	logStream.print("<composedconstraint>");
	logStream.print(toString());
	logStream.println("</composedconstraint>");
    }

    @Override
    public ComposedConstraint toDNF() {
	ComposedConstraint leftNew = left.toDNF();
	ComposedConstraint rightNew = right.toDNF();
	if (leftNew instanceof OrConstraint){
	    OrConstraint or = (OrConstraint)leftNew;
	    return OrConstraint.newInstance(AndConstraint.newInstance(or.left, rightNew), AndConstraint.newInstance(or.right, rightNew)).toDNF();
	}
	if (rightNew instanceof OrConstraint){
	    OrConstraint or = (OrConstraint)rightNew;
	    return OrConstraint.newInstance(AndConstraint.newInstance(leftNew, or.left), AndConstraint.newInstance(leftNew, or.right)).toDNF();
	}
	return this;
    }

	public ComposedConstraint getLeft() {
		return left;
	}

	public ComposedConstraint getRight() {
		return right;
	}
}
