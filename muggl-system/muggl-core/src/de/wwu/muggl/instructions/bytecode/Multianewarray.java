package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantClass;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.PrimitiveWrappingImpossibleException;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>multianewarray</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Multianewarray extends de.wwu.muggl.instructions.general.ObjectInitialization
		implements Instruction, StackPop {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Multianewarray(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			// Get the dimensions and their length.
			int dimensions = this.otherBytes[2];
			int[] count = new int[dimensions];
			Stack<Object> stack = frame.getOperandStack();
			for (int a = dimensions - 1; a >= 0; a--) {
				count[a] = (Integer) stack.pop();
				// Runtime Exception: count is less than zero.
				if (count[a] < 0)
					throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NegativeArraySizeException",
					"Cannot create an array of negative size."));
			}

			// Create the array.
			generateAndPushNewArray(frame, count);
		} catch (VmRuntimeException e) {
			ExceptionHandler handler = new ExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailed(e2);
			}
		}
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		try {
			// Get the dimensions and their length.
			int dimensions = this.otherBytes[2];
			int[] count = new int[dimensions];
			Stack<Object> stack = frame.getOperandStack();
			for (int a = dimensions - 1; a >= 0; a--) {
				Term term = (Term) stack.pop();
				if (term.isConstant()) {
					count[a] = ((IntConstant) term).getIntValue();
					// Runtime Exception: count is less than zero.
					if (count[a] < 0)
						throw new VmRuntimeException(frame.getVm().generateExc(
								"java.lang.NegativeArraySizeException",
								"Cannot create an array of negative size."));
				} else {
					// TODO: Use a choice point for this and push arrays of various length as it is done when loading arrays.
					throw new SymbolicExecutionException("Cannot generate an array dimension of variable length.");
				}
			}

			// Create the array.
			generateAndPushNewArray(frame, count);
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
	 * Resolve the array class, generate a new arrayref and push it.
	 *
	 * @param frame The currently executed frame.
	 * @param count The length of the new array.
	 * @throws PrimitiveWrappingImpossibleException If the represented ClassFile cannot be used as a
	 *         primitive wrapper.
	 * @throws VmRuntimeException Thrown on runtime exceptions.
	 */
	private void generateAndPushNewArray(Frame frame, int[] count) throws PrimitiveWrappingImpossibleException, VmRuntimeException {
		// Resolve the class.
		String className = ((ConstantClass) frame.getConstantPool()[this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]]).getValue();
		ResolutionAlgorithms resolution = new ResolutionAlgorithms(frame.getVm().getClassLoader());
		ClassFile c;
		try {
			 c = resolution.resolveClassAsClassFile(frame.getMethod().getClassFile(), className);
		} catch (IllegalAccessError e) {
			// Illegal access to a class that is neither public nor in the same package than the class generating the new array.
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalAccessError", e.getMessage()));
		} catch (NoClassDefFoundError e) {
			// The class could not be found.
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
		}

		// Check if the className hints to primitive type.
		boolean lastDimensionAsPrimitiveArray = false;
		className = className.replace("[", "");
		// Drop the "L", if there is one.
		if (className.startsWith("L")) className = className.substring(1);
		// Drop the ";" at the end, if there is one.
		if (className.endsWith(";")) className = className.substring(0, className.length() - 1);
		// Is it a primitive value?
		if (className.length() == 1) lastDimensionAsPrimitiveArray = true;

		try {
			// Generate the reference value array.
			ReferenceValue referenceValue;
			if (lastDimensionAsPrimitiveArray) {
				referenceValue = c.getAPrimitiveWrapperObjectref(frame.getVm());
			} else {
				referenceValue = frame.getVm().getAnObjectref(c);
			}
			Arrayref arrayref = new Arrayref(referenceValue, count);

			// Push the new array.
			frame.getOperandStack().push(arrayref);
		} catch (ExceptionInInitializerError e) {
			// Linking exception: ExceptionInInitializerError.
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ExceptionInInitializerError", e.getMessage()));
		}
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "multianewarray";
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 3;
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.IllegalAccessError",
									"java.lang.NegativeArraySizeException",
									"java.lang.NoClassDefFoundError"};
		return exceptionTypes;
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be popped from the stack.
	 */
	public int getNumberOfPoppedElements() {
		return this.otherBytes[2];
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
		byte[] types = new byte[this.otherBytes[2]];
		for (int a = 0; a < types.length; a++) {
			types[a] = ClassFile.T_INT;
		}
		return types;
	}

}
