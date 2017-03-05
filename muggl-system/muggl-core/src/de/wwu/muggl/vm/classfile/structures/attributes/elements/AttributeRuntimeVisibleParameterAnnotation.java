package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;

/**
 * Representation of a attribute_runtime_visible_parameter_annotation of a class.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-03
 */
public class AttributeRuntimeVisibleParameterAnnotation extends Attribute {
	private int numParameters;
	private ParameterAnnotation[] parameterAnnotations;
	
	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeRuntimeVisibleParameterAnnotation(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("RuntimeVisibleParameterAnnotations")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_runtime_visible_parameter_annotation must be \"RuntimeVisibleParameterAnnotations\".");
		}
		this.numParameters = classFile.getDis().readUnsignedByte();
		this.parameterAnnotations = new ParameterAnnotation[this.numParameters];
		for (int a = 0; a < this.numParameters; a++) {
			this.parameterAnnotations[a] = new ParameterAnnotation(classFile);
		}
		
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read the Attribute \"RuntimeVisibleParameterAnnotation\"");
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
		return "attribute_runtime_visible_parameter_annotation";
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
