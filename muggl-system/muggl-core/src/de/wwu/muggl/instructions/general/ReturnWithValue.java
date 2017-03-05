package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.interfaces.data.OtherFrameStackPush;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Abstract instruction with some concrete methods for returning from a method and handing a
 * return value to the invoker. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-04-01
 */
public abstract class ReturnWithValue extends ReturnWithoutOrWithoutValue implements StackPop, OtherFrameStackPush {
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
			// Pop the value.
			Object value = frame.getOperandStack().pop();

			// Unexpected exception: The value is not of one of the required types.
			value = this.typedInstruction.validateReturnValue(value, frame);

			// Synchronized?
			ifSynchronizedExitMonitor(frame);

			frame.returnFromMethod(value);
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
	 * Execute the inheriting instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		frame.returnFromMethod(frame.getOperandStack().pop());
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
