package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.InnerClass;

/**
 * Representation of a attribute_inner_classes of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class AttributeInnerClasses extends Attribute {
	private int numberOfClasses;
	private InnerClass[] classes;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeInnerClasses(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("InnerClasses")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_inner_classes must be \"InnerClasses\".");
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Reading the Attribute \"InnerClasses\"");
		this.numberOfClasses = classFile.getDis().readUnsignedShort();
		if (this.numberOfClasses < 0) throw new ClassFileException("Encountered a corrupt class file: number_of_classes of an attribute_inner_classes must not be negative.");
		this.classes = new InnerClass[this.numberOfClasses];
		for (int a = 0; a < this.numberOfClasses; a++) {
			this.classes[a] = new InnerClass(classFile);
		}
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.numberOfClasses);
		for (int a = 0; a < this.numberOfClasses; a++) {
			this.classes[a].writeToClassFile(dos);
		}
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_inner_classes";
	}

	/**
	 * Getter for the inner_classes.
	 * @return The inner classes as an array of InnerClass objects.
	 */
	public InnerClass[] getClasses() {
		return this.classes;
	}

	/**
	 * Getter for the number_of_classes.
	 * @return The number_of_classes as an int.
	 */
	public int getNumberOfClasses() {
		return this.numberOfClasses;
	}

}
