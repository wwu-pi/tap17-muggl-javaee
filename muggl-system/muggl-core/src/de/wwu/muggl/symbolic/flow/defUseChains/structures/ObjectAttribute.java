package de.wwu.muggl.symbolic.flow.defUseChains.structures;

import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This DefUseVariable represents a field of an object being accessed by a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class ObjectAttribute extends AbstractObjectAttribute {
	private final Field attribute;
	
	/**
	 * Construct the DefUseVariable representing a field of an object.<br />
	 * <br />
	 * Please note that this is an internal data-structure, not to be used without the handling of
	 * def-use chains. Neither attribute must be null, the instantiation number must be positive and
	 * the field must not be static. This is, however, not checked for performance reasons.
	 *
	 * @param method The Method reading from or writing to the field.
	 * @param instantiationNumber The consecutive instantiation number of the object.
	 * @param attribute The non-static field.
	 */
	public ObjectAttribute(Method method, long instantiationNumber, Field attribute) {
		super(method, instantiationNumber);
		this.attribute = attribute;
	}

	/**
	 * Construct the DefUseVariable representing a field of an object from a candidate for such a
	 * variable.
	 * 
	 * @param candicate The candidate for an object field, i.e. a newly generated object.
	 * @param attribute The non-static field.
	 */
	public ObjectAttribute(ObjectAttributeCandidate candicate, Field attribute) {
		super(candicate.method, candicate.instantiationNumber);
		this.attribute = attribute;
	}
	
	/**
	 * Returns a string representation of the object attribute.
	 *
	 * @return A String representation of the object attribute.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.method.getFullName() + ", #" + this.instantiationNumber + ", "
				+ this.attribute.toString() + "]";
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
	 * Object attributes are lexicographically ordered by the full class name and full signature of
	 * the method. If the latter is equal, they are ordered by their instantiation number and
	 * lexicographically ordered by their fields full class name and name.
	 *
	 * @param arg0 The DefUseVariable to compare this one to.
	 * @return -1 if this DefUseVariable is "less", 1 if it is "greater" and 0 if it is equal.
	 */
	public int compareTo(DefUseVariable arg0) {
		// By definition, an object attribute is less than a local variable or an stack position.
		if (arg0 instanceof LocalVariable || arg0 instanceof StackPosition)
			return -1;

		// An ObjectAttributeCandidate is less.
		if (arg0 instanceof ObjectAttributeCandidate) {
			return -1;
		}
		
		if (arg0 instanceof ObjectAttribute) {
			ObjectAttribute objectAttribute = (ObjectAttribute) arg0;
			// Check if it is the same.
			if (this.method == objectAttribute.method
					&& this.instantiationNumber == objectAttribute.instantiationNumber
					&& this.attribute == objectAttribute.attribute)
					return 0;

			// Check the class name first.
			int result = (this.method.getClassFile().getName()).compareTo(objectAttribute.method
					.getClassFile().getName());
			if (result != 0) return result;

			// Now check the full signature.
			result = this.method.getFullName().compareTo(objectAttribute.method.getFullName());
			if (result != 0) return result;

			// Compare the instantiation number.
			long resultLong = this.instantiationNumber - objectAttribute.instantiationNumber;
			if (resultLong != 0) {
				if (resultLong > 0)
					return 1;
				return -1;
			}

			// Check the attribute's class name.
			result = this.attribute.getClassFile().getName().compareTo(
					objectAttribute.attribute.getClassFile().getName());
			if (result != 0) return result;

			// Finally, check the field name.
			return this.attribute.getFullName().compareTo(
					objectAttribute.attribute.getFullName());
		}

		// By definition, an object attribute is greater than a static attribute.
		return 1;
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
		if (obj instanceof ObjectAttribute) {
			ObjectAttribute objectAttribute = (ObjectAttribute) obj;
			if (this.method == objectAttribute.method
					&& this.instantiationNumber == objectAttribute.instantiationNumber
					&& this.attribute == objectAttribute.attribute)
					return true;
		}
		return false;
	}

}
