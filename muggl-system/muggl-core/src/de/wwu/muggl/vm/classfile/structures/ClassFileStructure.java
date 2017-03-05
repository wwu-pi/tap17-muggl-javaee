package de.wwu.muggl.vm.classfile.structures;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;

/**
 * Abstract class that any class implementing a structure from a class file will inherit.
 * It offers access to the ClassFile, enabling the structure to get entrys from the
 * constant_pool for example.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class ClassFileStructure {
	/**
	 * The class file this structure belongs to.
	 */
	protected ClassFile classFile;

	/**
	 * Constructor to set the ClassFile.
	 * @param classFile The ClassFile
	 */
	public ClassFileStructure(ClassFile classFile) {
		this.classFile = classFile;
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	public abstract void writeToClassFile(DataOutputStream dos) throws IOException;

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	public abstract String getStructureName();

	/**
	 * Getter for the ClassFile.
	 * @return The ClassFile.
	 */
	public ClassFile getClassFile() {
		return this.classFile;
	}

	/**
	 * Check if the index into the constant pool is valid. It only is valid, if it is
	 * greater than zero and less than the number of entries.
	 * @param index The index into the constant pool to check.
	 * @throws ClassFileException If the index is invalid.
	 */
	protected void checkIndexIntoTheConstantPool(int index) throws ClassFileException {
    	if (index >= this.classFile.getConstantPoolCount()) {
			throw new ClassFileException(
					"Encountered a corrupt class file: An index into the constant pool at position "
							+ index + " was found. Yet, there are only "
							+ this.classFile.getConstantPoolCount() + " entries in total.");
		}
		if (index <= 0) {
			throw new ClassFileException(
					"Encountered a corrupt class file: An index into the constant pool was found thats value is less than one.");
		}
	}

}
