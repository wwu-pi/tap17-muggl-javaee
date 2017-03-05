package de.wwu.muggl.vm.classfile.structures.constants;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.vm.classfile.ClassFile;

/**
 * Representation of a CONSTANT_Float_info of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-13
 */
public class ConstantFloat extends ConstantIntegerFloat {
	private float value;

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public ConstantFloat(ClassFile classFile) throws IOException {
		super(classFile);
		this.value = classFile.getDis().readFloat();
		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Read new Constant: Float");
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeFloat(this.value);
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "CONSTANT_Float_info";
	}

	/**
	 * Get the value of the constant.
	 * @return The constants value as a Float.
	 */
	@Override
	public Float getValue() {
		return this.value;
	}

	/**
	 * Get this Constant's tag.
	 * @return The Constant's tag as a byte.
	 */
	@Override
	public byte getTag() {
		return ClassFile.CONSTANT_FLOAT;
	}

}
