package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface to be implemented by all shell windows used in this GUI.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface ShellWindow {

	/**
	 * Get the Shell of this Window.
	 * @return The Shell of this Window.
	 */
	 Shell getShell();

	/**
	 * Close the current window.
	 * @return true, if the closing was successfull, false otherwise.
	 */
	boolean doExit();

}
