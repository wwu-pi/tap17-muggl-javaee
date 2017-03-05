package de.wwu.muggl.ui.gui.support;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.MugglException;

/**
 * This class offers a couple of often needed methods and constants for the GUI, especially to show
 * message boxes and to calculate the needed size and position of windows. It cannot be
 * instantiated.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public final class StaticGuiSupport {
	/**
	 * A short wide hint for {@link GridData} instances.
	 */
	public static final int SWT_GRID_WIDTH_HINT_SHORT = 80;
	/**
	 * A medium wide hint for {@link GridData} instances.
	 */
	public static final int SWT_GRID_WIDTH_HINT_MEDIUM = 100;
	/**
	 * A long wide hint for {@link GridData} instances.
	 */
	public static final int SWT_GRID_WIDTH_HINT_LONG = 120;
	
	/**
	 * White color.
	 */
	public static final RGB RGB_WHITE = new RGB(255, 255, 255); 
	
	/**
	 * Private constructor: This class cannot be instantiated.
	 */
	private StaticGuiSupport() { }

	/**
	 * Show a message box in the Shell shell with the supplied Message. The window
	 * title will be "Information", it will provide an "OK"-Button and have an
	 * Information-Icon.
	 *
	 * This Method is overloaded and offers two more customizable alternatives.
	 * @param shell The Shell to display the message box in.
	 * @param message The message that box is to be filled with.
	 * @return The response from the message box as an int.
	 */
	public static int showMessageBox(Shell shell, String message) {
		return showMessageBox(shell, "Information", message, SWT.OK | SWT.ICON_INFORMATION);
	}

	/**
	 * Show a message box in the Shell shell with the supplied Message. The window
	 * title will be "Information", while its' style is determind by the supplied
	 * parameter type.
	 *
	 * This Method is overloaded and offers one more simply and one more customizable alternatives.
	 * @param shell The Shell to display the message box in.
	 * @param message The message that box is to be filled with.
	 * @param type The type of the message box.
	 * @return The response from the message box as an int.
	 */
	public static int showMessageBox(Shell shell, String message, int type) {
		return showMessageBox(shell, "Information", message, type);
	}

	/**
	 * Show a message box in the Shell shell with the supplied parameters.
	 *
	 * This Method is overloaded and offers two more simply alternatives.
	 * @param shell The Shell to display the message box in.
	 * @param text The window title of the message box.
	 * @param message The message that box is to be filled with.
	 * @param type The type of the message box.
	 * @return The response from the message box as an int.
	 */
	public static int showMessageBox(Shell shell, String text, String message, int type) {
		MessageBox messageBox = new MessageBox(shell, type);
		messageBox.setMessage(message);
		messageBox.setText(text);
		return messageBox.open();
	}

	/**
	 * Get the centered position for a window according to its' parent Shell
	 * and its' width and height.
	 * @param myWidth The windows' width.
	 * @param myHeight The windows' height.
	 * @param parentShell The parent Shell.
	 * @return An array containing the X and the Y position.
	 */
	public static int[] getCenteredPosition(int myWidth, int myHeight, Shell parentShell) {
		// Calculate the current position.
		Rectangle r = parentShell.getBounds();
		Rectangle displayBounds = parentShell.getDisplay().getBounds();
		int parentCenterX = r.x + (r.width / 2);
		int parentCenterY = r.y + (r.height / 2);
		int[] myPos = new int[2];
		myPos[0] = parentCenterX - (myWidth / 2);
		myPos[1] = parentCenterY - (myHeight / 2);

		// Leaving upper or left bounds?
		if (myPos[0] < 0) myPos[0] = 0;
		if (myPos[1] < 0) myPos[1] = 0;

		// Leaving lower or right bounds?
		if (myPos[0] > displayBounds.width - myWidth) myPos[0] = displayBounds.width - myWidth;
		if (myPos[1] > displayBounds.height - myHeight) myPos[1] = displayBounds.height - myHeight;

		// Finished.
		return myPos;
	}

	/**
	 * Get the centered position for a window on the primary monitor according
	 * to its' width and height.
	 * @param myWidth The windows' width.
	 * @param myHeight The windows' height.
	 * @param display The Display.
	 * @return An array containing the X and the Y position.
	 */
	public static int[] getCenteredPosition(int myWidth, int myHeight, Display display) {
		// Calculate the current position.
		Rectangle displayBounds = display.getPrimaryMonitor().getBounds();
		int[] myPos = new int[2];
		myPos[0] = displayBounds.width / 2 - (myWidth / 2);
		myPos[1] = displayBounds.height / 2 - (myHeight / 2);

		// Finished.
		return myPos;
	}

	/**
	 * Process an error that requires a window of the gui to be closed. The Throwable is
	 * processed, the logging is differentiated between Exceptions and Errors. Also a
	 * message box is generated, informing the user what has happened.
	 * @param t The Throwable to process.
	 * @param windowName The windows' name that is to be inserted into the message.
	 * @param shell The Shell to display the message box in. Should be the Shell of the parent window of the "crashing" one.
	 */
	public static void processGuiError(Throwable t, String windowName, Shell shell) {
		String message = "rror in the SWT Gui: The " + windowName + " window was shut unexpectedly. The root reason is an ";
		if (t instanceof MugglException) {
			message = "E" + message;
			message += Globals.APP_NAME + " Exception";
		} else if (t instanceof Error) {
			message = "Serious e" + message;
			message += "Error";
		} else {
			message = "E" + message;
			message += "Exception";
		}
		message += " of type " + t.getClass().getName() + " with the message: " + t.getMessage();

		Globals constants = Globals.getInst();
		if (constants.guiLogger.isEnabledFor(Level.ERROR)) constants.guiLogger.error(message);

		/*
		 * If Logging is at least in debug mode, log the full stack trace. Exceptions that have this method
		 * called to be handled hint to general bugs in the application. Logging the full stack
		 * trace should help to find and fix them.
		 */
		if (constants.guiLogger.isDebugEnabled()) {
			String toLog = "The exception that required the gui window to be closed:<br />\n"
							+ t.getClass().getName();
			if (t.getMessage() != null) toLog += "(" + t.getMessage() + ")";
			toLog += "<br>\n";
			StackTraceElement[] elements = t.getStackTrace();
			for (StackTraceElement element : elements) {
				toLog += "&nbsp;&nbsp;&nbsp;" + element.toString() + "<br />\n";
			}
			synchronized (constants) {
				constants.setLoggingMessagesEscaping(false);
				constants.guiLogger.debug(toLog);
				constants.setLoggingMessagesEscaping(true);
			}
		}
		showMessageBox(shell, "Error", message, SWT.OK | SWT.ICON_ERROR);
	}

	/**
	 * Format a numeric value. Numeric values must be the String representations of
	 * double, float, int or long values. Other, especially pre-formated input values
	 * will lead to unexpected results.
	 * @param numericValue The String representations of a double, float, int or long value.
	 * @param maxDecimalPlaces The maximum decimal places. Rounding will be used.
	 * @return The formated value.
	 */
	public static String formatNumericValue(String numericValue, int maxDecimalPlaces) {
		try {
			// Is it a real String value already that is not desired for output?
			if (numericValue.equals("Infinity") || numericValue.equals("NaN"))
				return "0";

			// Is it a value in the form valueEexponent?
			if (numericValue.contains("E")) {
				// Loose precision, but it can at least be handled then.
				numericValue = String.valueOf(Double.valueOf(Double.parseDouble(numericValue)).longValue());
			}

			// Variables.
			String decimalPlaces = "";
			String formatedValue = "";
			// Are there decimal places?
			int position = numericValue.indexOf(".");
			if (position != -1) {
				decimalPlaces = numericValue.substring(position + 1);
				numericValue = numericValue.substring(0, position);
				// Cut the decimal places.
				if (decimalPlaces.equals("0")) {
					decimalPlaces = "";
				} else if (decimalPlaces.length() > maxDecimalPlaces) {
					decimalPlaces = decimalPlaces.substring(0, maxDecimalPlaces) + "." + decimalPlaces.substring(maxDecimalPlaces);
					decimalPlaces = String.valueOf(Math.round(Double.parseDouble(decimalPlaces)));
					decimalPlaces = "," + decimalPlaces;
				} else {
					decimalPlaces = "," + decimalPlaces;
				}
			}

			// Format the value.
			while (numericValue.length() > 3) {
				if (formatedValue.length() > 0) formatedValue = " " + formatedValue;
				formatedValue = numericValue.substring(numericValue.length() - 3) + formatedValue;
				numericValue = numericValue.substring(0, numericValue.length() - 3);
			}
			if (formatedValue.length() > 0) formatedValue = " " + formatedValue;
			formatedValue = numericValue + formatedValue;

			// Combine and return the String.
			return formatedValue + decimalPlaces;
		} catch (NumberFormatException e) {
			return "(Number is not formatable.)";
		}
	}

	/**
	 * Format a numeric value of type double.
	 * @param numericValue A double value.
	 * @param maxDecimalPlaces The maximum decimal places. Rounding will be used.
	 * @return The formated value.
	 */
	public static String formatNumericValue(double numericValue, int maxDecimalPlaces) {
		return formatNumericValue(String.valueOf(numericValue), maxDecimalPlaces);
	}

	/**
	 * Format a numeric value of type float.
	 * @param numericValue A float value.
	 * @param maxDecimalPlaces The maximum decimal places. Rounding will be used.
	 * @return The formated value.
	 */
	public static String formatNumericValue(float numericValue, int maxDecimalPlaces) {
		return formatNumericValue(String.valueOf(numericValue), maxDecimalPlaces);
	}

	/**
	 * Format a numeric value of type int.
	 * @param numericValue A int value.
	 * @param maxDecimalPlaces The maximum decimal places. (Not of relevance for an int value.)
	 * @return The formated value.
	 */
	public static String formatNumericValue(int numericValue, int maxDecimalPlaces) {
		return formatNumericValue(String.valueOf(numericValue), maxDecimalPlaces);
	}

	/**
	 * Format a numeric value of type int.
	 * @param numericValue A int value.
	 * @return The formated value.
	 */
	public static String formatNumericValue(int numericValue) {
		return formatNumericValue(String.valueOf(numericValue), 0);
	}

	/**
	 * Format a numeric value of type long.
	 * @param numericValue A long value.
	 * @param maxDecimalPlaces The maximum decimal places. (Not of relevance for a long value.)
	 * @return The formated value.
	 */
	public static String formatNumericValue(long numericValue, int maxDecimalPlaces) {
		return formatNumericValue(String.valueOf(numericValue), maxDecimalPlaces);
	}

	/**
	 * Format a numeric value of type long.
	 * @param numericValue A long value.
	 * @return The formated value.
	 */
	public static String formatNumericValue(long numericValue) {
		return formatNumericValue(String.valueOf(numericValue), 0);
	}

	/**
	 * Convert an ArrayList of String objects into an array of String.
	 * @param arrayList The ArrayList to be converted.
	 * @return A String[] with arrayList.size() dimensions.
	 */
	public static String[] arrayList2StringArray(List<String> arrayList) {
		String[] array = new String[arrayList.size()];
		Iterator<String> iterator = arrayList.iterator();
		int a = 0;
		while (iterator.hasNext()) {
			array[a] = iterator.next();
			a++;
		}
		return array;
	}

	/**
	 * Get the formated String for a line number for a set line number length.
	 * @param lineNumber The line number to format.
	 * @param lineNumberLength The desired length (without ": ").
	 * @param addColon Toggles whether a colon and a space is inserted to finish the line.
	 * @return The formated line number.
	 */
	public static String getFormatedIndexNumber(int lineNumber, int lineNumberLength, boolean addColon) {
		String lineNumberString = String.valueOf(lineNumber);
		for (int a = lineNumberString.length(); a < lineNumberLength; a++) {
			lineNumberString = "0" + lineNumberString;
		}
		if (addColon) {
			lineNumberString += ": ";
		}
		return lineNumberString;
	}

	/**
	 * Call getFormatedIndexNumber(lineNumber, lineNumberLength, addColon) with a lineNumberLength
	 * calculated from the maximum number that will be encountered. For this, simply the length of the
	 * String representation of the number is taken.
	 *
	 * @param lineNumber The line number to format.
	 * @param maximumNumber The maximum number that the line number is generated in relation to.
	 * @param addColon Toggles wheteher a colon and a space is inserted to finish the line.
	 * @return The formated line number.
	 */
	public static String getFormatedIndexNumberByMaximumNumber(int lineNumber, int maximumNumber, boolean addColon) {
		return getFormatedIndexNumber(lineNumber, String.valueOf(maximumNumber).length(), addColon);
	}
}
