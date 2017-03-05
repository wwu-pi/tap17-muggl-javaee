package de.wwu.muggl.instructions.replaced.quick;

import java.util.Stack;

import de.wwu.muggl.instructions.bytecode.Invokestatic;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.instructions.replaced.replacer.InvokestaticReplacer;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * {@link QuickInstruction} for <i>invokestatic</i>. It used prepared information to quickly invoke
 * a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class InvokestaticQuickNormal extends InvokestaticQuickAbstract {

	/**
	 * Construct an instance to replace {@link Invokestatic} if the invoked method is not native.
	 *
	 * @param replacer The {@link ReplacingInstruction} that constructs this.
	 * @param otherBytes The additional bytes of the invokestatic instruction replaced by this.
	 * @param method The Method to invoke.
	 * @param accSynchronized Flag whether the method is synchronized, or not.
	 * @param parameterCount The number of parameters passed to the method.
	 */
	public InvokestaticQuickNormal(InvokestaticReplacer replacer, short[] otherBytes, Method method,
			boolean accSynchronized, int parameterCount) {
		super(replacer, otherBytes, method, accSynchronized, parameterCount);
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
			invokeStatic(frame);
		} catch (ExecutionException e) {
			executionFailed(e);
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
			invokeStatic(frame);
		} catch (ExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 *
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	private void invokeStatic(Frame frame) throws ExecutionException {
		// Prepare the parameter's array.
		Stack<Object> stack = frame.getOperandStack();
		if (stack.size() < super.parameterCount)
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": There are less elements on the stack than parameters needed.");
		// As it is invokestatic the number of parameters is equal to the number of arguments.
		Object[] parameters = new Object[super.parameterCount];

		// Get nargs arguments.
		for (int a = super.parameterCount - 1; a >= 0; a--) {
			parameters[a] = stack.pop();
		}

		// Enter the monitor if the method is synchronized.
		if (super.accSynchronized) {
			frame.getVm().getMonitorForStaticInvocation(this.method.getClassFile());
		}

		// Save current frame...
		frame.setPc(frame.getVm().getPc() + 3); // The instruction takes up three bytes.
		frame.getVm().getStack().push(frame);

		// Push new one.
		frame.getVm().createAndPushFrame(frame, super.method, parameters);

		// Finish.
		frame.getVm().setReturnFromCurrentExecution(true);
	}

}
