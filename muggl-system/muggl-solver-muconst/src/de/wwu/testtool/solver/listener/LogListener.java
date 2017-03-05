package de.wwu.testtool.solver.listener;

import org.apache.log4j.Logger;

import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.listener.SolverManagerListener;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSystem;
import de.wwu.muggl.solvers.solver.tools.StringFormater;

/**
 * This Listener logs events to a log4j logger. It can be used to  
 * When the constructor is called without an argument, then the testool's
 * logger is used. 
 * @author Christoph Lembeck
 */
public class LogListener extends StatisticListener implements SolverManagerListener {

    protected static Logger logger = null;

    /**
     * Defaults to the solverLogger in GlobalOptions.
     */
    public LogListener(){
	logger = TesttoolConfig.getLogger();
    }
    
    /**
     * 
     * @param logger The Logger used for logging.
     */
    public LogListener(Logger logger){
	LogListener.logger = logger;
    }
    
    @Override
    public void constraintAdded(SolverManager manager, ConstraintExpression ce, ComposedConstraint cc){
	super.constraintAdded(manager, ce, cc);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: constraint added: " + ce.toString() + " --&gt; " + cc.toString());
    }

    @Override
    public void constraintRemoved(SolverManager manager){
	super.constraintRemoved(manager);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: 1 constraint removed");
    }

    @Override
    public void constraintsRemoved(SolverManager manager, int count){
	super.constraintsRemoved(manager, count);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: " + count + " constraints removed");
    }

    @Override
    public void finalized(SolverManager manager){
	super.finalized(manager);
	if (logger.isDebugEnabled()) {
	    logger.debug("SolverManager finalized");
	    logger.debug("SolverManager statistics: constraints added: " + addedConstraintCount);
	    logger.debug("SolverManager statistics: maximum stack size: " + maxStackElementCount);
	    logger.debug("SolverManager statistics: Number of hasSolution calls: " + hasSolutionCount + " (" + StringFormater.nanosToMillis(hasSolutionTime, 3) + " ms)");
	    logger.debug("SolverManager statistics: Number of hasSolution calls inside the SolverManager: " + internalHasSolutionCount + " (" + StringFormater.nanosToMillis(internalHasSolutionTime, 3) + " ms)");
	    logger.debug("SolverManager statistics: Number of hasSolution calls on external solvers: " + solverHasSolutionCount + " (" + StringFormater.nanosToMillis(solverHasSolutionTime, 3) + " ms)");
	    for (SolverInfo info: solverInfos.values())
		logger.debug("SolverManager statistics: " + info.solverName + ".hasSolution(): " + info.instanceHasSolutionCount + " (" + StringFormater.nanosToMillis(info.instanceHasSolutionTime, 3) + " ms total, " + StringFormater.nanosToMillis(info.maxHasSolutionTime, 3) + " ms max)");
	    logger.debug("SolverManager statistics: Number of getSolution calls: " + getSolutionCount + " (" + StringFormater.nanosToMillis(getSolutionTime, 3) + " ms)");
	    logger.debug("SolverManager statistics: Number of getSolution calls inside the SolverManager: " + internalGetSolutionCount + " (" + StringFormater.nanosToMillis(internalGetSolutionTime, 3) + " ms)");
	    logger.debug("SolverManager statistics: Number of getSolution calls on external solvers: " + solverGetSolutionCount + " (" + StringFormater.nanosToMillis(solverGetSolutionTime, 3) + " ms)");
	    for (SolverInfo info: solverInfos.values())
		logger.debug("SolverManager statistics: "+ info.solverName + ".getSolution(): " + info.instanceGetSolutionCount + " (" + StringFormater.nanosToMillis(info.instanceGetSolutionTime, 3) + " ms total, " + StringFormater.nanosToMillis(info.maxGetSolutionTime, 3) + " ms max)");
	}	
    }

    @Override
    public void getSolutionFinished(SolverManager manager, Solution solution, long time){
	super.getSolutionFinished(manager, solution, time);
	if (logger.isDebugEnabled()) logger.debug("SolverManager.getSolution: " + solution + ", Time: " + StringFormater.nanosToMillis(time, 3) + " ms");
    }

    @Override
    public void getSolutionStarted(SolverManager manager){
	super.getSolutionStarted(manager);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: start getSolution()");
    }

    @Override
    public void hasSolutionFinished(SolverManager manager, boolean result, long time){
	super.hasSolutionFinished(manager, result, time);
	if (logger.isDebugEnabled()) logger.debug("SolverManager.hasSolution: " + result + ", Time: " + StringFormater.nanosToMillis(time, 3) + " ms");
    }

    @Override
    public void hasSolutionStarted(SolverManager manager){
	super.hasSolutionStarted(manager);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: start hasSolution()");
    }

    @Override
    public void internalGetSolutionFinished(SolverManager manager, int idx, Solution solution, long time){
	super.internalGetSolutionFinished(manager, idx, solution, time);
    }

    @Override
    public void internalGetSolutionStarted(SolverManager manager, int idx,
	    ConstraintSystem system, boolean cacheHit){
	super.internalGetSolutionStarted(manager, idx, system, cacheHit);
    }

    @Override
    public void internalHasSolutionFinished(SolverManager manager, int idx,
	    boolean result, long time){
	super.internalHasSolutionFinished(manager, idx, result, time);
    }

    @Override
    public void internalHasSolutionStarted(SolverManager manager, int idx,
	    ConstraintSystem system, boolean cacheHit){
	super.internalHasSolutionStarted(manager, idx, system, cacheHit);
    }

    @Override
    public void solverGetSolutionFinished(SolverManager manager, Solver solver,
	    SingleConstraintSet constraints, Solution solution, long time){
	super.solverGetSolutionFinished(manager, solver, constraints, solution, time);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: start " + solver.getName() + ".getSolution: " + solution + ", Time: " + StringFormater.nanosToMillis(time, 3) + " ms");
    }

    @Override
    public void solverGetSolutionStarted(SolverManager manager, Solver solver,
	    SingleConstraintSet constraints){
	super.solverGetSolutionStarted(manager, solver, constraints);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: end " + solver.getName() + ".getSolution()");
    }

    @Override
    public void solverHasSolutionFinished(SolverManager manager, Solver solver,
	    boolean result, long time){
	super.solverHasSolutionFinished(manager, solver, result, time);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: end " + solver.getName() + ".hasSolution: " + result + ", Time: " + StringFormater.nanosToMillis(time, 3) + " ms");
    }

    @Override
    public void solverHasSolutionStarted(SolverManager manager, Solver solver,
	    SingleConstraintSet constraints){
	super.solverHasSolutionStarted(manager, solver, constraints);
	if (logger.isDebugEnabled()) logger.debug("SolverManager: start " + solver.getName() + ".hasSolution()");
    }
}
