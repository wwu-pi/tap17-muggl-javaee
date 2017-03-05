package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.ModifieableArrayref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>newarray</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-29
 */
public class Newarray extends de.wwu.muggl.instructions.general.ObjectInitialization implements
		Instruction, StackPop {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Newarray(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			int count = (Integer) stack.pop();
			// Runtime Exception: count is less than zero.
			if (count < 0)
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NegativeArraySizeException",
						"Cannot create an array of negative size."));

			// Generate a suitable Objectref.
			try {
				byte type = (byte) this.otherBytes[0];
				MugglClassLoader classLoader = frame.getVm().getClassLoader();
				ReferenceValue referenceValue;
				if (type == ClassFile.T_BOOLEAN) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Boolean")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else if (type == ClassFile.T_BYTE) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Byte")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else if (type == ClassFile.T_CHAR) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Character")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else if (type == ClassFile.T_DOUBLE) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Double")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else if (type == ClassFile.T_FLOAT) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Float")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else if (type == ClassFile.T_INT) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Integer")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else if (type == ClassFile.T_LONG) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Long")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else if (type == ClassFile.T_SHORT) {
					referenceValue = classLoader.getClassAsClassFile("java.lang.Short")
							.getAPrimitiveWrapperObjectref(frame.getVm());
				} else {
					// Unexpected exception: The type can not be processed by this method.
					throw new ExecutionException("atype suplied for newarray is unknown.");
				}

				// Generate and push the array.
				stack.push(new Arrayref(referenceValue, count));
			} catch (ClassFileException e) {
				// It is almost impossible that this happens.
				throw new ExecutionException(
						"Instruction newarray failed with an unexpected ClassFileException. "
								+ "Since the class files that may be requested are known, this exception "
								+ "indicates a serious problem. Probably the libraries for java.lang.* "
								+ "are missing or broken. The root cause is: " + e.getMessage());
			} catch (ExceptionInInitializerError e) {
				throw new ExecutionException(
						"Instruction newarray failed with an unexpected ExceptionInInitializerError. "
								+ "The root cause is: " + e.getMessage());
			}
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
			// Determine the type.
			byte type = (byte) this.otherBytes[0];
			String representedType;
			if (type == ClassFile.T_BOOLEAN) {
				representedType = "java.lang.Boolean";
			} else if (type == ClassFile.T_BYTE) {
				representedType = "java.lang.Byte";
			} else if (type == ClassFile.T_CHAR) {
				representedType = "java.lang.Character";
			} else if (type == ClassFile.T_DOUBLE) {
				representedType = "java.lang.Double";
			} else if (type == ClassFile.T_FLOAT) {
				representedType = "java.lang.Float";
			} else if (type == ClassFile.T_INT) {
				representedType = "java.lang.Integer";
			} else if (type == ClassFile.T_LONG) {
				representedType = "java.lang.Long";
			} else if (type == ClassFile.T_SHORT) {
				representedType = "java.lang.Short";
			} else {
				// Unexpected exception: The type can not be processed by this method.
				throw new ExecutionException("atype suplied for newarray is unknown.");
			}
			
			// Instantiate the array.
			Term term = (Term) frame.getOperandStack().pop();
			if (term.isConstant()) {
				int count = ((IntConstant) term).getIntValue();
				// Runtime Exception: count is less than zero.
				if (count < 0)
					throw new VmRuntimeException(frame.getVm().generateExc(
							"java.lang.NegativeArraySizeException",
							"Cannot create an array of negative size."));

				// Generate a suitable Objectref.
				try {
					ReferenceValue referenceValue = frame.getVm().getAnObjectref(frame.getVm().getClassLoader().getClassAsClassFile(
									"de.wwu.muggl.solvers.expressions.Term"));

					// Generate and push the array.
					ModifieableArrayref arrayref = new ModifieableArrayref(referenceValue, count);
					arrayref.disableTypeChecking();
					arrayref.setRepresentedTypeAsAPrimitiveWrapper(representedType);
					frame.getOperandStack().push(arrayref);
				} catch (ClassFileException e) {
					// It is almost impossible that this happens.
					throw new ExecutionException("Instruction newarray failed with an unexpected ClassFileException. Since the class files that may be requested are known, this exception indicates a serious problem. Probably the libraries for java.lang.* are missing or broken. The root cause is: " + e.getMessage());
				} catch (ExceptionInInitializerError e) {
					throw new ExecutionException("Instruction newarray failed with an unexpected ExceptionInInitializerError. The root cause is: " + e.getMessage());
				}
			} else {
				// Create a choice point and push arrays of various length as it is done when loading arrays.
				((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, null, representedType);
			}
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (ExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 1;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "newarray";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = {"java.lang.NegativeArraySizeException"};
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
		byte[] types = {ClassFile.T_INT};
		return types;
	}

}
