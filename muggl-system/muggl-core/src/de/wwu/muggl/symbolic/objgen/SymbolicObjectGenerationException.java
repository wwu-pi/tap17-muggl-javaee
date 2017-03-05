package de.wwu.muggl.symbolic.objgen;

public class SymbolicObjectGenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public SymbolicObjectGenerationException(String message, Throwable t) {
		super(message, t);
	}
	
	public SymbolicObjectGenerationException(String message) {
		super(message);
	}

}
