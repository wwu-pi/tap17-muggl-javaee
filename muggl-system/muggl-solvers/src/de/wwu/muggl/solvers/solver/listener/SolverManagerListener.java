package de.wwu.muggl.solvers.solver.listener;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSystem;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public interface SolverManagerListener {

    public void constraintAdded(SolverManager manager, ConstraintExpression ce, ComposedConstraint cc);

    public void constraintRemoved(SolverManager manager);

    public void constraintsRemoved(SolverManager manager, int count);

    public void finalized(SolverManager manager);

    public void getSolutionFinished(SolverManager manager, Solution solution, long time);

    public void getSolutionStarted(SolverManager manager);

    public void hasSolutionFinished(SolverManager manager, boolean result, long time);

    public void hasSolutionStarted(SolverManager manager);

    public void internalGetSolutionFinished(SolverManager manager, int idx, Solution solution, long time);

    public void internalGetSolutionStarted(SolverManager manager, int idx, ConstraintSystem system, boolean cacheHit);

    public void internalHasSolutionFinished(SolverManager manager, int idx, boolean result, long time);

    public void internalHasSolutionStarted(SolverManager manager, int idx, ConstraintSystem system, boolean cacheHit);

    public void solverGetSolutionFinished(SolverManager manager, Solver solver, SingleConstraintSet constrsints, Solution solution, long time);

    public void solverGetSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints);

    public void solverHasSolutionFinished(SolverManager manager, Solver solver, boolean result, long time);

    public void solverHasSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints);
    
}
