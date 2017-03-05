package de.wwu.muggl.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.list.ISymbolicResultList;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.expressions.NumericVariable;

public class VirtualObjectDatabase {

	private Map<String, Set<DatabaseObject>> entityDataMap;
	
	private Map<String, Set<DatabaseObject>> preExecutionRequired;
	
	
	// for resetting
	private Map<Integer, Set<String>> snapPreExecutionLevelMap;
	private Map<Integer, Set<String>> snapPostExpectedLevelMap;
	private Map<Integer, Set<String>> snapSymbolicQueryResultsMap;
	
//	private int currentLevel;
	
	private Set<ISymbolicResultList> symbolicQueryResults;
	
	public VirtualObjectDatabase() {
		this.entityDataMap = new HashMap<>();
		this.preExecutionRequired = new HashMap<>();
		this.symbolicQueryResults = new HashSet<>();
		
		this.snapPreExecutionLevelMap = new HashMap<>();
		this.snapPostExpectedLevelMap = new HashMap<>();
		this.snapSymbolicQueryResultsMap = new HashMap<>();
	}
	
	public Map<String, Set<DatabaseObject>> getPreExecutionRequiredData() {
		return this.preExecutionRequired;
	}
	
	public void addPreExecutionRequiredData(SolverManager solverManager, String entityName, DatabaseObject data) {
		snapshotCurrentDB(solverManager);
		addData(this.preExecutionRequired, entityName, data);
	}

	public void addEntityData(SolverManager solverManager, String entityName, DatabaseObject data) {
		snapshotCurrentDB(solverManager);
		addData(this.entityDataMap, entityName, data);
	}
	
	protected void addData(Map<String, Set<DatabaseObject>> dataMap, String entityName, DatabaseObject data) {
		Set<DatabaseObject> entityData = dataMap.get(entityName);
		if(entityData == null) {
			entityData = new HashSet<>();
		}
		entityData.add(data);
		dataMap.put(entityName, entityData);
	}

	public Set<DatabaseObject> getData(String entityName) {
		return this.entityDataMap.get(entityName);
	}
	
	public Map<String, Set<DatabaseObject>> getData() {
		return this.entityDataMap;
	}

	public void addQueryResultList(ISymbolicResultList resultList) {
		this.symbolicQueryResults.add(resultList);
	}
	
	public Set<ISymbolicResultList> getSymbolicQueryResultLists() {
		return this.symbolicQueryResults;
	}
	
	
	
	
	
	
	
	
	public void snapshotCurrentDB(SolverManager solverManager) {
		int level = solverManager.getConstraintLevel();
//		this.currentLevel = level;
		
		Set<String> preExecutionSet = new HashSet<>();
		for(String entityName : this.preExecutionRequired.keySet()) {
			for(DatabaseObject dbObj : this.preExecutionRequired.get(entityName)) {
				preExecutionSet.add(dbObj.getObjectId());
			}
		}
		
		Set<String> postExpectedSet = new HashSet<>();
		for(String entityName : this.entityDataMap.keySet()) {
			for(DatabaseObject dbObj : this.entityDataMap.get(entityName)) {
				postExpectedSet.add(dbObj.getObjectId());
			}
		}
		
		Set<String> qryExpectedSet = new HashSet<>();
		for(ISymbolicResultList s : this.symbolicQueryResults) {			
			qryExpectedSet.add(s.getId());
		}
		
		addLevelsBefore(snapPreExecutionLevelMap, level);
		addLevelsBefore(snapPostExpectedLevelMap, level);
		addLevelsBefore(snapSymbolicQueryResultsMap, level);
		
		this.snapPreExecutionLevelMap.put(level, preExecutionSet);
		this.snapPostExpectedLevelMap.put(level, postExpectedSet);
		this.snapSymbolicQueryResultsMap.put(level, qryExpectedSet);
	}
	
	private void addLevelsBefore(Map<Integer, Set<String>> map, int level) {
		Integer before = getLastBefore(map, level);
		if(before != null) {
			Set<String> a = map.get(before);
			for(int i=(before+1); i<level; i++ ){
				map.put(i, a);
			}
		}
	}
	
	private Integer getLastBefore(Map<Integer, Set<String>> map, int level) {
		if(level > 0) {
			int levelBefore = level - 1;
			Set<String> before = map.get(levelBefore);
			if(before == null) {
				return getLastBefore(map, levelBefore);
			}
			return levelBefore;
		}
		return null;
	}

	public void resetDatabase(int level) {
		
		Set<String> allowedPreExeObjectIds = this.snapPreExecutionLevelMap.get(level);
		resetSymbolicDatabase(this.preExecutionRequired, allowedPreExeObjectIds);
		
		Set<String> allowedPostExpObjectIds = this.snapPostExpectedLevelMap.get(level);
		resetSymbolicDatabase(this.entityDataMap, allowedPostExpObjectIds);
		
		String x;
		
		Set<ISymbolicResultList> removeLists = new HashSet<>(); 
		Set<String> allowedQuerys = this.snapSymbolicQueryResultsMap.get(level);
		if(allowedQuerys != null) {
			for(ISymbolicResultList l : this.symbolicQueryResults) {
				if(!allowedQuerys.contains(l.getId())) {
					removeLists.add(l);
				}
			}
			for(ISymbolicResultList l : removeLists) {
				this.symbolicQueryResults.remove(l);
			}
		}
		
		
	}
	
	private void resetSymbolicDatabase(Map<String, Set<DatabaseObject>> data, Set<String> allowedObjectIds) {
		if(allowedObjectIds == null) return;
		
		for(String e : data.keySet()) {
			Set<DatabaseObject> e_data = data.get(e);
			Set<DatabaseObject> remove_e_data = new HashSet<>(); 
			for(DatabaseObject d : e_data) {
				if(!allowedObjectIds.contains(d.getObjectId())) {
					remove_e_data.add(d);
				}
			}
			for(DatabaseObject d : remove_e_data) {
				e_data.remove(d);
			}
		}
	}
	
//	private void resetSymbolicQueryListDatabase() {
//		Set<ISymbolicResultList> set = this.vm.getVirtualObjectDatabase().getSymbolicQueryResultLists();
//
//		Set<ISymbolicResultList> removeLists = new HashSet<>(); 
//		for(ISymbolicResultList l : set) {
//			if(!this.snapResultQueryListIds.contains(l.getId())) {
//				removeLists.add(l);
//			}
//		}
//		
//		for(ISymbolicResultList l : removeLists) {
//			set.remove(l);
//		}
//	}
}
