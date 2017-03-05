package de.wwu.testtool.solver.listener;

import java.util.Hashtable;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.listener.SolverManagerListener;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSystem;

/**
 * @author Christoph Lembeck
 */
public class StatisticListener implements SolverManagerListener {

    protected int addedConstraintCount = 0;
    protected int getSolutionCount = 0;
    protected long getSolutionTime = 0;
    protected int hasSolutionCount = 0;
    protected long hasSolutionTime = 0;
    protected int internalGetSolutionCount = 0;
    protected long internalGetSolutionTime = 0;
    protected int internalHasSolutionCount = 0;
    protected long internalHasSolutionTime = 0;
    protected int maxStackElementCount = 0;
    protected int solverGetSolutionCount = 0;
    protected long solverGetSolutionTime = 0;
    protected int solverHasSolutionCount = 0;
    protected long solverHasSolutionTime = 0;
    protected Hashtable<String, SolverInfo> solverInfos = new Hashtable<String, SolverInfo>();

    public void constraintAdded(SolverManager manager, ConstraintExpression ce, ComposedConstraint cc) {
	addedConstraintCount++;
    }

    public void constraintRemoved(SolverManager manager) {
	// nothing to do here
    }

    public void constraintsRemoved(SolverManager manager, int count) {
	// nothing to do here
    }

    public void finalized(SolverManager manager) {
	// nothing to do here
    }

    public void getSolutionFinished(SolverManager manager, Solution solution, long time) {
	getSolutionTime += time;
    }

    public void getSolutionStarted(SolverManager manager) {
	getSolutionCount++;
    }

    public void hasSolutionFinished(SolverManager manager, boolean result, long time) {
	hasSolutionTime += time;
    }

    public void hasSolutionStarted(SolverManager manager) {
	hasSolutionCount++;
    }

    public void internalGetSolutionFinished(SolverManager manager, int idx, Solution solution, long time) {
	internalGetSolutionTime += time;
    }

    public void internalGetSolutionStarted(SolverManager manager, int idx, ConstraintSystem system, boolean cacheHit) {
	internalGetSolutionCount++;
    }

    public void internalHasSolutionFinished(SolverManager manager, int idx, boolean result, long time) {
	internalHasSolutionTime += time;
    }

    public void internalHasSolutionStarted(SolverManager manager, int idx, ConstraintSystem system, boolean cacheHit) {
	internalHasSolutionCount++;
    }

    public void solverGetSolutionFinished(SolverManager manager, Solver solver, SingleConstraintSet constraints, Solution solution, long time) {
	solverGetSolutionTime += time;
	String solverName = solver.getClass().getName();
	SolverInfo info = solverInfos.get(solverName);
	if (info == null){
	    info = new SolverInfo(solver);
	    info.instanceGetSolutionTime += time;
	    if (time > info.maxGetSolutionTime)
		info.maxGetSolutionTime = time;
	    solverInfos.put(solverName, info);
	} else{
	    info.instanceGetSolutionTime += time;
	    if (time > info.maxGetSolutionTime)
		info.maxGetSolutionTime = time;
	}
    }

    public void solverGetSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints) {
	solverGetSolutionCount++;
	String solverName = solver.getClass().getName();
	SolverInfo info = solverInfos.get(solverName);
	if (info == null){
	    info = new SolverInfo(solver);
	    info.instanceGetSolutionCount++;
	    solverInfos.put(solverName, info);
	} else
	    info.instanceGetSolutionCount++;
    }

    public void solverHasSolutionFinished(SolverManager manager, Solver solver, boolean result, long time) {
	solverHasSolutionTime += time;
	String solverName = solver.getClass().getName();
	SolverInfo info = solverInfos.get(solverName);
	if (info == null){
	    info = new SolverInfo(solver);
	    info.instanceHasSolutionTime += time;
	    if (time > info.maxHasSolutionTime)
		info.maxHasSolutionTime = time;
	    solverInfos.put(solverName, info);
	} else{
	    info.instanceHasSolutionTime += time;
	    if (time > info.maxHasSolutionTime)
		info.maxHasSolutionTime = time;
	}
    }

    public void solverHasSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints) {
	solverHasSolutionCount++;
	String solverName = solver.getClass().getName();
	SolverInfo info = solverInfos.get(solverName);
	if (info == null){
	    info = new SolverInfo(solver);
	    info.instanceHasSolutionCount++;
	    solverInfos.put(solverName, info);
	} else
	    info.instanceHasSolutionCount++;
    }

    class SolverInfo {
	protected int instanceGetSolutionCount = 0;
	protected long instanceGetSolutionTime = 0;
	protected int instanceHasSolutionCount = 0;
	protected long instanceHasSolutionTime = 0;
	protected long maxGetSolutionTime = 0;
	protected long maxHasSolutionTime = 0;
	protected String solverName;

	public SolverInfo(Solver solver){
	    this.solverName = solver.getClass().getName();
	}
    }
}
