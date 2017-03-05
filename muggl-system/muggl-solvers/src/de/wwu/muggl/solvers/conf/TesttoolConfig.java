package de.wwu.muggl.solvers.conf;

import java.io.IOException;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TexLayout;
import org.w3c.dom.Node;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSetTransformer;

/**
 * Global configuration options for the testtool.
 * <br/><br/>
 * Configuration is read from the {@link ConfigReader} configuration file.
 *
 * @author Marko Ernsting
 */
public class TesttoolConfig {
    
    private static TesttoolConfig instance = new TesttoolConfig();
    
    public static TesttoolConfig getInstance(){
	return instance;
    }
    
    // this one can be replaced with any kind of Logger
    protected static Logger logger;
    // these two are for debugging purposes only and not to be changed
    protected static Logger texLogger;
    
    // NumericVariable
    protected static String internalVariablesNamePrefix;
    protected static String internalVariablesNamePostfix;
    
    // ConstraintStack
    protected static ConstraintSetTransformer[] transformersForMethodGetSystem;
    protected static ConstraintSetTransformer[] transformersForStackElements;

    // logger filenames
    protected static String texFileLoggerFilename;
    protected static String XMLLoggerFilename;
    
    static {
	ConfigReader configReader = ConfigReader.getInstance();
	
	// logging
	try{
	    // solverLogLevel = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/General/solverlog/@level");
	    // solverlogFilename = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/General/solverlog/@filename");	    
	    XMLLoggerFilename = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/General/logfile/@filename");
	    texFileLoggerFilename = configReader.getTextContent("//TesttoolConfiguration/SolverSystem/General/SolverManagerListener[attribute::class='de.wwu.testtool.solver.listener.TexLogListener']/@filename");
	} catch (XPathExpressionException xpee){
	    throw new InternalError(xpee.toString());
	}	
	
	// use the muggl logger for now
	logger = Globals.getInst().solverLogger;
	texLogger = Logger.getLogger("de.wwu.testtool.tex");
	TexLayout texLayout = new TexLayout();

	try {
	    FileAppender texFileAppender = new FileAppender( texLayout, texFileLoggerFilename, false);
	    texFileAppender.setName(texFileLoggerFilename);
	    texLogger.addAppender(texFileAppender);
	} catch (IOException e) {
	    System.out.println("Fatal error: Could not initialize logging due to an I/O error. Halting.\n" + e);
	    System.exit(1);
	}
	logger.info("TexLogger log file name is " + texFileLoggerFilename );
	logger.info("XMLLogger log file name is " + XMLLoggerFilename );
	
	// internal variable Names
	try{
	    Node node = configReader.getNode("//TesttoolConfiguration/SolverSystem/General/InternalVariablesNames");
	    
	    internalVariablesNamePrefix = configReader.getTextContent("@prefix", node);
	    if (logger.isDebugEnabled())
		logger.debug("Config: "
			+ "NumericVariable: prefix for internal variables: &quot;"
			+ internalVariablesNamePrefix + "&quot;");
	    
	    internalVariablesNamePostfix = configReader.getTextContent("@postfix", node);
	    if (logger.isDebugEnabled())
		logger.debug("Config: "
			+ "NumericVariable: postfix for internal variables: &quot;" + internalVariablesNamePostfix + "&quot;");
	    
	    if (internalVariablesNamePrefix.length() == 0){
		if (logger.isEnabledFor(Level.WARN)) logger.warn("Warning: Invalid prefix for internal variabels. Please check the configuration file!");
		throw new InternalError("Warning: Invalid prefix for internal variabels. Please check the configuration file!");
	    }
	} catch (XPathException xpe){
	    if (logger.isEnabledFor(Level.WARN)) logger.warn("Warning: Invalid prefix for internal variabels. Please check the configuration file!");
	    throw new InternalError(xpe.toString());
	}
	
	
	// ConstraintStack
	try{
	    String[] classNames = configReader.getTextContents("//TesttoolConfiguration/SolverSystem/ConstraintStack/ConstraintSetTransformerListForGetSystemMethod/ConstraintSetTransformer[attribute::enabled='yes']/@class");
	    transformersForMethodGetSystem = new ConstraintSetTransformer[classNames.length];
	    for (int i = 0; i < classNames.length; i++){
		transformersForMethodGetSystem[i] = (ConstraintSetTransformer)Class.forName(classNames[i]).newInstance();
		if (logger.isDebugEnabled()) 
		    logger.debug("Config: " + "ConstraintStack: add transformer for method getSystem " + transformersForMethodGetSystem[i].getClass().getName());
	    }
	    
	    classNames = configReader.getTextContents("//TesttoolConfiguration/SolverSystem/ConstraintStack/ConstraintSetTransformerListForStackElements/ConstraintSetTransformer[attribute::enabled='yes']/@class");
	    transformersForStackElements = new ConstraintSetTransformer[classNames.length];
	    for (int i = 0; i < classNames.length; i++){
		transformersForStackElements[i] = (ConstraintSetTransformer)Class.forName(classNames[i]).newInstance();
		if (logger.isDebugEnabled())
		    logger.debug("Config: " + "ConstraintStack: add transformer for stack elements " + transformersForMethodGetSystem[i].getClass().getName());
	    }
	} catch (XPathExpressionException e){
	    throw new InternalError(e.toString());
	} catch (ClassNotFoundException cnfe){
	    throw new InternalError(cnfe.toString());
	} catch (IllegalAccessException iae){
	    throw new InternalError(iae.toString());
	} catch (InstantiationException ie){
	    throw new InternalError(ie.toString());
	}	
    }
    
    /**
     * This is needed and it must be called by SolverManager.finalize() to properly shut down
     * the loggers. The footer for the TexLogger file is not written if omitted.
     */
    public void finalize() {
	TesttoolConfig.getTexLogger().removeAllAppenders();
    }
    
    // getter/setter
    public String getInternalVariablesNamePrefix() { return internalVariablesNamePrefix; }
    public String getInternalVariablesNamePostfix() { return internalVariablesNamePostfix; }
    
    public ConstraintSetTransformer[] getTransformersForMethodGetSystem() { return transformersForMethodGetSystem; }
    public ConstraintSetTransformer[] getTransformersForStackElements() { return transformersForStackElements; }
    
    public String getTexFileLoggerFilename() { return texFileLoggerFilename; }
    public String getXMLLoggerFilename() { return XMLLoggerFilename; };    
    
    public static Logger getLogger() { return logger;}
    public static void setLogger(Logger logger) { TesttoolConfig.logger = logger; }
    
    public static Logger getTexLogger() { return texLogger; }
}
