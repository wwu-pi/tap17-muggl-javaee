package de.wwu.muggl.instructions.replaced.replacer;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.Invokestatic;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.instructions.replaced.quick.InvokestaticQuickNative;
import de.wwu.muggl.instructions.replaced.quick.InvokestaticQuickNormal;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantMethodref;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.InitializedClass;

/**
 * {@link ReplacingInstruction} for <i>invokestatic</i>. Executed once, it will store the executions results
 * and replace itself with either {@link InvokestaticQuickNormal} or {@link InvokestaticQuickNative}.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class InvokestaticReplacer extends Invokestatic implements ReplacingInstruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized
	 *         successfully, most likely due to missing additional bytes. This might be caused by a
	 *         corrupt class file, or a class file of a more recent version than can be handled.
	 */
	public InvokestaticReplacer(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction. First prepare any steps for the invocation that will not change on
	 * multiple runs, then generate the quick instruction and put it. Finally run the quick
	 * instruction for the first time.
	 *
	 * @param frame The currently executed frame.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			prepareInvocation(frame).execute(frame);
		} catch (VmRuntimeException e) {
			ExceptionHandler handler = new ExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailed(e2);
			}
		} catch (ClassFileException e) {
			executionFailed(e);
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the instruction symbolically. First prepare any steps for the invocation that will
	 * not change on multiple runs, then generate the quick instruction and put it. Finally run the
	 * quick instruction for the first time.
	 *
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException In case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		try {
			prepareInvocation(frame).executeSymbolically(frame);
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (ClassFileException e) {
			executionFailedSymbolically(e);
		} catch (ExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Prepare any steps for the invocation that will not change on multiple runs, then generate the
	 * quick instruction and put it. Finally run the quick instruction for the first time.<br />
	 * <br />
	 * This method encapsulates any action that can be statically derived from the byte code and
	 * hence prepared. By doing so, only some steps have to be done by the quick instruction which
	 * offers a major increase in execution speed on multiple runs.
	 *
	 * @param frame The currently executed frame.
	 * @return The prepared {@link QuickInstruction}.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 * @throws VmRuntimeException If runtime exceptions occur.
	 */
	private QuickInstruction prepareInvocation(Frame frame) throws ClassFileException, ExecutionException, VmRuntimeException {
		// Prepare the invocation.
		int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
		Constant constant = frame.getConstantPool()[index];
		if (!(constant instanceof ConstantMethodref)) {
			throw new ExecutionException(
					"Error while executing instruction " + getName()
						+ ": Expected runtime constant pool item at index "
						+ "to be a symbolic reference to a method.");
		}

		// Get the name and the descriptor.
		String[] nameAndType = ((ConstantMethodref) constant).getNameAndTypeInfo();
		ClassFile methodClassFile = frame.getVm().getClassLoader().getClassAsClassFile(
				((ConstantMethodref) constant).getClassName());

		// Try to resolve method from this class.
		ResolutionAlgorithms resoluton = new ResolutionAlgorithms(frame.getVm().getClassLoader());
		Method method;
		try {
			method = resoluton.resolveMethod(methodClassFile, nameAndType);
		} catch (ClassFileException e) {
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
		} catch (NoSuchMethodError e) {
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NoSuchMethodError", e.getMessage()));
		}

		// Get the number of arguments.
		int parameterCount = method.getNumberOfArguments();

		// Unexpected exception: the method is an instance initialization method.
		if (nameAndType[0].equals("<init>"))
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": The Method must not be the initialization method.");

		// Unexpected exception: the method is not static.
		if (!method.isAccStatic())
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IncompatibleClassChangeError",
					"Error while executing instruction " + getName()
							+ ": The Method must be static."));

		// Unexpected exception: the method is not static.
		if (method.isAccAbstract())
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": The Method must not be abstract.");

		/*
		 * Initialize the class of the method. As each static initializer obviously only has to be
		 * run once, this can be done now.
		 */
		@SuppressWarnings("unused") // TODO
		InitializedClass initializedClass = method.getClassFile().getTheInitializedClass(frame.getVm());

		// Create the quick instruction.
		QuickInstruction quick;

		// Is the method native?
		if (method.isAccNative()) {
			if (Options.getInst().doNotHaltOnNativeMethods) {
				// Quick instruction for native invocation.
				quick = new InvokestaticQuickNative(this, this.otherBytes, method, method
						.isAccSynchronized(), parameterCount, methodClassFile);
			} else {
				// The method is native but there is no way of handling it.
				throw new ExecutionException("Error while executing instruction " + getName()
						+ ": Execution of native methods is impossible.");
			}
		} else {
			// Quick instruction for normal invocation.
			quick = new InvokestaticQuickNormal(this, this.otherBytes, method, method
					.isAccSynchronized(), parameterCount);
		}

		// Set and the quick instruction and return it.
		frame.getMethod().replaceInstruction(quick, frame.getVm().getPc());
		return quick;
	}

}
