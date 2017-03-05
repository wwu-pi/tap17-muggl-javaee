package de.wwu.muggl.symbolic.generating.jpa;

public class JPAAnalyzeException extends Exception {

	private static final long serialVersionUID = 1L;

	public JPAAnalyzeException(String message) {
		super(message);
	}
	
	public JPAAnalyzeException(String message, Throwable e) {
		super(message, e);
	}
}
