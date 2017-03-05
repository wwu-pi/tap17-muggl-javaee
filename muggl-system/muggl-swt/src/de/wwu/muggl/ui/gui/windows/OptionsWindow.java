package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.components.OptionsComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * This window shows the options for the execution. It should only be
 * opened from the StepByStepExecutionComposite, since it needs an
 * instance of it to get the current settings from and to save the
 * changes to.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class OptionsWindow implements ShellWindow {
	// Window related fields.
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;

	/**
	 * Build and show the Windows, dispose it after is is not longer needed.
	 * @param parentShell The parent windows' Shell.
	 * @param classLoader The system MugglClassLoader.
	 */
	public void show(Shell parentShell, MugglClassLoader classLoader) {
		try {
			this.display = Display.getDefault();
			createShell(parentShell, classLoader);
			this.shell.open();

			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
		} catch (Throwable t) {
			t.printStackTrace();
			StaticGuiSupport.processGuiError(t, "options", parentShell);
		} finally {
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 * @param parentShell The parent windows' Shell.
	 * @param classLoader The system MugglClassLoader.
	 */
	private void createShell(Shell parentShell, MugglClassLoader classLoader) {
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Options");
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		final Image small = new Image(shell.getDisplay(),
		        OptionsWindow.class.getResourceAsStream("/images/tray_small.png"));
		final Image large = new Image(shell.getDisplay(),
				OptionsWindow.class.getResourceAsStream("/images/tray_large.png"));
		this.shell.setImages(new Image[] { small, large });
		
		// No need to read it later, so it is not assigned to a variable.
		new OptionsComposite(this, this.shell, SWT.NONE, classLoader);

		// Compute the needed size.
		Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 2;
		point.y += 2;
		int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, parentShell);
		this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
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
