package de.wwu.muggl.instructions.bytecode;

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
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction  <code>anewarray</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class Anewarray extends de.wwu.muggl.instructions.general.ObjectInitialization implements
		Instruction, StackPop {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be
	 *         initialized successfully, most likely due to missing additional bytes. This might be
	 *         caused by a corrupt classfile, or a classfile of a more recent version than what can
	 *         be handled.
	 */
	public Anewarray(AttributeCode code) throws InvalidInstructionInitialisationException {
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
			int count = (Integer) frame.getOperandStack().pop();
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
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic
	 *         execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		try {
			Term term = (Term) frame.getOperandStack().pop();
			if (term.isConstant()) {
				int count = ((IntConstant) term).getIntValue();
				generateAndPushNewArray(frame, count);
			} else {
				// TODO: Use a choice point for this and push arrays of various length as it is done
				// when loading arrays.
				throw new SymbolicExecutionException("Cannot generate an array of variable length.");
			}
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Resolve the array class, generate a new arrayref and push it.
	 *
	 * @param frame The currently executed frame.
	 * @param count The length of the new array.
	 * @throws VmRuntimeException Thrown on runtime exceptions.
	 */
	private void generateAndPushNewArray(Frame frame, int count) throws VmRuntimeException {
		// Runtime Exception: count is less than zero.
		if (count < 0)
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NegativeArraySizeException",
					"Cannot create an array of negative size."));

		// Resolve the class.
		ResolutionAlgorithms resolution = new ResolutionAlgorithms(frame.getVm().getClassLoader());
		String className = ((ConstantClass) frame.getConstantPool()[this.otherBytes[0] << ONE_BYTE
				| this.otherBytes[1]]).getValue();
		ClassFile c;
		try {
			c = resolution.resolveClassAsClassFile(frame.getMethod().getClassFile(), className);
		} catch (NoClassDefFoundError e) {
			// The class could not be found.
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
		} catch (IllegalAccessError e) {
			/*
			 *  Illegal access to a class that is neither public nor in the same package than the
			 *  class generating the new array.
			 */
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalAccessError", e.getMessage()));
		}

		try {
			// Generate the array.
			Arrayref arrayref = new Arrayref(frame.getVm().getAnObjectref(c), count);

			// Push the new array.
			frame.getOperandStack().push(arrayref);
		} catch (ExceptionInInitializerError e) {
			// Linking exception: ExceptionInInitializerError.
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ExceptionInInitializerError", e.getMessage()));
		}
	}

	/**
	 * Resolve the instructions name.
	 *
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "anewarray";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 *
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.NegativeArraySizeException",
				"java.lang.NoClassDefFoundError" };
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
