package de.wwu.muggl.ui.gui.windows;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.components.ClassPathEntriesComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;

/**
 * The ClassPathEntriesWindow shows the currently set class path entries. A class path
 * entry might be a directory or a jar file. The first class path entry is statically and
 * can only be read in this window; any other entry might be deleted, as well as new entries
 * can be added. There can never be a duplicated entry.<br />
 * <br />
 * The class path entries are used when the class loader tries to fetch a new class. It
 * will only find classes that can be either found within the java environment jar files
 * or any of the class path directories / jar files. Hence it is important that a proper
 * class path is set prior to the execution of a method. Basically, the class path should
 * match the class path that was used during the development time. This means, that any
 * utilized projects or libraries have to have own class path entries. The directory
 * structure of the projects class files (usually /bin) has to match the structure of the
 * source file (usually /src) and reflect the packages used. This also applies to other
 * directories on the class path: the proper root of a directory structure has to be
 * selected (this again usually is /bin) since the class loader will treat any directory
 * beneath this as if it was a part of the package name.<br />
 * <br />
 * Example:<br />
 * C:/java_project/bin is added to the class path. The class "myprog.tools.Gui" would now
 * be expected to be at C:/java_project/bin/myprog/tools/Gui.class on the file system.
 * Adding just the project directory (C:/java_project/) would lead to the file being
 * expected to be at C:/java_project/myprog/tools/Gui.class, resulting in a
 * ClassNotFoundException should it actually be accessed.<br />
 * <br />
 * Please note that selecting a class that does not have a /bin directory in its path
 * will only have its own directory added to the class path. Any other directories have
 * to be added manually.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class ClassPathEntriesWindow implements ShellWindow {
	// Window related fields.
	private ShellWindow parent;
	private Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;

	// Widget related fields.
	private List<String> classPathEntries;

	/**
	 * Build and show the Windows, dispose it after is is not longer needed. Return the new
	 * ArrayList of class path entries.
	 * @param parent The parent ShellWindow.
	 * @param classPathEntries The class path entries.
	 * @return An ArrayList of class path entries, or null, if something went wrong.
	 */
	public List<String> show(ShellWindow parent, List<String> classPathEntries) {
		try {
			// Initialize.
			this.parent = parent;
			this.display = Display.getDefault();
			createShell(classPathEntries);
			this.shell.open();

			// Keep the window active as long as it is needed.
			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}

			// Return the class path entries.
			return this.classPathEntries;
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "Class path entries", parent.getShell());
			return null;
		} finally {
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 * @param classPathEntries The class path entries.
	 */
	private void createShell(List<String> classPathEntries) {
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Class Path Entries");
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));

		new ClassPathEntriesComposite(this, this.shell, this.display, SWT.NONE, classPathEntries);

		// Compute the needed size.
		Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 2;
		point.y += 2;
		int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, this.parent.getShell());
		this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
	}

	/**
	 * Setter for the class path entries. This method should be used by
	 * the child composite to set the class path entries so they can be
	 * returned.
	 * @param classPathEntries The class path entries as an ArrayList of String objects.
	 */
	public void setClassPathEntries(List<String> classPathEntries) {
		this.classPathEntries = classPathEntries;
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
	public synchronized  boolean doExit() {
		if (!this.isClosing && !this.shell.isDisposed()) {
			this.isClosing = true;
			this.shell.close();
			this.shell.dispose();
			return true;
		}
		return false;
	}

}
