package de.wwu.muggl.symbolic.searchAlgorithms.choice.array;

/**
 * Interface for classes that implement symbolic array generation algorithms.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface ArrayGenerator {

	/**
	 * Provide the next array's length following the algorithm's generation rules. It will
	 * be determined by the algorithm.
	 * @return The next length for the array.
	 */
	int provideNextArraysLength();

}
