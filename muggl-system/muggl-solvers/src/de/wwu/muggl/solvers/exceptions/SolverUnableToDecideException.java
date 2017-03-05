package de.wwu.muggl.solvers.exceptions;

import de.wwu.muggl.solvers.conf.TesttoolConfig;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("serial")
public class SolverUnableToDecideException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public SolverUnableToDecideException(String message){
	super(message);
	if (TesttoolConfig.getLogger().isDebugEnabled())
	    TesttoolConfig.getLogger().debug("SolverUnableToDecideException thrown: " + message);
    }

}
