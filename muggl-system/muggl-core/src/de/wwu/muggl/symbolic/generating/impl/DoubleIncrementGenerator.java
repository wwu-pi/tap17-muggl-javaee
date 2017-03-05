package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.solvers.expressions.DoubleConstant;

/**
 * This Generator provides a sequence of DoubleConstant objects at which each value is succeeded by
 * one that is increased respectively decreased by a specified value (1.0 by default). The starting
 * value and the increment are customizable. It can also be toggled also return the special values
 * Double.NaN, Double.POSITIVE_INFINITY and DOUBLE.NEGATIVE_INFINITY.<br />
 * <br />
 * Examples sequences:
 * <ul>
 * <li>0.0, 1.0, 2.0, 3.0,..., Double.MAX_VALUE</li>
 * <li>0.5, -0.5, -1.5, -2.5,..., Double.MIN_VALUE</li>
 * <li>5.7, 6.8, 7.9, 10,..., Double.MAX_VALUE</li>
 * </ul>
 * <br />
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class DoubleIncrementGenerator implements Generator {
	// Fields.
	private double startAt;
	private boolean countDown;
	private double increment;
	private double maximum;
	private double lastValue;
	private boolean specialValues;
	private int specialValuesReturned;

	/**
	 * Construct the Generator with standard settings. The first Double will
	 * have a value of 0.0 and each successor will be increased by 1.0. The maximum
	 * value is Double.MAX_VALUE. Special values will not be returned.
	 */
	public DoubleIncrementGenerator() {
		this(0.0, false, 1.0, Double.MAX_VALUE, false);
	}

	/**
	 * Construct the Generator with customized settings for the increment. The starting value is 0.0
	 * and successors always have a increased value. The maximum value is Double.MAX_VALUE. Special
	 * values will not be returned.
	 *
	 * @param increment The increment. By this value each Double will be greater/less than its
	 *        successor.
	 * @throws IllegalArgumentException If the increment is zero or less than zero.
	 */
	public DoubleIncrementGenerator(double increment) {
		this(0.0, false, increment, Double.MAX_VALUE, false);
	}

	/**
	 * Construct the Generator with customized settings for the starting value and the direction.
	 * The increment is 1.0. The maximum value is Double.MAX_VALUE. Special values will not be
	 * returned.
	 *
	 * @param startAt The first Double returned by the generator.
	 * @param countDown If set to true, the value will be decreased with each Double provided.
	 */
	public DoubleIncrementGenerator(double startAt, boolean countDown) {
		this(startAt, countDown, 1.0, Double.MAX_VALUE, false);
	}

	/**
	 * Construct the Generator with customized settings for the starting value and
	 * the increment. The successors always have an increased value. The maximum
	 * value is Double.MAX_VALUE.  Special values will not be returned.
	 *
	 * @param startAt The first Double returned by the generator.
	 * @param increment The increment. By this value each Double will be greater/less than its successor.
	 * @throws IllegalArgumentException If the increment is zero or less than zero.
	 */
	public DoubleIncrementGenerator(double startAt, double increment) {
		this(startAt, false, increment, Double.MAX_VALUE, false);
	}

	/**
	 * Construct the Generator with customized settings for the starting value, the increment and
	 * the maximum value. The successors always have an increased value. Special values will not be
	 * returned.
	 *
	 * @param startAt The first Double returned by the generator.
	 * @param increment The increment. By this value each Double will be greater/less than its
	 *        successor.
	 * @param maximum The maximum value.
	 * @throws IllegalArgumentException If the increment is zero or less than zero.
	 */
	public DoubleIncrementGenerator(double startAt, double increment, double maximum) {
		this(startAt, false, increment, maximum, false);
	}

	/**
	 * Construct the Generator with customized settings for the starting value, the direction, the
	 * increment and for special values.
	 *
	 * @param startAt The first Double returned by the generator.
	 * @param countDown If set to true, the value will be decreased with each Double provided.
	 * @param increment The increment. By this value each Double will be greater/less than its
	 *        successor.
	 * @param maximum The maximum value.
	 * @param specialValues Toggles whether the special values Double.NaN, Double.POSITIVE_INFINITY
	 *        and DOUBLE.NEGATIVE_INFINITY are returned.
	 * @throws IllegalArgumentException If the increment is zero or less than zero or if the maximum
	 *         value is invalid.
	 */
	public DoubleIncrementGenerator(double startAt, boolean countDown, double increment,
			double maximum, boolean specialValues) {
		// Check for problems.
		if (increment == 0.0)
			throw new IllegalArgumentException("The increment must not be zero.");
		if (increment < 0.0)
			throw new IllegalArgumentException(
					"The increment must not be less than zero. "
					+ "Use the appropriate constructor to configure a decreasing generator.");
		// Set the parameters.
		this.startAt = startAt;
		this.countDown = countDown;
		this.increment = increment;
		this.maximum = maximum;
		this.specialValues = specialValues;
		this.specialValuesReturned = 0;
		if (countDown) {
			this.lastValue = startAt + 1.0;
			if (maximum >= startAt)
				throw new IllegalArgumentException("The maximum must be less than the first value.");
		} else {
			this.lastValue = startAt - 1.0;
			if (maximum <= startAt)
				throw new IllegalArgumentException("The maximum must be greater than the first value.");
		}
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
		if (this.countDown) {
			if (this.lastValue == this.maximum || this.specialValuesReturned > 0) {
				return hasMoreSpecialValues();
			}
		} else {
			if (this.lastValue == this.maximum || this.specialValuesReturned > 0) {
				return hasMoreSpecialValues();
			}
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
		if (this.countDown) {
			if (this.lastValue < this.maximum || this.specialValuesReturned > 0) {
				this.lastValue = getNextSpecialValue();
			} else {
				this.lastValue -= this.increment;
				if (this.lastValue == Double.NEGATIVE_INFINITY) {
					this.lastValue = this.maximum;
				}
			}
		} else {
			if (this.lastValue > this.maximum || this.specialValuesReturned > 0) {
				this.lastValue = getNextSpecialValue();
			} else {
				this.lastValue += this.increment;
				if (this.lastValue == Double.POSITIVE_INFINITY) {
					this.lastValue = this.maximum;
				}
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
		if (this.countDown) {
			this.lastValue = this.startAt + 1.0;
		} else {
			this.lastValue = this.startAt - 1.0;
		}
		this.specialValuesReturned = 0;
	}

	/**
	 * Get the name of the Generator.
	 * @return "Double increment generator".
	 */
	public String getName() {
		return "Double increment generator";
	}

	/**
	 * Get a description of what the generator does and how it works.
	 * @return A description of the generator.
	 */
	public String getDescription() {
		return  "Provides a sequence of DoubleConstant objects at which each value "
		 	+ "is succeeded by one that is increased respectively decreased a specified value (1.0 by default). "
		 	+ "The starting value and the increment are customizable.\n\n"
		 	+ "Examples sequences:\n"
		 	+ "0.0,  1.0,  2.0,  3.0,..., Double.MAX_VALUE\n"
		 	+ "0.5, -0.5, -1.5, -2.5,..., Double.MIN_VALUE\n"
		 	+ "5.7,  6.8,  7.9,  10,..., Double.MAX_VALUE";
	}

}
