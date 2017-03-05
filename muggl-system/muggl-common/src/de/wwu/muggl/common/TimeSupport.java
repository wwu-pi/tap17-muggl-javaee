package de.wwu.muggl.common;

/**
 * Helper class providing static methods that deal with time.
 *  
 * @author Jan Dageförde
 */
public class TimeSupport {

	// Constants.
	/**
	 * Milliseconds per second.
	 */
	public static final int MILLIS_SECOND = 1000;
	/**
	 * Milliseconds per second.
	 */
	public static final double MILLIS_SECOND_D = 1000D;
	/**
	 * Milliseconds per minute.
	 */
	public static final int MILLIS_MINUTE = 60000;
	/**
	 * Milliseconds per hour.
	 */
	public static final int MILLIS_HOUR = 3600000;
	/**
	 * Milliseconds per day.
	 */
	public static final int MILLIS_DAY = 86400000;
	/**
	 * Seconds per minute.
	 */
	public static final int SECONDS_MINUTE = 60;
	/**
	 * Seconds per hour.
	 */
	public static final int SECONDS_HOUR = 3600;
	/**
	 * Seconds per day.
	 */
	public static final int SECONDS_DAY = 86400;
	/**
	 * Return a String representation of the current running time. It will contain detailed
	 * time information in the form days, hours, minutes, seconds, miliseconds. Values that
	 * are zero wil not be shown.
	 * @param timeRun The running time to compute.
	 * @param accuracyWarning If set to true, a warning regarding the accuracy will be appended if the time is below one second.
	 * @return A String representation of the current running time, or "infinite", if the time value is -1.
	 */
	public static String computeRunningTime(long timeRun, boolean accuracyWarning) {
		// If the value is -1, just return "infinite".
		if (timeRun == -1) return "infinite";
	
		// Initialize the values.
		String time = "";
		int days = 0, hours = 0, minutes = 0, seconds = 0;
		// Compute the single values.
		while (timeRun >= MILLIS_DAY) {
			timeRun -= MILLIS_DAY;
			days++;
		}
		while (timeRun >= MILLIS_HOUR) {
			timeRun -= MILLIS_HOUR;
			hours++;
		}
		while (timeRun >= MILLIS_MINUTE) {
			timeRun -= MILLIS_MINUTE;
			minutes++;
		}
		while (timeRun >= MILLIS_SECOND) {
			timeRun -= MILLIS_SECOND;
			seconds++;
		}
	
		// Build the String.
		if (days > 0) time = days + " days";
		if (hours > 0) {
			if (time.length() > 0) time += ", ";
			time += hours + " hours";
		}
		if (minutes > 0) {
			if (time.length() > 0) time += ", ";
			time += minutes + " minutes";
		}
		if (seconds > 0) {
			if (time.length() > 0) time += ", ";
			time += seconds + " seconds";
		}
		if (timeRun > 0 || time.length() == 0) {
			if (time.length() > 0) time += ", ";
			time += timeRun + " miliseconds";
		}
	
		// Additional information?
		if (accuracyWarning && days == 0 && hours == 0 && minutes == 0 && seconds == 0)
			time += " (The value might be too high due to threading issues.)";
	
		// Return it.
		return time;
	}

}
