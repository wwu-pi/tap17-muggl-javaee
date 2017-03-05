package de.wwu.muggl.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Level;
import org.eclipse.swt.graphics.RGB;

/**
 * The ConfigReader offers static methods to load and save settings from a configuration file. All
 * methods might be used with a file explicitly supplied as a FileReader or Writer. If they are
 * called without arguments, the currently set configuration file is uses.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-15
 */
public class ConfigReader {
	private static final int BUFFER_BYTES = 1024;
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/**
	 * Protected default constructor.
	 */
	protected ConfigReader() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Load the settings from the currently set configuration file.
	 * 
	 * @param loadDefaultValues If set to true, default settings will be loaded.
	 * @return true, if loading the settings was successful, false otherwise.
	 */
	public static boolean loadConfig(boolean loadDefaultValues) {
		String configFile = Options.getInst().configurationFile;
		File file = new File(configFile);
		// Check if the file can be used.
		if (!file.exists() || !file.isFile()) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Loading the current configuration from file " + file.getName() + " failed as it could not be opened.");
			return false;
		}

		// Instantiate the FileReader and invoke the method to write the entries.
		try {
			return loadConfig(loadDefaultValues, new FileReader(file));
		} catch (IOException e) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Loading the current configuration from file " + file.getName() + " failed with an IOException.");
			return false;
		}
	}

	/**
	 * Load the settings using the supplied FileReader.
	 * @param loadDefaultValues loadDefaultValues If set to true, default settings will be loaded.
	 * @param in A FileReader.
	 * @return true, if loading the settings was successful, false otherwise.
	 */
	public static boolean loadConfig(boolean loadDefaultValues, FileReader in) {
		String contents = "";
		// Read the file contents into a string.
		try {
			StringBuilder buffer = new StringBuilder(BUFFER_BYTES);
			BufferedReader reader = new BufferedReader(in);

			char[] chars = new char[BUFFER_BYTES];
			for (int bytes = reader.read(chars); bytes > -1; bytes = reader.read(chars)) {
				buffer.append(String.valueOf(chars), 0, bytes);
			}

			reader.close();
			contents = buffer.toString();
		} catch (IOException e) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Loading the current configuration from a file failed with an IOException.");
			return false;
		}

		// Check the header.
		if (contents.contains("<?xml version=\"1.0\"?>"+LINE_SEPARATOR)) {
			contents = contents.substring(contents.indexOf("<?xml version=\"1.0\"?>"+LINE_SEPARATOR) + 24);
		} else {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Loading the current configuration from a file failed. It does not seem to be a valid xml file.");
			return false;
		}

		if (contents.contains("<" + Globals.APP_NAME + "Configuration>"+LINE_SEPARATOR)) {
			contents = contents.substring(contents.indexOf("<" + Globals.APP_NAME + "Configuration>"+LINE_SEPARATOR) + Globals.APP_NAME.length() + 16);
		} else {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Loading the current configuration from a file failed. It does not seem to be a valid " + Globals.APP_NAME + " configuration file.");
			return false;
		}

		// Check the footer.
		if (contents.contains("</" + Globals.APP_NAME + "Configuration>"+LINE_SEPARATOR)) {
			contents = contents.substring(0, contents.indexOf("</" + Globals.APP_NAME + "Configuration>"+LINE_SEPARATOR));
		} else {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN))
				Globals.getInst().logger.warn("Loading the current configuration from a file failed. It does not seem to be a valid " + Globals.APP_NAME + " configuration file.");
			return false;
		}

		// Split it.
		String[] entries = contents.split("<setting ");

		// Now parse the single entries
		for (int a = 0; a < entries.length; a++) {
			try {
				// Reading...
				int closingTagPosition = entries[a].indexOf(">");
				// Skip empty entries.
				if (closingTagPosition != -1) {
					int namePosition = entries[a].indexOf("name=\"") + 6;
					int nameClosePosition = entries[a].substring(namePosition).indexOf("\"") + 6;
					if (nameClosePosition < closingTagPosition && namePosition < nameClosePosition) {
						// Read the name.
						String name = entries[a].substring(namePosition, nameClosePosition);

						// Find out if it is a special entry.
						String type = "";
						if (entries[a].substring(0, closingTagPosition).contains("type=\"")) {
							int typeBeginning = entries[a].substring(0, closingTagPosition).indexOf("type=\"") + 6;
							type = entries[a].substring(typeBeginning, typeBeginning + entries[a].substring(typeBeginning).indexOf("\""));
						}
						entries[a] = entries[a].substring(closingTagPosition);

						// Encountered an rbg field?
						if (type.equals("rgb")) {
							int red = -1, green = -1, blue = -1;
							int defaultRed = -1, defaultGreen = -1, defaultBlue = -1;
							// Collect the values.
							String[] valueStrings = entries[a].split("<value name=\"");
							for (int b = 0; b < valueStrings.length; b++) {
								// Remove the trailing "</value>". If there is no such String contained, just skip it.
								int valueClosingPosition = valueStrings[b].indexOf("</value>");
								if (valueClosingPosition != -1) {
									String valueName = valueStrings[b].substring(0, valueStrings[b].indexOf("\">"));
									String value = valueStrings[b].substring(valueStrings[b].indexOf("\">") + 2, valueClosingPosition);
									try {
										int color = Integer.parseInt(value);
										if (valueName.equals("red")) {
											red = color;
										} else if (valueName.equals("green")) {
											green = color;
										} else if (valueName.equals("blue")) {
											blue = color;
										} else if (valueName.equals("default red")) {
											defaultRed = color;
										} else if (valueName.equals("default green")) {
											defaultGreen = color;
										} else if (valueName.equals("default blue")) {
											defaultBlue = color;
										}
									} catch (NumberFormatException e) {
										if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Bad syntax.");
									}
								}
							}

							// Found all values?
							if (red != -1 && green != -1 && blue != -1 && defaultRed != -1
									&& defaultGreen != -1 && defaultBlue != -1) {
								RGB rgb = new RGB(red, green, blue);
								RGB defaultRgb = new RGB(defaultRed, defaultGreen, defaultBlue);
								loadOption(name, rgb, defaultRgb, loadDefaultValues);
							} else {
								if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Bad syntax.");
							}
						} else if (type.equals("multi") || type.length() == 0) {
							// Read the default value.
							int defaultStart = entries[a].indexOf("<default>");
							int defaultEnd = entries[a].indexOf("</default");
							if (defaultStart != -1 && defaultEnd != -1) {
								String defaultValue = entries[a].substring(defaultStart + 9, defaultEnd);
								// Only process what remains of the String.
								entries[a] = entries[a].substring(0, defaultStart) + entries[a].substring(defaultEnd);

								// Read the value or the values.
								if (type.equals("multi")) {
									// Initialize the ArrayList.
									ArrayList<String> values = new ArrayList<String>();
									// Split the String.
									String[] valueStrings = entries[a].split("<value>");
									for (int b = 0; b < valueStrings.length; b++) {
										// Remove the trailing "</value>". If there is no such String contained, just skip it.
										int valueClosingPosition = valueStrings[b].indexOf("</value>");
										if (valueClosingPosition != -1) {
											values.add(valueStrings[b].substring(0, valueClosingPosition));
										}
									}
									loadOption(name, values, defaultValue, loadDefaultValues);
								} else {
									int valueStart = entries[a].indexOf("<value>");
									int valueEnd = entries[a].indexOf("</value>");

									if (valueStart != -1 && valueEnd != -1) {
										// Get the value and the default value.
										String value = entries[a].substring(valueStart + 7, valueEnd);

										// Finally load the option.
										loadOption(name, value, defaultValue, loadDefaultValues);
									} else {
										if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Bad syntax.");
									}
								}
							} else {
								if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Bad syntax.");
							}
						} else {
							if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Unknown entry type.");
						}
					} else {
						if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Bad syntax.");
					}

					// Finishing that setting.
					int closing = entries[a].indexOf("</setting>");
					if (closing == -1) break;
					entries[a] = entries[a].substring(closing);
				}
			} catch (IndexOutOfBoundsException e) {
				if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Bad syntax.");
			}
		}

		// Finish.
		return true;
	}

	/**
	 * Load a setting that has a single value.
	 * @param name The name of the setting.
	 * @param value The value.
	 * @param defaultValue The default value.
	 * @param loadDefaultValues If true, the default value will be loaded, otherwise, the value will be loaded.
	 */
	private static void loadOption(String name, String value, String defaultValue, boolean loadDefaultValues) {
		Options options = Options.getInst();

		// Distinguish between the available settings.
		try {
			if (name.equals("javaHome")) {
				if (loadDefaultValues) {
					options.javaHome = value;
				} else {
					// Special handling: The default value is ignored and the system's default value is used.
					options.javaHome = System.getProperty("java.home");
				}
			} else if (name.equals("javaVersion")) {
				options.javaVersion = loadDefaultValues ? value : System.getProperty("java.version");
			} else if (name.equals("methodListHideInitClinit")) {
				options.methodListHideInitClinit = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("methodListShowMainMethodOnly")) {
				options.methodListShowMainMethodOnly = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("executionModeSingleSteps")) {
				options.executionModeSingleSteps = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("symbolicalMode")) {
				options.symbolicMode = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("logicMode")) {
				options.logicMode = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("jpaMode")) {
				options.jpaMode = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("assumeMissingValues")) {
				options.assumeMissingValues = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("askUserInCasOfMissingValues")) {
				options.askUserMissingValues = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("stepByStepVisuallySkipInvocations")) {
				options.stepByStepVisuallySkipInvoc = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("visuallySkipStaticInitializers")) {
				options.visuallySkipStaticInit = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("stepByStepShowInstructionBytePosition")) {
				options.stepByStepShowInstrBytePosition = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("symbolicSearchAlgorithm")) {
				options.searchAlgorithm = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("iterativeDeepeningStartingDepth")) {
				options.iterativeDeepeningStartingDepth = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("iterativeDeepeningDeepnessIncrement")) {
				options.iterativeDeepeningDeepnessIncrement = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("doNotHaltOnNativeMethods")) {
				options.doNotHaltOnNativeMethods = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("assumeNativeReturnValuesToBeZeroNull")) {
				options.assumeNativeReturnValuesToBeZeroNull = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("forwardJavaPackageNativeInvocations")) {
				options.forwardJavaPackageNativeInvoc = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("maximumExecutionTime")) {
				options.maximumExecutionTime = loadDefaultValues ? Long.parseLong(defaultValue) : Long.parseLong(value);
			} else if (name.equals("maximumLoopsToTake")) {
				options.maximumLoopsToTake = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("maximumInstructionsBeforeFindingANewSolution")) {
				options.maxInstrBeforeFindingANewSolution = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("onlyCountChoicePointGeneratingInstructions")) {
				options.onlyCountChoicePointGeneratingInst = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("maximumStepByStepLoggingEntries")) {
				options.maximumStepByStepLoggingEntries = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("testClassesDirectory")) {
				options.testClassesDirectory = loadDefaultValues ? defaultValue : value;
			} else if (name.equals("testClassesPackageName")) {
				options.testClassesPackageName = loadDefaultValues ? defaultValue : value;
			} else if (name.equals("testClassesName")) {
				options.testClassesName = loadDefaultValues ? defaultValue : value;
			} else if (name.equals("maximumLogEntries")) {
				options.maximumLogEntries = loadDefaultValues ? Long.parseLong(defaultValue) : Long.parseLong(value);
			} else if (name.equals("htmlLogging")) {
				if (loadDefaultValues) {
					options.setHtmlLogging(Boolean.parseBoolean(defaultValue));
				} else {
					options.setHtmlLogging(Boolean.parseBoolean(value));
				}
			} else if (name.equals("measureSymbolicExecutionTime")) {
				options.measureSymbolicExecutionTime = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("hideDrivesAB")) {
				options.hideDrivesAB = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("doNotClearClassLoaderCache")) {
				options.doNotClearClassLoaderCache = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("maximumClassLoaderCacheEntries")) {
				options.maximumClassLoaderCacheEntries = loadDefaultValues ? Long.parseLong(defaultValue) : Long.parseLong(value);
			} else if (name.equals("maximumClassLoaderCacheByteSize")) {
				options.maximumClassLoaderCacheBytes = loadDefaultValues ? Long.parseLong(defaultValue) : Long.parseLong(value);
			} else if (name.equals("useDefUseCoverage")) {
				options.useDUCoverage = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("useControlFlowCoverage")) {
				options.useCFCoverage = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("coverageTracking")) {
				options.coverageTracking = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("coverageAbortionCriteria")) {
				options.coverageAbortionCriteria = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("eliminateSolutionsByCoverage")) {
				options.eliminateSolutionsByCoverage = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("numberOfRecentFiles")) {
				options.numberOfRecentFiles = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("classFileWriteAccess")) {
				options.classFileWriteAccess = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("enableSJVMMultithreading")) {
				options.enableSJVMMultithreading = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("numberOfSimultaneousThreads")) {
				options.numberOfSimultaneousThreads = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("symbolicArrayInitializationNumberOfRunsTotal")) {
				options.symbArrayInitNumberOfRunsTotal = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("symbolicArrayInitializationStartingLength")) {
				options.symbArrayInitStartingLength = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("symbolicArrayInitializationIncrementationStrategy")) {
				options.symbArrayInitIncrStrategy = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("symbolicArrayInitializationIncrementationStrategyLinearStepSize")) {
				options.symbArrayInitIncrStrategyLinearStepSize = loadDefaultValues ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
			} else if (name.equals("symbolicArrayInitializationTestNull")) {
				options.symbArrayInitTestNull = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("symbolicArrayInitializationTestZeroLengthArray")) {
				options.symbArrayInitTestZeroLengthArray = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("dynamicallyReplaceInstructionsWithOptimizedOnes")) {
				options.dynReplaceInstWithOptimizedOnes = loadDefaultValues ? Boolean.parseBoolean(defaultValue) : Boolean.parseBoolean(value);
			} else if (name.equals("solverManager")) {
				options.solverManager = loadDefaultValues ? defaultValue : value;
			} else {
				// No match found - log that.
				if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Setting " + name + " could not be recognized and was ignored.");
			}
		} catch (NumberFormatException e) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: The value for setting " + name + " could not be parsed to the appropriate type and was ignored.");
		}
	}

	/**
	 * Load a setting that has more than one value.
	 * @param name The name of the setting.
	 * @param values The ArrayList of String objects with the values.
	 * @param defaultValue The default value.
	 * @param loadDefaultValues If true, the default value will be loaded, otherwise, the values will be loaded.
	 */
	private static void loadOption(String name, ArrayList<String> values, String defaultValue, boolean loadDefaultValues) {
		Options options = Options.getInst();

		// Distinguish between the available settings.
		if (name.equals("classPathEntries")) {
			if (loadDefaultValues) {
				ArrayList<String> classPathEntries = new ArrayList<String>();
				classPathEntries.add(defaultValue);
				options.classPathEntries = classPathEntries;
			} else {
				options.classPathEntries = values;
			}
		} else if (name.equals("recentFilesPaths")) {
			// By default, there are no recently opened files.
			if (!loadDefaultValues) {
				options.recentFilesPaths = values;
			}
		} else {
			// No match found - log that.
			if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Setting " + name + " could not be recognized and was ignored.");
		}
	}

	/**
	 * Load a SWT RGB value setting.
	 *
	 * @param name The name of the setting.
	 * @param rgb The colors..
	 * @param defaultRgb The default colors.
	 * @param loadDefaultValues If true, the default value will be loaded, otherwise, the value will be loaded.
	 */
	private static void loadOption(String name, RGB rgb, RGB defaultRgb, boolean loadDefaultValues) {
		Options options = Options.getInst();

		// Distinguish between the available settings.
		if (name.equals("rgb_fileInspection_ConstantClass")) {
			options.rgbFileInspConstantClass = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantDouble")) {
			options.rgbFileInspConstantDouble = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantFieldref")) {
			options.rgbFileInspConstantFieldref = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantFloat")) {
			options.rgbFileInspConstantFloat = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantInteger")) {
			options.rgbFileInspConstantInteger = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantInterfaceMethodref")) {
			options.rgbFileInspConstantInterfaceMethodref = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantLong")) {
			options.rgbFileInspConstantLong = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantMethodref")) {
			options.rgbFileInspConstantMethodref = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantNameAndType")) {
			options.rgbFileInspConstantNameAndType = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantString")) {
			options.rgbFileInspConstantString = loadDefaultValues ? defaultRgb : rgb;
		} else  if (name.equals("rgb_fileInspection_ConstantUtf8")) {
			options.rgbFileInspConstantUtf8 = loadDefaultValues ? defaultRgb : rgb;
		} else {
			// No match found - log that.
			if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered a problem loading the current configuration from a file: Setting " + name + " could not be recognized and was ignored.");
		}
	}


	/**
	 * Write the setting to the currently set configuration file. If the file does not exist,
	 * create it.
	 * @return true, if the writing was successfull, false otherwise.
	 */
	public static boolean saveCurrentConfig() {
		File file = new File(Options.getInst().configurationFile);
		// Check if the file exists and create it if it does not.
		if (!file.exists()) {
			try {
				if (!file.createNewFile())
					if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Written the current configuration to file " + file.getName() + " failed as it could not be created.");
			} catch (IOException e) {
				if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Written the current configuration to file " + file.getName() + " failed with an IOException.");
			}
		}

		// Check if the file can be used.
		if (!file.exists() || !file.isFile()) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Written the current configuration to file " + file.getName() + " failed as it could not be opened.");
			return false;
		}

		// Instantiate the FileWriter and invoke the method to write the entries.
		try {
			return saveCurrentConfig(new FileWriter(file));
		} catch (IOException e) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Written the current configuration to file " + file.getName() + " failed with an IOException.");
			return false;
		}
	}

	/**
	 * Write the setting to the currently set configuration file using the supplied FileWriter.
	 * @param out A FileWriter.
	 * @return true, if the writing was successful, false otherwise.
	 */
	public static boolean saveCurrentConfig(FileWriter out) {
		try {
			Options options = Options.getInst();

			// Get date and time information.
			SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM, yyyy h:mm a", Locale.ENGLISH);

			// Open a new file and write the header.
			out.write("<?xml version=\"1.0\"?>"+LINE_SEPARATOR);
			out.write("<!-- Written on " + df.format(new Date()) + " -->"+LINE_SEPARATOR);
			out.write("<" + Globals.APP_NAME + "Configuration>"+LINE_SEPARATOR);

			// Write the settings.
			out.write(generateNewEntry("javaHome", String.valueOf(options.javaHome), ""));
			out.write(generateNewEntry("javaVersion", String.valueOf(options.javaVersion), ""));
			out.write(generateNewArrayListEntry("classPathEntries", options.classPathEntries, ""));
			out.write(generateNewEntry("methodListHideInitClinit", String.valueOf(options.methodListHideInitClinit), String.valueOf(Defaults.METHODLIST_HIDE_INIT_CLINIT)));
			out.write(generateNewEntry("methodListShowMainMethodOnly", String.valueOf(options.methodListShowMainMethodOnly), String.valueOf(Defaults.METHODLIST_SHOW_MAIN_METHOD_ONLY)));
			out.write(generateNewEntry("executionModeSingleSteps", String.valueOf(options.executionModeSingleSteps), String.valueOf(Defaults.EXECUTIONMODE_SINGLE_STEPS)));
			out.write(generateNewEntry("symbolicalMode", String.valueOf(options.symbolicMode), String.valueOf(Defaults.SYMBOLIC_MODE)));
			out.write(generateNewEntry("logicMode", String.valueOf(options.logicMode), String.valueOf(Defaults.LOGIC_MODE)));
			out.write(generateNewEntry("jpaMode", String.valueOf(options.jpaMode), String.valueOf(Defaults.JPA_MODE)));
			out.write(generateNewEntry("assumeMissingValues", String.valueOf(options.assumeMissingValues), String.valueOf(Defaults.ASSUME_MISSING_VALUES)));
			out.write(generateNewEntry("askUserInCasOfMissingValues", String.valueOf(options.askUserMissingValues), String.valueOf(Defaults.ASK_USER_MISSING_VALUES)));
			out.write(generateNewEntry("stepByStepVisuallySkipInvocations", String.valueOf(options.stepByStepVisuallySkipInvoc), String.valueOf(Defaults.STEPBYSTEP_VISUALLY_SKIP_INVOC)));
			out.write(generateNewEntry("visuallySkipStaticInitializers", String.valueOf(options.visuallySkipStaticInit), String.valueOf(Defaults.VISUALLY_SKIP_STATIC_INIT)));
			out.write(generateNewEntry("stepByStepShowInstructionBytePosition", String.valueOf(options.stepByStepShowInstrBytePosition), String.valueOf(Defaults.STEPBYSTEP_SHOW_INSTR_BYTE_POSITION)));
			out.write(generateNewEntry("symbolicSearchAlgorithm", String.valueOf(options.searchAlgorithm), String.valueOf(Defaults.SEARCH_ALGORITHM)));
			out.write(generateNewEntry("iterativeDeepeningStartingDepth", String.valueOf(options.iterativeDeepeningStartingDepth), String.valueOf(Defaults.ITERATIVE_DEEPENING_STARTING_DEPTH)));
			out.write(generateNewEntry("iterativeDeepeningDeepnessIncrement", String.valueOf(options.iterativeDeepeningDeepnessIncrement), String.valueOf(Defaults.ITERATIVE_DEEPENING_DEEPNESS_INCREMENT)));
			out.write(generateNewEntry("doNotHaltOnNativeMethods", String.valueOf(options.doNotHaltOnNativeMethods), String.valueOf(Defaults.DO_NOT_HALT_ON_NATIVE_METHODS)));
			out.write(generateNewEntry("assumeNativeReturnValuesToBeZeroNull", String.valueOf(options.assumeNativeReturnValuesToBeZeroNull), String.valueOf(Defaults.ASSUME_NATIVE_RETURN_VALUES_TO_BE_ZERO_NULL)));
			out.write(generateNewEntry("forwardJavaPackageNativeInvocations", String.valueOf(options.forwardJavaPackageNativeInvoc), String.valueOf(Defaults.FORWARD_JAVA_PACKAGE_NATIVE_INVOC)));
			out.write(generateNewEntry("maximumExecutionTime", String.valueOf(options.maximumExecutionTime), String.valueOf(Defaults.MAX_EXECUTION_TIME)));
			out.write(generateNewEntry("maximumLoopsToTake", String.valueOf(options.maximumLoopsToTake), String.valueOf(Defaults.MAX_LOOPS_TO_TAKE)));
			out.write(generateNewEntry("maximumInstructionsBeforeFindingANewSolution", String.valueOf(options.maxInstrBeforeFindingANewSolution), String.valueOf(Defaults.MAX_INSTR_BEFORE_FINDING_A_NEW_SOLUTION)));
			out.write(generateNewEntry("onlyCountChoicePointGeneratingInstructions", String.valueOf(options.onlyCountChoicePointGeneratingInst), String.valueOf(Defaults.ONLY_COUNT_CHOICEPOINT_GENERATING_INSTR)));
			out.write(generateNewEntry("maximumStepByStepLoggingEntries", String.valueOf(options.maximumStepByStepLoggingEntries), String.valueOf(Defaults.MAX_STEPBYSTEP_LOGGING_ENTRIES)));
			out.write(generateNewEntry("testClassesDirectory", options.testClassesDirectory, Defaults.TEST_CLASSES_DIRECTORY));
			out.write(generateNewEntry("testClassesPackageName", options.testClassesPackageName, Defaults.TEST_CLASSES_PACKAGE_NAME));
			out.write(generateNewEntry("testClassesName", options.testClassesName, Defaults.TEST_CLASSES_NAME));
			out.write(generateNewEntry("maximumLogEntries", String.valueOf(options.maximumLogEntries), String.valueOf(Defaults.MAX_LOG_ENTRIES)));
			out.write(generateNewEntry("htmlLogging", String.valueOf(options.getHtmlLogging()), String.valueOf(Defaults.HTML_LOGGING)));
			out.write(generateNewEntry("measureSymbolicExecutionTime", String.valueOf(options.measureSymbolicExecutionTime), String.valueOf(Defaults.MEASURE_SYMBOLIC_EXECUTION_TIME)));
			out.write(generateNewEntry("hideDrivesAB", String.valueOf(options.hideDrivesAB), String.valueOf(Defaults.HIDE_DRIVES_AB)));
			out.write(generateNewEntry("doNotClearClassLoaderCache", String.valueOf(options.doNotClearClassLoaderCache), String.valueOf(Defaults.DO_NOT_CLEAR_CLASSLOADER_CACHE)));
			out.write(generateNewEntry("maximumClassLoaderCacheEntries", String.valueOf(options.maximumClassLoaderCacheEntries), String.valueOf(Defaults.MAX_CLASSLOADER_CACHE_ENTRIES)));
			out.write(generateNewEntry("maximumClassLoaderCacheByteSize", String.valueOf(options.maximumClassLoaderCacheBytes), String.valueOf(Defaults.MAX_CLASSLOADER_CACHE_BYTES)));
			out.write(generateNewEntry("useDefUseCoverage", String.valueOf(options.useDUCoverage), String.valueOf(Defaults.USE_DU_COVERAGE)));
			out.write(generateNewEntry("useControlFlowCoverage", String.valueOf(options.useCFCoverage), String.valueOf(Defaults.USE_CF_COVERAGE)));
			out.write(generateNewEntry("coverageTracking", String.valueOf(options.coverageTracking), String.valueOf(Defaults.COVERAGE_TRACKING)));
			out.write(generateNewEntry("coverageAbortionCriteria", String.valueOf(options.coverageAbortionCriteria), String.valueOf(Defaults.COVERAGE_ABORTION_CRITERIA)));
			out.write(generateNewEntry("eliminateSolutionsByCoverage", String.valueOf(options.eliminateSolutionsByCoverage), String.valueOf(Defaults.ELIMINATE_SOLUTIONS_BY_COVERAGE)));
			out.write(generateNewEntry("numberOfRecentFiles", String.valueOf(options.numberOfRecentFiles), String.valueOf(Defaults.NUMBER_OF_RECENT_FILES)));
			out.write(generateNewArrayListEntry("recentFilesPaths", options.recentFilesPaths, ""));
			out.write(generateNewEntry("rgb_fileInspection_ConstantClass", options.rgbFileInspConstantClass, Defaults.RGB_FILEINSP_CONSTANT_CLASS));
			out.write(generateNewEntry("rgb_fileInspection_ConstantDouble", options.rgbFileInspConstantDouble, Defaults.RGB_FILEINSP_CONSTANT_DOUBLE));
			out.write(generateNewEntry("rgb_fileInspection_ConstantFieldref", options.rgbFileInspConstantFieldref, Defaults.RGB_FILEINSP_CONSTANT_FIELDRED));
			out.write(generateNewEntry("rgb_fileInspection_ConstantFloat", options.rgbFileInspConstantFloat, Defaults.RGB_FILEINSP_CONSTANT_FLOAT));
			out.write(generateNewEntry("rgb_fileInspection_ConstantInteger", options.rgbFileInspConstantInteger, Defaults.RGB_FILEINSP_CONSTANT_INTEGER));
			out.write(generateNewEntry("rgb_fileInspection_ConstantInterfaceMethodref", options.rgbFileInspConstantInterfaceMethodref, Defaults.RGB_FILEINSP_CONSTANT_INTERFACEMETHODREF));
			out.write(generateNewEntry("rgb_fileInspection_ConstantLong", options.rgbFileInspConstantLong, Defaults.RGB_FILEINSP_CONSTANT_LONG));
			out.write(generateNewEntry("rgb_fileInspection_ConstantMethodref", options.rgbFileInspConstantMethodref, Defaults.RGB_FILEINSP_CONSTANT_METHODREF));
			out.write(generateNewEntry("rgb_fileInspection_ConstantNameAndType", options.rgbFileInspConstantNameAndType, Defaults.RGB_FILEINSP_CONSTANT_NAMEANDTYPE));
			out.write(generateNewEntry("rgb_fileInspection_ConstantString", options.rgbFileInspConstantString, Defaults.RGB_FILEINSP_CONSTANT_STRING));
			out.write(generateNewEntry("rgb_fileInspection_ConstantUtf8", options.rgbFileInspConstantUtf8, Defaults.RGB_FILEINSP_CONSTANT_UTF8));
			out.write(generateNewEntry("classFileWriteAccess", String.valueOf(options.classFileWriteAccess), String.valueOf(Defaults.CLASS_FILE_WRITE_ACCESS)));
			out.write(generateNewEntry("enableSJVMMultithreading", String.valueOf(options.enableSJVMMultithreading), String.valueOf(Defaults.ENABLE_SJVM_MULTITHREADING)));
			out.write(generateNewEntry("numberOfSimultaneousThreads", String.valueOf(options.numberOfSimultaneousThreads), String.valueOf(Defaults.NUMBER_OF_SIMULTANEOUS_THREADS)));
			out.write(generateNewEntry("symbolicArrayInitializationNumberOfRunsTotal", String.valueOf(options.symbArrayInitNumberOfRunsTotal), String.valueOf(Defaults.SYMB_ARRAY_INIT_NUMBER_OF_RUNS_TOTAL)));
			out.write(generateNewEntry("symbolicArrayInitializationStartingLength", String.valueOf(options.symbArrayInitStartingLength), String.valueOf(Defaults.SYMB_ARRAY_INIT_STARTING_LENGTH)));
			out.write(generateNewEntry("symbolicArrayInitializationIncrementationStrategy", String.valueOf(options.symbArrayInitIncrStrategy), String.valueOf(Defaults.SYMB_ARRAY_INIT_INCR_STRATEGY)));
			out.write(generateNewEntry("symbolicArrayInitializationIncrementationStrategyLinearStepSize", String.valueOf(options.symbArrayInitIncrStrategyLinearStepSize), String.valueOf(Defaults.SYMB_ARRAY_INIT_INCR_STRATEGY_LINEAR_STEPSIZE)));
			out.write(generateNewEntry("symbolicArrayInitializationTestNull", String.valueOf(options.symbArrayInitTestNull), String.valueOf(Defaults.SYMB_ARRAY_INIT_TEST_NULL)));
			out.write(generateNewEntry("symbolicArrayInitializationTestZeroLengthArray", String.valueOf(options.symbArrayInitTestZeroLengthArray), String.valueOf(Defaults.SYMB_ARRAY_INIT_TEST_ZERO_LENGTH_ARRAY)));
			out.write(generateNewEntry("dynamicallyReplaceInstructionsWithOptimizedOnes", String.valueOf(options.dynReplaceInstWithOptimizedOnes), String.valueOf(Defaults.DYN_REPLACE_INSTR_WITH_OPTIMIZED_ONES)));
			out.write(generateNewEntry("solverManager", String.valueOf(options.solverManager), String.valueOf(Defaults.SOLVER_MANAGER)));

			// Finish.
			out.write("</" + Globals.APP_NAME + "Configuration>"+LINE_SEPARATOR);
			out.close();
			return true;
		} catch (IOException e) {
			if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Written the current configuration to a file failed with an IOException.");
			return false;
		}
	}

	/**
	 * Generate a new entry for the configuration file.
	 * @param name The name of the setting to store.
	 * @param value The value of the setting.
	 * @param defaultValue The default value of the setting.
	 * @return The entry as a String.
	 */
	private static String generateNewEntry(String name, String value, String defaultValue) {
		return "\t<setting name=\"" + name + "\">"+LINE_SEPARATOR
			+ "\t\t<value>" + value + "</value>"+LINE_SEPARATOR
			+ "\t\t<default>" + defaultValue + "</default>"+LINE_SEPARATOR
			+ "\t</setting>"+LINE_SEPARATOR;
	}

	/**
	 * Generate a new entry with multiple values for the configuration file.
	 * @param name The name of the setting to store.
	 * @param value The values of the setting as an ArrayList of String objects.
	 * @param defaultValue The default value of the setting.
	 * @return The entry as a String.
	 */
	private static String generateNewArrayListEntry(String name, List<String> value, String defaultValue) {
		String entry = "\t<setting name=\"" + name + "\" type=\"multi\">"+LINE_SEPARATOR;
		Iterator<String> iterator = value.iterator();
		while (iterator.hasNext()) {
			entry += "\t\t<value>" + iterator.next() + "</value>"+LINE_SEPARATOR;
		}
		entry += "\t\t<default>" + defaultValue + "</default>"+LINE_SEPARATOR
		+ "\t</setting>"+LINE_SEPARATOR;

		return entry;
	}

	/**
	 * Generate a new entry with multiple values for the configuration file. Null
	 * values will be skipped.
	 * @param name The name of the setting to store.
	 * @param value The values of the setting as an Array of String objects.
	 * @param defaultValue The default value of the setting.
	 * @return The entry as a String.
	 */
	@SuppressWarnings("unused")
	private static String generateNewArrayEntry(String name, String[] value, String defaultValue) {
		String entry = "\t<setting name=\"" + name + "\" type=\"multi\">"+LINE_SEPARATOR;
		for (int a = 0; a < value.length; a++) {
			if (value[a] != null)
				entry += "\t\t<value>" + value[a] + "</value>"+LINE_SEPARATOR;
		}
		entry += "\t\t<default>" + defaultValue + "</default>"+LINE_SEPARATOR
		+ "\t</setting>"+LINE_SEPARATOR;

		return entry;
	}

	/**
	 * Generate a new color entry for the configuration file. It will save values for
	 * red, green and blue.
	 * @param name The name of the setting to store.
	 * @param rgb The SWT RGB object to save.
	 * @param rgbDefault The default RGB values.
	 * @return The entry as a String.
	 */
	private static String generateNewEntry(String name, RGB rgb, RGB rgbDefault) {
		return "\t<setting name=\"" + name + "\" type=\"rgb\">"+LINE_SEPARATOR
			+ "\t\t<value name=\"red\">" + rgb.red + "</value>"+LINE_SEPARATOR
			+ "\t\t<value name=\"green\">" + rgb.green + "</value>"+LINE_SEPARATOR
			+ "\t\t<value name=\"blue\">" + rgb.blue + "</value>"+LINE_SEPARATOR
			+ "\t\t<value name=\"default red\">" + rgbDefault.red + "</value>"+LINE_SEPARATOR
			+ "\t\t<value name=\"default green\">" + rgbDefault.green + "</value>"+LINE_SEPARATOR
			+ "\t\t<value name=\"default blue\">" + rgbDefault.blue + "</value>"+LINE_SEPARATOR
			+ "\t</setting>"+LINE_SEPARATOR;
	}

}
