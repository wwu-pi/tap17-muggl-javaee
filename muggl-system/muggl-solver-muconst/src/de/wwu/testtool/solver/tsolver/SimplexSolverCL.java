package de.wwu.testtool.solver.tsolver;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.testtool.conf.SimplexSolverConfig;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.Monomial;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.testtool.tools.RandomSingleton;
import de.wwu.testtool.tools.Timer;

/**
 * @author Christoph Lembeck
 */
public class SimplexSolverCL implements Solver{

    protected static NumberFactory defaultNumberFactory;
    
    static{
	SimplexSolverConfig simplexConfig = SimplexSolverConfig.getInstance();
	defaultNumberFactory = simplexConfig.getDefaultNumberFactory();
    }

    /**
     * Returns a new SimplexSolverCL Instance.
     * @param solverManager
     * @return
     */
    public static SimplexSolverCL newInstance(SolverManager manager){
	return new SimplexSolverCL(null, defaultNumberFactory);
    }

    protected SingleConstraintSet constraints;

    /**
     * The object that is responsible for generating the log.
     */
    protected Logger texLogger;
    
    
    /**
     * Timeout Timer.
     */
    long timeout;
    public void setTimeout(long ms){
	timeout = ms;
    }
    Timer timer;
        
    /**
     * The factory object for the numbers used for the internal calculations.
     */
    protected NumberFactory numberFactory;

    protected SolverManager solverManager;

    /**
     * Stores the initial simplex table.
     */
    protected SimplexTable starttable;

    /**
     * Stores the variables of the original initial problem in the same order
     * their coefficients are used later in the individual simplex tables.
     */
    protected Vector<Variable> variables;

    
    /**
     * Called only by {@link newInstance} method.
     * @param solverManager
     * @param numberFactory
     */
    private SimplexSolverCL(SolverManager solverManager, NumberFactory numberFactory){
	this.solverManager = solverManager;
	this.texLogger = TesttoolConfig.getTexLogger();
	texLogger.setLevel(Level.INFO);
	this.numberFactory = numberFactory;
	
	timeout = SimplexSolverConfig.getInstance().getTimeout();
    }

    /**
     * Creates a new simplex solver with the given values for its starting simplex
     * table.
     * 
     * Called only by Elimination Solver.
     * @param solverManager the SolverManager instance this solver belongs to.
     * @param variablesIt an iterator over the variables used in the simplex
     * table.
     * @param coefficients the coefficient matrix of the simplex table without
     * widening it to handle negative values and without any slack or artificial
     * variables.
     * @param rhs the right hand side values of the equations.
     * @param isEquation boolean indicator for the existence of an equation or
     * an inequation for each row. <i>true</i> indicates an equation, <i>false</i>
     * a weak inequation.
     * @param logger the object that is responsible for generating the log.
     * @param numberFactory the factory object that generates numbers with the
     * desired precision for the internal calculations.
     */
    protected SimplexSolverCL(SolverManager solverManager, NumericVariable[] variablesIt,
	    NumberWrapper[][] coefficients, NumberWrapper[] rhs,
	    boolean[] isEquation, Logger logger,
	    NumberFactory numberFactory){
	this.solverManager = solverManager;
	initTableau(variablesIt, coefficients, rhs, isEquation, logger, numberFactory);
    }

    /**
     * Adds a new constraint to the system of constraints a solution should be
     * calculated for by this solver.
     * @param constraint the additional constraint.
     */
    public void addConstraint(SingleConstraint constraint){
	if (constraints == null)
	    constraints = new SingleConstraintSet(constraint);
	else
	    constraints.add(constraint);
	initialize();
    }

    public void addConstraintSet(SingleConstraintSet set){
	for (SingleConstraint constraint: set)
	    addConstraint(constraint);
    }

    /**
     * Returns the name of the solver for displaying it in some user interfaces or
     * logfiles.
     * @return the name of the solver.
     */
    public String getName(){
	return "SimplexSolverCL";
    }

    public List<Class<? extends Solver>> getRequiredSubsolvers(){
	return null;
    }

    /**
     * Calculates a single solution that satisfies the prior added constraints.
     * @return the solution that satisfies the constraints passed to this solver
     * or Solution.NOSOLUTION if definitively no such solution exists.
     * @throws IncorrectSolverException if the solver is not able to handle the
     * actual set of constraints.
     * @throws TimeoutException 
     * @see de.wwu.muggl.solvers.Solution#NOSOLUTION
     */
    public Solution getSolution() throws IncorrectSolverException, TimeoutException {
	if (texLogger.isDebugEnabled()) texLogger.debug("TEX: getsolution\\\\");

	if (timeout > 0){
	    timer = new Timer(timeout);
	    timer.run();
	} else {
	    timer = new Timer(0);
	}
	
	Solution result = null;
	if (starttable == null){
	    result = Solution.NOSOLUTION;
	} else {
	    result = getSolution(starttable);
	}
	return result;
    }
    

    /**
     * Calculates the solution out of the passed simplex table argument. This
     * method will invoke itself recursively to ensure the finding of integer
     * values for each integer variable in the constraint system using the branch
     * and bound method if necessary.
     * 
     * Is called recursively for Branch and Bound.
     * @param tab the simplex table a solution should be calculated for.
     * @return the found solution
     * @throws IncorrectSolverException
     * @throws TimeoutException 
     */
    protected Solution getSolution(SimplexTable tab) throws IncorrectSolverException, TimeoutException {	

	// if we have artificial variables, we do not have a valid basis and need to run phase 1 of the simplex algorithm
	// phase one: find an initial basis-solution for the original problem
	if (tab.artificialVariablesCount > 0) {
	    if (texLogger.isDebugEnabled()) texLogger.debug("Phase 1\\\\");
	    
	    
	    boolean valid;
	    int degeneratedArtificialVariableRow;
	    int degeneratedArtificialVariableCol;
	    
	    // find a valid basis solution without artificial variables
	    do {
		// (try to) find optimal solution with standard pivoting rule
		while (!tab.isOptimum){
		    /*
		     * Timeouts!
		     */
		    if (timer.timeout())
			throw new TimeoutException("Timeout " + timeout + " reached. No solution found.");
		    performSingleTransformation(tab);
		}
		
		// maybe artificial variables are left in the basis
		// we try to do pivot transformations to get them out
		valid = true;
		degeneratedArtificialVariableRow = -1;
		degeneratedArtificialVariableCol = -1;
		int row = 0;
		// check all pivotColumns until an artificial variable is found (or not)
		while (valid && (row < tab.pivotColumns.length)){
		    if (tab.pivotColumns[row] >= variables.size() * 2 + tab.slackVariablesCount){
			// an artificial variable is inside the base
			valid = false;
			if (tab.rhs[row].isZero()){
			    // the right hand side of the condition is zero, so it may
			    // be possible to remove the artificial variable from the base
			    for (int col = 0; (col < variables.size() * 2 + tab.slackVariablesCount) && (degeneratedArtificialVariableCol == -1); col++){
				if (!tab.isPivotColumn[col] && !tab.array[row][col].isZero()){
				    degeneratedArtificialVariableCol = col;
				    degeneratedArtificialVariableRow = row;
				}
			    }
			}
		    }
		    row++;
		}
		// if an artificial variable is found in the basis
		if (!valid && degeneratedArtificialVariableCol != -1){
		    // SolverLogger.getInst().info(" * * * * Artificial Variable in row " + degeneratedArtificialVariableRow + " column " + tab.pivotColumns[degeneratedArtificialVariableRow] + " will be replaced by column " + degeneratedArtificialVariableCol);
		    tab = performSingleTransformation(tab, degeneratedArtificialVariableCol, degeneratedArtificialVariableRow);
		}
	    } while (!valid && degeneratedArtificialVariableCol != -1);
	    
	    // phase 1 failed, i.e. there are no feasible points for the original problem, there is no solution
	    if (!valid){
		return Solution.NOSOLUTION;
	    }

	    // copy the left part of the simplex table into a new table without
	    // the columns for the artificial variables.
	    SimplexTable newTable = new SimplexTable(tab.width - tab.artificialVariablesCount, tab.height);
	    newTable.slackVariablesCount = tab.slackVariablesCount;
	    newTable.artificialVariablesCount = tab.artificialVariablesCount;
	    Arrays.fill(newTable.costs, 0, variables.size() * 2, numberFactory.getOne());
	    Arrays.fill(newTable.costs, variables.size() * 2, newTable.costs.length, numberFactory.getZero());
	    System.arraycopy(tab.rhs, 0, newTable.rhs, 0, newTable.height);
	    newTable.isOptimum = false;
	    System.arraycopy(tab.isPivotColumn, 0, newTable.isPivotColumn, 0, newTable.isPivotColumn.length);
	    System.arraycopy(tab.pivotColumns, 0, newTable.pivotColumns, 0, newTable.pivotColumns.length);
	    for (int row = 0; row < newTable.array.length; row++)
		System.arraycopy(tab.array[row], 0, newTable.array[row], 0, newTable.array[row].length);
	    tab = newTable;
	} else {
	    if (texLogger.isDebugEnabled()) texLogger.debug("Phase 1 omitted: No artificial variables present. \\\\");
	}

	
	// phase two: find the optimal solution for the origin problem
	
	if (texLogger.isDebugEnabled()) texLogger.debug("Phase 2\\\\");

	while (!tab.isOptimum){
	    performSingleTransformation(tab);
	}

	// uses branch and bound and recursive getSolution(ceilingtable / floortable) calling	
	
	int invalidIDX = -1;
	for (int i = 0; i < variables.size() * 2; i++){
	    NumberWrapper value = tab.getVariableValue(i);
	    NumericVariable var = (NumericVariable)variables.get(i / 2);
	    if (Term.isIntegerType(var.getType()) && !value.isInteger())
		invalidIDX = i;
	}
	if (invalidIDX == -1){
	    Solution sol = new Solution();
	    for (int i = 0; i < variables.size(); i++){
		NumberWrapper value = tab.getVariableValue(2 * i).sub(tab.getVariableValue(2 * i + 1));
		NumericVariable var = (NumericVariable)variables.get(i);
		NumericConstant ncValue;
		switch (var.getType()){
		case Expression.BYTE:
		case Expression.CHAR:
		case Expression.SHORT:
		case Expression.INT:
		    ncValue = IntConstant.getInstance((int)value.doubleValue());
		    break;
		case Expression.LONG:
		    ncValue = LongConstant.getInstance((long)value.doubleValue());
		    break;
		case Expression.FLOAT:
		    ncValue = FloatConstant.getInstance((float)value.doubleValue());
		    break;
		default:
		    ncValue = DoubleConstant.getInstance(value.doubleValue());
		}
		sol.addBinding(var, ncValue);
	    }
	    return sol;
	} else {
	    int invalidVarIDX = invalidIDX / 2;
	    NumberWrapper value = tab.getVariableValue(invalidIDX);
	    if (invalidIDX % 2 == 1)
		value = value.negate();
	    NumberWrapper floor = value.floor();
	    NumberWrapper ceil = value.ceil();
	    int invalidRow = 0;
	    while ((invalidRow < tab.height) && (tab.pivotColumns[invalidRow] != invalidIDX))
		invalidRow++;
	    if (invalidRow == tab.height)
		throw new InternalError();
	    SimplexTable floorTable;
	    if (floor.greaterOrEqual(numberFactory.getZero())){
		// generate a new simplex table with the additional equation
		// x_invalidIDX + slack_new = ceil
		floorTable = new SimplexTable(tab.width + 2, tab.height + 1);
		floorTable.artificialVariablesCount = 1;
		floorTable.slackVariablesCount = tab.slackVariablesCount + 1;
		Arrays.fill(floorTable.costs, 0, tab.width + 1, numberFactory.getZero());
		floorTable.costs[floorTable.width - 1] = numberFactory.getOne();
		System.arraycopy(tab.pivotColumns, 0, floorTable.pivotColumns, 0, tab.height);
		System.arraycopy(tab.isPivotColumn, 0, floorTable.isPivotColumn, 0, tab.width);
		floorTable.pivotColumns[floorTable.height - 1] = floorTable.width - 2;
		floorTable.pivotColumns[invalidRow] = floorTable.width - 1;
		floorTable.isPivotColumn[invalidIDX] = false;
		floorTable.isPivotColumn[floorTable.width - 1] = true;
		floorTable.isPivotColumn[floorTable.width - 2] = true;
		System.arraycopy(tab.rhs, 0, floorTable.rhs, 0, tab.height);
		floorTable.rhs[floorTable.height - 1] = floor;
		for (int i = 0; i < tab.height; i++){
		    System.arraycopy(tab.array[i], 0, floorTable.array[i], 0, tab.width);
		    floorTable.array[i][floorTable.width - 2] = numberFactory.getZero();
		    if (i == invalidRow)
			floorTable.array[i][floorTable.width - 1] = numberFactory.getOne();
		    else
			floorTable.array[i][floorTable.width - 1] = numberFactory.getZero();
		}
		Arrays.fill(floorTable.array[floorTable.height - 1], numberFactory.getZero());

		floorTable.array[floorTable.height - 1][invalidVarIDX * 2] = numberFactory.getOne();
		floorTable.array[floorTable.height - 1][invalidVarIDX * 2 + 1] = numberFactory.getMinusOne();

		floorTable.array[floorTable.height - 1][floorTable.width - 2] = numberFactory.getOne();
		floorTable.isOptimum = false;
	    } else {
		// generate a new simplex table with the additional equation
		// -x_invalidIDX - slack_new + artificial_new = -ceil
		floorTable = new SimplexTable(tab.width + 3, tab.height + 1);
		floorTable.artificialVariablesCount = 2;
		floorTable.slackVariablesCount = tab.slackVariablesCount + 1;
		Arrays.fill(floorTable.costs, 0, tab.width + 1, numberFactory.getZero());
		floorTable.costs[floorTable.width - 1] = numberFactory.getOne();
		floorTable.costs[floorTable.width - 2] = numberFactory.getOne();
		System.arraycopy(tab.pivotColumns, 0, floorTable.pivotColumns, 0, tab.height);
		System.arraycopy(tab.isPivotColumn, 0, floorTable.isPivotColumn, 0, tab.width);
		floorTable.pivotColumns[floorTable.height - 1] = floorTable.width - 2;
		floorTable.pivotColumns[invalidRow] = floorTable.width - 1;
		floorTable.isPivotColumn[invalidIDX] = false;
		floorTable.isPivotColumn[floorTable.width - 1] = true;
		floorTable.isPivotColumn[floorTable.width - 2] = true;
		System.arraycopy(tab.rhs, 0, floorTable.rhs, 0, tab.height);
		floorTable.rhs[floorTable.height - 1] = floor.negate();
		for (int i = 0; i < tab.height; i++){
		    System.arraycopy(tab.array[i], 0, floorTable.array[i], 0, tab.width);
		    floorTable.array[i][floorTable.width - 3] = numberFactory.getZero();
		    floorTable.array[i][floorTable.width - 2] = numberFactory.getZero();
		    if (i == invalidRow)
			floorTable.array[i][floorTable.width - 1] = numberFactory.getOne();
		    else
			floorTable.array[i][floorTable.width - 1] = numberFactory.getZero();
		}
		Arrays.fill(floorTable.array[floorTable.height - 1], numberFactory.getZero());
		floorTable.array[floorTable.height - 1][invalidVarIDX * 2] = numberFactory.getMinusOne();
		floorTable.array[floorTable.height - 1][invalidVarIDX * 2 + 1] = numberFactory.getOne();
		floorTable.array[floorTable.height - 1][floorTable.width - 3] = numberFactory.getMinusOne();
		floorTable.array[floorTable.height - 1][floorTable.width - 2] = numberFactory.getOne();
		floorTable.isOptimum = false;
	    }
	    SimplexTable ceilTable;
	    if (floor.greaterOrEqual(numberFactory.getZero())){
		// generate a new simplex table with the additional equation
		// -x_invalidIDX - slack_new + artificial_new = -ceil
		ceilTable = new SimplexTable(tab.width + 3, tab.height + 1);
		ceilTable.artificialVariablesCount = 2;
		ceilTable.slackVariablesCount = tab.slackVariablesCount + 1;
		Arrays.fill(ceilTable.costs, 0, tab.width + 1, numberFactory.getZero());
		ceilTable.costs[ceilTable.width - 1] = numberFactory.getOne();
		ceilTable.costs[ceilTable.width - 2] = numberFactory.getOne();
		System.arraycopy(tab.pivotColumns, 0, ceilTable.pivotColumns, 0, tab.height);
		System.arraycopy(tab.isPivotColumn, 0, ceilTable.isPivotColumn, 0, tab.width);
		ceilTable.pivotColumns[ceilTable.height - 1] = ceilTable.width - 2;
		ceilTable.pivotColumns[invalidRow] = ceilTable.width - 1;
		ceilTable.isPivotColumn[invalidIDX] = false;
		ceilTable.isPivotColumn[ceilTable.width - 1] = true;
		ceilTable.isPivotColumn[ceilTable.width - 2] = true;
		System.arraycopy(tab.rhs, 0, ceilTable.rhs, 0, tab.height);
		ceilTable.rhs[ceilTable.height - 1] = ceil;
		for (int i = 0; i < tab.height; i++){
		    System.arraycopy(tab.array[i], 0, ceilTable.array[i], 0, tab.width);
		    ceilTable.array[i][ceilTable.width - 3] = numberFactory.getZero();
		    ceilTable.array[i][ceilTable.width - 2] = numberFactory.getZero();
		    if (i == invalidRow)
			ceilTable.array[i][ceilTable.width - 1] = numberFactory.getOne();
		    else
			ceilTable.array[i][ceilTable.width - 1] = numberFactory.getZero();
		}
		Arrays.fill(ceilTable.array[ceilTable.height - 1], numberFactory.getZero());

		ceilTable.array[ceilTable.height - 1][invalidVarIDX * 2] = numberFactory.getOne();
		ceilTable.array[ceilTable.height - 1][invalidVarIDX * 2 + 1] = numberFactory.getMinusOne();
		ceilTable.array[ceilTable.height - 1][ceilTable.width - 3] = numberFactory.getMinusOne();
		ceilTable.array[ceilTable.height - 1][ceilTable.width - 2] = numberFactory.getOne();
		ceilTable.isOptimum = false;
	    } else {
		// generate a new simplex table with the additional equation
		// x_invalidIDX + slack_new = ceil
		ceilTable = new SimplexTable(tab.width + 2, tab.height + 1);
		ceilTable.artificialVariablesCount = 1;
		ceilTable.slackVariablesCount = tab.slackVariablesCount + 1;
		Arrays.fill(ceilTable.costs, 0, tab.width + 1, numberFactory.getZero());
		ceilTable.costs[ceilTable.width - 1] = numberFactory.getOne();
		System.arraycopy(tab.pivotColumns, 0, ceilTable.pivotColumns, 0, tab.height);
		System.arraycopy(tab.isPivotColumn, 0, ceilTable.isPivotColumn, 0, tab.width);
		ceilTable.pivotColumns[ceilTable.height - 1] = ceilTable.width - 2;
		ceilTable.pivotColumns[invalidRow] = ceilTable.width - 1;
		ceilTable.isPivotColumn[invalidIDX] = false;
		ceilTable.isPivotColumn[ceilTable.width - 1] = true;
		ceilTable.isPivotColumn[ceilTable.width - 2] = true;
		System.arraycopy(tab.rhs, 0, ceilTable.rhs, 0, tab.height);
		ceilTable.rhs[ceilTable.height - 1] = ceil.negate();
		for (int i = 0; i < tab.height; i++){
		    System.arraycopy(tab.array[i], 0, ceilTable.array[i], 0, tab.width);
		    ceilTable.array[i][ceilTable.width - 2] = numberFactory.getZero();
		    if (i == invalidRow)
			ceilTable.array[i][ceilTable.width - 1] = numberFactory.getOne();
		    else
			ceilTable.array[i][ceilTable.width - 1] = numberFactory.getZero();
		}
		Arrays.fill(ceilTable.array[ceilTable.height - 1], numberFactory.getZero());

		ceilTable.array[ceilTable.height - 1][invalidVarIDX * 2] = numberFactory.getMinusOne();
		ceilTable.array[ceilTable.height - 1][invalidVarIDX * 2 + 1] = numberFactory.getOne();
		ceilTable.array[ceilTable.height - 1][ceilTable.width - 2] = numberFactory.getOne();
		ceilTable.isOptimum = false;
	    }
	    Solution floorSol = getSolution(floorTable);
	    Solution ceilSol = getSolution(ceilTable);

	    if (floorSol.equals(Solution.NOSOLUTION))
		return ceilSol;
	    if (ceilSol.equals(Solution.NOSOLUTION))
		return floorSol;

	    double floorDistance = 0d;
	    for (Variable var: floorSol.variables())
		floorDistance += Math.pow(floorSol.getNumericValue(var).getDoubleValue(), 2d);

	    double ceilDistance = 0d;
	    for (Variable var: ceilSol.variables())
		ceilDistance += Math.pow(ceilSol.getNumericValue(var).getDoubleValue(), 2d);
	    if (floorDistance < ceilDistance)
		return floorSol;
	    else
		return ceilSol;
	}
    }

    public boolean handlesEquations(){
	return true;
    }

    public boolean handlesIntegerEquations(){
	return true;
    }

    public boolean handlesNonlinearProblems(){
	return false;
    }

    public boolean handlesNumericProblems(){
	return true;
    }

    public boolean handlesStrictInequalities(){
	return false;
    }

    public boolean handlesStrictIntegerInequalities(){
	return true;
    }

    public boolean handlesWeakInequalities(){
	return true;
    }

    public boolean handlesWeakIntegerInequalities(){
	return true;
    }

    /**
     * Checks whether a solution exists for the system of constraints stored in
     * the actual constraint stack by using the hasSolution methods of the
     * constraint solvers. This method may be a little bit faster when only
     * an assertion about the solvability of a system of constraints should be
     * calculated.
     * @return <i>true</i> if any solution for the given problem exists,
     * <i>false</i> if definetively no solution satisfies the constraints.
     * @throws IncorrectSolverException if the solver is not able to handle the
     * actual set of constraints.
     * @throws TimeoutException 
     */
    public HasSolutionInformation hasSolution() throws IncorrectSolverException, TimeoutException{
	// CLTODO optimieren!
	return new HasSolutionInformation(getSolution());
    }

    /**
     * Internal method that generates the initial simplex table out of the basic
     * informations about the given constraints.
     * </BR></BR>
     * Does some preprocessing:
     * <ul>
     * 	</li>Gets rid of linear dependent equalities in a somewhat whicked way.
     *  </li>Determination of already existing pivot columns by finding columns with all but one zero entries
     * </ul>
     * 
     * @param variablesArray an array ofthe variables used in the simplex table.
     * @param coefficients the coefficient matrix of the simplex table without
     * widening it to handle negative values and without any slack or artificial
     * variables.
     * @param rhs the right hand side values of the equations.
     * @param isEquation boolean indicator for the existence of an equation or
     * an inequation for each row. <i>true</i> indicates an equation, <i>false</i>
     * an inequation.
     * @param sMLogger the object that is responsible for generating the log.
     * @param nFactory the factory object that generates numbers with the
     * desired precision for the internal calculations.
     */
    protected void initTableau(NumericVariable[] variablesArray, NumberWrapper[][] coefficients, NumberWrapper[] rhs, boolean[] isEquation, Logger sMLogger, NumberFactory nFactory){
	// CLTODO optimiertes Simplextableau ohne überflüssige künstliche Variablen erstellen
	this.numberFactory = nFactory;
	this.texLogger = sMLogger;
	this.variables = new Vector<Variable>();
	for (int i = 0; i < variablesArray.length; i++)
	    this.variables.add(variablesArray[i]);

	int constraintCount = rhs.length;
	int inequationCount = 0;
	for (int i = 0; i < constraintCount; i++)
	    if (!isEquation[i])
		inequationCount++;

	// transform the inequations to equations and check whether some of the
	// equations are linearly dependent.
	Vector<NumberWrapper[]> uncheckedEquations = new Vector<NumberWrapper[]>();
	Vector<NumberWrapper[]> checkedEquations = new Vector<NumberWrapper[]>();
	int inequationIDX = variables.size();
	int equationLength = variables.size() + inequationCount + 1;
	
	// WHICKED: inequations are always checked equations are unchecked
	for (int row = 0; row < constraintCount; row++){
	    NumberWrapper[] equation = new NumberWrapper[equationLength];
	    System.arraycopy(coefficients[row], 0, equation, 0, variables.size());
	    Arrays.fill(equation, variables.size(), equationLength - 1, numberFactory.getZero());
	    equation[equation.length - 1] = rhs[row];
	    if (!isEquation[row]){
		equation[inequationIDX] = numberFactory.getOne();
		inequationIDX++;
	    }
	    if (!isEquation[row]){
		// equations containting a slack variable are always linearly
		// independend from all other variables.
		checkedEquations.add(equation);
	    } else {
		// for the other equations we need a more detailed examination
		uncheckedEquations.add(equation);
	    }
	}
	
	// Sorts out linearly dependend equations using gaussian elmination
	while (!uncheckedEquations.isEmpty()){
	    // filter all equations that contain a variable with a nonzero coefficient
	    // where all other equations have the coefficient zero for that variable.
	    for (int column = 0; column < equationLength - 1; column++){
		int uniqueNonZeroPos = -1;
		for (int row = 0; row < uncheckedEquations.size(); row++){
		    NumberWrapper[] coeffRow = uncheckedEquations.get(row);
		    if (!coeffRow[column].isZero()){
			if (uniqueNonZeroPos == -1)
			    uniqueNonZeroPos = row;
			else
			    uniqueNonZeroPos = -2;
		    }
		}
		if (uniqueNonZeroPos >= 0)
		    checkedEquations.add(uncheckedEquations.remove(uniqueNonZeroPos));
	    }
	    // find the first nonzero coefficient in the remaining equations and
	    // perform a gaussian transformation step.
	    if (uncheckedEquations.size() > 0){
		int x = 0;
		NumberWrapper[] equation = uncheckedEquations.get(0);
		while ((x < equationLength - 1) && (equation[x].isZero()))
		    x++;
		if (x == variables.size() + inequationCount){
		    // all coefficients are equal to zero
		    if (equation[equation.length - 1].isZero()){
			uncheckedEquations.remove(0);
		    } else {
			this.starttable = null;
			return;
		    }
		} else {
		    for (int y = 1; y < uncheckedEquations.size(); y++){
			NumberWrapper[] equation2 = uncheckedEquations.get(y);
			
			if (texLogger.isInfoEnabled()) texLogger.info( Arrays.toString(equation2) );
			NumberWrapper factor = equation2[x];
			for (int col = 0; col < equation2.length; col++){
			    equation2[col] = equation2[col].sub(equation[col].mult(factor).div(equation[x]));
			}
		    }
		    checkedEquations.add(uncheckedEquations.remove(0));
		}
	    }
	}
	uncheckedEquations = null;
	// correct the sign of the rhs and copy the equations into an array
	NumberWrapper[][] equations = new NumberWrapper[checkedEquations.size()][];
	for (int i = 0; i < checkedEquations.size(); i++){
	    equations[i] = checkedEquations.get(i);
	    if (equations[i][equationLength - 1].lessThan(numberFactory.getZero()))
		for (int x = 0; x < equationLength; x++)
		    equations[i][x] = equations[i][x].negate();
	}
	checkedEquations = null;

	// find already existing pivot columns to avoid the generation of too many
	// artificial variables.
	int[] pivotColumns = new int[equations.length];
	int artificialVariablesCount = equations.length;
	Arrays.fill(pivotColumns, -1);
	for (int column = equationLength - 2; column >= 0; column--){
	    int pivotRow = -1;
	    for (int row = 0; row < equations.length; row++){
		NumberWrapper coeff = equations[row][column];
		if (!coeff.isZero()){
		    if ((pivotRow == -1) && coeff.greaterThan(numberFactory.getZero()))
			pivotRow = row;
		    else
			pivotRow = -2;
		}
	    }
	    if ((pivotRow >= 0) && (pivotColumns[pivotRow] == -1)){
		pivotColumns[pivotRow] = column;
		artificialVariablesCount--;
		NumberWrapper quotient = equations[pivotRow][column];
		for (int x = 0; x < equationLength; x++)
		    equations[pivotRow][x] = equations[pivotRow][x].div(quotient);
	    }
	}

	// build a new SimplexTable object from the equations
	starttable = new SimplexTable(2 * variables.size() + inequationCount + artificialVariablesCount, equations.length);
	starttable.artificialVariablesCount = artificialVariablesCount;
	starttable.slackVariablesCount = inequationCount;
	starttable.pivotColumns = pivotColumns;
	int artificialVariablesPos = 2 * variables.size() + inequationCount;
	for (int row = 0; row < starttable.height; row++){
	    for (int column = 0; column < variables.size(); column++){
		starttable.array[row][column * 2] = equations[row][column];
		starttable.array[row][column * 2 + 1] = equations[row][column].negate();
	    }
	    for (int column = 0; column < inequationCount; column++){
		starttable.array[row][2*variables.size() + column] = equations[row][variables.size() + column];
	    }
	    starttable.rhs[row] = equations[row][equationLength - 1];
	    Arrays.fill(starttable.array[row], 2 * variables.size() + inequationCount, starttable.array[row].length, numberFactory.getZero());
	    if (starttable.pivotColumns[row] == -1){
		starttable.pivotColumns[row] = artificialVariablesPos;
		artificialVariablesPos++;
		starttable.array[row][starttable.pivotColumns[row]] = numberFactory.getOne();
	    } else{
		if (starttable.pivotColumns[row] < variables.size())
		    starttable.pivotColumns[row] *= 2;
		else
		    starttable.pivotColumns[row] += variables.size();
	    }
	    starttable.isPivotColumn[starttable.pivotColumns[row]] = true;
	}
	if (artificialVariablesCount == 0){
	    Arrays.fill(starttable.costs, 0, 2 * variables.size(), numberFactory.getOne());
	    Arrays.fill(starttable.costs, 2 * variables.size(), starttable.width, numberFactory.getZero());
	} else {
	    Arrays.fill(starttable.costs, 0, starttable.width - artificialVariablesCount, numberFactory.getZero());
	    Arrays.fill(starttable.costs, starttable.width - artificialVariablesCount, starttable.width, numberFactory.getOne());
	}
    }

    private void initialize(){
	// variables
	HashSet<Variable> vars = new HashSet<Variable>();
	constraints.collectVariables(vars);
	NumericVariable[] varArray = new NumericVariable[vars.size()];
	varArray = vars.toArray(varArray);

	if (constraints.containsStrictInequations())
	    throw new InternalError("SimplexSolverCL is unable to handle strict inequations");

	// constraints
	int constraintCount = constraints.getConstraintCount();
	NumberWrapper[][] coeffs = new NumberWrapper[constraintCount][vars.size()];
	NumberWrapper[] rhs = new NumberWrapper[constraintCount];
	boolean[] isEquation = new boolean[constraintCount];

	// build coefficients array
	for (int i = 0; i < constraintCount; i++){
	    NumericConstraint constraint = (NumericConstraint) constraints.getConstraint(i);
	    Polynomial poly = constraint.getPolynomial();
	    NumberWrapper constantNC = poly.getConstant();
	    if (constantNC != null)
		rhs[i] = constantNC.negate();
	    else rhs[i] = numberFactory.getZero();
	    for (int j = 0; j < varArray.length; j++){
		NumberWrapper coeff = poly.getCoefficient(new Monomial(varArray[j], 1));
		if (coeff != null)
		    coeffs[i][j] = coeff;
		else
		    coeffs[i][j] = numberFactory.getZero();
	    }
	    isEquation[i] = constraint.isEquation();
	}
	
	// initialize tableau
	initTableau(varArray, coeffs, rhs, isEquation, texLogger, numberFactory);
    }

    /**
     * Performs a single simplex transformation step on the passed simplex table.
     * One transformation contains the finding of the next pivot column that enters
     * the basis of the simplex table, the finding of the pivot element that
     * leaves the basis, and the calculations that are needed to realize these
     * changes. If it will be recognized, that the simplex table is already in
     * an optimal state, the table will be marked to be optimal and nothing else
     * will be done.
     * @param oldTable the table the next transformation should be applied to.
     * @return the simplex table after the transformation.
     */
    public SimplexTable performSingleTransformation(SimplexTable oldTable){
	if (oldTable.isOptimum)
	    return oldTable;
	
	// finding the new pivot column
	// uses delta function i.e. is greedy
	int pivotColumn = 0;
	NumberWrapper maxDelta = oldTable.getDelta(pivotColumn);
	for (int i = 1; i < oldTable.width; i++)
	    if (!oldTable.isPivotColumn[i]){
		NumberWrapper delta = oldTable.getDelta(i);
		if (delta.greaterThan(maxDelta)){
		    maxDelta = delta;
		    pivotColumn = i;
		}
	    }
	
	// if no positive delta value can be found, we have the optimal solution
	if (maxDelta.lessOrEqual(numberFactory.getZero())){
	    oldTable.isOptimum = true;
	    return oldTable;
	}

	// TODO Marko & Tim

	// finding the pivot row
	// again greedy with randomization on the minima
	int pivotRow = -1;
	NumberWrapper minQuotient = null;
	for (int i = 0; i < oldTable.height; i++){
	    if (oldTable.array[i][pivotColumn].greaterThan(numberFactory.getZero())){
		NumberWrapper quotient = oldTable.rhs[i].div(oldTable.array[i][pivotColumn]);
		if ((minQuotient == null) || ((quotient.equals(minQuotient)) && (RandomSingleton.staticNextBoolean())) || (quotient.lessThan(minQuotient))){
		    minQuotient = quotient;
		    pivotRow = i;
		}
	    }
	}
	
	// perform transformations until the table is optimal
	return performSingleTransformation(oldTable, pivotColumn, pivotRow);
    }

    /**
     * Performs a pivot operation on the tableau with the given parameters without checking any restrictions whatsoever.
     * </BR>
     * May be used for primal as well as for dual simplex.
     * 
     * @param oldTable
     * @param pivotColumn 
     * @param pivotRow
     * @return the oldTable is altered and returned.
     */
    public SimplexTable performSingleTransformation(SimplexTable oldTable, int pivotColumn, int pivotRow){
	if (texLogger.isDebugEnabled()) texLogger.debug("$"+ oldTable.toTex() + "$\\\\\n");

	// mark the old pivot column to leave the basis
	oldTable.isPivotColumn[oldTable.pivotColumns[pivotRow]] = false;
	// mark the new pivot column to join the basis
	oldTable.isPivotColumn[pivotColumn] = true;
	// mark the new pivot column as the pivot column of the current row
	oldTable.pivotColumns[pivotRow] = pivotColumn;

	// calculate new rows (without actual pivoting row)
	for (int row = 0; row < oldTable.height; row++) {
	    if (row != pivotRow){
		NumberWrapper q = oldTable.array[pivotRow][pivotColumn];
		NumberWrapper factor = oldTable.array[row][pivotColumn].div(q);
		// threat the whole row including rhs
		oldTable.rhs[row] = oldTable.rhs[row].sub(oldTable.rhs[pivotRow].mult(factor));
		for (int column = 0; column < oldTable.width; column++){
		    if (column == pivotColumn)
			// special rule for pivoting column
			oldTable.array[row][column] = numberFactory.getZero();
		    else
			oldTable.array[row][column] = oldTable.array[row][column].sub(oldTable.array[pivotRow][column].mult(factor));
		}
	    }
    	}
	
	// calculate new pivoting row
	NumberWrapper q = oldTable.array[pivotRow][pivotColumn];
	for (int column = 0; column < oldTable.width; column++) {
	    oldTable.array[pivotRow][column] = oldTable.array[pivotRow][column].div(q);
	}
	oldTable.rhs[pivotRow] = oldTable.rhs[pivotRow].div(q);

	if (texLogger.isDebugEnabled()) texLogger.debug("$"+ oldTable.toTex() + "$\\\\\n");
	
	return oldTable;
    }

    /**
     * Removes the lastly added constraint from the set of constraints.
     * </BR>
     * Not implemented at all.
     */
    public void removeConstraint() {
	// CLTODO Auto-generated method stub
    }

    /**
     * Creates a new SimplexSolverCL instance using the current SolverManager.
     */
    public SimplexSolverCL reset(){
	return newInstance(null);
    }

    /**
     * Inner class for the representation of all information belonging to one
     * single simplex table.
     * @author Christoph Lembeck
     */
    class SimplexTable{

	/**
	 * the matrix of the coefficients extracted out of the constraints.
	 */
	protected NumberWrapper[][] array;

	/**
	 * the number of the artificial variables in the simplex table.
	 * The coefficients of the artificial variables are always the coefficients
	 * on the most rightly aligned positions of the coefficient matrix.
	 */
	protected int artificialVariablesCount;

	/**
	 * the coefficients of the cost function for the simplex table.
	 */
	protected NumberWrapper[] costs;

	/**
	 * the number of constraints contained in the simplex table.
	 */
	protected int height;

	/**
	 * indicates if the simplex table is already in an optimal state or not.
	 */
	protected boolean isOptimum = false;

	/**
	 * Stores for each column of the simplex table if it is a column containing
	 * an pivot variable or not.
	 */
	protected boolean[] isPivotColumn;

	/**
	 * the list of the actual pivot columns for each row.
	 */
	protected int[] pivotColumns;

	/**
	 * the right hand side values of the equations stored in the simplex table
	 */
	protected NumberWrapper[] rhs;

	/**
	 * the number of the slack variables contained in the simplex table.
	 */
	protected int slackVariablesCount;

	/**
	 * the width of the simplex table. The width is the sum of the number of
	 * columns used to represent the coefficients of the variables (twice as
	 * many as the number of the variables due to the fact that they may become
	 * negative) and the number of slack variables and artificial variables.
	 */
	protected int width;

	/**
	 * Creates a new, empty simplex table with the given width and height.
	 * @param width the new width of the simplex table inclusive the columns for
	 * the slack variables and the artificial variables.
	 * @param height the height (number of constraints) of the simplex table.
	 */
	public SimplexTable(int width, int height){
	    this.width = width;
	    this.height = height;
	    this.array = new NumberWrapper[height][width];
	    this.costs = new NumberWrapper[width];
	    this.rhs = new NumberWrapper[height];
	    this.pivotColumns = new int[height];
	    this.isPivotColumn = new boolean[width];
	}

	/**
	 * calculates the actual delta value for the passed column (beginning with
	 * the index 0). The delta value represents the relative gradient of the
	 * cost function regarding the variable belonging to the specified column.
	 * @param column the column the delta value should be computed for.
	 * @return the computed delta value.
	 * 
	 * TODO: Why is he using this function and is not calculating the reduced costs within the tableau operations?
	 */
	public NumberWrapper getDelta(int column){
	    NumberWrapper delta = costs[column].negate();
	    for (int i = 0; i < height; i++)
		delta = delta.add( array[i][column].mult(costs[pivotColumns[i]]) );
	    return delta;
	}

	/**
	 * Returns the actual value for the variable specified by the passed
	 * column (beginning with the index 0).
	 * @param idx the index of the columns the value of the variable should be
	 * calculated for.
	 * @return the actual value for the variable specified by the passed
	 * column (beginning with the index 0).
	 */
	public NumberWrapper getVariableValue(int idx){
	    int row = 0;
	    while ((row < height) && (pivotColumns[row] != idx))
		row++;
	    if (row == height)
		return numberFactory.getZero();
	    else
		return rhs[row];
	}

	@Override
	public String toString(){
	    StringBuffer sb = new StringBuffer();
	    sb.append("Costs:");
	    for (int i = 0; i < costs.length; i++)
		sb.append(" " + costs[i]);
	    sb.append('\n');
	    sb.append("IsPivotColumn:");
	    for (int i = 0; i < isPivotColumn.length; i++)
		sb.append(" " + isPivotColumn[i]);
	    sb.append('\n');
	    for (int y = 0; y < pivotColumns.length; y++){
		sb.append(pivotColumns[y]);
		sb.append(":");
		for (int i = 0; i < isPivotColumn.length; i++){
		    sb.append(" " + array[y][i]);
		}
		sb.append(" : " + rhs[y]);
		sb.append('\n');
	    }
	    for (int i = 0; i < width; i++)
		sb.append(getDelta(i) + " ");
	    return sb.toString();
	}

	/**
	 * Returns a representation of the current simplex table in tex notation.
	 * @return a representation of the current simplex table in tex notation.
	 */
	public String toTex(){
	    StringBuffer out = new StringBuffer();
	    out.append("\\begin{tabular}{c|");
	    for (int i = 0; i < width; i++)
		out.append("c");
	    out.append("|cc}\n");
	    for (int i = 0; i < width; i++)
		out.append("&" + costs[i]);
	    out.append("\\\\\n");
	    for (int i = 0; i < width; i++)
		out.append("&" + (i+1));
	    out.append("\\\\\\hline");
	    for (int row = 0; row < height; row++){
		out.append("\n" + (pivotColumns[row] + 1)+"");
		for (int column = 0; column < width; column++)
		    out.append("&" + array[row][column]);
		out.append("&" + rhs[row] + "\\\\");
	    }
	    out.append("\\hline\n");
	    for (int column = 0; column < width; column++)
		out.append("&" + getDelta(column));

	    out.append("\\end{tabular}");
	    return out.toString();
	}

	/**
	 * Writes the actual state of the simplex table into the log stream.
	 * @param logStream the stream the looging informations should be written
	 * into.
	 */
	public void writeToLog(PrintStream logStream){
	    logStream.println("  <simplextable width=\"" + width +
		    "\" height=\"" + height +
		    "\" artificialvariablescount=\"" + artificialVariablesCount +
		    "\" slackvariablescount=\"" + slackVariablesCount + "\">");
	    logStream.println("   <costs>");
	    for (int i = 0; i < width; i++)
		logStream.println("    <number>" + costs[i].toString() + "</number>");
	    logStream.println("   </costs>");
	    logStream.println("   <simplexrows>");
	    for (int i = 0; i < height; i++){
		logStream.println("    <simplexrow>");
		logStream.println("     <pivotcolumn><number>" + (pivotColumns[i]+1) + "</number></pivotcolumn>");
		for (int j = 0; j < width; j++)
		    logStream.println("     <number>" + array[i][j].toString() + "</number>");
		logStream.println("     <simplexrhs><number>" + rhs[i] + "</number></simplexrhs>");
		logStream.println("    </simplexrow>");
	    }
	    logStream.println("   </simplexrows>");
	    logStream.println("   <deltas>");
	    for (int i = 0; i < width; i++)
		logStream.println("    <number>" + getDelta(i).toString() + "</number>");
	    logStream.println("   </deltas>");
	    logStream.println("  </simplextable>");
	}
    }
}
