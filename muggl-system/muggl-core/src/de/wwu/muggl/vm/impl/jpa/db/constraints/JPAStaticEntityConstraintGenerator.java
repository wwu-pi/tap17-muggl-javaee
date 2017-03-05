package de.wwu.muggl.vm.impl.jpa.db.constraints;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.wwu.muggl.symbolic.jpa.JPAFieldConstraints;

public class JPAStaticEntityConstraintGenerator {
	
	private Map<String, JPAStaticEntityConstraint> entityConstraintMap;
	
	public JPAStaticEntityConstraintGenerator() {
		this.entityConstraintMap = new HashMap<>();
	}

	public JPAStaticEntityConstraint generateEntityConstraints(String entityName) {
		JPAStaticEntityConstraint constraint = entityConstraintMap.get(entityName);
		if(constraint != null) {
			return constraint;
		}
		
		// generate new constraint, and put it on cache map at the end
		
		constraint = new JPAStaticEntityConstraint(entityName);
		
		// load entity class
		Class<?> entityClass;
		try {
			entityClass = ClassLoader.getSystemClassLoader().loadClass(entityName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not load class: " + entityName, e);
		}
		
		// check each field in entity class for static constraints, and add them
		for(Field field : entityClass.getDeclaredFields()) {
			if(java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			
			JPAFieldConstraints fieldConstraint = getJPAFieldConstraint(field);
			
			constraint.addFieldConstraint(field.getName(), fieldConstraint);
		}
		
		entityConstraintMap.put(entityName, constraint);
		
		return constraint;
	}
	
	private JPAFieldConstraints getJPAFieldConstraint(Field field) {
		JPAFieldConstraints fieldConstraint = new JPAFieldConstraints();
		
		// is field ID?
		fieldConstraint.setIsId(field.isAnnotationPresent(Id.class));
		try {
			Method method = field.getDeclaringClass().getMethod("get"+field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1));
			if(method.isAnnotationPresent(Id.class)) {
				fieldConstraint.setIsId(true);
			}
		} catch(Exception e) {
			// nothing to do here...
		}
		
		// is field 'not-null'?
		fieldConstraint.setIsNotNull(field.isAnnotationPresent(NotNull.class));
		
		Column columnAnnotation = field.getAnnotation(Column.class);
		if(columnAnnotation != null) {
			
			// is field unique?
			fieldConstraint.setIsUnique(columnAnnotation.unique());
		}
		
		// get minimum and maximum size
		Size sizeAnnotation = field.getAnnotation(Size.class);
		if(sizeAnnotation != null) {
			fieldConstraint.setMinSize(sizeAnnotation.min());
			fieldConstraint.setMaxSize(sizeAnnotation.max());
		}
		Min minAnnotation = field.getAnnotation(Min.class);
		if(minAnnotation != null) {
			fieldConstraint.setMinSize((int)minAnnotation.value());
		}
		Max maxAnnotation = field.getAnnotation(Max.class);
		if(maxAnnotation != null) {
			fieldConstraint.setMaxSize((int)maxAnnotation.value());
		}
		
		return fieldConstraint;
	}
}
