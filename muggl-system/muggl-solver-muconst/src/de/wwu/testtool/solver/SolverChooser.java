package de.wwu.testtool.solver;

import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.conf.SolverManagerConfig;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.solver.SolverInfo;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.testtool.solver.tsolver.BooleanSolver;

/**
 * The name says it all.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class SolverChooser {

    private static Solver[] resetSolvers(Solver[] solvers) {
	Solver[] result = new Solver[solvers.length];
	for (int i = 0; i < result.length; i++)
	    result[i] = solvers[i].reset();
	return result;
    }

    protected BooleanSolver booleanSolver;

    protected Solver[] booleanSolvers;

    protected Solver[] linearStrictInequalitiesSolvers;

    protected Solver[] linearWeakInequalitiesSolvers;

    protected Solver[] nonlinearSolvers;

    protected SolverInfo[] solvers;

    /**
     * Reads enabled solvers from config file and initializes the solver chooser object accordingly.
     * 
     * @param solverManager
     */
    public SolverChooser(SolverManager solverManager) {
	SolverManagerConfig config = SolverManagerConfig.getInstance();

	
	solvers = config.getSolverInfos(true);
	Arrays.sort(solvers);
	
	if (solvers == null) {
	    if (Globals.getInst().solverLogger.isDebugEnabled()) Globals.getInst().solverLogger.debug("Program aborted"); // Changed 2008.02.05
	    System.exit(0);
	}

	String message = "SolverChooser: Enabled solvers: ";
	for (int i = 0; i < solvers.length; i++) {
	    message += solvers[i].getSolver(solverManager).getName();
	    if (i < solvers.length - 1)
		message += ", ";
	}

	if (Globals.getInst().solverLogger.isDebugEnabled()) Globals.getInst().solverLogger.debug("Config: " + message);

	Vector<Solver> booleans = new Vector<Solver>();
	Vector<Solver> linearStricts = new Vector<Solver>();
	Vector<Solver> linearWeaks = new Vector<Solver>();
	Vector<Solver> nonlinears = new Vector<Solver>();
	for (SolverInfo solverInfo: solvers) {
	    Solver solver = solverInfo.getSolver(solverManager);
	    if (!solver.handlesNumericProblems())
		booleans.add(solver);
	    if (solver.handlesNumericProblems()) {
		if (solver.handlesStrictInequalities())
		    linearStricts.add(solver);
		if (solver.handlesWeakInequalities())
		    linearWeaks.add(solver);
		if (solver.handlesNonlinearProblems())
		    nonlinears.add(solver);
	    }
	}
	booleanSolvers = new Solver[booleans.size()];
	booleanSolvers = booleans.toArray(booleanSolvers);
	linearStrictInequalitiesSolvers = new Solver[linearStricts.size()];
	linearStrictInequalitiesSolvers = linearStricts.toArray(linearStrictInequalitiesSolvers);
	linearWeakInequalitiesSolvers = new Solver[linearWeaks.size()];
	linearWeakInequalitiesSolvers = linearWeaks.toArray(linearWeakInequalitiesSolvers);
	nonlinearSolvers = new Solver[nonlinears.size()];
	nonlinearSolvers = nonlinears.toArray(nonlinearSolvers);

	Logger solverLogger = Globals.getInst().solverLogger;
	if (solverLogger.isDebugEnabled()) {
	    solverLogger.debug("SolverChooser: BooleanSolvers: " + Arrays.toString(booleanSolvers));
	    solverLogger.debug("SolverChooser: LinearStrictSolvers: " + Arrays.toString(linearStrictInequalitiesSolvers));
	    solverLogger.debug("SolverChooser: LinearWeakSolvers: " + Arrays.toString(linearWeakInequalitiesSolvers));
	    solverLogger.debug("SolverChooser: NonlinearSolvers: " + Arrays.toString(nonlinearSolvers));
	}
    }

    /**
     * @param constraints
     * @return solvers which are capable of solving the passed constraint set.
     */
    public Solver[] getSolvers(SingleConstraintSet constraints) {
	if (constraints.isBoolean()) {
	    return resetSolvers(booleanSolvers);
	}
	if (constraints.isLinear()) {
	    if (constraints.containsStrictInequations())
		return resetSolvers(linearStrictInequalitiesSolvers);
	    else
		return resetSolvers(linearWeakInequalitiesSolvers);
	} else {
	    return resetSolvers(nonlinearSolvers);
	}
    }
}