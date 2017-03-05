package de.wwu.muggl.solvers.exceptions;

/**
 * The exception that appears when a solver will be instructed to find a vaild
 * solution for a constraint or a set of constraints he is not able to deal
 * with.
 * @author Christoph
 */
@SuppressWarnings("serial")
public class IncorrectSolverException extends TesttoolException {

    /**
     * Creates a new IncorrectSolverException with the specified detailed message.
     * @param message the message that should be assigned to the exception.
     */
    public IncorrectSolverException(String message){
	super(message);
    }

}
