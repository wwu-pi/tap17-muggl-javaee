package de.wwu.muggl.symbolic.flow.defUseChains.structures;

import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This abstract DefUseVariable represents a field of an object being accessed by a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public abstract class AbstractObjectAttribute implements DefUseVariable {
	// Data that constitutes this DefUseVariable.
	/**
	 * The method the object attribute belongs to.
	 */
	protected final Method method;
	/**
	 * The instantiation number of the object attribute.
	 */
	protected final long instantiationNumber;

	
	/**
	 * Construct the abstract DefUseVariable representing a field of an object.<br />
	 * <br />
	 * Please note that this is an internal data-structure, not to be used without the handling of
	 * def-use chains. For performance reasons no checks are done.
	 *
	 * @param method The Method reading from or writing to the field.
	 * @param instantiationNumber The consecutive instantiation number of the object.
	 */
	public AbstractObjectAttribute(Method method, long instantiationNumber) {
		this.method = method;
		this.instantiationNumber = instantiationNumber;
	}
	
	/**
	 * Returns a hash code value for the object.
	 * 
     * @return  a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (this.method.hashCode() + this.instantiationNumber);
	}
	
}
