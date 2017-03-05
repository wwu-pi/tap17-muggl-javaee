package de.wwu.muggl.vm.var.sym;

import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class SymbolicQueryResultListArrayref extends SymbolicArrayref {

	protected SymbolicQueryResultList symQryResList;
	
	SymbolicQueryResultListArrayref(InitializedClass initializedClass, String name, SymbolicQueryResultList symQryResList) {
		super(initializedClass, name);
		this.symQryResList = symQryResList;
	}

	public SymbolicQueryResultList getSymbolicResultList() {
		return this.symQryResList;
	}
}
