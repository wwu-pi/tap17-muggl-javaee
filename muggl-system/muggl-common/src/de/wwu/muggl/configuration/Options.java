package de.wwu.muggl.configuration;

import java.util.List;

import org.eclipse.swt.graphics.RGB;

/**
 * Singleton class that holds mostly options that are used in the application for configuration
 * purposes.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-15
 */
public final class Options {

	// The options.
	/**
	 * The path and filename of the configuration file. This option cannot be changed via the
	 * GUI but only by using a command line argument.
	 */
	public String configurationFile;

	/**
	 * The java home directory.
	 */
	public String javaHome;
	/**
	 * The java version.
	 */
	public String javaVersion;

	/**
	 * Main GUI window: Show <init> and <clinit> in the method list.
	 */
	public boolean methodListHideInitClinit;
	/**
	 * Main GUI window: Only show main methods in the method list.
	 */
	public boolean methodListShowMainMethodOnly;
	/**
	 * Main GUI window: Set the execution mode to step by step.
	 */
	public boolean executionModeSingleSteps;

	/**
	 * The current entries of the class path.
	 */
	public List<String> classPathEntries;

	/**
	 * Switch between normal and symbolic execution mode. The normal execution mode works with
	 * constants, like Sun's java virtual machine. The symbolic mode uses variables instead and
	 * tries to find possible solutions which can be used as test cases.
	 */
	public boolean symbolicMode;

	/**
	 * Switch between symbolic and logic execution mode. Logic operation only works if symbolic
	 * operation is enabled.
	 */
	public boolean logicMode;
	
	/**
	 * Switch between symbolic and JPA execution mode. JPA operation only works if symbolic
	 * operation is enabled.
	 */
	public boolean jpaMode;

	/**
	 * If the execution mode is not symbolic, the arguments of the initial method have to be
	 * specified. If this is not done, they are set to an undefined value indicator (which is
	 * not null). Should the value be needed during the program execution, the most likely
	 * result is an execution exception leading to the abortion of the execution.
	 *
	 * This can be prevented by having missing values assumed to be null, or the right
	 * representation of zero for primitive types and their java.lang wrapper classes. Please
	 * note that this however might have unexpected results.
	 */
	public boolean assumeMissingValues;

	/**
	 * If missing values are encountered (see above), the user can be asked to set the value by
	 * hand. This will only be done in step by step execution mode.
	 */
	public boolean askUserMissingValues;

	/**
	 * Step by step mode: visually skip the invocation of other methods.
	 * 0:	Show every invocation.
	 * 1:	Skip invocation of methods from the java.* packages.
	 * 2:	Skip invocation of methods from any other class.
	 * 3:	Skip any invocation.
	 */
	public int stepByStepVisuallySkipInvoc;

	/**
	 * This field is used to determine if static initializers (<clinit>)-Methods
	 * should be skipped. A couple of instructions might lead to the static initialization
	 * of classes. Static initialization always occurs when a class is needed for the first
	 * time, for example by putting or getting a static field from it, invocation of methods
	 * of a class for the first time or pushing new objects. This branching might be
	 * unexpected and undesired by the user. Also, there might be further branching and
	 * invocation so that the static initialization of a single class might results in
	 * hundreds of instructions beeing executed before the control flow returns to the
	 * method that causes the initialization. If this field is set to true, static ini-
	 * tializers will be executed, but no gui output will be produced. This will not only
	 * disburden the user visually, but speed up this execution majorly. (Visual execution
	 * is not only a big overhead, but artificially slows down execution to prevent threading
	 * problems.
	 */
	public boolean visuallySkipStaticInit;

	/**
	 * In the step by step execution window the instructions are listed chronologically. Each
	 * instruction gets a number. This number is either just increased by one for each instruction
	 * (starting at zero), or it can be given accordingly to the instructions position in the
	 * code. In that case, the number will reflect the pc at which it is executed.
	 */
	public boolean stepByStepShowInstrBytePosition;

	/**
	 * Symbolic execution: Search algorithm.
	 * 0: Breadth first.
	 * 1: Depth first.
	 * 2: Iterative deepening with a depth of iterativeDeepeningDepth.
	 */
	public int searchAlgorithm;
	/**
	 * The iterative deepening options are explained in the documentation of the search
	 * algorithms' class.
	 * @see de.wwu.muggl.symbolic.searchAlgorithms.iterativeDeepening.IterativeDeepeningSearchAlgorithm
	 */
	public int iterativeDeepeningStartingDepth;
	/**
	 * The iterative deepening options are explained in the documentation of the search
	 * algorithms' class.
	 * @see de.wwu.muggl.symbolic.searchAlgorithms.iterativeDeepening.IterativeDeepeningSearchAlgorithm
	 */
	public int iterativeDeepeningDeepnessIncrement;

	/**
	 * This application does not offer native support directly. So the first choice is
	 * whether to halt on any invocation of a native method or not. If the virtual
	 * machine does not halt, there are two more possibilities:
	 * 1. Since the execution will most likely not continue sucessfully if native methods
	 * that have a return value are skipped, their returned value can be assumed to be
	 * zero (for primitive types, including java.lang wrapper classes) or null (for
	 * reference types). This might lead to some unforseen consequences, and the
	 * execution might still fail, especially if returned objects are needed and
	 * null values do never suit.
	 * 2. Native calls to any method of the java-package can be forwarded to the
	 * implementation of the host system. This will slow execution down a bit due to the
	 * overhead of invoking those methods, it however offers good means of using most
	 * of the native methods usually encountered.
	 *
	 * The second options will be checked first; this means, if a native method from the
	 * java package is to be executed and both options are enabled, the invocation will
	 * be forwarded.
	 *
	 * Native call forwarding is not fully available for the symbolic execution due to the
	 * nature of it. If enabled, only those native mathods from the java-package will be
	 * executed that do not take any arguments (parameters) and return a primitive value
	 * (or an corresponding java.lang wrapper object). This value will then be converted
	 * into a constant value for the symbolic execution.
	 */
	public boolean doNotHaltOnNativeMethods;
	/**
	 * Toggles the assumption of return values from native methods to be null.
	 */
	public boolean assumeNativeReturnValuesToBeZeroNull;
	/**
	 * Toggles the forwarding of calls to native methods from the java package.
	 */
	public boolean forwardJavaPackageNativeInvoc;

	/**
	 * For any realistic application, a maximum execution time is needed. In most cases,
	 * it is unlikely that users will infinitely to get the results of the execution.
	 * Hence, a maximum execution time can be specified.
	 *
	 * A maximum execution time of -1 means, the execution should not be limited by the
	 * time it may take.
	 *
	 * The unit for the maximum execution time is seconds.
	 */
	public long maximumExecutionTime;

	/**
	 * When executing symbolically, there might be situations where a loop is not exited
	 * normally. To prevent this loop from being taken infinitely, a maximum number of
	 * executions that are to be taken per loop can be set. After running the loop for that
	 * number of times, the execution is aborted and the symbolic virtual machine tracks
	 * back.
	 *
	 * If there should be no abortion of loops at all, maximumLoopsToTake can be set to -1,
	 * which will disable the detection and the checking of loops.
	 */
	public int maximumLoopsToTake;

	/**
	 * As an abortion criterion, the maximum number of instructions before finding a new
	 * solution can be used. If enabled (set to a value other than -1), the symbolic
	 * execution will halt if there has not been a new solution within the last x
	 * executed instructions. When finding a new solution, this counter will be reseted.
	 *
	 * Optionally, only those instructions can be counted that lead to the gendration of
	 * choice points.
	 */
	public int maxInstrBeforeFindingANewSolution;
	/**
	 * @see #maxInstrBeforeFindingANewSolution
	 */
	public boolean onlyCountChoicePointGeneratingInst;

	/**
	 * How much entries should the logging history in the step by step execution window contain?
	 */
	public int maximumStepByStepLoggingEntries;

	/**
	 * The directory test cases will be stored in and the name of the test classes. A package
	 * name should be chosen according to te directory. The name will be expanded by a number
	 * of type long. I.e., if the name is "TestClass" generated classes will be "TestClass1",
	 * "TestClass2" etc.
	 */
	public String testClassesDirectory;
	/**
	 * @see #testClassesDirectory
	 */
	public String testClassesPackageName;
	/**
	 * @see #testClassesDirectory
	 */
	public String testClassesName;

	/**
	 * The maximum number of entries for a single log file. -1 indicates an infinite number.
	 */
	public long maximumLogEntries;

	/**
	 * If this option is enabled, the symbolic virtual machine will measure the execution
	 * time of distinct operations. This helps to find bottlenecks as the information can
	 * be used in combination with the total runtime to find out which part the distinct
	 * operations of the execution took in the total execution time.
	 */
	public boolean measureSymbolicExecutionTime;

	/**
	 * On windows systems it might take some time to build the directory tree if floppy disk drives
	 * are present. By enabling this option, drives with the letters A: and B: will be hidden.
	 */
	public boolean hideDrivesAB;

	/**
	 * By default, the class loaders cache is cleared when the main class path is changed.
	 * This for example happens when changing to another project; while browsing a project
	 * folder, caching of classes is always active. Not clearing the cache will result in
	 * a gain in execution speed, especially on slower systems. At the same time, the memory
	 * footprint of the application will increase. However, memory problems should not arise
	 * when not loading more than some hundred classes. If full caching is enabled and two
	 * projects with the same package names (e.g. an old and a new version of the same project,
	 * or two versions of a jar-archive) are browsed, this might lead to major problems when
	 * starting the execution, as classes of more than one version might have been loaded. This
	 * might have no effect, but could lead to unexpected results or even making a successful
	 * execution impossible.
	 */
	public boolean doNotClearClassLoaderCache;
	/**
	 * The maximum number of cached classes can be limited using this value. In general, the more
	 * classes the cache can take, the less often I/O accesses to load classes are needed. However,
	 * each cached class uses runtime memory. If the memory is short or you run a very memory
	 * consuming application, it can be a good idea to limit the number of cached classes.<br />
	 * <br />
	 * There are three possible settings for this value:
	 * <ul>
	 * <li>-1 (or less) disables the maximum,</li>
	 * <li>0 disabled caching and</li>
	 * <li>any other positive number is taken as the maximum number of cached classes.</li>
	 * </ul>
	 * When using a limitation, the class that has not been loaded for the longest time will be
	 * dropped first. Please note that there is a slight overhead inherent to this.<br />
	 * <br />
	 * The general recommendation is to keep the maximum disabled as long as you do not run into
	 * memory problems. Be sure to adjust the memory provided to the system's JVM before changing
	 * this setting.
	 */
	public long maximumClassLoaderCacheEntries;
	/**
	 * Similarly to maximumClassLoaderCacheEntries, this setting is used to control the caching of
	 * classes. Each class has a size in bytes it totally takes. If the maximum byte size is reached, the class that has not been loaded for the longest time will be
	 * dropped first.<br /><br />
	 * Please note that the byte length of a class file is connected to its memory footprint. However, the footprint will be about two to three times larger, depending on the class' structures. The value should be 1/4 or less of the actual available heap space for the system's JVM.<br />
	 * <br />
	 * If you have 512 MB of Heap space available  you should not set this value to anything greater than 128 MB (134217728 Bytes).<br />
	 * <br />
	 * The general recommendation is to keep the maximum disabled as long as you do not run into
	 * memory problems. Be sure to adjust the memory provided to the system's JVM before changing
	 * this setting.
	 */
	public long maximumClassLoaderCacheBytes;

	/**
	 * Find def-use chains and check whether they are covered.
	 */
	public boolean useDUCoverage;

	/**
	 * Build a control flow graph and check its coverage.
	 */
	public boolean useCFCoverage;

	/**
	 * When tracking the coverage of data and/or control flow, this can be limited to the initially
	 * executed method. In fact, invoked method can be checked for coverage, too. This is a very
	 * strong criterion and it is much less likely to fulfill it than to reach full coverage in the
	 * initial method only as the execution of method and classes is bound to the (symbolic, yet
	 * constrained) parameters passed by the initially executed method. Hence, in many cases full
	 * coverage will be impossible to reach and the possibly reached coverage will - speaking in
	 * average terms - by lower with any invocation of a distinct method.
	 *
	 * This option should hence be used with care. Especially, other abortion criteria (especially
	 * the total execution time) should be used if aiming at top-level or total coverage.
	 *
	 * There are five possible options:
	 * 0: Initial method
	 * 1: Methods of the initial class
	 * 2: Initial package
	 * 3: Initial top-level-package (e.g. java.*)
	 * 4: Any method in any package (not recommended)
	 */
	public int coverageTracking;

	/**
	 * This value controls the abortion of the symbolic execution which is driven by the coverage
	 * of data and control flow.
	 * There are four possible options:
	 * 0: Do not abort when either data or control flow coverage is reached
	 * 1: Full def-use coverage
	 * 2: Full data-flow coverage (not recommended)
	 * 3: Full def-use and data-flow coverage (not recommended)
	 *
	 * Abort the symbolic execution if all def-use chains are covered does not necessarily mean
	 * that all potential solutions have been found, but it is quite likely. It especially is
	 * almost sure a lot of the potential test cases have been found. Reaching full control flow
	 * coverage is very unlikely. In most cases it will simply be impossible to reach full coverage
	 * here as semantical constraints will especially have the effect, that some exceptions will
	 * never be thrown.
	 */
	public int coverageAbortionCriteria;

	/**
	 * Solutions can be eliminated using the control-flow and data-flow coverage. A heuristical
	 * algorithm will be used. It picks the solution first that covers the most def-use chains (this
	 * strategy is called "greedy" in the literature). If there is more than one solution with and
	 * equal number of def-use chains covered, the one is picked that covers more control-flow
	 * edges. Then the solutions is picked that covers most of the remaining def-use-chains, again
	 * applying the control-flow coverage as the second criterion. This is continued until all
	 * chains are covered. If any control-flow edges are unpicked by then, the algorithm will
	 * continue to add those solutions that offer the coverage of yet unpicked control flow edges,
	 * again working in a "greedy" way until the set of solution picks all chains and edges.
	 * Solutions not yet picked are discarded.<br />
	 * <br />
	 *
	 * If either elimination by def-use chain or control flow coverage is disabled, only the other
	 * criterion will be used.<br />
	 * <br />
	 *
	 * There are four possible options:<br />
	 * <ol start="0"> <li>Do not eliminate test cases by their coverage contribution.</li> <li>
	 * Eliminate test cases by def-use chain coverage.</li> <li>Eliminate test cases by control flow
	 * graph edge coverage.</li> <li>Eliminate test cases both by their contribution to def-use
	 * chains and control flow coverage.</li>
	 * <ol>
	 */
	public int eliminateSolutionsByCoverage;

	/**
	 * The number of recently opened files that is stored.
	 */
	public int numberOfRecentFiles;
	/**
	 * The ArrayList to store the paths of the recently opened files.
	 */
	public List<String> recentFilesPaths;

	// RGB for Colors.
	/**
	 * The RGB for the background of the CONSTANT_Class_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantClass;
	/**
	 * The RGB for the background of the CONSTANT_Double_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantDouble;
	/**
	 * The RGB for the background of the CONSTANT_Fieldref_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantFieldref;
	/**
	 * The RGB for the background of the CONSTANT_Float_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantFloat;
	/**
	 * The RGB for the background of the CONSTANT_Integer_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantInteger;
	/**
	 * The RGB for the background of the CONSTANT_InterfaceMethodref_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantInterfaceMethodref;
	/**
	 * The RGB for the background of the CONSTANT_Long_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantLong;
	/**
	 * The RGB for the background of the CONSTANT_Methodref_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantMethodref;
	/**
	 * The RGB for the background of the CONSTANT_NameAndType_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantNameAndType;
	/**
	 * The RGB for the background of the CONSTANT_String_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantString;
	/**
	 * The RGB for the background of the CONSTANT_Utf8_info in the file inspection window will be painted in.
	 */
	public RGB rgbFileInspConstantUtf8;

	/**
	 * Toggle write access to ClassFile objects and their structure.
	 */
	public boolean classFileWriteAccess;

	/**
	 * Enable the usage of multiple threads by the engine. Of course this application uses multiple threads,
	 * for example the gui is running in an own thread so it will not be blocked by the JVM. However, when
	 * running the virtual machine symbolically most of the work has to be done sequentially while other
	 * threads wait. To speed up execution on systems with more than one core, multiple SJVM threads can be
	 * spawned when using a depth-first search algorithm. In that case, as many branches are visited
	 * simultaneously as threads are allowed. Having a number of SJVM threads similar to or near to the number
	 * of cpu cores available can drive the cpu load to almost 100%. Execution can almost linearly decrease
	 * with the number of cores.
	 * WARNING: Depth-first is a search algorithm with a low memory footprint. Using multiple threads for
	 * processing the search-tree greatly increases the memory footprint. Not only does each thread need
	 * memory for the current state, but the information about former states (for backtracking issues) can
	 * only be shared to some extend either.
	 */
	public boolean enableSJVMMultithreading;

	/**
	 * If SVJM multithreading is enables, this field holds the number of threads allowed. It should never be
	 * greater than the actual number of physical cpu cores usable by the JVM executing this application (as
	 * can be checked with Runtime.getRuntime().availableProcessors()). Each thread will do enough load to
	 * keep ony core busy.
	 *
	 * For sytems with a small number of cores, a number of threads equal to the number of cores would be ideal.
	 * If a lot of cores are present, probably a lower number should be taken due to memory consumption. If
	 * memory is no issue, one out of eight cores or so should not be used so the other threads (gui etc.) can
	 * use it, and the system will use it for its one functions aswell as assign it to the JVM executoing this
	 * application so it can run tasks like the garbage collection.
	 */
	public int numberOfSimultaneousThreads;

	/**
	 * It is almost impossible to find a suitable mathematical representation for arrays that are created at
	 * runtime and that have an unknown length. While of course approaches exists, constraint solving does not
	 * seem possible. Hence, arrays are tested in a partly non symbolic way. The systems runs with an generated
	 * array until it backtracks and restarts with another one. The elements are symbolic, while the array is
	 * fixed.
	 *
	 * This value determines the number of runs with each array in total.
	 */
	public int symbArrayInitNumberOfRunsTotal;
	/**
	 * This value determines the length the first generated array has. It has to be one at least, since zero
	 * length arrays are treated separately.
	 *
	 * @see #symbArrayInitNumberOfRunsTotal
	 */
	public int symbArrayInitStartingLength;
	/**
	 * This value determines the incrementing strategy for arrays. With each run, the arrays length will be
	 * increased. As there is a limited number of runs in total, it might be desired to not have an linear
	 * increment of one but to increase in another way.
	 *
	 * The available options are:
	 * 0 - linear increasing as determined by symbolicArrayInitializationIncrementationStrategyLinearStepSize
	 *     e.g.: 1, 2, 3, ... or 5, 10, 15, ...
	 * 1 - fibonacci increasing
	 *     e.g.: 0, 1, 1, 2, 3, 5, 8, 13, ...
	 * 2 - exponential increasing, with 1 followed by 2.
	 *     e.g.: 1, 2, 4, 8, 16, ...
	 * 3 - function 10 ^ (x - 1)
	 *     e.g.: 1, 10, 100, 1.000, 10.000, ...
	 *
	 * @see #symbArrayInitNumberOfRunsTotal
	 */
	public int symbArrayInitIncrStrategy;
	/**
	 * This value determines the gap size if the linear increasing strategy has been chosen. The next value is
	 * calculated as x_new = x_old + step_size
	 *
	 * @see #symbArrayInitIncrStrategy
	 */
	public int symbArrayInitIncrStrategyLinearStepSize;
	/**
	 * Toggles whether a array without initialization, i.e. a null reference, is tested. In general, public
	 * methods should be capable of handling this without malfunctioning.
	 *
	 * @see #symbArrayInitNumberOfRunsTotal
	 */
	public boolean symbArrayInitTestNull;
	/**
	 * Toggles whether a array with zero length is tested. In general, public methods should be capable of
	 * handling this without malfunctioning.
	 *
	 * @see #symbArrayInitNumberOfRunsTotal
	 */
	public boolean symbArrayInitTestZeroLengthArray;

	/**
	 * Toggles the dynamic replacement of instructions with optimized ones.<br />
	 * <br />
	 * Some instructions require computationally expensive actions to be taken at runtime. This for
	 * example includes getting elements from the constant pool. Quite a lot of these expensive
	 * operations have to be done every time an instruction is encountered. Hence, it is a good idea
	 * to save the result of the operation and use it the next time the instruction is encountered.
	 * The results will be saved to a new instruction which is the "quick" version of the original
	 * instruction.<br />
	 * <br />
	 * In rare cases, enabling this options can lead to a slow down of the application instead of a
	 * speed up.
	 */
	public boolean dynReplaceInstWithOptimizedOnes;

	/**
	 * Log to a HTML file.
	 */
	private boolean htmlLogging;

	/**
	 * Qualified name of the solver manager that is to be used. 
	 * Needs to implement the interface de.wwu.muggl.solvers.SolverManager.
	 * @see de.wwu.muggl.solvers.SolverManager 
	 */
	public String solverManager;
	
	// Singleton.
	private static final Options OPTIONS = new Options();
	
	/**
	 * Private Constructor.
	 */
	private Options() {
		this.configurationFile = Defaults.CONFIGURATION_FILE;
		this.javaHome = Defaults.JAVA_HOME;
		this.javaVersion = Defaults.JAVA_VERSION;

		// Set the main window options to default values.
		this.methodListHideInitClinit = Defaults.METHODLIST_HIDE_INIT_CLINIT;
		this.methodListShowMainMethodOnly = Defaults.METHODLIST_SHOW_MAIN_METHOD_ONLY;
		this.executionModeSingleSteps = Defaults.EXECUTIONMODE_SINGLE_STEPS;

		// Basic initialization to default values.
		this.classPathEntries = Defaults.CLASSPATH_ENTRIES;
		this.symbolicMode = Defaults.SYMBOLIC_MODE;
		this.logicMode = Defaults.LOGIC_MODE;
		this.jpaMode = Defaults.JPA_MODE;
		this.assumeMissingValues = Defaults.ASSUME_MISSING_VALUES;
		this.askUserMissingValues = Defaults.ASK_USER_MISSING_VALUES;
		this.stepByStepVisuallySkipInvoc = Defaults.STEPBYSTEP_VISUALLY_SKIP_INVOC;
		this.visuallySkipStaticInit = Defaults.VISUALLY_SKIP_STATIC_INIT;
		this.stepByStepShowInstrBytePosition = Defaults.STEPBYSTEP_SHOW_INSTR_BYTE_POSITION;
		this.searchAlgorithm = Defaults.SEARCH_ALGORITHM;
		this.iterativeDeepeningStartingDepth = Defaults.ITERATIVE_DEEPENING_STARTING_DEPTH;
		this.iterativeDeepeningDeepnessIncrement = Defaults.ITERATIVE_DEEPENING_DEEPNESS_INCREMENT;
		this.doNotHaltOnNativeMethods = Defaults.DO_NOT_HALT_ON_NATIVE_METHODS;
		this.assumeNativeReturnValuesToBeZeroNull = Defaults.ASSUME_NATIVE_RETURN_VALUES_TO_BE_ZERO_NULL;
		this.forwardJavaPackageNativeInvoc = Defaults.FORWARD_JAVA_PACKAGE_NATIVE_INVOC;
		this.maximumExecutionTime = Defaults.MAX_EXECUTION_TIME;
		this.maximumLoopsToTake = Defaults.MAX_LOOPS_TO_TAKE;
		this.maxInstrBeforeFindingANewSolution = Defaults.MAX_INSTR_BEFORE_FINDING_A_NEW_SOLUTION;
		this.onlyCountChoicePointGeneratingInst = Defaults.ONLY_COUNT_CHOICEPOINT_GENERATING_INSTR;
		this.maximumStepByStepLoggingEntries = Defaults.MAX_STEPBYSTEP_LOGGING_ENTRIES;
		this.testClassesDirectory = Defaults.TEST_CLASSES_DIRECTORY;
		this.testClassesPackageName = Defaults.TEST_CLASSES_PACKAGE_NAME;
		this.testClassesName = Defaults.TEST_CLASSES_NAME;
		this.maximumLogEntries = Defaults.MAX_LOG_ENTRIES;
		this.measureSymbolicExecutionTime = Defaults.MEASURE_SYMBOLIC_EXECUTION_TIME;
		this.hideDrivesAB = Defaults.HIDE_DRIVES_AB;
		this.doNotClearClassLoaderCache = Defaults.DO_NOT_CLEAR_CLASSLOADER_CACHE;
		// The following value cannot be changed via the GUI currently.
		this.maximumClassLoaderCacheEntries = Defaults.MAX_CLASSLOADER_CACHE_ENTRIES;
		// The following value cannot be changed via the GUI currently.
		this.maximumClassLoaderCacheBytes = Defaults.MAX_CLASSLOADER_CACHE_BYTES;
		this.useDUCoverage = Defaults.USE_DU_COVERAGE;
		this.useCFCoverage = Defaults.USE_CF_COVERAGE;
		this.coverageTracking = Defaults.COVERAGE_TRACKING;
		this.coverageAbortionCriteria = Defaults.COVERAGE_ABORTION_CRITERIA;
		this.eliminateSolutionsByCoverage = Defaults.ELIMINATE_SOLUTIONS_BY_COVERAGE;
		this.numberOfRecentFiles = Defaults.NUMBER_OF_RECENT_FILES;
		this.recentFilesPaths = Defaults.RECENT_FILES_PATH;
		this.rgbFileInspConstantClass = Defaults.RGB_FILEINSP_CONSTANT_CLASS;
		this.rgbFileInspConstantDouble = Defaults.RGB_FILEINSP_CONSTANT_DOUBLE;
		this.rgbFileInspConstantFieldref = Defaults.RGB_FILEINSP_CONSTANT_FIELDRED;
		this.rgbFileInspConstantFloat = Defaults.RGB_FILEINSP_CONSTANT_FLOAT;
		this.rgbFileInspConstantInteger = Defaults.RGB_FILEINSP_CONSTANT_INTEGER;
		this.rgbFileInspConstantInterfaceMethodref = Defaults.RGB_FILEINSP_CONSTANT_INTERFACEMETHODREF;
		this.rgbFileInspConstantLong = Defaults.RGB_FILEINSP_CONSTANT_LONG;
		this.rgbFileInspConstantMethodref = Defaults.RGB_FILEINSP_CONSTANT_METHODREF;
		this.rgbFileInspConstantNameAndType = Defaults.RGB_FILEINSP_CONSTANT_NAMEANDTYPE;
		this.rgbFileInspConstantString = Defaults.RGB_FILEINSP_CONSTANT_STRING;
		this.rgbFileInspConstantUtf8 = Defaults.RGB_FILEINSP_CONSTANT_UTF8;
		this.classFileWriteAccess = Defaults.CLASS_FILE_WRITE_ACCESS;
		this.enableSJVMMultithreading = Defaults.ENABLE_SJVM_MULTITHREADING;
		this.numberOfSimultaneousThreads = Defaults.NUMBER_OF_SIMULTANEOUS_THREADS;
		this.symbArrayInitNumberOfRunsTotal = Defaults.SYMB_ARRAY_INIT_NUMBER_OF_RUNS_TOTAL;
		this.symbArrayInitStartingLength = Defaults.SYMB_ARRAY_INIT_STARTING_LENGTH;
		this.symbArrayInitIncrStrategy = Defaults.SYMB_ARRAY_INIT_INCR_STRATEGY;
		this.symbArrayInitIncrStrategyLinearStepSize = Defaults.SYMB_ARRAY_INIT_INCR_STRATEGY_LINEAR_STEPSIZE;
		this.symbArrayInitTestNull = Defaults.SYMB_ARRAY_INIT_TEST_NULL;
		this.symbArrayInitTestZeroLengthArray = Defaults.SYMB_ARRAY_INIT_TEST_ZERO_LENGTH_ARRAY;
		this.dynReplaceInstWithOptimizedOnes = Defaults.DYN_REPLACE_INSTR_WITH_OPTIMIZED_ONES;
		this.htmlLogging = Defaults.HTML_LOGGING;
		this.solverManager = Defaults.SOLVER_MANAGER;
	}

	/**
	 * Getting the only instance of this class.
	 * @return The only instance of this class.
	 */
	public static Options getInst() {
		return OPTIONS;
	}

	/**
	 * Getter for the htmlLogging field.
	 * @return True, if logging to html files is enabled, false otherwise.
	 */
	public boolean getHtmlLogging() {
		return this.htmlLogging;
	}

	/**
	 * Change the layout type for the log files and start logging with that Layout
	 * to a new log file.
	 * @param htmlLogging True, if logging should be done to HTML files, false if simple text files should be used.
	 */
	public void setHtmlLogging(boolean htmlLogging) {
		if (htmlLogging != this.getHtmlLogging()) {
			this.htmlLogging = htmlLogging;
			Globals.getInst().changeLoggingLayout();
		}
	}

}
