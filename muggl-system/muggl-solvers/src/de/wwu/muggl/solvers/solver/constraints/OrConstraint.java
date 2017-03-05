package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.Set;
import java.util.Vector;

import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.tools.StringFormater;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class OrConstraint implements ComposedConstraint{

    /**
     * Creates a new instance of OrConstraint if the new arising OrConstraint
     * will not be simplifyable by simple conversions. If the new OrConstraint
     * would be easy to simplify, the simplification of the OrConstraint will be
     * returned instead of creating the OrConstraint.
     * @param left the left handed side of the or operator.
     * @param right the right handed side of the or operator.
     * @return the new instance of OrConstraint or an equivalent simplification
     * of it.
     */
    public static ComposedConstraint newInstance(ComposedConstraint left, ComposedConstraint right){
	if (left.equals(BooleanConstant.TRUE))
	    return BooleanConstant.TRUE;
	if (right.equals(BooleanConstant.TRUE))
	    return BooleanConstant.TRUE;
	if (left.equals(BooleanConstant.FALSE))
	    return right;
	if (right.equals(BooleanConstant.FALSE))
	    return left;
	if (left.equals(right))
	    return left;
	return new OrConstraint(left, right);
    }

    /**
     * The left handed side of the or operator.
     */
    protected ComposedConstraint left;

    /**
     * The right handed side of the or operator.
     */
    protected ComposedConstraint right;

    /**
     * Stores the number of systems of constraints in the disjunctive normal form
     * of this constraint. Here the total number will only be the sum of the
     * number of systems of both of the arguments of the or opreator.
     */
    protected int systemCount;

    /**
     * Creates a new instance of OrConstraint. This constructor has private
     * access only because no OrConstraint should be created directly using this
     * constructor. Please use the method newInstance instead.
     * @param left the first argument of the or operand.
     * @param right the second argument of the or operand.
     * @see de.wwu.muggl.solvers.solver.constraints.AndConstraint#newInstance(de.wwu.muggl.solvers.solver.constraints.ComposedConstraint,de.wwu.muggl.solvers.solver.constraints.ComposedConstraint)
     */
    private OrConstraint(ComposedConstraint left, ComposedConstraint right) {
	this.left = left;
	this.right = right;
	systemCount = left.getSystemCount() + right.getSystemCount();
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
     * @return <i>true</i> if the other object is also of type OrConstraint and
     * each of the arguments is equal to one of the other OrConstraint;
     * <i>false</i> otherwise.
     */
    @Override
    public boolean equals(Object o){
	if (o instanceof OrConstraint){
	    OrConstraint oOr = (OrConstraint)o;
	    return (oOr.left.equals(left) && oOr.right.equals(right)) ||
	    (oOr.left.equals(right) && oOr.right.equals(left));
	} else
	    return false;
    }
    
    @Override
    public ConstraintSystem getSystem(int idx) {
	if (idx >= systemCount)
	    throw new IllegalArgumentException("Not so many systems (" + idx + ")");
	int leftCount = left.getSystemCount();
	if (idx < leftCount)
	    return left.getSystem(idx);
	else
	    return right.getSystem(idx - leftCount);
    }

    /**
     * {@inheritDoc}
     * </BR></BR>
     * 
     * Here only the number of disjunctive systems of
     * constraints contained in the left handed argument is added to the
     * number of systems of the right handed argument. Even this will only be done
     * during construction of the OrConstraint so that this method will answer in
     * O(1).
     * 
     * @return the number of disjunctive associated systems of constraints in the
     * disjunctive normal form of this constraint.
     */
    @Override
    public int getSystemCount(){
	return systemCount;
    }

    @Override
    public int hashCode(){
	return left.hashCode() + right.hashCode() + 3589;
    }

    @Override
    public String toString(){
	return "(" + left.toString() + " | " + right.toString() +  ")";
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
	    if (constraint instanceof OrConstraint){
		alternatives.remove(i);
		alternatives.add(i, ((OrConstraint)constraint).right);
		alternatives.add(i, ((OrConstraint)constraint).left);
		i--;
	    } else {
		if (i > 0)
		    sb.append("\\vee ");
		sb.append(constraint.toTexString());
	    }
	}
	sb.append(")");
	return sb.toString();
    }

    @Override
    public void writeCCToLog(PrintStream logStream) {
	logStream.print("<composedconstraint>");
	logStream.print(StringFormater.xmlEncode(toString()));
	logStream.println("</composedconstraint>");
    }

    @Override
    public ComposedConstraint toDNF() {
	ComposedConstraint leftNew = left.toDNF();
	ComposedConstraint rightNew = right.toDNF();
	if (leftNew != left || rightNew != right)
	    return OrConstraint.newInstance(leftNew, rightNew);
	else
	    return this;
    }

	public ComposedConstraint getLeft() {
		return left;
	}

	public ComposedConstraint getRight() {
		return right;
	}


}
