package de.wwu.muggl.ui.gui.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.ui.gui.support.EscKeyListener;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;

/**
 * Providing the small input window for the altering the values of primitive
 * types, their wrappers and of Strings.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class InputWindow implements ShellWindow {
	// Window related fields.
	private Shell shell;
	private Display display;
	Shell smallShell = null;
	private boolean isClosing = false;
	private String informationText;

	// Widgets.
	private FormData informationFormData;
	private Label informationLabel;
	private FormData inputTextGroup;
	Text inputText;
	private FormData typeSelectionFormData;
	private Combo typeSelectionCombo;
	private FormData okFormData;
	private Button okButton;
	private FormData cancelFormData;
	private Button cancelButton;

	// The true or false combo to replace the input text if a boolean value is required.
	private Combo booleanValueCombo;
	private boolean showBooleanValueCombo;

	// General fields.
	boolean returnNoUndefinedValue;
	int initialPhase;
	int currentPhase;
	Object variable;
	String typeString;

	/**
	 * Basic constructor.
	 * @param shell The current Shell.
	 * @param display The current Display.
	 */
	public InputWindow(Shell shell, Display display) {
		this.shell = shell;
		this.display = display;
		this.informationText = "Please enter the desired value...";
	}

	/**
	 * Initialize and show the window, let a type be chosen and
	 * return the parameter when it is closed.
	 * @return The chosen value (probably an UndefinedValue if the editing was canceled).
	 */
	public Object show() {
		try {
			this.returnNoUndefinedValue = false;
			this.initialPhase = 1;
			this.variable = new UndefinedValue();
			this.typeString = null;
			createShell();
			this.smallShell.open();

			while (!this.smallShell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
			if (this.returnNoUndefinedValue) return this.variable;
			return new UndefinedValue();
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "input", this.shell);
			return null;
		} finally {
			doExit();
		}
	}

	/**
	 * Initialize and show the window, return the parameter when it is closed.
	 * @param variable The parameter that can be changed.
	 * @param typeString The String representation of the parameters type.
	 * @return The (probably changed) parameter.
	 */
	public Object show(Object variable, String typeString) {
		try {
			this.returnNoUndefinedValue = false;
			this.initialPhase = 2;
			this.variable = variable;
			this.typeString = typeString;

			if (!isEditableType(typeString)) {
				StaticGuiSupport.showMessageBox(this.shell, "This value cannot be edited.");
			} else {
				createShell();
				this.smallShell.open();

				while (!this.smallShell.isDisposed()) {
					if (!this.display.readAndDispatch())
						this.display.sleep();
					}
			}
			if (this.returnNoUndefinedValue) return this.variable;
			return new UndefinedValue();
		} catch (Throwable t) {
			StaticGuiSupport.processGuiError(t, "input", this.shell);
			return null;
		} finally {
			doExit();
		}
	}

	/**
	 * Setter for the information text. This method will have no effect once the window is opened.
	 *
	 * @param text The information text for the window.
	 */
	public void setInformationText(String text) {
		this.informationText = text;
	}

	/**
	 * Initialize the Shell.
	 */
	private void createShell() {
		// Initialize the shell itself.
		this.smallShell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE);
		this.smallShell.setText(Globals.WINDOWS_TITLE + Globals.WINDOWS_TITLE_CONNECTOR + "Input...");
		this.smallShell.setLayout(new FormLayout());

		// The information label.
		this.informationFormData = new FormData();
		this.informationFormData.top = new FormAttachment(this.smallShell, 5, SWT.BOTTOM);
		this.informationFormData.bottom = new FormAttachment(this.smallShell, 25, SWT.BOTTOM);
		this.informationFormData.left = new FormAttachment(this.smallShell, 5, SWT.RIGHT);
		this.informationFormData.right = new FormAttachment(this.smallShell, 225, SWT.RIGHT);

		this.informationLabel = new Label(this.smallShell, SWT.NONE);
		this.informationLabel.setText("Please choose the desired type...");
		this.informationLabel.setLayoutData(this.informationFormData);

		// Phase 1: Setting the type.
		this.typeSelectionFormData = new FormData();
		this.typeSelectionFormData.top = new FormAttachment(this.informationLabel, 5, SWT.BOTTOM);
		this.typeSelectionFormData.bottom = new FormAttachment(this.informationLabel, 25, SWT.BOTTOM);
		this.typeSelectionFormData.left = new FormAttachment(this.smallShell, 5, SWT.RIGHT);
		this.typeSelectionFormData.right = new FormAttachment(this.smallShell, 125, SWT.RIGHT);

		this.typeSelectionCombo = new Combo(this.smallShell, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.typeSelectionCombo.setLayoutData(this.typeSelectionFormData);
		this.typeSelectionCombo.add("java.lang.Double");
		this.typeSelectionCombo.add("java.lang.Float");
		this.typeSelectionCombo.add("java.lang.Integer");
		this.typeSelectionCombo.add("java.lang.Long");
		this.typeSelectionCombo.add("java.lang.String");

		// Phase 2: Entering the value.
		this.inputTextGroup = new FormData();
		this.inputTextGroup.top = new FormAttachment(this.informationLabel, 5, SWT.BOTTOM);
		this.inputTextGroup.bottom = new FormAttachment(this.informationLabel, 25, SWT.BOTTOM);
		this.inputTextGroup.left = new FormAttachment(this.smallShell, 5, SWT.RIGHT);
		this.inputTextGroup.right = new FormAttachment(this.smallShell, 125, SWT.RIGHT);

		this.inputText = new Text(this.smallShell, SWT.BORDER);
		if (this.variable != null && !(this.variable instanceof UndefinedValue)) this.inputText.setText(this.variable.toString());
		this.inputText.setSelection(0, this.inputText.getText().length());
		this.inputText.setLayoutData(this.inputTextGroup);
		this.inputText.setVisible(false);

		this.booleanValueCombo = new Combo(this.smallShell, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.booleanValueCombo.add("false");
		this.booleanValueCombo.add("true");
		this.booleanValueCombo.select(0);
		this.booleanValueCombo.setLayoutData(this.inputTextGroup);
		this.booleanValueCombo.setVisible(false);

		// Is a boolean type required?
		if (this.typeString != null && (this.typeString.equals("boolean") || this.typeString.equals("java.lang.Boolean"))) {
			// Is there a initial value for it?
			if (this.variable != null && !(this.variable instanceof UndefinedValue)) {
				if (this.variable instanceof Boolean && ((Boolean) this.variable).booleanValue()) {
					this.booleanValueCombo.select(1);
				} else if (this.variable instanceof BooleanConstant && ((BooleanConstant) this.variable).getValue()) {
					this.booleanValueCombo.select(1);
				}
			}
			this.showBooleanValueCombo = true;
		} else {
			this.showBooleanValueCombo = false;
		}

		// Buttons.
		this.okFormData = new FormData();
		this.okFormData.top = new FormAttachment(this.informationLabel, 5, SWT.BOTTOM);
		this.okFormData.bottom = new FormAttachment(this.informationLabel, 25, SWT.BOTTOM);
		this.okFormData.left = new FormAttachment(this.inputText, 10, SWT.RIGHT);
		this.okFormData.right = new FormAttachment(this.inputText, 45, SWT.RIGHT);

		this.okButton = new Button(this.smallShell, SWT.None);
		this.okButton.setText("&OK");
		this.okButton.setLayoutData(this.okFormData);

		this.cancelFormData = new FormData();
		this.cancelFormData.top = new FormAttachment(this.informationLabel, 5, SWT.BOTTOM);
		this.cancelFormData.bottom = new FormAttachment(this.informationLabel, 25, SWT.BOTTOM);
		this.cancelFormData.left = new FormAttachment(this.okButton, 10, SWT.RIGHT);
		this.cancelFormData.right = new FormAttachment(this.okButton, 65, SWT.RIGHT);

		this.cancelButton = new Button(this.smallShell, SWT.None);
		this.cancelButton.setText("&Back");
		this.cancelButton.setLayoutData(this.cancelFormData);

		this.smallShell.setDefaultButton(this.okButton);

		Point p = this.smallShell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

		Rectangle parentBounds = this.shell.getBounds();
		int posX = parentBounds.x + (parentBounds.width - p.x) / 2;
		int posY = parentBounds.y + (parentBounds.height - p.y) / 2;
		this.smallShell.setBounds(posX, posY, p.x + 4, p.y + 4);

		// Esc Listener.
		this.smallShell.addKeyListener(new EscKeyListener(this));
		this.informationLabel.addKeyListener(new EscKeyListener(this));
		this.typeSelectionCombo.addKeyListener(new EscKeyListener(this));
		this.inputText.addKeyListener(new EscKeyListener(this));
		this.okButton.addKeyListener(new EscKeyListener(this));
		this.cancelButton.addKeyListener(new EscKeyListener(this));

		/*
		 * The OK Button.
		 */
		this.okButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		okButtonPressed();
	    	}
	    });

		/*
		 * The Back/Cancel Button.
		 */
		this.cancelButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		cancelButtonPressed();
	    	}
	    });

		// Change to the initially needed phase.
		changePhase(this.initialPhase);
	}

	/**
	 * Change to the desired phase, enabling the needed widgets.
	 * @param phase The phase to change to
	 */
	protected void changePhase(int phase) {
		if (phase == 1) {
			this.informationLabel.setText("Please choose the desired type...");
			this.cancelButton.setText("&Cancel");
			this.typeSelectionCombo.setVisible(true);
			this.inputText.setVisible(false);
			this.currentPhase = phase;
		} else if (phase == 2) {
			this.informationLabel.setText(this.informationText);
			if (this.initialPhase == phase) {
				this.cancelButton.setText("&Cancel");
			} else {
				this.cancelButton.setText("&Back");
			}
			this.typeSelectionCombo.setVisible(false);
			if (this.showBooleanValueCombo) {
				this.booleanValueCombo.setVisible(true);
			} else {
				this.inputText.setVisible(true);
			}
			this.currentPhase = phase;
		}
	}

	/**
	 * If the first phase is active, switch to the second phase and
	 * save the type information. Otherwise check and save the entered
	 * data and exit.
	 *
	 */
	protected void okButtonPressed() {
		if (this.currentPhase == 1) {
			this.inputText.setText("0");
			this.typeString = this.typeSelectionCombo.getText();
			checkAndSaveData();
			changePhase(2);
		} else if (this.currentPhase == 2) {
			if (checkAndSaveData()) {
				this.returnNoUndefinedValue = true;
				doExit();
			}
		}
	}

	/**
	 * Change back to the first phase if we are in the second one
	 * and we did actually start with the first one. In any other
	 * case, close the window and discard any changes.
	 *
	 */
	protected void cancelButtonPressed() {
		if (this.currentPhase == 2 && this.initialPhase == 1) {
			this.typeString = null;
			this.variable = null;
			changePhase(1);
		} else {
			doExit();
		}
	}

	/**
	 * Check if the entered data can be parsed for the parameter type. If it is possible,
	 * parse it and store it in an new object of the appropriate type.
	 * @return true, if everything was successful, false otherwise.
	 */
	protected boolean checkAndSaveData() {
		try {
			if (this.typeString.equals("byte") || this.typeString.equals("java.lang.Byte")) {
				if (Options.getInst().symbolicMode) {
					this.variable = IntConstant.getInstance(Byte.parseByte(this.inputText.getText()));
				} else {
					this.variable = Byte.valueOf(this.inputText.getText());
				}
				return true;
			} else if (this.typeString.equals("boolean") || this.typeString.equals("java.lang.Boolean")) {
				if (Options.getInst().symbolicMode) {
					this.variable = BooleanConstant.getInstance(Boolean.parseBoolean(this.booleanValueCombo.getText()));
				} else {
					this.variable = Boolean.valueOf(this.booleanValueCombo.getText());
				}
				return true;
			} else if (this.typeString.equals("char") || this.typeString.equals("java.lang.Character")) {
				if (Options.getInst().symbolicMode) {
					this.variable = IntConstant.getInstance(Character.valueOf(this.inputText.getText().charAt(0)));
				} else {
					this.variable = Character.valueOf(this.inputText.getText().charAt(0));
				}
				return true;
			} else if (this.typeString.equals("double") || this.typeString.equals("java.lang.Double")) {
				if (Options.getInst().symbolicMode) {
					this.variable = DoubleConstant.getInstance(Double.parseDouble(this.inputText.getText()));
				} else {
					this.variable = Double.valueOf(this.inputText.getText());
				}
				return true;
			} else if (this.typeString.equals("float") || this.typeString.equals("java.lang.Float")) {
				if (Options.getInst().symbolicMode) {
					this.variable = FloatConstant.getInstance(Float.parseFloat(this.inputText.getText()));
				} else {
					this.variable = Float.valueOf(this.inputText.getText());
				}
				return true;
			} else if (this.typeString.equals("int") || this.typeString.equals("java.lang.Integer")) {
				if (Options.getInst().symbolicMode) {
					this.variable = IntConstant.getInstance(Integer.parseInt(this.inputText.getText()));
				} else {
					this.variable = Integer.valueOf(this.inputText.getText());
				}
				return true;
			} else if (this.typeString.equals("long") || this.typeString.equals("java.lang.Long")) {
				if (Options.getInst().symbolicMode) {
					this.variable = LongConstant.getInstance(Long.parseLong(this.inputText.getText()));
				} else {
					this.variable = Long.valueOf(this.inputText.getText());
				}
				return true;
			} else if (this.typeString.equals("short") || this.typeString.equals("java.lang.Short")) {
				if (Options.getInst().symbolicMode) {
					this.variable = IntConstant.getInstance(Short.parseShort(this.inputText.getText()));
				} else {
					this.variable = Short.valueOf(this.inputText.getText());
				}
				return true;
			} else { // It has to be a java.lang.String then.
				this.variable = this.inputText.getText();
				return true;
			}
		} catch (IndexOutOfBoundsException e) {
			// If there is no char as position 0.
			StaticGuiSupport.showMessageBox(this.smallShell, "Error", "Your input value \"" + this.inputText.getText() + "\" cannot be parsed as a " + this.typeString + ".", SWT.OK | SWT.ICON_ERROR);
			return false;
		} catch (NumberFormatException e) {
			// If parsing numbers fails.
			StaticGuiSupport.showMessageBox(this.smallShell, "Error", "Your input value \"" + this.inputText.getText() + "\" cannot be parsed as a " + this.typeString + ".", SWT.OK | SWT.ICON_ERROR);
			return false;
		}
	}

	/**
	 * Is the String indicating a type that can be edited in this window?
	 * @param typeString The String to inspect.
	 * @return true, if the described value is editable, false otherwise.
	 */
	private boolean isEditableType(String typeString) {
		if (typeString.equals("byte") || typeString.equals("java.lang.Byte")
				|| typeString.equals("char") || typeString.equals("java.lang.Character")
				|| typeString.equals("boolean") || typeString.equals("java.lang.Boolean")
				|| typeString.equals("double") || typeString.equals("java.lang.Double")
				|| typeString.equals("int") || typeString.equals("java.lang.Integer")
				|| typeString.equals("float") || typeString.equals("java.lang.Float")
				|| typeString.equals("long") || typeString.equals("java.lang.Long")
				|| typeString.equals("short") || typeString.equals("java.lang.Short")
				|| typeString.equals("java.lang.String"))
			return true;
		return false;
	}

	/**
	 * Getter for the current Shell.
	 * @return The Shell.
	 */
	public Shell getShell() {
		return this.shell;
	}

	/**
	 * Close the current window.
	 * @return true, if the closing was successfull, false otherwise.
	 */
	public synchronized boolean doExit() {
		if (!this.isClosing && !this.smallShell.isDisposed()) {
			this.isClosing = true;
			this.smallShell.close();
			this.smallShell.dispose();
			return true;
		}
		return false;
	}

}
