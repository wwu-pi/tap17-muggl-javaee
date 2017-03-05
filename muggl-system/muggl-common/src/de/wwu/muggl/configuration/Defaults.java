package de.wwu.muggl.configuration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

/**
 * A class that only defines constants that mark default values for Muggl options.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-15
 */
final class Defaults {

	/**
	 * Private constructor to prevent initialization.
	 */
	private Defaults() { }
	
	/**
	 * Path to the configuration file.
	 * @see Options#configurationFile
	 */
	public static final String CONFIGURATION_FILE = System.getProperties().getProperty("user.dir").replace("\\", "/") + "/conf/muggl-config.xml";

	/**
	 * Fetch the java home directory.
	 * @see Options#javaHome
	 */
	public static final String JAVA_HOME = System.getProperty("java.home");
	/**
	 * Fetch the java version.
	 * @see Options#javaVersion
	 */
	public static final String JAVA_VERSION = System.getProperty("java.version");

	/**
	 * @see Options#methodListHideInitClinit
	 */
	public static final boolean METHODLIST_HIDE_INIT_CLINIT = true;
	/**
	 * @see Options#methodListShowMainMethodOnly
	 */
	public static final boolean METHODLIST_SHOW_MAIN_METHOD_ONLY = false;
	/**
	 * @see Options#executionModeSingleSteps
	 */
	public static final boolean EXECUTIONMODE_SINGLE_STEPS = true;

	/**
	 * @see Options#classPathEntries
	 */
	public static final List<String> CLASSPATH_ENTRIES = new ArrayList<String>();
	/**
	 * @see Options#symbolicMode
	 */
	public static final boolean SYMBOLIC_MODE = false;
	/**
	 * @see Options#logicMode
	 */
	public static final boolean LOGIC_MODE = false;
	/**
	 * @see Options#jpaMode
	 */
	public static final boolean JPA_MODE = false;
	/**
	 * @see Options#assumeMissingValues
	 */
	public static final boolean ASSUME_MISSING_VALUES = false;
	/**
	 * @see Options#askUserMissingValues
	 */
	public static final boolean ASK_USER_MISSING_VALUES = false;
	/**
	 * @see Options#stepByStepVisuallySkipInvoc
	 */
	public static final int STEPBYSTEP_VISUALLY_SKIP_INVOC = 0;
	/**
	 * @see Options#visuallySkipStaticInit
	 */
	public static final boolean VISUALLY_SKIP_STATIC_INIT = true;
	/**
	 * @see Options#searchAlgorithm
	 */
	public static final int SEARCH_ALGORITHM = 2;
	/**
	 * @see Options#stepByStepShowInstrBytePosition
	 */
	public static final boolean STEPBYSTEP_SHOW_INSTR_BYTE_POSITION = false;
	/**
	 * @see Options#iterativeDeepeningStartingDepth
	 */
	public static final int ITERATIVE_DEEPENING_STARTING_DEPTH = 10;
	/**
	 * @see Options#iterativeDeepeningDeepnessIncrement
	 */
	public static final int ITERATIVE_DEEPENING_DEEPNESS_INCREMENT = 5;
	/**
	 * @see Options#doNotHaltOnNativeMethods
	 */
	
	public static final boolean DO_NOT_HALT_ON_NATIVE_METHODS = true;
	/**
	 * @see Options#assumeNativeReturnValuesToBeZeroNull
	 */
	public static final boolean ASSUME_NATIVE_RETURN_VALUES_TO_BE_ZERO_NULL = true;
	/**
	 * @see Options#forwardJavaPackageNativeInvoc
	 */
	public static final boolean FORWARD_JAVA_PACKAGE_NATIVE_INVOC = true;
	/**
	 * @see Options#maximumExecutionTime
	 */
	public static final int MAX_EXECUTION_TIME = 60;
	/**
	 * @see Options#maximumLoopsToTake
	 */
	public static final int MAX_LOOPS_TO_TAKE = 100;
	/**
	 * @see Options#maxInstrBeforeFindingANewSolution
	 */
	public static final int MAX_INSTR_BEFORE_FINDING_A_NEW_SOLUTION = -1;
	/**
	 * @see Options#onlyCountChoicePointGeneratingInst
	 */
	public static final boolean ONLY_COUNT_CHOICEPOINT_GENERATING_INSTR = false;
	/**
	 * @see Options#maximumStepByStepLoggingEntries
	 */
	public static final int MAX_STEPBYSTEP_LOGGING_ENTRIES = 15;
	/**
	 * @see Options#testClassesDirectory
	 */
	public static final String TEST_CLASSES_DIRECTORY = "E:/Daten/Uni-Arbeit/Dissertation/Muggl/src/test";
	/**
	 * @see Options#testClassesPackageName
	 */
	public static final String TEST_CLASSES_PACKAGE_NAME = "test";
	/**
	 * @see Options#testClassesName
	 */
	public static final String TEST_CLASSES_NAME = "TestClass";
	/**
	 * @see Options#maximumLogEntries
	 */
	public static final long MAX_LOG_ENTRIES = 50000L;
	/**
	 * Log to a HTML file.
	 */
	public static final boolean HTML_LOGGING = true;
	/**
	 * @see Options#measureSymbolicExecutionTime
	 */
	public static final boolean MEASURE_SYMBOLIC_EXECUTION_TIME = true;
	/**
	 * @see Options#hideDrivesAB
	 */
	public static final boolean HIDE_DRIVES_AB = true;
	/**
	 * @see Options#doNotClearClassLoaderCache
	 */
	public static final boolean DO_NOT_CLEAR_CLASSLOADER_CACHE = true;
	/**
	 * @see Options#maximumClassLoaderCacheEntries
	 */
	public static final long MAX_CLASSLOADER_CACHE_ENTRIES = -1L;
	/**
	 * @see Options#maximumClassLoaderCacheBytes
	 */
	public static final long MAX_CLASSLOADER_CACHE_BYTES = 0L;
	/**
	 * @see Options#useCFCoverage
	 */
	public static final boolean USE_CF_COVERAGE = true;
	/**
	 * @see Options#useDUCoverage
	 */
	public static final boolean USE_DU_COVERAGE = true;
	/**
	 * @see Options#coverageTracking
	 */
	public static final int COVERAGE_TRACKING = 2;
	/**
	 * @see Options#coverageAbortionCriteria
	 */
	public static final int COVERAGE_ABORTION_CRITERIA = 1;
	/**
	 * @see Options#eliminateSolutionsByCoverage
	 */
	public static final int ELIMINATE_SOLUTIONS_BY_COVERAGE = 3;
	/**
	 * @see Options#numberOfRecentFiles
	 */
	public static final int NUMBER_OF_RECENT_FILES = 5;
	/**
	 * @see Options#recentFilesPaths
	 */
	public static final List<String> RECENT_FILES_PATH = new ArrayList<String>();
	/**
	 * @see Options#rgbFileInspConstantClass
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_CLASS = new RGB(230, 230, 255);
	/**
	 * @see Options#rgbFileInspConstantDouble
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_DOUBLE = new RGB(230, 255, 255);
	/**
	 * @see Options#rgbFileInspConstantFieldref
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_FIELDRED = new RGB(220, 255, 220);
	/**
	 * @see Options#rgbFileInspConstantFloat
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_FLOAT = new RGB(235, 255, 255);
	/**
	 * @see Options#rgbFileInspConstantInteger
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_INTEGER  = new RGB(255, 235, 255);
	/**
	 * @see Options#rgbFileInspConstantInterfaceMethodref
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_INTERFACEMETHODREF = new RGB(230, 255, 230);
	/**
	 * @see Options#rgbFileInspConstantLong
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_LONG = new RGB(255, 240, 255);
	/**
	 * @see Options#rgbFileInspConstantMethodref
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_METHODREF = new RGB(240, 255, 240);
	/**
	 * @see Options#rgbFileInspConstantNameAndType
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_NAMEANDTYPE  = new RGB(255, 255, 230);
	/**
	 * @see Options#rgbFileInspConstantString
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_STRING = new RGB(255, 230, 230);
	/**
	 * @see Options#rgbFileInspConstantUtf8
	 */
	public static final RGB RGB_FILEINSP_CONSTANT_UTF8 = new RGB(255, 240, 240);
	/**
	 * @see Options#classFileWriteAccess
	 */
	public static final boolean CLASS_FILE_WRITE_ACCESS = true;
	/**
	 * @see Options#enableSJVMMultithreading
	 */
	public static final boolean ENABLE_SJVM_MULTITHREADING = false;
	/**
	 * @see Options#numberOfSimultaneousThreads
	 */
	public static final int NUMBER_OF_SIMULTANEOUS_THREADS = 2;
	/**
	 * @see Options#symbArrayInitNumberOfRunsTotal
	 */
	public static final int SYMB_ARRAY_INIT_NUMBER_OF_RUNS_TOTAL = 25;
	/**
	 * @see Options#symbArrayInitStartingLength
	 */
	public static final int SYMB_ARRAY_INIT_STARTING_LENGTH = 1;
	/**
	 * @see Options#symbArrayInitIncrStrategy
	 */
	public static final int SYMB_ARRAY_INIT_INCR_STRATEGY = 2;
	/**
	 * @see Options#symbArrayInitIncrStrategyLinearStepSize
	 */
	public static final int SYMB_ARRAY_INIT_INCR_STRATEGY_LINEAR_STEPSIZE = 1;
	/**
	 * @see Options#symbArrayInitTestNull
	 */
	public static final boolean SYMB_ARRAY_INIT_TEST_NULL = true;
	/**
	 * @see Options#symbArrayInitTestZeroLengthArray
	 */
	public static final boolean SYMB_ARRAY_INIT_TEST_ZERO_LENGTH_ARRAY = true;
	/**
	 * @see Options#dynReplaceInstWithOptimizedOnes
	 */
	public static final boolean DYN_REPLACE_INSTR_WITH_OPTIMIZED_ONES = true;
	/**
	 * @see Options#solverManager
	 */
	public static final String SOLVER_MANAGER = "de.wwu.muggl.solvers.choco.ChocoSolverManager";
	
}
