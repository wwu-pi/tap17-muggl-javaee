package de.wwu.muggl.symbolic.jpa.criteria;

import javax.persistence.criteria.Root;

import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.vm.initialization.Objectref;

public class CriteriaRootWrapper {
	
	protected Root root;

	public CriteriaRootWrapper(Root root) {
		this.root = root;
	}

	public CriteriaPathWrapper get(SymbolicEntityAttribute singularAttributeRef) {
		return new CriteriaPathWrapper(this.root.get(singularAttributeRef.getFieldName()));
	}

	public Root getOriginal() {
		return this.root;
	}
}
