package de.wwu.muggl.solvers;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;

/**
 * Should take care of:
 * - initialization of solvers
 * - substitutions in constraints
 * - statistics and logging
 * - pass constraints to appropriate solvers
 * @author Marko Ernsting
 */
public interface SolverManager {

	/**
	 * Add a static constraint, i.e. a constraint that always holds, even if other constraints are removed.
	 * @param constraint
	 */
	@Deprecated
	public void addStaticConstraint(ConstraintExpression constraint);
	
    /**
     * Adds a new constraint onto the top of the constraint stack.
     * @param ce the new constraint defined by an expression built by the
     * virtual machine.
     * @return the transformed system of constraints that was added to the
     * constraint stack.
     */
    public void addConstraint(ConstraintExpression ce);
    
    /**
     * Add multiple constraints on the same level.
     * @param constraints
     */
    @Deprecated
    public void addConstraints(ConstraintExpression... constraints);
    
    /**
     * Tries to find a solution for the first non-contradictory constraint system contained in the constraint
     * stack of the solver manager.
     * @return a solution for the actual existing constraints or
     * Solution.NOSOLUTION if no such solution exists.
     * @throws SolverUnableToDecideException if the used solvers are not able to
     * decide whether a solution exists or not or are not able to find an existing
     * solution instance.
     * @throws TimeoutException if the used algorithms stop because of reaching
     * the specified time limits before being able to decide about the given
     * problem.
     * @see de.wwu.muggl.solvers.Solution#NOSOLUTION
     */
    public Solution getSolution() throws SolverUnableToDecideException, TimeoutException;
    
   
    /**
     * Checks whether a solution exists for the system of constraints stored in
     * the actual constraint stack by using the hasSolution methods of the
     * constraint solvers. This method may be a little bit faster when only
     * an assertion about the solvability of a system of constraints should be
     * calculated.
     * @return <i>true</i> if any solution for the given problem exists,
     * <i>false</i> if definitively no solution satisfies the constraints.
     * @throws SolverUnableToDecideException if the used solvers are not able to
     * decide whether a solution exists or not or are not able to find an existing
     * solution instance.
     * @throws TimeoutException if the used algorithms stop because of reaching
     * the specified time limits before being able to decide about the given
     * problem.
     */
    public boolean hasSolution() throws SolverUnableToDecideException, TimeoutException;

    /**
     * Removes the lastly added constraint from the constraint stack.
     */
    public void removeConstraint();

    public void reset();

    public void finalize() throws Throwable;
    
    
    /**
     * Returns the current level of the solver manager, this is needed to identify different solving states. 
     * @return
     */
    public int getConstraintLevel();
    
    /**
     * Set the level of the solver manager to a previous level.
     * <code>level</code> must be lower than <code>currentLevel</code>
     */
    public void resetConstraintLevel(int level);
    
    /**
     * TODOME: put this into listeners.
     * Getter for the total number of constraints checked with hasSolution(). Added 2008.02.05
     * @return The total number of constraints checked.
     */
    @Deprecated
    public long getTotalConstraintsChecked();

    /**
     * TODOME: put this into listeners.
     * Reset the statistical counter in this class. Added 2008.02.05
     */
    @Deprecated
    public void resetCounter();

}