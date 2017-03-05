package de.wwu.muggl.vm.impl.jpa.db.constraints;

import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.symbolic.jpa.JPAFieldConstraints;

public class JPAStaticEntityConstraint {

	private String entityClassName;
	private Map<String, JPAFieldConstraints> fieldConstraints;

	public JPAStaticEntityConstraint(String entityClassName) {
		this.entityClassName = entityClassName;
		fieldConstraints = new HashMap<String, JPAFieldConstraints>();
	}
	
	public void addFieldConstraint(String fieldName, JPAFieldConstraints constraint) {
		this.fieldConstraints.put(fieldName, constraint);
	}
	
	public Map<String, JPAFieldConstraints> getFieldConstraints() {
		return this.fieldConstraints;
	}
	
	public String getIdField() {
		for(String fieldName : fieldConstraints.keySet()) {
			JPAFieldConstraints fieldConstraint = fieldConstraints.get(fieldName);
			if(fieldConstraint.isId()) {
				return fieldName;
			}
		}
		return null;
	}

	public String getEntityClassName() {
		return this.entityClassName;
	}
}
