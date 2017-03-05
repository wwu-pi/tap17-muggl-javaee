package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.Frame;

/**
 * This TrailElement indicates that the topmost element of the operand stack of the specified frame
 * has to be popped.
 *
 * @author Tim Majchrzak
 * @version 1.0.0 Last modified: 2008-08-29
 */
public class PopFromFrame implements TrailElement {
	private Frame frame;

	/**
	 * Construct the trail element.
	 *
	 * @param frame The frame to pop an element from.
	 */
	public PopFromFrame(Frame frame) {
		this.frame = frame;
	}

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Trail element that pops an object from the operand machine stack of the specified frame ("
				+ this.frame.toString() + ").";
	}

	/**
	 * Getter for the frame to pop an element from.
	 *
	 * @return The frame to pop an element from.
	 */
	public Frame getFrame() {
		return this.frame;
	}

}
