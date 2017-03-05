package de.wwu.muggl.solvers.exceptions;

/**
 * Thrown if it was recognized that a solution does not contain bindings for all
 * the variables that appear in a special systems of contraints.
 * @author Christoph Lembeck
 */
@SuppressWarnings("serial")
public class IncompleteSolutionException extends TesttoolException {

    /**
     * Creates a new instance of IncompleteSolutionException with the passed
     * message as detailed description of the exception.
     * 
     * @param message the detailed description to the exception.
     */
    public IncompleteSolutionException(String message){
	super(message);
    }
}
