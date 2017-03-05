package de.wwu.muggl.vm.var.sym.gen.ctr;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.gen.meta.SymoblicEntityFieldGenerationException;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;
import de.wwu.muggl.vm.var.sym.gen.ctr.meta.StaticConstraintException;

public class SymbolicStaticEntityConstraints {
	
	protected SymoblicEntityFieldGenerator fieldGenerator;
	private JPAVirtualMachine vm;
	
	public SymbolicStaticEntityConstraints(JPAVirtualMachine vm) {
		this.vm = vm;
		this.fieldGenerator = new SymoblicEntityFieldGenerator(vm);
	}
	
	public void addStaticConstraints(Objectref entityObject) throws VmRuntimeException {
		ClassFile classFile = entityObject.getInitializedClass().getClassFile();
		
		ClassFile entityFieldClassFile = classFile;
		while(entityFieldClassFile != null) {
			applyStaticFieldConstraints(entityObject, entityFieldClassFile);
			try {
				entityFieldClassFile = entityFieldClassFile.getSuperClassFile();
			} catch (ClassFileException e) {
				e.printStackTrace();
				break;
			}
		}
		
		// Add ID special constraints, only if NOT generated
		if(!isGeneratedIdAttribute(entityObject)) {
			String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityObject.getObjectType());
			Field idField = getIdField(classFile, idFieldName);
			Object idReference = entityObject.getField(idField);
			
			if(idReference instanceof NumericVariable) {
				this.vm.getSolverManager().addConstraint(GreaterThan.newInstance((NumericVariable)idReference, NumericConstant.getInstance(0, Expression.INT)));
			}
			if(idReference instanceof EntityStringObjectref) {
				EntityStringObjectref stringObj = (EntityStringObjectref)idReference;
				Field valueField = stringObj.getInitializedClass().getClassFile().getFieldByName("value");
				SymbolicArrayref symArray = (SymbolicArrayref)stringObj.getField(valueField);
				this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(symArray.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			}
			if(idReference instanceof ReferenceVariable && ((ReferenceVariable)idReference).getObjectType().equals("java.lang.String")) {
				ReferenceVariable refVar = (ReferenceVariable)idReference;
				Field valueField = refVar.getInitializedClass().getClassFile().getFieldByName("value");
				SymbolicArrayref symArray = (SymbolicArrayref)refVar.getField(valueField);
				this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(symArray.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			}
		}
		
		try {
			if(!this.vm.getSolverManager().hasSolution()) {
				System.err.println("NO SOLUTION AFTER STATIC CONSTRAINTS");
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (SolverUnableToDecideException e) {
			e.printStackTrace();
		}
	}

	protected void applyStaticFieldConstraints(Objectref entityObject,	ClassFile classFile) throws VmRuntimeException {
		for(Field field : classFile.getFields()) {
			for(Attribute attribute : field.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations runAtt = (AttributeRuntimeVisibleAnnotations)attribute;
					for(Annotation anno : runAtt.getAnnotations()) {
						Constant c = classFile.getConstantPool()[anno.getTypeIndex()];
						if(c.getStringValue().equals("Ljavax/validation/constraints/NotNull;")) {
							applyNotNullConstraint(vm, entityObject, field.getName());
						}
						if(c.getStringValue().equals("Ljavax/validation/constraints/Size;")) {
							String minimumValue = anno.getElementValuePairs()[0].getElementValues().getStringValue();
							applySizeConstraint(vm, entityObject, field.getName(), Integer.parseInt(minimumValue));
						}
						if(c.getStringValue().equals("Ljavax/validation/constraints/Min;")) {
							String minimumValue = anno.getElementValuePairs()[0].getElementValues().getStringValue();
							applyMinConstraint(vm, entityObject, field.getName(), Integer.parseInt(minimumValue));
						} 
						if(c.getStringValue().equals("Ljavax/validation/constraints/Max;")) {
							String maxValue = anno.getElementValuePairs()[0].getElementValues().getStringValue();
							applyMaxConstraint(vm, entityObject, field.getName(), Integer.parseInt(maxValue));
						}
						if(c.getStringValue().equals("Ljavax/persistence/Column;")) {
							String isUnique = anno.getElementValuePairs()[0].getElementValues().getStringValue();
							if(isUnique.equals("1")) {
								applyUniqueConstraint(vm, entityObject, field.getName());
							}
						}
					}
				}
			}
		}
	}
	

	
	
	private void applySizeConstraint(JPAVirtualMachine vm, Objectref entityObject, String attributeName, int parseInt) throws VmRuntimeException {
		applyMinConstraint(vm, entityObject, attributeName, parseInt);
	}

	protected Field getIdField(ClassFile entityClassFile) {
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityClassFile.getName());
		return entityClassFile.getFieldByName(idFieldName, true);
	}
	
	private boolean isGeneratedIdAttribute(Objectref objectToCheck) {
		ClassFile classFile = objectToCheck.getInitializedClass().getClassFile();
		
		// check field annotations
		Field idField = getIdField(classFile);
		for(Attribute attribute : idField.getAttributes()) {
			AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
			for(Annotation anno : attributeAnnotation.getAnnotations()) {
				if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
					return true;
				}
			}
		}
		
		// check method annotations
		Method idMethod = null;
		String idMethodName = "get"+idField.getName();
		for(Method m : classFile.getMethods()) {
			if(m.getName().toUpperCase().equals(idMethodName.toUpperCase())) {
				idMethod = m;
				break;
			}
		}
		if(idMethod != null) {
			for(Attribute attribute : idMethod.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation anno : attributeAnnotation.getAnnotations()) {
						if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private Field getIdField(ClassFile entityClassFile, String idFieldName) {
		try {
			Field field = entityClassFile.getFieldByName(idFieldName, true);
			return field;
		} catch(FieldResolutionError e) {
			try {
				if(entityClassFile.getSuperClassFile() != null) {
					return getIdField(entityClassFile.getSuperClassFile(), idFieldName);
				}
			} catch (ClassFileException e1) {
			}
		}
		throw new RuntimeException("ID field for entity class: " + entityClassFile + " not found");
	}

	private void applyUniqueConstraint(JPAVirtualMachine vm, Objectref entityObject, String attributeName) {
		Field field = entityObject.getInitializedClass().getClassFile().getFieldByName(attributeName, true);
		Object unqiueValue = entityObject.getField(field);
		if(unqiueValue instanceof NumericVariable) {
			NumericVariable nv = (NumericVariable)unqiueValue;
			vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(nv, NumericConstant.getInstance(1, nv.getType())));
		} else if(unqiueValue instanceof EntityStringObjectref) {
			Field f = ((EntityStringObjectref) unqiueValue).getInitializedClass().getClassFile().getFieldByName("value");
			SymbolicArrayref s = (SymbolicArrayref)((EntityStringObjectref) unqiueValue).getField(f);
			NumericVariable nv = s.getSymbolicLength();
			vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(nv, NumericConstant.getInstance(1, nv.getType())));
		}
	}

	private void applyMinConstraint(JPAVirtualMachine vm, Objectref entityObject, String attributeName, int minimumValue) throws VmRuntimeException {
		Field field = entityObject.getInitializedClass().getClassFile().getFieldByName(attributeName, true);
		Object fieldValue = entityObject.getField(field);
		if(fieldValue == null) {
			try {
				fieldValue = fieldGenerator.generateSymbolicEntityField(entityObject.getName()+"."+field.getName(), field, entityObject);
				entityObject.putField(field, fieldValue);
			} catch (SymoblicEntityFieldGenerationException e) {
				return;
			} 
		}
		
		if(fieldValue instanceof NumericVariable) {
			NumericVariable nv = (NumericVariable)fieldValue;
			vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(nv, NumericConstant.getInstance(minimumValue, nv.getType())));
		} else if(fieldValue instanceof Objectref && ((Objectref)fieldValue).getObjectType().equals("java.lang.String")) {
			Objectref stringRef = (Objectref)fieldValue;
			Field f = stringRef.getInitializedClass().getClassFile().getFieldByName("value");
			Object value = stringRef.getField(f);
			if(value instanceof SymbolicArrayref) {
				SymbolicArrayref sa =(SymbolicArrayref)value;
				vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(sa.getSymbolicLength(), NumericConstant.getInstance(minimumValue, Expression.INT)));
			}
		} else if(fieldValue instanceof NumericConstant) {
			NumericConstant nc = (NumericConstant)fieldValue;
			if(nc.getIntValue() < minimumValue) {
				throw new VmRuntimeException(vm.generateExc("javax.validation.ConstraintViolationException"));
			}
		}
		else {
			throw new StaticConstraintException("@Min on field: "+ field + " with value of type: " + fieldValue + " not supported yet");
		}
	}
	
	private void applyMaxConstraint(JPAVirtualMachine vm, Objectref entityObject, String attributeName, int maxValue) {
		Field field = entityObject.getInitializedClass().getClassFile().getFieldByName(attributeName, true);
		Object fieldValue = entityObject.getField(field);
		if(fieldValue instanceof NumericVariable) {
			NumericVariable nv = (NumericVariable)fieldValue;
			vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(NumericConstant.getInstance(maxValue, nv.getType()), nv));
		} else {
			throw new StaticConstraintException("@Max on field: "+ field + " with value of type: " + fieldValue + " not supported yet");
		}
	}	
	
	private void applyNotNullConstraint(JPAVirtualMachine vm, Objectref entityObject, String attributeName) throws VmRuntimeException{
		Field field = entityObject.getInitializedClass().getClassFile().getFieldByName(attributeName, true);
		Object fieldValue = entityObject.getField(field);
		if(field.getType().equals(String.class.getName())) {
			Objectref stringObjectref = (Objectref)fieldValue;
			Field stringValueField = stringObjectref.getInitializedClass().getClassFile().getFieldByName("value");
			Object arrayRef = stringObjectref.getField(stringValueField);
			if(arrayRef instanceof SymbolicArrayref) {
				SymbolicArrayref symValueArrayref = (SymbolicArrayref)arrayRef;
				vm.getSolverManager().addConstraint(GreaterThan.newInstance(symValueArrayref.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			} else if(arrayRef instanceof Arrayref) {
				Arrayref a = (Arrayref)arrayRef;
				if(a.getLength() == 0) {
					throw new VmRuntimeException(vm.generateExc("javax.validation.ConstraintViolationException"));
				}
			}
		} else {
			throw new StaticConstraintException("@NotNull on field: "+ field + " not supported yet");
		}
	}
	
	
}
