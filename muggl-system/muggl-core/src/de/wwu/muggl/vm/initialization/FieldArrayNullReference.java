package de.wwu.muggl.vm.initialization;

import de.wwu.muggl.vm.classfile.structures.Field;

public class FieldArrayNullReference extends Arrayref {

	protected Field field;

	public FieldArrayNullReference(Field field, ReferenceValue referenceValue, int length) {
		super(referenceValue, length);
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
