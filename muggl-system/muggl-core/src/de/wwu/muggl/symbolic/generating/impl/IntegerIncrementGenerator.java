package de.wwu.muggl.symbolic.generating.impl;

import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * This Generator provides a sequence of IntConstant objects at which each value is succeeded by one
 * that is increased respectively decreased by a specified value (1.0 by default). The starting
 * value and the increment are customizable.<br />
 * <br />
 * Examples sequences:
 * <ul>
 * <li>0, 1, 2, 3,..., Integer.MAX_VALUE</li>
 * <li>0, -1, -2, -3,..., Integer.MIN_VALUE</li>
 * <li>5, 6, 7, 8,..., Integer.MAX_VALUE</li>
 * </ul>
 * <br />
 * Last modified: 2010-03-10
 *
 * @author Tim Majchrzak
 * @version 1.0.0
 */
public class IntegerIncrementGenerator implements Generator {
	// Fields.
	private int startAt;
	private boolean countDown;
	private int increment;
	private int maximum;
	private int lastValue;

	/**
	 * Construct the Generator with standard settings. The first Integer will
	 * have a value of 0 and each successor will be increased by 1. The maximum
	 * value is Integer.MAX_VALUE.
	 */
	public IntegerIncrementGenerator() {
		this(0, false, 1, Integer.MAX_VALUE);
	}

	/**
	 * Construct the Generator with customized settings for the increment. The starting
	 * value is 0 and successors always have a increased value. The maximum value is
	 * Integer.MAX_VALUE.
	 *
	 * @param increment The increment. By this value each integer will be greater/less than its successor.
	 * @throws IllegalArgumentException If the increment is zero or less than zero.
	 */
	public IntegerIncrementGenerator(int increment) {
		this(0, false, increment, Integer.MAX_VALUE);
	}

	/**
	 * Construct the Generator with customized settings for the starting value and
	 * the direction. The increment is 1. The maximum value is Integer.MAX_VALUE.
	 *
	 * @param startAt The first Integer returned by the generator.
	 * @param countDown If set to true, the value will be decreased with each Integer provided.
	 */
	public IntegerIncrementGenerator(int startAt, boolean countDown) {
		this(startAt, countDown, 1, Integer.MAX_VALUE);
	}

	/**
	 * Construct the Generator with customized settings for the starting value and
	 * the increment. The successors always have an increased value. The maximum
	 * value is Integer.MAX_VALUE.
	 *
	 * @param startAt The first Integer returned by the generator.
	 * @param increment The increment. By this value each integer will be greater/less than its successor.
	 * @throws IllegalArgumentException If the increment is zero or less than zero.
	 */
	public IntegerIncrementGenerator(int startAt, int increment) {
		this(startAt, false, increment, Integer.MAX_VALUE);
	}

	/**
	 * Construct the Generator with customized settings for the starting value, the
	 * increment and the maximum value. The successors always have an increased value.
	 *
	 * @param startAt The first Integer returned by the generator.
	 * @param increment The increment. By this value each integer will be greater/less than its successor.
	 * @param maximum The maximum value.
	 * @throws IllegalArgumentException If the increment is zero or less than zero.
	 */
	public IntegerIncrementGenerator(int startAt, int increment, int maximum) {
		this(startAt, false, increment, maximum);
	}

	/**
	 * Construct the Generator with customized settings for the starting value, the direction and
	 * the increment.
	 * 
	 * @param startAt The first Integer returned by the generator.
	 * @param countDown If set to true, the value will be decreased with each Integer provided.
	 * @param increment The increment. By this value each integer will be greater/less than its
	 *        successor.
	 * @param maximum The maximum value.
	 * @throws IllegalArgumentException If the increment is zero or less than zero or if the maximum
	 *         value is invalid.
	 */
	public IntegerIncrementGenerator(int startAt, boolean countDown, int increment, int maximum) {
		// Check for problems.
		if (increment == 0)
			throw new IllegalArgumentException("The increment must not be zero.");
		if (increment < 0)
			throw new IllegalArgumentException(
					"The increment must not be less than zero. "
					+ "Use the appropriate constructor to configure a decreasing generator.");
		// Set the parameters.
		this.startAt = startAt;
		this.countDown = countDown;
		this.increment = increment;
		this.maximum = maximum;
		if (countDown) {
			this.lastValue = startAt + 1;
			if (maximum >= startAt)
				throw new IllegalArgumentException("The maximum must be less than the first value.");
		} else {
			this.lastValue = startAt - 1;
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
	 * Check if this generator can provide another object. It does if counting up
	 * and the last returned integer was not the maximum specified.
	 *
	 * @return true, if this generator can provide another object, false otherwise.
	 */
	public boolean hasAnotherObject()  {
		if (this.countDown) {
			if (this.lastValue < this.maximum)
				return false;
		} else {
			if (this.lastValue > this.maximum)
				return false;
		}
		return true;
	}

	/**
	 * Provide the next Integer.
	 *
	 * @return The next Integer.
	 * @throws IllegalStateException If no more elements are available. This cannot happen if
	 *         checking the availability with hasAnotherElement() first and not having more than one
	 *         thread access this generator.
	 */
	public IntConstant provideObject() {
		if (!hasAnotherObject())
			throw new IllegalStateException("There are no more elements available.");
		if (this.countDown) {
			this.lastValue -= this.increment;
		} else {
			this.lastValue += this.increment;
		}
		return IntConstant.getInstance(this.lastValue);
	}

	/**
	 * Check if this generator supplies java objects that are no wrappers for primitive types. It
	 * does not do so but supplied Integer objects that wrap primitive int values.
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
			this.lastValue = this.startAt + 1;
		} else {
			this.lastValue = this.startAt - 1;
		}
	}

	/**
	 * Get the name of the Generator.
	 * @return "Integer increment generator".
	 */
	public String getName() {
		return "Integer increment generator";
	}

	/**
	 * Get a description of what the generator does and how it works.
	 * @return A description of the generator.
	 */
	public String getDescription() {
		return  "Provides a sequence of IntConstant objects at which each value "
		 	+ "is succeeded by one that is increased respectively decreased by a specified value (1 by default). "
		 	+ "The starting value and the increment are customizable.\n\n"
		 	+ "Examples sequences:\n"
		 	+ "0,  1,  2,  3,..., Integer.MAX_VALUE\n"
		 	+ "0, -1, -2, -3,..., Integer.MIN_VALUE\n"
		 	+ "5,  6,  7,  8,..., Integer.MAX_VALUE";
	}

}
