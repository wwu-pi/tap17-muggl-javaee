package de.wwu.muggl.vm.classfile.structures.constants;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * Representation of a CONSTANT_Integer_info of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-13
 */
public class ConstantInteger extends ConstantIntegerFloat {
	private int value;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public ConstantInteger(ClassFile classFile) throws IOException {
		super(classFile);
		this.value = classFile.getDis().readInt();
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read new Constant: Integer");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeInt(this.value);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "CONSTANT_Integer_info";
	}

	/**
	 * Get the value of the constant.
	 * @return The constants value as an Integer.
	 */
	@Override
	public Integer getValue() {
		return this.value;
	}

	/**
	 * Get this Constant's tag.
	 * @return The Constant's tag as a byte.
	 */
	@Override
	public byte getTag() {
		return ClassFile.CONSTANT_INTEGER;
	}

}
