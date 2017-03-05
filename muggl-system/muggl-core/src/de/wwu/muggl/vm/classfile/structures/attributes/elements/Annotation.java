package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;

/**
 * Representation of a annotation structure of a attribute of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-04
 */
public class Annotation extends ClassFileStructure {
	private int typeIndex;
	private int numElementValuePairs;
	private ElementValuePair[] elementValuesPairs;

	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the local_variable_table belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	public Annotation(ClassFile classFile) throws IOException, ClassFileException {
		super(classFile);
		
		this.typeIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.typeIndex);
		if (this.classFile.getConstantPool()[this.typeIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException(
					"Encountered a corrupt class file: type_index of a annotation should point to a CONSTANT_Utf8_info.");
		}
		this.numElementValuePairs = classFile.getDis().readUnsignedShort();
		this.elementValuesPairs = new ElementValuePair[this.numElementValuePairs];
		for (int a = 0; a < this.numElementValuePairs; a++) {
			this.elementValuesPairs[a] = new ElementValuePair(classFile);
		}
		if (Globals.getInst().logger.isTraceEnabled())
			Globals.getInst().logger.trace("Parsing: Read an annotation.");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * 
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.typeIndex);
		dos.writeShort(this.numElementValuePairs);
		for (int a = 0; a < this.numElementValuePairs; a++) {
			this.elementValuesPairs[a].writeToClassFile(dos);
		}
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "annotation";
	}

	/**
	 * Getter for the type_index.
	 *
	 * @return The type_index of the element_value.
	 */
	public int getTypeIndex() {
		return this.typeIndex;
	}
	
	/**
	 * Getter for the num_element_value_pairs.
	 *
	 * @return The num_element_value_pairs of the element_value.
	 */
	public int getNumElementValuePairs() {
		return this.numElementValuePairs;
	}
	
	/**
	 * Getter for element_value_pairs.
	 *
	 * @return The element_value_pairs.
	 */
	public ElementValuePair[] getElementValuePairs() {
		return this.elementValuesPairs;
	}
	
}
