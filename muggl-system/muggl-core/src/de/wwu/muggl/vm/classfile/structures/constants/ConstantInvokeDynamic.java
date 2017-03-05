package de.wwu.muggl.vm.classfile.structures.constants;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;

/**
 * Representation of a CONSTANT_InvokeDynamic_info of a class.
 *
 * @author Max Schulze
 * @see <a href=
 *      "https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.8">
 */
public class ConstantInvokeDynamic extends Constant {
	private int bootstrapMethodAttrIndex;
	private int nameAndTypeIndex;

	/**
	 * Basic constructor.
	 * 
	 * @param classFile
	 *            The ClassFile the constant belongs to.
	 * @throws ClassFileException
	 *             If the attributeNameIndex is invalid.
	 * @throws IOException
	 *             Thrown on errors reading from the DataInputStream of the
	 *             class.
	 */
	public ConstantInvokeDynamic(ClassFile classFile)
			throws ClassFileException, IOException {
		super(classFile);
		// We should check if this points to an entry in the BootstrapMethods table,
		// but it is probably not read by now.
		this.bootstrapMethodAttrIndex = classFile.getDis().readUnsignedShort();

		this.nameAndTypeIndex = classFile.getDis().readUnsignedShort();
		checkIndexIntoTheConstantPool(this.nameAndTypeIndex);
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * 
	 * @param dos
	 *            A DataOutputStream to write the represented structure to.
	 * @throws IOException
	 *             If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.bootstrapMethodAttrIndex);
		dos.writeShort(this.nameAndTypeIndex);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * 
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "CONSTANT_InvokeDynamic_info";
	}

	/**
	 * @return The ndescriptor_index as an int.
	 */
	public int getBootstrapMethodAttrIndex() {
		return this.bootstrapMethodAttrIndex;
	}

	public int getNameAndTypeIndex() {
		return this.nameAndTypeIndex;
	}

	/**
	 * @return The descriptor.
	 */
	public String getNameAndType() {
		return this.classFile.getConstantPool()[this.nameAndTypeIndex]
				.toString();
	}

	/**
	 * Get the value of the constant.
	 * 
	 * @return The constants value as a String.
	 */
	@Override
	public String getValue() {
		return "#" + this.bootstrapMethodAttrIndex + ":"
				+ ((ConstantNameAndType) this.classFile
						.getConstantPool()[this.nameAndTypeIndex]).getValue();
	}

	/**
	 * Get the string value of the constant.
	 * 
	 * @return The constants value in its String representation.
	 */
	@Override
	public String getStringValue() {
		return getValue();
	}

	/**
	 * Get this Constant's tag.
	 * 
	 * @return The Constant's tag as a byte.
	 */
	@Override
	public byte getTag() {
		return ClassFile.CONSTANT_INVOKEDYNAMIC;
	}
}