package de.wwu.muggl.vm;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.util.StaticStringFormatter;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.logic.LogicVirtualMachine;
import de.wwu.muggl.vm.impl.real.RealVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * An Application is the top level element of any execution. it holds the reference to the
 * virtual machine and the class loader. It will start the execution in a new thread and
 * provide access to the results of the execution.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-15
 */
public class Application extends Thread {
	private MugglClassLoader classLoader;
	private VirtualMachine virtualMachine;
	private boolean executionFinished = false;
	private boolean vmHasChanged = false;
	private boolean vmIsInitalizing = true;
	private boolean finalized;
	private boolean cleanedUp;

	/**
	 * Basic constructor.
	 * @param classLoader The main classLoader to use.
	 * @param initialClassName The class that is to be executed initially.
	 * @param method The method that is to be executed initially. It must be a method of the class initialClassName.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws InitializationException If initialization of auxiliary classes fails.
	 */
	public Application(
			MugglClassLoader classLoader,
			String initialClassName,
			Method method
			) throws ClassFileException, InitializationException {
		this.classLoader = classLoader;
		ClassFile classFile = this.classLoader.getClassAsClassFile(initialClassName);
		if (Options.getInst().symbolicMode) {
			if (Options.getInst().logicMode) {
				this.virtualMachine = new LogicVirtualMachine(this, this.classLoader, classFile, method);
			} else if(Options.getInst().jpaMode) {
				this.virtualMachine = new JPAVirtualMachine(this, this.classLoader, classFile, method);
			} else {
				this.virtualMachine = new SymbolicVirtualMachine(this, this.classLoader, classFile, method);
			}
		} else {
			this.virtualMachine = new RealVirtualMachine(this, this.classLoader, classFile, method);
		}
		this.finalized = false;
		this.cleanedUp = false;
		if (Globals.getInst().logger.isDebugEnabled())
			Globals.getInst().logger.debug("New application set up successfully.");
	}

	/**
	 * Basic constructor for initialization without an existing class loader.
	 * @param classPathEntries A String array of class path entries.
	 * @param initialClassName The class that is to be executed initially.
	 * @param initialMethodNumber The methods number in the class file that is to be executed first.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws InitializationException If initialization of auxiliary classes fails.
	 */
	public Application(
			String[] classPathEntries,
			String initialClassName,
			int initialMethodNumber
			) throws ClassFileException, InitializationException {
		this.classLoader = new MugglClassLoader(classPathEntries);
		ClassFile classFile = this.classLoader.getClassAsClassFile(initialClassName);
		if (Options.getInst().symbolicMode) {
			if (Options.getInst().logicMode) {
				this.virtualMachine = new LogicVirtualMachine(this, this.classLoader, classFile, classFile.getMethods()[initialMethodNumber]);
			} else if(Options.getInst().jpaMode) {
				this.virtualMachine = new JPAVirtualMachine(this, this.classLoader, classFile, classFile.getMethods()[initialMethodNumber]);
			} else {
				this.virtualMachine = new SymbolicVirtualMachine(this, this.classLoader, classFile, classFile.getMethods()[initialMethodNumber]);
			}
		} else {
			this.virtualMachine = new RealVirtualMachine(this, this.classLoader, classFile, classFile.getMethods()[initialMethodNumber]);
		}
		this.finalized = false;
		this.cleanedUp = false;
		if (Globals.getInst().logger.isDebugEnabled())
			Globals.getInst().logger.debug("New application set up successfully.");
	}

	/**
	 * Starts execution within the initialized virtual machine. If the virtual machine has
	 * finished, check if there is a new instance of it waiting for execution.
	 */
	@Override
	public void run() {
		do {
			this.vmHasChanged = false;
			if (this.vmHasChanged && Globals.getInst().execLogger.isInfoEnabled()) Globals.getInst().execLogger.info("Starting the next virtual machine...");
			this.virtualMachine.start();
			this.virtualMachine.executeNextStep();
			// Polling: When to continue?
			try {
				synchronized (this) {
					while (!this.vmHasChanged) {
						wait();
					}
				}
				this.executionFinished = false;
			}  catch (InterruptedException e) {
				// Just log it.
				if (!this.finalized) {
					if (Globals.getInst().execLogger.isInfoEnabled())
						Globals.getInst().execLogger
								.info("The execution process failed with an InterruptedException as the Application was finalized.");
				} else {
					if (Globals.getInst().execLogger.isInfoEnabled())
						Globals.getInst().execLogger
								.info("Waiting for the next virtual machine to start failed with an InterruptedException (probably the execution was aborted). Now checking if another virtual machine is ready, stopping otherwise.");
				}
			}
		} while (this.vmHasChanged);
		this.executionFinished = true;
	}

	/**
	 * Aborts the execution in the virtual machine. This will only have consequences if the step by
	 * step mode is enabled.
	 *
	 * @throws NullPointerException If cleanup has already been run and there is no more virtual
	 *         machine present.
	 */
	public void abortExecution() {
		this.virtualMachine.interrupt();
		this.virtualMachine.finalize();
		finalizeApplication();
	}

	/**
	 * Getter for executionFinished.
	 * @return true, if the execution has finished, false otherwise.
	 */
	public boolean getExecutionFinished() {
		return this.executionFinished;
	}

	/**
	 * Set executionFinished to true.
	 */
	public void executionHasFinished() {
		this.executionFinished = true;
	}

	/**
	 * Getter for the errorOccured field.
	 *
	 * @return true, if an error has occurred during execution, false otherwise.
	 * @throws NullPointerException
	 *         If cleanup has already been run and there is no more virtual machine present.
	 */
	public boolean errorOccured() {
		return this.virtualMachine.errorOccured();
	}

	/**
	 * Fetch the error message generated by the execution, if there was any.
	 *
	 * @return The error message.
	 * @throws NullPointerException
	 *         If cleanup has already been run and there is no more virtual machine present.
	 */
	public String fetchError() {
		return this.virtualMachine.getErrorMessage();
	}

	/**
	 * Getter for the returned object.
	 *
	 * @return The returned object from an execution.
	 * @throws NullPointerException
	 *         If cleanup has already been run and there is no more virtual machine present.
	 */
	public Object getReturnedObject() {
		return this.virtualMachine.getReturnedObject();
	}

	/**
	 * Getter for the information whether a value was returned, or not.
	 *
	 * @return true, if a value was returned, false otherwise.
	 * @throws NullPointerException
	 *         If cleanup has already been run and there is no more virtual machine present.
	 */
	public boolean getHasAReturnValue() {
		return this.virtualMachine.getHasAReturnValue();
	}

	/**
	 * Getter for the information whether the return value can be treated
	 * as the actual return value of the method's execution (false), or if
	 * execution the method has resulted in an uncaught Exception being thrown (true).
	 *
	 * @return true, if the method threw an uncaught exception, false otherwise.
	 * @throws NullPointerException
	 *         If cleanup has already been run and there is no more virtual machine present.
	 */
	public boolean getThrewAnUncaughtException() {
		return this.virtualMachine.getThrewAnUncaughtException();
	}

	/**
	 * Getter for the virtual machine.
	 * @return The VirtualMachine.
	 */
	public VirtualMachine getVirtualMachine() {
		return this.virtualMachine;
	}

	/**
	 * Setter for the virtual machine. This will abort execution in the
	 * virtual machine currently running.
	 *
	 * @param vm The new VirtualMachine.
	 * @throws NullPointerException
	 *         If cleanup has already been run and there is no more virtual machine present.
	 */
	public synchronized void setVirtualMachine(VirtualMachine vm) {
		// Abort execution in the currently running vm and finalize it.
		this.virtualMachine.interrupt();
		this.virtualMachine.finalize();
		// Mark that a new vm is initializing and that the vm has changed.
		this.vmIsInitalizing = true;
		this.vmHasChanged = true;
		// Set the vm and notify me.
		this.virtualMachine = vm;
		notify();
	}

	/**
	 * Getter for vmIsInitalizing. It will return false if this Application
	 * has been finalized.
	 * @return true, if the vm has not yet been fully initialized and is not finalized, false otherwise.
	 */
	public boolean getVmIsInitializing() {
		if (this.finalized) return false;
		return this.vmIsInitalizing;
	}

	/**
	 * Set that a new vm has been initialized.
	 */
	public void newVMHasBeenInitialized() {
		this.vmIsInitalizing = false;
	}

	/**
	 * Finalize the application if that has not been done before and clean up its memory footprint
	 * after doing so.
	 *
	 * @param forceCleanup
	 *        If set to true, a cleanup will be done even if this method has already been run.
	 */
	public synchronized void cleanUp(boolean forceCleanup) {
		if (!this.cleanedUp || forceCleanup) {
			if (!this.finalized) finalizeApplication();

			// Cleaning up the memory.
			Runtime runtime = Runtime.getRuntime();
			long freeMemory = runtime.freeMemory();

			// Logging.
			String forcedString = "";
			if (this.cleanedUp) {
				forcedString = " Clean up was ran already but is forced to be done again.";
			}
			if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
				Globals.getInst().execLogger.info("Execution of an application is finished. Free memory: "
								+ StaticStringFormatter.formatByteValue(freeMemory) + " of "
								+ StaticStringFormatter.formatByteValue(runtime.maxMemory())
								+ ". " + forcedString  + "Cleaning up...");

			// Remove any references.
			this.classLoader = null;
			this.virtualMachine = null;

			// Run finalization.
			System.runFinalization();

			// It's a good time to run garbage collection now.
			runtime.gc();
			this.cleanedUp = true;

			// Logging.
			if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
				Globals.getInst().execLogger.info(
					"Cleanup finished. Freed "
					+ StaticStringFormatter.formatByteValue(runtime.freeMemory() - freeMemory) + ".");
		}
	}

	/**
	 * Finalize execution in this application.
	 */
	public synchronized void finalizeApplication() {
		if (!this.finalized) {
			this.finalized = true;
			this.executionFinished = true;
			this.interrupt();

			// Force the ClassLoader to drop any cached classes, resulting in freeing a lot of ressources.
			this.classLoader.resetInitializedClassFileCache();

			// Undo optimizations in loaded classes.
			this.classLoader.undoOptimizations();
		}
	}

	/**
	 * Finalize and clean up this application if that has not been done, yet.
	 */
	@Override
	public synchronized void finalize() {
		try {
			if (!this.cleanedUp) cleanUp(false);
		} finally {
			try {
				super.finalize();
			} catch (Throwable t) {
				// Log it, but do nothing.
				if (Globals.getInst().execLogger.isEnabledFor(Level.WARN))
					Globals.getInst().execLogger.warn("Finalizing the application failed.");
			}
		}
	}

}
