package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.VirtualMachine;

/**
 * The fast execution window is used in step by step execution mode. It is initialized with a number of
 * instructions that will not be executed step by step. In fact, they will be executed as fast as possible.
 * The execution speed should be similar to the direct execution. Once the number of instructions has been
 * executed or execution in the virtual machine has finished, fast executed will stop.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class FastExecutionWindow implements ShellWindow {
	// Window related fields.
	Shell shell = null;
	private Display display = null;
	private boolean isClosing = false;
	Label informationLabel;
	Button button;
	Text instructionsText;
	ProgressBar progressBar;
	Text percentText;

	// Other fields.
	Application application;
	long numberOfInstructions;
	double divisor;

	/**
	 * Build and show the Windows, dispose it after is is not longer needed.
	 *
	 * @param parentShell The parent windows' Shell.
	 * @param application The currently active Application.
	 * @param numberOfInstructions The number of instructions to execute.
	 *
	 * @throws IllegalArgumentException If the number of instructions is less than one.
	 * @throws IllegalStateException If execution of the Application is finished.
	 * @throws NullPointerException If the Application reference is null.
	 */
	public FastExecutionWindow(Shell parentShell, Application application, long numberOfInstructions) {
		// Application must not be null.
		if (application == null)
			throw new NullPointerException("Application must not be null.");

		// Execution of the Application must not be finished.
		if (application.getExecutionFinished())
			throw new IllegalStateException("Execution of the Application must not be finished");

		// A positive number of instructions is needed.
		if (numberOfInstructions <= 0)
			throw new IllegalArgumentException("The number of instrction must be greater than zero.");

		// Set the fields.
		this.application = application;
		this.numberOfInstructions = numberOfInstructions;
		this.divisor = 1.0;

		// Set up the window.
		try {
			this.display = Display.getDefault();
			createShell(parentShell);
			this.shell.open();
			startFastExecution();

			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "fast execution", parentShell);
		} finally {
			doExit();
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 *
	 * @param parentShell The parent windows' Shell.
	 */
	private void createShell(Shell parentShell) {
		// Set up the sell.
		this.shell = new Shell(this.display, SWT.BORDER);
		this.shell.setLayout(new GridLayout(3, false));
		this.shell.setText("Fast Execution...");

		// Check if the number of instructions is greater than an int value. Find a divisor in that case.
		if (this.numberOfInstructions > Integer.MAX_VALUE) {
			this.divisor = (double) this.numberOfInstructions / (double) Integer.MAX_VALUE;
		}

		// Components
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;

		this.informationLabel = new Label(this.shell, SWT.NONE);
		this.informationLabel.setText("Executing " + this.numberOfInstructions + " instructions...");
		this.informationLabel.setLayoutData(gridData);

		this.button = new Button(this.shell, SWT.PUSH);
		this.button.setText("&Stop");
		this.shell.setDefaultButton(this.button);

		this.instructionsText = new Text(this.shell, SWT.BORDER | SWT.RIGHT);
		this.instructionsText.setBackground(this.shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		this.instructionsText.setText("     " + String.valueOf(this.numberOfInstructions));
		this.instructionsText.setEditable(false);

		this.progressBar = new ProgressBar(this.shell, SWT.HORIZONTAL | SWT.SMOOTH);
		this.progressBar.setMinimum(0);
		this.progressBar.setMaximum((int) (this.numberOfInstructions / this.divisor));

		this.percentText = new Text(this.shell, SWT.BORDER | SWT.RIGHT);
		this.percentText.setBackground(this.shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		this.percentText.setText("    " + "00%");
		this.percentText.setEditable(false);

		// Listener.
		/**
		 * Close the window.
		 */
		this.button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				doExit();
			}
		});

		// Escape Listener.
		this.shell.addKeyListener(new EscKeyListener(this));
		this.informationLabel.addKeyListener(new EscKeyListener(this));
		this.button.addKeyListener(new EscKeyListener(this));
		this.instructionsText.addKeyListener(new EscKeyListener(this));
		this.progressBar.addKeyListener(new EscKeyListener(this));
		this.percentText.addKeyListener(new EscKeyListener(this));

		// Compute the needed size.
		Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 2;
		point.y += 2;
		int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, parentShell);
		this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
	}

	/**
	 * Start the fast execution.
	 */
	private void startFastExecution() {
		// Start the execution.
		try {
			this.application.getVirtualMachine().setRunUntilNoOfInstructionsReached(this.numberOfInstructions);
		} catch (IllegalStateException e) {
			StaticGuiSupport.showMessageBox(
							this.shell,
							"Error",
							"Setting up the fast execution failed. "
							+ "It can only be used in step by step execution mode.",
							SWT.OK | SWT.ICON_ERROR);
			doExit();
		}

		// And now keep refreshing the progress information.
		Thread runner = new Thread() {
			/**
			 * Periodically refresh the window with progress information. Do this in an own thread.
			 *
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				while (true) {
					// Get the number of executed instructions.
					synchronized (FastExecutionWindow.this.application) {
						VirtualMachine vm = FastExecutionWindow.this.application.getVirtualMachine();
						if (vm != null) {
							// Get the number of instructions not yet executed.
							long numberOfInstructions = vm.getRunUntilNoOfInstructionsReached();

							// Refresh the text fields and the progress bar.
							refreshProgressInformation(numberOfInstructions);

							// Exit the loop?
							if (numberOfInstructions == 0) break;
						}
					}

					// Sleep for a while.
					try {
						Thread.sleep(Globals.MAX_SLEEPING_SLICE / 2);
					} catch (InterruptedException e) {
						// Just ignore it.
					}
				}
			}
		};
		runner.start();
	}

	/**
	 * Refresh the progress information. This method works asynchronic to access the GUI thread from
	 * the thread that checks the virtual machine status.
	 *
	 * @param numberOfInstructions The number oft instructions left for fast execution.
	 */
	protected synchronized void refreshProgressInformation(final long numberOfInstructions) {
		// Asynchronous access.
		this.display.syncExec(new Runnable() {
			public void run() {
				synchronized (FastExecutionWindow.this.shell) {
					if (!FastExecutionWindow.this.shell.isDisposed()) {
						// Refresh the labels and the progress bar.
						long maximum = FastExecutionWindow.this.numberOfInstructions;
						String instructionsString = StaticGuiSupport.formatNumericValue(numberOfInstructions);
						FastExecutionWindow.this.instructionsText.setText(instructionsString);
						int selection = (int) ((maximum - numberOfInstructions) / FastExecutionWindow.this.divisor);
						FastExecutionWindow.this.progressBar.setSelection(selection);
						double percent = (double) (maximum - numberOfInstructions) / (double) maximum;
						percent = Math.round(percent * 100);
						FastExecutionWindow.this.percentText.setText(Double.valueOf(percent).intValue() + "%");

						// Finished?
						if (numberOfInstructions == 0) {
							FastExecutionWindow.this.informationLabel.setText("Finished!");
							FastExecutionWindow.this.button.setText("&Exit");
							FastExecutionWindow.this.instructionsText.setText("");
						}
					}
				}
			}
		});
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
		// Stop the execution.
		if (this.application != null) {
			synchronized (this.application) {
				VirtualMachine vm = this.application.getVirtualMachine();
				if (vm != null)
					vm.stopRunUntilNoOfInstructionsReached();
			}
		}

		// Close the window.
		if (!this.isClosing && !this.shell.isDisposed()) {
			this.isClosing = true;
			this.shell.close();
			this.shell.dispose();
			return true;
		}
		return false;
	}

}