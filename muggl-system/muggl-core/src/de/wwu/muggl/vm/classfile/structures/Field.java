package de.wwu.muggl.vm.classfile.structures;

import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeConstantValue;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeDeprecated;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeInvisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSignature;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSynthetic;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeUnknownSkipped;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;

/**
 * Representation of a field in classes. This class is a concrete implementation of the
 * abstract FieldMethod.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-05-03
 */
public class Field extends FieldMethod {
	// Fields for the parsed access flags.
	private boolean accVolatile;
	private boolean accTransient;
	private boolean accEnum;

	/**
	 * Basic constructor.
	 *
	 * @param classFile The ClassFile the Method belongs to.
	 * @throws ClassFileException On unexpected fatal errors when parsing the class file.
	 * @throws IOException On errors reading from the DataInputStream of the class.
	 */
	public Field(ClassFile classFile) throws ClassFileException, IOException {
		super(classFile);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "Field_info";
	}

	/**
	 * Parse the attribute number a. Only such attributes will be read that are of relevance
	 * for fields. Other attributes will be skipped.
	 * @param a The number of the attribute.
	 * @throws ClassFileException Thrown on unexpected fatal errors when parsing the class file.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	@Override
	protected void readAttribute(int a) throws ClassFileException, IOException {
    	int attributeNameIndex = this.classFile.getDis().readUnsignedShort();
    	checkIndexIntoTheConstantPool(attributeNameIndex);
    	ConstantUtf8 constant = null;
    	try {
    		constant = (ConstantUtf8) this.classFile.getConstantPool()[attributeNameIndex];
    	} catch (ClassCastException e) {
    		throw new ClassFileException("Expected a ConstantUtf8 at " + attributeNameIndex + " in the constant_pool when reading the attributes, but got " + this.classFile.getConstantPool()[attributeNameIndex].getClass().getName() + ".");
    	}

    	String attributeName = constant.getStringValue();
    	// Which Attribute is it?
    	if (attributeName.equals("ConstantValue")) {
    		this.attributes[a] = new AttributeConstantValue(this.classFile, attributeNameIndex);
    	} else if (attributeName.equals("Synthetic")) {
    		this.attributes[a] = new AttributeSynthetic(this.classFile, attributeNameIndex);
    	} else if (attributeName.equals("Deprecated")) {
    		this.attributes[a] = new AttributeDeprecated(this.classFile, attributeNameIndex);
		} else if (attributeName.equals("RuntimeVisibleAnnotations")) {
			this.attributes[a] = new AttributeRuntimeVisibleAnnotations(this.classFile,
					attributeNameIndex);
		} else if (attributeName.equals("RuntimeInvisibleAnnotations")) {
			this.attributes[a] = new AttributeRuntimeInvisibleAnnotations(this.classFile,
					attributeNameIndex);
		} else if (attributeName.equals("Signature")) {
			this.attributes[a] = new AttributeSignature(this.classFile, attributeNameIndex);
    	} else {
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug("Parsing: Encountered an unknown attribute \""
						+ attributeName + "\"");
			this.attributes[a] = new AttributeUnknownSkipped(this.classFile, attributeNameIndex);
    	}
	}

	/**
	 * Simply return "Field" as a String.
	 * @return "Field" as a String.
	 */
	@Override
	protected String getFieldMethod() {
		return "Field";
	}

	/**
	 * Parse the access flags field so that the single access flags can be easily checked.
	 *
	 * @throws ClassFileException If the access_flags have a value less than zero or if there is an
	 *         illegal combination of flags.
	 */
	@Override
	protected void parseAccessFlags() throws ClassFileException {
		int flags = this.accessFlags;
		if (flags < 0) throw new ClassFileException("Encountered a corrupt class file: access_flags of a " + getName() + " is less than zero.");

		// Parse the flags.
		if (flags >= ClassFile.ACC_ENUM) {
			flags -= ClassFile.ACC_ENUM;
			this.accEnum = true;
		}
		if (flags >= 0x2000) {
			flags -= 0x2000;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x2000 which is unknown for a field.");
		}
		if (flags >= ClassFile.ACC_SYNTHETIC) {
			flags -= ClassFile.ACC_SYNTHETIC;
			this.accSynthetic = true;
		}
		if (flags >= 0x0800) {
			flags -= 0x0800;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x0080 which is unknown for a field.");
		}
		if (flags >= 0x0400) {
			flags -= 0x0400;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x0400 which is unknown for a field.");
		}
		if (flags >= 0x0200) {
			flags -= 0x0200;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x0200 which is unknown for a field.");
		}
		if (flags >= 0x0100) {
			flags -= 0x0100;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x0100 which is unknown for a field.");
		}
		if (flags >= ClassFile.ACC_TRANSIENT) {
			flags -= ClassFile.ACC_TRANSIENT;
			this.accTransient = true;
		}
		if (flags >= ClassFile.ACC_VOLATILE) {
			flags -= ClassFile.ACC_VOLATILE;
			this.accVolatile = true;
		}
		if (flags >= 0x0020) {
			flags -= 0x0020;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x0020 which is unknown for a field.");
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

		// Check the flags.
		if ((this.accPublic && (this.accPrivate | this.accProtected))
				|| (this.accPrivate && this.accProtected))
			throw new ClassFileException(
					"At most one of the flags ACC_PRIVATE, ACC_PROTECTED and ACC_PUBLIC is allowed for a field.");
		if (this.accFinal && this.accVolatile)
			throw new ClassFileException("A field must not have both the flags ACC_FINAL and ACC_VOLATILE set.");
		if (this.classFile.isAccInterface()) {
			if (!this.accPublic)
				throw new ClassFileException("A field of an interface must have its ACC_PUBLIC flag set.");
			if (!this.accStatic)
				throw new ClassFileException("A field of an interface must have its ACC_STATIC flag set.");
			if (!this.accFinal)
				throw new ClassFileException("A field of an interface must have its ACC_FINAL flag set.");
			if (this.accEnum)
				throw new ClassFileException("A field of an interface must not have the ACC_ENUM flag set.");
			if (this.accTransient)
				throw new ClassFileException("A field of an interface must not have the ACC_TRANSIENT flag set.");
			if (this.accVolatile)
				throw new ClassFileException("A field of an interface must not have the ACC_VOLATILE flag set.");
			if (this.accProtected)
				throw new ClassFileException("A field of an interface must not have the ACC_PROTECTED flag set.");
			if (this.accPrivate)
				throw new ClassFileException("A field of an interface must not have the ACC_PRIVATE flag set.");
		}
	}

	/**
	 * Getter for the access flag "transient".
	 * @return true, if the field is transient, false otherwise.
	 */
	public boolean isAccTransient() {
		return this.accTransient;
	}

	/**
	 * Getter for the access flag "volatile".
	 * @return true, if the field is volatile, false otherwise.
	 */
	public boolean isAccVolatile() {
		return this.accVolatile;
	}

	/**
	 * Getter for the access flag "enum".
	 * @return true, if the field is an enum, false otherwise.
	 */
	public boolean isAccEnum() {
		return this.accEnum;
	}

	/**
	 * Build and return a String representation of the access flags.
	 * @return A String representation of the access flags.
	 */
	@Override
	public String getPrefix() {
		String prefix = super.getPrefix();
		if (this.accVolatile) {
			prefix += "volatile ";
		}
		if (this.accTransient) {
			prefix += "transient ";
		}
		if (this.accEnum) {
			prefix += "enum ";
		}

		return prefix;
	}

	/**
	 * Get a String representation of the type of this field.
	 * 
	 * @return A String representation of the type.
	 */
	public String getType() {
		return getLongTypeDescription(getDescriptor());
	}

	/**
	 * Find out if the type is a primitive type. If the return type is an array that consists
	 * of primitive elements, this method will return true. This behavior is different from
	 * {@link java.lang.Class#isPrimitive()}, which would not return true in that case.
	 *
	 * @return true, if the type of this field is primitive, false otherwise.
	 */
	public boolean isPrimitiveType() {
		return getDescriptor().replace("[", "").length() == 1;
	}

	/**
	 * Get the full name including prefixes and type.
	 *
	 * @return The full name.
	 */
	@Override
	public String getFullName() {
		return getPrefix() + getType() + " " + getName();
	}

}
