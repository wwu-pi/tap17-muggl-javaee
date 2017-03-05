package de.wwu.muggl.db.obj;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.type.IObjectreference;

public class DBObjectGenerator {

	private ObjectValueProvider provider;
	private Map<String, DBObject> cache;
	private Set<String> objectIdSet;
	
	public DBObjectGenerator(Set<String> objectIdSet) {
		this.cache = new HashMap<>();
		this.provider = new ObjectValueProvider(this);
		this.objectIdSet = objectIdSet;
	}
	
	protected Set<String> getObjectIdSet() {
		return this.objectIdSet;
	}
	
	public DBObject generateObjectByEntityEntry(IObjectreference generateableObject, Solution solution) {
		DBObject cachedObject = this.cache.get(generateableObject.getObjectId());
		if(cachedObject != null) {
			return cachedObject;
		}
		
		DBObject object = new DBObject(generateableObject.getObjectType());
		this.cache.put(generateableObject.getObjectId(), object);
		
		for(String fieldName : generateableObject.valueMap().keySet()) {
			Object objectValue = provider.getObjectValue(generateableObject.valueMap().get(fieldName), solution);
			object.add(fieldName, objectValue);
		}
		
		return object;
	}

	
}
