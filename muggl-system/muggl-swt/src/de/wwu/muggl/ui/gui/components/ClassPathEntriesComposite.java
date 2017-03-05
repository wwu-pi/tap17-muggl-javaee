package de.wwu.muggl.ui.gui.components;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ClassPathEntriesWindow;

/**
 * The composite for the ClassPathEntriesWindow. It offers most of its element and the corresponding
 * methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class ClassPathEntriesComposite extends Composite {
	// Constants for minimum and maximum dimensions.
	private static final int MIN_WIDTH_ENTRY_TEXT = 340;
	private static final int MAX_WIDTH_ENTRY_TEXT = 460;
	private static final int MAX_HEIGHT_SCROLLGROUP = 450;

	// General fields for the window.
	ClassPathEntriesWindow  parent;
	Shell shell;
	Display display;

	// Fields for data that can be altered using the functionality of this composite.
	List<String> classPathEntries;

	// Constant fields for the composites elements.
	private final GridData scrollGroupGridData;
	private final ScrolledComposite scrollGroup;
	private final Composite mainGroup;
	private final Label numberLabel;
	private final Label entryLabel;
	private final Label aktionLabel;
	private Label[] numberLabels;
	private Text[] entryText;
	private Button[] aktionButton;
	private final Button saveAndExitButton;
	private final Button discardAndExitButton;

	/**
	 * Set up the composite for the ClassPathEntriesWindow window.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 * @param classPathEntries The class path entries.
	 */
	public ClassPathEntriesComposite(
			ClassPathEntriesWindow parent,
			Shell shell,
			Display display,
			int style,
			List<String> classPathEntries
			) {
		// General initialization.
		super(shell, style);
		this.parent = parent;
		this.shell = shell;
		this.display = display;
		this.classPathEntries = classPathEntries;

		// GridData for spanning several columns.
		this.scrollGroupGridData = new GridData();
		this.scrollGroupGridData.horizontalSpan = 3;

		// GridData for width hints.
		final GridData widthHint80 = new GridData();
		widthHint80.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_SHORT;

		final GridData widthHint100 = new GridData();
		widthHint100.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_SHORT;

		final GridData widthHintEntryText = new GridData();
		widthHintEntryText.widthHint = MIN_WIDTH_ENTRY_TEXT;

		// Layout
		GridLayout compositeGridLayout = new GridLayout(3, false);
		this.setLayout(compositeGridLayout);

		// Components.
		this.numberLabel = new Label(this, SWT.CENTER);
		this.numberLabel.setText("Parameter");
		this.numberLabel.setLayoutData(widthHint80);

		this.entryLabel = new Label(this, SWT.CENTER);
		this.entryLabel.setText("Entry");
        this.entryLabel.setLayoutData(widthHintEntryText);

		this.aktionLabel = new Label(this, SWT.CENTER);
		this.aktionLabel.setText("Action");
		this.aktionLabel.setLayoutData(widthHint100);

		this.scrollGroup = new ScrolledComposite(this, SWT.V_SCROLL);
		this.scrollGroup.setLayoutData(this.scrollGroupGridData);

		this.mainGroup = new Composite(this.scrollGroup, SWT.NONE);

		// Array null initialization.
		this.numberLabels = null;
		this.entryText = null;
		this.aktionButton = null;

		// Draw main group.
		buildEntryGroup();

		// Buttons for ok and cancel.
		this.saveAndExitButton = new Button(this, SWT.None);
		this.saveAndExitButton.setText("Save and exit");

		this.discardAndExitButton = new Button(this, SWT.None);
		this.discardAndExitButton.setText("Discard and exit");

		Label invisibleFillingLabel = new Label(this, SWT.NONE);
		invisibleFillingLabel.setText("");

		this.shell.setDefaultButton(this.saveAndExitButton);
		this.saveAndExitButton.setFocus();

		// Prepare scrolling.
		this.scrollGroup.setContent(this.mainGroup);
		this.scrollGroup.setExpandVertical(true);

		// Esc listeners.
		this.addKeyListener(new EscKeyListener(parent));
		this.scrollGroup.addKeyListener(new EscKeyListener(parent));
		this.numberLabel.addKeyListener(new EscKeyListener(parent));
		this.entryLabel.addKeyListener(new EscKeyListener(parent));
		this.aktionLabel.addKeyListener(new EscKeyListener(parent));
		this.saveAndExitButton.addKeyListener(new EscKeyListener(parent));
		this.discardAndExitButton.addKeyListener(new EscKeyListener(parent));
		invisibleFillingLabel.addKeyListener(new EscKeyListener(parent));

		// Listener.
		/*
		 * Save the changes and close the window.
		 */
	    this.saveAndExitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		// Save the entries.
	    		ClassPathEntriesComposite.this.parent.setClassPathEntries(ClassPathEntriesComposite.this.classPathEntries);

	    		// Close the window.
	    		getParentWindow().doExit();
	    	}
	    });

	    /*
	     * Discard the changes and close the window.
	     */
	    this.discardAndExitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		getParentWindow().doExit();

	    	}
	    });
	}

	/**
	 * Build the main group of the composite, containing the class path entries.
	 */
	protected void buildEntryGroup() {
		Iterator<String> iterator = this.classPathEntries.iterator();
		// Remove controls.
		Control[] controls = this.mainGroup.getChildren();
		if (controls != null) {
			for (int a = 0; a < controls.length; a++) {
				controls[a].dispose();
				controls[a] = null;
			}
		}

		// Entries.
		int entries = this.classPathEntries.size() + 2;

		// Layout.
		GridLayout mainGridLayout = new GridLayout(3, false);

		// Draw main group.
		this.mainGroup.setLayout(mainGridLayout);

		// Array initialization.
		this.numberLabels = new Label[entries];
		this.entryText = new Text[entries];
		this.aktionButton = new Button[entries];

		// GridData for width hints.
		final GridData widthHint80 = new GridData();
		widthHint80.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_SHORT;

		final GridData widthHint100 = new GridData();
		widthHint100.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_SHORT;

		final GridData widthHintLong = new GridData();

		// Create new controls.
		int a = 0;
		while (iterator.hasNext()) {
			this.numberLabels[a] = new Label(this.mainGroup, SWT.CENTER);
			this.numberLabels[a].setText(String.valueOf(a + 1));
			this.numberLabels[a].setLayoutData(widthHint80);

			this.entryText[a] = new Text(this.mainGroup, SWT.BORDER);
			this.entryText[a].setEditable(false);
			this.entryText[a].setBackground(new Color(this.display, StaticGuiSupport.RGB_WHITE));
			this.entryText[a].setText(iterator.next());

			// Skip the buttons on the first entry, since it cannot be altered.
			if (a == 0) {
				Label invisibleFillingLabel = new Label(this.mainGroup, SWT.NONE);
				invisibleFillingLabel.setText("");
				invisibleFillingLabel.addKeyListener(new EscKeyListener(this.parent));
			} else {
				this.aktionButton[a] = new Button(this.mainGroup, SWT.None);
				this.aktionButton[a].setText("Delete...");
				this.aktionButton[a].setData(Integer.valueOf(a));
				this.aktionButton[a].setLayoutData(widthHint100);

				// Listener
				this.aktionButton[a].addKeyListener(new EscKeyListener(this.parent));

				/*
				 * Remove this entry.
				 */
				this.aktionButton[a].addListener(SWT.Selection, new Listener() {
			    	public void handleEvent(Event event) {
			    		int index = (Integer) ((Button) event.widget).getData();
			    		getClassPathEntries().remove(getEntryText(index).getText());
			    		buildEntryGroup();
			    	}
			    });
			}

			// Listener
			this.numberLabels[a].addKeyListener(new EscKeyListener(this.parent));
			this.entryText[a].addKeyListener(new EscKeyListener(this.parent));

			a++;
		}

		// New entry button for jar files.
		this.numberLabels[a] = new Label(this.mainGroup, SWT.CENTER);
		this.numberLabels[a].setText(String.valueOf(a + 1));
		this.numberLabels[a].setLayoutData(widthHint80);

		this.entryText[a] = new Text(this.mainGroup, SWT.BORDER);
		this.entryText[a].setEditable(false);

		this.aktionButton[a] = new Button(this.mainGroup, SWT.None);
		this.aktionButton[a].setText("New jar...");
		this.aktionButton[a].setLayoutData(widthHint100);

		// Listener
		this.aktionButton[a].addKeyListener(new EscKeyListener(this.parent));

		/*
		 * Add a new entry. To archive this, open a file opening dialog and let the
		 * user choose a jar archive to add. Add it, if it is not already among the
		 * class path entries.
		 */
		this.aktionButton[a].addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		FileDialog fileDialog = new FileDialog(ClassPathEntriesComposite.this.shell, SWT.OPEN);
	    		String[] extensions = {"*.jar"};
	    		String[] names = {"Jar archive (*.jar)"};
	    		fileDialog.setFilterExtensions(extensions);
	    		fileDialog.setFilterNames(names);
	    		String path = fileDialog.open();
	    		if (path != null) {
	    			// first of all replace double backslashes against slashes
	    			path = path.replace("\\\\", "/");
	    			// and convert to normal slashes
	    			path = path.replace("\\", "/");

	    			// check if this entry already exists
	    			if (getClassPathEntries().contains(path)) {
	    				StaticGuiSupport.showMessageBox(ClassPathEntriesComposite.this.shell, "Error", "This entry already exists.", SWT.OK | SWT.ICON_ERROR);
	    			} else {
	    				getClassPathEntries().add(path);
	    				buildEntryGroup();
	    			}
	    		}
	    	}
	    });

		// Listener
		this.numberLabels[a].addKeyListener(new EscKeyListener(this.parent));
		this.entryText[a].addKeyListener(new EscKeyListener(this.parent));

		// New entry button for directories.
		a++;

		this.numberLabels[a] = new Label(this.mainGroup, SWT.CENTER);
		this.numberLabels[a].setText(String.valueOf(a + 1));
		this.numberLabels[a].setLayoutData(widthHint80);

		this.entryText[a] = new Text(this.mainGroup, SWT.BORDER);
		this.entryText[a].setEditable(false);

		this.aktionButton[a] = new Button(this.mainGroup, SWT.None);
		this.aktionButton[a].setText("New directory...");
		this.aktionButton[a].setLayoutData(widthHint100);

		// Listener
		this.aktionButton[a].addKeyListener(new EscKeyListener(this.parent));

		/*
		 * Add a directory. To archive this, open a directory opening dialog and
		 * let the user choose a directory. Add it, if it is not already among the
		 * class path entries.
		 */
		this.aktionButton[a].addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		DirectoryDialog directoryDialog = new DirectoryDialog(ClassPathEntriesComposite.this.shell, SWT.NONE);
	    		directoryDialog.setMessage("Please select a directory...");
	    		String path = directoryDialog.open();
	    		if (path != null) {
	    			// First of all replace double backslashes against slashes.
	    			path = path.replace("\\\\", "/");
	    			// And convert to normal slashes
	    			path = path.replace("\\", "/");
	    			// Add a trailing slash if the last character is not a slash.
	    			if (!path.substring(path.length() - 1).equals("/")) path += "/";

	    			// check if this entry already exists
	    			if (getClassPathEntries().contains(path)) {
	    				StaticGuiSupport.showMessageBox(ClassPathEntriesComposite.this.shell, "Error", "This entry already exists.", SWT.OK | SWT.ICON_ERROR);
	    			} else {
	    				getClassPathEntries().add(path);
	    				buildEntryGroup();
	    			}
	    		}
	    	}
	    });

		// Esc listener
		this.numberLabels[a].addKeyListener(new EscKeyListener(this.parent));
		this.entryText[a].addKeyListener(new EscKeyListener(this.parent));

		// add the new mainGroup to the scrollGroup
		this.scrollGroup.setContent(this.mainGroup);
		this.scrollGroup.setExpandHorizontal(true);
		this.scrollGroup.setExpandVertical(true);
		this.scrollGroup.setMinSize(this.mainGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// Pack the main group to calculate initial sizes.
		this.mainGroup.pack(true);

		// Set all text fields to a equal width that does not exceed a given maximum.
		int maxWidth = 0;
		for (a = 0; a < entries; a++) {
			int x = this.entryText[a].getSize().x;
			if (x > maxWidth) maxWidth = x;
		}
		if (maxWidth < MIN_WIDTH_ENTRY_TEXT) maxWidth = MIN_WIDTH_ENTRY_TEXT;
		if (maxWidth > MAX_WIDTH_ENTRY_TEXT) maxWidth = MAX_WIDTH_ENTRY_TEXT;
		widthHintLong.widthHint = maxWidth;
		for (a = 0; a < entries; a++) {
			this.entryText[a].setLayoutData(widthHintLong);
		}
		this.entryLabel.setLayoutData(widthHintLong);
		this.mainGroup.pack(true);

		// Make sure the main group does not exceed the desired maximum height.
		int height = this.mainGroup.getSize().y;
		if (height > MAX_HEIGHT_SCROLLGROUP) {
			this.scrollGroupGridData.horizontalSpan = 3;
			this.scrollGroupGridData.heightHint = MAX_HEIGHT_SCROLLGROUP;
		} else {
			this.scrollGroupGridData.heightHint = SWT.DEFAULT;
		}
		this.scrollGroup.setLayoutData(this.scrollGroupGridData);

		// Finish setting up the composite.
		this.pack(true);
		this.shell.pack(true);
	}

	/**
	 * Getter for the parent Window.
	 * @return The ClassPathEntriesWindow.
	 */
	protected ClassPathEntriesWindow getParentWindow() {
		return this.parent;
	}

	/**
	 * Getter for the current Display.
	 * @return The Display.
	 */
	@Override
	public Display getDisplay() {
		return this.display;
	}

	/**
	 * Getter for this Composite.
	 * @return This ClassPathEntriesComposite.
	 */
	protected ClassPathEntriesComposite getThis() {
		return this;
	}

	/**
	 * Getter for the save and exit button.
	 * @return The saveAndExitButton.
	 */
	protected Button getSaveAndExitButton() {
		return this.saveAndExitButton;
	}

	/**
	 * Getter for the discard and exit button.
	 * @return The discardAndExitButton.
	 */
	protected Button getDiscardAndExitButton() {
		return this.discardAndExitButton;
	}

	/**
	 * Getter for the class path entries.
	 * @return The classPathEntries as an arrayList of String objects.
	 */
	protected List<String> getClassPathEntries() {
		return this.classPathEntries;
	}

	/**
	 * Getter for the text of the entry at position index.
	 * @param index The index of the entry to get the text from.
	 * @return The Text of the entry at the specified index.
	 */
	protected Text getEntryText(int index) {
		return this.entryText[index];
	}

	/**
	 * Getter for the the number label.
	 * @return The numberLabel.
	 */
	protected Label getNumberLabel() {
		return this.numberLabel;
	}

	/**
	 * Getter for the scrolled Composite.
	 * @return The scrolledComposite.
	 */
	protected ScrolledComposite getScrollGroup() {
		return this.scrollGroup;
	}

	/**
	 * Getter for the main group Composite.
	 * @return The mainGroup.
	 */
	protected Composite getMainGroup() {
		return this.mainGroup;
	}

}
