package de.wwu.muggl.symbolic.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;

public class JPAEntityConstraint {

	private Class<?> entityClass;
	private String uuid;
	private Map<String, Variable> variables;
	private Map<String, JPAFieldConstraints> fieldConstraints;
	private Set<Class<?>> dependentEntities;
	
	public JPAEntityConstraint(Class<?> entityClass) {
		variables = new HashMap<String, Variable>();
		uuid = UUID.randomUUID().toString();
		fieldConstraints = new HashMap<String, JPAFieldConstraints>();
		dependentEntities = new HashSet<Class<?>>();
		this.entityClass = entityClass;
		JPAConstraintVariableManager.getInstance().addEntityConstraint(entityClass.getName(), this);
	}
	
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
	public void addInitialNumericVariable(String fieldName, String type) {
		NumericVariable nv = new NumericVariable(uuid+"_"+fieldName, type);
		this.variables.put(fieldName, nv);
	}
	
	public Set<String> getFields() {
		return variables.keySet();
	}
	
	public Variable getVariable(String field) {
		return variables.get(field);
	}
	
	public void addFieldConstraint(String fieldName, JPAFieldConstraints constraint) {
		this.fieldConstraints.put(fieldName, constraint);
	}
	
	public Map<String, JPAFieldConstraints> getFieldConstraints() {
		return this.fieldConstraints;
	}
	
	public void addDependentEntity(Class<?> constraint) {
		this.dependentEntities.add(constraint);
	}
		
	public Set<Class<?>> getDependentEntities() {
		return dependentEntities;
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
}
