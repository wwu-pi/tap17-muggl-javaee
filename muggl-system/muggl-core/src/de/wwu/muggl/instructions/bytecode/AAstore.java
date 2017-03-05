package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.general.Astore;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.typed.ReferenceInstruction;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.ArrayRestore;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * Implementation of the instruction  <code>aastore</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class AAstore extends Astore implements Instruction {

	/**
	 * Execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			// Preparations.
			Stack<Object> stack = frame.getOperandStack();
			Object value = stack.pop();
			int index = (Integer) stack.pop();

			// Runtime exception: arrayref is null
			if (stack.peek() == null) {
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));
			}

			// Unexpected exception: arrayref does not point to an array.
			if (!((Arrayref) stack.peek()).isArray()) {
				throw new ExecutionException("Could not  " + getName() + ": Expected an array, but did not get one.");
			}
			Arrayref arrayref  = (Arrayref) stack.pop();

			// Set the value into the array and save it.
			try {
				arrayref.putElement(index, value);
			} catch (ArrayIndexOutOfBoundsException e) {
				// Runtime exception array index out of bounds.
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ArrayIndexOutOfBoundsException", e.getMessage()));
			} catch (ArrayStoreException e) {
				// Runtime exception: Array store exception.
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ArrayStoreException", e.getMessage()));
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
			// Preparations.
			Stack<Object> stack = frame.getOperandStack();
			Object value = stack.pop();
			int index = ((IntConstant) stack.pop()).getValue();

			// Runtime exception: arrayref is null
			if (stack.peek() == null) {
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));
			}

			// Unexpected exception: arrayref does not point to an array.
			if (!((Arrayref) stack.peek()).isArray()) {
				throw new SymbolicExecutionException("Could not  " + getName() + ": Expected an array, but did not get one.");
			}
			Arrayref arrayref  = (Arrayref) stack.pop();

			// Save the current value, if necessary.
			if (((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().savingArrayValues()) {
				ArrayRestore arrayValue = new ArrayRestore(arrayref, index, value);
				((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().saveArrayValue(arrayValue);
			}

			// Set the value into the array and save it.
			try {
				arrayref.putElement(index, value);
			} catch (ArrayIndexOutOfBoundsException e) {
				// Runtime exception array index out of bounds.
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ArrayIndexOutOfBoundsException", e.getMessage()));
			} catch (ArrayStoreException e) {
				// Runtime exception: Array store exception.
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ArrayStoreException", e.getMessage()));
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
	 * Constructor to initialize the TypedInstruction.
	 */
	public AAstore() {
		 this.typedInstruction = new ReferenceInstruction();
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "a" + super.getName();
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
		byte[] types = {0, ClassFile.T_INT, 0};
		return types;
	}

}
