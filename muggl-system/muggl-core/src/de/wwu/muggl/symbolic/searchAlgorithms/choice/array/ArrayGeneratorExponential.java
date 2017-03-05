package de.wwu.muggl.symbolic.searchAlgorithms.choice.array;

/**
 * This symbolic array generator generates arrays with a length of 2 ^ x,
 * where x is the number of calls of provideNextArray(). The first call is
 * counted as zero. Hence, the length of the provided array is the following
 * sequence: 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, ...
 * The maximum number of calls is 32. The value returned will be
 * java.lang.Integer.MAX_VALUE then; any further call will have that value
 * returned. Any results will be added up with the starting length, so the
 * maximum value might be reached after a lower number of calls.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-10
 */
public class ArrayGeneratorExponential implements ArrayGenerator {
	// Fields.
	private long startingLength;
	private long numberOfCalls;

	/**
	 * Sets up the generator.
	 * @param startingLength The length the first generated array has.
	 */
	public ArrayGeneratorExponential(int startingLength) {
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
		if (this.numberOfCalls < 31) {
			long numberOfElementsLong = this.startingLength + (long) Math.pow(2, this.numberOfCalls);
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
