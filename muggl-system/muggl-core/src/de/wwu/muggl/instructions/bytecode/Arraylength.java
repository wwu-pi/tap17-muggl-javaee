package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;

/**
 * Implementation of the instruction  <code>arraylength</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Arraylength extends de.wwu.muggl.instructions.general.ArraylengthAbstract implements Instruction {

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			Object stackTop = stack.pop();
			// Unexpected exception: arrayref does not point to an array at all.
			if (stackTop != null && !(stackTop instanceof Arrayref)) {
				throw new ExecutionException("Could not get the arraylength since the element on top of the stack is not an array.");
			}
			Arrayref arrayref = (Arrayref) stackTop;

			// Runtime exception: arrayref is null.
			if (arrayref == null)
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

			// Push the length of the array.
			stack.push(arrayref.length);
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
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			Object stackTop = stack.pop();
			
			// check if stack element is a symoblic array reference
			if(stackTop != null && stackTop instanceof SymbolicArrayref) {
				SymbolicArrayref symbolicArrayRef = (SymbolicArrayref) stackTop;
				NumericVariable symbolicArrayLength = symbolicArrayRef.getSymbolicLength();
				stack.push(symbolicArrayLength);
				return;
			}
			
			// Unexpected exception: arrayref does not point to an array at all.
			if (stackTop != null && !(stackTop instanceof Arrayref)) {
				throw new SymbolicExecutionException("Could not get the arraylength since the element on top of the stack is not an array.");
			}
			Arrayref arrayref = (Arrayref) stackTop;

			// Runtime exception: arrayref is null.
			if (arrayref == null)
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

			// Push the length of the array.
			stack.push(IntConstant.getInstance(arrayref.length));
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
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "arraylength";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = {"java.lang.NullPointerException"};
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
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be pushed onto the stack.
	 */
	public int getNumberOfPushedElements() {
		return 1;
	}

	/**
	 * Get the type of elements this instruction will push onto the stack.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The type this instruction pushes. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the pushed type cannot be determined statically.
	 */
	public byte getTypePushed(ClassFile methodClassFile) {
		return ClassFile.T_INT;
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
		byte[] types = {0};
		return types;
	}

}
