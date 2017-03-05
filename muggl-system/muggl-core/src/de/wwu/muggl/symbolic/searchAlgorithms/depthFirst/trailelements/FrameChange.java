package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.Frame;

/**
 * This TrailElement indicates that the current Frame has changed and can be used to set the frame
 * previously executed as the current one.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-08-29
 */
public class FrameChange implements TrailElement {
	// The stored frame.
	private Frame	frame;

	/**
	 * Initialize with the frame.
	 *
	 * @param frame The previous frame.
	 */
	public FrameChange(Frame frame) {
		this.frame = frame;
	}

	/**
	 * Getter for the frame.
	 *
	 * @return The previous frame.
	 */
	public Frame getFrame() {
		return this.frame;
	}

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Trail element that changes the currently executed frame back to the stored frame for method "
				+ this.frame.getMethod().getFullNameWithParameterTypesAndNames();
	}

}
