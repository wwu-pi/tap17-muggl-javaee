//package de.wwu.muggl.vm.impl.jpa.db.solution;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.expressions.Constant;
//import de.wwu.muggl.solvers.expressions.IntConstant;
//import de.wwu.muggl.solvers.expressions.NumericConstant;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.Sum;
//import de.wwu.muggl.solvers.expressions.Term;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.solvers.expressions.list.SymbolicList;
//import de.wwu.muggl.solvers.expressions.list.SymbolicListElement;
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.impl.jpa.db.EntityObjectEntry;
//import de.wwu.muggl.vm.impl.jpa.db.NewVirtualDatabase;
//import de.wwu.muggl.vm.impl.jpa.list.SymbolicListElementObjectRef;
//import de.wwu.muggl.vm.initialization.Arrayref;
//import de.wwu.muggl.vm.initialization.InitializedClass;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class VirtualDatabaseSolution {
//	
//	protected Map<String, List<VDBSolutionEntry>> database;
//	
//	protected Map<Long, Object> initializedObjectCache;
//	
//	public VirtualDatabaseSolution(NewVirtualDatabase vdb, Solution solution) {
//		this.database = new HashMap<>();
//		this.initializedObjectCache = new HashMap<>();
//		
//		for(String entityName : vdb.getEntityMap().keySet()) {
//			List<VDBSolutionEntry> entryList = this.database.get(entityName);
//			if(entryList == null) {
//				entryList = new ArrayList<>();
//			}
//			for(EntityObjectEntry entry : vdb.getEntityMap().get(entityName)) {
//				VDBSolutionEntry solutionEntry = new VDBSolutionEntry();
//				solutionEntry.setPreExecutionRequired(entry.isPreExecutionRequired());
//				for(Field field : entry.getFields().keySet()) {
//					Object valueObject = entry.getField(field);
//					Object value = getObjectValue(valueObject, solution);
//					solutionEntry.addValue(field.getName(), value);
//				}
//				entryList.add(solutionEntry);
//			}
//			this.database.put(entityName, entryList);
//		}
//	}
//	
//	protected Object getObjectValue(Object valueObject, Solution solution) {
//		if(valueObject instanceof Variable) {
//			Variable variable = (Variable)valueObject;
//			Constant constant = solution.getValue(variable);
//			if(variable instanceof NumericVariable && constant == null) { 
//				int randIntValue = 42;
//				return randIntValue;
//			} else if(constant instanceof NumericConstant) {
//				NumericConstant nc = (NumericConstant)constant;
//				int intValue = nc.getIntValue();
//				return intValue;
//			} else {
//				throw new RuntimeException("Could not get value for field");
//			}
//		} else if(valueObject instanceof Sum) {
//			Sum sum = (Sum)valueObject;
//			Object left = sum.getLeft();
//			Object right = sum.getRight();
//			Object leftValueTerm = getObjectValue(left, solution);
//			Object rightValueTerm = getObjectValue(right, solution);
//			Integer leftValue = null;
//			Integer rightValue = null;
//			if(leftValueTerm instanceof Integer) {
//				leftValue = (Integer)leftValueTerm;
//			}
//			if(rightValueTerm instanceof Integer) {
//				rightValue = (Integer)rightValueTerm;
//			}
//			return leftValue + rightValue;
//		} else if(valueObject instanceof NumericConstant) {
//			NumericConstant nc = (NumericConstant)valueObject;
//			int intValue = nc.getIntValue();
//			return intValue;
//		} else if(valueObject instanceof Integer) {
//			return (Integer)valueObject;
//		} else if(valueObject instanceof Objectref) {
//			Objectref objectRef = (Objectref)valueObject;
//			return instantiateObjectref(objectRef, solution);
//			
//			
////			Objectref objectRef = (Objectref)valueObject;
////			InitializedClass initClass = objectRef.getInitializedClass();
////			if(initClass != null && initClass.getClassFile().getName().equals("java.util.ArrayList")) {
////				Field elementData = initClass.getClassFile().getFieldByName("elementData");
////				Object elementDataValue = objectRef.getField(elementData);
////				if(elementDataValue instanceof Arrayref) {
////					Arrayref arrayRef = (Arrayref)elementDataValue;
////					ArrayList<Object> arrayList = new ArrayList<>();
////					for(int i=0; i<arrayRef.length; i++) {
////						Object element = arrayRef.getElement(i);
////						Object arrayElementValue = getObjectValue(element, solution);
////						arrayList.add(arrayElementValue);
////					}
////					return arrayList;
////				}
////			} else {
////				return instantiateObjectref(objectRef, solution);
////			}
//		}
//		throw new RuntimeException("Could not get value object: " + valueObject );
//	}
//	
//	private Object[] getArrayListValues(Arrayref arrayRef, Solution solution) throws Exception {
//		Object[] array = new Object[arrayRef.length];
//		for(int i=0; i<arrayRef.length; i++) {
//			Object element = arrayRef.getElement(i);
//			if(element instanceof Objectref) {
//				Objectref objRef = (Objectref)element;
////				Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(objRef.getInitializedClass().getClassFile().getName());
////				Object object = clazz.newInstance();
//				Object object = instantiateObjectref(objRef, solution);
//				array[i] = object;
//			}
//		}
//		return array;
//	}
//	
//	private Object instantiateObjectref(Objectref objectRef, Solution solution) {
//		try {
//			Object cachedObject = this.initializedObjectCache.get(objectRef.getInstantiationNumber());
//			if(cachedObject != null) {
//				return cachedObject;
//			}
//			
//			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(objectRef.getInitializedClass().getClassFile().getName());
//			Object object = clazz.newInstance();
//			
//			this.initializedObjectCache.put(objectRef.getInstantiationNumber(), object);
//
//			for(Field field : objectRef.getFields().keySet()) {
//				java.lang.reflect.Field objectField = null;
//				try {
//					objectField = clazz.getDeclaredField(field.getName());
//				} catch(NoSuchFieldException nsfe) {
//					continue;
//				}
//				
//				Object fieldValue = objectRef.getFields().get(field);
//				Object objectValue = null;
//				if(fieldValue instanceof Objectref) {
//					objectValue = getObjectValue(fieldValue, solution);
//				}
////				if(fieldValue instanceof Arrayref) {
////					objectValue = getArrayListValues((Arrayref)fieldValue, solution);
////				} else {
////					objectValue = getObjectValue(fieldValue, solution);
////				}
//				objectField.setAccessible(true);
//				objectField.set(object, objectValue);
//			}
//			
//			if(objectRef instanceof SymbolicList) {
//				SymbolicList symList = (SymbolicList)objectRef;
//				Object[] realObjectElementArray = new Object[symList.elements().length];
//				for(int i=0; i<symList.elements().length; i++) {
//					SymbolicListElementObjectRef elementObjRef = (SymbolicListElementObjectRef)symList.elements()[i];
//					Object realEleObject = instantiateObjectref(elementObjRef, solution);
//					realObjectElementArray[i] = realEleObject;
//				}
//				try {
//					java.lang.reflect.Field elementField = clazz.getDeclaredField("elementData");
//					elementField.setAccessible(true);
//					elementField.set(object, realObjectElementArray);
//					
//					java.lang.reflect.Field sizeField = clazz.getDeclaredField("size");
//					sizeField.setAccessible(true);
//					sizeField.set(object, symList.elements().length);
//				} catch(NoSuchFieldException nsfe) {
//				}
//			}
//			
//			this.initializedObjectCache.put(objectRef.getInstantiationNumber(), object);
//			return object;
//		} catch(Exception e) {
//			throw new RuntimeException("Could not inantiate object", e);
//		}
//	}
//	
//	public Map<String, List<VDBSolutionEntry>> getSolution() {
//		return this.database;
//	}
//	
//	
//}
