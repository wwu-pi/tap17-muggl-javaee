package de.wwu.testtool.solver.listener;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;

import de.wwu.muggl.solvers.conf.ConfigReader;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.tools.StringFormater;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class TexLogListener extends StatisticListener{

    protected static PrintStream printStream = null;
    protected static boolean loggingEnabled = false;
    
    protected Logger texLogger = null;
    
    public TexLogListener() throws FileNotFoundException{
	super();
	
	texLogger = TesttoolConfig.getTexLogger();
    }

    // listener functions
    
    @Override
    public void finalized(SolverManager manager){
	texLogger.debug("Constraints added: " + addedConstraintCount + "\\\\");
	texLogger.debug("Maximum stack size: " + maxStackElementCount + "\\\\");
	texLogger.debug("Number of external hasSolution calls: " + hasSolutionCount + " (" + StringFormater.nanosToMillis(hasSolutionTime, 3) + " ms)\\\\");
	texLogger.debug("Number of hasSolution calls on single systems of constraints: " + internalHasSolutionCount + " (" + StringFormater.nanosToMillis(internalHasSolutionTime, 3) + " ms)\\\\");
	texLogger.debug("Number of hasSolution calls on external solvers: " + solverHasSolutionCount + " (" + StringFormater.nanosToMillis(solverHasSolutionTime, 3) + " ms)\\\\");
	for (String solverName: solverInfos.keySet()){
	    SolverInfo info = solverInfos.get(solverName);
	    texLogger.debug("\\hspace*{0.5cm}" + solverName + ": " + info.instanceHasSolutionCount + " (" + StringFormater.nanosToMillis(info.instanceHasSolutionTime, 3) + " ms total, " + StringFormater.nanosToMillis(info.maxHasSolutionTime, 3) + " ms max)\\\\");
	}
	texLogger.debug("Number of external getSolution calls: " + getSolutionCount + " (" + StringFormater.nanosToMillis(getSolutionTime, 3) + " ms)\\\\");
	texLogger.debug("Number of getSolution calls on single systems of constraints: " + internalGetSolutionCount + " (" + StringFormater.nanosToMillis(internalGetSolutionTime, 3) + " ms)\\\\");
	texLogger.debug("Number of getSolution calls on external solvers: " + solverGetSolutionCount + " (" + StringFormater.nanosToMillis(solverGetSolutionTime, 3) + " ms)\\\\");
	for (String solverName: solverInfos.keySet()){
	    SolverInfo info = solverInfos.get(solverName);
	    texLogger.debug("\\hspace*{0.5cm}" + solverName + ": " + info.instanceGetSolutionCount + " (" + StringFormater.nanosToMillis(info.instanceGetSolutionTime, 3) + " ms total, " + StringFormater.nanosToMillis(info.maxGetSolutionTime, 3) + " ms max)\\\\");
	}
	super.finalized(manager);
    }

    @Override
    public void solverGetSolutionFinished(SolverManager manager, Solver solver, SingleConstraintSet constraints, Solution solution, long time){
	super.solverGetSolutionFinished(manager, solver, constraints, solution, time);
	if (solution == null)
	    texLogger.debug("Result: undecidable.\\\\");
	else
	    if (solution.equals(Solution.NOSOLUTION))
		texLogger.debug("Result: no solution.\\\\");
	    else
		texLogger.debug("Result: " + solution.toTexString(true) + "\\\\");
	texLogger.debug("Solver: " + solver.getClass().getName() + "\\\\");
	texLogger.debug("Time: " + StringFormater.nanosToMillis(time, 3) + " ms\\\\\n");
    }

    @Override
    public void solverGetSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints){
	super.solverGetSolutionStarted(manager, solver, constraints);
	texLogger.debug("getSolution:\\\\");
	texLogger.debug("$" + constraints.toTexString(true) + "$\\\\");
    }

    @Override
    public void solverHasSolutionFinished(SolverManager manager, Solver solver, boolean result, long time){
	super.solverHasSolutionFinished(manager, solver, result, time);
	if (result)
	    texLogger.debug("Result: is solvable.\\\\");
	else
	    texLogger.debug("Result: not solvable.\\\\");
	texLogger.debug("Solver: " + solver.getClass().getName() + "\\\\");
	texLogger.debug("Time: " + StringFormater.nanosToMillis(time, 3) + " ms\\\\\n");
    }

    @Override
    public void solverHasSolutionStarted(SolverManager manager, Solver solver, SingleConstraintSet constraints){
	super.solverHasSolutionStarted(manager, solver, constraints);
	texLogger.debug("hasSolution:\\\\");
	texLogger.debug("$" + constraints.toTexString(true) + "$\\\\");
    }

}
