package de.wwu.testtool.solver;

import org.apache.log4j.Logger;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.conf.SolverManagerConfig;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.ConstraintStack;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSystem;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.listener.SolverManagerListener;
import de.wwu.muggl.solvers.solver.listener.SolverManagerListenerList;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

public class OldSolverManagerSolver implements ComposedConstraintSolver {
    
    /**
     * Solver Manager.
     */
    SolverManager solverManager;
    
    /**
     * The constraint stack who is responsible for administrating the
     * incrementally adding and removing of new or obsolete constraints.
     */
    protected ConstraintStack constraintStack;

    protected SolverManagerListenerList listeners;

    protected SolverChooser solverChooser;

    protected SubstitutionTable substitutionTable;

    /**
     * Creates a new Solver Manager object and initializes it with a stream that
     * collects the logging informations if wanted.
     */
    public OldSolverManagerSolver(SolverManager solverManager) {
	this.solverManager = solverManager;
	Logger logger = TesttoolConfig.getLogger();
	
	if (logger.isDebugEnabled()) logger.debug("SolverManager started");
	
	substitutionTable = new SubstitutionTable();
	constraintStack = new ConstraintStack();
	solverChooser = new SolverChooser(solverManager);
	
	SolverManagerConfig solverConf = SolverManagerConfig.getInstance();
	listeners = new SolverManagerListenerList();
	for (SolverManagerListener listener : solverConf.getListeners()){
	    listeners.addListener(listener);
	    logger.debug("OldSolverManagerSolver: added listener " + listener.getClass().getName());
	}
    }

    /**
     * Adds a new constraint onto the top of the constraint stack.
     * @param ce the new constraint defined by an expression built by the
     * virtual machine.
     * @return the transformed system of constraints that was added to the
     * constraint stack.
     */
    @Override
    public void addConstraint(ComposedConstraint cc){	
	constraintStack.addConstraint(BooleanConstant.TRUE, cc);
	substitutionTable.signalStackElementAdded();
    }

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
    @Override
    public Solution getSolution() throws SolverUnableToDecideException, TimeoutException{
	// if no ConstraintSystem present return trivial Solution
	if (constraintStack.getSystemCount() == 0){
	    Solution solution = new Solution();
	    return solution;
	}

	// try to find a solution for a ConstraintSystem starting with the first on which is not already proved contradictory
	int idx = constraintStack.getFirstNoncontradictorySystemIndex();
	while (idx < constraintStack.getSystemCount()){
	    Solution solution = getSolution(idx);
	    if (solution.equals(Solution.NOSOLUTION))
		idx++;
	    else{
		return solution;
	    }
	}

	// no solution for any of our ConstraintSystems found
	return Solution.NOSOLUTION;
    }

    /**
     * Tries to find a solution for the system of constraints defined by the
     * actual state of the constraint stack and the passed index argument. If
     * such a solution can be found it will be stored into a solution cache and
     * will be reused if the same request will appear again later.
     * @param idx the index of the system of constraints a solution should be
     * generated for.
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
    private Solution getSolution(int idx) throws SolverUnableToDecideException, TimeoutException{
	long startTime = System.nanoTime();

	// try to find solution in the cache
	Solution solution = constraintStack.getCachedSolution(idx);
	if (solution != null){
	    return solution;
	}

	// otherwise calculate it
	ConstraintSystem system = constraintStack.getSystem(idx);
	if (system.isContradictory()) {
	    solution = Solution.NOSOLUTION;
	} else {
	    try{
		// a system has several variable-disjunct constraint sets
		// thus each one is solved independently and solutions are joined afterwards
		// to form a solution for the whole conjunctive constraint system
		for (int i = 0; i < system.getConstraintSetCount(); i++){

		    SingleConstraintSet constraintSet = system.getConstraintSet(i);
		    Solution newSolution = null;

		    // if constraint set consists only of one assignment we already have solution
		    if (constraintSet.getConstraintCount() == 1){
			SingleConstraint constraint = constraintSet.getConstraint(0);
			if (constraint instanceof Assignment){
			    Assignment assignment = (Assignment) constraint;
			    newSolution = new Solution();
			    newSolution.addBinding(assignment.getVariable(), assignment.getValue());
			}
		    }

		    // otherwise try every solver that is able to handle the constraint set
		    // and take the first solution found
		    if (newSolution == null){
			Solver[] solver = getSolver(constraintSet);
			for (int solverNo = 0; solverNo < solver.length; solverNo++){
			    if (newSolution != null) break;

			    listeners.fireSolverGetSolutionStarted(solverManager, solver[solverNo], constraintSet);

			    // if the last solver is unable to find a solution an exception is thrown
			    try{
				for (SingleConstraint constraint : constraintSet)
				    solver[solverNo].addConstraint(constraint);
				newSolution = solver[solverNo].getSolution();
			    } catch (SolverUnableToDecideException sutde){
				if (solverNo == solver.length - 1)
				    throw sutde;
			    } catch (TimeoutException te){
				if (solverNo == solver.length -1)
				    throw te;
			    }

			    listeners.fireSolverGetSolutionFinished(solverManager, solver[solverNo], constraintSet, newSolution, System.nanoTime() - startTime);
			}
		    }


		    // join the sub solutions found so far
		    if (solution == null)
			// if we are in the first iteration i.e. i==0 we start with a new solution
			solution = newSolution;
		    else
			// otherwise we just join the new found solution with the previous one
			solution = solution.join(newSolution);


		    // if one ConstraintSet is contradictory then the whole system has no solution
		    // and we can stop here
		    if (solution.equals(Solution.NOSOLUTION)){
			break;
//			constraintStack.setSolution(idx, Solution.NOSOLUTION);
//			return Solution.NOSOLUTION;
		    }
		}
	    } catch (IncorrectSolverException ise){
		ise.printStackTrace();
		return null;
	    }
	}

	constraintStack.setSolution(idx, solution);
	return solution;
    }

    /**
     * Tries to find a valid constraint solver for the given set of constraints.
     * @param constraintSet the system of constraints a dedicated solver should be
     * found for.
     * @return the solver that should be able to handle the passed system of
     * conatraints.
     */
    private Solver[] getSolver(SingleConstraintSet constraintSet){
	Solver[] result = solverChooser.getSolvers(constraintSet);
	if (result == null || result.length == 0)
	    throw new InternalError("No appropriate solver found!\n" + constraintSet);
	return result;
    }

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
    public boolean hasSolution() throws SolverUnableToDecideException, TimeoutException{
	if (constraintStack.getSystemCount() == 0){
	    return true;
	}

	int idx = constraintStack.getFirstNoncontradictorySystemIndex();
	while (idx < constraintStack.getSystemCount()){
	    if ( hasSolution(idx) ){
		return true;
	    }

	    idx++;
	}
	return false;
    }

    /**
     * Checks whether a solution exists for the system of constraints stored in
     * the actual constraint stack at the position idx by using the hasSolution
     * methods of the constraint solvers. This method may be a little bit faster
     * when only an assertion about the solvability of a system of constraints
     * should be calculated.
     * @param idx the index of the system of constraints the information about the
     * existence of a solution should be calculated for.
     * @return <i>true</i> if any solution for the given problem exists,
     * <i>false</i> if definitively no solution satisfies the constraints.
     * @throws SolverUnableToDecideException if the used solvers are not able to
     * decide whether a solution exists or not or are not able to find an existing
     * solution instance.
     * @throws TimeoutException if the used algorithms stops because of reaching
     * the specified time limits before being able to decide about the given
     * problem.
     */
    private boolean hasSolution(int idx) throws SolverUnableToDecideException, TimeoutException{
	Solution solutionTmp = constraintStack.getCachedSolution(idx);
	if (solutionTmp != null){
	    boolean result = !solutionTmp.equals(Solution.NOSOLUTION);
	    return result;
	}

	ConstraintSystem system = constraintStack.getSystem(idx);
	if ( system.isContradictory() ){
	    constraintStack.setSolution(idx, Solution.NOSOLUTION);
	    return false;
	} else {
	    boolean result = true;
	    try {
		for (int i = 0; i < system.getConstraintSetCount(); i++){
		    SingleConstraintSet constraintSet = system.getConstraintSet(i);

		    if ( (constraintSet.getConstraintCount() != 1) || !(constraintSet.getConstraint(0) instanceof Assignment) ){
			// if there is at least one non assignment constraint try to solve the set

			Solver[] solver = getSolver(constraintSet);
			for (int solverNo = 0; solverNo < solver.length && result; solverNo++){
			    HasSolutionInformation intermediateResult = null;
			    try {
				for (SingleConstraint constraint : constraintSet)
				    solver[solverNo].addConstraint(constraint);
				intermediateResult = solver[solverNo].hasSolution();
				if (intermediateResult.hasSolution()){
				    // solution for this constraint set is present -> skip to next set
				    break;
				} else {
				    result = false;
				}
			    } catch (SolverUnableToDecideException sutde) {
				if (solverNo == solver.length - 1)
				    throw sutde;
			    } catch (TimeoutException te) {
				if (solverNo == solver.length -1)
				    throw te;
			    } finally {
			    }
			}
		    }

		    if (result == false){
			constraintStack.setSolution(idx, Solution.NOSOLUTION);
			return false;
		    }
		}
	    } catch (IncorrectSolverException ise){
		ise.printStackTrace();
		return false;
	    }
	}
	return true;
    }

    @Override
    public void removeConstraint(){
	constraintStack.removeConstraint();
    }

    private void removeConstraints(int count){
	if (count > 0){
	    constraintStack.removeConstraints(count);
	}
    }

    @Override
    public OldSolverManagerSolver reset(){
	removeConstraints(constraintStack.getSize());
	return this;
    }

    /**
     * 
     * @param solution
     * @return
     */
    protected boolean verifySolution(Solution solution){
	return constraintStack.verifySolution(solution);
    }

}
