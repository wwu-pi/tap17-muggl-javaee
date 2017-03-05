package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;

/**
 * Representation of a exception_table structure of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-13
 */
public class ExceptionTable extends ClassFileStructure {
	private int startPc;
	private int endPc;
	private int handlerPc;
	private int catchType;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the exception_table belongs to.
	 * @param code The attribute_code the exception_table belongs to.
	 * @throws ClassFileException If any incorrect values are detected.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public ExceptionTable(ClassFile classFile, AttributeCode code) throws ClassFileException, IOException {
		super(classFile);
		this.startPc = classFile.getDis().readUnsignedShort();
		if (this.startPc < 0) throw new ClassFileException("Encountered a corrupt class file: start_pc of an exception_table must not be less than zero.");
		if (this.startPc >= code.getCodeLength()) throw new ClassFileException("Encountered a corrupt class file: start_pc of an exception_table must not exceed the code's length minus one (be a valid index into the code).");
		this.endPc = classFile.getDis().readUnsignedShort();
		if (this.endPc < 0) throw new ClassFileException("Encountered a corrupt class file: end_pc of an exception_table must not be less than zero.");
		if (this.endPc > code.getCodeLength()) throw new ClassFileException("Encountered a corrupt class file: end_pc of an exception_table must not exceed the code's length.");
		if (this.endPc <= this.startPc) throw new ClassFileException("Encountered a corrupt class file: start_pc must be less than end_pc for an exception_table.");
		this.handlerPc = classFile.getDis().readUnsignedShort();
		if (this.handlerPc < 0) throw new ClassFileException("Encountered a corrupt class file: handler_pc of an exception_table must not be less than zero.");
		if (this.handlerPc >= code.getCodeLength()) throw new ClassFileException("Encountered a corrupt class file: handler_pc of an exception_table must not exceed the code's length minus one (be a valid index into the code).");
		this.catchType = classFile.getDis().readUnsignedShort();
    	if (this.catchType >= this.classFile.getConstantPoolCount()) {
    		throw new ClassFileException("Encountered a corrupt class file: A catch_type index into the constant pool at position " + this.catchType + " was found. Yet, there are only " + this.classFile.getConstantPoolCount() + " entries in total.");
    	}
    	if (this.catchType < 0) {
    		throw new ClassFileException("Encountered a corrupt class file: A catch_type index into the constant pool was found thats value is less than zero.");
    	}
		if (this.catchType != 0 && classFile.getConstantPool()[this.catchType].getTag() != ClassFile.CONSTANT_CLASS) {
			throw new ClassFileException("Encountered a corrupt class file: catch_type of an attribute should point to a CONSTANT_Class_info.");
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read a exception table entry with catch_type " + getCatchTypeClassName() + ".");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.startPc);
		dos.writeShort(this.endPc);
		dos.writeShort(this.handlerPc);
		dos.writeShort(this.catchType);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "exception_table";
	}

	/**
	 * Getter for the catch_type.
	 * @return The catch_type as an int.
	 */
	public int getCatchType() {
		return this.catchType;
	}

	/**
	 * Getter for the exceptions catch_type class as fetched from the constant_pool entry with index catch_type.
	 * @return The name of this exceptions catch_type class.
	 */
	public String getCatchTypeClassName() {
		if (this.catchType == 0) return "";
		return this.classFile.getConstantPool()[this.catchType].getStringValue();
	}

	/**
	 * Getter for the end_pc.
	 * @return The end_pc as an int.
	 */
	public int getEndPc() {
		return this.endPc;
	}

	/**
	 * Getter for the handler_pc.
	 * @return The handler_pc as an int.
	 */
	public int getHandlerPc() {
		return this.handlerPc;
	}

	/**
	 * Getter for the start_pc.
	 * @return The start_pc as an int.
	 */
	public int getStartPc() {
		return this.startPc;
	}

}
