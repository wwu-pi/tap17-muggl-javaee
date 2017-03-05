package de.wwu.muggl.solvers.solver.constraints;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;

/**
 * Defines the basic functionality of single constraints. Single constraints are
 * boolean constants, boolean variables and their negations, equations, and
 * strong and weak inequations.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public interface SingleConstraint extends ComposedConstraint {

    /**
     * Returns the new SingleConstraint which is created by inserting the given assignment.
     * @param assignment the assignment to insert.
     * @return a new constraint simplified with the given assignment.
     */
    public abstract SingleConstraint insert(Assignment assignment);

    /**
     * Returns whether the constraint is of type boolean or not.
     * @return <i>true</i> if the constraint is a boolean variable or its negation
     * or a boolean constant, <i>false</i> otherwise.
     */
    public boolean isBoolean();

    /**
     * Checks wether the constraint is an equation or not.
     * @return <i>true</i> if the constrant is an equation, <i>false</i>
     * otherwise.
     */
    public boolean isEquation();

    /**
     * Indicates whether a numeric constraint contains only linear relationships
     * or not.
     * @return <i>true</i> if the constraint is a numeric constraint and does only
     * contain linear dependencies, <i>false</i> if the constraint is of type
     * boolean or does contain any kind of nonlinearity.
     */
    public boolean isLinear();

    /**
     * Checks wheter the constraint is a strict inequation or not.
     * @return <i>true</i> if the constrant is a strict inequation, <i>false</i>
     * otherwise.
     */
    public boolean isStrictInequation();

    /**
     * Checks wheter the constraint is a weak inequation or not.
     * @return <i>true</i> if the constrant is a weak inequation, <i>false</i>
     * otherwise.
     */
    public boolean isWeakInequation();

    /**
     * Returns a representation of this constraint as a latex expression.
     * @param inArrayEnvironment if set to <code>true</code>, the result of this method will be
     * used as member in an array environment, <code>false</code> otherwise.
     * @return a representation of this constraint as a latex expression.
     */
    public String toTexString(boolean inArrayEnvironment, boolean useInternalVariables);

    /**
     * Checks wheter the solution object represents a valid solution for the
     * constraint.
     * @param solution the solution that should be validated.
     * @return <i>true</i> if the passed argument is a valid solution for the
     * constraint, <i>false</i> otherwise.
     * @throws IncompleteSolutionException if the constraint contains a variable
     * for that no binding is available in the passed solution.
     */
    public boolean validateSolution(Solution solution) throws IncompleteSolutionException;
}
