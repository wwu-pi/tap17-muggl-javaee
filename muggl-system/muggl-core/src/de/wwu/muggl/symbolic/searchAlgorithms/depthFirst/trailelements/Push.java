package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

/**
 * This TrailElement takes an object as its initialization argument. It indicates that
 * this object has to be pushed onto the current operand stack.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-08-29
 */
public class Push implements TrailElement {
	/**
	 * The value that was pushed.
	 */
	protected Object object;

	/**
	 * Initialize with the object.
	 * @param object The object that has to be pushed onto the operand stack.
	 */
	public Push(Object object) {
		this.object = object;
	}

	/**
	 * Getter for the object.
	 * @return The object that has to be pushed onto the operand stack.
	 */
	public Object getObject() {
		return this.object;
	}

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String objectInfo;
		if (this.object == null) {
			objectInfo = "The object is a null reference.";
		} else {
			objectInfo = "The object is of type " + this.object.getClass().getName()
					+ " and its toString() method returns: " + this.object.toString();
		}
		return "Trail element that pushes an object onto the operand stack of the currently executed frame. " + objectInfo;
	}

}
