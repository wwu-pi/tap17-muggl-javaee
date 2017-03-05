package de.wwu.muggl.vm.classfile.structures.constants;

import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;

/**
 * Representation of a CONSTANT_Methodref_info of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class ConstantMethodref extends ConstantFieldInterfaceMethod {

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 * @throws ClassFileException If the attributeNameIndex is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public ConstantMethodref(ClassFile classFile) throws ClassFileException, IOException {
		super(classFile);
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read new Constant: Methodref, class_index is " + getClassIndex() + ", name_and_type_index is " + getNameAndTypeIndex());
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "CONSTANT_Methodref_info";
	}

	/**
	 * Get this Constant's tag.
	 * @return The Constant's tag as a byte.
	 */
	@Override
	public byte getTag() {
		return ClassFile.CONSTANT_METHODREF;
	}

}
