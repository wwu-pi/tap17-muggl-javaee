package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.BooleanVariable;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * Stores a set of simple conjunctive combined constraints. Applies some
 * simplifications while constraints are being added.
 * </BR></BR>
 * Each of the constraints has to implement the interface SingleConstraint.
 * @author Christoph Lembeck
 */
public class SingleConstraintSet implements Iterable<SingleConstraint>{

    public static final SingleConstraintSet FALSESET = new SingleConstraintSet(BooleanConstant.FALSE);


    /**
     * Stores the single constraints.
     */
    protected ArrayList<SingleConstraint> constraints;

    /**
     * Creates a new empty set of constraints.
     */
    public SingleConstraintSet(){
	this(BooleanConstant.TRUE);
    }

    /**
     * Creates a new set of constraints with the passed constraint as its only
     * member.
     * @param c the first member of the new set of constraints.
     */
    public SingleConstraintSet(SingleConstraint c){
	constraints = new ArrayList<SingleConstraint>();
	constraints.add(c);
    }

    /**
     * Adds the passed constraint to the actual set of constraints.
     * @param c the constraint that should be added.
     */
    public void add(SingleConstraint c){
	if (c instanceof Assignment){
	    for (int i = 0; i < constraints.size(); i++){
		SingleConstraint constraint = constraints.remove(i);
		constraint = constraint.insert((Assignment)c);
		constraints.add(i, constraint);
	    }
	} else {
	    for (int i = 0; i < constraints.size(); i++){
		SingleConstraint constraint = constraints.get(i);
		if (constraint instanceof Assignment){
		    c = c.insert((Assignment)constraint);
		}
	    }
	}
	if (constraints.contains(c))
	    return;
	if (constraints.contains(BooleanConstant.FALSE)){
	    if (constraints.size() == 1)
		return;
	    else {
		constraints = new ArrayList<SingleConstraint>();
		constraints.add(BooleanConstant.FALSE);
		return;
	    }
	}
	if (c.equals(BooleanConstant.FALSE)){
	    constraints = new ArrayList<SingleConstraint>();
	    constraints.add(c);
	    return;
	}
	if (c.equals(BooleanConstant.TRUE) && (constraints.size() > 0))
	    return;
	constraints.remove(BooleanConstant.TRUE);
	if (c instanceof BooleanVariable){
	    if (constraints.contains(NotConstraint.newInstance(c))){
		constraints = new ArrayList<SingleConstraint>();
		constraints.add(BooleanConstant.FALSE);
		return;
	    }
	}
	if (c instanceof NotConstraint){
	    if (constraints.contains(((NotConstraint)c).arg)){
		constraints = new ArrayList<SingleConstraint>();
		constraints.add(BooleanConstant.FALSE);
		return;
	    }
	}
	constraints.add(c);
    }

    /**
     * Adds all constraint contained in the passed set of constraints to this
     * set of constraints.
     * @param cs the set of constraints that should be added to this set.
     */
    public void addAll(SingleConstraintSet cs){
	Iterator<SingleConstraint> it = cs.iterator();
	while (it.hasNext())
	    add(it.next());
    }

    public void collectNumericVariables(Set<NumericVariable> variables){
	for (SingleConstraint constraint: constraints)
	    constraint.collectNumericVariables(variables);
    }

    /**
     * Adds the variables that are contained in the constraints to the passed
     * set.
     * @param variables the set the contained variables should be added to.
     */
    public void collectVariables(Set<Variable> variables){
	for (int i = 0; i < constraints.size(); i++)
	    getConstraint(i).collectVariables(variables);
    }

    /**
     * Checks whether the set of constraints has a strict inequation as its member
     * or not.
     * @return <i>true</i> if the set contains at least one strict inequation,
     * <i>false</i> otherwise.
     */
    public boolean containsStrictInequations(){
	for (int i = 0; i < constraints.size(); i++)
	    if (getConstraint(i).isStrictInequation())
		return true;
	return false;
    }

    public boolean containsVariable(Variable variable){
	for (int i = 0; i < constraints.size(); i++)
	    if (getConstraint(i).containsVariable(variable))
		return true;
	return false;
    }

    /**
     * Returns the constraint specified by the passed index.
     * @param idx the index of the constraint that should be returned.
     * @return the constraint with the index idx.
     */
    public SingleConstraint getConstraint(int idx){
	return constraints.get(idx);
    }

    /**
     * Returns the number of constraint contained in the set of constraints.
     * @return the number of constraint contained in the set of constraints.
     */
    public int getConstraintCount(){
	return constraints.size();
    }

    /**
     * returns a Collextion of the constraints contained in this set of
     * constraints.
     * @return a Collextion of the constraints contained in this set of
     * constraints.
     */
    public Collection<SingleConstraint> getConstraints(){
	return constraints;
    }

    /**
     * Checks whether the contained constraints are all boolean.
     * @return <i>true</i> if each constraint contained in the set is boolean,
     * <i>false</i> if one of the constraint is an arithmetic constraint.
     */
    public boolean isBoolean(){
	for (int i = 0; i < constraints.size(); i++)
	    if (!getConstraint(i).isBoolean())
		return false;
	return true;
    }

    /**
     * Checks whether this set of constraints is marked to be contradictory or
     * not.
     * @return <i>true</i> if the actual set of constraints is obviously
     * contradictory, <i>false</i> otherwise.
     */
    public boolean isContradictory(){
	return (constraints.size() == 1) &&
	constraints.contains(BooleanConstant.FALSE);
    }

    /**
     * Checks wheter the contained constraints are all linear.
     * @return <i>true</i> if each constraint contained in the set is linear,
     * <i>false</i> if one of the constraint is nonlinear.
     */
    public boolean isLinear(){
	for (int i = 0; i < constraints.size(); i++)
	    if (!getConstraint(i).isLinear())
		return false;
	return true;
    }

    /**
     * Returns an iterator over the constraints.
     * @return an iterator over the constraints.
     */
    public Iterator<SingleConstraint> iterator(){
	return constraints.iterator();
    }

    /**
     * Removes the lastly added constraint from the set of constraints.
     * @return the SingleConstraint that is removed.
     */
    public SingleConstraint removeLastConstraint(){
	return constraints.remove(constraints.size() - 1);
    }

    @Override
    public String toString(){
	StringBuffer out = new StringBuffer();
	if (getConstraintCount() > 1)
	    out.append("(");
	for (int i = 0; i < getConstraintCount(); i++){
	    out.append(getConstraint(i));
	    if (i < getConstraintCount() -1)
		out.append(" & ");
	}
	if (getConstraintCount() > 1)
	    out.append(")");
	return out.toString();
    }

    /**
     * Returns a representation of this constraint as a latex expression.
     * @return a representation of this constraint as a latex expression.
     */
    public String toTexString(boolean useInternalVariables){
	StringBuffer sb = new StringBuffer("\\left(\\begin{array}{rcl}");
	for (int i = 0; i < constraints.size(); i++){
	    SingleConstraint constraint = constraints.get(i);
	    if (i > 0)
		sb.append("\\\\");
	    sb.append(constraint.toTexString(true, useInternalVariables));
	}
	sb.append("\\end{array}\\right)");
	return sb.toString();
    }

    /**
     * Checks whether the solution object represents a valid solution for the
     * system of constraints.
     * @param solution the solution that should be validated.
     * @return <i>true</i> if the passed argument is a valid solution for the
     * system of constraints, <i>false</i> otherwise.
     * @throws IncompleteSolutionException if the set of constraints contains a
     * variable for that no binding is available in the passed solution.
     */
    public boolean validateSolution(Solution solution) throws IncompleteSolutionException{
	if (solution == Solution.NOSOLUTION) return false;
	
	for (int i = 0; i < constraints.size(); i++)
	    if (!getConstraint(i).validateSolution(solution))
		return false;
	return true;
    }

    /**
     * Writes actual state of the set of constraints to the passed print stream.
     * @param logStream the stream the logging informations should be written
     * into.
     */
    public void writeToLog(PrintStream logStream){
	logStream.println("<constraintset constraintcount=\"" + getConstraintCount() + "\">");
	for (int i = 0; i < getConstraintCount(); i++)
	    logStream.println("<constraint text=\"" + constraints.get(i).toString() + "\" />");
	logStream.println("</constraintset>");
    }
}
