package de.wwu.muggl.symbolic.jpa.var.entity;

import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.ReferenceValue;

public class EntityObjectReference implements ReferenceValue {

	private String entityClassName;
	private Map<String, Expression> entityFieldValueMap;
	
	public EntityObjectReference(String entityClassName) {
		this.entityClassName = entityClassName;
		this.entityFieldValueMap = new HashMap<>();
	}
	
	public String getEntityClassName() {
		return entityClassName;
	}
	
	public Expression getFieldValue(String fieldName) {
		return this.entityFieldValueMap.get(fieldName);
	}
	
	public void addFieldValue(String fieldName, Expression value) {
		this.entityFieldValueMap.put(fieldName, value);
	}
	
	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public String getName() {
		return "Entity reference value of: " + entityClassName;
	}

	@Override
	public InitializedClass getInitializedClass() {
		return null;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public long getInstantiationNumber() {
		return 0;
	}

}
