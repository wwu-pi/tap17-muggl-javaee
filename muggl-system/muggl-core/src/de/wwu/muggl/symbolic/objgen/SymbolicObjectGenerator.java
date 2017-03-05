package de.wwu.muggl.symbolic.objgen;

import java.lang.reflect.ParameterizedType;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSignature;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.vm.var.ReferenceCollectionVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class SymbolicObjectGenerator {

	public Object getReferenceValue(SymbolicVirtualMachine svm, String parentName, Field field) throws SymbolicObjectGenerationException {
		return getReferenceValue(svm, parentName, field, true);
	}
	
	public Object getReferenceValue(SymbolicVirtualMachine svm, String parentName, Field field, boolean allowNested) throws SymbolicObjectGenerationException {
		if(field.getType().endsWith("[]")) {
			return getSymbolicArrayReference(svm, parentName, field.getName(), field.getType(), true);
		} else {
			return getSymbolicObjectReference(svm, parentName, field, true, allowNested);
		}
	}
	
	
	public Object getReferenceValue(Frame frame, String parentName, Field field) throws SymbolicObjectGenerationException {
		return getReferenceValue((SymbolicVirtualMachine)frame.getVm(), parentName, field);
	}

	public SymbolicArrayref getSymbolicArrayReference(Frame frame, String parentName, String fieldName, String fieldType) throws SymbolicObjectGenerationException {
		return getSymbolicArrayReference((SymbolicVirtualMachine)frame.getVm(), parentName, fieldName, fieldType, true);
	}

	/**
	 * 
	 * @param frame the frame that is currently executed
	 * @param parentName the name of the parent, i.e. the container that this field belongs to
	 * @param fieldName the name of the field
	 * @param fieldType the field type, must end with []
	 * @return
	 * @throws SymbolicObjectGenerationException
	 */
	public SymbolicArrayref getSymbolicArrayReference(SymbolicVirtualMachine svm, String parentName, String fieldName, String fieldType, boolean addConstraints) throws SymbolicObjectGenerationException {
		
		if(!fieldType.endsWith("[]")) {
			throw new SymbolicObjectGenerationException("Given field type ["+fieldType+"] is not a array, it must end on []");
		}
		
		ReferenceValue refVal = null;
		try {
			String arrayTypeString = fieldType.substring(0, fieldType.length()-2);
			refVal = getReferenceValue(getTypeNumber(arrayTypeString), arrayTypeString, svm);
		} catch (Exception e) {
			throw new SymbolicObjectGenerationException("Could not get reference value for field with type: " + fieldType, e);          
		}
		
		String arrayName = parentName + "." + fieldName;
		SymbolicArrayref arrayRef = new SymbolicArrayref(refVal.getInitializedClass(), arrayName);
		
		if(addConstraints) {
			// add a constraint that the length of the array must be >= 0
			NumericVariable arrayLengthVariable = arrayRef.getSymbolicLength();
		
			ConstraintExpression staticConstraint = GreaterOrEqual.newInstance(arrayLengthVariable, NumericConstant.getZero(Expression.INT));
//			svm.getSolverManager().addStaticConstraint(staticConstraint);
			svm.getSolverManager().addConstraint(staticConstraint);
		}
		
		return arrayRef;
	}

	
	public ReferenceVariable getSymbolicObjectReference(Frame frame, String parentName, Field field) throws SymbolicObjectGenerationException {
		return getSymbolicObjectReference((SymbolicVirtualMachine)frame.getVm(), parentName, field, true);
	}

	public ReferenceVariable getSymbolicObjectReference(SymbolicVirtualMachine svm, String parentName, Field field, boolean addConstraints) throws SymbolicObjectGenerationException {
		return getSymbolicObjectReference(svm, parentName, field, addConstraints, true);
	}
	
	public ReferenceVariable getSymbolicObjectReference(SymbolicVirtualMachine svm, String parentName, Field field, boolean addConstraints, boolean allowNested) throws SymbolicObjectGenerationException {
		String fieldType = field.getType();
		String fieldName = field.getName();
		
		String referenceFieldName = parentName + "." + fieldName;
		
		if(fieldName.equals("parts") || fieldType.equals("java.util.List")) {
			System.out.println("here we go");
		}
		
		if(fieldType.equals("java.util.Collection") || fieldType.equals("java.util.List")) {
			ClassFile classFile = null;
			try {
				classFile = svm.getClassLoader().getClassAsClassFile("java.util.ArrayList");
			} catch (ClassFileException e) {
				throw new SymbolicObjectGenerationException("Could not get reference value for field with type: java.util.ArrayList");
			}
			Objectref fieldReferenceValue = svm.getAnObjectref(classFile);
			
			Field elementDataField = fieldReferenceValue.getInitializedClass().getClassFile().getFieldByName("elementData");
			ReferenceCollectionVariable referenceValue = new ReferenceCollectionVariable(referenceFieldName, fieldReferenceValue, (JPAVirtualMachine)svm);
			
			byte signatureConstantPoolIndex = ((AttributeSignature)field.getAttributes()[0]).getSignatureIndex();
			if(signatureConstantPoolIndex < 0) {
				// workaround : get it from reflection
				System.out.println("--");
				try {
					Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(classFile.getName());
					java.lang.reflect.Field f = clazz.getField(field.getName());
					Object o = ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
					String x;
				} catch(Exception e) {
					
				}
			}
			
			ConstantUtf8 constant = (ConstantUtf8)field.getClassFile().getConstantPool()[signatureConstantPoolIndex];
			String collectionTypeString = constant.getValue().substring(field.getType().length()+2, constant.getValue().length()-2);
			String colType = getCollectionType(collectionTypeString);
			if(colType == null) throw new RuntimeException("Could not get type of generic: " + field);
			SymbolicArrayref symArray = getSymbolicArrayReference(svm, referenceFieldName, "elementData", colType+"[]", addConstraints);
			referenceValue.putField(elementDataField, symArray);
			referenceValue.setCollectionType(colType);
			
			Field sizeField = fieldReferenceValue.getInitializedClass().getClassFile().getFieldByName("size");
			NumericVariable elementDataLengthVar = new NumericVariable(referenceFieldName+".size", Expression.INT);
			referenceValue.putField(sizeField, elementDataLengthVar);

			if(addConstraints) {
				svm.getSolverManager().addConstraint(
						GreaterOrEqual.newInstance(elementDataLengthVar, NumericConstant.getZero(Expression.INT)));
				svm.getSolverManager().addConstraint(
						NumericEqual.newInstance(elementDataLengthVar, symArray.getSymbolicLength()));
			}
			
			return referenceValue;
		}
		

		ClassFile classFile = null;
		try {
			classFile = svm.getClassLoader().getClassAsClassFile(fieldType);
		} catch (ClassFileException e) {
			throw new SymbolicObjectGenerationException("Could not get reference value for field with type: " + fieldType);
		}
		Objectref fieldReferenceValue = svm.getAnObjectref(classFile);
		

		
		ReferenceVariable referenceValue = new ReferenceVariable(referenceFieldName, fieldReferenceValue, (JPAVirtualMachine)svm);
		
		if(!allowNested) {
			return referenceValue;
		}
		
		
		for(Field refField : classFile.getFields()) {
			if(!refField.isAccStatic()) {
				String type = refField.getType();
				String name = refField.getName();
				String newRefFieldName = referenceValue.getName() + "." + name;
				
				Object fieldValue = null;
				if(type.equals("int") || type.equals("java.lang.Integer")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.INT);
				} else if(type.equals("double") || type.equals("java.lang.Double")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.DOUBLE);
				} else if(type.equals("float") || type.equals("java.lang.Float")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.FLOAT);
				} else if(type.equals("char") || type.equals("java.lang.Character")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.CHAR);
				} else if(type.equals("boolean") || type.equals("java.lang.Boolean")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.INT);
					((NumericVariable)fieldValue).setIsBoolean(true);
				} else if(type.equals("byte") || type.equals("java.lang.Byte")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.BYTE);
				} else if(type.equals("long") || type.equals("java.lang.Long")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.LONG);
				} else if(type.equals("short") || type.equals("java.lang.Short")) {
					fieldValue = new NumericVariable(newRefFieldName, Expression.SHORT);
				} else {
					// no numeric value, must be another reference value
					
					
					// if it is a string, OK with nested..
					if(type.equals("java.lang.String")) {
						fieldValue = getReferenceValue(svm, referenceFieldName, refField, true);
					} else {
						fieldValue = getReferenceValue(svm, referenceFieldName, refField, false);
					}
				}
				
				referenceValue.putField(refField, fieldValue);
			}
		}
		
		addSpecialConstraints(svm, classFile, referenceValue);
		
		return referenceValue;
	}
	
	
	private String getCollectionType(String collectionTypeString) {
		// Lorg/oracle/roster/entity/Team;
		if(collectionTypeString.startsWith("L")) {
			return collectionTypeString.substring(1, collectionTypeString.length()-1).replace('/', '.');
		}
		return null;
	}


	private void addSpecialConstraints(SymbolicVirtualMachine svm, ClassFile classFile, ReferenceVariable referenceVariable) {
		if(classFile.getName().equals("java.util.ArrayList")) {
			// add constraint that the size of the array list must be the size of its internal array of elementData
			
			// first, get the variable of the length of the field element data
			Field elementDataField = classFile.getFieldByName("elementData");
			Object elementDataValue = referenceVariable.getField(elementDataField);
			
			NumericVariable elementDataLengthVar = null;
			if(elementDataValue instanceof SymbolicArrayref) {
				SymbolicArrayref elementDataSymArrayRef = (SymbolicArrayref)elementDataValue;
				elementDataLengthVar = elementDataSymArrayRef.getSymbolicLength();
			}
			
			// second, get the variable of the field size of the given arraylist
			Field sizeField = classFile.getFieldByName("size");
			Object sizeValue = referenceVariable.getField(sizeField);
			
			NumericVariable arrayListSizeVar = null;
			if(sizeValue instanceof NumericVariable) {
				arrayListSizeVar = (NumericVariable)sizeValue;
			}
			
			// last, add the constraint
			svm.getSolverManager().addConstraint(
					NumericEqual.newInstance(elementDataLengthVar, arrayListSizeVar));
		}
	}


	protected byte getTypeNumber(String typeString) {
		if (typeString.equals("boolean")) {
			return ClassFile.T_BOOLEAN;
		} else if (typeString.equals("byte")) {
			return ClassFile.T_BYTE;
		} else if (typeString.equals("char")) {
			return ClassFile.T_CHAR;
		} else if (typeString.equals("double")) {
			return ClassFile.T_DOUBLE;
		} else if (typeString.equals("float")) {
			return ClassFile.T_FLOAT;
		} else if (typeString.equals("int")) {
			return ClassFile.T_INT;
		} else if (typeString.equals("long")) {
			return ClassFile.T_LONG;
		} else if (typeString.equals("short")) {
			return ClassFile.T_SHORT;
		}

		// It has to be a reference type.
		return 0;
	}
	
	protected ReferenceValue getReferenceValue(byte type, String typeName, Frame frame) throws Exception {
		return getReferenceValue(type, typeName, frame.getVm());
	}
	
	protected ReferenceValue getReferenceValue(byte type, String typeName, VirtualMachine vm) throws Exception {
		MugglClassLoader classLoader = vm.getClassLoader();
		
		if (type == ClassFile.T_BOOLEAN) {
			return classLoader.getClassAsClassFile("java.lang.Boolean")
					.getAPrimitiveWrapperObjectref(vm);
		} else if (type == ClassFile.T_BYTE) {
			return classLoader.getClassAsClassFile("java.lang.Byte")
					.getAPrimitiveWrapperObjectref(vm);
		} else if (type == ClassFile.T_CHAR) {
			return classLoader.getClassAsClassFile("java.lang.Character")
					.getAPrimitiveWrapperObjectref(vm);
		} else if (type == ClassFile.T_DOUBLE) {
			return classLoader.getClassAsClassFile("java.lang.Double")
					.getAPrimitiveWrapperObjectref(vm);
		} else if (type == ClassFile.T_FLOAT) {
			return classLoader.getClassAsClassFile("java.lang.Float")
					.getAPrimitiveWrapperObjectref(vm);
		} else if (type == ClassFile.T_INT) {
			return classLoader.getClassAsClassFile("java.lang.Integer")
					.getAPrimitiveWrapperObjectref(vm);
		} else if (type == ClassFile.T_LONG) {
			return classLoader.getClassAsClassFile("java.lang.Long")
					.getAPrimitiveWrapperObjectref(vm);
		} else if (type == ClassFile.T_SHORT) {
			return classLoader.getClassAsClassFile("java.lang.Short")
					.getAPrimitiveWrapperObjectref(vm);
		} else {
			ClassFile classFile = vm.getClassLoader().getClassAsClassFile(typeName);
			return vm.getAnObjectref(classFile);
		}
	}
}
