package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.Set;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;


/**
 * Common interface for all composed constraints that may become members of
 * single constraint stack elements. As the individual constraints will be added
 * incrementally to the constraint stack of the constraint solver manager and
 * will there be treated as conjunctive associated constraints, composed
 * constraints allow several disjunctive associated or negated constraints to be
 * added to the constraint stack within one constraint stack element.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public interface ComposedConstraint {

    /**
     * Adds all numeric variables contained in the constraint to the given set.
     * If any boolean variables will be contained in the constraints, a
     * RuntimeException will be thrown.
     * @param set the set, the contained variables should be added to.
     */
    public void collectNumericVariables(Set<NumericVariable> set);

    /**
     * Adds all variables contained in the constraint to the given set.
     * @param set the set, the contained variables should be added to.
     */
    public void collectVariables(Set<Variable> set);

    /**
     * Checks whether the constraint contains the passed variable <i>var</i> or not.
     * @param var the variable that should be searched for in the constraint.
     * @return <i>true</i> if the variable is member of the constraint,
     * <i>false</i> otherwise.
     */
    public boolean containsVariable(Variable var);

    /**
     * Returns the conjunctive associated system of constraints with the index
     * <i>idx</i> of the disjunctive normal form of the constraint (Beginning with
     * 0). The disjunctive normal form of the constraint has not to be calculated
     * for this purpose, thus the calculation of the system can be performed very
     * fast.
     * @param idx the index of the conjunctive associated system of constraints in
     * the disjuntive normal form of the constraint.
     * @return the conjunctive associated system of constraints with the index
     * <i>idx</i>.
     */
    public ConstraintSystem getSystem(int idx);

    /**
     * Returns the number of disjunctive associated systems of constraints. The
     * systems will not be generated and the constraint will not be transformed to
     * disjunctive normal form. Only the number of disjunctive associated systems
     * of constraints that would build the disjunctive normal form of the
     * constraint will be calculated.
     * @return the number of disjunctive associated systems of constraints in the
     * disjunctive normal form of this constraint.
     */
    public int getSystemCount();

    /**
     * Returns a representation of this constraint as a latex expression.
     * @return a representation of this constraint as a latex expression.
     */
    public String toTexString();

    /**
     * TODOME: remove this
     * Writes the state of the object to the log.
     * @param logStream the stream the state of the object should be written to.
     */
    @Deprecated
    public void writeCCToLog(PrintStream logStream);

    /**
     * TODOME: doc!
     * @return
     */
    public abstract ComposedConstraint toDNF();

}