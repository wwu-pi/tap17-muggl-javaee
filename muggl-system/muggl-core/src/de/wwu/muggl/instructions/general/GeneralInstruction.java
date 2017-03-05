package de.wwu.muggl.instructions.general;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.threading.Monitor;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * The class represents an abstract instruction with some concrete methods. It is the super class for
 * any instruction implemented in the application. Abstract instructions can be extended from this
 * class; it is however not meant to be implemented by concrete instructions directly.<br />
 * <br />
 * Beside the specification of abstract methods it offers concrete methods for the handling of
 * errors. These methods are not intended to be overridden. The class also has Constants for the
 * description of the jumping possibilities an instruction offers.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public abstract class GeneralInstruction {
	// Constants for the jumping possibilities.
	/**
	 * Flag that indicates an instruction that will never lead to a jump. As the consequence, the next
	 * instruction executed is always the instruction at the pc current_pc + 1 + this_instructions_other_bytes.
	 */
	public static final int JUMP_NEVER = 1;
	/**
	 * Flag that indicates an instruction that always leads to a jump to another instruction.
	 */
	public static final int JUMP_ALWAYS = 2;
	/**
	 * Flag that indicates an instruction that is a conditional jump.
	 */
	public static final int JUMP_CONDITIONAL = 4;
	/**
	 * Flag that indicates an instruction that might throw an exception.
	 */
	public static final int JUMP_EXCEPTION = 8;
	/**
	 * Flag that indicates an instruction that is a swit6ching statement.
	 */
	public static final int JUMP_SWITCHING = 16;
	/**
	 * Flag that indicates an instruction that will invoke a method.
	 */
	public static final int JUMP_INVOCATION = 32;

	/**
	 * One byte, i.e. 8 bits. This constant is to be used for shifting.
	 */
	protected static final int ONE_BYTE = 8;
	/**
	 * Two bytes, i.e. 16 bits. This constant is to be used for shifting.
	 */
	protected static final int TWO_BYTES = 16;
	/**
	 * Three bytes, i.e. 24 bits. This constant is to be used for shifting.
	 */
	protected static final int THREE_BYTES = 24;
	
	/**
	 * Abstract method for the execution.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	protected abstract void execute(Frame frame) throws ExecutionException;

	/**
	 * Abstract method for the symbolic execution.
	 *
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	protected abstract void executeSymbolically(Frame frame) throws ExecutionException;

	/**
	 * Process any exception thrown during the execution which has not been handled by the exception
	 * handler. It will be logged on the execution Logger with the level WARN and an ExecutionException
	 * will be thrown, which can be processed by the virtual machine (most likely resulting in halting
	 * the virtual machine). This Exception will contain the root exceptions' type and message.
	 * @param e The Exception to process.
	 * @throws ExecutionException Thrown in any case where no NoExceptionHandlerFoundException is thrown, containing information about the root exception.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 */
	protected final void executionFailed(Exception e) throws ExecutionException,
			NoExceptionHandlerFoundException {
		// NoExceptionHandlerFoundException just have to be re-thrown.
	if (e instanceof NoExceptionHandlerFoundException) {
			throw (NoExceptionHandlerFoundException) e;
		}

		// Proceed normally.
		if (Globals.getInst().execLogger.isEnabledFor(Level.WARN)) Globals.getInst().execLogger.warn("Execution of " + getName() + " failed. Reason: " + e.getMessage());
		if (e.getMessage().contains("Root Reason is:"))
			throw new ExecutionException(e.getMessage());
		throw new ExecutionException("Execution of " + getName() + " failed. Root Reason is a " + e.getClass().getName() + " with message: " + e.getMessage());
	}

	/**
	 * Process any exception thrown during the symbolic execution which has not been handled by the
	 * exception handler. It will be logged on the symbolic execution Logger with the level WARN and
	 * an SymbolicExecutionException will be thrown, which can be processed by the symbolic virtual
	 * machine (most likely resulting in halting the virtual machine). This Exception will contain
	 * the root exceptions' type and message.
	 *
	 * @param e The Exception to process.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in any case where no
	 *         NoExceptionHandlerFoundException is thrown, containing information about the root
	 *         exception.
	 */
	protected final void executionFailedSymbolically(Exception e) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		// NoExceptionHandlerFoundException just have to be re-thrown.
		if (e instanceof NoExceptionHandlerFoundException) {
			throw (NoExceptionHandlerFoundException) e;
		}

		// Proceed normally.
		if (Globals.getInst().execLogger.isEnabledFor(Level.WARN)) Globals.getInst().execLogger.warn("Symbolic execution of " + getName() + " failed. Reason: " + e.getMessage());
		if (e.getMessage().contains("Root Reason is:"))
			throw new SymbolicExecutionException(e.getMessage());
		throw new SymbolicExecutionException("Symbolic execution of " + getName() + " failed. Root Reason is: " + e.getMessage());
	}

	/**
	 * Process normal execution exceptions thrown during the symbolic execution and are not
	 * handled by the exception handler. The behaviour of this method is equal to
	 * executionFailedSymbolically(Exception e), it just does not rethrown exceptions of type
	 * NoExceptionHandlerFoundException. It is meant to be used when the symbolic execution is equal
	 * to the normal one and just in case of failure the exception type ultimatively thrown should
	 * be SymbolicExecutionException.
	 *
	 * @param e The Exception to process.
	 * @throws SymbolicExecutionException
	 *         Thrown in any case where no NoExceptionHandlerFoundException is thrown, containing
	 *         information about the root exception.
	 */
	protected final void symbolicExecutionFailedWithAnExecutionException(ExecutionException e) throws SymbolicExecutionException {
		// Proceed normally.
		if (Globals.getInst().execLogger.isEnabledFor(Level.WARN)) Globals.getInst().execLogger.warn("Symbolic execution of " + getName() + " failed. Reason: " + e.getMessage());
		if (e.getMessage().contains("Root Reason is:"))
			throw new SymbolicExecutionException(e.getMessage());
		throw new SymbolicExecutionException("Symbolic execution of " + getName() + " failed. Root Reason is: " + e.getMessage());
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	protected abstract String getName();

	/**
	 * Resolve the instructions name including the additional bytes (if there are any).
	 * @return The instructions name inlcuding the additional bytes as a String.
	 */
	public String getNameWithOtherBytes() {
		return getName();
	}

	/**
	 * Check the category of an object, returning true if it is a Double or a Long instance.
	 *
	 * @param object The object to check.
	 * @return true if it is a Double or a Long instance; false otherwise.
	 */
	protected boolean checkCategory2(Object object) {
		return object instanceof Double || object instanceof Long;
	}

	/**
	 * Check the category of an object, returning true if it is a Double or a Long instance.
	 *
	 * @param type The types of the elements to check. Non-primitive types should be represented as
	 *        -1. Types are {@link ClassFile#T_BOOLEAN}, {@link ClassFile#T_CHAR},
	 *        {@link ClassFile#T_DOUBLE}, {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT},
	 *        {@link ClassFile#T_LONG} and {@link ClassFile#T_SHORT}.
	 * @return true If type is {@link ClassFile#T_DOUBLE} or {@link ClassFile#T_LONG}; false
	 *         otherwise.
	 */
	protected boolean checkCategory2ByType(byte type) {
		return type == ClassFile.T_DOUBLE || type == ClassFile.T_LONG;
	}

	/**
	 * Check the category of an object symbolically , returning true if a term is of type double or
	 * long.
	 *
	 * @param term The term to check.
	 * @return true if the term is of type dDouble or long; false otherwise.
	 */
	protected boolean checkCategory2Symbolically(Term term) {
		return term.getType() == Expression.DOUBLE || term.getType() == Expression.LONG;
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	public abstract int getNumberOfOtherBytes();

	/**
	 * If the executing method is synchronized, exit the monitor associated with it. An
	 * IllegalMonitorStateException will be thrown, if the current thread is not the
	 * owner of the monitor acquired or reentered. It will also be thrown if there is
	 * no current monitor at all.
	 *
	 * @param frame The currently executed frame.
	 * @throws VmRuntimeException Thrown in case of an illegal monitor state, wrapping an IllegalMonitorStateException.
	 */
	public void ifSynchronizedExitMonitor(Frame frame) throws VmRuntimeException {
		if (frame.getMethod().isAccSynchronized()) {
			Monitor monitor = frame.getMonitor();
			if (monitor == null)
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalMonitorStateException",
						"No monitor has been entered for this method."));
			try {
				monitor.monitorExit();
			} catch (IllegalMonitorStateException e) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IllegalMonitorStateException", e.getMessage()));
			}
		}
	}
}
