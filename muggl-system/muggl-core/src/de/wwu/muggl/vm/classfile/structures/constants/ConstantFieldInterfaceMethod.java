package de.wwu.muggl.vm.classfile.structures.constants;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;

/**
 * Abstract class, combining data of the class structures CONSTANT_Fieldref_info,  CONSTANT_InterfaceMethodref_info
 * and CONSTANT_Methodref_info. It is inherited by the concrete classes for these constants.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class ConstantFieldInterfaceMethod extends Constant {
	/**
	 * The class_index of this constant.
	 */
	protected int classIndex;
	/**
	 * the name_and_type_index of this constant.
	 */
	protected int nameAndTypeIndex;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 * @throws ClassFileException If the attributeNameIndex is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public ConstantFieldInterfaceMethod(ClassFile classFile) throws ClassFileException, IOException {
		super(classFile);
		this.classIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.classIndex);
		this.nameAndTypeIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.nameAndTypeIndex);
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.classIndex);
		dos.writeShort(this.nameAndTypeIndex);
	}

	/**
	 * Getter for the class_index.
	 * @return The class_index as an int.
	 */
	public int getClassIndex() {
		return this.classIndex;
	}

	/**
	 * Getter for the ClassName fetched from the constant_pool.
	 * @return The class name.
	 */
	public String getClassName() {
		return ((ConstantClass) this.classFile.getConstantPool()[this.classIndex]).getStringValue();
	}

	/**
	 * Getter for the name_and_type_index.
	 * @return The name_and_type_index as an int.
	 */
	public int getNameAndTypeIndex() {
		return this.nameAndTypeIndex;
	}

	/**
	 * Get the value of the constant.
	 * @return The constants value as a String.
	 */
	@Override
	public String getValue() {
		return ((ConstantClass) this.classFile.getConstantPool()[this.classIndex]).getStringValue() + " " + ((ConstantNameAndType) this.classFile.getConstantPool()[this.nameAndTypeIndex]).getStringValue();
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
	 * Get a String array of the name and the type(descriptor).
	 * @return The name and the type.
	 */
	public String[] getNameAndTypeInfo() {
		ConstantNameAndType nameAndType = (ConstantNameAndType) this.classFile.getConstantPool()[this.nameAndTypeIndex];
		String[] nameAndTypeInfo = {nameAndType.getName(), nameAndType.getDescription()};
		return nameAndTypeInfo;
	}

}
