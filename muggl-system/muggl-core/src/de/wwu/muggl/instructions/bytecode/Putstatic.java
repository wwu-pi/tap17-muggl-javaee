package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Put;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.log.MugglLogger;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.StaticFieldPut;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
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
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.ModifieableArrayref;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>putstatic</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Putstatic extends Put implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Putstatic(AttributeCode code) throws InvalidInstructionInitialisationException {
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
			Field field = fetchField(frame);

			// Check for assignment compatibility.
			Object value = frame.getOperandStack().pop();
			String type = field.getType();
			ExecutionAlgorithms ea = new ExecutionAlgorithms(frame.getVm().getClassLoader());
			if (!ea.checkForAssignmentCompatibility(value, type, frame.getVm(), false)) {
				// Unexpected exception: value is not assignment compatible to the expected type.
				throw new ExecutionException("Cannot write a value that is not assignment compatible to " + type + ".");
			}

			// If necessary, initialize the class.
			InitializedClass initializedClass = field.getClassFile().getTheInitializedClass(frame.getVm());

			// Finally assign the value.
			initializedClass.putField(field, value);
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
			Field field = fetchField(frame);

			// Check for assignment compatibility.
			Object value = frame.getOperandStack().pop();
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
			} else {
				if(value instanceof MugglLogger) {
					// do nothing
				}
				// Value is a reference type - use the normal assignment compatibility check.
				else if (!ea.checkForAssignmentCompatibility(value, type, frame.getVm(), false))
				{
					// Unexpected exception: value is not assignment compatible to the expected type.
					throw new ExecutionException("Cannot write a value that is not assignment compatible to " + type + ".");
				}
			}

			// If necessary, initialize the class.
			InitializedClass initializedClass = field.getClassFile().getTheInitializedClass(frame.getVm());

			// Save the current value, if necessary.
			if (((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().savingFieldValues()) {
				StaticFieldPut fieldValue = new StaticFieldPut(initializedClass, field, initializedClass.getField(field));
				((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().saveFieldValue(fieldValue);
			}

			// Finally assign the value.
			initializedClass.putField(field, value);
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
	 * @return The fetched field.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 * @throws VmRuntimeException Wrapping any RuntimeExceptions thrown.
	 */
	private Field fetchField(Frame frame) throws ExecutionException, VmRuntimeException {
		// Preparations.
		ClassFile methodClassFile = frame.getMethod().getClassFile();
		Field field = getField(frame, methodClassFile, methodClassFile.getClassLoader());

		// Runtime exception: is the field not static?
		if (!field.isAccStatic())
			throw new VmRuntimeException(frame.getVm().generateExc(
					"java.lang.IncompatibleClassChangeError",
					"The field accessed by instruction " + getName() + " must be static."));

		// Runtime exception: is the field final and not declared in the current class?
		if (field.isAccFinal()) {
			// Only the clinit method of the same class may write it!
			if (!field.getClassFile().getName().equals(frame.getMethod().getClassFile().getName())
				|| !frame.getMethod().getName().equals("<clinit>"))
					throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IllegalAccessError", "Cannot assign values to a final field."));
		}

		return field;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "putstatic";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.ExceptionInInitializerError",
									"java.lang.IllegalAccessError",
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
		return 1;
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
		byte[] types = {getFieldType(methodClassFile)};
		return types;
	}
}
