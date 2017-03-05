package de.wwu.muggl.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.entry.EntityEntry;

public class VirtualDatabase {

	protected Map<String, EntityEntry> requiredData;
	protected Set<DatabaseObject> database;
	
	public VirtualDatabase() {
		this.requiredData = new HashMap<String, EntityEntry>();
		this.database = new HashSet<DatabaseObject>();
	}
	
	public void addData(DatabaseObject object) {
		if(this.database.contains(object)) {
			throw new RuntimeException("Object already exists");
		}
		this.database.add(object);
	}
	
	public void removeData(DatabaseObject object) {
		boolean exists = false;
		
		for(DatabaseObject dbObj : this.database) {
			if(dbObj.getObjectId().equals(object.getObjectId())) {
				this.database.remove(dbObj);
				exists = true;
				break;
			}
		}
		
		if(!exists) {
			System.out.println("Object does not exist, cannot remove it");
//			throw new RuntimeException("Object does not exist, cannot remove it");
		}
	}
	
	public void addRequired(String objectRefKey, EntityEntry entity) {
		entity.setObjectId(objectRefKey);
		requiredData.put(objectRefKey, entity);
	}
	
	public Map<String, EntityEntry> getRequiredData() {
		return this.requiredData;
	}

	public Set<DatabaseObject> getData() {
		return this.database;
	}

	public VirtualDatabase getClone() {		
		VirtualDatabase newVDB = new VirtualDatabase();
		for(DatabaseObject dbObj : this.database) {
			DatabaseObject newDBObj = dbObj.getClone();
			newVDB.addData(newDBObj);
			EntityEntry oldEntityEntry = this.requiredData.get(dbObj.getObjectId());
			if(oldEntityEntry != null) {
				newVDB.requiredData.put(newDBObj.getObjectId(), oldEntityEntry.getClone());
			}
		}
		
		return newVDB;
	}
}
