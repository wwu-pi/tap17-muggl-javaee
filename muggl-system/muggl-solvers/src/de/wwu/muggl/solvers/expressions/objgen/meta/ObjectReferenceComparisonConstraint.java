package de.wwu.muggl.solvers.expressions.objgen.meta;

import de.wwu.muggl.solvers.type.IObjectreference;

public abstract class ObjectReferenceComparisonConstraint extends ObjectGenerationConstraint {

	protected IObjectreference objectReference;
	protected Object objectToCompare;
	
	public ObjectReferenceComparisonConstraint(IObjectreference objectReference, Object objectToCompare) {
		this.objectReference = objectReference;
		this.objectToCompare = objectToCompare;
	}
	
	public IObjectreference getObjectReference() {
		return this.objectReference;
	}
	
	public Object getObjectToCompare() {
		return this.objectToCompare;
	}
	
}
