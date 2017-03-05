package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;

/**
 * Representation of a element_value_pair structure of an annotation of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-04
 */
public class ElementValuePair extends ClassFileStructure {
	private int elementNameIndex;
	private ElementValue value;

	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the local_variable_table belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	public ElementValuePair(ClassFile classFile) throws IOException, ClassFileException {
		super(classFile);
		
		this.elementNameIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.elementNameIndex);
		if (this.classFile.getConstantPool()[this.elementNameIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException(
					"Encountered a corrupt class file: each element_name_index of a annotation should point to a CONSTANT_Utf8_info.");
		}
		this.value = ElementValue.getElementValue(classFile);
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * 
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.elementNameIndex);
		this.value.writeToClassFile(dos);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "element_value_pair";
	}
	
	/**
	 * Getter for the element_name_index.
	 * 
	 * @return The element_name_index.
	 */
	public int getElementNameIndex() {
		return this.elementNameIndex;
	}
	
	/**
	 * Getter for the value entries.
	 *
	 * @return The value.
	 */
	public ElementValue getElementValues() {
		return this.value;
	}
	
}
