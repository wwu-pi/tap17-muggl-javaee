package de.wwu.muggl.vm.classfile.structures.constants;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;

/**
 * Representation of a CONSTANT_NameAndType_info of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-13
 */
public class ConstantNameAndType extends Constant {
	private int nameIndex;
	private int descriptorIndex;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 * @throws ClassFileException If the attributeNameIndex is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public ConstantNameAndType(ClassFile classFile) throws ClassFileException, IOException {
		super(classFile);
		this.nameIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.nameIndex);
		this.descriptorIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.descriptorIndex);
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read new Constant: NameAndType, name_index is " + getNameIndex() + ", descriptor_indey is " + getDescriptorIndex());
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.nameIndex);
		dos.writeShort(this.descriptorIndex);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "CONSTANT_NameAndType_info";
	}

	/**
	 * Getter for the name_index of this constant.
	 * @return The name_index as an int.
	 */
	public int getNameIndex() {
		return this.nameIndex;
	}

	/**
	 * Getter for the name, as fetched from the constant pool.
	 * @return The name
	 */
	public String getName() {
		return this.classFile.getConstantPool()[this.nameIndex].toString();
	}

	/**
	 * Getter for the descriptor_index of this constant.
	 * @return The ndescriptor_index as an int.
	 */
	public int getDescriptorIndex() {
		return this.descriptorIndex;
	}

	/**
	 * Getter for the descriptor, as fetched from the constant pool.
	 * @return The descriptor.
	 */
	public String getDescription() {
		return this.classFile.getConstantPool()[this.descriptorIndex].toString();
	}

	/**
	 * Get the value of the constant.
	 * @return The constants value as a String.
	 */
	@Override
	public String getValue() {
		return ((ConstantUtf8) this.classFile.getConstantPool()[this.nameIndex]).getValue() + " " + ((ConstantUtf8) this.classFile.getConstantPool()[this.nameIndex]).getValue();
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
		return ClassFile.CONSTANT_NAMEANDTYPE;
	}
}
