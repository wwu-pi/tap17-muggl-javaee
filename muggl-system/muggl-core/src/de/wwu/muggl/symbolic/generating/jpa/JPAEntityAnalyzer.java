package de.wwu.muggl.symbolic.generating.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.jpa.JPAConstraintVariableManager;
import de.wwu.muggl.symbolic.jpa.JPAEntityConstraint;
import de.wwu.muggl.symbolic.jpa.JPAFieldConstraints;

public class JPAEntityAnalyzer {
	
	public JPAEntityConstraint generateInitialEntityConstraint(Class<?> entityClass, SolverManager solverManager) throws JPAAnalyzeException {
		return generateInitialEntityConstraint(entityClass, null, solverManager);
	}

	protected JPAEntityConstraint generateInitialEntityConstraint(Class<?> entityClass, Class<?> referencedClass, SolverManager solverManager) throws JPAAnalyzeException {
		JPAEntityConstraint entityConstraint = new JPAEntityConstraint(entityClass);
		
		for(Field field : entityClass.getDeclaredFields()) {
			if(java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			
			if(field.getType().equals(Integer.class) || field.getType().getName().equals("int")) {
				entityConstraint.addInitialNumericVariable(field.getName(), "int");
			} else if(field.getType().equals(Long.class) || field.getType().getName().equals("long")) {
				entityConstraint.addInitialNumericVariable(field.getName(), "long");
			} else if(field.getType().equals(Double.class) || field.getType().getName().equals("double")) {
				entityConstraint.addInitialNumericVariable(field.getName(), "double");
			} else if(field.getType().equals(Collection.class)) {
				Type genericType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
				String genericTypeName = genericType.toString().substring(6);
				try {
					Class<?> collectionEntityClass = ClassLoader.getSystemClassLoader().loadClass(genericTypeName);
					if(collectionEntityClass.isAnnotationPresent(Entity.class)) {
						entityConstraint.addDependentEntity(collectionEntityClass);
						String mappedBy = null;
						if(field.isAnnotationPresent(OneToMany.class)) {
							// TODO: we assume, mappedBy attribute is given when used @OneToMany
							mappedBy = field.getAnnotation(OneToMany.class).mappedBy();
						} else if(field.isAnnotationPresent(ManyToMany.class)) {
							// TODO: we assume, mappedBy attribute is given when used @OneToMany
							mappedBy = field.getAnnotation(ManyToMany.class).mappedBy();
						}
						if(referencedClass == null || !referencedClass.getName().equals(collectionEntityClass.getName())) {
							addForeignKey(entityConstraint, collectionEntityClass, mappedBy, solverManager);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else if(field.getType().equals(String.class) || field.getType().getName().equals("String")) {
				// TODO: String is not int, you know?
				entityConstraint.addInitialNumericVariable(field.getName(), "int");
			} else if(field.getType().isAnnotationPresent(Entity.class)) {
				// it is an entity field
				try {
					Class<?> fieldTypeClass = ClassLoader.getSystemClassLoader().loadClass(field.getType().getName());
					if(fieldTypeClass.isAnnotationPresent(Entity.class)) {
						entityConstraint.addDependentEntity(fieldTypeClass);
						
						
						// TODO: add foreign constraint
//						NumericVariable nv = (NumericVariable)entityConstraint.getVariable(field.getName());						
//						solverManager.addConstraint(NumericEqual.newInstance(nv, nv));						
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new JPAAnalyzeException("Error while getting class type ["+field.getType() +"] for field [" + field.getName() + "] in entity ["+ entityClass.getName() + "]");
				}
			}
			
			JPAFieldConstraints jpaFieldConnstraint = getJPAFieldConstraint(field);
			entityConstraint.addFieldConstraint(field.getName(), jpaFieldConnstraint);
			
			Variable variable = entityConstraint.getVariable(field.getName());
			if(variable != null) {
				if(jpaFieldConnstraint.getMinSize() != null) {
					if(variable instanceof NumericVariable) {
						solverManager.addConstraint(GreaterOrEqual.newInstance((NumericVariable)variable, IntConstant.getInstance(jpaFieldConnstraint.getMinSize())));
					}
				}
			}
		}
		
		return entityConstraint;
	}
	
	
/*
	public JPAEntityConstraint generateInitialEntityConstraintOLD(Class<?> entityClass, SolverManager solverManager) throws JPAAnalyzeException {
		
		JPAEntityConstraint entityConstraint = new JPAEntityConstraint(entityClass);
//		entityConstraint.addDependendEntities(getRequiredEntities(entityClass));
		for(Field field : entityClass.getDeclaredFields()) {
			JPAFieldConstraints jpaFieldConnstraint = getJPAFieldConstraint(field);
			entityConstraint.addFieldConstraint(field.getName(), jpaFieldConnstraint);
			
			if(field.getType().equals(Integer.class) || field.getType().getName().equals("int")) {
				entityConstraint.addInitialNumericVariable(field.getName(), "int");
			} else if(field.getType().equals(Long.class) || field.getType().getName().equals("long")) {
				entityConstraint.addInitialNumericVariable(field.getName(), "long");
			} else if(field.getType().equals(Double.class) || field.getType().getName().equals("double")) {
				entityConstraint.addInitialNumericVariable(field.getName(), "double");
//			}
//			if(field.getType().equals(Integer.class) || field.getType().equals(Long.class) || field.getType().getName().equals("int") || field.getType().getName().equals("long")) {
//				entityConstraint.addInitialNumericVariable(field.getName());
			} else if(field.getType().equals(Collection.class) && field.isAnnotationPresent(OneToMany.class)) {
				Type genericType = field.getGenericType();
				if(genericType instanceof ParameterizedType ) {
					ParameterizedType pType = (ParameterizedType )genericType;
					Type realGenericType = pType.getActualTypeArguments()[0];
//					String typeName = realGenericType.getTypeName();
					throw new RuntimeException("Sorry, that does not work under Java 1.7");
//					try {
//						Class<?> collectionEntityClass = ClassLoader.getSystemClassLoader().loadClass(typeName);
//						entityConstraint.addDependendEntity(collectionEntityClass);
//						
//						// TODO: we assume, mappedBy attribute is given when used @OneToMany
//						String mappedBy = field.getAnnotation(OneToMany.class).mappedBy();
//						addForeignKey(entityConstraint, collectionEntityClass, mappedBy, solverManager);
//					} catch (ClassNotFoundException e) {
//						e.printStackTrace();
//					}
				}
			} else if(field.getType().equals(String.class)) {
				// TODO: string is not integer ... you know?
				entityConstraint.addInitialNumericVariable(field.getName(), "int");
			} else {
				// it must be an entity type
				try {
					Class<?> fieldTypeClass = ClassLoader.getSystemClassLoader().loadClass(field.getType().getName());
					if(fieldTypeClass.isAnnotationPresent(Entity.class)) {
						entityConstraint.addDependendEntity(fieldTypeClass);
						entityConstraint.addInitialNumericVariable(field.getName());
						
						
						// TODO: add foreign constraint
//						NumericVariable nv = (NumericVariable)entityConstraint.getVariable(field.getName());						
//						solverManager.addConstraint(NumericEqual.newInstance(nv, nv));						
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new JPAAnalyzeException("Error while getting class type ["+field.getType() +"] for field [" + field.getName() + "] in entity ["+ entityClass.getName() + "]");
				}
				
//				throw new JPAAnalyzeException("Cannot handle fields of type: ["+field.getType()+ "] in entity: ["+entityClass.getName()+"]");
			}
		}
		return entityConstraint;
	}
*/
	
	private void addForeignKey(JPAEntityConstraint entityConstraint, Class<?> foreignEntityClass, String mappedBy, SolverManager solverManager) throws JPAAnalyzeException {
		JPAEntityConstraint foreignEntityConstraint = generateInitialEntityConstraint(foreignEntityClass, entityConstraint.getEntityClass(), solverManager);
		NumericVariable mappedVar = (NumericVariable)foreignEntityConstraint.getVariable(mappedBy);
		
		String idField = entityConstraint.getIdField();
		NumericVariable idVar = (NumericVariable)entityConstraint.getVariable(idField);
		
		solverManager.addConstraint(NumericEqual.newInstance(mappedVar, idVar));
	}

	private JPAFieldConstraints getJPAFieldConstraint(Field field) {
		JPAFieldConstraints fieldConstraint = new JPAFieldConstraints();
		
		// is field ID?
		fieldConstraint.setIsId(field.isAnnotationPresent(Id.class));
		
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
	
	
//	public Set<Class<?>> getRequiredEntities(Class<?> entityClass) {
//		return getRequiredEntities(new HashSet<Class<?>>(), entityClass);
//	}
//	
//	private Set<Class<?>> getRequiredEntities(Set<Class<?>> requiredEntities, Class<?> entityClass) {
//		// TODO: currently no inheritance supported, just in own class
//		for(Field field : entityClass.getDeclaredFields()) {
//			if(field.getType().getDeclaredAnnotation(Entity.class) != null) {
//				requiredEntities = addIfNotInField(requiredEntities, field.getType());
//			} else if(field.getType().isAssignableFrom(Collection.class)) {
//				Type genericType = field.getGenericType();
//				if(genericType instanceof ParameterizedType ) {
//					ParameterizedType pType = (ParameterizedType )genericType;
//					Type realGenericType = pType.getActualTypeArguments()[0];
//					String typeName = realGenericType.getTypeName();
//					try {
//						Class<?> collectionEntityClass = ClassLoader.getSystemClassLoader().loadClass(typeName);
//						requiredEntities = addIfNotInField(requiredEntities, collectionEntityClass);
////						getRequiredEntities(requiredEntities, collectionEntityClass);
//					} catch (ClassNotFoundException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//		return requiredEntities;
//	}
//	
//	private Set<Class<?>> addIfNotInField(Set<Class<?>> requiredEntities, Class<?> entityClass) {
//		boolean isAlreadyInList = requiredEntities.contains(entityClass);
//		requiredEntities.add(entityClass);
//		if(!isAlreadyInList) {
//			getRequiredEntities(requiredEntities, entityClass);
//		}
//		return requiredEntities;
//	}
//	
//
//	public void analyze(Class<?> entityClass) throws JPAAnalyzeException{
//		
//		for(Field field : entityClass.getDeclaredFields()) {
//			System.out.println("Field: " + field);
//		}
//	}
	
	
}
