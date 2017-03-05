//package de.wwu.muggl.symbolic.testCases.jpa;
//
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import de.wwu.muggl.db.obj.DBObject;
//
//
//public class JPARequiredEntityGenerator {
//	
//	private Map<String, Integer> entityCounterMap;
//	private Map<String, DBObject> localFieldNameMap;
//	
//	public JPARequiredEntityGenerator() {
//		this.entityCounterMap = new HashMap<>();
//		this.localFieldNameMap = new HashMap<>();
//	}
//	
//	public StringBuilder generatePreExecutionRequiredString(Set<DBObject> dbSolution) {
//		StringBuilder sb = new StringBuilder();
//		
//		boolean preExecutionDataExists = false;
//		for(DBObject dbObject : dbSolution) {
//			
//			sb.append("\t\t// Generate data for: " + _entityFieldName + "\r\n");
//		}
//		
//		for(String entityName : dbSolution.keySet()) {
//			
//			sb.append("\t\t// *** Create required data for entity: " + entityName + " *** \r\n");
//			List<DBObject> solutionEntryList = dbSolution.get(entityName);
//			
//			for(DBObject solutionEntry : solutionEntryList) {
//				preExecutionDataExists = true;
//				
//				String _entityFieldName = generateNewEntityLocalFieldName(entityName);
//				sb.append("\t\t// Generate data for: " + _entityFieldName + "\r\n");
//				addNewEntityObject2StringBuilder(sb, entityName, _entityFieldName, solutionEntry);
//				
//				sb.append("\t\tem.persist("+ _entityFieldName + ");\r\n\r\n");
//				
//			}
//		}
//		
//		if(!preExecutionDataExists) {
//			sb.append("\t\t// no pre-execution data required for this test case\r\n");
//		}
//		
//		return sb;
//	}
//	
//	private void addNewEntityObject2StringBuilder(StringBuilder sb, String entityName, String _entityFieldName, DBObject solutionEntry) {
//		sb.append("\t\t" + entityName + " " + _entityFieldName + " = new " + entityName + "();\r\n");
//		localFieldNameMap.put(_entityFieldName, solutionEntry);
//		for(String fieldName : solutionEntry.values().keySet()) {
//			
//			Object value = solutionEntry.values().get(fieldName);
//			generateSetValue(sb, _entityFieldName, fieldName, value);
//		}
//	}
//	
//	private void generateSetValue(StringBuilder sb, String _entityFieldName, String fieldName, Object value) {
//		if(value instanceof Integer) {
//			sb.append("\t\t" + _entityFieldName + "." + getSetterMethod(fieldName) + "("+value + ");\r\n");
//		} else if(value instanceof List) {
//			String listName = _entityFieldName+"_"+Integer.toHexString(value.hashCode());
//			sb.append("\t\tList<?> " + listName + " = new ArrayList<?>();\r\n");
//			int elementCounter = 0;
//			for(Object listElement : (List<?>)value) {
//				String elementName = listName + "_" + elementCounter;
//				try {
//					generateListElement(sb, elementName, listElement);
//				} catch (Exception e) {
//					sb.append("\t\t// could not generate list element for value=[" + listElement +"] - reason: " + e.getMessage() + "\r\n");
//					e.printStackTrace();
//				}
//				sb.append("\t\t"+listName+".add("+elementName+");\r\n");
//				elementCounter++;
//			}
//			sb.append("\t\t" + _entityFieldName + "." + getSetterMethod(fieldName) + "("+listName+ ");\r\n");
//		} else {
//			
//			sb.append("\t\t// ooops, set of value=["+value+"] failed for field=["+fieldName+"]\r\n");
//		}
//	}
//	
//	private void generateListElement(StringBuilder sb, String elementName, Object listElement) throws Exception {
//		if(listElement instanceof Integer) {
//			sb.append("\t\tInteger " + elementName + " = " + (Integer)listElement + ";\r\n");
//		} else {
//			String elementType = listElement.getClass().getName();
//			sb.append("\t\t"+elementType + " " + elementName + " = new " + elementType + "();\r\n");
//			for(Field field : listElement.getClass().getDeclaredFields()) {
//				field.setAccessible(true);
//				Object fieldValue = field.get(listElement);
//				sb.append("\t\t" + elementName + "." + getSetterMethod(field.getName()) + "(" + fieldValue + ");\r\n");
//			}
//		}
//	}
//
//	private String getSetterMethod(String attribute) {
//		return "set" + attribute.toUpperCase().substring(0, 1) + attribute.substring(1);
//	}
//	
//	private Integer getEntityLocalFieldCounter(String entityName) {
//		Integer counter = this.entityCounterMap.get(entityName);
//		if(counter == null) {
//			counter = new Integer(0);
//			this.entityCounterMap.put(entityName, counter);
//		}
//		return counter;
//	}
//	
//	private String generateNewEntityLocalFieldName(String entityName) {
//		String[] splitted = entityName.split("\\.");
//		String simpleEntityName = splitted[splitted.length-1].toLowerCase();
//		int counter = getEntityLocalFieldCounter(entityName);
//		String name = simpleEntityName + counter;
//		counter++;
//		this.entityCounterMap.put(entityName, counter);
//		return name;
//	}
//}
