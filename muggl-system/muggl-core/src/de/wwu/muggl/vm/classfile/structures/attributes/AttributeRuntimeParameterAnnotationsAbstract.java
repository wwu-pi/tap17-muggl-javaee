package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ParameterAnnotation;

/**
 * Abstract super class for annotation parameters of a class.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-04
 */
public abstract class AttributeRuntimeParameterAnnotationsAbstract extends Attribute {
	private String nameUnderscores;
	private String nameCamelCase;
	private int numParameters;
	private ParameterAnnotation[] parameterAnnotations;

	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a
	 *        UTF8.
	 * @param nameUnderscores The name of this attribute with underscores.
	 * @param nameCamelCase The name of this attribute in camel case.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeRuntimeParameterAnnotationsAbstract(ClassFile classFile, int attributeNameIndex,
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
		this.numParameters = classFile.getDis().readUnsignedByte();
		this.parameterAnnotations = new ParameterAnnotation[this.numParameters];
		for (int a = 0; a < this.numParameters; a++) {
			this.parameterAnnotations[a] = new ParameterAnnotation(classFile);
		}

		if (Globals.getInst().logger.isTraceEnabled())
			Globals.getInst().logger
					.trace("Parsing: Read the Attribute \"\" + this.nameCamelCase + \"\"");
	}
	
	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeByte(this.numParameters);
		for (int a = 0; a < this.numParameters; a++) {
			this.parameterAnnotations[a].writeToClassFile(dos);
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
	 * Getter for the num_parameters.
	 *
	 * @return The num_parameters.
	 */
	public int getNumParameters() {
		return this.numParameters;
	}

	/**
	 * Getter for the parameter_annotations.
	 *
	 * @return The parameter_annotations.
	 */
	public ParameterAnnotation[] getParameterAnnotations() {
		return this.parameterAnnotations;
	}
	
}
