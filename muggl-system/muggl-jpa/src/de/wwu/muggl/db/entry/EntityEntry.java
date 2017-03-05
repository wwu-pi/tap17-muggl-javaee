package de.wwu.muggl.db.entry;

import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.solvers.type.IObjectreference;

public class EntityEntry implements IObjectreference {

	private String objectId;
	private String name;
	private String entityClassName;
	private Map<String, Object> values;
	
	public EntityEntry(String name, String entityClassName) {
		this.name = name;
		this.entityClassName = entityClassName;
		this.values = new HashMap<String, Object>();
	}

	public String getName() {
		return this.name;
	}
	
	public void addValue(String fieldName, Object value) {
		this.values.put(fieldName, value);
	}
	
	public Object getValue(String fieldName) {
		return this.values.get(fieldName);
	}
	
	public Map<String, Object> valueMap() {
		return this.values;
	}

	public String getEntityClassName() {
		return this.entityClassName;
	}
	
	public String toString() {
		return this.name + "(objId=" + this.objectId + ")";
	}

	public EntityEntry getClone() {
		EntityEntry newEntityEntry = new EntityEntry(this.name, this.entityClassName);
		for(String fieldName : this.values.keySet()) {
			newEntityEntry.values.put(fieldName, this.values.get(fieldName)); // TODO: hier clonen den value...
		}
		newEntityEntry.objectId = this.objectId;
		return newEntityEntry;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Override
	public String getObjectType() {
		return this.entityClassName;
	}

	@Override
	public long getInstantiationNumber() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
