package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.solvers.expressions.DoubleConstant;

/**
 * This Generator provides a sequence of DoubleConstant objects at which each value is succeeded by
 * one that is multiplied with a specified value (2.0 by default). The starting value is
 * customizable. It can also be toggled also return the special values Double.NaN,
 * Double.POSITIVE_INFINITY and DOUBLE.NEGATIVE_INFINITY.<br />
 * <br />
 * Examples sequences:
 * <ul>
 * <li>1.0, 2.0, 4.0, 8.0,..., Double.MAX_VALUE</li>
 * <li>-0.5, -1.5, -4.5, -13,5,..., Double.MIN_VALUE</li>
 * <li>10.0, 100.0, 1000.0, 10000.0,..., Double.MAX_VALUE</li>
 * </ul>
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class DoubleMultiplicationGenerator implements Generator {
	// Fields.
	private double startAt;
	private double multiplier;
	private double maximum;
	private double lastValue;
	private boolean specialValues;
	private int	specialValuesReturned;

	/**
	 * Construct the Generator with standard settings. The first Double will have a value of 1.0 and
	 * each successor will be multiplied with 2.0. The maximum value is 4,294,967,296.0. Special
	 * values will not be returned.
	 */
	public DoubleMultiplicationGenerator() {
		this(1.0, 2.0, 4294967296.0, false);
	}

	/**
	 * Construct the Generator with customized settings for the increment. The starting value is 1.0
	 * and successors are multiplied with the specified value. The maximum value is
	 * 4,294,967,296.0. Special values will not be returned.
	 *
	 * @param multiplier The multiplier by which successive values will be multiplied.
	 * @throws IllegalArgumentException If the multiplier is zero, less than zero or an invalid
	 *         value.
	 */
	public DoubleMultiplicationGenerator(double multiplier) {
		this(1.0, multiplier, 4294967296.0, false);
	}

	/**
	 * Construct the Generator with customized settings for the starting value and the increment.
	 * The successors are multiplied with the specified value. The maximum value is
	 * 4,294,967,296.0. Special values will not be returned.
	 *
	 * @param startAt The value initially multiplied with the multiplier.
	 * @param multiplier The multiplier by which successive values will be multiplied.
	 * @throws IllegalArgumentException If the starting value is zero or not a valid value, if the
	 *         multiplier is zero, less than zero or an invalid value..
	 */
	public DoubleMultiplicationGenerator(double startAt, double multiplier) {
		this(startAt, multiplier, 4294967296.0, false);
	}

	/**
	 * Construct the Generator with customized settings for the starting value, the increment and
	 * the maximum value. The successors are multiplied with the specified value. Special values
	 * will not be returned.
	 *
	 * @param startAt The value initially multiplied with the multiplier.
	 * @param multiplier The multiplier by which successive values will be multiplied.
	 * @param maximum The maximum value.
	 * @throws IllegalArgumentException If the starting value is zero or not a valid value, if the
	 *         multiplier is zero, less than zero or an invalid value or if the maximum value is
	 *         invalid.
	 */
	public DoubleMultiplicationGenerator(double startAt, double multiplier, double maximum) {
		this(startAt, multiplier, maximum, false);
	}

	/**
	 * Construct the Generator with customized settings for the starting value, the direction, the
	 * increment and for special values.
	 *
	 * @param startAt The value initially multiplied with the multiplier.
	 * @param multiplier The multiplier by which successive values will be multiplied.
	 * @param maximum The maximum value.
	 * @param specialValues Toggles whether the special values Double.NaN, Double.POSITIVE_INFINITY
	 *        and DOUBLE.NEGATIVE_INFINITY are returned.
	 * @throws IllegalArgumentException If the starting value is zero or not a valid value, if the
	 *         multiplier is zero, less than zero or an invalid value or if the maximum value is
	 *         invalid.
	 */
	public DoubleMultiplicationGenerator(double startAt, double multiplier, double maximum,
			boolean specialValues) {
		// Check for problems.
		if (Double.valueOf(startAt).isNaN())
			throw new IllegalArgumentException("The starting value must be a number.");
		if (Double.valueOf(startAt).isInfinite())
			throw new IllegalArgumentException("The starting value must not be infinite.");
		if (startAt == 0.0)
			throw new IllegalArgumentException("The starting value must not be zero.");
		if (Double.valueOf(multiplier).isNaN())
			throw new IllegalArgumentException("The multiplier must be a number.");
		if (Double.valueOf(multiplier).isInfinite())
			throw new IllegalArgumentException("The multiplier must not be infinite.");
		if (multiplier == 0.0)
			throw new IllegalArgumentException("The multiplier must not be zero.");
		if (multiplier < 0.0)
			throw new IllegalArgumentException("The multiplier must not be less than zero. "
					+ "Use a negative starting value to configure a decreasing generator.");
		if (Double.valueOf(maximum).isNaN())
			throw new IllegalArgumentException("The maximum must be a number.");
		if (Double.valueOf(maximum).isInfinite())
			throw new IllegalArgumentException("The maximum must not be infinite.");
		if (multiplier == 0.0)
			throw new IllegalArgumentException("The maximum must not be zero.");
		if (startAt < 0.0 && maximum > startAt)
			throw new IllegalArgumentException("If the starting value is negative, maximum has to be less than it is.");
		if (startAt > 0.0 && maximum < startAt)
			throw new IllegalArgumentException("If the starting value is positive, maximum has to be greater than it is.");
		// Set the parameters.
		this.startAt = startAt;
		this.lastValue = startAt;
		this.multiplier = multiplier;
		this.maximum = maximum;
		this.specialValues = specialValues;
		this.specialValuesReturned = 0;
	}

	/**
	 * Returns true as the Generator can be used along with a ChoicePoint.
	 *
	 * @return true
	 */
	public boolean allowsChoicePoint() {
		return true;
	}

	/**
	 * Check if this generator can provide another object. It does if the last returned Double
	 * was not the maximum / minimum specified and there is not a special values which should be but
	 * has not be returned, yet.
	 *
	 * @return true, if this generator can provide another object, false otherwise.
	 */
	public boolean hasAnotherObject()  {
		if (this.lastValue == this.maximum || this.specialValuesReturned > 0) {
			return hasMoreSpecialValues();
		}
		return true;
	}

	/**
	 * Check if more special values can be returned or if either no returning of spcial values is
	 * desired or all three special values have been returned already.
	 *
	 * @return true, if more special values can be returned, false otherwise.
	 */
	private boolean hasMoreSpecialValues() {
		if (this.specialValues && this.specialValuesReturned < 3) {
			return true;
		}
		return false;
	}

	/**
	 * Provide the next Double.
	 *
	 * @return The next Double.
	 * @throws IllegalStateException If no more elements are available. This cannot happen if
	 *         checking the availability with hasAnotherElement() first and not having more than one
	 *         thread access this generator.
	 */
	public DoubleConstant provideObject() {
		if (!hasAnotherObject())
			throw new IllegalStateException("There are no more elements available.");
		if (this.lastValue == this.maximum || this.specialValuesReturned > 0) {
			this.lastValue = getNextSpecialValue();
		} else {
			double newValue = this.lastValue * this.multiplier;
			if (newValue == Double.POSITIVE_INFINITY || newValue == Double.NEGATIVE_INFINITY
					|| (this.startAt < 0.0 && newValue < this.maximum)
					|| (this.startAt > 0.0 && newValue > this.maximum)) {
				this.lastValue = this.maximum;
			} else {
				this.lastValue = newValue;
			}
		}
		return DoubleConstant.getInstance(this.lastValue);
	}

	/**
	 * Provide the next special value. This method does not check if special values are supposed to
	 * be returned at all or there are any more special values to be returned; from the third
	 * request on it will simply return Double.NaN. So use {@link #hasMoreSpecialValues()} first.
	 *
	 * @return The next special value (either Double.NaN, Double.POSITIVE_INFINITY or
	 *         DOUBLE.NEGATIVE_INFINITY).
	 */
	private double getNextSpecialValue() {
		double specialValue = Double.NaN;
		if (this.specialValuesReturned == 0) {
			specialValue = Double.POSITIVE_INFINITY;
		} else if (this.specialValuesReturned == 1) {
			specialValue = Double.NEGATIVE_INFINITY;
		}
		this.specialValuesReturned++;
		return specialValue;
	}

	/**
	 * Check if this generator supplies java objects that are no wrappers for primitive types. It
	 * does not do so but supplied Double objects that wrap primitive double values.
	 *
	 * @return false.
	 */
	public boolean objectNeedsConversion() {
		return false;
	}

	/**
	 * Reset this generator to the starting value.
	 */
	public void reset() {
		this.lastValue = this.startAt;
		this.specialValuesReturned = 0;
	}

	/**
	 * Get the name of the Generator.
	 * @return "Double increment generator".
	 */
	public String getName() {
		return "Double multiplication generator";
	}

	/**
	 * Get a description of what the generator does and how it works.
	 * @return A description of the generator.
	 */
	public String getDescription() {
		return  "Provides a sequence of DoubleConstant objects at which each value "
		 	+ "is succeeded by one that is mutiplicated with a specified value (2.0 by default). "
		 	+ "The starting value and the increment are customizable.\n\n"
		 	+ "Examples sequences:\n"
		 	+ "1.0,  2.0,  4.0,  8.0,..., Double.MAX_VALUE\n"
		 	+ "-0.5, -1.5, -4.5, -13.5,..., Double.MIN_VALUE\n"
		 	+ "10.0,  100.0,  1000.0,  10000.0,..., Double.MAX_VALUE";
	}

}
