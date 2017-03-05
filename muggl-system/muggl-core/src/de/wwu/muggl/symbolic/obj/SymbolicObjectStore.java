package de.wwu.muggl.symbolic.obj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.vm.var.ReferenceVariable;

public class SymbolicObjectStore {

	protected Map<String, ReferenceVariable> objectMap;
	protected Map<String, Set<ReferenceVariable>> objectEqualsMap;
	
	public SymbolicObjectStore() {
		objectMap = new HashMap<>();
		objectEqualsMap = new HashMap<>();
	}
	
	public void mergeEqualObjectValues() {
		for(String objectId : this.objectEqualsMap.keySet()) {
			ReferenceVariable objectRef = objectMap.get(objectId);
			for(ReferenceVariable equalObject : this.objectEqualsMap.get(objectId)) {
				
			}
		}
	}
	
	protected void mergeObjectValues(ReferenceVariable o1, ReferenceVariable o2) {
		Set<String> inspectedFields = new HashSet<>();
		for(String fieldName : o1.valueMap().keySet()) {
			inspectedFields.add(fieldName);
			Object o1Value = o1.valueMap().get(fieldName);
			Object o2Value = o2.valueMap().get(fieldName);
			if(o2Value == null && o1Value != null) {
				
			}
		}
	}
	
	public void addObject(ReferenceVariable object) {
		this.objectMap.put(object.getObjectId(), object);
	}
	
	public ReferenceVariable getObject(String objectId) {
		return this.objectMap.get(objectId);
	}
	
	public void addObjectsEqual(ReferenceVariable o1, ReferenceVariable o2) {
		// add object o2 to the equal-set of o1
		addEqual(o1.getObjectId(), o2);
		
		// add object o1 to the equal-set of o2
		addEqual(o2.getObjectId(), o1);
	}
	
	public Set<ReferenceVariable> getEqualObjects(String objectId) {
		return this.objectEqualsMap.get(objectId);
	}
	
	private void addEqual(String objectId, ReferenceVariable object) {
		Set<ReferenceVariable> objectEqualSet = objectEqualsMap.get(objectId);
		if(objectEqualSet == null) {
			objectEqualSet = new HashSet<>();
		}
		objectEqualSet.add(object);
		objectEqualsMap.put(objectId, objectEqualSet);
	}
	

}
