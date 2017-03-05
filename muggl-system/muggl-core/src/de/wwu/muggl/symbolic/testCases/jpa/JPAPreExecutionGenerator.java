package de.wwu.muggl.symbolic.testCases.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.db.obj.DBObject;

public class JPAPreExecutionGenerator {

	protected Map<String, Integer> entityCounterMap;
	protected Map<DBObject, String> dbObjectFieldNameMap;
	
	public JPAPreExecutionGenerator() {
		this.dbObjectFieldNameMap = new HashMap<>();
		this.entityCounterMap = new HashMap<>();
	}
	
	public StringBuilder generatePreExecutionRequiredString(Set<DBObject> dbSolution) {
		StringBuilder sb = new StringBuilder();
		
		boolean preExecutionDataExists = false;
		
		for(DBObject dbObject : dbSolution) {
			generateNewObject(sb, dbObject);
			preExecutionDataExists = true;
		}
		
		if(!preExecutionDataExists) {
			sb.append("\t\t// no pre-execution data required for this test case\r\n");
		}
		
		return sb;
	}
	
	private String getSetterMethod(String attribute) {
		return "set" + attribute.toUpperCase().substring(0, 1) + attribute.substring(1);
	}

	protected void generateNewObject(StringBuilder sb, DBObject dbObject) {
		String _entityFieldName = dbObjectFieldNameMap.get(dbObject);
		if(_entityFieldName == null) {
			// the object has not been create yet, create it...
			_entityFieldName = generateNewEntityLocalFieldName(dbObject.getObjectType());
			this.dbObjectFieldNameMap.put(dbObject, _entityFieldName);
			sb.append("\t\t" + dbObject.getObjectType() + " " + _entityFieldName + " = new " + dbObject.getObjectType() + "(); // Generate data for: " +_entityFieldName + "\r\n");
			for(String fieldName : dbObject.values().keySet()) {
				Object value = dbObject.values().get(fieldName);
				createSetter(sb, _entityFieldName, fieldName, value);
			}
			sb.append("\t\tem.persist(" + _entityFieldName + ");\r\n");
			this.dbObjectFieldNameMap.put(dbObject, _entityFieldName);
		} else {
			// object already created...
		}
	}
	
	private void createSetter(StringBuilder sb, String _entityFieldName, String fieldName, Object value) {
		String setterMethod = getSetterMethod(fieldName);
		if(value instanceof Integer) {
			sb.append("\t\t" + _entityFieldName + "." + setterMethod + "("+ value + ");\r\n");
		} else if(value instanceof String) {
			sb.append("\t\t" + _entityFieldName + "." + setterMethod + "(\""+ value + "\");\r\n");
		} else if(value instanceof List) {
			String listName = _entityFieldName+"_"+fieldName+"_"+Integer.toHexString(value.hashCode());
			sb.append("\t\tList<?> " + listName + " = new ArrayList<>();\r\n");
			int elementCounter = 0;
			for(Object listElement : (List<?>)value) {
				String elementName = listName + "_" + elementCounter;
				if(listElement instanceof DBObject) {
					String listElementFieldName = dbObjectFieldNameMap.get((DBObject)listElement);
					if(listElementFieldName != null) {
						sb.append("\t\tObject"+elementName+" = "+ listElementFieldName +";\r\n");
					} else {
						generateNewObject(sb, (DBObject)listElement);
						sb.append("\t\tObject "+elementName+" = "+ dbObjectFieldNameMap.get((DBObject)listElement) +";\r\n");
					}
				}
				sb.append("\t\t"+listName+".add("+elementName+");\r\n");
				elementCounter++;
			}			
			sb.append("\t\t" + _entityFieldName + "." + getSetterMethod(fieldName) + "("+listName+ ");\r\n");
		} else if(value instanceof DBObject) {
			String listElementFieldName = dbObjectFieldNameMap.get((DBObject)value);
			if(listElementFieldName != null) {
				sb.append("\t\t" + _entityFieldName + "." + setterMethod + "("+ listElementFieldName + ");\r\n");
			} else {
				generateNewObject(sb, (DBObject)value);
				sb.append("\t\t" + _entityFieldName + "." + setterMethod + "("+ dbObjectFieldNameMap.get((DBObject)value) + ");\r\n");
			}
		}
	}

	private String generateNewEntityLocalFieldName(String entityName) {
		String[] splitted = entityName.split("\\.");
		String simpleEntityName = splitted[splitted.length-1].toLowerCase();
		int counter = getEntityLocalFieldCounter(entityName);
		String name = simpleEntityName + counter;
		counter++;
		this.entityCounterMap.put(entityName, counter);
		return name;
	}
	
	private Integer getEntityLocalFieldCounter(String entityName) {
		Integer counter = this.entityCounterMap.get(entityName);
		if(counter == null) {
			counter = new Integer(0);
			this.entityCounterMap.put(entityName, counter);
		}
		return counter;
	}
}
