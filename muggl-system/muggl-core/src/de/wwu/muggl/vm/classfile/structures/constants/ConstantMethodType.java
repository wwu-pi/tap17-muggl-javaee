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
 * @author Max Schulze
 */
public class ConstantMethodType extends Constant {
	/**
	 * The descriptor_index of this constant.
	 */
	protected int descriptorIndex;

	/**
	 * Basic constructor.
	 * 
	 * @param classFile
	 *            The ClassFile the constant belongs to.
	 * @throws ClassFileException
	 *             If the attributeNameIndex is invalid.
	 * @throws IOException
	 *             Thrown on errors reading from the DataInputStream of the
	 *             class.
	 */
	public ConstantMethodType(ClassFile classFile)
			throws ClassFileException, IOException {
		super(classFile);
		this.descriptorIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.descriptorIndex);
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * 
	 * @param dos
	 *            A DataOutputStream to write the represented structure to.
	 * @throws IOException
	 *             If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.descriptorIndex);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * 
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "CONSTANT_MethodType_info";
	}

	/**
	 * Getter for the descriptor_index.
	 * 
	 * @return The descriptor_index as an int.
	 */
	public int getDescriptorIndex() {
		return this.descriptorIndex;
	}

	/**
	 * Get the value of the constant.
	 * 
	 * @return The constants value as a String.
	 */
	@Override
	public String getValue() {
		return ((ConstantUtf8) this.classFile
				.getConstantPool()[this.descriptorIndex]).getStringValue();
	}

	/**
	 * Get the string value of the constant.
	 * 
	 * @return The constants value in its String representation.
	 */
	@Override
	public String getStringValue() {
		return getValue();
	}

	/**
	 * Get this Constant's tag.
	 * 
	 * @return The Constant's tag as a byte.
	 */
	@Override
	public byte getTag() {
		return ClassFile.CONSTANT_METHODTYPE;
	}

}