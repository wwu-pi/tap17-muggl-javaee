package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;

/**
 * Representation of a attribute_source_file of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-13
 */
public class AttributeSourceFile extends Attribute {
	private int sourcefileIndex;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeSourceFile(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("SourceFile")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_source_file must be \"SourceFile\".");
		}
		this.sourcefileIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.sourcefileIndex);
		if (classFile.getConstantPool()[this.sourcefileIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException("Encountered a corrupt class file: sourcefile_index of an attribute_source_file should point to a CONSTANT_Utf8_info.");
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read the Attribute \"SourceFile\"");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.sourcefileIndex);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_source_file";
	}

	/**
	 * Getter for the sourcefile_index.
	 * @return The sourcefile_index as an int.
	 */
	public int getSourcefileIndex() {
		return this.sourcefileIndex;
	}

}
