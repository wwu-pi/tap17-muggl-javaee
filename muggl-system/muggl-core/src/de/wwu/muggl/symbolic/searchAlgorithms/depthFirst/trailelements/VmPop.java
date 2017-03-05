package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

/**
 * This TrailElement extends Pop and indicates that the topmost element of the
 * current virtual machine stack has to be popped.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-08-29
 */
public class VmPop extends Pop {

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Trail element that pops an object from the virtual machine stack.";
	}

}
