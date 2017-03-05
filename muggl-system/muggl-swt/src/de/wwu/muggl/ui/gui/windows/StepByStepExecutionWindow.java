package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.ui.gui.components.StepByStepExecutionComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The StepByStepExecutionWindow allows the user to view the details of the execution
 * of a program. He can execute single steps, a couple of steps or let the execution
 * just process very slowly. While the program is executed, internal details like the
 * local variables, the operand stack, the currently executed method and the virtual
 * machine stack can be observed. This can be used for detailed debugging, or just to
 * understand how a particular program or algorithm works.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-25
 */
public class StepByStepExecutionWindow implements ShellWindow {
	// Window related fields.
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;
	private StepByStepExecutionComposite stepByStepExecutionComposite;

	/**
	 * Build and show the Windows, dispose it after is is not longer needed.
	 * @param parentShell The parent windows' Shell.
	 * @param classLoader The system MugglClassLoader.
	 * @param classFile The classFile the initial Method belongs to.
	 * @param method The initial Method.
	 */
	public void show(Shell parentShell, MugglClassLoader classLoader, ClassFile classFile, Method method) {
		try {
			this.display = Display.getDefault();
			if (createShell(parentShell, classLoader, classFile, method))
			this.shell.open();

			// Now make the parent shell invisible.
			parentShell.setVisible(false);

			// Keep the window alive.
			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "Step by step execution", parentShell);
		} finally {
			//Make the parent shell visible.
			parentShell.setVisible(true);
			// Make sure execution is aborted. Otherwise the Thread would not be stopped and the memory released after this window is closed.
			if (this.stepByStepExecutionComposite != null) this.stepByStepExecutionComposite.abortExecution(false);
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 * @param parentShell The parent windows' Shell.
	 * @param classLoader The system MugglClassLoader.
	 * @param classFile The classFile the initial Method belongs to.
	 * @param method The initial Method.
	 * @return true, if the shell could be setup properly, false otherwise.
	 */
	private boolean createShell(Shell parentShell, MugglClassLoader classLoader, ClassFile classFile, Method method) {
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Step by Step Execution");
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));
		try {
			// Compute and set the needed size. As there might be a MessageBox displayed, this has to be done now!
			Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			point.x += 2;
			point.y += 2;
			int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, parentShell);
			this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);

			// Show the composite.
			this.stepByStepExecutionComposite = new StepByStepExecutionComposite(this, this.shell, this.display, SWT.NONE, classLoader, classFile, method);

			// Compute and set the needed size.
			point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			point.x += 2;
			point.y += 2;
			posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, parentShell);
			this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
			return true;
		} catch (GUIException e) {
			// Only one (user-caused) exception will not be displayed for the user. In all other cases generate a message box to inform him what has happened.
			if (!e.getMessage().contains("Do not show this window!")) {
				StaticGuiSupport.showMessageBox(parentShell, "Error", "The step by step execution window could not be loaded.\n\nThe root cause is:\n" + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			}
			doExit();
			return false;
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
