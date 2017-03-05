package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;

/**
 * Representation of a element_value structure of an annotation attribute of a class. This is the
 * abstract class inherited by any type of element_value.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-05
 */
public abstract class ElementValue extends ClassFileStructure {
	protected char tag;

	/**
	 * Basic constructor. It is protected since it should be only used by inheriting classes. The
	 * appropriate way to initialize a class representing an element_value is to use
	 * {@link #getElementValue(ClassFile)}.
	 * 
	 * @param classFile The ClassFile the element_value belongs to.
	 * @param tag The tag of this element_value.
	 */
	protected ElementValue(ClassFile classFile, char tag) {
		super(classFile);
		this.tag = tag;
	}

	/**
	 * Get the appropriate element_value type as indicated by the tag.
	 * 
	 * @param classFile The ClassFile the element_value belongs to.
	 * @return The element value of the appropriate sub type.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	public static ElementValue getElementValue(ClassFile classFile) throws IOException, ClassFileException {
		char tag = (char) classFile.getDis().readUnsignedByte();
		
		switch(tag) {
			case 'B':
				//$FALL-THROUGH$
			case 'C':
				//$FALL-THROUGH$
			case 'I':
				//$FALL-THROUGH$
			case 'S':
				//$FALL-THROUGH$
			case 'Z':
				return new ElementValuePrimitive(classFile, tag, "CONSTANT_Integer_info");
			case 'D':
				return new ElementValuePrimitive(classFile, tag, "CONSTANT_Double_info");
			case 'F':
				return new ElementValuePrimitive(classFile, tag, "CONSTANT_Float_info");
			case 'J':
				return new ElementValuePrimitive(classFile, tag, "CONSTANT_Long_info");
			case 's':
				return new ElementValuePrimitive(classFile, tag, "CONSTANT_Utf8_info");
			case 'e':
				return new ElementValueEnum(classFile, tag);
			case 'c':
				return new ElementValueClass(classFile, tag);
			case '@':
				return new ElementValueAnnotation(classFile, tag);
			case '[':
				return new ElementValueArray(classFile, tag);
		}
		
		// No match.
		throw new ClassFileException(
				"Encountered a corrupt class file: tag of an entry_value must have a valid value. Value is \""
						+ tag + "\".");
	}
	
	
	/**
	 * Write the represented structure to the output stream provided.
	 * 
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeByte(this.tag);
		
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "element_value";
	}
	
	/**
	 * Abstract method for getting the string value of the element_value.
	 * 
	 * @return The element_value in its String representation.
	 */
	public abstract String getStringValue();
	
	/**
	 * Getter for the tag.
	 *
	 * @return The tag.
	 */
	public char getTag() {
		return this.tag;
	}

}
