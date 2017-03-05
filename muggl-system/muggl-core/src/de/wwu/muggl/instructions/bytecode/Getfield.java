package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Get;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerator;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.PrimitiveWrappingImpossibleException;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

/**
 * Implementation of the instruction <code>getfield</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Getfield extends Get implements Instruction {
	
	protected SymbolicObjectGenerator symbolicObjectGenerator;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Getfield(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
		this.symbolicObjectGenerator = new SymbolicObjectGenerator();
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			// Fetch and push the value.
			frame.getOperandStack().push(getFieldValue(frame));
		} catch (VmRuntimeException e) {
			ExceptionHandler handler = new ExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailed(e2);
			}
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			Objectref objectref = (Objectref) frame.getOperandStack().peek();
			if(objectref.canBeNull()) {
				((JPAVirtualMachine)frame.getVm()).generateNewGetFieldOfNullableObjectChoicePoint(this, objectref);
				throw new VmRuntimeException(frame.getVm()
						.generateExc("java.lang.NullPointerException"));
			}
			
			
			
			// Get the fields' value.
			Object value = getFieldValue(frame);

			// Push it.
			frame.getOperandStack().push(value);
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		} catch (ExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Get the value of an instance field.
	 * @param frame The currently executed frame.
	 * @return The fields' value.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 * @throws VmRuntimeException If the Field could not be found, wrapping a NoSuchFieldError.
	 */
	public Object getFieldValue(Frame frame) throws ExecutionException, VmRuntimeException {
		// Preparations.
		Objectref objectref = (Objectref) frame.getOperandStack().pop();
		ClassFile methodClassFile = frame.getMethod().getClassFile();
		Field field = getField(frame, methodClassFile, methodClassFile.getClassLoader());

		// Runtime exception: is the field static?
		if (field.isAccStatic())
			throw new VmRuntimeException(frame.getVm().generateExc(
					"java.lang.IncompatibleClassChangeError",
					"The field accessed by instruction " + getName() + " must not be static."));

		// Runtime exception: objectref is null.
		if (objectref == null || objectref.isNull())
			throw new VmRuntimeException(frame.getVm()
					.generateExc("java.lang.NullPointerException"));

		// Fetch the class of objectref.
		ClassFile objectrefClassFile = objectref.getInitializedClass().getClassFile();
		Class<?> objectrefClass = objectrefClassFile.getClass();

		// Unexpected exception: objectref is an array.
		if (objectrefClass.isArray()) throw new ExecutionException("Objectref must not be an array.");

		// Is the access allowed?
		if (field.isAccProtected()) {
			// Runtime exception: Due to the kind of resolution the Field has to be a member of the current class or of one of its superclasses. So check objectref.
			if (!field.getClassFile().getName().equals(objectrefClassFile.getName())) {
				// Objectref is not the same class. Is it a subclass then.
				boolean classMatchFound = false;
				while (objectrefClassFile.getSuperClass() != 0) {
					try {
						objectrefClassFile = frame.getVm().getClassLoader().getClassAsClassFile(objectrefClassFile.getConstantPool()[objectrefClassFile.getSuperClass()].getStringValue());
					} catch (ClassFileException e) {
						throw new VmRuntimeException(frame.getVm().generateExc(
								"java.lang.NoClassDefFoundError", e.getMessage()));
					}

					if (field.getClassFile().getName().equals(objectrefClassFile.getName())) {
						// Found the match!
						classMatchFound = true;
						break;
					}
				}
				if (!classMatchFound)
					throw new VmRuntimeException(frame.getVm().generateExc(
							"java.lang.IllegalAccessError",
							objectref.getInitializedClass().getClassFile().getName()
									+ " may not access field " + field.getName() + " in class "
									+ frame.getMethod().getClassFile().getName() + "."));
			}
		}

		// Get the value of the field.
		Object value = objectref.getField(field);
		
		if(value == null && objectref instanceof ReferenceVariable) {
			// if the value is null, but the object reference
			// is a reference variable, we do not know the real value of
			// the field yet, so we add a new 'variable' to it...
			if(field.getType().endsWith("[]")) {
				// it is an array reference
				SymbolicArrayref symArrayRef = null;
				try {
					symArrayRef = symbolicObjectGenerator.getSymbolicArrayReference(frame, objectref.getName(), field.getName(), field.getType());
				} catch (SymbolicObjectGenerationException e) {
					throw new ExecutionException("Could not get symbolic array reference for field: " + field.getName(), e);
				}
				objectref.putField(field, symArrayRef);
				
				// if it is an symbolic array list, add constraint that number of elements of list must be equal to the number in the array
				symArrayRef.getSymbolicLength();
				if(objectref instanceof ReferenceArrayListVariable) {
					NumericVariable arrayListLength = ((ReferenceArrayListVariable)objectref).getSymbolicLength();
					NumericVariable arrayLength = symArrayRef.getSymbolicLength();
					((SymbolicVirtualMachine)frame.getVm()).getSolverManager().addConstraint(NumericEqual.newInstance(arrayListLength, arrayLength));
				}
				
				// return the symbolic array reference
				value = symArrayRef;
				
			} else {
				// it is _not_ an array reference
				ReferenceVariable referenceValue = null;
				try {
					referenceValue = symbolicObjectGenerator.getSymbolicObjectReference(frame, objectref.getName(), field);
				} catch (SymbolicObjectGenerationException e) {
					throw new ExecutionException("Could not get symbolic object reference for field: " + field.getName(), e);
				}
				objectref.putField(field, referenceValue);
				
				// return the symbolic object reference
				value = referenceValue;
				
//				ReferenceVariable parentReference = (ReferenceVariable)objectref;
//				String referenceFieldName = parentReference.getName() + "." + field.getName();
//				try {
//					ClassFile classFile = ((SymbolicVirtualMachine)frame.getVm()).getClassLoader().getClassAsClassFile(field.getType());
//					Objectref fieldReferenceValue = ((SymbolicVirtualMachine)frame.getVm()).getAnObjectref(classFile);
//					ReferenceVariable referenceValue = new ReferenceVariable(referenceFieldName, fieldReferenceValue);
//					
//					for(Field newRefField : classFile.getFields()) {
//						if(!newRefField.isAccStatic()) {
//							String type = newRefField.getType();
//							String name = newRefField.getName();
//							String newRefFieldName = referenceValue.getName() + "." + name;
//							Object newRefFieldValue = null;
//							if(type.equals("int") || type.equals("java.lang.Integer")) {
//								newRefFieldValue = new NumericVariable(newRefFieldName, Expression.INT);
//							} else {
//								System.out.println("dont know what to do with field: type="+type+", and name="+name);
//							}
//							referenceValue.putField(newRefField, newRefFieldValue);
//						}
//					}
//					
//					objectref.putField(field, referenceValue);
//					return referenceValue;
//				} catch (ClassFileException e) {
//					e.printStackTrace();
//					System.err.println("Could not get symoblic reference to field: " + field.getName());
//					return null;
//				}
			}
		}

		// Return the value.
		return value;
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
		MugglClassLoader classLoader = frame.getVm().getClassLoader();
		
		if (type == ClassFile.T_BOOLEAN) {
			return classLoader.getClassAsClassFile("java.lang.Boolean")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else if (type == ClassFile.T_BYTE) {
			return classLoader.getClassAsClassFile("java.lang.Byte")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else if (type == ClassFile.T_CHAR) {
			return classLoader.getClassAsClassFile("java.lang.Character")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else if (type == ClassFile.T_DOUBLE) {
			return classLoader.getClassAsClassFile("java.lang.Double")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else if (type == ClassFile.T_FLOAT) {
			return classLoader.getClassAsClassFile("java.lang.Float")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else if (type == ClassFile.T_INT) {
			return classLoader.getClassAsClassFile("java.lang.Integer")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else if (type == ClassFile.T_LONG) {
			return classLoader.getClassAsClassFile("java.lang.Long")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else if (type == ClassFile.T_SHORT) {
			return classLoader.getClassAsClassFile("java.lang.Short")
					.getAPrimitiveWrapperObjectref(frame.getVm());
		} else {
			ClassFile classFile = frame.getVm().getClassLoader().getClassAsClassFile(typeName);
			return frame.getVm().getAnObjectref(classFile);
		}
	}
	

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "getfield";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.IllegalAccessError",
									"java.lang.NoClassDefFoundError",
									"java.lang.NoSuchFieldError",
									"java.lang.NullPointerException"};
		return exceptionTypes;
	}

}
