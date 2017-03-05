package de.wwu.muggl.solvers.expressions.objgen;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectGenerationConstraint;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectReferenceComparisonConstraint;
import de.wwu.muggl.solvers.type.IObjectreference;

public class ObjectReferenceEqual extends ObjectReferenceComparisonConstraint {
	
	public ObjectReferenceEqual(IObjectreference objectReference, Object objectEqualToObjectReference) {
		super(objectReference, objectEqualToObjectReference);
	}

	@Override
	public void checkTypes() throws TypeCheckException {
		
	}

	@Override
	public String toString(boolean useInternalVariableNames) {
		return "Object reference with id=["+ objectReference.getObjectId() +"] EQUAL to object=["+ objectToCompare +"]";
	}

	@Override
	public ConstraintExpression negate() {
		return new ObjectReferenceNotEqual(objectReference, objectToCompare);
	}

	
}
