//package de.wwu.muggl.symbolic.generating.jpa;
//
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import de.wwu.muggl.jpa.FindResult;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.var.CollectionVariableImpl;
//import de.wwu.muggl.vm.VirtualMachine;
//import de.wwu.muggl.vm.classfile.ClassFile;
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class JPAConstraintManager {
//	
//	protected JPAVirtualMachine vm;
//	
//	private Map<String, Map<String, Map<String, Variable>>> allEntitiesVariableMap;
//	
//	private Map<String, Objectref> entityId2Objectref;
//	private Map<Objectref, String> entityObjectref2Id;
//
//	public JPAConstraintManager(JPAVirtualMachine vm) {
//		this.vm = vm;
//		this.allEntitiesVariableMap = new HashMap<>();
//		this.entityId2Objectref = new HashMap<>();
//		this.entityObjectref2Id = new HashMap<>();
//	}
//	
//	public Objectref getEntityObjectrefFromId(String entityId) {
//		return this.entityId2Objectref.get(entityId);
//	}
//	
//	public String getEntityIdFromObjectref(Objectref objectRef) {
//		return this.entityObjectref2Id.get(objectRef);
//	}
//	
//	public FindResult generateInitialFindResultEntity(String entityClassName) {
//		try {
//			ClassFile entityClassFile =  vm.getClassLoader().getClassAsClassFile(entityClassName);
//			Class<?> entityClass = ClassLoader.getSystemClassLoader().loadClass(entityClassName);
//			
//			FindResult objectRef = vm.getAFindResultObjectref(entityClassFile);
//			
//			fillEntity(entityClassFile, entityClass, entityClassName, objectRef);
//
//			return objectRef;
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public Objectref generateInitialEntity(String entityClassName) {
//		try {
//			ClassFile entityClassFile =  vm.getClassLoader().getClassAsClassFile(entityClassName);
//			Class<?> entityClass = ClassLoader.getSystemClassLoader().loadClass(entityClassName);
//			
//			Objectref objectRef = vm.getAnObjectref(entityClassFile);
//			fillEntity(entityClassFile, entityClass, entityClassName, objectRef);
//			
//			return objectRef;
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public Objectref fillEntity(ClassFile entityClassFile, Class<?> entityClass, String entityClassName, Objectref objectRef) {
//		try {
//			String uuid = UUID.randomUUID().toString();
//			
//			Map<String, Map<String, Variable>> entityVars = allEntitiesVariableMap.get(entityClassName);
//			if(entityVars == null) {
//				entityVars = new HashMap<>();
//			}
//						
//			Map<String, Variable> entityVarMap = entityVars.get(uuid);
//			if(entityVarMap == null) {
//				entityVarMap = new HashMap<>();
//			}
//			
//			for(Field field : entityClassFile.getFields()) {
//				if(!field.isAccStatic()) {
//					Variable variable = getInitialVariable(entityClassName, uuid, field.getType(), entityClass.getDeclaredField(field.getName()));
//					objectRef.putField(field, variable);
//					entityVarMap.put(field.getName(), variable);
//				}
//			}
//			
//			entityVars.put(uuid, entityVarMap);
//			allEntitiesVariableMap.put(entityClassName, entityVars);
//			
//			entityId2Objectref.put(uuid, objectRef);
//			entityObjectref2Id.put(objectRef, uuid);
//			
//			return objectRef;
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public Map<String, Map<String, Map<String, Variable>>> getEntitiesVariableMap() {
//		return this.allEntitiesVariableMap;
//	}
//
//	private Variable getInitialVariable(String entityName, String uuid, String type, java.lang.reflect.Field field) {
//		String varName = uuid + entityName + "." + field.getName();
//		if(type.equals(Collection.class.getName())) {
//			Type genericType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
//			String genericTypeName = genericType.toString().substring(6);
//			return new CollectionVariableImpl(varName, genericTypeName, (SymbolicVirtualMachine)this.vm);
//		}
//		
//		if(type.equals("int") || type.equals(Integer.class.getName())) {
//			return new NumericVariable(varName, Expression.INT);
//		}
//		
//		
//		
//		return null;
//	}
//	
//	
//}
