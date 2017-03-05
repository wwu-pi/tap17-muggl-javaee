package de.wwu.muggl.symbolic.searchAlgorithms.choice.array;

/**
 * This symbolic array generator generates arrays with a length of 1 + x * stepSize,
 * where x is the number of calls of provideNextArray(). The first call is
 * counted as zero. The maximum number of calls is determined by the stepSize.
 * Once a value of java.lang.Integer.MAX_VALUE is reached, any further call will have
 * that value returned. Any results will be added up with the starting length, so the
 * maximum value might be reached after a lower number of calls.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-10
 */
public class ArrayGeneratorLinear implements ArrayGenerator {
	// Fields.
	private long startingLength;
	private long stepSize;
	private long numberOfCalls;

	/**
	 * Sets up the generator.
	 * @param startingLength The length the first generated array has.
	 * @param stepSize The increament per step.
	 */
	public ArrayGeneratorLinear(int startingLength, int stepSize) {
		this.startingLength = startingLength;
		this.stepSize = stepSize;
		this.numberOfCalls = 0;
	}

	/**
	 * Provide the next array's length following the algorithm's generation rules. It will
	 * be determined by the algorithm.
	 * @return The next length for the array.
	 */
	public int provideNextArraysLength() {
		// Determine the number of elements as a long.
		long numberOfElementsLong = this.startingLength + this.numberOfCalls * this.stepSize;
		int numberOfElements;
		if (numberOfElementsLong > Integer.MAX_VALUE) {
			// It is beyond the maximum value.
			numberOfElements = Integer.MAX_VALUE;
		} else {
			// It is below the maximum value.
			numberOfElements = (int) numberOfElementsLong;
		}
		// Count up the number of calls.
		this.numberOfCalls++;
		// Generate and return the array.
		return numberOfElements;
	}

}
