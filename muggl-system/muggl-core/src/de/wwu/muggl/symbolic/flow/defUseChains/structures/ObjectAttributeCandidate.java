package de.wwu.muggl.symbolic.flow.defUseChains.structures;

import de.wwu.muggl.vm.classfile.Limitations;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This DefUseVariable represents a possible access of a field of an object. It is used to represent
 * "candidates" for such access, i.e. newly generated objects.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class ObjectAttributeCandidate extends AbstractObjectAttribute {
	private final int stackPosition;

	/**
	 * Construct the DefUseVariable representing a candidate field of an object.<br />
	 * <br />
	 * Please note that this is an internal data-structure, not to be used without the handling of
	 * def-use chains.The method must not be null, the instantiation number must be positive and the
	 * stack position must be greater or equal to zero and less than the maximum stack position
	 * allowed by the JVM {@link Limitations#MAX_MAX_STACK}. This is, however, not checked for
	 * performance reasons.
	 * 
	 * @param method The Method reading from or writing to the field.
	 * @param instantiationNumber The consecutive instantiation number of the object.
	 * @param stackPosition The stack position of the newly generated object.
	 */
	public ObjectAttributeCandidate(Method method, long instantiationNumber, int stackPosition) {
		super(method, instantiationNumber);
		this.stackPosition = stackPosition;
	}
	
	/**
	 * Returns a string representation of the object attribute.
	 *
	 * @return A String representation of the object attribute.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Candidate: [" + this.method.getFullName() + ", #" + this.instantiationNumber + ", stack pos: "
				+ this.stackPosition + "]";
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
		
		if (arg0 instanceof ObjectAttributeCandidate) {
			ObjectAttributeCandidate objectAttribute = (ObjectAttributeCandidate) arg0;
			// Check if it is the same.
			if (this.method == objectAttribute.method
					&& this.instantiationNumber == objectAttribute.instantiationNumber
					&& this.stackPosition == objectAttribute.stackPosition)
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

			// Compare the stack position.
			int resultPos = this.stackPosition - objectAttribute.stackPosition;
			return resultPos;
		}

		// An ObjectAttribute is greater.
		if (arg0 instanceof ObjectAttribute) {
			return 1;
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
		if (obj instanceof ObjectAttributeCandidate) {
			ObjectAttributeCandidate objectAttribute = (ObjectAttributeCandidate) obj;
			if (this.method == objectAttribute.method
					&& this.instantiationNumber == objectAttribute.instantiationNumber
					&& this.stackPosition == objectAttribute.stackPosition)
					return true;
		}
		return false;
	}
	
}
