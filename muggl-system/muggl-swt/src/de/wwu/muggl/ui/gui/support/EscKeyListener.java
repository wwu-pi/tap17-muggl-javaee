package de.wwu.muggl.ui.gui.support;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

import de.wwu.muggl.ui.gui.windows.ShellWindow;

/**
 * Standardized listener that is to be added to all widgets of a window that can be closed by
 * pressing ESC. It will simply make the widgets invoke this.shell.doExit(); if ESC is pressed.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class EscKeyListener implements KeyListener {
		private ShellWindow shell;

		/**
		 * The quite simple constructor, just setting the Shell.
		 * @param shell The current Shell.
		 */
		public EscKeyListener(ShellWindow shell) {
			this.shell = shell;
		}

		/**
		 * Unimplemented method.
		 *
		 * @param event An KeyEvent.
		 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
    	public void keyPressed(KeyEvent event) { }

    	/**
    	 * Invoke the method to close the window if ESC is pressed.
    	 * @param event The KeyEvent.
    	 */
    	public void keyReleased(KeyEvent event) {
    		if (event.keyCode == SWT.ESC) this.shell.doExit();
    	}

}
