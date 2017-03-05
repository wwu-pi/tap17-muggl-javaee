package de.wwu.muggl.instructions.jpa;

import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.ReferenceValue;

public class JPAReferenceWrapper implements ReferenceValue {

	protected Object wrappedObject;
	
	public JPAReferenceWrapper(Object wrappedObject) {
		this.wrappedObject = wrappedObject;
	}
	
	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public String getName() {
		return "JPA Reference Wrapper of: " + this.wrappedObject;
	}

	@Override
	public InitializedClass getInitializedClass() {
		return null;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public long getInstantiationNumber() {
		return 0;
	}

}
