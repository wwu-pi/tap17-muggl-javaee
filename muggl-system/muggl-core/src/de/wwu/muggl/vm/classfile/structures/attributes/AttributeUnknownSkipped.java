package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;

/**
 * While parsing class files, unknown attributes must be skipped gracefully. In Order to be able to save
 * the class file without deleting unknown attributes from it, its bytes are saved. To delete an attribute,
 * more changes than simply deleting it and decreasing the attribute count by one is needed. It also
 * should always be up to the user whether an attribute is deleted or not.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-06-03
 */
public class AttributeUnknownSkipped extends Attribute {
	private byte[] bytes;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException If the index is invalid.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeUnknownSkipped(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		this.bytes = new byte[this.attributeLength];
		for (int a = 0; a < this.attributeLength; a++) {
			this.bytes[a] = classFile.getDis().readByte();
		}
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read the byte of an unknown attribute");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.write(this.bytes);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "(Unknown attribute that was skipped)";
	}

	/**
	 * Getter for this attribute's bytes.
	 * @return The bytes of this unknown attribute as an array of byte.
	 */
	public byte[] getBytes() {
		return this.bytes;
	}


}
