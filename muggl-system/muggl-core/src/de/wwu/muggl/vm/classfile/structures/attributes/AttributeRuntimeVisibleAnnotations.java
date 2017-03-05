package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;

/**
 * Representation of a attribute_annotation_default of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-04
 */
public class AttributeRuntimeVisibleAnnotations extends AttributeRuntimeAnnotationsAbstract {

	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeRuntimeVisibleAnnotations(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex, "attribute_runtime_visible_annotation",
				"RuntimeVisibleAnnotations");
	}

}
