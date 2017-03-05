package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantDouble;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantFloat;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantInteger;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantLong;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantString;

/**
 * Representation of a attribute_constant_value of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class AttributeConstantValue extends Attribute {
	private int constantvalueIndex;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the constant value is not either of type ConstantDouble, ConstantFloat, ConstantInteger, ConstantLong or ConstantString, indicating an error or a specfication violation in the class file. It also is If an invalid index into the constant pool is encountered.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeConstantValue(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("ConstantValue")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_constant_value must be \"ConstantValue\".");
		}
		if (this.attributeLength != 2) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_length of an attribute_constant_value must be 2.");
		}

		this.constantvalueIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.constantvalueIndex);
		// check if constantvalue_index has a valid value
		if (!(
this.classFile.getConstantPool()[this.constantvalueIndex] instanceof ConstantLong
				|| this.classFile.getConstantPool()[this.constantvalueIndex] instanceof ConstantFloat
				|| this.classFile.getConstantPool()[this.constantvalueIndex] instanceof ConstantDouble
				|| this.classFile.getConstantPool()[this.constantvalueIndex] instanceof ConstantInteger || this.classFile
				.getConstantPool()[this.constantvalueIndex] instanceof ConstantString
				)) {
			throw new ClassFileException("Class File invalid: constantvalue_index for attribute has an invalid value");
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read the Attribute \"ConstantValue\"");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.constantvalueIndex);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_constant_value";
	}

	/**
	 * Getter for the constantvalue_index.
	 * @return The constantvalue_index as an int.
	 */
	public int getConstantvalueIndex() {
		return this.constantvalueIndex;
	}
}
