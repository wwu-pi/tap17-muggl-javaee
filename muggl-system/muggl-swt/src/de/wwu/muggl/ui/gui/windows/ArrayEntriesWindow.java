package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.components.ArrayEntriesComposite;
import de.wwu.muggl.ui.gui.support.ArrayModificationHandler;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;

/**
 * The ArrayEntriesWindow is used to show alter the elements of arrays. It utilizes a
 * ArrayModificationHandler for its operations.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class ArrayEntriesWindow implements ShellWindow {
	// Window related fields.
	private ShellWindow parent;
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;

	/**
	 * Build and show the Window, dispose it after is is not longer needed.
	 * @param parent The parent ShellWindow.
	 * @param arrayModificationHandler The ArrayModificationHandler the holds the represented array.
	 * @param myDimension The dimension of a probably multidimensional array this Window represents.
	 * @param dimensionsIndexes Dimension indexes of the higher-level dimensions the array represented might be a part of.
	 */
	public void show(ShellWindow parent, ArrayModificationHandler arrayModificationHandler, int myDimension, int[] dimensionsIndexes) {
		try {
			// Initialize.
			this.parent = parent;
			this.display = Display.getDefault();
			createShell(arrayModificationHandler, myDimension, dimensionsIndexes);
			this.shell.open();

			// Keep the window active as long as it is needed.
			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
		} catch (Throwable t) {
			arrayModificationHandler.doNotReturnThisArray();
			StaticGuiSupport.processGuiError(t, "Array entries", parent.getShell());
		} finally {
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 * @param arrayModificationHandler The ArrayModificationHandler the holds the represented array.
	 * @param myDimension The dimension of a probably multidimensional array this Window represents.
	 * @param dimensionsIndexes Dimension indexes of the higher-level dimensions the array represented might be a part of.
	 */
	private void createShell(ArrayModificationHandler arrayModificationHandler, int myDimension, int[] dimensionsIndexes) {
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Array Entries");
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));

		// Initialize the composite.
		new ArrayEntriesComposite(this, this.shell, this.display, SWT.NONE, arrayModificationHandler, myDimension, dimensionsIndexes);

		// Compute the needed size.
		Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 2;
		point.y += 2;
		int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, this.parent.getShell());
		this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
	}

	/**
	 * Get the Shell of this Window.
	 * @return The Shell of this Window.
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
