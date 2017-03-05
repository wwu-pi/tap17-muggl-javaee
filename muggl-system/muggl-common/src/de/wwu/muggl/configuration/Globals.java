package de.wwu.muggl.configuration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.CountingFileAppender;
import org.apache.log4j.HTMLLayoutEscapeOption;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Singleton class that holds global constants and some variables that are used in the application
 * for configuration purposes. Most of them refer to either logging, the design of GUI windows or
 * version information.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-18
 */
public final class Globals {	
	// The constants.
	/**
	 * The application's base directory.
	 */
	public static final String BASE_DIRECTORY;
	/**
	 * The application's jar file.
	 */
	public static final String JAR_FILE_NAME;
	/**
	 * The application's name.
	 */
	public static final String APP_NAME;
	/**
	 * The String any window title begins with.
	 */
	public static final String WINDOWS_TITLE;
	/**
	 * The String that connects the general window to the specific one.
	 */
	public static final String WINDOWS_TITLE_CONNECTOR;
	/**
	 * The major version number.
	 */
	public static final String VERSION_MAJOR;
	/**
	 * The minor version number.
	 */
	public static final String VERSION_MINOR;
	/**
	 * The version release information.
	 */
	public static final String VERSION_RELEASE;
	/**
	 * The version's release state.
	 */
	public static final String VERSION_STATUS;
	/**
	 * The log file currently used.
	 */
	public String currentLogfile;
	/**
	 * Base delay for an invocation of sleep() in milliseconds. At some point, a delay is needed due to threading.
	 */
	public static final int SAFETY_SLEEP_DELAY;
	/**
	 * Smaller base delay for an invocation of sleep() in milliseconds. At some point, a delay is needed due to threading.
	 */
	public static final int REDUCE_SAFETY_SLEEP_DELAY;
	/**
	 * The minimum sleep time between the execution of two steps in the step by step GUI.
	 */
	public static final int MIN_SLEEP_BETWEEN_STEPS;
	/**
	 * The message displayed if the application runs short of memory.
	 */
	public static final String OUTOFMEMORYERROR_MESSAGE;
	/**
	 * Maximum sleep time for UI outputs in milliseconds.
	 */
	public static final int MAX_SLEEPING_SLICE;

	// Logger.
	/**
	 * General Logger:
	 * FATAL: Condition that leads to the halting of the application.
	 * ERROR: Condition that leads to the halting of parts of the application, while continuing is possible.
	 * WARN: Something unexpected happened, but continuing is possible.
	 * INFO: General information, probably success in finishing some task.
	 * DEBUG: Detailed information about what will be or was done.
	 * TRACE: Very detailed information about what will be or was done.
	 */
	public final Logger logger;
	/**
	 * Execution Logger:
	 * FATAL: Error in the virtual machine which forces halting of the application.
	 * ERROR: Error in the virtual machine which forces halting of the virtual machine.
	 * WARN: Unexpected condition which needs handling (possibly resulting in a halting of the virtual machine).
	 * INFO: General information, probably success in finishing some task.
	 * DEBUG: Detailed information about what will be or was done.
	 * TRACE: Very detailed information about what will be or was done.
	 */
	public final Logger execLogger;
	/**
	 * GUI Logger:
	 * FATAL: Error on the GUI which forces halting of the application.
	 * ERROR: Error or Exceptions on the GUI which forces the closing of a window.
	 * WARN: Not used by this logger.
	 * INFO: General information about GUI events.
	 * DEBUG: Not used by this logger.
	 * TRACE: Not used by this logger.
	 */
	public final Logger guiLogger;
	/**
	 * Solver Logger:
	 * FATAL: Not used by this logger.
	 * ERROR: Not used by this logger.
	 * WARN: Solving failed for an unexpected reason.
	 * INFO: General information about solving events.
	 * DEBUG: Detailed information about the solvers work.
	 * TRACE: Really detailed information about the solvers work.
	 */
	public final Logger solverLogger;
	
	/**
	 * Symbolic Execution Logger:
	 * FATAL: Unexpected error that shuldn't occur
	 * ERROR: Not used
	 * WARN: Not used
	 * INFO: Not used
	 * DEBUG: Detailed information about the use of the debugger
	 * TRACE: Very detailed information about steps being taken by the JacopSolver
	 */
	public final Logger jacopLogger;

	/**
	 * Symbolic Execution Logger:
	 * FATAL: Error in the symbolic execution which forces halting of the application.
	 * ERROR: Error in the symbolic execution which forces halting of the execution.
	 * WARN: Unexpected condition which might lead to results being blurred.
	 * INFO: General information about steps being taken for the symbolic execution.
	 * DEBUG: Detailed information about steps being taken for or conclusions gained by the symbolic execution.
	 * TRACE: Very detailed information about steps being taken for or conclusions gained by the symbolic execution.
	 */
	public final Logger symbolicExecLogger;

	// Private logging fields.
	private String staticLogFileNamePartBeginning;
	private String staticLogFileNamePartEnd;
	private String staticLogFileNamePartExtension;
	private long logFileNumber;
	private long logFileSubNumber;
	private CountingFileAppender fileAppender;
	private final Vector<Logger> loggers;
	
	// Static initialization.
	static {
		APP_NAME = "Muggl";
		JAR_FILE_NAME = "muggl.jar";
		WINDOWS_TITLE = "Muggl";
		WINDOWS_TITLE_CONNECTOR = " - ";
		VERSION_MAJOR = "1";
		VERSION_MINOR = "00";
		VERSION_RELEASE = "Alpha (unreleased)";
		VERSION_STATUS = " unstable / experimental";

		SAFETY_SLEEP_DELAY = 50;
		REDUCE_SAFETY_SLEEP_DELAY = 1;
		MIN_SLEEP_BETWEEN_STEPS = 10;
		OUTOFMEMORYERROR_MESSAGE = "Most likely, the class file you are trying to load is too big for beeing displayed, since visual elements take a lot of memory. "
				+ "There are two things you can try to solve this problem:\n"
				+ "1. Increase the memory to be used by the java virtual machine. Refer to the manual for further information how to increase this value.\n"
				+ "2. Try using the class file without graphical support, i.e. do not use the file inspection window and the step by step gui.\n\n"
				+ "Even on machines with little memory "
				+ APP_NAME
				+ " should be able to process files properly, since the non-graphical execution engine has been optimized to generate a admissible memory footprint only.";
		MAX_SLEEPING_SLICE = 500;
		
		BASE_DIRECTORY = System.getProperties().getProperty("user.dir").replace("\\", "/");
	}
	
	// Singleton
	private static final Globals GLOBALS = new Globals();

	/**
	 * Private Constructor.
	 */
	private Globals() {
		// Logging setup.
		this.currentLogfile = getLogfileName();

		// The loggers.
		this.logger = Logger.getLogger(APP_NAME + " general");
		this.execLogger = Logger.getLogger(APP_NAME + " execution");
		this.guiLogger = Logger.getLogger(APP_NAME + " gui");
		this.solverLogger = Logger.getLogger(APP_NAME + " solver");
		this.symbolicExecLogger = Logger.getLogger(APP_NAME + " symbolic execution");
		this.jacopLogger = Logger.getLogger(APP_NAME + " JaCoP solver");

		// Finally start logging.
		try {
			this.fileAppender = new CountingFileAppender(getLayout(), this.currentLogfile, true);
			this.fileAppender.setMaximumEventsToLog(Options.getInst().maximumLogEntries);
			this.fileAppender.setName("File appender (to " + this.currentLogfile + ")");

			this.logger.addAppender(this.fileAppender);
			this.execLogger.addAppender(this.fileAppender);
			this.guiLogger.addAppender(this.fileAppender);
			this.solverLogger.addAppender(this.fileAppender);
			this.symbolicExecLogger.addAppender(this.fileAppender);
			this.jacopLogger.addAppender(this.fileAppender);
		} catch (IOException e) {
			System.out.println("Fatal error: Could not initialize logging due to an I/O error. Halting.");
			System.exit(1);
		}

		// Set up the Vector of loggers.
		this.loggers = new Vector<Logger>();
		this.loggers.add(this.logger);
		this.loggers.add(this.execLogger);
		this.loggers.add(this.guiLogger);
		this.loggers.add(this.solverLogger);
		this.loggers.add(this.symbolicExecLogger);
		this.loggers.add(this.jacopLogger);

		// Set the basic level for logging.
		Iterator<Logger> iterator = this.loggers.iterator();
		while (iterator.hasNext()) {
			iterator.next().setLevel(Level.INFO); 
		}
		
		// Finished
		if (this.logger.isInfoEnabled())
			this.logger.info("Logging started. Current logging level is " + this.logger.getLevel().toString());
	}

	/**
	 * Getting the only instance of this class.
	 * @return The only instance of this class.
	 */
	public static Globals getInst() {
		return GLOBALS;
	}

	/**
	 * Setter for the log level. This Method will alter the value in the Constants class.
	 * Do NOT call this method from within the private constructor (it will attempt to
	 * generate logging output and hence request the logger which will lead to an
	 * initialization error).
	 * @param level The new log level.
	 */
	public void changeLogLevel(Level level) {
		Iterator<Logger> iterator = this.loggers.iterator();
		while (iterator.hasNext()) {
			iterator.next().setLevel(level);
		}
		if (Globals.getInst().logger.isInfoEnabled())
			Globals.getInst().logger.info("Changed logging level to " + level.toString());
	}

	/**
	 * Return the currently set logging level. As it is not encouraged to change the level for
	 * single loggers, getting the level of the main logger should be suitable in most cases.
	 * @return The Level of the main logger.
	 */
	public Level getLoggingLevel() {
		return this.logger.getLevel();
	}

	/**
	 * Open a new logfile and apply the currently set Layout to its appender.
	 */
	public void changeLoggingLayout() {
		if (Options.getInst().getHtmlLogging()) {
			this.staticLogFileNamePartExtension = "html";
		} else {
			this.staticLogFileNamePartExtension = "txt";
		}
		continueWithNewLogfile(true);
	}

	/**
	 * Try to generate the name for the new log file and open it. If this fails, logging stops. If it
	 * succeeds, a message is written to the current log file. New entries will be written to the new
	 * log file, which becomes the current one.
	 *
	 * @param changeLayout If set to true, a the Layout of the appender will be changed.
	 */
	public void continueWithNewLogfile(boolean changeLayout) {
		if (this.logFileSubNumber == Long.MAX_VALUE - 1) {
			this.logger.error("Logging stops. A new log file should have been started accordingly to the setting for the maxmimum number of lines per logfile, but no name could be taken due to the enormous logfile count.");
			this.fileAppender.stopLogging();
			return;
		}
		this.logFileSubNumber++;
		String newLogfile = this.staticLogFileNamePartBeginning + expandNumberRepresentation(this.logFileNumber) + "-" + expandNumberRepresentation(this.logFileSubNumber) + this.staticLogFileNamePartEnd + this.staticLogFileNamePartExtension;
		// First check if the new file name can be used.
		if (new File(newLogfile).isFile()) {
			this.logger.error("Logging stops. A new log file should have been started accordingly to the setting for the maxmimum number of lines per logfile, but the next suitable filename was already used. This is unexpected. To avoid confusion when reading the logfiles, no new file will be created.");
			this.fileAppender.stopLogging();
			return;
		}

		// Log an info entry and then close the log file.
		this.logger.info("Logging is continued in the file " + newLogfile + ".");
		this.fileAppender.writeFooter();

		// Change the layout if this is desired.
		if (changeLayout) {
			this.fileAppender.setLayout(getLayout());
		}

		// Set the new log file.
		this.fileAppender.setFile(newLogfile);
		this.fileAppender.activateOptions();
		this.currentLogfile = newLogfile;
	}

	/**
	 * Set up the log file. The next available name for the log file will be found and chosen.
	 *
	 * @return The name for the log file.
	 */
	private String getLogfileName() {
		// Initialization.
		this.logFileNumber = 1;
		this.logFileSubNumber = 1;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

		// Get the static parts of a logfile's name.
		this.staticLogFileNamePartBeginning = BASE_DIRECTORY + "/log/" + dateFormat.format(new Date()) + "-";
		this.staticLogFileNamePartEnd = "-Muggl.";
		if (Options.getInst().getHtmlLogging()) {
			this.staticLogFileNamePartExtension = "html";
		} else {
			this.staticLogFileNamePartExtension = "txt";
		}

		// Generate the log file's name.
		String currentLogfile = this.staticLogFileNamePartBeginning + expandNumberRepresentation(this.logFileNumber) + "-" + expandNumberRepresentation(this.logFileSubNumber) + this.staticLogFileNamePartEnd + this.staticLogFileNamePartExtension;
		// Check if this log file already exists.
		if (new File(currentLogfile).isFile()) {
			for (long a = 1; a < Long.MAX_VALUE; a++) {
				// Finding the appropriate log file name.
				currentLogfile = this.staticLogFileNamePartBeginning + expandNumberRepresentation(a) + "-" + expandNumberRepresentation(this.logFileSubNumber) + this.staticLogFileNamePartEnd + this.staticLogFileNamePartExtension;
				if (!new File(currentLogfile).isFile())  {
					this.logFileNumber = a;
					break;
				}
				if (a == Long.MAX_VALUE - 1) {
					System.out.println("Fatal error: Could not initialize logging due to an enormous logfile count. Please clear the /log-directory. Halting.");
					System.exit(1);
				}
			}
		}
		return currentLogfile;
	}

	/**
	 * Add leading zeros to any String representation of a long that is shorter
	 * than 10 digits.
	 *
	 * If long values become really large, they will take more than ten digits.
	 * However, the purpose of this method is the usage for the file generation.
	 * Having more than 1.000.000.000 files in a directory would be insane anyway.
	 *
	 * @param number The long to get the (probably expanded) String representation off.
	 * @return The (probably expanded) String representation.
	 */
	private String expandNumberRepresentation(long number) {
		String numberString = String.valueOf(number);
		while (numberString.length() < 10) {
			numberString = "0" + numberString;
		}
		return numberString;
	}

	/**
	 * Get the Layout for the appender. Based on the Options setting, either
	 * a HTMLLayout (with the character escape option) or a PatternLayout
	 * is returned.
	 * @return The Layout for the appender.
	 */
	private Layout getLayout() {
		Layout layout;
		if (Options.getInst().getHtmlLogging()) {
			layout = new HTMLLayoutEscapeOption();
			((HTMLLayoutEscapeOption) layout).setTitle(APP_NAME + " Logfile");
		} else {
			 layout = new PatternLayout();
			 ((PatternLayout) layout).setConversionPattern("%d{ABSOLUTE} %5p %c{1}:%L - %m%n");
		}
		return layout;
	}

	/**
	 * If the logging format is HTML, toggle the escaping of values. If HTML logging is disabled
	 * it will have no effect, especially it will NOT be changed.
	 * @see org.apache.log4j.HTMLLayoutEscapeOption
	 * @param value If set to true, message escaping will be enabled, otherwise it will be disabled.
	 */
	public void setLoggingMessagesEscaping(boolean value) {
		if (Options.getInst().getHtmlLogging()) {
			HTMLLayoutEscapeOption.escapeMessages = value;
		}
	}

}
