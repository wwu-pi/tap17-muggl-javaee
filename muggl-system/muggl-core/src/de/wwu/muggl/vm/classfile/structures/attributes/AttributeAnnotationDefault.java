package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ElementValue;

/**
 * Representation of a attribute_annotation_default of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-04
 */
public class AttributeAnnotationDefault extends Attribute {
	private ElementValue defaultValue;
	
	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeAnnotationDefault(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals(
				"AnnotationDefault")) {
			throw new ClassFileException(
					"Encountered a corrupt class file: attribute_name_index of an attribute_annotation_default must be \"AnnotationDefault\".");
		}
		this.defaultValue = ElementValue.getElementValue(classFile);
	
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read the Attribute \"AnnotationDefault\"");
	}
	
	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_annotation_default";
	}
	
	/**
	 * Getter for the default_value.
	 *
	 * @return The default_value.
	 */
	public ElementValue getDefaultValue() {
		return this.defaultValue;
	}

}
