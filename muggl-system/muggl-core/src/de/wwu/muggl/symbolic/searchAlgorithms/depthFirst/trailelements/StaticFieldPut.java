package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.InitializedClass;

/**
 * This TrailElement takes an static reference to an object (InitializedClass), a field
 * and an object as its initialization argument. It indicates that this object has to be
 * pushed to the static field in the static reference, so the value of the field at the
 * desired state of execution is restored.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-08-29
 */
public class StaticFieldPut extends FieldPut {
	// The stored data.
	private InitializedClass initializedClass;

	/**
	 * Initialize with the InitializedClass, the field and the value.
	 * @param initializedClass The objectref the field belongs to.
	 * @param field The Field that has its value to be restored.
	 * @param value The object that has to be pushed onto the operand stack.
	 */
	public StaticFieldPut(InitializedClass initializedClass, Field field, Object value) {
		super(field, value);
		this.initializedClass = initializedClass;
	}

	/**
	 * Restore the field. By avoiding to invoke the instruction putstatic, there
	 * is no need for a restoring mode that prevents this change from beeing added
	 * to the trail.
	 */
	public void restoreField() {
		this.initializedClass.putField(this.field, this.value);
	}

}
