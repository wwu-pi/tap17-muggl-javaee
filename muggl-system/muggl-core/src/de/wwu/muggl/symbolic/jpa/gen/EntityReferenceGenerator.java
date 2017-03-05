package de.wwu.muggl.symbolic.jpa.gen;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerator;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class EntityReferenceGenerator {

	protected JPAVirtualMachine vm;
	
	protected Map<String, Integer> entityNameMap;
	
	// TODO: um eindeutige namen zu habne, hier eigentlich nur einmal den entity generaotr erstlelen, damit entityNameMap auhc eindeutig ist ->z.b in der VM speichern...
	public EntityReferenceGenerator(JPAVirtualMachine vm) {
		this.vm = vm;
		this.entityNameMap = new HashMap<>();
	}
	
	public ReferenceVariable generateNewEntityReference(String entityClassName) {
		try {
			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(entityClassName);
			Objectref referenceValue = this.vm.getAnObjectref(entityClassFile);
			String name = getReferenceEntityName(entityClassName);
			ReferenceVariable refVar = new ReferenceVariable(name, referenceValue, this.vm);
			
			SymbolicObjectGenerator symbolicObjectGenerator = new SymbolicObjectGenerator();
			for(Field field : entityClassFile.getFields()) {
				if(!field.isAccStatic()) {
					
					if(field.getType().endsWith("[]")) {
						SymbolicArrayref symArrayRef = null;
						try {
							symArrayRef = symbolicObjectGenerator.getSymbolicArrayReference(vm.getCurrentFrame(), name, field.getName(), field.getType());
						} catch (SymbolicObjectGenerationException e) {
							throw new RuntimeException("Could not get symbolic array reference for field: " + field.getName(), e);
						}
						refVar.putField(field, symArrayRef);
					} else if(field.isPrimitiveType()) {
						String fieldName = name + "." + field.getName();
						Object fieldValue = null;
						if(field.getType().equals("int") || field.getType().equals("java.lang.Integer")) {
							fieldValue = new NumericVariable(fieldName, Expression.INT);
						} else if(field.getType().equals("double") || field.getType().equals("java.lang.Double")) {
							fieldValue = new NumericVariable(fieldName, Expression.DOUBLE);
						} else if(field.getType().equals("float") || field.getType().equals("java.lang.Float")) {
							fieldValue = new NumericVariable(fieldName, Expression.FLOAT);
						} else if(field.getType().equals("char") || field.getType().equals("java.lang.Character")) {
							fieldValue = new NumericVariable(fieldName, Expression.CHAR);
						} else if(field.getType().equals("boolean") || field.getType().equals("java.lang.Boolean")) {
							fieldValue = new NumericVariable(fieldName, Expression.BOOLEAN);
						} else if(field.getType().equals("byte") || field.getType().equals("java.lang.Byte")) {
							fieldValue = new NumericVariable(fieldName, Expression.BYTE);
						} else if(field.getType().equals("long") || field.getType().equals("java.lang.Long")) {
							fieldValue = new NumericVariable(fieldName, Expression.LONG);
						} else if(field.getType().equals("short") || field.getType().equals("java.lang.Short")) {
							fieldValue = new NumericVariable(fieldName, Expression.SHORT);
						}
						refVar.putField(field, fieldValue);
					} else {
						ReferenceVariable refFieldVar = null;
						try {
							refFieldVar = symbolicObjectGenerator.getSymbolicObjectReference(vm.getCurrentFrame(), name, field);
						} catch (SymbolicObjectGenerationException e) {
							throw new RuntimeException("Could not get symbolic object reference for field: " + field.getName(), e);
						}
						refVar.putField(field, refFieldVar);
					}
					
				}
			}
			
			return refVar;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public ReferenceVariable generateNewEntityReference(Objectref classReference) {
		String entityClassName = getClassNameOfClassObjectRef(classReference);
		return generateNewEntityReference(entityClassName);
	}
	
	public ReferenceVariable generateNewEntityReferenceOLD(Objectref classReference) {
		String entityClassName = getClassNameOfClassObjectRef(classReference);
		
		try {
			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(entityClassName);
			Objectref objectRef = this.vm.getAnObjectref(entityClassFile);
			
			return new ReferenceVariable(getReferenceEntityName(entityClassName), objectRef, this.vm);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	protected String getReferenceEntityName(String entityName) {
		Integer number = entityNameMap.get(entityName);
		if(number == null) {
			number = 0;
		}
		String name = entityName+"#"+number;
		this.entityNameMap.put(entityName, ++number);
		return name;
	}
	
	public String getClassNameOfClassObjectRef(Objectref classObjectRef) {
		for(Field f : classObjectRef.getFields().keySet()) {
			if(f.getName().equals("name")) {
				return getStringValueOfObjectRef((Objectref)classObjectRef.getFields().get(f));
			}
		}
		return null;
	}
	
	public String getStringValueOfObjectRef(Objectref oRef) {
		Enumeration<Field> fields = oRef.getFields().keys();
		String entityClassName = "";
		while(fields.hasMoreElements()){
			Field f = fields.nextElement();
			if(f.getName().equals("value")) {
				Object THE_CLASS = oRef.getField(f);
				Arrayref classNameRef = (Arrayref)THE_CLASS;
				for(int i=0; i<classNameRef.length; i++) {
					IntConstant ic = (IntConstant)classNameRef.getElement(i);
					byte asciiNumber = (byte)ic.getValue();
					// TODO: concatenate via string builder...
					entityClassName += Character.toString((char)asciiNumber);
				}
			}
		}
		return entityClassName;
	}
}
