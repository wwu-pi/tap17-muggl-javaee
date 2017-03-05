package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.ui.gui.components.ExecutionComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The ExecutionWindow replaced the application's main window. It keeps the user informed about
 * the executions approach with some statistics and offers the possibility to pause or halt the
 * execution.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class ExecutionWindow implements ShellWindow {
	// Window related fields.
	private Shell parentShell;
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;
	ExecutionComposite executionComposite;

	/**
	 * Build and show the Windows, dispose it after is is not longer needed.
	 * @param parentShell The parent windows' Shell.
	 * @param classLoader The system MugglClassLoader.
	 * @param classFile The classFile the initial Method belongs to.
	 * @param method The initial Method.
	 */
	public void show(Shell parentShell, MugglClassLoader classLoader, ClassFile classFile, Method method) {
		try {
			this.parentShell = parentShell;
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
			StaticGuiSupport.processGuiError(t, "step by step execution", parentShell);
		} finally {
			// Make the parent shell visible.
			if (!parentShell.isDisposed())
				parentShell.setVisible(true);

			// Make sure execution is aborted. Otherwise the Thread would not be stopped and the memory released after this window is closed.
			if (this.executionComposite != null) this.executionComposite.abortExecution();
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
		this.shell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Execution of " + method.getFullName());
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));

		final Image small = new Image(shell.getDisplay(),
				ExecutionWindow.class.getResourceAsStream("/images/tray_small.png"));
		final Image large = new Image(shell.getDisplay(),
				ExecutionWindow.class.getResourceAsStream("/images/tray_large.png"));
		this.shell.setImages(new Image[] { small, large });
		
		/*
		 * Listen for close events.
		 */
		this.shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
		    	/*
				 * Make sure the execution is aborted right now. Children windows might still be
				 * opened and will inhibit running the finally block of the show-method.Make sure
				 * the execution is aborted right now. Children windows might still be opened and
				 * will
				 */
		    	if (ExecutionWindow.this.executionComposite != null)
		    		ExecutionWindow.this.executionComposite.abortExecution();

		    	// Close the window.
		        doExit();
		      }
		    });

		try {
			// Compute and set the needed size. As there might be a MessageBox displayed, this has to be done now!
			Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			point.x += 2;
			point.y += 2;
			int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, parentShell);
			this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);

			// Show the composite.
			this.executionComposite = new ExecutionComposite(this, this.shell, this.display, SWT.NONE, classLoader, classFile, method);

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
		// Make the parent shell visible.
		if (!this.parentShell.isDisposed()) {
			this.parentShell.setVisible(true);
			this.parentShell.setActive();
		}

		// Exit.
		if (!this.isClosing && !this.shell.isDisposed()) {
			this.isClosing = true;
			this.shell.close();
			this.shell.dispose();
			return true;
		}
		return false;
	}

}
