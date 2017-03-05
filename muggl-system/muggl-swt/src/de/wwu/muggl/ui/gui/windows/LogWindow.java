package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.components.LogComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;

/**
 * This window shows the current log file of the application.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class LogWindow implements ShellWindow {
	// Window related fields.
	private Shell parentShell;
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;

	/**
	 * Initialize the log window.
	 *
	 *  @param parentShell The parent windows' Shell.
	 */
	public LogWindow(Shell parentShell) {
		this.parentShell = parentShell;
	}

	/**
	 * Build and show the Windows, dispose it after is is not longer needed.
	 *
	 * @throws IllegalStateException If the parent shell is unusable (null or disposed).
	 */
	public void show() {
		try {
			this.display = Display.getDefault();
			createShell();
			this.shell.open();

			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
		} catch (Throwable t) {
			if (this.parentShell != null && !this.parentShell.isDisposed()) {
				StaticGuiSupport.processGuiError(t, "log", this.parentShell);
			}
		} finally {
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 *
	 * @throws IllegalStateException If the parent shell is unusable (null or disposed).
	 */
	private void createShell() {
		// Lock the parent shell for this operation.
		if (this.parentShell != null) {
			synchronized (this.parentShell) {
				if (this.parentShell.isDisposed()) {
					throw new IllegalStateException("The parent shell is unusable.");
				}

				// Continue building this shell.
				this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
				this.shell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Log...");
				this.shell.setLayout(new FillLayout(SWT.VERTICAL));

				// No need to read it later, so it is not assigned to a variable.
				new LogComposite(this, this.shell, this.display, SWT.NONE);

				// Compute the needed size.
				Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				point.x += 2;
				point.y += 2;
				int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, this.parentShell);
				this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
			}
		} else {
			throw new IllegalStateException("The parent shell is unusable.");
		}
	}

	/**
	 * Getter for the current Shell.
	 * @return The Shell.
	 */
	public Shell getShell() {
		return this.shell;
	}

	/**
	 * Close the current window.
	 * @return true, if the closing was successful, false otherwise.
	 */
	public synchronized boolean doExit() {
		if (!this.isClosing && !this.shell.isDisposed()) {
			this.isClosing = true;
			this.shell.close();
			this.shell.dispose();
			return true;
		}
		return false;
	}

}
