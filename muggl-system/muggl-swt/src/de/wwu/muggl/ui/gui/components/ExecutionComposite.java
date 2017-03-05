package de.wwu.muggl.ui.gui.components;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import de.wwu.muggl.common.TimeSupport;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.ExecutionRunner;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ExecutionWindow;
import de.wwu.muggl.ui.gui.windows.LogWindow;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The composite for the ExecutionWindow. It offers most of its element and the
 * corresponding methods. It heavily utilizes the supplied ExecutionRunner instance.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class ExecutionComposite extends Composite {
	// General fields for the window.
	ExecutionWindow parent;
	Shell shell;
	Display display;
	ExecutionRunner executionRunner;
	boolean isFinished;

	// Fields for the ExecutionRunner
	private MugglClassLoader classLoader;
	private ClassFile classFile;
	private Method method;

	// Constant fields for the composites elements.
	private final FormData progressInformationLabelFormData;
	private final Label progressInformationLabel;
	private final FormData progressInformationTextFormData;
	private final Text progressInformationText;
	private final FormData refreshEveryLabelFormData;
	private final Label refreshEveryLabel;
	private final FormData refreshEveryComboFormData;
	private final Combo refreshEveryCombo;
	private final FormData pauseResumeFormData;
	final Button pauseResumeButton;
	private final FormData abortFormData;
	private final Button abortButton;
	private final FormData showLogFormData;
	private final Button showLogButton;
	final Button runInBackgroundButton;
	final Button openTestCaseFileButton;

	// Fields for a tray item.
	ToolTip toolTip;
	TrayItem trayItem;
	Image image;
	
	// Fields for the path to the generated test case file.
	String testCasePath;

	/**
	 * Set up the composite for the ExecutionWindow window.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 * @param classLoader The system MugglClassLoader
	 * @param classFile The classFile the initial Method belongs to.
	 * @param method The initial Method.
	 * @throws GUIException If the windows has to be closed.
	 */
	public ExecutionComposite(
			ExecutionWindow parent,
			Shell shell,
			Display display,
			int style,
			MugglClassLoader classLoader,
			ClassFile classFile,
			Method method
			) throws GUIException {
		// Basic initialization.
		super(shell, style);
		this.parent = parent;
		this.shell = shell;
		this.display = display;
		this.isFinished = false;
		this.trayItem = null;
		this.image = null;
		this. testCasePath = null;

		// Save the setup information for the execution runner for later.
		this.classLoader = classLoader;
		this.classFile = classFile;
		this.method = method;

		// Initialize the StepByStepExecutionRunner.
		this.executionRunner = new ExecutionRunner(this, this.classLoader, this.classFile, this.method, 500);

		// Layout
		this.setLayout(new FormLayout());

		// Build the composite's elements.
		this.progressInformationLabelFormData = new FormData();
		this.progressInformationLabelFormData.top = new FormAttachment(this, 5, SWT.BOTTOM);
		this.progressInformationLabelFormData.bottom = new FormAttachment(this, 20, SWT.BOTTOM);
		this.progressInformationLabelFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.progressInformationLabelFormData.right = new FormAttachment(this, 500, SWT.RIGHT);

		this.progressInformationLabel = new Label(this, SWT.NONE);
		this.progressInformationLabel.setText("Progress information:");
		this.progressInformationLabel.setLayoutData(this.progressInformationLabelFormData);

		this.progressInformationTextFormData = new FormData();
		this.progressInformationTextFormData.top = new FormAttachment(this.progressInformationLabel, 5, SWT.BOTTOM);
		this.progressInformationTextFormData.bottom = new FormAttachment(this.progressInformationLabel, 600, SWT.BOTTOM);
		this.progressInformationTextFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.progressInformationTextFormData.right = new FormAttachment(this, 600, SWT.RIGHT);

		this.progressInformationText = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		this.progressInformationText.setText("");
		this.progressInformationText.setLayoutData(this.progressInformationTextFormData);

		this.refreshEveryLabelFormData = new FormData();
		this.refreshEveryLabelFormData.top = new FormAttachment(this.progressInformationText, 7, SWT.BOTTOM);
		this.refreshEveryLabelFormData.bottom = new FormAttachment(this.progressInformationText, 20, SWT.BOTTOM);
		this.refreshEveryLabelFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.refreshEveryLabelFormData.right = new FormAttachment(this, 140, SWT.RIGHT);

		this.refreshEveryLabel = new Label(this, SWT.NONE);
		this.refreshEveryLabel.setText("Refesh information every:");
		this.refreshEveryLabel.setLayoutData(this.refreshEveryLabelFormData);

		this.refreshEveryComboFormData = new FormData();
		this.refreshEveryComboFormData.top = new FormAttachment(this.progressInformationText, 5, SWT.BOTTOM);
		this.refreshEveryComboFormData.bottom = new FormAttachment(this.progressInformationText, 20, SWT.BOTTOM);
		this.refreshEveryComboFormData.left = new FormAttachment(this.refreshEveryLabel, 5, SWT.RIGHT);
		this.refreshEveryComboFormData.right = new FormAttachment(this.refreshEveryLabel, 65, SWT.RIGHT);

		this.refreshEveryCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.refreshEveryCombo.setLayoutData(this.refreshEveryComboFormData);
		this.refreshEveryCombo.add("0.05 s");
		this.refreshEveryCombo.add("0.1 s");
		this.refreshEveryCombo.add("0.25 s");
		this.refreshEveryCombo.add("0.5 s");
		this.refreshEveryCombo.add("1 s");
		this.refreshEveryCombo.add("2 s");
		this.refreshEveryCombo.add("5 s");
		this.refreshEveryCombo.add("10 s");
		this.refreshEveryCombo.add("30 s");
		this.refreshEveryCombo.add("1 m");
		this.refreshEveryCombo.add("2 m");
		this.refreshEveryCombo.add("5 m");
		this.refreshEveryCombo.add("15 m");
		this.refreshEveryCombo.add("30 m");
		this.refreshEveryCombo.add("1 h");
		this.refreshEveryCombo.add("2 h");
		this.refreshEveryCombo.add("6 h");
		this.refreshEveryCombo.add("12 h");
		this.refreshEveryCombo.select(3);

		this.pauseResumeFormData = new FormData();
		this.pauseResumeFormData.top = new FormAttachment(this.refreshEveryCombo, 8, SWT.BOTTOM);
		this.pauseResumeFormData.bottom = new FormAttachment(this.refreshEveryCombo, 33, SWT.BOTTOM);
		this.pauseResumeFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.pauseResumeFormData.right = new FormAttachment(this, 85, SWT.RIGHT);

		this.pauseResumeButton = new Button(this, SWT.NONE);
		this.pauseResumeButton.setText("&Pause");
		this.pauseResumeButton.setLayoutData(this.pauseResumeFormData);

		this.abortFormData = new FormData();
		this.abortFormData.top = new FormAttachment(this.refreshEveryCombo, 8, SWT.BOTTOM);
		this.abortFormData.bottom = new FormAttachment(this.refreshEveryCombo, 33, SWT.BOTTOM);
		this.abortFormData.left = new FormAttachment(this.pauseResumeButton, 5, SWT.RIGHT);
		this.abortFormData.right = new FormAttachment(this.pauseResumeButton, 85, SWT.RIGHT);

		this.abortButton = new Button(this, SWT.NONE);
		this.abortButton.setText("&Abort");
		this.abortButton.setLayoutData(this.abortFormData);

		this.showLogFormData = new FormData();
		this.showLogFormData.top = new FormAttachment(this.refreshEveryCombo, 8, SWT.BOTTOM);
		this.showLogFormData.bottom = new FormAttachment(this.refreshEveryCombo, 33, SWT.BOTTOM);
		this.showLogFormData.left = new FormAttachment(this.abortButton, 5, SWT.RIGHT);
		this.showLogFormData.right = new FormAttachment(this.abortButton, 85, SWT.RIGHT);

		this.showLogButton = new Button(this, SWT.NONE);
		this.showLogButton.setText("&Show Log");
		this.showLogButton.setLayoutData(this.showLogFormData);

		FormData runInBackgroundFormData = new FormData();
		runInBackgroundFormData.top = new FormAttachment(this.refreshEveryCombo, 8, SWT.BOTTOM);
		runInBackgroundFormData.bottom = new FormAttachment(this.refreshEveryCombo, 33, SWT.BOTTOM);
		runInBackgroundFormData.left = new FormAttachment(this.showLogButton, 5, SWT.RIGHT);
		runInBackgroundFormData.right = new FormAttachment(this.showLogButton, 125, SWT.RIGHT);

		this.runInBackgroundButton = new Button(this, SWT.NONE);
		this.runInBackgroundButton.setText("&Run in background");
		this.runInBackgroundButton.setLayoutData(runInBackgroundFormData);
		
		FormData openTestCaseFileFormData = new FormData();
		openTestCaseFileFormData.top = new FormAttachment(this.refreshEveryCombo, 8, SWT.BOTTOM);
		openTestCaseFileFormData.bottom = new FormAttachment(this.refreshEveryCombo, 33, SWT.BOTTOM);
		openTestCaseFileFormData.left = new FormAttachment(this.runInBackgroundButton, 5, SWT.RIGHT);
		openTestCaseFileFormData.right = new FormAttachment(this.runInBackgroundButton, 125, SWT.RIGHT);

		this.openTestCaseFileButton = new Button(this, SWT.NONE);
		this.openTestCaseFileButton.setVisible(false);
		this.openTestCaseFileButton.setEnabled(false);
		this.openTestCaseFileButton.setText("&Open test case file");
		this.openTestCaseFileButton.setLayoutData(openTestCaseFileFormData);

		// Set up the listener for the combo and the buttons.
		/*
		 * Update the ExecutionRunner with the new refresh information.
		 */
		this.refreshEveryCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String executeEveryString = ((Combo) e.widget).getText();
				String dimension = executeEveryString.substring(executeEveryString.length() - 1);
				String time = executeEveryString.substring(0, executeEveryString.length() - 2);
				int timeInt = (int) (Double.parseDouble(time) * TimeSupport.MILLIS_SECOND);
				if (dimension.equals("m")) timeInt *= TimeSupport.SECONDS_MINUTE;
				else if (dimension.equals("h")) timeInt *= TimeSupport.SECONDS_HOUR;
				getExecutionRunner().setRefreshEvery(timeInt);
			}
		});

		/*
		 * Edit the selected entry of the operand stack.
		 */
		this.pauseResumeButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (getExecutionRunner().isPaused()) {
	    			getExecutionRunner().resumeExecution();
	    			ExecutionComposite.this.pauseResumeButton.setText("&Pause");
	    			ExecutionComposite.this.runInBackgroundButton.setEnabled(true);
	    		} else {
	    			getExecutionRunner().pauseExecution();
	    			ExecutionComposite.this.pauseResumeButton.setText("&Resume");
	    			ExecutionComposite.this.runInBackgroundButton.setEnabled(false);
	    		}
	    	}
	    });

		/*
		 * Abort the execution.
		 */
		this.abortButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (isFinished()) {
	    			getParentWindow().doExit();
	    		} else {
	    			getExecutionRunner().abortExecution();
	    		}
	    	}
	    });

		/*
		 * Show log.
		 */
		this.showLogButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		LogWindow logWindow = new LogWindow(ExecutionComposite.this.shell);
	    		logWindow.show();
	    	}
	    });

		/*
		 * Minimize to system tray.
		 */
		this.runInBackgroundButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		final Tray tray = ExecutionComposite.this.display.getSystemTray();
	    		if (tray == null) {
	    			StaticGuiSupport.showMessageBox(
									ExecutionComposite.this.shell,
									"Warning",
									"Minimizing to the system tray not possible. "
									+ "The system tray might not be available on this platform.",
									SWT.OK | SWT.ICON_WARNING);
	    		} else {
	    			// Show the tray item.
					ToolTip toolTip = new ToolTip(ExecutionComposite.this.shell, SWT.BALLOON | SWT.ICON_INFORMATION);
					toolTip.setMessage("The execution has finished!");
					toolTip.setText("Finished.");
	    			final Image image = ExecutionComposite.this.display.getSystemImage(SWT.ICON_WORKING);
	    			final TrayItem item = new TrayItem(tray, SWT.NONE);
	    			item.setImage(image);
	    			item.setToolTipText(Globals.WINDOWS_TITLE
							+ Globals.WINDOWS_TITLE_CONNECTOR + "Executing...");
	    			item.setToolTip(toolTip);

	    			/*
	    			 * Listener for left clicks...
	    			 */
	    			item.addListener(SWT.Selection, new Listener() {
	    				public void handleEvent(Event event) {
	    					ExecutionComposite.this.trayItem = null;
	    					ExecutionComposite.this.shell.setVisible(true);
	    					item.dispose();
	    					// ExecutionComposite.this.image.dispose(); Enable if using non-system images.
	    					ExecutionComposite.this.image = null;
	    				}
	    			});
	    			/*
	    			 * Listener for right clicks...
	    			 */
	    			item.addListener(SWT.MenuDetect, new Listener() {
	    				public void handleEvent(Event event) {
	    					ExecutionComposite.this.trayItem = null;
	    					ExecutionComposite.this.shell.setVisible(true);
	    					item.dispose();
	    					// ExecutionComposite.this.image.dispose(); Enable if using non-system images.
	    					ExecutionComposite.this.image = null;
	    				}
	    			});
	    			ExecutionComposite.this.toolTip = toolTip;
	    			ExecutionComposite.this.trayItem = item;
	    			ExecutionComposite.this.image = image;

	    			// Make this window invisible.
	    			ExecutionComposite.this.shell.setVisible(false);
	    		}
	    	}
	    });
		
		/*
		 * Open the test case file in the system's editor.
		 */
		this.openTestCaseFileButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
		    	Desktop desktop = Desktop.getDesktop(); 	
				try {
					String path = ExecutionComposite.this.testCasePath;
					if(path.startsWith("./")) {
						path = path.substring(2);
					}
		    		desktop.open(new File(path));
				} catch (IllegalArgumentException e) {
					StaticGuiSupport.showMessageBox(ExecutionComposite.this.shell, "Warning",
							"The test case file does not seem to exist any longer.", SWT.OK
									| SWT.ICON_WARNING);
				} catch (IOException e) {
					StaticGuiSupport.showMessageBox(ExecutionComposite.this.shell, "Error",
							"Launching the system editor failed.", SWT.OK | SWT.ICON_ERROR);
				} catch (UnsupportedOperationException e) {
					StaticGuiSupport.showMessageBox(ExecutionComposite.this.shell, "Error",
							"Launching the system editor is not supported on this system.", SWT.OK
									| SWT.ICON_ERROR);
				}
	    		
	    		
	    	}
	    });

		// Esc Listener
	    this.addKeyListener(new EscKeyListener(this.parent));
	    this.progressInformationLabel.addKeyListener(new EscKeyListener(this.parent));
	    this.progressInformationText.addKeyListener(new EscKeyListener(this.parent));
	    this.refreshEveryLabel.addKeyListener(new EscKeyListener(this.parent));
	    this.refreshEveryCombo.addKeyListener(new EscKeyListener(this.parent));
	    this.pauseResumeButton.addKeyListener(new EscKeyListener(this.parent));
	    this.abortButton.addKeyListener(new EscKeyListener(this.parent));
	    this.showLogButton.addKeyListener(new EscKeyListener(this.parent));
	    this.runInBackgroundButton.addKeyListener(new EscKeyListener(this.parent));
	    this.openTestCaseFileButton.addKeyListener(new EscKeyListener(this.parent));
	    
		// Finish setting up the composite.
		this.pack();

		// Set up the execution.
		this.executionRunner.start();
		this.executionRunner.startExecution();

	    // If the execution runner has not been initialized at this point, close the window.
	    if (this.executionRunner == null)
	    	throw new GUIException("Do not show this window!");
	}

	/**
	 * Getter for this composite.
	 * @return This StepByStepExecutionComposite.
	 */
	public ExecutionComposite getThis() {
		return this;
	}

	/**
	 * Getter for the parent window.
	 * @return The parent instance of StepByStepExecutionWindow.
	 */
	protected ExecutionWindow getParentWindow() {
		return this.parent;
	}

	/**
	 * Wrapper method for the ExecutionRunner: Disable the pause button.
	 */
	public void disablePauseButtonByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
		    public void run() {
		    	ExecutionComposite.this.pauseResumeButton.setEnabled(false);
		    }
		  });
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: abort the execution.
	 */
	public synchronized void abortExecutionByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
		    public void run() {
		    	abortExecution();
		    }
		 });
	}

	/**
	 * If an Application has been initialized, invoke the method
	 * to abort the execution.
	 */
	public void abortExecution() {
		if (this.executionRunner != null) {
			this.executionRunner.abortExecution();
			this.executionRunner = null;
		}
		executionFinished();
	}

	/**
	 * Wrapper method for the ExecutionRunner: Set that the execution is finished.
	 */
	public void executionFinishedByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
		    public void run() {
		    	executionFinished();
		    }
		  });
	}

	/**
	 * Set that the execution is finished. This will disable the pause / resume Button,
	 * make the abort Button act as an exit button and set the field isFinished to true.
	 * If the window is minimized, the try item's tooltip will be updated.
	 */
	protected void executionFinished() {
		this.isFinished = true;
		if (!this.isDisposed()) {
			this.pauseResumeButton.setEnabled(false);
			this.abortButton.setText("&Exit");
			this.runInBackgroundButton.setEnabled(false);
			if (this.testCasePath != null) {
				this.openTestCaseFileButton.setEnabled(true);
				this.openTestCaseFileButton.setVisible(true);
			}
		}

		// Is there a tray item present?
		if (this.trayItem != null) {
			synchronized (this.trayItem) {
				// Update the image.
				final Image image = this.display.getSystemImage(SWT.ICON_INFORMATION);
				this.trayItem.setImage(image);
				// this.image.dispose(); Enable if using non-system images.
				this.image = image;

				// Update the tool tip.
				this.trayItem.setToolTipText(Globals.WINDOWS_TITLE
						+ Globals.WINDOWS_TITLE_CONNECTOR + "Finished.");

				// Show the tool tip balloon
				this.display.asyncExec(new Runnable() {
				    public void run() {
				    	if (ExecutionComposite.this.toolTip != null) {
					    	ExecutionComposite.this.toolTip.setVisible(true);
					    	ExecutionComposite.this.toolTip = null;
				    	}
				    }
				  });
			}
		}	
	}

	/**
	 * Wrapper method for the ExecutionRunner: Refresh the progress information text.
	 * 
	 * @param progressInformation The progress information text.
	 */
	public void refreshProgressInformationByExecutionRunner(final String progressInformation) {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
		    public void run() {
		    	refreshProgressInformation(progressInformation);
		    }
		  });
	}
	
	/**
	 * Wrapper method for the ExecutionRunner: Set the path of the generated test case file.
	 * 
	 * @param testCasePath The generated test case file's path.
	 */
	public void setTestCasePathByExecutionRunner(final String testCasePath) {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
		    public void run() {
		    	ExecutionComposite.this.testCasePath = testCasePath;
		    }
		  });
	}

	/**
	 * Refresh the progress information text.
	 * @param progressInformation The progress information text.
	 */
	protected void refreshProgressInformation(String progressInformation) {
		if (!this.isDisposed()) {
			// Get the top position.
			int topPixel = this.progressInformationText.getTopIndex();
			// Set the new text.
			this.progressInformationText.setText(progressInformation);
			// Set the top position.
			this.progressInformationText.setTopIndex(topPixel);
		}
	}

	/**
	 * Getter for the currently instantiated StepByStepExecutionRunner.
	 * @return The StepByStepExecutionRunner.
	 */
	protected ExecutionRunner getExecutionRunner() {
		return this.executionRunner;
	}

	/**
	 * Getter for isFinished.
	 * @return true, if the execution is finished, false otherwise.
	 */
	protected boolean isFinished() {
		return this.isFinished;
	}

	/**
	 * Wrapper method for the ExecutionRunner: Show a MessageBox. If the MessageBox
	 * has a return value, provide it.
	 * @param message The text to display in the MessageBox' title.
	 * @param text The main text to display in  MessageBox.
	 * @param flags The MessageBox flags regarding its style.
	 * @return The return value of the MessageBox.
	 */
	public int drawMessageBoxForExecutionRunner(final String message, final String text, final int flags) {
		/**
		 * Inner Class that draws a MessageBox in an own thread. This is needed
		 * for the synchronous access to it. Not drawing it in an own thread will
		 * lead to serious problems since the swt thread for the gui cannot be
		 * accessed by the thread of the ExecutionWindow without wrapping through
		 * this.display.syncExec() or this.display.asyncExec().
		 */
		class MessageBoxWrapper extends Thread {
			// Field to store the response from the MessageBox.
			private int response = -1;

			/**
			 * Draw the MessageBox in an own thread.
			 */
			@Override
		    public void run() {
		    	this.response = StaticGuiSupport.showMessageBox(ExecutionComposite.this.shell, message, text, flags);
		    }

			/**
			 * Method to get the response from the MessageBox.
			 * @return The response from the MessageBox.
			 */
			public int getResponse() {
				return this.response;
			}
		}

		// Instantiate the MessageBoxWrapper.
		MessageBoxWrapper run = new MessageBoxWrapper();

		/**
		 * Synchronous access, since the MessageBox' results might be needed for the
		 * further execution of the Execution Runner. It will hence wait for the
		 * results.
		 */
		this.display.syncExec(run);
		return run.getResponse();
	}

}
