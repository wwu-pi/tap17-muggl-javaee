package de.wwu.muggl.db.ana;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.entry.EntityEntry;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.db.sym.var.EntityReferenceVariable;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.solvers.expressions.Variable;

public class EntityAnalyzer {
	
	public static List<Field> getEntityFields(String entityClassName) throws Exception {
		List<Field> fields = new ArrayList<Field>();
		Class<?> entityClass = ClassLoader.getSystemClassLoader().loadClass(entityClassName);
		for(Field f : entityClass.getDeclaredFields()) {
			if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
				fields.add(f);
			}
		}
		return fields;
	}

	public static EntityEntry getInitialEntityEntry(String entityClassName) throws Exception {
		String name = "e_"+UUID.randomUUID().toString().substring(0, 5);
		EntityEntry entry = new EntityEntry(name, entityClassName);
		for(Field field : getEntityFields(entityClassName)) {
			String fieldName = field.getName();
			Variable variable = getInitValue(field, name+"_"+fieldName);
			entry.addValue(fieldName, variable);
		}
		return entry;
	}
	
	public static Variable getInitValue(Field field, String varName) throws Exception {
		Class<?> type = field.getType();
		if  (type.getName().equals("int")
		  || type.getName().equals(Integer.class.getName())) {
			return new NumericVariable(varName, "int");
		}
		if  (type.getName().equals("double")
		  || type.getName().equals(Double.class.getName())) {
			return new NumericVariable(varName, "double");
		}
		if  (type.getName().equals("float")
		  || type.getName().equals(Float.class.getName())) {
			return new NumericVariable(varName, "float");
		}		
		if (type.getName().equals(String.class.getName())) {
//			return new StringVariable(varName);
			throw new RuntimeException("String variable here not supported?!");
		}
		if (type.getName().equals(Collection.class.getName())) {
			Type genericType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
			String genericTypeName = genericType.toString().substring(6);
			return new CollectionVariable<DatabaseObject>(null, varName, field.getName(), genericTypeName);
		}

		return new EntityReferenceVariable(varName, type);
	}
}
