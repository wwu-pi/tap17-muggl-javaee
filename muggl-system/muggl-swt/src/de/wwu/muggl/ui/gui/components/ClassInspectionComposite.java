package de.wwu.muggl.ui.gui.components;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ClassInspectionWindow;
import de.wwu.muggl.ui.gui.windows.InputWindow;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileWriteAccessViolationException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeAnnotationDefault;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeConstantValue;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeExceptions;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeInnerClasses;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeLineNumberTable;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeLocalVariableTable;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeAnnotationsAbstract;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeParameterAnnotationsAbstract;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSourceFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeUnknownSkipped;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ElementValue;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ElementValueAnnotation;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ElementValueArray;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ElementValuePair;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ExceptionTable;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.InnerClass;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.LineNumberTable;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.LocalVariableTable;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ParameterAnnotation;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantClass;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;

/**
 * The composite for the ClassInspectionWindow. It offers most of its element and the corresponding methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-05
 */
public class ClassInspectionComposite extends Composite {
	// General fields for the window.
	ClassInspectionWindow parent;
	Shell shell;
	Display display;

	// Constant fields for the composites elements.
	private final FormData classInfoFormData;
	private final Tree classInfoTree;
	private final FormData expandAllButtonFormData;
	private final Button expandAllButton;
	private final FormData collapseAllFormData;
	private final Button collapseAllButton;
	private final FormData expandAllBelowFormData;
	private final Button expandAllBelowButton;
	private final FormData collapseAllBelowFormData;
	private final Button collapseAllBelowButton;
	private final FormData searchTextFormData;
	private final Text searchText;
	private final FormData searchButtonFormData;
	private final Button searchButton;
	private final FormData saveAsFormData;
	private final Button saveAsButton;
	private final FormData exitFormData;
	private final Button exitButton;

	// Further fields.
	private String className;
	private ClassFile classFile;
	private int ignoreFirstXHits = 0;
	private int alreadyHit = 0;

	/**
	 * Set up the composite for the ClassInspectionWindow window.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 * @param className The name of the class to inspect.
	 * @param classFile The ClassFile to inspect.
	 */
	public ClassInspectionComposite(ClassInspectionWindow parent, Shell shell, Display display,
			int style, String className, ClassFile classFile) {
		// Basic initialization.
		super(shell, style);
		this.parent = parent;
		this.shell = shell;
		this.display = display;
		this.className = className;
		this.classFile = classFile;

		// Layout
		this.setLayout(new FormLayout());

		// Components
		this.classInfoFormData = new FormData();
		this.classInfoFormData.top = new FormAttachment(this, 5, SWT.BOTTOM);
		this.classInfoFormData.bottom = new FormAttachment(this, 540, SWT.BOTTOM);
		this.classInfoFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.classInfoFormData.right = new FormAttachment(this, 808, SWT.RIGHT);

		this.classInfoTree = new Tree(this, SWT.BORDER);
		this.classInfoTree.setLayoutData(this.classInfoFormData);
		// Increase the font size to 10pt.
		Font font = this.classInfoTree.getFont();
		FontData fontData = font.getFontData()[0];
		fontData.setHeight(10);
		this.classInfoTree.setFont(new Font(this.display, fontData));

		this.expandAllButtonFormData = new FormData();
		this.expandAllButtonFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.expandAllButtonFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.expandAllButtonFormData.left = new FormAttachment(this, 5, SWT.RIGHT);
		this.expandAllButtonFormData.right = new FormAttachment(this, 95, SWT.RIGHT);

		this.expandAllButton = new Button(this, SWT.None);
		this.expandAllButton.setText("&Expand all");
		this.expandAllButton.setLayoutData(this.expandAllButtonFormData);

		this.collapseAllFormData = new FormData();
		this.collapseAllFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.collapseAllFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.collapseAllFormData.left = new FormAttachment(this.expandAllButton, 6, SWT.RIGHT);
		this.collapseAllFormData.right = new FormAttachment(this.expandAllButton, 93, SWT.RIGHT);

		this.collapseAllButton = new Button(this, SWT.None);
		this.collapseAllButton.setText("&Collapse all");
		this.collapseAllButton.setLayoutData(this.collapseAllFormData);

		this.expandAllBelowFormData = new FormData();
		this.expandAllBelowFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.expandAllBelowFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.expandAllBelowFormData.left = new FormAttachment(this.collapseAllButton, 20, SWT.RIGHT);
		this.expandAllBelowFormData.right = new FormAttachment(this.collapseAllButton, 110, SWT.RIGHT);

		this.expandAllBelowButton = new Button(this, SWT.None);
		this.expandAllBelowButton.setText("Expand &all below");
		this.expandAllBelowButton.setLayoutData(this.expandAllBelowFormData);

		this.collapseAllBelowFormData = new FormData();
		this.collapseAllBelowFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.collapseAllBelowFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.collapseAllBelowFormData.left = new FormAttachment(this.expandAllBelowButton, 6, SWT.RIGHT);
		this.collapseAllBelowFormData.right = new FormAttachment(this.expandAllBelowButton, 113, SWT.RIGHT);

		this.collapseAllBelowButton = new Button(this, SWT.None);
		this.collapseAllBelowButton.setText("Collapse all &below");
		this.collapseAllBelowButton.setLayoutData(this.collapseAllBelowFormData);

		this.searchTextFormData = new FormData();
		this.searchTextFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.searchTextFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.searchTextFormData.left = new FormAttachment(this.collapseAllBelowButton, 20, SWT.RIGHT);
		this.searchTextFormData.right = new FormAttachment(this.collapseAllBelowButton, 180, SWT.RIGHT);

		this.searchText = new Text(this, SWT.BORDER);
		this.searchText.setText("");
		this.searchText.setLayoutData(this.searchTextFormData);

		this.searchButtonFormData = new FormData();
		this.searchButtonFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.searchButtonFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.searchButtonFormData.left = new FormAttachment(this.searchText, 5, SWT.RIGHT);
		this.searchButtonFormData.right = new FormAttachment(this.searchText, 75, SWT.RIGHT);

		this.searchButton = new Button(this, SWT.None);
		this.searchButton.setText("&Search...");
		this.searchButton.setLayoutData(this.searchButtonFormData);

		this.saveAsFormData = new FormData();
		this.saveAsFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.saveAsFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.saveAsFormData.left = new FormAttachment(this.searchButton, 20, SWT.RIGHT);
		this.saveAsFormData.right = new FormAttachment(this.searchButton, 85, SWT.RIGHT);

		this.saveAsButton = new Button(this, SWT.None);
		this.saveAsButton.setText("S&ave as...");
		this.saveAsButton.setLayoutData(this.saveAsFormData);

		this.exitFormData = new FormData();
		this.exitFormData.top = new FormAttachment(this.classInfoTree, 5, SWT.BOTTOM);
		this.exitFormData.bottom = new FormAttachment(this.classInfoTree, 30, SWT.BOTTOM);
		this.exitFormData.left = new FormAttachment(this.saveAsButton, 10, SWT.RIGHT);
		this.exitFormData.right = new FormAttachment(this.saveAsButton, 58, SWT.RIGHT);

		this.exitButton = new Button(this, SWT.None);
		this.exitButton.setText("E&xit");
		this.exitButton.setLayoutData(this.exitFormData);

		this.shell.setDefaultButton(this.exitButton);

		// Listener
	    this.addKeyListener(new EscKeyListener(this.parent));
	    this.classInfoTree.addKeyListener(new EscKeyListener(this.parent));
	    this.expandAllButton.addKeyListener(new EscKeyListener(this.parent));
	    this.collapseAllButton.addKeyListener(new EscKeyListener(this.parent));
	    this.expandAllBelowButton.addKeyListener(new EscKeyListener(this.parent));
	    this.collapseAllBelowButton.addKeyListener(new EscKeyListener(this.parent));
	    this.searchText.addKeyListener(new EscKeyListener(this.parent));
	    this.searchButton.addKeyListener(new EscKeyListener(this.parent));
	    this.saveAsButton.addKeyListener(new EscKeyListener(this.parent));
	    this.exitButton.addKeyListener(new EscKeyListener(this.parent));

	    /**
	     * Expand all nodes.
	     */
	    this.expandAllButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		browseAll(true, null);
	    	}
	    });

	    /**
	     * Collapse all nodes.
	     */
	    this.collapseAllButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		browseAll(false, null);
	    	}
	    });

	    /**
	     * Expand all nodes below the currently selected one.
	     */
	    this.expandAllBelowButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		TreeItem item = getCurrentItem();
	    		if (item != null) {
	    			browseAll(item, true, null);
	    		}
	    	}
	    });

	    /**
	     * Collapse all nodes below the currently selected one.
	     */
	    this.collapseAllBelowButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		TreeItem item = getCurrentItem();
	    		if (item != null) {
	    			browseAll(item, false, null);
	    		}
	    	}
	    });

	    /**
	     * Reset the search Button.
	     */
	    this.searchText.addKeyListener(new KeyListener() {
	    	public void keyPressed(KeyEvent event) {
	    		resetSearchButton();
	    	}
	    	public void keyReleased(KeyEvent event) {
	    	}
	    });

	    /**
	     * Search for the entered text.
	     */
	    this.searchButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		searchTree();
	    	}
	    });

	    /**
	     * Save the class file to another file.
	     */
	    this.saveAsButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		saveClassFileAs();
	    	}
	    });

	    /**
	     * Close the Window.
	     */
	    this.exitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		getParentWindow().doExit();
	    	}
	    });

	    // Finally, load the tree.
		loadTree(className, classFile);
	}

	/**
	 * Load the class structure into a Tree.
	 * @param className The name of the class to load.
	 * @param classFile The classFile to load.
	 */
	protected void loadTree(String className, ClassFile classFile) {
		// Get the constantPool, interfaces, fields and methods.
		Constant[] constantPool = classFile.getConstantPool();
		int[] interfaces = classFile.getInterfaces();
    	Field[] fields = classFile.getFields();
		Method[] methods = classFile.getMethods();

		// Initialize the root.
		this.classInfoTree.removeAll();
    	TreeItem root = new TreeItem(this.classInfoTree, 0);
    	root.setText(className);
    	// Level 1: basic class information.
    	TreeItem itemMagic = new TreeItem(root, 0);
    	itemMagic.setText("Magic Number: 0x" + Integer.toHexString(classFile.getMagic()).toUpperCase());
    	TreeItem itemMinorVersion = new TreeItem(root, 0);
    	itemMinorVersion.setText("Minor version: " + classFile.getMinorVersion());
    	TreeItem itemMajorVersion = new TreeItem(root, 0);
    	itemMajorVersion.setText("Major version: " + classFile.getMajorVersion());
    	TreeItem itemConstantPool = new TreeItem(root, 0);
    	itemConstantPool.setText("Constant Pool (" + classFile.getConstantPoolCount() + " entries)");

    	// Level 2: constant_pool entries.
    	TreeItem item = new TreeItem(itemConstantPool, 0);
    	item.setText(StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(0, classFile.getConstantPoolCount(), true) + "(Empty entry)");
    	for (int a = 1; a < classFile.getConstantPoolCount(); a++) {
    		item = new TreeItem(itemConstantPool, 0);
    		item.setText(StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(a, classFile.getConstantPoolCount(), true) + constantPool[a].getStructureName() + ": " + constantPool[a].getStringValue());
    		item.setData(constantPool[a]);
    		int tag = constantPool[a].getTag();
    		switch (tag) {
			case ClassFile.CONSTANT_CLASS:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantClass));
				break;
			case ClassFile.CONSTANT_FIELDREF:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantFieldref));
				break;
			case ClassFile.CONSTANT_METHODREF:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantMethodref));
				break;
			case ClassFile.CONSTANT_INTERFACEMETHODREF:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantInterfaceMethodref));
				break;
			case ClassFile.CONSTANT_STRING:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantString));
				break;
			case ClassFile.CONSTANT_INTEGER:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantInteger));
				break;
			case ClassFile.CONSTANT_FLOAT:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantFloat));
				break;
			case ClassFile.CONSTANT_LONG:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantLong));
				a++; // Count up as it takes two slots.
				break;
			case ClassFile.CONSTANT_DOUBLE:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantDouble));
				a++; // Count up as it takes two slots.
				break;
			case ClassFile.CONSTANT_NAMEANDTYPE:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantNameAndType));
				break;
			case ClassFile.CONSTANT_UTF8:
				item.setBackground(new Color(this.display, Options.getInst().rgbFileInspConstantUtf8));
				break;
    		}
    	}

    	// Level 1: The access flags entry.
    	TreeItem itemAccessFlags = new TreeItem(root, 0);
    	itemAccessFlags.setText("Access flags");
    	// Level 2: single access flags.
    	if (classFile.isAccPublic()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("public");
		}
		if (classFile.isAccFinal()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("final");
		}
		if (classFile.isAccSuper()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("super");
		}
		if (classFile.isAccInterface()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("interface");
		}
		if (classFile.isAccAbstract()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("abstract");
		}
		if (classFile.isAccSynthetic()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("synthetic");
		}
		if (classFile.isAccAnnotation()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("annotation");
		}
		if (classFile.isAccEnum()) {
			item = new TreeItem(itemAccessFlags, 0);
			item.setText("enum");
		}

    	TreeItem itemThisClass = new TreeItem(root, 0);
    	itemThisClass.setText("This class: " + ((ConstantClass) constantPool[classFile.getThisClass()]).getStringValue().replace("/", "."));
    	TreeItem itemSuperClass = new TreeItem(root, 0);
    	if (classFile.getSuperClass() == 0) {
    		itemSuperClass.setText("Super class: (no super class)");
    	} else {
    		itemSuperClass.setText("Super class: " + ((ConstantClass) constantPool[classFile.getSuperClass()]).getStringValue().replace("/", "."));
    	}
    	TreeItem itemInterfaces = new TreeItem(root, 0);
    	itemInterfaces.setText("Interfaces (" + classFile.getInterfacesCount() + " entries)");

    	// Level 2: interfaces.
    	for (int a = 0; a < classFile.getInterfacesCount(); a++) {
    		item = new TreeItem(itemInterfaces, 0);
    		item.setText(StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(a, classFile.getInterfacesCount(), false) + " with constant pool entry #" + StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(interfaces[a], classFile.getConstantPoolCount(), true) + constantPool[interfaces[a]].getStringValue().replace("/", "."));
    	}
    	TreeItem itemFields = new TreeItem(root, 0);

    	// Level 2: fields.
    	itemFields.setText("Fields (" + classFile.getFieldsCount() + " entries)");
    	for (int a = 0; a < classFile.getFieldsCount(); a++) {
    		item = new TreeItem(itemFields, 0);
    		item.setText(a + ": " + fields[a].getPrefix() + fields[a].getType() + " " + fields[a].getName() + " (" + fields[a].getAttributeCount() + " attributes)");
    		// Levels 3, 4 and probably below: attributes.
    		if (fields[a].getAttributeCount() > 0) {
	        	TreeItem itemAttributes = new TreeItem(item, 0);
	        	itemAttributes.setText("Atributes (" + fields[a].getAttributeCount() + " entries)");
	    		addAttributes(itemAttributes, fields[a].getAttributes(), constantPool);
    		}
    	}

    	// Level 2: methods.
    	TreeItem itemMethods = new TreeItem(root, 0);
    	itemMethods.setText("Methods (" + classFile.getMethodsCount() + " entries)");
    	for (int a = 0; a < classFile.getMethodsCount(); a++) {
    		item = new TreeItem(itemMethods, 0);
    		item.setText(a + ": " + methods[a].getPrefix() + methods[a].getReturnType() + " " + methods[a].getName() + "(" + methods[a].getParameterTypesAndNames() + ")" + " with " + methods[a].getAttributeCount() + " attributes");
    		if (methods[a].isAccBridge()) {
	        	TreeItem itemAttributes = new TreeItem(item, 0);
	        	itemAttributes.setText("This method is a bridge.");
    		}
    		// Levels 3, 4 and probably below: attributes.
    		if (methods[a].getAttributeCount() > 0) {
	        	TreeItem itemAttributes = new TreeItem(item, 0);
	        	itemAttributes.setText("Atributes (" + methods[a].getAttributeCount() + " entries)");
	    		addAttributes(itemAttributes, methods[a].getAttributes(), constantPool);
    		}
    	}

    	// Level 3: attributes.
    	TreeItem itemAttributes = new TreeItem(root, 0);
    	itemAttributes.setText("Atributes (" + classFile.getAttributeCount() + " entries)");
    	addAttributes(itemAttributes, classFile.getAttributes(), constantPool);

    	// Add a listener for right clicks.
    	this.classInfoTree.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent arg0) { }

			/**
			 * Take action if the mouse button was pressed. In this case,
			 * the right click (context) menu will be set if it can be offers
			 * for the given context as characterized by the data of the
			 * currently selected node.
			 * @param arg0 The MouseEvent that was generated.
			 */
			public void mouseDown(MouseEvent arg0) {
				// Was the right button released?
				if (arg0.button == 3) {
					// Does the current selection hold any data?
					final TreeItem[] items = getClassInfoTree().getSelection();
					if (items != null && items.length > 0 && items[0] != null && items[0].getData() != null && items[0].getData() instanceof Constant) {
						if (((Constant) items[0].getData()).getTag() != ClassFile.CONSTANT_UTF8) {
							Menu popupMenu = new Menu(ClassInspectionComposite.this.shell, SWT.POP_UP);
							MenuItem popupMenuItem = new MenuItem(popupMenu, SWT.NONE);
							popupMenuItem.setText("(No action is currently available)");
							popupMenuItem.setEnabled(false);
							getClassInfoTree().setMenu(popupMenu);
						} else {
							// Menu.
							Menu popupMenu = new Menu(ClassInspectionComposite.this.shell, SWT.POP_UP);
							MenuItem popupMenuItem = new MenuItem(popupMenu, SWT.NONE);
							popupMenuItem.setText("&Edit...");
							getClassInfoTree().setMenu(popupMenu);

							// Listener and action.
							final ConstantUtf8 constantUtf8 = (ConstantUtf8) items[0].getData();
							/**
							 * Open the InputWindow to change the UTF8.
							 */
							popupMenuItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent event) {
									Charset utf8Charset = Charset.forName("UTF-8");
									String utf8AsString = null;
									utf8AsString = new String(constantUtf8.getBytes(), utf8Charset);
									InputWindow inputWindow = new InputWindow(ClassInspectionComposite.this.shell, getDisplay());
									Object changedString = inputWindow.show(utf8AsString, "java.lang.String");
									// Has the String changed?
									if (changedString != null
											&& !(changedString instanceof UndefinedValue)
											&& !utf8AsString.equals(changedString)) {
										// Convert to UTF8 bytes.
										byte[] bytes = ((String) changedString).getBytes(utf8Charset);
										try {
											// Save to the CONSTANT_Utf8_info.
											constantUtf8.setBytes(bytes);
											// Suggest to reload the tree.
											if (SWT.YES == StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Question", "It is very likely that other structures of the class file use this CONSTANT_Utf8_info. It is highly recommended to reload the class tree.\n\nDo you want to do so now?", SWT.YES | SWT.NO | SWT.ICON_QUESTION)) {
												loadTree(getClassName(), getClassFile());
											} else {
												// Only update the TreeItem.
												String text = items[0].getText();
												text = text.substring(0, text.indexOf("info: ") + 6) + changedString;
												items[0].setText(text);
											}
										} catch (ClassFileWriteAccessViolationException e) {
											StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Warning", "Write access to class files is (currently) not allowed. The changed value has NOT been saved.", SWT.OK | SWT.ICON_WARNING);
										}
									}
								}

								public void widgetDefaultSelected(SelectionEvent e) { }
							});


						}
					}
				}
			}

			/**
			 * Take action if the mouse button was released. In this case,
			 * the menu will be removed, so it will have to be newly set on
			 * the next mouseDown. Only by doing it this way, the application
			 * will also respond to right clicks with the right menu.
			 * @param arg0 The MouseEvent that was generated.
			 */
			public void mouseUp(MouseEvent arg0) {
				// Was the right button released?
				if (arg0.button == 3) {
					// Remove the menu!
					getClassInfoTree().setMenu(null);
				}
			}

    	});
	}

	/**
	 * Add sub items with the supplied attributes to the supplied node.
	 * @param root The TreeItem that is the root node for the new items.
	 * @param attributes The Attribute array to process.
	 * @param constantPool The required constant_pool.
	 */
	private void addAttributes(TreeItem root, Attribute[] attributes, Constant[] constantPool) {
    	for (int a = 0; a < attributes.length; a++) {
    		TreeItem item = new TreeItem(root, 0);
    		String furtherInfos = "";
    		Attribute attribute = attributes[a];
			// Distinguish between the attributes.
    		if (attribute instanceof AttributeConstantValue) {
    			furtherInfos = constantPool[((AttributeConstantValue) attributes[a]).getConstantvalueIndex()].getStringValue();
    		} else if (attribute instanceof AttributeCode) {
    			// The attribute_code has a couple of sub-attributes, especially the code.
    			TreeItem subItem = new TreeItem(item, 0);
    			subItem.setText("max stack: " + ((AttributeCode) attribute).getMaxStack());
    			TreeItem subItem2 = new TreeItem(item, 0);
    			subItem2.setText("max locals: " + ((AttributeCode) attribute).getMaxLocals());
    			TreeItem subItem3 = new TreeItem(item, 0);
    			subItem3.setText("Code");
    			// The code has the instructions as sub-items.
    			try {
	    			Instruction[] instructions = ((AttributeCode) attribute).getInstructions();
	    			subItem3.setText("Code (" + instructions.length + " instructions)");
	    			for (int b = 0; b < instructions.length; b++) {
	    				TreeItem subsubItem = new TreeItem(subItem3, 0);
	    				subsubItem.setText(instructions[b].getNameWithOtherBytes());
	    			}
    			} catch (InvalidInstructionInitialisationException e) {
					StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Error",
							"There was a problem loading instructions: " + e.getMessage(), SWT.OK
									| SWT.ICON_ERROR);
				}
    			TreeItem subItem4 = new TreeItem(item, 0);
    			subItem4.setText("Exception Table");
    			ExceptionTable[] exceptionTable = ((AttributeCode) attribute).getExceptionTable();
    			// Also the exception_table has sub-items.
    			for (int b = 0; b < exceptionTable.length; b++) {
    				TreeItem subsubItem = new TreeItem(subItem4, 0);
    				String catchType = "";
    				if (exceptionTable[b].getCatchType() != 0) {
    					catchType = constantPool[exceptionTable[b].getCatchType()].getStringValue();
    				} else {
    					catchType = "(finally)";
    				}
					subsubItem.setText(exceptionTable[b].getStartPc() + " - "
							+ exceptionTable[b].getEndPc() + " => "
							+ exceptionTable[b].getHandlerPc() + " (" + catchType + ")");
				}
    			TreeItem subItem5 = new TreeItem(item, 0);
    			subItem5.setText("Attributes (" + ((AttributeCode) attribute).getAttributeCount() +  " entries)");
    			addAttributes(subItem5, ((AttributeCode) attribute).getAttributes(), constantPool);
    		} else if (attribute instanceof AttributeExceptions) {
    			furtherInfos = " " + ((AttributeExceptions)  attribute).getNumberOfExceptions() + " exception entries";
    			int[] exceptionIndexTable = ((AttributeExceptions)  attribute).getExceptionIndexTable();
    			// The exception_index_table is added as sub items.
    			for (int b = 0; b < exceptionIndexTable.length; b++) {
    				TreeItem subSubItem = new TreeItem(item, 0);
    				subSubItem.setText(constantPool[exceptionIndexTable[b]].getStringValue());

    			}
    		} else if (attribute instanceof AttributeInnerClasses) {
    			furtherInfos = " (" + ((AttributeInnerClasses) attribute).getNumberOfClasses() + ")";
    			InnerClass[] innerClasses = ((AttributeInnerClasses)  attribute).getClasses();
    			for (int b = 0; b < innerClasses.length; b++) {
    				if (innerClasses[b].getInnerClassInfoIndex() == 0) {
    					TreeItem subItem = new TreeItem(item, 0);
    					subItem.setText("(none)");
    				} else {
    					String innerInfo;
    					String outerInfo;
    					String name;
    					if (innerClasses[b].getInnerClassInfoIndex() == 0) {
    						innerInfo = "(no value)";
    					} else {
    						innerInfo = constantPool[innerClasses[b].getInnerClassInfoIndex()].getStringValue();
    					}
    					if (innerClasses[b].getOuterClassInfoIndex() == 0) {
    						outerInfo = "(no value)";
    					} else {
    						outerInfo = constantPool[innerClasses[b].getOuterClassInfoIndex()].getStringValue();
    					}
    					if (innerClasses[b].getInnerNameIndex() == 0) {
    						name = "(no value)";
    					} else {
    						name = constantPool[innerClasses[b].getInnerNameIndex()].getStringValue();
    					}
    					// The inner_classes information as sub-items.
    					TreeItem subItem = new TreeItem(item, 0);
    					subItem.setText("Inner Class");
    					TreeItem subSubItem = new TreeItem(subItem, 0);
    					subSubItem.setText("Inner info: " + innerInfo);
    					TreeItem subSubItem2 = new TreeItem(subItem, 0);
    					subSubItem2.setText("Outer info: " + outerInfo);
    					TreeItem subSubItem3 = new TreeItem(subItem, 0);
    					subSubItem3.setText("Name: " + name);
    					TreeItem subSubItem4 = new TreeItem(subItem, 0);
    					subSubItem4.setText("Access flags");
    					String[] flags = innerClasses[b].getPrefix().split(" ");
    					for (int c = 0; c < flags.length; c++) {
        					TreeItem subSubSubItem = new TreeItem(subSubItem4, 0);
        					subSubSubItem.setText(flags[c]);
    					}
    				}
    			}
    		} else if (attribute instanceof AttributeSourceFile) {
    			furtherInfos = constantPool[((AttributeSourceFile) attributes[a]).getSourcefileIndex()].getStringValue();
    		} else if (attribute instanceof AttributeLineNumberTable) {
    			furtherInfos = " (" + ((AttributeLineNumberTable) attribute).getLineNumberTableLength() +  " entries)";
    			LineNumberTable[] lineNumberTable = ((AttributeLineNumberTable) attribute).getLineNumberTable();
    			// The attribute_line_number_table as sub-items.
    			for (int b = 0; b < lineNumberTable.length; b++) {
    				TreeItem subItem = new TreeItem(item, 0);
					subItem.setText(lineNumberTable[b].getStartPC() + " - " + lineNumberTable[b].getLineNumber());
    			}
    		} else if (attribute instanceof AttributeLocalVariableTable) {
    			furtherInfos = " (" + ((AttributeLocalVariableTable) attribute).getLocalVariableTableLength() +  " entries)";
    			LocalVariableTable[] localVariableTable = ((AttributeLocalVariableTable) attribute).getLocalVariableTable();
    			for (int b = 0; b < localVariableTable.length; b++) {
    				TreeItem subItem = new TreeItem(item, 0);
					subItem.setText(localVariableTable[b].getStartPc()
							+ " - "
							+ (localVariableTable[b].getStartPc() + localVariableTable[b]
									.getLength())
							+ ": "
							+ constantPool[localVariableTable[b].getNameIndex()].getStringValue()
							+ " "
							+ constantPool[localVariableTable[b].getDescriptorIndex()]
									.getStringValue() + " index is "
							+ localVariableTable[b].getIndex());
				}
    		} else if (attribute instanceof AttributeRuntimeAnnotationsAbstract) {
    			furtherInfos = " (" + ((AttributeRuntimeAnnotationsAbstract) attribute).getNumAnnotations() +  " entries)";
    			Annotation[] annotations = ((AttributeRuntimeAnnotationsAbstract) attribute).getAnnotations();
    			addAnnotation(item, annotations, constantPool);
    		} else if (attribute instanceof AttributeRuntimeParameterAnnotationsAbstract) {
				furtherInfos = " ("
						+ ((AttributeRuntimeParameterAnnotationsAbstract) attribute)
								.getNumParameters() + " entries)";
				ParameterAnnotation[] parameterAnnotations = ((AttributeRuntimeParameterAnnotationsAbstract) attribute)
						.getParameterAnnotations();
				addParameterAnnotation(item, parameterAnnotations, constantPool);
    		} else if (attribute instanceof AttributeAnnotationDefault) {
				furtherInfos = " default value: "
						+ getElementValueText(item, ((AttributeAnnotationDefault) attribute)
								.getDefaultValue(), constantPool);
    		} else if (attribute instanceof AttributeUnknownSkipped) {
				furtherInfos = constantPool[attribute.getAttributeNameIndex()].getStringValue()
						+ " with " + ((AttributeUnknownSkipped) attribute).getBytes().length
						+ " bytes";
			}
    		String text = StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(a, attributes.length, true) + attributes[a].getStructureName();
    		if (furtherInfos.length() > 0) {
    			text += ": " + furtherInfos;
    		}
    		item.setText(text);
    	}
	}
	
	/**
	 * Add annotations as sub items to the supplied node.
	 * 
	 * @param root The TreeItem that is the root node for the new items.
	 * @param annotations The Annotation array to process.
	 * @param constantPool The required constant_pool.
	 */
	private void addAnnotation(TreeItem root, Annotation[] annotations, Constant[] constantPool) {
		for (int a = 0; a < annotations.length; a++) {
			TreeItem item = new TreeItem(root, 0);
			int numElementValuePairs = annotations[a].getNumElementValuePairs();
			item.setText(StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(a,
					annotations.length, true)
					+ constantPool[annotations[a].getTypeIndex()].getStringValue()
					+ " with "
					+ numElementValuePairs + " element value pairs");

			ElementValuePair[] elementValuePairs = annotations[a].getElementValuePairs();
			addElementValuePairs(item, elementValuePairs, constantPool);
		}
	}
	
	/**
	 * Add parameter annotations as sub items to the supplied node.
	 * 
	 * @param root The TreeItem that is the root node for the new items.
	 * @param parameterAnnotations The ParameterAnnotation array to process.
	 * @param constantPool The required constant_pool.
	 */
	private void addParameterAnnotation(TreeItem root, ParameterAnnotation[] parameterAnnotations,
			Constant[] constantPool) {
		for (int a = 0; a < parameterAnnotations.length; a++) {
			TreeItem item = new TreeItem(root, 0);
			int numAnnotations = parameterAnnotations[a].getNumAnnotations();
			item.setText(StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(a,
					parameterAnnotations.length, true)
					+ " with " + numAnnotations + " annotations");
			addAnnotation(item, parameterAnnotations[a].getAnnotations(), constantPool);
		}
	}
		
	/**
	 * Add element values pairs as sub items to the supplied node.
	 * 
	 * @param root The TreeItem that is the root node for the new items.
	 * @param elementValuePairs The ElementValuePair array to process.
	 * @param constantPool The required constant_pool.
	 */
	private void addElementValuePairs(TreeItem root, ElementValuePair[] elementValuePairs, Constant[] constantPool) {
		for (int a = 0; a < elementValuePairs.length; a++) {
			TreeItem item = new TreeItem(root, 0);
			item.setText(StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(a,
					elementValuePairs.length, true)
					+ constantPool[elementValuePairs[a].getElementNameIndex()].getStringValue()
					+ " with type "
					+ getElementValueText(item, elementValuePairs[a].getElementValues(),
							constantPool));
		}
	}
	
	/**
	 * Get a String representation of an element_value and add sub items if necessary.
	 *
	 * @param root The TreeItem that is the root node for the new items.
	 * @param elementValue The ElementValue to process.
	 * @param constantPool The required constant_pool.
	 * @return A String representation of an element_value.
	 */
	private String getElementValueText(TreeItem root, ElementValue elementValue, Constant[] constantPool) {
		switch (elementValue.getTag()) {
			case 'B':
				return "byte and value \"" + elementValue.getStringValue() + "\"";
			case 'C':
				return "char and value \"" + elementValue.getStringValue() + "\"";
			case 'D':
				return  "double and value \"" + elementValue.getStringValue() + "\"";
			case 'F':
				return "float and value \"" + elementValue.getStringValue() + "\"";
			case 'I':
				return "int and value \"" + elementValue.getStringValue() + "\"";
			case 'J':
				return "long and value \"" + elementValue.getStringValue() + "\"";
			case 'S':
				return "short and value \"" + elementValue.getStringValue() + "\"";
			case 'Z':
				return "boolean and value \"" + elementValue.getStringValue() + "\"";
			case 's':
				return "String and value \"" + elementValue.getStringValue() + "\"";
			case 'e':
				return "enum " + elementValue.getStringValue();
			case 'c':
				return "class " + elementValue.getStringValue();
			case '@':
				Annotation annotation = ((ElementValueAnnotation) elementValue).getAnnotation();
				TreeItem item = new TreeItem(root, 0);
				int numElementValuePairs = annotation.getNumElementValuePairs();
				ElementValuePair[] elementValuePairs = annotation.getElementValuePairs();
				addElementValuePairs(item , elementValuePairs, constantPool);
				
				return constantPool[annotation.getTypeIndex()].getStringValue()
				+ " with " + numElementValuePairs + " element value pairs";
			case '[':
				ElementValue[] elementValues = ((ElementValueArray) elementValue).getElementValues();
				for (int a = 0; a < elementValues.length; a++) {
					TreeItem item2 = new TreeItem(root, 0);
					item2.setText(StaticGuiSupport.getFormatedIndexNumberByMaximumNumber(a,
					elementValues.length, true) + " " + getElementValueText(item2, elementValues[a], constantPool));
				}
				return "array of " + ((ElementValueArray) elementValue).getNumValues() + " element values";
		}
		// This cannot be reached for semantic reasons.
		return null;
	}

	/**
	 * Getter for the parent Window.
	 * @return The ClassInspectionWindow
	 */
	protected ClassInspectionWindow getParentWindow() {
		return this.parent;
	}

	/**
	 * Getter for the class info Tree.
	 * @return The class info Tree.
	 */
	protected Tree getClassInfoTree() {
		return this.classInfoTree;
	}

	/**
	 * Get for the name of the class to inspect.
	 * @return The name of the class to inspect.
	 */
	protected String getClassName() {
		return this.className;
	}

	/**
	 * Get for the class to inspect.
	 * @return The ClassFile of the class to inspect
	 */
	protected ClassFile getClassFile() {
		return this.classFile;
	}

	/**
	 * Browse trough all nodes of the tree, trying to find the String that is searched
	 * for (if it is not null).
	 * @param value If set to true, the tree will be expanded. It will be collapsed otherwise.
	 * @param find The String to find. If set to null, the whole tree will be expandes or collapsed.
	 */
	protected void browseAll(boolean value, String find) {
		TreeItem[] root = this.classInfoTree.getItems();
		for (int a = 0; a < root.length; a++) {
			if (!browseAll(root[a], value, find)) {
				String message = "Your search for\n\n" + this.searchText.getText() + "\n\nwas completed without a hit.\n\n"
				+ "Please note that searching is case insensitive and only full phrases will be matched.";
				StaticGuiSupport.showMessageBox(this.shell, message);
				resetSearchButton();
			} else if (find != null) {
				this.searchButton.setText("Find &next...");
				this.ignoreFirstXHits++;
			}
		}
	}

	/**
	 * Browse trough all nodes of the tree beginning at the TreeItem item,
	 * trying to find the String that is searched for (if it is not null).
	 * @param item The TreeItem to start searching at. Only subitems will be searched for.
	 * @param value If set to true, the tree will be expanded. It will be collapsed otherwise.
	 * @param find The String to find. If set to null, the whole tree will be expandes or collapsed.
	 * @return true, if the item was found or value was null, false otherwise.
	 */
	protected boolean browseAll(TreeItem item, boolean value, String find) {
		// find text.
		if (find != null && item.getText().toLowerCase().contains(find)) {
			if (this.ignoreFirstXHits <= this.alreadyHit) {
				TreeItem[] items = {item};
				this.classInfoTree.setSelection(items);
				return true;
			}
			this.alreadyHit++;
		}

		// expand the tree
		if (item.getItemCount() > 0) {
			TreeItem[] items = item.getItems();
			for (int a = 0; a < items.length; a++) {
				// recursion!
				if (browseAll(items[a], value, find)) {
					item.setExpanded(value);
					if (find != null) return true;
				}
			}
		}
		if (find != null) return false;
		return true;
	}

	/**
	 * Search the tree. Before doing so, set the field for skipping the first x found entries to 0.
	 */
	protected void searchTree() {
		this.alreadyHit = 0;
		browseAll(true, this.searchText.getText().toLowerCase());
	}

	/**
	 * Reset the search button as well as the field for skipping the first x found entries.
	 */
	protected void resetSearchButton() {
		this.ignoreFirstXHits = 0;
		this.searchButton.setText("&Search...");
	}

	/**
	 * Get the currently selected item.
	 * @return The currently selected TreeItem.
	 */
	protected TreeItem getCurrentItem() {
		return this.classInfoTree.getSelection()[0];
	}

	/**
	 * Open a file dialog. After the user has chosen the file name to save the class to,
	 * generate an output stream and write the ClassFile.
	 */
	protected void saveClassFileAs() {
		FileDialog fileDialog = new FileDialog(this.shell, SWT.SAVE);
		String[] extensions = {"*.class"};
		String[] names = {"Class file (*.class)"};
		fileDialog.setFilterExtensions(extensions);
		fileDialog.setFilterNames(names);
		String path = fileDialog.open();
		if (path != null) {
			// First of all replace double backslashes against slashes.
			path = path.replace("\\\\", "\\");

			// Check if the file already exists.
			File file = new File(path);
			if (file.isDirectory()) {
				StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Error", "You selected a directory, not a path.", SWT.OK | SWT.ICON_ERROR);
			}

			// Overwrite an existing file?
			if (file.exists()) {
				if (SWT.NO == StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Question", "The file " + path + " already exists.\n\nAre you sure that you want to overwrite it?", SWT.YES | SWT.NO | SWT.ICON_QUESTION)) {
					// Do not continue then!
					return;
				}
			}

			// Check if the file is writable.
			if (file.exists() && !file.canWrite()) {
				StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Error", "The file selected appears to be locked. Please make sure it is not opened by other applications.", SWT.OK | SWT.ICON_ERROR);
				return;
			}

			// Now save the file.
			FileOutputStream fileOutputStream = null;
			DataOutputStream dataOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(file);
				dataOutputStream = new DataOutputStream(fileOutputStream);
				this.classFile.writeClass(dataOutputStream);
				// Update it's full path.
				this.classFile.setFullPath(path);
			} catch (FileNotFoundException e) {
				StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Error", "Could not write to the file selected. The reason is: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			} catch (IOException e) {
				StaticGuiSupport.showMessageBox(ClassInspectionComposite.this.shell, "Error", "Writing to the file selected failed with an I/O exception. The reason is: " + e.getMessage(), SWT.OK | SWT.ICON_ERROR);
			} finally {
				try {
					if (dataOutputStream != null) dataOutputStream.close();
				} catch (IOException e) {
					// Do nothing.
				}
				try {
					if (fileOutputStream != null) fileOutputStream.close();
				} catch (IOException e) {
					// Do nothing.
				}
			}
		}
	}
}
