package de.wwu.muggl.symbolic.flow.coverage;

import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;

/**
 * The TrailElement is used for checking the coverage of def-use chains. Definitions and usages
 * covered after the choice point the execution is backtracked to have to be unset in order to
 * reflect the actual coverage.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-12
 */
public class DUCoverageTrailElement implements TrailElement {
	// Data stored.
	private DUCoverage dUCoverage;
	private long number;
	private int coveredIndex;
	private boolean defOrUse;

	/**
	 * Initialize with the DUCoverage object and the definition index to unset.
	 *
	 * @param dUCoverage The DUCoverage instance to unset the definition at.
	 * @param number The number of this def-use coverage trail element.
	 * @param coveredIndex The definition index to unset.
	 * @param defOrUse Indicates whether the definition or the usage is unset. False means def, true
	 *        means use.
	 */
	public DUCoverageTrailElement(DUCoverage dUCoverage, long number, int coveredIndex, boolean defOrUse) {
		this.dUCoverage = dUCoverage;
		this.number = number;
		this.coveredIndex = coveredIndex;
		this.defOrUse = defOrUse;
	}

	/**
	 * Called when restoring a former state in the symbolic execution. This
	 * triggers the reverting of the coverage in the DUCoverage instance.
	 */
	public void restore() {
		this.dUCoverage.revertCoverage(this.number, this.coveredIndex, this.defOrUse);
	}

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String defOrUse = "definition";
		if (this.defOrUse) defOrUse = "usage";
		return "Trail element that resets the def-use coverage for the " + defOrUse + " at index "
				+ this.coveredIndex + ".";
	}

}
