package de.wwu.testtool.test;

import java.util.ArrayList;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.wwu.testtool.conf.SimplexSolverConfig;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.testtool.solver.tsolver.EliminationSolver;
import de.wwu.testtool.solver.tsolver.SimplexSolver;
import de.wwu.testtool.solver.tsolver.SimplexSolverCL;
import de.wwu.testtool.test.DiplomaThesisProblems;

/**
 * Program to reproduce (most of) the results from my Diploma Thesis in just one run.
 * @author Marko Ernsting
 */
public class DiplomaThesis {

    /**
     * How many times should each test run to get reasonable timing results?
     * The mean of all runs is printed as running time.
     */
    private static final int RUNS = 1;
    
    /**
     * Values to configure random problems from DiplomaThesisProblems. 
     */
    private long RANDOM_SEED = 2;
    private int RANDOM_CONSTRAIN_LENGTH = 12;
    private int RANDOM_CONSTRAINT_COUNT = 60;
    
    /**
     * The problem definitions.
     */
    private DiplomaThesisProblems problems = new DiplomaThesisProblems(RANDOM_CONSTRAINT_COUNT, RANDOM_CONSTRAIN_LENGTH, RANDOM_SEED);

    /**
     * The timeout we use for all solvers. Be sure to set -Xmx1536m -Xms1536m to appropriate sizes for the old solver.
     */
    private static final long TIMEOUT = 60000;

    /**
     * Specifies up to which threshold a false solution is considered a rounding error
     */
    private static final double ROUNDING_ERROR_THRESHOLD = 1E-8; 
    
    /**
     * If a constraint set has numerical errors or times out, should we restart the complete
     * set with the next constraint.
     * I.e. 34 times out. should we reset readd all 34 and try 35?
     */
    private static final boolean RESTART_TIMEOUTS_AND_NUMERICAL_ERRORS = false;

    /**
     * should progress be printed?
     */
    private static final boolean VERBOSE = false;
    
    /**
     * How many constraints to roll back for backtracking simulation.
     */
    private static int BACKTRACKING_COUNT = 0; 

    /**
     * possible solution types
     * @author Marko Ernsting
     */
    private enum SolutionType {
	OK 		   {public String toString() {return "OK";}},
	OK_ROUNDING_ERRORS {public String toString() {return "R";}},
	WRONG              {public String toString() {return "W";}},
	NO_SOLUTION	   {public String toString() {return "NS";}},
	TIMEOUT		   {public String toString() {return "T";}},
	NUMERICAL_ERROR    {public String toString() {return "NE";}},
	OVERFLOW	   {public String toString() {return "O";}},
    }

    private SimplexSolver simplexSolver;
    private SimplexSolverCL simplexSolverCL;
    private EliminationSolver eliminationSolver;
    
    private Logger logger;
    private Logger console;

    private boolean plotOnlyAccumulated;
    
    
    /**
     * Private helper class for results gathering.
     * Without trivial getters and setters for convenience.
     * @author Marko Ernsting
     */
    private class Results{
	public ArrayList<Long> timings;
	public ArrayList<SolutionType> solutionTypes;
	public int size;
	public String name;
	
	Results(int size){
	    this.size = size;
	    solutionTypes = new ArrayList<SolutionType>(size);
	    timings = new ArrayList<Long>(size);
	    for (int i = 0; i < size; ++i){
		timings.add(i, 0L);
		solutionTypes.add(i, SolutionType.TIMEOUT);
	    }
	    name = "";
	}
	
	public String toString(){
	    String result = name+"\n"; 
	    for (int i = 0; i < size; ++i){
		result += "Constraint "+i+": "+timings.get(i)+"mus, result = "+solutionTypes.get(i)+"\n";
	    }	    
	    return result;
	}
	
    }
    
        
    /**
     * The damn constructor.
     */
    public DiplomaThesis(){
	loggerSetup();
	
	SimplexSolverConfig.getInstance().setTimeout(TIMEOUT);
	SimplexSolverConfig.getInstance().setLogger(logger);
	
	simplexSolver = new SimplexSolver();
	simplexSolverCL = SimplexSolverCL.newInstance(null);
	eliminationSolver = EliminationSolver.newInstance(null);
	
	simplexSolver.setDebugLogger(logger);	
    }
    
    /**
     * Just some logging set up here.
     */
    private void loggerSetup() {
	logger = TesttoolConfig.getLogger();
	logger.setLevel(Level.INFO);
	
	console = Logger.getLogger("de.wwu.testtool.test.console");
	console.setLevel(Level.INFO);
	
	ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%-5p [%t:%r] %M(): %m%n"));
	consoleAppender.setName("Console Appender");
	//consoleAppender.setLayout(new PatternLayout("[%r] %20.20M(): %m%n") );
	consoleAppender.setLayout(new PatternLayout("%m%n") );
	console.addAppender(consoleAppender);
    }

    /**
     * Prints the solution information to the console. 
     * Checks whether all constraints are satisfied or not.
     * If only rounding errors are responsible for wrong results, this is reported. 
     * @param set
     * @param solution
     */
    private void printSolutionValidation(SingleConstraintSet set, Solution solution)  {
	try {
	    console.info("Solution: "+solution);
	    if (solution != Solution.NOSOLUTION){
		console.info("Inserting into constraints: " + set.validateSolution(solution));
		console.info("Solution check: " +checkSolution(set, solution));
		for (int i = 0; i < set.getConstraintCount(); ++i){
		    NumericConstraint constraint = ((NumericConstraint) set.getConstraint(i));
		    Polynomial poly = constraint.getPolynomial();
		    
		    if ( constraint.isStrictInequation() ) {
			if (poly.computeValue(solution).doubleValue() >= 0){
			    console.info("Constraint "+i+" not satisfied: " + poly.computeValue(solution).doubleValue()+" >= 0 " );
			}
		    } else if ( constraint.isWeakInequation() ){
			if ( poly.computeValue(solution).doubleValue() > 0) {
			    console.info("Constraint "+i+" not satisfied: " + poly.computeValue(solution).doubleValue()+" > 0" );
			}
		    } else {
			if ( poly.computeValue(solution).doubleValue() > 0) {
			    console.info("Constraint "+i+" not satisfied: " + poly.computeValue(solution).doubleValue()+" != 0" );
			}
		    }
		}
	    } else {
		console.info("NO_SOLUTION");
	    }
	} catch (IncompleteSolutionException e1) {
	    e1.printStackTrace();
	}
    }
    
    
    /**
     * Checks whether the solution is right, wrong, right with rounding errors, nosolution or timeout.
     * @param set
     * @param solution
     * @return
     * @throws IncompleteSolutionException
     */
    private SolutionType checkSolution(SingleConstraintSet set, Solution solution) throws IncompleteSolutionException{
	boolean hasRoundingError = false;
	boolean isWrong = false;
	
	for(int i = 0; i < set.getConstraintCount(); ++i){
	    
	    double result = ( (NumericConstraint) set.getConstraint(i)).getPolynomial().computeValue(solution).doubleValue();
	    
	    if (result > 0){
		if (result < ROUNDING_ERROR_THRESHOLD){
		    hasRoundingError = true;
		} else {
		    isWrong = true;
		    System.out.println("Wrong Values: "+result);
		}
	    }
	}
	
	for (int i = 0; i < set.getConstraintCount(); ++i){
	    NumericConstraint constraint = ((NumericConstraint) set.getConstraint(i));
	    Polynomial poly = constraint.getPolynomial();
	    
	    double result = poly.computeValue(solution).doubleValue();
	    
	    if ( constraint.isStrictInequation() ) {
		if (result >= 0){
		    if (result < ROUNDING_ERROR_THRESHOLD && result != 0){
			hasRoundingError = true;
		    } else {
			isWrong = true;
			System.out.println("ERROR: Wrong result: " + result);
		    }
		}
	    } else {
		if (result > 0){
		    if (result < ROUNDING_ERROR_THRESHOLD){
			hasRoundingError = true;
		    } else {
			isWrong = true;
			System.out.println(result);
		    }
		}
	    }
	}
	
	
	SolutionType result = SolutionType.OK;
	if (hasRoundingError){
	    result = SolutionType.OK_ROUNDING_ERRORS;
	}
	if (isWrong){
	    result = SolutionType.WRONG;
	}
	
	return result; 
    }
    
    
    
    /**
     * A helper function for Random Test.
     * @param solver
     * @param constantType
     * @param variableType
     * @param ineqType
     * @param runs
     * @return
     * @throws IncorrectSolverException
     * @throws IncompleteSolutionException
     * @throws SolverUnableToDecideException
     */
    private Results compareTestHelper(Solver solver, Constant constantType, Variable variableType, IneqType ineqType, int runs, String comment) 
    throws IncorrectSolverException, IncompleteSolutionException 
    {
	SingleConstraintSet set =  problems.getRandomSingleConstraintSetProblem(constantType, variableType, ineqType);
	Results results = new Results(set.getConstraintCount());
	
	/*
	 * Give it a name.
	 */
	String name = "";
	if (solver instanceof SimplexSolver) name += "New:";
	if (solver instanceof SimplexSolverCL) name += "Old:";
	if (solver instanceof EliminationSolver) name += "Elm:"; 
	name += (variableType == Variable.INT) ? " MILA" : " LA";
	name += (constantType == Constant.INT) ? " /w Int coeff" : " /w Double coeff";
	name += (ineqType == IneqType.WEAK) ? ", Weak" : ", Strict";
	name += ", "+runs+" runs";
	name += (comment != "") ? " ("+comment+")" : "";
	results.name = name;
	console.info("\n"+name);
		
	/*
	 * Run the tests.
	 */
	for (int j = 0; j < runs; ++j){
	    solver = solver.reset();
	    SingleConstraintSet verifyingSet = new SingleConstraintSet();
	    
	    SolutionType solutionType = SolutionType.NO_SOLUTION;
	    
	    for (int i = 0; i < set.getConstraintCount(); ++i){
		long timer = System.nanoTime();
		
		Throwable throwable = null;
		
		/*
		 * Simulate Backtracking of BACKTRACKING_COUNT constraints.
		 * And add them again.
		 */
		boolean backtrackingFailure = false;
		if ( BACKTRACKING_COUNT > 0 && i >= BACKTRACKING_COUNT) {
		    for (int k = 0; k < BACKTRACKING_COUNT; ++k ){
			solver.removeConstraint();
		    }
		    
		    for (int k = i - BACKTRACKING_COUNT; k < i; ++k ){
			solver.addConstraint(set.getConstraint(k));
			try {
			    solver.getSolution();
			} catch (TimeoutException e) {
			    backtrackingFailure = true;
			    solutionType = SolutionType.TIMEOUT;		    
			} catch (SolverUnableToDecideException e) {
			    backtrackingFailure = true;
			    solutionType = SolutionType.NUMERICAL_ERROR;
			    throwable = e;
			} catch (StackOverflowError e) {
			    backtrackingFailure = true;
			    solutionType = SolutionType.OVERFLOW;
			    throwable = e;
			} catch (OutOfMemoryError e) {
			    backtrackingFailure = true;
			    solutionType = SolutionType.OVERFLOW;
			    throwable = e;
			}
		    }
		}
		
		Solution solution = null;
		
		if (!backtrackingFailure){
		    solver.addConstraint( set.getConstraint(i) );
		    verifyingSet.add( set.getConstraint(i) );		    

		    solution = null;
		    try {
			solution = solver.getSolution();
		    } catch (TimeoutException e) {
			solutionType = SolutionType.TIMEOUT;		    
		    } catch (SolverUnableToDecideException e) {
			solutionType = SolutionType.NUMERICAL_ERROR;
			throwable = e;
		    } catch (StackOverflowError e) {
			solutionType = SolutionType.OVERFLOW;
			throwable = e;
		    } catch (OutOfMemoryError e) {
			backtrackingFailure = true;
			solutionType = SolutionType.OVERFLOW;
			throwable = e;
		    }
		}
		
		results.timings.set(i, results.timings.get(i) + (System.nanoTime() - timer));
		
		/*
		 * Only give output at the first run.
		 */
		if (j == 0) {
		    if (solution == null) {
			if( solutionType == SolutionType.TIMEOUT) {
			    if ( VERBOSE ) console.info("Constraint "+i+": Timeout reached.");
			    results.solutionTypes.add(i, SolutionType.TIMEOUT);
			}
			if (solutionType == SolutionType.NUMERICAL_ERROR) {
			    if ( VERBOSE ) console.info("Constraint "+i+": Numerical errors encountered: " +throwable);
			    results.solutionTypes.add(i, SolutionType.NUMERICAL_ERROR);			    
			}
			if (solutionType == SolutionType.OVERFLOW) {
			    if ( VERBOSE ) console.info("Constraint "+i+": Overflow encountered: " +throwable);
			    results.solutionTypes.add(i, SolutionType.OVERFLOW);			    
			}
		    } else {
			if (solution == Solution.NOSOLUTION) {
			    if ( VERBOSE ) console.info("Constraint "+i+": NO_SOLUTION.");
			    results.solutionTypes.add(i, SolutionType.NO_SOLUTION);
			} else { // OK, OK_ROUNDED or WRONG??
			    if ( VERBOSE ) console.info( "Constraint "+i+": " + checkSolution(verifyingSet, solution));
			    if ( VERBOSE ) printSolutionValidation(verifyingSet, solution);
			    results.solutionTypes.add(i, checkSolution(verifyingSet, solution));
			}
		    }
		}
		
		/*
		 * Calculate mean at last run.
		 */
		if (j == runs-1) {
		    results.timings.set(i, (results.timings.get(i)/1000000) / (j+1) );
		}
		
		if (solutionType == SolutionType.NUMERICAL_ERROR 
			|| solutionType == SolutionType.TIMEOUT
			|| solutionType == SolutionType.OVERFLOW){
		    solver = solver.reset();
		    if (RESTART_TIMEOUTS_AND_NUMERICAL_ERRORS) {  // restart for next try with more constraints
			for (int k = 0; k <= i; ++k){
			    solver.addConstraint(set.getConstraint(k));
			}
		    }
		    else {  // or just cancel next constraints
			SolutionType currentSolutionType = results.solutionTypes.get(i);
			for (int k = i+1; k < set.getConstraintCount(); ++k){
			    results.solutionTypes.add(k, currentSolutionType);
			    results.timings.add(k, 0L);
			}
			i = set.getConstraintCount()-1;
		    }
		    solution = null;
		}
	    }
	}
	return results;
    }
    
    /**
     * The Branch and Bound Loop Test.
     * @throws IncorrectSolverException
     */
    public void exampleRoundingError() throws IncorrectSolverException{
	SingleConstraintSet set = problems.branchAndBoundLoopProblemRelaxed();
	Solution solution = null;
	
	console.info("");
	console.info("--- Rounding errors test");
	console.info("");
	for (int i = 0; i < set.getConstraintCount(); ++i){
	    console.info(set.getConstraint(i));
	}
	console.info("");
	console.info("New Simplex: /w exact arithmetic; w/o post solving for rounding errors");
	simplexSolver = simplexSolver.reset();
	simplexSolver.useDoubleWrapper(false);
	simplexSolver.usePostSolvingForRoundingErrors(false);
	for (int i = 0; i < set.getConstraintCount(); ++i){
	    simplexSolver.addConstraint( set.getConstraint(i) );
	}
	try {
	    solution = simplexSolver.getSolution();
	} catch (TimeoutException e) {
	    console.info("Timeout reached.");
	} catch (SolverUnableToDecideException e) {
	    console.info(e);
	}
	printSolutionValidation(set, solution);
	
	
	
	console.info("");
	console.info("New Simplex: /w exact arithmetic; w/o post solving for rounding errors");
	simplexSolver = simplexSolver.reset();
	simplexSolver.usePostSolvingForRoundingErrors(true);
	for (int i = 0; i < set.getConstraintCount(); ++i){
	    simplexSolver.addConstraint( set.getConstraint(i) );
	}
	try {
	    solution = simplexSolver.getSolution();
	} catch (TimeoutException e) {
	    console.info("Timeout reached.");
	} catch (SolverUnableToDecideException e) {
	    console.info(e);
	}
	printSolutionValidation(set, solution);
	
	
	console.info("");
	console.info("CL Simplex: /w exact arithmetic");
	simplexSolverCL = simplexSolverCL.reset();
	simplexSolverCL.addConstraintSet( set );
	try {
	    solution = simplexSolverCL.getSolution();
	} catch (TimeoutException e) {
	    console.info("Timeout reached.");
	}
	console.info(solution);
	printSolutionValidation(set, solution);
    }
    
    
    /**
     * The Branch and Bound Loop Test.
     * @throws IncorrectSolverException
     */
    public void exampleBranchAndBoundLoopTest() throws IncorrectSolverException{
	SingleConstraintSet set = problems.branchAndBoundLoopProblem();
	SingleConstraintSet relaxedSet = problems.branchAndBoundLoopProblemRelaxed();
	Solution solution = null;
	
	console.info("");
	console.info("--- Branch and Bound Loop Test");
	console.info("");
	console.info("New Simplex: LA relaxed:");
	simplexSolver = simplexSolver.reset();
	simplexSolver.useDoubleWrapper(true);
	for (int i = 0; i < set.getConstraintCount(); ++i){
	    console.info( relaxedSet.getConstraint(i) );
	    simplexSolver.addConstraint( relaxedSet.getConstraint(i) );
	}
	try {
	    solution = simplexSolver.getSolution();
	} catch (TimeoutException e) {
	    console.info("Timeout reached.");
	} catch (SolverUnableToDecideException e) {
	    console.info(e);
	}
	printSolutionValidation(relaxedSet, solution);
	

	console.info("");
	console.info("New Simplex: Branch And Bound w/o Gomory Cuts:");
	simplexSolver = simplexSolver.reset();
	simplexSolver.useGomoryCuts(false);
	for (int i = 0; i < set.getConstraintCount(); ++i){
	    console.info(set.getConstraint(i));
	    simplexSolver.addConstraint( set.getConstraint(i) );
	}
	try {
	    solution = simplexSolver.getSolution();
	} catch (TimeoutException e) {
	    console.info("-> Timeout reached.");
	} catch (SolverUnableToDecideException e) {
	    console.info(e);
	} catch (StackOverflowError e) {
	    console.info("-> Caught a StackOverflowError exception!!! This can be considered a timeout :-).");
	}

	console.info("");
	console.info("New Simplex: Branch And Bound w/ Gomory Cuts:");
	simplexSolver = simplexSolver.reset();
	simplexSolver.useGomoryCuts(true);
	for (int i = 0; i < set.getConstraintCount(); ++i){
	    console.info(set.getConstraint(i));
	    simplexSolver.addConstraint( set.getConstraint(i) );
	}
	try {
	    solution = simplexSolver.getSolution();
	} catch (TimeoutException e) {
	    console.info("Timeout reached.");
	} catch (SolverUnableToDecideException e) {
	    console.info(e);
	}
	printSolutionValidation(set, solution);
	
	
	
	console.info("");
	console.info("CL Simplex: LA relaxed:");
	simplexSolverCL = simplexSolverCL.reset();
	simplexSolverCL.addConstraintSet( relaxedSet );
	try {
	    solution = simplexSolverCL.getSolution();
	} catch (TimeoutException e) {
	    console.info("Timeout reached.");
	}
	console.info(solution);
	printSolutionValidation(relaxedSet, solution);

	
	console.info("");
	console.info("CL Simplex: MILA");
	simplexSolverCL = simplexSolverCL.reset();
	simplexSolver.addConstraintSet( set );
	try {
	    solution = simplexSolverCL.getSolution();
	} catch (TimeoutException e) {
	    console.info("Timeout reached.");
	}
	console.info(solution);
	printSolutionValidation(set, solution);
    }
    
    /**
     * Compares the new solvers capabilities for backtracking and incremental solving.
     * @throws IncorrectSolverException
     * @throws IncompleteSolutionException
     * @throws SolverUnableToDecideException
     */
    public void compareIncrementalBacktrackingTest(Constant constantType) throws IncorrectSolverException, IncompleteSolutionException, SolverUnableToDecideException{
	int runs = RUNS;
	ArrayList<Results> resultsArray;
	
	BACKTRACKING_COUNT = 10;
	
	/*
	 *  LA
	 */
	resultsArray = new ArrayList<Results>();
	String comment = "";
	simplexSolver.useDoubleWrapper(false); // using Fraction to be comparable
	runs = 1;
	compareTestHelper(simplexSolver, constantType, Variable.DOUBLE, IneqType.WEAK, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	
	comment = "non-incremental";
	simplexSolver.useIncrementalSolving(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	
	
	comment = "non-backtracking";
	simplexSolver.useBacktracking(false);
	simplexSolver.useIncrementalSolving(true);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	
	comment = "non-incremental, non-backtracking";
	simplexSolver.useIncrementalSolving(false);
	simplexSolver.useBacktracking(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	simplexSolver.useIncrementalSolving(true);
	simplexSolver.useBacktracking(true);
	
	printResults(resultsArray, "incrementalBacktrackingTest() /w "+constantType+", "+runs+" runs");
    } 
    
    
    /**
     * Compares the new and the old simplex solver.
     * @throws IncorrectSolverException
     * @throws IncompleteSolutionException
     * @throws SolverUnableToDecideException
     */
    public void compareOldAndNewSimplex() throws IncorrectSolverException, IncompleteSolutionException, SolverUnableToDecideException{
	int runs = RUNS;
	ArrayList<Results> resultsArray;
	
	
	BACKTRACKING_COUNT = 10;
	BACKTRACKING_COUNT = 0;
	
	/*
	 *  LA
	 */
	resultsArray = new ArrayList<Results>();	
	String comment = "Fraction";
	simplexSolver.useDoubleWrapper(false);
	runs = 1;
	compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.WEAK, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	comment = "DoubleWrapper";
	simplexSolver.useDoubleWrapper(true);
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	comment = "";
	resultsArray.add( compareTestHelper(simplexSolverCL, Constant.INT, Variable.DOUBLE, IneqType.WEAK, runs, comment));

	printResults(resultsArray, "compareOldAndNewSimplex() /w LA, "+runs+" runs");
	
	
	/*
	 * MILA
	 */
	resultsArray = new ArrayList<Results>();
	comment = "Fraction";
	runs = 1;
	compareTestHelper(simplexSolver, Constant.INT, Variable.INT, IneqType.WEAK, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.INT, IneqType.WEAK, runs, comment) );
	
	comment = "Fraction /w non-incremental, non-backtracking";
	simplexSolver.useBacktracking(false);
	simplexSolver.useIncrementalSolving(false);
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.INT, IneqType.WEAK, runs, comment) );
	simplexSolver.useBacktracking(true);
	simplexSolver.useIncrementalSolving(true);
	
	simplexSolver.useDoubleWrapper(false);
	comment = "DoubleWrapper";
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.INT, IneqType.WEAK, runs, comment) );
	comment = "";
	resultsArray.add( compareTestHelper(simplexSolverCL, Constant.INT, Variable.INT, IneqType.WEAK, runs, comment));
	
	printResults(resultsArray, "compareOldAndNewSimplex() /w MILA, "+runs+" runs");
	
	BACKTRACKING_COUNT = 0;
    } 
    
    
    /**
     * Compare Solutions without and with postsolving
     * @throws IncorrectSolverException
     * @throws IncompleteSolutionException
     * @throws SolverUnableToDecideException
     */
    public void comparePostSolving() throws IncorrectSolverException, IncompleteSolutionException, SolverUnableToDecideException{
	int runs = RUNS;
	ArrayList<Results> resultsArray;
	
	BACKTRACKING_COUNT = 10;
	
	/*
	 *  LA with double
	 */
	resultsArray = new ArrayList<Results>();	
	String comment = "Fraction";
	simplexSolver.useDoubleWrapper(false);
	runs = 1;
	compareTestHelper(simplexSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.WEAK, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	comment = "Fraction w/o postSolvingForRoundingErrors";
	simplexSolver.usePostSolvingForRoundingErrors(false);
	resultsArray.add( compareTestHelper(simplexSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.WEAK, runs, comment) );
	simplexSolver.usePostSolvingForRoundingErrors(true);

	printResults(resultsArray, "comparePostSolving() /w LA, "+runs+" runs");
		
	BACKTRACKING_COUNT = 0;
    } 
    
    
    
    /**
     * Compares the solvers for strict inequalities. For the LA and MILA case.
     * @throws IncorrectSolverException
     * @throws IncompleteSolutionException
     * @throws SolverUnableToDecideException
     */
    public void compareStrictInequalities() throws IncorrectSolverException, IncompleteSolutionException, SolverUnableToDecideException{
	int runs = RUNS;
	ArrayList<Results> resultsArray;
	
	BACKTRACKING_COUNT = 0;
	
	/*
	 *  LA
	 */
	resultsArray = new ArrayList<Results>();
	String comment = "Fraction";
	simplexSolver.useDoubleWrapper(false);
	runs = 1;
	compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.STRICT, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.STRICT, runs, comment) );
	resultsArray.add( compareTestHelper(simplexSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.STRICT, runs, comment) );
	comment = "";
	resultsArray.add( compareTestHelper(eliminationSolver, Constant.INT, Variable.DOUBLE, IneqType.STRICT, runs, comment));
	resultsArray.add( compareTestHelper(eliminationSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.STRICT, runs, comment));

	printResults(resultsArray, "compareStrictInequalities() /w LA, "+runs+" runs");
		
	/*
	 * MILA
	 */
	resultsArray = new ArrayList<Results>();
	comment = "Fraction";
	simplexSolver.useDoubleWrapper(false);
	runs = 1;
	compareTestHelper(simplexSolver, Constant.INT, Variable.INT, IneqType.STRICT, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.INT, IneqType.STRICT, runs, comment) );
	comment = "";
	resultsArray.add( compareTestHelper(eliminationSolver, Constant.INT, Variable.INT, IneqType.STRICT, runs, comment));

	printResults(resultsArray, "compareStrictInequalities() /w MILA, "+runs+" runs");
    } 
    

    /**
     * Compares the different options of the new simplex.
     * @throws IncorrectSolverException
     * @throws IncompleteSolutionException
     * @throws SolverUnableToDecideException
     */
    public void compareOptions(Constant constantType) throws IncorrectSolverException, IncompleteSolutionException, SolverUnableToDecideException{
	int runs = RUNS;
	ArrayList<Results> resultsArray = new ArrayList<Results>();
	
	String comment = "";
	
	runs = 1;
	compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment);
	runs = RUNS;
	

	// MILA
	simplexSolver.useDoubleWrapper(false);
	comment = "FR";
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
//	strict genauso schnell wie ohne gomory cuts !!!  Noch Testen
//	resultsArray.add( testHelper(simplexSolver, constantType, Variable.INT, IneqType.STRICT, runs, comment));
	
	comment = "FR w/o Gomory Cuts";
	simplexSolver.useGomoryCuts(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
	simplexSolver.useGomoryCuts(true);
	
	comment = "FR w/o addVar";
	simplexSolver.useAdditionalVarPreferredForBranchAndBound(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
	
	comment = "FR w/o GomoryCut, addVar";
	simplexSolver.useGomoryCuts(false);
	simplexSolver.useAdditionalVarPreferredForBranchAndBound(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
	simplexSolver.useGomoryCuts(true);
	simplexSolver.useAdditionalVarPreferredForBranchAndBound(true);
	
	
	// MILA
	simplexSolver.useDoubleWrapper(true);
	comment = "DW";
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
//	strict genauso schnell wie ohne gomory cuts !!!
//	resultsArray.add( testHelper(simplexSolver, constantType, Variable.INT, IneqType.STRICT, runs, comment));
	
	comment = "DW w/o Gomory Cuts";
	simplexSolver.useGomoryCuts(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
	simplexSolver.useGomoryCuts(true);
	
	comment = "DW w/o addVar";
	simplexSolver.useAdditionalVarPreferredForBranchAndBound(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
	
	comment = "DW w/o GomoryCut, addVar";
	simplexSolver.useGomoryCuts(false);
	simplexSolver.useAdditionalVarPreferredForBranchAndBound(false);
	resultsArray.add( compareTestHelper(simplexSolver, constantType, Variable.INT, IneqType.WEAK, runs, comment));
	simplexSolver.useGomoryCuts(true);
	simplexSolver.useAdditionalVarPreferredForBranchAndBound(true);
	
	printResults(resultsArray, "compareOptions(), "+constantType+", " + runs + " runs");	
    }
    
    /**
     * Compares the two different NumberWrapper types. DoubleWrapper and Fraction.
     * @param constantType
     * @throws IncorrectSolverException
     * @throws IncompleteSolutionException
     * @throws SolverUnableToDecideException
     */
    public void compareFractionDoubleWrapperTests() throws IncorrectSolverException, IncompleteSolutionException, SolverUnableToDecideException{
	int runs = RUNS;
	ArrayList<Results> resultsArray = new ArrayList<Results>();
	
	BACKTRACKING_COUNT = 0;
	
	String comment = "";
	
	comment = "FR";
	simplexSolver.useDoubleWrapper(false);
	runs = 1;
	compareTestHelper(simplexSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.WEAK, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.WEAK, runs, comment));	

	comment = "DW";
	simplexSolver.useDoubleWrapper(true);
	resultsArray.add( compareTestHelper(simplexSolver, Constant.DOUBLE, Variable.DOUBLE, IneqType.WEAK, runs, comment));	

	
	comment = "FR";
	simplexSolver.useDoubleWrapper(false);
	runs = 1;
	compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.WEAK, runs, comment);
	runs = RUNS;
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.WEAK, runs, comment));	

	comment = "DW";
	simplexSolver.useDoubleWrapper(true);
	resultsArray.add( compareTestHelper(simplexSolver, Constant.INT, Variable.DOUBLE, IneqType.WEAK, runs, comment));	
	
	
	printResults(resultsArray, "compareFractionDoubleWrapperTests(), " + runs + " runs");
    }
    
    
    
    
    /**
     * Pretty latex print the results.
     * @param resultsArray
     */
    private void printResults(ArrayList<Results> resultsArray, String description) {
	String timeunit = "$\\mu s$";
	timeunit = "ms";
	boolean sideways = false;

	console.info("%Latex Output:");
	console.info("%Required packages:");
	if (sideways) console.info("%\\usepackage{rotating}");
	console.info("%\\usepackage{tikz}");
	console.info("%\\usepackage{pgfplots}");
	console.info("");
	
	if (resultsArray.size() == 0 || resultsArray.get(0).size == 0) return;

	ArrayList<StringBuffer> lines = new ArrayList<StringBuffer>();
	int constraintCount = resultsArray.get(0).size;
	int resultsCount = resultsArray.size();
	
	
	/*
	 * Table
	 */
	// preamble
	StringBuffer tabular = new StringBuffer("");
	
	if (sideways)
	    tabular = new StringBuffer("\\begin{sidewaystable}\n");
	else
	    tabular = new StringBuffer("\\begin{table}\n");
	
	tabular.append("\\caption{"+description+"}\n\\label{table:"+description+"}\n\\begin{center}\n");
	tabular.append("{\\tiny\n");
	tabular.append("\\begin{tabular}\n{|r");
	for (int j = 0; j < resultsCount-1; ++j){
	    tabular.append("|r|r|c");
	}
	tabular.append("|r|r|c|}");
	lines.add(tabular);
	lines.add(new StringBuffer("\\hline"));
	
	StringBuffer descriptions = new StringBuffer("&");
	StringBuffer descriptions2 = new StringBuffer("\\#&");
	descriptions.append("\n\\multicolumn{3}{c}{" + resultsArray.get(0).name + "} & ");
	descriptions2.append("\\multicolumn{1}{c}{"+timeunit+"}& \\multicolumn{1}{c}{$\\Sigma$} & \\multicolumn{1}{c}{result} & ");

	for (int j = 1; j < resultsCount-1; ++j){
	    descriptions.append("\n\\multicolumn{3}{|c}{" + resultsArray.get(j).name + "} & ");
	    descriptions2.append("\\multicolumn{1}{|c}{"+timeunit+"}& \\multicolumn{1}{c}{$\\Sigma$} & \\multicolumn{1}{c}{result} & ");
	}
	descriptions.append("\n\\multicolumn{3}{|c|}{" + resultsArray.get(resultsCount-1).name + "} \\\\ ");
	descriptions2.append("\\multicolumn{1}{|c}{"+timeunit+"}& \\multicolumn{1}{c}{$\\Sigma$} & \\multicolumn{1}{c|}{result} \\\\ ");
	lines.add(descriptions);
	lines.add(descriptions2);
	lines.add(new StringBuffer("\\hline"));
	
	
	// contenu
	
	// row (constraint) loop 
	for (int i = 0; i < constraintCount; ++i){
	    StringBuffer currentLine = new StringBuffer((i+1)+"&");
	    lines.add(currentLine);
	    long cummulatedRuntime = 0L;
	    // column (problem) loop
	    for (int j = 0; j < resultsCount; ++j){
		// timing columns
		cummulatedRuntime = 0L;
		if (i == 0 || resultsArray.get(j).solutionTypes.get(i-1) == SolutionType.OK
			   || resultsArray.get(j).solutionTypes.get(i-1) == SolutionType.OK_ROUNDING_ERRORS
			   || resultsArray.get(j).solutionTypes.get(i-1) == SolutionType.NO_SOLUTION) {
		    for (int k = 0; k <= i; ++k) {		    
			cummulatedRuntime += resultsArray.get(j).timings.get(k);
		    }
		    currentLine.append(resultsArray.get(j).timings.get(i));
		    currentLine.append(" & ");
		    currentLine.append(cummulatedRuntime);    
		} else {
		    currentLine.append("N/A");
		    currentLine.append(" & ");
		    currentLine.append("N/A");
		}
		
		// solution type column
		currentLine.append(" & ");
		currentLine.append(resultsArray.get(j).solutionTypes.get(i));
		
		// last column case
		if ( j == resultsCount-1){		    
		    currentLine.append(" \\\\ ");
		} else {
		    currentLine.append(" & ");		    
		}
	    }
	}
	
	// rest
	lines.add(new StringBuffer("\\hline"));
	lines.add(new StringBuffer("\\end{tabular}"));	
	lines.add(new StringBuffer("} %tiny")); // tiny
	lines.add(new StringBuffer("\\end{center}"));
	if (sideways)
	    lines.add(new StringBuffer("\\end{sidewaystable}"));
	else 
	    lines.add(new StringBuffer("\\end{table}"));
	
	for (StringBuffer line: lines){
	    console.info(line);
	}
	
	
	/*
	 * Plots
	 */
	String beginfigure = "\\begin{figure}\n\\begin{center}";
	String pgfintro = "\\pgfplotsset{width=\\linewidth,height=.5\\linewidth,compat=1.3}\n\\begin{tikzpicture}\n\\begin{axis}[xlabel=constraint, ylabel = {runtime ["+timeunit+"]}, ymin=0, xmin = 1, xmax="+constraintCount+", unbounded coords=jump, legend style={cells={anchor=west}, legend pos=north west}]";
	String pgfoutro = "\\end{axis}\n\\end{tikzpicture}\n\\end{center}\n";
	String endfigure = "\\end{figure}";
	if (!plotOnlyAccumulated) {
	console.info(beginfigure);
	console.info(pgfintro);
	for (int i = 0; i < resultsCount; ++i){
	    StringBuffer plotline = new StringBuffer("\\addplot plot coordinates{");
	    for(int j = 0; j < constraintCount; ++j){		
		if (resultsArray.get(i).solutionTypes.get(j) == SolutionType.OK_ROUNDING_ERRORS
			|| resultsArray.get(i).solutionTypes.get(j) == SolutionType.OK
			|| resultsArray.get(i).solutionTypes.get(j) != SolutionType.NO_SOLUTION)
		    plotline.append("("+(j+1)+","+resultsArray.get(i).timings.get(j)+")");
		else
		    plotline.append("("+(j+1)+",inf)");
	    }
	    plotline.append("};"); 
	    console.info(plotline);
	}
	
	for (int i = 0; i < resultsCount; ++i){
	    console.info("\\addlegendentry{"+resultsArray.get(i).name+"}");
	}
	console.info(pgfoutro);
	console.info("\\caption{Runtimes for "+description+"}\n\\label{fig:"+description+"}");
	console.info(endfigure);
	}
	
	
	
	console.info(beginfigure);
	console.info(pgfintro);
	for (int i = 0; i < resultsCount; ++i){
	    long cummulatedRuntime = 0L;
	    StringBuffer plotline = new StringBuffer("\\addplot plot coordinates{");
	    for(int j = 0; j < constraintCount; ++j){
		if (resultsArray.get(i).solutionTypes.get(j) != SolutionType.OK
			&& resultsArray.get(i).solutionTypes.get(j) != SolutionType.OK_ROUNDING_ERRORS
			&& resultsArray.get(i).solutionTypes.get(j) != SolutionType.NO_SOLUTION)
		    plotline.append("("+(j+1)+",inf)");
		else {
		    cummulatedRuntime += resultsArray.get(i).timings.get(j);
		    plotline.append("("+(j+1)+","+cummulatedRuntime+")");
		}
	    }
	    plotline.append("};"); 
	    console.info(plotline);
	}
	
	for (int i = 0; i < resultsCount; ++i){
	    console.info("\\addlegendentry{"+resultsArray.get(i).name+"}");
	}
	console.info(pgfoutro);
	console.info("\\caption{Accumulated runtimes for "+description+"}\n\\label{fig:"+description+"}\n");
	console.info(endfigure);
	
	console.info("\n\n\n\\cleardoublepage\n%==================================================================\n\n\n");
    }
    
    
    /**
     * The running example from the diploma thesis.
     * @throws IncorrectSolverException
     * @throws TimeoutException
     * @throws SolverUnableToDecideException
     */
    public void exampleRunningExample() throws IncorrectSolverException, TimeoutException, SolverUnableToDecideException{
	SingleConstraintSet set;
	SingleConstraintSet verifyingSet;
	Solution solution;

	console.info("");
	console.info("--- New Simplex: Running example with weak Inequ.");
	set = problems.runningExample(IneqType.WEAK);	
	verifyingSet = new SingleConstraintSet();
	simplexSolver = simplexSolver.reset();
	for (int i = 0; i <= 2; ++i){
	    console.info("Adding constraint " + set.getConstraint(i));
	    simplexSolver.addConstraint(set.getConstraint(i));
	    verifyingSet.add(set.getConstraint(i));
	}
	solution = simplexSolver.getSolution();
	printSolutionValidation(verifyingSet, solution);
	console.info("Adding constraint " + set.getConstraint(3));
	simplexSolver.addConstraint(set.getConstraint(3));
	solution = simplexSolver.getSolution();
	printSolutionValidation(set, solution);
	console.info("Removing constraint " + set.getConstraint(3));
	solution = simplexSolver.getSolution();
	printSolutionValidation(verifyingSet, solution);
	
	
	
	console.info("--- New Simplex: Running example with strict Inequ.");
	set = problems.runningExample(IneqType.STRICT);
	verifyingSet = new SingleConstraintSet();
	simplexSolver = simplexSolver.reset();
	for (int i = 0; i <= 2; ++i){
	    console.info("Adding constraint " + set.getConstraint(i));
	    simplexSolver.addConstraint(set.getConstraint(i));
	    verifyingSet.add(set.getConstraint(i));
	}
	solution = simplexSolver.getSolution();
	console.info("Constraints: "+set);
	printSolutionValidation(verifyingSet, solution);
	console.info("Adding constraint " + set.getConstraint(3));
	simplexSolver.addConstraint(set.getConstraint(3));
	solution = simplexSolver.getSolution();
	printSolutionValidation(set, solution);
	console.info("Removing constraint " + set.getConstraint(3));
	solution = simplexSolver.getSolution();
	printSolutionValidation(verifyingSet, solution);

	
	console.info("--- CL Simplex: Running example with weak Inequalities");
	set = problems.runningExample(IneqType.WEAK);
	verifyingSet = new SingleConstraintSet();
	simplexSolverCL = simplexSolverCL.reset();
	for (int i = 0; i <= 2; ++i){
	    console.info("Adding constraint " + set.getConstraint(i));
	    simplexSolverCL.addConstraint(set.getConstraint(i));
	    verifyingSet.add(set.getConstraint(i));
	}
	solution = simplexSolverCL.getSolution();
	printSolutionValidation(set, solution);
	console.info("Adding constraint " + set.getConstraint(3));
	simplexSolverCL.addConstraint(set.getConstraint(3));
	solution = simplexSolverCL.getSolution();
	printSolutionValidation(set, solution);
	console.info("Removing constraint " + set.getConstraint(3));
	solution = simplexSolverCL.getSolution();
	printSolutionValidation(verifyingSet, solution);
    }
    
    
    public void runTests() throws IncorrectSolverException, TimeoutException, IncompleteSolutionException, SolverUnableToDecideException{

	/*
	 * examples
	 */
	
	exampleRunningExample();
	
	exampleRoundingError();
	
	exampleBranchAndBoundLoopTest();
	
	/*
	 * comparisons for the results section
	 */

	BACKTRACKING_COUNT = 10;
	
	plotOnlyAccumulated = false;
	
	compareIncrementalBacktrackingTest(Constant.INT);
	
	plotOnlyAccumulated = true;	
		
	comparePostSolving();

	compareOldAndNewSimplex();
	
	BACKTRACKING_COUNT = 0;
	
	compareStrictInequalities();
	
	compareFractionDoubleWrapperTests();

	compareOptions(Constant.INT);
	compareOptions(Constant.DOUBLE);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
	DiplomaThesis diplomaThesis = new DiplomaThesis();
	try {
	    diplomaThesis.runTests();
	} catch (IncorrectSolverException e) {
	    e.printStackTrace();
	} catch (TimeoutException e) {
	    e.printStackTrace();
	} catch (IncompleteSolutionException e) {
	    e.printStackTrace();
	} catch (SolverUnableToDecideException e) {
	    e.printStackTrace();
	}
    }

}
