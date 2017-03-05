package de.wwu.muggl.symbolic.testCases.jpa.obj;

import java.util.Map;

import de.wwu.muggl.solvers.Solution;

public class EntityObjectTextGenerator {
	
	protected ObjectValueStringManager valueStringManager;
	
	public EntityObjectTextGenerator(Solution solution) {
		this.valueStringManager = new ObjectValueStringManager(solution);
	}

	public void generateObject(StringBuilder sb, Map<String, Object> objectValues, String objectType, String objectFieldName) {
		sb.append("\t\t"+objectType+" = new "+objectType+"();\r\n");
		for(String fieldName : objectValues.keySet()) {
			generateObjectField(sb, objectFieldName, fieldName, objectValues.get(fieldName));
		}
	}

	private void generateObjectField(StringBuilder sb, String objectFieldName, String fieldName, Object object) {
		String setterName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
		String objectFieldValueString = this.valueStringManager.getFieldValueString(sb, object);
		sb.append("\t\t"+objectFieldName+"."+setterName+"(" + objectFieldValueString + ");\r\n");
	}
}
