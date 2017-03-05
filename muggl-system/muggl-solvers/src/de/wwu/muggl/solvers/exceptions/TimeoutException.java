package de.wwu.muggl.solvers.exceptions;

import de.wwu.muggl.solvers.conf.TesttoolConfig;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("serial")
public class TimeoutException extends TesttoolException {

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public TimeoutException(String message){
	super(message);
	if (TesttoolConfig.getLogger().isDebugEnabled())
	    TesttoolConfig.getLogger().debug("TimeoutException thrown: " + message);
    }
}