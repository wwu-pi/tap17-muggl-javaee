package de.wwu.muggl.symbolic.flow.defUseChains.structures;

import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This DefUseVariable represents a static field being accessed by a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class StaticAttribute implements DefUseVariable {
	// Data that constitutes this DefUseVariable.
	private final Method method;
	private final Field attribute;

	/**
	 * Construct the DefUseVariable representing a static field.<br />
	 * <br />
	 * Please note that this is an internal data-structure, not to be used without the handling of
	 * def-use chains. Neither parameter must be null and the attribute must be static. This is,
	 * however, not checked for performance reasons.
	 *
	 * @param method The Method reading from or writing to the field.
	 * @param attribute The static field.
	 */
	public StaticAttribute(Method method, Field attribute) {
		this.method = method;
		this.attribute = attribute;
	}

	/**
	 * Compare this DefUseVariable to the one supplied.<br />
	 * <br />
	 * The order of attributes is defined as follows:<br />
	 * <ol>
	 * <li>Static attributes</li>
	 * <li>Object attributes</li>
	 * <li>Local variables of methods</li>
	 * <li>Operand stack positions during execution</li>
	 * </ol>
	 * <br />
	 * Static attributes are lexicographically ordered by the full class name and full signature
	 * of the method. If the latter is equal, they are lexicographically ordered by their fields
	 * full class name and name.
	 *
	 * @param arg0 The DefUseVariable to compare this one to.
	 * @return -1 if this DefUseVariable is "less", 1 if it is "greater" and 0 if it is equal.
	 */
	public int compareTo(DefUseVariable arg0) {
		if (arg0 instanceof StaticAttribute) {
			StaticAttribute staticAttribute = (StaticAttribute) arg0;
			// Check if it is the same.
			if (this.method == staticAttribute.method
					&& this.attribute == staticAttribute.attribute) return 0;

			// Check the class name first.
			int result = (this.method.getClassFile().getName()).compareTo(staticAttribute.method
					.getClassFile().getName());
			if (result != 0) return result;

			// Now check the full signature.
			result = this.method.getFullName().compareTo(staticAttribute.method.getFullName());
			if (result != 0) return result;

			// Check the attribute's class name.
			result = this.attribute.getClassFile().getName().compareTo(
					staticAttribute.attribute.getClassFile().getName());
			if (result != 0) return result;

			// Finally, check the field name.
			return this.attribute.getFullName().compareTo(
					staticAttribute.attribute.getFullName());
		}

		// By definition, a static attribute is less than any other DefUseVariable.
		return -1;
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 *
	 * @param obj The object to check for equality.
	 * @return true, if the supplied object is of type DefUseVariable and all its fields are equal;
	 *         false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StaticAttribute) {
			StaticAttribute staticAttribute = (StaticAttribute) obj;
			if (this.method == staticAttribute.method
					&& this.attribute == staticAttribute.attribute) return true;
		}
		return false;
	}
	
	/**
	 * Returns a hash code value for the object.
	 * 
     * @return  a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.method.hashCode() + this.attribute.hashCode();
	}

	/**
	 * Returns a string representation of the static attribute.
	 *
	 * @return A String representation of the static attribute.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.method.getFullName() + ", " + this.attribute.toString() + "]";
	}
}
