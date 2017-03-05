package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.LineNumberTable;

/**
 * Representation of a attribute_line_number_table of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class AttributeLineNumberTable extends Attribute {
	private int lineNumberTableLength;
	private LineNumberTable[] lineNumberTable;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @param code The attribute_code the exception_table belongs to.
	 * @throws ClassFileException If the index is invalid or parsing of an line_number_table entry fails.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeLineNumberTable(ClassFile classFile, int attributeNameIndex, AttributeCode code) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("LineNumberTable")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_line_number_table must be \"LineNumberTable\".");
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Reading the Attribute \"LineNumberTable\"");
		this.lineNumberTableLength = classFile.getDis().readUnsignedShort();
		if (this.lineNumberTableLength < 0) throw new ClassFileException("Encountered a corrupt class file: line_number_table_length of an attribute_line_number_table must not be negative.");
		this.lineNumberTable = new LineNumberTable[this.lineNumberTableLength];
		for (int a = 0; a < this.lineNumberTableLength; a++) {
			this.lineNumberTable[a] = new LineNumberTable(classFile, code);
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
		dos.writeShort(this.lineNumberTableLength);
		for (int a = 0; a < this.lineNumberTableLength; a++) {
			this.lineNumberTable[a].writeToClassFile(dos);
		}
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_line_number_table";
	}

	/**
	 * Getter for the line_number_table_length.
	 * @return The line_number_table_length as an int.
	 */
	public int getLineNumberTableLength() {
		return this.lineNumberTableLength;
	}

	/**
	 * Gett for the line_number_table.
	 * @return The line_number_table as an array of LineNumberTable objects.
	 */
	public LineNumberTable[] getLineNumberTable() {
		return this.lineNumberTable;
	}

}
