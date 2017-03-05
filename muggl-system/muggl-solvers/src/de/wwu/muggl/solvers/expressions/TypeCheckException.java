package de.wwu.muggl.solvers.expressions;

import de.wwu.muggl.solvers.exceptions.TesttoolException;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("serial")
public class TypeCheckException extends TesttoolException {

    /**
     * Creates a new instance of TypeCheckException.
     * @param msg the message that should be stored inside the exception.
     */
    public TypeCheckException(String msg) {
	super(msg);
    }

}
