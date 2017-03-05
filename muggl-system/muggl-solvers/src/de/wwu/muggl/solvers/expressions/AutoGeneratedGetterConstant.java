package de.wwu.muggl.solvers.expressions;

public class AutoGeneratedGetterConstant implements Constant {

	protected String fullGetterName;
	
	protected Variable variable;
	
	public AutoGeneratedGetterConstant(String fullGetterName, Variable variable) {
		this.fullGetterName = fullGetterName;
		this.variable = variable;
	}
	
	public Variable getVariable() {
		return this.variable;
	}
	
	public String getFullGetterName() {
		return this.fullGetterName;
	}
	
	@Override
	public String toTexString() {
		return null;
	}

}
