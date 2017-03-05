package de.wwu.muggl.solvers.expressions;

/**
 * Specifies the basic functionality of each constant that may appear during
 * execution of a java program.
 * @author Christoph Lembeck
 */
public interface Constant {

    /**
     * Returns the representation of the constant as latex expression.
     * @return the representation of the constant as latex expression.
     */
    public String toTexString();
}
