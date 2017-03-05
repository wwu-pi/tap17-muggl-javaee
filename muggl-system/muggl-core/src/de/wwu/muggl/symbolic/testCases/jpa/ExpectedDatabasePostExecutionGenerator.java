package de.wwu.muggl.symbolic.testCases.jpa;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.db.obj.DBObject;

public class ExpectedDatabasePostExecutionGenerator {
	
	public StringBuilder generatePostExecutionDBString(Map<String, List<DBObject>> dbSolution) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("");//sb.append("\t\t// expected db after test execution\r\n");
		
		return sb;
	}
	
	public StringBuilder generatePostExecutionDataString(Set<DBObject> dbSolution) {
		StringBuilder sb = new StringBuilder();
//		sb.append("\t\t// expect " + dbSolution.size() + " new entries in database:\r\n");
		for(DBObject dbObject : dbSolution) {
			sb.append("\t\t// \ttype=" + dbObject.getObjectType()+"\t\t");
			for(String fieldName : dbObject.values().keySet()) {
				sb.append(fieldName+"="+dbObject.values().get(fieldName)  + "\t");
			}
			sb.append("\r\n");
		}
		return sb;
	}
}
