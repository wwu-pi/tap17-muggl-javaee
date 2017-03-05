/**
 * 
 */
package de.wwu.muggl.solvers.exceptions;

/**
 * @author Marko Ernsting
 *
 */
@SuppressWarnings("serial")
public class TesttoolException extends Exception {

    /**
     * 
     */
    public TesttoolException() {
    }

    /**
     * @param message
     */
    public TesttoolException(String message) {
	super(message);
    }

    /**
     * @param cause
     */
    public TesttoolException(Throwable cause) {
	super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public TesttoolException(String message, Throwable cause) {
	super(message, cause);
    }

}
