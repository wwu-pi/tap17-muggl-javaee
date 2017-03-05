package de.wwu.muggl.solvers.expressions;

import java.util.Map;

public class StringVariable extends NumericVariable {

	protected Map<Integer,NumericVariable> elements;
	protected NumericVariable length;
	
	public StringVariable(String name, NumericVariable length, Map<Integer, NumericVariable> elements) {
		super(name, Expression.STRING);
		this.length = length;
		this.elements = elements;
	}
	
	public NumericVariable getLength() {
		return this.length;
	}
	
	public Map<Integer, NumericVariable> getElements() {
		return this.elements;
	}
}
