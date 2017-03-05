package de.wwu.muggl.util;

public class HostEnvironment {
	private static int minor = -1;
	private static int major = -1;
	
	public static int getMinor() {
		if (minor == -1) {
			splitVersionString();
		}
		return minor;
	}
	
	public static int getMajor() {
		if (major == -1) {
			splitVersionString();
		}
		return major;
	}

	private static void splitVersionString() {
		String version = System.getProperty("java.version");
		String[] parts = version.split("\\.");
		if (parts.length < 2) {
			throw new HostEnvironmentNotCompatibleException(version);
		}
		major = Integer.parseInt(parts[0]);
		minor = Integer.parseInt(parts[1]);
	}
	
}