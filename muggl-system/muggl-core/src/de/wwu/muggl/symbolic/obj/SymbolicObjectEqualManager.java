package de.wwu.muggl.symbolic.obj;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.array.ISymbolicArrayref;
import de.wwu.muggl.solvers.type.IObjectreference;

public class SymbolicObjectEqualManager {

	public boolean canBeEqual(IObjectreference o1, IObjectreference o2) {
		Set<String> inspectedFields = new HashSet<>();
		
		Map<String, Object> o1ValueMap = o1.valueMap();
		Map<String, Object> o2ValueMap = o2.valueMap();
		
		for(String field : o1ValueMap.keySet()) {
			Object o1FieldValue = o1ValueMap.get(field);
			Object o2FieldValue = o2ValueMap.get(field);
			boolean canBeEqual = canBeEqualFieldValue(o1FieldValue, o2FieldValue);
			if(!canBeEqual) {
				return false;
			}
			inspectedFields.add(field);
		}
		
		for(String field : o2ValueMap.keySet()) {
			if(inspectedFields.contains(field)) {
				continue; // already checked in upper for-loop
			}
			
			Object o1FieldValue = o1ValueMap.get(field);
			Object o2FieldValue = o2ValueMap.get(field);
			boolean canBeEqual = canBeEqualFieldValue(o1FieldValue, o2FieldValue);
			if(!canBeEqual) {
				return false;
			}
			inspectedFields.add(field);
		}
		
		return true;
	}
	
	
	
	protected boolean canBeEqualFieldValue(Object o1FieldValue, Object o2FieldValue) {
		if(o1FieldValue == null || o2FieldValue == null) {
			return true;
		}
		
		if(!o1FieldValue.getClass().equals(o2FieldValue.getClass())) {
			throw new IllegalArgumentException("Cannot compare two different types: o1="+o1FieldValue.getClass() + " and o2="+o2FieldValue.getClass());
		}
		
		if(o1FieldValue instanceof NumericVariable && o2FieldValue instanceof NumericVariable) {
			return canBeEqual_NumericVariables((NumericVariable)o1FieldValue, (NumericVariable)o2FieldValue);
		}
		
		if(o1FieldValue instanceof ISymbolicArrayref && o2FieldValue instanceof ISymbolicArrayref) {
			return canBeEqual_SymbolicArrayref((ISymbolicArrayref)o1FieldValue, (ISymbolicArrayref)o2FieldValue);
		}
		
		if(o1FieldValue instanceof IObjectreference && o2FieldValue instanceof IObjectreference) {
			return canBeEqual((IObjectreference)o1FieldValue, (IObjectreference)o2FieldValue);
		}
		
		return false;
	}


	private boolean canBeEqual_NumericVariables(NumericVariable o1FieldValue, NumericVariable o2FieldValue) {
		System.err.println("\n\n\n********************************** IMPLEMENT ME \n*********************************************\n\n\n");
		return true;
	}
	
	private boolean canBeEqual_SymbolicArrayref(ISymbolicArrayref o1FieldValue, ISymbolicArrayref o2FieldValue) {
		System.err.println("\n\n\n********************************** IMPLEMENT ME \n*********************************************\n\n\n");
		return true;
	}
}
