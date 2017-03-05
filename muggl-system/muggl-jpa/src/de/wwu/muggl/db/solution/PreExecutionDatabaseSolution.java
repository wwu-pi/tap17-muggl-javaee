package de.wwu.muggl.db.solution;

import java.util.HashSet;
import java.util.Set;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.entry.EntityEntry;
import de.wwu.muggl.db.obj.DBObject;
import de.wwu.muggl.db.obj.DBObjectGenerator;
import de.wwu.muggl.solvers.Solution;

public class PreExecutionDatabaseSolution {

	protected Set<DBObject> solution;
	
	public PreExecutionDatabaseSolution(VirtualDatabase database, Solution solution) {
		this.solution = new HashSet<DBObject>();
		fillSolution(database, solution);
	}
	
	protected void fillSolution(VirtualDatabase database, Solution solution) {
		// just the required data is allowed for pre-execution generation...
		Set<String> objectIdSet = new HashSet<>();
		for(EntityEntry data : database.getRequiredData().values()) {
			objectIdSet.add(data.getObjectId());
		}
		
		DBObjectGenerator gen = new DBObjectGenerator(objectIdSet);
		for(EntityEntry data : database.getRequiredData().values()) {
			DBObject dbObject = gen.generateObjectByEntityEntry(data, solution);
			this.solution.add(dbObject);
		}
	}
	
	public Set<DBObject> getSolution() {
		return this.solution;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	private void foobar(VirtualDatabase database, Solution solution) {
//		DBObjectGenerator gen = new DBObjectGenerator();
//		for(EntityEntry data : database.getRequiredData().values()) {
//			DBObject dbObject = gen.generateObjectByEntityEntry(data, solution);
//			System.out.println("dbObject :  " + dbObject);
//		}
//	}
//
//	protected void fillSolutionOLD(VirtualDatabase database, Solution solution) {
//		foobar(database, solution);
//		
////		generateObjects(database, solution);
//		for(EntityEntry data : database.getRequiredData().values()) {
//			List<DatabaseSolutionEntry> entitySolutionList = this.solution.get(data.getEntityClassName());
//			if(entitySolutionList == null) {
//				entitySolutionList = new ArrayList<DatabaseSolutionEntry>();
//			}
//			
//			DatabaseSolutionEntry entry = new DatabaseSolutionEntry();
//			
//			for(String fieldName : data.valueMap().keySet()) {
//				Object value = solutionValueManager.getObjectValue(data.getValue(fieldName), solution);
//				entry.add(fieldName, value);
//			}
//			entitySolutionList.add(entry);
//			
//			this.solution.put(data.getEntityClassName(), entitySolutionList);
//		}
//	}
//	
//	private void generateObjects(VirtualDatabase database, Solution solution) {
//		generatedSolutionEntriesMap = new HashMap<>();
//		for(String objRefId : database.getRequiredData().keySet()) {
//			generateObject(database, objRefId, solution);
//		}
//	}
//
//	private Map<String, DatabaseSolutionEntry> generatedSolutionEntriesMap;
//	
//	protected DatabaseSolutionEntry generateObject(VirtualDatabase database, String objRefId, Solution solution) {
//		DatabaseSolutionEntry entry = new DatabaseSolutionEntry();
//		generatedSolutionEntriesMap.put(objRefId, entry);
//		
//		EntityEntry entityEntry = database.getRequiredData().get(objRefId);
//		
//		for(String fieldName : entityEntry.valueMap().keySet()) {
//			Object value = entityEntry.getValue(fieldName);
//			entry.add(fieldName, getObjectValue(value, solution));
//		}
//		
//		return entry;
//	}
//
//	private Object getObjectValue(Object value, Solution solution) {
//		System.out.println("Get concrete value for: " + value);
//		try {
////			Object realValue = getConcreteValue(value, solution);
////			System.out.println("  -> Concrete value: " + realValue);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
}
