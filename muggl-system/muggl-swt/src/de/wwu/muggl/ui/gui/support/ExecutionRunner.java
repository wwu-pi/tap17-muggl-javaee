package de.wwu.muggl.ui.gui.support;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.eclipse.swt.SWT;

import de.wwu.muggl.common.TimeSupport;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.symbolic.flow.coverage.CGCoverage;
import de.wwu.muggl.symbolic.flow.coverage.CoverageController;
import de.wwu.muggl.symbolic.flow.coverage.DUCoverage;
import de.wwu.muggl.symbolic.testCases.SolutionProcessor;
import de.wwu.muggl.ui.gui.components.ExecutionComposite;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The ExecutionRunner will fork into a new thread. It is the link between execution GUI and the
 * virtual machine. Basically, the ExecutionComposite initializes the ExecutionRunner, which then
 * sets up the virtual machine and controls it. By doing so, it is possible to pause and halt
 * applications. The ExecutionRunner will also periodically supply statistical information
 * to the ExecutionWindow.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class ExecutionRunner extends Thread {
	// The corresponding composite.
	private ExecutionComposite executionComposite;

	// Execution related fields - supplied by the initializing class.
	private MugglClassLoader classLoader;
	private ClassFile classFile;
	private Method method;

	// Execution related fields.
	private boolean isStarted;
	private boolean isPaused;
	private boolean abortedManually;
	private Application application;
	private long timeStarted;
	private boolean wroteFinalOutput;
	private long pauseStarted;
	private long pauseTimeTotal;
	private String finishingMessage;
	private String abortionMessage;
	private long maxSolutionsFound;

	// The refresh period.
	private int refreshEvery;

	// Fields for the additional displaying of test case generation statistics.
	private long testCasesTimeStarted;
	private String finalInformation;

	/**
	 * Initialize the ExecutionRunner.
	 * @param executionComposite The corresponding stepByStepExecutionComposite.
	 * @param classLoader The system MugglClassLoader.
	 * @param classFile The ClassFile which is executed first.
	 * @param method The method which is executed first.
	 * @param refreshEvery The period in miliseconds after which the ExecutionWindow is refreshed.
	 */
	public ExecutionRunner(
			ExecutionComposite executionComposite,
			MugglClassLoader classLoader,
			ClassFile classFile,
			Method method,
			int refreshEvery
		) {
		// Set up the execution related fields.
		this.classLoader = classLoader;
		this.classFile = classFile;
		this.method = method;
		this.executionComposite = executionComposite;
		this.isStarted = false;
		this.isPaused = false;
		this.abortedManually = false;
		this.application = null;
		this.timeStarted = 0L;
		this.wroteFinalOutput = false;
		this.pauseStarted = 0L;
		this.pauseTimeTotal = 0L;
		this.maxSolutionsFound = 0L;
		this.refreshEvery = refreshEvery;
		this.testCasesTimeStarted = 0L;
		this.finalInformation = "";
	}

	/**
	 * Fork into a new thread and set up the execution. Then start the main execution loop.
	 * It will block until execution is started by the ExecutionWindow. It then continues
	 * to update the ExecutionWindow with information until execution is either finished or
	 * aborted.
	 */
	@Override
	public void run() {
		Options options = Options.getInst();
		
		// Initialize the Application.
		boolean initialized = true;
		try {
			this.application  = new Application(this.classLoader, this.classFile.getName(), this.method);
 		} catch (ClassFileException e) {
 			this.executionComposite.drawMessageBoxForExecutionRunner("Error", "ClassFileException: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
 			this.executionComposite.abortExecutionByExecutionRunner();
 			initialized = false;
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			this.executionComposite.drawMessageBoxForExecutionRunner("Error", "IllegalStateException: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
 			this.executionComposite.abortExecutionByExecutionRunner();
 			initialized = false;
 		} catch (InitializationException e) {
 			this.executionComposite.drawMessageBoxForExecutionRunner("Error", "InitializationException: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
 			this.executionComposite.abortExecutionByExecutionRunner();
 			initialized = false;
 		}

 		// Enter the main execution loop.
 		if (initialized) {
			try {
				// Poll when to start.
				synchronized (this) {
					while (!this.isStarted) {
						wait();
					}
				}
				this.timeStarted = System.currentTimeMillis();
				this.application.start();
	
				// Reset the counters of the SolverManager of the SymbolicalVirtualMachine, should the execution mode be symbolic.
				if (options.symbolicMode) ((SymbolicVirtualMachine) this.application.getVirtualMachine()).getSolverManager().resetCounter();
	
				// The first sleep should be shorter.
				boolean firstSleep = true;
	
				// The main execution loop.
				while (this.isStarted) {
					// Check if the maximum execution time has been reached.
					if (options.maximumExecutionTime != -1) {
						if (System.currentTimeMillis() - this.timeStarted - this.pauseTimeTotal > options.maximumExecutionTime * TimeSupport.MILLIS_SECOND) {
							// If the mode is symbolical, store information that the execution was stopped.
							if (options.symbolicMode) {
								((SymbolicVirtualMachine) this.application.getVirtualMachine()).setAbortionCriterionMatched(true);
								((SymbolicVirtualMachine) this.application.getVirtualMachine()).setAbortionCriterionMatchedMessage("The time limit has been reached.");
							}
	
							// Stop the execution.
							this.finishingMessage = "The Time has been limit reached. A halt of the virtual machine was enforced.";
							stopExecutionDueToLimitations();
							break;
						}
					}
	
					// Refresh the execution window;
					refreshExecutionWindow(false, false);
	
					// Sleep for the desired time.
					if (firstSleep) {
						Thread.sleep(Globals.SAFETY_SLEEP_DELAY);
						firstSleep = false;
					} else {
						// Save the time sleeping started.
						long sleeptStarted = System.currentTimeMillis();
						int sleepFor = this.refreshEvery;
						int maximumSleepingSlice = Globals.SAFETY_SLEEP_DELAY;
						// Continue to sleep until we slept long enough.
						while (sleepFor > 0) {
							// Has the execution finished?
							if (getExecutionFinished()) break;
							// And now sleep.
							try {
								/*
								 * Determine if the sleeping period is shorter than the maximum time to sleep
								 * before checking if the execution has finished. If the sleeping time is
								 * high and execution finishes in the meanwhile, there would be no output
								 * until sleeping is finished. So this is checked more frequently with higher
								 * sleeping times. It does not consume a lot more cpu (actually its not really
								 * appreciable), but the user will almost immediately get informed if the
								 * execution finished.
								 */
								int sleepingSlice = sleepFor;
								if (sleepingSlice > maximumSleepingSlice) sleepingSlice = maximumSleepingSlice;
								Thread.sleep(sleepingSlice);
								// Sleeping finished as expected. So decrease the needed sleeping time by the minimum sleep delay.
								sleepFor -= sleepingSlice;
							} catch (InterruptedException e) {
								// Sleeping was interrupted as the time to sleep was changed. Set it to the new time, but drop the time we slept already.
								sleepFor = this.refreshEvery - (int) (System.currentTimeMillis() - sleeptStarted);
							}
						}
					}
	
					// Wait if the execution has been paused.
					synchronized (this) {
						while (this.isPaused) {
							wait();
						}
					}
				}
	
				// Finished the execution.
				refreshExecutionWindow(true, false);
	
				/*
				 * If the execution mode is symbolical, it now might take some time to generate test cases.
				 * Generation will of course not be started if the execution was aborted manually or if an
				 * error occurred during execution. In that case, there is no need to display anything about
				 * the test case generation process.
				 */
				if (!this.abortedManually && !this.application.getVirtualMachine().errorOccured()
						&& options.symbolicMode) {
					this.executionComposite.disablePauseButtonByExecutionRunner();
					this.testCasesTimeStarted = System.currentTimeMillis();
	
					SolutionProcessor solutionProcessor =
						((SymbolicVirtualMachine) this.application.getVirtualMachine()).getSolutionProcessor();
					while (!solutionProcessor.hasTestCaseGenerationFinished() && !solutionProcessor.hasTestCaseGenerationAborted()) {
						// Sleep for the desired time.
						if (firstSleep) {
							Thread.sleep(Globals.SAFETY_SLEEP_DELAY);
							firstSleep = false;
						} else {
							// Save the time sleeping started.
							long sleeptStarted = System.currentTimeMillis();
							int sleepFor = this.refreshEvery;
							int maximumSleepingSlice = Globals.SAFETY_SLEEP_DELAY;
							// Continue to sleep until we slept long enough.
							while (sleepFor > 0) {
								// Has the execution finished?
								if (solutionProcessor.hasTestCaseGenerationFinished()) break;
								// And now sleep.
								try {
									/*
									 * Determine if the sleeping period is shorter than the maximum time to sleep
									 * before checking if the execution has finished. If the sleeping time is
									 * high and execution finishes in the meanwhile, there would be no output
									 * until sleeping is finished. So this is checked more frequently with higher
									 * sleeping times. It does not consume a lot more CPU (actually its not really
									 * appreciable), but the user will almost immediately get informed if the
									 * execution finished.
									 */
									int sleepingSlice = sleepFor;
									if (sleepingSlice > maximumSleepingSlice) sleepingSlice = maximumSleepingSlice;
									Thread.sleep(sleepingSlice);
									// Sleeping finished as expected. So decrease the needed sleeping time by the minimum sleep delay.
									sleepFor -= sleepingSlice;
								} catch (InterruptedException e) {
									// Sleeping was interrupted as the time to sleep was changed. Set it to the new time, but drop the time we slept already.
									sleepFor = this.refreshEvery - (int) (System.currentTimeMillis() - sleeptStarted);
								}
							}
						}
	
						// Print the statistics.
						if (solutionProcessor.hasTestCaseGenerationStarted())
							refreshExecutionWindowWithTestCaseStatistics(false, solutionProcessor);
	
						// Wait if the execution has been paused.
						synchronized (this) {
							while (this.isPaused) {
								wait();
							}
						}
					}
					// Finished with the test case generation!
					refreshExecutionWindowWithTestCaseStatistics(true, solutionProcessor);
				}
	
				// Finished with everything!
				this.executionComposite.executionFinishedByExecutionRunner();
	
			} catch (InterruptedException e) {
				// Just give out a message and then abort.
	 			this.executionComposite.drawMessageBoxForExecutionRunner("Error", "Execution was not successfull due to a threading error. Please try again.", SWT.OK | SWT.ICON_ERROR);
	 			this.executionComposite.abortExecutionByExecutionRunner();
			} finally {
				finalize();
			}
 		}
	}

	/**
	 * Refresh the ExecutionWindow with statistical information and details about the abortion
	 * of finish of the execution.
	 * @param finished If set to true it signalizes that the execution was finished.
	 * @param aborted If set to true it signalizes that the execution was aborted.
	 */
	private void refreshExecutionWindow(boolean finished, boolean aborted) {
		Options options = Options.getInst();
		if (!this.wroteFinalOutput) {
			// Make sure initialization was successful.
			if (this.application != null) {
				// Gather general statistics.
				VirtualMachine vm = this.application.getVirtualMachine();
				long miliSecondsRun = System.currentTimeMillis() - this.timeStarted - this.pauseTimeTotal;
				long secondsRun = Math.round(((double) miliSecondsRun) / TimeSupport.MILLIS_SECOND);
				String generalStatistics = "Statistical information:\n"
					+ "Execution time:\t\t\t\t\t\t\t" + TimeSupport.computeRunningTime(miliSecondsRun, true) + "\n"
					+ "Maximum running time:\t\t\t\t\t";
				long maximumExecutionTime = options.maximumExecutionTime;
				if (maximumExecutionTime == -1) {
					generalStatistics += "no limit";
				} else {
					generalStatistics += TimeSupport.computeRunningTime(maximumExecutionTime * TimeSupport.MILLIS_SECOND, false);
				}
				
				if (options.symbolicMode) {
					long[] arrayGeneration = ((SymbolicVirtualMachine) vm).getArrayGenerationInformation();
					generalStatistics += "\n"
						+ "Number of frames executed:\t\t\t\t" + StaticGuiSupport.formatNumericValue(vm.getExecutedFrames(), 3) + "\n"
						+ "Number of frames per second:\t\t\t" + StaticGuiSupport.formatNumericValue((double) Long.valueOf(vm.getExecutedFrames()) / (double) secondsRun, 3) + "\n"
						+ "Number of instructions executed:\t\t\t" + StaticGuiSupport.formatNumericValue(vm.getExecutedInstructions(), 3) + "\n"
						+ "Number of instructions per second:\t\t" + StaticGuiSupport.formatNumericValue((double) Long.valueOf(vm.getExecutedInstructions()) / (double) secondsRun, 3) + "\n\n"
						+ "Number of classes parsed and loaded:\t" + vm.getClassLoader().getClassesLoaded() + "\n"
						+ "Number of classes instantiated:\t\t\t" + vm.getClassLoader().getClassesInstantiated() + "\n"
						+ "Number of array generators used:\t\t" + arrayGeneration[0] + "\n"
						+ "Number of arrays generated:\t\t\t\t" + arrayGeneration[1] + "\n";
				}
				
				String information = "";
				// Has the execution been aborted?
				if (aborted) {
					information += "The execution has been aborted.";
					if (this.abortionMessage != null) information += " " + this.abortionMessage;
					information += "\n\n" + generalStatistics;
					this.wroteFinalOutput = true;
				} else {
					// Distinguish between normal and symbolic mode.
					if (options.symbolicMode) {
						if (finished) {
							if (this.finishingMessage != null) information = this.finishingMessage + "\n\n";
							// Find out if execution finished successfully.
							if (this.application.errorOccured()) {
								// There was an error.
								information += "Execution did not finish successfully. The reason is:\n"
									+ this.application.fetchError() + "\n\n";
							} else {
								// Symbolic execution finished.
								information += "Symbolic execution finished successfully.\n\n";
	
								if (((SymbolicVirtualMachine) this.application.getVirtualMachine()).getAbortionCriterionMatched()) {
									information += "Execution was aborted before it came to an end by visiting all branches in the search tree. "
										+ ((SymbolicVirtualMachine) this.application.getVirtualMachine()).getAbortionCriterionMatchedMessage() + "\n";
								}
								if (((SymbolicVirtualMachine) this.application.getVirtualMachine()).getMaximumLoopsReached()) {
									information += "The maximum number of loops was reached at least one time. Setting a "
									+ "higher number of maximum loops to reach before backtracking might lead to a higher "
									+ "number of solutions found.\n";
								}
	
								information += "\n";
							}
	
							this.wroteFinalOutput = true;
						}
	
						// Output statistical information about the symbolic execution.
						information	+= generalStatistics;
						String[][] statistics = ((SymbolicVirtualMachine) vm).getSearchAlgorithm().getStatisticalInformation();
						if (statistics != null && statistics.length > 0) {
							for (int a = 0; a < statistics.length; a++) {
								information += "\n" + statistics[a][0]
											+ StaticGuiSupport.formatNumericValue(statistics[a][1], 3);
							}
							// If the last information is "# of constraints checked total", add time statistics.
							if (statistics[statistics.length - 1][0].startsWith("# of constraints checked total")) {
								information += "\n...per second:\t\t\t\t\t\t\t\t"
											+ StaticGuiSupport.formatNumericValue((double) Long.valueOf(statistics[statistics.length - 1][1]) / (double) secondsRun, 3);
							}
							information += "\n\n";
						}
	
						if (!finished) {
							// Check if coverage information should be shown.
							CoverageController coverage = ((SymbolicVirtualMachine) vm).getCoverageController();
	
							// Output information about the def-use coverage, if it is used.
							if (options.useDUCoverage) {
								information += "Def-use coverage:\n"
										+ "Current\ttotal\tmaxmimum\n";
								DUCoverage dUCoverage = coverage.getDUCoverage();
								information += dUCoverage.getNumberOfCurrentlyCoveredDUChains() + "\t\t\t"
									+ dUCoverage.getNumberOfCoveredDUChains() + "\t\t"
									+ dUCoverage.getNumberOfTotaldDUChains() + "\n";
							}
	
							// Output information about the control graph coverage, if it is used.
							if (options.useCFCoverage) {
								information += "Current control graph coverage:\n"
									+ "Current\ttotal\tmaxmimum\tmethod\n";
								Map<Method, CGCoverage> cGCoverageMap = coverage.getCGCoverageMap();
								try {
								for (Entry<Method, CGCoverage> entry : cGCoverageMap.entrySet()) {
									Method method = entry.getKey();
									CGCoverage gCoverage = entry.getValue();
									information += gCoverage.getNumberOfCurrentlyCoveredCGEdges() + "\t\t\t"
									+ gCoverage.getNumberOfCoveredCGEdges() + "\t\t"
									+ gCoverage.getNumberOfEdges() + "\t\t"
									+ method.getClassFile().getName() + "." + method.getFullName() + "\n";
								}
								} catch (ConcurrentModificationException e) {
									// Ignore for now. TODO
								}
							}
	
							if (options.useDUCoverage || options.useCFCoverage)
								information += "\n";
	
							// Output the number of solutions found.
							long numberOfSolutions = ((SymbolicVirtualMachine) vm).getSolutionProcessor().getNumberOfSolutions();
							if (numberOfSolutions > this.maxSolutionsFound) this.maxSolutionsFound = numberOfSolutions;
							information += "Solutions found so far:\t\t\t\t\t" + StaticGuiSupport.formatNumericValue(this.maxSolutionsFound, 3) + "\n"
							+ "Solutions found per second:\t\t\t\t" + StaticGuiSupport.formatNumericValue((double) this.maxSolutionsFound / (double) secondsRun, 3) + "\n";
						}
	
						// Output statistical information about the solving.
						if (Options.getInst().measureSymbolicExecutionTime) {
							information += "\nTime spent on...\n";
							long[] executionTimeInformation = ((SymbolicVirtualMachine) vm).getExecutionTimeInformation();
							double percentage = ((double) executionTimeInformation[0]) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / (double) 100;
							information += "...instruction execution:\t\t\t\t\t" + TimeSupport.computeRunningTime(executionTimeInformation[0], false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n";
							percentage = ((double) executionTimeInformation[1]) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / (double) 100;
							information += "...loop detection:\t\t\t\t\t\t\t" + TimeSupport.computeRunningTime(executionTimeInformation[1], false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n";
							percentage = ((double) executionTimeInformation[2]) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / (double) 100;
							information += "...coverage checking:\t\t\t\t\t\t" + TimeSupport.computeRunningTime(executionTimeInformation[2], false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n\n";
							percentage = ((double) executionTimeInformation[3]) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / (double) 100;
							information += "...choice point generation:\t\t\t\t" + TimeSupport.computeRunningTime(executionTimeInformation[3], false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n";
							percentage = ((double) executionTimeInformation[4]) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / (double) 100;
							information += "...solving:\t\t\t\t\t\t\t\t\t" + TimeSupport.computeRunningTime(executionTimeInformation[4], false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n";
							percentage = ((double) executionTimeInformation[5]) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / (double) 100;
							information += "...backtracking:\t\t\t\t\t\t\t" + TimeSupport.computeRunningTime(executionTimeInformation[5], false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n";
							percentage = ((double) executionTimeInformation[6]) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / 100;
							information += "...solution generation:\t\t\t\t\t" + TimeSupport.computeRunningTime(executionTimeInformation[6], false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n\n";
							long aggregatedExecutionTimes = executionTimeInformation[0] + executionTimeInformation[1]
							                              + executionTimeInformation[2] + executionTimeInformation[3]
							                              + executionTimeInformation[4] + executionTimeInformation[5]
							                              + executionTimeInformation[6];
							percentage = ((double) aggregatedExecutionTimes) / ((double) miliSecondsRun);
							percentage = (Math.round(percentage * 10000)) / 100;
							information += "...all measured actions:\t\t\t\t\t" + TimeSupport.computeRunningTime(aggregatedExecutionTimes, false) + "\n"
								+ "Percentage of total running time:\t\t\t" + percentage + "%\n";
	
	
						}
					} else {
						if (finished) {
							if (this.finishingMessage != null) information = this.finishingMessage + "\n\n";
							// Find out if execution finished successfully.
							if (this.application.errorOccured()) {
								// There was an error.
								information = "Execution did not finish successfully. The reason is:\n"
									+ this.application.fetchError() + "\n\n";
							} else {
								// Normal execution finished.
								information = "Execution finished successfully.\n\n";
								if (this.application.getHasAReturnValue()) {
									Object object = this.application.getReturnedObject();
									if (object == null) {
										information += "A null reference was returned.";
									} else {
										information += "An object of type " + object.getClass().getName() + " was returned. Its' toString()-Method said: " + object.toString();
									}
								} else if (this.application.getThrewAnUncaughtException()) {
									//Throwable throwable =  (Throwable) this.application.getReturnedObject();
									Objectref objectref = (Objectref) this.application.getReturnedObject();
									//Field field = objectref.getInitializedClass().getClassFile().getFieldByName("detailMessage", true);
									//objectref.getField(field);
									// Extract the message
									information += "An uncaught exception was thrown: "
												+ objectref.getName();// + " (" +  + ")";
								} else {
									information += "There was no return value.";
								}
							}
							this.wroteFinalOutput = true;
						}
						information += "\n\n" + generalStatistics;
					}
				}
	
				// Last time there is anything written?
				if (this.wroteFinalOutput)
					this.finalInformation = information;
	
				// Set the progress information string.
				this.executionComposite.refreshProgressInformationByExecutionRunner(information);
			}
		}
	}

	/**
	 * This method can be called if execution is finished.
	 * 
	 * @param finished Indicates whether execution is finished or test cases are generated.
	 * @param solutionProcessor The {@link SolutionProcessor} used.
	 */
	private void refreshExecutionWindowWithTestCaseStatistics(boolean finished, SolutionProcessor solutionProcessor) {
		String information = "";

		// Is the test case generation finished?
		if (!finished) {
			information += "Now generating test cases...\n\n";
		} else {
			long miliSecondsRun = System.currentTimeMillis() - this.timeStarted - this.pauseTimeTotal;
			information += "Total running time:\t\t\t\t" + TimeSupport.computeRunningTime(miliSecondsRun, true) + "\n";
		}
		long miliSecondsRun = System.currentTimeMillis() - this.testCasesTimeStarted;
			information += "Elimination time:\t\t\t\t\t" + TimeSupport.computeRunningTime(miliSecondsRun, true) + "\n";
		if (!finished) {
			information += "Current step:\t\t\t\t\t\t";
			if (!solutionProcessor.hasFinishedDeletingRedudancy()) {
				information += "Deleting redundant solutions...";
			} else if (!solutionProcessor.hasFinishedElimination()) {
				information += "Eliminating test cases...";
			} else {
				information += "Writing test cases...";
			}
			information += "\n\n";
		} else {
			// Finished as the process really was finished or due to abortion?`
			if (solutionProcessor.hasTestCaseGenerationAborted()) {
				information += "Test case generation was aborted before the test file was written.\n";
			} else if (solutionProcessor.hasTestCaseGenerationFailed()) {
				Throwable causeOfFailure = solutionProcessor.getCauseOfFailure();
				information += "Test case generation failed. The reason is: " + causeOfFailure.getClass().getName()
				+ " (" + causeOfFailure.getMessage() + "). No test cases have been written.\n";
			} else {
				String testCasePath = solutionProcessor.getGeneratedClassFilePath();
				information += "A Total of " + StaticGuiSupport.formatNumericValue(solutionProcessor.getTestCaseEliminationFigures()[2])
					+ " Test cases have been written to "
					+ testCasePath + ".\n";
				this.executionComposite.setTestCasePathByExecutionRunner(testCasePath);
			}
		}

		long[] testCaseEliminationFigures = solutionProcessor.getTestCaseEliminationFigures();
		if (testCaseEliminationFigures[0] != -1L) {
			information += "\nSolutions found:\t\t\t\t\t" + StaticGuiSupport.formatNumericValue(testCaseEliminationFigures[0]) + "\n";
			if (!solutionProcessor.hasFinishedDeletingRedudancy()) {
				long solutionNumber = solutionProcessor.getDeletingRedudancySolutionCurrentlyProcessed();
				if (solutionNumber != -1) {
					information += "Deleting solutions by redundacy:\tProcessing #"
						+ StaticGuiSupport.formatNumericValue(solutionNumber) + "\n";
				}
			}
		}
		if (testCaseEliminationFigures[1] != -1L) {
			information += "Solutions without redudancy:\t\t" + StaticGuiSupport.formatNumericValue(testCaseEliminationFigures[1]) + "\n";
			if (!solutionProcessor.hasFinishedElimination()) {
				information += "Eliminating test cases...\n"
					+ "\talready dropped:\t\t\t\t" + StaticGuiSupport.formatNumericValue(solutionProcessor.getEliminationSolutionsDropped()) + "\n"
					+ "\talready kept:\t\t\t\t\t" + StaticGuiSupport.formatNumericValue(solutionProcessor.getEliminationSolutionsKept()) + "\n";
			}
		}
		if (testCaseEliminationFigures[2] != -1L) {
			information += "Test cases:\t\t\t\t\t\t" + StaticGuiSupport.formatNumericValue(testCaseEliminationFigures[2]) + "\n";
		}
		information += "\n";

		// Set the progress information String concatenated with the already finished information String.
		this.executionComposite.refreshProgressInformationByExecutionRunner(information + this.finalInformation);
	}


	/**
	 * Start the execution.
	 */
	public void startExecution() {
		this.isStarted = true;
	}

	/**
	 * Pause the execution.
	 */
	public synchronized void pauseExecution() {
		this.application.getVirtualMachine().pauseExecution();
		this.pauseStarted = System.currentTimeMillis();
		this.isPaused = true;
	}

	/**
	 * Resume the execution.
	 */
	public synchronized void resumeExecution() {
		this.application.getVirtualMachine().resumeExecution();
		this.pauseTimeTotal += System.currentTimeMillis() - this.pauseStarted;
		this.pauseStarted = 0;
		this.isPaused = false;
		// Awaken the main loop.
		notify();
	}

	/**
	 * If the execution is finished, the buttons will be disables and the
	 * machine state refreshed; the method then return true. It returns
	 * false otherwise.
	 * @return true, if the execution is finished, false otherwise.
	 */
	private boolean getExecutionFinished() {
		// Continue only if the virtual machine executed by the Application has not changed.
		if (!this.application.getVmIsInitializing()) {
			// Find out, if the execution has finished.
			if (this.application.getExecutionFinished()) {
				// Finalize the execution.
				this.isPaused = false;
				this.isStarted = false;

				// Signalize to the composite that it does not need to offer the ability to abort any more.
				this.executionComposite.executionFinishedByExecutionRunner();

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

		// Refresh the information.
		this.abortionMessage = "The execution was aborted manually.";
		refreshExecutionWindow(true, true);

		// Signalize to the window that the execution is finished.
		this.executionComposite.executionFinishedByExecutionRunner();

		// Stop this runner.
		this.abortedManually = true;
		this.isStarted = false;
		this.isPaused = false; // Execution might be in waiting state. So waiting is ended as this.isPaused is false at the next polling and the thread will terminate.
		// If this thread is not terminated or was never run, notify it.
		if (this.getState() != java.lang.Thread.State.TERMINATED && this.getState() != java.lang.Thread.State.NEW) {
			notify();
		}
	}

	/**
	 * If an Application has been initialized, invoke its method
	 * to abort the execution but do not display a message about the
	 * abortion. If the execution mode was symbolical, try to generate
	 * test cases.
	 */
	public synchronized void stopExecutionDueToLimitations() {
		// Stop the Application.
		if (this.application != null) this.application.abortExecution();

		// Refresh the information.
		this.abortionMessage = "The execution was aborted manually.";

		// Signalize to the window that the execution is finished.
		this.executionComposite.executionFinishedByExecutionRunner();

		// Stop this runner.
		this.isStarted = false;
		this.isPaused = false; // Execution might be in waiting state. So waiting is ended as this.isPaused is false at the next polling and the thread will terminate.
		// If this thread is not terminated or was never run, notify it.
		if (this.getState() != java.lang.Thread.State.TERMINATED && this.getState() != java.lang.Thread.State.NEW) {
			notify();
		}
	}

	/**
	 * Getter for the Application.
	 * @return The Application generated by this ExectionRunner.
	 */
	public Application getApplication() {
		return this.application;
	}

	/**
	 * Getter for the field indicating the milliseconds after which the ExecutionWindows will be refreshed.
	 * @param refreshEvery The miliseconds after which the ExecutionWindows will be refreshed.
	 */
	public synchronized void setRefreshEvery(int refreshEvery) {
		this.refreshEvery = refreshEvery;
		// Interrupt the current waiting for the next refresh - do not do this if we are waiting as the execution is paused!
		if (!this.isPaused) interrupt();
	}

	/**
	 * Getter for isPaused.
	 * @return true, if the execution is paused, false otherwise.
	 */
	public boolean isPaused() {
		return this.isPaused;
	}

	/**
	 * Finalize the ExecutionRunner by finalizing the Application.
	 */
	@Override
	public void finalize() {
		try {
			// Finalize and clean up the Application.
			if (this.application != null) {
				synchronized (this.application) {
					boolean forceCleanup = false;
					try {
						/*
						 * Force cleanup if an error occurred. Some errors or special circumstances might
						 * have the finalizer of the Application instance run and hence the cleanup
						 * invoked. This might be done too early, though, leaving much memory occupied.
						 * Running the clean up again will have great effect in that cases.
						 */
						forceCleanup = this.application.getVirtualMachine().errorOccured();
					} catch (NullPointerException e) {
						/*
						 * There is no virtual machine present any more. This means that the
						 * finalizer has been run already or there as another serious problem.
						 * It hence is a good idea to run cleanup again.
						 */
						forceCleanup = true;
					}
					this.application.cleanUp(forceCleanup);
					this.application = null;
				}
			}
		} finally {
			try {
				super.finalize();
			} catch (Throwable t) {
				// Log it, but do nothing.
				if (Globals.getInst().guiLogger.isEnabledFor(Level.WARN))
					Globals.getInst().guiLogger.warn("Finalizing the execution runner failed.");
			}
		}
	}

}
