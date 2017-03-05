package de.wwu.muggl.vm.classfile.structures;

/**
 * The class represents an undefined values. If a variable within the virtual machine is expected to have no
 * value at all, since the null reference would be a value, instances of this class can be used. An example
 * is the pool of local variables which gets initialized with the parameters supplied to a method. If any of
 * this parameters is supposed to not have been set at all (instead of beeing set to null) it can be set to
 * a new instance of this class. The operations dealing with these variables (e.g. the load-instructions)
 * will detect an UndefinedValue and act accordingly by for example throwing an exception.
 *
 * Please note that it is not needed to create multiple instances of this class if more than than variable
 * should get an UndefinedValue; any operation to deal with it will not distinguish between multiple instan-
 * ces but only check for the class itself.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-09-26
 */
public class UndefinedValue {

	/**
	 * Get an appropriate String representation.
	 * @return Returns "undefined".
	 */
	@Override
	public String toString() {
		return "undefined";
	}
}
