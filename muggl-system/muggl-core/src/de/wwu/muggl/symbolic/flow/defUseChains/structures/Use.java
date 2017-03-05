package de.wwu.muggl.symbolic.flow.defUseChains.structures;

/**
 * This class represents an usage of a variable. It can be used to construct def-use chains,
 * especially to store the information needed to generate them.<br />
 * <br />
 * The interface Comparable is implemented to enable sorting of def-use chains.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class Use implements Comparable<Use> {
	private final DefUseVariable variable;
	private final int instructionNumber;

	/**
	 * Initialize the usage.
	 *
	 * @param variable The variable that is used.
	 * @param instructionNumber The number of the instruction responsible for the usage (additional bytes are counted!).
	 */
	public Use(DefUseVariable variable, int instructionNumber) {
		this.variable = variable;
		this.instructionNumber = instructionNumber;		
	}

	/**
	 * Getter for the variable index.
	 * @return The variable index.
	 */
	public DefUseVariable getVariable() {
		return this.variable;
	}

	/**
	 * Getter for the number of the instruction responsible for the usage.
	 * @return The instruction number.
	 */
	public int getInstructionNumber() {
		return this.instructionNumber;
	}

	/**
	 * Compare this usage to the one supplied.<br />
	 * <br />
	 * Usages are sorted by their variable. If two Usages have the same variable, they are
	 * sorted in ascending order by their instruction number.
	 *
	 * @param arg0 The usage to compare this one to.
	 * @return -1 if this usage is "less", 1 if it is "greater" and 0 if it is equal.
	 */
	public int compareTo(Use arg0) {
		int result = this.variable.compareTo(arg0.variable);
		if (result != 0) return result;

		if (this.instructionNumber < arg0.instructionNumber) {
			return -1;
		}
		if (this.instructionNumber > arg0.instructionNumber) {
			return 1;
		}
		return 0;
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 *
	 * @param obj The object to check for equality.
	 * @return true, if the supplied object is of type Use and both the variable and the instruction
	 *         number are equal; false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Def) {
			Use use = (Use) obj;
			if (use.variable == this.variable && use.instructionNumber == this.instructionNumber)
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
		return this.variable.hashCode() + this.instructionNumber;
	}

	/**
	 * Get a String representation of the usage.
	 * @return A String representation of the usage.
	 */
	@Override
	public String toString() {
		return "[" + this.variable + ", ?, " + this.instructionNumber + "]";
	}

}
