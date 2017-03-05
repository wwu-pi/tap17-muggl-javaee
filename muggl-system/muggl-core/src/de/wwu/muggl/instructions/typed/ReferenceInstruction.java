package de.wwu.muggl.instructions.typed;

import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionAlgorithms;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.ReferenceArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.StringVariable;
//import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerator;

/**
 * This class provides static methods to be accessed by instructions typed as a reference.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-31
 */
public class ReferenceInstruction extends TypedInstruction {

	/**
	 * Get a String representation of the desired type of this instruction. This method is used to
	 * check whather the fetched Object is an instance of the desired type. For aload it is not needed.
	 * @return A String representation of the desired type.
	 */
	@Override
	public String getDesiredType() {
		return "de.wwu.muggl.vm.initialization.ReferenceValue";
	}

	/**
	 * Returns true in this overriding method, since no check is needed here.
	 *
	 * @param objectref The objectref (unused in this overriding method).
	 * @return true.
	 */
	@Override
	public boolean checkDesiredType(String objectref) {
		return true;
	}

	/**
	 * Get a new reference variable for the symbolic execution.
	 *
	 * @param method The Method the variable is a parameter of.
	 * @param localVariable The index into the local variables (required for the correct naming of
	 *        the variable generated).
	 * @return null.
	 */
	@Override
	public Variable getNewVariable(Method method, int localVariable, VirtualMachine vm) {
//		ReferenceVariable refVar = 
//				
//				
//		String name = generateVariableNameByNumber(method, localVariable);
//		String type = method.getParameterTypeAtIndex(localVariable);
//		if(type.equals("java.lang.String")) {
//			return new StringVariable(name);
//		}

		try {
			String name = generateVariableNameByNumber(method, localVariable);
			String type = method.getParameterTypeAtIndex(localVariable);
			ClassFile classFile = vm.getClassLoader().getClassAsClassFile(type);
			if(classFile.isAccInterface()) {
				if(classFile.getName().equals("java.io.Serializable")) {
					try {
						classFile = vm.getClassLoader().getClassAsClassFile("java.lang.Object");
					} catch (ClassFileException e) {
						e.printStackTrace();
					}
				} else if(classFile.getName().equals("java.util.List")) {
					try {
						ReferenceArrayListVariable list = new ReferenceArrayListVariable(name, (JPAVirtualMachine) vm); 
						list.setCollectionType(Object.class.getName());
						return list;
					} catch (SymbolicObjectGenerationException e) {
						throw new RuntimeException("error while generating list variable");
					}
				} else {
					try {
						classFile = vm.getClassLoader().getClassAsClassFile(type+"Impl");
					} catch (ClassFileException e) {
						e.printStackTrace();
					}
				}
			}
			ReferenceValue referenceValue = vm.getAnObjectref(classFile);
			ReferenceVariable refVar = new ReferenceVariable(name, referenceValue, (JPAVirtualMachine)vm);
			
			/*
			SymbolicObjectGenerator symbolicObjectGenerator = new SymbolicObjectGenerator();
			for(Field field : classFile.getFields()) {
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
			*/
			
			return refVar;
		} catch (ClassFileException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a String array representation of the desired types of this instruction. This method is used to
	 * check whather the fetched Object is an instance of one of the desired types.
	 * @return A String representation of the desired type.
	 */
	@Override
	public String[] getDesiredTypes() {
		String[] desiredTypes = {"de.wwu.muggl.vm.initialization.ReferenceValue"};
		return desiredTypes;
	}

	/**
	 * Just return the value since no check is needed here.
	 *
	 * @param value The value.
	 * @return The value.
	 */
	@Override
	public Object validateAndExtendValue(Object value) {
		return value;
	}

	/**
	 * Get the int representation of the type that will be wrapped by a Term.
	 *
	 * @return The int value -1.
	 */
	@Override
	public int[] getDesiredSymbolicalTypes() {
		int[] types = {-1};
		return types;
	}

	/**
	 * Validate the value. Therefore, the assignment compatibility algorithm given in the java virtual machine
	 * specification is used.
	 * @param value The value that is to be stored into an array.
	 * @param arrayref The array reference.
	 * @param frame The currently executed frame.
	 * @return The value.
	 * @throws ExecutionException If the supplied value is not assignment compatible, a VmRuntimeException with an enclosed ArrayStoreException is thrown. Other Exceptions might be thrown due to problems while loading classes.
	 * @throws VmRuntimeException If a runtime exception happened and should be handled by the exception handler.
	 */
	@Override
	@SuppressWarnings("unused")
	public Object validateAndTruncateValue(Object value, Object arrayref, Frame frame) throws ExecutionException, VmRuntimeException {
		// A null value is always assignment compatible.
		if (value == null) return value;

		// Check assignment compatibility.
		ExecutionAlgorithms ea = new ExecutionAlgorithms(frame.getVm().getClassLoader());
		if (!ea.checkForAssignmentCompatibility((ReferenceValue) value, (ReferenceValue) arrayref))
			throw new VmRuntimeException(frame.getVm().generateExc(
					"java.lang.ArrayStoreException",
					value.getClass().getName() + " is not assignment compatible with "
							+ arrayref.getClass().getName()));
		return value;
	}

	/**
	 * Validate a return value. This is similar to validating a reference that is to be stored,
	 * but the type to check assignment compatibility against is fetched from the return descriptor
	 * of the current method.
	 * @param value The value that is to be returned.
	 * @param frame The currently executed frame.
	 * @return The value.
	 * @throws ExecutionException If the supplied value does not match the expected type, an ExecutionException is thrown.
	 * @throws VmRuntimeException If a runtime exception happened and should be handled by the exception handler.
	 */
	@Override
	public Object validateReturnValue(Object value, Frame frame) throws ExecutionException, VmRuntimeException {
		// Get return type.
		String returnType = frame.getMethod().getReturnType();

		ExecutionAlgorithms ea = new ExecutionAlgorithms(frame.getVm().getClassLoader());
		if (!ea.checkForAssignmentCompatibility(value, returnType, frame.getVm(), false))
			throw new VmRuntimeException(frame.getVm()
					.generateExc(
							"java.lang.ArrayStoreException",
							value.getClass().getName() + " is not assignment compatible with "
									+ returnType));
		return value;
	}

}
