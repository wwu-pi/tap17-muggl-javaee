//package de.wwu.muggl.symbolic.jpa;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//@Deprecated
//public class VirtualDatabase {
//
//	private static VirtualDatabase instance;
//	protected Map<String, Set<Object>> database;
//	
//	public VirtualDatabase() {
//		this.database = new HashMap<String, Set<Object>>();
//	}
//	
//	public Set<String> getDataTables() {
//		return this.database.keySet();
//	}
//	
//	public Set<Object> getData(String tbl) {
//		return this.database.get(tbl);
//	}
//	
//	public void addData(String tbl, Object data) {
//		Set<Object> dataset = this.database.get(tbl);
//		if(dataset == null) {
//			dataset = new HashSet<Object>();
//		}
//		dataset.add(data);
//		this.database.put(tbl, dataset);
//	}
//	
//	// TODO:der solution den virtual database hinzufügen...
//	@Deprecated
//	public static synchronized VirtualDatabase getInstance() {
//		if(instance == null) {
//			instance = new VirtualDatabase();
//		}
//		return instance;
//	}
//}
