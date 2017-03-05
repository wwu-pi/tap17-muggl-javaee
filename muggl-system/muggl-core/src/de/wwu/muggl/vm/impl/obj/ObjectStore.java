package de.wwu.muggl.vm.impl.obj;

import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.vm.impl.obj.eq.ObjectEqualStore;
import de.wwu.muggl.vm.impl.obj.meta.ObjectEqualException;
import de.wwu.muggl.vm.initialization.Objectref;

public class ObjectStore {

	/**
	 *  key = object-id, value = object as object-reference
	 */
	protected Map<String, Objectref> objectIdMap;
	
	protected ObjectEqualStore objectEqualStore;
	
	public ObjectStore() {
		this.objectEqualStore = new ObjectEqualStore();
		this.objectIdMap = new HashMap<>();
	}
	
	public void addObject(Objectref objectRef) {
		objectIdMap.put(objectRef.getObjectId(), objectRef);
	}
	
	public void addEqualObject(String objectId1, String objectId2) throws ObjectEqualException {
		this.objectEqualStore.addEqualObjects(objectId1, objectId2);
	}
	
}
