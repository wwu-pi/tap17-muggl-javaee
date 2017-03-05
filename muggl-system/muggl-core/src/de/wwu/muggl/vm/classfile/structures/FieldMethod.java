package de.wwu.muggl.vm.classfile.structures;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;

/**
 * Abstract class for the structures field and method of a class file. It offers fields and methods
 * used by both inheriting subclasses.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-05-03
 */
public abstract class FieldMethod extends ClassFileStructure  {
	// Fields representing data structures.
	/**
	 * The name_index of the field or method.
	 */
	protected int nameIndex;
	/**
	 * The access_flags of the of the field or method.
	 */
	protected int accessFlags;
	/**
	 * The descriptor_index of the field or method.
	 */
	protected int descriptorIndex;
	/**
	 * The attribute_count of the field or method.
	 */
	protected int attributeCount;
	/**
	 * The attributes of the field or method.
	 */
	protected Attribute[] attributes;

	// Fields for the parsed access flags.
	/**
	 * Flag indicating public accessibility of the field or method.
	 */
	protected boolean accPublic;
	/**
	 * Flag indicating private accessibility of the field or method.
	 */
	protected boolean accPrivate;
	/**
	 * Flag indicating protected accessibility of the field or method.
	 */
	protected boolean accProtected;
	/**
	 * Flag indicating that the field or method is static.
	 */
	protected boolean accStatic;
	/**
	 * Flag indicating that the field or method is final.
	 */
	protected boolean accFinal;
	/**
	 * Flag indicating that the field or method is synthetic.
	 */
	protected boolean accSynthetic;

	/**
	 * Basic constructor.
	 *
	 * @param classFile The ClassFile the Method belongs to.
	 * @throws ClassFileException On unexpected fatal errors when parsing the class file.
	 * @throws IOException On errors reading from the DataInputStream of the class.
	 */
	public FieldMethod(ClassFile classFile) throws ClassFileException, IOException {
		super(classFile);
		
		this.accessFlags = classFile.getDis().readUnsignedShort();
		this.nameIndex = classFile.getDis().readUnsignedShort();
		
		checkIndexIntoTheConstantPool(this.nameIndex);
		if (classFile.getConstantPool()[this.nameIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException("Encountered a corrupt class file: name_index of a Field should point to a CONSTANT_Utf8_info.");
		}
		this.descriptorIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.descriptorIndex);
		if (classFile.getConstantPool()[this.descriptorIndex].getTag() != ClassFile.CONSTANT_UTF8) {
			throw new ClassFileException("Encountered a corrupt class file: descriptor_index of a Field should point to a CONSTANT_Utf8_info.");
		}
		this.attributeCount = classFile.getDis().readUnsignedShort();
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Reading the " + getFieldMethod() + " \"" + ((ConstantUtf8) this.classFile.getConstantPool()[this.nameIndex]).getStringValue() + "\" with " + this.attributeCount + " attributes");
		if (this.attributeCount < 0) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_count of a " + getName() + " is less than zero.");
		}
		// Parse the attributes
		this.attributes = new Attribute[this.attributeCount];
		for (int a = 0; a < this.attributeCount; a++) {
			readAttribute(a);
		}

		// Parse the flags.
		parseAccessFlags();
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		dos.writeShort(this.accessFlags);
		dos.writeShort(this.nameIndex);
		dos.writeShort(this.descriptorIndex);
		dos.writeShort(this.attributeCount);
		for (int a = 0; a < this.attributeCount; a++) {
			this.attributes[a].writeToClassFile(dos);
		}
	}

	/**
	 * Getter for the name of the field or method.
	 *
	 * @return The name as a String.
	 */
	public String getName() {
		return ((ConstantUtf8) this.classFile.getConstantPool()[this.nameIndex]).getStringValue();
	}

	/**
	 * Getter for the package and name of the field or method.
	 * @return The name as a String.
	 */
	public String getPackageAndName() {
		return this.classFile.getName() + "." + ((ConstantUtf8) this.classFile.getConstantPool()[this.nameIndex]).getStringValue();
	}

	/**
	 * Getter for the descriptor of the field or method.
	 * @return The descriptor as a String.
	 */
	public String getDescriptor() {
		return ((ConstantUtf8) this.classFile.getConstantPool()[this.descriptorIndex]).getStringValue();
	}

	/**
	 * Getter for the access_flags.
	 * @return The access_flags as a int.
	 */
	public int getAccessFlags() {
		return this.accessFlags;
	}

	/**
	 * Getter for the attributes.
	 * @return The attributes as an array of Attribute objects.
	 */
	public Attribute[] getAttributes() {
		return this.attributes;
	}

	/**
	 * Getter for the attribute_count.
	 * @return The attribute_count as an int.
	 */
	public int getAttributeCount() {
		return this.attributeCount;
	}

	/**
	 * Getter for the name_index.
	 * @return The name_index as an int.
	 */
	public int getNameIndex() {
		return this.nameIndex;
	}

	/**
	 * Getter for the descriptor_index.
	 * @return The descriptor_index as an int.
	 */
	public int getDescriptorIndex() {
		return this.descriptorIndex;
	}

	/**
	 * Abstract method for the parsing of the attributes, to be overridden by the concrete
	 * methods of the inheriting classes.
	 * @param a The number of the attribute.
	 * @throws ClassFileException Thrown on unexpected fatal errors when parsing the class file.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	protected abstract void readAttribute(int a) throws ClassFileException, IOException;

	/**
	 * Abstract method to get a String representation of either "Field" or "Method", depending
	 * on the inheriting classes.
	 * @return The String "Field" or the String "Method".
	 */
	protected abstract String getFieldMethod();

	/**
	 * Parse the access flags field so that the single access flags can be easily checked.
	 *
	 * @throws ClassFileException If the access_flags have a value less than zero or if there is an
	 *         illegal combination of flags.
	 */
	protected abstract void parseAccessFlags() throws ClassFileException;

	/**
	 * Getter for the access flag "final".
	 * @return true, if the field/method is final, false otherwise.
	 */
	public boolean isAccFinal() {
		return this.accFinal;
	}

	/**
	 * Getter for the access flag "private".
	 * @return true, if the field/method is private, false otherwise.
	 */
	public boolean isAccPrivate() {
		return this.accPrivate;
	}

	/**
	 * Getter for the access flag "protected".
	 * @return true, if the field/method is protected, false otherwise.
	 */
	public boolean isAccProtected() {
		return this.accProtected;
	}

	/**
	 * Getter for the access flag "public".
	 * @return true, if the field/method is public, false otherwise.
	 */
	public boolean isAccPublic() {
		return this.accPublic;
	}

	/**
	 * Getter for the access flag "static".
	 * @return true, if the field/method is static, false otherwise.
	 */
	public boolean isAccStatic() {
		return this.accStatic;
	}

	/**
	 * Getter for the access flag "synthetic".
	 * @return true, if the field/method is synthetic, false otherwise.
	 */
	public boolean isAccSynthetic() {
		return this.accSynthetic;
	}

	/**
	 * Build and return a String representation of the access flags.
	 *
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
		if (this.accSynthetic) {
			prefix += "synthetic ";
		}

		return prefix;
	}

	/**
	 * Parse the long (detailed) type description from a description String. This will return a
	 * representation equal to the one in the source code, thus being more human-readable.
	 *
	 * @param description The description to parse.
	 * @return A detailed description String of types.
	 */
	protected String getLongTypeDescription(String description) {
		String returnedString = "";
		int arrayCount = 0;
		boolean parsingArray = false;
		for (int a = 0; a < description.length(); a++) {
			// Add a separator if necessary.
			if (!parsingArray && returnedString.length() > 0) returnedString += ", ";

			// Parse the next input character.
			parsingArray = false;
			String singleCharacter = description.substring(a, a + 1);
			if (singleCharacter.equals("V")) {
				returnedString += "void";
			} else if (singleCharacter.equals("B")) {
				returnedString += "byte";
			} else if (singleCharacter.equals("C")) {
				returnedString += "char";
			} else if (singleCharacter.equals("D")) {
				returnedString += "double";
			} else if (singleCharacter.equals("I")) {
				returnedString += "int";
			} else if (singleCharacter.equals("F")) {
				returnedString += "float";
			} else if (singleCharacter.equals("J")) {
				returnedString += "long";
			} else if (singleCharacter.equals("S")) {
				returnedString += "short";
			} else if (singleCharacter.equals("Z")) {
				returnedString += "boolean";
			} else if (singleCharacter.equals("L")) {
				int endPos = description.substring(a).indexOf(";");
				returnedString += description.substring(a + 1, a + endPos);
				a = a + endPos;
			} else if (singleCharacter.equals("[")) {
				arrayCount++;
				parsingArray = true;
			} else {
				if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("Encountered an unknown type description while parsing the description of " + getFieldMethod() + " " + getName());
				returnedString += "(unknown type)";
			}

			// Probably we got an array.
			if (!parsingArray && arrayCount > 0) {
				for (int b = 0; b < arrayCount; b++) {
					returnedString += "[]";
				}
				arrayCount = 0;
			}

		}

		// Packages in full class names should be separated by an dot.
		return returnedString.replace("/", ".");
	}

	/**
	 * Return an appropriate String representation of this field or method by simply invoking getName().
	 *
	 * @return An appropriate String representation of this field or method.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getFullName();
	}
	
	/**
	 * Get the full name.
	 *
	 * @return The full name.
	 */
	public abstract String getFullName();

}
