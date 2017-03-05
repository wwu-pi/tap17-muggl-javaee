package de.wwu.testtool.solver.tsolver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.testtool.solver.MuconstSolverManager;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.numbers.Fraction;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;

/**
 * @author Christoph Lembeck
 */
public class LeastSquaresSolver implements Solver {

    public static LeastSquaresSolver newInstance(SolverManager solverManager) {
	return new LeastSquaresSolver(solverManager);
    }

    protected SolverManager solverManager;

    private LeastSquaresSolver(SolverManager solverManager) {
	this.solverManager = solverManager;
	constraints = new SingleConstraintSet();
    }

    protected NumberFactory numberFactory = new Fraction();

    protected SingleConstraintSet constraints;

    public void addConstraint(SingleConstraint constraint){
	constraints.add(constraint);
    }

    public void addConstraintSet(SingleConstraintSet constraintSet){
	constraints.addAll(constraintSet);
    }

    public List<Class<? extends Solver>> getRequiredSubsolvers() {
	return null;
    }

    public Solution getSolution(){
	// 2010.07.16: Replaced the GlassTT logger with the Muggl logger. It is faster and saves a lot of memory.
	Logger solverLogger = Globals.getInst().solverLogger;
	if (solverLogger.isDebugEnabled()) {
	    solverLogger.debug("KQSolver"); // Changed 2008.02.05
	    solverLogger.debug("---\n" + constraints + "\n---"); // Changed 2008.02.05
	}
	Polynomial[] polynomial = new Polynomial[constraints.getConstraintCount()]; // Changed 2008.02.05
	Polynomial lsPolynomial = new Polynomial();
	for (int i = 0; i < constraints.getConstraintCount(); i++) {
	    polynomial[i] = ((NumericConstraint) constraints.getConstraint(i))
	    .getPolynomial();
	    lsPolynomial = lsPolynomial.addPolynomial(polynomial[i]
	                                                         .multiplyPolynomial(polynomial[i]));
	}
	TreeSet<NumericVariable> vars = new TreeSet<NumericVariable>();
	lsPolynomial.collectNumericVariables(vars);
	NumericVariable[] variables = vars
	.toArray(new NumericVariable[vars.size()]);
	HashMap<NumericVariable, NumberWrapper> position = new HashMap<NumericVariable, NumberWrapper>();
	Polynomial[] derivates = new Polynomial[variables.length];
	for (int i = 0; i < variables.length; i++)
	    derivates[i] = lsPolynomial.getDerivate(variables[i]);
	if (solverLogger.isDebugEnabled()) {
	    solverLogger.debug("kqPolynomial: " + lsPolynomial); // Changed 2008.02.05
	    solverLogger.debug("deriavtes: " + Arrays.toString(derivates)); // Changed 2008.02.05
	}
	Solution testSolution = new Solution();
	for (int i = 0; i < variables.length; i++) {
	    NumericVariable var = variables[i];
	    position.put(var, numberFactory.getZero());
	    testSolution.addBinding(var, position.get(var).toNumericConstant(
		    var.getType()));
	}
	// testSolution.replaceBinding(variables[0], NumericConstant.getInstance(9,
	// variables[0].getType()));
	// testSolution.replaceBinding(variables[1], NumericConstant.getInstance(8,
	// variables[1].getType()));
	try {
	    NumberWrapper newValue = lsPolynomial.computeValue(position);
	    NumberWrapper oldValue;
	    if (solverLogger.isDebugEnabled()) solverLogger.debug("teste " + testSolution); // Changed 2008.02.05
	    int counter = 0;
	    while (!((MuconstSolverManager)solverManager).verifySolution(testSolution)) {
		oldValue = newValue;
		NumberWrapper maxGradient = null;
		int maxIDX = 0;
		maxGradient = derivates[0].computeValue(position);
		for (int i = 1; i < variables.length; i++) {
		    NumberWrapper gradient = derivates[i].computeValue(position);
		    if (gradient.abs().greaterThan(maxGradient.abs())) {
			maxGradient = gradient;
			maxIDX = i;
		    }
		}
		NumericVariable var = variables[maxIDX];
		NumberWrapper oldPosition = position.get(variables[maxIDX]);
		NumberWrapper movement = oldValue.div(maxGradient);
		NumberWrapper newPosition = oldPosition.sub(movement);
		position.put(variables[maxIDX], newPosition);
		testSolution.replaceBinding(var, newPosition.toNumericConstant(var
			.getType()));
		newValue = lsPolynomial.computeValue(position);
		while (newValue.greaterThan(oldValue)) {
		    // wheight the movement by the real difference in relation to the
		    // expected difference
		    movement = movement.mult(oldValue).div(oldValue.add(newValue));
		    newPosition = oldPosition.sub(movement);
		    if (newPosition.equals(oldPosition)) {
			throw new InternalError("does not converge");
		    }
		    position.put(variables[maxIDX], newPosition);
		    testSolution.replaceBinding(var, newPosition.toNumericConstant(var
			    .getType()));
		    newValue = lsPolynomial.computeValue(position);
		    // SolverLogger.getInst().info("movement " + movement);
		}
		if (counter++ == 10) {
		    if (solverLogger.isDebugEnabled()) {
			solverLogger.debug("teste " + testSolution + " : "
				+ newValue.doubleValue() + ", " + newValue + " " + position); // Changed 2008.02.05
		    }
		    counter = 0;
		}
	    }
	} catch (IncompleteSolutionException ise) {
	    throw new InternalError(ise.toString());
	}
	return testSolution;
    }

    public boolean handlesEquations() {
	return true;
    }

    public boolean handlesIntegerEquations() {
	return false;
    }

    public boolean handlesNonlinearProblems() {
	return true;
    }

    public boolean handlesNumericProblems() {
	return true;
    }

    public boolean handlesStrictInequalities() {
	return false;
    }

    public boolean handlesStrictIntegerInequalities() {
	return false;
    }

    public boolean handlesWeakInequalities() {
	return true;
    }

    public boolean handlesWeakIntegerInequalities() {
	return false;
    }

    public HasSolutionInformation hasSolution() {
	return new HasSolutionInformation(getSolution());
    }

    public void removeConstraint() {
	// CLTODO implementieren
    }

    public Solver reset() {
	return newInstance(solverManager);
    }

    public String getName() {
	return "LeastSquaresSolver";
    }
}
