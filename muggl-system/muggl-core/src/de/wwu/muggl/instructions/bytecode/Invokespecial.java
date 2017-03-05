package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.MethodResolutionError;
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
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * Implementation of the instruction <code>invokespecial</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Invokespecial extends Invoke implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized
	 *         successfully, most likely due to missing additional bytes. This might be caused by a
	 *         corrupt class file, or a class file of a more recent version than what can be handled.
	 */
	public Invokespecial(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Set the object reference to invoke the method on as the first parameter and return its class
	 * file.
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
		// Fetch the object reference to invoke the method on.
		ReferenceValue objectref = (ReferenceValue) frame.getOperandStack().pop();
		parameters[0] = objectref;

		// Runtime exception: objectref is null.
		if (objectref == null) throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

		// Unexpected exception: objectref is not a constant_class.
		if (!(objectref instanceof Objectref)) throw new ExecutionException("Objectref must be a reference to a Class.");

		// Fetch the class of objectref and return it.
		return objectref.getInitializedClass().getClassFile();
	}

	/**
	 * Perform access checks. If the method is protected elaborate checks are needed.
	 *
	 * @param frame The currently executed frame.
	 * @param method The resolved method.
	 * @param objectrefClassFile The {@link ClassFile} of the object reference to invoke the method on.
	 * @throws ClassFileException If a required class file cannot be loaded.
	 * @throws VmRuntimeException If an unexpected condition it met and a runtime exception is
	 *         thrown.
	 */
	@Override
	protected void checkAccess(Frame frame, Method method, ClassFile objectrefClassFile)
			throws ClassFileException, VmRuntimeException {
		// Elaborate checks for protected methods.
		if (method.isAccProtected()) {
			boolean isMemberOfClassOrSuperclass = false;
			// Check if the method is a member of the current class or of a superclass of the current class.
			if (method.getClassFile().getName().equals(frame.getMethod().getClassFile().getName())) {
				isMemberOfClassOrSuperclass = true;
			} else {
				ClassFile methodClassFileSuperClass = frame.getMethod().getClassFile();
				while (methodClassFileSuperClass.getSuperClass() != 0) {
					methodClassFileSuperClass = frame.getVm().getClassLoader().getClassAsClassFile(
							methodClassFileSuperClass.getConstantPool()[methodClassFileSuperClass
									.getSuperClass()].getStringValue());
					if (method.getClassFile().getName().equals(methodClassFileSuperClass.getName())) {
						isMemberOfClassOrSuperclass = true;
						break;
					}
				}
			}

			// Do further checking.
			if (isMemberOfClassOrSuperclass) {
				// Is objectref the current class?
				if (!method.getClassFile().getName().equals(objectrefClassFile.getName())) {
					// Objectref is not the same class. Is it a subclass then.
					boolean classMatchFound = false;
					ClassFile objectrefClassFileSuperClass = objectrefClassFile;
					while (objectrefClassFileSuperClass.getSuperClass() != 0) {
						objectrefClassFileSuperClass = frame.getVm().getClassLoader().getClassAsClassFile(
										objectrefClassFileSuperClass.getConstantPool()[objectrefClassFileSuperClass
												.getSuperClass()].getStringValue());
						if (method.getClassFile().getName().equals(objectrefClassFileSuperClass.getName())) {
							// Found the match!
							classMatchFound = true;
							break;
						}
					}
					// Runtime exception.
					if (!classMatchFound)
						throw new VmRuntimeException(frame.getVm().generateExc(
								"java.lang.IllegalAccessError",
								objectrefClassFile.getName() + " may not access method "
										+ method.getName() + " in class "
										+ frame.getMethod().getClassFile().getName() + "."));
				}
			}
		}
	}

	/**
	 * Check if the method can be selected and then select the actual method for invocation. Perform final
	 * checks on it.
	 *
	 * @param frame The currently executed frame.
	 * @param method The resolved method.
	 * @param methodClassFile The method's class file according to its name and descriptor.
	 * @param objectrefClassFile The {@link ClassFile} of the object reference to invoke the method
	 *        on.
	 * @return The selected method. Might be the method specified.
	 * @throws ClassFileException If a required class file cannot be loaded.
	 * @throws VmRuntimeException If an unexpected condition it met and a runtime exception is
	 *         thrown.
	 */
	@Override
	protected Method selectMethod(Frame frame, Method method, ClassFile methodClassFile,
			ClassFile objectrefClassFile) throws ClassFileException, VmRuntimeException {
		// Check if the method can be selected for invocation.
		if (frame.getVm().getCurrentFrame().getMethod().getClassFile().isAccSuper()
				&& !method.getName().equals("<init>")) {
			// Check if the class of the resolved method is a superclass of the current class.
			boolean isASuperClass = false;
			methodClassFile = method.getClassFile();
			while (methodClassFile.getSuperClass() != 0) {
				methodClassFile = frame.getVm().getClassLoader().getClassAsClassFile(
						methodClassFile.getConstantPool()[methodClassFile.getSuperClass()]
								.getStringValue());
				if (frame.getVm().getCurrentFrame().getMethod().getClassFile().getName().equals(
						methodClassFile.getName())) {
					// Found the match!
					isASuperClass = true;
					break;
				}
			}

			if (isASuperClass) {
				/*
				 * Apply a special selection procedure. Let C be the direct superclass of the
				 * current class.
				 */
				boolean methodSelected = false;
				ClassFile superClassesClassFile = frame.getVm().getCurrentFrame().getMethod().getClassFile();
				/*
				 * Does C or any of its super classes contain an instance method with the same name
				 * and descriptor as the resolved method?
				 */
				while (superClassesClassFile.getSuperClass() != 0) {
					superClassesClassFile = frame.getVm().getClassLoader().getClassAsClassFile(
							superClassesClassFile.getConstantPool()[superClassesClassFile
									.getSuperClass()].getStringValue());
					try {
						method = superClassesClassFile.getMethodByNameAndDescriptor(method.getName(), method.getDescriptor());
						// Found the match!
						methodSelected = true;
						break;
					} catch (MethodResolutionError e) {
						// Ignore this error and continue recursively with the super class.
					}
				}

				// Has the method for invocation been selected?
				if (!methodSelected)
					throw new VmRuntimeException(frame.getVm().generateExc("java.lang.AbstractMethodError", "The method to be invoked with " + getName()
									+ " must not be abstract."));
			}
		}

		// Is it static?
		if (method.isAccStatic())
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IncompatibleClassChangeError",
					"The method to be invoked with " + getName() + " must not be static."));

		// Is it abstract?
		if (method.isAccAbstract())
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.AbstractMethodError",
					"The method to be invoked with " + getName() + " must not be abstract."));

		/*
		 * Is it the instance initializer but the class in which it is symbolically referenced is
		 * not the declaring class?
		 */
		if (method.getName().equals("<init>")
				&& !method.getClassFile().getName().equals(methodClassFile.getName()))
			throw new VmRuntimeException(frame.getVm().generateExc(
					"java.lang.NoSuchMethodError",
					"No method could be found by the name " + method.getName() + " in class "
							+ objectrefClassFile.getName() + "."));

		// Return the selected method.
		return method;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "invokespecial";
	}

	/**
	 * Get a reference to the Method invoked by this instruction.
	 *
	 * @param constantPool The constant pool.
	 * @param classLoader The current class loader.
	 * @return The resolved Method.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ExecutionException If the constant pool does not have a symbolic reference to a
	 *         method at the position encoded in the bytecode
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
		String[] exceptionTypes = { "java.lang.AbstractMethodError",
									"java.lang.ExceptionInInitializerError",
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
		return 1;
	}

}
