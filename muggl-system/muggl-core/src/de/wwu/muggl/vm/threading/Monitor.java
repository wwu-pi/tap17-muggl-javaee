package de.wwu.muggl.vm.threading;

import de.wwu.muggl.vm.exceptions.VmRuntimeException;

/**
 * This class represents a java Monitor, which is used for locking-purposes in multi-thread applications.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Monitor {
	private long timesEntered;

	/**
	 * Basic constructor.
	 */
	public Monitor() { }

	/**
	 * Enter the monitor.
	 */
	public void monitorEnter() {
		// TODO
		if (Math.floor(1.0) == Math.floor(1.0)) { // TODO: has the thread ownership of this monitor?
			this.timesEntered++;
		} else {
			// TODO: gain ownership of this monitor
			this.timesEntered = 1;
		}

	}

	/**
	 * Leave the monitor.
	 * 
	 * @throws VmRuntimeException If the thread trying to exit the monitor is not its owner (it is a
	 *         wrapped IllegalMonitorStateException).
	 */
	@SuppressWarnings("unused") // TODO
	public void monitorExit() throws VmRuntimeException {
		// Decrement the counter.
		this.timesEntered--;

		// If the counter becomes zero, release the monitor.
		if (this.timesEntered == 0) releseMonitor();
	}

	/**
	 * Release the monitor.
	 */
	private void releseMonitor() {

	}

}
