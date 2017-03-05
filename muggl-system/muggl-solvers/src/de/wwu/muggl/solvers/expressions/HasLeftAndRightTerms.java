package de.wwu.muggl.solvers.expressions;

/**
 * Common interface for equation-type constraints (ge, gt, le, lt, eq, neq)
 * @author Jan C. Dageförde
 *
 */
public interface HasLeftAndRightTerms {
	public Term getLeft();
	public Term getRight();
}
