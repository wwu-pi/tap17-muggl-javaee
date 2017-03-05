package de.wwu.testtool.solver.tsolver;

import java.util.List;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import de.wwu.muggl.solvers.conf.ConfigReader;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;


/**
 * @author Christoph Lembeck
 */
public class DefaultValueGuessSolver implements Solver{

    protected static Constant[] booleanValues = {BooleanConstant.FALSE, BooleanConstant.TRUE};

    protected static Constant[] doubleValues;

    protected static Constant[] floatValues;

    protected static Constant[] intValues;

    protected static Constant[] longValues;

    protected static long timeout;

    static{
	intValues = new Constant[21];
	for (int i = 0; i <= 10; i++)
	    intValues[i] = IntConstant.getInstance(i);
	for (int i = 0; i < 10; i++)
	    intValues[i + 11] = IntConstant.getInstance(-1 - i);

	longValues = new Constant[21];
	for (int i = 0; i <= 10; i++)
	    longValues[i] = LongConstant.getInstance(i);
	for (int i = 0; i < 10; i++)
	    longValues[i + 11] = LongConstant.getInstance(-1 - i);

	floatValues = new Constant[41];
	for (int i = 0; i <= 20; i++)
	    floatValues[i] = FloatConstant.getInstance((float)(0.5 * i));
	for (int i = 0; i < 20; i++)
	    floatValues[i + 21] = FloatConstant.getInstance((float)(-0.5 * i - 0.5));

	doubleValues = new Constant[41];
	for (int i = 0; i <= 20; i++)
	    doubleValues[i] = DoubleConstant.getInstance(0.5 * i);
	for (int i = 0; i < 20; i++)
	    doubleValues[i + 21] = DoubleConstant.getInstance(-0.5 * i - 0.5);

	try{
	    ConfigReader configReader = ConfigReader.getInstance();
	    timeout = Long.parseLong(configReader.getTextContent("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"de.wwu.testtool.solver.tsolver.DefaultValueGuessSolver\"]/param[attribute::name=\"timeout\"]/@value"));
	} catch (XPathExpressionException xpee){
	    throw new InternalError(xpee.toString());
	}
    }

    public static DefaultValueGuessSolver newInstance(SolverManager solverManager){
	return new DefaultValueGuessSolver(solverManager);
    }

    protected SingleConstraintSet constraints;

    protected Solution solution;

    protected SolverManager solverManager;

    private DefaultValueGuessSolver(SolverManager solverManager){
	this.solverManager = solverManager;
    }

    public void addConstraint(SingleConstraint constraint){
	if (constraints == null)
	    constraints = new SingleConstraintSet(constraint);
	else
	    constraints.add(constraint);
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
	return "DefaultValueGuessSolver";
    }

    protected Constant[] getPossibleValues(byte type){
	final Constant[] result;
	switch (type){
	case Expression.BOOLEAN:
	    return booleanValues;
	case Expression.BYTE:
	case Expression.CHAR:
	case Expression.SHORT:
	case Expression.INT:
	    return intValues;
	case Expression.LONG:
	    return longValues;
	case Expression.FLOAT:
	    return floatValues;
	case Expression.DOUBLE:
	    return doubleValues;
	default:
	    result = null;
	}
	return result;
    }

    public List<Class<? extends Solver>> getRequiredSubsolvers(){
	return null;
    }

    public Solution getSolution() throws SolverUnableToDecideException{
	Worker worker = new Worker();
	Thread workerThread = new Thread(worker);
	workerThread.start();
	try{
	    workerThread.join(timeout);
	} catch (InterruptedException ie){
	    // should never occur
	}
	worker.killWorker();
	if (solution != null)
	    return solution;
	throw new SolverUnableToDecideException("DefaultValueGuessSolver");
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

    public HasSolutionInformation hasSolution() throws SolverUnableToDecideException{
	return new HasSolutionInformation(getSolution());
    }

    public void removeConstraint(){
	constraints.removeLastConstraint();
    }

    public Solver reset(){
	return newInstance(solverManager);
    }

    class Worker implements Runnable{

	protected boolean run = true;

	public void killWorker(){
	    run = false;
	}

	public void run(){
	    TreeSet<Variable> variables = new TreeSet<Variable>();
	    constraints.collectVariables(variables);
	    int varCount = variables.size();
	    Variable[] varArray = variables.toArray(new Variable[varCount]);
	    Constant[][] values = new Constant[varCount][];
	    for (int i = 0; i < varCount; i++){
		values[i] = getPossibleValues(varArray[i].getType());
	    }
	    int[] currentSolutionIDX = new int[varCount];
	    while (run && currentSolutionIDX[varCount - 1] < values[varCount - 1].length){
		Solution testSolution = new Solution();
		for (int i = 0; i < varCount; i++)
		    testSolution.addBinding(varArray[i], values[i][currentSolutionIDX[i]]);
		try{
		    if (constraints.validateSolution(testSolution)){
			DefaultValueGuessSolver.this.solution = testSolution;
			run = false;
			break;
		    }
		} catch (IncompleteSolutionException ise){
		    throw new InternalError(ise.toString());
		}
		currentSolutionIDX[0]++;
		for (int idx = 0; currentSolutionIDX[idx] == values[idx].length && idx < currentSolutionIDX.length - 1; idx++){
		    currentSolutionIDX[idx] = 0;
		    currentSolutionIDX[idx+1]++;
		}
	    }
	}
    }
}
