package de.wwu.muggl.util;

public class HostEnvironmentNotCompatibleException extends RuntimeException {

	private static final long serialVersionUID = -3376226973850693841L;

	public HostEnvironmentNotCompatibleException(String arg0) {
		super("Host VM version not (yet) compatible: "+arg0);
	}
}