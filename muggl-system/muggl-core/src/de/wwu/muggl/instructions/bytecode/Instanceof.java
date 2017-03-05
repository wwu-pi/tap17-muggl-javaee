package de.wwu.muggl.instructions.bytecode;

import java.util.Stack;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.CheckcastInstanceof;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantClass;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ExecutionAlgorithms;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * Implementation of the instruction <code>instanceof</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Instanceof extends CheckcastInstanceof implements Instruction, JumpException, StackPop, StackPush {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Instanceof(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			Stack<Object> stack = frame.getOperandStack();
			ReferenceValue objectref = (ReferenceValue) stack.pop();
			Object constant = frame.getConstantPool()[this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]];

			// Unexpected exception: the element from the constant_pool is no ConstantClass.
			if (!(constant instanceof ConstantClass))
				throw new ExecutionException("The constant_pool entry fetched does not have the correct type.");

			// Check if the object is an instance of the expected class.
			String castingToClassName = ((ConstantClass) constant).getName();
			ExecutionAlgorithms ea = new ExecutionAlgorithms(frame.getVm().getClassLoader());
			if (objectref != null
					&& ea.checkForAssignmentCompatibility(objectref, castingToClassName, frame
							.getVm(), false)) {
				// Objectref is not null and assignment compatible (can be cast to) the expected class.
				stack.push(Integer.valueOf(1));
			} else {
				// Objectref is no instance of the expected class.
				stack.push(Integer.valueOf(0));
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
			ReferenceValue objectref = (ReferenceValue) stack.pop();
			Object constant = frame.getConstantPool()[this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]];

			// Unexpected exception: the element from the constant_pool is no ConstantClass.
			if (!(constant instanceof ConstantClass))
				throw new ExecutionException("The constant_pool entry fetched does not have the correct type.");

			// Check if the object is an instance of the expected class.
			String castingToClassName = ((ConstantClass) constant).getName();
			ExecutionAlgorithms ea = new ExecutionAlgorithms(frame.getVm().getClassLoader());
			if (objectref != null && ea.checkForAssignmentCompatibility(objectref, castingToClassName, frame.getVm(), false)) {
				// Objectref is not null and assignment compatible (can be cast to) the expected class.
				stack.push(IntConstant.getInstance(1));
			} else {
				// Objectref is no instance of the expected class.
				stack.push(IntConstant.getInstance(0));
			}
		} catch (ExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "instanceof";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.IllegalAccessError",
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
