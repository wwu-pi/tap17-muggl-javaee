package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;
import de.wwu.muggl.ui.gui.components.GeneratorSelectionComposite;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * This window is used to choose variable generators and array elements generators for the
 * parameters of a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class GeneratorSelectionWindow implements ShellWindow {
	// Window related fields.
	private ShellWindow			parent;
	private MugglClassLoader	classLoader;
	private Shell				shell;
	private Display				display;
	private boolean				isClosing;

	/**
	 * Build the Window, dispose it after is is not longer needed.
	 *
	 * @param parent The parent ShellWindow.
	 * @param classLoader The class loader to use.
	 */
	public GeneratorSelectionWindow(ShellWindow parent, MugglClassLoader classLoader) {
		this.parent = parent;
		this.classLoader = classLoader;
		this.isClosing = false;
		try {
			this.display = Display.getDefault();
			createShell();
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "generator selection", parent.getShell());
		}
	}

	/**
	 * Show the window and provide the chosen generator provider when it is closed.
	 *
	 * @param generatorProvider The currently chosen generator provider. May be null.
	 * @return The chosen generator provider, or null.
	 */
	public GeneratorProvider showAndProvideGeneratorProvider(GeneratorProvider generatorProvider) {
		try {
			// Disable the parent shell.
			this.parent.getShell().setEnabled(false);

			// Initialize and show.
			this.shell.setText(this.shell.getText() + "Variable Generator");
			GeneratorSelectionComposite composite = new GeneratorSelectionComposite(this,
					this.shell, this.display, this.classLoader, false);
			computeAndSetSize();
			this.shell.open();
			/*
			 * Set the generator after computing the size. The window's size will be always the same
			 * instead of adjusting to the length of the description provided by the generator and
			 * its generator provider.
			 */
			composite.setGeneratorProvider(generatorProvider);

			// Run.
			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch()) this.display.sleep();
			}

			// Return the generator provider
			return composite.getGeneratorProvider();
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "generator selection", this.parent.getShell());
			return null;
		} finally {
			// Enable the parent shell and activate it.
			this.parent.getShell().setEnabled(true);
			this.parent.getShell().setActive();
			doExit();
		}
	}

	/**
	 * Show the window and provide the chosen array elements generator provider when it is closed.
	 *
	 * @param generatorProvider The currently chosen array elements generator provider. May be null.
	 * @return The chosen array elements generator provider, or null.
	 */
	public ArrayElementsGeneratorProvider showAndProvideGeneratorProvider(
			ArrayElementsGeneratorProvider generatorProvider) {
		try {
			// Disable the parent shell.
			this.parent.getShell().setEnabled(false);

			// Initialize and show.
			this.shell.setText(this.shell.getText() + "Array Elements Generator");
			GeneratorSelectionComposite composite = new GeneratorSelectionComposite(this,
					this.shell, this.display, this.classLoader, true);
			computeAndSetSize();
			this.shell.open();
			/*
			 * Set the generator after computing the size. The window's size will be always the same
			 * instead of adjusting to the length of the description provided by the generator and
			 * its generator provider.
			 */
			composite.setArrayElementsGeneratorProvider(generatorProvider);

			// Run.
			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch()) this.display.sleep();
			}

			// Return the generator provider
			return composite.getArrayElementsGeneratorProvider();
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "array elements generator selection", this.parent
					.getShell());
			return null;
		} finally {
			// Enable the parent shell and activate it.
			this.parent.getShell().setEnabled(true);
			this.parent.getShell().setActive();
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 */
	private void createShell() {
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(Globals.WINDOWS_TITLE
				+ Globals.WINDOWS_TITLE_CONNECTOR + "Select ");
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));
	}

	/**
	 * Compute and set the size of the window.
	 */
	private void computeAndSetSize() {
		Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 2;
		point.y += 2;
		int[] posXY = StaticGuiSupport
				.getCenteredPosition(point.x, point.y, this.parent.getShell());
		this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
	}

	/**
	 * Close the current window.
	 *
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

	/**
	 * Getter for the current Shell.
	 *
	 * @return The Shell.
	 */
	public Shell getShell() {
		return this.shell;
	}
}
