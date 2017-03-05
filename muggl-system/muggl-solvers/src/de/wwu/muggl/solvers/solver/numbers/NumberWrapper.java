package de.wwu.muggl.solvers.solver.numbers;

import de.wwu.muggl.solvers.expressions.NumericConstant;

/**
 * TODOME doc
 * @author Christoph Lembeck
 */
public interface NumberWrapper {

    public NumberWrapper abs();

    /**
     * Returns the sum of the actual and the passed number.
     * @param addend the number that should be added to this number.
     * @return the sum of the actual number and the passed number.
     */
    public NumberWrapper add(NumberWrapper addend);

    /**
     * Returns the largest (closest to positive infinity) number that is not
     * greater than this number and is equal to a mathematical integer.
     * @return the ceiling of the actual number.
     */
    public NumberWrapper ceil();

    /**
     * Returns the quotient of the actual and the passed number (wich has to
     * be of the type DoubleWrapper too).
     * @param divisor the number this number should by divided by.
     * @return the quotient of the actual number and the passed number.
     */
    public NumberWrapper div(NumberWrapper divisor);

    /**
     * Divides by 2.
     * @return
     */
    public NumberWrapper div2();

    /**
     * Returns the closest double value of the value of the actual wrapper object.
     * @return the closest double value of the value of the actual wrapper object.
     */
    public double doubleValue();

    /**
     * Indicates whether some other NumberWrapper is "equal to" this one, i.e.
     * represents the same value.
     * @param other the ohter number that this one should be compared to.
     * @return <i>true</i> of both wrappers represent the same number,
     * <i>false</i> otherwise.
     */
    public boolean equals(NumberWrapper other);

    public float floatValue();

    /**
     * Returns the smallest (closest to negative infinity) number that is not
     * smaller than this number and is equal to a mathematical integer.
     * @return the floor of the actual number.
     */
    public NumberWrapper floor();

    /**
     * Returns the greatest common divisor of this number and the passed value.
     * @param other the other number the greatest common divisor should be
     * calculated for.
     * @return the greatest common divisor of this number and the passed value.
     */
    public NumberWrapper gcd(NumberWrapper other);

    /**
     * Gets the NumberFactory which produced this NumberWrapper.
     * @return this wrapper's NumberFactory.
     */
    public NumberFactory getFactory();

    /**
     * Checks whether this number is greater or equal than the passed argument.
     * @param other the other number this one should be compared to.
     * @return <i>true</i> if this number is greater or equal than the passed
     * argument, <i>false</i> otherwise.
     */
    public boolean greaterOrEqual(NumberWrapper other);

    /**
     * Checks whether this number is greater than the passed argument.
     * @param other the other number this one should be compared to.
     * @return <i>true</i> if this number is greater than the passed
     * argument, <i>false</i> otherwise.
     */
    public boolean greaterThan(NumberWrapper other);

    public NumberWrapper inc();

    public int intValue();

    public boolean isGreaterOrEqualZero();

    public boolean isGreaterThanZero();

    public boolean isInfinite();

    /**
     * Checks whether the internal value of this wrapper is a mathematical integer
     * or not.
     * @return <i>true</i> if the actual number is integer, <i>false</i> if the
     * number is noninteger.
     */
    public boolean isInteger();

    public boolean isInvalid();

    public boolean isLessOrEqualZero();

    public boolean isLessThanOne();

    /**
     * Checks whether the internal value of this wrapper is negative or not.
     * @return <i>true</i> if the value of the wrapper is &lt; 0, </i>false</i>
     * if the value is &gt;= 0.
     */
    public boolean isLessThanZero();

    public boolean isMinusOne();

    public boolean isNegativeInfinity();

    public boolean isOne();

    public boolean isPositiveInfinity();

    /**
     * Checks whether the represented number is equal to 0 or not.
     * @return <i>true</i> if the represented number is 0, <i>false</i> otherwise.
     */
    public boolean isZero();

    /**
     * Checks whether this number is lesser or equal than the passed argument.
     * @param other the other number this one should be compared to.
     * @return <i>true</i> if this number is lesser or equal than the passed
     * argument, <i>false</i> otherwise.
     */
    public boolean lessOrEqual(NumberWrapper other);

    /**
     * Checks whether this number is lesser than the passed argument.
     * @param other the other number this one should be compared to.
     * @return <i>true</i> if this number is lesser than the passed
     * argument, <i>false</i> otherwise.
     */
    public boolean lessThan(NumberWrapper other);

    public long longValue();

    /**
     * Returns the product of the actual and the passed number.
     * @param factor the number this number should be multiplied with.
     * @return the product of the actual number and the passed number.
     */
    public NumberWrapper mult(NumberWrapper factor);

    /**
     * Returns a wrapper object representing the negated value of this wrapper.
     * @return a wrapper object representing the negated value of this wrapper.
     */
    public NumberWrapper negate();

    public NumberWrapper pow(int exponent);

    public NumberWrapper square();

    /**
     * Returns the difference of the actual and the passed number.
     * @param subtrahend the number that should be subtracted from this number.
     * @return the difference of the actual number and the passed number.
     */
    public NumberWrapper sub(NumberWrapper subtrahend);

    public NumericConstant toNumericConstant(byte type);

    public String toTexString();
}
