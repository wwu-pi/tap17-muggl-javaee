package de.wwu.testtool.solver;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;

/**
 * This is an interface for ComposedConstraint Solvers.</BR></BR>
 * These are not solvers in the usual way. They take care of reducing the composed
 * constraint to a disjunctive normal form of SingleConstraints whose conjunctions can
 * then be solved by an appropriate (meta-)solver.
 * 
 * @author Marko Ernsting
 *
 */
public interface ComposedConstraintSolver {

    /**
     * Adds a new constraint to the system of constraints a solution should be
     * calculated for by this solver.
     * @param constraint the additional constraint.
     * @throws IncorrectSolverException if the solver is not able to handle
     * the new constraint or the entire system of constraints.
     */
    public void addConstraint(ComposedConstraint constraint) throws IncorrectSolverException;
    
    
    /**
     * Calculates a single solution (i.e. a set of values for each
     * variable contained in the set of constraints) that satisfies all of
     * the prior added constraints.
     * @return the solution that satisfies the constraints passed to this solver
     * or Solution.NOSOLUTION if definitively no such solution exists.
     * @throws IncorrectSolverException if the solver is not able to handle the
     * actual set of constraints.
     * @throws SolverUnableToDecideException if the given problem is too
     * complicated for the solver to decide about the solvability of the problem
     * or to find a solution instance.
     * @throws TimeoutException if the algorithm stops because of reaching the
     * timeout limit before being able to decide about the problem
     * @see de.wwu.muggl.solvers.Solution#NOSOLUTION
     */    
    public Solution getSolution() throws SolverUnableToDecideException, TimeoutException;
        
    /**
     * Removes the lastly added constraint from the constraint stack.
     */
    public void removeConstraint();
    
    /**
     * Resets the solver.
     * @return a new, resetted ComposedConstraintSolver object.
     */
    public ComposedConstraintSolver reset();
}
