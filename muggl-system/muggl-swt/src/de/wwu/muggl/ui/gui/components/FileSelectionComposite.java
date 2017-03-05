package de.wwu.muggl.ui.gui.components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarFile;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.ui.gui.SWTGui;
import de.wwu.muggl.ui.gui.support.ImageRepository;
import de.wwu.muggl.ui.gui.support.JarFileEntry;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ClassInspectionWindow;
import de.wwu.muggl.ui.gui.windows.ExecutionWindow;
import de.wwu.muggl.ui.gui.windows.MethodParametersWindow;
import de.wwu.muggl.ui.gui.windows.StepByStepExecutionWindow;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The composite for the SWTGui's main window. It offers most of its element and the corresponding
 * methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-8-18
 */
public class FileSelectionComposite extends Composite {
	// General fields for the window.
	SWTGui parent;
	Shell shell;
	Display display;

	// Fields for data that can be altered using the functionality of this composite.
	ArrayList<Object[]> fileListArray;
	MugglClassLoader classLoader;
	ClassFile currentClass;
	String currentClassSelected;
	String currentClassPackage;
	boolean hideDrivesABOldSetting;

	// Constant fields for the composites elements.
	private final FormData directorySelectionLabelFormData;
	private final Label directorySelectionLabel;
	private final FormData directoryTreeFormData;
	private final Tree directoryTree;
	private final FormData refreshTreeButtonFormData;
	private final Button refreshTreeButton;
	private final FormData directOpenButtonFormData;
	private final Button directOpenButton;
	private final FormData fileSelectionLabelFormData;
	private final Label fileSelectionLabel;
	private final FormData fileListFormData;
	final List fileList;
	private final FormData fileInspectButtonFormData;
	private final Button fileInspectButton;
	private final FormData fileSelectButtonFormData;
	private final Button refreshClassButton;
	private final FormData methodSelectionLabelFormData;
	private final Label methodSelectionLabel;
	private final FormData methodListFormData;
	final List methodList;
	private final FormData hideInitCheckFormData;
	final Button hideInitCheckButton;
	private final FormData mainOnlyCheckFormData;
	final Button mainOnlyCheckButton;
	private final FormData modeSelectionFormData;
	private final Group modeSelectionGroup;
	private final FormData normalModeRadioFormData;
	private final Button normalModeRadioButton;
	private final FormData symbolicModeRadioFormData;
	private final Button symbolicModeRadioButton;
	private final FormData logicModeRadioFormData;
	private final FormData jpaModeRadioFormData;
	private final Button logicModeRadioButton;
	private final Button jpaModeRadioButton;
	private final FormData stepByStepCheckFormData;
	private final Button stepByStepCheckButton;
	private final FormData predefinedDataSelectionFormData;
	private final Group predefinedDataSelectionGroup;
	private final FormData methodParametersLabelFormData;
	private final Label methodParametersLabel;
	private final FormData methodParametersTextFormData;
	private final Text methodParametersText;
	private final FormData methodParametersButtonFormData;
	private final Button methodParametersButton;
	private final FormData variableGeneratorsLabelFormData;
	private final Label variableGeneratorsLabel;
	private final FormData variableGeneratorsTextFormData;
	private final Text variableGeneratorsText;
	private final FormData variableValidatorsLabelFormData;
	private final Label variableValidatorsLabel;
	private final FormData variableValidatorsTextFormData;
	private final Text variableValidatorsText;
	private final FormData variableValidatorsButtonFormData;
	private final Button variableValidatorsButton;
	private final FormData executeButtonFormData;
	private final Button executeButton;

	// A popup menu and an auxiliary field.
	final Menu visibilityMenu;
	int visibility;

	/**
	 * Set up the composite for the SWTGui window.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 */
	public FileSelectionComposite(SWTGui parent, Shell shell, Display display, int style) {
		// General initialization.
		super(shell, style);
		this.parent = parent;
		this.shell = shell;
		this.display = display;
		this.setLayout(new FormLayout());
		this.hideDrivesABOldSetting = Options.getInst().hideDrivesAB;

		// Set up the array.
		this.fileListArray = new ArrayList<Object[]>();

		// Set up the widgets: directory selection widgets.
		this.directorySelectionLabelFormData = new FormData();
		this.directorySelectionLabelFormData.top = new FormAttachment(this, 2, SWT.BOTTOM);
		this.directorySelectionLabelFormData.bottom = new FormAttachment(this, 20, SWT.BOTTOM);
		this.directorySelectionLabelFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.directorySelectionLabelFormData.right = new FormAttachment(this, 255, SWT.RIGHT);

		this.directorySelectionLabel = new Label(this, SWT.None);
		this.directorySelectionLabel.setText("Class-file directory / jar-file:");
		this.directorySelectionLabel.setLayoutData(this.directorySelectionLabelFormData);

		this.directoryTreeFormData = new FormData();
		this.directoryTreeFormData.top = new FormAttachment(this.directorySelectionLabel, 5, SWT.BOTTOM);
		this.directoryTreeFormData.bottom = new FormAttachment(this.directorySelectionLabel, 505, SWT.BOTTOM);
		this.directoryTreeFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.directoryTreeFormData.right = new FormAttachment(this, 255, SWT.RIGHT);

		this.directoryTree = new Tree(this, SWT.BORDER);
		this.directoryTree.setSize(250, 500);
		this.directoryTree.setLayoutData(this.directoryTreeFormData);

		this.refreshTreeButtonFormData = new FormData();
		this.refreshTreeButtonFormData.top = new FormAttachment(this.directoryTree, 10, SWT.BOTTOM);
		this.refreshTreeButtonFormData.bottom = new FormAttachment(this.directoryTree, 45, SWT.BOTTOM);
		this.refreshTreeButtonFormData.left = new FormAttachment(this, 20, SWT.RIGHT);
		this.refreshTreeButtonFormData.right = new FormAttachment(this, 50, SWT.RIGHT);

		this.refreshTreeButton = new Button(this, SWT.NONE);
		this.refreshTreeButton.setText("&Refresh...");
		try {
			this.refreshTreeButton.setImage(ImageRepository.getInst().refreshImage);
		} catch (GUIException e1) {
			// This cannot happen since it would have been triggered when the SWT GUI was launched.
		}
		this.refreshTreeButton.setLayoutData(this.refreshTreeButtonFormData);

		this.directOpenButtonFormData = new FormData();
		this.directOpenButtonFormData.top = new FormAttachment(this.directoryTree, 10, SWT.BOTTOM);
		this.directOpenButtonFormData.bottom = new FormAttachment(this.directoryTree, 45, SWT.BOTTOM);
		this.directOpenButtonFormData.left = new FormAttachment(this.refreshTreeButton, 80, SWT.RIGHT);
		this.directOpenButtonFormData.right = new FormAttachment(this.refreshTreeButton, 165, SWT.RIGHT);

		this.directOpenButton = new Button(this, SWT.NONE);
		this.directOpenButton.setText("Direct &open");
		this.directOpenButton.setLayoutData(this.directOpenButtonFormData);

		// Set up the widgets: file selection widgets.
		this.fileSelectionLabelFormData = new FormData();
		this.fileSelectionLabelFormData.top = new FormAttachment(this, 2, SWT.BOTTOM);
		this.fileSelectionLabelFormData.bottom = new FormAttachment(this, 20, SWT.BOTTOM);
		this.fileSelectionLabelFormData.left = new FormAttachment(this.directorySelectionLabel, 5, SWT.RIGHT);
		this.fileSelectionLabelFormData.right = new FormAttachment(this.directorySelectionLabel, 205, SWT.RIGHT);

		this.fileSelectionLabel = new Label(this, SWT.None);
		this.fileSelectionLabel.setText("Class file:");
		this.fileSelectionLabel.setLayoutData(this.fileSelectionLabelFormData);

		this.fileListFormData = new FormData();
		this.fileListFormData.top = new FormAttachment(this.directorySelectionLabel, 5, SWT.BOTTOM);
		this.fileListFormData.bottom = new FormAttachment(this.directorySelectionLabel, 505, SWT.BOTTOM);
		this.fileListFormData.left = new FormAttachment(this.directoryTree, 5, SWT.RIGHT);
		this.fileListFormData.right = new FormAttachment(this.directoryTree, 205, SWT.RIGHT);

		this.fileList = new List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		this.fileList.setSize(200, 550);
		this.fileList.setLayoutData(this.fileListFormData);

		this.fileInspectButtonFormData = new FormData();
		this.fileInspectButtonFormData.top = new FormAttachment(this.fileList, 10, SWT.BOTTOM);
		this.fileInspectButtonFormData.bottom = new FormAttachment(this.fileList, 45, SWT.BOTTOM);
		this.fileInspectButtonFormData.left = new FormAttachment(this.directoryTree, 10, SWT.RIGHT);
		this.fileInspectButtonFormData.right = new FormAttachment(this.directoryTree, 90, SWT.RIGHT);

		this.fileInspectButton = new Button(this, SWT.NONE);
		this.fileInspectButton.setText("&Inspect file...");
		this.fileInspectButton.setLayoutData(this.fileInspectButtonFormData);

		this.fileSelectButtonFormData = new FormData();
		this.fileSelectButtonFormData.top = new FormAttachment(this.fileList, 10, SWT.BOTTOM);
		this.fileSelectButtonFormData.bottom = new FormAttachment(this.fileList, 45, SWT.BOTTOM);
		this.fileSelectButtonFormData.left = new FormAttachment(this.fileInspectButton, 10, SWT.RIGHT);
		this.fileSelectButtonFormData.right = new FormAttachment(this.fileInspectButton, 110, SWT.RIGHT);

		this.refreshClassButton = new Button(this, SWT.NONE);
		this.refreshClassButton.setText("&Refresh class");
		this.refreshClassButton.setLayoutData(this.fileSelectButtonFormData);

		// Set up the widgets: method selection widgets.
		this.methodSelectionLabelFormData = new FormData();
		this.methodSelectionLabelFormData.top = new FormAttachment(this, 2, SWT.BOTTOM);
		this.methodSelectionLabelFormData.bottom = new FormAttachment(this, 20, SWT.BOTTOM);
		this.methodSelectionLabelFormData.left = new FormAttachment(this.fileSelectionLabel, 5, SWT.RIGHT);
		this.methodSelectionLabelFormData.right = new FormAttachment(this.fileSelectionLabel, 360, SWT.RIGHT);

		this.methodSelectionLabel = new Label(this, SWT.None);
		this.methodSelectionLabel.setText("Method:");
		this.methodSelectionLabel.setLayoutData(this.methodSelectionLabelFormData);

		this.methodListFormData = new FormData();
		this.methodListFormData.top = new FormAttachment(this.directorySelectionLabel, 5, SWT.BOTTOM);
		this.methodListFormData.bottom = new FormAttachment(this.directorySelectionLabel, 331, SWT.BOTTOM);
		this.methodListFormData.left = new FormAttachment(this.fileList, 5, SWT.RIGHT);
		this.methodListFormData.right = new FormAttachment(this.fileList, 360, SWT.RIGHT);

		this.methodList = new List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		this.methodList.setLayoutData(this.methodListFormData);

		this.hideInitCheckFormData = new FormData();
		this.hideInitCheckFormData.top = new FormAttachment(this.methodList, 5, SWT.BOTTOM);
		this.hideInitCheckFormData.bottom = new FormAttachment(this.methodList, 25, SWT.BOTTOM);
		this.hideInitCheckFormData.left = new FormAttachment(this.fileList, 10, SWT.RIGHT);
		this.hideInitCheckFormData.right = new FormAttachment(this.fileList, 360, SWT.RIGHT);

		this.hideInitCheckButton = new Button(this, SWT.CHECK);
		this.hideInitCheckButton.setText("Hide <init>- and <clinit>-Methods (static / instance constructors))");
		this.hideInitCheckButton.setSelection(true);
		this.hideInitCheckButton.setLayoutData(this.hideInitCheckFormData);

		this.mainOnlyCheckFormData = new FormData();
		this.mainOnlyCheckFormData.top = new FormAttachment(this.hideInitCheckButton, 5, SWT.BOTTOM);
		this.mainOnlyCheckFormData.bottom = new FormAttachment(this.hideInitCheckButton, 20, SWT.BOTTOM);
		this.mainOnlyCheckFormData.left = new FormAttachment(this.fileList, 10, SWT.RIGHT);
		this.mainOnlyCheckFormData.right = new FormAttachment(this.fileList, 360, SWT.RIGHT);

		this.mainOnlyCheckButton = new Button(this, SWT.CHECK);
		this.mainOnlyCheckButton.setText("Show public static void main(String[] args)-Method only");
		this.mainOnlyCheckButton.setSelection(false);
		this.mainOnlyCheckButton.setLayoutData(this.mainOnlyCheckFormData);

		// Set up the widgets: option widgets, setting of predefined data.
		this.predefinedDataSelectionFormData = new FormData();
		this.predefinedDataSelectionFormData.top = new FormAttachment(this.mainOnlyCheckButton, 7, SWT.BOTTOM);
		this.predefinedDataSelectionFormData.bottom = new FormAttachment(this.mainOnlyCheckButton, 93, SWT.BOTTOM);
		this.predefinedDataSelectionFormData.left = new FormAttachment(this.fileList, 10, SWT.RIGHT);
		this.predefinedDataSelectionFormData.right = new FormAttachment(this.fileList, 360, SWT.RIGHT);

		this.predefinedDataSelectionGroup = new Group(this, SWT.NONE);
		this.predefinedDataSelectionGroup.setSize(134, 255);
		this.predefinedDataSelectionGroup.setText("Predefined data");
		this.predefinedDataSelectionGroup.setLayoutData(this.predefinedDataSelectionFormData);
		this.predefinedDataSelectionGroup.setLayout(new FormLayout());

		this.methodParametersLabelFormData = new FormData();
		this.methodParametersLabelFormData.top = new FormAttachment(this.predefinedDataSelectionGroup, 5, SWT.BOTTOM);
		this.methodParametersLabelFormData.bottom = new FormAttachment(this.predefinedDataSelectionGroup, 22, SWT.BOTTOM);
		this.methodParametersLabelFormData.left = new FormAttachment(this.predefinedDataSelectionGroup, 5, SWT.RIGHT);
		this.methodParametersLabelFormData.right = new FormAttachment(this.predefinedDataSelectionGroup, 130, SWT.RIGHT);

		this.methodParametersLabel = new Label(this.predefinedDataSelectionGroup, SWT.None);
		this.methodParametersLabel.setText("Supplied parameters:");
		this.methodParametersLabel.setLayoutData(this.methodParametersLabelFormData);

		this.methodParametersTextFormData = new FormData();
		this.methodParametersTextFormData.top = new FormAttachment(this.predefinedDataSelectionGroup, 5, SWT.BOTTOM);
		this.methodParametersTextFormData.bottom = new FormAttachment(this.predefinedDataSelectionGroup, 22, SWT.BOTTOM);
		this.methodParametersTextFormData.left = new FormAttachment(this.methodParametersLabel, 5, SWT.RIGHT);
		this.methodParametersTextFormData.right = new FormAttachment(this.methodParametersLabel, 47, SWT.RIGHT);

		this.methodParametersText = new Text(this.predefinedDataSelectionGroup, SWT.BORDER | SWT.RIGHT);
		this.methodParametersText.setEditable(false);
		this.methodParametersText.setLayoutData(this.methodParametersTextFormData);

		this.methodParametersButtonFormData = new FormData();
		this.methodParametersButtonFormData.top = new FormAttachment(this.predefinedDataSelectionGroup, 11, SWT.BOTTOM);
		this.methodParametersButtonFormData.bottom = new FormAttachment(this.predefinedDataSelectionGroup, 38, SWT.BOTTOM);
		this.methodParametersButtonFormData.left = new FormAttachment(this.methodParametersText, 13, SWT.RIGHT);
		this.methodParametersButtonFormData.right = new FormAttachment(this.methodParametersText, 65, SWT.RIGHT);

		this.methodParametersButton = new Button(this.predefinedDataSelectionGroup, SWT.None);
		this.methodParametersButton.setText("Edit...");
		this.methodParametersButton.setLayoutData(this.methodParametersButtonFormData);

		this.variableGeneratorsLabelFormData = new FormData();
		this.variableGeneratorsLabelFormData.top = new FormAttachment(this.methodParametersLabel, 5, SWT.BOTTOM);
		this.variableGeneratorsLabelFormData.bottom = new FormAttachment(this.methodParametersLabel, 22, SWT.BOTTOM);
		this.variableGeneratorsLabelFormData.left = new FormAttachment(this.predefinedDataSelectionGroup, 5, SWT.RIGHT);
		this.variableGeneratorsLabelFormData.right = new FormAttachment(this.predefinedDataSelectionGroup, 130, SWT.RIGHT);

		this.variableGeneratorsLabel = new Label(this.predefinedDataSelectionGroup, SWT.None);
		this.variableGeneratorsLabel.setText("Variable generators:");
		this.variableGeneratorsLabel.setLayoutData(this.variableGeneratorsLabelFormData);

		this.variableGeneratorsTextFormData = new FormData();
		this.variableGeneratorsTextFormData.top = new FormAttachment(this.methodParametersLabel, 5, SWT.BOTTOM);
		this.variableGeneratorsTextFormData.bottom = new FormAttachment(this.methodParametersLabel, 22, SWT.BOTTOM);
		this.variableGeneratorsTextFormData.left = new FormAttachment(this.variableGeneratorsLabel, 5, SWT.RIGHT);
		this.variableGeneratorsTextFormData.right = new FormAttachment(this.variableGeneratorsLabel, 47, SWT.RIGHT);

		this.variableGeneratorsText = new Text(this.predefinedDataSelectionGroup, SWT.BORDER | SWT.RIGHT);
		this.variableGeneratorsText.setEditable(false);
		this.variableGeneratorsText.setLayoutData(this.variableGeneratorsTextFormData);

		this.variableValidatorsLabelFormData = new FormData();
		this.variableValidatorsLabelFormData.top = new FormAttachment(this.variableGeneratorsLabel, 5, SWT.BOTTOM);
		this.variableValidatorsLabelFormData.bottom = new FormAttachment(this.variableGeneratorsLabel, 22, SWT.BOTTOM);
		this.variableValidatorsLabelFormData.left = new FormAttachment(this.predefinedDataSelectionGroup, 5, SWT.RIGHT);
		this.variableValidatorsLabelFormData.right = new FormAttachment(this.predefinedDataSelectionGroup, 130, SWT.RIGHT);

		this.variableValidatorsLabel = new Label(this.predefinedDataSelectionGroup, SWT.None);
		this.variableValidatorsLabel.setText("Variable validators:");
		this.variableValidatorsLabel.setLayoutData(this.variableValidatorsLabelFormData);

		this.variableValidatorsTextFormData = new FormData();
		this.variableValidatorsTextFormData.top = new FormAttachment(this.variableGeneratorsLabel, 5, SWT.BOTTOM);
		this.variableValidatorsTextFormData.bottom = new FormAttachment(this.variableGeneratorsLabel, 22, SWT.BOTTOM);
		this.variableValidatorsTextFormData.left = new FormAttachment(this.variableValidatorsLabel, 5, SWT.RIGHT);
		this.variableValidatorsTextFormData.right = new FormAttachment(this.variableValidatorsLabel, 47, SWT.RIGHT);

		this.variableValidatorsText = new Text(this.predefinedDataSelectionGroup, SWT.BORDER | SWT.RIGHT);
		this.variableValidatorsText.setEditable(false);
		this.variableValidatorsText.setLayoutData(this.variableValidatorsTextFormData);

		this.variableValidatorsButtonFormData = new FormData();
		this.variableValidatorsButtonFormData.top = new FormAttachment(this.variableGeneratorsLabel, 5, SWT.BOTTOM);
		this.variableValidatorsButtonFormData.bottom = new FormAttachment(this.variableGeneratorsLabel, 22, SWT.BOTTOM);
		this.variableValidatorsButtonFormData.left = new FormAttachment(this.variableValidatorsText, 13, SWT.RIGHT);
		this.variableValidatorsButtonFormData.right = new FormAttachment(this.variableValidatorsText, 65, SWT.RIGHT);

		this.variableValidatorsButton = new Button(this.predefinedDataSelectionGroup, SWT.None);
		this.variableValidatorsButton.setText("Edit...");
		this.variableValidatorsButton.setLayoutData(this.variableValidatorsButtonFormData);

		// Set up the widgets: option widgets, selection of the operation mode
		this.modeSelectionFormData = new FormData();
		this.modeSelectionFormData.top = new FormAttachment(this.predefinedDataSelectionGroup, 3, SWT.BOTTOM);
		this.modeSelectionFormData.bottom = new FormAttachment(this.predefinedDataSelectionGroup, 81, SWT.BOTTOM);
		this.modeSelectionFormData.left = new FormAttachment(this.fileList, 10, SWT.RIGHT);
		this.modeSelectionFormData.right = new FormAttachment(this.fileList, 220, SWT.RIGHT);

		this.modeSelectionGroup = new Group(this, SWT.NONE);
		//this.modeSelectionGroup.setSize(78, 255);
		this.modeSelectionGroup.setText("Execution mode");
		this.modeSelectionGroup.setLayoutData(this.modeSelectionFormData);
		this.modeSelectionGroup.setLayout(new FormLayout());

		this.normalModeRadioFormData = new FormData();
		this.normalModeRadioFormData.top = new FormAttachment(this.modeSelectionGroup, 5, SWT.BOTTOM);
		this.normalModeRadioFormData.bottom = new FormAttachment(this.modeSelectionGroup, 20, SWT.BOTTOM);
		this.normalModeRadioFormData.left = new FormAttachment(this.modeSelectionGroup, 5, SWT.RIGHT);
		this.normalModeRadioFormData.right = new FormAttachment(this.modeSelectionGroup, 100, SWT.RIGHT);

		this.normalModeRadioButton = new Button(this.modeSelectionGroup, SWT.RADIO);
		this.normalModeRadioButton.setText("Normal");
		this.normalModeRadioButton.setLayoutData(this.normalModeRadioFormData);

		this.symbolicModeRadioFormData = new FormData();
		this.symbolicModeRadioFormData.top = new FormAttachment(this.normalModeRadioButton, 5, SWT.BOTTOM);
		this.symbolicModeRadioFormData.bottom = new FormAttachment(this.normalModeRadioButton, 20, SWT.BOTTOM);
		this.symbolicModeRadioFormData.left = new FormAttachment(this.modeSelectionGroup, 5, SWT.RIGHT);
		this.symbolicModeRadioFormData.right = new FormAttachment(this.modeSelectionGroup, 100, SWT.RIGHT);

		this.symbolicModeRadioButton = new Button(this.modeSelectionGroup, SWT.RADIO);
		this.symbolicModeRadioButton.setText("Symbolical");
		this.symbolicModeRadioButton.setLayoutData(this.symbolicModeRadioFormData);
		
		this.jpaModeRadioFormData = new FormData();
		this.jpaModeRadioFormData.top = new FormAttachment(this.symbolicModeRadioButton, 5, SWT.BOTTOM);
		this.jpaModeRadioFormData.bottom = new FormAttachment(this.symbolicModeRadioButton, 20, SWT.BOTTOM);
		this.jpaModeRadioFormData.left = new FormAttachment(this.modeSelectionGroup, 5, SWT.RIGHT);
		this.jpaModeRadioFormData.right = new FormAttachment(this.modeSelectionGroup, 100, SWT.RIGHT);
		
		this.jpaModeRadioButton = new Button(this.modeSelectionGroup, SWT.RADIO);
		this.jpaModeRadioButton.setText("JPA");
		this.jpaModeRadioButton.setLayoutData(this.jpaModeRadioFormData);
		
		this.logicModeRadioFormData = new FormData();
		this.logicModeRadioFormData.top = new FormAttachment(this.jpaModeRadioButton, 5, SWT.BOTTOM);
		this.logicModeRadioFormData.bottom = new FormAttachment(this.jpaModeRadioButton, 20, SWT.BOTTOM);
		this.logicModeRadioFormData.left = new FormAttachment(this.modeSelectionGroup, 5, SWT.RIGHT);
		this.logicModeRadioFormData.right = new FormAttachment(this.modeSelectionGroup, 100, SWT.RIGHT);

		this.logicModeRadioButton = new Button(this.modeSelectionGroup, SWT.RADIO);
		this.logicModeRadioButton.setText("Logical");
		this.logicModeRadioButton.setLayoutData(this.logicModeRadioFormData);

		this.stepByStepCheckFormData = new FormData();
		this.stepByStepCheckFormData.top = new FormAttachment(this.normalModeRadioButton, 10, SWT.TOP);
		this.stepByStepCheckFormData.bottom = new FormAttachment(this.normalModeRadioButton, 25, SWT.TOP);
		this.stepByStepCheckFormData.left = new FormAttachment(this.normalModeRadioButton, 5, SWT.RIGHT);
		this.stepByStepCheckFormData.right = new FormAttachment(this.normalModeRadioButton, 80, SWT.RIGHT);

		this.stepByStepCheckButton = new Button(this.modeSelectionGroup, SWT.CHECK);
		this.stepByStepCheckButton.setText("Single steps");
		this.stepByStepCheckButton.setSelection(true);
		this.stepByStepCheckButton.setLayoutData(this.stepByStepCheckFormData);

		this.executeButtonFormData = new FormData();
		this.executeButtonFormData.top = new FormAttachment(this.predefinedDataSelectionGroup, 9, SWT.BOTTOM);
		this.executeButtonFormData.bottom = new FormAttachment(this.predefinedDataSelectionGroup, 44, SWT.BOTTOM);
		this.executeButtonFormData.left = new FormAttachment(this.modeSelectionGroup, 25, SWT.RIGHT);
		this.executeButtonFormData.right = new FormAttachment(this.modeSelectionGroup, 120, SWT.RIGHT);

		this.executeButton = new Button(this, SWT.NONE);
		this.executeButton.setText("&Execute!");
		this.executeButton.setLayoutData(this.executeButtonFormData);

		// Popup menu.
		String[] options = {"public", "package", "protected", "private"};
		this.visibility = 1;
		this.visibilityMenu = new Menu(FileSelectionComposite.this.shell, SWT.POP_UP);
		MenuItem captionMenuItem = new MenuItem(this.visibilityMenu, SWT.PUSH);
		captionMenuItem.setEnabled(false);
		captionMenuItem.setText("Select visibility");
		new MenuItem(this.visibilityMenu, SWT.SEPARATOR);

		// Add the items.
		for (int a = 0; a < options.length; a++) {
			final int visibility = a;
			MenuItem menuItem = new MenuItem(this.visibilityMenu, SWT.RADIO);
			menuItem.setText(options[a]);
			if (visibility == this.visibility) menuItem.setSelection(true);
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					MenuItem item = (MenuItem) e.widget;
					if (item.getSelection()) {
						changeVisibility(visibility);
					}
				}
			});
		}
		// Set the menu.
		this.methodList.setMenu(this.visibilityMenu);
		this.shell.setMenu(this.visibilityMenu);

		// Initialize.
		this.classLoader = new MugglClassLoader(StaticGuiSupport.arrayList2StringArray(Options.getInst().classPathEntries));
		changeFileListEnabled(false);
		loadRootsIntoDirectoryTree();

	    // Add listeners.
		/*
		 * If the window get the focus, it probably does so because another window was closed.
		 * In the mean time, settings could have changes. Check that and take actions accordingly.
		 */
		this.shell.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				if (Options.getInst().hideDrivesAB != FileSelectionComposite.this.hideDrivesABOldSetting) {
					FileSelectionComposite.this.hideDrivesABOldSetting = Options.getInst().hideDrivesAB;
					loadRootsIntoDirectoryTree();
				}
			}

			public void focusLost(FocusEvent arg0) { }

		});


		/*
		 * Alter the file list when elements on the directory tree are selected.
		 */
	    this.directoryTree.addSelectionListener(new SelectionListener() {
	    	public void widgetDefaultSelected(final SelectionEvent event) { }

	    	public void widgetSelected(final SelectionEvent event) {
	    		getCurrentClassPath();

	    		if (event != null && event.item != null) {
		    		getFileList().removeAll();
		    		resetCurrentClass();
		    		clearFileList();
		    		Object object = event.item.getData();
		    		// Distinguish between normal files and jar files.
		    		if (object instanceof File) {
		    			File file = (File) object;
			    		File[] files = file.listFiles();
			    		if (files == null || files.length == 0) {
			    			if (JarFileEntry.isArchive(file.getName())) {
			    				try {
			    					JarFile jarfile = new JarFile(file);
			    					JarFileEntry jfe = new JarFileEntry(jarfile, "", getFileListArray());
			    					jfe.expandClassFiles(getFileList());
			    					if (getFileList().getItemCount() > 0) {
			    		    			getFileList().setEnabled(true);
			    		    		} else {
			    		    			changeFileListEnabled(false);
			    		    			
			    		    		}
			    				} catch (IOException e) {
			    					StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Could not load classes in the jar file's root due to an I/O error.", SWT.OK | SWT.ICON_ERROR);
			    				}
			    				return;
			    			} else {
				    			changeFileListEnabled(false);
				    			return;
			    			}
			    		}
			    		for (int a = 0; a < files.length; a++) {
			    			if (files[a].isFile()) {
			    				String filename = files[a].getName();
			    				int filenameLength = filename.length();
			    				if (filenameLength > 6 && filename.substring(filenameLength - 6).equals(".class")) {
			    					getFileList().add(filename);
			    					addToFileList(filename, files[a].getAbsolutePath(), null);
			    				}
			    			}
			    		}
		    		} else if (object instanceof JarFileEntry) {
		    			JarFileEntry jfe = (JarFileEntry) object;
		    			jfe.expandClassFiles(getFileList());
		    		}

		    		// Enable or disable the fileList according to the files that were found in the current directory.
		    		if (getFileList().getItemCount() > 0) {
		    			getFileList().setEnabled(true);
		    		} else {
		    			changeFileListEnabled(false);
		    		}
	    		}
	    	}
	    });

		/*
		 * Expand the directory tree if an element in it is selected.
		 */
	    this.directoryTree.addListener(SWT.Expand, new Listener() {
	    	public void handleEvent(final Event event) {
	    		final TreeItem root = (TreeItem) event.item;
	    		expandDirectoryTree(root);
	    	}
	    });

	    /*
	     * Refresh the directory tree if the responsible button is pressed.
	     */
	    this.refreshTreeButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    			refreshTree();
	    	}
	    });

	    /*
	     * Launch the dialog for the direct opening of class files upon
	     * pressing the responsible button.
	     */
	    this.directOpenButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		openFileDirectly();
	    	}
	    });

	    /*
	     * Proceed with the handling of a selected file.
	     */
	    this.fileList.addSelectionListener(new SelectionListener() {
	    	public void widgetDefaultSelected(final SelectionEvent event) { }

	    	public void widgetSelected(final SelectionEvent event) {
	    		String[] selection = getFileList().getSelection();
	    		classWasSelected(selection);
	    	}
	    });

	    /*
	     * Open the selected class file in the ClassInspectionWindow and catch possible
	     * exceptions while trying to do so, generating information for the user in such
	     * a case.
	     */
	    this.fileInspectButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		String [] selection = getFileList().getSelection();
	    		if (selection != null && selection.length != 0) {
	    			String className = selection[0];
	    			if (className != null) {
	    				changeCursor(SWT.CURSOR_WAIT);
	    				getRefreshClassButton().setEnabled(false);
	    				try {
	    					// Launch the window.
	    	    			loadMethods(className, false);
	    	    			getMethodList().setEnabled(true);
	    	    			changeCursor(SWT.CURSOR_ARROW);
	    	    			getRefreshClassButton().setEnabled(true);
	    	    			try {
		    	    			ClassInspectionWindow classInspectionWindow = new ClassInspectionWindow();
		    	    			classInspectionWindow.show(FileSelectionComposite.this.shell, className, getCurrentClass());
	    	    			} catch (Throwable t) {
	    	    				StaticGuiSupport.processGuiError(t, "class inspection", FileSelectionComposite.this.shell);
	    	    			}
	    				} catch (ClassFileException e) {
	    					StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Error", "Could not load class file due to an parsing error.\n\nReason is: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
	    				}
	    				if (!getThis().isDisposed()) {
		    				getRefreshClassButton().setEnabled(true);
		    				changeCursor(SWT.CURSOR_ARROW);
	    				}
	    			}
	    		}
	    	}
	    });

	    /*
	     * Pressing the fileSelectButton starts the parsing of the selected file.
	     */
	    this.refreshClassButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		showMethods(true);
	    	}
	    });

	    /*
	     * When a method is selected, the options dialog should be enabled and
	     * its' values set to default values.
	     */
	    this.methodList.addSelectionListener(new SelectionListener() {
	    	public void widgetDefaultSelected(final SelectionEvent event) { }

	    	public void widgetSelected(final SelectionEvent event) {
	    		changeOptionsEnabled(true);
	    		resetPredefinedDataValues();
	    	}
	    });

	    /*
	     * Set the location of the popup menu.
	     */
	    this.methodList.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent event) { }

			public void mouseDown(MouseEvent event) { }

			public void mouseUp(MouseEvent event) {
				// Add the bounds of the window and of the composite to get the correct position for the popup.
				Rectangle shellBounds = FileSelectionComposite.this.shell.getBounds();
				Rectangle compositeBounds = FileSelectionComposite.this.methodList.getBounds();
				int x = shellBounds.x + compositeBounds.x + event.x;
				int y = shellBounds.y + compositeBounds.y + event.y;
				FileSelectionComposite.this.visibilityMenu.setLocation(x, y);
			}
	    });

	    /*
	     * Reload the methods of the currently selected class, there might be
	     * a change in the methods to display.
	     */
	    this.hideInitCheckButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		try {
	    			if (FileSelectionComposite.this.fileList.getSelectionCount() > 0)
	    				loadMethods(null, false);
	    		} catch (ClassFileException e) {
	    			StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Error", "Could not reload the methods due to a ClassFileException, so the listing might be incorrect.\n\nRoot cause is: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
	    		}
	    	}
	    });

	    /*
	     * Reload the methods of the currently selected class, there might be
	     * a change in the methods to display.
	     */
	    this.mainOnlyCheckButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		try {
	    			// Enable or disable the subordinated button.
	    			if (FileSelectionComposite.this.mainOnlyCheckButton.getSelection()) {
	    				FileSelectionComposite.this.hideInitCheckButton.setEnabled(false);
	    			} else {
	    				FileSelectionComposite.this.hideInitCheckButton.setEnabled(true);
	    			}

	    			// Reload the methods.
	    			if (FileSelectionComposite.this.fileList.getSelectionCount() > 0)
	    				loadMethods(null, false);
	    		} catch (ClassFileException e) {
	    			StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Error", "Could not reload the methods due to a ClassFileException, so the listing might be incorrect.\n\nRoot cause is: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
	    		}
	    	}
	    });

	    /*
	     * Set the execution mode to normal.
	     */
	    this.normalModeRadioButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (Options.getInst().symbolicMode == true) {
	    			Options.getInst().symbolicMode = false;
	    		}
	    		if (Options.getInst().logicMode == true) {
	    			Options.getInst().logicMode = false;
	    		}
	    		if (Options.getInst().jpaMode == true) {
	    			Options.getInst().jpaMode = false;
	    		}
	    	}
	    });

	    /*
	     * Set the execution mode to symbolic and enable those options that only apply for
	     * the symbolic execution mode.
	     */
	    this.symbolicModeRadioButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (Options.getInst().symbolicMode == false) {
		    		Options.getInst().symbolicMode = true;
	    		}
	    		if (Options.getInst().logicMode == true) {
	    			Options.getInst().logicMode = false;
	    		}
	    		if (Options.getInst().jpaMode == true) {
	    			Options.getInst().jpaMode = false;
	    		}
	    	}
	    });
	    
	    /*
	     * Set the execution mode to logic and enable those options that only apply for
	     * the symbolic execution mode.
	     */
	    this.logicModeRadioButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (Options.getInst().symbolicMode == false) {
		    		Options.getInst().symbolicMode = true;
	    		}
	    		if (Options.getInst().logicMode == false) {
	    			Options.getInst().logicMode = true;
	    		}
	    		if (Options.getInst().jpaMode == true) {
	    			Options.getInst().jpaMode = false;
	    		}
	    	}
	    });
	    
	    /*
	     * Set the execution mode to logic and enable those options that only apply for
	     * the symbolic execution mode.
	     */
	    this.jpaModeRadioButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (Options.getInst().symbolicMode == false) {
		    		Options.getInst().symbolicMode = true;
	    		}
	    		if (Options.getInst().logicMode == true) {
	    			Options.getInst().logicMode = false;
	    		}
	    		if (Options.getInst().jpaMode == false) {
	    			Options.getInst().jpaMode = true;
	    		}
	    	}
	    });

	    /*
	     * Launch the MethodParametersWindow.
	     */
	    this.methodParametersButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (getSelectedMethod().getPredefinedParameters() == null) {
	    			StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "This method does not have any parameters.");
	    		} else {
		    		FileSelectionComposite.this.shell.setEnabled(false);
		    		try {
			    		@SuppressWarnings("unused")
						MethodParametersWindow methodParametersWindow = new MethodParametersWindow(getThis(), getSelectedMethod());
	    			} catch (Throwable t) {
	    				StaticGuiSupport.processGuiError(t, "method parameter", FileSelectionComposite.this.shell);
	    			}
	    			FileSelectionComposite.this.shell.setFocus();
		    		FileSelectionComposite.this.shell.setEnabled(true);
		    		FileSelectionComposite.this.shell.setActive();
	    		}
	    	}
	    });

	    /*
	     * Currently disabled: Just render a message box.
	     */
	    this.variableValidatorsButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "The feature is currently not implemented.");
	    	}
	    });

	    /*
	     * Start the execution.
	     */
	    this.executeButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		if (getCurrentClassSelected() != null && getSelectedMethod() != null) {
	    			// Cannot execute abstract methods.
	    			if (getSelectedMethod().isAccAbstract()) {
	    				StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Abstract methods cannot be executed.");
	    				return;
	    			}

	    			// Show the step by step or the normal window?
		    		if (getStepByStepCheckButton().getSelection()) {
		    			// Open the window.
			    		try {
				    		StepByStepExecutionWindow stepByStepExecutionWindow = new StepByStepExecutionWindow();
				    		stepByStepExecutionWindow.show(FileSelectionComposite.this.shell, getClassLoader(), getCurrentClass(), getSelectedMethod());
    	    			} catch (Throwable t) {
    	    				StaticGuiSupport.processGuiError(t, "step by step execution", FileSelectionComposite.this.shell);
    	    			}
		    		} else {
		    			// Ask the user if he really wants to continue if the logging level is detailed.
		    			Level level = Globals.getInst().getLoggingLevel();
		    			if (level.toInt() == Priority.DEBUG_INT || level.toInt() == Level.TRACE_INT || level.toInt() == Priority.ALL_INT) {
		    				String message = "You have choosen a detailed log level.\n"
		    					+ "This might slow down the execution and is generally only recommended for debuging purposes or the step by step mode. "
		    					+ "For longer executions it can lead to larger log files.\n\n"
		    					+ "You have chosen the ";
		    				if (level.toInt() == Priority.DEBUG_INT) {
		    					message += "debug level.\n"
		    						+ "The decrease in execution speed should be acceptable. "
		    						+ "However, the log file might grow to hundreds of megabytes if execution will take hours.";
		    				} else if (level.toInt() == Level.TRACE_INT || level.toInt() == Priority.ALL_INT) {
		    					if (level.toInt() == Level.TRACE_INT) {
		    						message += "trace level.\n";
		    					} else if (level.toInt() == Priority.ALL_INT) {
		    						message += "all level.\n";
	    						}
		    					message += "There will be a major decrease in execution speed (up to several magnitudes!). "
		    						+ "The log file is likely to grow to some gigabytes within minutes.";
		    				}
		    				message += "\n\n"
		    					+ "You can change the log level via the menu of the main window. Chose \"Logging\", then \"Logging level\" and pick a level of your choice. In General, \"info\" is the suggested level.\n\n"
		    					+ "Do you really wish to continue?";
		    				if (StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.NO)
		    				{
		    					// Do not open the window.
		    					return;
		    				}
		    			}

		    			// Ask the user if execution should really start if there are not all parameters set.
		    			if (getSelectedMethod().getNumberOfArguments() > getSelectedMethod().getNumberOfDefinedPredefinedParameters() && !Options.getInst().symbolicMode)
		    			{
		    				String message = "The Method expects " + getSelectedMethod().getNumberOfArguments() + " parameters.\n"
		    								+ "Currently only " + getSelectedMethod().getNumberOfDefinedPredefinedParameters() + " have been defined.\n";
		    				if (Options.getInst().assumeMissingValues) {
		    					message += "Missing valued will be initialized to 0 (if numeric) respectively to null.\n\n";
		    				}
		    					message	+= "This will most likely result in an abnormal execution of the program.\n\nDo you really wish to continue?";
		    				if (StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.ICON_QUESTION) == SWT.NO)
		    				{
		    					// Do not open the window.
		    					return;
		    				}
		    			}

		    			// Open the window.
			    		try {
				    		ExecutionWindow executionWindow = new ExecutionWindow();
				    		executionWindow.show(FileSelectionComposite.this.shell, getClassLoader(), getCurrentClass(), getSelectedMethod());
    	    			} catch (Throwable t) {
    	    				StaticGuiSupport.processGuiError(t, "step by step execution", FileSelectionComposite.this.shell);
    	    			}
			    	}
	    			// Just in case something went wrong, set the window visible and active.
		    		if (!FileSelectionComposite.this.shell.isDisposed()) {
		    			FileSelectionComposite.this.shell.setFocus();
		    			FileSelectionComposite.this.shell.setVisible(true);
		    			FileSelectionComposite.this.shell.setActive();
		    		}
	    		}
	    	}
	    });

	    // Load the options.
	    loadOptions();

		// Finish setting up the composite.
		this.pack();

		// And finally browse to the directory of the class path saved.
		if (Options.getInst().classPathEntries.size() > 0) {
			browseTroughTheDirectoryTree(Options.getInst().classPathEntries.get(0), null);
		}
	}

	/**
	 * Load the options and set the widgets accordingly.
	 */
	private void loadOptions() {
		Options options = Options.getInst();
		// Options that are only loaded for the gui.
		this.hideInitCheckButton.setSelection(options.methodListHideInitClinit);
		this.mainOnlyCheckButton.setSelection(options.methodListShowMainMethodOnly);
		if (options.methodListShowMainMethodOnly) {
			this.hideInitCheckButton.setEnabled(false);
		} else {
			this.hideInitCheckButton.setEnabled(true);
		}
		this.stepByStepCheckButton.setSelection(options.executionModeSingleSteps);

		// Symbolic execution mode.
		if (options.symbolicMode) {
			if (options.logicMode) {
				this.logicModeRadioButton.setSelection(true);
			} else if(options.jpaMode) {
				this.jpaModeRadioButton.setSelection(true);
			} else {
				this.symbolicModeRadioButton.setSelection(true);
			}
		} else {
			this.normalModeRadioButton.setSelection(true);
		}
	}

	/**
	 * Getter for the this composite.
	 * @return This FileSelectionComposite.
	 */
	public FileSelectionComposite getThis() {
		return this;
	}

	/**
	 * Getter for the array for the list of files.
	 * @return The fileListArray as an ArrayList of String objects.
	 */
	protected ArrayList<Object[]> getFileListArray() {
		return this.fileListArray;
	}

	/**
	 * Clear the fileListArray.
	 */
	protected void clearFileList() {
		this.fileListArray.clear();
	}

	/**
	 * Add an entry to the fileListArray.
	 * @param filename The filename of the new object (the name of the file only).
	 * @param fullFilename The filename including the full path to it.
	 * @param jarFile The jarFile the file is in. A null value indicates a file that just is on the file system.
	 */
	protected void addToFileList(String filename, String fullFilename, Object jarFile) {
		Object[] object = {filename, fullFilename, jarFile};
		this.fileListArray.add(object);
	}

	/**
	 * Getter for the fileList.
	 * @return The fileList.
	 */
	protected List getFileList() {
		return this.fileList;
	}

	/**
	 * Getter for the methodList.
	 * @return The methodList.
	 */
	protected List getMethodList() {
		return this.methodList;
	}

	/**
	 * Getter for the refreshClassButton.
	 * @return The refreshClassButton.
	 */
	protected Button getRefreshClassButton() {
		return this.refreshClassButton;
	}

	/**
	 * Getter for the current ClassFile.
	 * @return The currentClass.
	 */
	protected ClassFile getCurrentClass() {
		return this.currentClass;
	}

	/**
	 * Getter for the name of the currently selected class.
	 * @return The currentClassSelected as a String.
	 */
	protected String getCurrentClassSelected() {
		return this.currentClassSelected;
	}

	/**
	 * Setter for the currently selected class.
	 * @param className The name of the currently selected class.
	 */
	protected void setCurrentClassSelected(String className) {
		this.currentClassSelected = className;
	}

	/**
	 * Sets the field currentClass to null, indicating it has to be reloaded, even if
	 * the same class would be selected (otherwise reloading of a changed class will
	 * not work properly).
	 */
	protected void resetCurrentClass() {
		this.currentClass = null;
	}

	/**
	 * Load the root directories of the file system into the directory tree,
	 * priorly removing any entries.
	 */
	public void loadRootsIntoDirectoryTree() {
		this.directoryTree.removeAll();

	    File[] roots = File.listRoots();
	    for (int a = 0; a < roots.length; a++) {
			/*
			 * Skip drive A if it is not explicitly wanted - no one needs it nowadays and it slows
			 * initialization down.
			 */
			if (!Options.getInst().hideDrivesAB
					|| (!roots[a].getPath().startsWith("A:") && !roots[a].getPath()
							.startsWith("B:"))) {
				String rootEntry = roots[a].toString();
	    		if (rootEntry.endsWith("\\")) rootEntry = rootEntry.substring(0, rootEntry.length() - 1);
	    		TreeItem root = new TreeItem(this.directoryTree, 0);
		    	root.setText(rootEntry);
		    	root.setData(roots[a]);
		    	new TreeItem(root, 0);
	    	}
	    }
	}

	/**
	 * Process the current class selection, refreshing the method list if desired.
	 * @param selection The selection String.
	 */
	protected void classWasSelected(String[] selection) {
		if (selection != null && selection.length != 0) {
    		String currentClassSelected = selection[0];
    		if (getCurrentClassSelected() == null || !getCurrentClassSelected().equals(currentClassSelected)) {
	    		setCurrentClassSelected(currentClassSelected);
	    		getMethodList().removeAll();
    		}
    		changeFileInspectSelectButtonEnabled(true);
    		changeOptionsEnabled(false);
    	}
		// Show the methods.
		if (showMethods(false)) {
			// Since the methods could be shown, add the ClassFile to the list of recently opened files.
			addClassToRecentFileList();
		} else {
			// Remove the ClassFile of the list of recently opened files.
			if (selection != null && selection.length != 0) {
				String path = "";
	    		TreeItem item = this.directoryTree.getSelection()[0];
	    		while (item != null) {
	    			String addToPath = item.getText();
	    			if (JarFileEntry.isArchive(addToPath)) {
	    				addToPath += "|";
	    			} else {
	    				addToPath += "/";
	    			}
	    			path = addToPath + path;
	    			item = item.getParentItem();
	    		}
	    		// Make sure the entry is removed irrespective of the slashes.
				Options.getInst().recentFilesPaths.remove(path + selection[0]);
				Options.getInst().recentFilesPaths.remove(path.replace("/", "\\") + selection[0]);
			}
		}
	}

	/**
	 * Enable or disable the fileList. If it is disabled, also disable the file inspection and
	 * the file selection button.
	 * @param value A boolean which indicates whether to enable (true), or disable (false) the fileList.
	 */
	protected void changeFileListEnabled(boolean value) {
        this.fileList.setEnabled(value);
        if (!value) {
        	changeFileInspectSelectButtonEnabled(false);
        }
	}

	/**
	 * Enable or disable the the file inspection and the file selection button. If
	 * they are enabled, also enable the fileList. If they are disabled, also disable the
	 * methodList.
	 * @param value A boolean which indicated whether to enable (true), or disable (false) the two button.
	 */
	protected void changeFileInspectSelectButtonEnabled(boolean value) {
		this.fileInspectButton.setEnabled(value);
		this.refreshClassButton.setEnabled(value);
		if (value) {
			changeFileListEnabled(true);
		} else {
			changeMethodListEnabled(false);
		}
	}

	/**
	 * Enable or disable the methodList. If it is enabled, also enable the file inspection button.
	 * If it is disabled, also disable the options.
	 * @param value A boolean which indicates whether to enable (true), or disable (false) the methodList.
	 */
	protected void changeMethodListEnabled(boolean value) {
        this.methodList.setEnabled(value);
        if (value) {
        	changeFileInspectSelectButtonEnabled(true);
        } else {
        	this.methodList.removeAll();
        	changeOptionsEnabled(false);
        }
	}

	/**
	 * Enable or disable the options. If they are enabled, also enable the methodList. Also
	 * invoke the checking of options dependend on the symbolic execution mode.
	 * @param value A boolean which indicates whether to enable (true), or disable (false) the options.
	 */
	protected void changeOptionsEnabled(boolean value) {
        this.modeSelectionGroup.setEnabled(value);
        this.normalModeRadioButton.setEnabled(value);
        this.symbolicModeRadioButton.setEnabled(value);
        this.logicModeRadioButton.setEnabled(value);
        this.jpaModeRadioButton.setEnabled(value);
        this.stepByStepCheckButton.setEnabled(value);
    	this.predefinedDataSelectionGroup.setEnabled(value);
    	this.methodParametersLabel.setEnabled(value);
    	this.methodParametersText.setEnabled(value);
    	this.methodParametersButton.setEnabled(value);
    	this.variableValidatorsLabel.setEnabled(value);
    	this.variableValidatorsText.setEnabled(value);
    	this.variableValidatorsButton.setEnabled(value);
    	this.variableGeneratorsLabel.setEnabled(value);
    	this.variableGeneratorsText.setEnabled(value);
    	this.executeButton.setEnabled(value);
        if (value) changeMethodListEnabled(true);
	}

	/**
	 * Reset the values of the predefined data.
	 */
	protected void resetPredefinedDataValues() {
		resetPredefinedParametersValue();
		resetVariavleValidatorsValue();
		resetVariableGeneratorsValue();
	}

	/**
	 * Update the appropriate widgets with the current number of predefined parameters, generators
	 * and array element generators.
	 */
	public void resetPredefinedParametersValue() {
		Method selectedMethod = getSelectedMethod();
		if (selectedMethod != null) {
			// Updates predefined values.
			Object[] predefinedParameters = selectedMethod.getPredefinedParameters();
			// Find out the number of values not being undefined (null is a value!).
			int valuesNotUndefined = 0;
			if (predefinedParameters != null) {
				for (int a = 0; a < predefinedParameters.length; a++) {
					if (!(predefinedParameters[a] instanceof UndefinedValue)) valuesNotUndefined++;
				}
			}
			this.methodParametersText.setText(String.valueOf(valuesNotUndefined));

			// Update generators.
			int generatorCount = selectedMethod.getGeneratorCount() +  selectedMethod.getArrayElementGeneratorCount();
			this.variableGeneratorsText.setText(String.valueOf(generatorCount));
		}
	}

	/**
	 * Set the number of validators to 0, since this functionality is not yet active.
	 */
	public void resetVariavleValidatorsValue() {
		Method selectedMethod = getSelectedMethod();
		if (selectedMethod != null) {
			this.variableValidatorsText.setText(
				String.valueOf(selectedMethod.getGeneratorCount() + selectedMethod.getArrayElementGeneratorCount()));
		}
	}

	/**
	 * Set the number of generators to 0, since this functionality is not yet active.
	 */
	public void resetVariableGeneratorsValue() {
		this.variableGeneratorsText.setText("0");
	}

	/**
	 * Change the cursors style.
	 * @param style The style as an int, according to the available SWT styles for cursors.
	 */
	protected void changeCursor(int style) {
		this.shell.setCursor(new Cursor(this.display, style));
	}

	/**
	 * Refresh the directory tree.
	 */
	protected void refreshTree() {
		TreeItem[] items = this.directoryTree.getItems();
		for (int a = 0; a < items.length; a++) {
			searchForMoreNodes(items[a]);
		}
		refreshNode(this.directoryTree.getItem(0));
	}

	/**
	 * Recursive support method for the refreshing of the directory tree: Search for
	 * nodes that the current item has as subitems, invoke the recursive search of
	 * them and finally refresh them.
	 * @param item The TreeItem to process.
	 */
	private void searchForMoreNodes(TreeItem item) {
		TreeItem[] items = item.getItems();
		for (int a = 0; a < items.length; a++) {
			// Recursion.
			if (!(items[a].getData() instanceof JarFileEntry))
				searchForMoreNodes(items[a]);
		}
		refreshNode(item);

	}

	/**
	 * Collect the paths of a array of TreeIteam objects and browse their children. This method is
	 * intended to be used recursively.
	 * @param items The array of TreeItem objects
	 * @param pathYet The path to the currently processed items.
	 * @param openedPaths The paths collected yet.
	 * @param closeNodes Close the nodes after visiting them.
	 */
	private void collectPaths(TreeItem[] items, String pathYet, ArrayList<String> openedPaths, boolean closeNodes) {
		for (int a = 0; a < items.length; a++) {
			openedPaths.add(pathYet + "/" + items[a].getText());
			if (items[a].getExpanded() && items[a].getItemCount() > 0)
				collectPaths(items[a].getItems(), pathYet + "/" + items[a].getText(), openedPaths, closeNodes);
			items[a].dispose();
		}
	}

	/**
	 * Support method for the refreshing of the directory tree: refresh a single node.
	 * @param item The TreeItem to process.
	 */
	private void refreshNode(TreeItem item) {
		// Refresh the children if the item is expanded.
		if (item.getExpanded() && item.getItemCount() > 0) {
			TreeItem[] items = item.getItems();
			ArrayList<String> oldItems = new ArrayList<String>();
			ArrayList<File> newItems = new ArrayList<File>();
			for (int a = 0; a < items.length; a++) {
				oldItems.add(items[a].getText());
			}

			Object object = item.getData();
			if (object instanceof File) {
	    		File file = (File) object;
	    		// Directory. 
	    		if (file.isDirectory()) {
		    		File[] files = file.listFiles();
		    		if (files == null) {
		    			item.removeAll();
		    			return;
		    		}

		    		for (int a = 0; a < files.length; a++) {
		    			newItems.add(files[a]);
		    		}
	    		} else if (file.isFile() && JarFileEntry.isArchive(file.getName())) {
	    			// A jar-file cannot be refreshed at this point. It needs special treatment. First record all opened paths.
	    			ArrayList<String> openedPaths = new ArrayList<String>();
	    			collectPaths(item.getItems(), "", openedPaths, true);

	    			// Reopen the jar file.
    				try {
    					JarFile jarfile = new JarFile(file);
    					JarFileEntry jfe = new JarFileEntry(jarfile, "", getFileListArray());
    					jfe.expand(item, true);
        				// Expand all paths that were open.
        				Iterator<String> iterator = openedPaths.iterator();
        				while (iterator.hasNext()) {
        					String path = iterator.next();
        					// Remove the leading slash if there is any.
        					if (path.startsWith("/")) path = path.substring(1);
        					// Open the directory.
        					browseTroughTheDirectoryTree(path, item);
        				}
    				} catch (IOException e) {
    					StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Could not expand the jar-file " + file.getName() + " due to an I/O error.", SWT.OK | SWT.ICON_ERROR);
    				}
    				return;
	    		}
			}

			// Now delete positions from files.
			Iterator<File> iterator = newItems.iterator();
			@SuppressWarnings("unchecked")
			ArrayList<File> clonedNewItems = (ArrayList<File>) newItems.clone();
			while (iterator.hasNext()) {
				File file = iterator.next();
				if (oldItems.contains(file.getName())) {
					oldItems.remove(file.getName());
					clonedNewItems.remove(file);
				}
			}
			newItems = clonedNewItems;

			// Whatever remains has to be updated.
			iterator = newItems.iterator(); // New items.
			while (iterator.hasNext()) {
				File file = iterator.next();
				int index;
				for (index = 0; index < items.length; index++) {
					if (file.getName().compareToIgnoreCase(items[index].getText()) < 0) break;
				}
				if (file.isDirectory()) {
	    			TreeItem itemNew = new TreeItem(item, 0, index);
	    			itemNew.setText(file.getName());
	    			itemNew.setData(file);
	    			// Check if the directory has sub directories or jar-files beneath it.
	    			boolean hasSubEntries = false;
	    			File[] subFiles = file.listFiles();
	    			if (subFiles != null) {
		    			for (int b = 0; b < subFiles.length; b++) {
							if (subFiles[b].isDirectory()
									|| ( subFiles[b].isFile() && JarFileEntry.isArchive(subFiles[b].getName()) )
									) {
		    					hasSubEntries = true;
		    					break;
		    				}

		    			}
	    			}
	    			// Is expanding desired?
	    			if (hasSubEntries) new TreeItem(itemNew, 0);
				} else if (file.isFile()) {
					if (JarFileEntry.isArchive(file.getName())) {
		    			TreeItem itemNew = new TreeItem(item, 0, index);
		    			itemNew.setText(file.getName());
		    			itemNew.setData(file);
		    			new TreeItem(itemNew, 0);
					}
				}
			}

			// Dispose the remaining items, as they do not seem to be on the file system any longer.
			Iterator<String> iterator2 = oldItems.iterator();
			while (iterator2.hasNext()) {
				String itemName = iterator2.next();
				for (int a = 0; a < items.length; a++) {
					if (!items[a].isDisposed() && items[a].getText().equals(itemName)) {
						items[a].dispose();
						break;
					}
				}
			}
		} else if (!item.getExpanded() && item.getItemCount() > 0) {
			// If the item is not expanded but has children, dispose them to free ressources.
			TreeItem[] items = item.getItems();
			for (int a = 0; a < items.length; a++) {
				items[a].dispose();
			}
			new TreeItem(item, 0);
		}

		if (item.getItemCount() == 0) {
			// check if is has got children
			Object object = item.getData();
			if (object instanceof File) {
				File file = (File) object;
				if (file.isDirectory()) {
					boolean hasSubEntries = false;
	    			File[] subFiles = file.listFiles();
	    			if (subFiles != null) {
		    			for (int b = 0; b < subFiles.length; b++) {
		    				if (subFiles[b].isDirectory()
		    						|| JarFileEntry.isArchive(subFiles[b].getName())) {
		    					hasSubEntries = true;
		    					break;
		    				}

		    			}
	    			}
	    			// Is expanding desired?
	    			if (hasSubEntries) {
						new TreeItem(item, 0);
						item.setExpanded(false);
	    			}
				}
			}
		}
	}

	/**
	 * Show the methods of a class file. Render message boxes in case of exceptions, informing
	 * the user about the problem detected.
	 * @param refresh If set to true, the class file will be read and parsed even if it is cached.
	 * @return true, if the methods could be shown, false otherwise.
	 */
	protected boolean showMethods(boolean refresh) {
		String[] selection = getFileList().getSelection();
		if (selection != null && selection.length != 0) {
    		String className = selection[0];
    		if (className != null) {
    			changeCursor(SWT.CURSOR_WAIT);
    			getRefreshClassButton().setEnabled(false);
    			try {
	    			loadMethods(className, refresh);
	    			getMethodList().setEnabled(true);
    			} catch (ClassFileException e) {
    				StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Error", "Could not load class file due to an parsing error.\n\nReason is: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
    				return false;
    			} finally {
        			getRefreshClassButton().setEnabled(true);
        			changeCursor(SWT.CURSOR_ARROW);
    			}
    			return true;
    		}
		}
		return false;
	}

	/**
	 * Load the methods from a classFile. This is done in two steps:
	 * 1. Use the classLoader to get the desired class.
	 * 2. Load the methods from this class.
	 *
	 * If the methods detects that its work would lead to no change (i.e., the methods
	 * of the given class are already loaded) it will do nothing.
	 *
	 * @param className The name of the class to load the Methods from.
	 * @param refresh If set to true, the class file will be read and parsed even if it is cached.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 */
	protected void loadMethods(String className, boolean refresh) throws ClassFileException {
		// If nothing changed and there was no refresh requested, the following can be skipped.
		if (refresh || this.currentClass == null || !(this.currentClassPackage + className).equals(this.currentClass.getName() + ".class") )
		{
			// Check if the method has not changed.
			String selectedMethodSignature = null;
			if (this.currentClass != null && (this.currentClassPackage + className).equals(this.currentClass.getName() + ".class")) {
				String[] selected = this.methodList.getSelection();
				if (selected != null && selected.length > 0)
					selectedMethodSignature = selected[0];
			}

			// 1. Use the classLoader.
			if (className != null) {
				// find the appropriate file first
				Iterator<Object[]> iterator = this.fileListArray.iterator();
				while (iterator.hasNext()) {
					Object[] object = iterator.next();
					if (((String) object[0]).equals(className)) {
						String pkg = this.currentClassPackage == null ? "" : this.currentClassPackage;
						this.currentClass = this.classLoader.getClassAsClassFile(pkg + className, refresh);
						break;
					}
				}
			}

			if (this.currentClass == null) {
				throw new ClassFileException("No classfile has been loaded.");
			}

			// 2. Load the methods.
			this.methodList.removeAll();
			Method[] methods = this.currentClass.getMethods();
			for (int a = 0; a < methods.length; a++) {
				// Skip methods of undesired visibility.
				if (methods[a].isAccPublic()
					|| (this.visibility >= 1 && !methods[a].isAccPublic() && !methods[a].isAccProtected() && !methods[a].isAccPrivate())
					|| (this.visibility >= 2 && !methods[a].isAccPrivate())
					|| this.visibility >= 3) {
					// Skip some methods if this is desired by the user.
					if (!(this.hideInitCheckButton.getSelection() && (methods[a].getName().equals("<init>") || methods[a].getName().equals("<clinit>")))
							&& (!this.mainOnlyCheckButton.getSelection()
							|| (methods[a].getName().equals("main") && methods[a].isAccPublic() && methods[a].isAccStatic() && methods[a].getReturnType().equals("void")))
						) {
						this.methodList.add(methods[a].getFullSignature());
					}
				}
			}

			// Select a particular method?
			boolean methodSelected = false;
			if (selectedMethodSignature != null) {
				String[] items = this.methodList.getItems();
				for (int a = 0; a < items.length; a++) {
					if (selectedMethodSignature.equals(items[a])) {
						this.methodList.setSelection(a);
						methodSelected = true;
						break;
					}
				}
			}

			if (!methodSelected) {
				this.executeButton.setEnabled(false);
			}
		}
	}

	/**
	 * Get the selected method as an instance of Method.
	 * @return The selected Method, or null in cases of no success.
	 */
	protected Method getSelectedMethod() {
		String[] selection = getMethodList().getSelection();
		if (selection != null && selection.length != 0) {
    		String methodName = selection[0];
    		if (this.methodList.getEnabled() && this.currentClass != null) {
				Method[] methods = this.currentClass.getMethods();
				for (int a = 0; a < methods.length; a++) {
						if (methodName.equals(methods[a].getFullSignature())) {
							return methods[a];
						}
				}
			}
    		// There was a selection but no success fetching the method - this is an error!
    		StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Error", "Could not fetch the method informationen.", SWT.OK | SWT.ICON_ERROR);
		}
		// Return null. If there was no selection, no message will have been displays priorly. We just skip.
		return null;
	}

	/**
	 * Expand the directory tree from the given root on.
	 * @param root The TreeItem to start the expansion with.
	 */
	protected void expandDirectoryTree(TreeItem root) {
		TreeItem[] items = root.getItems();
		for (int a = 0; a < items.length; a++) {
			if (items[a].getData() != null) return;
			items[a].dispose();
		}
		Object object = root.getData();
		if (object instanceof File) {
    		File file = (File) object;
    		// Directory. 
    		if (file.isDirectory()) {
	    		File[] files = file.listFiles();
	    		if (files == null) return;
	    		for (int a = 0; a < files.length; a++) {
	    			if (files[a].isDirectory()) {
		    			TreeItem item = new TreeItem(root, 0);
		    			item.setText(files[a].getName());
		    			item.setData(files[a]);
		    			// Check if the directory has sub directories or jar-files beneath it.
		    			boolean hasSubEntries = false;
		    			File[] subFiles = files[a].listFiles();
		    			if (subFiles != null) {
			    			for (int b = 0; b < subFiles.length; b++) {
			    				if (subFiles[b].isDirectory()
			    						|| (subFiles[b].isFile() && 
			    								JarFileEntry.isArchive(subFiles[b].getName()) )) {
			    					hasSubEntries = true;
			    					break;
			    				}

			    			}
		    			}
		    			// Is expanding desired?
		    			if (hasSubEntries) new TreeItem(item, 0);
	    			} else if (files[a].isFile()) {
	    				String filename = files[a].getName();
	    				if (JarFileEntry.isArchive(filename))
	    				{
			    			TreeItem item = new TreeItem(root, 0);
			    			item.setText(filename);
			    			item.setData(files[a]);
			    			new TreeItem(item, 0);
	    				}
	    			}
	    		}
    		} else if (file.isFile()) { // Jar-file.
    			if (JarFileEntry.isArchive(file.getName())) {
    				try {
    					JarFile jarfile = new JarFile(file);
    					JarFileEntry jfe = new JarFileEntry(jarfile, "", getFileListArray());
    					jfe.expand(root, true);
    				} catch (IOException e) {
    					StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Could not expand the jar-file due to an I/O error.", SWT.OK | SWT.ICON_ERROR);
    				}
    			}
    		}
		} else if (object instanceof JarFileEntry) {
			JarFileEntry jfe = (JarFileEntry) object;
			jfe.expand(root, true);
		}
	}

	/**
	 * Open the file dialog for the direct opening of a file. If a file is returned,
	 * expand the directory tree accordingly.
	 */
	public void openFileDirectly() {
		FileDialog fileDialog = new FileDialog(this.shell, SWT.OPEN);
		String[] extensions = {"*.class", "*.jar", "*.war", "*.ear"};
		String[] names = {"Class file (*.class)", "Jar archive (*.jar)", "War archive (*.war)", "Ear archive (*.ear)"};
		fileDialog.setFilterExtensions(extensions);
		fileDialog.setFilterNames(names);
		String path = fileDialog.open();
		if (path != null) {
			// First of all replace double backslashes against slashes.
			path = path.replace("\\\\", "\\");

			// different handling of class and jar files
			if (JarFileEntry.isArchive(path) || (path.length() > 6 && path.substring(path.length() - 6).equals(".class")))
			{
				// Browse through the directory tree.
				browseTroughTheDirectoryTree(path, null);
			} else {
				StaticGuiSupport.showMessageBox(FileSelectionComposite.this.shell, "Information", "This file cannot be processed.", SWT.OK | SWT.ICON_WARNING);
			}
		}
	}

	/**
	 * Browse and expand the directory tree so it shows the just opened file. The
	 * method is invoked recursively until the desired node has been reached.
	 * @param path The path of the file.
	 * @param item The TreeItem that is currently opened.
	 * @return true, if the path could be followed, false otherwise.
	 */
	public boolean browseTroughTheDirectoryTree(String path, TreeItem item) {
		String needToFind = "";
		path = path.replace("\\\\", "\\");
		path = path.replace("\\", "/");
		int slashPos = path.indexOf("/");
		int barPos = path.indexOf("|");

		// Reached the file or final directory?
		if (slashPos == -1 && barPos == -1) {
			// Jar or class file?
			if (JarFileEntry.isArchive(path)) {
				TreeItem[] items = item.getItems();
				// Now the last entry should match this TreeItem.
				for (int a = 0; a < items.length; a++) {
					if (items[a].getText().equals(path)) {
						TreeItem[] selectedItems = {items[a]};
						this.directoryTree.setSelection(selectedItems);
						Event event = new Event();
						event.item = items[a];
						this.directoryTree.notifyListeners(SWT.Selection, event);
						return true;
					}
				}
			} else {
				TreeItem[] selectedItems = {item};
				this.directoryTree.setSelection(selectedItems);
				Event event = new Event();
				event.item = item;
				this.directoryTree.notifyListeners(SWT.Selection, event);

				// Find the index of the just added classes.
				String[] listItems = this.fileList.getItems();
				for (int a = 0; a < listItems.length; a++) {
					if (listItems[a].equals(path)) {
						this.fileList.setSelection(a);
						classWasSelected(this.fileList.getSelection());
						return true;
					}
				}
				this.changeFileInspectSelectButtonEnabled(true);
			}

			// End the recursion, since all required nodes have been opened.
			return false;
		}

		// If the next separator is not a slash, but a bar, handle it the same way.
		if (barPos != -1 && barPos < slashPos) {
			slashPos = barPos;
		}

		// Get the new path and file to find.		
		needToFind = path.substring(0, slashPos); 
		// rafaC: if failed if the path started by /; in this case the empty string ins obtained
		if (needToFind.length()==0)
			needToFind = path.substring(0, slashPos+1); 
			 
		path = path.substring(slashPos + 1);

		// Open tree.
		TreeItem[] items;
		if (item == null) {
			// If the item was null, start at the root level.
			items = this.directoryTree.getItems();
		} else {
			items = item.getItems();
		}
		for (int a = 0; a < items.length; a++) {
			String itemName = items[a].getText();
			itemName = itemName.replace("\\\\", "\\");
			itemName = itemName.replace("\\", "/");
			if (itemName.equals(needToFind)) {
				// Open.
				items[a].setExpanded(true);
				expandDirectoryTree(items[a]);
				// Proceed.
				return browseTroughTheDirectoryTree(path, items[a]);
			}
		}
		return false;
	}

	/**
	 * Get the basic class path and replace the first class path entry
	 * with it.
	 */
	protected void getCurrentClassPath() {
		// Get the current class path.
		if (this.directoryTree.getSelection().length > 0) {
			String newEntry = "";
			TreeItem item = this.directoryTree.getSelection()[0];
			Object data = item.getData();
			if (data instanceof JarFileEntry) {
				// just add the jar file as the class path entry
				JarFileEntry jfe = (JarFileEntry) data;
				JarFile jf = jfe.getJarFile();
				newEntry = jf.getName();
				this.currentClassPackage = jfe.getFileName().replace("/", ".").replace("\\", ".");
			} else if (data instanceof File && ((File) data).isFile()) {
				// It is just a single file, so add the path to it.
				newEntry =  ((File) data).getPath();
				this.currentClassPackage = "";
			} else {
				// Try to find out if we are somewhere in a structure with a root dir "bin". Otherwise just add the current dir.
				String fullPath = "";
				boolean resetClassPackage = true;
				while (item != null) {
					fullPath = item.getText() + "/" + fullPath;
					if (item.getText().toLowerCase().equals("bin")) {
						fullPath = fullPath.replace("/", ".").replace("\\", ".");
						if (fullPath.contains(".")) {
							fullPath = fullPath.substring(fullPath.indexOf(".") + 1);
						}
						this.currentClassPackage = fullPath;
						resetClassPackage = false;
						fullPath = "/bin"; // Forget where this leads to.
					}
					item = item.getParentItem();
				}
				if (resetClassPackage) {
					this.currentClassPackage = "";
				}
				fullPath = fullPath.replace("\\/", "/");
				fullPath = fullPath.replace("\\", "/");
				fullPath = fullPath.replace("//", "/");
				newEntry = fullPath;
				if (!newEntry.substring(newEntry.length() - 1).equals("/")) newEntry += "/";
			}

			// Set the entry.
			if (Options.getInst().classPathEntries.size() == 0) {
				Options.getInst().classPathEntries.add(newEntry);
			} else {
				Options.getInst().classPathEntries.set(0, newEntry);
			}

			// Update the classLoader.
			this.classLoader.updateClassPath(StaticGuiSupport.arrayList2StringArray(Options.getInst().classPathEntries), !Options.getInst().doNotClearClassLoaderCache);
		}
	}

	/**
	 * Getter for the class loader.
	 * @return The current MugglClassLoader.
	 */
	public MugglClassLoader getClassLoader() {
		return this.classLoader;
	}

	/**
	 * Getter for the current class package.
	 * @return The currentClassPackage as a String.
	 */
	protected String getCurrentClassPackage() {
		return this.currentClassPackage;
	}

	/**
	 * Getter for the parent window.
	 * @return The SWTGui.
	 */
	protected SWTGui getParentWindow() {
		return this.parent;
	}

	/**
	 * Getter for the stepByStepCheckButton.
	 * @return The stepByStepCheckButton.
	 */
	public Button getStepByStepCheckButton() {
		return this.stepByStepCheckButton;
	}

	/**
	 * Getter for the hideInitCheckButton.
	 * @return The hideInitCheckButton.
	 */
	public Button getHideInitCheckButton() {
		return this.hideInitCheckButton;
	}

	/**
	 * Getter for the mainOnlyCheckButton.
	 * @return The mainOnlyCheckButton.
	 */
	public Button getMainOnlyCheckButton() {
		return this.mainOnlyCheckButton;
	}

	/**
	 * If there is a current class this method will add it to the list of recently opened
	 * files. Should it be on this list already, it will be removed. If the number of entries
	 * on the list will exceed the maximum, the last entry is dropped.
	 */
	private void addClassToRecentFileList() {
		if (this.currentClass != null) {
			// Get the full path.
			String recentFile = this.currentClass.getFullPath();

			// Check if this file is already referenced. It it is, remove it.
			java.util.List<String> recentFilesPaths = Options.getInst().recentFilesPaths;
			recentFilesPaths.remove(recentFile);

			// Insert the file to the first position.
			recentFilesPaths.add(0, recentFile);

			// Check if the list got too long. Remove the last element in that case.
			if (recentFilesPaths.size() > Options.getInst().numberOfRecentFiles) {
				recentFilesPaths.remove(Options.getInst().numberOfRecentFiles);
			}

			// Write back the entries.
			Options.getInst().recentFilesPaths = recentFilesPaths;
		}
	}

	/**
	 * Change the visibility of the methods displayed.<br />
	 * <br />
	 * Available visibilities are:
	 * <ul>
	 * <ol>public</ol>
	 * <ol>package</ol>
	 * <ol>protected</ol>
	 * <ol>private</ol>
	 * </ul>
	 *
	 * @param visibility The new visibility value.
	 */
	protected void changeVisibility(int visibility) {
		// Check if that visibility is already set.
		if (this.visibility != visibility) {
			this.visibility = visibility;
			try {
				loadMethods(this.currentClassSelected, true);
			} catch (ClassFileException e) {
				// This is really unlikely to happen, since the class is already loaded.
			}
		}
	}

}
