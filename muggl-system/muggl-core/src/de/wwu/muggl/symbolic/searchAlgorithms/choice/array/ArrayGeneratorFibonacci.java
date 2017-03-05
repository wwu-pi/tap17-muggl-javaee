package de.wwu.muggl.symbolic.searchAlgorithms.choice.array;

/**
 * This symbolic array generator generates arrays with a length of fib(x),
 * where x is the number of calls of provideNextArray(). The first call is
 * counted as one (in contradiction to the behavior of most other generators!).
 * The maximum number of calls is 46, which results in a value of 1.836.311.903.
 * Any further call will have java.lang.Integer.MAX_VALUE returned, as the
 * fibonacci number of 47 is not representable by an integer. Any results will
 * be added up with the starting length, so the maximum value might be reached
 * after a lower number of calls.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-08-28
 */
public class ArrayGeneratorFibonacci implements ArrayGenerator {
	// Fields.
	private long startingLength;
	private long numberOfCalls;

	/**
	 * Sets up the generator.
	 * @param startingLength The length the first generated array has.
	 */
	public ArrayGeneratorFibonacci(int startingLength) {
		this.startingLength = startingLength;
		this.numberOfCalls = 1;
	}

	/**
	 * Provide the next array's length following the algorithm's generation rules. It will
	 * be determined by the algorithm.
	 * @return The next length for the array.
	 */
	public int provideNextArraysLength() {
		// Determine the number of elements as a long.
		int numberOfElements;
		if (this.startingLength + this.numberOfCalls < 47) {
			long numberOfElementsLong =  fibonacci(this.startingLength + this.numberOfCalls);
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

	/**
	 * Get the xth fibonacci number. This method works recursively.
	 * 
	 * @param x The fibonacci number to calculate.
	 * @return The calculated fibonacci number
	 */
	private long fibonacci(long x) {
		if (x == 0) return 0;
		if (x == 1) return 1;
		return fibonacci(x - 1) + fibonacci(x - 2);
	}

}
