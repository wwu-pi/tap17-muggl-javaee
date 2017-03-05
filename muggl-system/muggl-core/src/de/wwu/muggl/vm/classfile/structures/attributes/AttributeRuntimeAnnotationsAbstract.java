package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;

/**
 * Abstract super class for annotations of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-07
 */
public abstract class AttributeRuntimeAnnotationsAbstract extends Attribute {		
	private String nameUnderscores;
	private String nameCamelCase;
	private int numAnnotations;
	private Annotation[] annotations;
	
	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @param nameUnderscores The name of this attribute with underscores.
	 * @param nameCamelCase The name of this attribute in camel case.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeRuntimeAnnotationsAbstract(ClassFile classFile, int attributeNameIndex,
			String nameUnderscores, String nameCamelCase) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		this.nameUnderscores = nameUnderscores;
		this.nameCamelCase = nameCamelCase;

		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals(
				this.nameCamelCase)) {
			throw new ClassFileException(
					"Encountered a corrupt class file: attribute_name_index of an "
							+ this.nameUnderscores + " must be \"" + this.nameCamelCase + "\".");
		}
		this.numAnnotations = classFile.getDis().readUnsignedShort();
		this.annotations = new Annotation[this.numAnnotations];
		for (int a = 0; a < this.numAnnotations; a++) {
			this.annotations[a] = new Annotation(classFile);
		}

		if (Globals.getInst().logger.isTraceEnabled())
			Globals.getInst().logger.trace("Parsing: Read the Attribute \"" + this.nameCamelCase
					+ "\"");
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
		dos.writeShort(this.numAnnotations);
		for (int a = 0; a < this.numAnnotations; a++) {
			this.annotations[a].writeToClassFile(dos);
		}
	}
	
	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return this.nameUnderscores;
	}

	/**
	 * Getter for num_annotations.
	 *
	 * @return num_annotations as an int.
	 */
	public int getNumAnnotations() {
		return this.numAnnotations;
	}
	
	/**
	 * Getter for the annotations.
	 *
	 * @return The annotations as an array of {@link Annotation}.
	 */
	public Annotation[] getAnnotations() {
		return this.annotations;
	}
	
}
