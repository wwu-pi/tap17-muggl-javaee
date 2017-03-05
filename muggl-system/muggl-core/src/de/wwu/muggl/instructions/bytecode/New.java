package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
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
import de.wwu.muggl.vm.initialization.ReferenceValue;

/**
 * Implementation of the instruction <code>new</code>.
 *
 * @author Tim Majchrzak
 * @version 2.0.
 * Last modified: 2010-12-14
 */
public class New extends de.wwu.muggl.instructions.general.ObjectInitialization implements
		Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public New(AttributeCode code) throws InvalidInstructionInitialisationException {
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
			pushNewObjectref(frame);
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
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			pushNewObjectref(frame);
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
	 * Resolve the class, initialize the objectref and push it.
	 *
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 * @throws VmRuntimeException Thrown on runtime exceptions.
	 */
	private void pushNewObjectref(Frame frame) throws ExecutionException, VmRuntimeException {
		// Resolve the class.
		ResolutionAlgorithms resolution = new ResolutionAlgorithms(frame.getVm().getClassLoader());
		String className = ((ConstantClass) frame.getConstantPool()[this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]]).getValue();
		ClassFile c;
		try {
			c = resolution.resolveClassAsClassFile(frame.getMethod().getClassFile(), className);
		} catch (IllegalAccessError e) {
			// Illegal access to a class that is neither public nor in the same package than the class generating the new array.
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalAccessError", e.getMessage()));
		} catch (NoClassDefFoundError e) {
			// The class could not be found.
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
		}

		// Unexpected exception: check if it is a class type.
		if (c.isAccInterface()) throw new ExecutionException("The class resolved by " + getName() + " should neither be an interface, nor an array type.");

		// Linking exception: the class is abstract.
		if (c.isAccAbstract()) throw new VmRuntimeException(frame.getVm().generateExc("java.lang.InstantiationError", "Cannot instantiate an abstract class."));

		// Initialize the class.
		ReferenceValue objectref;
		try {
			objectref = frame.getVm().getAnObjectref(c);
			if (objectref.isArray()) throw new ExecutionException("The class resolved by " + getName() + " should neither be an interface, nor an array type.");
		} catch (ExceptionInInitializerError e) {
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ExceptionInInitializerError", e.getMessage()));
		}

		// Push the reference to the new class.
		frame.getOperandStack().push(objectref);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "new";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.IllegalAccessError",
									"java.lang.InstantiationError",
									"java.lang.NoClassDefFoundError"};
		return exceptionTypes;
	}

}
