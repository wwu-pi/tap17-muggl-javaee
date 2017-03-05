package de.wwu.muggl.symbolic.flow.defUseChains.structures;

/**
 * There are several kinds of variables than can form def-use chains. Each of it is represented by
 * an own class that implements this interface. It forces the implementing class to also implement
 * Comparable<DefUseVariable>. Besides that, all implementing classes should override equals(Object)
 * and toString().
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public interface DefUseVariable extends Comparable<DefUseVariable> {

	/**
	 * Indicates whether some other object is equal to this one.
	 *
	 * @param obj The object to check for equality.
	 * @return true, if the supplied object is of type DefUseVariable and all its fields are equal;
	 *         false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	boolean equals(Object obj);

	/**
	 * Returns a hash code value for the object.
	 * 
     * @return  a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	 int hashCode();
	
	/**
	 * Returns a string representation of the DefUseVariable.
	 *
	 * @return A String representation of the DefUseVariable.
	 * @see java.lang.Object#toString()
	 */
	String toString();

}
