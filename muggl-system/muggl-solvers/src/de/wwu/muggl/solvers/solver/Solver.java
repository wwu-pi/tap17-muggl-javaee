package de.wwu.muggl.solvers.solver;

import java.util.List;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;

/**
 * Describes the basic functionality of objects that are able to find single
 * constranit satisfying solutions for a given set of constraints.
 * @author Christoph Lembeck
 */
public interface Solver {

    /**
     * Adds a new constraint to the system of constraints a solution should be
     * calculated for by this solver.
     * @param constraint the additional constraint.
     * @throws IncorrectSolverException if the solver is not able to handle
     * the new constraint or the entire system of constraints.
     */
    public void addConstraint(SingleConstraint constraint) throws IncorrectSolverException;

    /**
     * This method is currently heavily used by SolverManganger but is totally
     * contradictory to the philosophy of incremental solving.
     * </BR>
     * It will be removed.
     * 
     * @param constraintSet
     * @throws IncorrectSolverException
     * @see #addConstraint(SingleConstraint)
     */
    public void addConstraintSet(SingleConstraintSet constraintSet) throws IncorrectSolverException;

    /**
     * 
     * @return
     */
    public List<Class<? extends Solver>> getRequiredSubsolvers();

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
    public Solution getSolution() throws IncorrectSolverException, SolverUnableToDecideException, TimeoutException;

    //public boolean handlesBooleanConstraints();
    
    public boolean handlesEquations();

    public boolean handlesIntegerEquations();

    public boolean handlesNonlinearProblems();

    public boolean handlesNumericProblems();

    public boolean handlesStrictInequalities();

    public boolean handlesStrictIntegerInequalities();

    public boolean handlesWeakInequalities();

    public boolean handlesWeakIntegerInequalities();

    /**
     * Checks whether a solution exists for the system of constraints stored in
     * the actual constraint stack by using the hasSolution methods of the
     * constraint solvers. This method may be a little bit faster when only
     * an assertion about the solvability of a system of constraints should be
     * calculated.
     * @return <i>true</i> if any solution for the given problem exists,
     * <i>false</i> if definetively no solution satisfies the constraints.
     * @throws IncorrectSolverException if the solver is not able to handle the
     * actual set of constraints.
     * @throws SolverUnableToDecideException if the given problem is too
     * complicated for the solver to decide about the solvability of the problem
     * or to find a solution instance.
     * @throws TimeoutException if the algorithm stops because of reaching the
     * timeout limit before being able to decide about the problem
     */
    public HasSolutionInformation hasSolution() throws IncorrectSolverException, SolverUnableToDecideException, TimeoutException;

    /**
     * Removes the lastly added constraint from the set of constraints.
     */
    public void removeConstraint();

    public Solver reset();

    /**
     * Returns the name of the solver for displaying it in some user interfaces or
     * logfiles.
     * @return the name of the solver.
     */
    public String getName();
}
