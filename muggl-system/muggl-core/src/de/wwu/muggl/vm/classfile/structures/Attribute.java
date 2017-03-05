package de.wwu.muggl.vm.classfile.structures;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;

/**
 * Abstract class for attributes in classes. It is inherited by the concrete attributes in the
 * sub package attributes.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class Attribute extends ClassFileStructure {
	/**
	 * The attribute_name_index of this attribute.
	 */
	protected int attributeNameIndex;
	/**
	 * The attribute_length of this attribute.
	 */
	protected int attributeLength;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex A reference to the constant_pool that points to the UTF8 representation of the attribute.
	 * @throws ClassFileException If the attributeNameIndex is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public Attribute(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile);
		this.attributeNameIndex = attributeNameIndex;
		checkIndexIntoTheConstantPool(this.attributeNameIndex);
		if (classFile.getConstantPool()[this.attributeNameIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute should point to a CONSTANT_Utf8_info.");
		}
		this.attributeLength = classFile.getDis().readInt();
		if (this.attributeLength < 0) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_length of an attribute must not be less than zero.");
		}
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.attributeNameIndex);
		dos.writeInt(this.attributeLength);
	}

	/**
	 * Getter For the attributes name.
	 * @return The attributes name as a String.
	 */
	public String getName() {
		return ((ConstantUtf8) this.classFile.getConstantPool()[this.attributeNameIndex]).getStringValue();
	}

	/**
	 * Getter for the attributes length (number of bytes).
	 * @return The attributes length as an int.
	 */
	public int getAttributeLength() {
		return this.attributeLength;
	}

	/**
	 * Getter for the attributes name index.
	 * @return The attribute_name_index as an int.
	 */
	public int getAttributeNameIndex() {
		return this.attributeNameIndex;
	}
}
