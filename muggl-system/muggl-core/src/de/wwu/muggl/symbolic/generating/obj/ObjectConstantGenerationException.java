package de.wwu.muggl.symbolic.generating.obj;

public class ObjectConstantGenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ObjectConstantGenerationException(String message, Throwable t) {
		super(message, t);
	}
	
	public ObjectConstantGenerationException(String message) {
		super(message);
	}
}
