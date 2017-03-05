package de.wwu.muggl.symbolic.flow.coverage;

import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;

/**
 * The TrailElement is used for checking the coverage of control graph. Covered edges
 * have to be unset when backtracking.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-28
 */
public class CGCoverageTrailElement implements TrailElement {
	// Fields.
	private CGCoverage cGCoverage;
	private long number;
	private int lastPc;
	private int pc;

	/**
	 * Initialize with the ControlGraph object and the definition index to unset.
	 *
	 * @param cGCoverage The ControlGraphCoverage instance to unset the definition at.
	 * @param number The number of the control graph trail element.
	 * @param lastPc The pc executed last.
	 * @param pc The currently executed pc.
	 */
	public CGCoverageTrailElement(CGCoverage cGCoverage,  long number, int lastPc, int pc) {
		this.cGCoverage = cGCoverage;
		this.number = number;
		this.lastPc = lastPc;
		this.pc = pc;
	}

	/**
	 * Called when restoring a former state in the symbolic execution. This
	 * triggers the reverting of the coverage in the ControlGraph instance.
	 */
	public void restore() {
		this.cGCoverage.revertCoverage(this.number, this.lastPc, this.pc);
	}

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Trail element that resets the control flow coverage between " + this.pc + " and "
				+ this.lastPc + ".";
	}

}
