package de.wwu.muggl.ui.gui.components;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Stack;

import org.apache.log4j.Priority;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.common.TimeSupport;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.BiPush;
import de.wwu.muggl.instructions.bytecode.Goto;
import de.wwu.muggl.instructions.bytecode.Invokeinterface;
import de.wwu.muggl.instructions.bytecode.Invokespecial;
import de.wwu.muggl.instructions.bytecode.Invokestatic;
import de.wwu.muggl.instructions.bytecode.Invokevirtual;
import de.wwu.muggl.instructions.bytecode.SiPush;
import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.general.If;
import de.wwu.muggl.instructions.general.If_icmp;
import de.wwu.muggl.instructions.general.PushAbstract;
import de.wwu.muggl.instructions.general.PushFromConstantPool;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.control.JumpAlways;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.instructions.interfaces.control.JumpInvocation;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.instructions.interfaces.control.JumpSwitching;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.ImageRepository;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.support.StepByStepExecutionRunner;
import de.wwu.muggl.ui.gui.support.StepByStepLoggingAppender;
import de.wwu.muggl.ui.gui.windows.FastExecutionWindow;
import de.wwu.muggl.ui.gui.windows.InputWindow;
import de.wwu.muggl.ui.gui.windows.OptionsWindow;
import de.wwu.muggl.ui.gui.windows.StepByStepExecutionWindow;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.Limitations;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * The composite for the StepByStepExecutionWindow. It offers most of its
 * element and the corresponding methods. It heavily utilizes the supplied
 * StepByStepExecutionRunner instance.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-18
 */
public class StepByStepExecutionComposite extends Composite {
	// General fields for the window.
	StepByStepExecutionWindow parent;
	Shell shell;
	Display display;
	StepByStepExecutionRunner executionRunner;
	Application application;
	private int currentlyHighlightedInstruction;
	private int[] styleRangeOffsets;

	// Execution related fields - initialized later.
	private int[] instructionsAndOtherBytesToInstructionsMappingTable;
	private int[] instructionsToInstructionsAndOtherBytesMappingTable;

	// Fields for the StepByStepExecutionRunner
	MugglClassLoader classLoader;
	private ClassFile classFile;
	Method method;

	// Variables for thread safety.
	private boolean readyForNextMessage;
	private boolean instructionLoadingComplete;

	// Logging.
	private StepByStepLoggingAppender styledTextAppender;
	private ArrayList<Long> newLoggingEntryTimestamp;
	private ArrayList<Priority> newLoggingEntryPriority;
	private ArrayList<String> newLoggingEntryMessage;
	private int entriesLogged;

	// Colors.
	private final Color COLOR_JUMP_NEVER;
	private final Color COLOR_JUMP_ALWAYS;
	private final Color COLOR_JUMP_CONDITIONAL;
	private final Color COLOR_JUMP_EXCEPTION;
	private final Color COLOR_JUMP_SWITCHING;
	private final Color COLOR_JUMP_INVOCATION;

	// Constant fields for the composites elements. The left group with the bytecode list.
	private final FormData methodNameFormData;
	private final Label methodNameLabel;
	private final FormData bytecodeFormData;
	private final Composite bytecodeComposite;
	private final StyledText bytecodeStyledText;

	// The machine state group.
	private final FormData machineStateFormData;
	private final Group machineStateGroup;
	private final FormData localVariablesLabelFormData;
	private final Label localVariablesLabel;
	private final FormData localVariablesFormData;
	private final List localVariablesList;
	private final FormData operandStackLabelFormData;
	private final Label operandStackLabel;
	private final FormData operandStackFormData;
	private final List operandStackList;
	private final FormData operandStackEditFormData;
	private final Button operandStackEditButton;
	private final FormData operandStackPushFormData;
	private final Button operandStackPushButton;
	private final FormData operandStackPopFormData;
	private final Button operandStackPopButton;
	private final FormData operandStackEmptyFormData;
	private final Button operandStackEmptyButton;
	private final FormData virtualMachineStackLabelFormData;
	private final Label virtualMachineStackLabel;
	private final FormData virtualMachineStackFormData;
	private final List virtualMachineStackList;

	// The symbolic execution group.
	private final FormData symbolicExecutionStateFormData;
	private final Group symbolicExecutionStateGroup;
	private final FormData choicePointsLabelFormData;
	private final Label choicePointsLabel;
	private final FormData choicePointsFormData;
	private final List choicePointsList;
	private final FormData constraintsLabelFormData;
	private final Label constraintsLabel;
	private final FormData constraintsFormData;
	private final List constraintsList;
	private final FormData coverageLabelFormData;
	private final Label coverageLabel;
	private final FormData coverageFormData;
	private final List coverageList;

	// The solutions group.
	private final FormData solutionsFormData;
	private final Group solutionsGroup;
	private final FormData solutionsListFormData;
	private final List solutionsList;

	// Buttons and other widgets to control the execution, the logging window, the exit and the options button.
	private final FormData buttonsFormData;
	private final Group buttonsGroup;
	private final FormData restartFormData;
	private final Button restartButton;
	private final FormData nextStepFormData;
	private final Button nextStepButton;
	private final FormData buttonsSash1FormData;
	private final Sash buttonsSash1;
	private final FormData executeNextLabelFormData;
	private final Label executeNextLabel;
	private final FormData executeNextComboFormData;
	private final Combo executeNextCombo;
	private final FormData stepsFormData;
	private final Label stepsLabel;
	private final FormData goFormData;
	private final Button goButton;
	private final FormData gotoFormData;
	private final Button gotoButton;
	private final FormData executeOnlyFormData;
	private final Button executeOnlyButton;
	private final FormData secondsComboFormData;
	private final Combo secondsCombo;
	private final FormData secondsLabelFormData;
	private final Label secondsLabel;
	private final FormData stopFormData;
	private final Button stopButton;
	private final FormData buttonsSash2FormData;
	private final Sash buttonsSash2;
	private final FormData loggingSpacerFormData;
	private final Composite loggingSpacerComposite;
	private final FormData loggingFormData;
	private final StyledText loggingStyledText;
	private final FormData buttonsSash3FormData;
	private final Sash buttonsSash3;
	private final FormData optionsFormData;
	private final Button optionsButton;
	private final FormData exitFormData;
	private final Button exitButton;

	/**
	 * Set up the composite for the StepByStepExecutionWindow window.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 * @param classLoader The system MugglClassLoader.
	 * @param classFile The classFile the initial Method belongs to.
	 * @param method The initial Method.
	 * @throws GUIException Thrown by errors thats consequences render displaying the window useless.
	 */
	public StepByStepExecutionComposite(StepByStepExecutionWindow parent,
			Shell shell, Display display, int style,
			MugglClassLoader classLoader, ClassFile classFile, Method method)
			throws GUIException {
		// Basic initialization.
		super(shell, style);
		this.parent = parent;
		this.shell = shell;
		this.display = display;

		// Save the setup information for the execution runner for later.
		this.classLoader = classLoader;
		this.classFile = classFile;
		this.method = method;

		// Setting variables for thread safety to true.
		this.readyForNextMessage = true;
		this.instructionLoadingComplete = true;

		// Set up the colors.
		this.COLOR_JUMP_NEVER = new Color(this.display, 0, 220, 0); // Green.
		this.COLOR_JUMP_ALWAYS = new Color(this.display, 220, 0, 0); // Red.
		this.COLOR_JUMP_CONDITIONAL = new Color(this.display, 240, 240, 0); // Yellow.
		this.COLOR_JUMP_EXCEPTION = new Color(this.display, 00, 160, 0); // Darker green.
		this.COLOR_JUMP_SWITCHING = new Color(this.display, 220, 60, 220); // Magenta.
		this.COLOR_JUMP_INVOCATION = new Color(this.display, 0, 220, 160); // Green blue.

		// Initialize the StepByStepExecutionRunner.
		setupExecutionRunner();

		try {
			// Setup the application
			if (!this.executionRunner.setupApplication()) {
				abortExecution(false);
				if (this.application == null) {
					throw new GUIException("Do not show this window!");
				}
				try {
					throw new GUIException(this.application.fetchError());
				} catch (NullPointerException e) {
					throw new GUIException(
							"Cannot provide an error message since there is no virtual machine present. "
							+ "It probably crashed. See the log file for details. "
							+ "If it does not contain any information related to the problem, "
							+ "try a more detailed logging level.");
				}
			}

			// Get the image repository.
			ImageRepository repos = ImageRepository.getInst();
			
			// Layout
			this.setLayout(new FormLayout());

			// First add the Method name label.
			this.methodNameFormData = new FormData();
			this.methodNameFormData.top = new FormAttachment(this, 5, SWT.BOTTOM);
			this.methodNameFormData.bottom = new FormAttachment(this, 20, SWT.BOTTOM);
			this.methodNameFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
			this.methodNameFormData.right = new FormAttachment(this, 370, SWT.RIGHT);

			this.methodNameLabel = new Label(this, SWT.NONE);
			this.methodNameLabel.setLayoutData(this.methodNameFormData);

			// Set up the bytecode composite.
			this.bytecodeFormData = new FormData();
			this.bytecodeFormData.top = new FormAttachment(this.methodNameLabel, 5, SWT.BOTTOM);
			this.bytecodeFormData.bottom = new FormAttachment(this.methodNameLabel, 640, SWT.BOTTOM);
			this.bytecodeFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
			this.bytecodeFormData.right = new FormAttachment(this, 370, SWT.RIGHT);

			GridLayout bytecodeCompositeGridLayout = new GridLayout(1, false);
			bytecodeCompositeGridLayout.marginRight = 0;
			bytecodeCompositeGridLayout.marginTop = 0;
			bytecodeCompositeGridLayout.marginBottom = 0;
			bytecodeCompositeGridLayout.horizontalSpacing = 0;
			bytecodeCompositeGridLayout.verticalSpacing = 0;
			bytecodeCompositeGridLayout.marginWidth = 0;
			bytecodeCompositeGridLayout.marginHeight = 0;
			bytecodeCompositeGridLayout.marginLeft = 0;

			this.bytecodeComposite = new Composite(this, SWT.BORDER);
			this.bytecodeComposite.setLayoutData(this.bytecodeFormData);
			this.bytecodeComposite.setLayout(bytecodeCompositeGridLayout);

			this.bytecodeComposite.setBackground(this.display.getSystemColor(SWT.COLOR_WHITE));

			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.horizontalIndent = 3;

			this.bytecodeStyledText = new StyledText(this.bytecodeComposite, SWT.H_SCROLL | SWT.V_SCROLL);
			this.bytecodeStyledText.setBackground(this.display.getSystemColor(SWT.COLOR_WHITE));
			this.bytecodeStyledText.setEditable(false);
			this.bytecodeStyledText.setLayoutData(gridData);

			// Set up the machine state Group.
			this.machineStateFormData = new FormData();
			this.machineStateFormData.top = new FormAttachment(this, 5, SWT.BOTTOM);
			this.machineStateFormData.bottom = new FormAttachment(this, 660, SWT.BOTTOM);
			this.machineStateFormData.left = new FormAttachment(this.bytecodeComposite, 10, SWT.RIGHT);
			this.machineStateFormData.right = new FormAttachment(this.bytecodeComposite, 310, SWT.RIGHT);

			this.machineStateGroup = new Group(this, SWT.NONE);
			this.machineStateGroup.setText("Machine state");
			this.machineStateGroup.setLayout(new FormLayout());
			this.machineStateGroup.setLayoutData(this.machineStateFormData);

			this.localVariablesLabelFormData = new FormData();
			this.localVariablesLabelFormData.top = new FormAttachment(this.machineStateGroup, 2, SWT.BOTTOM);
			this.localVariablesLabelFormData.bottom = new FormAttachment(this.machineStateGroup, 17, SWT.BOTTOM);
			this.localVariablesLabelFormData.left = new FormAttachment(this.machineStateGroup, 5, SWT.RIGHT);
			this.localVariablesLabelFormData.right = new FormAttachment(this.machineStateGroup, 287, SWT.RIGHT);

			this.localVariablesLabel = new Label(this.machineStateGroup, SWT.NONE);
			this.localVariablesLabel.setText("Local variables:");
			this.localVariablesLabel.setLayoutData(this.localVariablesLabelFormData);

			this.localVariablesFormData = new FormData();
			this.localVariablesFormData.top = new FormAttachment(this.localVariablesLabel, 0, SWT.BOTTOM);
			this.localVariablesFormData.bottom = new FormAttachment(this.localVariablesLabel, 191, SWT.BOTTOM);
			this.localVariablesFormData.left = new FormAttachment(this.machineStateGroup, 5, SWT.RIGHT);
			this.localVariablesFormData.right = new FormAttachment(this.machineStateGroup, 287, SWT.RIGHT);

			this.localVariablesList = new List(this.machineStateGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.localVariablesList.setLayoutData(this.localVariablesFormData);

			this.operandStackLabelFormData = new FormData();
			this.operandStackLabelFormData.top = new FormAttachment(this.localVariablesList, 5, SWT.BOTTOM);
			this.operandStackLabelFormData.bottom = new FormAttachment(this.localVariablesList, 20, SWT.BOTTOM);
			this.operandStackLabelFormData.left = new FormAttachment(this.machineStateGroup, 5, SWT.RIGHT);
			this.operandStackLabelFormData.right = new FormAttachment(this.machineStateGroup, 287, SWT.RIGHT);

			this.operandStackLabel = new Label(this.machineStateGroup, SWT.NONE);
			this.operandStackLabel.setText("Operand stack:");
			this.operandStackLabel.setLayoutData(this.operandStackLabelFormData);

			this.operandStackFormData = new FormData();
			this.operandStackFormData.top = new FormAttachment(this.operandStackLabel, 0, SWT.BOTTOM);
			this.operandStackFormData.bottom = new FormAttachment(this.operandStackLabel, 191, SWT.BOTTOM);
			this.operandStackFormData.left = new FormAttachment(this.machineStateGroup, 5, SWT.RIGHT);
			this.operandStackFormData.right = new FormAttachment(this.machineStateGroup, 265, SWT.RIGHT);

			this.operandStackList = new List(this.machineStateGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.operandStackList.setLayoutData(this.operandStackFormData);

			this.operandStackEditFormData = new FormData();
			this.operandStackEditFormData.top = new FormAttachment(this.operandStackLabel, 5, SWT.BOTTOM);
			this.operandStackEditFormData.bottom = new FormAttachment(this.operandStackLabel, 25, SWT.BOTTOM);
			this.operandStackEditFormData.left = new FormAttachment(this.operandStackList, 4, SWT.RIGHT);
			this.operandStackEditFormData.right = new FormAttachment(this.operandStackList, 24, SWT.RIGHT);

			this.operandStackEditButton = new Button(this.machineStateGroup, SWT.NONE);
			try {
				// Try to load an image. If it fails, display text.
				this.operandStackEditButton.setImage(repos.editImage);
			} catch (SWTException e) {
				this.operandStackEditButton.setText("Edt");
			}
			this.operandStackEditButton.setToolTipText("Edit the selected entry...");
			this.operandStackEditButton.setSelection(false);
			this.operandStackEditButton.setLayoutData(this.operandStackEditFormData);

			this.operandStackPushFormData = new FormData();
			this.operandStackPushFormData.top = new FormAttachment(this.operandStackLabel, 30, SWT.BOTTOM);
			this.operandStackPushFormData.bottom = new FormAttachment(this.operandStackLabel, 50, SWT.BOTTOM);
			this.operandStackPushFormData.left = new FormAttachment(this.operandStackList, 4, SWT.RIGHT);
			this.operandStackPushFormData.right = new FormAttachment(this.operandStackList, 24, SWT.RIGHT);

			this.operandStackPushButton = new Button(this.machineStateGroup, SWT.NONE);
			try {
				// Try to load an image. If it fails, display text.
				this.operandStackPushButton.setImage(repos.pushImage);
			} catch (SWTException e) {
				this.operandStackPushButton.setText("Psh");
			}
			this.operandStackPushButton.setToolTipText("Push a new entry onto the stack");
			this.operandStackPushButton.setSelection(false);
			this.operandStackPushButton.setLayoutData(this.operandStackPushFormData);

			this.operandStackPopFormData = new FormData();
			this.operandStackPopFormData.top = new FormAttachment(this.operandStackLabel, 55, SWT.BOTTOM);
			this.operandStackPopFormData.bottom = new FormAttachment(this.operandStackLabel, 75, SWT.BOTTOM);
			this.operandStackPopFormData.left = new FormAttachment(this.operandStackList, 4, SWT.RIGHT);
			this.operandStackPopFormData.right = new FormAttachment(this.operandStackList, 24, SWT.RIGHT);

			this.operandStackPopButton = new Button(this.machineStateGroup, SWT.NONE);
			try {
				// Try to load an image. If it fails, display text.
				this.operandStackPopButton.setImage(repos.popImage);
			} catch (SWTException e) {
				this.operandStackPopButton.setText("Pop");
			}
			this.operandStackPopButton.setToolTipText("Pop the topmost entry from the stack.");
			this.operandStackPopButton.setSelection(false);
			this.operandStackPopButton.setLayoutData(this.operandStackPopFormData);

			this.operandStackEmptyFormData = new FormData();
			this.operandStackEmptyFormData.top = new FormAttachment(this.operandStackLabel, 80, SWT.BOTTOM);
			this.operandStackEmptyFormData.bottom = new FormAttachment(this.operandStackLabel, 100, SWT.BOTTOM);
			this.operandStackEmptyFormData.left = new FormAttachment(this.operandStackList, 4, SWT.RIGHT);
			this.operandStackEmptyFormData.right = new FormAttachment(this.operandStackList, 24, SWT.RIGHT);

			this.operandStackEmptyButton = new Button(this.machineStateGroup, SWT.NONE);
			try {
				// Try to load an image. If it fails, display text.
				this.operandStackEmptyButton.setImage(repos.emptyImage);
			} catch (SWTException e) {
				this.operandStackEmptyButton.setText("Emp");
			}
			this.operandStackEmptyButton.setToolTipText("Pop all entrys from the stack.");
			this.operandStackEmptyButton.setSelection(false);
			this.operandStackEmptyButton.setLayoutData(this.operandStackEmptyFormData);

			this.virtualMachineStackLabelFormData = new FormData();
			this.virtualMachineStackLabelFormData.top = new FormAttachment(this.operandStackList, 5, SWT.BOTTOM);
			this.virtualMachineStackLabelFormData.bottom = new FormAttachment(this.operandStackList, 20, SWT.BOTTOM);
			this.virtualMachineStackLabelFormData.left = new FormAttachment(this.machineStateGroup, 5, SWT.RIGHT);
			this.virtualMachineStackLabelFormData.right = new FormAttachment(this.machineStateGroup, 287, SWT.RIGHT);

			this.virtualMachineStackLabel = new Label(this.machineStateGroup, SWT.NONE);
			this.virtualMachineStackLabel.setText("Virtual machine stack:");
			this.virtualMachineStackLabel.setLayoutData(this.virtualMachineStackLabelFormData);

			this.virtualMachineStackFormData = new FormData();
			this.virtualMachineStackFormData.top = new FormAttachment(this.virtualMachineStackLabel, 0, SWT.BOTTOM);
			this.virtualMachineStackFormData.bottom = new FormAttachment(this.virtualMachineStackLabel, 191, SWT.BOTTOM);
			this.virtualMachineStackFormData.left = new FormAttachment(this.machineStateGroup, 5, SWT.RIGHT);
			this.virtualMachineStackFormData.right = new FormAttachment(this.machineStateGroup, 287, SWT.RIGHT);

			this.virtualMachineStackList = new List(this.machineStateGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.virtualMachineStackList.setLayoutData(this.virtualMachineStackFormData);

			// Set up the symbolic execution state Group.
			this.symbolicExecutionStateFormData = new FormData();
			this.symbolicExecutionStateFormData.top = new FormAttachment(this, 5, SWT.BOTTOM);
			this.symbolicExecutionStateFormData.bottom = new FormAttachment(this, 510, SWT.BOTTOM);
			this.symbolicExecutionStateFormData.left = new FormAttachment(this.machineStateGroup, 15, SWT.RIGHT);
			this.symbolicExecutionStateFormData.right = new FormAttachment(this.machineStateGroup, 328, SWT.RIGHT);

			this.symbolicExecutionStateGroup = new Group(this, SWT.NONE);
			this.symbolicExecutionStateGroup.setText("Symbolic execution state");
			this.symbolicExecutionStateGroup.setLayout(new FormLayout());
			this.symbolicExecutionStateGroup.setLayoutData(this.symbolicExecutionStateFormData);

			this.choicePointsLabelFormData = new FormData();
			this.choicePointsLabelFormData.top = new FormAttachment(this.symbolicExecutionStateGroup, 2, SWT.BOTTOM);
			this.choicePointsLabelFormData.bottom = new FormAttachment(this.symbolicExecutionStateGroup, 17, SWT.BOTTOM);
			this.choicePointsLabelFormData.left = new FormAttachment(this.symbolicExecutionStateGroup, 5, SWT.RIGHT);
			this.choicePointsLabelFormData.right = new FormAttachment(this.symbolicExecutionStateGroup, 301, SWT.RIGHT);

			this.choicePointsLabel = new Label(this.symbolicExecutionStateGroup, SWT.NONE);
			this.choicePointsLabel.setText("Choice points:");
			this.choicePointsLabel.setLayoutData(this.choicePointsLabelFormData);

			this.choicePointsFormData = new FormData();
			this.choicePointsFormData.top = new FormAttachment(this.choicePointsLabel, 0, SWT.BOTTOM);
			this.choicePointsFormData.bottom = new FormAttachment(this.choicePointsLabel, 141, SWT.BOTTOM);
			this.choicePointsFormData.left = new FormAttachment(this.symbolicExecutionStateGroup, 5, SWT.RIGHT);
			this.choicePointsFormData.right = new FormAttachment(this.symbolicExecutionStateGroup, 301, SWT.RIGHT);

			this.choicePointsList = new List(this.symbolicExecutionStateGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.choicePointsList.setLayoutData(this.choicePointsFormData);

			this.constraintsLabelFormData = new FormData();
			this.constraintsLabelFormData.top = new FormAttachment(this.choicePointsList, 5, SWT.BOTTOM);
			this.constraintsLabelFormData.bottom = new FormAttachment(this.choicePointsList, 20, SWT.BOTTOM);
			this.constraintsLabelFormData.left = new FormAttachment(this.symbolicExecutionStateGroup, 5, SWT.RIGHT);
			this.constraintsLabelFormData.right = new FormAttachment(this.symbolicExecutionStateGroup, 301, SWT.RIGHT);

			this.constraintsLabel = new Label(this.symbolicExecutionStateGroup, SWT.NONE);
			this.constraintsLabel.setText("Constraints:");
			this.constraintsLabel.setLayoutData(this.constraintsLabelFormData);

			this.constraintsFormData = new FormData();
			this.constraintsFormData.top = new FormAttachment(this.constraintsLabel, 0, SWT.BOTTOM);
			this.constraintsFormData.bottom = new FormAttachment(this.constraintsLabel, 141, SWT.BOTTOM);
			this.constraintsFormData.left = new FormAttachment(this.symbolicExecutionStateGroup, 5, SWT.RIGHT);
			this.constraintsFormData.right = new FormAttachment(this.symbolicExecutionStateGroup, 301, SWT.RIGHT);

			this.constraintsList = new List(this.symbolicExecutionStateGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.constraintsList.setLayoutData(this.constraintsFormData);

			this.coverageLabelFormData = new FormData();
			this.coverageLabelFormData.top = new FormAttachment(this.constraintsList, 5, SWT.BOTTOM);
			this.coverageLabelFormData.bottom = new FormAttachment(this.constraintsList, 20, SWT.BOTTOM);
			this.coverageLabelFormData.left = new FormAttachment(this.symbolicExecutionStateGroup, 5, SWT.RIGHT);
			this.coverageLabelFormData.right = new FormAttachment(this.symbolicExecutionStateGroup, 301, SWT.RIGHT);

			this.coverageLabel = new Label(this.symbolicExecutionStateGroup, SWT.NONE);
			this.coverageLabel.setText("Coverage:");
			this.coverageLabel.setLayoutData(this.coverageLabelFormData);

			this.coverageFormData = new FormData();
			this.coverageFormData.top = new FormAttachment(this.coverageLabel, 0, SWT.BOTTOM);
			this.coverageFormData.bottom = new FormAttachment(this.coverageLabel, 141, SWT.BOTTOM);
			this.coverageFormData.left = new FormAttachment(this.symbolicExecutionStateGroup, 5, SWT.RIGHT);
			this.coverageFormData.right = new FormAttachment(this.symbolicExecutionStateGroup, 301, SWT.RIGHT);

			this.coverageList = new List(this.symbolicExecutionStateGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.coverageList.setLayoutData(this.coverageFormData);

			// Set up the listener for the buttons in the machine state Group.
			/*
			 * Edit the selected entry of the operand stack.
			 */
			this.operandStackEditButton.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							editOperandStackEntry();
						}
					});

			/*
			 * Push a new entry onto the operand stack.
			 */
			this.operandStackPushButton.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							pushOntoOperandStack();
						}
					});

			/*
			 * Pop the topmost entry from the operand stack.
			 */
			this.operandStackPopButton.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							popFromOperandStack();
						}
					});

			/*
			 * Pop all entries from the operand stack.
			 */
			this.operandStackEmptyButton.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							emptyOperandStack();
						}
					});

			// Set up the solutions Group.
			this.solutionsFormData = new FormData();
			this.solutionsFormData.top = new FormAttachment(this.symbolicExecutionStateGroup, 10, SWT.BOTTOM);
			this.solutionsFormData.bottom = new FormAttachment(this.symbolicExecutionStateGroup, 150, SWT.BOTTOM);
			this.solutionsFormData.left = new FormAttachment(this.machineStateGroup, 15, SWT.RIGHT);
			this.solutionsFormData.right = new FormAttachment(this.machineStateGroup, 328, SWT.RIGHT);

			this.solutionsGroup = new Group(this, SWT.NONE);
			this.solutionsGroup.setText("Solutions");
			this.solutionsGroup.setLayout(new FormLayout());
			this.solutionsGroup.setLayoutData(this.solutionsFormData);

			this.solutionsListFormData = new FormData();
			this.solutionsListFormData.top = new FormAttachment(this.solutionsGroup, 2, SWT.BOTTOM);
			this.solutionsListFormData.bottom = new FormAttachment(this.solutionsGroup, 120, SWT.BOTTOM);
			this.solutionsListFormData.left = new FormAttachment(this.solutionsGroup, 5, SWT.RIGHT);
			this.solutionsListFormData.right = new FormAttachment(this.solutionsGroup, 301, SWT.RIGHT);

			this.solutionsList = new List(this.solutionsGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			this.solutionsList.setLayoutData(this.solutionsListFormData);

			// Set up the buttons Group.
			this.buttonsFormData = new FormData();
			this.buttonsFormData.top = new FormAttachment(this.bytecodeComposite, 5, SWT.BOTTOM);
			this.buttonsFormData.bottom = new FormAttachment(this.bytecodeComposite, 80, SWT.BOTTOM);
			this.buttonsFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
			this.buttonsFormData.right = new FormAttachment(this, 1008, SWT.RIGHT);

			this.buttonsGroup = new Group(this, SWT.NONE);
			this.buttonsGroup.setText("Execution");
			this.buttonsGroup.setLayout(new FormLayout());
			this.buttonsGroup.setLayoutData(this.buttonsFormData);

			this.nextStepFormData = new FormData();
			this.nextStepFormData.top = new FormAttachment(this.buttonsGroup, 4, SWT.BOTTOM);
			this.nextStepFormData.bottom = new FormAttachment(this.buttonsGroup, 29, SWT.BOTTOM);
			this.nextStepFormData.left = new FormAttachment(this.buttonsGroup, 5, SWT.RIGHT);
			this.nextStepFormData.right = new FormAttachment(this.buttonsGroup, 85, SWT.RIGHT);

			this.nextStepButton = new Button(this.buttonsGroup, SWT.NONE);
			this.nextStepButton.setText("Next step");
			this.nextStepButton.setSelection(false);
			this.nextStepButton.setLayoutData(this.nextStepFormData);

			this.restartFormData = new FormData();
			this.restartFormData.top = new FormAttachment(this.buttonsGroup, 32, SWT.BOTTOM);
			this.restartFormData.bottom = new FormAttachment(this.buttonsGroup, 57, SWT.BOTTOM);
			this.restartFormData.left = new FormAttachment(this.buttonsGroup, 5, SWT.RIGHT);
			this.restartFormData.right = new FormAttachment(this.buttonsGroup, 85, SWT.RIGHT);

			this.restartButton = new Button(this.buttonsGroup, SWT.NONE);
			this.restartButton.setText("Restart");
			this.restartButton.setSelection(false);
			this.restartButton.setLayoutData(this.restartFormData);

			this.buttonsSash1FormData = new FormData();
			this.buttonsSash1FormData.top = new FormAttachment(this.buttonsGroup, 6, SWT.BOTTOM);
			this.buttonsSash1FormData.bottom = new FormAttachment(this.buttonsGroup, 55, SWT.BOTTOM);
			this.buttonsSash1FormData.left = new FormAttachment(this.nextStepButton, 7, SWT.RIGHT);
			this.buttonsSash1FormData.right = new FormAttachment(this.nextStepButton, 12, SWT.RIGHT);

			this.buttonsSash1 = new Sash(this.buttonsGroup, SWT.BORDER);
			this.buttonsSash1.setLayoutData(this.buttonsSash1FormData);

			this.executeNextLabelFormData = new FormData();
			this.executeNextLabelFormData.top = new FormAttachment(this.buttonsGroup, 9, SWT.BOTTOM);
			this.executeNextLabelFormData.bottom = new FormAttachment(this.buttonsGroup, 29, SWT.BOTTOM);
			this.executeNextLabelFormData.left = new FormAttachment(this.buttonsSash1, 7, SWT.RIGHT);
			this.executeNextLabelFormData.right = new FormAttachment(this.buttonsSash1, 78, SWT.RIGHT);

			this.executeNextLabel = new Label(this.buttonsGroup, SWT.NONE);
			this.executeNextLabel.setText("Execute next");
			this.executeNextLabel.setLayoutData(this.executeNextLabelFormData);

			this.executeNextComboFormData = new FormData();
			this.executeNextComboFormData.top = new FormAttachment(this.buttonsGroup, 6, SWT.BOTTOM);
			this.executeNextComboFormData.bottom = new FormAttachment(this.buttonsGroup, 29, SWT.BOTTOM);
			this.executeNextComboFormData.left = new FormAttachment(this.executeNextLabel, 5, SWT.RIGHT);
			this.executeNextComboFormData.right = new FormAttachment(this.executeNextLabel, 80, SWT.RIGHT);

			this.executeNextCombo = new Combo(this.buttonsGroup, SWT.DROP_DOWN);
			this.executeNextCombo.setLayoutData(this.executeNextComboFormData);
			this.executeNextCombo.add("1");
			this.executeNextCombo.add("2");
			this.executeNextCombo.add("3");
			this.executeNextCombo.add("5");
			this.executeNextCombo.add("10");
			this.executeNextCombo.add("25");
			this.executeNextCombo.add("50");
			this.executeNextCombo.add("100");
			this.executeNextCombo.add("200");
			this.executeNextCombo.add("500");
			this.executeNextCombo.add("1.000");
			this.executeNextCombo.add("2.000");
			this.executeNextCombo.add("5.000");
			this.executeNextCombo.add("10.000");
			this.executeNextCombo.add("unlimited");

			this.stepsFormData = new FormData();
			this.stepsFormData.top = new FormAttachment(this.buttonsGroup, 9, SWT.BOTTOM);
			this.stepsFormData.bottom = new FormAttachment(this.buttonsGroup, 29, SWT.BOTTOM);
			this.stepsFormData.left = new FormAttachment(this.executeNextCombo, 4, SWT.RIGHT);
			this.stepsFormData.right = new FormAttachment(this.executeNextCombo, 40, SWT.RIGHT);

			this.stepsLabel = new Label(this.buttonsGroup, SWT.NONE);
			this.stepsLabel.setText("steps.");
			this.stepsLabel.setLayoutData(this.stepsFormData);

			this.goFormData = new FormData();
			this.goFormData.top = new FormAttachment(this.buttonsGroup, 4, SWT.BOTTOM);
			this.goFormData.bottom = new FormAttachment(this.buttonsGroup, 29, SWT.BOTTOM);
			this.goFormData.left = new FormAttachment(this.stepsLabel, 5, SWT.RIGHT);
			this.goFormData.right = new FormAttachment(this.stepsLabel, 55, SWT.RIGHT);

			this.goButton = new Button(this.buttonsGroup, SWT.NONE);
			this.goButton.setText("Go!");
			this.goButton.setSelection(false);
			this.goButton.setLayoutData(this.goFormData);

			this.gotoFormData = new FormData();
			this.gotoFormData.top = new FormAttachment(this.buttonsGroup, 4, SWT.BOTTOM);
			this.gotoFormData.bottom = new FormAttachment(this.buttonsGroup, 29, SWT.BOTTOM);
			this.gotoFormData.left = new FormAttachment(this.goButton, 5, SWT.RIGHT);
			this.gotoFormData.right = new FormAttachment(this.goButton, 55, SWT.RIGHT);

			this.gotoButton = new Button(this.buttonsGroup, SWT.NONE);
			this.gotoButton.setText("Go to...");
			this.gotoButton.setSelection(false);
			this.gotoButton.setLayoutData(this.gotoFormData);

			this.executeOnlyFormData = new FormData();
			this.executeOnlyFormData.top = new FormAttachment(this.buttonsGroup, 33, SWT.BOTTOM);
			this.executeOnlyFormData.bottom = new FormAttachment(this.buttonsGroup, 56, SWT.BOTTOM);
			this.executeOnlyFormData.left = new FormAttachment(this.buttonsSash1, 8, SWT.RIGHT);
			this.executeOnlyFormData.right = new FormAttachment(this.buttonsSash1, 118, SWT.RIGHT);

			this.executeOnlyButton = new Button(this.buttonsGroup, SWT.CHECK);
			this.executeOnlyButton.setText("Only one step per");
			this.executeOnlyButton.setSelection(false);
			this.executeOnlyButton.setLayoutData(this.executeOnlyFormData);

			this.secondsComboFormData = new FormData();
			this.secondsComboFormData.top = new FormAttachment(this.buttonsGroup, 33, SWT.BOTTOM);
			this.secondsComboFormData.bottom = new FormAttachment(this.buttonsGroup, 57, SWT.BOTTOM);
			this.secondsComboFormData.left = new FormAttachment(this.executeOnlyButton, 0, SWT.RIGHT);
			this.secondsComboFormData.right = new FormAttachment(this.executeOnlyButton, 55, SWT.RIGHT);

			this.secondsCombo = new Combo(this.buttonsGroup, SWT.DROP_DOWN);
			this.secondsCombo.setLayoutData(this.secondsComboFormData);
			this.secondsCombo.add("0.01");
			this.secondsCombo.add("0.025");
			this.secondsCombo.add("0.05");
			this.secondsCombo.add("0.1");
			this.secondsCombo.add("0.2");
			this.secondsCombo.add("0.5");
			this.secondsCombo.add("1");
			this.secondsCombo.add("2");
			this.secondsCombo.add("5");
			this.secondsCombo.add("10");
			this.secondsCombo.add("30");
			this.secondsCombo.add("60");

			this.secondsLabelFormData = new FormData();
			this.secondsLabelFormData.top = new FormAttachment(this.buttonsGroup, 37, SWT.BOTTOM);
			this.secondsLabelFormData.bottom = new FormAttachment(this.buttonsGroup, 57, SWT.BOTTOM);
			this.secondsLabelFormData.left = new FormAttachment(this.secondsCombo, 4, SWT.RIGHT);
			this.secondsLabelFormData.right = new FormAttachment(this.secondsCombo, 24, SWT.RIGHT);

			this.secondsLabel = new Label(this.buttonsGroup, SWT.NONE);
			this.secondsLabel.setText("s.");
			this.secondsLabel.setLayoutData(this.secondsLabelFormData);

			this.stopFormData = new FormData();
			this.stopFormData.top = new FormAttachment(this.buttonsGroup, 32, SWT.BOTTOM);
			this.stopFormData.bottom = new FormAttachment(this.buttonsGroup, 57, SWT.BOTTOM);
			this.stopFormData.left = new FormAttachment(this.stepsLabel, 5, SWT.RIGHT);
			this.stopFormData.right = new FormAttachment(this.stepsLabel, 55, SWT.RIGHT);

			this.stopButton = new Button(this.buttonsGroup, SWT.NONE);
			this.stopButton.setText("Stop");
			this.stopButton.setSelection(false);
			this.stopButton.setLayoutData(this.stopFormData);
			this.stopButton.setEnabled(false);

			this.buttonsSash2FormData = new FormData();
			this.buttonsSash2FormData.top = new FormAttachment(this.buttonsGroup, 6, SWT.BOTTOM);
			this.buttonsSash2FormData.bottom = new FormAttachment(this.buttonsGroup, 55, SWT.BOTTOM);
			this.buttonsSash2FormData.left = new FormAttachment(this.gotoButton, 7, SWT.RIGHT);
			this.buttonsSash2FormData.right = new FormAttachment(this.gotoButton, 12, SWT.RIGHT);

			this.buttonsSash2 = new Sash(this.buttonsGroup, SWT.BORDER);
			this.buttonsSash2.setLayoutData(this.buttonsSash2FormData);

			this.loggingSpacerFormData = new FormData();
			this.loggingSpacerFormData.top = new FormAttachment(this.buttonsGroup, 4, SWT.BOTTOM);
			this.loggingSpacerFormData.bottom = new FormAttachment(this.buttonsGroup, 57, SWT.BOTTOM);
			this.loggingSpacerFormData.left = new FormAttachment(this.buttonsSash2, 7, SWT.RIGHT);
			this.loggingSpacerFormData.right = new FormAttachment(this.buttonsSash2, 495, SWT.RIGHT);

			this.loggingSpacerComposite = new Composite(this.buttonsGroup, SWT.BORDER);
			this.loggingSpacerComposite.setBackground(this.display.getSystemColor(SWT.COLOR_WHITE));
			this.loggingSpacerComposite.setLayout(new FormLayout());
			this.loggingSpacerComposite.setLayoutData(this.loggingSpacerFormData);

			this.loggingFormData = new FormData();
			this.loggingFormData.top = new FormAttachment(this.loggingSpacerComposite, 0, SWT.BOTTOM);
			this.loggingFormData.bottom = new FormAttachment(this.loggingSpacerComposite, 49, SWT.BOTTOM);
			this.loggingFormData.left = new FormAttachment(this.loggingSpacerComposite, 5, SWT.RIGHT);
			this.loggingFormData.right = new FormAttachment(this.loggingSpacerComposite, 484, SWT.RIGHT);

			this.loggingStyledText = new StyledText(this.loggingSpacerComposite, SWT.LEFT | SWT.V_SCROLL);
			this.loggingStyledText.setBackground(this.display.getSystemColor(SWT.COLOR_WHITE));
			this.loggingStyledText.setText("");
			this.loggingStyledText.setLayoutData(this.loggingFormData);

			this.buttonsSash3FormData = new FormData();
			this.buttonsSash3FormData.top = new FormAttachment(this.buttonsGroup, 6, SWT.BOTTOM);
			this.buttonsSash3FormData.bottom = new FormAttachment(this.buttonsGroup, 55, SWT.BOTTOM);
			this.buttonsSash3FormData.left = new FormAttachment(this.loggingSpacerComposite, 7, SWT.RIGHT);
			this.buttonsSash3FormData.right = new FormAttachment(this.loggingSpacerComposite, 12, SWT.RIGHT);

			this.buttonsSash3 = new Sash(this.buttonsGroup, SWT.BORDER);
			this.buttonsSash3.setLayoutData(this.buttonsSash3FormData);

			this.optionsFormData = new FormData();
			this.optionsFormData.top = new FormAttachment(this.buttonsGroup, 4, SWT.BOTTOM);
			this.optionsFormData.bottom = new FormAttachment(this.buttonsGroup, 29, SWT.BOTTOM);
			this.optionsFormData.left = new FormAttachment(this.buttonsSash3, 7, SWT.RIGHT);
			this.optionsFormData.right = new FormAttachment(this.buttonsSash3, 67, SWT.RIGHT);

			this.optionsButton = new Button(this.buttonsGroup, SWT.NONE);
			this.optionsButton.setText("Options");
			this.optionsButton.setSelection(false);
			this.optionsButton.setLayoutData(this.optionsFormData);

			this.exitFormData = new FormData();
			this.exitFormData.top = new FormAttachment(this.buttonsGroup, 32, SWT.BOTTOM);
			this.exitFormData.bottom = new FormAttachment(this.buttonsGroup, 57, SWT.BOTTOM);
			this.exitFormData.left = new FormAttachment(this.buttonsSash3, 7, SWT.RIGHT);
			this.exitFormData.right = new FormAttachment(this.buttonsSash3, 67, SWT.RIGHT);

			this.exitButton = new Button(this.buttonsGroup, SWT.NONE);
			this.exitButton.setText("Exit");
			this.exitButton.setSelection(false);
			this.exitButton.setLayoutData(this.exitFormData);

			// Set up button listener.
			/*
			 * Execute the next step.
			 */
			this.nextStepButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					changeCursor(SWT.CURSOR_APPSTARTING);
					// getExecutionRunner().executeNextStep(); // Old possibility: Do it directly. This method is private now!

					getExecutionRunner().setExecuteSteps(1);
					getExecutionRunner().setExecuteInfiniteSteps(false);
					getExecutionRunner().setExecuteEvery(-1);
					startExecutionRunner(); // New way: Execute the runner with one step to go. Only by doing so, the skipping mode is possible.

					changeCursor(SWT.CURSOR_ARROW);
					checkIfErrorOccurred();
				}
			});

			/*
			 * Restart the execution. This will completely set up a new
			 * Application, clearing any caches (but the ClassLoaders class
			 * cache).
			 */
			this.restartButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					// First abort the current execution, releasing all opened threads.
					abortExecution(true);
					// Setup a new StepByStepExecutionRunner.
					setupExecutionRunner();
					// Generate a new Application; in case of a error abort the execution and disable the execution buttons.
					if (!getExecutionRunner().setupApplication()) {
						abortExecution(false);
						setExecutionButtonsEnabled(false);
						getParentWindow().doExit();
						return;
					}
					// Refresh all the states and enable the buttons for the execution.
					setupInstructionListAndRefreshStates(false);
				}
			});

			/*
			 * Start the execution.
			 */
			this.goButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					changeCursor(SWT.CURSOR_APPSTARTING);
					startExecution();
					changeCursor(SWT.CURSOR_ARROW);
				}
			});

			/*
			 * Go to the specified instruction.
			 */
			this.gotoButton.addListener(SWT.Selection, new Listener() {
				@SuppressWarnings("unused")
				public void handleEvent(Event event) {
					StepByStepExecutionComposite.this.shell.setEnabled(false);
					Options options = Options.getInst();

					/*
					 * Using the fast execution may lead to a jump into code that would be skipped if
					 * not showing any invocation and the invocation of static initializers. This
					 * might have unexpected consequences. Hence, ask the user to disable any skipping
					 * and do not continue if he disagrees to do so.
					 */
					if (options.visuallySkipStaticInit || options.stepByStepVisuallySkipInvoc > 0) {
						int answer = StaticGuiSupport.showMessageBox(
								StepByStepExecutionComposite.this.shell,
								"Qestion",
								"Using the fast execution may lead to a jump into code that would be skipped if "
								+ "not showing any invocation and the invocation of static initializers. Hence, "
								+ "disabling any skipping is required to use this function.\n\n"
								+ "Do you really want to disable any visual skipping?\n\n"
								+ "(It can be enabled again in the options at any time).",
								SWT.YES | SWT.NO | SWT.ICON_QUESTION);
						// Just return if the answer was "no".
						if (answer == SWT.NO) {
							StepByStepExecutionComposite.this.shell.setEnabled(true);
							StepByStepExecutionComposite.this.shell.setActive();
							return;
						}

						// Disable any skipping.
						options.visuallySkipStaticInit = false;
						options.stepByStepVisuallySkipInvoc = 0;
					}

					// Try to get the number of instructions to execute.
					Long numberOfInstructions = 100L;
					InputWindow inputWindow = new InputWindow(StepByStepExecutionComposite.this.shell, StepByStepExecutionComposite.this.shell.getDisplay());
					inputWindow.setInformationText("The number of instructions to execute:");
					Object returnedValue = inputWindow.show(numberOfInstructions, "long");
					// Got a return value?
					if (!(returnedValue instanceof UndefinedValue)) {
						if (options.symbolicMode) {
							numberOfInstructions = ((LongConstant) returnedValue).getLongValue();
						} else {
							numberOfInstructions = (Long) returnedValue;
						}
						// Got a suitable return value?
						if (numberOfInstructions < 1) {
							StepByStepExecutionComposite.this.shell.setEnabled(true);
							StepByStepExecutionComposite.this.shell.setActive();
							StaticGuiSupport.showMessageBox(
											StepByStepExecutionComposite.this.shell,
											"Warning",
											"The number of instructions to execute has to be greater than zero.",
											SWT.OK | SWT.ICON_WARNING);
						} else {
							// Show the fast execution window.
							try {
								FastExecutionWindow window = new FastExecutionWindow(StepByStepExecutionComposite.this.shell,
										StepByStepExecutionComposite.this.application, numberOfInstructions);
							} catch (Throwable t) {
								StaticGuiSupport.processGuiError(t, "Fast execution", StepByStepExecutionComposite.this.shell);
							}

							// Check if the right method is shown.
							if (StepByStepExecutionComposite.this.executionRunner.checkForMethodRefresh()) {
								final Method method = StepByStepExecutionComposite.this.executionRunner
										.getApplication().getVirtualMachine().getCurrentFrame()
										.getMethod();

								// Execute the following in a new Thread so the GUI will not block.
								Thread runner = new Thread() {
									@Override
									public void run() {
										loadInstructionsByExecutionRunner(method);
										/*
										 * For safety reasons, we will now sleep until the loading
										 * of the instructions is complete.
										 */
										if (!getInstructionLoadingComplete()) {
											while (!getInstructionLoadingComplete()) {
												try {
													sleep(Globals.SAFETY_SLEEP_DELAY);
												} catch (InterruptedException e) {
													// Do nothing, this is unproblematical.
												}
											}
										}

										// Refresh the machine state.
										refreshMachineStateByExecutionRunner();

										// Refresh the Symbolic execution state.
										refreshSymbolicExecutionStateByExecutionRunner();

										// Set the pc.
										setPCByExecutionRunner();

										// Check if the execution has been finished.
										StepByStepExecutionComposite.this.executionRunner.hasExecutionFinished();
									}
								};
								runner.start();
							} else {
								// Refresh the machine state.
								refreshMachineState();

								// Refresh the Symbolic execution state.
								refreshSymbolicExecutionState();

								// Set the pc.
								setPC();

								// Check if the execution has been finished.
								StepByStepExecutionComposite.this.executionRunner.hasExecutionFinished();
							}
						}
					}
					StepByStepExecutionComposite.this.shell.setEnabled(true);
					StepByStepExecutionComposite.this.shell.setActive();
				}
			});

			/*
			 * Stop the execution.
			 */
			this.stopButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					stopExecution();
				}
			});

			/*
			 * Open the Options window. Disable this Shell for the meantime.
			 */
			this.optionsButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					StepByStepExecutionComposite.this.shell.setEnabled(false);
					try {
						OptionsWindow optionsWindow = new OptionsWindow();
						optionsWindow.show(StepByStepExecutionComposite.this.shell, StepByStepExecutionComposite.this.classLoader);
					} catch (Throwable t) {
						StaticGuiSupport.processGuiError(t, "Execution options", StepByStepExecutionComposite.this.shell);
					}
					StepByStepExecutionComposite.this.shell.setEnabled(true);
					StepByStepExecutionComposite.this.shell.setActive();
				}
			});

			/*
			 * Exit the step by step execution window.
			 */
			this.exitButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					getParentWindow().doExit();
				}
			});

			// Esc Listener
			this.addKeyListener(new EscKeyListener(this.parent));
			this.machineStateGroup.addKeyListener(new EscKeyListener(this.parent));
			this.localVariablesLabel.addKeyListener(new EscKeyListener(this.parent));
			this.localVariablesList.addKeyListener(new EscKeyListener(this.parent));
			this.operandStackLabel.addKeyListener(new EscKeyListener(this.parent));
			this.operandStackList.addKeyListener(new EscKeyListener(this.parent));
			this.operandStackEditButton.addKeyListener(new EscKeyListener(this.parent));
			this.operandStackPushButton.addKeyListener(new EscKeyListener(this.parent));
			this.operandStackPopButton.addKeyListener(new EscKeyListener(this.parent));
			this.operandStackEmptyButton.addKeyListener(new EscKeyListener(this.parent));
			this.virtualMachineStackLabel.addKeyListener(new EscKeyListener(this.parent));
			this.virtualMachineStackList.addKeyListener(new EscKeyListener(this.parent));
			this.symbolicExecutionStateGroup.addKeyListener(new EscKeyListener(this.parent));
			this.choicePointsLabel.addKeyListener(new EscKeyListener(this.parent));
			this.choicePointsList.addKeyListener(new EscKeyListener(this.parent));
			this.constraintsLabel.addKeyListener(new EscKeyListener(this.parent));
			this.constraintsList.addKeyListener(new EscKeyListener(this.parent));
			this.coverageLabel.addKeyListener(new EscKeyListener(this.parent));
			this.coverageList.addKeyListener(new EscKeyListener(this.parent));
			this.solutionsGroup.addKeyListener(new EscKeyListener(this.parent));
			this.solutionsList.addKeyListener(new EscKeyListener(this.parent));
			this.nextStepButton.addKeyListener(new EscKeyListener(this.parent));
			this.restartButton.addKeyListener(new EscKeyListener(this.parent));
			this.buttonsSash1.addKeyListener(new EscKeyListener(this.parent));
			this.executeNextLabel.addKeyListener(new EscKeyListener(this.parent));
			this.executeNextCombo.addKeyListener(new EscKeyListener(this.parent));
			this.stepsLabel.addKeyListener(new EscKeyListener(this.parent));
			this.goButton.addKeyListener(new EscKeyListener(this.parent));
			this.gotoButton.addKeyListener(new EscKeyListener(this.parent));
			this.executeOnlyButton.addKeyListener(new EscKeyListener(this.parent));
			this.secondsCombo.addKeyListener(new EscKeyListener(this.parent));
			this.secondsLabel.addKeyListener(new EscKeyListener(this.parent));
			this.stopButton.addKeyListener(new EscKeyListener(this.parent));
			this.buttonsSash2.addKeyListener(new EscKeyListener(this.parent));
			this.loggingSpacerComposite.addKeyListener(new EscKeyListener(this.parent));
			this.loggingStyledText.addKeyListener(new EscKeyListener(this.parent));
			this.buttonsSash3.addKeyListener(new EscKeyListener(this.parent));
			this.optionsButton.addKeyListener(new EscKeyListener(this.parent));
			this.exitButton.addKeyListener(new EscKeyListener(this.parent));

			// Finish setting up the composite.
			this.pack();

			// Invoke other methods for the initialization of the widgets.
			setSymbolicalElementsEnabled(Options.getInst().symbolicMode);

			// Start logging.
			startLogging();

			// Set up the instruction list.
			setupInstructionListAndRefreshStates(false);
		} catch (GUIException e) {
			// Make sure the execution is aborted, so the Thread will be released!
			abortExecution(false);
			throw e;
		}
	}

	/**
	 * Set up the instruction list and refresh fields showing the state of the executed application.
	 * This is done in an own thread so the window will not freeze.
	 *
	 * @param simplyReloadInstructions If set to true, just the instructions will be reloaded. It should not be used when calling the method for the first time or in order to restart the execution.
	 */
	protected void setupInstructionListAndRefreshStates(boolean simplyReloadInstructions) {
		setupInstructionListAndRefreshStates(simplyReloadInstructions, this.method);
	}

	/**
	 * Set up the instruction list and refresh fields showing the state of the executed application.
	 * This is done in an own thread so the window will not freeze. This overloaded method offers the
	 * possibility to specified the method that instructions are loaded for. It will be checked
	 * against the method retrieved from the current execution. If the current execution is busy
	 * with non-shown executions like execution static initializers, loading might be delayed.
	 *
	 * @param simplyReloadInstructions If set to true, just the instructions will be reloaded. It should not be used when calling the method for the first time or in order to restart the execution.
	 * @param method The method to load instructions for.
	 */
	protected void setupInstructionListAndRefreshStates(boolean simplyReloadInstructions, Method method) {
		// Change the cursor.
		this.shell.setCursor(new Cursor(this.display, SWT.CURSOR_WAIT));
		// Display a "loading" message.
		this.methodNameLabel.setText("Loading...");
		// Load
		InstructionLoader loader = new InstructionLoader(simplyReloadInstructions, method);
		loader.start();
	}

	/**
	 * The InstructionLoader is a supporting class that executes the instruction loading in
	 * an own thread.
	 */
	private class InstructionLoader extends Thread {
		// Fields.
		protected final boolean simplyReloadInstructions;
		protected final Method currentMethod;

		/**
		 * Initialize the InstructionLoader.
		 * @param simplyReloadInstructions If set to true, just the instructions will be reloaded.
		 * @param method The method to load instructions for.
		 */
		public InstructionLoader(boolean simplyReloadInstructions, Method method) {
			this.simplyReloadInstructions = simplyReloadInstructions;
			this.currentMethod = method;
		}

		/**
		 * Load the instructions and refresh the state fields.
		 */
		@Override
		public void run() {
			try {
				// Asynchronous access.
				StepByStepExecutionComposite.this.display.asyncExec(new Runnable() {
					public void run() {
						// Throttle, as there might be some kind of pre-execution, e.g. of static initializers.
						while (InstructionLoader.this.currentMethod != StepByStepExecutionComposite.this.application.getVirtualMachine().getCurrentFrame().getMethod())
						{
							try {
								Thread.sleep(Globals.SAFETY_SLEEP_DELAY);
							} catch (InterruptedException e) {
								// No need to do a thing.
							}
						}

						// Fill the elements with data.
						loadInstructions();

						// Continue?
						if (InstructionLoader.this.simplyReloadInstructions) return;

						try {
							// Throttle, as the instructions initialization might not be yet completed.
							Thread.sleep(Globals.SAFETY_SLEEP_DELAY);
						} catch (InterruptedException e) {
							// No need to do a thing.
						}
						setPC();
						refreshMachineState();
						refreshSymbolicExecutionState();
						clearSolutions();
						setExecutionButtonsEnabled(true);
					}
				});
			} finally {
				// Change back the cursor (asynchronous access).
				StepByStepExecutionComposite.this.display.asyncExec(new Runnable() {
					public void run() {
						StepByStepExecutionComposite.this.shell.setCursor(new Cursor(StepByStepExecutionComposite.this.display, SWT.CURSOR_ARROW));
					}
				});
			}
		}
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: Show a MessageBox. If
	 * the MessageBox has a return value, provide it.
	 * @param message The text to display in the MessageBox' title.
	 * @param text The main text to display in MessageBox.
	 * @param flags The MessageBox flags regarding its style.
	 * @return The return value of the MessageBox.
	 */
	public int drawMessageBoxForExecutionRunner(final String message,
			final String text, final int flags) {
		/**
		 * Inner Class that draws a MessageBox in an own thread. This is needed
		 * for the synchronous access to it. Not drawing it in an own thread
		 * will lead to serious problems since the SWT thread for the GUI cannot
		 * be accessed by the thread of the ExecutionWindow without wrapping
		 * through this.display.syncExec() or this.display.asyncExec().
		 */
		class MessageBoxWrapper extends Thread {
			// Field to store the response from the MessageBox.
			private int response = -1;

			/**
			 * Draw the MessageBox in an own thread.
			 */
			@Override
			public void run() {
				this.response = StaticGuiSupport.showMessageBox(StepByStepExecutionComposite.this.shell, message, text, flags);
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
		 * Synchronous access, since the MessageBox' results might be needed for
		 * the further execution of the Execution Runner. It will hence wait for
		 * the results.
		 */
		this.display.syncExec(run);
		return run.getResponse();
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: draw an InputWindow to
	 * ask the user for required input regarding an unset local variable.
	 * @param currentFrame The currently executed Frame.
	 * @param localVariableIndex The local variable the user is asked to set.
	 */
	public void drawInputWindowForExecutionRunner(final Frame currentFrame, final int localVariableIndex) {
		/**
		 * Inner Class that draws the InputWindow in an own thread. This is
		 * needed for the synchronous access to it. Not drawing it in an own
		 * thread will lead to serious problems since the SWT thread for the GUI
		 * cannot be accessed by the thread of the ExecutionWindow without
		 * wrapping through this.display.syncExec() or this.display.asyncExec().
		 */
		class InputWindowWrapper extends Thread {
			@Override
			public void run() {
				// Disable this windows' shell.
				StepByStepExecutionComposite.this.shell.setEnabled(false);
				InputWindow inputWindow = new InputWindow(StepByStepExecutionComposite.this.shell, getDisplay());

				// Ask the user for the value and set the local variable accordingly.
				Object localVariable = currentFrame.getMethod().getZeroOrNullParameter(localVariableIndex);
				localVariable = inputWindow.show(localVariable, localVariable.getClass().getName());
				currentFrame.setLocalVariable(localVariableIndex, localVariable);

				// Enable this windows' shell.
				StepByStepExecutionComposite.this.shell.setEnabled(true);
				StepByStepExecutionComposite.this.shell.setActive();
			}

		}
		// Instantiate the InputWindowWrapper.
		InputWindowWrapper run = new InputWindowWrapper();

		/**
		 * Synchronous access, since the InputWindows results are needed for the
		 * further execution of the Execution Runner. It will hence wait for the
		 * results.
		 */
		this.display.syncExec(run);
	}

	/**
	 * Getter for this composite.
	 * @return This StepByStepExecutionComposite.
	 */
	public StepByStepExecutionComposite getThis() {
		return this;
	}

	/**
	 * Getter for the parent window.
	 * @return The parent instance of StepByStepExecutionWindow.
	 */
	protected StepByStepExecutionWindow getParentWindow() {
		return this.parent;
	}

	/**
	 * Set up the StepByStepExecutionRunner with the basic data needed for its
	 * operation.
	 */
	protected void setupExecutionRunner() {
		this.executionRunner = new StepByStepExecutionRunner(this,
				this.classLoader, this.classFile, this.method);
	}

	/**
	 * Start the execution. Therefore, the execution settings are parsed and
	 * checked. If checking is successful, the execution related buttons are
	 * disabled (the stop button is enabled of course) and the
	 * StepByStepExecutionRunner started.
	 */
	protected void startExecution() {
		// Check if the execution can be started with the data in the combo fields.
		int executeSteps = -1;
		boolean executeInfiniteSteps = false;
		double executeEvery = -1;
		try {
			if (this.executeNextCombo.getText().equals("unlimited")) {
				executeInfiniteSteps = true;
			} else {
				executeSteps = Integer.parseInt(this.executeNextCombo.getText().replace(".", ""));
			}
		} catch (NumberFormatException e) {
			executeSteps = -1;
		}
		if (!executeInfiniteSteps && executeSteps <= 0) {
			// Parsing unsuccessful or value to low - tell this to the user and exit.
			StaticGuiSupport.showMessageBox(
							this.shell,
							"Warning",
							"Cannot start execution. The number of steps to execute must be an integer value between 1 and " + Integer.MAX_VALUE + ". Please enter a valid value and try again.",
							SWT.OK | SWT.ICON_WARNING);
			return;
		}
		if (this.executeOnlyButton.getSelection()) {
			try {
				executeEvery = Double.parseDouble(this.secondsCombo.getText());
			} catch (NumberFormatException e) {
				executeEvery = -1;
			}
			if (executeEvery <= 0.0D) {
				// Parsing unsuccessful or value to low - tell this to the user and exit.
				StaticGuiSupport.showMessageBox(
								this.shell,
								"Warning",
								"Cannot start execution. The time between the execution of two steps must be a decimal value between 0 and " + Integer.MAX_VALUE + ". Please enter a valid value and try again.",
								SWT.OK | SWT.ICON_WARNING);
				return;
			} else if (executeEvery < ((double) Globals.MIN_SLEEP_BETWEEN_STEPS) / TimeSupport.MILLIS_SECOND) {
				StaticGuiSupport.showMessageBox(
								this.shell,
								"Warning",
								"Cannot start execution. The time between the execution of two steps must be at least " + (((double) Globals.MIN_SLEEP_BETWEEN_STEPS) / TimeSupport.MILLIS_SECOND) + ". Lower values might lead to errors, since the gui cannot generate the output as fast as it would be needed. Please enter a valid value and try again.\n\nTo speed up execution, consider to enable the visual skipping of method invocations. This will increase performance vastly. See the options for details.",
								SWT.OK | SWT.ICON_WARNING);
				return;
			}
		}

		// Start the execution.
		this.nextStepButton.setEnabled(false);
		this.goButton.setEnabled(false);
		this.gotoButton.setEnabled(false);
		this.executeNextCombo.setEnabled(false);
		this.executeOnlyButton.setEnabled(false);
		this.secondsCombo.setEnabled(false);
		this.stopButton.setEnabled(true);
		this.executionRunner.setExecuteSteps(executeSteps);
		this.executionRunner.setExecuteInfiniteSteps(executeInfiniteSteps);
		this.executionRunner.setExecuteEvery(executeEvery);
		startExecutionRunner();
	}

	/**
	 * Start the StepByStepExecutionRunner. Since the StepByStepExecutionRunner
	 * always forks to a new thread, the threading state of it is checked first.
	 * If it is a not yet started thread, its is started by invoking start(). If
	 * it is in the terminated thread, it has already finished the execution of
	 * its run() method and may not be run again. This should not happen and
	 * means that an error has occurred. Probably it was aborted but due to
	 * threading issues this access it late. In any other case, its
	 * continueExecution() method is invoked, signaling that execution might
	 * now be continued.
	 */
	protected void startExecutionRunner() {
		if (this.executionRunner.getState() == java.lang.Thread.State.NEW) {
			this.executionRunner.start();
		} else if (this.executionRunner.getState() != java.lang.Thread.State.TERMINATED) {
			this.executionRunner.continueExecution();
		} else {
			StaticGuiSupport.showMessageBox(
							this.shell,
							"Error",
							"Could not continue to a fatal threading error. Please restart the execution and try again.",
							SWT.OK | SWT.ICON_ERROR);
			this.setExecutionButtonsEnabled(false);
		}
	}

	/**
	 * Load the instructions.
	 */
	protected void loadInstructions() {
		// Mark the the loading is currently being done.
		this.instructionLoadingComplete = false;

		try {
			// Load the instructions.
			Instruction[] instructions = this.application.getVirtualMachine().getCurrentFrame().getMethod().getInstructions();
			Instruction[] instructionsAndOtherBytes = this.application.getVirtualMachine().getCurrentFrame().getMethod().getInstructionsAndOtherBytes();

			// Reset the StyledText control.
			this.bytecodeStyledText.setStyleRanges(new StyleRange[0]);
			this.bytecodeStyledText.setText("");

			// Build the mapping tables. The pc is unequal to the instructions number, since other bytes are counted with regard to the pc.
			int m = 0;
			this.instructionsAndOtherBytesToInstructionsMappingTable = new int[instructionsAndOtherBytes.length];
			for (int a = 0; a < instructionsAndOtherBytes.length; a++) {
				if (instructionsAndOtherBytes[a] != null) {
					this.instructionsAndOtherBytesToInstructionsMappingTable[a] = m;
					m++;
				}
			}
			m = 0;
			this.instructionsToInstructionsAndOtherBytesMappingTable = new int[instructions.length];
			for (int a = 0; a < instructionsAndOtherBytes.length; a++) {
				if (instructionsAndOtherBytes[a] != null) {
					this.instructionsToInstructionsAndOtherBytesMappingTable[m] = a;
					m++;
				}
			}

			// Determine the magnitude of dimensions.
			int lineNumberLength = 1;
			if (instructions.length > 10)
				lineNumberLength++;
			if (instructions.length > 100)
				lineNumberLength++;
			if (instructions.length > 1000)
				lineNumberLength++;
			if (instructions.length > 10000)
				lineNumberLength++;

			// Set up an array list for the styles and basic variables.
			ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();
			int totalCharacters = 0;
			String text = "";
			this.styleRangeOffsets = new int[instructions.length];

			// Process the instructions.
			int numberOfStyleRanges = 0;
			for (int a = 0; a < instructions.length; a++) {
				// Line break?
				if (totalCharacters > 0) {
					text += "\n";
					totalCharacters++;
				}

				// StyledText for the bytecode line number.
				int number;
				if (Options.getInst().stepByStepShowInstrBytePosition) {
					number = this.instructionsToInstructionsAndOtherBytesMappingTable[a];
				} else {
					number = a;
				}
				String formatedLineNumber = StaticGuiSupport.getFormatedIndexNumber(number, lineNumberLength, true);
				text += formatedLineNumber + " ";
				int length = formatedLineNumber.length();

				StyleRange lineNumberStyleRange = new StyleRange();
				lineNumberStyleRange.start = totalCharacters;
				lineNumberStyleRange.length = length;
				totalCharacters += length + 1;

				// Set the background color by the type of block.
				boolean doNotOverrideTheColor = false;
				if (instructions[a] instanceof JumpInvocation && !doNotOverrideTheColor) {
					lineNumberStyleRange.background = this.COLOR_JUMP_INVOCATION;
					doNotOverrideTheColor = true;
				}
				if (instructions[a] instanceof JumpSwitching && !doNotOverrideTheColor) {
					lineNumberStyleRange.background = this.COLOR_JUMP_SWITCHING;
					doNotOverrideTheColor = true;
				}
				if (instructions[a] instanceof JumpException && !doNotOverrideTheColor) {
					lineNumberStyleRange.background = this.COLOR_JUMP_EXCEPTION;
					// Can be overridden if jumping by a condition or in general is possible.
				}
				if (instructions[a] instanceof JumpConditional && !doNotOverrideTheColor) {
					lineNumberStyleRange.background = this.COLOR_JUMP_CONDITIONAL;
					doNotOverrideTheColor = true;
				}
				if (instructions[a] instanceof JumpAlways && !doNotOverrideTheColor) {
					lineNumberStyleRange.background = this.COLOR_JUMP_ALWAYS;
					doNotOverrideTheColor = true;
				}
				if (instructions[a] instanceof JumpNever && !doNotOverrideTheColor) {
					lineNumberStyleRange.background = this.COLOR_JUMP_NEVER;
					doNotOverrideTheColor = true;
				}
				styleRanges.add(lineNumberStyleRange);
				numberOfStyleRanges++;

				// StyledText for the bytecode.
				String instructionText = instructions[a].getNameWithOtherBytes();
				int instructionTextLength = instructionText.length();
				text += instructionText;
				StyleRange instructionStyleRange = new StyleRange();
				instructionStyleRange.start = totalCharacters;
				if (instructionText.indexOf(" ") != -1) {
					instructionStyleRange.length = instructionText.indexOf(" ");
				} else {
					instructionStyleRange.length = instructionTextLength;
				}
				instructionStyleRange.fontStyle = SWT.BOLD;
				instructionStyleRange.foreground = this.display.getSystemColor(SWT.COLOR_BLACK);
				totalCharacters += instructionTextLength;

				// Add the style range.
				styleRanges.add(instructionStyleRange);
				this.styleRangeOffsets[a] = numberOfStyleRanges++;

				// StyledText for the additional information.
				String additionalInformationText = "";

				// Check if special interfaces are implemented.
				if (instructions[a] instanceof ReplacingInstruction || instructions[a] instanceof QuickInstruction) {
					styleRanges.add(createStyleRange(totalCharacters + 1, 7, SWT.NORMAL, SWT.COLOR_RED));
					numberOfStyleRanges++;
					text += " (quick)";
					totalCharacters += 8;
				}

				// Determine the type of the instruction - probably additional information can be provided
				if (instructions[a] instanceof Invokeinterface || instructions[a] instanceof Invokespecial || instructions[a] instanceof Invokestatic || instructions[a] instanceof Invokevirtual) {
					int index = ((GeneralInstructionWithOtherBytes) instructions[a]).constructValueFromOtherBytes(0, 1);
					additionalInformationText = this.application.getVirtualMachine().getCurrentFrame().getMethod().getClassFile().getConstantPool()[index].getStringValue().replace("/", ".");
					int start = 0;
					if (additionalInformationText.indexOf(" ") != -1) {
						start = additionalInformationText.indexOf(" ");
					}
					length = additionalInformationText.length() - start;
					if (additionalInformationText.indexOf(" ", start + 1) != -1) {
						length = additionalInformationText.indexOf(" ", start + 1) - start;
					}
					styleRanges.add(createStyleRange(totalCharacters + start, length, SWT.BOLD, SWT.COLOR_BLACK));
					numberOfStyleRanges++;
				} else if (instructions[a] instanceof Goto) {
					int index = ((GeneralInstructionWithOtherBytes) instructions[a]).constructValueFromOtherBytes(0, 1);
					index += this.instructionsToInstructionsAndOtherBytesMappingTable[a];
					if (index >= Limitations.MAX_CODE_LENGTH) {
						index -= Limitations.MAX_CODE_LENGTH;
					}
					int pc;
					if (Options.getInst().stepByStepShowInstrBytePosition) {
						pc = index;
					} else {
						pc = this.instructionsAndOtherBytesToInstructionsMappingTable[index];
					}
					additionalInformationText = "=> " + StaticGuiSupport.getFormatedIndexNumber(pc, lineNumberLength, false);
				} else if (instructions[a] instanceof If || instructions[a] instanceof If_icmp) {
					int index = ((GeneralInstructionWithOtherBytes) instructions[a]).constructValueFromOtherBytes(0, 1);
					index += this.instructionsToInstructionsAndOtherBytesMappingTable[a];
					if (index >= Limitations.MAX_CODE_LENGTH) {
						index -= Limitations.MAX_CODE_LENGTH;
					}
					int pc;
					if (Options.getInst().stepByStepShowInstrBytePosition) {
						pc = index;
					} else {
						pc = this.instructionsAndOtherBytesToInstructionsMappingTable[index];
					}
					additionalInformationText = "? => " + StaticGuiSupport.getFormatedIndexNumber(pc, lineNumberLength, false);
				} else if (instructions[a] instanceof PushFromConstantPool) {
					String[] pushedValueInformation = ((PushFromConstantPool) instructions[a]).provideObjectInformation(this.application.getVirtualMachine().getCurrentFrame());
					additionalInformationText = "Push: " + pushedValueInformation[1] + " (" + pushedValueInformation[0] + ")";
					styleRanges.add(createStyleRange(totalCharacters + 6, pushedValueInformation[1].length(), SWT.BOLD, SWT.COLOR_BLACK));
					styleRanges.add(createStyleRange(totalCharacters + 8 + pushedValueInformation[1].length(), additionalInformationText.length() - pushedValueInformation[1].length() - 9, SWT.ITALIC, SWT.COLOR_BLACK));
					numberOfStyleRanges += 2;
				} else if (instructions[a] instanceof BiPush || instructions[a] instanceof SiPush) {
					String pushedValue = String.valueOf(((PushAbstract) instructions[a]).getPushedValue());
					additionalInformationText = "Push: " + pushedValue + " (java.lang.Integer)";
					styleRanges.add(createStyleRange(totalCharacters + 6, pushedValue.length(), SWT.BOLD, SWT.COLOR_BLACK));
					styleRanges.add(createStyleRange(totalCharacters + 8 + pushedValue.length(), additionalInformationText .length() - pushedValue.length() - 9, SWT.ITALIC, SWT.COLOR_BLACK));
					numberOfStyleRanges += 2;
				}

				// If there is additional information, tab it so it appears with the same indentation.
				if (additionalInformationText.length() > 0) {
					int numberOfTabs = 1;

					if (instructionTextLength > 25) {
						numberOfTabs = 1;
					} else {
						if (instructionTextLength > 17) {
							numberOfTabs = 2;
						} else {
							if (instructionTextLength > 9) {
								numberOfTabs = 3;
							} else {
								numberOfTabs = 4;
							}
						}
					}

					for (int b = 0; b < numberOfTabs; b++) {
						text += "\t";
					}
					totalCharacters += numberOfTabs;

					// Add the additional information.
					text += additionalInformationText;
					totalCharacters += additionalInformationText.length();
				}
			}

			// Set the text and the style ranges.
			this.bytecodeStyledText.setTabs(8);
			this.bytecodeStyledText.setText(text);
			StyleRange[] styleRangesArray = new StyleRange[styleRanges.size()];
			Iterator<StyleRange> iterator = styleRanges.iterator();
			int a = 0;
			while (iterator.hasNext()) {
				styleRangesArray[a] = iterator.next();
				a++;
			}
			this.bytecodeStyledText.setStyleRanges(styleRangesArray);

			// Set up the ScrollBar
			ScrollBar verticalScrollBar = this.bytecodeStyledText.getVerticalBar();
			int length = verticalScrollBar.getMaximum() - verticalScrollBar.getMinimum();
			int lengthPerInstruction = length;
			if (instructions.length > 1) {
				lengthPerInstruction = length / (instructions.length - 1);
			}
			verticalScrollBar.setIncrement(lengthPerInstruction);

			// Finally, redraw the control.
			this.bytecodeStyledText.redraw();
		} catch (IndexOutOfBoundsException e) {
			// Something failed, inform the user.
			StaticGuiSupport.showMessageBox(this.shell, "Error", "A fatal error occured loading the instructions of the current frame. Execution cannot be continued. However, please try again. This might be a temporary problem.\n\nThe root cause is:\n" + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			// Make sure execution is aborted, so the threads are resources are released.
			this.executionRunner.abortExecution();
			// Disable the buttons so the user cannot continue the execution.
			setExecutionButtonsEnabled(false);
		} catch (InvalidInstructionInitialisationException e) {
			// Something failed, inform the user.
			StaticGuiSupport.showMessageBox(this.shell, "Error", "A fatal error occured loading the instructions of the current frame. Execution cannot be continued.\n\nThe root cause is:\n" + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			// Make sure execution is aborted, so the threads are resources are released.
			this.executionRunner.abortExecution();
			// Disable the buttons so the user cannot continue the execution.
			setExecutionButtonsEnabled(false);
		} finally {
			// Mark that the loading is finished (even if something went wrong). Not doing so might block the StepByStepExecutionRunner.
			this.instructionLoadingComplete = true;
		}
	}

	/**
	 * Create a StyleRange with the given parameters.
	 *
	 * @param start The character the range will start at.
	 * @param length The length of the range.
	 * @param style The desired style (must be a valid SWT style).
	 * @param color The desired color (must be a valid SWT color).
	 * @return The StyleRange.
	 */
	private StyleRange createStyleRange(int start, int length, int style, int color) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;
		styleRange.fontStyle = style;
		styleRange.foreground = this.display.getSystemColor(color);
		return styleRange;
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: Load the instructions
	 * and format them.
	 *
	 * @param method The method to load instructions for.
	 */
	public synchronized void loadInstructionsByExecutionRunner(final Method method) {
		this.instructionLoadingComplete = false;
		// Asynchronous access.
		this.display.syncExec(new Runnable() {
			public void run() {
				setupInstructionListAndRefreshStates(true, method);
			}
		});
	}

	/**
	 * Set the name of the method of the currently executed frame.
	 */
	private void setMethodName() {
		this.methodNameLabel.setText(this.application.getVirtualMachine().getCurrentFrame().getMethod().getClassFile().getName() + ": " + this.application.getVirtualMachine().getCurrentFrame().getMethod().getFullName());
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: Refresh the machine
	 * state.
	 */
	public synchronized void refreshMachineStateByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				refreshMachineState();
			}
		});
	}

	/**
	 * Refresh the machine state. This means, set the method name, the local
	 * variables and the stacks of the virtual machine as well as of the
	 * currently executed Frame.
	 */
	protected void refreshMachineState() {
		setMethodName();
		setLocalVariables();
		setOperandStack();
		setVirtualMachineStack();
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: refresh the symbolic
	 * execution state.
	 */
	public synchronized void refreshSymbolicExecutionStateByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				refreshSymbolicExecutionState();
			}
		});
	}

	/**
	 * Refresh the symbolic execution state. This means, set the choice points,
	 * the constraints and the coverage.
	 */
	protected void refreshSymbolicExecutionState() {
		if (Options.getInst().symbolicMode) {
			setChoicePointsAndConstraints();
			setCoverage();
		}
	}

	/**
	 * Clear the solutions List.
	 */
	protected void clearSolutions() {
		this.solutionsList.removeAll();
	}

	/**
	 * Display the currently set local variables.
	 */
	private void setLocalVariables() {
		// Empty the list.
		this.localVariablesList.removeAll();
		// Get and process the local variables.
		Frame currentFrame = this.application.getVirtualMachine().getCurrentFrame();
		Object[] localVartiables = currentFrame.getLocalVariables();
		for (int a = 0; a < localVartiables.length; a++) {
			// Only show initialized values.
			if (!(localVartiables[a] instanceof UndefinedValue)) {
				String parameterName = "";
				if (currentFrame.getMethod().parameterNamesAvailable()) {
					parameterName = " (" + currentFrame.getMethod().getParameterName(a) + ")";
				}
				this.localVariablesList.add("#"
						+ StaticGuiSupport.getFormatedIndexNumber(a, 3, false) + parameterName
						+ ": " + formatVMObject(localVartiables[a]));
			}
		}
	}

	/**
	 * Display the elements of the current frames' operand stack.
	 */
	private void setOperandStack() {
		try {
			// Empty the list.
			this.operandStackList.removeAll();
			// Get and process the elements.
			Stack<Object> operandStack = this.application.getVirtualMachine().getCurrentFrame().getOperandStack();
			Iterator<Object> iterator = operandStack.iterator();
			Object[] reversedOrder = new Object[operandStack.size()];
			int a = operandStack.size() - 1;
			while (iterator.hasNext()) {
				reversedOrder[a] = iterator.next();
				a--;
			}
			// Display them in reversed order, so the topmost element of the stack is actually the tompost element in the List.
			for (a = 0; a < reversedOrder.length; a++) {
				this.operandStackList.add(formatVMObject(reversedOrder[a]));
			}
		} catch (ConcurrentModificationException e) {
			// This exception might occur when the automated execution of instruction is too fast. Just silently ignore it.
		}
	}

	/**
	 * Display the elements of the virtual machine stack.
	 */
	private void setVirtualMachineStack() {
		// Empty the list.
		this.virtualMachineStackList.removeAll();
		// Get and process the elements.
		Stack<Object> virtualMachineStack = this.application.getVirtualMachine().getStack();
		Iterator<Object> iterator = virtualMachineStack.iterator();
		Object[] reversedOrder = new Object[virtualMachineStack.size()];
		int a = virtualMachineStack.size() - 1;
		while (iterator.hasNext()) {
			reversedOrder[a] = iterator.next();
			a--;
		}
		// Display them in reversed order, so the topmost element of the stack is actually the topmost element in the List.
		for (a = 0; a < reversedOrder.length; a++) {
			this.virtualMachineStackList.add(formatVMObject(reversedOrder[a]));
		}
	}

	/**
	 * Format elements of the virtual machine stack, the operand stack and the
	 * local variables for better display, trying to provide a maximum of
	 * relevant information for the user. Some elements to not need to be
	 * formated, as they provide suitable information by themselves.
	 *
	 * @param object The object to format.
	 * @return A String representation of the objects' most relevant information.
	 */
	protected String formatVMObject(Object object) {
		// Is it null?
		if (object == null) return "null";

		// Frame: display the class' name, the methods name and the current pc.
		if (object instanceof Frame) {
			return "Frame: " + ((Frame) object).getMethod().getClassFile().getName() + "." + ((Frame) object).getMethod().getName() + "() @ pc " + ((Frame) object).getPc();
		}

		// Symbolic mode?
		if (Options.getInst().symbolicMode && object instanceof Term) {
			return ((Term) object).toString(false);
		}

		/** TODO this has changed!
		 * Is the object an array? Since this application has its only array
		 * representation and only uses arrays of java.lang wrapper classes to
		 * represent arrays of primitive types, the object should be analysed
		 * regarding to this and the primitive type and the number of dimensions
		 * returned.
		 */
		if (object.getClass().isArray()) {
			// Get the name.
			String name = object.getClass().getName();
			// Count the dimensions.
			int dimensions = 0;
			boolean isArray = false;
			while (name.startsWith("[")) {
				dimensions++;
				name = name.substring(1);
				isArray = true;
			}
			if (isArray) name = name.substring(1, name.length() - 1);

			if (name.contains("Boolean")) {
				name = "boolean";
			} else if (name.contains("Byte")) {
				name = "byte";
			} else if (name.contains("Character")) {
				name = "char";
			} else if (name.contains("Double")) {
				name = "double";
			} else if (name.contains("Float")) {
				name = "float";
			} else if (name.contains("Integer")) {
				name = "int";
			} else if (name.contains("Long")) {
				name = "long";
			}
			return "Array of " + name + " with " + dimensions + " dimensions.";
		}

		/**
		 * The java.lang wrapper classes for the primitive types are used in
		 * this application to represent primitive values. Format them according
		 * to this.
		 */
		if (object instanceof Boolean) {
			return "boolean: " + object.toString();
		}
		if (object instanceof Byte) {
			return "byte: " + object.toString();
		}
		if (object instanceof Character) {
			return "char: " + object.toString();
		}
		if (object instanceof Double) {
			return "double: " + object.toString();
		}
		if (object instanceof Float) {
			return "float: " + object.toString();
		}
		if (object instanceof Integer) {
			return "int: " + object.toString();
		}
		if (object instanceof Long) {
			return "long: " + object.toString();
		}

		// Get the information via the toString() method and truncate the string if it is too long.
		String toString = object.toString();
		if (toString.length() > 100) toString = toString.substring(0, 95) + "[...]";
		toString = "Class: " + object.getClass().getName() + ": " + toString;
		return toString;
	}

	/**
	 * Set the choice points and constraints.
	 */
	private void setChoicePointsAndConstraints() {
		// Empty the Lists.
		this.choicePointsList.removeAll();
		this.constraintsList.removeAll();

		// Get the last choice point.
		ChoicePoint choicePoint = ((SymbolicVirtualMachine) this.application.getVirtualMachine()).getSearchAlgorithm().getCurrentChoicePoint();
		// Process from bottom to top.
		int count = 1;
		while (choicePoint != null) {
			// Construct the entry for the list of choice points.
			String entry = count + ": " + choicePoint.getFrame().getMethod().getClassFile() .getName() + "."
					+ choicePoint.getFrame().getMethod().getName() + "() at pc " + choicePoint.getPc();
			try {
				entry += " (" + choicePoint.getFrame().getMethod().getInstructionsAndOtherBytes()[choicePoint.getPc()].getName() + ")";
			} catch (InvalidInstructionInitialisationException e) {
				// In this case, just ignore it. It is really unlikely to happen.
			}
			this.choicePointsList.add(entry, 0);

			// Add the entry to the list of constraints.
			if (choicePoint.changesTheConstraintSystem()) {
				this.constraintsList.add(count + ": " + choicePoint.getConstraintExpression().toString(), 0);
			}
			this.choicePointsList.add(count + ": " + choicePoint.getChoicePointType(), 0);

			// Move to the parent ChoicePoint.
			choicePoint = choicePoint.getParent();

			// Count up.
			count++;
		}
	}

	/**
	 * Set the coverage.
	 */
	private void setCoverage() {
		this.coverageList.removeAll();
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: set the pc.
	 *
	 */
	public synchronized void setPCByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				setPC();
			}
		});
	}

	/**
	 * Set the pc by painting the instruction with a blue background.
	 */
	protected void setPC() {
		// Only continue if initialization of the StyledText is complete.
		if (this.styleRangeOffsets == null) return;

		// Load the number of instructions.
		int numberOfInstructionsAndOtherBytes = this.instructionsAndOtherBytesToInstructionsMappingTable.length;
		int numberOfInstructions = this.instructionsToInstructionsAndOtherBytesMappingTable.length;

		// Paint the currently highlighted instruction white.
		if (this.currentlyHighlightedInstruction < this.styleRangeOffsets.length) {
			int styleRangeNo = this.styleRangeOffsets[this.currentlyHighlightedInstruction];
			StyleRange highlightedStyleRange = this.bytecodeStyledText.getStyleRanges()[styleRangeNo];
			highlightedStyleRange.background = this.display.getSystemColor(SWT.COLOR_WHITE);
			this.bytecodeStyledText.setStyleRange(highlightedStyleRange);
		}

		// Paint the current instruction blue.
		int position = numberOfInstructionsAndOtherBytes - 1;

		if (this.application.getVirtualMachine().getPc() < numberOfInstructionsAndOtherBytes)
			position = this.instructionsAndOtherBytesToInstructionsMappingTable[this.application.getVirtualMachine().getPc()];
		if (position < numberOfInstructions) {
			this.currentlyHighlightedInstruction = position;

			int styleRangeNo = this.styleRangeOffsets[position];
			StyleRange highlightedStyleRange = this.bytecodeStyledText.getStyleRanges()[styleRangeNo];
			highlightedStyleRange.background = new Color(this.display, 160, 160, 255);
			this.bytecodeStyledText.setStyleRange(highlightedStyleRange);

			// Scroll accordingly.
			final int instructionsVisible = 47;
			ScrollBar verticalScrollBar = this.bytecodeStyledText.getVerticalBar();
			int totalContentHeight = verticalScrollBar.getMaximum() - verticalScrollBar.getMinimum();
			int heightPerInstruction = totalContentHeight;
			if (numberOfInstructions > 1) {
				heightPerInstruction = totalContentHeight / (numberOfInstructions - 1);
			}
			int totalClippingHeight = instructionsVisible * heightPerInstruction;
			int currentPosition = verticalScrollBar.getSelection();
			int currentlyHighlighted = position * heightPerInstruction;
			int newPosition = -1;


			// Scrolled to the bottom?
			if (currentPosition + totalClippingHeight - 4 * heightPerInstruction < currentlyHighlighted)
				newPosition = currentlyHighlighted - totalClippingHeight + 4 * heightPerInstruction;
			// Scrolled to the top?
			else if (currentPosition > currentlyHighlighted - (heightPerInstruction * 2))
				newPosition = currentlyHighlighted - (totalClippingHeight / 2);

			// Only proceed if a new position has to be set.
			if (newPosition != -1) {
				// Make sure the clipping begins at the top of an instruction.
				int remainder = newPosition % heightPerInstruction;
				newPosition -= remainder;

				// Check that the bounds are not left.
				if (newPosition < verticalScrollBar.getMinimum()) newPosition = verticalScrollBar.getMinimum();
				else if (newPosition > verticalScrollBar.getMaximum()) newPosition = verticalScrollBar.getMaximum();

				// Calculate how far scrolling is needed. Initialization...
				int smallStep = verticalScrollBar.getIncrement();
				int bigStep = verticalScrollBar.getPageIncrement();
				int difference = newPosition - currentPosition;
				boolean moveDown = true;
				int smallSteps = 0;
				int bigSteps = 0;
				if (difference < 0) {
					difference *= -1;
					moveDown = false;
				}
				// Calculate the number of steps needed.
				while (difference >= bigStep) {
					bigSteps++;
					difference -= bigStep;
				}
				while (difference >= smallStep) {
					smallSteps++;
					difference -= smallStep;
				}

				// Set the scroll bar's position. Important: This has to be donw before sending the scrolling events, or scrolling up will not trigger a paint event.
				verticalScrollBar.setSelection(newPosition);

				// Send the events.
				while (bigSteps > 0) {
					Event event = new Event();
					event.widget = verticalScrollBar;
					if (moveDown) {
						event.detail = SWT.PAGE_DOWN;
					} else {
						event.detail = SWT.PAGE_UP;
					}
					verticalScrollBar.notifyListeners(SWT.Selection, event);
					bigSteps--;
				}
				while (smallSteps > 0) {
					Event event = new Event();
					event.widget = verticalScrollBar;
					if (moveDown) {
						event.detail = SWT.ARROW_DOWN;
					} else {
						event.detail = SWT.ARROW_UP;
					}
					verticalScrollBar.notifyListeners(SWT.Selection, event);
					smallSteps--;
				}
			}
		}
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: check if an error has
	 * occurred.
	 */
	public synchronized void checkIfErrorOccurredByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				checkIfErrorOccurred();
			}
		});
	}

	/**
	 * If an error has occurred while executing, further execution is disabled
	 * and the cause of the error will be shown to the user in a message box.
	 * Setting the field readyForNextMessage to false ensures that there is not
	 * another error risen until the message box for the current error has be
	 * drawn and was disposed by the user.
	 */
	protected void checkIfErrorOccurred() {
		// Has an error occurred?
		if (this.executionRunner != null && this.readyForNextMessage) {
			this.readyForNextMessage = false;
			if (this.application.getVirtualMachine().errorOccured()) {
				// Process the error.
				this.executionRunner.stopExecution();
				StaticGuiSupport.showMessageBox(this.shell, "Error", "An error occured:\n\n" + this.application.getVirtualMachine().getErrorMessage() + "\n\nExecution cannot be continued.", SWT.OK | SWT.ICON_ERROR);
				setExecutionButtonsEnabled(false);

				// Try to refresh the states for a last time.
				refreshMachineState();
				refreshSymbolicExecutionState();
				setPC();

			}
			this.readyForNextMessage = true;
		}
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: enable or disable the
	 * buttons and corresponding elements for the actual execution.
	 * @param value enables the elements, while false disables them.
	 */
	public synchronized void setExecutionButtonsEnabledByExecutionRunner(
			final boolean value) {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				setExecutionButtonsEnabled(value);
			}
		});
	}

	/**
	 * Enabled or disable any buttons and corresponding elements for the actual
	 * execution.
	 * @param value true enables the elements, while false disables them.
	 */
	protected void setExecutionButtonsEnabled(boolean value) {
		this.nextStepButton.setEnabled(value);
		this.goButton.setEnabled(value);
		this.gotoButton.setEnabled(value);
		this.executeNextLabel.setEnabled(value);
		this.executeNextCombo.setEnabled(value);
		this.stepsLabel.setEnabled(value);
		this.executeOnlyButton.setEnabled(value);
		this.secondsCombo.setEnabled(value);
		this.secondsLabel.setEnabled(value);
		this.buttonsSash2.setEnabled(value);

		// Whenever this method is invoked, the stop button has to be disabled.
		this.stopButton.setEnabled(false);
	}

	/**
	 * Enabled or disable any elements that are relevant for the symbolic
	 * execution only.
	 * @param value true enables the elements, while false disables them.
	 */
	private void setSymbolicalElementsEnabled(boolean value) {
		this.symbolicExecutionStateGroup.setEnabled(value);
		this.choicePointsLabel.setEnabled(value);
		this.choicePointsList.setEnabled(value);
		this.constraintsLabel.setEnabled(value);
		this.constraintsList.setEnabled(value);
		this.coverageLabel.setEnabled(value);
		this.coverageList.setEnabled(value);
	}

	/**
	 * Edit a value of the operand stack of the currently executed frame in the
	 * virtual machine, and refresh the machine state afterwards. The value is
	 * shown in an InputWindow and can be changed there. While the InputWindow
	 * is shown, the Shell of this composites' window is disabled.
	 */
	protected void editOperandStackEntry() {
		if (this.operandStackList.getSelectionIndices() == null || this.operandStackList.getSelectionIndices().length == 0) {
			// No entry has been selected.
			StaticGuiSupport.showMessageBox(this.shell, "Please select an entry you want to edit and try again.");
		} else {
			int selection = this.operandStackList.getItemCount() - this.operandStackList.getSelectionIndices()[0] - 1;
			Object object = this.application.getVirtualMachine().getCurrentFrame().getOperandStack().get(selection);

			// Disable the Shell.
			this.shell.setEnabled(false);

			// Draw the InputWindow.
			try {
				InputWindow inputWindow = new InputWindow(StepByStepExecutionComposite.this.shell, getDisplay());
				object = inputWindow.show(object, object.getClass().getName());

				// Set the value.
				this.application.getVirtualMachine().getCurrentFrame().getOperandStack().set(selection, object);
				refreshMachineState();
			} catch (Throwable t) {
				StaticGuiSupport.processGuiError(t, "input", StepByStepExecutionComposite.this.shell);
			}

			// Enable the Shell.
			this.shell.setEnabled(true);
			this.shell.setActive();
		}
	}

	/**
	 * Push a value onto the operand stack of the currently executed frame in
	 * the virtual machine, and refresh the machine state afterwards. The value
	 * is fetched from an InputWindow. While the InputWindow is shown, the Shell
	 * of this composites' window is disabled.
	 */
	protected void pushOntoOperandStack() {
		// Disable the Shell.
		this.shell.setEnabled(false);
		Object object = new UndefinedValue();

		// Draw the InputWindow.
		try {
			InputWindow inputWindow = new InputWindow(StepByStepExecutionComposite.this.shell, getDisplay());
			object = inputWindow.show();

			// Push the new value.
			if (!(object instanceof UndefinedValue)) {
				this.application.getVirtualMachine().getCurrentFrame().getOperandStack().push(object);
				refreshMachineState();
			}
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "input", StepByStepExecutionComposite.this.shell);
		}

		// Enable the Shell.
		this.shell.setEnabled(true);
		this.shell.setActive();
	}

	/**
	 * Pop the topmost element of the operand stack of the currently executed
	 * frame in the virtual machine, and refresh the machine state afterwards.
	 * Render a MessageBox for the user if there is no element to be popped.
	 *
	 */
	protected void popFromOperandStack() {
		if (!this.application.getVirtualMachine().getCurrentFrame().getOperandStack().isEmpty()) {
			this.application.getVirtualMachine().getCurrentFrame().getOperandStack().pop();
			refreshMachineState();
		} else {
			StaticGuiSupport.showMessageBox(this.shell, "There are no more elements to pop from the stack.");
		}
	}

	/**
	 * Empty the operand stack of the currently executed frame in the virtual
	 * machine, and refresh the machine state afterwards.
	 */
	protected void emptyOperandStack() {
		this.application.getVirtualMachine().getCurrentFrame().getOperandStack().clear();
		refreshMachineState();
	}

	/**
	 * Change the cursor's style.
	 * @param style The style as an int, according to the available SWT styles for cursors.
	 */
	protected void changeCursor(int style) {
		this.shell.setCursor(new Cursor(this.display, style));
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: abort the execution.
	 * @param doNotStopLogging If set to true, do not stop the logging.
	 */
	public synchronized void abortExecutionByExecutionRunner(final boolean doNotStopLogging) {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				abortExecution(doNotStopLogging);
			}
		});
	}

	/**
	 * If an Application has been initialized, invoke the method to abort the
	 * execution.
	 * @param doNotStopLogging If set to true, do not stop the logging.
	 */
	public void abortExecution(boolean doNotStopLogging) {
		if (this.executionRunner != null) {
			this.executionRunner.abortExecution();
			this.executionRunner = null;
		}
		if (!doNotStopLogging && this.styledTextAppender != null) {
			Globals.getInst().execLogger.removeAppender(this.styledTextAppender);
			Globals.getInst().symbolicExecLogger.removeAppender(this.styledTextAppender);
		}
	}

	/**
	 * Start logging. This initializes the ArrayLists for logging and adds the
	 * Appender for displaying the messages in the StepByStepExecutionWindow.
	 */
	protected void startLogging() {
		this.newLoggingEntryTimestamp = new ArrayList<Long>();
		this.newLoggingEntryPriority = new ArrayList<Priority>();
		this.newLoggingEntryMessage = new ArrayList<String>();
		this.entriesLogged = 0;
		this.styledTextAppender = new StepByStepLoggingAppender(this);
		Globals.getInst().execLogger.addAppender(this.styledTextAppender);
		Globals.getInst().symbolicExecLogger.addAppender(this.styledTextAppender);
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: append the logging
	 * control with a new entry.
	 *
	 * The new entries are stored in an ArrayList. Due to the asynchronity of the
	 * threads running, the entries to log might be "produced" faster than they
	 * can be displayed. To avoid threading problems and to ensure, every
	 * message is actually displayed, new logging entries are appended to the
	 * ArrayList. Due to the asynchronous calls, logToStyledText() is invoked as
	 * many times as there are new entries. However, some new entries might have
	 * been gathered before the invocation of logToStyledText() starts.
	 *
	 * @param newLoggingEntryTimestamp The timestamp of the new entry.
	 * @param newLoggingEntryPriority The Priority of the new entry.
	 * @param newLoggingEntryMessage The message of the new entry.
	 */
	public void appendLoggingStyledText(long newLoggingEntryTimestamp, Priority newLoggingEntryPriority, String newLoggingEntryMessage) {
		// Add a new entry to the lists.
		this.newLoggingEntryTimestamp.add(newLoggingEntryTimestamp);
		this.newLoggingEntryPriority.add(newLoggingEntryPriority);
		this.newLoggingEntryMessage.add(newLoggingEntryMessage);

		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				logToStyledText();
			}
		});
	}

	/**
	 * Append the logging control with a new entry.
	 */
	protected void logToStyledText() {
		// Do nothing if the logging control is disposed. This might happen if the Window is currently beeing closed but this method is still invoked.
		if (this.loggingStyledText.isDisposed()) {
			return;
		}

		// Get the entries.
		String entries = this.loggingStyledText.getText();

		// Does an old entry have to be dropped?
		boolean dropentry = false;
		if (this.entriesLogged >= Options.getInst().maximumStepByStepLoggingEntries) dropentry = true;
		if (dropentry) {
			// Find the last \n position.
			int position = entries.lastIndexOf("\n");
			if (position == -1) {
				entries = "";
			} else {
				// Drop the last line with this information.
				entries = entries.substring(0, position);
			}
		}

		// Get the new entry.
		String newEntry = this.newLoggingEntryTimestamp.get(0) + "\t" + this.newLoggingEntryPriority.get(0).toString() + "\t\t";
		// Generate a style range.
		StyleRange styleRange = new StyleRange();
		styleRange.start = newEntry.length();
		styleRange.length = this.newLoggingEntryMessage.get(0).length();
		styleRange.fontStyle = SWT.BOLD;
		styleRange.foreground = this.display.getSystemColor(SWT.COLOR_BLACK);
		newEntry += this.newLoggingEntryMessage.get(0);

		// Append the logger.
		if (entries.equals("")) {
			// There are no entries, yet. Just add the new entry.
			this.loggingStyledText.setText(newEntry);
			this.loggingStyledText.setStyleRange(styleRange);
		} else {
			// Process the existing style ranges. Since the new entry has an own style range, these ranges have to be "shifted" so the text will keep the correct formatting.
			int totalLength = newEntry.length() + 1; // Add one for the \n at the lines' end.
			StyleRange[] styleRanges = this.loggingStyledText.getStyleRanges();
			if (dropentry) {
				// Drop the oldest one and shift the other old entries.
				for (int a = styleRanges.length - 1; a > 0; a--) {
					styleRanges[a - 1].start += totalLength;
					styleRanges[a] = styleRanges[a - 1];
				}
				styleRanges[0] = styleRange;
			} else {
				// Expand the number of StyleRanges and shift the old entries.
				StyleRange[] styleRanges2 = new StyleRange[styleRanges.length + 1];
				for (int a = 0; a < styleRanges.length; a++) {
					styleRanges[a].start += totalLength;
					styleRanges2[a + 1] = styleRanges[a];
				}
				styleRanges2[0] = styleRange;
				styleRanges = styleRanges2;
			}

			// Append
			this.loggingStyledText.setText(newEntry + "\n" + entries);
			try {
				this.loggingStyledText.setStyleRanges(styleRanges);
			} catch (Exception e) {
				// Something failed. Ignore it.
			}
		}
		this.entriesLogged++;

		// Remove the entry from the list to process.
		this.newLoggingEntryTimestamp.remove(0);
		this.newLoggingEntryPriority.remove(0);
		this.newLoggingEntryMessage.remove(0);
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: stop the execution.
	 * There are some cases where the execution is not stopped by the user but
	 * by the StepByStepExecutionRunner. This will for example have some buttons
	 * enabled or disabled.
	 */
	public synchronized void stopExecutionByExecutionRunner() {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				stopExecution();
			}
		});
	}

	/**
	 * Stop the execution: Disable the stop Button, enable the buttons for
	 * further execution and signalize the StepByStepExecutionRunner to not
	 * continue execution (for now, this is not an abortion).
	 */
	protected void stopExecution() {
		this.executionRunner.stopExecution();
		this.stopButton.setEnabled(false);
		this.nextStepButton.setEnabled(true);
		this.goButton.setEnabled(true);
		this.gotoButton.setEnabled(true);
		this.executeNextCombo.setEnabled(true);
		this.executeOnlyButton.setEnabled(true);
		this.secondsCombo.setEnabled(true);
	}

	/**
	 * Getter for the currently instantiated StepByStepExecutionRunner.
	 * @return The StepByStepExecutionRunner.
	 */
	protected StepByStepExecutionRunner getExecutionRunner() {
		return this.executionRunner;
	}

	/**
	 * Getter for the List of solutions.
	 * @return The List of solutions.
	 */
	protected List getSolutionsList() {
		return this.solutionsList;
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: add a solution to the
	 * list of solutions. If doNotFormat is set to true, the Object solution has
	 * to be a String and already formated for being added to the list.
	 * Otherwise, it will be formatted to extract its information.
	 *
	 * @param solution  The solution to add.
	 * @param doNotFormat Toggles whether the object will be formated before being added to the solution list.
	 */
	public synchronized void addSolution(final Object solution, final boolean doNotFormat) {
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				if (doNotFormat) {
					getSolutionsList().add((String) solution);
				} else {
					getSolutionsList().add(formatVMObject(solution));
				}
			}
		});
	}

	/**
	 * Getter for the instructionLoadingComplete. It is used by the
	 * StepByStepExecutionRunner to find out whether the loading of the
	 * instructions (as a result of a change in the currently executed frame)
	 * has been completed. The StepByStepExecutionRunner might for example not
	 * set the pc reached in a new frame until the instructions have been loaded
	 * completely, since this pc might not even exists for the "old" set of
	 * instructions. Loading the instructions needs some analysis and might take
	 * a lot of memory due to the graphical formating, so it can take some time.
	 *
	 * @return true, if the instructions has been loaded, false otherwise.
	 */
	public boolean getInstructionLoadingComplete() {
		return this.instructionLoadingComplete;
	}

	/**
	 * Set the application thats execution is controlled in this step by step
	 * execution.
	 *
	 * @param application The Application for this StepByStepExecutionComposite.
	 */
	public void setApplication(Application application) {
		this.application = application;
	}

	/**
	 * Wrapper method for the StepByStepExecutionRunner: set the steps that are
	 * still to go.
	 *
	 * If multiple steps are executed in a row, the user will see the steps
	 * beeing counted down until they reach zero.
	 *
	 * @param steps The next value for the steps.
	 */
	public synchronized void setStepsToGo(final int steps) {
		final Combo executeNextCombo = this.executeNextCombo;
		// Asynchronous access.
		this.display.asyncExec(new Runnable() {
			public void run() {
				executeNextCombo.setText(String.valueOf(steps));
			}
		});
	}

}
