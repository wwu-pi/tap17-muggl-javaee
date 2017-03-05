package de.wwu.muggl.ui.gui.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import de.wwu.muggl.configuration.ConfigReader;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ClassPathEntriesWindow;
import de.wwu.muggl.ui.gui.windows.OptionsWindow;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The composite for the OptionsWindow. It offers most of its element and the corresponding methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-14
 */
public class OptionsComposite extends Composite {
	// Constants.
	private static final int MAX_RECENTLY_OPENED_FILES = 50;
	
	// General fields for the window.
	OptionsWindow parentWindow;
	Shell shell;
	private GridLayout compositeGridLayout;
	MugglClassLoader classLoader;
	boolean needToSave;

	// TabItem "general".
	private Text javaEnvironmentText;
	Text classPathEntriesText;
	Button doNotClearClassLoaderCacheButton;

	// TabItem "fileInspection".
	Image fileInspConstantClassImage;
	Image fileInspConstantDoubleImage;
	Image fileInspConstantFieldrefImage;
	Image fileInspConstantFloatImage;
	Image fileInspConstantIntegerImage;
	Image fileInspConstantInterfaceMethodrefImage;
	Image fileInspConstantLongImage;
	Image fileInspConstantMethodrefImage;
	Image fileInspConstantNameAndTypeImage;
	Image fileInspConstantStringImage;
	Image fileInspConstantUtf8Image;
	Button fileInspConstantClassButton;
	Button fileInspConstantDoubleButton;
	Button fileInspConstantFieldrefButton;
	Button fileInspConstantFloatButton;
	Button fileInspConstantIntegerButton;
	Button fileInspConstantInterfaceMethodrefButton;
	Button fileInspConstantLongButton;
	Button fileInspConstantMethodrefButton;
	Button fileInspConstantNameAndTypeButton;
	Button fileInspConstantStringButton;
	Button fileInspConstantUtf8Button;
	Button classFileWriteAccessButton;

	// TabItem "visual".
	private Combo numberOfRecentlyOpenedFilesCombo;
	Button hideDrivesABButton;
	Button measureSymbolicExecutionTimeButton;
	private Button showEveryInvocationButton;
	private Button skipInvocationOfJavaPackageButton;
	private Button skipInvocationOfOtherClassesButton;
	private Button skipAnyInvocationButton;
	private Button visuallySkipStaticInitializersButton;
	private Button showInstructionBytePositionButton;

	// TabItem "execution".
	private Combo maximumExecutionTimeCombo;
	private Button assumeMissingValuesButton;
	private Button askUserInCasOfMissingValuesButton;
	private Button doNotHaltOnNativeMethodsButton;
	private Button assumeNativeReturnValuesToBeZeroNullButton;
	private Button forwardJavaPackageNativeInvocationsButton;
	Button dynamicallyReplaceInstructionsWithOptimizedOnesButton;

	// TabItem "symbolicExecution".
	private Combo symbolicArrayInitializationNumberOfRunsTotalCombo;
	private Combo symbolicArrayInitializationStartingLengthCombo;
	private Button symbolicArrayInitializationIncrementationStrategyLinearButton;
	private Label symbolicArrayInitializationIncrementationStrategyLinearStepSizeLabel;
	private Combo symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo;
	private Button symbolicArrayInitializationIncrementationStrategyFibonacciButton;
	private Button symbolicArrayInitializationIncrementationStrategyExponentialButton;
	private Button symbolicArrayInitializationIncrementationStrategyTenPowerButton;
	private Button symbolicArrayInitializationTestNullButton;
	private Button symbolicArrayInitializationTestZeroLengthArrayButton;

	// TabItem "searchAlgorithm".
	private Button breadthFirstSearchButton;
	private Button depthFirstSearchButton;
	private Button iterativeDeepeningButton;
	private Label iterativeDeepeningStartingDepthLabel;
	private Combo iterativeDeepeningStartingDepthCombo;
	private Label iterativeDeepeningDeepnessIncrementLabel;
	private Combo iterativeDeepeningDeepnessIncrementCombo;
	Button multithreadingButton;
	Combo numberOfSimultaneousThreadsCombo;
	private Combo maximumLoopsCombo;
	private Combo maximumInstructionsBeforeFindingANewSolutionCombo;
	private Button onlyCountChoicePointGeneratingInstructions;

	// TabItem coverage.
	Button useDefUseCoverageButton;
	Button useControlFlowCoverageButton;
	Button coverageTrackingInitialMethodButton;
	Button coverageTrackingInitialClassButton;
	Button coverageTrackingInitialPackageButton;
	Button coverageTrackingTopLevelPackageButton;
	Button coverageTrackingAnyButton;
	Group coverageAbortionGroup;
	Button coverageAbortionCriteriaNoButton;
	Button coverageAbortionCriteriaDefUseButton;
	Button coverageAbortionCriteriaControlFlowButton;
	Button coverageAbortionCriteriaFullButton;

	// TabItem "testCases".
	private Text testClassesDirectoryText;
	private Button testClassesDirectoryButton;
	private Text testClassesPackageNameText;
	private Text testClassesNameText;
	Group testCaseEliminationGroup;
	Button eliminateSolutionsByCoverageNotButton;
	Button eliminateSolutionsByCoverageDefUseButton;
	Button eliminateSolutionsByCoverageControlFlowButton;
	Button eliminateSolutionsByCoverageBothButton;

	// TabItem "logging".
	private Combo loggingLayoutCombo;
	private Combo maximumEntriesPerLogfileCombo;

	// Buttons.
	private Button okButton;
	private Button applyButton;
	private Button discardButton;
	private Button cancelButton;
	private Button loadDefaultsButton;

	// Fields to temporary store parse data, so this is not done twice for checking and saving.
	private long maximumExecutionTime;
	private int numberOfSimultaneousThreads;
	private int maximumLoops;
	private int maximumInstructionsBeforeFindingANewSolution;
	private int symbolicArrayInitializationNumberOfRunsTotal;
	private int symbolicArrayInitializationStartingLength;
	private int symbolicArrayInitializationIncrementationStrategyLinearStepSize;

	// Fields to temporary store changed data.
	List<String> classPathEntries;
	RGB rgbFileInspectionConstantClass;
	RGB rgbFileInspectionConstantDouble;
	RGB rgbFileInspectionConstantFieldref;
	RGB rgbFileInspectionConstantFloat;
	RGB rgbFileInspectionConstantInteger;
	RGB rgbFileInspectionConstantInterfaceMethodref;
	RGB rgbFileInspectionConstantLong;
	RGB rgbFileInspectionConstantMethodref;
	RGB rgbFileInspectionConstantNameAndType;
	RGB rgbFileInspectionConstantString;
	RGB rgbFileInspectionConstantUtf8;
	
	// Time constants
	private static final int	SECONDS_MINUTE	= 60;
	private static final int	SECONDS_HOUR	= 3600;
	private static final int	SECOND_DAY	= 86400;

	/**
	 * Basic constructor for launching the composite of the options window.
	 *
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param style The style for this composite.
	 * @param classLoader The system MugglClassLoader.
	 */
	public OptionsComposite(
			OptionsWindow parent,
			Shell shell,
			int style,
			MugglClassLoader classLoader
			) {
		// General initialization.
		super(shell, style);
		this.parentWindow = parent;
		this.shell = shell;
		this.classLoader = classLoader;
		this.needToSave = false;

		// Layout
		this.compositeGridLayout = new GridLayout();
		this.compositeGridLayout.numColumns = 5;
		this.setLayout(this.compositeGridLayout);

		// Set up the TabFolder
		final GridData optionsGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		optionsGridData.horizontalSpan = 5;

		final TabFolder optionsTabFolder = new TabFolder(this, SWT.TOP);
		optionsTabFolder.setLayoutData(optionsGridData);

		final RowLayout forGroupsRowLayout = new RowLayout(SWT.VERTICAL);

		final TabItem generalTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		generalTabItem.setText("General");
		final Composite generalComposite = new Composite(optionsTabFolder, SWT.NONE);
		generalComposite.setLayout(forGroupsRowLayout);
		generalTabItem.setControl(generalComposite);

		final TabItem fileInspectionTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		fileInspectionTabItem.setText("File inspection");
		final Composite fileInspectionComposite = new Composite(optionsTabFolder, SWT.NONE);
		fileInspectionComposite.setLayout(forGroupsRowLayout);
		fileInspectionTabItem.setControl(fileInspectionComposite);

		final TabItem visualTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		visualTabItem.setText("Visual");
		final Composite visualComposite = new Composite(optionsTabFolder, SWT.NONE);
		visualComposite.setLayout(forGroupsRowLayout);
		visualTabItem.setControl(visualComposite);

		final TabItem executionTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		executionTabItem.setText("Execution");
		final Composite executionComposite = new Composite(optionsTabFolder, SWT.NONE);
		executionComposite.setLayout(forGroupsRowLayout);
		executionTabItem.setControl(executionComposite);

		final TabItem symbolicExecutionTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		symbolicExecutionTabItem.setText("Symbolic execution");
		final Composite symbolicExecutionComposite = new Composite(optionsTabFolder, SWT.NONE);
		symbolicExecutionComposite.setLayout(forGroupsRowLayout);
		symbolicExecutionTabItem.setControl(symbolicExecutionComposite);

		final TabItem searchAlgorithmTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		searchAlgorithmTabItem.setText("Search algorithm");
		final Composite searchAlgorithmComposite = new Composite(optionsTabFolder, SWT.NONE);
		searchAlgorithmComposite.setLayout(forGroupsRowLayout);
		searchAlgorithmTabItem.setControl(searchAlgorithmComposite);

		final TabItem coverageTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		coverageTabItem.setText("Coverage");
		final Composite coverageComposite = new Composite(optionsTabFolder, SWT.NONE);
		coverageComposite.setLayout(forGroupsRowLayout);
		coverageTabItem.setControl(coverageComposite);

		final TabItem testCasesTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		testCasesTabItem.setText("Test cases");
		final Composite testCasesComposite = new Composite(optionsTabFolder, SWT.NONE);
		testCasesComposite.setLayout(forGroupsRowLayout);
		testCasesTabItem.setControl(testCasesComposite);

		final TabItem loggingTabItem = new TabItem(optionsTabFolder, SWT.NONE);
		loggingTabItem.setText("Logging");
		final Composite loggingComposite = new Composite(optionsTabFolder, SWT.NONE);
		loggingComposite.setLayout(forGroupsRowLayout);
		loggingTabItem.setControl(loggingComposite);

		// Determine the maximum available horizontal space.
		int maxWidth = optionsTabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int maxLabelWidth = maxWidth - 20;

		// Set up the generalComposite.
		final GridLayout environmentGridLayout = new GridLayout(3, false);
		environmentGridLayout.marginHeight = 5;
		environmentGridLayout.marginWidth = 5;

		final Group environmentGroup = new Group(generalComposite, SWT.NONE);
		environmentGroup.setText("Execution environment");
		environmentGroup.setLayout(environmentGridLayout);

		final Label javaEnvironmentLabel = new Label(environmentGroup, SWT.None);
		javaEnvironmentLabel.setText("Java Environment:");

		this.javaEnvironmentText = new Text(environmentGroup, SWT.BORDER | SWT.RIGHT);
		this.javaEnvironmentText.setEditable(false);

		final Button javaEnvironmentButton = new Button(environmentGroup, SWT.None);
		javaEnvironmentButton.setText("Edit...");

		final Label classPathEntriesLabel = new Label(environmentGroup, SWT.None);
		classPathEntriesLabel.setText("Class path entries:");

		this.classPathEntriesText = new Text(environmentGroup, SWT.BORDER | SWT.RIGHT);
		this.classPathEntriesText.setEditable(false);

		final Button classPathEntriesButton = new Button(environmentGroup, SWT.None);
		classPathEntriesButton.setText("Edit...");

		final GridData doNotClearClassLoaderCacheButtonGridData = new GridData();
		doNotClearClassLoaderCacheButtonGridData.horizontalSpan = 3;

		this.doNotClearClassLoaderCacheButton = new Button(environmentGroup, SWT.CHECK);
		this.doNotClearClassLoaderCacheButton.setText("Do not clear the class loader after class path changes");
		this.doNotClearClassLoaderCacheButton.setLayoutData(doNotClearClassLoaderCacheButtonGridData);

		// Set up the fileInspectionComposite.
		final GridLayout colorsGridLayout = new GridLayout(5, false);
		colorsGridLayout.marginHeight = 5;
		colorsGridLayout.marginWidth = 5;

		final Group colorsGroup = new Group(fileInspectionComposite, SWT.NONE);
		colorsGroup.setText("Colors");
		colorsGroup.setLayout(colorsGridLayout);

		// Set up the images.
		this.fileInspConstantClassImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantDoubleImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantFieldrefImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantFloatImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantIntegerImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantInterfaceMethodrefImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantLongImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantMethodrefImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantNameAndTypeImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantStringImage = new Image(getDisplay(), 16, 16);
		this.fileInspConstantUtf8Image = new Image(getDisplay(), 16, 16);

		// And apply them to the corresponding buttons.
		final Label fileInspConstantClassLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantClassLabel.setText("CONSTANT_Class:");
		this.fileInspConstantClassButton = new Button(colorsGroup, SWT.PUSH);

		final GridData colorsSpacerGridData = new GridData(GridData.FILL_VERTICAL);
		colorsSpacerGridData.verticalSpan = 6;
		colorsSpacerGridData.widthHint = 1;

		Sash colorsSpacerSash = new Sash(colorsGroup, SWT.VERTICAL | SWT.BORDER);
		colorsSpacerSash.setLayoutData(colorsSpacerGridData);

		final Label fileInspConstantDoubleLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantDoubleLabel.setText("CONSTANT_Double:");
		this.fileInspConstantDoubleButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantFieldrefLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantFieldrefLabel.setText("CONSTANT_Fieldref:");
		this.fileInspConstantFieldrefButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantFloatLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantFloatLabel.setText("CONSTANT_Float:");
		this.fileInspConstantFloatButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantIntegerLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantIntegerLabel.setText("CONSTANT_Integer:");
		this.fileInspConstantIntegerButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantInterfaceMethodrefLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantInterfaceMethodrefLabel.setText("CONSTANT_InterfaceMethodref:");
		this.fileInspConstantInterfaceMethodrefButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantLongLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantLongLabel.setText("CONSTANT_Long:");
		this.fileInspConstantLongButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantMethodrefLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantMethodrefLabel.setText("CONSTANT_Methodref:");
		this.fileInspConstantMethodrefButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantNameAndTypeLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantNameAndTypeLabel.setText("CONSTANT_NameAndType:");
		this.fileInspConstantNameAndTypeButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantStringLabel = new Label(colorsGroup, SWT.NONE);
		fileInspConstantStringLabel.setText("CONSTANT_String:");
		this.fileInspConstantStringButton = new Button(colorsGroup, SWT.PUSH);

		final Label fileInspConstantUtf8Label = new Label(colorsGroup, SWT.NONE);
		fileInspConstantUtf8Label.setText("CONSTANT_Utf8:");
		this.fileInspConstantUtf8Button = new Button(colorsGroup, SWT.PUSH);

		// The next group...
		final GridLayout classFileWriteAccessGridLayout = new GridLayout(1, false);
		classFileWriteAccessGridLayout.marginHeight = 5;
		classFileWriteAccessGridLayout.marginWidth = 5;

		final Group classFileWriteAccessGroup = new Group(fileInspectionComposite, SWT.NONE);
		classFileWriteAccessGroup.setText("Class file write access");
		classFileWriteAccessGroup.setLayout(classFileWriteAccessGridLayout);

		this.classFileWriteAccessButton = new Button(classFileWriteAccessGroup, SWT.CHECK);
		this.classFileWriteAccessButton.setText("Allow write access to class files");

		// Set up the visualComposite.
		GridLayout guiOptionsGridLayout = new GridLayout(3, false);
		guiOptionsGridLayout.marginHeight = 5;
		guiOptionsGridLayout.marginWidth = 5;

		final Group guiOptionsGroup = new Group(visualComposite, SWT.NONE);
		guiOptionsGroup.setText("Gui options");
		guiOptionsGroup.setLayout(guiOptionsGridLayout);

		final Label numberOfRecentlyOpenedFilesLabel = new Label(guiOptionsGroup, SWT.None);
		numberOfRecentlyOpenedFilesLabel.setText("Number of files in the list of recently openes files:");

		this.numberOfRecentlyOpenedFilesCombo = new Combo(guiOptionsGroup, SWT.BORDER);
		this.numberOfRecentlyOpenedFilesCombo.add("0");
		this.numberOfRecentlyOpenedFilesCombo.add("4");
		this.numberOfRecentlyOpenedFilesCombo.add("5");
		this.numberOfRecentlyOpenedFilesCombo.add("10");
		this.numberOfRecentlyOpenedFilesCombo.add("20");
		this.numberOfRecentlyOpenedFilesCombo.add("50");

		Button numberOfRecentlyOpenedFilesButton = new Button(guiOptionsGroup, SWT.NONE);
		numberOfRecentlyOpenedFilesButton.setText("Clear all entries...");

		final GridData hideDrivesABButtonGridData = new GridData();
		hideDrivesABButtonGridData.horizontalSpan = 3;

		this.hideDrivesABButton = new Button(guiOptionsGroup, SWT.CHECK);
		this.hideDrivesABButton.setText("Hide drives A: and B: on Windows systems");
		this.hideDrivesABButton.setLayoutData(hideDrivesABButtonGridData);

		final GridData measureSymbolicExecutionTimeButtonGridData = new GridData();
		measureSymbolicExecutionTimeButtonGridData.horizontalSpan = 3;

		this.measureSymbolicExecutionTimeButton = new Button(guiOptionsGroup, SWT.CHECK);
		this.measureSymbolicExecutionTimeButton.setText("Measure execution time when executing symbolically");
		this.measureSymbolicExecutionTimeButton.setLayoutData(measureSymbolicExecutionTimeButtonGridData);

		final GridLayout stepByStepGridLayout = new GridLayout(1, false);
		stepByStepGridLayout.marginHeight = 5;
		stepByStepGridLayout.marginWidth = 5;

		final Group stepByStepGroup = new Group(visualComposite, SWT.NONE);
		stepByStepGroup.setText("Step by step execution");
		stepByStepGroup.setLayout(stepByStepGridLayout);

		this.showEveryInvocationButton = new Button(stepByStepGroup, SWT.RADIO);
		this.showEveryInvocationButton.setText("Show &every invocation");

		this.skipInvocationOfJavaPackageButton = new Button(stepByStepGroup, SWT.RADIO);
		this.skipInvocationOfJavaPackageButton.setText("Skip invocation of methods from classes of the &java.* package");

		this.skipInvocationOfOtherClassesButton = new Button(stepByStepGroup, SWT.RADIO);
		this.skipInvocationOfOtherClassesButton.setText("Skip invocation of methods from any &other classes");

		this.skipAnyInvocationButton = new Button(stepByStepGroup, SWT.RADIO);
		this.skipAnyInvocationButton.setText("Skip &any invocation, just display the current method");

		this.visuallySkipStaticInitializersButton = new Button(stepByStepGroup, SWT.CHECK);
		this.visuallySkipStaticInitializersButton.setText("Visually skip static initializers");

		this.showInstructionBytePositionButton = new Button(stepByStepGroup, SWT.CHECK);
		this.showInstructionBytePositionButton.setText("Show instructions' byte positions instead of their chronological number (takes effect when loading instructions)");

		// Set up the executionComposite.
		final GridLayout timeLimitGridLayout = new GridLayout(2, false);
		timeLimitGridLayout.marginHeight = 5;
		timeLimitGridLayout.marginWidth = 5;

		final Group timeLimitGroup = new Group(executionComposite, SWT.NONE);
		timeLimitGroup.setText("Time limit");
		timeLimitGroup.setLayout(timeLimitGridLayout);

		final Label maximumExecutionTimeLabel = new Label(timeLimitGroup, SWT.CHECK);
		maximumExecutionTimeLabel.setText("Maximum execution time: ");

		this.maximumExecutionTimeCombo = new Combo(timeLimitGroup, SWT.BORDER);
		this.maximumExecutionTimeCombo.add("1s");
		this.maximumExecutionTimeCombo.add("3s");
		this.maximumExecutionTimeCombo.add("5s");
		this.maximumExecutionTimeCombo.add("10s");
		this.maximumExecutionTimeCombo.add("15s");
		this.maximumExecutionTimeCombo.add("30s");
		this.maximumExecutionTimeCombo.add("1m");
		this.maximumExecutionTimeCombo.add("2m");
		this.maximumExecutionTimeCombo.add("3m");
		this.maximumExecutionTimeCombo.add("5m");
		this.maximumExecutionTimeCombo.add("10m");
		this.maximumExecutionTimeCombo.add("15m");
		this.maximumExecutionTimeCombo.add("30m");
		this.maximumExecutionTimeCombo.add("1h");
		this.maximumExecutionTimeCombo.add("2h");
		this.maximumExecutionTimeCombo.add("6h");
		this.maximumExecutionTimeCombo.add("12h");
		this.maximumExecutionTimeCombo.add("1d");
		this.maximumExecutionTimeCombo.add("2d");
		this.maximumExecutionTimeCombo.add("5d");

		final GridLayout missingValuesGridLayout = new GridLayout(1, false);
		missingValuesGridLayout.marginHeight = 5;
		missingValuesGridLayout.marginWidth = 5;

		final Group missingValuesGroup = new Group(executionComposite, SWT.NONE);
		missingValuesGroup.setText("Missing values");
		missingValuesGroup.setLayout(missingValuesGridLayout);

		this.assumeMissingValuesButton = new Button(missingValuesGroup, SWT.CHECK);
		this.assumeMissingValuesButton.setText("Assume missing values to be null respectively 0");

		this.askUserInCasOfMissingValuesButton = new Button(missingValuesGroup, SWT.CHECK);
		this.askUserInCasOfMissingValuesButton.setText("Ask for user input in case of missing values (step by step mode only)");

		final GridLayout handlingOfNativeMethodsGridLayout = new GridLayout(1, false);
		handlingOfNativeMethodsGridLayout.marginHeight = 5;
		handlingOfNativeMethodsGridLayout.marginWidth = 5;

		final Group handlingOfNativeMethodsGroup = new Group(executionComposite, SWT.NONE);
		handlingOfNativeMethodsGroup.setText("Handling of native methods");
		handlingOfNativeMethodsGroup.setLayout(handlingOfNativeMethodsGridLayout);

		this.doNotHaltOnNativeMethodsButton = new Button(handlingOfNativeMethodsGroup, SWT.CHECK);
		this.doNotHaltOnNativeMethodsButton.setText("Do not halt if encountering native method calls");

		this.assumeNativeReturnValuesToBeZeroNullButton = new Button(handlingOfNativeMethodsGroup, SWT.CHECK);
		this.assumeNativeReturnValuesToBeZeroNullButton.setText("Assume the return values of native methods to be zero respectively null");

		this.forwardJavaPackageNativeInvocationsButton = new Button(handlingOfNativeMethodsGroup, SWT.CHECK);
		this.forwardJavaPackageNativeInvocationsButton.setText("Forward native calls of methods from the java package");

		final GridLayout optimizationGridLayout = new GridLayout(1, false);
		optimizationGridLayout.marginHeight = 5;
		optimizationGridLayout.marginWidth = 5;

		final Group optimizationGroup = new Group(executionComposite, SWT.NONE);
		optimizationGroup.setText("Optimization techniques");
		optimizationGroup.setLayout(handlingOfNativeMethodsGridLayout);

		this.dynamicallyReplaceInstructionsWithOptimizedOnesButton = new Button(optimizationGroup, SWT.CHECK);
		this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.setText("Dynamically replace instructions with optimized ones (recommended)");

		// Set up the symbolicExecutionComposite.
		final GridLayout arrayInitializationGridLayout = new GridLayout(2, false);
		arrayInitializationGridLayout.marginHeight = 5;
		arrayInitializationGridLayout.marginWidth = 5;

		final Group arrayInitializationGroup = new Group(symbolicExecutionComposite, SWT.NONE);
		arrayInitializationGroup.setText("Array initialization");
		arrayInitializationGroup.setLayout(arrayInitializationGridLayout);

		final Label symbolicArrayInitializationNumberOfRunsTotalLabel = new Label(arrayInitializationGroup, SWT.NONE);
		symbolicArrayInitializationNumberOfRunsTotalLabel.setText("Number of total runs:");

		final GridData symbolicArrayInitializationNumberOfRunsTotalComboGridData = new GridData();
		symbolicArrayInitializationNumberOfRunsTotalComboGridData.widthHint = 50;

		this.symbolicArrayInitializationNumberOfRunsTotalCombo = new Combo(arrayInitializationGroup, SWT.BORDER);
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("5");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("10");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("25");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("50");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("100");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("250");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("500");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("1.000");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("2.500");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("5.000");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("10.000");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("25.000");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("50.000");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("100.000");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.add("250.000");
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.setLayoutData(symbolicArrayInitializationNumberOfRunsTotalComboGridData);

		final Label symbolicArrayInitializationStartingLengthLabel = new Label(arrayInitializationGroup, SWT.NONE);
		symbolicArrayInitializationStartingLengthLabel.setText("Starting length:");

		final GridData symbolicArrayInitializationStartingLengthComboGridData = new GridData();
		symbolicArrayInitializationStartingLengthComboGridData.widthHint = 50;

		this.symbolicArrayInitializationStartingLengthCombo = new Combo(arrayInitializationGroup, SWT.BORDER);
		this.symbolicArrayInitializationStartingLengthCombo.add("1");
		this.symbolicArrayInitializationStartingLengthCombo.add("2");
		this.symbolicArrayInitializationStartingLengthCombo.add("3");
		this.symbolicArrayInitializationStartingLengthCombo.add("4");
		this.symbolicArrayInitializationStartingLengthCombo.add("5");
		this.symbolicArrayInitializationStartingLengthCombo.add("8");
		this.symbolicArrayInitializationStartingLengthCombo.add("10");
		this.symbolicArrayInitializationStartingLengthCombo.add("16");
		this.symbolicArrayInitializationStartingLengthCombo.add("25");
		this.symbolicArrayInitializationStartingLengthCombo.add("32");
		this.symbolicArrayInitializationStartingLengthCombo.add("50");
		this.symbolicArrayInitializationStartingLengthCombo.add("64");
		this.symbolicArrayInitializationStartingLengthCombo.add("100");
		this.symbolicArrayInitializationStartingLengthCombo.add("128");
		this.symbolicArrayInitializationStartingLengthCombo.setLayoutData(symbolicArrayInitializationStartingLengthComboGridData);

		final GridData symbolicArrayInitializationIncrementationStrategyLabelGridData = new GridData();
		symbolicArrayInitializationIncrementationStrategyLabelGridData.horizontalSpan = 2;

		final Label symbolicArrayInitializationIncrementationStrategyLabel = new Label(arrayInitializationGroup, SWT.NONE);
		symbolicArrayInitializationIncrementationStrategyLabel.setText("Incrementation strategy:");
		symbolicArrayInitializationIncrementationStrategyLabel.setLayoutData(symbolicArrayInitializationIncrementationStrategyLabelGridData);

		final GridData symbolicArrayInitializationIncrementationStrategyLinearButtonGridData = new GridData();
		symbolicArrayInitializationIncrementationStrategyLinearButtonGridData.horizontalSpan = 2;

		this.symbolicArrayInitializationIncrementationStrategyLinearButton = new Button(arrayInitializationGroup, SWT.RADIO);
		this.symbolicArrayInitializationIncrementationStrategyLinearButton.setText("Linear");
		this.symbolicArrayInitializationIncrementationStrategyLinearButton.setLayoutData(symbolicArrayInitializationIncrementationStrategyLinearButtonGridData);

		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeLabel = new Label(arrayInitializationGroup, SWT.NONE);
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeLabel.setText("Linear increment:");

		final GridData symbolicArrayInitializationIncrementationStrategyLinearStepSizeComboGridData = new GridData();
		symbolicArrayInitializationIncrementationStrategyLinearStepSizeComboGridData.widthHint = 50;

		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo = new Combo(arrayInitializationGroup, SWT.BORDER);
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("1");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("2");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("3");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("5");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("10");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("20");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("50");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("100");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("1.000");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("10.0000");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("100.000");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.add("1.000.000");
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.setLayoutData(symbolicArrayInitializationIncrementationStrategyLinearStepSizeComboGridData);

		final GridData symbolicArrayInitializationIncrementationStrategyFibonacciButtonGridData = new GridData();
		symbolicArrayInitializationIncrementationStrategyFibonacciButtonGridData.horizontalSpan = 2;

		this.symbolicArrayInitializationIncrementationStrategyFibonacciButton = new Button(arrayInitializationGroup, SWT.RADIO);
		this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.setText("Fibonacci");
		this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.setLayoutData(symbolicArrayInitializationIncrementationStrategyFibonacciButtonGridData);

		final GridData symbolicArrayInitializationIncrementationStrategyExponentialButtonGridData = new GridData();
		symbolicArrayInitializationIncrementationStrategyExponentialButtonGridData.horizontalSpan = 2;

		this.symbolicArrayInitializationIncrementationStrategyExponentialButton = new Button(arrayInitializationGroup, SWT.RADIO);
		this.symbolicArrayInitializationIncrementationStrategyExponentialButton.setText("Exponential");
		this.symbolicArrayInitializationIncrementationStrategyExponentialButton.setLayoutData(symbolicArrayInitializationIncrementationStrategyExponentialButtonGridData);

		final GridData symbolicArrayInitializationIncrementationStrategyTenPowerButtonGridData = new GridData();
		symbolicArrayInitializationIncrementationStrategyTenPowerButtonGridData.horizontalSpan = 2;

		this.symbolicArrayInitializationIncrementationStrategyTenPowerButton = new Button(arrayInitializationGroup, SWT.RADIO);
		this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.setText("10 ^ (x - 1)");
		this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.setLayoutData(symbolicArrayInitializationIncrementationStrategyTenPowerButtonGridData);

		final GridData symbolicArrayInitializationTestNullButtonGridData = new GridData();
		symbolicArrayInitializationTestNullButtonGridData.horizontalSpan = 2;

		this.symbolicArrayInitializationTestNullButton = new Button(arrayInitializationGroup, SWT.CHECK);
		this.symbolicArrayInitializationTestNullButton.setText("Test an array without initialization (null reference)");
		this.symbolicArrayInitializationTestNullButton.setLayoutData(symbolicArrayInitializationTestNullButtonGridData);

		final GridData symbolicArrayInitializationTestZeroLengthArrayButtonGridData = new GridData();
		symbolicArrayInitializationTestZeroLengthArrayButtonGridData.horizontalSpan = 2;

		this.symbolicArrayInitializationTestZeroLengthArrayButton = new Button(arrayInitializationGroup, SWT.CHECK);
		this.symbolicArrayInitializationTestZeroLengthArrayButton.setText("Test an array of zero length");
		this.symbolicArrayInitializationTestZeroLengthArrayButton.setLayoutData(symbolicArrayInitializationTestZeroLengthArrayButtonGridData);

		// Set up the searchAlgorithmComposite.
		final GridLayout searchAlgorithmGridLayout = new GridLayout(4, false);
		searchAlgorithmGridLayout.marginHeight = 5;
		searchAlgorithmGridLayout.marginWidth = 5;

		final Group searchAlgorithmGroup = new Group(searchAlgorithmComposite, SWT.NONE);
		searchAlgorithmGroup.setText("Search algorithm");
		searchAlgorithmGroup.setLayout(searchAlgorithmGridLayout);

		final GridData twoRowsFourColumnsSpanGridData = new GridData();
		twoRowsFourColumnsSpanGridData.verticalSpan = 2;
		twoRowsFourColumnsSpanGridData.horizontalSpan = 4;
		twoRowsFourColumnsSpanGridData.widthHint = maxLabelWidth;

		final Label searchAlgorithmLabel = new Label(searchAlgorithmGroup, SWT.WRAP);
		searchAlgorithmLabel.setText("The algorithm has a major effect on both the runtime and the effectivity, especially in combination with other options. Please refer to the manual before changing it.");
		searchAlgorithmLabel.setLayoutData(twoRowsFourColumnsSpanGridData);

		final GridData breadthFirstSearchButtonGridData = new GridData();
		breadthFirstSearchButtonGridData.horizontalSpan = 4;

		this.breadthFirstSearchButton = new Button(searchAlgorithmGroup, SWT.RADIO);
		this.breadthFirstSearchButton.setText("&Breadth first");
		this.breadthFirstSearchButton.setLayoutData(breadthFirstSearchButtonGridData);
		this.breadthFirstSearchButton.setEnabled(false);

		final GridData depthFirstSearchButtonGridData = new GridData();
		depthFirstSearchButtonGridData.horizontalSpan = 4;

		this.depthFirstSearchButton = new Button(searchAlgorithmGroup, SWT.RADIO);
		this.depthFirstSearchButton.setText("&Depth first");
		this.depthFirstSearchButton.setLayoutData(depthFirstSearchButtonGridData);

		final GridData iterativeDeepeningButtonGridData = new GridData();
		iterativeDeepeningButtonGridData.horizontalSpan = 4;

		this.iterativeDeepeningButton = new Button(searchAlgorithmGroup, SWT.RADIO);
		this.iterativeDeepeningButton.setText("&Iterative deepening");
		this.iterativeDeepeningButton.setLayoutData(iterativeDeepeningButtonGridData);

		this.iterativeDeepeningStartingDepthLabel = new Label(searchAlgorithmGroup, SWT.NONE);
		this.iterativeDeepeningStartingDepthLabel.setText("Starting depth:");

		this.iterativeDeepeningStartingDepthCombo = new Combo(searchAlgorithmGroup, SWT.DROP_DOWN);
		this.iterativeDeepeningStartingDepthCombo.add("2");
		this.iterativeDeepeningStartingDepthCombo.add("3");
		this.iterativeDeepeningStartingDepthCombo.add("5");
		this.iterativeDeepeningStartingDepthCombo.add("10");
		this.iterativeDeepeningStartingDepthCombo.add("20");
		this.iterativeDeepeningStartingDepthCombo.add("50");
		this.iterativeDeepeningStartingDepthCombo.add("100");
		this.iterativeDeepeningStartingDepthCombo.add("200");
		this.iterativeDeepeningStartingDepthCombo.add("500");
		this.iterativeDeepeningStartingDepthCombo.add("1.000");

		this.iterativeDeepeningDeepnessIncrementLabel = new Label(searchAlgorithmGroup, SWT.NONE);
		this.iterativeDeepeningDeepnessIncrementLabel.setText("Deepening:");

		this.iterativeDeepeningDeepnessIncrementCombo = new Combo(searchAlgorithmGroup, SWT.DROP_DOWN);
		this.iterativeDeepeningDeepnessIncrementCombo.add("1");
		this.iterativeDeepeningDeepnessIncrementCombo.add("2");
		this.iterativeDeepeningDeepnessIncrementCombo.add("3");
		this.iterativeDeepeningDeepnessIncrementCombo.add("5");
		this.iterativeDeepeningDeepnessIncrementCombo.add("10");
		this.iterativeDeepeningDeepnessIncrementCombo.add("20");
		this.iterativeDeepeningDeepnessIncrementCombo.add("50");
		this.iterativeDeepeningDeepnessIncrementCombo.add("100");
		this.iterativeDeepeningDeepnessIncrementCombo.add("200");

		final GridData searchAlgorithmNoteLabelGridData = new GridData();
		searchAlgorithmNoteLabelGridData.horizontalSpan = 4;

		final Label searchAlgorithmNoteLabel = new Label(searchAlgorithmGroup, SWT.NONE);
		searchAlgorithmNoteLabel.setText("Changing the search algorithm will have no effect until restarting the execution.");
		searchAlgorithmNoteLabel.setLayoutData(searchAlgorithmNoteLabelGridData);

		final GridLayout multithreadingGridLayout = new GridLayout(2, false);
		multithreadingGridLayout.marginHeight = 5;
		multithreadingGridLayout.marginWidth = 5;

		final Group multithreadingGroup = new Group(searchAlgorithmComposite, SWT.NONE);
		multithreadingGroup.setText("SJVM multithreading");
		multithreadingGroup.setLayout(multithreadingGridLayout);

		final GridData multithreadingButtonGridData = new GridData();
		multithreadingButtonGridData.horizontalSpan = 2;

		this.multithreadingButton = new Button(multithreadingGroup, SWT.CHECK);
		this.multithreadingButton.setText("Enable SJVM multithreading");
		this.multithreadingButton.setLayoutData(multithreadingButtonGridData);

		final Label numberOfSimultaneousThreadsLabel = new Label(multithreadingGroup, SWT.NONE);
		numberOfSimultaneousThreadsLabel.setText("Number of parallel threads: ");

		this.numberOfSimultaneousThreadsCombo = new Combo(multithreadingGroup, SWT.BORDER);
		this.numberOfSimultaneousThreadsCombo.add("2");
		this.numberOfSimultaneousThreadsCombo.add("4");
		this.numberOfSimultaneousThreadsCombo.add("8");
		this.numberOfSimultaneousThreadsCombo.add("16");
		this.numberOfSimultaneousThreadsCombo.add("32");
		this.numberOfSimultaneousThreadsCombo.add("64");
		this.numberOfSimultaneousThreadsCombo.add("128");

		final GridData multithreadingLabelGridData = new GridData();
		multithreadingLabelGridData.horizontalSpan = 2;
		multithreadingLabelGridData.widthHint = maxLabelWidth;

		final Label multithreadingLabel = new Label(multithreadingGroup, SWT.WRAP);
		multithreadingLabel.setText("You should only enable SVJM multithreading if the machine you run this application on "
										+ "has more than one physical core. Do not set this to a higher value than cores available "
										+ "to the JVM executing this application (which is " + Runtime.getRuntime().availableProcessors() + " currently). "
										+ "Beside that, we recommend to keep one out of eight or so cores reserved for other "
										+ "threads, the OS etc.");
		multithreadingLabel.setLayoutData(multithreadingLabelGridData);

		final GridLayout forcedBacktrackingGridLayout = new GridLayout(2, false);
		forcedBacktrackingGridLayout.marginHeight = 5;
		forcedBacktrackingGridLayout.marginWidth = 5;

		final Group forcedBacktrackingGroup = new Group(searchAlgorithmComposite, SWT.NONE);
		forcedBacktrackingGroup.setText("Forced Backtracking");
		forcedBacktrackingGroup.setLayout(forcedBacktrackingGridLayout);

		final Label maximumLoopsLabel = new Label(forcedBacktrackingGroup, SWT.CHECK);
		maximumLoopsLabel.setText("Maximum loops to take: ");

		this.maximumLoopsCombo = new Combo(forcedBacktrackingGroup, SWT.BORDER);
		this.maximumLoopsCombo.add("1");
		this.maximumLoopsCombo.add("2");
		this.maximumLoopsCombo.add("3");
		this.maximumLoopsCombo.add("5");
		this.maximumLoopsCombo.add("10");
		this.maximumLoopsCombo.add("20");
		this.maximumLoopsCombo.add("50");
		this.maximumLoopsCombo.add("100");
		this.maximumLoopsCombo.add("200");
		this.maximumLoopsCombo.add("500");
		this.maximumLoopsCombo.add("1.000");
		this.maximumLoopsCombo.add("2.500");
		this.maximumLoopsCombo.add("5.000");
		this.maximumLoopsCombo.add("10.000");
		this.maximumLoopsCombo.add("25.000");
		this.maximumLoopsCombo.add("50.000");
		this.maximumLoopsCombo.add("100.000");
		this.maximumLoopsCombo.add("infinite");

		final GridLayout abortionCriteriaGridLayout = new GridLayout(2, false);
		abortionCriteriaGridLayout.marginHeight = 5;
		abortionCriteriaGridLayout.marginWidth = 5;

		final Group abortionCriteriaGroup = new Group(searchAlgorithmComposite, SWT.NONE);
		abortionCriteriaGroup.setText("Abortion criteria");
		abortionCriteriaGroup.setLayout(abortionCriteriaGridLayout);

		final GridData maximumInstructionsBeforeFindingANewSolutionLabelGridData = new GridData();
		maximumInstructionsBeforeFindingANewSolutionLabelGridData.horizontalSpan = 2;

		final Label maximumInstructionsBeforeFindingANewSolutionLabel = new Label(abortionCriteriaGroup, SWT.NONE);
		maximumInstructionsBeforeFindingANewSolutionLabel.setText("Maximum instructions before finding a new solution:");
		maximumInstructionsBeforeFindingANewSolutionLabel.setLayoutData(maximumInstructionsBeforeFindingANewSolutionLabelGridData);

		this.maximumInstructionsBeforeFindingANewSolutionCombo = new Combo(abortionCriteriaGroup, SWT.BORDER);
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("10");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("50");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("100");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("500");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("1.000");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("5.000");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("10.000");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("25.000");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("50.000");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("100.000");
		this.maximumInstructionsBeforeFindingANewSolutionCombo.add("infinite");

		final GridData horizontallyIndent20pxGridData = new GridData();
		horizontallyIndent20pxGridData.horizontalIndent = 20;

		this.onlyCountChoicePointGeneratingInstructions = new Button(abortionCriteriaGroup, SWT.CHECK);
		this.onlyCountChoicePointGeneratingInstructions.setText("Only count choice point generating instructions");
		this.onlyCountChoicePointGeneratingInstructions.setLayoutData(horizontallyIndent20pxGridData);

		// Set up the coverageComposite.
		final GridLayout coverageUsageAndTrackingGridLayout = new GridLayout(1, false);
		coverageUsageAndTrackingGridLayout.marginHeight = 5;
		coverageUsageAndTrackingGridLayout.marginWidth = 5;

		final Group coverageUsageAndTrackingGroup = new Group(coverageComposite, SWT.NONE);
		coverageUsageAndTrackingGroup.setText("General test cases settings");
		coverageUsageAndTrackingGroup.setLayout(coverageUsageAndTrackingGridLayout);

		this.useDefUseCoverageButton = new Button(coverageUsageAndTrackingGroup, SWT.CHECK);
		this.useDefUseCoverageButton.setText("Generate and trace def-use chains");

		this.useControlFlowCoverageButton = new Button(coverageUsageAndTrackingGroup, SWT.CHECK);
		this.useControlFlowCoverageButton.setText("Generate a control graph and trace the control flow");

		final GridData coveragerTrackingLabelGridData = new GridData();
		coveragerTrackingLabelGridData.widthHint = maxLabelWidth;

		final Label coveragerTrackingLabel = new Label(coverageUsageAndTrackingGroup, SWT.WRAP);
		coveragerTrackingLabel.setText("When tracking the coverage of data and/or control flow, this can "
									  + "be limited to the intially executed method or expanded to method "
									  + "invocations. However, reaching full coverage in invoked methods "
									  + "might be impossible to reach.\n\n"
									  + "Tracking...");
		coveragerTrackingLabel.setLayoutData(coveragerTrackingLabelGridData);

		this.coverageTrackingInitialMethodButton = new Button(coverageUsageAndTrackingGroup, SWT.RADIO);
		this.coverageTrackingInitialMethodButton.setText("the initial method only.");

		this.coverageTrackingInitialClassButton = new Button(coverageUsageAndTrackingGroup, SWT.RADIO);
		this.coverageTrackingInitialClassButton.setText("the initial method and any method it invokes from its own class.");

		this.coverageTrackingInitialPackageButton = new Button(coverageUsageAndTrackingGroup, SWT.RADIO);
		this.coverageTrackingInitialPackageButton.setText("the initial method and any method it invokes from its own package.");

		this.coverageTrackingTopLevelPackageButton = new Button(coverageUsageAndTrackingGroup, SWT.RADIO);
		this.coverageTrackingTopLevelPackageButton.setText("the initial method and method from the top-level package or it.");

		this.coverageTrackingAnyButton = new Button(coverageUsageAndTrackingGroup, SWT.RADIO);
		this.coverageTrackingAnyButton.setText("anything (not recommended).");

		final GridLayout coverageAbortionGridLayout = new GridLayout(1, false);
		coverageAbortionGridLayout.marginHeight = 5;
		coverageAbortionGridLayout.marginWidth = 5;

		this.coverageAbortionGroup = new Group(coverageComposite, SWT.NONE);
		this.coverageAbortionGroup.setText("General test cases settings");
		this.coverageAbortionGroup.setLayout(coverageAbortionGridLayout);

		final Label coverageAbortionLabel = new Label(this.coverageAbortionGroup, SWT.NONE);
		coverageAbortionLabel.setText("Abortion when coverage is reached:");

		this.coverageAbortionCriteriaNoButton = new Button(this.coverageAbortionGroup, SWT.RADIO);
		this.coverageAbortionCriteriaNoButton.setText("Never abort when meeting coverage criteria.");

		this.coverageAbortionCriteriaDefUseButton = new Button(this.coverageAbortionGroup, SWT.RADIO);
		this.coverageAbortionCriteriaDefUseButton.setText("Abort when full def-use coverage is reached.");

		this.coverageAbortionCriteriaControlFlowButton = new Button(this.coverageAbortionGroup, SWT.RADIO);
		this.coverageAbortionCriteriaControlFlowButton.setText("Abort when full control flow coverage is reached (not recommended).");

		this.coverageAbortionCriteriaFullButton = new Button(this.coverageAbortionGroup, SWT.RADIO);
		this.coverageAbortionCriteriaFullButton.setText("Abort only if full def-use and control flow coverage is reached (not recommended).");

		// Set up the testCasesComposite.
		final GridLayout testCaseSettingsGridLayout = new GridLayout(3, false);
		testCaseSettingsGridLayout.marginHeight = 5;
		testCaseSettingsGridLayout.marginWidth = 5;

		final Group testCaseSettingsGroup = new Group(testCasesComposite, SWT.NONE);
		testCaseSettingsGroup.setText("General test cases settings");
		testCaseSettingsGroup.setLayout(testCaseSettingsGridLayout);

		final Label testClassesDirectoryLabel = new Label(testCaseSettingsGroup, SWT.NONE);
		testClassesDirectoryLabel.setText("Directory for test case classes:");

		final GridData testClassesDirectoryTextGridData = new GridData();
		testClassesDirectoryTextGridData.minimumWidth = 100;
		testClassesDirectoryTextGridData.widthHint = 270;

		this.testClassesDirectoryText = new Text(testCaseSettingsGroup, SWT.BORDER);
		this.testClassesDirectoryText.setLayoutData(testClassesDirectoryTextGridData);

		this.testClassesDirectoryButton = new Button(testCaseSettingsGroup, SWT.NONE);
		this.testClassesDirectoryButton.setText("Browse...");

		final Label testClassesPackageNameLabel = new Label(testCaseSettingsGroup, SWT.NONE);
		testClassesPackageNameLabel.setText("Package for test case classes:");

		final GridData testClassesPackageNameTextGridData = new GridData();
		testClassesPackageNameTextGridData.minimumWidth = 100;
		testClassesPackageNameTextGridData.widthHint = 270;

		this.testClassesPackageNameText = new Text(testCaseSettingsGroup, SWT.BORDER);
		this.testClassesPackageNameText.setLayoutData(testClassesPackageNameTextGridData);

		final Label spacingLabel01 = new Label(testCaseSettingsGroup, SWT.NONE);

		final Label testClassesNameLabel = new Label(testCaseSettingsGroup, SWT.NONE);
		testClassesNameLabel.setText("Name prefix of test classes:");

		final GridData testClassesNameTextGridData = new GridData();
		testClassesNameTextGridData.minimumWidth = 100;
		testClassesNameTextGridData.widthHint = 270;

		this.testClassesNameText = new Text(testCaseSettingsGroup, SWT.BORDER);
		this.testClassesNameText.setLayoutData(testClassesNameTextGridData);

		final Label spacingLabel02 = new Label(testCaseSettingsGroup, SWT.NONE);

		final GridData twoRowsThreeColumnsSpanGridData = new GridData();
		twoRowsThreeColumnsSpanGridData.verticalSpan = 2;
		twoRowsThreeColumnsSpanGridData.horizontalSpan = 3;
		twoRowsThreeColumnsSpanGridData.widthHint = maxLabelWidth;

		final Label testCaseSettingsNoteLabel = new Label(testCaseSettingsGroup, SWT.WRAP);
		testCaseSettingsNoteLabel.setText("Please make sure the the package and the class name follow java coding standards. "
											+ "Furthermore, the directory should be chosen in accordance with the package name.");
		testCaseSettingsNoteLabel.setLayoutData(twoRowsThreeColumnsSpanGridData);

		final GridLayout testCaseEliminationGridLayout = new GridLayout(1, false);
		testCaseEliminationGridLayout.marginHeight = 5;
		testCaseEliminationGridLayout.marginWidth = 5;

		this.testCaseEliminationGroup = new Group(testCasesComposite, SWT.NONE);
		this.testCaseEliminationGroup.setText("Test cases elimination");
		this.testCaseEliminationGroup.setLayout(testCaseEliminationGridLayout);

		this.eliminateSolutionsByCoverageNotButton = new Button(this.testCaseEliminationGroup, SWT.RADIO);
		this.eliminateSolutionsByCoverageNotButton.setText("Do not eliminate test cases by their coverage contribution.");

		this.eliminateSolutionsByCoverageDefUseButton = new Button(this.testCaseEliminationGroup, SWT.RADIO);
		this.eliminateSolutionsByCoverageDefUseButton.setText("Eliminate test cases by def-use chain coverage.");

		this.eliminateSolutionsByCoverageControlFlowButton = new Button(this.testCaseEliminationGroup, SWT.RADIO);
		this.eliminateSolutionsByCoverageControlFlowButton.setText("Eliminate test cases by control flow graph edge coverage.");

		this.eliminateSolutionsByCoverageBothButton = new Button(this.testCaseEliminationGroup, SWT.RADIO);
		this.eliminateSolutionsByCoverageBothButton.setText("Eliminate test cases both by their contribution to def-use chain and control flow coverage.");

		// Set up the loggingComposite.
		final GridLayout loggingGridLayout = new GridLayout(2, false);
		loggingGridLayout.marginHeight = 5;
		loggingGridLayout.marginWidth = 5;

		final Group loggingGroup = new Group(loggingComposite, SWT.NONE);
		loggingGroup.setText("Logging");
		loggingGroup.setLayout(loggingGridLayout);

		final Label loggingLayoutLabel = new Label(loggingGroup, SWT.NONE);
		loggingLayoutLabel.setText("Logging Layout: ");

		this.loggingLayoutCombo = new Combo(loggingGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.loggingLayoutCombo.add("HTML (recommended)");
		this.loggingLayoutCombo.add("Simple text");

		Label maximumEntriesPerLogfileLabel = new Label(loggingGroup, SWT.NONE);
		maximumEntriesPerLogfileLabel.setText("Maxmimum entries per logfile: ");

		this.maximumEntriesPerLogfileCombo = new Combo(loggingGroup, SWT.DROP_DOWN);
		this.maximumEntriesPerLogfileCombo.add("infinite");
		this.maximumEntriesPerLogfileCombo.add("10");
		this.maximumEntriesPerLogfileCombo.add("50");
		this.maximumEntriesPerLogfileCombo.add("100");
		this.maximumEntriesPerLogfileCombo.add("500");
		this.maximumEntriesPerLogfileCombo.add("1.000");
		this.maximumEntriesPerLogfileCombo.add("5.000");
		this.maximumEntriesPerLogfileCombo.add("10.000");
		this.maximumEntriesPerLogfileCombo.add("50.000");
		this.maximumEntriesPerLogfileCombo.add("100.000");
		this.maximumEntriesPerLogfileCombo.add("500.000");
		this.maximumEntriesPerLogfileCombo.add("1.000.000");
		this.maximumEntriesPerLogfileCombo.add("5.000.000");
		this.maximumEntriesPerLogfileCombo.add("10.000.000");

		// Pack the groups.
		environmentGroup.pack();
		colorsGroup.pack();
		classFileWriteAccessGroup.pack();
		guiOptionsGroup.pack();
		stepByStepGroup.pack();
		timeLimitGroup.pack();
		missingValuesGroup.pack();
		handlingOfNativeMethodsGroup.pack();
		optimizationGroup.pack();
		arrayInitializationGroup.pack();
		searchAlgorithmGroup.pack();
		multithreadingGroup.pack();
		forcedBacktrackingGroup.pack();
		abortionCriteriaGroup.pack();
		coverageUsageAndTrackingGroup.pack();
		this.coverageAbortionGroup.pack();
		testCaseSettingsGroup.pack();
		this.testCaseEliminationGroup.pack();
		loggingGroup.pack();

		/* Old way to determine the maximum width.
		// Resize the groups to the preferred width. First get the maximum width.
		int maxWidth = 0;
		if (this.environmentGroup.getSize().x > maxWidth) maxWidth = this.environmentGroup.getSize().x;
		if (this.guiOptionsGroup.getSize().x > maxWidth) maxWidth = this.guiOptionsGroup.getSize().x;
		if (this.stepByStepGroup.getSize().x > maxWidth) maxWidth = this.stepByStepGroup.getSize().x;
		if (this.timeLimitGroup.getSize().x > maxWidth) maxWidth = this.timeLimitGroup.getSize().x;
		if (this.missingValuesGroup.getSize().x > maxWidth) maxWidth = this.missingValuesGroup.getSize().x;
		if (this.handlingOfNativeMethodsGroup.getSize().x > maxWidth) maxWidth = this.handlingOfNativeMethodsGroup.getSize().x;
		if (this.arrayInitializationGroup.getSize().x > maxWidth) maxWidth = this.arrayInitializationGroup.getSize().x;
		if (this.searchAlgorithmGroup.getSize().x > maxWidth) maxWidth = this.searchAlgorithmGroup.getSize().x;
		if (this.multithreadingGroup.getSize().x > maxWidth) maxWidth = this.multithreadingGroup.getSize().x;
		if (this.forcedBacktrackingGroup.getSize().x > maxWidth) maxWidth = this.forcedBacktrackingGroup.getSize().x;
		if (this.abortionCriteriaGroup.getSize().x > maxWidth) maxWidth = this.abortionCriteriaGroup.getSize().x;
		if (this.testCaseSettingsGroup.getSize().x > maxWidth) maxWidth = this.testCaseSettingsGroup.getSize().x;
		if (this.testCaseEliminationGroup.getSize().x > maxWidth) maxWidth = this.testCaseEliminationGroup.getSize().x;
		if (this.loggingGroup.getSize().x > maxWidth) maxWidth = this.loggingGroup.getSize().x;
		*/

		// Now set the width.
		environmentGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		colorsGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		classFileWriteAccessGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		guiOptionsGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		stepByStepGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		timeLimitGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		missingValuesGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		handlingOfNativeMethodsGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		optimizationGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		arrayInitializationGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		searchAlgorithmGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		multithreadingGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		forcedBacktrackingGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		abortionCriteriaGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		coverageUsageAndTrackingGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		this.coverageAbortionGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		testCaseSettingsGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		this.testCaseEliminationGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));
		loggingGroup.setLayoutData(new RowData(maxWidth, SWT.DEFAULT));

		// Pack the composites.
		generalComposite.pack();
		fileInspectionComposite.pack();
		visualComposite.pack();
		executionComposite.pack();
		symbolicExecutionComposite.pack();
		searchAlgorithmComposite.pack();
		coverageComposite.pack();
		testCasesComposite.pack();
		loggingComposite.pack();

		// Pack the TabFolder.
		optionsTabFolder.pack();

		// Set up the Buttons.
		final GridData okGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		okGridData.widthHint = 70;
		okGridData.heightHint = 20;

		this.okButton = new Button(this, SWT.NONE);
		this.okButton.setText("&Ok");
		this.okButton.setSelection(false);
		this.okButton.setLayoutData(okGridData);

		final GridData applyGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		applyGridData.widthHint = 70;
		applyGridData.heightHint = 20;

		this.applyButton = new Button(this, SWT.NONE);
		this.applyButton.setText("&Apply");
		this.applyButton.setSelection(false);
		this.applyButton.setLayoutData(applyGridData);
		this.applyButton.setEnabled(false);

		final GridData discardGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		discardGridData.widthHint = 70;
		discardGridData.heightHint = 20;
		discardGridData.horizontalIndent = 20;

		this.discardButton = new Button(this, SWT.NONE);
		this.discardButton.setText("&Discard");
		this.discardButton.setSelection(false);
		this.discardButton.setLayoutData(discardGridData);

		final GridData cancelGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		cancelGridData.widthHint = 70;
		cancelGridData.heightHint = 20;

		this.cancelButton = new Button(this, SWT.NONE);
		this.cancelButton.setText("&Cancel");
		this.cancelButton.setSelection(false);
		this.cancelButton.setLayoutData(cancelGridData);

		final GridData loadDefaultsGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		loadDefaultsGridData.widthHint = 85;
		loadDefaultsGridData.heightHint = 20;

		this.loadDefaultsButton = new Button(this, SWT.NONE);
		this.loadDefaultsButton.setText("&Load defaults");
		this.loadDefaultsButton.setSelection(false);
		this.loadDefaultsButton.setLayoutData(loadDefaultsGridData);

		// Listener for changes.
	    /*
	     * Currently disabled: Just render a message box.
	     */
	    javaEnvironmentButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		StaticGuiSupport.showMessageBox(OptionsComposite.this.shell, "This feature is currently not Implemented");
	    	}
	    });

	    /*
	     * Launch the ClassPathEntriesWindow.
	     */
	    classPathEntriesButton.addListener(SWT.Selection, new Listener() {
	    	@SuppressWarnings("unchecked")
			public void handleEvent(Event event) {
	    		OptionsComposite.this.shell.setEnabled(false);
	    		try {
		    		ClassPathEntriesWindow classPathEntriesWindow = new ClassPathEntriesWindow();
		    		List<String> classPathEntriesTemp;
		    		if (OptionsComposite.this.classPathEntries != null) {
		    			classPathEntriesTemp = (List<String>) ((ArrayList<String>) OptionsComposite.this.classPathEntries).clone();
		    		} else {
		    			classPathEntriesTemp = (List<String>) ((ArrayList<String>) Options.getInst().classPathEntries).clone();
		    		}
		    		classPathEntriesTemp = classPathEntriesWindow.show(OptionsComposite.this.parentWindow, classPathEntriesTemp);
		    		if (classPathEntriesTemp != null) {
		    			OptionsComposite.this.classPathEntries = classPathEntriesTemp;
		    			OptionsComposite.this.classPathEntriesText.setText(String.valueOf(OptionsComposite.this.classPathEntries.size()));
		    			somethingHasChanged();
		    		}
	    		} catch (Throwable t) {
    				StaticGuiSupport.processGuiError(t, "Class path entries", OptionsComposite.this.shell);
    			}
	    		OptionsComposite.this.shell.setEnabled(true);
	    		OptionsComposite.this.shell.setActive();
	    	}
	    });

		/*
		 * If the option to not clear the class loader on class path changes is off and would be set to on,
		 * the user is asked if if he really wants to turn it on. If he agrees, it is turned on. If it
		 * currently is on, it is set off.
		 */
		this.doNotClearClassLoaderCacheButton.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				if (OptionsComposite.this.doNotClearClassLoaderCacheButton.getSelection()) {
					String message = "By default, the class loaders cache is cleared when the main class path is changed. This for example happens when changing to another project. While browsing a project folder, caching of classes is always active.\n\n"
									+ "Not clearing the cache will result in a gain in execution speed, especially on slower systems. At the same time, the memory footprint of " + Globals.APP_NAME + " will increase. However, memory problems should not arise when not loading more than some hundred classes.\n\n"
									+ "If you have full caching enabled and browse two projects with the same package names (e.g. an old an a new version of the same project, or two versions of a jar-archive), this might lead to major problems when starting the execution, as classes of more than one version might have been loaded. This might have no effect, or lead to unexpected results or even making a successfull execution impossible. Handle this option with care!\n\n"
									+ "Do you wish to enable full caching?";
					if (StaticGuiSupport.showMessageBox(OptionsComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.NO) {
						OptionsComposite.this.doNotClearClassLoaderCacheButton.setSelection(false);
					} else {
						somethingHasChanged();
					}
				} else {
					somethingHasChanged();
				}
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		// ColorDialogs.
		this.fileInspConstantClassButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantClass);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantClass = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantClassImage, rgb);
	    			OptionsComposite.this.fileInspConstantClassButton.setImage(OptionsComposite.this.fileInspConstantClassImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantDoubleButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantDouble);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantDouble = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantDoubleImage, rgb);
	    			OptionsComposite.this.fileInspConstantDoubleButton.setImage(OptionsComposite.this.fileInspConstantDoubleImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantFieldrefButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantFieldref);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantFieldref = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantFieldrefImage, rgb);
	    			OptionsComposite.this.fileInspConstantFieldrefButton.setImage(OptionsComposite.this.fileInspConstantFieldrefImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantFloatButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantFloat);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantFloat = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantFloatImage, rgb);
	    			OptionsComposite.this.fileInspConstantFloatButton.setImage(OptionsComposite.this.fileInspConstantFloatImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantIntegerButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantInteger);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantInteger = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantIntegerImage, rgb);
	    			OptionsComposite.this.fileInspConstantIntegerButton.setImage(OptionsComposite.this.fileInspConstantIntegerImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantInterfaceMethodrefButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantInterfaceMethodref);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantInterfaceMethodref = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantInterfaceMethodrefImage, rgb);
	    			OptionsComposite.this.fileInspConstantInterfaceMethodrefButton.setImage(OptionsComposite.this.fileInspConstantInterfaceMethodrefImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantLongButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantLong);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantLong = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantLongImage, rgb);
	    			OptionsComposite.this.fileInspConstantLongButton.setImage(OptionsComposite.this.fileInspConstantLongImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantMethodrefButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantMethodref);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantMethodref = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantMethodrefImage, rgb);
	    			OptionsComposite.this.fileInspConstantMethodrefButton.setImage(OptionsComposite.this.fileInspConstantMethodrefImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantNameAndTypeButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantNameAndType);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantNameAndType = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantNameAndTypeImage, rgb);
	    			OptionsComposite.this.fileInspConstantNameAndTypeButton.setImage(OptionsComposite.this.fileInspConstantNameAndTypeImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantStringButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantString);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantString = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantStringImage, rgb);
	    			OptionsComposite.this.fileInspConstantStringButton.setImage(OptionsComposite.this.fileInspConstantStringImage);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.fileInspConstantUtf8Button.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ColorDialog dialog = new ColorDialog(OptionsComposite.this.shell);
	    		dialog.setRGB(OptionsComposite.this.rgbFileInspectionConstantUtf8);
	    		RGB rgb = dialog.open();
	    		// Has a color been selected?
	    		if (rgb != null) {
	    			OptionsComposite.this.rgbFileInspectionConstantUtf8 = rgb;
	    			changeImageBackgroundColor(OptionsComposite.this.fileInspConstantUtf8Image, rgb);
	    			OptionsComposite.this.fileInspConstantUtf8Button.setImage(OptionsComposite.this.fileInspConstantUtf8Image);
	    			somethingHasChanged();
	    		}
	    	}
	    });

		this.classFileWriteAccessButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.numberOfRecentlyOpenedFilesCombo.addModifyListener(new SomethingHasChangedModifyListener());

		/*
		 * Clear the recently opened files.
		 */
		numberOfRecentlyOpenedFilesButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		Options.getInst().recentFilesPaths.clear();
	    	}
	    });

 		/*
		 * Ask the user if he is sure about reloading the directory tree before doing it. If the user
		 * wants to reload it, do that according to set value selected.
		 */
	    this.hideDrivesABButton.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				String message = "Changing this value requires the directory tree to reload, collapsing it.\n\n"
								+ "Do you wish to continue?";
				if (StaticGuiSupport.showMessageBox(OptionsComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.NO)
				{
					// Keep the old value!
					if (OptionsComposite.this.hideDrivesABButton.getSelection()) {
						OptionsComposite.this.hideDrivesABButton.setSelection(false);
					} else {
						OptionsComposite.this.hideDrivesABButton.setSelection(true);
					}
				} else {
					somethingHasChanged();
				}
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		this.showEveryInvocationButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.skipInvocationOfJavaPackageButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.skipInvocationOfOtherClassesButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.skipAnyInvocationButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.visuallySkipStaticInitializersButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.showInstructionBytePositionButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.maximumExecutionTimeCombo.addModifyListener(new SomethingHasChangedModifyListener());
		this.assumeMissingValuesButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.askUserInCasOfMissingValuesButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.doNotHaltOnNativeMethodsButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.assumeNativeReturnValuesToBeZeroNullButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.forwardJavaPackageNativeInvocationsButton.addListener(SWT.Selection, new SomethingHasChangedListener());

 		/*
		 * If enabling the option, any pre-cached instructions have to be unloaded, so they can be
		 * replaced with optimized ones on the next time they are requested. If disabling the
		 * options, this also has to be done so that non-optimized instructions are used on the next
		 * run.
		 */
		this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				String message = "Changing this value requires unloading all cached instructions. The class loader will be blocked for that time.\n\n"
								+ "Please also note that the instruction set of a currently executed method will NOT be refreshed.\n\n"
								+ "Do you wish to continue and flush the instructions cache now?";
				if (StaticGuiSupport.showMessageBox(OptionsComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.NO)
				{
					// Keep the old value!
					if (OptionsComposite.this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.getSelection()) {
						OptionsComposite.this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.setSelection(false);
					} else {
						OptionsComposite.this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.setSelection(true);
					}
				} else {
					OptionsComposite.this.classLoader.unloadAllInstructions();
					somethingHasChanged();
				}
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });


		this.measureSymbolicExecutionTimeButton.addListener(SWT.Selection, new SomethingHasChangedListener());

		this.symbolicArrayInitializationNumberOfRunsTotalCombo.addModifyListener(new SomethingHasChangedModifyListener());
		this.symbolicArrayInitializationStartingLengthCombo.addModifyListener(new SomethingHasChangedModifyListener());
		this.symbolicArrayInitializationIncrementationStrategyLinearButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				setIncrementationStrategyLinearStepSizeStatus(true);
			}
		});

		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.addModifyListener(new SomethingHasChangedModifyListener());

		this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				setIncrementationStrategyLinearStepSizeStatus(false);
			}
		});

		this.symbolicArrayInitializationIncrementationStrategyExponentialButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				setIncrementationStrategyLinearStepSizeStatus(false);
			}
		});

		this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				setIncrementationStrategyLinearStepSizeStatus(false);
			}
		});

		this.symbolicArrayInitializationTestNullButton.addListener(SWT.Selection, new SomethingHasChangedListener());
		this.symbolicArrayInitializationTestZeroLengthArrayButton.addListener(SWT.Selection, new SomethingHasChangedListener());

		this.breadthFirstSearchButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				setIterativeDeepeningSettingsStatus(false);
			}
		});

		this.depthFirstSearchButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				setIterativeDeepeningSettingsStatus(false);
			}
		});

		this.iterativeDeepeningButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				setIterativeDeepeningSettingsStatus(true);
			}
		});

		this.iterativeDeepeningStartingDepthCombo.addModifyListener(new SomethingHasChangedModifyListener());
		this.iterativeDeepeningDeepnessIncrementCombo.addModifyListener(new SomethingHasChangedModifyListener());

		this.multithreadingButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
				OptionsComposite.this.numberOfSimultaneousThreadsCombo.setEnabled(OptionsComposite.this.multithreadingButton.getSelection());
			}
		});

		this.numberOfSimultaneousThreadsCombo.addModifyListener(new SomethingHasChangedModifyListener());

		this.maximumLoopsCombo.addModifyListener(new SomethingHasChangedModifyListener());
		this.maximumInstructionsBeforeFindingANewSolutionCombo.addModifyListener(new SomethingHasChangedModifyListener());
		this.onlyCountChoicePointGeneratingInstructions.addListener(SWT.Selection, new SomethingHasChangedListener());

		this.useDefUseCoverageButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				enableOrDisableCoverageButtons();
				somethingHasChanged();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		this.useControlFlowCoverageButton.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				enableOrDisableCoverageButtons();
				somethingHasChanged();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		this.coverageTrackingInitialMethodButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageTrackingInitialClassButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageTrackingInitialPackageButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageTrackingTopLevelPackageButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageTrackingAnyButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageAbortionCriteriaNoButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageAbortionCriteriaDefUseButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageAbortionCriteriaControlFlowButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.coverageAbortionCriteriaFullButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});


		this.testClassesDirectoryText.addModifyListener(new SomethingHasChangedModifyListener());

	    /*
	     * Open the dialog to choose a directory for the test case classes.
	     */
	    this.testClassesDirectoryButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		chooseTestClassesDirectory();
	    	}
	    });

		this.testClassesPackageNameText.addModifyListener(new SomethingHasChangedModifyListener());
		this.testClassesNameText.addModifyListener(new SomethingHasChangedModifyListener());

		this.eliminateSolutionsByCoverageNotButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.eliminateSolutionsByCoverageDefUseButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.eliminateSolutionsByCoverageControlFlowButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.eliminateSolutionsByCoverageBothButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				somethingHasChanged();
			}
		});

		this.loggingLayoutCombo.addModifyListener(new SomethingHasChangedModifyListener());
		this.maximumEntriesPerLogfileCombo.addModifyListener(new SomethingHasChangedModifyListener());

		// Button Listener
		this.okButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		freeImageRessources();
	    		if (applyChanges()) getParentWindow().doExit();
	    	}
	    });

		this.applyButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		applyChanges();
	    	}
	    });

		this.discardButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		String message = "Do you really want to discard any changes?";
	    		if (StaticGuiSupport.showMessageBox(OptionsComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.YES) {
	    			loadSettings();
	    		}
	    	}
	    });

		this.cancelButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		freeImageRessources();
	    		getParentWindow().doExit();
	    	}
	    });

		this.loadDefaultsButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		String message = "Do you really want to load the default settings? This will override and SAVE all settings.";
	    		if (StaticGuiSupport.showMessageBox(OptionsComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.YES) {
	    			ConfigReader.loadConfig(true);
	    			loadSettings();
	    			ConfigReader.saveCurrentConfig();
	    		}
	    	}
	    });

		// Esc Listener.
		this.addKeyListener(new EscKeyListener(parent));
		optionsTabFolder.addKeyListener(new EscKeyListener(parent));
		generalComposite.addKeyListener(new EscKeyListener(parent));
		fileInspectionComposite.addKeyListener(new EscKeyListener(parent));
		visualComposite.addKeyListener(new EscKeyListener(parent));
		executionComposite.addKeyListener(new EscKeyListener(parent));
		symbolicExecutionComposite.addKeyListener(new EscKeyListener(parent));
		searchAlgorithmComposite.addKeyListener(new EscKeyListener(parent));
		testCasesComposite.addKeyListener(new EscKeyListener(parent));
		loggingComposite.addKeyListener(new EscKeyListener(parent));
		environmentGroup.addKeyListener(new EscKeyListener(parent));
		javaEnvironmentLabel.addKeyListener(new EscKeyListener(parent));
		this.javaEnvironmentText.addKeyListener(new EscKeyListener(parent));
		javaEnvironmentButton.addKeyListener(new EscKeyListener(parent));
		classPathEntriesLabel.addKeyListener(new EscKeyListener(parent));
		this.classPathEntriesText.addKeyListener(new EscKeyListener(parent));
		classPathEntriesButton.addKeyListener(new EscKeyListener(parent));
		this.doNotClearClassLoaderCacheButton.addKeyListener(new EscKeyListener(parent));
		colorsGroup.addKeyListener(new EscKeyListener(parent));
		fileInspConstantClassLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantClassButton.addKeyListener(new EscKeyListener(parent));
		colorsSpacerSash.addKeyListener(new EscKeyListener(parent));
		fileInspConstantDoubleLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantDoubleButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantFieldrefLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantFieldrefButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantFloatLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantFloatButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantIntegerLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantIntegerButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantInterfaceMethodrefLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantInterfaceMethodrefButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantLongLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantLongButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantMethodrefLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantMethodrefButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantNameAndTypeLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantNameAndTypeButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantStringLabel.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantStringButton.addKeyListener(new EscKeyListener(parent));
		fileInspConstantUtf8Label.addKeyListener(new EscKeyListener(parent));
		this.fileInspConstantUtf8Button.addKeyListener(new EscKeyListener(parent));
		classFileWriteAccessGroup.addKeyListener(new EscKeyListener(parent));
		this.classFileWriteAccessButton.addKeyListener(new EscKeyListener(parent));
		timeLimitGroup.addKeyListener(new EscKeyListener(parent));
		maximumExecutionTimeLabel.addKeyListener(new EscKeyListener(parent));
		this.maximumExecutionTimeCombo.addKeyListener(new EscKeyListener(parent));
		missingValuesGroup.addKeyListener(new EscKeyListener(parent));
		this.assumeMissingValuesButton.addKeyListener(new EscKeyListener(parent));
		this.askUserInCasOfMissingValuesButton.addKeyListener(new EscKeyListener(parent));
		this.doNotHaltOnNativeMethodsButton.addKeyListener(new EscKeyListener(parent));
		this.assumeNativeReturnValuesToBeZeroNullButton.addKeyListener(new EscKeyListener(parent));
		this.forwardJavaPackageNativeInvocationsButton.addKeyListener(new EscKeyListener(parent));
		this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.addKeyListener(new EscKeyListener(parent));
		guiOptionsGroup.addKeyListener(new EscKeyListener(parent));
		numberOfRecentlyOpenedFilesLabel.addKeyListener(new EscKeyListener(parent));
		this.numberOfRecentlyOpenedFilesCombo.addKeyListener(new EscKeyListener(parent));
		numberOfRecentlyOpenedFilesButton.addKeyListener(new EscKeyListener(parent));
		this.hideDrivesABButton.addKeyListener(new EscKeyListener(parent));
		this.measureSymbolicExecutionTimeButton.addKeyListener(new EscKeyListener(parent));
		stepByStepGroup.addKeyListener(new EscKeyListener(parent));
		this.showEveryInvocationButton.addKeyListener(new EscKeyListener(parent));
		this.skipInvocationOfJavaPackageButton.addKeyListener(new EscKeyListener(parent));
		this.skipInvocationOfOtherClassesButton.addKeyListener(new EscKeyListener(parent));
		this.skipAnyInvocationButton.addKeyListener(new EscKeyListener(parent));
		this.visuallySkipStaticInitializersButton.addKeyListener(new EscKeyListener(parent));
		this.showInstructionBytePositionButton.addKeyListener(new EscKeyListener(parent));
		arrayInitializationGroup.addKeyListener(new EscKeyListener(parent));
		symbolicArrayInitializationNumberOfRunsTotalLabel.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.addKeyListener(new EscKeyListener(parent));
		symbolicArrayInitializationStartingLengthLabel.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationStartingLengthCombo.addKeyListener(new EscKeyListener(parent));
		symbolicArrayInitializationIncrementationStrategyLabel.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationIncrementationStrategyLinearButton.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeLabel.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationIncrementationStrategyExponentialButton.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationTestNullButton.addKeyListener(new EscKeyListener(parent));
		this.symbolicArrayInitializationTestZeroLengthArrayButton.addKeyListener(new EscKeyListener(parent));
		searchAlgorithmGroup.addKeyListener(new EscKeyListener(parent));
		searchAlgorithmLabel.addKeyListener(new EscKeyListener(parent));
		this.breadthFirstSearchButton.addKeyListener(new EscKeyListener(parent));
		this.depthFirstSearchButton.addKeyListener(new EscKeyListener(parent));
		this.iterativeDeepeningButton.addKeyListener(new EscKeyListener(parent));
		this.iterativeDeepeningStartingDepthLabel.addKeyListener(new EscKeyListener(parent));
		this.iterativeDeepeningStartingDepthCombo.addKeyListener(new EscKeyListener(parent));
		this.iterativeDeepeningDeepnessIncrementLabel.addKeyListener(new EscKeyListener(parent));
		this.iterativeDeepeningDeepnessIncrementCombo.addKeyListener(new EscKeyListener(parent));
		searchAlgorithmNoteLabel.addKeyListener(new EscKeyListener(parent));
		multithreadingGroup.addKeyListener(new EscKeyListener(parent));
		this.multithreadingButton.addKeyListener(new EscKeyListener(parent));
		numberOfSimultaneousThreadsLabel.addKeyListener(new EscKeyListener(parent));
		this.numberOfSimultaneousThreadsCombo.addKeyListener(new EscKeyListener(parent));
		multithreadingLabel.addKeyListener(new EscKeyListener(parent));
		forcedBacktrackingGroup.addKeyListener(new EscKeyListener(parent));
		maximumLoopsLabel.addKeyListener(new EscKeyListener(parent));
		this.maximumLoopsCombo.addKeyListener(new EscKeyListener(parent));
		abortionCriteriaGroup.addKeyListener(new EscKeyListener(parent));
		maximumInstructionsBeforeFindingANewSolutionLabel.addKeyListener(new EscKeyListener(parent));
		this.maximumInstructionsBeforeFindingANewSolutionCombo.addKeyListener(new EscKeyListener(parent));
		this.onlyCountChoicePointGeneratingInstructions.addKeyListener(new EscKeyListener(parent));
		coverageUsageAndTrackingGroup.addKeyListener(new EscKeyListener(parent));
		this.useDefUseCoverageButton.addKeyListener(new EscKeyListener(parent));
		this.useControlFlowCoverageButton.addKeyListener(new EscKeyListener(parent));
		coveragerTrackingLabel.addKeyListener(new EscKeyListener(parent));
		this.coverageTrackingInitialMethodButton.addKeyListener(new EscKeyListener(parent));
		this.coverageTrackingInitialClassButton.addKeyListener(new EscKeyListener(parent));
		this.coverageTrackingInitialPackageButton.addKeyListener(new EscKeyListener(parent));
		this.coverageTrackingTopLevelPackageButton.addKeyListener(new EscKeyListener(parent));
		this.coverageTrackingAnyButton.addKeyListener(new EscKeyListener(parent));
		this.coverageAbortionGroup.addKeyListener(new EscKeyListener(parent));
		coverageAbortionLabel.addKeyListener(new EscKeyListener(parent));
		this.coverageAbortionCriteriaNoButton.addKeyListener(new EscKeyListener(parent));
		this.coverageAbortionCriteriaDefUseButton.addKeyListener(new EscKeyListener(parent));
		this.coverageAbortionCriteriaControlFlowButton.addKeyListener(new EscKeyListener(parent));
		this.coverageAbortionCriteriaFullButton.addKeyListener(new EscKeyListener(parent));
		coverageComposite.addKeyListener(new EscKeyListener(parent));
		testCaseSettingsGroup.addKeyListener(new EscKeyListener(parent));
		testClassesDirectoryLabel.addKeyListener(new EscKeyListener(parent));
		this.testClassesDirectoryText.addKeyListener(new EscKeyListener(parent));
		this.testClassesDirectoryButton.addKeyListener(new EscKeyListener(parent));
		testClassesPackageNameLabel.addKeyListener(new EscKeyListener(parent));
		this.testClassesPackageNameText.addKeyListener(new EscKeyListener(parent));
		spacingLabel01.addKeyListener(new EscKeyListener(parent));
		testClassesNameLabel.addKeyListener(new EscKeyListener(parent));
		this.testClassesNameText.addKeyListener(new EscKeyListener(parent));
		spacingLabel02.addKeyListener(new EscKeyListener(parent));
		testCaseSettingsNoteLabel.addKeyListener(new EscKeyListener(parent));
		this.testCaseEliminationGroup.addKeyListener(new EscKeyListener(parent));
		this.eliminateSolutionsByCoverageNotButton.addKeyListener(new EscKeyListener(parent));
		this.eliminateSolutionsByCoverageDefUseButton.addKeyListener(new EscKeyListener(parent));
		this.eliminateSolutionsByCoverageControlFlowButton.addKeyListener(new EscKeyListener(parent));
		this.eliminateSolutionsByCoverageBothButton.addKeyListener(new EscKeyListener(parent));
		loggingGroup.addKeyListener(new EscKeyListener(parent));
		loggingLayoutLabel.addKeyListener(new EscKeyListener(parent));
		this.loggingLayoutCombo.addKeyListener(new EscKeyListener(parent));
		maximumEntriesPerLogfileLabel.addKeyListener(new EscKeyListener(parent));
		this.maximumEntriesPerLogfileCombo.addKeyListener(new EscKeyListener(parent));
		this.okButton.addKeyListener(new EscKeyListener(parent));
		this.applyButton.addKeyListener(new EscKeyListener(parent));
		this.discardButton.addKeyListener(new EscKeyListener(parent));
		this.cancelButton.addKeyListener(new EscKeyListener(parent));
		this.loadDefaultsButton.addKeyListener(new EscKeyListener(parent));

		// Finish setting up the composite.
		this.pack();

		// Load the settings.
		loadSettings();
	}

	/**
	 * Private super class that offers a Listener that simply will notify the
	 * OptionsComposite that the value of an element has changed.
	 */
	protected class SomethingHasChangedListener implements Listener {
		
    	/**
    	 * @param event An Event
    	 * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
    	 */
    	public void handleEvent(Event event) {
    		somethingHasChanged();
    	}
	}

	/**
	 * Private super class that offers a ModifyListener that simply will notify
	 * the OptionsComposite that the value of an element has changed.
	 */
	protected class SomethingHasChangedModifyListener implements ModifyListener {
    	/**
    	 * @param event An Event
    	 * @see org.eclipse.swt.events.ModifyListener#modifyText(ModifyEvent)
    	 */
    	public void modifyText(final ModifyEvent event) {
    		somethingHasChanged();
    	}
	}

	/**
	 * Return the parent window (instance of OptionsWindow).
	 * @return The parent window.
	 */
	protected OptionsWindow getParentWindow() {
		return this.parentWindow;
	}

	/**
	 * Load the settings from the Options instance and set the checkboxes, combos etc.
	 * accordingly.
	 */
	protected void loadSettings() {
		Options options = Options.getInst();

		// Load information about the environment.
		this.classPathEntries = null;
		this.javaEnvironmentText.setText(options.javaVersion);
		this.classPathEntriesText.setText(String.valueOf(options.classPathEntries.size()));
		if (options.doNotClearClassLoaderCache) {
			this.doNotClearClassLoaderCacheButton.setSelection(true);
		} else {
			this.doNotClearClassLoaderCacheButton.setSelection(false);
		}

		// Load colors.
		this.rgbFileInspectionConstantClass = options.rgbFileInspConstantClass;
		this.rgbFileInspectionConstantDouble = options.rgbFileInspConstantDouble;
		this.rgbFileInspectionConstantFieldref = options.rgbFileInspConstantFieldref;
		this.rgbFileInspectionConstantFloat = options.rgbFileInspConstantFloat;
		this.rgbFileInspectionConstantInteger = options.rgbFileInspConstantInteger;
		this.rgbFileInspectionConstantInterfaceMethodref = options.rgbFileInspConstantInterfaceMethodref;
		this.rgbFileInspectionConstantLong = options.rgbFileInspConstantLong;
		this.rgbFileInspectionConstantMethodref = options.rgbFileInspConstantMethodref;
		this.rgbFileInspectionConstantNameAndType = options.rgbFileInspConstantNameAndType;
		this.rgbFileInspectionConstantString = options.rgbFileInspConstantString;
		this.rgbFileInspectionConstantUtf8 = options.rgbFileInspConstantUtf8;

		// Paint the images.
		changeImageBackgroundColor(this.fileInspConstantClassImage, this.rgbFileInspectionConstantClass);
		changeImageBackgroundColor(this.fileInspConstantDoubleImage, this.rgbFileInspectionConstantDouble);
		changeImageBackgroundColor(this.fileInspConstantFieldrefImage, this.rgbFileInspectionConstantFieldref);
		changeImageBackgroundColor(this.fileInspConstantFloatImage, this.rgbFileInspectionConstantFloat);
		changeImageBackgroundColor(this.fileInspConstantIntegerImage, this.rgbFileInspectionConstantInteger);
		changeImageBackgroundColor(this.fileInspConstantInterfaceMethodrefImage, this.rgbFileInspectionConstantInterfaceMethodref);
		changeImageBackgroundColor(this.fileInspConstantLongImage, this.rgbFileInspectionConstantLong);
		changeImageBackgroundColor(this.fileInspConstantMethodrefImage, this.rgbFileInspectionConstantMethodref);
		changeImageBackgroundColor(this.fileInspConstantNameAndTypeImage, this.rgbFileInspectionConstantNameAndType);
		changeImageBackgroundColor(this.fileInspConstantStringImage, this.rgbFileInspectionConstantString);
		changeImageBackgroundColor(this.fileInspConstantUtf8Image, this.rgbFileInspectionConstantUtf8);

		// Set the images.
		this.fileInspConstantClassButton.setImage(this.fileInspConstantClassImage);
		this.fileInspConstantDoubleButton.setImage(this.fileInspConstantDoubleImage);
		this.fileInspConstantFieldrefButton.setImage(this.fileInspConstantFieldrefImage);
		this.fileInspConstantFloatButton.setImage(this.fileInspConstantFloatImage);
		this.fileInspConstantIntegerButton.setImage(this.fileInspConstantIntegerImage);
		this.fileInspConstantInterfaceMethodrefButton.setImage(this.fileInspConstantInterfaceMethodrefImage);
		this.fileInspConstantLongButton.setImage(this.fileInspConstantLongImage);
		this.fileInspConstantMethodrefButton.setImage(this.fileInspConstantMethodrefImage);
		this.fileInspConstantNameAndTypeButton.setImage(this.fileInspConstantNameAndTypeImage);
		this.fileInspConstantStringButton.setImage(this.fileInspConstantStringImage);
		this.fileInspConstantUtf8Button.setImage(this.fileInspConstantUtf8Image);

		// Load setting for the class file write access.
		if (options.classFileWriteAccess) {
			this.classFileWriteAccessButton.setSelection(true);
		} else {
			this.classFileWriteAccessButton.setSelection(false);
		}

		// Load settings for the maximum execution time.
		this.maximumExecutionTimeCombo.setText(formatLongTimeToSingleValue(options.maximumExecutionTime));

		// Load settings for the assumption of missing values.
		if (options.assumeMissingValues) {
			this.assumeMissingValuesButton.setSelection(true);
		} else {
			this.assumeMissingValuesButton.setSelection(false);
		}

		// Load settings for Asking the user in case of missing values.
		if (options.askUserMissingValues) {
			this.askUserInCasOfMissingValuesButton.setSelection(true);
		} else {
			this.askUserInCasOfMissingValuesButton.setSelection(false);
		}

		// Load setting for the handling of native methods.
		if (options.doNotHaltOnNativeMethods) {
			this.doNotHaltOnNativeMethodsButton.setSelection(true);
		} else {
			this.doNotHaltOnNativeMethodsButton.setSelection(false);
		}

		if (options.assumeNativeReturnValuesToBeZeroNull) {
			this.assumeNativeReturnValuesToBeZeroNullButton.setSelection(true);
		} else {
			this.assumeNativeReturnValuesToBeZeroNullButton.setSelection(false);
		}

		if (options.forwardJavaPackageNativeInvoc) {
			this.forwardJavaPackageNativeInvocationsButton.setSelection(true);
		} else {
			this.forwardJavaPackageNativeInvocationsButton.setSelection(false);
		}

		// Load setting for optimizations.
		if (options.dynReplaceInstWithOptimizedOnes) {
			this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.setSelection(true);
		} else {
			this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.setSelection(false);
		}

		// Load GUI options.
		this.numberOfRecentlyOpenedFilesCombo.setText(String.valueOf(options.numberOfRecentFiles));
		if (options.hideDrivesAB) {
			this.hideDrivesABButton.setSelection(true);
		} else {
			this.hideDrivesABButton.setSelection(false);
		}

		if (options.measureSymbolicExecutionTime) {
			this.measureSymbolicExecutionTimeButton.setSelection(true);
		} else {
			this.measureSymbolicExecutionTimeButton.setSelection(false);
		}

		// Load settings for the visual skipping of invocations.
		this.showEveryInvocationButton.setSelection(false);
		this.skipInvocationOfJavaPackageButton.setSelection(false);
		this.skipInvocationOfOtherClassesButton.setSelection(false);
		this.skipAnyInvocationButton.setSelection(false);
		int skipVisualInvocation = options.stepByStepVisuallySkipInvoc;
		if (skipVisualInvocation == 0) {
			this.showEveryInvocationButton.setSelection(true);
		} else if (skipVisualInvocation == 1) {
			this.skipInvocationOfJavaPackageButton.setSelection(true);
		} else if (skipVisualInvocation == 2) {
			this.skipInvocationOfOtherClassesButton.setSelection(true);
		} else {
			this.skipAnyInvocationButton.setSelection(true);
		}
		if (options.visuallySkipStaticInit) {
			this.visuallySkipStaticInitializersButton.setSelection(true);
		} else {
			this.visuallySkipStaticInitializersButton.setSelection(false);
		}
		if (options.stepByStepShowInstrBytePosition) {
			this.showInstructionBytePositionButton.setSelection(true);
		} else {
			this.showInstructionBytePositionButton.setSelection(false);
		}

		// Load settings for the array initialization
		this.symbolicArrayInitializationNumberOfRunsTotalCombo.setText(String.valueOf(options.symbArrayInitNumberOfRunsTotal));
		this.symbolicArrayInitializationStartingLengthCombo.setText(String.valueOf(options.symbArrayInitStartingLength));

		this.symbolicArrayInitializationIncrementationStrategyLinearButton.setSelection(false);
		this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.setSelection(false);
		this.symbolicArrayInitializationIncrementationStrategyExponentialButton.setSelection(false);
		this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.setSelection(false);
		int incrementationStrategy = options.symbArrayInitIncrStrategy;
		switch (incrementationStrategy) {
			case 0:
				this.symbolicArrayInitializationIncrementationStrategyLinearButton.setSelection(true);
				setIncrementationStrategyLinearStepSizeStatus(true);
				break;
			case 1:
				this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.setSelection(true);
				setIncrementationStrategyLinearStepSizeStatus(false);
				break;
			case 3:
				this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.setSelection(true);
				setIncrementationStrategyLinearStepSizeStatus(false);
				break;
			default:
				this.symbolicArrayInitializationIncrementationStrategyExponentialButton.setSelection(true);
			setIncrementationStrategyLinearStepSizeStatus(false);
			break;
		}

		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.setText(String.valueOf(options.symbArrayInitIncrStrategyLinearStepSize));

		if (options.symbArrayInitTestNull) {
			this.symbolicArrayInitializationTestNullButton.setSelection(true);
		} else {
			this.symbolicArrayInitializationTestNullButton.setSelection(false);
		}
		if (options.symbArrayInitTestZeroLengthArray) {
			this.symbolicArrayInitializationTestZeroLengthArrayButton.setSelection(true);
		} else {
			this.symbolicArrayInitializationTestZeroLengthArrayButton.setSelection(false);
		}

		// Load settings for the search algorithms.
		this.breadthFirstSearchButton.setSelection(false);
		this.depthFirstSearchButton.setSelection(false);
		this.iterativeDeepeningButton.setSelection(false);
		setIterativeDeepeningSettingsStatus(false);
		int symbolicSearchAlgorithm = options.searchAlgorithm;
		if (symbolicSearchAlgorithm == 0) {
			this.breadthFirstSearchButton.setSelection(true);
			setIterativeDeepeningSettingsStatus(false);
		} else if (symbolicSearchAlgorithm == 1) {
			this.depthFirstSearchButton.setSelection(true);
			setIterativeDeepeningSettingsStatus(false);
		} else {
			this.iterativeDeepeningButton.setSelection(true);
			setIterativeDeepeningSettingsStatus(true);
		}
		this.iterativeDeepeningStartingDepthCombo.setText(String.valueOf(options.iterativeDeepeningStartingDepth));
		this.iterativeDeepeningDeepnessIncrementCombo.setText(String.valueOf(options.iterativeDeepeningDeepnessIncrement));

		// Load settings for SJVM multithreading.
		if (options.enableSJVMMultithreading) {
			this.multithreadingButton.setSelection(true);
			this.numberOfSimultaneousThreadsCombo.setEnabled(true);
		} else {
			this.multithreadingButton.setSelection(false);
			this.numberOfSimultaneousThreadsCombo.setEnabled(false);
		}

		this.numberOfSimultaneousThreadsCombo.setText(String.valueOf(options.numberOfSimultaneousThreads));

		// Load settings for the maximum loops.
		int maximumLoops = options.maximumLoopsToTake;
		if (maximumLoops == -1) {
			this.maximumLoopsCombo.setText("infinite");
		} else {
			this.maximumLoopsCombo.setText(String.valueOf(maximumLoops));
		}

		// Load settings for the abortion criteria.
		int maximumInstructionsBeforeFindingANewSolution = options.maxInstrBeforeFindingANewSolution;
		if (maximumInstructionsBeforeFindingANewSolution == -1) {
			this.maximumInstructionsBeforeFindingANewSolutionCombo.setText("infinite");
		} else {
			this.maximumInstructionsBeforeFindingANewSolutionCombo.setText(String.valueOf(maximumInstructionsBeforeFindingANewSolution));
		}

		if (options.onlyCountChoicePointGeneratingInst) {
			this.onlyCountChoicePointGeneratingInstructions.setSelection(true);
		} else {
			this.onlyCountChoicePointGeneratingInstructions.setSelection(false);
		}

		// Load coverage settings
		this.useDefUseCoverageButton.setSelection(options.useDUCoverage);
		this.useControlFlowCoverageButton.setSelection(options.useCFCoverage);

		this.coverageTrackingInitialMethodButton.setSelection(false);
		this.coverageTrackingInitialClassButton.setSelection(false);
		this.coverageTrackingInitialPackageButton.setSelection(false);
		this.coverageTrackingTopLevelPackageButton.setSelection(false);
		this.coverageTrackingAnyButton.setSelection(false);
		int coveragerTracking = options.coverageTracking;
		switch (coveragerTracking) {
			case 1:
				this.coverageTrackingInitialClassButton.setSelection(true);
				break;
			case 2:
				this.coverageTrackingInitialPackageButton.setSelection(true);
				break;
			case 3:
				this.coverageTrackingTopLevelPackageButton.setSelection(true);
				break;
			case 4:
				this.coverageTrackingAnyButton.setSelection(true);
				break;
			default:
				this.coverageTrackingInitialMethodButton.setSelection(true);
				break;
		}

		this.coverageAbortionCriteriaNoButton.setSelection(false);
		this.coverageAbortionCriteriaDefUseButton.setSelection(false);
		this.coverageAbortionCriteriaControlFlowButton.setSelection(false);
		this.coverageAbortionCriteriaFullButton.setSelection(false);
		int coverageAbortionCriteria = options.coverageAbortionCriteria;
		switch (coverageAbortionCriteria) {
			case 1:
				this.coverageAbortionCriteriaDefUseButton.setSelection(true);
				break;
			case 2:
				this.coverageAbortionCriteriaControlFlowButton.setSelection(true);
				break;
			case 3:
				this.coverageAbortionCriteriaFullButton.setSelection(true);
				break;
			default:
				this.coverageAbortionCriteriaNoButton.setSelection(true);
				break;
		}

		// Load settings for the test case settings.
		this.testClassesDirectoryText.setText(options.testClassesDirectory);
		this.testClassesPackageNameText.setText(options.testClassesPackageName);
		this.testClassesNameText.setText(options.testClassesName);

		// Load settings for the test case elimination.
		this.eliminateSolutionsByCoverageNotButton.setSelection(false);
		this.eliminateSolutionsByCoverageDefUseButton.setSelection(false);
		this.eliminateSolutionsByCoverageControlFlowButton.setSelection(false);
		this.eliminateSolutionsByCoverageBothButton.setSelection(false);
		int eliminateSolutionsByCoverage = options.eliminateSolutionsByCoverage;
		switch (eliminateSolutionsByCoverage) {
			case 1:
				this.eliminateSolutionsByCoverageDefUseButton.setSelection(true);
				break;
			case 2:
				this.eliminateSolutionsByCoverageControlFlowButton.setSelection(true);
				break;
			case 3:
				this.eliminateSolutionsByCoverageBothButton.setSelection(true);
				break;
			default:
				this.eliminateSolutionsByCoverageNotButton.setSelection(true);
				break;
		}

		// Load setting for the logging.
		if (options.getHtmlLogging()) {
			this.loggingLayoutCombo.select(0);
		} else {
			this.loggingLayoutCombo.select(1);
		}

		long maximumLogEntries = options.maximumLogEntries;
		if (maximumLogEntries == -1) {
			this.maximumEntriesPerLogfileCombo.setText("infinite");
		} else {
			this.maximumEntriesPerLogfileCombo.setText(String.valueOf(maximumLogEntries));
		}

		// Enable or disable elements depending on others.
		enableOrDisableCoverageButtons();

		// Finally make sure the "Apply" and "Discard" buttons are still disabled.
		this.needToSave = false;
		this.applyButton.setEnabled(false);
		this.discardButton.setEnabled(false);
	}

	/**
	 * After they have been checked for correctness, save the settings to the Options instance.
	 * @return true, if the settings could be saved, false otherwise.
	 */
	protected boolean applyChanges() {
		// Do not take he time to save if it it is not needed.
		if (!this.needToSave) return true;

		// Check the settings for correctness.
		if (!checkForCorrectness()) return false;
		this.applyButton.setEnabled(false);
		this.discardButton.setEnabled(false);
		Options options = Options.getInst();

		// Save the environmental settings.
		options.doNotClearClassLoaderCache = this.doNotClearClassLoaderCacheButton.getSelection();
		if (this.classPathEntries != null) {
			Options.getInst().classPathEntries = this.classPathEntries;
			this.classLoader.updateClassPath(StaticGuiSupport.arrayList2StringArray(this.classPathEntries), !options.doNotClearClassLoaderCache);
		}

		// Save colors.
		options.rgbFileInspConstantClass = this.rgbFileInspectionConstantClass;
		options.rgbFileInspConstantDouble = this.rgbFileInspectionConstantDouble;
		options.rgbFileInspConstantFieldref = this.rgbFileInspectionConstantFieldref;
		options.rgbFileInspConstantFloat = this.rgbFileInspectionConstantFloat;
		options.rgbFileInspConstantInteger = this.rgbFileInspectionConstantInteger;
		options.rgbFileInspConstantInterfaceMethodref = this.rgbFileInspectionConstantInterfaceMethodref;
		options.rgbFileInspConstantLong = this.rgbFileInspectionConstantLong;
		options.rgbFileInspConstantMethodref = this.rgbFileInspectionConstantMethodref;
		options.rgbFileInspConstantNameAndType = this.rgbFileInspectionConstantNameAndType;
		options.rgbFileInspConstantString = this.rgbFileInspectionConstantString;
		options.rgbFileInspConstantUtf8 = this.rgbFileInspectionConstantUtf8;

		// Save setting for the class file write access.
		options.classFileWriteAccess = this.classFileWriteAccessButton.getSelection();

		// Save and apply the GUI option settings.
		options.numberOfRecentFiles = Integer.parseInt(this.numberOfRecentlyOpenedFilesCombo.getText());
		// If there are too many entries, remove some.
		while (options.recentFilesPaths.size() > options.numberOfRecentFiles) {
			options.recentFilesPaths.remove(options.recentFilesPaths.size() - 1);
		}
		options.hideDrivesAB = this.hideDrivesABButton.getSelection();
		options.measureSymbolicExecutionTime = this.measureSymbolicExecutionTimeButton.getSelection();

		// Save the settings for the visual skipping of invocations.
		if (this.showEveryInvocationButton.getSelection()) options.stepByStepVisuallySkipInvoc = 0;
		else if (this.skipInvocationOfJavaPackageButton.getSelection()) options.stepByStepVisuallySkipInvoc = 1;
		else if (this.skipInvocationOfOtherClassesButton.getSelection()) {
			options.stepByStepVisuallySkipInvoc = 2;
		} else {
			options.stepByStepVisuallySkipInvoc = 3;
		}
		options.visuallySkipStaticInit = this.visuallySkipStaticInitializersButton.getSelection();
		options.stepByStepShowInstrBytePosition = this.showInstructionBytePositionButton.getSelection();

		// Save the maximum execution time.
		options.maximumExecutionTime = this.maximumExecutionTime;

		// Save the missing values settings.
		if (this.assumeMissingValuesButton.getSelection()) {
			options.assumeMissingValues = true;
		} else {
			options.assumeMissingValues = false;
		}

		if (this.askUserInCasOfMissingValuesButton.getSelection()) {
			options.askUserMissingValues = true;
		} else {
			options.askUserMissingValues = false;
		}

		// Save setting for the handling of native methods.
		options.doNotHaltOnNativeMethods = this.doNotHaltOnNativeMethodsButton.getSelection();
		options.assumeNativeReturnValuesToBeZeroNull = this.assumeNativeReturnValuesToBeZeroNullButton.getSelection();
		options.forwardJavaPackageNativeInvoc = this.forwardJavaPackageNativeInvocationsButton.getSelection();

		// Save settings for optimizations.
		options.dynReplaceInstWithOptimizedOnes = this.dynamicallyReplaceInstructionsWithOptimizedOnesButton.getSelection();

		// Save settings for the array initialization.
		options.symbArrayInitNumberOfRunsTotal = this.symbolicArrayInitializationNumberOfRunsTotal;
		options.symbArrayInitStartingLength = this.symbolicArrayInitializationStartingLength;
		int icrementationStrategy = 2;
		if (this.symbolicArrayInitializationIncrementationStrategyLinearButton.getSelection()) {
			icrementationStrategy = 0;
		} else if (this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.getSelection()) {
			icrementationStrategy = 1;
		} else if (this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.getSelection()) {
			icrementationStrategy = 3;
		}
		options.symbArrayInitIncrStrategy = icrementationStrategy;
		if (icrementationStrategy == 0) {
			options.symbArrayInitIncrStrategyLinearStepSize = this.symbolicArrayInitializationIncrementationStrategyLinearStepSize;
		}
		options.symbArrayInitTestNull = this.symbolicArrayInitializationTestNullButton.getSelection();
		options.symbArrayInitTestZeroLengthArray = this.symbolicArrayInitializationTestZeroLengthArrayButton.getSelection();

		// Save settings for the search algorithm.
		if (this.breadthFirstSearchButton.getSelection()) options.searchAlgorithm = 0;
		else if (this.depthFirstSearchButton.getSelection()) options.searchAlgorithm = 1;
		else {
			options.searchAlgorithm = 2;
			options.iterativeDeepeningStartingDepth = Integer.parseInt(this.iterativeDeepeningStartingDepthCombo.getText().replace(".", ""));
			options.iterativeDeepeningDeepnessIncrement = Integer.parseInt(this.iterativeDeepeningDeepnessIncrementCombo.getText().replace(".", ""));
		}

		// Save the SJVM multithreading setting.
		if (this.multithreadingButton.getSelection()) {
			options.enableSJVMMultithreading = true;
			options.numberOfSimultaneousThreads = this.numberOfSimultaneousThreads;
		} else {
			options.enableSJVMMultithreading = false;
		}

		// Save the maximum loops to take.
		options.maximumLoopsToTake = this.maximumLoops;

		// Save the settings for the abortion criteria.
		options.maxInstrBeforeFindingANewSolution = this.maximumInstructionsBeforeFindingANewSolution;
		if (this.onlyCountChoicePointGeneratingInstructions.getSelection()) {
			options.onlyCountChoicePointGeneratingInst = true;
		} else {
			options.onlyCountChoicePointGeneratingInst = false;
		}

		// Save coverage settings.
		options.useDUCoverage = this.useDefUseCoverageButton.getSelection();
		options.useCFCoverage = this.useControlFlowCoverageButton.getSelection();

		if (this.coverageTrackingInitialClassButton.getSelection()) {
			options.coverageTracking = 1;
		} else if (this.coverageTrackingInitialPackageButton.getSelection()) {
			options.coverageTracking = 2;
		} else if (this.coverageTrackingTopLevelPackageButton.getSelection()) {
			options.coverageTracking = 3;
		} else if (this.coverageTrackingAnyButton.getSelection()) {
			options.coverageTracking = 4;
		} else {
			options.coverageTracking = 0;
		}

		if (this.coverageAbortionCriteriaDefUseButton.getSelection()) {
			options.coverageAbortionCriteria = 1;
		} else if (this.coverageAbortionCriteriaControlFlowButton.getSelection()) {
			options.coverageAbortionCriteria = 2;
		} else if (this.coverageAbortionCriteriaFullButton.getSelection()) {
			options.coverageAbortionCriteria = 3;
		} else {
			options.coverageAbortionCriteria = 0;
		}

		// Save settings for the test case settings.
		options.testClassesDirectory = this.testClassesDirectoryText.getText();
		options.testClassesPackageName = this.testClassesPackageNameText.getText();
		options.testClassesName = this.testClassesNameText.getText();

		// Save the settings from the logging.
		if (this.loggingLayoutCombo.getText().equals("Simple text")) {
			options.setHtmlLogging(false);
		} else {
			options.setHtmlLogging(true);
		}
		if (this.maximumEntriesPerLogfileCombo.getText().equals("infinite")) {
			options.maximumLogEntries = -1;
		} else {
			options.maximumLogEntries = Long.parseLong(this.maximumEntriesPerLogfileCombo.getText().replace(".", "").replace(",", ""));
		}

		// Save the settings for the test case elimination.
		if (this.eliminateSolutionsByCoverageDefUseButton.getSelection()) {
			options.eliminateSolutionsByCoverage = 1;
		} else if (this.eliminateSolutionsByCoverageControlFlowButton.getSelection()) {
			options.eliminateSolutionsByCoverage = 2;
		} else if (this.eliminateSolutionsByCoverageBothButton.getSelection()) {
			options.eliminateSolutionsByCoverage = 3;
		} else {
			options.eliminateSolutionsByCoverage = 0;
		}

		// Write the values to the configuration file.
		boolean success = ConfigReader.saveCurrentConfig();
		if (!success) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The settings were saved for this session but could not be written to the configuration file.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}
		this.needToSave = false;
		return true;
	}

	/**
	 * Check the settings for correctness. Some settings might have been set to values which
	 * cannot be saved. If this is detected, the method will return false and open a message
	 * box to inform the user.
	 * @return true, if every checked setting is correct, false otherwise.
	 */
	private boolean checkForCorrectness() {
		// Parse the maximum number of recently opened files.
		try {
			int numberOfRecentlyOpenedFiles = Integer.parseInt(this.numberOfRecentlyOpenedFilesCombo.getText());
			if (numberOfRecentlyOpenedFiles < 0 || numberOfRecentlyOpenedFiles > MAX_RECENTLY_OPENED_FILES) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the number of recently opened files. It must be greater than zero and less than or equal to " + MAX_RECENTLY_OPENED_FILES + ".", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		} catch (NumberFormatException e) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the number of recently opened files. It must be a valid integer value that is greater than zero and less than or equal to 50.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		// Parse the execution time String.
		try {
			this.maximumExecutionTime = parseTimeString(this.maximumExecutionTimeCombo.getText().replace(".", ""));
		} catch (NumberFormatException e) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the maximum execution time. It must be either a valid integer value or \"infinite\". As addition to the integer value, you have to use \"s\" (seconds), \"m\" (minutes), \"h\" (hours) or \"d\" (days).", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		if (this.maximumExecutionTime <= 0 && this.maximumExecutionTime != -1) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The maximum execution time must be positive value.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		// Parse strings for the array initialization.
		try {
			this.symbolicArrayInitializationNumberOfRunsTotal = Integer.parseInt(this.symbolicArrayInitializationNumberOfRunsTotalCombo.getText().replace(".", ""));
		} catch (NumberFormatException e) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the number of total runs when initializing an array in symbolic execution mode.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		if (this.symbolicArrayInitializationNumberOfRunsTotal <= 0) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The number of total runs when initializing an array in symbolic execution mode must be greater than zero.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		try {
			this.symbolicArrayInitializationStartingLength = Integer.parseInt(this.symbolicArrayInitializationStartingLengthCombo.getText().replace(".", ""));
		} catch (NumberFormatException e) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the starting length when initializing an array in symbolic execution mode.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		if (this.symbolicArrayInitializationStartingLength < 0) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The starting length when initializing an array in symbolic execution mode must be greater than or equal to zero.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		// Check that the number of runs is not too high for a certain generation algorithm.
		if (this.symbolicArrayInitializationIncrementationStrategyLinearButton.getSelection()) {
			// Starting length has to be at least one for linear increasing.
			if (this.symbolicArrayInitializationStartingLength < 1) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "The starting length when initializing an array in symbolic execution mode and using the linear increasing strategy must be greater than or equal to one.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}

			// Check the highest possible value.
			long numberOfElementsLong = this.symbolicArrayInitializationStartingLength + ((long) this.symbolicArrayInitializationNumberOfRunsTotal) * ((long) this.symbolicArrayInitializationIncrementationStrategyLinearStepSize);
			if (numberOfElementsLong > Integer.MAX_VALUE) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "When initializing an array in symbolic execution mode and using the linear increasing strategy starting_legth + step_size * number_of_runs has to be less or equal to (2 ^ 31) - 1. This is the greatest size an array can have, as it is the maximum size for an integer type in the java virtual machine.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}
		if (this.symbolicArrayInitializationIncrementationStrategyFibonacciButton.getSelection()) {
			// Starting length has to be at least two for fibonacci increasing.
			if (this.symbolicArrayInitializationStartingLength < 2) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "The starting length when initializing an array in symbolic execution mode and using the fibonacci increasing strategy must be greater than or equal to two.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}

			// Check the highest possible value.
			long numberOfElementsLong;
			if (this.symbolicArrayInitializationStartingLength + this.symbolicArrayInitializationNumberOfRunsTotal <= 47) {
				numberOfElementsLong = fibonacci(this.symbolicArrayInitializationStartingLength + this.symbolicArrayInitializationNumberOfRunsTotal);
			} else {
				numberOfElementsLong = Integer.MAX_VALUE + 1L;
			}
			if (numberOfElementsLong > Integer.MAX_VALUE) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "When initializing an array in symbolic execution mode and using the fibonacci increasing strategy  the fibonacci number of starting_legth + number_of_runs has to be less or equal to (2 ^ 31) - 1. This is the greatest size an array can have, as it is the maximum size for an integer type in the java virtual machine.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}
		if (this.symbolicArrayInitializationIncrementationStrategyExponentialButton.getSelection()) {
			// Check the highest possible value.
			long numberOfElementsLong;
			if (this.symbolicArrayInitializationNumberOfRunsTotal <= 31) {
				numberOfElementsLong = this.symbolicArrayInitializationStartingLength + (long) Math.pow(2, this.symbolicArrayInitializationNumberOfRunsTotal);
				if (this.symbolicArrayInitializationNumberOfRunsTotal == 31) numberOfElementsLong--;
			} else {
				numberOfElementsLong = Integer.MAX_VALUE + 1L;
			}
			if (numberOfElementsLong > Integer.MAX_VALUE) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "When initializing an array in symbolic execution mode and using the exponential increasing strategy starting_legth + 2 ^ number_of_runs has to be less or equal to (2 ^ 31) - 1. This is the greatest size an array can have, as it is the maximum size for an integer type in the java virtual machine.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}
		if (this.symbolicArrayInitializationIncrementationStrategyTenPowerButton.getSelection()) {
			// Check the highest possible value.
			long numberOfElementsLong;
			if (this.symbolicArrayInitializationNumberOfRunsTotal <= 9) {
				numberOfElementsLong = this.symbolicArrayInitializationStartingLength + (long) Math.pow(10, this.symbolicArrayInitializationNumberOfRunsTotal);
			} else {
				numberOfElementsLong = Integer.MAX_VALUE + 1L;
			}
			if (numberOfElementsLong > Integer.MAX_VALUE) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "When initializing an array in symbolic execution mode and using the 10 to the power of x increasing strategy starting_legth + 10 ^ x has to be less or equal to (2 ^ 31) - 1. This is the greatest size an array can have, as it is the maximum size for an integer type in the java virtual machine.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}

		// Parse the linear increment string if necessary.
		if (this.symbolicArrayInitializationIncrementationStrategyLinearButton.getSelection()) {
			try {
				this.symbolicArrayInitializationIncrementationStrategyLinearStepSize = Integer.parseInt(this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.getText().replace(".", ""));
			} catch (NumberFormatException e) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have chosen linear increasing as the increasing strategy for initializing an array in symbolic execution mode. Please choose a valid value for the increament per step.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}

			if (this.symbolicArrayInitializationIncrementationStrategyLinearStepSize <= 0) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have chosen linear increasing as the increasing strategy for initializing an array in symbolic execution mode. The step size must be greater than zero.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}

		// If iterative deepening is selected as the search algorithm, check if the corresponding combo boxes have valid integer values.
		if (this.iterativeDeepeningButton.getSelection()) {
			try {
				Integer.parseInt(this.iterativeDeepeningStartingDepthCombo.getText().replace(".", ""));
			} catch (NumberFormatException e) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please enter an integer value for the maximum depth when choosing the iterative deepening search algorithm.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
			try {
				Integer.parseInt(this.iterativeDeepeningDeepnessIncrementCombo.getText().replace(".", ""));
			} catch (NumberFormatException e) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please enter an integer value for the deepening increment when choosing the iterative deepening search algorithm.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}

		// If required, parse the number of simultaneous threads String.
		if (this.multithreadingButton.getSelection()) {
			try {
				this.numberOfSimultaneousThreads = Integer.parseInt(this.numberOfSimultaneousThreadsCombo.getText().replace(".", ""));
			} catch (NumberFormatException e) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the number of simultaneous SJVM threads.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}

			if (this.numberOfSimultaneousThreads < 2) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "The number of simultaneous SJVM threads must be two at least.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}

		// Parse the maximum loops String.
		try {
			if (this.maximumLoopsCombo.getText().equals("infinite")) {
				this.maximumLoops = -1;
			} else {
				this.maximumLoops = Integer.parseInt(this.maximumLoopsCombo.getText().replace(".", ""));
			}
		} catch (NumberFormatException e) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the maximum loops to take.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		if (this.maximumLoops <= 0 && this.maximumLoops != -1) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The maximum loops to take must be positive value.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		// Parse the maximum instructions before finding a new solution String.
		try {
			if (this.maximumInstructionsBeforeFindingANewSolutionCombo.getText().equals("infinite")) {
				this.maximumInstructionsBeforeFindingANewSolution = -1;
			} else {
				this.maximumInstructionsBeforeFindingANewSolution = Integer.parseInt(this.maximumInstructionsBeforeFindingANewSolutionCombo.getText().replace(".", ""));
			}
		} catch (NumberFormatException e) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please choose a valid value for the maximum number of instructions before finding a new solution.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		if (this.maximumInstructionsBeforeFindingANewSolution <= 0 && this.maximumInstructionsBeforeFindingANewSolution != -1) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The maximum number of instructions before finding a new solution must be positive value.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		// Check that no coverage or test case settings that are disabled are selected. This is only of interest if either def-use or control flow tracking is enabled.
		if (this.useDefUseCoverageButton.getSelection() ^ this.useControlFlowCoverageButton.getSelection()) {
			if (this.useControlFlowCoverageButton.getSelection()) {
				if (this.coverageAbortionCriteriaDefUseButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled def-use chain tracking but selected def-use coverage as an abortion criterion. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
				if (this.coverageAbortionCriteriaFullButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled def-use chain tracking but selected def-use and control flow coverage as an abortion criterion. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
				if (this.eliminateSolutionsByCoverageDefUseButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled def-use chain tracking but selected def-use coverage for the elimination of test cases. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
				if (this.eliminateSolutionsByCoverageBothButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled def-use chain tracking but selected def-use and control flow coverage for the elimination of test cases. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
			} else {
				if (this.coverageAbortionCriteriaControlFlowButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled control flow tracking but selected control flow coverage as an abortion criterion. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
				if (this.coverageAbortionCriteriaFullButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled control flow tracking but selected def-use and control flow coverage as an abortion criterion. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
				if (this.eliminateSolutionsByCoverageControlFlowButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled control flow tracking but selected control flow coverage for the elimination of test cases. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
				if (this.eliminateSolutionsByCoverageBothButton.getSelection()) {
					StaticGuiSupport.showMessageBox(this.shell, "Warning", "You have disabled control flow tracking but selected def-use and control flow coverage for the elimination of test cases. This is impossible.", SWT.OK | SWT.ICON_WARNING);
					return false;
				}
			}
		}

		// Check if the test cases directory exists.
		File file = new File(this.testClassesDirectoryText.getText());
		if (!file.exists() || !file.isDirectory()) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The test cases directory does not exist. Please correct it.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		// The String value for the test case class name prefix cannot be fully checked here. However, it should not have any space in it.
		if (this.testClassesNameText.getText().contains(" ")) {
			StaticGuiSupport.showMessageBox(this.shell, "Warning", "The test cases class name prefix must not have any space in it.", SWT.OK | SWT.ICON_WARNING);
			return false;
		}

		// Correct values for the maximum number of entries per logfile are long values and "infinite".
		String maximumLogEntries = this.maximumEntriesPerLogfileCombo.getText();
		if (!maximumLogEntries.equals("infinite")) {
			try {
				Long.parseLong(maximumLogEntries.replace(".", "").replace(",", ""));
			} catch (NumberFormatException e) {
				StaticGuiSupport.showMessageBox(this.shell, "Warning", "Please enter an (long) integer value or \"infinite\" for the maximum number of entries per logfile.", SWT.OK | SWT.ICON_WARNING);
				return false;
			}
		}

		// Everything is fine!
		return true;
	}

	/**
	 * Get the xth fibonacci number. This method works recursively.
	 * @param x The fibonacci number to calculate.
	 * @return The calculated fibonacci number
	 */
	private int fibonacci(long x) {
		if (x == 0) return 0;
		if (x == 1) return 1;
		return fibonacci(x - 1) + fibonacci(x - 2);
	}

	/**
	 * Some settings has been changed in the gui, so enable the "apply" button.
	 */
	protected void somethingHasChanged() {
		this.needToSave = true;
		this.applyButton.setEnabled(true);
		this.discardButton.setEnabled(true);
	}

	/**
	 * Enable or disable the controls for the iterative deepening search algorithm
	 * settings.
	 * @param status If true, enable the controls, otherwise disable them.
	 */
	protected void setIterativeDeepeningSettingsStatus(boolean status) {
		this.iterativeDeepeningStartingDepthLabel.setEnabled(status);
		this.iterativeDeepeningStartingDepthCombo.setEnabled(status);
		this.iterativeDeepeningDeepnessIncrementLabel.setEnabled(status);
		this.iterativeDeepeningDeepnessIncrementCombo.setEnabled(status);
	}

	/**
	 * Enable or disable the label and the combo for the step size for the linear
	 * increasing strategy.
	 *
	 * @param status If true, enable the label and the combo, otherwise disable them.
	 */
	protected void setIncrementationStrategyLinearStepSizeStatus(boolean status) {
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeLabel.setEnabled(status);
		this.symbolicArrayInitializationIncrementationStrategyLinearStepSizeCombo.setEnabled(status);
	}

	/**
	 * Process a time in seconds and format it to a single value. This is
	 * a number followed by either "s" (seconds), "m" (minutes), "h" (hours),
	 * or "d" (days). There is one exception: If the input time is "-1", the
	 * returned string will be "infinity".
	 *
	 * This method must not be used if the time is not in full days, hours
	 * or minutes. For example, 90 seconds will just be displayed as "1m".
	 *
	 * @param time The input time in seconds.
	 * @return The formated value.
	 */
	private String formatLongTimeToSingleValue(long time) {
		if (time == -1) return "infinity";
		if (time > SECOND_DAY) return String.valueOf(time / SECOND_DAY) + "d";
		if (time > SECONDS_HOUR) return String.valueOf(time / SECONDS_HOUR) + "h";
		if (time > SECONDS_MINUTE) return String.valueOf(time / SECONDS_MINUTE) + "m";
		return time + "s";
	}

	/**
	 * Parse a String showing a time value in the form "time dimension"
	 * and return the value in seconds. If the String is "infinity",
	 * "-1" is returned.
	 * @param timeString The String to parse.
	 * @return The time in seconds as a long.
	 * @throws NumberFormatException If no number could be parsed.
	 */
	private long parseTimeString(String timeString) {
		timeString = timeString.trim();
		if (timeString.equals("infinity")) return -1;
		if (timeString.length() < 2) throw new NumberFormatException("The minimum length of the value has to be two.");
		String dimension = timeString.substring(timeString.length() - 1);
		timeString = timeString.substring(0, timeString.length() - 1);
		timeString = timeString.trim();
		int time = Integer.parseInt(timeString);
		if (dimension.equals("m")) time *= SECONDS_MINUTE;
		else if (dimension.equals("h")) time *= SECONDS_HOUR;
		else if (dimension.equals("d")) time *= SECOND_DAY;
		return time;
	}

    /**
     * Open the dialog to chose a directory for the test case classes.
     */
	protected void chooseTestClassesDirectory() {
		// Initialize the dialog.
		DirectoryDialog dialog = new DirectoryDialog(this.shell);
		dialog.setMessage("Please chose a directory for the test case classes.");
		// Check if the test cases directory exists.
		File file = new File(this.testClassesDirectoryText.getText());
		if (file.exists() && file.isDirectory()) {
			// Set as the start directory.
			dialog.setFilterPath(this.testClassesDirectoryText.getText());
		}

		// Open the dialog and process its result.
		String path = dialog.open();
		if (path != null) {
			// First of all replace double backslashes against slashes.
			path = path.replace("\\\\", "\\");

			// Convert backslashes to slashes.
			path = path.replace("\\", "/");

			// Set it as the text.
			this.testClassesDirectoryText.setText(path);
		}
	}

	/**
	 * Change the background color of an image and draw a rectangle on it that
	 * has the same bounds as the image. This effectively turns the image into
	 * the color supplied with the RGB.
	 * @param image The image to be painted.
	 * @param rgb The desired RGB for the image.
	 */
	protected void changeImageBackgroundColor(Image image, RGB rgb) {
		Display display = this.shell.getDisplay();
		GC gc = new GC(image);
		gc.setBackground(new Color(display, rgb));
		gc.fillRectangle(this.fileInspConstantClassImage.getBounds());
		gc.dispose();
	}


	/**
	 * Free the ressources used by the images.
	 */
	protected void freeImageRessources() {
		this.fileInspConstantClassImage.dispose();
		this.fileInspConstantDoubleImage.dispose();
		this.fileInspConstantFieldrefImage.dispose();
		this.fileInspConstantFloatImage.dispose();
		this.fileInspConstantIntegerImage.dispose();
		this.fileInspConstantInterfaceMethodrefImage.dispose();
		this.fileInspConstantLongImage.dispose();
		this.fileInspConstantMethodrefImage.dispose();
		this.fileInspConstantNameAndTypeImage.dispose();
		this.fileInspConstantStringImage.dispose();
		this.fileInspConstantUtf8Image.dispose();
	}

	/**
	 * Enable or disable the buttons for the coverage tracking, abortion by
	 * coverage fulfillment and test cases elimination.
	 *
	 */
	protected void enableOrDisableCoverageButtons() {
		boolean useDefUseCoverage = this.useDefUseCoverageButton.getSelection();
		boolean useControlFlowCoverage = this.useControlFlowCoverageButton.getSelection();

		this.coverageAbortionGroup.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.coverageTrackingInitialMethodButton.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.coverageTrackingInitialClassButton.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.coverageTrackingInitialPackageButton.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.coverageTrackingTopLevelPackageButton.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.coverageTrackingAnyButton.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.coverageAbortionCriteriaNoButton.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.coverageAbortionCriteriaDefUseButton.setEnabled(useDefUseCoverage);
		this.coverageAbortionCriteriaControlFlowButton.setEnabled(useControlFlowCoverage);
		this.coverageAbortionCriteriaFullButton.setEnabled(useDefUseCoverage && useControlFlowCoverage);
		this.testCaseEliminationGroup.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.eliminateSolutionsByCoverageNotButton.setEnabled(useDefUseCoverage || useControlFlowCoverage);
		this.eliminateSolutionsByCoverageDefUseButton.setEnabled(useDefUseCoverage);
		this.eliminateSolutionsByCoverageControlFlowButton.setEnabled(useControlFlowCoverage);
		this.eliminateSolutionsByCoverageBothButton.setEnabled(useDefUseCoverage && useControlFlowCoverage);
	}

}
