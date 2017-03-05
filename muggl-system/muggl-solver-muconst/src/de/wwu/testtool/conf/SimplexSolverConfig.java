package de.wwu.testtool.conf;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import de.wwu.muggl.solvers.conf.ConfigReader;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.testtool.solver.tsolver.SimplexSolver;
import de.wwu.testtool.solver.tsolver.SimplexSolverCL;

/**
 * Configuration class encapsulating configuration for the SimplexSolverCL.
 * <br/><br/>
 * Configuration is read from {@link ConfigReader} configuration file.
 * 
 * @author Marko Ernsting
 *
 */
public class SimplexSolverConfig {

    private static SimplexSolverConfig instance = new SimplexSolverConfig();
    
    public static SimplexSolverConfig getInstance(){
	return instance;
    }
        
    protected static Logger logger;
    private static long timeout;
    
    /**
     * Do we want to derive Gomory Cuts?
     */
    private static boolean useGomoryCuts;

    /**
     * When doing branch and bound it is desirable to change the selection strategy
     * for non-basic variables to enter the basis.
     * <BR>
     * By prefering additonal variables to enter the basis we have a higher chance of
     * not changing the problem variables values, which would lead to further recursive
     * branch and bound calls.
     */
    private static boolean useAdditionalVarPreferredForBranchAndBound;

    /**
     * Threshold for for constraint adjustment in case of rounding errors.
     */
    private static double zeroThreshold = 1E-12;
    private static boolean useZeroThresholdingForGomoryCuts;
    private static boolean useZeroThresholdingForPivoting;
    
    /**
     * Postsolving.
     */
    private static boolean usePostSolvingForRoundingErrors = true;
    private static double postSolvingViolatingFactor = 10.;    
    private static int postSolvingRuns = 10;
    
    private static boolean useIncrementalSolving = true;
    private static boolean useBacktracking = true;    
    
    
    
    // config
    
    /**
     * The number factory used by the simplex solver.
     */
    protected static NumberFactory defaultNumberFactory;

    static {
	logger = TesttoolConfig.getLogger();
	ConfigReader configReader = ConfigReader.getInstance();
	
	try{
	    Node solverNode = configReader.getNode("//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"" + SimplexSolverCL.class.getName() + "\"]");
	    String selectedNumberFactoryName = configReader.getTextContent("NumberFactory/@selected", solverNode);
	    String selectedFactoryClass = configReader.getTextContent("NumberFactory/NumberFactoryOption[attribute::name=\"" + selectedNumberFactoryName + "\"]/@class", solverNode);
	    defaultNumberFactory = (NumberFactory) Class.forName(selectedFactoryClass).newInstance();
	    
	    String preString = "//TesttoolConfiguration/SolverSystem/SolverList/Solver[attribute::class=\"de.wwu.testtool.solver.tsolver.SimplexSolver\"]/param[attribute::name=\"";
	    String postString = "\"]/@value";
	    	    
	    timeout = Long.parseLong(configReader.getTextContent(preString + "timeout" + postString));
	    useGomoryCuts = Boolean.parseBoolean(configReader.getTextContent(preString + "useGomoryCuts" + postString));;
	    useAdditionalVarPreferredForBranchAndBound = Boolean.parseBoolean(configReader.getTextContent(preString + "useAdditionalVarPreferredForBranchAndBound" + postString));
	    zeroThreshold = Double.parseDouble(configReader.getTextContent(preString + "zeroThreshold" + postString));
	    useZeroThresholdingForGomoryCuts = Boolean.parseBoolean(configReader.getTextContent(preString + "useZeroThresholdingForGomoryCuts" + postString));
	    useZeroThresholdingForPivoting = Boolean.parseBoolean(configReader.getTextContent(preString + "useZeroThresholdingForPivoting" + postString));
	    usePostSolvingForRoundingErrors = Boolean.parseBoolean(configReader.getTextContent(preString + "usePostSolvingForRoundingErrors" + postString));
	    postSolvingViolatingFactor = Double.parseDouble(configReader.getTextContent(preString + "postSolvingViolatingFactor" + postString));    
	    postSolvingRuns = Integer.parseInt(configReader.getTextContent(preString + "postSolvingRuns" + postString));
	    useIncrementalSolving = Boolean.parseBoolean(configReader.getTextContent(preString + "useIncrementalSolving" + postString));
	    useBacktracking = Boolean.parseBoolean(configReader.getTextContent(preString + "useBacktracking" + postString));
	    
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
    
    // getter/setter
    
    public NumberFactory getDefaultNumberFactory() { return defaultNumberFactory; }

 
    public long getTimeout() {
	return timeout;
    }
    public void setTimeout(long ms) {
	timeout = ms;
    }
    
    /**
     * do we need this builder pattern, i.e. do we need different simplex solvers with
     * different configurations?
     * @return
     */
    public static SimplexSolver getSimplexSolverInstance(){
	SimplexSolver solver = new SimplexSolver();
	solver.setTimeout(SimplexSolverConfig.getInstance().getTimeout());
	solver.setDebugLogger(logger);
	return solver;
    }

    public void setLogger(Logger newLogger) {
	logger = newLogger;
    }
    
    public Logger getLogger(){
	return logger;
    }
    
    public boolean getUseGomoryCuts(){
	return useGomoryCuts;
    }

    /**
     * When doing branch and bound it is desirable to change the selection strategy
     * for non-basic variables to enter the basis.
     * <BR>
     * By prefering additonal variables to enter the basis we have a higher chance of
     * not changing the problem variables values, which would lead to further recursive
     * branch and bound calls.
     */
    public boolean getUseAdditionalVarPreferredForBranchAndBound(){
	return useAdditionalVarPreferredForBranchAndBound;
    }

    /**
     * Threshold for for constraint adjustment in case of rounding errors.
     */
    public double getZeroThreshold(){
	return zeroThreshold;
    }
    public boolean getUseZeroThresholdingForGomoryCuts(){
	return useZeroThresholdingForGomoryCuts;
    }
    public boolean getUseZeroThresholdingForPivoting(){
	return useZeroThresholdingForPivoting;
    }
    
    /**
     * Postsolving.
     */
    public boolean getUsePostSolvingForRoundingErrors(){
	return usePostSolvingForRoundingErrors;
    }
    public double getPostSolvingViolatingFactor(){
	return postSolvingViolatingFactor;
    }    
    public int getPostSolvingRuns(){
	return postSolvingRuns;
    }
    
    public boolean getUseIncrementalSolving(){
	return useIncrementalSolving;
    }
    public boolean getUseBacktracking(){
	return useBacktracking;
    }
}
