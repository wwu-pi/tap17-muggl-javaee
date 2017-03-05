package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;

/**
 * Representation of a primitive type or String element_value structure of an annotation attribute
 * of a class.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-05
 */
public class ElementValuePrimitive extends ElementValue {
	private int	constValueIndex;

	/**
	 * Basic constructor. Default visibility ensures that this constructor is only access by
	 * {@link ElementValue}.
	 * 
	 * @param classFile The ClassFile the element_value belongs to.
	 * @param tag The tag of this element_value.
	 * @param expected The expected primitive type in a String representation.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	ElementValuePrimitive(ClassFile classFile, char tag, String expected) throws IOException,
			ClassFileException {
		super(classFile, tag);

		// Read and check.
		this.constValueIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.constValueIndex);
		if (!this.classFile.getConstantPool()[this.constValueIndex].getStructureName().equals(
				expected)) {
			throw new ClassFileException(
					"Encountered a corrupt class file: element value tag and constant_pool entry type at constant_value_index do not match.");
		}
		
		if (Globals.getInst().logger.isTraceEnabled())
			Globals.getInst().logger
					.trace("Parsing: Read an primitive type or String element_value of type \""
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
		dos.writeShort(this.constValueIndex);
	}
	
	/**
	 * Get the string value of the element_value.
	 * 
	 * @return The element_value in its String representation.
	 */
	@Override
	public String getStringValue() {
		return this.classFile.getConstantPool()[this.constValueIndex].getStringValue();
	}
	
	/**
	 * Getter for the const_value_index.
	 *
	 * @return The const_value_index of the element_value.
	 */
	public int getConstValueIndex() {
		return this.constValueIndex;
	}
	
}
