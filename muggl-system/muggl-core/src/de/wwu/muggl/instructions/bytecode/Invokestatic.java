package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantMethodref;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * Implementation of the instruction <code>invokestatic</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Invokestatic extends Invoke implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized
	 *         successfully, most likely due to missing additional bytes. This might be caused by a
	 *         corrupt class file, or a class file of a more recent version than what can be handled.
	 */
	public Invokestatic(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
		this.hasObjectrefParameter = 0;
	}

	/**
	 * Make sure the method is not the instance initializer. Check that is is static and is not
	 * abstract. Statically initialize its class if needed and return null.
	 *
	 * @param frame The currently executed frame.
	 * @param nameAndType The name and the descriptor of the method.
	 * @param method The resolved method.
	 * @param parameters The (yet unfilled) array of parameters.
	 * @return The {@link ClassFile} of the object reference to invoke the method on.
	 * @throws ExecutionException If an unexpected problem is found that does not throw a runtime
	 *         exception but forces the virtual machine to halt.
	 * @throws VmRuntimeException If an unexpected condition it met and a runtime exception is
	 *         thrown.
	 */
	@Override
	protected ClassFile checkStaticMethod(Frame frame, String[] nameAndType,
			Method method, Object[] parameters) throws ExecutionException, VmRuntimeException {
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

		// Initialize the class of the method.
		@SuppressWarnings("unused") // TODO
		InitializedClass initializedClass = method.getClassFile().getTheInitializedClass(frame.getVm());

		// Return null.
		return null;
	}

	/**
	 * Do nothing.
	 *
	 * @param frame The currently executed frame.
	 * @param method The resolved method.
	 * @param objectrefClassFile The {@link ClassFile} of the object reference to invoke the method on.
	 */
	@Override
	protected void checkAccess(Frame frame, Method method, ClassFile objectrefClassFile) { }

	/**
	 * Do nothing.
	 *
	 * @param frame The currently executed frame.
	 * @param method The resolved method.
	 * @param methodClassFile The method's class file according to its name and descriptor.
	 * @param objectrefClassFile The {@link ClassFile} of the object reference to invoke the method
	 *        on.
	 * @return The selected method. Might be the method specified.
	 */
	@Override
	protected Method selectMethod(Frame frame, Method method, ClassFile methodClassFile,
			ClassFile objectrefClassFile) {
		return method;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "invokestatic";
	}

	/**
	 * Get a reference to the Method invoked by this instruction.
	 *
	 * @param constantPool The constant pool.
	 * @param classLoader The current class loader.
	 * @return The resolved Method.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ExecutionException If the method is resolved to be the initializer or the constant
	 *         pool does not have a symbolic reference to a method at the position encoded in the
	 *         bytecode.
	 * @throws NoSuchMethodError If the Method could not be found.
	 */
	public Method getInvokedMethod(Constant[] constantPool, MugglClassLoader classLoader)
		throws ClassFileException, ExecutionException {
		int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
		Constant constant = constantPool[index];
		if (!(constant instanceof ConstantMethodref))
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": Expected runtime constant pool item at index to be a symbolic reference to a method.");

		// Get the name and the descriptor.
		String[] nameAndType = ((ConstantMethodref) constant).getNameAndTypeInfo();
		ClassFile methodClassFile = classLoader.getClassAsClassFile(((ConstantMethodref) constant).getClassName());
		if (nameAndType[0].equals("<init>"))
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": The Method must not be the initialization method.");

		// Try to resolve method from this class.
		ResolutionAlgorithms resoluton = new ResolutionAlgorithms(classLoader);
		return resoluton.resolveMethod(methodClassFile, nameAndType);
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		// UnsatisfiedLinkError is not thrown by this vm implementation.
		String[] exceptionTypes = { "java.lang.ExceptionInInitializerError",
									"java.lang.IncompatibleClassChangeError",
									"java.lang.InstantiationException",
									"java.lang.InvocationTargetException",
									"java.lang.NoClassDefFoundError",
									"java.lang.NoSuchMethodError",
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
		return 0;
	}

}
