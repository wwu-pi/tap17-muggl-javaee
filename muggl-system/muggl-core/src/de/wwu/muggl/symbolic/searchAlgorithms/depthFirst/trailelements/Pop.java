package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

/**
 * This TrailElement indicates that the topmost element of the current operand stack has
 * to be popped.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-08-29
 */
public class Pop implements TrailElement {

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Trail element that pops an object from the operand machine stack of the currently executed frame.";
	}

}
