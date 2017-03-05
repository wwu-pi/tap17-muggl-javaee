package de.wwu.muggl.vm.classfile.structures;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * Abstract class for constants in classes. It is inherited by the concrete constants in the
 * sub package constants.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public abstract class Constant extends ClassFileStructure {
	
	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 */
	public Constant(ClassFile classFile) {
		super(classFile);
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeByte(getTag());
	}

	/**
	 * Abstract method for getting the value of the constant.
	 * @return The constants value as an object.
	 */
	public abstract Object getValue();

	/**
	 * Abstract method for getting the string value of the constant.
	 * @return The constants value in its String representation.
	 */
	public abstract String getStringValue();

	/**
	 * Invoke the getStringValue()-Method to get the String value, then return it.
	 * @return The String returned by the invocation of getStringValue().
	 */
	@Override
	public String toString() {
		return getStringValue();
	}

	/**
	 * Get this Constant's tag.
	 * @return The Constant's tag as a byte.
	 */
	public abstract byte getTag();

}
