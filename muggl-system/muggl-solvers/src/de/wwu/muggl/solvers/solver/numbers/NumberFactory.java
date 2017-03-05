package de.wwu.muggl.solvers.solver.numbers;

import de.wwu.muggl.solvers.expressions.NumericConstant;

/**
 * TODOME doc
 * @author Christoph Lembeck
 */
public abstract class NumberFactory {

    /**
     * Returns a NumberWrapper object that represents the passed double value
     * as precise as possible.
     * @param value the value that should be represented by the wrapper.
     * @return the wrapper object representing the passed argument.
     */
    public abstract NumberWrapper getInstance(double value);

    public abstract NumberWrapper getInstance(NumericConstant value);

    /**
     * Returns a wrapper for the constant -1.
     * @return the wrapper for the constant -1.
     */
    public abstract NumberWrapper getMinusOne();

    /**
     * Returns a wrapper for the constant 1.
     * @return the wrapper for the constant 1.
     */
    public abstract NumberWrapper getOne();

    /**
     * Returns a wrapper for the constant 2.
     * @return the wrapper for the constant 2.
     */
    public abstract NumberWrapper getTwo();

    /**
     * Returns a wrapper for the constant 0.
     * @return the wrapper for the constant 0.
     */
    public abstract NumberWrapper getZero();

    /**
     * Generates a number <pre>x</pre> that satisfies the following conditions:
     * <pre>
     * ((x > lowerBound) || (x >= lowerBound && isLowerIncluded)) &&
     * ((x < upperBound) || (x <= upperBound && isUpperIncluded))
     * </pre>
     * @param lowerBound the lower Bound of the interval.
     * @param isLowerIncluded indicates whether the lower bound may be included or
     * not.
     * @param upperBound the upper bonuf of the interval.
     * @param isUpperIncluded indicates whether the lower bound may be included or
     * not.
     * @return a number that satisfies the conditions mentioned above or null if
     * such a number does not exist.
     */
    public NumberWrapper findNumberBetween(NumberWrapper lowerBound, boolean isLowerIncluded, NumberWrapper upperBound, boolean isUpperIncluded) {
	if (lowerBound == null){
	    if (upperBound == null)
		return getZero();
	    else {
		if (upperBound.greaterThan(getZero()))
		    return getZero();
		else
		    return upperBound.sub(getOne());
	    }
	} else {
	    if (upperBound == null){
		if (lowerBound.lessThan(getZero()))
		    return getZero();
		else
		    return lowerBound.add(getOne());
	    } else {
		if (lowerBound.greaterThan(upperBound))
		    return null;
		if (lowerBound.equals(upperBound)){
		    if (isLowerIncluded && isUpperIncluded)
			return lowerBound;
		    else
			return null;
		} else {
		    if (lowerBound.lessThan(getZero()) && upperBound.greaterThan(getZero()))
			return getZero();
		    else
			return lowerBound.add(upperBound).div(getTwo());
		}
	    }
	}
    }

    public abstract NumberWrapper getBinomial(int n, int k);
}
