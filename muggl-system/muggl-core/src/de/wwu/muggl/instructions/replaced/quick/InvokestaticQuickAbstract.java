package de.wwu.muggl.instructions.replaced.quick;

import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.instructions.interfaces.control.JumpInvocation;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.VariableDefining;
import de.wwu.muggl.instructions.interfaces.data.VariableUsing;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.instructions.replaced.replacer.InvokestaticReplacer;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantMethodref;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * Abstract {@link QuickInstruction} for <i>invokestatic</i>. As there are two concrete quick
 * implementations, one for the normal and one for the native invocation, methods and functionality
 * used by both is encapsulated in this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public abstract class InvokestaticQuickAbstract extends GeneralInstructionWithOtherBytes implements
		JumpException, JumpInvocation, StackPop, VariableDefining, VariableUsing, QuickInstruction {
	// The replacing instruction.
	private ReplacingInstruction replacer;
	/**
	 * The invoked method.
	 */
	protected Method method;
	/**
	 * Flag indicating whether the method is invoked synchronized.
	 */
	protected boolean accSynchronized;
	/**
	 * The number of parameters supplied to the invoked method.
	 */
	protected int parameterCount;

	/**
	 * Construct an instance of this abstract class so the concrete class can inherit functionality.
	 *
	 * @param replacer The {@link ReplacingInstruction} that constructs this.
	 * @param otherBytes The additional bytes of the invokestatic instruction replaced by this.
	 * @param method The Method to invoke.
	 * @param accSynchronized Flag whether the method is synchronized, or not.
	 * @param parameterCount The number of parameters passed to the method.
	 */
	InvokestaticQuickAbstract(InvokestaticReplacer replacer, short[] otherBytes, Method method,
			boolean accSynchronized, int parameterCount) {
		this.replacer = replacer;
		this.otherBytes = otherBytes;
		this.method = method;
		this.accSynchronized = accSynchronized;
		this.parameterCount = parameterCount;
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
	 * @param constantPool The constant pool.
	 * @param classLoader The current class loader.
	 * @return The resolved Method.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ExecutionException If the method is resolved to be the initializer or the constant pool does not have a symbolic reference to a method at the position encoded in the bytecode.
	 * @throws NoSuchMethodError If the Method could not be found.
	 */
	public Method getInvokedMethod(Constant[] constantPool, MugglClassLoader classLoader)
		throws ClassFileException, ExecutionException {
		int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
		Constant constant = constantPool[index];
		if (!(constant instanceof ConstantMethodref))
			throw new ExecutionException(
					"Error while executing instruction " + getName()
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
	 * Get the number of other bytes for this instruction.
	 *
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 2;
	}

	/**
	 * Get the {@link ReplacingInstruction} that constructed this QuickInstruction.
	 *
	 * @return The ReplacingInstruction that constructed this QuickInstruction.
	 */
	public ReplacingInstruction getReplacer() {
		return this.replacer;
	}

	/*
	 * The following methods have just been copied from Invoke. There should be a better solution
	 * (without inheriting from Invoke). Probably a shared super class?
	 */
	
	/**
	 * Get the method to execute.<br />
	 * <br />
	 * This method is supposed to be used for static analysis and not by any runtime components!
	 *
	 * @param constantPool The constant pool of the class of the method of currently executed frame.
	 * @param classLoader An appropriately initialized class loader.
	 * @return The method to execute.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ExecutionException In case of any other problems.
	 */
	protected Method getMethod(Constant[] constantPool, MugglClassLoader classLoader)
			throws ClassFileException, ExecutionException {
		int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
		Constant constant = constantPool[index];
		if (!(constant instanceof ConstantMethodref)) {
			throw new ExecutionException(
					"Error while executing instruction " + getName()
						+ ": Expected runtime constant pool item at index "
						+ "to be a symbolic reference to a method.");
		}

		// Get the name and the descriptor.
		String[] nameAndType = ((ConstantMethodref) constant).getNameAndTypeInfo();
		ClassFile methodClassFile = classLoader.getClassAsClassFile(
				((ConstantMethodref) constant).getClassName());

		// Try to resolve method from this class.
		ResolutionAlgorithms resoluton = new ResolutionAlgorithms(classLoader);
		Method method;
		try {
			method = resoluton.resolveMethod(methodClassFile, nameAndType);
		} catch (ClassFileException e) {
			throw new ExecutionException(e);
		} catch (NoSuchMethodError e) {
			throw new ExecutionException(e);
		}

		return method;
	}

	/**
	 * Get the types of elements this instruction will pop from the stack.<br />
	 * <br />
	 * This method is supposed to be used for static analysis and not by any runtime components!
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
		byte[] types;
		Method method;
		try {
			method = getMethod(methodClassFile.getConstantPool(), methodClassFile.getClassLoader());

			String[] typeStrings = method.getParameterTypesAsArray();
			types = new byte[typeStrings.length];
			for (int a = 0; a < typeStrings.length; a++) {
				if (typeStrings[a].equals("boolean")) {
					types[a] = ClassFile.T_BOOLEAN;
				} else if (typeStrings[a].equals("byte")) {
					types[a] = ClassFile.T_BYTE;
				} else if (typeStrings[a].equals("char")) {
					types[a] = ClassFile.T_CHAR;
				} else if (typeStrings[a].equals("double")) {
					types[a] = ClassFile.T_DOUBLE;
				} else if (typeStrings[a].equals("float")) {
					types[a] = ClassFile.T_FLOAT;
				} else if (typeStrings[a].equals("int")) {
					types[a] = ClassFile.T_INT;
				} else if (typeStrings[a].equals("long")) {
					types[a] = ClassFile.T_LONG;
				} else if (typeStrings[a].equals("short")) {
					types[a] = ClassFile.T_SHORT;
				}
			}
		} catch (ClassFileException e) {
			types = new byte[1];
			types[0] = -1;
		} catch (ExecutionException e) {
			types = new byte[1];
			types[0] = -1;
		}

		return types;
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
