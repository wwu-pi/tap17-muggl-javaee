package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements;

import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.Objectref;

/**
 * This TrailElement takes an objectref, a field and an object as its initialization
 * argument. It indicates that this object has to be pushed to the instance field in
 * objectef, so the value of the field at the desired state of execution is restored.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-08-29
 */
public class InstanceFieldPut extends FieldPut {
	// The additionally stored data.
	private Objectref objectref;

	/**
	 * Initialize with the objectref, the field and the value.
	 * @param objectref The objectref the field belongs to.
	 * @param field The Field that has its value to be restored.
	 * @param value The object that has to be pushed onto the operand stack.
	 */
	public InstanceFieldPut(Objectref objectref, Field field, Object value) {
		super(field, value);
		this.objectref = objectref;
	}

	/**
	 * Restore the field. By avoiding to invoke the instruction putfield, there
	 * is no need for a restoring mode that prevents this change from beeing added
	 * to the trail.
	 */
	public void restoreField() {
		this.objectref.putField(this.field, this.value);
	}

}
