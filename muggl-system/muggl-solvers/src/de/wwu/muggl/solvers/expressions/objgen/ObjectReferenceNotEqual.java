package de.wwu.muggl.solvers.expressions.objgen;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectGenerationConstraint;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectReferenceComparisonConstraint;
import de.wwu.muggl.solvers.type.IObjectreference;

public class ObjectReferenceNotEqual extends ObjectReferenceComparisonConstraint {
	
	
	public ObjectReferenceNotEqual(IObjectreference objectReference, Object objectNotEqualToObjectReference) {
		super(objectReference, objectNotEqualToObjectReference);
	}

	@Override
	public void checkTypes() throws TypeCheckException {
		
	}

	@Override
	public String toString(boolean useInternalVariableNames) {
		return "Object reference with id=["+ objectReference.getObjectId() +"] NOT EQUAL to object=["+ objectToCompare +"]";
	}

	@Override
	public ConstraintExpression negate() {
		return new ObjectReferenceEqual(objectReference, objectToCompare);
	}

	
}
