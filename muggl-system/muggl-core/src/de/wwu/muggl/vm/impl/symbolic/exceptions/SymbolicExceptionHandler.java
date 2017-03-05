package de.wwu.muggl.vm.impl.symbolic.exceptions;

import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicFrame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;

/**
 * The SymbolicExceptionHandler inherits the ExceptionHandler and does not touch its functionality.
 * It however overrides some methods in order to keep track of the control flow, should it be
 * tracked by the symbolic virtual machine. Any overridden method will invoke its super
 * implementation once it finished its job.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-08
 */
public class SymbolicExceptionHandler extends ExceptionHandler {

	/**
	 * Inherit the super constructor.
	 * @param frame The frame the exception was thrown by.
	 * @param vmRuntimeException The wrapper for the thrown exception.
	 */
	public SymbolicExceptionHandler(Frame frame, VmRuntimeException vmRuntimeException) {
		super(frame, vmRuntimeException);
	}

	/**
	 * Inherit the super constructor.
	 *
	 * @param frame The frame the exception was thrown by.
	 * @param objectref The thrown object reference.
	 */
	public SymbolicExceptionHandler(Frame frame, Objectref objectref) {
		super(frame, objectref);
	}

	/**
	 * Update the control flow coverage and inherit the super implementation.<br />
	 * <br />
	 * The instruction executed last in the currently handler frame has thrown the exception and
	 * hence gets an edge representing that the method will be left at that point.
	 *
	 * @throws ExecutionException If no handler could be found ultimately and a Throwable has to be
	 *         instantiated but that process fails.
	 * @throws NoExceptionHandlerFoundException If no handler could be found ultimately.
	 */
	@Override
	protected void noHandlerFound() throws ExecutionException, NoExceptionHandlerFoundException {
		// Update coverage.
		Method method = this.frame.getMethod();
		/*
		 * Get the symbolic virtual machine. Frame does not need to be casted to SymbolicFrame since
		 * the stored vm can only be the symbolic one.
		 */
		SymbolicVirtualMachine svm = (SymbolicVirtualMachine) this.frame.getVm();
		svm.getCoverageController().reportFailedHandling(method);

		// Invoke the super implementation to continue exception handling.
		super.noHandlerFound();
	}

	/**
	 * Take care of a frame restored while handling an exception by reverting the last pc of the
	 * control flow tracking. This is necessary as the frame might be a frame of the same method
	 * that originally threw the exception being handled (recursive algorithm). In that case, the
	 * last pc of the control flow tracking would be some pc within the method, but not necessarily
	 * the pc of the instruction invoking another method. It hence needs to be reverted so the
	 * correct edge can be drawn.
	 */
	@Override
	protected void frameHasBeenRestored() {
		((SymbolicFrame) this.frame).revertLastPCForCFCovering();
	}

}

