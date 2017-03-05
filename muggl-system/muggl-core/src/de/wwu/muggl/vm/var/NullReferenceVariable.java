package de.wwu.muggl.vm.var;

import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.ReferenceValue;

public class NullReferenceVariable extends ReferenceVariable {

	public NullReferenceVariable(String name, ReferenceValue referenceValue, JPAVirtualMachine vm) {
		super(name, referenceValue, vm);
	}

}
