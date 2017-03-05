package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;

/**
 * Representation of a parameter_annotation structure of an annotation of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-04
 */
public class ParameterAnnotation extends ClassFileStructure {
	private int numAnnotations;
	private Annotation annotations[];

	/**
	 * Basic constructor.
	 * 
	 * @param classFile The ClassFile the local_variable_table belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException If any incorrect values are detected.
	 */
	public ParameterAnnotation(ClassFile classFile) throws IOException, ClassFileException {
		super(classFile);
		
		this.numAnnotations = classFile.getDis().readUnsignedShort();
		this.annotations = new Annotation[this.numAnnotations];
		for (int a = 0; a < this.numAnnotations; a++) {
			this.annotations[a] = new Annotation(classFile);
		}
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * 
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.numAnnotations);
		for (int a = 0; a < this.numAnnotations; a++) {
			this.annotations[a].writeToClassFile(dos);
		}
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * 
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "parameter_annotation";
	}
	
	/**
	 * Getter for the num_annotations.
	 * 
	 * @return The num_annotations.
	 */
	public int getNumAnnotations() {
		return this.numAnnotations;
	}
	
	/**
	 * Getter for the annotations..
	 *
	 * @return The annotations.
	 */
	public Annotation[] getAnnotations() {
		return this.annotations;
	}
	
}
