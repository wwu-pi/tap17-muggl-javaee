package de.wwu.muggl.jpa;

import de.wwu.muggl.vm.initialization.Objectref;

@Deprecated
public class FindResult extends JPAEntityType {

	public FindResult(Objectref original) {
		super(original);
	}
	
	@Override
	public String toString() {
		String referenceString = super.toString();
		return "Find Result as " + referenceString;
	}
}
