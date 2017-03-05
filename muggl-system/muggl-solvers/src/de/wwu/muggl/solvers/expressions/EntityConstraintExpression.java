package de.wwu.muggl.solvers.expressions;

public class EntityConstraintExpression {

	protected String entityName;
	protected ConstraintExpression constraint;
	
	public EntityConstraintExpression(String entityName, ConstraintExpression constraint) {
		super();
		this.entityName = entityName;
		this.constraint = constraint;
	}
	
	public String getEntityName() {
		return entityName;
	}
	public ConstraintExpression getConstraint() {
		return constraint;
	}
	
}
