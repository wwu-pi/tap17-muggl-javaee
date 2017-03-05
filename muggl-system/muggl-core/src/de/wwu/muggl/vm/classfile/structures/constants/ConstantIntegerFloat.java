package de.wwu.muggl.vm.classfile.structures.constants;

import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Constant;

/**
 * Abstract class, combining data of the class structures constant_float and constant_integer.
 * It is inherited by two concrete subclasses.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-06-11
 */
public abstract class ConstantIntegerFloat extends Constant {

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the constant belongs to.
	 */
	public ConstantIntegerFloat(ClassFile classFile) {
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
