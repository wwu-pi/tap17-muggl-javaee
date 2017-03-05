package de.wwu.muggl.db.obj;

import java.util.ArrayList;
import java.util.List;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.db.sym.list.SymbolicSize;
import de.wwu.muggl.db.sym.var.EntityReferenceVariable;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.type.IObjectreference;

public class ObjectValueProvider {
	
	private static final Integer DEFAULT_NUMERIC_VALUE = 0;
	private static final ArrayList<?> DEFAULT_COLLECTION_VALUE = new ArrayList<>();
	private static final Object DEFAULT_ENTITY_REF_VALUE = null;

	private DBObjectGenerator generator;
	
	public ObjectValueProvider(DBObjectGenerator generator) {
		this.generator = generator;
	}

	Object getObjectValue(Object valueObject, Solution solution) {
		if(valueObject instanceof CollectionVariable) {
			CollectionVariable<DatabaseObject> collection = (CollectionVariable<DatabaseObject>)valueObject;
			List<Object> realList = new ArrayList<>();
			for(DatabaseObject element : collection.getRealCollection()) {
				Object elementObject = getObjectValue(element, solution);
				if( !realList.contains(elementObject) && (!isEntityObject(elementObject) || (isEntityObject(elementObject) && generator.getObjectIdSet().contains(element.getObjectId())) ) ) {
					realList.add(elementObject);
				}
			}
			SymbolicSize symSize = collection.getSymbolicSize();
			if(symSize != null) {
				Constant constantSize = solution.getValue(symSize);
				System.out.println("Constant: " + constantSize);
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
		
		if(valueObject instanceof IObjectreference) {
			return generator.generateObjectByEntityEntry((IObjectreference)valueObject, solution);
		}
		
		throw new RuntimeException("Could not generate concrete value for : " + valueObject);
	}
	
	private boolean isEntityObject(Object elementObject) {
		return true; // TODO: checken ob es ein Entity Object ist, und nicht nur ein "String" zum Beispiel, oder ein Integer der geaddedet werden soll in die Liste
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
		throw new RuntimeException("Could not get default value for constant: " + variable);
	}
}
