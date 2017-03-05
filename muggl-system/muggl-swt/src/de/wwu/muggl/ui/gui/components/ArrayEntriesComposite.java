package de.wwu.muggl.ui.gui.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.ui.gui.support.ArrayModificationHandler;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.ArrayEntriesWindow;
import de.wwu.muggl.ui.gui.windows.InputWindow;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;

/**
 * The composite for the ArrayEntriesWindow. It offers most of its element and the corresponding
 * methods.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class ArrayEntriesComposite extends Composite {
	// Constants for minimum and maximum dimensions.
	private static final int MAX_HEIGHT_SCROLLGROUP = 450;

	// General fields for the window.
	ArrayEntriesWindow parent;
	Shell shell;
	Display display;

	// Constant fields for the composites elements.
	private final GridData scrollGroupGridData;
	private final ScrolledComposite scrollGroup;
	private final Composite mainGroup;
	private final Label numberLabel;
	private final Label entryLabel;
	private final Label aktionLabel;
	private Label[] numberLabels;
	private Text[] valueText;
	private Button[] editButton;
	private Button[] deleteButton;
	private Button[] toNullButton;
	Combo insertAtCombo;
	private final Button saveAndExitButton;
	private final Button discardAndExitButton;

	// Further fields
	ArrayModificationHandler arrayModificationHandler;
	int myDimension;
	int[] dimensionsIndexes;
	String typeString;
	boolean entriesAreArrays;
	boolean doNotDiscardChanges;

	/**
	 * Set up the composite for the ArrayEntriesWindow window.
	 * @param parent The windows this composite belongs to.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 * @param style The style for this composite.
	 * @param arrayModificationHandler The ArrayModificationHandler the holds the represented array.
	 * @param myDimension The dimension of a probably multidimensional array this Window represents.
	 * @param dimensionsIndexes Dimension indexes of the higher-level dimensions the array represented might be a part of.
	 */
	public ArrayEntriesComposite(
			ArrayEntriesWindow parent,
			Shell shell,
			Display display,
			int style,
			ArrayModificationHandler arrayModificationHandler,
			int myDimension,
			int[] dimensionsIndexes
			) {
		// General initialization.
		super(shell, style);
		this.parent = parent;
		this.shell = shell;
		this.display = display;
		this.arrayModificationHandler = arrayModificationHandler;
		this.myDimension = myDimension;
		if (dimensionsIndexes == null) {
			this.dimensionsIndexes = new int[0];
		} else {
			this.dimensionsIndexes = dimensionsIndexes;
		}
		this.doNotDiscardChanges = false;

		// Check the type.
		this.typeString = arrayModificationHandler.getTypeString();

		// Remove as many [] as there are dimensions.
		for (int a = 0; a < this.myDimension; a++) {
			if (this.typeString.contains("[]")) {
				this.typeString = this.typeString.substring(0, this.typeString.length() - 2);
			}
		}

		// Are the entries arrays?
		if (this.typeString.contains("[]")) {
			this.entriesAreArrays = true;
		} else {
			this.entriesAreArrays = false;
		}

		// GridData for spanning several columns.
		this.scrollGroupGridData = new GridData();
		this.scrollGroupGridData.horizontalSpan = 3;

		// GridData for width hints.
		final GridData widthHint80 = new GridData();
		widthHint80.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_SHORT;

		final GridData widthHint100 = new GridData();
		widthHint100.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_MEDIUM;

		final GridData widthHint120 = new GridData();
		widthHint120.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_LONG;

		// Layout
		GridLayout compositeGridLayout = new GridLayout(3, false);
		this.setLayout(compositeGridLayout);

		// Components.
		this.numberLabel = new Label(this, SWT.CENTER);
		this.numberLabel.setText("Entry #");
		this.numberLabel.setLayoutData(widthHint80);

		this.entryLabel = new Label(this, SWT.CENTER);
		this.entryLabel.setText("Value");
        this.entryLabel.setLayoutData(widthHint120);

		this.aktionLabel = new Label(this, SWT.CENTER);
		this.aktionLabel.setText("Action");
		this.aktionLabel.setLayoutData(widthHint100);

		this.scrollGroup = new ScrolledComposite(this, SWT.V_SCROLL);
		this.scrollGroup.setLayoutData(this.scrollGroupGridData);

		this.mainGroup = new Composite(this.scrollGroup, SWT.NONE);

		// Array null initialization.
		this.numberLabels = null;
		this.valueText = null;
		this.editButton = null;
		this.deleteButton = null;

		// Draw main group.
		buildEntryGroup();

		// Buttons for ok and cancel.
		this.saveAndExitButton = new Button(this, SWT.None);
		if (this.myDimension == 0) {
			this.saveAndExitButton.setText("Save and exit");
		} else {
			this.saveAndExitButton.setText("OK");
		}

		this.discardAndExitButton = new Button(this, SWT.None);
		this.discardAndExitButton.setText("Discard and exit");
		if (this.myDimension > 0) {
			this.discardAndExitButton.setEnabled(false);
			this.discardAndExitButton.setVisible(false);
		}

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

		// Listener.
		/**
		 * Save the changes and close the window.
		 */
	    this.saveAndExitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ArrayEntriesComposite.this.doNotDiscardChanges = true;
	    		getParentWindow().doExit();
	    	}
	    });

	    /**
	     * Discard the changes and close the window.
	     */
	    this.discardAndExitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		// If this is the window of the topmost dimension, tell the ArrayModificationHandler not to return the array.
	    		if (ArrayEntriesComposite.this.myDimension == 0) {
	    			ArrayEntriesComposite.this.arrayModificationHandler.doNotReturnThisArray();
	    		}
	    		getParentWindow().doExit();
	    	}
	    });

		/**
		 * If the "X" at the right top of the window is clicked, close the window.
		 */
		this.shell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			@Override
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				// If this is the window of the topmost dimension, tell the ArrayModificationHandler not to return the array.
	    		if (!ArrayEntriesComposite.this.doNotDiscardChanges && ArrayEntriesComposite.this.myDimension == 0) {
	    			ArrayEntriesComposite.this.arrayModificationHandler.doNotReturnThisArray();
	    		}
	    		getParentWindow().doExit();
			}
		});
	}

	/**
	 * Build the main group of the composite, containing the array entries.
	 */
	protected void buildEntryGroup() {
		// Remove controls.
		Control[] controls = this.mainGroup.getChildren();
		if (controls != null) {
			for (int a = 0; a < controls.length; a++) {
				controls[a].dispose();
				controls[a] = null;
			}
		}

		// Entries.
		Object[] array = ArrayEntriesComposite.this.arrayModificationHandler.getPartialArray(ArrayEntriesComposite.this.dimensionsIndexes);
		final int entries = array.length + 1;

		// Layout.
		int oneMore = 0;
		if (!ArrayEntriesComposite.this.arrayModificationHandler.getIsPrimitive() && !this.entriesAreArrays) oneMore = 1;
		GridLayout mainGridLayout = new GridLayout(4 + oneMore, false);

		// Draw main group.
		this.mainGroup.setLayout(mainGridLayout);

		// Array initialization.
		this.numberLabels = new Label[entries];
		this.valueText = new Text[entries - 1];
		this.editButton = new Button[entries];
		this.deleteButton = new Button[entries - 1];
		if (!ArrayEntriesComposite.this.arrayModificationHandler.getIsPrimitive() && !this.entriesAreArrays) this.toNullButton = new Button[entries - 1];

		// GridData for width hints.
		final GridData widthHint80 = new GridData();
		widthHint80.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_SHORT;

		final GridData widthHint100 = new GridData();
		widthHint100.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_MEDIUM;

		final GridData widthHint120 = new GridData();
		widthHint120.widthHint = StaticGuiSupport.SWT_GRID_WIDTH_HINT_LONG;

		// Create new controls.
		for (int a = 0; a < entries - 1; a++) {
			this.numberLabels[a] = new Label(this.mainGroup, SWT.CENTER);
			this.numberLabels[a].setText(String.valueOf(a + 1));
			this.numberLabels[a].setLayoutData(widthHint80);

			this.valueText[a] = new Text(this.mainGroup, SWT.BORDER);
			this.valueText[a].setEditable(false);
			this.valueText[a].setBackground(new Color(this.display, StaticGuiSupport.RGB_WHITE));
			if (this.entriesAreArrays) {
				Font font = this.shell.getFont();
				FontData[] fontData = font.getFontData();
				this.valueText[a].setText("(Array)");
				fontData[0].setStyle(SWT.ITALIC);
				this.valueText[a].setFont(new Font(this.display, fontData));
			} else {
				if (array[a] == null) {
					Font font = this.shell.getFont();
					FontData[] fontData = font.getFontData();
					this.valueText[a].setText("null");
					fontData[0].setStyle(SWT.BOLD | SWT.ITALIC);
					this.valueText[a].setFont(new Font(this.display, fontData));
				} else {
					this.valueText[a].setText(array[a].toString());
				}
			}
			this.valueText[a].setLayoutData(widthHint120);

			this.editButton[a] = new Button(this.mainGroup, SWT.None);
			if (this.entriesAreArrays) {
				this.editButton[a].setText("Edit entries...");
			} else {
				this.editButton[a].setText("Edit...");
			}
			this.editButton[a].setData(Integer.valueOf(a));
			this.editButton[a].setLayoutData(widthHint100);

			if (!ArrayEntriesComposite.this.arrayModificationHandler.getIsPrimitive() && !this.entriesAreArrays) {
				this.toNullButton[a] = new Button(this.mainGroup, SWT.None);
				this.toNullButton[a].setText("Set to null");
				this.toNullButton[a].setData(Integer.valueOf(a));
				this.toNullButton[a].setLayoutData(widthHint100);
			}

			this.deleteButton[a] = new Button(this.mainGroup, SWT.None);
			this.deleteButton[a].setText("Delete...");
			this.deleteButton[a].setData(Integer.valueOf(a));
			this.deleteButton[a].setLayoutData(widthHint100);

			// Hide the delete button if this is not the last dimension and there is only one entry.
			if (this.entriesAreArrays && entries == 2) {
				this.deleteButton[a].setEnabled(false);
				this.deleteButton[a].setVisible(false);
			}

			// Listener.
			this.numberLabels[a].addKeyListener(new EscKeyListener(this.parent));
			this.valueText[a].addKeyListener(new EscKeyListener(this.parent));
			this.editButton[a].addKeyListener(new EscKeyListener(this.parent));
			if (!ArrayEntriesComposite.this.arrayModificationHandler.getIsPrimitive() && !this.entriesAreArrays) this.toNullButton[a].addKeyListener(new EscKeyListener(this.parent));
			this.deleteButton[a].addKeyListener(new EscKeyListener(this.parent));

			/**
			 * Edit this entry.
			 */
			this.editButton[a].addListener(SWT.Selection, new Listener() {
		    	public void handleEvent(Event event) {
		    		int index = (Integer) ((Button) event.widget).getData();
		    		if (ArrayEntriesComposite.this.entriesAreArrays) {
		    			ArrayEntriesComposite.this.parent.getShell().setEnabled(false);

		    			try {
		    				// Create the new dimension indexes array and add this dimension.
		    				int[] dimensionIndexes = new int[ArrayEntriesComposite.this.dimensionsIndexes.length + 1];
		    				for (int b = 0; b < ArrayEntriesComposite.this.dimensionsIndexes.length; b++)
		    				{
		    					dimensionIndexes[b] = ArrayEntriesComposite.this.dimensionsIndexes[b];
		    				}
		    				dimensionIndexes[dimensionIndexes.length - 1] = index;

		    				// Show the window.
		    				ArrayEntriesWindow arrayEntriesWindows = new ArrayEntriesWindow();
		    				arrayEntriesWindows.show(ArrayEntriesComposite.this.parent, ArrayEntriesComposite.this.arrayModificationHandler, ArrayEntriesComposite.this.myDimension + 1, dimensionIndexes);
	   	    			} catch (Throwable t) {
    	    				StaticGuiSupport.processGuiError(t, "method parameter", ArrayEntriesComposite.this.shell);
    	    			}

	   	    			ArrayEntriesComposite.this.parent.getShell().setEnabled(true);
		    			ArrayEntriesComposite.this.parent.getShell().setActive();
		    			getSaveAndExitButton().setFocus();
		    		} else {
		    			ArrayEntriesComposite.this.parent.getShell().setEnabled(false);

		    			try {
		    				InputWindow inputWindow = new InputWindow(ArrayEntriesComposite.this.shell, getDisplay());
		    				Object oldObject = ArrayEntriesComposite.this.arrayModificationHandler.getArrayEntry(ArrayEntriesComposite.this.dimensionsIndexes, index);
			    			Object newObject = inputWindow.show(oldObject, ArrayEntriesComposite.this.typeString);
			    			if (!(newObject instanceof UndefinedValue) && !newObject.equals(oldObject)) {
			    				ArrayEntriesComposite.this.arrayModificationHandler.setArrayEntry(ArrayEntriesComposite.this.dimensionsIndexes, index, newObject);
			    				buildEntryGroup();
			    			}
		    			} catch (Throwable t) {
    	    				StaticGuiSupport.processGuiError(t, "method parameter", ArrayEntriesComposite.this.shell);
    	    			}

		    			ArrayEntriesComposite.this.parent.getShell().setEnabled(true);
		    			ArrayEntriesComposite.this.parent.getShell().setActive();
		    			getSaveAndExitButton().setFocus();
		    		}
		    	}
		    });

			if (!ArrayEntriesComposite.this.arrayModificationHandler.getIsPrimitive() && !this.entriesAreArrays) {
				/**
				 * Set to null.
				 */
				this.toNullButton[a].addListener(SWT.Selection, new Listener() {
			    	public void handleEvent(Event event) {
			    		int index = (Integer) ((Button) event.widget).getData();
			    		ArrayEntriesComposite.this.arrayModificationHandler.setArrayEntry(ArrayEntriesComposite.this.dimensionsIndexes, index, null);
			    		buildEntryGroup();
			    	}
			    });
			}

			/**
			 * Remove this entry.
			 */
			this.deleteButton[a].addListener(SWT.Selection, new Listener() {
		    	public void handleEvent(Event event) {
		    		int index = (Integer) ((Button) event.widget).getData();
		    		try {
		    			boolean cancel = false;
		    			boolean shift = false;
		    			if (ArrayEntriesComposite.this.dimensionsIndexes.length > 0) {
		    				String message = "You are deleting an element from a nested array dimension. "
		    							+ "Changing a nested dimensions not only have an effect on this array but will also effect the other arrays in the higher dimension.\n\n"
		    							+ "Example:\n"
		    							+ "Imagine an Array with three nested arrays of three entries each, like int[3][3]. "
		    							+ "Now we delete [1][1]. Hence, the second dimension now contains two elements only. "
		    							+ "Deleting one element so effectively deletes three elements.\n\n"
		    							+ "There are two possible ways to do this:\n"
		    							+ "1. Delete the corresponding elements.\n"
		    							+ "2. Delete the last element of the other Arrays.\n\n"
		    							+ "In other words: 1. will have [0][1] and [2][1] deleted aswell, while 2. will have [0][2] and [2][2] deleted.\n"
		    							+ "We recommend to delete corresponding entries.\n\n"
		    							+ "Do you want to use strategy 1. instead of strategy 2.?";
		    				int answer = StaticGuiSupport.showMessageBox(ArrayEntriesComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
		    				if (answer == SWT.YES) shift = true;
		    				else if (answer == SWT.CANCEL) cancel = true;
		    			}
		    			if (!cancel) {
			    			ArrayEntriesComposite.this.arrayModificationHandler.truncateArray(ArrayEntriesComposite.this.myDimension, ArrayEntriesComposite.this.dimensionsIndexes, index, shift);
			    			buildEntryGroup();
		    			}
		    		} catch (GUIException e) {
	    				StaticGuiSupport.processGuiError(e, "Array entries", ArrayEntriesComposite.this.shell);
	    			}
		    	}
		    });
		}

		// New entry.
		this.numberLabels[entries - 1] = new Label(this.mainGroup, SWT.CENTER);
		this.numberLabels[entries - 1].setText("new");
		this.numberLabels[entries - 1].setLayoutData(widthHint80);

		Label invisibleFillingLabel1 = new Label(this.mainGroup, SWT.NONE);

		this.editButton[entries - 1] = new Button(this.mainGroup, SWT.None);
		this.editButton[entries - 1].setText("Add...");
		this.editButton[entries - 1].setLayoutData(widthHint100);

		this.insertAtCombo = new Combo(this.mainGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.insertAtCombo.setText("");
		this.insertAtCombo.setLayoutData(widthHint100);
		this.insertAtCombo.add("At the beginning");
		for (int a = 1; a < entries - 1; a++) {
			this.insertAtCombo.add("After " + a);
		}
		if (entries > 1) {
			this.insertAtCombo.add("At the end");
		}
		this.insertAtCombo.select(0);

		if (!ArrayEntriesComposite.this.arrayModificationHandler.getIsPrimitive() && !this.entriesAreArrays) {
			Label invisibleFillingLabel2 = new Label(this.mainGroup, SWT.NONE);
			invisibleFillingLabel2.addKeyListener(new EscKeyListener(this.parent));
		}

		// Listener
		invisibleFillingLabel1.addKeyListener(new EscKeyListener(this.parent));
		this.editButton[entries - 1].addKeyListener(new EscKeyListener(this.parent));
		this.insertAtCombo.addKeyListener(new EscKeyListener(this.parent));

		/**
		 * Add a new entry.
		 */
		this.editButton[entries - 1].addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		ArrayEntriesComposite.this.parent.getShell().setEnabled(false);

    			try {
    				Object object = null;
    				if (ArrayEntriesComposite.this.entriesAreArrays) {
    					// Determine the index to insert the new array at.
    					int index;
    					String positionString = ArrayEntriesComposite.this.insertAtCombo.getText();
	    				if (positionString.equals("At the beginning")) {
	    					index = 0;
	    				} else if (positionString.equals("At the end")) {
	    					index = entries - 1;
	    				} else {
	    					try {
	    						index = Integer.parseInt(positionString.substring(6));
	    					} catch (NumberFormatException e) {
	    						StaticGuiSupport.showMessageBox(ArrayEntriesComposite.this.shell, "Error", "Could not save the value. The index does not appear to exist.", SWT.OK | SWT.ICON_ERROR);
	    						return;
	    					}
	    				}

	    				// Expand this dimension.
	    				ArrayEntriesComposite.this.arrayModificationHandler.expandArray(ArrayEntriesComposite.this.myDimension, index);
	    				buildEntryGroup();
    				} else {
	    				// Draw the input window and get the result.
	    				InputWindow inputWindow = new InputWindow(ArrayEntriesComposite.this.shell, getDisplay());
		    			object = inputWindow.show(null, ArrayEntriesComposite.this.typeString);

		    			// Save the value?
	    				if (object != null && !(object instanceof UndefinedValue)) {
		    				// Determine the index to save the entry to.
	    					int index;
	    					String positionString = ArrayEntriesComposite.this.insertAtCombo.getText();
		    				if (positionString.equals("At the beginning")) {
		    					index = 0;
		    				} else if (positionString.equals("At the end")) {
		    					index = entries - 1;
		    				} else {
		    					try {
		    						index = Integer.parseInt(positionString.substring(6));
		    					} catch (NumberFormatException e) {
		    						StaticGuiSupport.showMessageBox(ArrayEntriesComposite.this.shell, "Error", "Could not save the value. The index does not appear to exist.", SWT.OK | SWT.ICON_ERROR);
		    						return;
		    					}
		    				}

		    				boolean cancel = false;
			    			boolean shift = false;
			    			if (ArrayEntriesComposite.this.dimensionsIndexes.length > 0) {
			    				String message = "You are inserting an element into a nested array dimension. "
			    							+ "Changing a nested dimensions not only have an effect on this array but will also effect the other arrays in the higher dimension.\n\n"
			    							+ "Example:\n"
			    							+ "Imagine an Array with three nested arrays of three entries each, like int[3][3]. "
			    							+ "Now we insert into the second array at the second position. Hence, the second dimension now contains four elements. "
			    							+ "Inserting one element so effectively inserts three elements (two of the are null initially).\n\n"
			    							+ "There are two possible ways to do this:\n"
			    							+ "1. Insert the null values at the corresponding positions.\n"
			    							+ "2. Insert the null values as the last elements of the other arrays.\n\n"
			    							+ "In other words: 1. will have [0][1] and [2][1] be null with the other elements shifted to the right by one, while 2. will have null values inserted as [0][3] and [2][3].\n"
			    							+ "We recommend to insert at the same positions.\n\n"
			    							+ "Do you want to use strategy 1. instead of strategy 2.?";
			    				int answer = StaticGuiSupport.showMessageBox(ArrayEntriesComposite.this.shell, "Question", message, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			    				if (answer == SWT.YES) shift = true;
			    				else if (answer == SWT.CANCEL) cancel = true;
			    			}
			    			if (!cancel) {
			    				// Expand and write to the correct array position.
			    				ArrayEntriesComposite.this.arrayModificationHandler.expandArrayInsert(ArrayEntriesComposite.this.dimensionsIndexes, index, object, shift);
				    			// Refresh the entries.
			    				buildEntryGroup();
			    			}
	    				}
    				}
	    		} catch (Throwable t) {
    				StaticGuiSupport.processGuiError(t, "Array entries", ArrayEntriesComposite.this.shell);
    			}

	    		ArrayEntriesComposite.this.parent.getShell().setEnabled(true);
    			ArrayEntriesComposite.this.parent.getShell().setActive();
    			getSaveAndExitButton().setFocus();
	    	}
	    });

		// Listener.
		this.numberLabels[entries - 1].addKeyListener(new EscKeyListener(this.parent));

		// Add the new mainGroup to the scrollGroup.
		this.scrollGroup.setContent(this.mainGroup);
		this.scrollGroup.setExpandHorizontal(true);
		this.scrollGroup.setExpandVertical(true);
		this.scrollGroup.setMinSize(this.mainGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// Make sure the main group does not exceed the desired maximum height.
		this.mainGroup.pack(true);
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
	 * @return The ArrayEntriesWindow.
	 */
	protected ArrayEntriesWindow getParentWindow() {
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
	 * Getter for the save and exit button.
	 * @return The saveAndExitButton.
	 */
	protected Button getSaveAndExitButton() {
		return this.saveAndExitButton;
	}

}
