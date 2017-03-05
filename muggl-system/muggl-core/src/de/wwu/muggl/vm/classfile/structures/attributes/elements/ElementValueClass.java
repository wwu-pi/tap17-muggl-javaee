package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;

/**
 * Representation of a class type element_value structure of an annotation attribute of a class.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-05
 */
public class ElementValueClass extends ElementValue {
	private int classInfoIndex;
	
	/**
	 * Basic constructor. Default visibility ensures that this constructor is only access by
	 * {@link ElementValue}.
	 * 
	 * @param classFile The ClassFile the element_value belongs to.
	 * @param tag The tag of this element_value.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	ElementValueClass(ClassFile classFile, char tag) throws IOException, ClassFileException {
		super(classFile, tag);
		
		this.classInfoIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.classInfoIndex);
		if (this.classFile.getConstantPool()[this.classInfoIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException(
					"Encountered a corrupt class file: class_info_index of a value_entry should point to a CONSTANT_Utf8_info.");
		}
		
		if (Globals.getInst().logger.isTraceEnabled())
			Globals.getInst().logger.trace("Parsing: Read an class type element_value of type \""
					+ this.tag + "\".");
	}
	
	/**
	 * Write the represented structure to the output stream provided.
	 * 
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.classInfoIndex);
	}
	
	/**
	 * Get the string value of the element_value.
	 * 
	 * @return The element_value in its String representation.
	 */
	@Override
	public String getStringValue() {
		return this.classFile.getConstantPool()[this.classInfoIndex].getStringValue();
	}
	
	/**
	 * Getter for the class_info_index.
	 *
	 * @return The class_info_index of the element_value.
	 */
	public int getClassInfoIndex() {
		return this.classInfoIndex;
	}
	
}
