package de.wwu.muggl.symbolic.searchAlgorithms.choice.array;

/**
 * This symbolic array generator generates arrays with a length of 10 ^ x,
 * where x is the number of calls of provideNextArray(). The first call is
 * counted as zero. The maximum number of calls is 9. Any further call will
 * have java.lang.Integer.MAX_VALU returned. Any results will be added up with
 * the starting length, so the maximum value might be reached after a lower
 * number of calls.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-10
 */
public class ArrayGeneratorTenPowerOfX implements ArrayGenerator {
	// Fields.
	private long startingLength;
	private long numberOfCalls;

	/**
	 * Sets up the generator.
	 * @param startingLength The length the first generated array has.
	 */
	public ArrayGeneratorTenPowerOfX(int startingLength) {
		this.startingLength = startingLength;
		this.numberOfCalls = 0;
	}

	/**
	 * Provide the next array's length following the algorithm's generation rules. It will
	 * be determined by the algorithm.
	 * @return The next length for the array.
	 */
	public int provideNextArraysLength() {
		// Determine the number of elements as a long.
		int numberOfElements;
		if (this.numberOfCalls <= 9) {
			long numberOfElementsLong = this.startingLength + (long) Math.pow(10, this.numberOfCalls);
			if (numberOfElementsLong > Integer.MAX_VALUE) {
				// It is beyond the maximum value.
				numberOfElements = Integer.MAX_VALUE;
			} else {
				// It is below the maximum value.
				numberOfElements = (int) numberOfElementsLong;
			}
		} else {
			numberOfElements = Integer.MAX_VALUE;
		}
		// Count up the number of calls.
		this.numberOfCalls++;
		// Generate and return the array.
		return numberOfElements;
	}

}
