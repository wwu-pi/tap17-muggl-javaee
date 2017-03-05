package de.wwu.muggl.symbolic.testCases;

import java.util.Map;

import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This class wraps a TestCaseSolution in order to ease the elimination process when
 * generating the final set test cases. It therefore contains a TestCaseSolution
 * and forwards the methods to get the def-use and control flow coverage mappings.
 * It introduced two additional fields. They are uses to store the number of def-use
 * chains and control graph edges which are covered by the solution wrapped. If
 * another solution was picked to be included in the final set of test cases, this
 * numbers can be decreased as they only have to reflect the number of yet uncovered
 * chains and edges, i.e. those, that are not covered by the already picked solution(s)
 * but are covered by the one wrapped here.<br />
 * <br />
 * Keeping track of these numbers means some initial effort and decreasing them after
 * picking a solution is a rather slow process. However, finding the next suitable
 * solution is sped up greatly. This justifies the minor increase in memory needed
 * for these wrapper objects.<br />
 * <br />
 * Note: this class has a natural ordering that is inconsistent with equals.<br />
 * <br />
 * The class has package visibility only as it is meant to be utilized by the
 * SolutionProcessor.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
class TestCaseSolutionEliminationWrap implements java.lang.Comparable<TestCaseSolutionEliminationWrap> {
	// Fields.
	private TestCaseSolution testCaseSolution;
	private long number;
	private long numberOfCoveredDefUseChains;
	private long numberOfCoveredControlGraphEdges;

	/**
	 * Construct the TestCaseSolutionEliminationWrap with the TestCaseSolution.
	 * @param testCaseSolution The TestCaseSolutio to wrap.
	 * @param number The number of the elimination wrapper as needed for the natural ordering.
	 */
	public TestCaseSolutionEliminationWrap(TestCaseSolution testCaseSolution, long number) {
		this.testCaseSolution = testCaseSolution;
		this.number = number;
	}

	/**
	 * Getter for the wrapped TestCaseSolution.
	 * @return The wrapped TestCaseSolution.
	 */
	public TestCaseSolution getTestCaseSolution() {
		return this.testCaseSolution;
	}

	/**
	 * Private getter for the number of this TestCaseSolutionEliminationWrap.
	 * @return The number assigned to this elimination wrapper.
	 */
	private long getNumber() {
		return this.number;
	}

	/**
	 * Getter for the number of def-use chains covered by the wrapped solution.
	 * @return The number of def-use chains covered by the wrapped solution.
	 */
	public long getNumberOfCoveredDefUseChains() {
		return this.numberOfCoveredDefUseChains;
	}

	/**
	 * Setter for the number of yet uncovered def-use chains covered by the wrapped solution.
	 * @param coveringUncoveredDefUseChains The number of yet uncovered def-use chains covered by the wrapped solution.
	 */
	public void setNumberOfCoveredDefUseChains(long coveringUncoveredDefUseChains) {
		this.numberOfCoveredDefUseChains = coveringUncoveredDefUseChains;
	}

	/**
	 * Decreases the number of yet uncovered def-use chains by the supplied value.
	 * @param value The decrement.
	 */
	public void decreaseNumberOfCoveredDefUseChainsBy(long value) {
		this.numberOfCoveredDefUseChains -= value;
	}

	/**
	 * Getter for the number of control graph edges covered by the wrapped solution.
	 * @return The number of control graph edges covered by the wrapped solution.
	 */
	public long getNumberOfCoveredControlGraphEdges() {
		return this.numberOfCoveredControlGraphEdges;
	}

	/**
	 * Setter for the number of yet uncovered control graph edges covered by the wrapped solution.
	 * @param coveringUncoveredControlGraphEdges The number of yet uncovered control graph edges covered by the wrapped solution.
	 */
	public void setNumberOfCoveredControlGraphEdges(long coveringUncoveredControlGraphEdges) {
		this.numberOfCoveredControlGraphEdges = coveringUncoveredControlGraphEdges;
	}

	/**
	 * Decreases the number of yet uncovered control graph edges by the supplied value.
	 * @param value The decrement.
	 */
	public void decreaseNumberOfCoveredControlGraphEdges(long value) {
		this.numberOfCoveredControlGraphEdges -= value;
	}

	/**
	 * Getter for the def-use coverage mapping. The request is simply forwarded to the
	 * wrapped TestCaseSolution.
	 * @return The mapping of methods and def-use coverage as an array of boolean values indicating which def-use chains have been covered.
	 */
	public boolean[] getDUCoverage() {
		return this.testCaseSolution.getDUCoverage();
	}

	/**
	 * 
	 * Getter for the control-flow coverage mapping. The request is simply forwarded to the wrapped
	 * TestCaseSolution.
	 * 
	 * @return The mapping of methods and control flow coverage as an array of boolean values
	 *         indicating which control graph edges have been covered.
	 */
	public Map<Method, boolean[]> getCFCoverageMap() {
		return this.testCaseSolution.getCFCoverageMap();
	}

	/**
	 * Compare this TestCaseSolutionEliminationWrap to an object. TestCaseSolutionEliminationWrap objects
	 * have a natural ordering set by the number supplied in the constructor. They are just compared by
	 * the number, not by the actual contained solution, as this comparator is only needed to store the
	 * objects within a TreeSet.
	 *
	 * @see java.lang.Comparable
	 *
	 * @param t The compared TestCaseSolutionEliminationWra.
	 * @return -1 if the number of this is smaller than the one of t, 0 if it is equal, and 1 if it is greater.
	 */
	public int compareTo(TestCaseSolutionEliminationWrap t) {
		if (this.number < t.getNumber()) {
			return -1;
		} else if (this.number > t.getNumber()) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 *
	 * @param obj the object to compare the wrapped test case solution elimination instance to.
	 * @return true, if the supplied object is of type TestCaseSolutionEliminationWrap and both the
	 *         number and the wrapped test case solutions are equal; false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestCaseSolutionEliminationWrap) {
			TestCaseSolutionEliminationWrap wrap = (TestCaseSolutionEliminationWrap) obj;
			if (wrap.number == this.number && wrap.testCaseSolution.equals(this.testCaseSolution))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns a hash code value for the object.
	 * 
     * @return  a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) this.number;
	}

	/**
	 * Get a String representation of this TestCaseSolution.
	 * @return A String representation of this object.
	 */
	@Override
	public String toString() {
		String toString = "Wrapping a s" + this.testCaseSolution.toString().substring(1);
		toString += "Internal number:\t\t\t" + this.number + "\n"
			+ "Covered def-use chains:\t\t" + this.numberOfCoveredDefUseChains + "\n"
			+ "Coivered control graph edges:\t" + this.numberOfCoveredControlGraphEdges;
		return toString;
	}

}
