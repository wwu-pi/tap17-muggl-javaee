package de.wwu.muggl.symbolic.jpa;

import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;

public class MugglUserTransaction {

	protected JPAVirtualMachine vm;
	
	public MugglUserTransaction(JPAVirtualMachine vm) {
		this.vm = vm;
	}
}
