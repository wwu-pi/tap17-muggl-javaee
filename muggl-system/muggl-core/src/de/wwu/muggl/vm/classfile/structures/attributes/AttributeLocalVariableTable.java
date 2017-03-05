package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.LocalVariableTable;

/**
 * Representation of a attribute_local_variable_table of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class AttributeLocalVariableTable extends Attribute {
	private int localVariableTableLength;
	private LocalVariableTable[] localVariableTable;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @param code The attribute_code the exception_table belongs to.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeLocalVariableTable(ClassFile classFile, int attributeNameIndex, AttributeCode code) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("LocalVariableTable")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_local_variable_table must be \"LocalVariableTable\".");
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Reading the Attribute \"LocalVariableTable\"");
		this.localVariableTableLength = classFile.getDis().readUnsignedShort();
		if (this.localVariableTableLength < 0) throw new ClassFileException("Encountered a corrupt class file: local_variable_table_length of an attribute_local_variable_table must not be negative.");
		this.localVariableTable = new LocalVariableTable[this.localVariableTableLength];
		for (int a = 0; a < this.localVariableTableLength; a++) {
			this.localVariableTable[a] = new LocalVariableTable(classFile, code);
		}
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.localVariableTableLength);
		for (int a = 0; a < this.localVariableTableLength; a++) {
			this.localVariableTable[a].writeToClassFile(dos);
		}
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_local_variable_table";
	}

	/**
	 * Getter for the local_variable_table_length.
	 * @return The local_variable_table_length as an int.
	 */
	public int getLocalVariableTableLength() {
		return this.localVariableTableLength;
	}

	/**
	 * Getter for the local_variable_table.
	 * @return The local_variable_table as an array of LocalVariableTable objects.
	 */
	public LocalVariableTable[] getLocalVariableTable() {
		return this.localVariableTable;
	}
}
