package de.wwu.muggl.solvers.solver.listener;

import java.util.ArrayList;
import java.util.Vector;

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
public class SolverManagerListenerList {

    protected ArrayList<SolverManagerListener> listeners;

    public SolverManagerListenerList(){
	listeners = new ArrayList<SolverManagerListener>();
    }

    public void addListener(SolverManagerListener listener){
	listeners.add(listener);
    }

    public void fireAddConstraint(SolverManager manager, ConstraintExpression ce, ComposedConstraint cc){
	for (SolverManagerListener listener: listeners)
	    listener.constraintAdded(manager, ce, cc);
    }

    public void fireConstraintRemoved(SolverManager manager){
	for (SolverManagerListener listener: listeners)
	    listener.constraintRemoved(manager);
    }

    public void fireConstraintsRemoved(SolverManager manager, int count){
	for (SolverManagerListener listener: listeners)
	    listener.constraintsRemoved(manager, count);
    }

    public void fireFinalize(SolverManager manager){
	for (SolverManagerListener listener: listeners)
	    listener.finalized(manager);
    }

    public void fireGetSolutionFinished(SolverManager manager, Solution solution, long time){
	for (SolverManagerListener listener: listeners)
	    listener.getSolutionFinished(manager, solution, time);
    }

    public void fireGetSolutionStarted(SolverManager manager){
	for (SolverManagerListener listener: listeners)
	    listener.getSolutionStarted(manager);
    }

    public void fireHasSolutionFinished(SolverManager manager, boolean result, long time){
	for (SolverManagerListener listener: listeners)
	    listener.hasSolutionFinished(manager, result, time);
    }

    public void fireHasSolutionStarted(SolverManager manager){
	for (SolverManagerListener listener: listeners)
	    listener.hasSolutionStarted(manager);
    }

    public void fireInternalGetSolutionFinished(SolverManager manager, int idx, Solution solution, long time){
	for (SolverManagerListener listener: listeners)
	    listener.internalGetSolutionFinished(manager, idx, solution, time);
    }

    public void fireInternalGetSolutionStarted(SolverManager manager, int idx, ConstraintSystem system, boolean cacheHit){
	for (SolverManagerListener listener: listeners)
	    listener.internalGetSolutionStarted(manager, idx, system, cacheHit);
    }

    public void fireInternalHasSolutionFinished(SolverManager manager, int idx, boolean result, long time){
	for (SolverManagerListener listener: listeners)
	    listener.internalHasSolutionFinished(manager, idx, result, time);
    }

    public void fireInternalHasSolutionStarted(SolverManager manager, int idx, ConstraintSystem constraints, boolean cacheHit){
	for (SolverManagerListener listener: listeners)
	    listener.internalHasSolutionStarted(manager, idx, constraints, cacheHit);
    }

    public void fireSolverGetSolutionFinished(SolverManager manager, Solver solver, SingleConstraintSet constraints, Solution solution, long time){
	for (SolverManagerListener listener: listeners)
	    listener.solverGetSolutionFinished(manager, solver, constraints, solution, time);
    }

    public void fireSolverGetSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints){
	for (SolverManagerListener listener: listeners)
	    listener.solverGetSolutionStarted(manager, solver, constraints);
    }

    public void fireSolverHasSolutionFinished(SolverManager manager, Solver solver, boolean result, long time){
	for (SolverManagerListener listener: listeners)
	    listener.solverHasSolutionFinished(manager, solver, result, time);
    }

    public void fireSolverHasSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints){
	for (SolverManagerListener listener: listeners)
	    listener.solverHasSolutionStarted(manager, solver, constraints);
    }

    public void removeListener(SolverManagerListener listener){
	listeners.remove(listener);
    }
}
