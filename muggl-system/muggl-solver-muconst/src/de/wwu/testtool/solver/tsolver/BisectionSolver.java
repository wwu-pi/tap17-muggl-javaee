package de.wwu.testtool.solver.tsolver;

import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.conf.ConfigReader;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.muggl.solvers.solver.tsolver.bisection.Hypercube;
import de.wwu.muggl.solvers.solver.tsolver.bisection.Interval;
import de.wwu.muggl.solvers.solver.tsolver.bisection.MultiIndexMap;
import de.wwu.muggl.solvers.solver.tsolver.bisection.MultiIndexVariablesReference;
import de.wwu.testtool.tools.Timer;



/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class BisectionSolver implements Solver{

    protected static NumberFactory defaultNumberFactory;

    /**
     * Indicates an equation.
     */
    public static final byte EQUAL_TO_ZERO = 0;

    /**
     * Indicates a weak inequation.
     */
    public static final byte LESS_OR_EQUAL_ZERO = 1;

    /**
     * Indicates a strong inequation.
     */
    public static final byte LESS_THAN_ZERO = 2;

    protected static double maxDouble;

    protected static float maxFloat;

    protected static double minDouble;

    protected static float minFloat;

    protected static long timeout;

    static{
	try{
	    ConfigReader configReader = ConfigReader.getInstance();
	    Node solverNode = configReader.getNode("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"" + BisectionSolver.class.getName() + "\"]");
	    String selectedNumberFactoryName = configReader.getTextContent("NumberFactory/@selected", solverNode);
	    String selectedFactoryClass = configReader.getTextContent("NumberFactory/NumberFactoryOption[attribute::name=\"" + selectedNumberFactoryName + "\"]/@class", solverNode);
	    defaultNumberFactory = (NumberFactory)Class.forName(selectedFactoryClass).newInstance();
	    timeout = Long.parseLong(configReader.getTextContent("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"de.wwu.testtool.solver.tsolver.BisectionSolver\"]/param[attribute::name=\"timeout\"]/@value"));
	    if (timeout < 0)
		timeout = 0;
	    String value = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"de.wwu.testtool.solver.tsolver.BisectionSolver\"]/param[attribute::name=\"minFloat\"]/@value");
	    if (value == null || value.equals("-Float.MAX_VALUE"))
		minFloat = -Float.MAX_VALUE;
	    else
		minFloat = Float.parseFloat(value);
	    value = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"de.wwu.testtool.solver.tsolver.BisectionSolver\"]/param[attribute::name=\"maxFloat\"]/@value");
	    if (value == null || value.equals("Float.MAX_VALUE"))
		minFloat = Float.MAX_VALUE;
	    else
		minFloat = Float.parseFloat(value);
	    value = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"de.wwu.testtool.solver.tsolver.BisectionSolver\"]/param[attribute::name=\"minDouble\"]/@value");
	    if (value == null || value.equals("-Double.MAX_VALUE"))
		minDouble = -Double.MAX_VALUE;
	    else
		minDouble = Double.parseDouble(value);
	    value = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"de.wwu.testtool.solver.tsolver.BisectionSolver\"]/param[attribute::name=\"maxDouble\"]/@value");
	    if (value == null || value.equals("Double.MAX_VALUE"))
		maxDouble = Double.MAX_VALUE;
	    else
		maxDouble = Double.parseDouble(value);
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

    public static BisectionSolver newInstance(SolverManager solverManager){
	return new BisectionSolver(solverManager, defaultNumberFactory);
    }

    public static BisectionSolver newInstance(SolverManager solverManager, NumberFactory numberFactory){
	return new BisectionSolver(solverManager, numberFactory);
    }

    protected SingleConstraintSet constraints;

    protected byte[] constraintTypes;

    protected NumberWrapper epsilon;

    protected MultiIndexMap[] initialMaps;

    protected NumberWrapper minusEpsilon;

    /**
     * The actual number factory for the generation of numbers for internal use of
     * the algorithm.
     */
    protected NumberFactory numberFactory;

    protected SolverManager solverManager;

    private BisectionSolver(SolverManager solverManager, NumberFactory numberFactory){
	this.solverManager = solverManager;
	this.numberFactory = numberFactory;
	epsilon = numberFactory.getInstance(1e-10);
	minusEpsilon = epsilon.negate();

    }

    public void addConstraint(SingleConstraint constraint) throws IncorrectSolverException{
	if (constraints == null)
	    constraints = new SingleConstraintSet(constraint);
	else
	    constraints.add(constraint);
	// CLTODO ganz dringend init() �berarbeiten oder ersetzen!!!
	init();
    }

    public void addConstraintSet(SingleConstraintSet set) throws IncorrectSolverException{
	for (SingleConstraint constraint: set)
	    addConstraint(constraint);
    }

    /**
     * Returns the name of the solver for displaying it in some user interfaces or
     * logfiles.
     * @return the name of the solver.
     */
    public String getName(){
	return "BisectionSolver";
    }

    public List<Class<? extends Solver>> getRequiredSubsolvers(){
	return null;
    }

    public Solution getSolution() throws TimeoutException{
	Timer timer = null;
	if (timeout > 0){
	    timer = new Timer(timeout);
	    timer.run();
	} else {
	    timer = new Timer(0);
	}
	//    BisectionSolverTestPanel panel = new BisectionSolverTestPanel(800, 800, 100, 100);
	TreeSet<NumericVariable> variables = new TreeSet<NumericVariable>();
	constraints.collectNumericVariables(variables);
	MultiIndexVariablesReference varRef = new MultiIndexVariablesReference(variables);
	Hypercube cube = new Hypercube(varRef);
	for (int i = 0; i < cube.getDimension(); i++){
	    NumericVariable var = cube.getVariable(i);
	    switch (var.getType()){
	    case Expression.BYTE:
		cube.setInterval(var, new Interval(numberFactory.getInstance(Byte.MIN_VALUE), numberFactory.getInstance(Byte.MAX_VALUE)));
		break;
	    case Expression.SHORT:
		cube.setInterval(var, new Interval(numberFactory.getInstance(Short.MIN_VALUE), numberFactory.getInstance(Byte.MAX_VALUE)));
		break;
	    case Expression.CHAR:
		cube.setInterval(var, new Interval(numberFactory.getInstance(Character.MIN_VALUE), numberFactory.getInstance(Character.MAX_VALUE)));
		break;
	    case Expression.INT:
		cube.setInterval(var, new Interval(numberFactory.getInstance(Integer.MIN_VALUE), numberFactory.getInstance(Integer.MAX_VALUE)));
		break;
	    case Expression.LONG:
		cube.setInterval(var, new Interval(numberFactory.getInstance(Long.MIN_VALUE), numberFactory.getInstance(Long.MAX_VALUE)));
		break;
	    case Expression.FLOAT:
		cube.setInterval(var, new Interval(numberFactory.getInstance(-Float.MAX_VALUE), numberFactory.getInstance(Float.MAX_VALUE)));
		break;
	    case Expression.DOUBLE:
		cube.setInterval(var, new Interval(numberFactory.getInstance(minDouble), numberFactory.getInstance(maxDouble)));
		break;
	    }
	}
	Stack<Hypercube> cubes = new Stack<Hypercube>();
	cubes.push(cube);
	MultiIndexMap[] maps = new MultiIndexMap[initialMaps.length];
	Solution lastTestSolution = Solution.NOSOLUTION;

	while (!cubes.isEmpty()){
	    try{
		if (timer.timeout()){
		    // 2010.07.16: Replaced the GlassTT logger with the Muggl logger. It is faster and saves a lot of memory.
		    if (Globals.getInst().solverLogger.isInfoEnabled()) Globals.getInst().solverLogger.info("BisectionSOlver TIMEOUT"); // Changed 2008.02.05
		    throw new TimeoutException("BisectionSolver");
		}
	    } catch (NullPointerException npe){
		// ok, no timeout was set...
	    }
	    cube = cubes.pop();
	    NumberWrapper[] center = cube.getCenter();
	    if (center != null){
		/* check if the point in the center of the cube already satisfies
		 * the constraints. */

		Solution testSolution = new Solution();
		for (int i = 0; i < cube.getDimension(); i++){
		    NumericVariable var = cube.getVariable(i);
		    switch (var.getType()){
		    case Expression.DOUBLE:
			testSolution.addBinding(var, DoubleConstant.getInstance(center[i].doubleValue()));
			break;
		    case Expression.FLOAT:
			testSolution.addBinding(var, FloatConstant.getInstance(center[i].floatValue()));
			break;
		    case Expression.LONG:
			testSolution.addBinding(var, LongConstant.getInstance(center[i].longValue()));
			break;
		    default:
			testSolution.addBinding(var, IntConstant.getInstance(center[i].intValue()));
		    }
		}
		/*        System.out.println("cube" + cube);
        for (int i = 0; i < cube.getDimension(); i++){
          Interval interval = cube.getInterval(i);
          NumberWrapper start = interval.start;
          NumberWrapper end = interval.end;
          System.out.print(start.longValue() + " <= " + cube.getVariable(i) + " <= " + end.longValue() + ", ");
        }
        System.out.println();*/

		/*        Interval xInter = cube.getInterval(0);
        Interval yInter = cube.getInterval(1);
        panel.drawRect(xInter.start.longValue(), yInter.start.longValue(), xInter.end.longValue(), yInter.end.longValue(), Color.YELLOW);
        try{
//          Thread.sleep(1000);
        } catch (Exception e){

        }
        panel.drawRect(xInter.start.longValue(), yInter.start.longValue(), xInter.end.longValue(), yInter.end.longValue(), Color.RED);
		 */
		if (!testSolution.equals(lastTestSolution))
		    //          System.out.println("teste " + Arrays.toString(center));

		    if (!testSolution.equals(lastTestSolution)){
			// 18.03.11
			// boolean validSolution = solverManager.verifySolution(testSolution);
			boolean validSolution = false;
			try {
			    validSolution = constraints.validateSolution(testSolution);
			} catch (IncompleteSolutionException e) {
			    e.printStackTrace();
			}
			
			if (validSolution)
			    return testSolution;
		    }
		lastTestSolution = testSolution;
		/* If we reach here, the center does not satisfy the constraints.
		 * So we have to check if the constraints may be satisfied at all
		 * inside the current cube. */
		boolean maybeSolvable = true;
		for (int constraintIdx = 0; constraintIdx < maps.length && maybeSolvable; constraintIdx++){
		    maps[constraintIdx] = initialMaps[constraintIdx].scale(cube);
		    if (!maps[constraintIdx].containsInfiniteValues()){
			MultiIndexMap bernsteinCoeff = maps[constraintIdx].getBernsteinCoefficientsDirect();
			if (!bernsteinCoeff.containsInfiniteValues()){
			    NumberWrapper[] minMax = bernsteinCoeff.getMinAndMaxValue();
			    NumberWrapper min = minMax[0];
			    NumberWrapper max = minMax[1];
			    switch(constraintTypes[constraintIdx]){
			    case EQUAL_TO_ZERO:
				if (min.greaterThan(epsilon) || max.lessThan(minusEpsilon))
				    maybeSolvable = false;
				break;
			    case LESS_OR_EQUAL_ZERO:
				if (min.greaterThan(numberFactory.getZero()))
				    maybeSolvable = false;
				break;
			    case LESS_THAN_ZERO:
				if (min.greaterOrEqual(numberFactory.getZero()))
				    maybeSolvable = false;
				break;
			    default:
				throw new InternalError("unknown constraint type");
			    }
			}
		    }
		}
		if (maybeSolvable){
		    Hypercube[] newCubes = cube.divide(epsilon);
		    if (newCubes != null)
			for (Hypercube newCube: newCubes)
			    cubes.push(newCube);
		}
	    }
	}
	return Solution.NOSOLUTION;
    }

    public boolean handlesEquations(){
	return true;
    }

    public boolean handlesIntegerEquations(){
	return true;
    }

    public boolean handlesNonlinearProblems(){
	return true;
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

    public HasSolutionInformation hasSolution() throws TimeoutException{
	return new HasSolutionInformation(getSolution());
    }

    private void init() throws IncorrectSolverException{
	initialMaps = new MultiIndexMap[constraints.getConstraintCount()];
	constraintTypes = new byte[constraints.getConstraintCount()];
	for (int idx = 0; idx < constraints.getConstraintCount(); idx++){
	    SingleConstraint constraint = constraints.getConstraint(idx);
	    if (!(constraint instanceof NumericConstraint))
		throw new IncorrectSolverException("Only numeric constraints are allowed for the BisectionSolver");
	    NumericConstraint nConstraint = (NumericConstraint)constraint;
	    initialMaps[idx] = nConstraint.getPolynomial().getMultiIndexCoefficients(numberFactory);
	    if (nConstraint.isEquation())
		constraintTypes[idx] = EQUAL_TO_ZERO;
	    else
		if (nConstraint.isStrictInequation())
		    constraintTypes[idx] = LESS_THAN_ZERO;
		else
		    constraintTypes[idx] = LESS_OR_EQUAL_ZERO;
	}
    }

    public void removeConstraint(){
	// CLTODO Auto-generated method stub
    }

    public Solver reset(){
	return newInstance(solverManager, numberFactory);
    }

}
