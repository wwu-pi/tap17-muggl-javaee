package de.wwu.muggl.vm.impl.obj.eq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.vm.impl.obj.meta.ObjectEqualException;

public class ObjectEqualStore {

	protected Map<String, Set<String>> equalObjectMap;
	
	protected Map<String, Set<String>> notEqualObjectMap;
	
	public ObjectEqualStore() {
		this.equalObjectMap = new HashMap<>();
		this.notEqualObjectMap = new HashMap<>();
	}
	
	protected boolean areEqual(String objId1, String objId2) {
		return this.equalObjectMap.get(objId1) != null && this.equalObjectMap.get(objId1).contains(objId2);
	}
	
	protected boolean areNotEqual(String objId1, String objId2) {
		return this.notEqualObjectMap.get(objId1) != null && this.notEqualObjectMap.get(objId1).contains(objId2);
	}
	
	public void addNotEqualObjects(String objId1, String objId2) throws ObjectEqualException {
		if(areEqual(objId1, objId2)) throw new ObjectEqualException("Cannot add objId1=[" + objId1 + "] and objId1=[" + objId2 + "] to be NOT equal, because both are already set to be EQUAL");
		addObjectToEqualMap(notEqualObjectMap, objId1, objId2);
		addObjectToEqualMap(notEqualObjectMap, objId2, objId1);
	}

	public void addEqualObjects(String objId1, String objId2) throws ObjectEqualException {
		if(areNotEqual(objId1, objId2)) throw new ObjectEqualException("Cannot add objId1=[" + objId1 + "] and objId1=[" + objId2 + "] to be EQUAL, because both are already set to be NOT EQUAL");
		addObjectToEqualMap(equalObjectMap, objId1, objId2);
		addObjectToEqualMap(equalObjectMap, objId2, objId1);
	}
	
	private void addObjectToEqualMap(Map<String, Set<String>> objMap, String objectId, String objectToAddToSet) {
		Set<String> set = objMap.get(objectId);
		if(set == null) {
			set = new HashSet<>();
		}
		set.add(objectToAddToSet);
		objMap.put(objectId, set);
	}
}
