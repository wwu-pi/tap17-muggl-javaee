package de.wwu.muggl.vm.classfile.structures.attributes.elements;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.ClassFileStructure;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;

/**
 * Representation of a inner_class structure of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class InnerClass extends ClassFileStructure {
	// Fields for the data structures of the class.
	private int innerClassInfoIndex;
	private int outerClassInfoIndex;
	private int innerNameIndex;
	private int innerClassAccessFlags;

	// Fields for the parsed access flags.
	private boolean accPublic = false;
	private boolean accPrivate = false;
	private boolean accProtected = false;
	private boolean accStatic = false;
	private boolean accFinal = false;
	private boolean accInterface = false;
	private boolean accAbstract = false;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the inner_class belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 * @throws ClassFileException Thrown on unexpected fatal errors when parsing the class file.
	 */
	public InnerClass(ClassFile classFile) throws IOException, ClassFileException {
		super(classFile);
		this.innerClassInfoIndex = classFile.getDis().readUnsignedShort();
		if (this.innerClassInfoIndex != 0) {
			checkIndexIntoTheConstantPool(this.innerClassInfoIndex);
			if (this.classFile.getConstantPool()[this.innerClassInfoIndex].getTag() != ClassFile.CONSTANT_CLASS) {
				throw new ClassFileException("Encountered a corrupt class file: inner_classes_info_index of an inner class should point to a CONSTANT_Class_info.");
			}
		}
		this.outerClassInfoIndex = classFile.getDis().readUnsignedShort();
		if (this.outerClassInfoIndex != 0) {
			checkIndexIntoTheConstantPool(this.outerClassInfoIndex);
			if (this.classFile.getConstantPool()[this.outerClassInfoIndex].getTag() != ClassFile.CONSTANT_CLASS) {
				throw new ClassFileException("Encountered a corrupt class file: outer_classes_info_index of an inner class should point to a CONSTANT_Class_info.");
			}
		}
		this.innerNameIndex = classFile.getDis().readUnsignedShort();
			if (this.innerNameIndex != 0) {
			checkIndexIntoTheConstantPool(this.innerNameIndex);
			if (this.classFile.getConstantPool()[this.innerNameIndex].getTag() != ClassFile.CONSTANT_UTF8) {
				throw new ClassFileException("Encountered a corrupt class file: inner_name_index of an inner class should point to a CONSTANT_Utf8_info.");
			}
		}
		this.innerClassAccessFlags = classFile.getDis().readUnsignedShort();
		parseAccessFlags();
		if (Globals.getInst().logger.isTraceEnabled()) {
			if (this.innerNameIndex != 0) {
				Globals.getInst().logger.trace("Parsing: Read a inner class with name \"" + ((ConstantUtf8) this.classFile.getConstantPool()[this.innerNameIndex]).getValue() + "\"");
			} else {
				Globals.getInst().logger.trace("Parsing: Read a inner class with an unknown name.");
			}
		}
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.innerClassInfoIndex);
		dos.writeShort(this.outerClassInfoIndex);
		dos.writeShort(this.innerNameIndex);
		dos.writeShort(this.innerClassAccessFlags);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "inner_class";
	}

	/**
	 * Parse the access flags field so that the single access flags can be easiely checked.
	 * @throws ClassFileException If the access_flags have a value less than zero.
	 */
	protected void parseAccessFlags() throws ClassFileException {
		int flags = this.innerClassAccessFlags;
		if (flags < 0) throw new ClassFileException("Encountered a corrupt class file: access_flags of an inner class is less than zero.");
		if (flags >= ClassFile.ACC_ABSTRACT) {
			flags -= ClassFile.ACC_ABSTRACT;
			this.accAbstract = true;
		}
		if (flags >= ClassFile.ACC_INTERFACE) {
			flags -= ClassFile.ACC_INTERFACE;
			this.accInterface = true;
		}
		if (flags >= ClassFile.ACC_FINAL) {
			flags -= ClassFile.ACC_FINAL;
			this.accFinal = true;
		}
		if (flags >= ClassFile.ACC_STATIC) {
			flags -= ClassFile.ACC_STATIC;
			this.accStatic = true;
		}
		if (flags >= ClassFile.ACC_PROTECTED) {
			flags -= ClassFile.ACC_PROTECTED;
			this.accProtected = true;
		}
		if (flags >= ClassFile.ACC_PRIVATE) {
			flags -= ClassFile.ACC_PRIVATE;
			this.accPrivate = true;
		}
		if (flags >= ClassFile.ACC_PUBLIC) {
			flags -= ClassFile.ACC_PUBLIC;
			this.accPublic = true;
		}
	}

	/**
	 * Getter for inner_class_access_flags.
	 * @return The inner_class_access_flags as an int.
	 */
	public int getInnerClassAccessFlags() {
		return this.innerClassAccessFlags;
	}

	/**
	 * Getter for the inner_class_info_index.
	 * @return The inner_class_info_index as an int.
	 */
	public int getInnerClassInfoIndex() {
		return this.innerClassInfoIndex;
	}

	/**
	 * Getter for the inner_name_index.
	 * @return The inner_name_index as an int.
	 */
	public int getInnerNameIndex() {
		return this.innerNameIndex;
	}

	/**
	 * Getter for the outer_class_info_index.
	 * @return The outer_class_info_index as an int.
	 */
	public int getOuterClassInfoIndex() {
		return this.outerClassInfoIndex;
	}

	/**
	 * Build and return a String representation of the access flags.
	 * @return A String representation of the access flags.
	 */
	public String getPrefix() {
		String prefix = "";
		if (this.accPublic) {
			prefix = "public ";
		} else if (this.accPrivate) {
			prefix = "private ";
		} else if (this.accProtected) {
			prefix = "protected ";
		}
		if (this.accStatic) {
			prefix += "static ";
		}
		if (this.accFinal) {
			prefix += "final ";
		}
		if (this.accInterface) {
			prefix += "interface ";
		}
		if (this.accAbstract) {
			prefix += "abstract ";
		}

		return prefix;
	}

	/**
	 * Getter for the access flag "abstract".
	 * @return true, if the class is abstract, false otherwise.
	 */
	public boolean isAccAbstract() {
		return this.accAbstract;
	}

	/**
	 * Getter for the access flag "final".
	 * @return true, if the class is final, false otherwise.
	 */
	public boolean isAccFinal() {
		return this.accFinal;
	}

	/**
	 * Getter for the access flag "interface".
	 * @return true, if the class is an interface, false otherwise.
	 */
	public boolean isAccInterface() {
		return this.accInterface;
	}

	/**
	 * Getter for the access flag "private".
	 * @return true, if the class is privatet, false otherwise.
	 */
	public boolean isAccPrivate() {
		return this.accPrivate;
	}

	/**
	 * Getter for the access flag "protected".
	 * @return true, if the class is protected, false otherwise.
	 */
	public boolean isAccProtected() {
		return this.accProtected;
	}

	/**
	 * Getter for the access flag "public".
	 * @return true, if the class is public, false otherwise.
	 */
	public boolean isAccPublic() {
		return this.accPublic;
	}

	/**
	 * Getter for the access flag "static".
	 * @return true, if the class is static false otherwise.
	 */
	public boolean isAccStatic() {
		return this.accStatic;
	}

}
