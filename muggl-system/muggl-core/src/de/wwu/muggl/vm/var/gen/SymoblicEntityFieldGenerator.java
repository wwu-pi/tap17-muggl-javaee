package de.wwu.muggl.vm.var.gen;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.Entity;
import javax.persistence.metamodel.EntityType;

import de.wwu.muggl.solvers.expressions.BooleanVariable;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerator;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSignature;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceCollectionVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.meta.SymoblicEntityFieldGenerationException;
import de.wwu.muggl.vm.var.gen.types.EntityDateObjectref;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;

public class SymoblicEntityFieldGenerator {
	
	protected JPAVirtualMachine vm;
	protected SymbolicObjectGenerator symObjGen;

	public SymoblicEntityFieldGenerator(JPAVirtualMachine vm) {
		this.vm = vm;
		this.symObjGen = new SymbolicObjectGenerator();
	}
	
	public Object generateSymbolicEntityField(String nameOfFieldVariable, Field field, Objectref refVar) throws SymoblicEntityFieldGenerationException {
		return generateSymbolicEntityField(nameOfFieldVariable, field, refVar, false);
	}
	
	public Object generateSymbolicEntityField(String nameOfFieldVariable, Field field, Objectref refVar, boolean fallback) throws SymoblicEntityFieldGenerationException {	
		if(field.getType().equals(java.lang.String.class.getName())) {
			return generateString(nameOfFieldVariable);
		}
		
		if(field.getType().equals(java.util.Collection.class.getName())) {
			return generateList(field, nameOfFieldVariable, refVar);
		}
		
		if(field.getType().equals(java.util.List.class.getName())) {
			return generateList(field, nameOfFieldVariable, refVar);
		}
		
		if(field.getType().equals("int") || field.getType().equals(java.lang.Integer.class.getName())) {
			return new NumericVariable(nameOfFieldVariable, Expression.INT);
		}
		
		if(field.getType().equals("long") || field.getType().equals(java.lang.Long.class.getName())) {
			return new NumericVariable(nameOfFieldVariable, Expression.LONG);
		}
		
		if(field.getType().equals("double") || field.getType().equals(java.lang.Double.class.getName())) {
			return new NumericVariable(nameOfFieldVariable, Expression.DOUBLE);
		}
		
		if(field.getType().equals("short") || field.getType().equals(java.lang.Short.class.getName())) {
			return new NumericVariable(nameOfFieldVariable, Expression.SHORT);
		}
		
		if(field.getType().equals("boolean") || field.getType().equals(java.lang.Boolean.class.getName())) {
			NumericVariable nv = new NumericVariable(nameOfFieldVariable, Expression.INT);
			nv.setIsBoolean(true);
			return nv;
		}
		
		if(field.getType().equals(java.util.Date.class.getName())) {
			return generateDate(nameOfFieldVariable);
		}
		
		// check if the type is an entity
		if(isEntityType(field.getType())) {
			return generateEntityObjectref(field.getType(), nameOfFieldVariable);
		}
		
		if(fallback) {
			try {
				if(field.getType().endsWith("[]")) {
					return symObjGen.getSymbolicArrayReference(this.vm, nameOfFieldVariable, field.getName(), field.getType(), true);
				} else {
					return symObjGen.getSymbolicObjectReference(this.vm, nameOfFieldVariable, field, true);
				}
			} catch (SymbolicObjectGenerationException e) {
				throw new SymoblicEntityFieldGenerationException("Error while generating values for field: "+ field.getName());
			}
		}
		
		throw new SymoblicEntityFieldGenerationException("Type of field=[" + field + "] not supported yet in entity generation process.");
	}
	
	protected EntityDateObjectref generateDate(String nameOfFieldVariable) throws SymoblicEntityFieldGenerationException {
		try {
			ClassFile dateClassFile = this.vm.getClassLoader().getClassAsClassFile(java.util.Date.class.getName());
			Field fastTimeField = dateClassFile.getFieldByName("fastTime");
			
			Objectref dateObjectref = this.vm.getAnObjectref(dateClassFile);
			EntityDateObjectref dateRef = new EntityDateObjectref(nameOfFieldVariable, dateObjectref);
			NumericVariable fastTimeVar = new NumericVariable(nameOfFieldVariable+".fastTime", Expression.INT);
			this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(fastTimeVar, NumericConstant.getInstance(0, Expression.INT)));
			dateRef.putField(fastTimeField, fastTimeVar);
			
			return dateRef;
		} catch(Exception e) {
			throw new SymoblicEntityFieldGenerationException("Could not generate Date field type for: " + nameOfFieldVariable, e);
		}
	}
	
	protected EntityStringObjectref generateString(String nameOfFieldVariable) throws SymoblicEntityFieldGenerationException {
		try {
			ClassFile stringClassFile = this.vm.getClassLoader().getClassAsClassFile(java.lang.String.class.getName());
			ClassFile charClassFile = this.vm.getClassLoader().getClassAsClassFile(java.lang.Character.class.getName());
			
			Objectref stringObjectref = this.vm.getAnObjectref(stringClassFile);
			EntityStringObjectref stringRef = new EntityStringObjectref(nameOfFieldVariable, stringObjectref);
			
			Field valueField = stringClassFile.getFieldByName("value");
			SymbolicArrayref symValueArray = new SymbolicArrayref(charClassFile.getInitializedClass(), nameOfFieldVariable+".value");
			vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(symValueArray.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			stringRef.putField(valueField, symValueArray);
			
			return stringRef;
		} catch(Exception e) {
			throw new SymoblicEntityFieldGenerationException("Could not generate String field type for: " + nameOfFieldVariable, e);
		}
	}
	
	
	protected ReferenceArrayListVariable generateList(Field field, String nameOfFieldVariable, Objectref refVar) throws SymoblicEntityFieldGenerationException {
		try {
			ClassFile classFile = this.vm.getClassLoader().getClassAsClassFile(java.util.ArrayList.class.getName());
			
			Field elementDataField = classFile.getFieldByName("elementData");
			ReferenceArrayListVariable referenceValue = new ReferenceArrayListVariable(nameOfFieldVariable, this.vm);
			
			String colType = null;
			// add type of this collection type
			byte signatureConstantPoolIndex = ((AttributeSignature)field.getAttributes()[0]).getSignatureIndex();
			if(signatureConstantPoolIndex < 0) {
				String name = refVar.getInitializedClass().getClassFile().getName();
				try {
					Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(name);
					java.lang.reflect.Field f = clazz.getDeclaredField(field.getName());
					Type genericType = ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
					if(genericType.toString().startsWith("interface")) {
						colType = genericType.toString().substring("interface".length()+1);
					}
					if(genericType.toString().startsWith("class")) {
						colType = genericType.toString().substring("class".length()+1);
					}
				} catch(Exception e) {
					
				}
			} else {
				ConstantUtf8 constant = (ConstantUtf8)field.getClassFile().getConstantPool()[signatureConstantPoolIndex];
				String collectionTypeString = constant.getValue().substring(field.getType().length()+2, constant.getValue().length()-2);
				colType = getCollectionType(collectionTypeString);
			}
			
			if(colType == null) colType = "java.lang.Object";
			
			ClassFile colCF = this.vm.getClassLoader().getClassAsClassFile(colType);			
			InitializedClass icCollection = colCF.getInitializedClass();
			if (icCollection == null) {
				icCollection = new InitializedClass(colCF, this.vm);
			}
			
			SymbolicArrayref symArray = getSymbolicArrayReference(icCollection, nameOfFieldVariable+".elementData");
			referenceValue.putField(elementDataField, symArray);
			referenceValue.setCollectionType(colType);
			
			return referenceValue;
		} catch(Exception e) {
			throw new SymoblicEntityFieldGenerationException("Could not generate java.util.List field type for: " + nameOfFieldVariable, e);
		}
	}
	
	protected SymbolicArrayref getSymbolicArrayReference(InitializedClass ic, String arrayName) throws SymoblicEntityFieldGenerationException{
		try {
			SymbolicArrayref arrayRef = new SymbolicArrayref(ic, arrayName);
			
			// add constraint: length >= 0
			NumericVariable arrayLengthVariable = arrayRef.getSymbolicLength();
			ConstraintExpression staticConstraint = GreaterOrEqual.newInstance(arrayLengthVariable, NumericConstant.getZero(Expression.INT));
			this.vm.getSolverManager().addConstraint(staticConstraint);
			
			return arrayRef;
		} catch(Exception e) {
			throw new SymoblicEntityFieldGenerationException("Could not generate java.lang.String field type for: " + arrayName, e);
		}
	}
	
	private String getCollectionType(String collectionTypeString) {
		// format: Lorg/oracle/roster/entity/Team;
		if(collectionTypeString.startsWith("L")) {
			return collectionTypeString.substring(1, collectionTypeString.length()-1).replace('/', '.');
		}
		return null;
	}
	
	
	
	protected EntityObjectref generateEntityObjectref(String type, String entityObjectName) throws SymoblicEntityFieldGenerationException{		
		try {
			ClassFile classFile = this.vm.getClassLoader().getClassAsClassFile(type);
			Objectref entityObjectref = this.vm.getAnObjectref(classFile);
			EntityObjectref entityObjectRef = new EntityObjectref(this, entityObjectName, entityObjectref);
			return entityObjectRef;
		} catch(Exception e) {
			throw new SymoblicEntityFieldGenerationException("Could not generate Entity reference field type for: " + type, e);
		}
	}
	
	private boolean isEntityType(String type) {
		for(EntityType<?> entityType : this.vm.getMugglEntityManager().getMetamodel().getEntities()) {
			if(entityType.getJavaType().getName().equals(type)) {
				return true;
			}
		}
		
		for(Class<?> entityClass : this.vm.getMugglEntityManager().getManagedEntityClasses()) {
			if(entityClass.getName().equals(type) && entityClass.isAnnotationPresent(Entity.class)) {
				return true;
			}
		}
		
		return false;
	}
	
	public JPAVirtualMachine getVM() {
		return this.vm;
	}
}
