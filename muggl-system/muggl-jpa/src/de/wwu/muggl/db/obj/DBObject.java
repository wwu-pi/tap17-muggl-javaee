package de.wwu.muggl.db.obj;

import java.util.HashMap;
import java.util.Map;

public class DBObject {

	private Map<String, Object> values;
	private String objectType;

	DBObject(String objectType) {
		this.values = new HashMap<String, Object>();
		this.objectType = objectType;
	}
		
	void add(String fieldName, Object value) {
		this.values.put(fieldName, value);
	}
	
	Object get(String fieldName) {
		return this.values.get(fieldName);
	}
	
	public Map<String, Object> values() {
		return this.values;
	}
	
	public String getObjectType() {
		return this.objectType;
	}
}
