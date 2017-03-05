package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;

/**
 * Representation of a line_number_table structure of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-13
 */
public class LineNumberTable extends ClassFileStructure {
	private int startPC;
	private int lineNumber;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the line_number_table belongs to.
	 * @param code The attribute_code the exception_table belongs to.
	 * @throws ClassFileException If start_pc or line_number have invalid values.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public LineNumberTable(ClassFile classFile, AttributeCode code) throws IOException, ClassFileException {
		super(classFile);
		this.startPC = classFile.getDis().readUnsignedShort();
		if (this.startPC < 0) throw new ClassFileException("Encountered a corrupt class file: start_pc of a line_number table must not be negative.");
		if (this.startPC >= code.getCodeLength()) throw new ClassFileException("Encountered a corrupt class file: start_pc of a line_number table must be a valid index into the code.");
		this.lineNumber = classFile.getDis().readUnsignedShort();
		if (this.lineNumber < 0) throw new ClassFileException("Encountered a corrupt class file: line_number of a line_number table must not be negative.");
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read a line number entry");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.startPC);
		dos.writeShort(this.lineNumber);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "line_number_table";
	}

	/**
	 * Getter for the line_number.
	 * @return The line_number as an int.
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}

	/**
	 * Getter for the start_pc.
	 * @return The start_pc as an int.
	 */
	public int getStartPC() {
		return this.startPC;
	}

}
