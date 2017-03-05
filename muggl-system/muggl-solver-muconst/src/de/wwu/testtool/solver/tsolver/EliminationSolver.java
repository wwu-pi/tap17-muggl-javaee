package de.wwu.testtool.solver.tsolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.conf.ConfigReader;
import de.wwu.testtool.conf.SimplexSolverConfig;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
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
import de.wwu.testtool.tools.Timer;

/**
 * @author Christoph Lembeck
 */
public class EliminationSolver implements Solver{

    /**
     * Indicates an equation.
     */
    public static final byte EQUAL = 0;

    /**
     * Indicates a weak inequation.
     */
    public static final byte LESSOREQUAL = 1;

    /**
     * Indicates a strong inequation.
     */
    public static final byte LESSTHAN = 2;

    protected static NumberFactory defaultNumberFactory;

    static{
	try{
	    ConfigReader configReader = ConfigReader.getInstance();
	    Node solverNode = configReader.getNode("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"" + EliminationSolver.class.getName() + "\"]");
	    String selectedNumberFactoryName = configReader.getTextContent("NumberFactory/@selected", solverNode);
	    String selectedFactoryClass = configReader.getTextContent("NumberFactory/NumberFactoryOption[attribute::name=\"" + selectedNumberFactoryName + "\"]/@class", solverNode);
	    defaultNumberFactory = (NumberFactory)Class.forName(selectedFactoryClass).newInstance();
	} catch (XPathExpressionException xpee){
	    throw new InternalError(xpee.toString());
	} catch (InstantiationException ie){
	    throw new InternalError(ie.toString());
	} catch (ClassNotFoundException cnf){
	    throw new InternalError(cnf.toString());
	} catch (IllegalAccessException iae){
	    throw new InternalError(iae.toString());
	}
    }

    public static EliminationSolver newInstance(SolverManager solverManager){
	return new EliminationSolver(solverManager, defaultNumberFactory);
    }

    public static EliminationSolver newInstance(SolverManager solverManager, NumberFactory numberFactory){
	return new EliminationSolver(solverManager, numberFactory);
    }

    protected SingleConstraintSet constraints;

    /**
     * The object that is responsible for generating the log.
     */
    protected Logger logger;

    private long timeout;
    private Timer timer;
    
    /**
     * The actual number factory for the generation of numbers for internal use of
     * the algorithm.
     */
    protected NumberFactory numberFactory;

    protected SolverManager solverManager;

    /**
     * Stores the coefficients of the initial constraint system. May only be
     * changed if a new constraint will be added or removed.
     */
    protected Coefficients startCoefficients;

    private EliminationSolver(SolverManager solverManager, NumberFactory numberFactory){
	this.solverManager = solverManager;
	this.logger = TesttoolConfig.getLogger();
	this.numberFactory = numberFactory;
	
	timeout = SimplexSolverConfig.getInstance().getTimeout();
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
	// CLTODO dringend initializeCoefficients �berarbeiten!!!
	initializeCoefficients();
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
	return "EliminationSolver";
    }

    public List<Class<? extends Solver>> getRequiredSubsolvers(){
	ArrayList<Class<? extends Solver>> result = new ArrayList<Class<? extends Solver>>();
	result.add(SimplexSolverCL.class);
	return result;
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
	try{
	    startCoefficients = startCoefficients.clean();
	}
	catch (NoSolutionException nse){
	    return Solution.NOSOLUTION;
	}

	if (timeout > 0){
	    timer = new Timer(timeout);
	    timer.run();
	} else {
	    timer = new Timer(0);
	}

	Coefficients currentSystem = startCoefficients;
	Coefficients integerEquations = null;
	Solution solution = new Solution();

	// remove the equations by isolating a variable and substitute it in the
	// remaining equations and inequations.
	while ((currentSystem.height > 1) && (currentSystem.width > 1) &&
		(currentSystem.containsEquations())){
	    
	    /*
	     * Timeouts!
	     */
	    if (timer.timeout())
		throw new TimeoutException("Timeout " + timeout + " reached. No solution found.");

	    
	    
	    int eqnIDX = -1;
	    int varIDX = -1;
	    int integerEqnIDX = -1;
	    int integerVarIDX = -1;
	    int rowTmp = 0;
	    // first try to find an equation and a noninteger variable having a
	    // coefficient not equal to zero. For the case that no such a variable
	    // can be found, store the position of the first occuring integer
	    // variable that is used in an equation.
	    while((eqnIDX == -1) && (rowTmp < currentSystem.height)){
		if (currentSystem.rowType[rowTmp] == EQUAL){
		    int column = 0;
		    while ((eqnIDX == -1) && (column < currentSystem.width)){
			if (currentSystem.variables[column].isInteger()){
			    if (!currentSystem.array[rowTmp][column].isZero()){
				integerEqnIDX = rowTmp;
				integerVarIDX = column;
			    }
			} else {
			    if (!currentSystem.array[rowTmp][column].isZero()){
				eqnIDX = rowTmp;
				varIDX = column;
			    }
			}
			column++;
		    }
		}
		rowTmp++;
	    }
	    // if no equation was found having the coefficient of a noninteger
	    // variable not equal to zero, the index of an integer variable will be
	    // used instead.
	    if (eqnIDX == -1){
		eqnIDX = integerEqnIDX;
		varIDX = integerVarIDX;
		// if we have to choose an integer variable for the first time we
		// store the remaining equations (that may only contain integer
		// variables) for the later appliance of the simplex algorithm
		if (integerEquations == null){
		    integerEquations = new Coefficients(currentSystem.width, currentSystem.getEquationCount());
		    System.arraycopy(currentSystem.variables, 0, integerEquations.variables, 0, currentSystem.width);
		    int eqnPos = 0;
		    for (int row = 0; row < currentSystem.height; row++){
			if (currentSystem.rowType[row] == EQUAL){
			    System.arraycopy(currentSystem.array[row], 0, integerEquations.array[eqnPos], 0, currentSystem.width);
			    integerEquations.rhs[eqnPos] = currentSystem.rhs[row];
			    integerEquations.rowType[eqnPos] = EQUAL;
			    eqnPos++;
			}
		    }
		    try{
			integerEquations = integerEquations.clean();
		    } catch (NoSolutionException nse){
			throw new InternalError("this should never happen");
		    }
		}
	    }
	    assert (eqnIDX != -1) && (varIDX != -1);

	    // eliminate the above selected variable
	    Coefficients newSystem = new Coefficients(currentSystem.width - 1, currentSystem.height - 1);
	    currentSystem.eliminationVariable = currentSystem.variables[varIDX];
	    newSystem.pred = currentSystem;

	    for (int column = 0; column < varIDX; column++)
		newSystem.variables[column] = currentSystem.variables[column];
	    for (int column = varIDX + 1; column < currentSystem.width; column++)
		newSystem.variables[column - 1] = currentSystem.variables[column];

	    // calculate the coefficients obove the eliminated equation
	    for (int row = 0; row < eqnIDX; row++){
		newSystem.rowType[row] = currentSystem.rowType[row];
		newSystem.rhs[row] = currentSystem.rhs[row].sub(currentSystem.rhs[eqnIDX].mult(currentSystem.array[row][varIDX]).div(currentSystem.array[eqnIDX][varIDX]));
		for (int column = 0; column < varIDX; column++){
		    newSystem.array[row][column] = currentSystem.array[row][column].sub(currentSystem.array[eqnIDX][column].mult(currentSystem.array[row][varIDX]).div(currentSystem.array[eqnIDX][varIDX]));
		}
		for (int column = varIDX + 1; column < currentSystem.width; column++){
		    newSystem.array[row][column - 1] = currentSystem.array[row][column].sub(currentSystem.array[eqnIDX][column].mult(currentSystem.array[row][varIDX]).div(currentSystem.array[eqnIDX][varIDX]));
		}
	    }
	    // calculate the coefficients below the eliminated equation
	    for (int row = eqnIDX + 1; row < currentSystem.height; row++){
		newSystem.rowType[row - 1] = currentSystem.rowType[row];
		newSystem.rhs[row - 1] = currentSystem.rhs[row].sub(currentSystem.rhs[eqnIDX].mult(currentSystem.array[row][varIDX]).div(currentSystem.array[eqnIDX][varIDX]));
		for (int column = 0; column < varIDX; column++){
		    newSystem.array[row - 1][column] = currentSystem.array[row][column].sub(currentSystem.array[eqnIDX][column].mult(currentSystem.array[row][varIDX]).div(currentSystem.array[eqnIDX][varIDX]));
		}
		for (int column = varIDX + 1; column < currentSystem.width; column++){
		    newSystem.array[row - 1][column - 1] = currentSystem.array[row][column].sub(currentSystem.array[eqnIDX][column].mult(currentSystem.array[row][varIDX]).div(currentSystem.array[eqnIDX][varIDX]));
		}
	    }
	    try{
		currentSystem = newSystem.clean();
	    }
	    catch (NoSolutionException nse){
		return Solution.NOSOLUTION;
	    }


	}
	// perform the origin fourier motzkin elimination on the remaining
	// inequations.
	while (currentSystem.containsNonintegerVariables()){
	    
	    /*
	     * Timeouts!
	     */
	    if (timer.timeout())
		throw new TimeoutException("Timeout " + timeout + " reached. No solution found.");
	    
	    int varIDX = currentSystem.getFourierMotzkinEliminationIndex();
	    currentSystem.eliminationVariable = currentSystem.variables[varIDX];
	    // create three vectors indexing the constraints that do not contain
	    // the selected variable and that contain the variable with a positive or
	    // negative coefficient
	    Vector<Integer> posIDXVec = new Vector<Integer>();
	    Vector<Integer> negIDXVec = new Vector<Integer>();
	    Vector<Integer> zeroIDXVec = new Vector<Integer>();
	    for (int i = 0; i < currentSystem.height; i++){
		NumberWrapper coeff = currentSystem.array[i][varIDX];
		if (coeff.greaterThan(numberFactory.getZero()))
		    posIDXVec.add(new Integer(i));
		else
		    if (coeff.isZero())
			zeroIDXVec.add(new Integer(i));
		    else
			negIDXVec.add(new Integer(i));
	    }
	    Coefficients newSystem = new Coefficients(currentSystem.width - 1, zeroIDXVec.size() + negIDXVec.size() * posIDXVec.size());
	    newSystem.pred = currentSystem;
	    for (int i = 0; i < varIDX; i++)
		newSystem.variables[i] = currentSystem.variables[i];
	    for (int i = varIDX + 1; i < currentSystem.width; i++)
		newSystem.variables[i - 1] = currentSystem.variables[i];
	    // copy the constraints that do not contain the selected variable
	    int insertPosition = 0;
	    for (int i = 0; i < zeroIDXVec.size(); i++){
		int rowIDX = (zeroIDXVec.get(i)).intValue();
		newSystem.rowType[insertPosition] = currentSystem.rowType[rowIDX];
		newSystem.rhs[insertPosition] = currentSystem.rhs[rowIDX];
		System.arraycopy(currentSystem.array[rowIDX], 0, newSystem.array[insertPosition], 0, varIDX);
		System.arraycopy(currentSystem.array[rowIDX], varIDX + 1, newSystem.array[insertPosition], varIDX, currentSystem.width - varIDX - 1);
		insertPosition++;
	    }
	    for (int posCounter = 0; posCounter < posIDXVec.size(); posCounter++){
		int posIDX = (posIDXVec.get(posCounter)).intValue();
		for (int negCounter = 0; negCounter < negIDXVec.size(); negCounter++){
		    int negIDX = (negIDXVec.get(negCounter)).intValue();
		    if ((currentSystem.rowType[negIDX] == LESSTHAN) || (currentSystem.rowType[posIDX] == LESSTHAN))
			newSystem.rowType[insertPosition] = LESSTHAN;
		    else
			newSystem.rowType[insertPosition] = LESSOREQUAL;
		    for (int i = 0; i < varIDX; i++)
			newSystem.array[insertPosition][i] = currentSystem.array[posIDX][i].sub(currentSystem.array[negIDX][i].mult(currentSystem.array[posIDX][varIDX]).div(currentSystem.array[negIDX][varIDX]));
		    for (int i = varIDX + 1; i < currentSystem.width; i++)
			newSystem.array[insertPosition][i - 1] = currentSystem.array[posIDX][i].sub(currentSystem.array[negIDX][i].mult(currentSystem.array[posIDX][varIDX]).div(currentSystem.array[negIDX][varIDX]));
		    newSystem.rhs[insertPosition] = currentSystem.rhs[posIDX].sub(currentSystem.rhs[negIDX].mult(currentSystem.array[posIDX][varIDX]).div(currentSystem.array[negIDX][varIDX]));
		    insertPosition++;
		}
	    }
	    try{
		currentSystem = newSystem.clean();
	    }
	    catch (NoSolutionException nse){
		return Solution.NOSOLUTION;
	    }
	}

	// Here we can assert, that every remainig variable is an integer variable
	// or all the variables are eliminated.
	if (currentSystem.width > 0){
	    // any integer variables remained, so we need to start the simplex solver
	    if (integerEquations == null)
		integerEquations = new Coefficients(0,0);
	    Vector<Variable> variables = new Vector<Variable>();
	    for (int i = 0; i < currentSystem.variables.length; i++)
		variables.add(currentSystem.variables[i]);
	    for (int i = 0; i < integerEquations.variables.length; i++)
		if (!variables.contains(integerEquations.variables[i]))
		    variables.add(integerEquations.variables[i]);
	    NumberWrapper[][] coeff = new NumberWrapper[currentSystem.height + integerEquations.height][variables.size()];
	    NumberWrapper[] rhs = new NumberWrapper[coeff.length];
	    boolean[] isEquation = new boolean[rhs.length];

	    for (int row = 0; row < currentSystem.height; row++){
		isEquation[row] = currentSystem.rowType[row] == EQUAL;
		for (int column = 0; column < currentSystem.width; column++)
		    coeff[row][column] = currentSystem.array[row][column];
		for (int column = currentSystem.width; column < coeff[row].length; column++)
		    coeff[row][column] = numberFactory.getZero();
		if (currentSystem.rowType[row] == LESSTHAN){
		    NumberWrapper gcd = currentSystem.rhs[row];
		    for (int column = 0; column < currentSystem.width; column++){
			NumberWrapper c = currentSystem.array[row][column];
			if (!c.isZero()){
			    if (gcd.isZero())
				gcd = c;
			    else
				gcd = gcd.gcd(c);
			}
		    }
		    rhs[row] = currentSystem.rhs[row].sub(gcd);
		} else {
		    rhs[row] = currentSystem.rhs[row];
		}
	    }

	    for (int row = 0; row < integerEquations.height; row++){
		isEquation[currentSystem.height + row] = true;
		rhs[currentSystem.height + row] = integerEquations.rhs[row];
		Arrays.fill(coeff[currentSystem.height + row], numberFactory.getZero());
		for (int column = 0; column < integerEquations.width; column++){
		    coeff[currentSystem.height + row][variables.indexOf(integerEquations.variables[column])] = integerEquations.array[row][column];
		}
	    }
	    NumericVariable[] variablesArray = new NumericVariable[variables.size()];
	    for (int i = 0; i < variables.size(); i++)
		variablesArray[i] = (NumericVariable)variables.get(i);

	    SimplexSolverCL simplex = new SimplexSolverCL(solverManager, variablesArray, coeff, rhs, isEquation, logger, numberFactory);
	    simplex.setTimeout(timer.getRemainingTime());
	    
	    solution = simplex.getSolution();
	    if (solution.equals(Solution.NOSOLUTION))
		return Solution.NOSOLUTION;
	    try{
		currentSystem = currentSystem.insert(solution);
	    } catch (NoSolutionException nse){
		throw new InternalError("simplex solver produced an invald solution!");
	    }
	}



	while (currentSystem != null){
	    
	    /*
	     * Timeouts!
	     */
	    if (timer.timeout())
		throw new TimeoutException("Timeout " + timeout + " reached. No solution found.");
	    
	    if (currentSystem.height == 0)
		currentSystem = currentSystem.pred;
	    else{
		if (currentSystem.width > 1){
		    // more than one variable was eliminated at once, so each additionally
		    // eliminated variable may take the value zero without running the
		    // risk producing inconsistencies.
		    for (int i = 0; i < currentSystem.width; i++)
			if (!currentSystem.variables[i].equals(currentSystem.eliminationVariable)){
			    NumericVariable variable = currentSystem.variables[i];
			    solution.addBinding(variable, NumericConstant.getZero(variable.getType()));
			    i--;
			    try{
				currentSystem = currentSystem.insert(variable, numberFactory.getZero());
			    } catch (NoSolutionException nse){
				nse.printStackTrace();
				throw new InternalError("This should never happen!");
			    }
			}
		}

		// Here we can be sure to have only one single nonlinear variable left
		NumericVariable variable = currentSystem.variables[0];
		NumberWrapper upperBound = null;
		NumberWrapper lowerBound = null;
		boolean upperIncluded = true;
		boolean lowerIncluded = true;
		for (int row = 0; row < currentSystem.height; row++){
		    NumberWrapper bound = currentSystem.rhs[row].div(currentSystem.array[row][0]);
		    if ((currentSystem.rowType[row] == EQUAL) || currentSystem.array[row][0].greaterThan(numberFactory.getZero())){
			// ax < c
			if (upperBound == null || upperBound.greaterThan(bound)){
			    upperBound = bound;
			    upperIncluded = (currentSystem.rowType[row] != LESSTHAN);
			} else {
			    if ((upperBound != null) && upperBound.equals(bound) && upperIncluded && (currentSystem.rowType[row] == LESSTHAN))
				upperIncluded = false;
			}
		    }
		    if ((currentSystem.rowType[row] == EQUAL) || currentSystem.array[row][0].lessThan(numberFactory.getZero())) {
			// -ax > -c
			if (lowerBound == null || lowerBound.lessThan(bound)){
			    lowerBound = bound;
			    lowerIncluded = (currentSystem.rowType[row] != LESSTHAN);
			} else {
			    if ((lowerBound != null) && lowerBound.equals(bound) && lowerIncluded && (currentSystem.rowType[row] == LESSTHAN))
				lowerIncluded = false;
			}
		    }
		}
		NumberWrapper value = numberFactory.findNumberBetween(lowerBound, lowerIncluded, upperBound, upperIncluded);
		if (value == null)
		    return Solution.NOSOLUTION;
		solution.addBinding(variable, DoubleConstant.getInstance(value.doubleValue()));
		try{
		    currentSystem = currentSystem.insert(variable, value);
		}
		catch (NoSolutionException nse){
		    nse.printStackTrace();
		    throw new InternalError("This should never happen!");
		}
		currentSystem = currentSystem.pred;
	    }
	}
	return solution;
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
	return true;
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
     * Removes the lastly added constraint from the set of constraints.
     */
    public void removeConstraint() {
	// CLTODO Auto-generated method stub

    }

    public Solver reset(){
	return newInstance(solverManager, numberFactory);
    }

    private void initializeCoefficients(){
	HashSet<Variable> varSet = new HashSet<Variable>();
	constraints.collectVariables(varSet);
	startCoefficients = new Coefficients(varSet.size(), constraints.getConstraintCount());
	Iterator<Variable> vars = varSet.iterator();
	int varIDX = 0;
	while (vars.hasNext())
	    startCoefficients.variables[varIDX++] = (NumericVariable)vars.next();
	for (int constraintIDX = 0; constraintIDX < constraints.getConstraintCount(); constraintIDX++){
	    NumericConstraint constraint = (NumericConstraint)constraints.getConstraint(constraintIDX);
	    Polynomial poly = constraint.getPolynomial();
	    Arrays.fill(startCoefficients.array[constraintIDX], numberFactory.getZero());
	    NumberWrapper constant = poly.getConstant();
	    if (constant != null)
		constant = constant.negate();
	    else
		constant = numberFactory.getZero();
	    startCoefficients.rhs[constraintIDX] = constant;
	    for (varIDX = 0; varIDX < startCoefficients.width; varIDX++){
		constant = poly.getCoefficient(new Monomial(startCoefficients.variables[varIDX], 1));
		if (constant == null)
		    constant = numberFactory.getZero();
		startCoefficients.array[constraintIDX][varIDX] = constant;
		if (constraint.isEquation())
		    startCoefficients.rowType[constraintIDX] = EQUAL;
		else
		    if (constraint.isStrictInequation())
			startCoefficients.rowType[constraintIDX] = LESSTHAN;
		    else
			startCoefficients.rowType[constraintIDX] = LESSOREQUAL;
	    }
	}
    }

    /**
     * Represents a system of constraints with its variables, types of equations
     * and inequations and all the coefficients.
     * @author Christoph Lembeck
     */
    class Coefficients{

	/**
	 * Stores the coefficients of the variables of each constraint.
	 */
	protected NumberWrapper[][] array;

	/**
	 * Stores the variable that was eliminated in the successing system of
	 * constraints.
	 */
	protected Variable eliminationVariable;

	/**
	 * Stores the number of constraints contained in the system of constraints.
	 */
	protected int height;

	/**
	 * Stoers the reference to the predecessing system of constraints this
	 * system was generated from.
	 */
	protected Coefficients pred;

	/**
	 * Stores the constants from the right hand side of the equations and
	 * inequations.
	 */
	protected NumberWrapper[] rhs;

	/**
	 * Indicates the type of the constraint for each row.
	 */
	protected byte[] rowType;

	/**
	 * The list of the variables containes in the constraints.
	 */
	protected NumericVariable[] variables;

	/**
	 * stores the number of variables contained in the system of constraints.
	 */
	protected int width;

	/**
	 * Creates a new representative of a linear system of equations and
	 * inequations.
	 * @param width the number of variables contained in the system.
	 * @param height the number of constraints contained in the system.
	 */
	public Coefficients(int width, int height){
	    this.width = width;
	    this.height = height;
	    variables = new NumericVariable[width];
	    array = new NumberWrapper[height][width];
	    rowType = new byte[height];
	    rhs = new NumberWrapper[height];
	}

	/**
	 * Removes empty lines out of the coefficient matrix and checks whether
	 * the system contains any contradictions.
	 * @return a copy of this Coefficients object without any redundant or
	 * dispensable informations.
	 * @throws NoSolutionException
	 */
	public Coefficients clean() throws NoSolutionException {
	    Vector<Integer> newRows = new Vector<Integer>();
	    Vector<Integer> newColumns = new Vector<Integer>();
	    Coefficients result = this;
	    for (int row = 0; row < height; row++){
		int column = 0;
		while ((column < width) && (array[row][column].isZero()))
		    column++;
		if (column < width)
		    newRows.add(new Integer(row));
		else {
		    switch (rowType[row]){
		    case LESSTHAN:
			if (!numberFactory.getZero().lessThan(rhs[row]))
			    throw new NoSolutionException("contradiction: 0 < " + rhs[row]);
			break;
		    case LESSOREQUAL:
			if (!numberFactory.getZero().lessOrEqual(rhs[row]))
			    throw new NoSolutionException("contradiction: 0 <= " + rhs[row]);
			break;
		    default:
			if (!rhs[row].isZero())
			    throw new NoSolutionException("contradiction: 0 == " + rhs[row]);
		    }
		}
	    }
	    for (int column = 0; column < width; column++){
		int row = 0;
		while ((row < height) && (array[row][column].isZero()))
		    row++;
		if (row < height)
		    newColumns.add(new Integer(column));
	    }
	    if ((newRows.size() != height) || (newColumns.size() != width)){
		result = new Coefficients(newColumns.size(), newRows.size());
		result.eliminationVariable = eliminationVariable;
		result.pred = pred;
		for (int column = 0; column < newColumns.size(); column++)
		    result.variables[column] = variables[newColumns.get(column).intValue()];
		for (int row = 0; row < newRows.size(); row++){
		    int referenceRow = newRows.get(row).intValue();
		    result.rhs[row] = rhs[referenceRow];
		    result.rowType[row] = rowType[referenceRow];
		    for (int column = 0; column < newColumns.size(); column++)
			result.array[row][column] = array[referenceRow][newColumns.get(column).intValue()];
		}
	    }
	    return result;
	}

	/**
	 * Checks wheter all the coefficients in the system are having the same
	 * sign per column.
	 * @return <i>true</i> if each column contains only coefficients having the
	 * same sign, <i>false</i> if one column exists containing coefficients of
	 * both signs.
	 */
	public boolean coefficientsHaveDifferentSigns(){
	    for (int column = 0; column < width; column++){
		boolean pos = false;
		boolean neg = false;
		for (int row = 0; row < height; row++){
		    NumberWrapper coeff = array[row][column];
		    if (coeff.greaterThan(numberFactory.getZero())){
			if (neg)
			    return true;
			pos = true;
		    } else
			if (coeff.lessThan(numberFactory.getZero())){
			    if (pos)
				return true;
			    neg = true;
			}
		}
	    }
	    return false;
	}

	/**
	 * Checks whether the actual coefficient matrix contains any equations or
	 * not.
	 * @return <i>true</i> if the atrix constains at least one equation,
	 * <i>false</i> if no equations can be found.
	 */
	public boolean containsEquations(){
	    for (int i = 0; i < rowType.length; i++)
		if (rowType[i] == EQUAL)
		    return true;
	    return false;
	}

	/**
	 * Checks whether the system contains at least one noninteger variable or
	 * not.
	 * @return <i>true</i> if the system contains noninteger variables,
	 * <i>false</i> otherwise.
	 */
	public boolean containsNonintegerVariables(){
	    for (int i = 0; i < variables.length; i++)
		if (!Term.isIntegerType(variables[i].getType()))
		    return true;
	    return false;
	}

	/**
	 * Checks whether all the variables have an integer type or not.
	 * @return <i>true</i> if all variables are integer, <i>false</i> if at
	 * least one variable is noninteger.
	 */
	public boolean containsOnlyIntegerVariables(){
	    for (int i = 0; i < variables.length; i++)
		if (!Term.isIntegerType(variables[i].getType()))
		    return false;
	    return true;
	}

	/**
	 * Returns the number of equations contained in the actual coefficient
	 * matrix.
	 * @return the number of equations contained in the actual coefficient
	 * matrix.
	 */
	public int getEquationCount(){
	    int result = 0;
	    for (int row = 0; row < height; row++)
		if (rowType[row] == EQUAL)
		    result++;
	    return result;
	}

	/**
	 * Calculates the number of constraints that will be generated if the
	 * variable with the passed index will be removed using Fourier Motzkin
	 * eliminiation.
	 * @param column the index of the column for that number of arising
	 * constraints should be calculated for.
	 * @return the number of the new constraints that will be created if the
	 * specified column will be eliminated.
	 */
	public int getFMFactor(int column){
	    int posCount = 0;
	    int negCount = 0;
	    int zeroCount = 0;
	    for (int row = 0; row < height; row++){
		NumberWrapper coeff = array[row][column];
		if (coeff.greaterThan(numberFactory.getZero()))
		    posCount++;
		else
		    if (coeff.greaterThan(numberFactory.getZero()))
			negCount++;
		    else
			zeroCount++;
	    }
	    return zeroCount + negCount * posCount;
	}

	/**
	 * Returns the index of the variable that produces the smallest set of
	 * constraints when it will be eliminated using the Fourier Motzkin
	 * elimination algorithm.
	 * @return the index of the variable that produces the smallest set of
	 * constraints when it will be eliminated using the Fourier Motzkin
	 * elimination algorithm.
	 */
	public int getFourierMotzkinEliminationIndex(){
	    int minFactor = -1;
	    int fmIDX = -1;
	    for (int column = 0; column < width; column++)
		if (!Term.isIntegerType(variables[column].getType())){
		    int fmFactor = getFMFactor(column);
		    if (((minFactor == -1) || (fmFactor < minFactor))){
			fmIDX = column;
			minFactor = fmFactor;
		    }
		}
	    return fmIDX;
	}

	/**
	 * Substitutes the assigned values for each variable contained in the
	 * solution into the current table and returns the resulting reduced
	 * table.
	 * @param variable the variable that should be substituted by a value.
	 * @param value the value that should substitute the variable.
	 * @return the new system of constraints that does not contain the
	 * substituted variable any more.
	 * @throws NoSolutionException if any contradictions appear.
	 */
	public Coefficients insert(NumericVariable variable, NumberWrapper value)
	throws NoSolutionException{
	    Coefficients result = this;
	    int idx = 0;
	    while ((idx < width) && !variables[idx].equals(variable))
		idx++;
	    if (idx < width){
		result = new Coefficients(width - 1, height);
		result.eliminationVariable = eliminationVariable;
		result.pred = pred;
		for (int v = 0; v < idx; v++)
		    result.variables[v] = variables[v];
		for (int v = idx + 1; v < width; v++)
		    result.variables[v - 1] = variables[v];
		for (int row = 0; row < height; row++){
		    result.rowType[row] = rowType[row];
		    for (int v = 0; v < idx; v++)
			result.array[row][v] = array[row][v];
		    for (int v = idx + 1; v < width; v++)
			result.array[row][v - 1] = array[row][v];
		    result.rhs[row] = rhs[row].sub(value.mult(array[row][idx]));
		}
		result = result.clean();
	    }
	    if (result.pred != null)
		result.pred = result.pred.insert(variable, value);
	    return result;
	}

	public Coefficients insert(Solution solution) throws NoSolutionException{
	    Coefficients result = this;
	    for (Variable variable: solution.variables())
		result = result.insert((NumericVariable)variable, numberFactory.getInstance(solution.getNumericValue(variable).getDoubleValue()));
	    return result;
	}

	/**
	 * Prints the actual system of equations and inequations to the console.
	 * Changed to logging 2008.02.05.
	 */
	public void print(){
	    // 2010.07.16: Replaced the GlassTT logger with the Muggl logger. It is faster and saves a lot of memory.
	    if (logger.isDebugEnabled()) {
		String out = "------------";
		out += Arrays.toString(variables);
		for (int row = 0; row < height; row++){
		    out += Arrays.toString(array[row]);
		    switch (rowType[row]){
		    case LESSTHAN:
			out += " < ";
			break;
		    case LESSOREQUAL:
			out += " <= ";
			break;
		    default:
			out += " == ";
		    }
		    out += rhs[row];
		}
		out += "------------";

		Globals.getInst().solverLogger.debug(out);
	    }
	}

    }

    /**
     * Indicates an existing contradiction in one of the reduced systems of
     * equations and inequations that leads the whole system to have no Solution.
     * @author Christoph Lembeck
     */
    @SuppressWarnings("serial")
    static class NoSolutionException extends Exception{

	/**
	 * Creates a new NoSolutionException object with the passed message as
	 * detailed explanation of the exception.
	 * @param msg the detailed explanation of the exception that should be
	 * stored inside the exception.
	 */
	public NoSolutionException(String msg){
	    super(msg);
	}
    }
}
