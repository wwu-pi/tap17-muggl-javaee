package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.classfile.structures.Field;

/**
 * This TrailElement is the abstract super class for InstanceFieldPut and StaticFielPut. It stored
 * the data needed to restore a field to a former value.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-09-10
 */
public abstract class FieldPut implements TrailElement {
	/**
	 * The field a value was stored on.
	 */
	protected Field		field;
	/**
	 * The value that was originally stored in the field.
	 */
	protected Object	value;

	/**
	 * Initialize with the the Field and the value.
	 *
	 * @param field The Field that has its value to be restored.
	 * @param object The object that has to be pushed onto the operand stack.
	 */
	public FieldPut(Field field, Object object) {
		this.field = field;
		this.value = object;
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
		return "Trail element that puts a value to the field " + this.field.getName()
				+ " of class " + this.field.getClassFile().getName() + "." + valueInfo;
	}

}
