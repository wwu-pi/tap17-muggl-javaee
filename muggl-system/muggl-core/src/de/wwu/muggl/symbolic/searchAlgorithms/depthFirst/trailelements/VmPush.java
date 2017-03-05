package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.impl.symbolic.SymbolicFrame;
import de.wwu.muggl.vm.threading.Monitor;

/**
 * This TrailElement extends Push and takes an object as its initialization argument.
 * It indicates that this object has to be popped from the current virtual machine
 * stack. It will also restore some of its states.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public class VmPush extends Push {
	// Fields.
	private boolean restoreStates;
	private int pc;
	private Monitor monitor;

	/**
	 * Initialize with the object.
	 * @param object The object that has to be pushed onto the operand stack.
	 */
	public VmPush(Object object) {
		super(object);
		if (object instanceof SymbolicFrame) {
			SymbolicFrame frame = (SymbolicFrame) object;
			this.pc = frame.getPc();
			this.monitor = frame.getMonitor();
			this.restoreStates = true;
		} else {
			this.restoreStates = false;
		}
	}

	/**
	 * Getter for the information whether states of a frame should be restore, or not.
	 *
	 * @return true to signal states have to be restored, or false to signal not do to so.
	 */
	public boolean restoreStates() {
		return this.restoreStates;
	}

	/**
	 * Getter for the pc to be restored.
	 *
	 * @return The pc to be restored.
	 */
	public int getPc() {
		return this.pc;
	}

	/**
	 * Getter for the monitor to be restored.
	 *
	 * @return The monitor to be restored.
	 */
	public Monitor getMonitor() {
		return this.monitor;
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
		return "Trail element that pushes an object onto the virtual machine stack. " + objectInfo;
	}

}
