package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.initialization.Arrayref;

/**
 * This TrailElement is the counterpiece for the xastore instructions. It is generated
 * when an array is changed. This can only happen when one of the following instructions
 * is executed: aastore, bastore, castore, dastore, fastore, iastore, lastore, sastore.
 * It takes the information which index of what arrayref has to be restored, and what
 * its original value was.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-24
 */
public class ArrayRestore extends Restore {
	// Field for the array reference.
	private Arrayref arrayref;
	private Number[] primitiveArrayref;

	/**
	 * Constructor for reference arrays: Initialize with the array reference, the index
	 * and the value to restore.
	 * @param arrayref The array reference.
	 * @param index An index into the local variables.
	 * @param object The object that has to be restored at the local variable index.
	 */
	public ArrayRestore(Arrayref arrayref, int index, Object object) {
		super(index, object);
		this.arrayref = arrayref;
		this.primitiveArrayref = null;
	}

	/**
	 * Constructor for primitive type arrays: Initialize with the reference to the
	 * primitive value array (represented by an array of the java.lang wrapper class),
	 * the index and the value to restore.
	 * @param primitiveArrayref The primitive value array reference.
	 * @param index An index into the local variables.
	 * @param object The Number object that has to be restored at the local variable index.
	 */
	public ArrayRestore(Number[] primitiveArrayref, int index, Number object) {
		super(index, object);
		this.arrayref = null;
		this.primitiveArrayref = primitiveArrayref;
	}

	/**
	 * Restore the array element saved by this class.
	 */
	public void restore() {
		if (this.arrayref != null) {
			this.arrayref.putElement(this.index, this.value);
		} else {
			this.primitiveArrayref[this.index] = (Number) this.value;
		}
	}

	/**
	 * Restore the array element saved by this class.
	 *
	 * @param frame This parameter will be ignored.
	 */
	@Override
	public void restore(Frame frame) {
		restore();
	}

	/**
	 * Returns a suitable String representation of the trail element.
	 *
	 * @return A String representation of the trail element.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String valueInfo;
		if (this.value == null) {
			valueInfo = "The value is a null reference.";
		} else {
			valueInfo = "The value is of type " + this.value.getClass().getName()
					+ " and its toString() method returns: " + this.value.toString();
		}
		return "Trail element that restores an element of an array to its former value. "
				+ "The element index is " + this.index + "." + valueInfo;
	}

}
