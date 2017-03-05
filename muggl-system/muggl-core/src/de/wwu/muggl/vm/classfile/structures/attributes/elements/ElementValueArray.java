package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;

/**
 * Representation of an array type element_value structure of an annotation attribute of a class.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-05
 */
public class ElementValueArray extends ElementValue {
	private int numValues;
	private ElementValue[] elementValues;
	
	/**
	 * Basic constructor. Default visibility ensures that this constructor is only access by
	 * {@link ElementValue}.
	 * 
	 * @param classFile The ClassFile the element_value belongs to.
	 * @param tag The tag of this element_value.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	ElementValueArray(ClassFile classFile, char tag) throws IOException, ClassFileException {
		super(classFile, tag);
		
		this.numValues = classFile.getDis().readUnsignedShort();
		this.elementValues = new ElementValue[this.numValues];
		for (int a = 0; a < this.numValues; a++) {
			this.elementValues[a] = ElementValue.getElementValue(classFile);
		}
		
		if (Globals.getInst().logger.isTraceEnabled())
			Globals.getInst().logger.trace("Parsing: Read an array type element_value of type \""
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
		dos.writeShort(this.numValues);
		for (int a = 0; a < this.numValues; a++) {
			this.elementValues[a].writeToClassFile(dos);
		}
	}
	
	/**
	 * Get the string value of the element_value.
	 * 
	 * @return The element_value in its String representation.
	 */
	@Override
	public String getStringValue() {
		return this.numValues + " element values";
	}
	
	/**
	 * Getter for the num_values.
	 *
	 * @return The num_values.
	 */
	public int getNumValues() {
		return this.numValues;
	}
	
	/**
	 * Getter for the element_values.
	 *
	 * @return The element_values of the element_value.
	 */
	public ElementValue[] getElementValues() {
		return this.elementValues;
	}
	
}
