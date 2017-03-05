package de.wwu.muggl.ui.gui.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.LogWindow;

/**
 * The composite for the LogWindow. It offers most of its element and the corresponding methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class LogComposite extends Composite {
	// General fields for the window.
	LogWindow parent;
	Shell shell;
	Display display;

	/**
	 * Basic constructor for launching the composite of the LogWindow.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 */
	public LogComposite(LogWindow parent, Shell shell, Display display, int style) {
		// General initialization.
		super(shell, style);
		this.parent = parent;
		this.shell = shell;
		this.display = display;

		// Layout.
		this.setLayout(new FillLayout());

		// Components.
		try {
			Browser logBrowser = new Browser(this, SWT.BORDER);
			logBrowser.setSize(1000, 740);
			logBrowser.setUrl(Globals.getInst().currentLogfile);

			// Listener.
			this.addKeyListener(new EscKeyListener(parent));
			logBrowser.addKeyListener(new EscKeyListener(parent));

			// Finish setting up the composite.
			this.pack();
		} catch (SWTError e) {
			StaticGuiSupport.showMessageBox(shell, "Error",
					"Cannot show the log file. No browser is available.", SWT.OK | SWT.ICON_ERROR);
			parent.doExit();
		}
	}

}
