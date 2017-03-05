package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;

/**
 * Representation of a attribute_exceptions of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class AttributeExceptions extends Attribute {
	private int numberOfExceptions;
	private int[] exceptionIndexTable;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeExceptions(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("Exceptions")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_exception must be \"Exceptions\".");
		}
		this.numberOfExceptions = classFile.getDis().readUnsignedShort();
		if (this.numberOfExceptions < 0) throw new ClassFileException("Encountered a corrupt class file: number_of_exceptions of an attribute_exception must not be negative.");
		this.exceptionIndexTable = new int[this.numberOfExceptions];
		for (int a = 0; a < this.numberOfExceptions; a++) {
			this.exceptionIndexTable[a] = classFile.getDis().readUnsignedShort();
			checkIndexIntoTheConstantPool(this.exceptionIndexTable[a]);
			if (this.classFile.getConstantPool()[this.exceptionIndexTable[a]].getTag() != ClassFile.CONSTANT_CLASS) {
				throw new ClassFileException("Encountered a corrupt class file: Each entry of the exception_index_table of an attribute_exception should point to a CONSTANT_Class_info.");
			}
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read the Attribute \"Exceptions\" ("  + this.numberOfExceptions + " entries in the exception_table)");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.numberOfExceptions);
		for (int a = 0; a < this.numberOfExceptions; a++) {
			dos.writeShort(this.exceptionIndexTable[a]);
		}
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_exceptions";
	}

	/**
	 * Getter for the exception_index_table.
	 * @return The exception_index_table as an array of int.
	 */
	public int[] getExceptionIndexTable() {
		return this.exceptionIndexTable;
	}

	/**
	 * Getter for the number of exceptions.
	 * @return The number_of_exceptions as an int.
	 */
	public int getNumberOfExceptions() {
		return this.numberOfExceptions;
	}

}
