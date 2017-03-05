package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import javax.persistence.criteria.CriteriaBuilder;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Put;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.jpa.criteria.MugglCriteriaBuilder;
import de.wwu.muggl.symbolic.jpa.criteria.CriteriaBuilderWrapper;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.InstanceFieldPut;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionAlgorithms;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.ModifieableArrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>putfield</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Putfield extends Put implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Putfield(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			// Preparations.
			Stack<Object> stack = frame.getOperandStack();
			Object value = stack.pop();
			Objectref objectref = (Objectref) stack.pop();
			Field field = fetchField(frame, objectref);

			// Check for assignment compatibility.
			String type = field.getType();
			ExecutionAlgorithms ea = new ExecutionAlgorithms(frame.getVm().getClassLoader());
			if (!ea.checkForAssignmentCompatibility(value, type, frame.getVm(), false)) {
				// Unexpected exception: value is not assignment compatible to the expected type.
				throw new ExecutionException("Cannot write a value that is not assignment compatible to " + type + ".");
			}

			// Finally assign the value.
			objectref.putField(field, value);
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
			// Preparations.
			Stack<Object> stack = frame.getOperandStack();
			Object value = stack.pop();
			Objectref objectref = (Objectref) stack.pop();
			Field field = fetchField(frame, objectref);

			// Check for assignment compatibility.
			String type = field.getType();
			ExecutionAlgorithms ea = new ExecutionAlgorithms(frame.getVm().getClassLoader());
			if (value instanceof Term) {
				// Value is a term. Get its type and do the type checking with it.
				String valueAsString = TypedInstruction.getStringRepresentationForWrappedType(((Term) value).getType());
				if (valueAsString == null || !ea.checkForAssignmentCompatibility(valueAsString, type, frame.getVm(), true))
				{
					// Unexpected exception: value is not assignment compatible to the expected type.
					throw new ExecutionException("Cannot write a value that is not assignment compatible to " + type + ".");
				}
			} else if (value instanceof Arrayref && ((Arrayref) value).getReferenceValue().getName().startsWith("de.wwu.muggl.solvers.expressions.Term")) {
				// Value is an array of term objects. Does it have a represented type?
				if (value instanceof ModifieableArrayref && ((ModifieableArrayref) value).getRepresentedType() != null) {
					// Get its representated type and do the type checking with it.
					if (!ea.checkForAssignmentCompatibility(((ModifieableArrayref) value).getRepresentedType(), type, frame.getVm(), true))
					{
						// Unexpected exception: value is not assignment compatible to the expected type.
						throw new ExecutionException("Cannot write a value that is not assignment compatible to " + type + ".");
					}
				} else {
					// Get its type and do the type checking with it.
					if (!ea.checkForAssignmentCompatibility(((Arrayref) value).getReferenceValue(), type, frame.getVm(), true))
					{
						// Unexpected exception: value is not assignment compatible to the expected type.
						throw new ExecutionException("Cannot write a value that is not assignment compatible to " + type + ".");
					}
				}
			} else if(value instanceof ReferenceVariable 
					&& ((ReferenceVariable)value).getObjectType().equals("java.lang.Object")
					&& field.getType().equals("java.io.Serializable")) {
				// alles ok...
			} else if(value instanceof MugglCriteriaBuilder) {
				// alles ok...
			} else if(value instanceof CriteriaBuilderWrapper) {
				// alles ok...
			} else {
				// Value is a reference type - use the normal assignment compatibility check.
				if (!ea.checkForAssignmentCompatibility(value, type, frame.getVm(), false))
				{
					// Unexpected exception: value is not assignment compatible to the expected type.
					throw new ExecutionException("Cannot write a value that is not assignment compatible to " + type + ".");
				}
			}

			// Save the current value, if necessary.
			if (((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().savingFieldValues()) {
				InstanceFieldPut fieldValue = new InstanceFieldPut(objectref, field, objectref.getField(field));
				((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().saveFieldValue(fieldValue);
			}

			// Finally assign the value.
			objectref.putField(field, value);
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
	 * Fetch the Field and check if it meet the requirements of the virtual machine.
	 * @param frame The currently executed Frame.
	 * @param objectref The objectref of the field.
	 * @return The fetched field.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 * @throws VmRuntimeException If the Field could not be found, wrapping a NoSuchFieldError.
	 */
	private Field fetchField(
			Frame frame,
			Objectref objectref
			) throws ExecutionException, VmRuntimeException {
		// Preparations.
		ClassFile methodClassFile = frame.getMethod().getClassFile();
		Field field = getField(frame, methodClassFile, methodClassFile.getClassLoader());

		// Runtime exception: is the field static?
		if (field.isAccStatic()) throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IncompatibleClassChangeError", "The field accessed by instruction " + getName() + " must not be static."));

		// Runtime exception: objectref is null.
		if (objectref == null) throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

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
						throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
					}

					if (field.getClassFile().getName().equals(objectrefClassFile.getName())) {
						// Found the match!
						classMatchFound = true;
						break;
					}
				}
				if (!classMatchFound) throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalAccessError", objectref.getInitializedClass().getClassFile().getName() + " may not access field " + field.getName() + " in class " + frame.getMethod().getClassFile().getName() + "."));
			}
		}

		// Return the field.
		return field;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "putfield";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.IllegalAccessError",
									"java.lang.IncompatibleClassChangeError",
									"java.lang.NoClassDefFoundError",
									"java.lang.NoSuchFieldError",
									"java.lang.NullPointerException"};
		return exceptionTypes;
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be popped from the stack.
	 */
	public int getNumberOfPoppedElements() {
		return 2;
	}

	/**
	 * Get the types of elements this instruction will pop from the stack.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The types this instruction pops. The length of the arrays reflects the number of
	 *         elements pushed in the order they are pushed. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the popped type cannot be determined statically.
	 */
	public byte[] getTypesPopped(ClassFile methodClassFile) {
		byte[] types = {getFieldType(methodClassFile), 0};
		return types;
	}

}
