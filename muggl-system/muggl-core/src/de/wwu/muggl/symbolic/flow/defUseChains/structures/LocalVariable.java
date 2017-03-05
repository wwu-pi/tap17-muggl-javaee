package de.wwu.muggl.symbolic.flow.defUseChains.structures;

import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This DefUseVariable represents a local variable of a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class LocalVariable implements DefUseVariable {
	// Data that constitutes this DefUseVariable.
	private final Method method;
	private final int localVariable;

	/**
	 * Construct the DefUseVariable representing a local variable.<br />
	 * <br />
	 * Please note that this is an internal data-structure, not to be used without the handling of
	 * def-use chains. Method must not be null and local variable must be within the range of the
	 * methods local variable table. This is, however, not checked for performance reasons.
	 *
	 * @param method The method the local variable belong to.
	 * @param localVariable The index into the method's local variable table.
	 */
	public LocalVariable(Method method, int localVariable) {
		this.method = method;
		this.localVariable = localVariable;
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
	 * Local variables are lexicographically ordered by the full class name and full signature of
	 * the method. If the latter is equal, they are ordered by their number in ascending order.
	 *
	 * @param arg0 The DefUseVariable to compare this one to.
	 * @return -1 if this DefUseVariable is "less", 1 if it is "greater" and 0 if it is equal.
	 */
	public int compareTo(DefUseVariable arg0) {
		// By definition, a local variable is greater then attributes.
		if (arg0 instanceof StaticAttribute || arg0 instanceof AbstractObjectAttribute)
			return 1;

		// Comparing to another local variable requires further attention.
		if (arg0 instanceof LocalVariable) {
			LocalVariable localVariable = (LocalVariable) arg0;

			// Check the class name first.
			int result = (this.method.getClassFile().getName()).compareTo(localVariable.method
					.getClassFile().getName());
			if (result != 0) return result;

			// Now check the full signature.
			result = this.method.getFullName().compareTo(localVariable.method.getFullName());
			if (result != 0) return result;

			// Finally, compare the local variable index.
			return this.localVariable - localVariable.localVariable;
		}

		// By definition, a local variable is less than a stack position.
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
		if (obj instanceof LocalVariable) {
			LocalVariable localVariable = (LocalVariable) obj;
			if (this.method == localVariable.method
					&& this.localVariable == localVariable.localVariable)
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
		return this.method.hashCode() + this.localVariable;
	}

	/**
	 * Returns a string representation of the local variable.
	 *
	 * @return A String representation of the local variable.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.method.getFullName() + ", v" + this.localVariable + "]";
	}

}
