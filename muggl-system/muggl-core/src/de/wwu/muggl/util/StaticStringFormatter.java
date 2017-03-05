package de.wwu.muggl.util;

/**
 * Provides static methods to format values into Strings.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class StaticStringFormatter {

	/**
	 * Protected default constructor.
	 */
	protected StaticStringFormatter() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Format a long value that represents a number of bytes. A separating dot will
	 * be added for each 1000 bytes and if there are more than 1024 bytes the short
	 * value in x bytes will be added in braces, where x is kilo, mega, giga etc.
	 * @param bytes The long value representing a number of bytes.
	 * @return The formated value as a String.
	 */
	public static String formatByteValue(long bytes) {
		String unformatedValue = String.valueOf(bytes);
		String formatedValue = "";

		// Add the dots.
		while (unformatedValue.length() > 3) {
			if (formatedValue.length() > 0) formatedValue = "." + formatedValue;
			formatedValue = unformatedValue.substring(unformatedValue.length() - 3) + formatedValue;
			unformatedValue = unformatedValue.substring(0, unformatedValue.length() - 3);
		}
		if (formatedValue.length() > 0) formatedValue = "." + formatedValue;
		formatedValue = unformatedValue + formatedValue + " Bytes";

		// Add the number in a greater unit, if needed.
		if (bytes > 1024) {
			double bytesDouble = bytes;
			int magnitude = 0;
			// Determine the magnitude.
			while (bytesDouble > 1024) {
				bytesDouble /= 1024;
				magnitude++;
				if (magnitude == 4) break;
			}

			// Round it.
			bytesDouble = Math.round(bytesDouble * 100d);
			bytesDouble /= 100d;

			// Format the value with the correct prefix.
			formatedValue += " (" + String.valueOf(bytesDouble).replace(".", ",");
			switch (magnitude) {
				case 1:
					formatedValue += " Kilo";
					break;
				case 2:
					formatedValue += " Mega";
					break;
				case 3:
					formatedValue += " Giga";
					break;
				case 4:
					formatedValue += " Tera";
					break;
			}
			formatedValue += "bytes)";
		}

		// Return the formated value.
		return formatedValue;
	}

}
