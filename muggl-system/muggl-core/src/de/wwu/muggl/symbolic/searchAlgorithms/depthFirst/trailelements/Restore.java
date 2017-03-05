package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.Frame;

/**
 * This TrailElement is the counterpiece for the <code>xstore</code> and the iinc instructions. It is generated
 * when a local variables is changed. This can only happen when one of the following instructions is
 * executed: astore, dstore, fstore, istore, lstore and iinc. It takes the information which local
 * variable has to be restored, and what its original value was.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-08-29
 */
public class Restore implements TrailElement {
	/**
	 * The index to the changed local variable.
	 */
	protected int		index;
	/**
	 * The original value of the local variable.
	 */
	protected Object	value;

	/**
	 * Initialize with the local variable index and the value to restore.
	 *
	 * @param index An index into the local variables.
	 * @param object The object that has to be restored at the local variable index.
	 */
	public Restore(int index, Object object) {
		this.index = index;
		this.value = object;
	}

	/**
	 * Restore the local variable saved by this class.
	 *
	 * @param frame The frame the store local variable belongs to.
	 */
	public void restore(Frame frame) {
		frame.setLocalVariable(this.index, this.value);
	}

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String valueInfo;
		if (this.value == null) {
			valueInfo = "The value is a null reference.";
		} else {
			valueInfo = "The value is of type " + this.value.getClass().getName()
					+ " and its toString() method returns: " + this.value.toString();
		}
		return "Trail element that restores a local variable in the currently executed frame to its former value. "
				+ "The variable index is " + this.index + "." + valueInfo;
	}
}
