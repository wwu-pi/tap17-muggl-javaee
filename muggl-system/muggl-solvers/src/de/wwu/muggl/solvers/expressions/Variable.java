package de.wwu.muggl.solvers.expressions;

import java.io.PrintStream;

/**
 * Defines the basic functionality of single variables that may be used by the
 * virtual machine or in the constraint solvers.
 * @author Christoph Lembeck
 */
public interface Variable extends Expression{

    /**
     * Returns the internal name of this variable. The internal name of
     * the variable is build by a leading character indicating the type of the
     * variable and a consecutively number.
     * @return the internal name of the variable.
     */
    public String getInternalName();

    /**
     * Returns the String representation of the variables name.
     * @return the String representation of the variables name.
     */
    public abstract String getName();
    
    /**
     * Returns the representation of this variable as a latex expression.
     * @return the representation of this variable as a latex expression.
     */
    public abstract String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames);

    /**
     * Writes the actuel state of the variable to the passed PrintStream.
     * @param logStream the stream the actual state of the variable should be
     * written into.
     */
    public abstract void writeToLog(PrintStream logStream);

    /**
     * Is this a variable which was not given by any expression but was only created internally to 
     * represent things like modulos or narrowing type casts in proper constraints
     * @return
     */
    public abstract boolean isInternalVariable();
    
    
//    public Variable getClone();
}
