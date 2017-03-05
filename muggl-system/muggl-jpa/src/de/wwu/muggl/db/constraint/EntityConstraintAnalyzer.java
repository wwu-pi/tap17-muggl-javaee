package de.wwu.muggl.db.constraint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;


public class EntityConstraintAnalyzer {

	@Deprecated//this map does not consider composite primary keys, i.e. mutliple attributes having an @Id annotation
	private static Map<String, String> entityIdMapOLD = new HashMap<>();
	
	private static Map<String, Set<String>> entityIdMap = new HashMap<>();
	
	public static Set<String> getIdFieldNames(Metamodel metamodel, String entityName) {
		Set<String> ids = entityIdMap.get(entityName);
		if(ids == null) {
			ids = new HashSet<>();
			for(EntityType<?> et : metamodel.getEntities()) {
				if(et.getJavaType().getName().equals(entityName)) {
					if(et.hasSingleIdAttribute()) {
						ids.add(et.getId(et.getIdType().getJavaType()).getName());
					} else {
						for(SingularAttribute<?, ?> idAttribute : et.getIdClassAttributes()) {
							ids.add(idAttribute.getName());
						}
					}
				}
			}
		}
		if(ids.size() == 0) {
			ids.add("id");
		}
		return ids;
	}

	
	//this method does not consider composite primary keys, i.e. mutliple attributes having an @Id annotation
	@Deprecated
	public static String getIdFieldName(String entityName) {
		String id = entityIdMapOLD.get(entityName);
		Class<?> clazz = null;
		if(id == null) {
			try {
				clazz = ClassLoader.getSystemClassLoader().loadClass(entityName);
				
				// first, check fields
				for(Field field : clazz.getDeclaredFields()) {
					if(field.isAnnotationPresent(Id.class)) {
						id = field.getName();
						entityIdMapOLD.put(entityName, id);
						return id;
					}
				}
				
				// second, check methods
				for(Method method : clazz.getDeclaredMethods()) {
					if(method.isAnnotationPresent(Id.class)) {
						String methodName = method.getName();
						id = methodName.substring(3,4).toLowerCase() + methodName.substring(4);
						try {
							clazz.getDeclaredField(id);
						} catch(NoSuchFieldException e) {
							throw new RuntimeException("sorry, but ID field for method annotated with @Id not found..");
						}
						entityIdMapOLD.put(entityName, id);
						return id;
					}
				}
//				"get"+field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)
			} catch(Exception e) {
				// 
			}
		}
		
		if(id == null && clazz != null) {
			// check super class for ID attribute
			if(clazz.getSuperclass() != null && clazz.getSuperclass().isAnnotationPresent(Entity.class)) {
				return getIdFieldName(clazz.getSuperclass().getName());
			}
		}
		
		return id;
	}
}
