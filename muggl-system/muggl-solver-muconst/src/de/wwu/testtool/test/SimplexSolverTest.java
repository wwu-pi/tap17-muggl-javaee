package de.wwu.testtool.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.CountingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayoutEscapeOption;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.*;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.testtool.solver.SolverManagerNew;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.Equation;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;
import de.wwu.testtool.solver.tsolver.SimplexSolver;
import de.wwu.testtool.solver.tsolver.SimplexSolverCL;


/**
 * @author Marko Ernsting
 *
 */
@SuppressWarnings("unused")
public class SimplexSolverTest {
    
    // logging
    private Logger logger;
    private Logger console;
    
    private SimplexSolver simplexSolver;

    private void loggerSetup() {
	logger = TesttoolConfig.getLogger();
	logger.setLevel(Level.DEBUG);
	logger.debug("Debug");
	logger.info("Info");

	console = Logger.getLogger("de.wwu.testtool.test.console");
	console.setLevel(Level.INFO);
	ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%-5p [%t:%r] %M(): %m%n"));
	consoleAppender.setName("Console Appender");
	consoleAppender.setLayout(new PatternLayout("[%r] %20.20M(): %m%n") );
	console.addAppender(consoleAppender);
	
    }
    
    public void falseTest() throws IncorrectSolverException, TimeoutException, SolverUnableToDecideException{
	ExampleProblems example = new ExampleProblems();

	simplexSolver.addConstraint( example.falseTest().getConstraint(0) );
	simplexSolver.addConstraint( example.falseTest().getConstraint(1) );
	simplexSolver.getSolution();

	ConstraintExpression falseTest = example.falseTestExpression();
	SolverManager solverManager = new SolverManagerNew();
	solverManager.addConstraint(falseTest);
	console.info(falseTest.verifySolution( solverManager.getSolution() ) );
	
    }
    
    
    public void compareSolvers() {
	ExampleProblems example = new ExampleProblems();
	int count = 20;
	int length = 12;
	console.setLevel(Level.DEBUG);
	
	SimplexSolverCL oldSimplex = SimplexSolverCL.newInstance(null);	
	Solution solution = null;
	try {
	    for (int i = 0; i < count; ++i){
		SingleConstraint constraint = (SingleConstraint) example.toComposedConstraint( example.getRandomDoubleCoeffDoubleVarLinearConstraint(length) );
		
		simplexSolver.addConstraint( constraint );
		solution = simplexSolver.getSolution();
		logger.info("New solver: Solution is " + constraint.validateSolution(solution));

		oldSimplex.addConstraint(constraint);
		solution = oldSimplex.getSolution();
		logger.info("Old solver: Solution is " + constraint.validateSolution(solution));
	    }
	} catch (IncompleteSolutionException e1) {
	    e1.printStackTrace();
	} catch (IncorrectSolverException e1) {
	    e1.printStackTrace();
	} catch (TimeoutException e1) {
	    e1.printStackTrace();
	} catch (SolverUnableToDecideException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}	
    }

    public void randomTest() throws IncorrectSolverException, TimeoutException, SolverUnableToDecideException, IncompleteSolutionException{
	ExampleProblems example = new ExampleProblems();
	int count = 30;
	//int length = 12;
	int length = 12;
	
	console.setLevel(Level.DEBUG);
	//Solver oldSimplex = SimplexSolverCL.newInstance(null);
	Solver oldSimplex = SimplexSolver.newInstance(null);
	
	SingleConstraintSet set = new SingleConstraintSet();
	
	for (int i = 0; i < count; ++i){
	    set.add( (SingleConstraint) example.toComposedConstraint( example.getRandomDoubleCoeffDoubleVarLinearConstraint(length) ));
	}
	
	SingleConstraintSet verifyingSet = new SingleConstraintSet();
	
	for (int i = 0; i < count; ++i){
	    oldSimplex.addConstraint( set.getConstraint(i) );
	    verifyingSet.add( set.getConstraint(i));
	    Solution solution = oldSimplex.getSolution();
	    console.info(solution);
	    if (solution == Solution.NOSOLUTION) 
		console.info("Constraint " + i + " returned NO_SOLUTION.");
	    else 
		console.info( "Constraint " + i + " " + verifyingSet.validateSolution( solution ) );
	}
    }
    
    
    
    
    
    public void diplomaThesisExample(){
	ExampleProblems example = new ExampleProblems();

	
	console.setLevel(Level.DEBUG);
	//Solver oldSimplex = SimplexSolverCL.newInstance(null);
	Solver simplex = SimplexSolver.newInstance(null);
	
	SingleConstraintSet set = example.diplomaThesisExample();		
	SingleConstraintSet verifyingSet = new SingleConstraintSet();
	
	try { // i <=2
	    for (int i = 0; i<=2 ; ++i ){   //SingleConstraint constraint : set){
		SingleConstraint constraint = set.getConstraint(i);
		simplex.addConstraint( constraint );
		verifyingSet.add( constraint );
	    }
	    
	    
	    Solution solution = simplex.getSolution();
	    console.info(solution);
//	    simplex.addConstraint(set.getConstraint(3));
//	    solution = simplex.getSolution();
//	    console.info(solution);
//	    simplex.removeConstraint();
//	    solution = simplex.getSolution();
//	    console.info(solution);


	    if (solution == Solution.NOSOLUTION) 
		console.info("Constraint " + " returned NO_SOLUTION.");
	    else 
		console.info( "Constraint " + " " + verifyingSet.validateSolution( solution ) );
//	    }
	} catch (IncorrectSolverException e1) {
	    e1.printStackTrace();
	} catch (TimeoutException e1) {
	    e1.printStackTrace();
	} catch (SolverUnableToDecideException e1) {
	    e1.printStackTrace();	  
	} catch (IncompleteSolutionException e) {
	    e.printStackTrace();
	}
    }
    
    
    
    public void branchAndBoundTest() throws IncompleteSolutionException{
	ExampleProblems example = new ExampleProblems();
	
	try {
	    SingleConstraintSet verifyingSet = new SingleConstraintSet();
	    
	    console.info("example.branchAndBoundTest()");
	    for (SingleConstraint sc : example.branchAndBoundTest()){
		simplexSolver.addConstraint( sc );
		verifyingSet.add(sc);
	    }
	    testAndLogSolution(simplexSolver.getSolution(), verifyingSet);
	    simplexSolver = simplexSolver.reset();
	    verifyingSet = new SingleConstraintSet();
	    
	    
	    console.info("example.getRandomDoubleCoeffIntVarLinearConstraint()");
	    for (int i = 0; i < 30; ++i){
		SingleConstraint sc = (SingleConstraint) example.toComposedConstraint( example.getRandomDoubleCoeffIntVarLinearConstraint(10));
		simplexSolver.addConstraint( sc );
		verifyingSet.add( sc );
		console.info(i+":");
		testAndLogSolution(simplexSolver.getSolution(), verifyingSet);
	    }
	    simplexSolver = simplexSolver.reset();
	    verifyingSet = new SingleConstraintSet();
	    
	    console.info("example.branchAndBoundLoopTest()");
	    for (SingleConstraint sc : example.branchAndBoundLoopTest()){
		simplexSolver.addConstraint( sc );
	    }
	    testAndLogSolution(simplexSolver.getSolution(), verifyingSet);
	    simplexSolver.reset();
	    
	} catch (IncorrectSolverException e) {
	    e.printStackTrace();
	} catch (TimeoutException e) {
	    e.printStackTrace();
	} catch (SolverUnableToDecideException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void removeTest() throws IncorrectSolverException, TimeoutException, IncompleteSolutionException, SolverUnableToDecideException{
	int constraintCount = 30;
	int variableCount = 12;
	
	ExampleProblems example = new ExampleProblems();
	SingleConstraintSet verifyingSet = new SingleConstraintSet();
	
	console.setLevel(Level.INFO);
	
	SingleConstraintSet cs = new SingleConstraintSet();
	for (int i = 0; i < constraintCount; ++i){
	    cs.add( (SingleConstraint) example.toComposedConstraint( example.getRandomDoubleCoeffIntVarLinearConstraint(variableCount)) );
	}
	
	constraintCount = 26;
	
//	for (int j = 1; j < constraintCount; ++j){
	{   int j = 1;
	
	
	    for (int i = j-1; i < constraintCount; ++i){
		simplexSolver.addConstraint(cs.getConstraint(i));
		verifyingSet.add( cs.getConstraint(i) );
		
		    HasSolutionInformation hasSolutionInformation = simplexSolver.hasSolution();
		    if (simplexSolver.hasSolution().hasSolution()) {
			Solution solution = hasSolutionInformation.getSolution();
			console.info( "["+j+", "+i+"] = "+verifyingSet.validateSolution( solution )+" --> "+solution);
			if (!verifyingSet.validateSolution(solution) ){
			    for (SingleConstraint constraint : verifyingSet) {
				Polynomial poly = ((NumericConstraint) constraint).getPolynomial();
				if ( poly.computeValue(solution).doubleValue() > 0) {
				    console.info(" -> solution: " + poly.computeValue(solution).doubleValue());
				}
			    }	
			} 
		    } else 
			console.info( "["+j+", "+i+"] = noSolution!");

	    }

//	    Solution solution = simplexSolver.getSolution();
//	    if (simplexSolver.hasSolution().hasSolution()) {
//		console.info( "["+j+"] = "+verifyingSet.validateSolution( solution ) );
//		if (!verifyingSet.validateSolution(solution) ){
//		    for (SingleConstraint constraint : verifyingSet) {
//			Polynomial poly = ((NumericConstraint) constraint).getPolynomial();
//			if ( poly.computeValue(solution).doubleValue() > 0) {
//			    console.info(" -> solution: " + poly.computeValue(solution).doubleValue() );
//			}
//		    }	
//		} 
//	    } else 
//		console.info( "["+j+"] = false");
	    

//	    simplexSolver.addConstraint(example.falseTest().getConstraint(0));
//	    simplexSolver.addConstraint(example.falseTest().getConstraint(1));
//	    
//	    solution = simplexSolver.getSolution();
//	    
//	    console.info("Solution: " + solution);
//	    console.info( "make unsolvable : "+ simplexSolver.hasSolution().hasSolution());
//	    
//	    simplexSolver.removeConstraint();
//	    simplexSolver.removeConstraint();
//	    console.info( "make solvable : "+verifyingSet.validateSolution( simplexSolver.getSolution() ) );
	    
	    for (int i = j; i < constraintCount; ++i){
		simplexSolver.removeConstraint();
		verifyingSet.removeLastConstraint();
	    }
	}
	    
	    
	    
//	    SingleConstraintSet cs = new SingleConstraintSet();
//	    for (int i = 0; i < 6; ++i){
//		cs.add( (SingleConstraint) example.toComposedConstraint( example.getRandomDoubleCoeffDoubleVarLinearConstraint(10)) );
//	    }
//	    
//	    simplexSolver.addConstraint(cs.getConstraint(0));
//	    simplexSolver.addConstraint(cs.getConstraint(1));
//	    simplexSolver.getSolution();
//	    simplexSolver.addConstraint(cs.getConstraint(2));
//	    simplexSolver.getSolution();
//	    simplexSolver.removeConstraint();
//	    simplexSolver.getSolution();
//	    simplexSolver.addConstraint(cs.getConstraint(3));
//	    simplexSolver.getSolution();
//	    simplexSolver.removeConstraint();
//	    simplexSolver.removeConstraint();
//	    simplexSolver.removeConstraint();
//	    simplexSolver.getSolution();
    }

    
    public void run() throws IncorrectSolverException, TimeoutException, SolverUnableToDecideException, IncompleteSolutionException{
	simplexSolver = new SimplexSolver();	
	loggerSetup();
	simplexSolver.setDebugLogger(logger);
	
	//compareSolvers();
	
	//randomTest();
	
	//diplomaThesisExample();
	
	
	branchAndBoundTest();
	
	//removeTest();

	//compareNewAndOldSimplex();
	
	//testOldSolver();
	
	//falseTest();
    }
    
    public static void main(String[] args) {
	SimplexSolverTest simplexTest = new SimplexSolverTest();
	try {
	    simplexTest.run();
	} catch (IncorrectSolverException e) {
	    e.printStackTrace();
	} catch (TimeoutException e) {
	    e.printStackTrace();
	} catch (SolverUnableToDecideException e) {
	    e.printStackTrace();
	} catch (IncompleteSolutionException e) {
	    e.printStackTrace();
	}
    }

    public void testOldSolver(){
	SolverManager solverManager = new SolverManagerNew();
	ExampleProblems example = new ExampleProblems();	
	ConstraintExpression test = example.typeCastExpression();

	solverManager.addConstraint( test );
	
	Solution solution = null;
	//solverManager.getConstraintStack().printStack(console);
	try {
	    solution = solverManager.getSolution();
	    console.info("Solution = " + solution);
	} catch (SolverUnableToDecideException e) {
	    console.info("SolverUnableToDecideException\n" + e);
	} catch (TimeoutException e) {
	    console.info("TimeoutException\n" + e);
	}

	console.debug("Solution accepted: " + test.verifySolution(solution) );
	
	solverManager.removeConstraint();
    }
    
    
    
    public void compareNewAndOldSimplex(){
	int loopCount = 1;
	int constraintCount = 20;
	int constraintLength = 6;
	
	console.setLevel(Level.INFO);

	ExampleProblems example = new ExampleProblems();
	SingleConstraintSet set = new SingleConstraintSet();
	for (int i = 0; i < constraintCount; ++i){
	    set.add( (SingleConstraint) example.toComposedConstraint( 
		    example.getRandomDoubleCoeffDoubleVarLinearConstraint(constraintLength))
		    //example.getRandomDoubleCoeffDoubleVarLinearConstraint(constraintLength)) 
		    );
	}
	    

	SimplexSolverCL oldSimplex = SimplexSolverCL.newInstance(null);
	SimplexSolver newSimplex = simplexSolver;
	
	long timer = 0;
	long oldSimplexTime;
	long newSimplexTime;
	long addTime;
	long solutionTime;
	
	try {
//	    console.info("Timing "+constraintCount+" random constraints with random double coefficients of max length "+constraintLength+":");
//	    console.info("adding all constraints, getSolution(), resetting solvers");
//	    timer = System.nanoTime();
//	    for (int i = 0; i <= loopCount; ++i){
//		for (int j = 0; j < constraintCount; ++j){
//		    newSimplex.addConstraint( set.getConstraint(j) ); 
//		}
//		newSimplex.getSolution();
//		simplexSolver newSimplex.reset();
//	    }
//	    newSimplexTime = System.nanoTime() - timer;
//	    console.info("new simplex: " + newSimplexTime ); 
//	    timer = System.nanoTime();
//	    for (int i = 0; i <= loopCount; ++i){
//		for (int j = 0; j < constraintCount; ++j){
//		    oldSimplex.addConstraint( set.getConstraint(j) ); 
//		}
//		oldSimplex.getSolution();
//		simplexSolver oldSimplex.reset();
//	    }
//	    oldSimplexTime = System.nanoTime() - timer;
//	    console.info("old simplex: " + oldSimplexTime );
//	    console.info("ratio: " + ((double) oldSimplexTime) / ((double) newSimplexTime) );
//	    
	    
	    
	    
	    
//	    console.info("normal muggl behavior: adding one constraint after the other, getSolution() inbetween, removing all constraints ");
//	    timer = System.nanoTime();
//	    for (int i = 0; i < loopCount; ++i){
//		
//		SingleConstraintSet verifyingSet = new SingleConstraintSet();
//		
//		for(int j = 0; j < set.getConstraintCount(); ++j){
//		    addTime = System.nanoTime();
//		    newSimplex.addConstraint(set.getConstraint(j));
//		    console.info("Adding:     " + (System.nanoTime() - addTime));
//		    solutionTime = System.nanoTime();
//		    Solution solution = newSimplex.getSolution();
//		    console.info("Solution: " + (System.nanoTime() - solutionTime));
//		    verifyingSet.add( set.getConstraint(i));
//		    //console.info("Constraint = " + set.getConstraint(j));
//		    //console.info( "Constraint " + j + " " + verifyingSet.validateSolution( solution ) );
//
//		}
//		newSimplex newSimplex.reset();
//	    }
//	    newSimplexTime = System.nanoTime() - timer;
//	    console.info("new simplex: " + newSimplexTime );  
//	    timer = System.nanoTime();
//	    for (int i = 0; i < loopCount; ++i){
//		
//		SingleConstraintSet verifyingSet = new SingleConstraintSet();
//		
//		for(int j = 0; j < set.getConstraintCount(); ++j){
//		    addTime = System.nanoTime();
//		    oldSimplex.addConstraint(set.getConstraint(j));
//		    console.info("Adding:      " + (System.nanoTime() - addTime));
//		    solutionTime = System.nanoTime();
//		    Solution solution = oldSimplex.getSolution();
//		    console.info("Solution: " + (System.nanoTime() - solutionTime));
//		    verifyingSet.add( set.getConstraint(i));
//		    console.info("Constraint = " + set.getConstraint(j));
//		    console.info( "Constraint " + j + " " + verifyingSet.validateSolution( solution ) );
//
//		}
//	oldSimplex=oldSimplex.reset();
//	    }
//	    oldSimplexTime = System.nanoTime() - timer;
//	    console.info("old simplex: " + oldSimplexTime );
//	    console.info("ratio: " + ((double) oldSimplexTime) / ((double) newSimplexTime) );


	    console.info("advanced muggl behavior: adding three constraints, getSolution(), removing one constraint, adding another, getSolution(), removing all remaining constraints");
	    timer = System.nanoTime();
	    for (int i = 0; i <= loopCount; ++i){
		for (int j = 1; j < constraintCount; ++j){
		    for (int k = j-1; k < constraintCount; ++k){
			newSimplex.addConstraint(set.getConstraint(k));
			newSimplex.getSolution();
		    }
		    for (int k = j; k < constraintCount; ++k){
			newSimplex.removeConstraint();
		    }
		}
	    }
	    newSimplexTime = System.nanoTime() - timer;
	    console.info("new simplex: " + newSimplexTime );
	    
	    timer = System.nanoTime();
	    for (int i = 0; i <= loopCount; ++i){
		for (int j = 1; j < constraintCount; ++j){
		    for (int k = j-1; k < constraintCount; ++k){
			oldSimplex.addConstraint(set.getConstraint(k));
			oldSimplex.getSolution();
		    }
		    for (int k = j; k < constraintCount; ++k){
			oldSimplex.removeConstraint();
		    }
		}
	    }
	    oldSimplexTime = System.nanoTime() - timer;
	    console.info("old simplex: " + oldSimplexTime );
	    console.info("ratio: " + ((double) oldSimplexTime) / ((double) newSimplexTime) );

	    
//	    console.info("advanced muggl behavior with constraint disabling");
//	    
//	    timer = System.nanoTime();
//	    for (int i = 0; i <= loopCount; ++i){
//		newSimplex.addConstraint(set.getConstraint(0));
//		newSimplex.getSolution();
//		newSimplex.addConstraint(set.getConstraint(1));
//		newSimplex.getSolution();
//		//newSimplex.disableConstraint(set.getConstraint(1));
//		newSimplex.removeConstraint();
//		newSimplex.addConstraint(set.getConstraint(2));
//		newSimplex.getSolution();
//		newSimplex.removeConstraint();
//		//newSimplex.enableConstraint(set.getConstraint(1));
//		newSimplex.addConstraint(set.getConstraint(1));
//		newSimplex.addConstraint(set.getConstraint(3));
//		newSimplex.getSolution();
//		newSimplex.removeConstraint();
//		newSimplex.removeConstraint();
//		newSimplex.removeConstraint();
//	    }
//	    newSimplexTime = System.nanoTime() - timer;
//	    console.info("new simplex: " + newSimplexTime );
//	    timer = System.nanoTime();
//	    for (int i = 0; i <= loopCount; ++i){
//		oldSimplex.addConstraint(set.getConstraint(0));
//		oldSimplex.getSolution();
//		oldSimplex.addConstraint(set.getConstraint(1));
//		oldSimplex.getSolution();
//		oldSimplex.removeConstraint();
//		oldSimplex.addConstraint(set.getConstraint(2));
//		oldSimplex.getSolution();
//		oldSimplex.removeConstraint();
//		oldSimplex.addConstraint(set.getConstraint(1));
//		oldSimplex.addConstraint(set.getConstraint(3));
//		oldSimplex.getSolution();
//		oldSimplex.removeConstraint();
//		oldSimplex.removeConstraint();
//		oldSimplex.removeConstraint();
//	    }
//	    oldSimplexTime = System.nanoTime() - timer;
//	    console.info("old simplex: " + oldSimplexTime );
//	    console.info("ratio: " + oldSimplexTime / newSimplexTime ); 
//
//	    
	} catch (IncorrectSolverException e) {
	    e.printStackTrace();
	} catch (TimeoutException e) {
	    e.printStackTrace();
//	} catch (IncompleteSolutionException e) {
	    
	} catch (SolverUnableToDecideException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void testAndLogSolution(Solution solution, SingleConstraintSet cs){
	    console.info(solution);
	    if (solution == Solution.NOSOLUTION) 
		console.info("Constraint " + " returned NO_SOLUTION.");
	    else
		try {
		    console.info( "Constraint " + " " + cs.validateSolution( solution ) );
		} catch (IncompleteSolutionException e) {
		    e.printStackTrace();
		}
    }
    
}
