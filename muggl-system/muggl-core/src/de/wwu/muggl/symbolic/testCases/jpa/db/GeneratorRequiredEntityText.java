package de.wwu.muggl.symbolic.testCases.jpa.db;

import java.util.Map;
import java.util.Set;

import de.wwu.muggl.db.entry.DatabaseObject;

public class GeneratorRequiredEntityText {

	public StringBuilder generatePreExeuctionRequiredEntityText(Map<String, Set<DatabaseObject>> data) {
		StringBuilder sb = new StringBuilder();
		for(String entityName : data.keySet()) {
			for(DatabaseObject dbObj : data.get(entityName)) {
				Map<String, Object> valueMap = dbObj.valueMap();
				for(String fieldName : valueMap.keySet()) {
					Object value = valueMap.get(fieldName);
					System.out.println("field="+fieldName+", value="+value);
				}
			}
		}
		return sb;
	}

}
