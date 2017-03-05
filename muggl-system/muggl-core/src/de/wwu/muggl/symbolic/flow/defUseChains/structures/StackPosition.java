package de.wwu.muggl.symbolic.flow.defUseChains.structures;

import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This DefUseVariable represents a position on the operand stack of a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class StackPosition implements DefUseVariable {
	// Data that constitutes this DefUseVariable.
	private final Method method;
	private final int position;

	/**
	 * Construct the DefUseVariable representing a position on an operand stack.<br />
	 * <br />
	 * Please note that this is an internal data-structure, not to be used without the handling of
	 * def-use chains. Method must not be null and the position must be a valid position on the
	 * stack. This is, however, not checked for performance reasons.
	 *
	 * @param method The method the operand stack belongs to.
	 * @param position The position on the method's operand stack.
	 */
	public StackPosition(Method method, int position) {
		this.method = method;
		this.position = position;
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
	 * Stack positions are lexicographically ordered by the full class name and full signature of
	 * the method. If the latter is equal, they are ordered by their position in ascending order.
	 *
	 * @param arg0 The DefUseVariable to compare this one to.
	 * @return -1 if this DefUseVariable is "less", 1 if it is "greater" and 0 if it is equal.
	 */
	public int compareTo(DefUseVariable arg0) {
		// Comparing to another local variable requires further attention.
		if (arg0 instanceof StackPosition) {
			StackPosition stackPosition = (StackPosition) arg0;

			// Check the class name first.
			int result = (this.method.getClassFile().getName()).compareTo(stackPosition.method
					.getClassFile().getName());
			if (result != 0) return result;

			// Now check the full signature.
			result = this.method.getFullName().compareTo(stackPosition.method.getFullName());
			if (result != 0) return result;

			// Finally, compare the local variable index.
			return this.position - stackPosition.position;
		}

		// By definition, a stack position is greater than any other DefUseVariable
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
		if (obj instanceof StaticAttribute) {
			StackPosition stackPosition = (StackPosition) obj;
			if (this.method == stackPosition.method && this.position == stackPosition.position)
				return true;
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
		return this.method.hashCode() - this.position;
	}

	/**
	 * Returns a string representation of the stack position.
	 *
	 * @return A String representation of the stack position.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.method.getFullName() + ", p" + this.position + "]";
	}
}
