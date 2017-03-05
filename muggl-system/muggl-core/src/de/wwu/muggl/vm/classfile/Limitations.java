package de.wwu.muggl.vm.classfile;

/**
 * Provided constants for all limitations inherent to the java virtual machine and the class file format.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public final class Limitations {

	/**
	 * This class cannot be instantiated.
	 */
	private Limitations() { }
	
	// Limitations as described in section 4.10 of the Java virtual machine specification.
	/**
	 * The maximum number of entries in the constant pool of a class file allowed in the JVM.
	 */
	public static final int MAX_CONSTANT_POOL_COUNT = 65535;
	/**
	 * The maximum length of the code per method allowed in the JVM.
	 */
	public static final int MAX_CODE_LENGTH = 65536;
	/**
	 * The maximum size of local variables per method allowed in the JVM.
	 */
	public static final int MAX_MAX_LOCALS = 65535;
	/**
	 * The maximum number of fields per class file allowed in the JVM.
	 */
	public static final int MAX_FIELDS_COUNT = 65535;
	/**
	 * The maximum number of fields per class file allowed in the JVM.
	 */
	public static final int MAX_METHODS_COUNT = 65535;
	/**
	 * The maximum number of fields per class file allowed in the JVM.
	 */
	public static final int MAX_INTERFACES_COUNT = 65535;	
	/**
	 * The maximum size of the operand stack of a frame allowed in the JVM.
	 */
	public static final int MAX_MAX_STACK = 65535;
	/**
	 * The maximum number of dimensions of an array.
	 */
	public static final int MAX_DIMENSIONS = 255;
	/**
	 * The maximum number of parameters of a method.
	 */
	public static final int MAX_PARAMETERS = 255;
	/**
	 * Maximum length of strings.
	 */
	public static final int MAX_UTF8_LENGTH = 65535;
	
	// Other limitations.
	/**
	 * The highest number a opcode can take (it is a signed byte).
	 */
	public static final int OPCODE_OVERFLOW = 127;
	/**
	 * The value to add to or subtract integers representing opcodes that are out of the allowed
	 * range.
	 */
	public static final int OPCODE_TO_OVERFLOW = 256;

}
