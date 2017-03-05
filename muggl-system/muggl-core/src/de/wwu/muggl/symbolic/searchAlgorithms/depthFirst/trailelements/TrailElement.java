package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

/**
 * A TrailElement is an element that can be added to the trail. Only objects that implement
 * TrailElement can be processed by the recoverState() method of the SymbolicVirtualMachine.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface TrailElement {

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	String toString();

}
