package de.wwu.muggl.ui.gui.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.symbolic.generating.ArrayElementsGenerator;
import de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider;
import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;
import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.ui.gui.support.ArrayModificationHandler;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ArrayEntriesWindow;
import de.wwu.muggl.ui.gui.windows.GeneratorSelectionWindow;
import de.wwu.muggl.ui.gui.windows.InputWindow;
import de.wwu.muggl.ui.gui.windows.MethodParametersWindow;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.solvers.expressions.Constant;

/**
 * The Composite for the MethodParametersWindow. It offers most of its element and the
 * corresponding methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class MethodParametersComposite extends Composite {
	// General fields for the window.
	FileSelectionComposite superParent;
	MethodParametersWindow parent;
	Shell shell;
	Display display;

	// General fields for the predefined parameters.
	Method method;
	Object[] predefinedParameters;
	GeneratorProvider[] generatorProviders;
	ArrayElementsGeneratorProvider[] arrayElementsGeneratorProviders;
	boolean[] isPrimitiveType;
	boolean[] isEditableType;
	boolean[] isArrayType;

	// Constant fields for the composites elements.
	private final ScrolledComposite parametersScrolledComposite;
	private final Label numberLabel;
	private final Label typeLabel;
	private final Label valueLabel;
	private final Label actionsLabel;
	private final Label[] numberLabels;
	private final StyledText[] typeStyledText;
	private final Text[] valueText;
	private final Button[] editButton;
	private final Button[] setNullButton;
	private final Button[] setUndefinedButton;
	private final Button[] setGeneratorButton;
	private final Button[] setArrayElementsGeneratorButton;
	private final Button saveAndExitButton;
	private final Button discardAndExitButton;

	/**
	 * Initialize the composite and its elements.
	 * @param superParent The composite that invokes the window this composite belongs to.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 * @param method The Method thats parameters should be coped by this composite.
	 */
	public MethodParametersComposite(FileSelectionComposite superParent,
			MethodParametersWindow parent, Shell shell, Display display, int style, Method method) {
		// General initialization
		super(shell, style);
		this.superParent = superParent;
		this.parent = parent;
		this.shell = shell;
		this.display = display;
		this.method = method;

		// Symbolic mode?
		boolean symbolicMode = Options.getInst().symbolicMode;
		int numberOfsymbolicExtraButtons = 0;
		if (symbolicMode) numberOfsymbolicExtraButtons = 2;

		// Get the number of parameters.
		int numberOfParameters = method.getNumberOfArguments();

		// Clone parameters, making it easy to discard changes afterwards.
		this.predefinedParameters = method.getPredefinedParameters().clone();
		this.generatorProviders = new GeneratorProvider[numberOfParameters];
		this.arrayElementsGeneratorProviders = new ArrayElementsGeneratorProvider[numberOfParameters];
		if (method.getGeneratorCount() > 0) {
			for (int a = 0; a < numberOfParameters; a++) {
				GeneratorProvider generatorProvider = method.getGeneratorProvider(a);
				if (generatorProvider != null)
					this.generatorProviders[a] = generatorProvider;
			}
		}
		if (method.getArrayElementGeneratorCount() > 0) {
			for (int a = 0; a < numberOfParameters; a++) {
				ArrayElementsGeneratorProvider generatorProvider = method.getArrayElementsGeneratorProvider(a);
				if (generatorProvider != null)
					this.arrayElementsGeneratorProviders[a] = generatorProvider;
			}
		}
		String typesString = method.getParameterTypes();

		// Set up layout.
		final GridLayout mainGridLayout = new GridLayout(2, false);
		mainGridLayout.marginHeight = 5;
		mainGridLayout.marginWidth = 5;
		this.setLayout(mainGridLayout);

		// Set up scrolled composite.
		final GridData doubleSpanGridData = new GridData();
		doubleSpanGridData.horizontalSpan = 2;

		final Composite heightComposite = new Composite(this, SWT.None);
		heightComposite.setLayoutData(doubleSpanGridData);
		heightComposite.setLayout(new RowLayout());

		this.parametersScrolledComposite = new ScrolledComposite(heightComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		this.parametersScrolledComposite.setLayout(new FillLayout());

		final GridLayout parametersGridLayout = new GridLayout(6 + numberOfsymbolicExtraButtons, false);
		parametersGridLayout.marginHeight = 5;
		parametersGridLayout.marginWidth = 5;

		Composite parametersComposite = new Composite(this.parametersScrolledComposite, SWT.NONE);
		parametersComposite.setLayout(parametersGridLayout);

		// Set up labels.
		this.numberLabel = new Label(parametersComposite, SWT.CENTER);
		this.numberLabel.setText("Parameter");

		this.typeLabel = new Label(parametersComposite, SWT.LEFT);
		this.typeLabel.setText("Type");

		this.valueLabel = new Label(parametersComposite, SWT.LEFT);
		this.valueLabel.setText("Value");

		final GridData quadSpanGridData = new GridData();
		quadSpanGridData.horizontalSpan = 3 + numberOfsymbolicExtraButtons;

		this.actionsLabel = new Label(parametersComposite, SWT.CENTER);
		this.actionsLabel.setText("Actions...");
		this.actionsLabel.setLayoutData(quadSpanGridData);

		// Array initialization.
		this.numberLabels = new Label[numberOfParameters];
		this.typeStyledText = new StyledText[numberOfParameters];
		this.valueText = new Text[numberOfParameters];
		this.editButton = new Button[numberOfParameters];
		this.setNullButton = new Button[numberOfParameters];
		this.setUndefinedButton = new Button[numberOfParameters];
		this.setGeneratorButton = new Button[numberOfParameters];
		this.setArrayElementsGeneratorButton = new Button[numberOfParameters];
		this.isPrimitiveType = new boolean[numberOfParameters];
		this.isEditableType = new boolean[numberOfParameters];
		this.isArrayType = new boolean[numberOfParameters];

		// Building the widgets for the parameters.
		for (int a = 0; a < numberOfParameters; a++) {
			// General initialization
			String typeString = "";
			int nextComma = typesString.indexOf(", ");
			if (nextComma != -1) {
				typeString = typesString.substring(0, nextComma);
				typesString = typesString.substring(nextComma + 2);
			} else {
				typeString = typesString;
			}

			this.isPrimitiveType[a] = false;
			this.isEditableType[a] = false;
			this.isArrayType[a] = false;
			if (typeString.contains("[]")) this.isArrayType[a] = true;
			String shorterTypeString = typeString.replace("[]", "");
			if (shorterTypeString.equals("char") || shorterTypeString.equals("boolean")
					|| shorterTypeString.equals("byte") || shorterTypeString.equals("double")
					|| shorterTypeString.equals("int") || shorterTypeString.equals("float")
					|| shorterTypeString.equals("long") || shorterTypeString.equals("short"))
				this.isPrimitiveType[a] = true;

			if (shorterTypeString.equals("char") || shorterTypeString.equals("java.lang.Character")
					|| shorterTypeString.equals("boolean")
					|| shorterTypeString.equals("java.lang.Boolean")
					|| shorterTypeString.equals("byte")
					|| shorterTypeString.equals("java.lang.Byte")
					|| shorterTypeString.equals("double")
					|| shorterTypeString.equals("java.lang.Double")
					|| shorterTypeString.equals("int")
					|| shorterTypeString.equals("java.lang.Integer")
					|| shorterTypeString.equals("float")
					|| shorterTypeString.equals("java.lang.Float")
					|| shorterTypeString.equals("long")
					|| shorterTypeString.equals("java.lang.Long")
					|| shorterTypeString.equals("short")
					|| shorterTypeString.equals("java.lang.Short")
					|| shorterTypeString.equals("java.lang.String")) this.isEditableType[a] = true;

			// Adding the widgets.
			GridData numberGridData = new GridData();
			numberGridData.horizontalAlignment = SWT.CENTER;

			this.numberLabels[a] = new Label(parametersComposite, SWT.CENTER);
			this.numberLabels[a].setText("#" + String.valueOf(a));
			this.numberLabels[a].setLayoutData(numberGridData);

			this.typeStyledText[a] = new StyledText(parametersComposite, SWT.CENTER);
			this.typeStyledText[a].setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			this.typeStyledText[a].setText(typeString);
			this.typeStyledText[a].setEditable(false);
			if (this.isPrimitiveType[a]) {
				StyleRange styleRange = new StyleRange();
				styleRange.start = 0;
				int nonArrayLength = typeString.indexOf("[");
				if (nonArrayLength != -1) {
					styleRange.length = nonArrayLength;
				} else {
					styleRange.length = typeString.length();
				}
				styleRange.fontStyle = SWT.BOLD;
				styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
				this.typeStyledText[a].setStyleRange(styleRange);
			}

			GridData valueGridData = new GridData();
			valueGridData.minimumWidth = 140;
			valueGridData.widthHint = 240;

			this.valueText[a] = new Text(parametersComposite, SWT.BORDER | SWT.RIGHT);
			this.valueText[a].setEditable(false);
			this.valueText[a].setBackground(new Color(this.display, 255, 255, 255));
			this.valueText[a].setLayoutData(valueGridData);

			GridData editGridData = new GridData();
			editGridData.horizontalAlignment = SWT.FILL;

			this.editButton[a] = new Button(parametersComposite, SWT.None);
			if (this.isEditableType[a]) {
				if (this.isArrayType[a]) {
					this.editButton[a].setText("Edit elements...");
				} else {
					this.editButton[a].setText("Edit value...");
				}
			} else {
				this.editButton[a].setText("Load...");
			}
			this.editButton[a].setLayoutData(editGridData);

			this.setNullButton[a] = new Button(parametersComposite, SWT.None);
			this.setNullButton[a].setText("Set &null");
			if (!this.isArrayType[a] && this.isPrimitiveType[a]) {
				this.setNullButton[a].setEnabled(false);
			}

			this.setUndefinedButton[a] = new Button(parametersComposite, SWT.None);
			this.setUndefinedButton[a].setText("Set &undefined");

			if (symbolicMode) {
				this.setGeneratorButton[a] = new Button(parametersComposite, SWT.None);
				this.setGeneratorButton[a].setText("Set &generator");

				this.setArrayElementsGeneratorButton[a] = new Button(parametersComposite, SWT.None);
				this.setArrayElementsGeneratorButton[a].setText("Set &array elements generator");
				if (!this.isArrayType[a])
					this.setArrayElementsGeneratorButton[a].setEnabled(false);
			}

			// Set data.
			Object[] data = {Integer.valueOf(a), typeString};
			this.editButton[a].setData(data);
			this.setNullButton[a].setData(data);
			this.setUndefinedButton[a].setData(data);
			if (symbolicMode) {
				this.setGeneratorButton[a].setData(data);
				this.setArrayElementsGeneratorButton[a].setData(data);
			}

			// Adding the listener.
			this.numberLabels[a].addKeyListener(new EscKeyListener(parent));
			this.typeStyledText[a].addKeyListener(new EscKeyListener(parent));
			this.valueText[a].addKeyListener(new EscKeyListener(parent));
			this.editButton[a].addKeyListener(new EscKeyListener(parent));
			this.setNullButton[a].addKeyListener(new EscKeyListener(parent));
			this.setUndefinedButton[a].addKeyListener(new EscKeyListener(parent));
			if (symbolicMode) {
				this.setGeneratorButton[a].addKeyListener(new EscKeyListener(parent));
				this.setArrayElementsGeneratorButton[a].addKeyListener(new EscKeyListener(parent));
			}

			/**
			 * With the edit button a small window can be opened to alter the value of a parameter.
			 */
			this.editButton[a].addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
		    		int index = (Integer) ((Object[]) ((Button) event.widget).getData())[0];
		    		if (!MethodParametersComposite.this.isEditableType[index]) {
		    			StaticGuiSupport.showMessageBox(MethodParametersComposite.this.shell, "Information", "This feature is not implemented or enabled in this version.", SWT.OK | SWT.ICON_INFORMATION);
		    		} else if (MethodParametersComposite.this.isArrayType[index]) {
		    			MethodParametersComposite.this.parent.getShell().setEnabled(false);

		    			// Get the array reference.
		    			Object[] array;
		    			Object object = MethodParametersComposite.this.predefinedParameters[index];
		    			try {
		    				if (object != null) {
		    					array = ((Object[]) object).clone();
		    				} else {
		    					array = null;
		    				}
		    			} catch (ClassCastException e) {
		    				array = null;
		    			}

		    			// Cut the last [] from the type String.
		    			String typeString = (String) ((Object[]) ((Button) event.widget).getData())[1];
		    			typeString = typeString.substring(0, typeString.length() - 2);


		    			try {
		    				ArrayModificationHandler arrayModificationHandler = new ArrayModificationHandler(array, typeString, MethodParametersComposite.this.isPrimitiveType[index]);
			    			try {
			    				ArrayEntriesWindow arrayEntriesWindows = new ArrayEntriesWindow();
			    				arrayEntriesWindows.show(MethodParametersComposite.this.parent, arrayModificationHandler, 0, null);
			    				if (arrayModificationHandler.hasAReturnValue()) {
			    					// Save the data!
			    					setPredefinedParameter(index, arrayModificationHandler.getFinishedArray());
			    				}
		   	    			} catch (Throwable t) {
	    	    				StaticGuiSupport.processGuiError(t, "method parameter", MethodParametersComposite.this.shell);
	    	    			}
			    		} catch (GUIException e) {
			    			String message;
			    			if (e.getMessage().endsWith("Type cannot be processed.")) {
			    				message = "The type " + typeString + " is not suitable for the inspection and replacement of its entries.";
			    			} else {
			    				message = "The type " + typeString + " cannot be processed. The root cause is: " + e.getMessage();
			    			}
			    			StaticGuiSupport.showMessageBox(MethodParametersComposite.this.shell, "Error", message, SWT.OK | SWT.ICON_ERROR);
			    		}

			    		MethodParametersComposite.this.parent.getShell().setEnabled(true);
			    		MethodParametersComposite.this.parent.getShell().setActive();
			    		MethodParametersComposite.this.parent.getShell().setActive();
		    			getSaveAndExitButton().setFocus();
		    		} else {
		    			MethodParametersComposite.this.parent.getShell().setEnabled(false);

		    			try {
		    				InputWindow inputWindow = new InputWindow(MethodParametersComposite.this.shell, getDisplay());
			    			Object object = inputWindow.show(MethodParametersComposite.this.predefinedParameters[index], (String) ((Object[]) ((Button) event.widget).getData())[1]);
			    			if (!(object instanceof UndefinedValue))
			    				setPredefinedParameter(index, object);
	   	    			} catch (Throwable t) {
    	    				StaticGuiSupport.processGuiError(t, "method parameter", MethodParametersComposite.this.shell);
    	    			}

	   	    			MethodParametersComposite.this.parent.getShell().setEnabled(true);
	   	    			MethodParametersComposite.this.parent.getShell().setActive();
	   	    			MethodParametersComposite.this.parent.getShell().setActive();
		    			getSaveAndExitButton().setFocus();
		    		}
		    	}
		    });

			/**
			 * The null button can be used to set a parameter to null.
			 */
			this.setNullButton[a].addListener(SWT.Selection, new Listener() {
		    	public void handleEvent(Event event) {
		    		int index = (Integer) ((Object[]) ((Button) event.widget).getData())[0];
		    		setPredefinedParameter(index, null);
		    	}
		    });

			/**
			 * The undefine button can be used to set a parameter to an instance of UndefinedValue, thus
			 * marking it as having no value at all.
			 */
			this.setUndefinedButton[a].addListener(SWT.Selection, new Listener() {
		    	public void handleEvent(Event event) {
		    		int index = (Integer) ((Object[]) ((Button) event.widget).getData())[0];
		    		setPredefinedParameter(index, new UndefinedValue());
		    	}
		    });

			if (symbolicMode) {
				/**
				 * Let this parameter be created by a variable generator.
				 */
				this.setGeneratorButton[a].addListener(SWT.Selection, new Listener() {
			    	public void handleEvent(Event event) {
			    		// Get the currently set generator provider.
			    		int index = (Integer) ((Object[]) ((Button) event.widget).getData())[0];
			    		GeneratorProvider generatorProvider =
			    			MethodParametersComposite.this.generatorProviders[index];

			    		// Show the window and get the new generator provider.
			    		MethodParametersWindow parentWindow = MethodParametersComposite.this.parent;
			    		FileSelectionComposite superParent = MethodParametersComposite.this.superParent;
			    		GeneratorSelectionWindow selectionWindow =
			    			new GeneratorSelectionWindow(parentWindow, superParent.classLoader);
			    		generatorProvider = selectionWindow.showAndProvideGeneratorProvider(generatorProvider);

			    		// Set the generator provider if a value was returned.
			    		if (generatorProvider != null)
			    			setGeneratorProvider(index, generatorProvider);
			    	}
			    });

				/**
				 * Let the elements of this array expected as the parameter be created by a
				 * array elements generator.
				 */
				this.setArrayElementsGeneratorButton[a].addListener(SWT.Selection, new Listener() {
			    	public void handleEvent(Event event) {
			    		// Get the currently set array elements generator provider.
			    		int index = (Integer) ((Object[]) ((Button) event.widget).getData())[0];
			    		ArrayElementsGeneratorProvider generatorProvider =
			    			MethodParametersComposite.this.arrayElementsGeneratorProviders[index];

			    		// Show the window and get the new array elements generator provider.
			    		MethodParametersWindow parentWindow = MethodParametersComposite.this.parent;
			    		FileSelectionComposite superParent = MethodParametersComposite.this.superParent;
			    		GeneratorSelectionWindow selectionWindow =
			    			new GeneratorSelectionWindow(parentWindow, superParent.classLoader);
			    		generatorProvider = selectionWindow.showAndProvideGeneratorProvider(generatorProvider);

			    		// Set the array elements generator provider if a value was returned.
			    		if (generatorProvider != null)
			    			setArrayElementsGeneratorProvider(index, generatorProvider);
			    	}
			    });
			}
		}

		// Buttons for ok and cancel.
		this.saveAndExitButton = new Button(this, SWT.None);
		this.saveAndExitButton.setText("Save and exit");

		this.discardAndExitButton = new Button(this, SWT.None);
		this.discardAndExitButton.setText("Discard and exit");

		this.shell.setDefaultButton(this.saveAndExitButton);
		this.saveAndExitButton.setFocus();

		// Pack the parameter's composite and make sure the window will not be too high.
		parametersComposite.pack();
		int maxWidth = this.display.getBounds().width - 180;
		int maxHeight = this.display.getBounds().height - 180;
		Rectangle compositeBounds = parametersComposite.getBounds();
		final RowData maxHeightRowData = new RowData();
		if (compositeBounds.width > maxWidth) {
			maxHeightRowData.width = maxWidth;
		} else {
			maxHeightRowData.width = compositeBounds.width;
		}
		if (compositeBounds.height > maxHeight) {
			maxHeightRowData.height = maxHeight;
		} else {
			maxHeightRowData.height = compositeBounds.height;
		}
		this.parametersScrolledComposite.setLayoutData(maxHeightRowData);

		// Prepare scrolling.
		this.parametersScrolledComposite.setContent(parametersComposite);
		this.parametersScrolledComposite.setExpandHorizontal(true);
		this.parametersScrolledComposite.setExpandVertical(true);
		this.parametersScrolledComposite.setMinSize(parametersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		this.parametersScrolledComposite.pack();

		// Adding listeners
		this.addKeyListener(new EscKeyListener(parent));
		this.parametersScrolledComposite.addKeyListener(new EscKeyListener(parent));
		parametersComposite.addKeyListener(new EscKeyListener(parent));
		this.numberLabel.addKeyListener(new EscKeyListener(parent));
		this.typeLabel.addKeyListener(new EscKeyListener(parent));
		this.valueLabel.addKeyListener(new EscKeyListener(parent));
		this.actionsLabel.addKeyListener(new EscKeyListener(parent));
		this.saveAndExitButton.addKeyListener(new EscKeyListener(parent));
		this.discardAndExitButton.addKeyListener(new EscKeyListener(parent));

		/*
		 * Write back the parameters to the Method and close the window.
		 */
	    this.saveAndExitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		// Save the predefined parameters to the method.
	    		MethodParametersComposite.this.method.setPredefinedParameters(MethodParametersComposite.this.predefinedParameters);

	    		// Save the generators to the method.
	    		for (int a = 0; a < MethodParametersComposite.this.generatorProviders.length; a++)
				{
	    			MethodParametersComposite.this.method.setGeneratorProvider(a,
							MethodParametersComposite.this.generatorProviders[a]);
				}

	    		// Save the arary elements generators to the method.
	    		for (int a = 0; a < MethodParametersComposite.this.arrayElementsGeneratorProviders.length; a++)
				{
	    			MethodParametersComposite.this.method.setArrayElementsGeneratorProvider(a,
							MethodParametersComposite.this.arrayElementsGeneratorProviders[a]);
				}


	    		// Trigger the reset of the displayed number of predefined variables.
	    		getSuperParent().resetPredefinedParametersValue();

	    		// Close the window.
	    		getParentWindow().doExit();
	    	}
	    });

	    /*
	     * Just close the window, discarding any changes.
	     */
	    this.discardAndExitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		getParentWindow().doExit();
	    	}
	    });

		// Finish setting up the composite.
		this.pack();

		// Load Values.
		loadValues();
	}

	/**
	 * Load the parameters values.
	 */
	protected void loadValues() {
		loadValues(0, this.predefinedParameters.length);
	}

	/**
	 * Load the parameters values, starting at parameter #from, finishing at #to - 1 inclusively.
	 * @param from The first parameter to load its value from.
	 * @param to The parameter after the last parameter to load its value from.
	 */
	protected void loadValues(int from, int to) {
		for (int a = from; a < to; a++) {
			Font font = this.valueText[a].getFont();
			FontData[] fontData = font.getFontData();
			// Is there a predefined value, a generator or an array element generator for this parameter?
			if (this.predefinedParameters[a] != null || this.generatorProviders[a] != null
				|| this.arrayElementsGeneratorProviders[a] != null) {
				// Is there a generator provider for this parameter?
				if (this.generatorProviders[a] != null) {
					String text;
					Color color;

					// Check if the Generator can be provided.
					try {
						Generator generator = this.generatorProviders[a].provideInstance("a");
						// Providing successul?
						if (generator != null) {
							text = generator.getName();
							color = this.display.getSystemColor(SWT.COLOR_DARK_GREEN);
							fontData[0].setStyle(SWT.NORMAL);
						} else {
							text = "No generator provided!";
							color = this.display.getSystemColor(SWT.COLOR_RED);
							fontData[0].setStyle(SWT.BOLD);
						}
					// Catch anything the generator provider may throw.
					} catch (Throwable t) {
						text = "Generator provider threw " + t.getClass().getName() + "!";
						color = this.display.getSystemColor(SWT.COLOR_RED);
						fontData[0].setStyle(SWT.BOLD);
					}

					// Set text and color.
					this.valueText[a].setText(text);
					this.valueText[a].setForeground(color);
				// Is there an array element generator providers for this parameter?
				} else if (this.arrayElementsGeneratorProviders[a] != null) {
					String text;
					Color color;

					// Check if the ArrayElementsGenerator can be provided.
					try {
						ArrayElementsGenerator generator =
							this.arrayElementsGeneratorProviders[a].provideInstance("a");
						// Providing successull?
						if (generator != null) {
							text = generator.getName();
							color = this.display.getSystemColor(SWT.COLOR_DARK_RED);
							fontData[0].setStyle(SWT.NORMAL);
						} else {
							text = "No generator provided!";
							color = this.display.getSystemColor(SWT.COLOR_RED);
							fontData[0].setStyle(SWT.BOLD);
						}
					// Catch anything the generator provider may throw.
					} catch (Throwable t) {
						text = "Generator provider threw " + t.getClass().getName() + "!";
						color = this.display.getSystemColor(SWT.COLOR_RED);
						fontData[0].setStyle(SWT.BOLD);
					}

					// Set text and color.
					this.valueText[a].setText(text);
					this.valueText[a].setForeground(color);
				} else {
					// Is it an undefined value?
					if (this.predefinedParameters[a] instanceof UndefinedValue) {
						this.valueText[a].setText("undefined");
						fontData[0].setStyle(SWT.BOLD | SWT.ITALIC);
					// Is it an array?
					} else if (this.isArrayType[a]) {
						String text = "Array with dimension";
						Object[] arrayObject = (Object[]) this.predefinedParameters[a];
						if (arrayObject.length > 0 && arrayObject[0] != null && arrayObject[0].getClass().isArray()) {
							text += "s";
						}
						text += " [" + arrayObject.length + "]";
						while (arrayObject.length > 0 && arrayObject[0] != null && arrayObject[0].getClass().isArray())
						{
							arrayObject = (Object[]) arrayObject[0];
							text += "[" + arrayObject.length + "]";
						}
						this.valueText[a].setText(text);
						this.valueText[a].setForeground(this.display.getSystemColor(SWT.COLOR_BLUE));
					// Is it a constant?
					} else if (this.predefinedParameters[a] instanceof Constant) {
						this.valueText[a].setText(((Constant) this.predefinedParameters[a]).toString() + " (symbolic)");
						fontData[0].setStyle(SWT.NORMAL);
					} else {
						// Any other value will just have its toString() method used.
						this.valueText[a].setText(this.predefinedParameters[a].toString());
						fontData[0].setStyle(SWT.NORMAL);
					}
				}
			} else {
				this.valueText[a].setText("null");
				fontData[0].setStyle(SWT.BOLD);
				this.valueText[a].setForeground(this.display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
			}
			this.valueText[a].setFont(new Font(this.display, fontData));
		}
	}

	/**
	 * Getter for the super parent composite.
	 * @return The FileSelectionComposite.
	 */
	protected FileSelectionComposite getSuperParent() {
		return this.superParent;
	}

	/**
	 * Getter for the parent window.
	 * @return The parent MethodParametersWindow.
	 */
	protected MethodParametersWindow getParentWindow() {
		return this.parent;
	}

	/**
	 * Set a predefined parameter to a new value. After doing so, the textual
	 * representation of it is refreshed. This method also makes sure any generator
	 * or array element generator set for the parameter is removed.
	 * @param index The index of the parameter to replace.
	 * @param object The new object that will be taken as a parameter.
	 */
	protected void setPredefinedParameter(int index, Object object) {
		this.predefinedParameters[index] = object;
		this.generatorProviders[index] = null;
		this.arrayElementsGeneratorProviders[index] = null;
		loadValues(index, index + 1);
	}

	/**
	 * Set a parameter to be generated by an variable generator provided by the supplied.
	 * generator provider.
	 *
	 * After doing so, the textual representation of it is refreshed. This method also
	 * makes sure any predefined value or variable generator set for the parameter is
	 * removed.
	 *
	 * @param index The index of the parameter to replace.
	 * @param generatorProvider The generator provider that will generate values for this parameter.
	 */
	protected void setGeneratorProvider(int index, GeneratorProvider generatorProvider) {
		this.predefinedParameters[index] = new UndefinedValue();
		this.generatorProviders[index] = generatorProvider;
		this.arrayElementsGeneratorProviders[index] = null;
		loadValues(index, index + 1);
	}

	/**
	 * Set a parameter to be generated by the built-in array generator which will have
	 * its values generated by the generator provided by the supplied array elements
	 * generator provider.
	 *
	 * After doing so, the textual representation of it is refreshed. This method also
	 * makes sure any predefined value or variable generator set for the parameter is
	 * removed.
	 *
	 * @param index The index of the parameter to replace.
	 * @param generatorProvider The generator provider that will generate the array elements.
	 */
	protected void setArrayElementsGeneratorProvider(int index, ArrayElementsGeneratorProvider generatorProvider) {
		this.predefinedParameters[index] = new UndefinedValue();
		this.generatorProviders[index] = null;
		this.arrayElementsGeneratorProviders[index] = generatorProvider;
		loadValues(index, index + 1);
	}

	/**
	 * Getter for the the Display.
	 * @return The current Display.
	 */
	@Override
	public Display getDisplay() {
		return this.display;
	}

	/**
	 * Getter for the this composite.
	 * @return This MethodParametersComposite.
	 */
	protected MethodParametersComposite getThis() {
		return this;
	}

	/**
	 * Getter for the saveAndExitButton.
	 * @return The Button saveAndExitButton.
	 */
	protected Button getSaveAndExitButton() {
		return this.saveAndExitButton;
	}
}
