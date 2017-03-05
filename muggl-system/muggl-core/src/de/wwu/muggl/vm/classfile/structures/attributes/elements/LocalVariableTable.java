package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;

/**
 * Representation of a local_variable_table structure of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-13
 */
public class LocalVariableTable extends ClassFileStructure {
	private int startPc;
	private int length;
	private int nameIndex;
	private int descriptorIndex;
	private int index;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the local_variable_table belongs to.
	 * @param code The attribute_code the exception_table belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	public LocalVariableTable(ClassFile classFile, AttributeCode code) throws IOException, ClassFileException {
		super(classFile);
		this.startPc = classFile.getDis().readUnsignedShort();
		if (this.startPc < 0) throw new ClassFileException("Encountered a corrupt class file: start_pc of an local_variable_table must not be less than zero.");
		if (this.startPc >= code.getCodeLength()) throw new ClassFileException("Encountered a corrupt class file: start_pc of an local_variable_table must not exceed the code's length minus one (be a valid index into the code).");
		this.length = classFile.getDis().readUnsignedShort();
		if (this.length < 0) throw new ClassFileException("Encountered a corrupt class file: length of an local_variable_table must not be less than zero.");
		if (this.startPc + this.length > code.getCodeLength()) throw new ClassFileException("Encountered a corrupt class file: start_pc + length of an local_variable_table must not exceed the code's length.");
		this.nameIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.nameIndex);
		if (classFile.getConstantPool()[this.nameIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException("Encountered a corrupt class file: name_index of an local_variable_table should point to a CONSTANT_Utf8_info.");
		}
		this.descriptorIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.descriptorIndex);
		if (classFile.getConstantPool()[this.descriptorIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException("Encountered a corrupt class file: descriptor_index of an local_variable_table should point to a CONSTANT_Utf8_info.");
		}
		this.index = classFile.getDis().readUnsignedShort();
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read a local variable table entry with name " + getName() + ".");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.startPc);
		dos.writeShort(this.length);
		dos.writeShort(this.nameIndex);
		dos.writeShort(this.descriptorIndex);
		dos.writeShort(this.index);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "local_variable_table";
	}

	/**
	 * Getter for the start_pc.
	 * @return The start_pc as an int.
	 */
	public int getStartPc() {
		return this.startPc;
	}

	/**
	 * Getter for the descriptor_index.
	 * @return The descriptor_index. as an int.
	 */
	public int getDescriptorIndex() {
		return this.descriptorIndex;
	}

	/**
	 * Getter for the index.
	 * @return The index as an int.
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Getter for the length.
	 * @return The length as an int.
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Getter for the name_index.
	 * @return The name_index as an int.
	 */
	public int getNameIndex() {
		return this.nameIndex;
	}

	/**
	 * Getter for the name as fetched from the constant_pool entry with index name_index.
	 * @return The name of this variable.
	 */
	public String getName() {
		return this.classFile.getConstantPool()[this.nameIndex].getStringValue();
	}

}
