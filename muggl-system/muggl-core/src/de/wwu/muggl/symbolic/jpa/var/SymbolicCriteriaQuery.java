package de.wwu.muggl.symbolic.jpa.var;

import de.wwu.muggl.symbolic.jpa.var.meta.JPASpecialType;

public class SymbolicCriteriaQuery extends SymbolicAbstractQuery implements JPASpecialType {
	
	private String entityClassName;

	public String getEntityClassName() {
		return entityClassName;
	}

	public void setEntityClassName(String name) {
		this.entityClassName = name;
	}

}
