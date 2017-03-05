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
//import de.wwu.muggl.vm.impl.jpa.list.SymbolicListElementObjectRef;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.FieldObjectNullReference;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * Implementation of the instruction <code>invokevirtual</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Invokevirtual extends Invoke implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized
	 *         successfully, most likely due to missing additional bytes. This might be caused by a
	 *         corrupt class file, or a class file of a more recent version than what can be handled.
	 */
	public Invokevirtual(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Make sure the method is neither the instance initializer nor the static initializer. Set the
	 * object reference to invoke the method on as the first parameter and return its class file.
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
		// The method must be neither the instance initializer nor the static initializer.
		if (method.getName().equals("<init>"))
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": The Method must not be the instance initialization method.");
		if (method.getName().equals("<clinit>"))
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": The Method must not be the class or interface initialization method.");

		// Fetch the object reference to invoke the method on.
		ReferenceValue objectref = (ReferenceValue) frame.getOperandStack().pop();
		parameters[0] = objectref;

		// Runtime exception: objectref is null.
		if (objectref == null || objectref instanceof FieldObjectNullReference || objectref instanceof ObjectNullRef) throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

		// Unexpected exception: objectref is not a constant_class.
		//if (!(objectref instanceof Objectref)) throw new ExecutionException("Objectref must be a reference to a Class.");
		// TODO: is the above needed here? It does not work if an arrayref is supplied, which might happen. Example: Cloning arrays.
		if(objectref instanceof Arrayref) {
			System.out.println("oh ja, das ist eine array ref");
			ClassFile classFile = objectref.getInitializedClass().getClassFile();
			
		}
		
		
//		if(objectref instanceof SymbolicListElementObjectRef) {
//			parameters[0] = ((SymbolicListElementObjectRef)objectref).getOriginal();
//		}
		
		// Fetch the class of objectref and return it.
		return objectref.getInitializedClass().getClassFile();
	}

	/**
	 * Perform access checks. If the method is protected elaborate checks are needed. Check that the
	 * method is not static.
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

		// Is it static?
		if (method.isAccStatic())
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IncompatibleClassChangeError",
					"The method to be invoked with " + getName() + " must not be static."));
	}

	/**
	 * Select the actual method for invocation and make sure it is not abstract.
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
		Method selectedMethod = null;
		while (true) {
			try {
				/*
				 * C contains a declaration for an instance method of the same name and descriptor
				 * and the resolved method is accessible from C.
				 */
				selectedMethod = objectrefClassFile.getMethodByNameAndDescriptor(method.getName(), method.getDescriptor());
				if (!((method.isAccPublic()) || // Accessible, as it is public.
						/*
						 * Accessible, since it is neither private nor protected and in the same
						 * package.
						 */
						(!method.isAccPrivate() && !method.isAccProtected() && method
								.getClassFile().getPackageName().equals(
										objectrefClassFile.getPackageName()))
				/* Accessible, since it is protected and C is a subclass of the methods' class. */
				|| (method.isAccProtected() && objectrefClassFile.equals(method.getClassFile()))))
					selectedMethod = null;
			} catch (MethodResolutionError e) {
				// This is expected. Do nothing.
				selectedMethod = null;
			}
			// Has the method been selected?
			if (selectedMethod != null) break;

			// Does C have a superclass?
			if (objectrefClassFile.getSuperClass() != 0) {
				// Get the super classes recursively.
				objectrefClassFile = frame.getVm().getClassLoader().getClassAsClassFile(
						objectrefClassFile.getConstantPool()[objectrefClassFile.getSuperClass()]
								.getStringValue());
			} else {
				break;
			}
		}

		// Has the method been selected?
		if (selectedMethod == null)
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.AbstractMethodError", "The method to be invoked with "
					+ getName() + " must not be abstract."));
		method = selectedMethod;

		// Is it abstract?
		if (method.isAccAbstract())
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.AbstractMethodError", "The method to be invoked with "
					+ getName() + " must not be abstract."));

		// Return the selected method.
		return method;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "invokevirtual";
	}

	/**
	 * Get a reference to the Method invoked by this instruction.
	 * @param constantPool The constant pool.
	 * @param classLoader The current class loader.
	 * @return The resolved Method.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ExecutionException If the method is resolved to be the static initializer or the initializer or the constant pool does not have a symbolic reference to a method at the position encoded in the bytecode.
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
					+ ": The Method must not be the instance initialization method.");
		if (nameAndType[0].equals("<clinit>"))
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": The Method must not be the class or interface initialization method.");

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
