package de.wwu.muggl.vm.initialization;

public class ObjectNullRef extends Objectref {

	public ObjectNullRef(InitializedClass staticReference) {
		super(staticReference, false);
	}
	
	@Override
	public String toString() {
		return "NULL-REFERENCE of: " + super.toString();
	}
}
