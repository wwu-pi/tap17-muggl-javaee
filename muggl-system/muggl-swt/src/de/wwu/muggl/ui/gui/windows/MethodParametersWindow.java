package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.components.FileSelectionComposite;
import de.wwu.muggl.ui.gui.components.MethodParametersComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This window is used to set the predefined parameters for a method.<br />
 * <br />
 * Usually, a program is started using its main-method. Since this application can
 * be used to invoke single methods, even private ones that would normally not be
 * executed directly, there is a mechanism required to specify the parameters that
 * will be supplied to the method, so it will work correctly. The window will show
 * as many input fields as there are parameters for the selected method, offering
 * to alter the values that will be used for execution. Furthermore, in the symbolic
 * execution mode Generators can be used to generate variable inputs.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class MethodParametersWindow implements ShellWindow {
	// Window related fields.
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;

	/**
	 * Build and show the Windows, dispose it after is is not longer needed.
	 * @param parent The composite this
	 * @param method The Method thats' parameters will be defined in this Window.
	 */
	public MethodParametersWindow(FileSelectionComposite parent, Method method) {
		// Check the number of parameters.
		if (method.getNumberOfArguments() == 0) {
			StaticGuiSupport.showMessageBox(parent.getShell(), "The selected method does not have any parameters.");
			return;
		}

		// Build and display the shell.
		try {
			this.display = Display.getDefault();
			createShell(parent, method);
			this.shell.open();

			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "methods parameters", parent.getShell());
		} finally {
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 * @param parent The Composite which this Window is invoked by.
	 * @param method The Method thats' parameters will be defined in this Window.
	 */
	private void createShell(FileSelectionComposite parent, Method method) {
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(
				Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR
				+ "Method Parameters and Variable Generators for " + method.getFullNameWithParameterTypesAndNames());
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));

		new MethodParametersComposite(parent, this, this.shell, this.display, SWT.NONE, method);

		// Compute the needed size.
		Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 2;
		point.y += 2;
		int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, parent.getShell());
		this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
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

	/**
	 * Getter for the current Shell.
	 * @return The Shell.
	 */
	public Shell getShell() {
		return this.shell;
	}
}
