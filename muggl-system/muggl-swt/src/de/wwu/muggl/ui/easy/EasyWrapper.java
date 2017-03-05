package de.wwu.muggl.ui.easy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Level;

import de.wwu.muggl.common.TimeSupport;
import de.wwu.muggl.configuration.ConfigReader;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The EasyWrapper is an interface for the EASy system but could also be used by other third party
 * tools. It is initialized with a class file and the name of the method. This method can be
 * executed with Muggl, resulting in the generation of a JUnit test case file. The content of this
 * file can be retrieved.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-01-05
 */
public final class EasyWrapper extends Thread {
	private static final int BUFFER_BYTES = 1024;
	private Application application;
	private long timeStarted;
	private int maximumExecutionTime;
	
	private MugglClassLoader classLoader;
	private ClassFile initialClassFile;
	private Method initialMethod;
	private boolean errorOccured;
	private String errorMessage;
	
	/**
	 * Initialize the wrapper.<br />
	 * <br />
	 * Be sure to use {@link #cleanUp()} when you do not need this object any more.
	 * 
	 * @param classFile A file of type class.
	 * @param methodName A method of that class.
	 * @param configFile The configuration file to use. Might be null in order to indicate that the
	 *        standard configuration file should be used.
	 *@param maximumExecutionTime Maximum execution time in seconds. -1 disables it.
	 * @throws ClassFileException On fatal errors parsing the specified class file or another
	 *         required class file.
	 * @throws FileNotFoundException If the specified file was not found.
	 * @throws IOException On error reading the specified class file.
	 */
	public EasyWrapper(File classFile, String methodName, File configFile, int maximumExecutionTime)
			throws ClassFileException, FileNotFoundException, IOException {
		this.maximumExecutionTime = maximumExecutionTime;
		this.errorOccured = false;
		
		if (configFile == null) {
			// Proceed with the standard configuration file.
			ConfigReader.loadConfig(false);
		} else {
			// Set the configuration file and load the configuration.
			Options.getInst().configurationFile = configFile.getPath();
			ConfigReader.loadConfig(false, new FileReader(configFile));
		}
		
		// Set logging level to warn.
		Globals.getInst().changeLogLevel(Level.WARN);
		
		// Use the class' directory as the class path.
		String path = classFile.getAbsolutePath();
		Options.getInst().classPathEntries.add(path.substring(0, path.indexOf(classFile.getName())));
		
		// Set up class loader and load the class file.
		this.classLoader = new MugglClassLoader(StaticGuiSupport.arrayList2StringArray(Options.getInst().classPathEntries));
		try {
			this.initialClassFile = new ClassFile(this.classLoader, new FileInputStream(classFile),
					new FileInputStream(classFile), classFile.length(), classFile.getPath());
		} catch (ClassFileException e) {
			throw new ClassFileException(
					"The supplied class file seems to be broken. Parsing it was impossible.");
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(
					"Could not parse the supplied class file due to an FileNotFoundException.");
		} catch (IOException e) {
			throw new IOException("Could not parse the supplied class file due to an IOException.");
		}
		
		// Determine the methods.
		this.initialMethod = null;
		for (Method method : this.initialClassFile.getMethods()) {
			if (method.getName().equals(methodName)) {
				if (this.initialMethod != null) {
					throw new IllegalArgumentException(
							"There are mutiple methods by the name specified."
									+ " This is not supported by this simple wrapper.");
				}
				this.initialMethod = method;
				break; // just take the first found method
			}
		}
		if (this.initialMethod == null) {
			throw new IllegalArgumentException("Method name not found.");
		}
		
		// TODO Set a generator provider.
		//this.initialMethod.setArrayElementsGeneratorProvider(0,
		//		new RandomConstantIntegerArrayElementsGeneratorProvider());
	}
	
	/**
	 * Start the execution. To fork it in a thread of its own, which is highly recommended, use start().
	 */
	@Override
	public void run() {
		// Set up the application.
		try {
			this.application = new Application(this.classLoader, this.initialClassFile
					.getClassName(), this.initialMethod);
		} catch (ClassFileException e) {
			this.errorMessage = "Parsing of a required class failed.";
			this.errorOccured = true;
			return;
		} catch (InitializationException e) {
			this.errorMessage = "Initializing a required object failed.";
			this.errorOccured = true;
			return;
		}
		
		this.timeStarted = System.currentTimeMillis();
		this.application.start();
		
		// Monitor execution.
		while (!isFinished()) {
			// Check if the maximum execution time has been reached.
			if (this.maximumExecutionTime != -1) {
				if (System.currentTimeMillis() - this.timeStarted > this.maximumExecutionTime * TimeSupport.MILLIS_SECOND) {
					((SymbolicVirtualMachine) this.application.getVirtualMachine()).setAbortionCriterionMatched(true);
					((SymbolicVirtualMachine) this.application.getVirtualMachine()).setAbortionCriterionMatchedMessage("The time limit has been reached.");

					// Stop the execution.
					if (this.application != null) this.application.abortExecution();
					break;
				}
			}
			
			// Sleep.
			try {
				Thread.sleep(Globals.SAFETY_SLEEP_DELAY);
			} catch (InterruptedException e){
				// Do nothing.
			}
		}
	}
	
	/**
	 * Check whether execution is finished. This method is intended to be polled recurringly.
	 *
	 * @return true, if execution is finished; false otherwise.
	 */
	public boolean isFinished() {
		// If an error occurred there is nothing more to do.
		if (this.errorOccured) {
			return true;
		}
		
		// The only reason for application to be null is that it is not yet initialized.
		if (this.application == null) {
			return false;
		}
		
		return this.application.getExecutionFinished();
	}

	/**
	 * Check whether execution was successful.<br />
	 * <br />
	 * Further information about why execution failed might be available from the log file. However,
	 * debugging will be almost impossible using this wrapper only. Please contact the Muggl author.
	 * 
	 * @return true, if execution is finished and was successful; false otherwise.
	 * @throws IllegalStateException If execution is not finished.
	 */
	public boolean wasSuccessful() {
		if (!isFinished()) {
			throw new IllegalStateException("Execution is not finished.");
		}
		
		return this.application.getExecutionFinished() && !this.application.errorOccured();
	}
	
	/**
	 * Find out whether an error has occurred.
	 *
	 * @return true, if an error has occurred; false otherwise.
	 */
	public boolean hasErrorOccured() {
		return this.errorOccured;
	}
	
	/**
	 * Get the message of an error.
	 *
	 * @return The message of an error.
	 * @throws IllegalStateException If no error occurred.
	 */
	public String getErrorMessage() {
		if (!this.errorOccured) {
			throw new IllegalStateException("No error occured!");
		}
		return this.errorMessage;
	}
	
	/**
	 * Get the generated test case file. This method returns a File instance to a JUnit test case file.
	 *
	 * @return A File instance to the generated test case.
	 * @throws IllegalStateException If execution is not yet finished or if no test case file was generated.
	 */
	public File getTestCaseFile() {
		if (!wasSuccessful()) {
			throw new IllegalStateException("Execution was not successful");
		}
		
		String classFilePath = ((SymbolicVirtualMachine) this.application.getVirtualMachine())
				.getSolutionProcessor().getGeneratedClassFilePath();

		if (classFilePath == null) {
			throw new IllegalStateException("No test case file generated");
		}
		return new File(classFilePath);
	}

	/**
	 * Get the generated test case. This method returns the source code of a JUnit test case file.
	 * It can be saved and compiled.
	 * 
	 * @return The generated test case's source code as a String.
	 * @throws IOException On problems reading the corresponding test case file.
	 * @throws IllegalStateException If execution is not yet finished or if no test case file was generated.
	 */
	public String getTestCase() throws IOException {
		return fileToString(getTestCaseFile());
	}

	/**
	 * Clean up Muggl. Using this method is recommended to clean up Muggl's memory footprint. It
	 * will eventually be cleaned by the garbage collector. This could however take a while due to
	 * the complexity of the structures created. Moreover, execution might use very much memory;
	 * cleaning it up speeds up further processing.
	 */
	public void cleanUp() {
		if (this.application != null) {
			this.application.cleanUp(true);
		}
	}
	
	/**
	 * Internal method to read a file into a String.
	 *
	 * @param fileIn The file to read.
	 * @return The file contents as a String.
	 * @throws IOException On fatal problems reading the file.
	 */
	private static String fileToString(File fileIn) throws IOException {
		StringBuilder buffer = new StringBuilder(BUFFER_BYTES);
		BufferedReader reader = new BufferedReader(new FileReader(fileIn));

		char[] chars = new char[BUFFER_BYTES];
		for (int bytes = reader.read(chars); bytes > -1; bytes = reader.read(chars)) {
			buffer.append(String.valueOf(chars), 0, bytes);
		}

		reader.close();
		return buffer.toString();
	}
	
	/**
	 * Test methods that demonstrates the usage of the EasyWrapper.
	 *
	 * @param args Ignored command line arguments.
	 */
	public static void main(String... args) {
		// File and methods.
		File file = new File("E:\\Daten\\Uni-Arbeit\\Dissertation\\Muggl-SVN\\bin\\test\\papers\\Paper200809.class");
		String methodName = "binSearch";
		
		// Initialize the wrapper.
		EasyWrapper wrapper;
		try {
			// You do not necessarily specified a configuration file.
			wrapper = new EasyWrapper(file, methodName, null, 30);
		} catch (Exception e) {
			/*
			 * You should catch all exception typed independently. However, if you do not specified
			 * a corrupt file, initialization should bear no problems.
			 */
			e.printStackTrace();
			return;
		}
		
		// Start and keep polling.
		wrapper.start();
		while (!wrapper.isFinished()) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// This should not happen. However, it is always a good idea to catch this exception.
				e.printStackTrace();
				wrapper.cleanUp();
				return;
			}
		}
		
		// Reaching this point, we can either retrieve the test case or execution failed.
		if (wrapper.wasSuccessful()) {
			try {
				// Do what ever you want to do!
				System.out.println(wrapper.getTestCase());
			} catch (IOException e) {
				// This is pretty unlikely. Still catch it.
				e.printStackTrace();
			}
		} else {
			// This also is unlikely, but might happen. Especially as long as Muggl is unfinished software.
			System.out.println("argl!");
		}
		
		// Clean up in order to have free ressources immediately.
		wrapper.cleanUp();
	}
	
}
