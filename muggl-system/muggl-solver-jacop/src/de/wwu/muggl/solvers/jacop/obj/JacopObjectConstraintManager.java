package de.wwu.muggl.solvers.jacop.obj;

import org.jacop.core.IntVar;
import org.jacop.core.Store;

import de.wwu.muggl.solvers.type.IObjectreference;

import java.util.HashMap;
import java.util.Map;

public class JacopObjectConstraintManager {

	protected final IntVar objectNullRefVariable;
	protected Map<IntVar, IObjectreference> variableToObjectMap;
	protected Map<IObjectreference, IntVar> objectToVariableMap;
	protected Store store;
	
	public JacopObjectConstraintManager(Store store) {
		this.objectNullRefVariable = new IntVar(store, "object.null.reference.variable", 0, 0);
		this.variableToObjectMap = new HashMap<>();
		this.objectToVariableMap = new HashMap<>();
		this.store = store;
	}
	
	public IntVar getInternalVariable(IObjectreference objectRef) {
		if(objectToVariableMap.get(objectRef) == null) {
			IntVar var = generateNewVariable(objectRef.getObjectId()+".GENVAR");
			this.variableToObjectMap.put(var, objectRef);
			this.objectToVariableMap.put(objectRef, var);
		}
		
		return objectToVariableMap.get(objectRef);		
	}
	
	protected IntVar generateNewVariable(String name) {
		return new IntVar(store, name, 1, 127);
	}
}
