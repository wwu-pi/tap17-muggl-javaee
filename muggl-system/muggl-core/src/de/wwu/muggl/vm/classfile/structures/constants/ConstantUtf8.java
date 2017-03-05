package de.wwu.muggl.vm.classfile.structures.constants;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileWriteAccessViolationException;
import de.wwu.muggl.vm.classfile.structures.Constant;

/**
 * Representation of a CONSTANT_Utf8_info of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-2010
 */
public class ConstantUtf8 extends Constant {
	private int length;
	private byte[] bytes;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public ConstantUtf8(ClassFile classFile) throws IOException {
		super(classFile);
		this.length = classFile.getDis().readUnsignedShort();
		this.bytes = new byte[this.length];
		for (int a = 0; a < this.length; a++) {
			this.bytes[a] = classFile.getDis().readByte();
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read new Constant: Utf8, bytes to String is \"" + getValue() + "\"");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.length);
		dos.write(this.bytes);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "CONSTANT_Utf8_info";
	}

	/**
	 * Get the length of this constant.
	 * @return The length (number if bytes) of this constant as an int.
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Get the bytes of this constant.
	 * @return The bytes of this constant as an array of byte.
	 */
	public byte[] getBytes() {
		return this.bytes;
	}

	/**
	 * Set the bytes of this constant.
	 * @param bytes The bytes of this constant as an array of byte.
	 * @throws ClassFileWriteAccessViolationException If write access is to ClassFiles is not allowed.
	 */
	public void setBytes(byte[] bytes) throws ClassFileWriteAccessViolationException {
		if (!Options.getInst().classFileWriteAccess)
			throw new ClassFileWriteAccessViolationException("Write access is to class files is (currently) not allowed");
		this.bytes = bytes;
	}


	/**
	 * Get the value of the constant.
	 * @return The constants value as a String.
	 */
	@Override
	public String getValue() {
		try {
			return new String(this.bytes, "UTF8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Get the string value of the constant.
	 * @return The constants value in its String representation.
	 */
	@Override
	public String getStringValue() {
		return getValue();
	}

	/**
	 * Get this Constant's tag.
	 * @return The Constant's tag as a byte.
	 */
	@Override
	public byte getTag() {
		return ClassFile.CONSTANT_UTF8;
	}
}
