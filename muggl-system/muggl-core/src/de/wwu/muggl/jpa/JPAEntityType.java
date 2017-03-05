package de.wwu.muggl.jpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.UUID;

import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.StringVariable;
//import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;

@Deprecated
public class JPAEntityType extends Objectref {
	
	protected String entityName;
	protected Objectref original;
	protected boolean preExecutionRequired;

	public JPAEntityType(Objectref original) {
		super(original.getInitializedClass(), false);
		this.entityName = original.getName();
		this.original = original;
		this.preExecutionRequired = true; // by default, every entity type is required before executing the test case
	}
	
	public void fillInitialEntityAttributesWithVariables() throws NoSuchFieldException, SecurityException, ClassNotFoundException {
		String uuid = UUID.randomUUID().toString();
		for(Field field : original.getInitializedClass().getClassFile().getFields()) {
			if(!field.isAccStatic()) {
				Variable variable = getInitialVariable(
						field.getType(), 
						uuid+"."+field.getName(),
						ClassLoader.getSystemClassLoader().loadClass(entityName).getDeclaredField(field.getName()));
				if(variable != null) {
					fields.put(field, variable);
				}
			}
		}
	}
	
	private Variable getInitialVariable(String type, String varName, java.lang.reflect.Field field) {
		if(type.equals("int") || type.equals(Integer.class.getName())) {
			return new NumericVariable(varName, Expression.INT);
		}
		
		else if(type.equals("double") || type.equals(Double.class.getName())) {
			return new NumericVariable(varName, Expression.DOUBLE);
		}
		
		else if(type.equals("float") || type.equals(Float.class.getName())) {
			return new NumericVariable(varName, Expression.FLOAT);
		}
		
		else if(type.equals(String.class.getName())) {
//			return new StringVariable(varName);
			throw new RuntimeException("String variable here not supported?!");
		}
		
		if(type.equals(Collection.class.getName())) {
			return null;
		}
		
//		if(type.equals(Collection.class.getName())) {
//			Type genericType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
//			String genericTypeName = genericType.toString().substring(6);
//			return new CollectionVariableImpl(varName, genericTypeName);
//		}
//		
//		if(type.equals(String.class.getName())) {
//			return new StringVariable(varName);
//		}
		
		throw new RuntimeException("Could not get variable for field type: " + type);
	}

	public String getEntityName() {
		return this.entityName;
	}

	public Objectref getObjectref() {
		return this.original;
	}
}
