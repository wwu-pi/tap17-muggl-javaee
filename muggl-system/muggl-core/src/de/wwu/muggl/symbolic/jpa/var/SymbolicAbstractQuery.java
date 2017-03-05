package de.wwu.muggl.symbolic.jpa.var;

import de.wwu.muggl.symbolic.jpa.var.meta.JPASpecialType;
import de.wwu.muggl.vm.initialization.Objectref;

public class SymbolicAbstractQuery implements JPASpecialType {
	
	private SymbolicSelection selection;

	public SymbolicRoot from(Objectref objectref) {
		SymbolicRoot root = new SymbolicRoot();
		return root;
	}
	
	public SymbolicAbstractQuery select(SymbolicSelection selection) {
		SymbolicAbstractQuery query = new SymbolicAbstractQuery();
		query.selection = selection;
		return query;
	}

}
