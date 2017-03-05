package de.wwu.muggl.ui.gui.support;

import org.apache.log4j.Level;
import org.eclipse.swt.SWT;

import de.wwu.muggl.common.TimeSupport;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Store;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.LocalVariableAccess;
import de.wwu.muggl.symbolic.testCases.TestCaseSolution;
import de.wwu.muggl.ui.gui.components.StepByStepExecutionComposite;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * The StepByStepExecutionRunner will fork into a new thread. It is the link between step by step
 * GUI and the virtual machine. Basically, the StepByStepExecutionComposite initializes the
 * StepByStepExecutionRunner, which then sets up the virtual machine and controls it. By doing so,
 * it is possible to step through applications, execute a couple of steps or "step over" an
 * invocation instruction and any instructions connected to it (i.e. whole branches of an
 * application). While the StepByStepExecutionRunner blocks on some occasions, the GUI thread always
 * stays unlocked and offers full reactivity to user input. As a side effect, large parts of the
 * step by step logic are encapsulated in this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public class StepByStepExecutionRunner extends Thread {
	// The corresponding composite.
	private StepByStepExecutionComposite stepByStepExecutionComposite;
	// Execution related fields - supplied by the initializing class.
	private MugglClassLoader classLoader;
	private ClassFile classFile;
	private Method method;
	// Execution related fields - initialized later.
	private Application application = null;
	// Execution related fields - initialized at the beginning, overridden during execution.
	private String currentClassName;
	private String currentMethodName;
	private String currentMethodDescriptor;
	// Fields for the execution of multiple steps
	private boolean notAborted;
	private boolean continueExecution;
	private boolean executeInfiniteSteps;
	private int executeSteps;
	private double executeEvery;
	private boolean skippingMode;

	/**
	 * Initialize the StepByStepExecutionRunner.
	 * @param stepByStepExecutionComposite The corresponding stepByStepExecutionComposite.
	 * @param classLoader The system MugglClassLoader.
	 * @param classFile The ClassFile which is executed first.
	 * @param method The method which is executed first.
	 */
	public StepByStepExecutionRunner(
			StepByStepExecutionComposite stepByStepExecutionComposite,
			MugglClassLoader classLoader,
			ClassFile classFile,
			Method method
		) {
		// Set up the execution related fields.
		this.classLoader = classLoader;
		this.classFile = classFile;
		this.method = method;

		this.currentClassName = classFile.getName();
		this.currentMethodName = method.getName();
		this.currentMethodDescriptor = method.getDescriptor();

		this.stepByStepExecutionComposite = stepByStepExecutionComposite;

		this.skippingMode = false;
	}

	/**
	 * Set up the Application.
	 * @return true, if everything could be initialized successfully, false otherwise.
	 */
	public boolean setupApplication() {
		try {
			// If this is not a restart and normal execution mode is enabled, ask the user in case of insufficiently set arguments.
			if (this.application == null && !Options.getInst().symbolicMode && this.method.getNumberOfArguments() > this.method.getNumberOfDefinedPredefinedParameters())
			{
				String message = "The Method expects " + this.method.getNumberOfArguments() + " parameters.\n"
								+ "Currently only " + this.method.getNumberOfDefinedPredefinedParameters() + " have been defined.\n"
								+ "This will most likely result in an abnormal execution of the program.\n\nDo you really wish to continue? Selecting \"No\" will return you to the main window.";
				if (this.stepByStepExecutionComposite.drawMessageBoxForExecutionRunner("Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.NO)
				{
					// do not continue!
					return false;
				}
			}

			// Initialize the Application.
			this.application = new Application(this.classLoader, this.classFile.getName(), this.method);
			this.application.getVirtualMachine().setStepByStepMode(true);
			this.stepByStepExecutionComposite.setApplication(this.application);
			this.application.start();

			// Throttle, as the application initialization might not be yet completed.
			while (this.application.getVirtualMachine().getCurrentFrame() == null && !this.application.getExecutionFinished())
			{
				try {
					Thread.sleep(Globals.SAFETY_SLEEP_DELAY * 3);
				} catch (InterruptedException e) { }
			}

			// Check if the initialization was successful.
			if (this.application.getExecutionFinished()) {
				return false;
			}

			// The method might have changed to &lt;clinit&gt;. Check that and set the fields accordingly, if not skipping of the static initializers has been enabled.
			if (!Options.getInst().visuallySkipStaticInit && !this.method.getName().equals("<clinit>") && this.application.getVirtualMachine().getCurrentFrame().getMethod().getName().equals("<clinit>"))
			{
				this.currentMethodName = this.application.getVirtualMachine().getCurrentFrame().getMethod().getName();
				this.currentMethodDescriptor = this.application.getVirtualMachine().getCurrentFrame().getMethod().getDescriptor();
			}

		// If anything fails, inform the user about what has gone wrong.
		} catch (ClassFileException e) {
			this.stepByStepExecutionComposite.drawMessageBoxForExecutionRunner("Error", "ClassFileException: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			return false;
		} catch (InitializationException e) {
			this.stepByStepExecutionComposite.drawMessageBoxForExecutionRunner("Error", "InitializationException: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Execute the next step. This means, the following actions are taken:
	 * 1. Checking if a value of a local variable is undefined and has to be either assumed or set by the user.
	 * 2. Unblock the virtual machine so it can execute the next step.
	 * 3. Wait for completion.
	 * 4. Check if the currently executed Frame of the virtual machine has changed. It might seen be needed to
	 *    switch to skipping mode.
	 * 5. It might be needed to load instructions for a new method into the gui and to refresh other elements of
	 *    the gui.
	 */
	private void executeNextStep() {
		// Should there be checked for missing values?
		if (!Options.getInst().symbolicMode && (Options.getInst().assumeMissingValues || Options.getInst().askUserMissingValues))
		{
			// Check if the next instruction is a instruction with LocalVariableAccess and if the local variable to load is undefined.
			try {
				Instruction instruction = this.application.getVirtualMachine().getCurrentFrame().getMethod().getInstructionsAndOtherBytes()[this.application.getVirtualMachine().getPc()];
				// Is it a instruction with LocalVariableAccess, but not a storing one?
				if (instruction instanceof LocalVariableAccess && !(instruction instanceof Store))
				{
					int localVariableIndex = ((LocalVariableAccess) instruction).getLocalVariableIndex();
					Object localVariable = this.application.getVirtualMachine().getCurrentFrame().getLocalVariables()[localVariableIndex];
					// Is the value undefined?
					if (localVariable instanceof UndefinedValue) {
						// The action to take no depend on the options selected.
						if (Options.getInst().assumeMissingValues && !Options.getInst().askUserMissingValues) {
							// Set to 0 or null.
							this.application.getVirtualMachine().getCurrentFrame().setLocalVariable(localVariableIndex, this.application.getVirtualMachine().getCurrentFrame().getMethod().getZeroOrNullParameter(localVariableIndex));
						} else {
							this.stepByStepExecutionComposite.setExecutionButtonsEnabledByExecutionRunner(false);
							// Build the text for the message box.
							String text = "The next instruction will load a value that currently is undefined.\n\n"
								+ "By default, values cannot be undefined since methods are called by each other while passing the needed argument. "
								+ "However, by directly accessing methods, those arguments are missing if you did no specifie them. "
								+ "Since you enabled the option, you may now choose to explicitely set a value.\n\n";
							if (Options.getInst().assumeMissingValues) {
								text += "As you also chose that missing values will be assumed to be 0 or null, you may select \"No\" and a value will be auto assigned. It will be 0 if the expected data type is double, float, integer or long. It will be null in any other case.\n\n";
							} else {
								text += "Selecting \"No\" will most likely cause a fatal execution exception when executing the next step. If you do not wish to manually set values, but to set undefined values automatically, abort the current execution, return to the main window and set the corresponding option.\n\n";
							}
							text += "Do you wish to set the value?";

							// Draw message box.
							int response = this.stepByStepExecutionComposite.drawMessageBoxForExecutionRunner("Question", text, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
							if (response == SWT.YES) {
								// Ask for the new value.
								this.stepByStepExecutionComposite.drawInputWindowForExecutionRunner(this.application.getVirtualMachine().getCurrentFrame(), localVariableIndex);
							} else if (response == SWT.CANCEL) {
								// Just return from this method. Execution will stay exactly where it was.
								this.stepByStepExecutionComposite.setExecutionButtonsEnabledByExecutionRunner(true);
								return;
							} else if (response == SWT.NO && Options.getInst().assumeMissingValues) {
								// Set to 0 or null.
								this.application.getVirtualMachine().getCurrentFrame().setLocalVariable(localVariableIndex, this.application.getVirtualMachine().getCurrentFrame().getMethod().getZeroOrNullParameter(localVariableIndex));
							}
							this.stepByStepExecutionComposite.setExecutionButtonsEnabledByExecutionRunner(true);
						}
					}
				}
			} catch (InvalidInstructionInitialisationException e) {
				if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("The check for missing values in method " + this.application.getVirtualMachine().getCurrentFrame().getMethod().getName() + " failed. The root cause is: " +  e.getMessage());
			}
		}

		// Save the current frame.
		Frame currentFrame = this.application.getVirtualMachine().getCurrentFrame();
		// Now execute the next step.
		this.application.getVirtualMachine().executeNextStep();

		// Wait until the last step has been executed.
		try {
			int safetySleepDelay = Globals.REDUCE_SAFETY_SLEEP_DELAY;
			while (this.application.getVirtualMachine().getNextStepReady()
					|| this.application.getVmIsInitializing()) {
				// The Thread will just sleep instead of being blocked completely. So it does not need to be awakened by the virtual machine.
				Thread.sleep(safetySleepDelay);
				// Has the execution finished? Warning: Not catching this will result in a deadlock, since this.application.getVirtualMachine().getNextStepReady() will never return false.
				if (hasExecutionFinished()) return;
			}
			// Check again!
			if (hasExecutionFinished()) return;
		} catch (InterruptedException e) {
			// do nothing
		}

		// Has the frame changed?
		if (currentFrame != this.application.getVirtualMachine().getCurrentFrame()) {
			// Check if skipping mode has to be disabled. If it is on and the class and the methods equals the one we started with, it can be enabled again.
			if (this.skippingMode
					&& this.application.getVirtualMachine().getCurrentFrame().getMethod()
							.getClassFile().getName().equals(this.currentClassName)
					&& this.application.getVirtualMachine().getCurrentFrame().getMethod().getName()
							.equals(this.currentMethodName)
					&& this.application.getVirtualMachine().getCurrentFrame().getMethod()
							.getDescriptor().equals(this.currentMethodDescriptor)) {
				this.skippingMode = false;
			} else if (!this.skippingMode) {
				// Switch to skipping mode?
				if (Options.getInst().stepByStepVisuallySkipInvoc == 0
						|| (Options.getInst().stepByStepVisuallySkipInvoc == 1 && !this.application
								.getVirtualMachine().getCurrentFrame().getMethod().getClassFile()
								.getName().startsWith("java"))
						|| (Options.getInst().stepByStepVisuallySkipInvoc == 2 && this.application
								.getVirtualMachine().getCurrentFrame().getMethod().getClassFile()
								.getName().equals(this.currentClassName))) {
					refreshMethodIfNeeded();
				} else {
					// Enable skipping mode;
					this.skippingMode = true;
					this.currentClassName = currentFrame.getMethod().getClassFile().getName();
					this.currentMethodName = currentFrame.getMethod().getName();
					this.currentMethodDescriptor = currentFrame.getMethod().getDescriptor();
				}
			}
		}

		// Refresh the GUI if skipping mode is not enabled.
		if (!this.skippingMode) {
			// Refresh the machine state.
			this.stepByStepExecutionComposite.refreshMachineStateByExecutionRunner();

			// Refresh the Symbolic execution state.
			this.stepByStepExecutionComposite.refreshSymbolicExecutionStateByExecutionRunner();

			// Set the pc.
			this.stepByStepExecutionComposite.setPCByExecutionRunner();
		}
	}

	/**
	 * Refresh the method if needed. This especially includes the visual representation of it.
	 */
	private void refreshMethodIfNeeded() {
		if (checkForMethodRefresh()) {
			Method method = this.application.getVirtualMachine().getCurrentFrame().getMethod();
			this.stepByStepExecutionComposite.loadInstructionsByExecutionRunner(method);
			// For safety reasons, we will now sleep until the loading of the instructions is complete.
			if (!this.stepByStepExecutionComposite.getInstructionLoadingComplete()) {
				while (!this.stepByStepExecutionComposite.getInstructionLoadingComplete()) {
					try {
						sleep(Globals.SAFETY_SLEEP_DELAY);
					} catch (InterruptedException e) {
						// Do nothing, this is unproblematical.
					}
				}
			}
		}
	}

	/**
	 * Check if a refresh of the method is needed. If it is needed, load the current method data
	 * and return true. Simply return false otherwise.
	 *
	 * @return true, if a refresh of the method is needed, false otherwise.
	 */
	public boolean checkForMethodRefresh() {
		Frame frame = this.application.getVirtualMachine().getCurrentFrame();

		// Refresh the instructions if either the class or both the name and the descriptor of the method changed.
		if (!frame.getMethod().getClassFile().getName().equals(this.currentClassName)
				|| !(frame.getMethod().getName().equals(this.currentMethodName)
				&& frame.getMethod().getDescriptor().equals(this.currentMethodDescriptor))) {
			// Set the new class and method name/descriptor.
			Method method = frame.getMethod();
			this.currentClassName = method.getClassFile().getName();
			this.currentMethodName = method.getName();
			this.currentMethodDescriptor = method.getDescriptor();
			return true;
		}
		return false;
	}

	/**
	 * If the execution is finished, the buttons will be disabled and the
	 * machine state refreshed; the method then return true. It returns
	 * false otherwise.
	 *
	 * @return true, if the execution is finished, false otherwise.
	 */
	public boolean hasExecutionFinished() {
		// Continue only if the virtual machine executed by the Application has not changed.
		if (!this.application.getVmIsInitializing()) {
			// If we are executing symbolically, find out if there has been found a new solution.
			if (Options.getInst().symbolicMode && ((SymbolicVirtualMachine) this.application.getVirtualMachine()).getSolutionProcessor().hasFoundSolution())
			{
				int number = ((SymbolicVirtualMachine) this.application.getVirtualMachine()).getSolutionProcessor().getNewestSolutionNumber() + 1;
				if (number > 1) this.stepByStepExecutionComposite.addSolution("", true); // A blank line.

				// Add the found solution(s).
				TestCaseSolution solution = ((SymbolicVirtualMachine) this.application.getVirtualMachine()).getSolutionProcessor().getNewestSolution();
				this.stepByStepExecutionComposite.addSolution(number + ": " + solution.getSolution().toString(), true);

				// Was an uncaught exception thrown?
				if (this.application.getThrewAnUncaughtException()) {
					// When executing symbolically, the returned object is not the uncaught Throwable directly but the wrapping NoExceptionHandlerFoundException.
					String[] nameAndMessage = ((NoExceptionHandlerFoundException) this.application.getReturnedObject()).getUncaughtThrowableNameAndMessage();
					this.stepByStepExecutionComposite.addSolution("uncaught exception: " + nameAndMessage + " (" + nameAndMessage + ")" , true);
				} else {
					// Is there a returned value?
					Object returnedValue = solution.getReturnValue();
					if (returnedValue == null) {
						this.stepByStepExecutionComposite.addSolution("Return value: null", true);
					} else if (returnedValue instanceof UndefinedValue) {
						this.stepByStepExecutionComposite.addSolution("(No return value", true);
					} else if (returnedValue instanceof Arrayref) {
						Arrayref returnedValueArrayref = (Arrayref) returnedValue;
						String solutionText = returnedValueArrayref.toString() + " Contents: ";
						for (int a = 0; a < returnedValueArrayref.length; a++) {
							if (a > 0) solutionText += ", ";
							Object element = returnedValueArrayref.getElement(a);
							if (element != null) {
								solutionText += element.toString();
							} else {
								solutionText += "null";
							}
						}
						this.stepByStepExecutionComposite.addSolution(solutionText, true);
					} else if (returnedValue instanceof Term) {
						this.stepByStepExecutionComposite.addSolution("Return value: " + ((Term) returnedValue).toString(false), true);
					} else {
						this.stepByStepExecutionComposite.addSolution("Return value: " + returnedValue.toString(), true);
					}
				}
			}

			// Find out, if the execution has finished.
			if (this.application.getExecutionFinished()) {
				// Finalize the execution.
				this.stepByStepExecutionComposite.setExecutionButtonsEnabledByExecutionRunner(false);
				this.stepByStepExecutionComposite.refreshMachineStateByExecutionRunner();
				this.stepByStepExecutionComposite.refreshSymbolicExecutionStateByExecutionRunner();
				this.continueExecution = false;
				// Save the solution, if we are not in symbolic mode.
				if (!Options.getInst().symbolicMode) {
					if (this.application.getHasAReturnValue()) {
						this.stepByStepExecutionComposite.addSolution(this.application.getReturnedObject(), false);
					} else if (this.application.getThrewAnUncaughtException()) {
						Objectref objectref = (Objectref) this.application.getReturnedObject();
						String message = "";
						try {
							message = this.application.getVirtualMachine().getStringCache()
									.getStringFieldValue(objectref, "detailMessage");
						} catch (ExecutionException e) {
							// This should not happen.
						}
						this.stepByStepExecutionComposite.addSolution("An uncaught exception was thrown: "
									+ objectref.getInitializedClass().getClassFile().getCanonicalName() + " (" + message + ")", true);
					} else {
						this.stepByStepExecutionComposite.addSolution("(no return value)", true);
					}
				}
				// Return.
				return true;
			}
		}
		return false;
	}

	/**
	 * If an Application has been initialized, invoke its method
	 * to abort the execution.
	 */
	public synchronized void abortExecution() {
		// Stop the Application.
		if (this.application != null) this.application.abortExecution();

		// Stop this runner.
		this.notAborted = false;
		this.continueExecution = true; // Execution might be in waiting state. So waiting is ended, but when then entering the loop this.notAborted is false and then thread will terminate.
		// If this thread is not terminated or was never run, notify it.
		if (this.getState() != java.lang.Thread.State.TERMINATED && this.getState() != java.lang.Thread.State.NEW)
		{
			notify();
		}

		// Finalize and clean up the Application.
		if (this.application != null) {
			this.application.cleanUp(false);
			this.application = null;
		}
	}

	/**
	 * Stop the execution. For doing so, continueExecution is set to false. This will stop the execution a the next
	 * possible point.
	 */
	public void stopExecution() {
		this.continueExecution = false;

	}

	/**
	 * Start the execution. When this method is invoked, the next thread will be created and the virtual machine started.
	 */
	@Override
	public void run() {
		// Basic initialization.
		this.continueExecution = true;
		this.notAborted = true;
		int stepsExecuted = 0;
		long sleepingMiliseconds = (long) (this.executeEvery * TimeSupport.MILLIS_SECOND_D);

		// Outer loop - do not abort it until the execution is really finished.
		while (this.notAborted) {
			// Reset the steps executed.
			stepsExecuted = 0;

			// Main loop. run while execution should be continued, and there are either infinite steps to take, not all steps are taken or skipping mode is being run.
			while (this.continueExecution && (this.skippingMode || this.executeInfiniteSteps || stepsExecuted < this.executeSteps))
			{
				// Probably the execution has to be stopped for a while - but not on the first step! With skipping mode there is no stopping either.
				if (!this.skippingMode && stepsExecuted > 0) {
					try {
						if (!(this.executeEvery == -1)) {
							sleep(sleepingMiliseconds);
						} else {
							sleep(Globals.MIN_SLEEP_BETWEEN_STEPS);
						}
						if (!this.notAborted) return;
					} catch (InterruptedException e) {
						// Stop the execution.
						this.stepByStepExecutionComposite.drawMessageBoxForExecutionRunner("Error", "A fatal error occured while waiting for the execution of the next step. Execution cannot be continued.\n\nThe root cause is:\n" + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
						this.continueExecution = false;
						return;
					}
				}

				// Execute the next step.
				executeNextStep();

				// Did any errors occur?
				this.stepByStepExecutionComposite.checkIfErrorOccurredByExecutionRunner();

				// Count the steps if not in skipping mode.
				if (!this.skippingMode) {
					// Count up.
					stepsExecuted++;
				}

				// Count in the GUI, if it is not a single step.
				if (this.executeSteps > 1) {
					this.stepByStepExecutionComposite.setStepsToGo(this.executeSteps - stepsExecuted);
				}

				// Finish the execution?
				if (this.application.getExecutionFinished()) return;
			}

			// Stop the execution in the parent window.
			this.stepByStepExecutionComposite.stopExecutionByExecutionRunner();
			// Set the steps in the GUI to the original value (if steps were neither unlimited nor just one).
			if (this.executeSteps > 1) {
				this.stepByStepExecutionComposite.setStepsToGo(this.executeSteps);
			}

			// Polling: When to continue?
			try {
				this.continueExecution = false;
				synchronized (this) {
					while (!this.continueExecution) {
						wait();
					}
				}
				sleepingMiliseconds = (long) (this.executeEvery * TimeSupport.MILLIS_SECOND_D); // Might have changed.
			}  catch (InterruptedException e) {
				this.stepByStepExecutionComposite.drawMessageBoxForExecutionRunner(
						"Error", e.getClass().getName() + ": "
							+ e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			}
		}
	}

	/**
	 * Continue the execution. For doing so, continueExecution is set to true. This will let the execution continue in
	 * a short while.
	 *
	 */
	public synchronized void continueExecution() {
		this.continueExecution = true;
		notify();
	}

	/**
	 * Getter for the Application.
	 * @return The Application generated by this ExectionRunner.
	 */
	public Application getApplication() {
		return this.application;
	}

	/**
	 * Setting the execution mode so that an infinite number of steps will be executed.
	 * @param executeInfiniteSteps If this argument is true, the execution of an infinite number of steps is enabled. It is disabled otherwise.
	 */
	public void setExecuteInfiniteSteps(boolean executeInfiniteSteps) {
		this.executeInfiniteSteps = executeInfiniteSteps;
	}

	/**
	 * Setter for the number of steps to execute.
	 * @param executeSteps The number of steps to execute.
	 */
	public void setExecuteSteps(int executeSteps) {
		this.executeSteps = executeSteps;
	}

	/**
	 * Setter for the number of seconds to wait between the execution of two steps.
	 * @param executeEvery The number of seconds to wait between the execution of two steps.
	 */
	public void setExecuteEvery(double executeEvery) {
		this.executeEvery = executeEvery;
	}

}
