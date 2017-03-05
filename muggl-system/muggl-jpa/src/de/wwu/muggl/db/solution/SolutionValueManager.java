package de.wwu.muggl.db.solution;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.db.sym.var.EntityReferenceVariable;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Variable;

public class SolutionValueManager {

	private static final Integer DEFAULT_NUMERIC_VALUE = 0;

	private static final ArrayList<?> DEFAULT_COLLECTION_VALUE = new ArrayList<>();

	private static final Object DEFAULT_ENTITY_REF_VALUE = null;
	
	protected Map<String, Object> initializedObjectCache;
	
	public SolutionValueManager() {
		this.initializedObjectCache = new HashMap<String, Object>();
	}

	public Object getObjectValue(Object valueObject, Solution solution) {
		if(valueObject instanceof CollectionVariable) {
			CollectionVariable<DatabaseObject> collection = (CollectionVariable<DatabaseObject>)valueObject;
			List<Object> realList = new ArrayList<>();
			for(DatabaseObject element : collection.getRealCollection()) {
				Object elementObject = getObjectValue(element, solution);
				if(!realList.contains(elementObject)) {
					realList.add(elementObject);
				}
			}
			return realList;
		}
		
		if(valueObject instanceof Variable) {
			return getVariableValue((Variable)valueObject, solution);
		}
		
		if(valueObject instanceof Sum) {
			return getSumValue((Sum)valueObject, solution);
		}
		
		if(valueObject instanceof NumericConstant) {
			return getNumericConstant((NumericConstant)valueObject);
		}
		
		if(valueObject instanceof Integer) {
			return (Integer)valueObject;
		}
		
		if(valueObject instanceof DatabaseObject) {
			return instantiateObject((DatabaseObject)valueObject, solution);
		}
		
		throw new RuntimeException("Could not get value for object: " + valueObject );
	}

	protected Object getVariableValue(Variable variable, Solution solution) {
		Constant constant = solution.getValue(variable);
		
		if(constant == null) {
			return getDefaultConstantValue(variable);
		}
		
		if(constant instanceof NumericConstant) {
			return getNumericConstant((NumericConstant)constant);
		}
		
		throw new RuntimeException("Could not get value for variable: " + variable);
	}

	private Integer getNumericConstant(NumericConstant constant) {
		return new Integer(constant.getIntValue());
	}

	private Object getDefaultConstantValue(Variable variable) {
		if(variable instanceof NumericVariable) {
			return DEFAULT_NUMERIC_VALUE;
		}
		if(variable instanceof CollectionVariable) {
			return DEFAULT_COLLECTION_VALUE;
		}
		if(variable instanceof EntityReferenceVariable) {
			return DEFAULT_ENTITY_REF_VALUE;
		}
		throw new RuntimeException("Could not get value for constant: " + variable);
	}

	private Object getSumValue(Sum sum, Solution solution) {
		Object left = sum.getLeft();
		Object right = sum.getRight();
		Object leftValueTerm = getObjectValue(left, solution);
		Object rightValueTerm = getObjectValue(right, solution);
		Integer leftValue = null;
		Integer rightValue = null;
		if(leftValueTerm instanceof Integer) {
			leftValue = (Integer)leftValueTerm;
		}
		if(rightValueTerm instanceof Integer) {
			rightValue = (Integer)rightValueTerm;
		}
		return leftValue + rightValue;
	}

	private Object instantiateObject(DatabaseObject valueObject, Solution solution) {
		try {
			Object cachedObject = this.initializedObjectCache.get(valueObject.getObjectId());
			if(cachedObject != null) {
				return cachedObject;
			}
			
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(valueObject.getInitializedClassName());
			Object object = null;
			if(Modifier.isAbstract(clazz.getModifiers())) {
				System.out.println("** das hier ist fake: get real subtype of : " + clazz.getName());
				clazz = ClassLoader.getSystemClassLoader().loadClass("de.wwu.pi.entity.SummerLeague");
			}
			object = clazz.newInstance();
			
			this.initializedObjectCache.put(valueObject.getObjectId(), object);

			for(String fieldName : valueObject.valueMap().keySet()) {
				Field field = null;
				try {
					field = clazz.getDeclaredField(fieldName);
				} catch(NoSuchFieldException nsfe1) {
					try {
						field = clazz.getSuperclass().getDeclaredField(fieldName);
					} catch(NoSuchFieldException nsfe2) {
						continue;
					}
				}
				
				Object fieldValue = valueObject.valueMap().get(fieldName);
				
				Object objectValue = getObjectValue(fieldValue, solution);
				
				field.setAccessible(true);
				field.set(object, objectValue);
			}
			
			this.initializedObjectCache.put(valueObject.getObjectId(), object);
			return object;
			
		} catch(Exception e) {
			throw new RuntimeException("Could not inantiate object", e);
		}	
	}
}
