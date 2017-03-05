package de.wwu.muggl.instructions.general;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.VariableUsing;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.ArrayRestore;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with some concrete methods for storing elements into arrays. Concrete
 * instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-26
 */
public abstract class Astore extends GeneralInstruction implements JumpException, StackPop,
		VariableUsing {
	/**
	 * The type the inheriting class will take.
	 */
	protected TypedInstruction typedInstruction;

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
			Object arrayrefObject  = stack.pop();

			// Runtime exception: arrayref is null
			if (arrayrefObject == null)
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

			// Unexpected exception: arrayref does not point to an array.
			if (!(arrayrefObject instanceof Arrayref)) {
				throw new ExecutionException("Could not  " + getName() + "  array entry #" + index + ": Expected an array, but did not get one.");
			}
			Arrayref arrayref = (Arrayref) arrayrefObject;

			// Unexpected exception: The value is not of one of the required types.
			value = this.typedInstruction.validateAndTruncateValue(value, arrayref, frame);

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
			Term value = (Term) stack.pop();
			int index = ((IntConstant) stack.pop()).getValue();
			Object arrayrefObject  = stack.pop();

			// Runtime exception: arrayref is null
			if (arrayrefObject == null) {
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));
			}

			// Unexpected exception: arrayref does not point to an array.
			if (!(arrayrefObject instanceof Arrayref)) {
				throw new SymbolicExecutionException("Could not  " + getName() + "  array entry #" + index + ": Expected an array, but did not get one.");
			}
			Arrayref arrayref = (Arrayref) arrayrefObject;

			// Save the current value, if necessary.
			if (((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().savingArrayValues()) {
				Object oldValue = arrayref.getElement(index);
				ArrayRestore arrayValue = new ArrayRestore(arrayref, index, oldValue);
				((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().saveArrayValue(arrayValue);
			}

			// Unexpected exception: The value is not of one of the required types.
			value = this.typedInstruction.validateAndTruncateSymbolicValue(value, arrayref, frame);

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
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "astore";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.ArrayIndexOutOfBoundsException",
									"java.lang.ArrayStoreException",
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

}
