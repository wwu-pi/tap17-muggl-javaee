package de.wwu.muggl.ui.gui.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;
import de.wwu.muggl.symbolic.generating.GenericGenerator;
import de.wwu.muggl.symbolic.generating.GenericGeneratorProvider;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ClassPathEntriesWindow;
import de.wwu.muggl.ui.gui.windows.ShellWindow;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.execution.ExecutionAlgorithms;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The Composite for the GeneratorSelectionWindow. It offers most of its element and the
 * corresponding methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-13
 */
public class GeneratorSelectionComposite extends Composite {
	// General fields for the window.
	protected FileSelectionComposite superParent;
	protected ShellWindow parent;
	protected Shell shell;
	protected Display display;
	protected MugglClassLoader classLoader;

	// Widgets that need to be accesses after opening the window.
	final Text classNameText;
	final Text pathText;
	final Text generatorDescriptionText;
	final Text providerDescriptionText;
	final Button okButton;

	/**
	 * Toggle for the pathText widget.
	 */
	protected volatile boolean togglePathTextListener;

	// Fields for the generator provider and the array elements generator provider.
	private boolean searchingArrayElementsGenerator;
	GeneratorProvider generatorProvider;
	ArrayElementsGeneratorProvider arrayElementsGeneratorProvider;

	/**
	 * Initialize the composite and its elements.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param classLoader The class loader to use.
	 * @param searchingArrayElementsGenerator Toggle whether a variable generator or an array elements generator us being looked for.
	 */
	public GeneratorSelectionComposite(ShellWindow parent, Shell shell, Display display,
			MugglClassLoader classLoader, boolean searchingArrayElementsGenerator) {
		// General initialization
		super(shell, SWT.NONE);
		this.parent = parent;
		this.shell = shell;
		this.display = display;
		this.classLoader = classLoader;
		this.searchingArrayElementsGenerator = searchingArrayElementsGenerator;
		this.togglePathTextListener = true;
		this.generatorProvider = null;
		this.arrayElementsGeneratorProvider = null;

		// Set up layout.
		final GridLayout mainGridLayout = new GridLayout(3, false);
		mainGridLayout.marginHeight = 5;
		mainGridLayout.marginWidth = 5;
		this.setLayout(mainGridLayout);

		// Variable texts.
		String chooseWhat = "";
		if (searchingArrayElementsGenerator) {
			 chooseWhat = "an array elements generator";
		} else {
			 chooseWhat = "a generator";
		}

		// Set up the widgets.
		final GridData trippleSpanGridData = new GridData();
		trippleSpanGridData.horizontalSpan = 3;

		final Label explanationLabel = new Label(this, SWT.NONE);
		explanationLabel.setText("Please choose " + chooseWhat + ":");
		explanationLabel.setLayoutData(trippleSpanGridData);

		final Label classNameLabel = new Label(this, SWT.NONE);
		classNameLabel.setText("Generator class:");

		final GridData width180GridData = new GridData();
		width180GridData.widthHint = 180;

		this.classNameText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		this.classNameText.setText("");
		this.classNameText.setLayoutData(width180GridData);

		final GridData grabHSpaceGridData = new GridData();
		grabHSpaceGridData.horizontalAlignment = SWT.FILL;

		Button recentClassesButton = new Button(this, SWT.PUSH);
		recentClassesButton.setText("&Recently used...");
		recentClassesButton.setLayoutData(grabHSpaceGridData);

		final Label pathLabel = new Label(this, SWT.NONE);
		pathLabel.setText("Path to provider:");

		final GridData width300GridData = new GridData();
		width300GridData.widthHint = 300;

		this.pathText = new Text(this, SWT.BORDER);
		this.pathText.setText("");
		this.pathText.setLayoutData(width300GridData);

		final GridData grabHSpaceGridData2 = new GridData();
		grabHSpaceGridData2.horizontalAlignment = SWT.FILL;

		Button searchButton = new Button(this, SWT.PUSH);
		searchButton.setText("&Search...");
		searchButton.setLayoutData(grabHSpaceGridData2);

		final GridData verticalAlignTopGridData = new GridData();
		verticalAlignTopGridData.verticalAlignment = SWT.TOP;

		final Label generatorDescriptionLabel = new Label(this, SWT.NONE);
		generatorDescriptionLabel.setText("Generator description:");
		generatorDescriptionLabel.setLayoutData(verticalAlignTopGridData);

		final GridData generatorDescriptionGridData = new GridData();
		generatorDescriptionGridData.horizontalSpan = 2;
		generatorDescriptionGridData.heightHint = 120;
		generatorDescriptionGridData.horizontalAlignment = SWT.FILL;

		this.generatorDescriptionText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		this.generatorDescriptionText.setText("");
		this.generatorDescriptionText.setLayoutData(generatorDescriptionGridData);

		final GridData verticalAlignTopGridData2 = new GridData();
		verticalAlignTopGridData2.verticalAlignment = SWT.TOP;

		final Label providerDescriptionLabel = new Label(this, SWT.NONE);
		providerDescriptionLabel.setText("Provider description:");
		providerDescriptionLabel.setLayoutData(verticalAlignTopGridData2);

		final GridData providerDescriptionGridData = new GridData();
		providerDescriptionGridData.horizontalSpan = 2;
		providerDescriptionGridData.heightHint = 120;
		providerDescriptionGridData.horizontalAlignment = SWT.FILL;

		this.providerDescriptionText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		this.providerDescriptionText.setText("");
		this.providerDescriptionText.setLayoutData(generatorDescriptionGridData);

		final GridData trippleSpanGridData2 = new GridData();
		trippleSpanGridData2.horizontalSpan = 3;

		final Link classPathLink = new Link(this, SWT.NONE);
		classPathLink.setText(
				"Please make sure that both the selected generator and the provider are on the <a>class path</a>.");
		classPathLink.setLayoutData(trippleSpanGridData2);

		final GridData width80GridData = new GridData();
		width80GridData.widthHint = 80;

		this.okButton = new Button(this, SWT.PUSH);
		this.okButton.setText("&Ok");
		this.okButton.setLayoutData(width80GridData);
		this.okButton.setEnabled(false);
		this.shell.setDefaultButton(this.okButton);

		final GridData width80GridData2 = new GridData();
		width80GridData2.widthHint = 80;

		final Button cancelButton = new Button(this, SWT.PUSH);
		cancelButton.setText("&Cancel");
		cancelButton.setLayoutData(width80GridData2);

		final Label spacerLabel = new Label(this, SWT.NONE);
		spacerLabel.setVisible(false);

		/*
		 * Choose a recently used generator.
		 */
		recentClassesButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
	    		StaticGuiSupport.showMessageBox(GeneratorSelectionComposite.this.shell,
						"Sorry, but this functionality is not implemented, yet.");
	    	}
	    });

		/*
		 * Detect changes to the path text field and try to use the provided path.
		 */
		this.pathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				if (GeneratorSelectionComposite.this.togglePathTextListener)
					checkGeneratorProviderFile(GeneratorSelectionComposite.this.pathText.getText());
			}
		});

		/*
		 * Search for a generator provider.
		 */
		searchButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
	    		FileDialog fileDialog = new FileDialog(GeneratorSelectionComposite.this.shell, SWT.OPEN);
	    		String[] extensions = {"*.class"};
	    		String[] names = {"Class file (*.class)"};
	    		fileDialog.setFilterExtensions(extensions);
	    		fileDialog.setFilterNames(names);
	    		String path = fileDialog.open();
	    		if (path != null) {

	    			GeneratorSelectionComposite.this.pathText.setText(path);
	    			GeneratorSelectionComposite.this.pathText.setSelection(path.length());
	    		}
	    	}
	    });

		// Show the class path entries window.
		classPathLink.addListener(SWT.Selection, new Listener()  {
			@SuppressWarnings("unchecked")
			public void handleEvent(Event arg0) {
				GeneratorSelectionComposite.this.shell.setEnabled(false);
	    		try {
		    		ClassPathEntriesWindow classPathEntriesWindow = new ClassPathEntriesWindow();
		    		List<String> classPathEntriesTemp =
		    			(List<String>) ((ArrayList<String>) Options.getInst().classPathEntries).clone();

		    		classPathEntriesTemp = classPathEntriesWindow.show(
		    				GeneratorSelectionComposite.this.parent, classPathEntriesTemp);
		    		// Finished successfully?
		    		if (classPathEntriesTemp != null) {
		    			Options.getInst().classPathEntries = classPathEntriesTemp;
						GeneratorSelectionComposite.this.classLoader.updateClassPath(
								StaticGuiSupport.arrayList2StringArray(classPathEntriesTemp),
								!Options.getInst().doNotClearClassLoaderCache);
					}
	    		} catch (Throwable t) {
    				StaticGuiSupport.processGuiError(t, "Class path entries", GeneratorSelectionComposite.this.shell);
    			}
	    		GeneratorSelectionComposite.this.shell.setEnabled(true);
	    		GeneratorSelectionComposite.this.shell.setActive();
			}
		});


		/*
		 * Return to the parameter selection window.
		 */
		this.okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
	    		GeneratorSelectionComposite.this.parent.doExit();
	    	}
	    });

		/*
		 * Cancel the operation and return to the parameter selection window.
		 */
		cancelButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
	    		GeneratorSelectionComposite.this.generatorProvider = null;
	    		GeneratorSelectionComposite.this.arrayElementsGeneratorProvider = null;
	    		GeneratorSelectionComposite.this.parent.doExit();
	    	}
	    });

		// Adding listeners
		this.addKeyListener(new EscKeyListener(parent));
		explanationLabel.addKeyListener(new EscKeyListener(parent));
		classNameLabel.addKeyListener(new EscKeyListener(parent));
		this.classNameText.addKeyListener(new EscKeyListener(parent));
		recentClassesButton.addKeyListener(new EscKeyListener(parent));
		pathLabel.addKeyListener(new EscKeyListener(parent));
		this.pathText.addKeyListener(new EscKeyListener(parent));
		searchButton.addKeyListener(new EscKeyListener(parent));
		generatorDescriptionLabel.addKeyListener(new EscKeyListener(parent));
		this.generatorDescriptionText.addKeyListener(new EscKeyListener(parent));
		classPathLink.addKeyListener(new EscKeyListener(parent));
		this.okButton.addKeyListener(new EscKeyListener(parent));
		cancelButton.addKeyListener(new EscKeyListener(parent));
		spacerLabel.addKeyListener(new EscKeyListener(parent));

		// Finish setting up the composite.
		this.pack();
	}

	/**
	 * Getter for the generator provider.
	 * @return The generator provider, or null if  "Cancel" was pressed.
	 * @throws IllegalStateException If an array elements generator is being looked for.
	 */
	public GeneratorProvider getGeneratorProvider() {
		if (this.searchingArrayElementsGenerator)
			throw new IllegalStateException("An array elements generator is being looked for.");
		return this.generatorProvider;
	}

	/**
	 * Setter for the generator provider.
	 * @param generatorProvider The generator provider, or null to signalize there is no generator selected.
	 * @throws IllegalStateException If an array elements generator is being looked for.
	 */
	public void setGeneratorProvider(GeneratorProvider generatorProvider) {
		if (this.searchingArrayElementsGenerator)
			throw new IllegalStateException("An array elements generator is being looked for.");
		if (generatorProvider != null) {
			// Set it.
			this.generatorProvider = generatorProvider;
			// Load it.
			checkGeneratorProviderFile(generatorProvider.getClass());
		}
	}

	/**
	 * Setter for the array elements generator provider.
	 * @return The array elements  generator provider, or null if  "Cancel" was pressed.
	 * @throws IllegalStateException If a variable generator is being looked for.
	 */
	public ArrayElementsGeneratorProvider getArrayElementsGeneratorProvider() {
		if (!this.searchingArrayElementsGenerator)
			throw new IllegalStateException("A variable generator is being looked for.");
		return this.arrayElementsGeneratorProvider;
	}

	/**
	 * Setter for the array elements generator provider.
	 *
	 * @param arrayElementsGeneratorProvider
	 *            The array elements generator provider, or null to signalize there is no generator selected.
	 * @throws IllegalStateException If a variable generator is being looked for.
	 */
	public void setArrayElementsGeneratorProvider(
			ArrayElementsGeneratorProvider arrayElementsGeneratorProvider) {
		if (!this.searchingArrayElementsGenerator)
			throw new IllegalStateException("A variable generator is being looked for.");
		if (arrayElementsGeneratorProvider != null) {
			// Set it.
			this.arrayElementsGeneratorProvider = arrayElementsGeneratorProvider;
			// Load it.
			checkGeneratorProviderFile(arrayElementsGeneratorProvider.getClass());
		}
	}

	/**
	 * Check if the supplied path point to a generator provider class. If it does so,
	 * analyse it. Should it be feasible, try to get a generator from it. While doing
	 * so add information to the text widgets.
	 *
	 * @param path The path to the generator provider.
	 */
	protected void checkGeneratorProviderFile(String path) {
		// Disable the "ok" button.
		this.okButton.setEnabled(false);

		// Clear the text widgets.
		this.classNameText.setText("");
		this.generatorDescriptionText.setText("");
		this.providerDescriptionText.setText("");

		// Check if the path actually points to a file.
		File file = new File(path);
		// Simply ignore the change if the file does not exist.
		if (!file.exists()) {
			this.classNameText.setText("");
			this.generatorDescriptionText.setText("");
			setProviderLoadingFailureText("No such file.");
			return;
		}

		// Try to parse the file as a ClassFile, if it has not been already loaded.
		ClassFile providerClassFile;
		try {
			String name = ClassFile.readNameFromClassFile(file);
			if (this.classLoader.isClassLoaded(name)) {
				providerClassFile = this.classLoader.getClassAsClassFile(name);
			} else {
				providerClassFile = new ClassFile(this.classLoader, file);
				this.classLoader.insertManuallyLoadedClassfile(providerClassFile);
			}
		} catch (ClassFileException e) {
			setProviderLoadingFailureText("Loading the provider failed with a ClassFileException (" + e.getMessage() + ").");
			return;
		} catch (FileNotFoundException e) {
			setProviderLoadingFailureText("Loading the provider failed with a FileNotFoundException (" + e.getMessage() + ").");
			return;
		} catch (IOException e) {
			setProviderLoadingFailureText("Loading the provider failed with an IOException (" + e.getMessage() + ").");
			return;
		}

		// Continue processing.
		checkGeneratorProviderFile(providerClassFile);
	}

	/**
	 * Try to get the ClassFile for the supplied Class. If that is successful, analyse it. Should it be
	 * feasible, try to get a generator from it. While doing so add information to the text widgets.
	 * Eventually set the path of it to the corresponding text widget.
	 *
	 * @param providerClass The Class of the generator provider.
	 */
	private void checkGeneratorProviderFile(Class<?> providerClass) {
		// Try to get a ClassFile from the Class.
		ClassFile providerClassFile;
		try {
			providerClassFile = this.classLoader.getClassAsClassFile(providerClass.getName());
		} catch (ClassFileException e) {
			setProviderLoadingFailureText("Loading the provider failed with a ClassFileException (" + e.getMessage() + ").");
			return;
		}

		// Set the path.
		this.togglePathTextListener = false;
		this.pathText.setText(providerClassFile.getFullPath());
		this.pathText.setSelection(this.pathText.getText().length());
		this.togglePathTextListener = true;

		// Continue processing.
		checkGeneratorProviderFile(providerClassFile);
	}

	/**
	 * Analyze the supplied generator provider ClassFile. Should it be feasible, try to get a generator from
	 * it. While doing so add information to the text widgets.
	 *
	 * @param providerClassFile The ClassFile of the generator.
	 */
	private void checkGeneratorProviderFile(ClassFile providerClassFile) {
		// Try to get the generator provider.
		GenericGeneratorProvider generatorProvider = tryToLoadGeneratorProvider(providerClassFile);
		if (generatorProvider == null) return;

		// Try to get the generator.
		GenericGenerator generator;
		try {
			if (this.searchingArrayElementsGenerator) {
				generator = ((ArrayElementsGeneratorProvider) generatorProvider).provideInstance("a");
			} else {
				generator = ((GeneratorProvider) generatorProvider).provideInstance("a");
			}
		} catch (Throwable t) {
			setGeneratorLoadingFailureText("Getting a generator from the generator provider failed with an unexpected "
					+ "Throwable being thrown: " + t.getClass().getName() + "(" + t.getMessage() + ").");
			return;
		}
		if (!analyseGenerator(generator)) return;

		/*
		 * If this point has been reached, everything went fine. Put the generator provider to the appropriate
		 * field so it can be returned as the selection's result.
		 */
		this.okButton.setEnabled(true);
		if (this.searchingArrayElementsGenerator) {
			this.arrayElementsGeneratorProvider = (ArrayElementsGeneratorProvider) generatorProvider;
		} else {
			this.generatorProvider = (GeneratorProvider) generatorProvider;
		}
	}

	/**
	 * Try to get the generator provider and analyze if it is feasible of providing a generator.
	 *
	 * @param providerClassFile The ClassFile of the generator provider.
	 * @return The generator provider, or null if case of any problem loading it.
	 */
	private GenericGeneratorProvider tryToLoadGeneratorProvider(ClassFile providerClassFile) {
		// Check if the provider implements the correct interface.
		String providerInterface;
		if (this.searchingArrayElementsGenerator) {
			providerInterface = "de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider";
		} else {
			providerInterface = "de.wwu.muggl.symbolic.generating.GeneratorProvider";
		}

		ClassFile providerInterfaceClassFile;
		try {
			providerInterfaceClassFile = this.classLoader.getClassAsClassFile(providerInterface);
		} catch (ClassFileException e) {
			setProviderLoadingFailureText("Loading the provider's interface failed with a ClassFileException (" + e.getMessage() + ").");
			return null;
		}

		// Found it?
		ExecutionAlgorithms executionAlgorithms = new ExecutionAlgorithms(this.classLoader);
		try {
			if (!executionAlgorithms.implementsInterface(providerClassFile, providerInterfaceClassFile)) {
				// Failure text.
				String text = "The provider class does not implement " + providerInterface + ". "
					+ "It cannot be used to provide a generator.";

				/*
				 * Check if it implements the GenericGenerator interface. A generator might accidently have
				 * been supplied instead of the provider for it.
				 */
				ClassFile generatorInterfaceClassFile;
				String generatorInterface;
				if (this.searchingArrayElementsGenerator) {
					generatorInterface = "de.wwu.muggl.symbolic.generating.ArrayElementsGenerator";
				} else {
					generatorInterface = "de.wwu.muggl.symbolic.generating.Generator";
				}

				try {
					generatorInterfaceClassFile = this.classLoader.getClassAsClassFile(generatorInterface);
					if (executionAlgorithms.implementsInterface(providerClassFile, generatorInterfaceClassFile)) {
						text += "\n\nThe class you supplied is a generator. Generators cannot be set directly. "
							+ "Please set an appropriate provider class for it.";
					}
				} catch (ClassFileException e) {
					generatorInterfaceClassFile = null;
					// Just ignore these exceptions at this place.
				}

				// Show the failure text.
				setProviderLoadingFailureText(text);
				return null;
			}
		} catch (ClassFileException e) {
			setProviderLoadingFailureText("Checking the provider's interface failed with a ClassFileException (" + e.getMessage() + ").");
			return null;
		}

		// Initialize the provider.
		GenericGeneratorProvider generatorProvider;
		Class<?> generatorProviderclass;
		try {
			generatorProviderclass = Class.forName(providerClassFile.getName(), true, this.classLoader);
			generatorProvider = (GenericGeneratorProvider) generatorProviderclass.newInstance();
		} catch (ClassNotFoundException e) {
			setProviderLoadingFailureText("Instantiting the provider failed with a ClassNotFoundException (" + e.getMessage() + ").");
			return null;
		} catch (IllegalAccessException e) {
			setProviderLoadingFailureText("Instantiting the provider failed with an IllegalAccessException (" + e.getMessage() + ").");
			return null;
		} catch (InstantiationException e) {
			setProviderLoadingFailureText("Instantiting the provider failed with an InstantiationException (" + e.getMessage() + ").");
			return null;
		}

		// Add information for the provider.
		Font font = this.providerDescriptionText.getFont();
		FontData[] fontData = font.getFontData();
		String providerDescription = generatorProvider.getDescription();
		if (providerDescription != null && providerDescription.length() > 0) {
			this.providerDescriptionText.setText(providerDescription);
			fontData[0].setStyle(SWT.NORMAL);
		} else {
			this.providerDescriptionText.setText("The provider does not have a description.");
			fontData[0].setStyle(SWT.ITALIC);
		}
		this.providerDescriptionText.setFont(new Font(this.display, fontData));

		// Return the generator provider.
		return generatorProvider;
	}

	/**
	 * Analyse a generator for usability and add its information to the appropriate
	 * text widgets.
	 * @param generator The generator to be analysed.
	 *  @return true, if the generator is usable.
	 */
	private boolean analyseGenerator(GenericGenerator generator) {
		// Has a generator been provided?
		if (generator == null) {
			setGeneratorLoadingFailureText("No generator was provided by the generator provider.");
			return false;
		}

		// Try to parse the file as a ClassFile.
		ClassFile generatorClassFile;
		ClassFile generatorInterfaceClassFile;
		try {
			generatorClassFile = this.classLoader.getClassAsClassFile(generator.getClass().getName());
		} catch (ClassFileException e) {
			setGeneratorLoadingFailureText("Loading the generator failed with a ClassFileException (" + e.getMessage() + ").");
			return false;
		}

		// Check if the provider implements the correct interface.
		String generatorInterface;
		if (this.searchingArrayElementsGenerator) {
			generatorInterface = "de.wwu.muggl.symbolic.generating.ArrayElementsGenerator";
		} else {
			generatorInterface = "de.wwu.muggl.symbolic.generating.Generator";
		}

		try {
			generatorInterfaceClassFile = this.classLoader.getClassAsClassFile(generatorInterface);
		} catch (ClassFileException e) {
			setGeneratorLoadingFailureText("Loading the generators's interface failed with a ClassFileException (" + e.getMessage() + ").");
			return false;
		}

		// Found it?
		ExecutionAlgorithms executionAlgorithms = new ExecutionAlgorithms(this.classLoader);
		try {
			if (!executionAlgorithms.implementsInterface(generatorClassFile, generatorInterfaceClassFile)) {
				setGeneratorLoadingFailureText("The generator class does not implement " + generatorInterface + ". "
						+ "It cannot be used as a generator.");
				return false;
			}
		} catch (ClassFileException e) {
			setGeneratorLoadingFailureText("Checking the generator's interface failed with a ClassFileException (" + e.getMessage() + ").");
			return false;
		}

		// Add information for the generator.
		this.classNameText.setText(generatorClassFile.getClassName());
		Font font = this.generatorDescriptionText.getFont();
		FontData[] fontData = font.getFontData();
		String generatorDescription = generator.getDescription();
		if (generatorDescription != null && generatorDescription.length() > 0) {
			this.generatorDescriptionText.setText(generatorDescription);
			fontData[0].setStyle(SWT.NORMAL);
		} else {
			this.generatorDescriptionText.setText("The generator does not have a description.");
			fontData[0].setStyle(SWT.ITALIC);
		}
		this.generatorDescriptionText.setFont(new Font(this.display, fontData));

		// If we reached this point, everything went fine.
		return true;
	}

	/**
	 * Set the supplied text for the provider description text widget and make its font style
	 * bold.
	 * @param text The text for the provider description text widget.
	 */
	private void setProviderLoadingFailureText(String text) {
		this.providerDescriptionText.setText(text);
		Font font = this.providerDescriptionText.getFont();
		FontData[] fontData = font.getFontData();
		fontData[0].setStyle(SWT.BOLD);
		this.providerDescriptionText.setFont(new Font(this.display, fontData));
	}

	/**
	 * Set the supplied text for the generator description text widget and make its font style
	 * bold.
	 * @param text The text for the generator description text widget.
	 */
	private void setGeneratorLoadingFailureText(String text) {
		this.generatorDescriptionText.setText(text);
		Font font = this.generatorDescriptionText.getFont();
		FontData[] fontData = font.getFontData();
		fontData[0].setStyle(SWT.BOLD);
		this.generatorDescriptionText.setFont(new Font(this.display, fontData));
	}

}
