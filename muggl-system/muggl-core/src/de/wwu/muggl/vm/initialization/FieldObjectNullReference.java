package de.wwu.muggl.vm.initialization;

import de.wwu.muggl.vm.classfile.structures.Field;

/**
 * This class represents a null-reference for a field in a class.
 */
public class FieldObjectNullReference extends Objectref {

	protected Field field;
	
	public FieldObjectNullReference(Field field, InitializedClass staticReference, boolean primitiveWrapper) {
		super(staticReference, primitiveWrapper);
		this.field = field;
	}
	
	public Field getField() {
		return this.field;
	}

	@Override
	public String getName() {
		return field.getClassFile() + "." + field.getName();
	}
	
	@Override
	public String toString() {
		return field.getClassFile() + "." + field.getName() + " -> NullReference";
	}
}
