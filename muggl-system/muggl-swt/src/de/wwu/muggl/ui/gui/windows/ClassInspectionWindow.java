package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.components.ClassInspectionComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * The ClassInspectionWindow shows a class in a tree view. All structures of it are read into a tree, were
 * elements that are subordinated by others inserted as children of those. This enabled an easy exploration
 * of classes, enabling the users to visit those parts that they are interested in. For example the code of
 * a particular method can be reached with five clicks only, while the display data always keeps being
 * clear.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class ClassInspectionWindow implements ShellWindow {
	// Window related fields.
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;

	/**
	 * Build and show the Windows, dispose it after is is not longer needed.
	 * @param parentShell The parent windows' Shell.
	 * @param className The name of the class to inspect.
	 * @param classFile The ClassFile to inspect.
	 */
	public void show(Shell parentShell, String className, ClassFile classFile) {
		try {
			this.display = Display.getDefault();
			createShell(parentShell, className, classFile);
			this.shell.open();

			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "class inspection", parentShell);
		} finally {
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 * @param parentShell The parent windows' Shell.
	 * @param className The name of the class to inspect.
	 * @param classFile The ClassFile to inspect.
	 */
	private void createShell(Shell parentShell, String className, ClassFile classFile) {
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Class File Inspection");
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));

		// No need to read it later, so it is not assigned to a variable.
		new ClassInspectionComposite(this, this.shell, this.display, SWT.NONE, className, classFile);

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
	 * @return true, if the closing was successfull, false otherwise.
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
