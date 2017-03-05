package de.wwu.testtool.solver.tsolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.testtool.conf.SimplexSolverConfig;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.Monomial;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.constraints.StrictInequation;
import de.wwu.muggl.solvers.solver.numbers.DoubleWrapper;
import de.wwu.muggl.solvers.solver.numbers.Fraction;
import de.wwu.muggl.solvers.solver.numbers.DeltaWrapper;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.testtool.tools.Timer;


/**
 * 
 * 
 * @author Marko Ernsting
 *
 */
public class SimplexSolver implements Solver {    

    private NumberFactory numberFactory;
    
    /**
     * Holds the Constraints of the Simplex Solver.
     */
    private Stack<SingleConstraint> constraints = new Stack<SingleConstraint>();
        
    /**
     * To be removed when finished.
     */
    private Logger logger;
    
    /**
     * The simplex tableau.
     */
    private SimplexTableau tableau = new SimplexTableau();
    
    /**
     * Is the current system of constraints satisfiable?
     */
    private boolean feasible;
    
    /**
     * Do we have a feasible assignment which is integer for problem variables of type integer.
     */
    private boolean allVarsInteger;
    
    private boolean stateIsBranchAndBound;

    
    /**
     * Timeout for the maximum running time of {@link #getSolution()} in milliseconds. 
     */
    private long timeout = 0;
    
    /**
     * Timer to track timeouts.
     */
    private Timer timer;

    /**
     * Do we want to derive Gomory Cuts?
     */
    private boolean useGomoryCuts;
    private static final boolean debugUseStrongerGomoryCuts = true;

    /**
     * When doing branch and bound it is desirable to change the selection strategy
     * for non-basic variables to enter the basis.
     * <BR>
     * By prefering additonal variables to enter the basis we have a higher chance of
     * not changing the problem variables values, which would lead to further recursive
     * branch and bound calls.
     */
    private boolean useAdditionalVarPreferredForBranchAndBound;

    /**
     * Threshold for for constraint adjustment in case of rounding errors.
     */
    private double zeroThreshold;
    private boolean useZeroThresholdingForGomoryCuts;
    private boolean useZeroThresholdingForPivoting;
    
    /**
     * Postsolving.
     */
    private boolean usePostSolvingForRoundingErrors;
    private double postSolvingViolatingFactor;    
    private int postSolvingRuns;
    
    /**
     * Use DoubleWrapper. Otherwise Fraction is used.
     */
    private boolean useDoubleWrapper;

    private boolean useIncrementalSolving;
    private boolean useBacktracking;
    
    /**
     * Debugging constants.
     */
    private static final double debugDevianceThreshold = 1E-1;
    private static final double debugAssignmentThresholdPercentage = .5;

 
    /**
     * Setters...
     * @param bool
     */
    public void useBacktracking(boolean bool) {useBacktracking = bool;}
    public void useIncrementalSolving(boolean bool) {useIncrementalSolving = bool;}
    public void usePostSolvingForRoundingErrors(boolean bool) {usePostSolvingForRoundingErrors = bool;}
    public void useGomoryCuts(boolean bool) {useGomoryCuts = bool;}
    public void useAdditionalVarPreferredForBranchAndBound(boolean bool) {useAdditionalVarPreferredForBranchAndBound = bool;}
    public void useDoubleWrapper(boolean bool) {useDoubleWrapper = bool;}

    /**
     * Temporary function to set logger for construction process.
     * @param logger
     */
    public void setDebugLogger(Logger logger){
	this.logger = logger;
	tableau.tableauLogger = logger;
    }
    
    /**
     * Sets the timeout for the {@link #getSolution()} function.
     * @param newTimeout the timeout in nanoseconds.
     */
    public void setTimeout(long newTimeout) {
	timeout = newTimeout;
    }
    
    /**
     * Exists because of compatibility issues.
     * @param manager
     * @return
     */
    public static SimplexSolver newInstance(SolverManager manager){
	return new SimplexSolver();
    }
    
    /**
     * Constructor.
     */
    public SimplexSolver() {
	timeout = SimplexSolverConfig.getInstance().getTimeout();	
	useBacktracking = SimplexSolverConfig.getInstance().getUseBacktracking();
	useIncrementalSolving = SimplexSolverConfig.getInstance().getUseIncrementalSolving();
	useAdditionalVarPreferredForBranchAndBound = SimplexSolverConfig.getInstance().getUseAdditionalVarPreferredForBranchAndBound();
	useGomoryCuts = SimplexSolverConfig.getInstance().getUseGomoryCuts();
		
	zeroThreshold = SimplexSolverConfig.getInstance().getZeroThreshold();
	useZeroThresholdingForGomoryCuts = SimplexSolverConfig.getInstance().getUseZeroThresholdingForGomoryCuts();
	useZeroThresholdingForPivoting = SimplexSolverConfig.getInstance().getUseZeroThresholdingForPivoting();
	usePostSolvingForRoundingErrors = SimplexSolverConfig.getInstance().getUsePostSolvingForRoundingErrors();
	postSolvingViolatingFactor = SimplexSolverConfig.getInstance().getPostSolvingViolatingFactor();    
	postSolvingRuns = SimplexSolverConfig.getInstance().getPostSolvingRuns();
	
	logger = SimplexSolverConfig.getInstance().getLogger();
	tableau.tableauLogger = logger;
	
	if (useDoubleWrapper){
	    numberFactory = DoubleWrapper.ONE;
	} else {	    
	    numberFactory = SimplexSolverConfig.getInstance().getDefaultNumberFactory();
	}
	
	// rework this delta issue.
	DeltaWrapper.setDelta(numberFactory.getOne());
	
	feasible = true;
	allVarsInteger = false;
	
	if (logger.isDebugEnabled()) {
	    logger.debug("SimplexSolver: Instance created.");
	    if (numberFactory instanceof DoubleWrapper) 
		logger.debug("SimplexSolver: Using number factory DoubleWrapper.");
	    else if (numberFactory instanceof Fraction)
		logger.debug("SimplexSolver: Using number factory Fraction.");
	    else
		logger.debug("SimplexSolver: Using number factory UNKNOWN.");
	}
    }
    
    @Override
    public String toString(){
	return tableau.toString();
    }
    
    @Override
    public void addConstraint(SingleConstraint constraint) throws IncorrectSolverException {
	/*
	 * Some safety checks.
	 */
	if ( !(constraint instanceof NumericConstraint) )
	    throw new IncorrectSolverException("Non-numeric constraint not supported:" + constraint);
	if ( ! constraint.isLinear() )
	    throw new IncorrectSolverException("Non-linear constraint bnot supported." + constraint);	
	if (logger.isDebugEnabled()) logger.debug("SimplexSolver: adding constraint " + constraint);
	
	constraints.add(constraint);
	
	try {
	    if (useIncrementalSolving){
		tableau.addConstraint(constraint);
	    } else {
		tableau = new SimplexTableau();
		feasible = false;
		allVarsInteger = false;
		for (SingleConstraint sc : constraints){
		    tableau.addConstraint(sc);
		}
	    }
	} catch (SolverUnableToDecideException e) {
	    // We ignore the exception here because it cannot be handled anyway and throwing
	    // again would require a change in the Solver interface.
	    // This is just for debugging anyway.
	}
	

	feasible = false;
	allVarsInteger = false;

	/*
	 * Gomory Cuts - Constraint is used as separator of cuts.
	 */
	tableau.constraintsGomoryCutsMap.put(constraint, new Stack<NumericVariable>());	    
    }
    

    @Override
    public void addConstraintSet(SingleConstraintSet constraintSet)
	    throws IncorrectSolverException {
	for(SingleConstraint cs : constraintSet) {
	    this.addConstraint(cs);
	}
    }

    @Override 
    public List<Class<? extends Solver>> getRequiredSubsolvers() {
	return null;
    }

    @Override
    public Solution getSolution() throws TimeoutException, SolverUnableToDecideException {

	if (timeout > 0){
	    timer = new Timer(timeout);
	    timer.run();
	} else {
	    timer = new Timer(0);
	}
		
	stateIsBranchAndBound = false;	
	branchAndCut();
	
	if (usePostSolvingForRoundingErrors) {
	    stateIsBranchAndBound = false;
	    postSolvingForRoundingErrors(postSolvingRuns);
	}
	
	if (feasible)
	    return getSolutionForCurrentAssignment();
	else
	    return Solution.NOSOLUTION;
    }
    
    /**
     * Used to deal with rounding errors rendering a solution unsatisfiable with
     * floating point arithmetic.
     * @throws TimeoutException if the timeout for the whole solving process is reached.
     * @throws SolverUnableToDecideException 
     * @see SimplexSolver#setTimeout(long)
     */
    private void postSolvingForRoundingErrors(int runs) throws TimeoutException, SolverUnableToDecideException {
	if (!feasible) return;
	if (runs <= 0) return;
	
	
	boolean oldBehaviour = false;
	
	/*
	 * Find constraint which is unsatisfied due to rounding errors.
	 */
	NumericVariable addVar = null;
	HashMap<NumericVariable, NumberWrapper> oldAddVarsBounds = new HashMap<NumericVariable, NumberWrapper>(); 
	for (SingleConstraint constraint : constraints) {
	    Polynomial poly = ((NumericConstraint) constraint).getPolynomial();
	    
	    double violatingValue = 0;
	    try {
		violatingValue = poly.computeValue(getSolutionForCurrentAssignment()).doubleValue();
	    } catch (IncompleteSolutionException e) {
		logger.error("SimplexSolver: This should never happen.");
		e.printStackTrace();
	    }
	    
	    // check next row for validity
	    if ( violatingValue <= 0 ) continue;

	    addVar = tableau.constraintAdditionalVariablesMap.get(constraint);
	    NumberWrapper upper = tableau.upperBounds.get(addVar);
	    NumberWrapper subValue = numberFactory.getInstance(violatingValue * postSolvingViolatingFactor);
	    
	    // calculate new bound
	    NumberWrapper newUpperBound = upper.sub(subValue);
	    if(logger.isDebugEnabled()) logger.debug("SimplexSolver: Found Rounding Error! Tightening " + addVar + "'s upper bound from " + upper + " to " + newUpperBound);
	    
	    // update its addVar's upper bound (possibly expensive!)
	    tableau.changeUpperBound(addVar, newUpperBound);
	    
	    oldAddVarsBounds.put(addVar, tableau.upperBounds.get(addVar));
	    if (oldBehaviour) break;
	}
	
	// if no rounding errors found
	if (oldAddVarsBounds.size() == 0)
	    return;
	
	// and branchAndCut again to see if a recursive call finds any more rounding errors
	branchAndCut();
	postSolvingForRoundingErrors(runs-1);

	
	/*
	 * And then tighten its constraint.
	 */	
	if ( addVar != null ) {
	    for (NumericVariable var : oldAddVarsBounds.keySet()){
		// revert upper bound
		tableau.changeUpperBound(var, oldAddVarsBounds.get(var));
	    }
	}
    }
    
    /**
     * Returns a solution object for the current assignment.
     * @return
     */
    private Solution getSolutionForCurrentAssignment(){
	if (!feasible ) //  || !allVarsInteger) 
	    return Solution.NOSOLUTION;
	
	Solution solution = new Solution();	
	for (NumericVariable problemVariable : tableau.decisionVariables.keySet()){
	    solution.addBinding(problemVariable, tableau.assignments.get(problemVariable).toNumericConstant(problemVariable.getType()) );
	}
	return solution;
    }
    
    /**
     * Branch & Bound procedure:
     * <ol><li>
     * Solve current tableau using the simplex algorithm.
     * </li><li>
     * Derive as many Gomory Cuts as possible.
     * </li><li>
     * Check if there is problem variable x of type integer with non integral assignments.
     * </li><li>
     * If yes, "Branch & Bound":
     * </br>
     * Branch and bound on integer variable x from above in the following way:
     * <ul><li>
     * create first branch by adding new constraint x <= floor(assignment(x))
     * </li><li>
     * try to solve first branch recursively
     * </li><li>
     * if no integer solution was found, remove constraint from the first branch.
     * Then create second branch by adding new constraint x >= ceil(assignment(x))
     * </li><li>
     * try to solve second branch recursively
     * </li></ul></ol>
     * 
     * Warning: This can lead to infinite recursion for certain constraints if no timeout is given.
     * @throws TimeoutException is thrown when timeout is reached.
     * @throws SolverUnableToDecideException 
     * @see #solve()
     * @see #deriveGomoryCuts()
     */
    private void branchAndCut() throws TimeoutException, SolverUnableToDecideException {
	
	/*
	 * branch and bound can lead to infinite recursion so we check for timeout here
	 */
	if (timer.timeout()) {
	    if (!feasible)
		throw new TimeoutException("Timeout " + timeout + "reached. No solution found.");
	    if (!allVarsInteger)
		throw new TimeoutException("Timeout " + timeout + "reached. No integral solution found.");
	}
	
	/*
	 * first solve the LA relaxation of the MILA problem with the usual simplex
	 */
	feasible = false;
	solve();
	if (!feasible) {
	    if (logger.isDebugEnabled()){
		tableau.debugCheckTableau();
	    }
	    return;
	}
	/*
	 * if there are still problem variables left, whose type is integer
	 * then we need to handle them with branch and bound
	 */
	NumericVariable nonIntegralVariable = null;
	for ( NumericVariable problemVar : tableau.decisionVariables.keySet()) {
	    if ( problemVar.isInteger() && !tableau.assignments.get(problemVar).isInteger()){
		nonIntegralVariable = problemVar;
		break;
	    }
	}
	if ( nonIntegralVariable == null ){
	    if (logger.isDebugEnabled()){
		logger.debug("SimplexSolver: Each integer variable has an integer assignment.");
		tableau.debugCheckTableau();
		debugCheckSolutionRelaxed();
	    }
	    allVarsInteger = true;
	    return;
	}
	
	
	
	if (logger.isDebugEnabled()) logger.debug("SimplexSolver: " + nonIntegralVariable + " not integer!");
	/*
	 * Gomory Cuts
	 */
	Stack<NumericVariable> currentGomoryStack = null;
	NumericVariable lastValidGomoryVar = null;
	if (useGomoryCuts){
	    deriveGomoryCuts();
	    
	    // keep track of last valid cut for later removal of further cuts
	    currentGomoryStack = tableau.constraintsGomoryCutsMap.get(constraints.peek());
	    lastValidGomoryVar = null;
	    if (! currentGomoryStack.isEmpty()) lastValidGomoryVar = currentGomoryStack.peek();
	    
//	    feasible = false;
//	    solve();
//	    if (!feasible) return;
	}
	
	/*
	 * Branch And Bound
	 */
	stateIsBranchAndBound = true;
	NumberWrapper assignment = tableau.assignments.get(nonIntegralVariable);
	NumberWrapper oldLowerBound = tableau.lowerBounds.get(nonIntegralVariable);
	NumberWrapper oldUpperBound = tableau.upperBounds.get(nonIntegralVariable);
	NumberWrapper ceil = assignment.ceil();
	NumberWrapper floor = assignment.floor();
	
	// set u_i <= floor(alpha(x_i))
	if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Setting new upper bound: " + nonIntegralVariable + " <= " + floor);
	tableau.changeUpperBound(nonIntegralVariable, floor);
	if (feasible)
	    branchAndCut();
	if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Resetting upper bound: " + nonIntegralVariable + " <= " + oldUpperBound);
	//  changing this bound does not change the basic assignments, because we lighten the constraints
	tableau.changeUpperBound(nonIntegralVariable, oldUpperBound);
	
	// do we already have an integral solution?
	if (!allVarsInteger) {
	    // set l_i >= ceil(alpha(x_i))	
	    if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Setting new lower bound: " + nonIntegralVariable + " >= " + ceil);
	    tableau.changeLowerBound(nonIntegralVariable, ceil);
	    if (feasible)
		branchAndCut();
	    if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Resetting bound: " + nonIntegralVariable + " >= " + oldLowerBound);
	    tableau.changeLowerBound(nonIntegralVariable, oldLowerBound);
	}
	
	if (useGomoryCuts){
	    while ( lastValidGomoryVar != null && /* !currentGomoryStack.isEmpty() && */ currentGomoryStack.peek() != lastValidGomoryVar ){
		if (logger.isDebugEnabled()) logger.debug("Removing Gomory Cut " + currentGomoryStack.peek());
		tableau.removeRow(currentGomoryStack.pop());
	    }
	    
	}

	
    }
            
    /**
     * This method is used to generate and add Gomory Cuts.
     */
    private void deriveGomoryCuts() {
	SingleConstraint lastConstraint = constraints.get(constraints.size()-1);
	
	if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Trying to find Gomory Cuts");
	
	NextRow:
	for (int i = 0; i < tableau.rowVars.size(); ++i){
	    NumericVariable basicVar = tableau.rowVars.get(i);
	    ArrayList<NumberWrapper> row = tableau.matrix.get(i);
	    if (logger.isDebugEnabled()) logger.debug("SimplexSolver: checking row "+ basicVar+ ": " + row);
	    
	    
	    if ( ! basicVar.isInteger() ) continue;

	    /*
	     * check if row is okay to derive gomory cut
	     */
	    if (logger.isDebugEnabled()) logger.debug("SimplexSolver: ... "+basicVar+" is integer variable.");	    
	    for (int j = 0; j < tableau.columnVars.size(); ++j){
		NumberWrapper a_ij = row.get(j);
		if ( ! a_ij.isZero() ) {
		    NumericVariable nonBasicVar = tableau.columnVars.get(j);
		    NumberWrapper value = tableau.assignments.get(nonBasicVar);
		    if ( ! value.equals(tableau.lowerBounds.get(nonBasicVar)) && ! value.equals(tableau.upperBounds.get(nonBasicVar)) ){
			//if (logger.isDebugEnabled()) logger.debug("SimplexSolver:    " + value + " != " + tableau.lowerBounds.get(nonBasicVar));
			//if (logger.isDebugEnabled()) logger.debug("SimplexSolver:    " + value + " != " + tableau.upperBounds.get(nonBasicVar));
			continue NextRow;
		    }
		}
	    }
	    // this row is fine, go on
	    if (logger.isDebugEnabled()) logger.debug("SimplexSolver: => row fine!");
	    
	    /*
	     * derive gomory cut
	     */
	    HashMap<NumericVariable, NumberWrapper> varNumMap = new HashMap<NumericVariable, NumberWrapper>();
	    HashMap<NumericVariable, NumberWrapper> varNumMap2 = new HashMap<NumericVariable, NumberWrapper>();
	    
	    // the constant is 1
	    DeltaWrapper upper = new DeltaWrapper(numberFactory.getOne());
	    DeltaWrapper upper2 = new DeltaWrapper(numberFactory.getOne());
	    
	    // preparing factors
	    NumberWrapper rest = tableau.assignments.get(basicVar).sub( tableau.assignments.get(basicVar).floor() );
	    rest = numberFactory.getInstance(rest.doubleValue());
	    NumberWrapper oneMinusRest = numberFactory.getOne().sub(rest);
	    
	    if (useZeroThresholdingForGomoryCuts){
		if (oneMinusRest.abs().lessThan(numberFactory.getInstance(zeroThreshold)))
		    return;
		if (rest.abs().lessThan(numberFactory.getInstance(zeroThreshold)))
		    return;
	    }
	    
	    // build the gomory cut constraint
	    for (int j = 0; j < tableau.columnVars.size(); ++j){
		NumericVariable nonBasicVar = tableau.columnVars.get(j);
		NumberWrapper value = tableau.assignments.get(nonBasicVar);
		NumberWrapper a_ij = row.get(j);
		
		NumberWrapper thresh = numberFactory.getInstance(zeroThreshold);
		
		boolean isZero = (a_ij.abs().lessThan(thresh));
		if ( ! isZero ) {
		    if(logger.isDebugEnabled()) logger.debug("a_ij = " + a_ij);
		    
		    NumberWrapper currentLowerBound = tableau.lowerBounds.get(nonBasicVar);
		    NumberWrapper currentUpperBound = tableau.upperBounds.get(nonBasicVar);
		    
		    // are K and J distinct??
		    if (currentLowerBound.equals(value) && currentUpperBound.equals(value)){
			if(logger.isDebugEnabled()) logger.debug("K and J not distinct. Omitting Gomory Cut");
			return;
		    }
		    
		    // J
		    if (currentLowerBound.equals(value) ){
			// J0
			if (nonBasicVar.isInteger()) {
			    NumberWrapper f_i = a_ij.sub(a_ij.floor());
			    if (oneMinusRest.greaterOrEqual(f_i)){
				upper = upper.add( currentLowerBound.mult(f_i).div(oneMinusRest) );
				varNumMap.put(nonBasicVar, f_i.div(oneMinusRest).negate());
			    } else {
				upper = upper.add( currentLowerBound.mult( numberFactory.getOne().sub(f_i).div(rest)) );
				varNumMap.put(nonBasicVar, numberFactory.getOne().sub(f_i).div(rest).negate() );
			    }
			} else {
			    // J+
			    if (a_ij.isGreaterOrEqualZero()) {
				upper = upper.add( currentLowerBound.mult(a_ij).div(oneMinusRest) );
				upper2 = upper2.add( currentLowerBound.mult(a_ij).div(oneMinusRest) );
				varNumMap.put(nonBasicVar, a_ij.div(oneMinusRest).negate());
				varNumMap2.put(nonBasicVar, a_ij.div(oneMinusRest).negate());
			    }
			    // J-
			    if (a_ij.isLessThanZero()) {
				upper = upper.sub( currentLowerBound.mult(a_ij).div(rest) );
				upper2 = upper2.sub( currentLowerBound.mult(a_ij).div(rest) );
				varNumMap.put(nonBasicVar, a_ij.div(rest));
				varNumMap2.put(nonBasicVar, a_ij.div(rest));
			    }
			}
		    }
		    
		    // K
		    if (currentUpperBound.equals(value) ) {
			// K0
			if (nonBasicVar.isInteger()) {
			    NumberWrapper f_i = a_ij.sub(a_ij.floor());
			    if (rest.greaterOrEqual(f_i)){
				upper = upper.add( currentUpperBound.mult(f_i.div(rest)).negate() );
				varNumMap.put(nonBasicVar, f_i.div(rest));
			    } else {
				upper = upper.add( currentUpperBound.mult( numberFactory.getOne().sub(f_i).div(oneMinusRest)).negate() );
				varNumMap.put( nonBasicVar, numberFactory.getOne().sub(f_i).div(oneMinusRest) );
			    }
			}else{
			    // K+
			    if (a_ij.isGreaterOrEqualZero()) {
				upper = upper.sub( currentUpperBound.mult(a_ij).div(rest) );
				upper2 = upper2.sub( currentUpperBound.mult(a_ij).div(rest) );
				varNumMap.put(nonBasicVar, a_ij.div(rest));
				varNumMap2.put(nonBasicVar, a_ij.div(rest));
			    }
			    // K-
			    if (a_ij.isLessThanZero()) {
				upper = upper.add( currentUpperBound.mult(a_ij).div(oneMinusRest) );
				upper2 = upper.add( currentUpperBound.mult(a_ij).div(oneMinusRest) );
				varNumMap.put(nonBasicVar, a_ij.div(oneMinusRest).negate());
				varNumMap2.put(nonBasicVar, a_ij.div(oneMinusRest).negate());
			    }
			}
		    }
		    
		    if (!currentLowerBound.equals(value) && !currentUpperBound.equals(value))
			throw new RuntimeException("Error in row determination for Gomory Cuts.");
		}		
	    }
	    
	    /*
	     * add gomory cut to the tableau
	     */
	    NumericVariable s = NumericVariable.createInternalVariable(Expression.DOUBLE);
	    DeltaWrapper lower_delta = new DeltaWrapper(DeltaWrapper.Delta.NEGATIVE_INFINITY);
	    DeltaWrapper upper_delta;
	    if (debugUseStrongerGomoryCuts) {
		upper_delta = upper.negate();
	    }else{
		upper_delta = upper2.negate();
	    }

	    if (logger.isDebugEnabled()) {
		logger.debug("SimplexSolver: Gomory Cut: " + varNumMap + " = " + s);
		logger.debug("SimplexSolver: ... with " + s + " <= " + upper_delta);
	    }
	    
	    if (logger.isDebugEnabled()) debugCheckSolutionRelaxed();	    	    
	    
	    tableau.addRow(varNumMap, s, lower_delta, upper_delta);
	    
	    /*
	     * link last constraint to this cut to get the cut
	     * removed together with the constraint
	     * 
	     * this gets far more complicated if we support disabling/enabling
	     * of arbitrary constraints. it should be enough to support just the
	     * enabling and disabling of the last constraint inserted.
	     * then gomory cuts are easy.
	     */
	    Stack<NumericVariable> currentList = tableau.constraintsGomoryCutsMap.get(constraints.peek());
	    if ( currentList == null){
		currentList = new Stack<NumericVariable>();
		tableau.constraintsGomoryCutsMap.put(lastConstraint, currentList);
	    }

	    currentList.add(s);
	    
	    break;
	}
    }

    /**
     * This procedure implements the dual simplex solving method. It is called by
     * the meta solving branch and bound algorithm implemented in {@link #branchAndCut()}.
     * </br></br>
     * After calling this method the satisfiability can be checked by reading {@link #feasible}.
     * @return feasibility
     * @throws SolverUnableToDecideException 
     * @see feasible
     * @see branchAndCut
     */
    private void solve() throws TimeoutException, SolverUnableToDecideException {
	
	/*
	 *  TODOME:
	 *  Check more pivoting strategies. 
	 *  Starred strategies can already be chosen.
	 *  
	 *  Basic:
	 *   - implicitVariableOrder* 			(safe! always back up to this at a certain point to prevent infinite loops.)
	 *   - tableauOrder* 				(fastest)
	 *   - preferDecisionVariable* 			()
	 *   - biggestPivotElement 			(expensive, all possible pivots have to be checked)
	 *  
	 *  NonBasic:
	 *   - implicitVariableOrder* 			(safe! always back up to this at a certain point to prevent infinite loops.)
	 *   - tableauOrder* 				(fastest)
	 *   - additionalVarPreferredForBranchAndBound*	(prevents decision variables from loosing integer ass.)
	 *   - notBoundedVarPreferred 			(move variables into basic that cannot violate their bounds)
	 *   - biggestPivotElement 			(numerically most stable, expensive[but less than for suit. basic var])
	 */

	// No constant for this. 
	// Changing should not be necessary and if, only with good knowledge of algorithm
	int maxLoops = constraints.size() * 10;
	int loopCounter = 0;
	// start with heuristics
	boolean loopSorted = false;

	
	/*
	 * if not feasible do pivoting until we are
	 */
	feasible = false;
	while ( !feasible ){
	    loopCounter++;
	    
	    loopSorted = (loopCounter > maxLoops);
	    if (logger.isDebugEnabled()) 
		if(loopCounter == maxLoops) {
		    logger.debug("SimplexSolver: Pivoting limit of "+maxLoops+" reached. Falling back to safe pivoting with fixed order variable selection.");
		}
	    
	    /*
	     * Timeouts!
	     */
	    if (timer.timeout()) 
		throw new TimeoutException("SimplexSolver: Timeout " + timeout + " reached. No solution found.");
	   	    	    
	    /*
	     * find suitable basic variable to swap out
	     */
	    if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Searching suitable basic variable:");
	    NumericVariable suitableBasicVariable = null;
	    int suitableBasicRow = 0;
	    // Which pivoting strategy?
	    Collection<NumericVariable> rowVarsLoop = (loopSorted) ? tableau.rowVarsSorted : tableau.rowVars;

	    for(NumericVariable basicVariable : rowVarsLoop) {
		//current row number
		int i = tableau.rowVars.indexOf(basicVariable);
		if (logger.isDebugEnabled()) logger.debug("SimplexSolver: ...trying " + basicVariable);
		// if basic variable is _suitable_ stop search
		if (    tableau.assignments.get(basicVariable).lessThan(tableau.lowerBounds.get(basicVariable)) 
		     || tableau.assignments.get(basicVariable).greaterThan(tableau.upperBounds.get(basicVariable)) ) {
		    suitableBasicVariable = basicVariable;
		    suitableBasicRow = i;		   		    
		    break;
		} 
		// current basicVariable was not suitable, try next
	    }
	    if( suitableBasicVariable == null ) {
		// if no suitable basic variable exists, the current assignment is satisfying all the constraints
		if (logger.isDebugEnabled()) 
			logger.debug("SimplexSolver: No suitable basic variable found. --> Solvable!");
		feasible = true;
		break;
	    }
	    if (logger.isDebugEnabled()) 
		logger.debug("SimplexSolver: "+suitableBasicVariable+" is good to leave the basis!");
	    
	    /*
	     * find suitable non-basic variable to swap out
	     */
	    if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Searching suitable non-basic variable:");
	    // we need the following three things:
	    NumericVariable suitableNonBasicVariable = null;	    
	    NumberWrapper newValueForBasicVariable = null;
	    int suitableNonBasicColumn = 0;
	    boolean searchingForAdditionalVar = false;
	    // there are two different preconditions which lead to different new assignment values
	    boolean basicVariableLessThanLowerBound = tableau.assignments.get(suitableBasicVariable).lessThan(tableau.lowerBounds.get(suitableBasicVariable));
	    // Which pivoting strategy?
	    Collection<NumericVariable> columnVarsLoop = (loopSorted) ? tableau.columnVarsSorted : tableau.columnVars;
	    
	    for(NumericVariable nonBasicVar : columnVarsLoop) {
		// current column number
		int i = tableau.columnVars.indexOf(nonBasicVar);
		
		if (suitableNonBasicVariable != null){ // we have a suitable non-basic variable 
		    if (useAdditionalVarPreferredForBranchAndBound  && stateIsBranchAndBound  && !loopSorted) {
			if (tableau.additionalVariables.contains(suitableNonBasicVariable)){
			    break;
			}
			if (!searchingForAdditionalVar) {
			    if(logger.isDebugEnabled()) logger.debug("SimplexSolver: Suitable non-basic variable is decision integer variable at integer bound. ");
			    searchingForAdditionalVar = true;
			}
			if (!tableau.additionalVariables.contains(nonBasicVar))
			    continue;
		    } else {
			// we have a non basic variable and do not care whether it is additional or not.
			break; 
		    }
		} else {
		    // find a suitable non-basic variable
		}
		
		if (logger.isDebugEnabled()) logger.debug("SimplexSolver: ...trying " + nonBasicVar);
		
		NumberWrapper potentialPivotElement = tableau.getElement(suitableBasicRow, i); 
		NumberWrapper valueOfNonBasicVariable = tableau.assignments.get(nonBasicVar); 
		NumberWrapper upperBoundOfNonBasicVariable = tableau.upperBounds.get(nonBasicVar);
		NumberWrapper lowerBoundOfNonBasicVariable = tableau.lowerBounds.get(nonBasicVar);
		if ( basicVariableLessThanLowerBound ) {
		    if(    ( potentialPivotElement.isGreaterThanZero() && valueOfNonBasicVariable.lessThan(upperBoundOfNonBasicVariable))
		        || ( potentialPivotElement.isLessThanZero() && valueOfNonBasicVariable.greaterThan(lowerBoundOfNonBasicVariable)) ) {
			suitableNonBasicVariable = nonBasicVar;
			suitableNonBasicColumn = i;
			newValueForBasicVariable = tableau.lowerBounds.get(suitableBasicVariable);
			//break; //this is done in the next round during the additional-var-test
		    }
		} else {
		    if(    ( potentialPivotElement.isGreaterThanZero() && valueOfNonBasicVariable.greaterThan(lowerBoundOfNonBasicVariable))
			|| ( potentialPivotElement.isLessThanZero() && valueOfNonBasicVariable.lessThan(upperBoundOfNonBasicVariable)) ) {
			suitableNonBasicVariable = nonBasicVar;
			suitableNonBasicColumn = i;
			newValueForBasicVariable = tableau.upperBounds.get(suitableBasicVariable);
			//break;
		    }
		}
		// current nonBasicVariable was not suitable -> try next
	    }
	    if( suitableNonBasicVariable == null ) {
		// if no suitable variable exists there is no solution to this constraint system
		if (logger.isDebugEnabled()) logger.debug("SimplexSolver: => no suitable non-basic variable found. Unsatisfiable!");
		feasible = false;
		return;
	    } else {
		if (logger.isDebugEnabled()) logger.debug("SimplexSolver: "+suitableNonBasicVariable + " is good to enter the basis!");		    
	    }
	    
	    /*
	     * update assignments of suitable non-basic variable and all basic variables accordingly
	     */
	    // increasing/decreasing the old non-basic variable by theta
	    NumberWrapper theta = newValueForBasicVariable.sub(tableau.assignments.get(suitableBasicVariable)).div(tableau.getElement(suitableBasicRow, suitableNonBasicColumn));	    
	    // add theta to suitableNonBasicVariable's assignment
	    tableau.assignments.put(suitableNonBasicVariable, tableau.assignments.get(suitableNonBasicVariable).add(theta));	    
	    // set suitableBasicVariable's assignment to its already determined value (upper/lower bound)
	    tableau.assignments.put(suitableBasicVariable, newValueForBasicVariable);
	    // update all the the other basic variable's assignments according to change of new non-basic variable
	    for (int i = 0; i < tableau.rowVars.size(); ++i) {
		NumericVariable basicVariable = tableau.rowVars.get(i);
		if ( basicVariable != suitableBasicVariable )
		    tableau.assignments.put(basicVariable, tableau.assignments.get(basicVariable).add( theta.mult(tableau.getElement(i, suitableNonBasicColumn)) ) );
	    }
	    
	    /*
	     * pivoting
	     */
	    tableau.pivot(suitableBasicRow, suitableNonBasicColumn);
	    
	    if (logger.isDebugEnabled()) logger.debug("Pivot " + suitableBasicRow + " with " + suitableNonBasicColumn);
	}
	
	/*
	 * updating the delta value for the infinitesimal parameter
	 */
	tableau.updateDelta();
	
	return;
    }

    /**
     * Checks the solution contained in the current assignments.
     * 
     * Evaluates all polynmoials of the constraints. Returns true if all results are less than zeroThreshold
     * 
     * @return 
     */
    private boolean debugCheckSolutionRelaxed(){
	Solution solution = getSolutionForCurrentAssignment();
	logger.debug("SimplexSolver:     assignments: " + tableau.assignments);
	logger.debug("SimplexSolver:     Solution: " + solution);
	boolean solutionCorrect = true;
	try {
	    for (SingleConstraint constraint : constraints) {
		Polynomial poly = ((NumericConstraint) constraint).getPolynomial();
		NumberWrapper result = poly.computeValue(solution); 
		if ( result.isGreaterThanZero() ) {
		    logger.debug("SimplexSolver:     Inserting solution into " + poly + " -> solution: " + poly.computeValue(solution).doubleValue() );
		    if(result.abs().greaterThan(result.getFactory().getInstance(zeroThreshold))){
			solutionCorrect = false;
		    }
		} else if (result == result.getFactory().getZero() && constraint instanceof StrictInequation){
		    solutionCorrect = false;
		}
	    }
	} catch (IncompleteSolutionException e) {
	    logger.error("SimplexSolver: Solution Incomplete. This should never happen. Throwing RuntimeException.");
	    throw new RuntimeException("Incomplete Solution detected.");
	}
	
	return solutionCorrect;
    }
    
    
    @Override
    public boolean handlesEquations() {	return true; }

    @Override
    public boolean handlesIntegerEquations() { return true; }

    @Override
    public boolean handlesNonlinearProblems() {	return false; }

    @Override
    public boolean handlesNumericProblems() { return true; }

    @Override
    public boolean handlesStrictInequalities() { return true; }

    @Override
    public boolean handlesStrictIntegerInequalities() { return true; }

    @Override
    public boolean handlesWeakInequalities() { return true; }

    @Override
    public boolean handlesWeakIntegerInequalities() { return true; }

    @Override
    public HasSolutionInformation hasSolution() throws IncorrectSolverException, TimeoutException, SolverUnableToDecideException {
	return new HasSolutionInformation(getSolution());
    }

    @Override
    public void removeConstraint() {
	if (logger.isDebugEnabled()) logger.debug("SimplexSolver: Removing Constraint: " + constraints.peek());
	
	SingleConstraint constraint = constraints.pop();
		    
	try {
	    if (useBacktracking){
		tableau.removeConstraint(constraint);
	    } else {
		tableau = new SimplexTableau();
		for (SingleConstraint sc : constraints){
		    tableau.addConstraint(sc);
		}
	    }
	} catch (SolverUnableToDecideException e) {
	    // We ignore the exception here because it cannot be handled anyway and throwing it
	    // again would require a change in the Solver interface.
	    // This is just for debugging anyway.
	    throw new RuntimeException();
	}	
    }

    @Override
    public SimplexSolver reset() {
	SimplexSolver newSolver = new SimplexSolver(); 
	
	/*
	 * Transferring configuration.
	 */
	newSolver.useBacktracking(useBacktracking);
	newSolver.useIncrementalSolving(useIncrementalSolving);
    	newSolver.usePostSolvingForRoundingErrors(usePostSolvingForRoundingErrors);
    	newSolver.useGomoryCuts(useGomoryCuts);
    	newSolver.useAdditionalVarPreferredForBranchAndBound(useAdditionalVarPreferredForBranchAndBound);
    	newSolver.useDoubleWrapper(useDoubleWrapper);
	
    	newSolver.setDebugLogger(logger);
    	newSolver.setTimeout(timeout);
    	
    	newSolver.tableau.tableauLogger = tableau.tableauLogger;
    	
	return newSolver;
    }

    @Override
    public String getName() {
	return "SimplexSolver";
    }
    
    /**
     * The Simplex Tableau.
     * </br>
     * Implementation uses ArrayLists for the matrix to support incremental adding and removal
     * of rows.
     * 
     * @author Marko Ernsting
     *
     */
    protected class SimplexTableau {
	/**
	 * The tableau matrix.
	 */
	private ArrayList<ArrayList<NumberWrapper>> matrix = new ArrayList<ArrayList<NumberWrapper>>();;
		
	/**
	 * Variables added as additional variables.
	 */
	private LinkedHashSet<NumericVariable> additionalVariables = new LinkedHashSet<NumericVariable>();

	/**
	 * Stores the valuations of the variables.
	 */
	private HashMap<NumericVariable, NumberWrapper> assignments = new HashMap<NumericVariable, NumberWrapper>();
	
	/**
	 * Stores the assignments right befor adding of constraints.
	 * <BR><BR>
	 * Not needed under normal circumstances. But values in the tableau increase heavily when
	 * not restoring old assignments after removal of constraints, especially when getting 
	 * unsolvable.
	 */
	private Stack<HashMap<NumericVariable, NumberWrapper>> assignmentsStack = new Stack<HashMap<NumericVariable, NumberWrapper>>();

	/**
	 * This array stores the mapping between rows and basic variables.
	 */
	private ArrayList<NumericVariable> rowVars = new ArrayList<NumericVariable>();
	
	/**
	 * Sorted set of (basic) row variables.
	 */
	private TreeSet<NumericVariable> rowVarsSorted = new TreeSet<NumericVariable>();
	
	/**
	 * Which NumericVariable is connected to which constraint.
	 */
	private HashMap<SingleConstraint, NumericVariable> constraintAdditionalVariablesMap = new HashMap<SingleConstraint, NumericVariable>();

	/**
	 * Maps constraints to additional Variables including the variables of gomory cuts
	 * which have been generated while the constraint was on top of the stack.
	 */
	private HashMap<SingleConstraint, Stack<NumericVariable>> 
		constraintsGomoryCutsMap = new HashMap<SingleConstraint, Stack<NumericVariable>>();

	/**
	 * List containing all the variables whose assignment needs to be integral.
	 */
	private LinkedList<NumericVariable> integralVariables = new LinkedList<NumericVariable>();

	/**
	 * Lower bounds for variables used in pivoting selection.
	 */
	private HashMap<NumericVariable, NumberWrapper> lowerBounds = new HashMap<NumericVariable, NumberWrapper>();

	/**
	 * This array stores the mapping between columns and non-basic variables.
	 */
	private ArrayList<NumericVariable> columnVars = new ArrayList<NumericVariable>();

	/**
	 * Sorted set of (basic) column variables.
	 */
	private TreeSet<NumericVariable> columnVarsSorted = new TreeSet<NumericVariable>();
	
	/**
	 * The variables coming directly from the problem.
	 */
	private LinkedHashMap<NumericVariable, Integer> decisionVariables = new LinkedHashMap<NumericVariable, Integer>();

	/**
	 * Upper bounds for variables used in pivoting selection.
	 */
	private HashMap<NumericVariable, NumberWrapper> upperBounds = new HashMap<NumericVariable, NumberWrapper>();

	/**
	 * The Logger for the tableau.
	 */
	private Logger tableauLogger = TesttoolConfig.getLogger();;
	
	/**
	 * Constructor
	 */
	public SimplexTableau() {
	}
	
	/**
	 * Removes a row (i.e. a constraint) from the tableau matrix.
	 * </br></br>
	 * Each constraint is linked to exactly one additional variable. If this variable is in the basis the row
	 * can be deleted without further care. If it is not we have to pivot to bring it into the basis and remove the
	 * row afterwards.
	 * </br></br>
	 * Removes all the constraint's gomory cuts according to {@link #constraintAdditionalVariablesMap} as well.
	 * <br></br>
	 * Each variable which is not used anymore after deletion of the given constraint is removed
	 * from bookkeeping and by making them non-basic (if not already) their columns are removed
	 * from the matrix as well.
	 * @param constraint the Constraint to remove.
	 * @throws SolverUnableToDecideException 
	 */
	public void removeConstraint(SingleConstraint constraint) throws SolverUnableToDecideException {
	    NumericVariable additionalVar = constraintAdditionalVariablesMap.remove(constraint);
	    if (additionalVar == null) throw new RuntimeException("SimplexTableau: Constraint " + constraint + " is not part of the constraint system." );
	    if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexTableau: Removing constraint with additional variable: " + additionalVar);

	    
	    /*
	     * First remove the concerning row from the tableau.
	     */
	    removeRow(additionalVar);
	    
	    /*
	     * Gather unused variables and remove them from bookkeeping.
	     */
	    // decrement variable counter for all variables in this constraint
	    HashSet<NumericVariable> variables = new HashSet<NumericVariable>();
	    constraint.collectNumericVariables(variables);
	    for ( NumericVariable var : variables ) {
		decisionVariables.put(var, decisionVariables.get(var) - 1);
	    }
	    // build list with all variables (columns) which are not used any more
	    LinkedList<NumericVariable> toRemove = new LinkedList<NumericVariable>();
	    for ( Entry<NumericVariable, Integer> entry: decisionVariables.entrySet() ) {
		if ( entry.getValue() == 0) {
		    toRemove.add(entry.getKey());    
		}
	    }
	    for (NumericVariable var : toRemove) {
		// need to remove these after the loop because the iterator supports no deletion
		decisionVariables.remove(var);
		if (var.isInteger()) integralVariables.remove(var);
	    }
	    
	    
	    /*
	     * Remove unused columns from the tableau.
	     */
	    if (! toRemove.isEmpty()) { 
		if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexTableau: Removing columns for variables: "+toRemove+".");
		
		// make basic variables non-basic
		for (int row = 0; row < rowVars.size(); ++row){
		    if( toRemove.contains( rowVars.get(row)) ) {
			if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexTableau: "+rowVars.get(row)+" is basic. => Pivoting.");
			NumberWrapper maxElement = numberFactory.getZero();
			int maxCol = 0;
			for (int col = 0; col < columnVars.size(); ++col) {
			    NumericVariable currentNonBasicVariable = columnVars.get(col);
			    if ( !toRemove.contains( currentNonBasicVariable ) && getElement(row, col).greaterThan(maxElement) ) {
				maxElement = getElement(row, col);
				maxCol = col;
			    }
			}
			if (maxElement.isZero()){
			    tableauLogger.error("SimplexTableau: Numerical Errors! Complete column zero. No row to pivot!");
			    throw new SolverUnableToDecideException("SimplexTableau: Numerical Errors! Complete Column zero. No row to pivot!");
			}
			if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexTableau: Pivoting basic "+rowVars.get(row)+" and nonBasic "+columnVars.get(maxCol)+".");
			pivot(row, maxCol);
		    }
		}

		//delete columns
		int lastElement = columnVars.size() - 1;
		for (int i = lastElement; i >= 0 ; --i){
		    if ( toRemove.contains( columnVars.get(i)) ) {
			if (tableauLogger.isDebugEnabled()) tableauLogger.debug("Deleting column for "+columnVars.get(i));
			NumericVariable removeVar = columnVars.get(i);

			// remove the variable
			toRemove.remove(removeVar);
			assignments.remove(removeVar);
			lowerBounds.remove(removeVar);
			upperBounds.remove(removeVar);

			// and its column
			NumericVariable varToReplace = columnVars.get(i);
			if (i < lastElement){
			    // swap column with last one and delete the last column
			    for (int j = 0; j < matrix.size(); ++j){
				ArrayList<NumberWrapper> currentRow = matrix.get(j);
				currentRow.set(i, currentRow.remove(lastElement));
			    }
			    columnVars.set(i, columnVars.remove(lastElement));
			    columnVarsSorted.remove(varToReplace);
			} else {
			    // delete the last column
			    for (int j = 0; j < matrix.size(); ++j){
				matrix.get(j).remove(lastElement);
			    }
			    columnVarsSorted.remove(varToReplace);
			    columnVars.remove(lastElement); //i
			}    
			lastElement--;
		    }
		}
	    }
	    
	    if (tableauLogger.isDebugEnabled()) debugCheckTableau();
	    
	    
	    /*
	     *  if row was added to infeasible tableau then there can be bound violating non-basic variables now.
	     */
	    
	    HashMap<NumericVariable, NumberWrapper> oldAssignments = assignmentsStack.pop(); 
	    if (oldAssignments == null){
		oldAssignments = new HashMap<NumericVariable,NumberWrapper>();
		for (NumericVariable colVar : columnVars){
		    oldAssignments.put(colVar, assignments.get(colVar));
		}
		for (int i = 0; i < matrix.size(); ++i){
		    NumericVariable rowVar = rowVars.get(i);
		    NumberWrapper value = assignments.get(rowVar).getFactory().getZero();
		    for (int j=0; j < matrix.get(0).size(); ++j){
			NumericVariable columnVar = columnVars.get(j);
			NumberWrapper currentCoeff = matrix.get(i).get(j);
			if (! currentCoeff.abs().lessThan(numberFactory.getInstance(zeroThreshold))){
			    value.add(assignments.get(columnVar).mult(currentCoeff));
			}
		    }
		    oldAssignments.put(rowVar, value);
		}
		assignments = oldAssignments;
	    } else {
		assignments =  oldAssignments;
	    }
	    
	    
	    /*
	     * Remove the constraint's Gomory Cuts.
	     */
	    Stack<NumericVariable> cuts = constraintsGomoryCutsMap.remove(constraint);
	    if (cuts == null) return;
	    for (NumericVariable gomoryAdditionalVar : cuts) {
		removeRow(gomoryAdditionalVar);
	    }
	    
	    if (tableauLogger.isDebugEnabled()) debugCheckTableau();
	}
	
	/**
	 * Removes the row for the given additional variable.
	 * <BR><BR>
	 * The additional variable is made basic if it is not yet. Then the corresponding row is
	 * removed from the matrix.
	 * @param additionalVar the additional variable for this Gomory Cut.
	 * @throws SolverUnableToDecideException 
	 */
	private void removeRow(NumericVariable additionalVar) throws SolverUnableToDecideException{
	    
	    /*
	     * Bring the additional variable into the basis to be able to remove the row.
	     */
	    int row = rowVars.indexOf(additionalVar);
	    if ( row == -1 ) {
		row = makeBasic(additionalVar);
	    }
	    
	    /*
	     * Bring row to the bottom and remove it; remove the constraint's additional variable and its bounds.
	     */
	    NumericVariable varToRemove = rowVars.get(row);
	    if (row < matrix.size()-1){
		matrix.set(row, matrix.remove(matrix.size()-1));
		rowVarsSorted.remove(varToRemove);
		rowVars.set(row, rowVars.remove(rowVars.size()-1));
	    } else {
		matrix.remove(matrix.size()-1);
		rowVarsSorted.remove(varToRemove);
		rowVars.remove(rowVars.size()-1);
	    }
	    additionalVariables.remove(additionalVar);
	    assignments.remove(additionalVar);
	    lowerBounds.remove(additionalVar);
	    upperBounds.remove(additionalVar);
	}
	
	/**
	 * Returns the desired element of the tableau matrix.
	 * </br>
	 * Warning: No safety checks.
	 * @param row
	 * @param column
	 * @return matrix[row, column]
	 */
	private NumberWrapper getElement(int row, int column){
	    return matrix.get(row).get(column);
	}
	
	/**
	 * - polynomials are already normalized L <> 0
	 * - introduce new additional variable s_i, add them to the additionalVariables
	 * - add new constraints s <> L.constant and (L.constant = 0) - s_i = 0
	 * @throws SolverUnableToDecideException 
	 */
	@SuppressWarnings("unchecked")
	public void addConstraint(SingleConstraint constraint) throws SolverUnableToDecideException{
	    /*
	     * Build the variables-to-coefficients map and keep track of problem variable use.
	     */
	    TreeMap<NumericVariable, NumberWrapper> varNumMap = new TreeMap<NumericVariable, NumberWrapper>();
	    Polynomial poly = ((NumericConstraint) constraint).getPolynomial();
	    for (Monomial monomial : poly.getMonomials()){
		NumberWrapper coeff = poly.getCoefficient(monomial);

		// Note: Polynomial uses Fraction hard coded
		if (numberFactory instanceof DoubleWrapper){
		    coeff = numberFactory.getInstance(coeff.doubleValue());
		}		
		HashSet<NumericVariable> tempVars = new HashSet<NumericVariable>();
		monomial.collectNumericVariables(tempVars);
		// there will be only one variable in the linear case
		NumericVariable var = tempVars.iterator().next();
		// add variable and its coefficient to current row
		varNumMap.put(var, coeff);
		if(var.isInteger()) {
		    integralVariables.add(var);
		}

		// we keep track of the problem variables to delete them if they are not used any more
		Integer usageCount = decisionVariables.get(var);
		if (usageCount != null) {
		    decisionVariables.put(var, usageCount + 1);
		} else {
		    decisionVariables.put(var, 1);
		}
	    }


	    /*
	     * Bounds for the new additional variable, deal with delta values.
	     */
	    NumberWrapper constant = poly.getConstant();
	    if (poly.getConstant() != null) { 
		constant = constant.negate();
		if (numberFactory instanceof DoubleWrapper)
			constant = numberFactory.getInstance(constant.doubleValue());
	    }
	    else
		constant = numberFactory.getZero();
	    
	    
	    
	    DeltaWrapper upper;
	    if (constraint.isStrictInequation()){
		if (logger.isDebugEnabled()) logger.debug("... strict inequation --> Using delta values.");
		upper = new DeltaWrapper(constant, numberFactory.getOne().negate());
	    } else {
		upper = new DeltaWrapper(constant, numberFactory.getZero());
	    }

	    DeltaWrapper lower;
	    if ( constraint.isEquation() ) {
		lower = new DeltaWrapper(constant);
	    } else {
		lower = new DeltaWrapper(DeltaWrapper.Delta.NEGATIVE_INFINITY);
	    }

	    /*
	     * Keep track of the new additional variable
	     */
	    NumericVariable s = NumericVariable.createInternalVariable(Expression.DOUBLE);
	    constraintAdditionalVariablesMap.put(constraint, s);
	    additionalVariables.add(s);

	    /*
	     * Push the assignments for later retrieval in case of constraint removal.
	     */
	    if (feasible){
		assignmentsStack.add((HashMap<NumericVariable, NumberWrapper>) assignments.clone());
	    }else {
		assignmentsStack.add(null);
	    }

	    if (tableauLogger.isDebugEnabled()) debugCheckTableau();
	    /*
	     * Finally add the row to the tableau and the constraint to bookkeeping.
	     */
	    addRow(varNumMap, s, lower, upper);
	    
	    if (tableauLogger.isDebugEnabled()){
		tableauLogger.debug("SimplexTableau: Added constraint " + constraint);
		debugCheckTableau();
	    }
	}
	
	/**
	 * Adds a row to the tableau matrix incrementally.
	 * </br>
	 * Constraint + s = 0,  lowerBound <= s <=  upperBound
	 * </br>
	 * A new row is built by using the order of variables already exiting in the tableau.
	 * Then any new variables coefficients are added at the end of the row as well as -1 for the new additional variable
	 * connected to this row. The other rows are filled with zeros in these new columns.
	 * </br>
	 * Then the new row is updated by gaussian elimination steps to regain a valid tableau.
	 * </br>
	 * Finally the bookkeeping for the Basis and NonBasis is done.
	 * 
	 * @param varCoeffMap mapping between variables and their coefficients.
	 * @param s artificial variable.
	 * @param l lower bound for s.
	 * @param u upper bound for s.
	 * @param constraint 
	 */
	private void addRow(Map<NumericVariable,NumberWrapper> varCoeffMap, 
		NumericVariable s,
		DeltaWrapper l,
		DeltaWrapper u){
	    
	    /*
	     * Build a new row in unpacked form (i.e. add basic columns) to do Gaussian Elimination on this row. 
	     */
	    ArrayList<NumberWrapper> newRow = new ArrayList<NumberWrapper>(rowVars.size() + columnVars.size() + 3);
	    for (int i = 0; i < columnVars.size(); ++i) {
		NumericVariable var = columnVars.get(i);
		NumberWrapper value = (varCoeffMap.containsKey(var)) ? varCoeffMap.remove(var) : numberFactory.getZero();
		newRow.add(i, value );
	    }
	    for (int i = 0; i < rowVars.size(); ++i) {
		NumericVariable var = rowVars.get(i);
		NumberWrapper value = (varCoeffMap.containsKey(var)) ? varCoeffMap.remove(var) : numberFactory.getZero();
		newRow.add( value );
	    }
	    
	    /*
	     * Gaussian Elimination on the new row to get 0 in all the current basic columns of the new row.
	     */
	    for (int i = columnVars.size()+rowVars.size()-1; i >= columnVars.size(); --i) {
		NumberWrapper pivotElement = newRow.get(i);
		if ( pivotElement.isZero() ) continue;
		
		int rowOfCurrentBasicVariable = i - columnVars.size();
		for( int j = 0; j < columnVars.size(); ++j) {
		    NumberWrapper elementOfBasicRow = getElement(rowOfCurrentBasicVariable, j);
		    // remember: omitted columns contain -1 and 0, therefore it is "add" here
		    NumberWrapper newValue = newRow.get(j).add( elementOfBasicRow.mult(pivotElement) ); 
		    newRow.set(j, newValue);
		}
		newRow.set(i, numberFactory.getZero() );
	    }
	    // finished. now remove helper elements at end of row.
	    for (int i = newRow.size() - 1; i >= columnVars.size(); --i ){
		newRow.remove(i);
	    }
	    
	    /*
	     * Add possibly existing new variables to the tableau matrix and to the new 
	     */
	    // add new columns to the tableau.
	    for( ArrayList<NumberWrapper> row : matrix ){
		// columns for problem variables which remain
		for (int i = 0 ; i < varCoeffMap.size(); ++i ) {
		    row.add(row.size(), numberFactory.getZero());
		}
	    }
	    // add new columns to new row and do bookkeeping for them
	    for (Map.Entry<NumericVariable, NumberWrapper> entry : varCoeffMap.entrySet()) {
		NumericVariable newProblemVariable = entry.getKey();
		NumberWrapper newProblemVariableCoeff = entry.getValue();

		// add value to the end of the row
		newRow.add( newRow.size(), newProblemVariableCoeff );

		// bookkeeping for new problem variables
		columnVars.add(newProblemVariable);
		columnVarsSorted.add(newProblemVariable);
		assignments.put( newProblemVariable, new DeltaWrapper(numberFactory.getZero()) );
		lowerBounds.put( newProblemVariable, new DeltaWrapper(DeltaWrapper.Delta.NEGATIVE_INFINITY) );
		upperBounds.put( newProblemVariable, new DeltaWrapper(DeltaWrapper.Delta.POSITIVE_INFINITY) );
	    }
	    
	    /*
	     *  Finally add the row to the matrix
	     */
	    matrix.add(newRow);
	    if (tableauLogger.isDebugEnabled()) tableauLogger.debug("New row added: "+newRow);
	    if (tableauLogger.isDebugEnabled()) tableauLogger.debug("... with bounds "+l+" <= "+s+" <= "+u);
	    
	    /*
	     * Add the constraint's additional variable to bookkeeping.
	     */
	    rowVars.add(s);
	    rowVarsSorted.add(s);
	    lowerBounds.put(s, l );
	    upperBounds.put(s, u );
	    // calculate the right assignment for the new basic additional variable
	    NumberWrapper value = new DeltaWrapper(numberFactory.getZero());
	    for (int i = 0; i < columnVars.size(); ++i){
		value = value.add( assignments.get(columnVars.get(i)).mult( newRow.get(i) ) ); 
	    }
	    assignments.put(s, value);
	}
	
	/**
	 * The pivoting function.
	 * </br></br>
	 * Do a pivoting step by bringing a basic variable out of the basis and a non-basis
	 * variable into the basis.
	 * @param row
	 * @param column
	 * @throws SolverUnableToDecideException 
	 */
	public void pivot(int row, int column) throws SolverUnableToDecideException{
	    if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexTableau: Pivoting: row " + row + " and column " + column + ".");
	    
	    ArrayList<NumberWrapper> oldBasicRow = matrix.get( row );
	    NumberWrapper oldPivotElement = oldBasicRow.get(column);
	    
	    /*
	     *  update pivot row
	     */
	    
	    // factor to get -1 at the pivoting position
	    NumberWrapper minusOneDivOldPivot = numberFactory.getMinusOne().div( oldPivotElement );
	    //if (tableauLogger.isDebugEnabled()) tableauLogger.debug("-1/pivotElement = " + minusOneDivOldPivot);
	    
	    for (int j = 0; j < oldBasicRow.size(); ++j){
		if (j != column) {
		    // if (tableauLogger.isDebugEnabled()) tableauLogger.debug(oldBasicRow.get(j) +" * " + factor + " = " + oldBasicRow.get(j).mult(factor) );
		    // pivot row element: a'_pj = a_pj*(-1)/a_ps = a_pj*factor
		    oldBasicRow.set(j, minusOneDivOldPivot.mult(oldBasicRow.get(j)));
		} else {
		    // pivot element: a'_ps = (-1)*(-1)/a_ps = 1/a_ps
		    oldBasicRow.set(column, numberFactory.getOne().div(oldPivotElement));    
		}
		// IMPORTANT: Thresholding for zero values
		if (useZeroThresholdingForPivoting){
		    if ( !oldBasicRow.get(j).isZero() && oldBasicRow.get(j).abs().lessThan(numberFactory.getInstance(zeroThreshold)) ){
			if (tableauLogger.isDebugEnabled()) {
			    tableauLogger.debug("SimplexTableau Thresholding: " + oldBasicRow.get(j).abs() + " < " + numberFactory.getInstance(zeroThreshold));
			    tableauLogger.debug("SimplexTableau: Thresholding: Resetting " + oldBasicRow.get(j) + " to 0.");
			}
			oldBasicRow.set(j, numberFactory.getZero());
		    }
		}
	    }
	    
	    /*
	     *  update remaining rows
	     */
	    for (int i = 0; i < matrix.size(); ++i){
		if( i != row){
		    ArrayList<NumberWrapper> currentRow = matrix.get(i);
		    // adding this row*factor gives zero in the pivoting column
		    NumberWrapper factor = currentRow.get(column);
		    // if (tableauLogger.isDebugEnabled()) tableauLogger.debug("factor = " + factor);
		    for (int j = 0; j < currentRow.size(); ++j) {
			if ( j != column) {
			    // if (tableauLogger.isDebugEnabled()) tableauLogger.debug( currentRow.get(j) +" + ( " + factor + " * " + oldBasicRow.get(j)  + " ) = " + currentRow.get(j).add( factor.mult(oldBasicRow.get(j)) ) );
			    currentRow.set(j, currentRow.get(j).add( factor.mult(oldBasicRow.get(j)) )) ;
			} else {
			    // if (tableauLogger.isDebugEnabled()) tableauLogger.debug( oldBasicRow.get(j) +" * " + factor + " = " + oldBasicRow.get(j).mult(factor) );
			    currentRow.set(j, factor.mult(oldBasicRow.get(j)) );
			}

			// IMPORTANT: Thresholding for zero values
			if (useZeroThresholdingForPivoting ){
			    if ( !currentRow.get(j).isZero() && currentRow.get(j).abs().lessThan(numberFactory.getInstance(zeroThreshold)) ){
				if (tableauLogger.isDebugEnabled()) {
				    logger.debug("SimplexTableau Thresholding: " + currentRow.get(j).abs() + " < " + numberFactory.getInstance(zeroThreshold));
				    logger.debug("SimplexTableau: Thresholding: Resetting " + currentRow.get(j)+ " to 0.");
				}
				currentRow.set(j, numberFactory.getZero());
			    }
			}
		    }
		}
	    }
	    
	    NumericVariable newRowVar = columnVars.get(column);
	    NumericVariable newColumnVar = rowVars.get(row);
	    columnVars.set(column, newColumnVar);
	    columnVarsSorted.remove(newRowVar);
	    columnVarsSorted.add(newColumnVar);
	    rowVars.set(row, newRowVar);
	    rowVarsSorted.remove(newColumnVar);
	    rowVarsSorted.add(newRowVar);
	    
	    
	    if (tableauLogger.isDebugEnabled()) debugCheckTableau();
	}
	
	@Override
	public String toString(){
	    if (columnVars.size() == 0) return "columVar.size() == 0\n";
	    if (rowVars.size() == 0) return "rowVar.size() == 0\n";
	    
	    String cr = "\n";
	    
	    StringBuffer string = new StringBuffer("[");
	    for(int i = 0; i < columnVars.size()-1; ++i){
		NumericVariable var = columnVars.get(i);
		string.append(var + "=" + assignments.get(var) + ", ");
	    }
	    NumericVariable var = columnVars.get(columnVars.size()-1);
	    string.append(var + "=" + assignments.get(var) + "]" + cr);
	    
	    for (int i = 0; i < matrix.size(); ++i){
		if (rowVars.size() > i)
		    var = rowVars.get(i);
		else
		    var = null;
		string.append( var + ": " + matrix.get(i) + " = "+assignments.get(var)+cr);
	    }
	    string.append(cr);
	    string.append("Basic assignments:"+cr);
	    for (NumericVariable basicVar : rowVars){
		string.append(lowerBounds.get(basicVar) +" <= " + basicVar + "="+assignments.get(basicVar)+" <= " +upperBounds.get(basicVar) + cr);
	    }
	    string.append("Non-basic assignments:"+cr);
	    for (NumericVariable nonBasicVar : columnVars){
		string.append(lowerBounds.get(nonBasicVar) +" <= " + nonBasicVar + "="+assignments.get(nonBasicVar)+" <= " +upperBounds.get(nonBasicVar) + cr);
	    }
	    
	    return string.toString();
	}
	
	/**
	 * Called by {@link #removeRow()}.</BR></BR>
	 * 
	 * The goal of this function is to pivot the given non-basic variable with a
	 * basic variable, i.e. to make the given variable basic.
	 * <BR/><BR/>
	 * The basic variable cannot be chosen arbitrarily though. First of all the potential pivot
	 * element has to be non-zero to be able to pivot at all. Further it has to be assured, that
	 * after pivoting the then new non-basic assignment does not violate the invariant which states
	 * that each non-basic variables assignment must not violate its bound.
	 * So we try to find a basic variable whose pivot element is non-zero and for which the invariant
	 * would after pivoting. If this is not possible we opt to choose a variable with non-zero pivot element
	 * and change its assignment to comply with its bounds. (Which is a bit more computationally intensive.)
	 * <BR/><BR/>
	 * Note: A non-zero pivot element can always be found because the matrix always has full rang.
	 * 
	 * @param nonBasicVariable which is to be made basic.
	 * @return the index of the row that corresponds to the new basic variable.
	 * @throws SolverUnableToDecideException 
	 */
	private int makeBasic(NumericVariable nonBasicVariable) throws SolverUnableToDecideException{
	    int col = columnVars.indexOf(nonBasicVariable);
	    int row = -1;
	    	    
	    /*
	     * find a basic variable to get out of the basis
	     */
	    for (int i = 0; i < rowVars.size(); ++i){
		if ( !getElement(i, col).isZero() ) {
		    row = i;
		    break;
		}
	    } // row = -1 impossible as rang is invariant to gaussian elimination
	    if (row == -1){
		tableauLogger.error("SimplexTableau: SimplexTableau: Numerical Errors! This should never happen. Something seriously wrong!");
		throw new SolverUnableToDecideException("SimplexTableau: Numerical Errors! Cannot find basic variable with nonzero " +
				"coefficient. This should never happen due to full rank of the Tableau Matrix.");
	    }
	    
	    /*
	     * if basic variable violates invariant, try to find one which doesn't
	     */
	    boolean basicVariablesNeedUpdate = false;
	    NumericVariable currentBasicVar = tableau.rowVars.get(row);
	    // try to find a basic variable to get out of the basis which does not violate its bounds
	    if (assignments.get(currentBasicVar).greaterThan(upperBounds.get(currentBasicVar)) 
		    || assignments.get(currentBasicVar).lessThan(lowerBounds.get(currentBasicVar))) {
		int new_row = -1;
		for (int i = row+1; i < rowVars.size(); ++i) {
		    currentBasicVar = rowVars.get(i);
		    if ( !getElement(i, col).isZero()
			    && assignments.get(currentBasicVar).lessThan(upperBounds.get(currentBasicVar)) 
			    && assignments.get(currentBasicVar).greaterThan(lowerBounds.get(currentBasicVar)) ) {
			new_row = i;
			break;
		    }
		}
		if (new_row != -1) {
		    row = new_row;
		} else {
		    basicVariablesNeedUpdate = true;
		}
	    }
	    
	    /*
	     * Finally pivot!
	     */
	    if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexTableau: Pivoting "+rowVars.get(row)+"["+row+"] and "+columnVars.get(col)+"["+col+"].");
	    pivot(row, col);
	    
	    /*
	     * IMPORTANT: If the new basic variable violates one of its bounds, change the variable's assignment accordingly
	     */
	    if (basicVariablesNeedUpdate) {
		NumberWrapper newValue = null;
		if ( assignments.get(currentBasicVar).greaterThan(upperBounds.get(currentBasicVar)) ){
		    newValue = upperBounds.get(currentBasicVar);
		} else {
		    newValue = lowerBounds.get(currentBasicVar);
		}
		assignments.put(currentBasicVar, newValue);
		changeNonBasicAssignment(row, newValue);
	    }
	    
	    if (tableauLogger.isDebugEnabled()) debugCheckTableau();
	    
	    return row;
	}

	/**
	 * Used by {@link #changeLowerBound()} and {@link #changeUpperBound()}. 
	 * </br></br> 
	 * If the assignment of a non basic variable is changed, we have to change
	 * the basic variables assignments as well, because these are determined by
	 * the non-basic variables (and the tableau).
	 * 
	 * @param indexOfNonBasicVar index of the non-basic variable's assignment which is to be changed.
	 * @param newValue the value for the new assignment.
	 */
	private void changeNonBasicAssignment(int indexOfNonBasicVar, NumberWrapper newValue) {
	    NumericVariable nonBasicVar = columnVars.get(indexOfNonBasicVar);
	    
	    // values of basic variables are determined by the non basic ones, so update them
	    for (int i = 0;  i < rowVars.size(); ++i){
	        NumberWrapper rowElement = tableau.getElement(i, indexOfNonBasicVar);
	        // only change assignment if the non-basic variables coefficient is different from zero 
	        if ( rowElement.isZero() ) {
	            continue;
	        }
	        NumericVariable basicVar = rowVars.get(i);
	        assignments.put(basicVar, assignments.get(basicVar).add( newValue.sub(assignments.get(nonBasicVar)).mult(rowElement)) );
	    }
	    
	    // finally update the new assignment for the non basic Variable
	    assignments.put(nonBasicVar, newValue);
	}

	/**
	 * Sets a new lower bound for a variable. If the new bound is less than the current bound
	 * nothing is changed.
	 * </br></br>
	 * 
	 * If the actual assigned value of the variable <code>var</code> is less than the new lower bound
	 * (i.e. the assignment is rendered infeasible) and is a non-basic variable then its assignment is 
	 * updated to the new bound. The assignments of the basic
	 * variables depend on <code>var</code>'s assignment. Therefore all assignments of basic
	 * variables are updated accordingly. On the other hand: If the variable <code>var</code> is a 
	 * basic variable it can't be changed and must be computed by the next simplex solving steps.
	 * </br></br>
	 * WARNING: feasibility of the newly build assignment is NOT guaranteed. Set {@link #feasible} to
	 * false and run solve again to assert that.
	 * </br></br>
	 * This function is used by {@link #branchAndCut()}, {@link #disableConstraint()} and {@link #enableConstraint()}.
	 * 
	 * @param var The variable whose lower bound should be changed.
	 * @param newLowerBound The desired new lower bound.
	 * @throws SolverUnableToDecideException 
	 * @see changeUpperBound
	 * @see setNonBasicVariableAssignment
	 */
	private void changeLowerBound(NumericVariable var, NumberWrapper newLowerBound) throws SolverUnableToDecideException{
	    if ( upperBounds.get(var).lessThan(newLowerBound) ) {
	        if (logger.isDebugEnabled()) logger.debug("SimplexTableau: New lower bound > upper bound => not feasible");
	        feasible = false;
	        return;
	    }
	    
	    lowerBounds.put(var, newLowerBound);
	    
	    int indexOfNonBasicVar = columnVars.indexOf(var);
	    if ( (indexOfNonBasicVar != -1) && assignments.get(var).lessThan(newLowerBound) ) 
	        changeNonBasicAssignment(indexOfNonBasicVar, newLowerBound);
	    
	    if (tableauLogger.isDebugEnabled()) debugCheckTableau();
	}

	/**
	 * Sets a new upper bound for a variable. If the new bound is greater than the current bound
	 * nothing is changed.
	 * </br></br>
	 * 
	 * If the actual assigned value of the variable <code>var</code> is greater than the new upper bound
	 * (i.e. the assignment is rendered infeasible) and is a non-basic variable then its assignment is 
	 * updated to the new bound. The assignments of the basic
	 * variables depend on <code>var</code>'s assignment. Therefore all assignments of basic
	 * variables are updated accordingly. On the other hand: If the variable <code>var</code> is a 
	 * basic variable it can't be changed and must be computed by the next simplex solving steps.
	 * </br></br>
	 * WARNING: feasibility of the newly build assignment is NOT guaranteed. Set {@link #feasible} to
	 * <code>false</code> and run {@link #solve()} again to assert that.
	 * </br></br>
	 * This function is used by {@link #branchAndCut()}, {@link #disableConstraint()} and {@link #enableConstraint()}.
	 * 
	 * @param var The variable whose upper bound should be changed.
	 * @param newUpperBound The desired new upper bound.
	 * @throws SolverUnableToDecideException 
	 * 
	 * @see changeLowerBound
	 * @see setNonBasicVariableAssignment
	 */
	private void changeUpperBound(NumericVariable var, NumberWrapper newUpperBound) throws SolverUnableToDecideException{
	    if ( lowerBounds.get(var).greaterThan(newUpperBound) ) {
	        if (logger.isDebugEnabled()) logger.debug("SimplexSolver: New upper bound < lower bound => not feasible");
	        feasible = false;
	        return;
	    }
	    
	    tableau.upperBounds.put(var, newUpperBound);
	    
	    int indexOfNonBasicVar = tableau.columnVars.indexOf(var);
	    if ( indexOfNonBasicVar != -1 && assignments.get(var).greaterThan(newUpperBound) ) 
	        changeNonBasicAssignment(indexOfNonBasicVar, newUpperBound);
	    
	    if (tableauLogger.isDebugEnabled()) debugCheckTableau();
	}
	
	
	private void debugCheckTableau() throws SolverUnableToDecideException{
	    if (columnVars.size() == 0) return;
	    if (rowVars.size() == 0) return;
	    if (matrix.size() == 0) return;
	    if (matrix.get(0).size() == 0) return;
	    
	    
	    updateDelta();
	    
	    // recalculate assignments for basic variables
	    for (int i = 0; i < rowVars.size(); ++i){
		NumericVariable rowVar = rowVars.get(i);
		NumberFactory factory = assignments.get(rowVar).getFactory();
		NumberWrapper assignment = factory.getZero();
		for (int j = 0; j < columnVars.size(); ++j){
		    assignment = assignment.add(assignments.get(columnVars.get(j)).mult(matrix.get(i).get(j)));
		}

		NumberWrapper deviance = assignments.get(rowVar).sub(assignment).abs();
		if (deviance.greaterThan(numberFactory.getInstance(debugDevianceThreshold))){		    
		    NumberWrapper threshold = assignments.get(rowVar).mult(numberFactory.getInstance(debugAssignmentThresholdPercentage)).abs();
		    if ( deviance.greaterThan(threshold) ) {
			tableauLogger.error("SimplexTableau: Numerical Errors! Not the same assignment for "+rowVar+": old = "+assignments.get(rowVar)+"; new = "+assignment+ ".");
			throw new SolverUnableToDecideException("SimplexTableau: Numerical Errors! Not the same assignment for "+rowVars.get(i)+": old = "+assignments.get(rowVars.get(i))+"; new = "+assignment);
		    } 
		}
	    }

	    // check whether there are non basic assignments that violate invariant 
	    for (NumericVariable nonBasicVar : columnVars) {
		if ( assignments.get(nonBasicVar).lessThan(lowerBounds.get(nonBasicVar)) ){
		    String output = "SimplexTableau: Numerical Errors! Non-basic variable "+ nonBasicVar +" violating lower bound: " +assignments.get(nonBasicVar)+ "<"+lowerBounds.get(nonBasicVar);
		    tableauLogger.error(output);
		    throw new SolverUnableToDecideException(output);
		}
		if ( assignments.get(nonBasicVar).greaterThan(upperBounds.get(nonBasicVar)) ){
		    String output = "SimplexTableau: Numerical Errors! Non-basic variable "+ nonBasicVar +" violating upper bound: "+assignments.get(nonBasicVar)+ " >" + upperBounds.get(nonBasicVar);
		    tableauLogger.error(output);
		    throw new SolverUnableToDecideException(output);
		}
	    }

	    // check if zero rows or columns are present
	    for (int i = 0; i < rowVars.size(); ++i){
		boolean notAllZero = false;
		List<NumberWrapper> currentRow = matrix.get(i);
		for (int j = 0; j < columnVars.size(); ++j){
		    if(currentRow.get(j).abs().doubleValue() > zeroThreshold) notAllZero = true;
		}
		if (!notAllZero){
		    tableauLogger.error("SimplexTableau: Numerical Errors! Row "+rowVars.get(i)+" ["+i+"] is zero.");
		    throw new SolverUnableToDecideException("SimplexTableau: Numerical Errors! Row "+rowVars.get(i)+" ["+i+"] is zero.");
		}
	    }
	    for (int j = 0; j < matrix.get(0).size(); ++j){
		boolean notAllZero = false;
		for (int i = 0; i < matrix.size(); ++i){
		    if(getElement(i,j).abs().doubleValue() > zeroThreshold) notAllZero = true;
		}
		if (!notAllZero){
		    tableauLogger.error("SimplexTableau: Numerical Errors! Column "+columnVars.get(j)+" ["+j+"] is zero.");
		    throw new SolverUnableToDecideException("SimplexTableau: Numerical Errors! Column "+matrix.get(j)+" ["+j+"] is zero.");
		}
	    }
	}

	/**
	 * Updates the delta for assignments and bounds FractionDelta numbers.
	 * </br></br>
	 * The delta value is used to calculate Fraction numbers out of FractionDelta numbers.
	 * </br></br>
	 * Used by {@link #solve()}.
	 */
	private void updateDelta() {
	    NumberWrapper delta = null;
	    	
	    for (NumericVariable var : assignments.keySet()) {
	        DeltaWrapper value = (DeltaWrapper) assignments.get(var);
	        DeltaWrapper lower = (DeltaWrapper) lowerBounds.get(var);
	        DeltaWrapper upper = (DeltaWrapper) upperBounds.get(var);
	        
	        if ( !lower.isNegativeInfinity() && lower.getC().lessThan(value.getC()) && lower.getK().greaterThan(value.getK())) {
	    	NumberWrapper helper = value.getC().sub(lower.getC());
	    	helper = helper.div( lower.getK().sub(value.getK()) );
	    	
	    	if ( delta == null || helper.lessThan(delta)){
	    	    delta = helper;
	    	}
	    	
	        }
	        
	        if ( !upper.isPositiveInfinity() && value.getC().lessThan(upper.getC()) && value.getK().greaterThan(upper.getK())) {
	    	NumberWrapper helper = upper.getC().sub(value.getC());
	    	helper = helper.div( value.getK().sub(upper.getK()) );
	    	
	    	if ( delta == null || helper.lessThan(delta) ){
	    	    delta = helper;
	    	}
	        }
	    }
	    	
	    if ( delta == null){
	        delta = numberFactory.getOne();
	        if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexSolver: No new delta value. Using " + delta);
	    } else {
	        if (tableauLogger.isDebugEnabled()) tableauLogger.debug("SimplexSolver: Calculating new delta value: " + delta);
	    }
	    DeltaWrapper.setDelta(delta);
	}	
    } // class SimplexTableau
    
}
