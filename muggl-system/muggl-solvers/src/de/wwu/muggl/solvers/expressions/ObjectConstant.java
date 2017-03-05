package de.wwu.muggl.solvers.expressions;

public class ObjectConstant implements Constant {

	protected Object reference;
	
	public ObjectConstant(Object reference) {
		this.reference = reference;
	}
	
	public Object getObjectReference() {
		return this.reference;
	}
	
	@Override
	public String toTexString() {
		return null;
	}

}
