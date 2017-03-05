package de.wwu.muggl.vm.classfile.structures.constants;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Constant;

/**
 * Abstract class, combining data of the class structures constant_double and constant_long.
 * It is inherited by two concrete subclasses.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-09-26
 */
public abstract class ConstantLongDouble extends Constant {

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 */
	public ConstantLongDouble(ClassFile classFile) {
		super(classFile);
	}

	/**
	 * Get the string value of the constant.
	 * @return The constants value in its String representation.
	 */
	@Override
	public String getStringValue() {
		return String.valueOf(getValue());
	}

}
