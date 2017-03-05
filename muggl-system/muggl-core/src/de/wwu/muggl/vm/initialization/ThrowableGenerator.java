package de.wwu.muggl.vm.initialization;

import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.execution.ExecutionAlgorithms;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * This classes provides method to generate instances of type {@link java.lang.Throwable} thrown
 * during the execution of instructions. It is especially meant to be used to generate the runtime
 * exceptions and errors specified by the Java virtual machine specification. This, of course, could
 * be done at the point an actual exception is thrown at. As the generation takes up some code, it
 * is wrapped here.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-31
 */
public class ThrowableGenerator {
	private VirtualMachine vm;
	private ExecutionAlgorithms execution;
	private ClassFile throwableClassFile;

	/**
	 * Construct an instance of the throwable generator. Not more than one generator is needed per
	 * virtual machine instance.stringObjectref
	 * 
	 * @param vm The virtual machine this generator belongs to.
	 * @throws InitializationException If loading class java.lang.Throwable fails.
	 * @throws NullPointerException If parameter <code>vm</code> is null.
	 */
	public ThrowableGenerator(VirtualMachine vm) throws InitializationException {
		if (vm == null)
			throw new NullPointerException("vm musst not be null");
		this.vm = vm;
		MugglClassLoader classLoader = vm.getClassLoader();
		this.execution = new ExecutionAlgorithms(classLoader);
		try {
			this.throwableClassFile = classLoader.getClassAsClassFile("java.lang.Throwable");
		} catch (ClassFileException e) {
			throw new InitializationException(
					"Could not initialize the ThrowableGenerator due to a class loading error.");
		}
	}

	/**
	 * Get an instance of a java.lang.Throwable sub type as specified. It will only be initialized
	 * statically, but no instance fields will be set. This equates to using the empty constructor
	 * {@link java.lang.Throwable#Throwable()}.
	 * 
	 * @param typeString The fully qualified class name of the Throwable object reference to
	 *        generate.
	 * @return The generated object reference.
	 */
	public Objectref getException(String typeString) {
		// Fetch the class file.
		ClassFile classFile;
		try {
			classFile = this.vm.getClassLoader().getClassAsClassFile(typeString);
		} catch (ClassFileException e) {
			throw new IllegalArgumentException("No class named " + typeString + " could be loaded.");
		}
		
		// Check if this is an exception of type RuntimeException.
		try {
			if (!this.execution.isClassOrSubclassOf(classFile, this.throwableClassFile)) {
				throw new IllegalArgumentException("Class " + typeString
						+ " is not of type RuntimeException.");
			}
		} catch (ClassFileException e) {
			throw new IllegalArgumentException("Error checking class " + typeString + ".");
		}
		
		// Create a new object reference and return it.
		return this.vm.getAnObjectref(classFile);
	}

	/**
	 * Get an instance of a java.lang.Throwable sub type as specified. It will only be initialized
	 * statically and have the field <code>detailMessage</code> set to the specified value. This
	 * equates to using the constructor {@link java.lang.Throwable#Throwable(String)}.
	 * 
	 * @param typeString The type of the exception to create. The fully qualified class name of the
	 *        Throwable object reference to generate.
	 * @param message The detail message of the exception. The detail message of the Throwable.
	 * @return The generated object reference.
	 */
	public Objectref getException(String typeString, String message) {
		// Get the object references.
		Objectref objectref = getException(typeString);
		Objectref stringObjectref = this.vm.getStringCache().getStringObjectref(message);
		
		// Put the value.
		Field detailMessageField = objectref.getInitializedClass().getClassFile().getFieldByName(
				"detailMessage", true);
		objectref.putField(detailMessageField, stringObjectref);
		
		// Return the object reference.
		return objectref;
	}
	
}
