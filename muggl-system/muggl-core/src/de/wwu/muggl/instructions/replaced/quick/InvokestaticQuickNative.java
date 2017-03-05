package de.wwu.muggl.instructions.replaced.quick;

import java.util.Stack;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.bytecode.Invokestatic;
import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.instructions.replaced.replacer.InvokestaticReplacer;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ForwardingUnsuccessfulException;
import de.wwu.muggl.vm.execution.NativeWrapper;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Objectref;

/**
 * {@link QuickInstruction} for <i>invokestatic</i>. It used prepared information to quickly invoke
 * a native method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class InvokestaticQuickNative extends InvokestaticQuickAbstract {
	// Execution related fields.
	private ClassFile methodClassFile;

	/**
	 * Construct an instance to replace {@link Invokestatic} if the invoked method is native.
	 *
	 * @param replacer The {@link ReplacingInstruction} that constructs this.
	 * @param otherBytes The additional bytes of the invokestatic instruction replaced by this.
	 * @param method The Method to invoke.
	 * @param accSynchronized Flag whether the method is synchronized, or not.
	 * @param parameterCount The number of parameters passed to the method.
	 * @param methodClassFile The class file of the method.
	 */
	public InvokestaticQuickNative(InvokestaticReplacer replacer, short[] otherBytes, Method method,
			boolean accSynchronized, int parameterCount, ClassFile methodClassFile) {
		super(replacer, otherBytes, method, accSynchronized, parameterCount);
		this.methodClassFile = methodClassFile;
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
			invokeStatic(frame, false);
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
	 *
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		try {
			invokeStatic(frame, true);
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
	 * Invoke a static method. This method encapsulates the whole invocation functionality.
	 *
	 * @param frame The currently executed frame.
	 * @param symbolic Toggles whether the execution is symbolic, or not.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 * @throws VmRuntimeException If runtime exceptions occur.
	 */
	private void invokeStatic(Frame frame, boolean symbolic) throws ExecutionException, VmRuntimeException {
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
			frame.getVm().getMonitorForStaticInvocation(this.methodClassFile).monitorEnter();
		}

		// Perform the native invocation.
		Method method = super.method;
		try {
			// Forward native methods?
			if (Options.getInst().forwardJavaPackageNativeInvoc) {
				// Try to forward.
				if (method.getClassFile().getPackageName().startsWith("java.")) {
					NativeWrapper.forwardNativeInvocation(frame, method, this.methodClassFile, null, parameters);
				} else if (method.getClassFile().getPackageName().equals("de.wwu.muggl.vm.execution.nativeWrapping")) {
					// Get the object reference of the invoking method.
					Objectref invokingObjectref = null;
					Method invokingMethod = frame.getMethod();
					if (!invokingMethod.isAccStatic()) {
						invokingObjectref = (Objectref) frame.getLocalVariables()[0];
					}

					// Invoke the wrapper.
					NativeWrapper.forwardToACustomWrapper(super.method, this.methodClassFile, parameters, invokingObjectref);
				} else {
					throw new ForwardingUnsuccessfulException("No wrapping handler for the native method was found.");
				}
				if (Globals.getInst().execLogger.isDebugEnabled())
					Globals.getInst().execLogger.debug(
							"Forwarded the native method " + method.getName() + " to a wrapper.");

				// Release the monitor if it is synchronized.
				if (super.accSynchronized) {
					frame.getVm().getMonitorForStaticInvocation(this.methodClassFile).monitorExit();
				}

				// Finished.
				return;
			}
		} catch (ForwardingUnsuccessfulException e) {
			// Ignore it, but log it.
			if (Globals.getInst().execLogger.isDebugEnabled())
				Globals.getInst().execLogger.debug(
						"Forwarding of the native method " + method.getName()
						+ " was not successfull. The reason is: " + e.getMessage());
		}
		/*
		 * Either push a zero / null value for the native method's return type, or
		 * completely ignore it.
		 */
		if (Options.getInst().assumeNativeReturnValuesToBeZeroNull) {
			Invoke.pushZeroOrNull(stack, method, symbolic);
			if (Globals.getInst().execLogger.isDebugEnabled())
				Globals.getInst().execLogger.debug(
						"Assume a null/zero value for the native method " + method.getName() + ".");
		} else {
			if (Globals.getInst().execLogger.isInfoEnabled())
				Globals.getInst().execLogger.info(
						"Skipping the native method " + method.getName() + ".");
		}

		// Release the monitor if it is synchronized.
		if (super.accSynchronized) {
			frame.getVm().getMonitorForStaticInvocation(this.methodClassFile).monitorExit();
		}

		// Finished.
		return;
	}

}
